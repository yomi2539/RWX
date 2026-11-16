package io.github.rwx.app

import io.github.rwx.geometry.Viewport
import io.github.rwx.logger
import io.github.rwx.session.GameSession
import io.github.rwx.ui.AppScreen
import io.github.rwx.ui.host.LoadingSceneHost

internal class StartupPreparationController(
    private val gameSession: GameSession,
    private val loadingSceneHost: LoadingSceneHost,
    private val navigateTo: (AppScreen) -> Unit,
    private val isBackgroundBattleDemoEnabled: () -> Boolean,
) {
    private var pendingEngineLoad = false
    private var deferEnginePreparation = false
    private var pendingEnginePreparation = false
    private var engineTargetScreen = AppScreen.InGame
    private var pendingStartupMenuBackgroundLoad = false
    private var deferStartupMenuBackground = false
    private var pendingStartupMenuBackgroundPreparation = false
    private var startupMenuBackgroundSurfaceVisible = false
    private var startupMenuBackgroundReadyFramePending = false

    fun request(mapPath: String?, targetScreen: AppScreen) {
        clear()
        if (mapPath != null) {
            gameSession.requestMap(mapPath)
            navigateTo(targetScreen)
            return
        }
        if (targetScreen == AppScreen.MainMenu && isBackgroundBattleDemoEnabled()) {
            pendingStartupMenuBackgroundLoad = true
            deferStartupMenuBackground = true
            pendingStartupMenuBackgroundPreparation = true
        } else {
            pendingEngineLoad = true
            deferEnginePreparation = true
            pendingEnginePreparation = true
            engineTargetScreen = targetScreen
        }
        loadingSceneHost.update(gameSession.loadingStatus())
        navigateTo(AppScreen.Loading)
    }

    fun clear() {
        pendingEngineLoad = false
        deferEnginePreparation = false
        pendingEnginePreparation = false
        pendingStartupMenuBackgroundLoad = false
        deferStartupMenuBackground = false
        pendingStartupMenuBackgroundPreparation = false
        startupMenuBackgroundReadyFramePending = false
        startupMenuBackgroundSurfaceVisible = false
    }

    fun isStartupMenuBackgroundLoading(screen: AppScreen): Boolean =
        screen == AppScreen.Loading && pendingStartupMenuBackgroundLoad

    fun isEngineLoading(screen: AppScreen): Boolean =
        screen == AppScreen.Loading && pendingEngineLoad

    fun renderStartupMenuBackgroundLoadingFrame(canvasViewport: Viewport, deltaSeconds: Float) {
        if (deferStartupMenuBackground) {
            deferStartupMenuBackground = false
            return
        }
        if (pendingStartupMenuBackgroundPreparation) {
            pendingStartupMenuBackgroundPreparation = false
            gameSession.prepareMenuBackgroundAsync(canvasViewport)
        }
        gameSession.mapLoadError(null)?.let { error ->
            logger.warn(error) { "Unable to prepare menu background" }
            pendingStartupMenuBackgroundLoad = false
            navigateTo(AppScreen.MainMenu)
            return
        }
        if (!startupMenuBackgroundSurfaceVisible) {
            gameSession.setGameVisible(true, canvasViewport, uiOverlay = true)
            startupMenuBackgroundSurfaceVisible = true
        }
        gameSession.updateFrame(canvasViewport, deltaSeconds, drainVisibleLayerBuffers = true)
        if (!gameSession.isPreparingMap() && gameSession.isMenuBackgroundActive()) {
            if (startupMenuBackgroundReadyFramePending) {
                clear()
                navigateTo(AppScreen.MainMenu)
            } else {
                startupMenuBackgroundReadyFramePending = true
            }
        } else {
            startupMenuBackgroundReadyFramePending = false
        }
    }

    fun renderEngineLoadingFrame(canvasViewport: Viewport) {
        if (deferEnginePreparation) {
            deferEnginePreparation = false
            return
        }
        if (pendingEnginePreparation) {
            pendingEnginePreparation = false
            gameSession.prepareEngineAsync(canvasViewport)
            return
        }
        if (gameSession.isPreparingEngine()) return
        val engine = gameSession.preload(canvasViewport)
        engine.settingsEngine.numIncompleteLoadAttempts = 0
        engine.settingsEngine.numLoadsSinceRunningGameOrNormalExit = 0
        engine.settingsEngine.save()
        pendingEngineLoad = false
        navigateTo(engineTargetScreen)
    }

    fun updateLoadingStatus(screen: AppScreen) {
        if (isStartupMenuBackgroundLoading(screen) || isEngineLoading(screen)) {
            loadingSceneHost.update(gameSession.loadingStatus())
        }
    }
}
