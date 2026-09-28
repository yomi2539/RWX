package io.github.rwx.app

import io.github.rwx.logger
import io.github.rwx.render.frame.GameFrame
import io.github.rwx.render.frame.GameViewport
import io.github.rwx.session.GameSession
import io.github.rwx.ui.AppScreen
import io.github.rwx.ui.host.LoadingSceneHost

internal class WarmupController(
    private val gameSession: GameSession,
    private val menuBackgroundSession: GameSession,
    private val loadingSceneHost: LoadingSceneHost,
    private val navigateTo: (AppScreen) -> Unit,
    private val clearExternalFrame: () -> Unit,
    private val isMenuBackgroundDemoEnabled: () -> Boolean,
) {
    private var pendingRwGameLoad = false
    private var warmingRwMapPath: String? = null
    private var deferRwWarmupUntilNextFrame = false
    private var pendingRwPreparation: ((GameViewport) -> Unit)? = null
    private var pendingWarmupStartedAtNanos: Long? = null
    private var pendingWarmupTarget: AppScreen = AppScreen.InGame
    private var pendingStartupMenuBackgroundLoad = false
    private var deferStartupMenuBackgroundUntilNextFrame = false
    private var pendingStartupMenuBackgroundPreparation = false
    private var startupMenuBackgroundSurfaceVisible = false
    private var startupMenuBackgroundReadyFramePending = false

    fun request(mapPath: String?, targetScreen: AppScreen) {
        clearExternalFrame()
        if (mapPath == null && targetScreen == AppScreen.MainMenu) {
            warmingRwMapPath = null
            pendingRwGameLoad = false
            pendingWarmupTarget = targetScreen
            pendingWarmupStartedAtNanos = null
            deferRwWarmupUntilNextFrame = false
            pendingRwPreparation = null
            if (!isMenuBackgroundDemoEnabled()) {
                pendingStartupMenuBackgroundLoad = false
                pendingRwGameLoad = true
                deferRwWarmupUntilNextFrame = true
                pendingRwPreparation = gameSession::prepareEngineAsync
                loadingSceneHost.update(gameSession.loadingStatus())
                navigateTo(AppScreen.Loading)
                return
            }
            pendingStartupMenuBackgroundLoad = true
            deferStartupMenuBackgroundUntilNextFrame = true
            pendingStartupMenuBackgroundPreparation = true
            startupMenuBackgroundReadyFramePending = false
            startupMenuBackgroundSurfaceVisible = false
            loadingSceneHost.update(menuBackgroundSession.loadingStatus())
            navigateTo(AppScreen.Loading)
            return
        }
        if (!gameSession.usesFrameCommandRendering) {
            if (mapPath == null) {
                warmingRwMapPath = null
                pendingRwGameLoad = true
                pendingWarmupTarget = targetScreen
                pendingWarmupStartedAtNanos = null
                deferRwWarmupUntilNextFrame = true
                pendingRwPreparation = gameSession::prepareEngineAsync
                loadingSceneHost.update(gameSession.loadingStatus())
                navigateTo(AppScreen.Loading)
            } else {
                warmingRwMapPath = null
                pendingRwGameLoad = false
                pendingWarmupTarget = targetScreen
                pendingWarmupStartedAtNanos = null
                deferRwWarmupUntilNextFrame = false
                gameSession.requestMap(mapPath)
                navigateTo(targetScreen)
            }
            return
        }
        warmingRwMapPath = mapPath
        pendingRwGameLoad = true
        pendingWarmupTarget = targetScreen
        pendingWarmupStartedAtNanos = null
        deferRwWarmupUntilNextFrame = true
        pendingRwPreparation = if (mapPath == null) {
            gameSession::prepareEngineAsync
        } else {
            { loadingViewport -> gameSession.prepareMapAsync(mapPath, loadingViewport) }
        }
        navigateTo(AppScreen.Loading)
    }

    fun clear() {
        pendingRwGameLoad = false
        warmingRwMapPath = null
        pendingWarmupStartedAtNanos = null
        deferRwWarmupUntilNextFrame = false
        pendingRwPreparation = null
        pendingStartupMenuBackgroundLoad = false
        deferStartupMenuBackgroundUntilNextFrame = false
        pendingStartupMenuBackgroundPreparation = false
        startupMenuBackgroundReadyFramePending = false
        startupMenuBackgroundSurfaceVisible = false
    }

    fun isStartupMenuBackgroundLoading(screen: AppScreen): Boolean =
        screen == AppScreen.Loading && pendingStartupMenuBackgroundLoad

    fun isRwGameLoading(screen: AppScreen): Boolean =
        screen == AppScreen.Loading && pendingRwGameLoad

    fun renderStartupMenuBackgroundLoadingFrame(
        canvasViewport: GameViewport,
        deltaSeconds: Float,
    ): GameFrame {
        if (deferStartupMenuBackgroundUntilNextFrame) {
            deferStartupMenuBackgroundUntilNextFrame = false
            return GameFrame(canvasViewport, emptyList())
        }
        if (pendingStartupMenuBackgroundPreparation) {
            pendingStartupMenuBackgroundPreparation = false
            menuBackgroundSession.prepareMenuBackgroundAsync(canvasViewport)
        }
        val loadError = menuBackgroundSession.mapLoadError(null)
        if (loadError != null) {
            logger.warn(loadError) { "Unable to prepare RW menu background" }
            pendingStartupMenuBackgroundLoad = false
            navigateTo(AppScreen.MainMenu)
            return GameFrame(canvasViewport, emptyList())
        }
        if (!menuBackgroundSession.usesFrameCommandRendering && !startupMenuBackgroundSurfaceVisible) {
            menuBackgroundSession.setGameVisible(
                visible = true,
                viewport = canvasViewport,
                uiOverlay = true,
            )
            startupMenuBackgroundSurfaceVisible = true
        }
        val frame = menuBackgroundSession.updateFrame(
            canvasViewport,
            deltaSeconds,
            drainVisibleLayerBuffers = true,
        )
        if (!menuBackgroundSession.isPreparingMap() &&
            menuBackgroundSession.isMenuBackgroundActive()
        ) {
            if (startupMenuBackgroundReadyFramePending) {
                pendingStartupMenuBackgroundLoad = false
                startupMenuBackgroundReadyFramePending = false
                startupMenuBackgroundSurfaceVisible = false
                navigateTo(AppScreen.MainMenu)
            } else {
                startupMenuBackgroundReadyFramePending = true
            }
        } else {
            startupMenuBackgroundReadyFramePending = false
        }
        return frame
    }

    fun renderRwGameLoadingFrame(
        canvasViewport: GameViewport,
        deltaSeconds: Float,
    ): GameFrame {
        if (deferRwWarmupUntilNextFrame) {
            deferRwWarmupUntilNextFrame = false
            return GameFrame(canvasViewport, emptyList())
        }
        pendingRwPreparation?.let { prepare ->
            pendingRwPreparation = null
            prepare(canvasViewport)
            return GameFrame(canvasViewport, emptyList())
        }
        if (warmingRwMapPath == null) {
            if (gameSession.isPreparingEngine()) {
                return GameFrame(canvasViewport, emptyList())
            }
            val gameEngine = gameSession.preload(canvasViewport)
            gameEngine.settingsEngine.numIncompleteLoadAttempts = 0
            gameEngine.settingsEngine.numLoadsSinceRunningGameOrNormalExit = 0
            gameEngine.settingsEngine.save()
            pendingRwGameLoad = false
            navigateTo(pendingWarmupTarget)
            return GameFrame(canvasViewport, emptyList())
        }
        if (!gameSession.usesFrameCommandRendering) {
            pendingRwGameLoad = false
            warmingRwMapPath = null
            navigateTo(pendingWarmupTarget)
            return GameFrame(canvasViewport, emptyList())
        }
        val warmingMap = warmingRwMapPath
        return if (gameSession.isPreparingMap(warmingMap)) {
            gameSession.currentFrame()
        } else {
            gameSession.updateFrame(
                canvasViewport,
                deltaSeconds,
                drainVisibleLayerBuffers = true,
            )
        }.also {
            val warmupStartedAt = pendingWarmupStartedAtNanos ?: System.nanoTime().also { now ->
                pendingWarmupStartedAtNanos = now
            }
            val warmupElapsedSeconds = (System.nanoTime() - warmupStartedAt) / 1_000_000_000.0
            if (gameSession.isReadyForDisplay(warmingMap) || warmupElapsedSeconds >= 15.0) {
                pendingRwGameLoad = false
                warmingRwMapPath = null
                navigateTo(pendingWarmupTarget)
            }
        }
    }

    fun updateLoadingStatus(screen: AppScreen) {
        val status = if (isStartupMenuBackgroundLoading(screen)) {
            menuBackgroundSession.loadingStatus()
        } else if (isRwGameLoading(screen)) {
            gameSession.loadingStatus()
        } else {
            return
        }
        loadingSceneHost.update(status)
    }
}
