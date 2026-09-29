package io.github.rwx.slick

import com.corrodinggames.rts.gameFramework.GameEngine
import com.corrodinggames.rts.gameFramework.PlatformCallbacks
import com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
import io.github.rwx.DesktopRenderBackend
import io.github.rwx.PlatformStorage
import io.github.rwx.logger
import io.github.rwx.platform.CoreGameView
import io.github.rwx.render.RenderBackend
import io.github.rwx.render.frame.*
import io.github.rwx.session.*
import io.github.rwx.ui.CoreUiEventQueue
import java.awt.Canvas
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.swing.SwingUtilities

class SlickGameSession(
    private val storage: PlatformStorage,
    registerShutdownHook: Boolean = true,
) : GameSession() {
    override val renderBackend: RenderBackend = DesktopRenderBackend.Slick
    private val running = AtomicBoolean(false)
    private val requestState = SlickSessionRequestState()
    private val stopRequested = AtomicBoolean(false)

    private val loadingStatusTracker = SlickLoadingStatusTracker()
    private val textInputBridge = SlickTextInputBridge()

    /**
     * Wakes [ensureStarted] waiters whenever engine-startup state may have changed. Waiters grab
     * the current future *before* re-checking state, then wait on it, so a signal can never be
     * lost; [signalEngineStartup] swaps in a fresh future and completes the old one.
     */
    private val engineStartupWaiters = AtomicReference(CompletableFuture<Unit>())
    @Volatile
    private var game: SlickGame? = null
    @Volatile
    private var container: EmbeddedSlickGameContainer? = null
    @Volatile
    private var renderThread: Thread? = null

    fun activeGame(): SlickGame? = if (running.get()) game else null

    @Volatile
    private var lastSlickViewport: GameViewport = GameViewport(1280, 720)

    private val frameSnapshotState =
        AtomicReference<SlickFrameSnapshot?>(null)
    @Volatile
    private var rendererStartupError: Throwable? = null

    @Volatile
    private var gameVisible: Boolean = false

    @Volatile
    private var pausedBackground: Boolean = false

    init {
        SlickCanvasHost.setResizeController(::resizeFromCanvasHost)
        SlickCanvasHost.setRendererShutdown(::stopRendererAndWait)
        configureRendererProfile(
            GameSessionRendererProfile(
                usesFrameCommandRendering = false,
                canStartNewSessionInPlace = true,
                usesNativeSurfaceForResumeBackground = true,
            )
        )
        if (registerShutdownHook) {
            Runtime.getRuntime().addShutdownHook(
                Thread({
                    stop()
                }, "RWX-slick-embedded-shutdown")
            )
        }
    }

    fun start(
        canvas: Canvas,
        request: SlickSessionRequest,
        viewport: GameViewport,
        visible: Boolean,
        pausedBackground: Boolean,
    ): SlickGame {
        val width = viewport.width.coerceAtLeast(320)
        val height = viewport.height.coerceAtLeast(240)
        // `running` is the single-starter gate: exactly one caller flips it and owns the startup
        // sequence below. A losing caller waits out the winner's tiny publish window instead of
        // failing spuriously; only a genuinely stopping runtime raises the error.
        if (!running.compareAndSet(false, true)) {
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
            while (System.nanoTime() < deadline) {
                game?.let { return it }
                if (stopRequested.get() || !running.get()) break
                Thread.sleep(10)
            }
            return game ?: error("Slick runtime is stopping")
        }

        try {
            stopRequested.set(false)
            configureLegacyDesktopPlatform()
            storage.createDirectories()
            val createdGame = createGame(request, width, height).also { game = it }

            Thread({
                var containerToDestroy: EmbeddedSlickGameContainer? = null
                try {
                    configureLwjgl2Natives()
                    waitForDisplayableCanvas(canvas)
                    if (stopRequested.get()) return@Thread
                    val displayWidth = canvas.width.takeIf { it > 0 } ?: width
                    val displayHeight = canvas.height.takeIf { it > 0 } ?: height
                    createdGame.requestSize(displayWidth, displayHeight)
                    val createdContainer = EmbeddedSlickGameContainer(
                        createdGame,
                        displayWidth.coerceAtLeast(320),
                        displayHeight.coerceAtLeast(240),
                        false,
                        parentCanvas = canvas,
                        onParentCanvasResize = createdGame::requestSize,
                    )
                    containerToDestroy = createdContainer
                    container = createdContainer
                    createdContainer.setDisplayMode(
                        displayWidth.coerceAtLeast(320),
                        displayHeight.coerceAtLeast(240),
                        false,
                    )
                    createdContainer.setForceExit(false)
                    createdContainer.setAlwaysRender(true)
                    createdContainer.setUpdateOnlyWhenVisible(false)
                    createdContainer.setShowFPS(false)
                    createdContainer.setTargetFrameRate(300)
                    SlickCanvasHost.requestGameFocus()
                    if (stopRequested.get()) {
                        createdContainer.requestExitFromAnyThread()
                    }
                    createdContainer.start()
                } catch (error: Throwable) {
                    handleRendererStartupError(error)
                    handleMapError(request.loadingPath.orEmpty(), error)
                } finally {
                    // Linux JAWT surfaces retain their creator thread's JNIEnv, so release before this thread exits.
                    runCatching { containerToDestroy?.destroy() }
                        .onFailure { error -> logger.warn(error) { "Failed to destroy the embedded Slick container" } }
                    container = null
                    game = null
                    renderThread = null
                    stopRequested.set(false)
                    running.set(false)
                    handleRendererStopped()
                }
            }, RENDER_THREAD_NAME).apply {
                isDaemon = true
                renderThread = this
                start()
            }

            createdGame.setGameVisible(visible, pausedBackground)
            return createdGame
        } catch (error: Throwable) {
            game = null
            container = null
            renderThread = null
            stopRequested.set(false)
            running.set(false)
            throw error
        }
    }

    fun stop() {
        stopRequested.set(true)
        game = null
        val containerToStop = container
        container = null
        containerToStop?.requestExitFromAnyThread()
    }
    fun stopAndWait(timeoutMillis: Long) {
        val thread = renderThread
        stop()
        if (thread == null || thread === Thread.currentThread() || !thread.isAlive) return
        require(timeoutMillis > 0) { "Renderer join timeout must be positive" }
        thread.join(timeoutMillis)
        check(!thread.isAlive) { "Slick render thread did not exit within ${timeoutMillis}ms; native canvas must remain alive" }
    }
    private fun createGame(request: SlickSessionRequest, width: Int, height: Int): SlickGame =
        SlickGame(
            graphicsEngine = SlickGraphicsEngine(storage),
            gameSession = this,
            initialWidth = width,
            initialHeight = height,
            initialRequest = request,
            onTextInputRequest = textInputBridge::deliver,
            onMapReady = ::handleMapReady,
            onMapError = ::handleMapError,
            onLoadingStatus = ::handleLoadingStatus,
        )
    private fun configureLwjgl2Natives() {
        System.setProperty("org.lwjgl.opengl.contextAPI", "native")
        val nativesDir = System.getProperty("rwx.slick.nativesDir")
            ?: System.getenv("RWX_SLICK_NATIVES_DIR")
            ?: return
        System.setProperty("org.lwjgl.librarypath", nativesDir)
        System.setProperty("java.library.path", nativesDir)
    }

    private fun waitForDisplayableCanvas(canvas: Canvas) {
        val deadline = System.nanoTime() + CANVAS_DISPLAY_TIMEOUT_NANOS
        while (System.nanoTime() < deadline) {
            var ready = false
            runCatching {
                if (SwingUtilities.isEventDispatchThread()) {
                    ready = canvas.isDisplayable && canvas.isShowing && canvas.width > 0 && canvas.height > 0
                } else {
                    SwingUtilities.invokeAndWait {
                        canvas.addNotify()
                        canvas.requestFocusInWindow()
                        ready = canvas.isDisplayable && canvas.isShowing && canvas.width > 0 && canvas.height > 0
                    }
                }
            }
            if (ready) return
            Thread.sleep(CANVAS_RETRY_MILLIS)
        }
        check(canvas.isDisplayable && canvas.width > 0 && canvas.height > 0) {
            "Slick parent canvas was not displayable"
        }
    }
    override fun ensureRendererEngine(
        viewport: GameViewport,
        graphicsEngine: GraphicsEngine,
        view: CoreGameView,
        platformCallbacks: PlatformCallbacks?,
    ): GameEngine =
        runCatching {
            super.ensureRendererEngine(viewport, graphicsEngine, view, platformCallbacks)
        }.onSuccess {
            rendererStartupError = null
            requestState.clearRequestIf { it.desired is SlickSessionRequest.EnginePreparation }
            signalEngineStartup()
        }.onFailure { error ->
            rendererStartupError = error
            signalEngineStartup()
        }.getOrThrow()

    override fun prepareMapAsync(mapPath: String?, viewport: GameViewport) {
        val requestedMapPath = mapPath?.takeIf { it.isNotBlank() } ?: return
        if (isMapLoaded(requestedMapPath)) return
        beginRendererMapPreparation(requestedMapPath)
        prepareRendererRequest(
            viewport,
            SlickSessionRequest.Map(requestedMapPath),
        )
        startSlickGameIfNeeded()
    }

    override fun prepareSavedGameAsync(saveName: String, viewport: GameViewport) {
        val requestedSaveName = saveName.takeIf { it.isNotBlank() } ?: return
        if (isMapLoaded(requestedSaveName)) return
        beginRendererMapPreparation(requestedSaveName)
        prepareRendererRequest(
            viewport,
            SlickSessionRequest.SavedGame(requestedSaveName),
        )
        startSlickGameIfNeeded()
    }

    override fun prepareMapSnapshotAsync(snapshot: MapSnapshot, viewport: GameViewport) {
        if (isMapLoaded(snapshot.mapPath)) return
        beginRendererMapPreparation(
            mapPath = snapshot.mapPath,
            mapSnapshot = snapshot,
            battleRoomConfig = snapshot.battleRoomConfig,
        )
        prepareRendererRequest(
            viewport,
            SlickSessionRequest.MapSnapshotRequest(snapshot),
        )
        startSlickGameIfNeeded()
    }

    override fun prepareBattleRoomAsync(config: BattleRoomLaunchConfig, viewport: GameViewport) {
        val requestedMapPath = config.room.mapPath.takeIf { it.isNotBlank() } ?: return
        if (isMapLoaded(requestedMapPath) && activeRendererBattleRoomConfig() == config) return
        beginRendererMapPreparation(
            mapPath = requestedMapPath,
            battleRoomConfig = config,
        )
        prepareRendererRequest(
            viewport,
            SlickSessionRequest.BattleRoom(config),
        )
        startSlickGameIfNeeded()
    }

   override fun updateFrame(
        viewport: GameViewport,
        deltaSeconds: Float,
        drainVisibleLayerBuffers: Boolean,
    ): GameFrame {
       lastSlickViewport = normalizedViewport(viewport)
       lastViewport = lastSlickViewport
       syncRequestedMapFromSession()
        startSlickGameIfNeeded()
        pollSlickFrameSnapshot()
        activeGame()?.let(textInputBridge::poll)
        return currentFrame()
    }

    // Pixels belong to Slick, not to a cross-renderer texture registry.
    internal fun currentSnapshot(): SlickFrameSnapshot? = activeGame()?.currentFrameSnapshot() ?: frameSnapshotState.get()

    override fun currentFrame(): GameFrame = emptyFrameFor(lastSlickViewport)

    override fun loadPendingMapNow(): GameFrame {
        syncRequestedMapFromSession()
        startSlickGameIfNeeded()
        pollSlickFrameSnapshot()
        return currentFrame()
    }

    protected override fun ensureStarted(viewport: GameViewport): GameEngine {
        synchronized(gameLock) {
            activeEngineLocked()?.let { return it }
        }
        val requestedViewport = normalizedViewport(viewport)
        lastSlickViewport = requestedViewport
        lastViewport = requestedViewport
        requestState.replaceIfEmpty(SlickSessionRequest.EnginePreparation)
        rendererStartupError = null

        val canvasDeadline = deadlineAfterMillis(SLICK_CANVAS_INSTALL_TIMEOUT_MILLIS)
        while (SlickCanvasHost.gameCanvas() == null && System.nanoTime() < canvasDeadline) {
            Thread.sleep(SLICK_ENGINE_START_POLL_MILLIS)
        }
        val canvas = checkNotNull(SlickCanvasHost.gameCanvas()) {
            "Slick game canvas was not installed within ${SLICK_CANVAS_INSTALL_TIMEOUT_MILLIS}ms"
        }
        val exposedCanvasForStartup = !canvas.isShowing
        if (exposedCanvasForStartup) {
            // AWTGLCanvas cannot create its GL context while hidden. Keep the in-frame loading UI
            // above it so engine preloading can initialize the native renderer without a UI flash.
            SlickCanvasHost.setGameVisible(visible = true, overlay = true)
        }

        try {
            startSlickGameIfNeeded()
            val engineDeadline = deadlineAfterMillis(SLICK_ENGINE_INITIALIZATION_TIMEOUT_MILLIS)
            while (true) {
                // Grab the waiter before re-checking state so a startup signal that fires between
                // the check and the wait completes this future rather than being lost.
                val waiter = engineStartupWaiters.get()
                synchronized(gameLock) {
                    activeEngineLocked()?.let { return it }
                }
                rendererStartupError?.let { error ->
                    throw IllegalStateException("Unable to start the Slick renderer", error)
                }
                val remainingNanos = engineDeadline - System.nanoTime()
                check(remainingNanos > 0L) {
                    "Slick renderer did not initialize within ${SLICK_ENGINE_INITIALIZATION_TIMEOUT_MILLIS}ms"
                }
                try {
                    waiter.get(remainingNanos, TimeUnit.NANOSECONDS)
                } catch (_: TimeoutException) {
                    // Loop re-evaluates the deadline and fails with the message above.
                }
            }
        } finally {
            if (exposedCanvasForStartup && !gameVisible) {
                SlickCanvasHost.setGameVisible(visible = false)
            }
        }
    }

    protected override fun applyViewport(engine: GameEngine, viewport: GameViewport) {
        val requestedViewport = normalizedViewport(viewport)
        lastSlickViewport = requestedViewport
        lastViewport = requestedViewport
        val activeGame = activeGame()
        val (gameWidth, gameHeight) = slickGameLogicalSize(requestedViewport)
        activeGame?.requestSize(
            gameWidth.coerceAtLeast(320),
            gameHeight.coerceAtLeast(240),
        )
    }

    override fun discardRunningGame() {
        super.discardRunningGame()
        stopRenderer()
    }

    override fun loadingStatus(): GameLoadingStatus {
        return loadingStatusTracker.mergeWith(super.loadingStatus())
    }

    override suspend fun requestReloadMods(): Boolean {
        val activeCanvas = SlickCanvasHost.gameCanvas()
        val exposedCanvasForReload = activeCanvas != null && !activeCanvas.isShowing
        val activeGame = activeGame() ?: return false
        // The render thread performs the reload; the flag keeps Compose UI reads off the engine
        // (and off gameLock) while it runs.
        modReloadInProgress = true
        val request = activeGame.modReloadQueue.request()
        if (exposedCanvasForReload) {
            SlickCanvasHost.setGameVisible(visible = true, overlay = true)
        }
        try {
            request.await()
        } finally {
            modReloadInProgress = false
            if (!request.isCompleted) {
                activeGame.modReloadQueue.cancelPending(request)
            }
            if (exposedCanvasForReload && !gameVisible) {
                SlickCanvasHost.setGameVisible(visible = false)
            }
        }
        return true
    }

    // The Slick render thread owns the engine while it is alive and never takes gameLock, so the
    // base class's inline-under-gameLock execution would race gameLoop(). Hand every engine
    // command to that thread instead; fall back to the base path only when the runtime is not
    // running (no owner to race) or when we are already on the render thread.

    override fun <T> runEngineCommand(label: String, command: (GameEngine) -> T?): T? {
        val activeGame = routedEngineCommandTarget() ?: return super.runEngineCommand(label, command)
        return activeGame.awaitEngineCommand(label, ENGINE_COMMAND_TIMEOUT_MILLIS, command)
    }

    override fun postEngineCommand(label: String, command: (GameEngine) -> Unit) {
        val activeGame = routedEngineCommandTarget()
        if (activeGame == null) {
            super.postEngineCommand(label, command)
        } else {
            activeGame.postEngineCommand(label, command)
        }
    }

    private fun routedEngineCommandTarget(): SlickGame? {
        // Re-entrant call from render-thread code must run inline or it would deadlock waiting on
        // its own drain.
        if (Thread.currentThread() === renderThread) return null
        return activeGame()
    }


    private fun stopRenderer() {
        requestState.clearRequest()
        rendererStartupError = IllegalStateException("Slick renderer stopped before startup completed")
        gameVisible = false
        pausedBackground = false
        clearFrameSnapshot()
        markRendererSurfaceStopped()
        stop()
        signalEngineStartup()
    }

    fun stopRendererAndWait(timeoutMillis: Long = RENDER_THREAD_JOIN_TIMEOUT_MILLIS) {
        requestState.clearRequest()
        rendererStartupError = IllegalStateException("Slick renderer stopped before startup completed")
        gameVisible = false
        pausedBackground = false
        clearFrameSnapshot()
        markRendererSurfaceStopped()
        signalEngineStartup()
        stopAndWait(timeoutMillis)
    }

    override fun setGameVisible(
        visible: Boolean,
        viewport: GameViewport,
        uiOverlay: Boolean,
        pausedBackground: Boolean,
    ) {
        val shouldCaptureExitFrame = gameVisible && !visible
        lastSlickViewport = normalizedViewport(viewport)
        lastViewport = lastSlickViewport
        gameVisible = visible
        this.pausedBackground = pausedBackground
        syncRequestedMapFromSession()
        val activeGame = activeGame()

        if (shouldCaptureExitFrame && pausedBackground) {
            // Prime the pending snapshot with a world-only render. The regular game loop must
            // remain stopped while a paused/resume background is captured.
            activeGame?.setGameVisible(true, pausedBackground = true)
        }
        if (shouldCaptureExitFrame) {
            activeGame?.requestFrameSnapshot(EXIT_FRAME_SNAPSHOT_TIMEOUT_MILLIS)?.let { snapshot ->
                updateFrameSnapshot(snapshot)
            }
        }
        SlickCanvasHost.setGameVisible(visible, uiOverlay)
        activeGame?.setGameVisible(visible, pausedBackground)
        if (visible && !uiOverlay) {
            SlickCanvasHost.requestGameFocus()
        }
        if (visible) {
            startSlickGameIfNeeded()
        }
    }

    override fun submitPointer(
        screenX: Float,
        screenY: Float,
        isDown: Boolean,
        pointerId: Int,
    ) {
        activeGame()?.submitOverlayPointer(screenX, screenY, isDown, pointerId)
    }

    override fun movePointer(screenX: Float, screenY: Float) {
        activeGame()?.moveOverlayPointer(screenX, screenY)
    }

    override fun submitMouseWheel(amount: Int) {
        if (amount != 0) activeGame()?.submitOverlayMouseWheel(amount)
    }

    override fun adoptStartedGameFromEngine(viewport: GameViewport): Boolean {
        val mapPath = synchronized(gameLock) {
            val engine = activeEngineLocked() ?: return@synchronized null
            val networkEngine = engine.networkEngine ?: return@synchronized null
            if (!networkEngine.hasActiveStartedGameConnection()) return@synchronized null
            networkEngine.selectedMapPath?.takeIf { it.isNotBlank() }
                ?: engine.currentMapPath?.takeIf { it.isNotBlank() }
        } ?: return false
        val normalizedViewport = normalizedViewport(viewport)
        val canLaunch = activeGame() != null || SlickCanvasHost.gameCanvas() != null
        if (!canLaunch) {
            logger.warn { "Unable to adopt started Slick game without an installed canvas: $mapPath" }
            return false
        }

        lastSlickViewport = normalizedViewport
        lastViewport = normalizedViewport
        requestState.replace(SlickSessionRequest.StartedGame(mapPath))
        clearFrameSnapshot()
        beginRendererStartedGamePreparation(mapPath)
        logger.info { "Adopting started battle room in embedded Slick renderer: $mapPath" }
        startSlickGameIfNeeded()
        return true
    }

    override fun prepareMenuBackgroundAsync(viewport: GameViewport) {
        if (requestState.desired is SlickSessionRequest.Replay) {
            return
        }
        if (loadState.menuBackgroundActive || requestState.desired is SlickSessionRequest.MenuBackground) {
            return
        }
        beginRendererStandalonePreparation(MENU_BACKGROUND_REQUEST)
        prepareRendererRequest(viewport, SlickSessionRequest.MenuBackground)
        startSlickGameIfNeeded()
    }

    override fun isMenuBackgroundActive(): Boolean = loadState.menuBackgroundActive

    override fun prepareReplayAsync(replayName: String, viewport: GameViewport) {
        val requestedReplayName = replayName.takeIf { it.isNotBlank() } ?: return
        prepareRendererRequest(
            viewport,
            SlickSessionRequest.Replay(requestedReplayName),
        )
        beginRendererStandalonePreparation(requestedReplayName)
        startSlickGameIfNeeded()
    }

    private fun prepareRendererRequest(
        viewport: GameViewport,
        request: SlickSessionRequest,
    ) {
        val requestedViewport = normalizedViewport(viewport)
        lastSlickViewport = requestedViewport
        lastViewport = requestedViewport
        requestState.replace(request)
        rendererStartupError = null
        loadingStatusTracker.reset()
        clearFrameSnapshot()
    }

    private fun signalEngineStartup() {
        engineStartupWaiters.getAndSet(CompletableFuture()).complete(Unit)
    }

    private fun resizeFromCanvasHost(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        val device = SlickCanvasHost.gameCanvas()?.deviceSizeFor(width, height)
        lastSlickViewport = if (device != null) {
            GameViewport(device.width, device.height)
        } else {
            GameViewport(width, height)
        }
        lastViewport = lastSlickViewport
        val activeGame = activeGame()
        activeGame?.requestSize(width.coerceAtLeast(320), height.coerceAtLeast(240))
    }

    private fun syncRequestedMapFromSession() {
        val preparation = rendererPreparationSnapshot()
        val pendingMapSnapshot = preparation.mapSnapshot
        val pendingBattleRoomConfig = preparation.battleRoomConfig
        val pendingMapPath = preparation.mapPath
        if (pendingMapSnapshot == null && pendingMapPath == null) {
            val desired = requestState.desired
            val preservesStandaloneRequest = desired is SlickSessionRequest.MenuBackground ||
                    desired is SlickSessionRequest.Replay ||
                    desired is SlickSessionRequest.SavedGame ||
                    desired is SlickSessionRequest.StartedGame
            if (preservesStandaloneRequest) {
                return
            }
            val activeMapPath = runningMapPath()
                ?.takeIf { it.isNotBlank() && isMapLoaded(it) }
                ?: return
            requestState.synchronize(SlickSessionRequest.Map(activeMapPath))
            return
        }
        val sessionMapPath = pendingMapSnapshot?.mapPath
            ?: pendingMapPath
            ?: return
        val desiredRequest = requestState.desired
        val request = when {
            pendingMapSnapshot != null -> SlickSessionRequest.MapSnapshotRequest(pendingMapSnapshot)
            pendingBattleRoomConfig != null -> SlickSessionRequest.BattleRoom(pendingBattleRoomConfig)
            desiredRequest is SlickSessionRequest.SavedGame &&
                    desiredRequest.saveName == sessionMapPath -> desiredRequest
            else -> SlickSessionRequest.Map(sessionMapPath)
        }
        if (requestState.synchronize(request)) {
            clearFrameSnapshot()
        }
    }

    private fun pollSlickFrameSnapshot() {
        val snapshot = activeGame()?.currentFrameSnapshot() ?: return
        updateFrameSnapshot(snapshot)
    }

    private fun startSlickGameIfNeeded() {
        val request = requestState.desired ?: return
        val shouldShowGame = gameVisible
        val shouldPauseBackground = pausedBackground
        val viewport = lastSlickViewport
        val existingGame = activeGame()
        val requestToDispatch = if (existingGame != null || !running.get()) {
            requestState.nextDispatch()
        } else {
            null
        }
        if (existingGame != null) {
            val (gameWidth, gameHeight) = slickGameLogicalSize(viewport)
            existingGame.requestSize(gameWidth.coerceAtLeast(320), gameHeight.coerceAtLeast(240))
            requestToDispatch?.let(existingGame::submit)
            return
        }
        if (running.get()) {
            return
        }

        val canvas = SlickCanvasHost.gameCanvas()
        if (canvas == null) {
            requestState.runtimeStopped()
            logger.warn { "Slick game canvas is not installed" }
            return
        }
        runCatching {
            SlickNativeRuntimeEnvironment.nativesDir()
            start(
                canvas = canvas,
                request = request,
                viewport = viewport,
                visible = shouldShowGame,
                pausedBackground = shouldPauseBackground,
            )
        }.onFailure { error ->
            requestState.runtimeStopped()
            handleRendererStartupError(error)
            logger.error(error) { "Failed to launch embedded Slick backend" }
        }
    }

    private fun handleMapReady(mapPath: String) {
        val menuBackgroundReady = requestState.dispatched is SlickSessionRequest.MenuBackground
        val viewport = lastSlickViewport
        if (menuBackgroundReady) {
            markRendererMenuBackgroundReady()
            CoreUiEventQueue.requestMenuBackgroundReady()
            logger.info { "Embedded Slick menu background ready: $mapPath" }
            return
        }
        markRendererMapReady(mapPath, viewport)
        logger.info { "Embedded Slick map ready: $mapPath" }
        println("RWX_SLICK_MAP_READY:$mapPath")
        System.out.flush()
    }

    private fun handleLoadingStatus(text: String) {
        loadingStatusTracker.update(text)
    }

    private fun handleMapError(mapPath: String, error: Throwable) {
        val dispatchedRequest = requestState.dispatched
        val failedMapPath = mapPath.takeIf { it.isNotBlank() } ?: dispatchedRequest?.loadingPath
        val failedMenuBackground = dispatchedRequest is SlickSessionRequest.MenuBackground
        if (failedMenuBackground || failedMapPath == null || dispatchedRequest?.loadingPath == failedMapPath) {
            // Only drop the request that actually failed: a newer request that replaced it in the
            // meantime must survive this error.
            requestState.clearRequestIf { it.dispatched == dispatchedRequest }
        }
        if (failedMenuBackground) {
            markRendererMenuBackgroundError(MENU_BACKGROUND_REQUEST, error)
        } else if (failedMapPath != null) {
            markRendererMapError(failedMapPath, error)
        }
        logger.error(error) { "Embedded Slick map load failed: ${mapPath.ifBlank { "<unknown>" }}" }
    }

    private fun handleRendererStartupError(error: Throwable) {
        rendererStartupError = error
        signalEngineStartup()
    }

    private fun handleRendererStopped() {
        if (activeGame() == null) {
            requestState.runtimeStopped()
        }
    }

    private fun normalizedViewport(viewport: GameViewport): GameViewport {
        if (viewport.width > 0 && viewport.height > 0) {
            return viewport
        }
        val logical = SlickCanvasHost.gameCanvasSize()?.takeIf { it.width > 0 && it.height > 0 }
        val canvas = SlickCanvasHost.gameCanvas()
        if (canvas != null && logical != null) {
            val device = canvas.deviceSizeFor(logical.width, logical.height)
            return GameViewport(device.width, device.height)
        }
        return GameViewport(
            width = viewport.width.takeIf { it > 0 } ?: 1280,
            height = viewport.height.takeIf { it > 0 } ?: 720,
        )
    }

    private fun slickGameLogicalSize(deviceViewport: GameViewport): Pair<Int, Int> {
        val logical = SlickCanvasHost.gameCanvasSize()?.takeIf { it.width > 0 && it.height > 0 }
        if (logical != null) {
            return logical.width to logical.height
        }
        return deviceViewport.width to deviceViewport.height
    }

    private fun updateFrameSnapshot(snapshot: SlickFrameSnapshot) {
        if (snapshot.width <= 0 || snapshot.height <= 0 || snapshot.pixels.size < snapshot.width * snapshot.height) return
        frameSnapshotState.set(snapshot)
    }

    private fun clearFrameSnapshot() {
        frameSnapshotState.set(null)
    }
    companion object{
        const val RENDER_THREAD_NAME = "RWX-slick-embedded"
        const val CANVAS_DISPLAY_TIMEOUT_NANOS = 5_000_000_000L
        const val CANVAS_RETRY_MILLIS = 16L
    }

}

private const val MENU_BACKGROUND_REQUEST = "<menu-background>"
private const val ENGINE_COMMAND_TIMEOUT_MILLIS = 2_000L
private const val EXIT_FRAME_SNAPSHOT_TIMEOUT_MILLIS = 150L
private const val RENDER_THREAD_JOIN_TIMEOUT_MILLIS = 2_000L
private const val SLICK_CANVAS_INSTALL_TIMEOUT_MILLIS = 15_000L
private const val SLICK_ENGINE_INITIALIZATION_TIMEOUT_MILLIS = 60_000L
private const val SLICK_ENGINE_START_POLL_MILLIS = 50L
private fun deadlineAfterMillis(timeoutMillis: Long): Long =
    System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis)


private fun emptyFrameFor(viewport: GameViewport): GameFrame =
    GameFrame(viewport, emptyList())
