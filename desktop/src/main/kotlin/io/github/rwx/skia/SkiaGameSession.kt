package io.github.rwx.skia

import com.corrodinggames.rts.gameFramework.GameEngine
import io.github.rwx.DesktopRenderBackend
import io.github.rwx.PlatformStorage
import io.github.rwx.app.launchOnIO
import io.github.rwx.bench.BenchFrameProbe
import io.github.rwx.geometry.Rect
import io.github.rwx.input.MultiTouchPointerState
import io.github.rwx.logger
import io.github.rwx.platform.CoreGameView
import io.github.rwx.render.RenderBackend
import io.github.rwx.render.SkiaGraphicsEngine
import io.github.rwx.render.canvas.Paint
import io.github.rwx.render.frame.GameFrame
import io.github.rwx.render.frame.GameViewport
import io.github.rwx.session.GameSession
import io.github.rwx.session.GameSessionRendererProfile
import io.github.rwx.session.BattleRoomLaunchConfig
import io.github.rwx.session.MapSnapshot
import io.github.rwx.session.hasActiveStartedGameConnection
import io.github.rwx.ui.BattleRoomUiBridge
import io.github.rwx.ui.InGameMenuController
import org.jetbrains.skia.Canvas
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt

internal fun startedSkiaMapPath(engine: GameEngine): String? =
    engine.networkEngine?.selectedMapPath?.takeIf { it.isNotBlank() }
        ?: engine.currentMapPath?.takeIf { it.isNotBlank() }

class SkiaGameSession(
    storage: PlatformStorage,
    registerShutdownHook: Boolean = true,
) : GameSession(), AutoCloseable {
    override val renderBackend: RenderBackend = DesktopRenderBackend.Skia

    private val view = SkiaCoreGameView(inGameMenuController)
    private val graphics = SkiaGraphicsEngine(storage)
    private val input = SkiaGameInputQueue()
    private val closed = AtomicBoolean(false)
    private var droppedFrameCount = 0

    @Volatile
    private var coordinateScale: Float = 1f
    override val inputCoordinateScale: Float
        get() = coordinateScale

    fun setInputCoordinateScale(scale: Float) {
        if (!closed.get() && scale.isFinite() && scale > 0f) coordinateScale = scale
    }

    @Volatile
    private var requestedGameVisible = false

    @Volatile
    private var pausedBackground = false

    fun isGameVisible(): Boolean = requestedGameVisible

    fun graphicsEngine(): SkiaGraphicsEngine = graphics

    private val hiddenFill = Paint().apply { setColor(0xFF000000.toInt()) }

    private val frameProbe by lazy { BenchFrameProbe() }

    init {
        configureRendererProfile(
            GameSessionRendererProfile(
                usesFrameCommandRendering = false,
                canStartNewSessionInPlace = true,
                usesNativeSurfaceForResumeBackground = false,
            )
        )
        if (registerShutdownHook) {
            Runtime.getRuntime().addShutdownHook(
                Thread({ runCatching { close() } }, "skia-session-shutdown")
            )
        }
    }

    fun renderInto(canvas: Canvas, width: Int, height: Int, deltaSeconds: Float) {
        if (closed.get() || isEngineBusyForUiReads()) return
        val probeOn = frameProbe.isEnabled
        val startNanos = if (probeOn) System.nanoTime() else 0L
        synchronized(gameLock) {
            if (closed.get() || isEngineBusyForUiReads()) return
            val viewport = GameViewport(width.coerceAtLeast(1), height.coerceAtLeast(1))
            lastViewport = viewport
            val engine = ensureStarted(viewport)
            applyViewport(engine, viewport)
            loadPendingMap(engine)
            drainInput(engine)
            val hidden = !requestedGameVisible && !pausedBackground
            val paused = !hidden && shouldRenderPausedFrame(engine)
            // Active frames redraw visible cells after updating the camera. Draining
            // here would rebuild the old grid just before a zoom invalidates it.
            if ((hidden || paused) && engine.hasLoadedLevel && engine.tileMap != null) {
                com.corrodinggames.rts.game.map.TileMap.layerBufferManager
                    .renderPendingRedraws(LAYER_REDRAW_BUDGET_MS)
            }
            runCatching {
                if (hidden) {
                    graphics.render(canvas, viewport.width, viewport.height) {
                        it.a(Rect(0, 0, viewport.width, viewport.height), hiddenFill)
                    }
                } else if (paused) {
                    graphics.render(canvas, viewport.width, viewport.height) {
                        (engine as com.corrodinggames.rts.game.GameLogic).drawWorldOnlyThreadSafe(0f)
                    }
                } else {
                    graphics.render(canvas, viewport.width, viewport.height) {
                        runGameLoop(engine, deltaSeconds)
                    }
                }
            }.onFailure { error ->
                droppedFrameCount++
                if (droppedFrameCount <= 3) {
                    logger.warn(error) { "Skia frame render failed; skipping frame" }
                }
            }
            lastFrame = GameFrame(viewport, emptyList())
        }
        if (probeOn) frameProbe.frame(System.nanoTime() - startNanos)
    }

    override fun updateFrame(
        viewport: GameViewport,
        deltaSeconds: Float,
        drainVisibleLayerBuffers: Boolean,
    ): GameFrame {
        if (closed.get() || isEngineBusyForUiReads()) return lastFrame
        synchronized(gameLock) {
            if (closed.get() || isEngineBusyForUiReads()) return lastFrame
            val resolved = viewport.takeIf { it.width > 0 && it.height > 0 }
                ?: GameViewport(1280, 720)
            lastViewport = resolved
            val engine = ensureStarted(resolved)
            applyViewport(engine, resolved)
            loadPendingMap(engine)
            drainInput(engine)
            lastFrame = GameFrame(resolved, emptyList())
            return lastFrame
        }
    }

    override fun adoptStartedGameFromEngine(viewport: GameViewport): Boolean {
        return !(closed.get() || isEngineBusyForUiReads()) && synchronized(gameLock) {
            if (closed.get() || isEngineBusyForUiReads()) return@synchronized false
            val engine = gameEngine ?: GameEngine.getInstance() ?: return@synchronized false
            if (engine.networkEngine?.hasActiveStartedGameConnection() != true) return@synchronized false

            lastViewport = viewport
            gameEngine = engine
            applyViewport(engine, viewport)

            BattleRoomUiBridge.setupGame()
            if (!engine.hasLoadedLevel || engine.networkEngine?.hasActiveStartedGameConnection() != true) {
                return@synchronized false
            }
            val activeMapPath = startedSkiaMapPath(engine) ?: return@synchronized false

            updateLoadState {
                it.copy(
                    mapLoadGeneration = it.mapLoadGeneration + 1,
                    pendingMapPath = null,
                    pendingMapSnapshot = null,
                    pendingRendererBattleRoomConfig = null,
                    asyncMapLoadPath = null,
                    asyncMapLoadInProgress = false,
                    asyncMapLoadError = null,
                    menuBackgroundActive = false,
                    runningMapPath = activeMapPath,
                )
            }
            input.submit(SkiaGameInputQueue.Event.Reset)
            engine.isStopped = false
            engine.isPaused = false
            true
        }
    }

    override fun currentFrame(): GameFrame = lastFrame

    override fun canResume(): Boolean = !closed.get() && super.canResume()

    override fun isMapLoaded(mapPath: String?): Boolean = !closed.get() && super.isMapLoaded(mapPath)

    override fun preload(viewport: GameViewport): GameEngine {
        check(!closed.get()) { "Game session is closed" }
        return super.preload(viewport)
    }

    override fun prepareEngineAsync(viewport: GameViewport) {
        if (!closed.get()) super.prepareEngineAsync(viewport)
    }

    override fun requestMap(mapPath: String?) {
        if (!closed.get()) super.requestMap(mapPath)
    }

    override fun requestMapSnapshot(snapshot: MapSnapshot) {
        if (!closed.get()) super.requestMapSnapshot(snapshot)
    }

    override fun prepareMapAsync(mapPath: String?, viewport: GameViewport) {
        if (!closed.get()) super.prepareMapAsync(mapPath, viewport)
    }

    override fun prepareSavedGameAsync(saveName: String, viewport: GameViewport) {
        if (!closed.get()) super.prepareSavedGameAsync(saveName, viewport)
    }

    override fun prepareMapSnapshotAsync(snapshot: MapSnapshot, viewport: GameViewport) {
        if (!closed.get()) super.prepareMapSnapshotAsync(snapshot, viewport)
    }

    override fun prepareReplayAsync(replayName: String, viewport: GameViewport) {
        if (!closed.get()) super.prepareReplayAsync(replayName, viewport)
    }

    override fun prepareBattleRoomAsync(config: BattleRoomLaunchConfig, viewport: GameViewport) {
        if (!closed.get()) super.prepareBattleRoomAsync(config, viewport)
    }

    override fun <T> runEngineCommand(label: String, command: (GameEngine) -> T?): T? {
        if (closed.get()) return null
        return synchronized(gameLock) {
            if (closed.get()) null else super.runEngineCommand(label, command)
        }
    }

    override fun loadPendingMapNow(): GameFrame =
        updateFrame(lastViewport, 0f)

    override fun prepareMenuBackgroundAsync(viewport: GameViewport) {
        if (closed.get()) return
        val state = loadState
        if (state.menuBackgroundActive) return
        if (state.runningMapPath != null && gameEngine?.hasLoadedLevel == true) return
        var generation: Long? = null
        updateLoadState { current ->
            if (closed.get() || current.asyncMapLoadInProgress || current.menuBackgroundActive) {
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
        logger.info { "Preparing desktop-skia menu background asynchronously" }
        launchOnIO("desktop-skia-menu-background-loader") {
            loadMenuBackgroundInBackground(viewport, issuedGeneration)
        }
    }

    override fun ensureStarted(viewport: GameViewport): GameEngine {
        check(!closed.get()) { "Game session is closed" }
        synchronized(gameLock) {
            check(!closed.get()) { "Game session is closed" }
            activeEngineLocked()?.let { return it }
            val resolved = viewport.takeIf { it.width > 0 && it.height > 0 }
                ?: GameViewport(1280, 720)
            return ensureRendererEngine(resolved, graphics, view, null)
        }
    }

    override fun applyViewport(engine: GameEngine, viewport: GameViewport) {
        check(!closed.get()) { "Game session is closed" }
        val width = viewport.width.coerceAtLeast(1)
        val height = viewport.height.coerceAtLeast(1)
        lastViewport = GameViewport(width, height)
        engine.updateWindowResolution(width, height)
        graphics.a(width, height)
        view.onSizeChanged()
    }

    override fun setGameVisible(
        visible: Boolean,
        viewport: GameViewport,
        uiOverlay: Boolean,
        pausedBackground: Boolean,
    ) {
        if (closed.get()) return
        requestedGameVisible = visible
        this.pausedBackground = pausedBackground
        if (viewport.width > 0 && viewport.height > 0) {
            lastViewport = viewport
        }
    }

    override fun submitPointer(screenX: Float, screenY: Float, isDown: Boolean, pointerId: Int) {
        input.submit(SkiaGameInputQueue.Event.Pointer(screenX, screenY, isDown, pointerId), isEngineBusyForUiReads())
    }

    override fun movePointer(screenX: Float, screenY: Float) {
        input.submit(SkiaGameInputQueue.Event.Move(screenX, screenY), isEngineBusyForUiReads())
    }

    override fun submitKey(androidKeyCode: Int, isDown: Boolean) {
        input.submit(SkiaGameInputQueue.Event.Key(androidKeyCode, isDown), isEngineBusyForUiReads())
    }

    override fun submitMouseWheel(amount: Int) {
        if (amount != 0) input.submit(SkiaGameInputQueue.Event.Wheel(amount), isEngineBusyForUiReads())
    }

    override fun clearInputState() {
        input.submit(SkiaGameInputQueue.Event.Reset)
    }

    private fun drainInput(engine: GameEngine) {
        input.drain { event ->
            when (event) {
                is SkiaGameInputQueue.Event.Pointer -> view.submitPointer(event.x, event.y, event.down, event.button)
                is SkiaGameInputQueue.Event.Move -> view.movePointer(event.x, event.y)
                is SkiaGameInputQueue.Event.Key -> engine.setKeyState(event.code, event.down)
                is SkiaGameInputQueue.Event.Wheel -> engine.queueMouseWheelDelta(event.amount)
                SkiaGameInputQueue.Event.Reset -> {
                    view.submitPointer(0f, 0f, false, -1)
                    engine.clearInputState()
                }
            }
        }
    }

    override fun close() {
        synchronized(gameLock) {
            if (!closed.compareAndSet(false, true)) return
            requestedGameVisible = false
            pausedBackground = false
            updateLoadState {
                SessionLoadState(mapLoadGeneration = it.mapLoadGeneration + 1)
            }
            input.close()
            view.submitPointer(0f, 0f, false, -1)
            view.stopRender()
            gameEngine?.clearInputState()
            lastFrame = GameFrame(lastViewport, emptyList())
            runCatching { graphics.close() }
                .onFailure { error -> logger.warn(error) { "Failed to close the Skia graphics engine" } }
        }
    }

    private fun loadMenuBackgroundInBackground(requestedViewport: GameViewport, generation: Long) {
        val startedAt = System.nanoTime()
        var loadedCurrentRequest = false
        runCatching {
            synchronized(gameLock) {
                if (closed.get() || generation != loadState.mapLoadGeneration) return@synchronized
                val viewport = requestedViewport.takeIf { it.width > 0 && it.height > 0 }
                    ?: GameViewport(1280, 720)
                lastViewport = viewport
                val engine = ensureStarted(viewport)
                applyViewport(engine, viewport)
                engine.isStopped = true
                engine.isPaused = true
                engine.loadMenuBackground()
                if (!engine.hasLoadedLevel || !engine.isMenuBackgroundMap) {
                    error("Menu background map did not load")
                }
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
                logger.info { "Prepared desktop-skia menu background in ${elapsedMs(startedAt)}ms" }
            } else {
                logger.info { "Discarded stale desktop-skia menu background preparation" }
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
                logger.warn(error) { "Desktop-skia menu background load failed" }
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

    private fun runGameLoop(engine: GameEngine, deltaSeconds: Float) {
        runCatching {
            engine.gameLoop(
                deltaSeconds.toGameSpeedDelta(),
                (deltaSeconds * 1000f).roundToInt().coerceAtLeast(0),
            )
        }.onFailure { error ->
            logger.warn(error) { "Skia game loop failed" }
        }
    }

    private fun shouldRenderPausedFrame(engine: GameEngine): Boolean {
        if (!engine.hasLoadedLevel || engine.tileMap == null) return false
        if (engine.isMenuBackgroundMap) return false
        if (engine.isNetworkGameActive()) return false
        return pausedBackground || engine.gameSpeed == 0f || engine.isPaused || engine.isStopped
    }

    private class SkiaCoreGameView(
        private val menuController: InGameMenuController,
    ) : CoreGameView {
        private val pointerState = MultiTouchPointerState()
        private var rendering = true

        fun submitPointer(screenX: Float, screenY: Float, isDown: Boolean, pointerId: Int) {
            pointerState.processEvent(screenX, screenY, isDown, pointerId)
        }

        fun movePointer(screenX: Float, screenY: Float) {
            pointerState.setStart(screenX, screenY)
        }

        override fun pause() {
            rendering = false
        }

        override fun isActive(): Boolean = rendering

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

    private companion object {
        const val MENU_BACKGROUND_REQUEST = "<menu-background>"
        const val LAYER_REDRAW_BUDGET_MS = 2
    }
}
