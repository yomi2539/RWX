package io.github.rwx.app

import io.github.rwx.ui.AppScreen
import io.github.rwx.ui.AppScreenLayout
import io.github.rwx.ui.InGameOverlayState

internal enum class BackgroundBattleState {
    Inactive,
    Preparing,
    Active,
}

internal enum class GameSurface {
    Hidden,
    InGame,
    MenuBackground,
    ResumeBackground,
    PreparingMenuBackground,
}

internal enum class UiOverlay {
    None,
    Surface,
    Hud,
    InGameOverlay,
    ModWindow,
}

internal data class ScreenPresentation(
    val gameSurface: GameSurface,
    val uiOverlay: UiOverlay,
    val settingsActive: Boolean,
    val menuBackgroundVisible: Boolean,
    val pausedBackground: Boolean,
)

internal object ScreenPresentationPolicy {
    fun present(
        screen: AppScreen,
        showBackgroundBattle: Boolean,
        backgroundBattle: BackgroundBattleState,
        canResume: Boolean,
        inGameOverlay: InGameOverlayState?,
        hasActiveHudLayers: Boolean,
    ): ScreenPresentation {
        val supportsBackground = supportsBackgroundBattle(screen)
        val gameSurface = when {
            screen == AppScreen.InGame -> GameSurface.InGame
            supportsBackground && canResume -> GameSurface.ResumeBackground
            supportsBackground && showBackgroundBattle &&
                    backgroundBattle == BackgroundBattleState.Active ->
                GameSurface.MenuBackground

            supportsBackground && showBackgroundBattle &&
                    backgroundBattle == BackgroundBattleState.Preparing ->
                GameSurface.PreparingMenuBackground

            else -> GameSurface.Hidden
        }
        val uiOverlay = when {
            screen == AppScreen.ModWindow -> UiOverlay.ModWindow
            screen == AppScreen.InGame -> when {
                inGameOverlay?.visible == true -> UiOverlay.InGameOverlay
                inGameOverlay == null && hasActiveHudLayers -> UiOverlay.Hud
                else -> UiOverlay.None
            }

            gameSurface != GameSurface.Hidden -> UiOverlay.Surface
            AppScreenLayout.visibilityFor(screen).hud && hasActiveHudLayers -> UiOverlay.Hud
            else -> UiOverlay.None
        }
        val menuBackgroundVisible =
            gameSurface == GameSurface.MenuBackground ||
                    gameSurface == GameSurface.ResumeBackground

        return ScreenPresentation(
            gameSurface = gameSurface,
            uiOverlay = uiOverlay,
            settingsActive = AppScreenLayout.visibilityFor(screen).settings,
            menuBackgroundVisible = menuBackgroundVisible,
            pausedBackground = screen == AppScreen.Paused ||
                    screen == AppScreen.ModWindow ||
                    gameSurface == GameSurface.ResumeBackground ||
                    inGameOverlay?.pausesGame == true,
        )
    }
}
