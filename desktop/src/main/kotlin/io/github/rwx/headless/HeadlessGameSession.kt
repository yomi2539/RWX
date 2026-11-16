package io.github.rwx.headless

import com.corrodinggames.rts.gameFramework.GameEngine
import com.corrodinggames.rts.gameFramework.InputController
import com.corrodinggames.rts.gameFramework.MusicManager
import com.corrodinggames.rts.gameFramework.NullMusicFactory
import com.corrodinggames.rts.gameFramework.audio.NullSound
import com.corrodinggames.rts.gameFramework.audio.SoundEngine
import com.corrodinggames.rts.gameFramework.utility.AssetInputStream
import io.github.rwx.DesktopInputHandler
import io.github.rwx.DesktopRenderBackend
import io.github.rwx.PlatformStorage
import io.github.rwx.geometry.Viewport
import io.github.rwx.input.MultiTouchPointerState
import io.github.rwx.logger
import io.github.rwx.platform.CoreGameView
import io.github.rwx.render.RenderBackend
import io.github.rwx.render.SkiaGraphicsEngine
import io.github.rwx.session.BattleRoomLaunchConfig
import io.github.rwx.session.GameSession
import io.github.rwx.session.MapSnapshot
import io.github.rwx.session.hasActiveStartedGameConnection
import io.github.rwx.ui.BattleRoomUiBridge
import io.github.rwx.ui.CoreUiEvent
import io.github.rwx.ui.CoreUiEventQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt

/** Caller-driven simulation. The legacy engine requires one session per OS process. */
class HeadlessGameSession(
    storage: PlatformStorage,
    registerShutdownHook: Boolean = true,
) : GameSession(), AutoCloseable {
    override val renderBackend: RenderBackend = DesktopRenderBackend.Headless
    override val defaultPreloadViewport = Viewport(640, 480)
    private val closed = AtomicBoolean(false)
    private val graphicsResource = lazy { SkiaGraphicsEngine(storage) }
    private val graphics by graphicsResource
    private val view = HeadlessGameView()
    private val shutdownHook = if (registerShutdownHook) {
        Thread({ close() }, "headless-session-shutdown").also { Runtime.getRuntime().addShutdownHook(it) }
    } else null

    val tick: Int get() = synchronized(gameLock) { gameEngine?.currentTick ?: 0 }

    override fun prepareForNewSession() = Unit

    fun step(deltaSeconds: Float = 1f / 60f) = updateFrame(defaultPreloadViewport, deltaSeconds)

    override fun updateFrame(viewport: Viewport, deltaSeconds: Float, drainVisibleLayerBuffers: Boolean) {
        require(deltaSeconds.isFinite() && deltaSeconds >= 0f) { "Simulation delta must be finite and non-negative" }
        if (closed.get() || isEngineBusyForUiReads()) return
        synchronized(gameLock) {
            if (closed.get() || isEngineBusyForUiReads()) return
            val resolved = viewport.takeIf { it.width > 0 && it.height > 0 } ?: defaultPreloadViewport
            val engine = ensureStarted(resolved)
            if (lastViewport != resolved) applyViewport(engine, resolved)
            loadPendingMap(engine)
            if (!engine.hasLoadedLevel && engine.networkEngine.hasActiveStartedGameConnection()) {
                check(adoptStartedGameFromEngine(resolved)) { "Unable to load the started network game" }
            }
            CoreUiEventQueue.drain { event ->
                when (event) {
                    is CoreUiEvent.MessageDialogRequested -> logger.info { "${event.title}: ${event.message}" }
                    is CoreUiEvent.PasswordDialogRequested -> {
                        event.handler.cancelPasswordEntry()
                        latestBattleRoomJoinError = "The server requires a password"
                    }
                    is CoreUiEvent.BattleRoomClosed -> {
                        latestBattleRoomJoinError = listOfNotNull(event.reason, event.message)
                            .joinToString(": ").ifBlank { "Server closed the connection" }
                    }
                    else -> Unit
                }
            }
            // Unloaded rooms still need packet processing, without activating a missing level.
            engine.isStopped = !engine.hasLoadedLevel || engine.isStopped
            val gameDelta = (deltaSeconds * 60f).coerceAtMost(3f)
            if (deltaSeconds > 0f) {
                if (!engine.hasLoadedLevel) engine.networkEngine.a(gameDelta)
                engine.shouldSkipNextDraw = true
                engine.gameLoop(gameDelta, (deltaSeconds * 1000f).roundToInt())
            }
        }
    }

    override fun loadPendingMapNow() = updateFrame(lastViewport, 0f)

    // Other backends may adopt the process singleton; this session only uses its own engine.
    override fun activeEngineLocked(): GameEngine? = gameEngine

    override fun ensureStarted(viewport: Viewport): GameEngine = synchronized(gameLock) {
        synchronized(GameEngine::class.java) { startLocked(viewport) }
    }

    private fun startLocked(viewport: Viewport): GameEngine {
        check(!closed.get()) { "Game session is closed" }
        gameEngine?.let { return it }
        check(GameEngine.getInstance() == null) { "A game engine already exists; use a separate process for a headless session" }
        System.setProperty("java.awt.headless", "true")
        GameEngine.isHeadlessMode = true
        GameEngine.isMenuBackgroundDisabled = true
        GameEngine.isNonAndroidVersion = true
        GameEngine.isDesktopInitialized = true
        GameEngine.isJavaDesktopVersion = true
        GameEngine.isPCOrIOSVersion = false
        InputController.b = DesktopInputHandler()
        MusicManager.musicFactory = NullMusicFactory()
        SoundEngine.soundFactory = object : NullSound() {
            override fun a(name: String?, stream: AssetInputStream?, registerByName: Boolean) =
                try { super.a(name, stream, registerByName) } finally { stream?.close() }
        }
        val resolved = viewport.takeIf { it.width > 0 && it.height > 0 } ?: defaultPreloadViewport
        return ensureRendererEngine(resolved, graphics, view).also {
            it.soundEngine.soundDisabled = true
            it.isStopped = true
        }
    }

    override fun applyViewport(engine: GameEngine, viewport: Viewport) {
        check(!closed.get()) { "Game session is closed" }
        lastViewport = Viewport(viewport.width.coerceAtLeast(1), viewport.height.coerceAtLeast(1))
        engine.updateWindowResolution(lastViewport.width, lastViewport.height)
        graphics.a(lastViewport.width, lastViewport.height)
    }

    override fun adoptStartedGameFromEngine(viewport: Viewport): Boolean = synchronized(gameLock) {
        if (closed.get()) return@synchronized false
        val engine = gameEngine ?: return@synchronized false
        if (!engine.networkEngine.hasActiveStartedGameConnection()) return@synchronized false
        if (!engine.hasLoadedLevel) BattleRoomUiBridge.setupGame()
        if (!engine.hasLoadedLevel) return@synchronized false
        applyViewport(engine, viewport)
        updateLoadState { it.copy(runningMapPath = engine.currentMapPath, menuBackgroundActive = false) }
        engine.isStopped = false
        engine.isPaused = false
        true
    }

    override fun submitKey(androidKeyCode: Int, isDown: Boolean) = Unit
    override fun submitMouseWheel(amount: Int) = Unit

    override fun prepareEngineAsync(viewport: Viewport) = whileOpen { super.prepareEngineAsync(viewport) }
    override fun requestMap(mapPath: String?) = whileOpen { super.requestMap(mapPath) }
    override fun requestMapSnapshot(snapshot: MapSnapshot) = whileOpen { super.requestMapSnapshot(snapshot) }
    override fun prepareMapAsync(mapPath: String?, viewport: Viewport) =
        whileOpen { super.prepareMapAsync(mapPath, viewport) }

    override fun prepareSavedGameAsync(saveName: String, viewport: Viewport) =
        whileOpen { super.prepareSavedGameAsync(saveName, viewport) }

    override fun prepareReplayAsync(replayName: String, viewport: Viewport) =
        whileOpen { super.prepareReplayAsync(replayName, viewport) }

    override fun prepareMapSnapshotAsync(snapshot: MapSnapshot, viewport: Viewport) =
        whileOpen { super.prepareMapSnapshotAsync(snapshot, viewport) }

    override fun prepareBattleRoomAsync(config: BattleRoomLaunchConfig, viewport: Viewport) =
        whileOpen { super.prepareBattleRoomAsync(config, viewport) }

    private fun whileOpen(action: () -> Unit) {
        synchronized(gameLock) { if (!closed.get()) action() }
    }

    override fun <T> runEngineCommand(label: String, command: (GameEngine) -> T?): T? = synchronized(gameLock) {
        if (closed.get()) null else super.runEngineCommand(label, command)
    }

    override fun close() {
        synchronized(gameLock) {
            if (!closed.compareAndSet(false, true)) return
            cancelBattleRoomJoin()
            super.discardRunningGame()
            view.stopRender()
            try {
                gameEngine?.stopAndReset()
                gameEngine?.musicManager?.release()
            } finally {
                if (graphicsResource.isInitialized()) graphics.close()
            }
        }
        shutdownHook?.let { runCatching { Runtime.getRuntime().removeShutdownHook(it) } }
    }

    private inner class HeadlessGameView : CoreGameView {
        private val pointers = MultiTouchPointerState()
        override fun isActive() = !closed.get()
        override fun isContinuousRendering() = false
        override fun isRendering() = false
        override fun getInGameMenuController() = this@HeadlessGameSession.inGameMenuController
        override fun getSettings() = pointers
        override fun pause() = Unit
        override fun onResume() = Unit
        override fun onSizeChanged() = Unit
        override fun stopRender() = Unit
    }
}
