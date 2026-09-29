package io.github.rwx

import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.corrodinggames.rts.game.GameLogic
import com.corrodinggames.rts.game.map.TileMap
import com.corrodinggames.rts.gameFramework.GameEngine
import io.github.rwx.app.launchOnIO
import io.github.rwx.geometry.Point
import io.github.rwx.input.MultiTouchPointerState
import io.github.rwx.platform.CoreGameView
import io.github.rwx.render.frame.GameFrame
import io.github.rwx.render.frame.GameViewport
import io.github.rwx.session.GameSession
import io.github.rwx.session.GameSessionRendererProfile
import io.github.rwx.ui.BattleRoomUiBridge
import io.github.rwx.ui.InGameMenuController
import kotlin.math.roundToInt
import androidx.core.view.isVisible

internal class AndroidGameSession(
    override var renderBackend: AndroidRenderBackend,
) : GameSession() {

    private val view = AndroidCoreGameView(inGameMenuController)
    private var activity: ComponentActivity? = null
    private var host: FrameLayout? = null
    private var composeView: ComposeView? = null
    private var attachmentObserver: DefaultLifecycleObserver? = null
    private var resumed = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private var visibilityUpdate: Runnable? = null
    private var framePresenter: AndroidFramePresenter? = null
    private var graphicsEngine: AndroidGraphicsEngine? = null
    private var appliedViewport: GameViewport = GameViewport(0, 0)
    private var appliedRenderSurfaceScale: Float = 0f

    @Volatile
    private var visibilityRequestInitialized = false

    @Volatile
    private var requestedGameVisible = false

    @Volatile
    private var requestedOverlay = false

    @Volatile
    private var requestedPausedBackground = false

    private var pausedForResumeBackground = false
    private var resumeBackgroundFrameReady = false

    init {
        configureRendererProfile(
            GameSessionRendererProfile(
                usesFrameCommandRendering = false,
                usesNativeSurfaceForResumeBackground = true,
            )
        )
    }

    fun attach(activity: ComponentActivity, host: FrameLayout, composeView: ComposeView) {
        checkMainThread()
        if (this.activity === activity && this.host === host && this.composeView === composeView) return
        detach()
        this.activity = activity
        this.host = host
        this.composeView = composeView
        val presenter = createPresenter(activity)
        if (graphicsEngine == null) {
            graphicsEngine = AndroidGraphicsEngine(activity.applicationContext, renderBackend)
        }
        host.addView(presenter.view, 0, matchParentLayoutParams())
        (composeView.parent as? ViewGroup)?.removeView(composeView)
        host.addView(composeView, matchParentLayoutParams())
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) = this@AndroidGameSession.onResume()
            override fun onPause(owner: LifecycleOwner) = this@AndroidGameSession.onPause()
            override fun onDestroy(owner: LifecycleOwner) {
                if (this@AndroidGameSession.activity === activity) detach()
            }
        }
        attachmentObserver = observer
        activity.lifecycle.addObserver(observer)
    }

    fun viewport(): GameViewport =
        GameViewport(framePresenter?.view?.width ?: 0, framePresenter?.view?.height ?: 0).resolved()

    private fun matchParentLayoutParams() = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT,
    )

    private fun checkMainThread() {
        check(Looper.myLooper() === Looper.getMainLooper()) { "Native views must be owned by the main thread" }
    }

    fun switchRenderBackend(renderBackend: AndroidRenderBackend) {
        checkMainThread()
        val activity = activity ?: return
        val host = host ?: return
        if (this.renderBackend == renderBackend) return
        synchronized(gameLock) {
            if (this.renderBackend == renderBackend) return

            val oldPresenter = framePresenter
            visibilityUpdate?.let(mainHandler::removeCallbacks)
            visibilityUpdate = null
            oldPresenter?.view?.setOnTouchListener(null)
            oldPresenter?.pause()
            oldPresenter?.view?.let { oldView ->
                (oldView.parent as? ViewGroup)?.removeView(oldView)
            }

            this.renderBackend = renderBackend
            graphicsEngine = graphicsEngine?.recreateForRenderBackend(renderBackend)
            graphicsEngine?.let { replacement ->
                GameEngine.graphicsEngine = replacement
                gameEngine?.let { engine ->
                    engine.renderGraphicsEngine = replacement
                    engine.minimap?.bindGraphicsBackend(replacement)
                    TileMap.layerBufferManager.bindGraphicsBackend(replacement)
                    TileMap.bindGraphicsBackend(replacement)
                }
            }
            appliedViewport = GameViewport(0, 0)
            appliedRenderSurfaceScale = 0f

            val presenter = createPresenter(activity)
            host.addView(
                presenter.view,
                0,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                ),
            )
            if (visibilityRequestInitialized) {
                presenter.view.visibility = if (requestedGameVisible) View.VISIBLE else View.GONE
                presenter.setVisible(requestedGameVisible && resumed)
                if (requestedGameVisible && requestedOverlay) {
                    composeView?.bringToFront()
                }
            }
            logger.info { "Switched Android game renderer to ${renderBackend.name}" }
        }
    }

    private fun createPresenter(activity: Activity): AndroidFramePresenter =
        renderBackend.createPresenter(activity).also { createdPresenter ->
            createdPresenter.view.apply {
                visibility = View.VISIBLE
                isFocusableInTouchMode = true
                setOnTouchListener { _, event ->
                    handleMotionEvent(event)
                    true
                }
            }
            framePresenter = createdPresenter
            if (!resumed) createdPresenter.pause()
        }

    fun detach() {
        checkMainThread()
        attachmentObserver?.let { activity?.lifecycle?.removeObserver(it) }
        attachmentObserver = null
        visibilityUpdate?.let(mainHandler::removeCallbacks)
        visibilityUpdate = null
        synchronized(gameLock) {
            onPause()
            framePresenter?.view?.let { nativeView ->
                nativeView.setOnTouchListener(null)
                (nativeView.parent as? ViewGroup)?.removeView(nativeView)
            }
            // Never reuse a View whose Context is the previous Activity.
            framePresenter = null
            composeView?.let { overlay ->
                overlay.disposeComposition()
                (overlay.parent as? ViewGroup)?.removeView(overlay)
            }
            activity = null
            host = null
            composeView = null
            visibilityRequestInitialized = false
            appliedViewport = GameViewport(0, 0)
            appliedRenderSurfaceScale = 0f
            resumeBackgroundFrameReady = false
            view.clearTouchSnapshot()
        }
    }

    fun onPause() {
        resumed = false
        framePresenter?.pause()
        view.pause()
        view.clearTouchSnapshot()
    }

    fun onResume() {
        resumed = true
        resumeBackgroundFrameReady = false
        view.onResume()
        framePresenter?.takeIf { it.view.isVisible }?.resume()
    }

    override fun submitPointer(screenX: Float, screenY: Float, isDown: Boolean, pointerId: Int) {
        view.submitPointer(screenX, screenY, isDown, pointerId)
    }

    override fun movePointer(screenX: Float, screenY: Float) {
        view.movePointer(screenX, screenY)
    }

    override fun updateFrame(
        viewport: GameViewport,
        deltaSeconds: Float,
        drainVisibleLayerBuffers: Boolean,
    ): GameFrame {
        if (loadState.asyncMapLoadInProgress || modReloadInProgress) {
            return lastFrame
        }
        if (loadState.menuBackgroundActive && (!requestedGameVisible || !requestedOverlay)) {
            setGameVisible(true, viewport, uiOverlay = true)
        }
        synchronized(gameLock) {
            lastViewport = viewport
            val engine = ensureStarted(viewport)
            applyViewport(engine, viewport)
            loadPendingMap(engine)

            val presenter = framePresenter
            val androidGraphics = graphicsEngine
            if (presenter == null || androidGraphics == null || !presenter.isReady()) {
                if (pausedForResumeBackground) {
                    return lastFrame
                }
                runGameLoop(engine, deltaSeconds, skipDraw = true)
                return lastFrame
            }

            if (pausedForResumeBackground && resumeBackgroundFrameReady) {
                return lastFrame
            }

            val frame = presenter.acquireFrame()
            if (frame == null) {
                if (pausedForResumeBackground) {
                    return lastFrame
                }
                runGameLoop(engine, deltaSeconds, skipDraw = true)
                return lastFrame
            }
            var submitFrame = false
            try {
                androidGraphics.beginFrame(frame.graphics, engine.screenWidth.toInt(), engine.screenHeight.toInt())
                if (pausedForResumeBackground) {
                    renderPausedBackgroundFrame(engine)
                } else {
                    runGameLoop(engine, deltaSeconds)
                }
                if (drainVisibleLayerBuffers && engine.hasLoadedLevel) {
                    TileMap.layerBufferManager.renderVisiblePendingRedrawsNow()
                }
                androidGraphics.endFrame()
                submitFrame = true
            } finally {
                if (submitFrame) {
                    frame.submit()
                } else {
                    frame.cancel()
                }
            }
            lastFrame = GameFrame(viewport, emptyList())
            if (pausedForResumeBackground) {
                resumeBackgroundFrameReady = true
            }
            return lastFrame
        }
    }

    private fun runGameLoop(engine: GameEngine, deltaSeconds: Float, skipDraw: Boolean = false) {
        if (skipDraw) {
            engine.shouldSkipNextDraw = true
        }
        try {
            runCatching {
                engine.gameLoop(
                    deltaSeconds.toGameSpeedDelta(),
                    (deltaSeconds * 1000f).roundToInt().coerceAtLeast(0),
                )
            }.onFailure { error ->
                logger.error(error) { " game loop failed" }
            }
        } finally {
            if (skipDraw) {
                engine.shouldSkipNextDraw = false
            }
        }
    }

    private fun renderPausedBackgroundFrame(engine: GameEngine) {
        runCatching {
            (engine as GameLogic).drawWorldOnlyThreadSafe(0f)
        }.onFailure { error ->
            logger.error(error) { " paused background render failed" }
        }
    }

    override fun currentFrame(): GameFrame = lastFrame

    override fun prepareMenuBackgroundAsync(viewport: GameViewport) {
        val state = loadState
        if (state.menuBackgroundActive) {
            return
        }
        if (state.runningMapPath != null && gameEngine?.hasLoadedLevel == true) {
            return
        }
        var generation: Long? = null
        updateLoadState { current ->
            if (current.asyncMapLoadInProgress || current.menuBackgroundActive) {
                generation = null
                current
            } else {
                current.copy(
                    mapLoadGeneration = current.mapLoadGeneration + 1,
                    asyncMapLoadPath = MENU_BACKGROUND_REQUEST,
                    asyncMapLoadInProgress = true,
                    asyncMapLoadError = null,
                ).also { generation = it.mapLoadGeneration }
            }
        }
        val issuedGeneration = generation ?: return
        lastFrame = GameFrame(viewport, emptyList())
        logger.info { "Preparing Android Canvas RW menu background asynchronously" }
        launchOnIO("${renderBackend.name.lowercase()}-menu-background-loader") {
            loadMenuBackgroundInBackground(viewport, issuedGeneration)
        }
    }

    override fun isMenuBackgroundActive(): Boolean {
        val state = loadState
        return state.menuBackgroundActive ||
                (state.asyncMapLoadInProgress && state.asyncMapLoadPath == MENU_BACKGROUND_REQUEST)
    }

    override fun adoptStartedGameFromEngine(viewport: GameViewport): Boolean =
        synchronized(gameLock) {
            val engine = gameEngine ?: GameEngine.getInstance() ?: return@synchronized false
            if (engine.networkEngine?.gameHasBeenStarted != true) return@synchronized false

            lastViewport = viewport
            if (gameEngine == null) {
                gameEngine = engine
            }
            applyViewport(engine, viewport)

            // The original Android battleroom runs startGameCommon() before opening
            // InGameActivity. Android has no renderer callback equivalent to SlickGame's, so
            // complete that load here before exposing the GameView.
            BattleRoomUiBridge.setupGame()
            val activeMapPath = activeRunningMapPath(engine) ?: return@synchronized false

            updateLoadState {
                it.copy(
                    pendingMapPath = null,
                    asyncMapLoadPath = null,
                    asyncMapLoadInProgress = false,
                    asyncMapLoadError = null,
                    menuBackgroundActive = false,
                    runningMapPath = activeMapPath,
                )
            }
            engine.isStopped = false
            engine.isPaused = false
            true
        }

    override fun clearInputState() {
        view.submitPointer(0f, 0f, false, -1)
    }

    override fun loadPendingMapNow(): GameFrame =
        updateFrame(lastViewport, 0f)

    override fun setGameVisible(
        visible: Boolean,
        viewport: GameViewport,
        uiOverlay: Boolean,
        pausedBackground: Boolean,
    ) {
        val presenter = framePresenter ?: return
        val view = presenter.view
        if (visibilityRequestInitialized &&
            requestedGameVisible == visible &&
            requestedOverlay == uiOverlay &&
            requestedPausedBackground == pausedBackground
        ) {
            return
        }
        visibilityRequestInitialized = true
        requestedGameVisible = visible
        requestedOverlay = uiOverlay
        requestedPausedBackground = pausedBackground
        synchronized(gameLock) {
            val engine = gameEngine
            val shouldPauseForBackground = pausedBackground &&
                engine?.hasLoadedLevel == true &&
                !engine.isMenuBackgroundMap &&
                !engine.isNetworkGameActive()
            if (pausedForResumeBackground != shouldPauseForBackground) {
                pausedForResumeBackground = shouldPauseForBackground
                resumeBackgroundFrameReady = false
            }
        }
        visibilityUpdate?.let(mainHandler::removeCallbacks)
        val update = Runnable {
            if (framePresenter !== presenter) return@Runnable
            visibilityUpdate = null
            view.visibility = if (visible) View.VISIBLE else View.GONE
            if (visible) {
                if (uiOverlay) {
                    composeView?.bringToFront()
                } else {
                    view.bringToFront()
                }
                presenter.setVisible(resumed)
            } else {
                presenter.setVisible(false)
            }
        }
        visibilityUpdate = update
        mainHandler.post(update)
    }

    private fun handleMotionEvent(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_POINTER_DOWN,
            MotionEvent.ACTION_MOVE -> {
                view.submitTouchSnapshot(event, true)
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_POINTER_UP -> {
                view.submitTouchSnapshot(event, false)
            }

            MotionEvent.ACTION_CANCEL -> {
                view.clearTouchSnapshot()
            }
        }
    }

    override fun ensureStarted(viewport: GameViewport): GameEngine {
        gameEngine?.let { return it }
        val androidGraphics = graphicsEngine
            ?: throw IllegalStateException("Android Canvas session is not attached")
        val resolvedViewport = viewport.resolved()
        val width = resolvedViewport.width
        val height = resolvedViewport.height
        GameEngine.screenSize = Point(width, height)
        GameEngine.graphicsEngine = androidGraphics
        GameEngine.externalGameLoopDriver = true
        val engine = GameEngine.createGameEngine(null)
        engine.renderGraphicsEngine = androidGraphics
        engine.colorizeLogMessage(view, false)
        view.onResume()
        gameEngine = engine
        return engine
    }

    override fun applyViewport(engine: GameEngine, viewport: GameViewport) {
        val resolvedViewport = viewport.resolved()
        val physicalWidth = resolvedViewport.width
        val physicalHeight = resolvedViewport.height
        val doubleScale = engine.settingsEngine.renderDoubleScale
        val renderSurfaceScale = if (doubleScale) 2f else 1f
        val logicalWidth = if (doubleScale) (physicalWidth / 2).coerceAtLeast(1) else physicalWidth
        val logicalHeight = if (doubleScale) (physicalHeight / 2).coerceAtLeast(1) else physicalHeight
        if (appliedViewport.width == physicalWidth &&
            appliedViewport.height == physicalHeight &&
            appliedRenderSurfaceScale == renderSurfaceScale
        ) {
            return
        }
        engine.updateWindowResolution(logicalWidth, logicalHeight, renderSurfaceScale)
        graphicsEngine?.a(logicalWidth, logicalHeight)
        view.onSizeChanged()
        appliedViewport = GameViewport(physicalWidth, physicalHeight)
        appliedRenderSurfaceScale = renderSurfaceScale
    }

    private fun loadMenuBackgroundInBackground(requestedViewport: GameViewport, generation: Long) {
        val startedAt = System.nanoTime()
        var loadedCurrentRequest = false
        runCatching {
            synchronized(gameLock) {
                if (generation != loadState.mapLoadGeneration) {
                    return@synchronized
                }
                val viewport = requestedViewport.takeIf { it.width > 0 && it.height > 0 }
                    ?: defaultPreloadViewport
                lastViewport = viewport
                val engine = ensureStarted(viewport)
                applyViewport(engine, viewport)
                engine.isStopped = true
                engine.isPaused = true
                engine.loadMenuBackground()
                if (!engine.hasLoadedLevel || !engine.isMenuBackgroundMap) {
                    error("RW menu background map did not load")
                }
                engine.targetZoom *= MENU_BACKGROUND_ZOOM_MULTIPLIER
                engine.zoom = engine.targetZoom * engine.densityZoomScale
                engine.updateWindowResolution(
                    engine.screenWidth.toInt(),
                    engine.screenHeight.toInt(),
                    engine.renderSurfaceScale,
                )
                val currentMapPath = engine.currentMapPath
                updateLoadState { current ->
                    if (current.mapLoadGeneration == generation) {
                        current.copy(runningMapPath = currentMapPath, menuBackgroundActive = true)
                            .also { loadedCurrentRequest = true }
                    } else {
                        current
                    }
                }
            }
            if (loadedCurrentRequest) {
                logger.info {
                    "Prepared Android Canvas RW menu background: ${loadState.runningMapPath ?: "<unknown>"} " +
                            "(${elapsedMs(startedAt)}ms)"
                }
            } else {
                logger.info { "Discarded stale Android Canvas RW menu background preparation" }
            }
        }.onFailure { error ->
            if (loadState.mapLoadGeneration == generation) {
                updateLoadState { current ->
                    if (current.mapLoadGeneration == generation) {
                        current.copy(menuBackgroundActive = false, asyncMapLoadError = error)
                    } else {
                        current
                    }
                }
                logger.error(error) { "Android Canvas RW menu background load failed" }
            }
        }.also {
            updateLoadState { current ->
                if (current.mapLoadGeneration == generation) {
                    current.copy(asyncMapLoadInProgress = false, asyncMapLoadPath = null)
                } else {
                    current
                }
            }
        }
    }

    private class AndroidCoreGameView(
        private val menuController: InGameMenuController,
    ) : CoreGameView {
        private val pointerState = MultiTouchPointerState()
        private val pointerXs = FloatArray(MAX_TOUCH_POINTERS)
        private val pointerYs = FloatArray(MAX_TOUCH_POINTERS)
        private var surfaceActive = true
        private var rendering = true

        override fun pause() {
            surfaceActive = false
        }

        override fun isActive(): Boolean = surfaceActive

        override fun isContinuousRendering(): Boolean = true

        override fun isRendering(): Boolean = rendering

        override fun getInGameMenuController(): InGameMenuController = menuController

        override fun onResume() {
            surfaceActive = true
            rendering = true
        }

        override fun getSettings(): MultiTouchPointerState = pointerState

        fun submitPointer(screenX: Float, screenY: Float, isDown: Boolean, pointerId: Int) {
            pointerState.processEvent(screenX, screenY, isDown, pointerId)
        }

        fun submitTouchSnapshot(event: MotionEvent, isDown: Boolean) {
            val count = minOf(event.pointerCount, MAX_TOUCH_POINTERS)
            for (index in 0 until count) {
                pointerXs[index] = event.getX(index)
                pointerYs[index] = event.getY(index)
            }
            pointerState.processTouchSnapshot(
                pointerXs,
                pointerYs,
                count,
                isDown,
                event.buttonState,
            )
        }

        fun clearTouchSnapshot() {
            pointerState.processTouchSnapshot(pointerXs, pointerYs, 0, false, 0)
        }

        fun movePointer(screenX: Float, screenY: Float) {
            pointerState.setStart(screenX, screenY)
        }

        override fun onSizeChanged() = Unit

        override fun stopRender() {
            rendering = false
        }

        private companion object {
            const val MAX_TOUCH_POINTERS = 10
        }
    }

    private companion object {
        const val MENU_BACKGROUND_REQUEST = "<menu-background>"
        const val MENU_BACKGROUND_ZOOM_MULTIPLIER = 2f
    }
}
