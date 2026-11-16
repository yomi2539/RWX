package io.github.rwx.app

import io.github.rwx.geometry.Viewport
import io.github.rwx.mod.registry.UiRegistry
import io.github.rwx.ui.AppScreen
import io.github.rwx.ui.InGameOverlayState

internal class ScreenPresenter(
    private val bootstrap: AppBootstrap,
    private val viewport: () -> Viewport,
) {
    var presentation = ScreenPresentation(
        gameSurface = GameSurface.Hidden,
        uiOverlay = UiOverlay.None,
        settingsActive = false,
        menuBackgroundVisible = false,
        pausedBackground = false,
    )
        private set

    private var inGameOverlay: InGameOverlayState? = null

    fun updateInGameOverlay(state: InGameOverlayState?, screen: AppScreen) {
        inGameOverlay = state
        if (screen == AppScreen.InGame) apply(screen)
    }

    fun apply(screen: AppScreen) {
        val canResume = bootstrap.gameSession.canResume()
        val background = if (bootstrap.settingsModel.showBackgroundBattleDemo.value &&
            supportsBackgroundBattle(screen) && !canResume
        ) {
            if (!bootstrap.gameSession.isMenuBackgroundActive()) {
                bootstrap.gameSession.prepareMenuBackgroundAsync(viewport())
            }
            backgroundState()
        } else {
            BackgroundBattleState.Inactive
        }
        publish(present(screen, background, canResume))
    }

    /** Engine preparation can finish without a screen transition or a UI event. */
    fun refresh(screen: AppScreen, canResume: Boolean) {
        val next = present(screen, backgroundState(), canResume)
        if (next != presentation) publish(next)
    }

    private fun backgroundState(): BackgroundBattleState = when {
        bootstrap.gameSession.isMenuBackgroundActive() -> BackgroundBattleState.Active
        bootstrap.gameSession.isPreparingMap() -> BackgroundBattleState.Preparing
        else -> BackgroundBattleState.Inactive
    }

    private fun present(
        screen: AppScreen,
        background: BackgroundBattleState,
        canResume: Boolean,
    ): ScreenPresentation = ScreenPresentationPolicy.present(
        screen = screen,
        showBackgroundBattle = bootstrap.settingsModel.showBackgroundBattleDemo.value,
        backgroundBattle = background,
        canResume = canResume,
        inGameOverlay = inGameOverlay.takeIf { screen == AppScreen.InGame },
        hasActiveHudLayers = UiRegistry.hasActiveHudLayers(),
    )

    private fun publish(next: ScreenPresentation) {
        presentation = next
        bootstrap.settingsSceneHost.setActive(next.settingsActive)
        bootstrap.gameSession.setGameVisible(
            next.gameSurface != GameSurface.Hidden,
            viewport(),
            uiOverlay = next.uiOverlay != UiOverlay.None,
            pausedBackground = next.pausedBackground,
        )
    }
}
