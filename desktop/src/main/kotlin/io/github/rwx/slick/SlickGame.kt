package io.github.rwx.slick

import com.corrodinggames.rts.R
import com.corrodinggames.rts.game.GameLogic
import com.corrodinggames.rts.game.map.LayerBufferManager
import com.corrodinggames.rts.game.map.TileMap
import com.corrodinggames.rts.gameFramework.GameEngine
import com.corrodinggames.rts.gameFramework.GameMode
import com.corrodinggames.rts.gameFramework.InputController
import com.corrodinggames.rts.gameFramework.PlatformCallbacks
import com.corrodinggames.rts.gameFramework.network.GameInputStream
import com.corrodinggames.rts.gameFramework.network.GameModeType
import com.corrodinggames.rts.gameFramework.network.PasswordHandler
import com.corrodinggames.rts.gameFramework.utility.SlickToAndroidKeycodes
import io.github.rwx.DesktopInputHandler
import io.github.rwx.ensureDesktopOpenAlMusicFactory
import io.github.rwx.bench.BenchFrameProbe
import io.github.rwx.geometry.Point
import io.github.rwx.input.MultiTouchPointerState
import io.github.rwx.logger
import io.github.rwx.net.CoreUiNetworkCallbacks
import io.github.rwx.platform.CoreGameView
import io.github.rwx.render.frame.GameViewport
import com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
import io.github.rwx.session.*
import io.github.rwx.ui.InGameMenuController
import kotlinx.coroutines.CompletableDeferred
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.newdawn.slick.BasicGame
import org.newdawn.slick.GameContainer
import org.newdawn.slick.Graphics
import org.newdawn.slick.Input
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

internal fun configureLegacyDesktopPlatform() {
    GameEngine.isMenuBackgroundDisabled = true
    GameEngine.isNonAndroidVersion = true
    GameEngine.isDesktopInitialized = true
    GameEngine.isJavaDesktopVersion = true
    GameEngine.isPCOrIOSVersion = true
    InputController.b = DesktopInputHandler()
    ensureDesktopOpenAlMusicFactory()
}

internal class SlickLoadingCallbacks(
    private val onLoadingStatus: (String) -> Unit,
) : PlatformCallbacks() {
    override fun a(str: String, i: Int) = Unit

    override fun a(str: String, str2: String) = Unit

    override fun a(str: String, z: Boolean) {
        onLoadingStatus(str)
    }

    override fun a(th: Throwable) = Unit
}

object SlickGraphicsBinding {
    @Volatile
    private var currentGraphicsEngine: SlickGraphicsEngine? = null

    @JvmStatic
    fun current(): SlickGraphicsEngine? = currentGraphicsEngine

    fun bind(graphicsEngine: SlickGraphicsEngine) {
        currentGraphicsEngine = graphicsEngine
    }
}

private const val SLICK_LAYER_REDRAW_BUDGET_MS = 2
private val SLICK_TARGET_FPS_OVERRIDE: Int? =
    System.getenv("RWX_SLICK_TARGET_FPS")?.toIntOrNull()?.takeIf { it > 0 }

internal fun shouldDriveSlickGameLoop(
    hasPendingLoad: Boolean,
    hasLoadedLevel: Boolean,
    hasRunningMap: Boolean,
    gameVisible: Boolean,
    runningMenuBackground: Boolean,
    networkGameStarted: Boolean,
): Boolean = hasPendingLoad ||
        (hasLoadedLevel &&
            (runningMenuBackground || networkGameStarted || (gameVisible && hasRunningMap)))

internal fun shouldRenderPausedSlickFrame(
    pausedBackground: Boolean,
    gameSpeed: Float,
    hasLoadedLevel: Boolean,
    gameVisible: Boolean,
    runningMenuBackground: Boolean,
    networkMultiplayerActive: Boolean,
    enginePaused: Boolean = false,
): Boolean = hasLoadedLevel &&
        gameVisible &&
        !runningMenuBackground &&
        !networkMultiplayerActive &&
        (pausedBackground || gameSpeed == 0f || enginePaused)

internal fun shouldCountSlickReadyFrame(
    hasLoadedLevel: Boolean,
    runningMapPath: String?,
    readyNotifiedMapPath: String?,
): Boolean = hasLoadedLevel &&
        runningMapPath != null &&
        readyNotifiedMapPath != runningMapPath

internal fun shouldAbortStartedGameAfterDisconnect(
    setupComplete: Boolean,
    hasLoadedLevel: Boolean,
    hasActiveConnection: Boolean,
    renderedFrames: Int,
): Boolean = setupComplete &&
        !hasLoadedLevel &&
        !hasActiveConnection &&
        renderedFrames == 0

internal fun shouldRenderSlickBackgroundLayerRedraw(gameLoopDriven: Boolean): Boolean =
    !gameLoopDriven

internal class SlickFrameDelta {
    private var pendingMillis: Int = 0

    fun update(deltaMillis: Int) {
        pendingMillis = deltaMillis.coerceAtLeast(0)
    }

    fun consumeMillis(): Int = pendingMillis.also { pendingMillis = 0 }
}

class SlickModReloadQueue {
    private val pendingRequest = AtomicReference<CompletableDeferred<Unit>?>(null)

    fun request(): CompletableDeferred<Unit> {
        while (true) {
            pendingRequest.get()?.let { return it }
            val request = CompletableDeferred<Unit>()
            if (pendingRequest.compareAndSet(null, request)) {
                return request
            }
        }
    }

    fun cancelPending(request: CompletableDeferred<Unit>): Boolean {
        if (!pendingRequest.compareAndSet(request, null)) {
            return false
        }
        request.cancel()
        return true
    }

    fun takePending(): CompletableDeferred<Unit>? = pendingRequest.getAndSet(null)
}

private data class CompletedModReload(
    val request: CompletableDeferred<Unit>,
    val error: Throwable?,
)

private data class PreparedModReload(
    val request: CompletableDeferred<Unit>,
    val graphicsEngine: GraphicsEngine,
)

internal fun legacySlickDeltaSpeed(deltaMillis: Int): Float = deltaMillis * 0.060000002f

data class SlickTextInputRequest(
    val id: Int,
    val title: String,
    val prompt: String,
    val confirmButtonLabel: String,
    val cancelButtonLabel: String,
)

class SlickGame(
    private val graphicsEngine: SlickGraphicsEngine,
    private val gameSession: GameSession,
    private val initialWidth: Int,
    private val initialHeight: Int,
    initialRequest: SlickSessionRequest? = null,
    private val noFog: Boolean = System.getenv("RWX_SLICK_NO_FOG") == "1",
    private val onTextInputRequest: (SlickGame, SlickTextInputRequest) -> Boolean = { _, _ -> false },
    private val onMapReady: (String) -> Unit = { mapPath ->
        logger.info { "Map($mapPath) is ready" }
    },
    private val onMapError: (String, Throwable) -> Unit = { mapPath, error ->
        logger.error(error) {  "Failed to load map:$mapPath"}
    },
    private val onLoadingStatus: (String) -> Unit = {},
) : BasicGame("RWX") {
    private val view = SlickCoreGameView(gameSession.inGameMenuController)
    private val debugSlickMenuInput = System.getenv("RWX_DEBUG_SLICK_MENU") == "1"
    private val keyStates = BooleanArray(SLICK_KEY_COUNT)
    private var engine: GameEngine? = null

    /**
     * The one thing this renderer has been asked to show but has not loaded yet.
     *
     * Single-valued on purpose: the previous six parallel `pending*` fields encoded a closed set of
     * mutually-exclusive states, so every request had to null out the other five by hand.
     */
    @Volatile
    private var pendingRequest: SlickSessionRequest? = initialRequest

    @Volatile
    private var runningMapPath: String? = null

    @Volatile
    private var runningMenuBackground: Boolean = false

    @Volatile
    private var startedBattleRoomGameSetupComplete: Boolean = false

    @Volatile
    private var gameVisible: Boolean = true

    @Volatile
    private var pausedBackground: Boolean = false
    private var readyNotifiedMapPath: String? = null
    private var renderedFramesForRunningMap: Int = 0

    @Volatile
    private var pendingWidth: Int = initialWidth

    @Volatile
    private var pendingHeight: Int = initialHeight

    val modReloadQueue = SlickModReloadQueue()
    private val pendingEngineCommands = ConcurrentLinkedQueue<PendingEngineCommand>()
    private val pendingTextInputRequests = ConcurrentLinkedQueue<SlickTextInputRequest>()
    private val pendingTextInputResponses = ConcurrentLinkedQueue<PendingTextInputResponse>()
    private val pendingTextInputHandlers = ConcurrentHashMap<Int, PasswordHandler>()
    private val pendingInputEvents = ConcurrentLinkedQueue<SlickInputEvent>()
    private val pendingInputEventCount = AtomicInteger(0)
    private val nextTextInputRequestId = AtomicInteger(1)
    private val frameDelta = SlickFrameDelta()
    private val frameProbe by lazy { BenchFrameProbe() }
    private var lastAppliedWidth: Int = 0
    private var lastAppliedHeight: Int = 0
    private var lastAppliedTargetFrameRate: Int = -1
    private var lastAppliedVsync: Boolean? = null
    private var lastAppliedSmoothDeltas: Boolean? = null
    private var pointerCursorApplied: Boolean = false
    private var preparedModReload: PreparedModReload? = null
    private var completedModReload: CompletedModReload? = null

    @Volatile
    private var latestFrameSnapshot: SlickFrameSnapshot? = null
    private var latestFrameSnapshotSequence: Long = 0L
    private val pendingFrameSnapshotRequest = AtomicReference<CompletableFuture<SlickFrameSnapshot?>?>(null)

    override fun init(container: GameContainer) {
        GameEngine.screenSize = Point(initialWidth, initialHeight)
        SlickGraphicsBinding.bind(graphicsEngine)
        GameEngine.graphicsEngine = graphicsEngine
        GameEngine.externalGameLoopDriver = true
        val activeEngine = gameSession.ensureRendererEngine(
            viewport = GameViewport(container.width, container.height),
            graphicsEngine = graphicsEngine,
            view = view,
            platformCallbacks = SlickLoadingCallbacks(onLoadingStatus),
        )
        resetSlickRenderCaches(activeEngine)
        if (activeEngine is GameLogic) {
            activeEngine.worldFrameRenderedListener = Runnable {
                captureFrameSnapshotAfterWorldRender(container)
            }
        }
        installNetworkCallbacks(activeEngine)
        activeEngine.colorizeLogMessage(view, false)
        view.onResume()
        activeEngine.updateWindowResolution(container.width, container.height)
        lastAppliedWidth = container.width
        lastAppliedHeight = container.height
        activeEngine.settingsEngine.numIncompleteLoadAttempts = 0
        activeEngine.settingsEngine.numLoadsSinceRunningGameOrNormalExit = 0
        engine = activeEngine
    }

    private fun applyPointerCursor(container: GameContainer): Boolean =
        runCatching {
            val pointerImage = (graphicsEngine.a(R.drawable.pointer, true) as? SlickTexture)?.image()
            if (pointerImage != null) {
                container.setMouseCursor(pointerImage, 0, 0)
            }
            pointerImage != null
        }.onFailure { error ->
            GameEngine.log("Failed to apply Slick mouse cursor", error)
        }.getOrDefault(false)

    private fun resetSlickRenderCaches(activeEngine: GameEngine) {
        graphicsEngine.resetBackendState()
        activeEngine.minimap?.bindGraphicsBackend(graphicsEngine)
        TileMap.layerBufferManager.releaseLayerBuffers()
        TileMap.layerBufferManager = LayerBufferManager()
        TileMap.layerBufferManager.bindGraphicsBackend(graphicsEngine)
        TileMap.bindGraphicsBackend(graphicsEngine)
    }

    private fun installNetworkCallbacks(activeEngine: GameEngine) {
        activeEngine.networkEngine?.callbacks = object : CoreUiNetworkCallbacks() {
            override fun onPasswordPrompt(passwordHandler: PasswordHandler): Boolean {
                queueTextInputRequest(passwordHandler)
                return true
            }
        }
    }

    private fun queueTextInputRequest(passwordHandler: PasswordHandler) {
        val requestId = nextTextInputRequestId.getAndIncrement()
        pendingTextInputHandlers[requestId] = passwordHandler
        val request = SlickTextInputRequest(
            id = requestId,
            title = passwordHandler.dialogTitle?.takeIf { it.isNotBlank() } ?: "Password Required",
            prompt = passwordHandler.promptMessage?.takeIf { it.isNotBlank() }
                ?: "This server requires a password to join",
            confirmButtonLabel = passwordHandler.confirmButtonLabel?.takeIf { it.isNotBlank() } ?: "OK",
            cancelButtonLabel = passwordHandler.cancelButtonLabel?.takeIf { it.isNotBlank() } ?: "Cancel",
        )
        val delivered = runCatching {
            onTextInputRequest(this, request)
        }.onFailure { error ->
            GameEngine.log("Failed to deliver Slick text input request", error)
        }.getOrDefault(false)
        logSlickMenuInput(
            "DesktopSlickGame.queueTextInputRequest" +
                    " id=${request.id}" +
                    " title=\"${request.title}\"" +
                    " delivered=$delivered" +
                    " thread=${Thread.currentThread().name}"
        )
        if (!delivered) {
            pendingTextInputRequests.add(request)
        }
    }

    override fun update(container: GameContainer, delta: Int) {
        frameDelta.update(delta)
    }

    override fun render(container: GameContainer, graphics: Graphics) {
        val activeEngine = engine ?: return
        val probeOn = frameProbe.isEnabled
        val startNanos = if (probeOn) System.nanoTime() else 0L
        try {
        graphicsEngine.setGraphics(graphics, container.width, container.height)
        activeEngine.renderGraphicsEngine = graphicsEngine
        completeModReload(activeEngine)
        applyContainerRuntimeSettings(activeEngine, container)
        if (prepareModReload(activeEngine)) return

        drainPendingWork(activeEngine, container)

        val deltaMillis = frameDelta.consumeMillis()
        val pausedFrame = shouldRenderPausedFrame(activeEngine)
        val gameLoopDriven = !pausedFrame && shouldDriveGameLoop(activeEngine)
        if (pausedFrame) {
            (activeEngine as GameLogic).drawWorldOnlyThreadSafe(0f)
        } else if (gameLoopDriven) {
            activeEngine.gameLoop(legacySlickDeltaSpeed(deltaMillis), deltaMillis)
        }
        if (shouldRenderSlickBackgroundLayerRedraw(gameLoopDriven)) {
            renderPendingLayerRedraws(activeEngine)
        }
        notifyReadyAfterVisibleFrames(activeEngine)
        } finally {
            if (probeOn) frameProbe.frame(System.nanoTime() - startNanos)
        }
    }

    private fun drainPendingWork(activeEngine: GameEngine, container: GameContainer) {
        applyPendingInputEvents(activeEngine)
        applyPendingSize(activeEngine, container)
        applyPendingRequest(activeEngine)
        applyPendingEngineCommands(activeEngine)
        applyPendingTextInputResponses()
        updatePointerCursor(container, activeEngine)
        abortStartedGameAfterDisconnect(activeEngine)
    }

    private fun renderPendingLayerRedraws(activeEngine: GameEngine) {
        synchronized(activeEngine.gameStateLock) {
            if (!activeEngine.hasLoadedLevel || activeEngine.tileMap == null) return
            TileMap.layerBufferManager.renderPendingRedraws(SLICK_LAYER_REDRAW_BUDGET_MS)
        }
    }

    private fun shouldDriveGameLoop(activeEngine: GameEngine): Boolean {
        val networkEngine = activeEngine.networkEngine
        return shouldDriveSlickGameLoop(
            hasPendingLoad = pendingRequest?.loadsLevel == true,
            hasLoadedLevel = activeEngine.hasLoadedLevel,
            hasRunningMap = runningMapPath != null,
            gameVisible = gameVisible,
            runningMenuBackground = runningMenuBackground,
            networkGameStarted = activeEngine.isNetworkGameActive() &&
                networkEngine?.gameHasBeenStarted == true,
        )
    }

    private fun shouldRenderPausedFrame(activeEngine: GameEngine): Boolean =
        shouldRenderPausedSlickFrame(
            pausedBackground = pausedBackground,
            gameSpeed = activeEngine.gameSpeed,
            hasLoadedLevel = activeEngine.hasLoadedLevel,
            gameVisible = gameVisible,
            runningMenuBackground = runningMenuBackground,
            networkMultiplayerActive = activeEngine.isNetworkGameActive(),
            enginePaused = activeEngine.isPaused || activeEngine.isStopped,
        )

    private fun applyContainerRuntimeSettings(activeEngine: GameEngine, container: GameContainer) {
        val vsync = activeEngine.settingsEngine.renderVsync
        val targetFrameRate = targetFrameRateFor(activeEngine, container)
        if (lastAppliedTargetFrameRate != targetFrameRate) {
            container.setTargetFrameRate(targetFrameRate)
            lastAppliedTargetFrameRate = targetFrameRate
        }
        if (lastAppliedVsync != vsync) {
            container.setVSync(vsync)
            lastAppliedVsync = vsync
        }
        val smoothDeltas = activeEngine.settingsEngine.renderSmoothDelta
        if (lastAppliedSmoothDeltas != smoothDeltas) {
            container.setSmoothDeltas(smoothDeltas)
            lastAppliedSmoothDeltas = smoothDeltas
        }
    }

    private fun targetFrameRateFor(activeEngine: GameEngine, container: GameContainer): Int {
        SLICK_TARGET_FPS_OVERRIDE?.let { return it }
        val highRefreshRate = activeEngine.settingsEngine.highRefreshRate
        return (container as? EmbeddedSlickGameContainer)
            ?.recommendedTargetFrameRate(highRefreshRate)
            ?: if (highRefreshRate) 300 else 120
    }

    private fun applyPendingSize(activeEngine: GameEngine, container: GameContainer) {
        val width = pendingWidth.coerceAtLeast(320)
        val height = pendingHeight.coerceAtLeast(240)
        if (container.width != width || container.height != height) {
            runCatching {
                if (container is EmbeddedSlickGameContainer) {
                    container.setDisplayMode(width, height, false)
                }
            }
        }
        if (lastAppliedWidth != container.width || lastAppliedHeight != container.height) {
            lastAppliedWidth = container.width
            lastAppliedHeight = container.height
            GameEngine.screenSize = Point(container.width, container.height)
            activeEngine.updateWindowResolution(container.width, container.height)
        }
    }

    private fun applyPendingRequest(activeEngine: GameEngine) {
        when (val request = pendingRequest) {
            null,
            SlickSessionRequest.EnginePreparation -> return

            is SlickSessionRequest.StartedGame -> adoptStartedGame(activeEngine, request)

            is SlickSessionRequest.Replay -> {
                // Deliberately skips applyNoFogOverride: a replay must render the fog it recorded.
                if (loadOrReportError(request.replayName) { loadReplay(activeEngine, request.replayName) }) {
                    beginRunningMap(request.replayName)
                }
                clearPendingRequest(request)
            }

            is SlickSessionRequest.Map ->
                loadRequestedLevel(activeEngine, request, request.mapPath) {
                    loadDirectMap(activeEngine, request.mapPath)
                }

            is SlickSessionRequest.SavedGame ->
                loadRequestedLevel(activeEngine, request, request.saveName) {
                    loadSavedGame(activeEngine, request.saveName)
                }

            is SlickSessionRequest.BattleRoom ->
                loadRequestedLevel(activeEngine, request, request.config.room.mapPath) {
                    loadBattleRoomMap(activeEngine, request.config)
                }

            is SlickSessionRequest.MapSnapshotRequest ->
                loadRequestedLevel(activeEngine, request, request.snapshot.mapPath) {
                    loadMapSnapshot(activeEngine, request.snapshot)
                }

            SlickSessionRequest.MenuBackground -> loadMenuBackground(activeEngine, request)
        }
    }

    private inline fun loadRequestedLevel(
        activeEngine: GameEngine,
        request: SlickSessionRequest,
        mapPath: String,
        load: () -> Unit,
    ) {
        if (loadOrReportError(mapPath, load)) {
            applyNoFogOverride(activeEngine)
            beginRunningMap(mapPath)
        }
        clearPendingRequest(request)
    }

    /**
     * Runs [load], reporting a failure as a map error rather than letting it escape.
     *
     * A throw here would propagate out of `render()`, which the embedded container treats as a
     * terminal failure: it stops the loop and tears down the GL context, so one unloadable map
     * would kill the renderer and force a full restart. [onMapError] already tells the session the
     * request failed, which is enough for it to surface the error and offer another map.
     *
     * @return true when [load] completed, so the caller may mark the level running.
     */
    private inline fun loadOrReportError(mapPath: String, load: () -> Unit): Boolean =
        try {
            load()
            true
        } catch (error: Throwable) {
            onMapError(mapPath, error)
            false
        }

    private fun beginRunningMap(mapPath: String?) {
        runningMapPath = mapPath
        readyNotifiedMapPath = null
        renderedFramesForRunningMap = 0
    }

    private fun clearPendingRequest(request: SlickSessionRequest) {
        if (pendingRequest == request) {
            pendingRequest = null
        }
    }

    private fun adoptStartedGame(activeEngine: GameEngine, request: SlickSessionRequest.StartedGame) {
        try {
            val networkEngine = activeEngine.networkEngine
                ?: error("Started battle room has no network engine")
            check(networkEngine.hasActiveStartedGameConnection()) {
                "Battle room network connection is no longer active"
            }
            if (activeEngine.hasLoadedLevel && !activeEngine.isMenuBackgroundMap) {
                startedBattleRoomGameSetupComplete = true
                runningMenuBackground = false
                beginRunningMap(
                    networkEngine.selectedMapPath
                        ?: activeEngine.currentMapPath
                        ?: request.mapPath
                )
            } else {
                completeStartedBattleRoomGame(activeEngine)
            }
            check(networkEngine.hasActiveStartedGameConnection()) {
                "Battle room network connection closed while loading the map"
            }
            check(activeEngine.hasLoadedLevel) { "Started battle room did not load a level" }
            clearPendingRequest(request)
        } catch (error: Throwable) {
            failStartedBattleRoomGame(activeEngine, request.mapPath, error)
        }
    }

    private fun abortStartedGameAfterDisconnect(activeEngine: GameEngine) {
        val networkEngine = activeEngine.networkEngine ?: return
        if (!shouldAbortStartedGameAfterDisconnect(
                setupComplete = startedBattleRoomGameSetupComplete,
                hasLoadedLevel = activeEngine.hasLoadedLevel,
                hasActiveConnection = networkEngine.hasActiveStartedGameConnection(),
                renderedFrames = renderedFramesForRunningMap,
            )
        ) {
            return
        }
        val mapPath = runningMapPath
            ?: (pendingRequest as? SlickSessionRequest.StartedGame)?.mapPath
            ?: return
        failStartedBattleRoomGame(
            activeEngine,
            mapPath,
            IllegalStateException("Battle room network connection closed before the first frame"),
        )
    }

    private fun failStartedBattleRoomGame(
        activeEngine: GameEngine,
        mapPath: String,
        error: Throwable,
    ) {
        (pendingRequest as? SlickSessionRequest.StartedGame)?.let(::clearPendingRequest)
        startedBattleRoomGameSetupComplete = false
        beginRunningMap(null)
        activeEngine.networkEngine
            ?.takeIf { it.gameHasBeenStarted }
            ?.onStartGameFailed()
        onMapError(mapPath, error)
    }

    private fun loadReplay(activeEngine: GameEngine, replayName: String) {
        runningMenuBackground = false
        activeEngine.isGameStarted = false
        activeEngine.isStopped = false
        activeEngine.isPaused = false
        check(activeEngine.replayEngine.loadReplay(replayName)) { "Unable to load replay: $replayName" }
    }

    private fun loadDirectMap(activeEngine: GameEngine, mapPath: String) {
        prepareActiveGameLoad(activeEngine)
        activeEngine.currentMapPath = mapPath
        if (noFog) {
            activeEngine.networkEngine.roomSettings.fogMode = 0
            activeEngine.networkEngine.roomSettings.revealedMap = true
        }
        activeEngine.loadGame(true, GameMode.normal)
    }

    private fun loadSavedGame(activeEngine: GameEngine, saveName: String) {
        activeEngine.networkEngine?.disconnectNetworking("loading new save")
        prepareActiveGameLoad(activeEngine)
        check(activeEngine.gameSaver.loadSaveFile(saveName, false)) {
            "Unable to load saved game: $saveName"
        }
        activeEngine.applyLoadedSinglePlayerFogRules()
    }

    private fun loadMapSnapshot(activeEngine: GameEngine, snapshot: MapSnapshot) {
        runningMenuBackground = false
        activeEngine.isStopped = false
        activeEngine.isPaused = false
        check(activeEngine.gameSaver.readSaveFromStream(GameInputStream(snapshot.saveBytes), false, false, false)) {
            "Unable to restore in-memory save: ${snapshot.mapPath}"
        }
    }

    private fun loadBattleRoomMap(activeEngine: GameEngine, config: BattleRoomLaunchConfig) {
        prepareActiveGameLoad(activeEngine)
        activeEngine.currentMapPath = config.room.mapPath
        val networkEngine = activeEngine.configureLocalBattleRoom(config, "Slick") ?: run {
            if (config.room.isSavedGame) {
                loadSavedGame(activeEngine, config.room.mapPath)
            } else {
                loadDirectMap(activeEngine, config.room.mapPath)
            }
            return
        }

        startedBattleRoomGameSetupComplete = false
        check(networkEngine.startBattleRoomGame()) { "Unable to start Slick battle room game" }
        completeStartedBattleRoomGame(activeEngine)
    }

    private fun loadMenuBackground(activeEngine: GameEngine, request: SlickSessionRequest) {
        // Cleared up front: unlike a level load, a failing menu background must not be retried.
        clearPendingRequest(request)
        val loaded = loadOrReportError(activeEngine.currentMapPath ?: "") {
            activeEngine.isStopped = true
            activeEngine.isPaused = true
            activeEngine.loadMenuBackground()
        }
        if (!loaded) return
        runningMenuBackground = true
        beginRunningMap(activeEngine.currentMapPath)
    }

    private fun setupBattleRoomGame(activeEngine: GameEngine) {
        val networkEngine = activeEngine.networkEngine
        prepareActiveGameLoad(activeEngine)
        activeEngine.remoteMapStream = null
        when (networkEngine.roomSettings.gameModeType) {
            GameModeType.savedGame -> {
                if (!networkEngine.isServer) {
                    activeEngine.gameSaver.readSaveFromStream(networkEngine.receivedSaveGameStream, true, false, false)
                    activeEngine.gameUI?.messageManager?.addMessage(null, "Note: Game was started from a saved game.")
                } else {
                    activeEngine.gameSaver.loadSaveFile(networkEngine.roomSettings.mapPath, true)
                }
            }

            GameModeType.customMap -> {
                if (!networkEngine.isServer) {
                    activeEngine.currentMapPath = ""
                    activeEngine.remoteMapStream = networkEngine.receivedCustomMapStream
                    activeEngine.loadGame(true, GameMode.normal)
                    activeEngine.gameUI?.messageManager?.addMessage(
                        null,
                        "Note: Game was started from a custom map on server.",
                    )
                } else {
                    activeEngine.currentMapPath = networkEngine.selectedMapPath
                    activeEngine.loadGame(true, GameMode.normal)
                }
            }

            else -> {
                activeEngine.currentMapPath = networkEngine.selectedMapPath
                activeEngine.loadGame(true, GameMode.normal)
            }
        }
    }

    private fun completeStartedBattleRoomGame(activeEngine: GameEngine) {
        if (startedBattleRoomGameSetupComplete) return
        val networkEngine = activeEngine.networkEngine ?: return
        startedBattleRoomGameSetupComplete = true
        runCatching {
            setupBattleRoomGame(activeEngine)
            applyNoFogOverride(activeEngine)
            runningMenuBackground = false
            runningMapPath = networkEngine.selectedMapPath ?: activeEngine.currentMapPath
            readyNotifiedMapPath = null
            renderedFramesForRunningMap = 0
        }.onFailure { error ->
            startedBattleRoomGameSetupComplete = false
            throw error
        }
    }

    private fun applyNoFogOverride(activeEngine: GameEngine) {
        if (!noFog) return
        activeEngine.tileMap?.fogEnabled = false
        activeEngine.tileMap?.fogPeriodicMaintenanceEnabled = false
        activeEngine.tileMap?.fogRenderActive = true
    }

    private fun prepareActiveGameLoad(activeEngine: GameEngine) {
        runningMenuBackground = false
        activeEngine.isStopped = false
        activeEngine.isPaused = false
        activeEngine.minimap?.release()
    }

    private fun notifyReadyAfterVisibleFrames(activeEngine: GameEngine) {
        val mapPath = runningMapPath ?: return
        if (!shouldCountSlickReadyFrame(
                hasLoadedLevel = activeEngine.hasLoadedLevel,
                runningMapPath = mapPath,
                readyNotifiedMapPath = readyNotifiedMapPath,
            )
        ) {
            return
        }
        renderedFramesForRunningMap += 1
        if (renderedFramesForRunningMap >= READY_FRAME_COUNT) {
            readyNotifiedMapPath = mapPath
            onMapReady(mapPath)
        }
    }

    private fun updatePointerCursor(container: GameContainer, activeEngine: GameEngine) {
        val shouldApply = gameVisible &&
                activeEngine.hasLoadedLevel &&
                !activeEngine.isMenuBackgroundMap &&
                activeEngine.tileMap?.isCursorActive == true &&
                runningMapPath != null &&
                !runningMenuBackground
        if (shouldApply == pointerCursorApplied) return
        pointerCursorApplied = if (shouldApply) {
            applyPointerCursor(container)
        } else {
            runCatching { container.setDefaultMouseCursor() }
            false
        }
    }

    fun submit(request: SlickSessionRequest) {
        resetFrameSnapshot()
        startedBattleRoomGameSetupComplete = false
        if (request != SlickSessionRequest.MenuBackground) {
            // The menu background keeps its flag until its own load replaces it.
            runningMenuBackground = false
        }
        pendingRequest = request
    }

    fun currentFrameSnapshot(): SlickFrameSnapshot? = latestFrameSnapshot

    fun requestFrameSnapshot(timeoutMillis: Long): SlickFrameSnapshot? {
        val request = CompletableFuture<SlickFrameSnapshot?>()
        pendingFrameSnapshotRequest.getAndSet(request)?.complete(latestFrameSnapshot)
        return runCatching {
            request.get(timeoutMillis, TimeUnit.MILLISECONDS)
        }.getOrElse {
            pendingFrameSnapshotRequest.compareAndSet(request, null)
            latestFrameSnapshot
        }
    }

    fun setGameVisible(visible: Boolean, pausedBackground: Boolean = false) {
        gameVisible = visible
        this.pausedBackground = pausedBackground
    }

    fun requestSize(width: Int, height: Int) {
        pendingWidth = width
        pendingHeight = height
    }

    /** Queues [command] for the render thread's next drain without waiting for its result. */
    fun postEngineCommand(label: String, command: (GameEngine) -> Any?) {
        pendingEngineCommands.add(PendingEngineCommand(label, command))
    }

    /**
     * Queues [command] and waits for the render thread to run it, returning its result. Returns
     * null when it does not complete within [timeoutMillis] (render thread mid-load or shutting
     * down); the command is then abandoned so it cannot apply late and surprise the caller.
     */
    fun <T> awaitEngineCommand(label: String, timeoutMillis: Long, command: (GameEngine) -> T?): T? {
        val pending = PendingEngineCommand(label) { engine -> command(engine) }
        pendingEngineCommands.add(pending)
        return try {
            @Suppress("UNCHECKED_CAST")
            pending.result.get(timeoutMillis, TimeUnit.MILLISECONDS) as T?
        } catch (error: Exception) {
            pending.abandoned.set(true)
            GameEngine.log("Engine command did not complete on the render thread: $label ($error)")
            null
        }
    }

    fun pollTextInputRequests(): List<SlickTextInputRequest> {
        val requests = mutableListOf<SlickTextInputRequest>()
        while (true) {
            requests += pendingTextInputRequests.poll() ?: break
        }
        return requests
    }

    fun submitTextInput(requestId: Int, text: String) {
        pendingTextInputResponses.add(PendingTextInputResponse(requestId, text, cancel = false))
    }

    fun cancelTextInput(requestId: Int) {
        pendingTextInputResponses.add(PendingTextInputResponse(requestId, null, cancel = true))
    }

    internal fun runPendingNonRenderingWork(drainEngineCommands: Boolean = false): Boolean {
        val activeEngine = engine ?: return false
        var didWork = false
        if (drainEngineCommands) {
            didWork = applyPendingEngineCommands(activeEngine) || didWork
            didWork = applyPendingTextInputResponses() || didWork
        }
        if (completedModReload != null) return didWork
        val reload = preparedModReload ?: return didWork
        preparedModReload = null
        val error = runCatching {
            activeEngine.renderGraphicsEngine = reload.graphicsEngine
            try {
                TileMap.buildFogSmoothAtlas(reload.graphicsEngine)
                reloadMods(activeEngine)
            } finally {
                activeEngine.renderGraphicsEngine = graphicsEngine
            }
        }.exceptionOrNull()
        completedModReload = CompletedModReload(reload.request, error)
        return true
    }

    private fun prepareModReload(activeEngine: GameEngine): Boolean {
        if (preparedModReload != null || completedModReload != null) return false
        val request = modReloadQueue.takePending() ?: return false
        val reloadGraphicsEngine = contextBoundSlickGraphics(graphicsEngine, SlickGlContext(checkNotNull(SlickCanvasHost.gameCanvas() as? SlickAwtGLCanvas)))
        val error = runCatching {
            graphicsEngine.resetBackendState()
            activeEngine.minimap?.bindGraphicsBackend(reloadGraphicsEngine)
            TileMap.layerBufferManager.releaseLayerBuffers()
            TileMap.layerBufferManager = LayerBufferManager()
            TileMap.layerBufferManager.bindGraphicsBackend(reloadGraphicsEngine)
            TileMap.releaseFogSmoothAtlas()
        }.exceptionOrNull()
        if (error == null) {
            preparedModReload = PreparedModReload(request, reloadGraphicsEngine)
        } else {
            completedModReload = CompletedModReload(request, error)
            completeModReload(activeEngine)
        }
        return true
    }

    private fun completeModReload(activeEngine: GameEngine) {
        val completion = completedModReload ?: return
        completedModReload = null
        val cacheResetError = runCatching {
            resetSlickRenderCaches(activeEngine)
        }.exceptionOrNull()
        val error = completion.error?.also { reloadError ->
            cacheResetError?.let(reloadError::addSuppressed)
        } ?: cacheResetError
        if (error == null) {
            completion.request.complete(Unit)
        } else {
            completion.request.completeExceptionally(error)
        }
    }

    private fun reloadMods(activeEngine: GameEngine) {
        try {
            activeEngine.beginLoadingStatus("Loading custom unit data", 12)
            activeEngine.loadLevel("Loading custom unit data")
            activeEngine.modManager.saveModSelection()
            activeEngine.modManager.applyAndSaveMods()
            activeEngine.markLoadingStatusComplete("Mods reloaded")
            activeEngine.settingsEngine.save()
            activeEngine.gameUI?.showMediumPriorityMessage("Mods reloaded")
        } catch (error: Throwable) {
            GameEngine.log("Failed to reload mods", error)
            activeEngine.gameUI?.showHighPriorityMessage("Mod reload failed: ${error.message ?: error.javaClass.simpleName}")
            throw error
        }
    }

    private fun applyPendingEngineCommands(activeEngine: GameEngine): Boolean {
        var didWork = false
        while (true) {
            val pending = pendingEngineCommands.poll() ?: break
            didWork = true
            if (pending.abandoned.get()) continue
            val outcome = runCatching { pending.command(activeEngine) }
            outcome.exceptionOrNull()?.let { error ->
                GameEngine.log("Engine command failed: ${pending.label}", error)
            }
            pending.result.complete(outcome.getOrNull())
        }
        return didWork
    }

    private fun applyPendingTextInputResponses(): Boolean {
        var didWork = false
        while (true) {
            val response = pendingTextInputResponses.poll() ?: break
            didWork = true
            val handler = pendingTextInputHandlers.remove(response.requestId) ?: continue
            runCatching {
                if (response.cancel) {
                    handler.cancelPasswordEntry()
                } else {
                    handler.submitPassword(response.text.orEmpty())
                }
            }.onFailure { error ->
                GameEngine.log("Failed to apply Slick text input response", error)
            }
        }
        return didWork
    }

    private fun captureFrameSnapshotAfterWorldRender(container: GameContainer) {
        val request = pendingFrameSnapshotRequest.get()
        if (request == null) {
            return
        }
        if (runningMapPath == null || (!runningMenuBackground && !gameVisible)) {
            request?.let { completeFrameSnapshotRequest(it, latestFrameSnapshot) }
            return
        }
        val snapshot = captureFrameSnapshot(container) ?: latestFrameSnapshot
        request?.let { completeFrameSnapshotRequest(it, snapshot) }
    }

    private fun resetFrameSnapshot() {
        latestFrameSnapshot = null
    }

    private fun captureFrameSnapshot(container: GameContainer): SlickFrameSnapshot? {
        val framebufferSize = (container as? EmbeddedSlickGameContainer)?.currentFramebufferSize()
        val width = framebufferSize?.width?.takeIf { it > 0 }
            ?: container.width.takeIf { it > 0 }
        val height = framebufferSize?.height?.takeIf { it > 0 }
            ?: container.height.takeIf { it > 0 }
        if (width == null || height == null) {
            return null
        }
        return runCatching {
            readBackBufferSnapshot(width, height, ++latestFrameSnapshotSequence).also { latestFrameSnapshot = it }
        }.onFailure { error ->
            GameEngine.log("Failed to capture Slick frame snapshot", error)
        }.getOrNull()
    }

    private fun completeFrameSnapshotRequest(
        request: CompletableFuture<SlickFrameSnapshot?>,
        snapshot: SlickFrameSnapshot?,
    ) {
        if (pendingFrameSnapshotRequest.compareAndSet(request, null)) {
            request.complete(snapshot)
        }
    }

    private fun readBackBufferSnapshot(width: Int, height: Int, sequence: Long): SlickFrameSnapshot {
        val buffer = BufferUtils.createByteBuffer(width * height * 4)
        GL11.glReadBuffer(GL11.GL_BACK)
        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1)
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer)

        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val sourceRow = height - 1 - y
            for (x in 0 until width) {
                val sourceOffset = (sourceRow * width + x) * 4
                val red = buffer.get(sourceOffset).toInt() and 0xff
                val green = buffer.get(sourceOffset + 1).toInt() and 0xff
                val blue = buffer.get(sourceOffset + 2).toInt() and 0xff
                pixels[y * width + x] = 0xff000000.toInt() or (red shl 16) or (green shl 8) or blue
            }
        }
        return SlickFrameSnapshot(width, height, pixels, sequence)
    }

    private fun applyPendingInputEvents(activeEngine: GameEngine) {
        var pendingMotion: SlickInputEvent.Motion? = null
        while (true) {
            val event = pendingInputEvents.poll() ?: break
            pendingInputEventCount.decrementAndGet()
            if (event is SlickInputEvent.Motion) {
                pendingMotion = event
                continue
            }
            pendingMotion?.let {
                applyInputEvent(activeEngine, it)
            }
            pendingMotion = null
            applyInputEvent(activeEngine, event)
        }
        pendingMotion?.let {
            applyInputEvent(activeEngine, it)
        }
    }

    private fun applyInputEvent(activeEngine: GameEngine, event: SlickInputEvent) {
        when (event) {
            is SlickInputEvent.PointerButton -> {
                view.pointerState.processEvent(event.x, event.y, event.down, event.pointerId)
            }

            is SlickInputEvent.Motion -> {
                view.pointerState.setStart(event.x, event.y)
            }

            is SlickInputEvent.Wheel -> {
                activeEngine.queueMouseWheelDelta(event.change)
            }

            is SlickInputEvent.Key -> {
                setKeyState(activeEngine, event.slickKey, event.down)
            }
        }
    }

    override fun mousePressed(button: Int, x: Int, y: Int) {
        val pointerId = pointerIdForButton(button)
        if (pointerId != -1) {
            enqueueInputEvent(SlickInputEvent.PointerButton(x.toFloat(), y.toFloat(), true, pointerId))
        }
        logSlickMenuInput("slickMousePressed button=$button pointerId=$pointerId xy=$x,$y ${engineTouchState()}")
    }

    override fun mouseReleased(button: Int, x: Int, y: Int) {
        val pointerId = pointerIdForButton(button)
        if (pointerId != -1) {
            enqueueInputEvent(SlickInputEvent.PointerButton(x.toFloat(), y.toFloat(), false, pointerId))
        }
        logSlickMenuInput("slickMouseReleased button=$button pointerId=$pointerId xy=$x,$y ${engineTouchState()}")
    }

    override fun mouseDragged(oldx: Int, oldy: Int, newx: Int, newy: Int) {
        enqueueInputEvent(SlickInputEvent.Motion(newx.toFloat(), newy.toFloat()))
    }

    override fun mouseMoved(oldx: Int, oldy: Int, newx: Int, newy: Int) {
        enqueueInputEvent(SlickInputEvent.Motion(newx.toFloat(), newy.toFloat()))
    }

    override fun mouseWheelMoved(change: Int) {
        enqueueInputEvent(SlickInputEvent.Wheel(change))
    }

    fun submitOverlayPointer(x: Float, y: Float, down: Boolean, pointerId: Int) {
        enqueueInputEvent(SlickInputEvent.PointerButton(x, y, down, pointerId))
    }

    fun moveOverlayPointer(x: Float, y: Float) {
        enqueueInputEvent(SlickInputEvent.Motion(x, y))
    }

    fun submitOverlayMouseWheel(change: Int) {
        enqueueInputEvent(SlickInputEvent.Wheel(change))
    }

    override fun keyPressed(key: Int, c: Char) {
        enqueueInputEvent(SlickInputEvent.Key(key, true))
    }

    override fun keyReleased(key: Int, c: Char) {
        enqueueInputEvent(SlickInputEvent.Key(key, false))
    }

    private fun enqueueInputEvent(event: SlickInputEvent) {
        pendingInputEventCount.incrementAndGet()
        pendingInputEvents.add(event)
    }

    private fun setKeyState(activeEngine: GameEngine, slickKey: Int, down: Boolean) {
        if (slickKey !in keyStates.indices || keyStates[slickKey] == down) return
        keyStates[slickKey] = down
        val androidKey = SlickToAndroidKeycodes.convertSlickToAndroidKeyCode(slickKey)
        if (androidKey != 0) {
            activeEngine.setKeyState(androidKey, down)
        }
    }

    private fun pointerIdForButton(button: Int): Int = when (button) {
        Input.MOUSE_LEFT_BUTTON -> 1
        Input.MOUSE_RIGHT_BUTTON -> 2
        Input.MOUSE_MIDDLE_BUTTON -> 3
        else -> -1
    }

    private fun logSlickMenuInput(message: String) {
        if (debugSlickMenuInput) {
            GameEngine.log("RWX_DEBUG_SLICK_MENU $message")
        }
    }

    private fun engineTouchState(): String {
        val activeEngine = engine ?: return "engine=null"
        return runCatching {
            val pointerCount = activeEngine.getTouchPointerCount()
            val pointerId0 = if (pointerCount > 0) activeEngine.getTouchPointerId(0) else -1
            "touch=${activeEngine.getTouchX()},${activeEngine.getTouchY()} touchDown=${activeEngine.isTouchDown()} pointerCount=$pointerCount pointerId0=$pointerId0 menuDragging=${activeEngine.gameUI?.isDraggingSelection}"
        }.getOrElse { error ->
            "engineTouchStateError=${error.javaClass.simpleName}:${error.message.orEmpty()}"
        }
    }

    private class SlickCoreGameView(
        private val menuController: InGameMenuController,
    ) : CoreGameView {
        val pointerState = MultiTouchPointerState()
        private var rendering = true

        override fun pause() {
            rendering = false
        }

        override fun isActive(): Boolean = true

        override fun isContinuousRendering(): Boolean = true

        override fun isRendering(): Boolean = rendering

        override fun getInGameMenuController(): InGameMenuController = menuController

        override fun onResume() {
            rendering = true
        }

        override fun getSettings(): MultiTouchPointerState = pointerState

        override fun onSizeChanged() = Unit

        override fun stopRender() {
            rendering = false
        }
    }

    private sealed interface SlickInputEvent {
        data class PointerButton(
            val x: Float,
            val y: Float,
            val down: Boolean,
            val pointerId: Int,
        ) : SlickInputEvent

        data class Motion(
            val x: Float,
            val y: Float,
        ) : SlickInputEvent

        data class Wheel(
            val change: Int,
        ) : SlickInputEvent

        data class Key(
            val slickKey: Int,
            val down: Boolean,
        ) : SlickInputEvent
    }

    private companion object {
        const val SLICK_KEY_COUNT = 224
        const val READY_FRAME_COUNT = 1
    }
}

/** A session-issued engine mutation waiting for the render thread that owns the engine. */
private class PendingEngineCommand(
    val label: String,
    val command: (GameEngine) -> Any?,
) {
    val result = CompletableFuture<Any?>()

    /** Set when the poster stopped waiting; the drain then skips the command entirely. */
    val abandoned = AtomicBoolean(false)
}

private data class PendingTextInputResponse(
    val requestId: Int,
    val text: String?,
    val cancel: Boolean,
)

data class SlickFrameSnapshot(
    val width: Int,
    val height: Int,
    val pixels: IntArray,
    val sequence: Long,
)
