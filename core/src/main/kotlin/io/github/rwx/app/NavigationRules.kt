package io.github.rwx.app

import io.github.rwx.ui.AppScreen
import io.github.rwx.ui.AppScreen.*

internal fun supportsBackgroundBattle(screen: AppScreen): Boolean =
    screen != BattleRoom &&
            screen != Loading &&
            screen != Paused &&
            screen != InGame &&
            screen != ModWindow

internal fun shouldShowResumeMenuBackground(screen: AppScreen, canResume: Boolean): Boolean =
    supportsBackgroundBattle(screen) && canResume

internal fun shouldSetRwGameVisibleForScreen(screen: AppScreen): Boolean =
    screen == InGame

internal fun shouldPauseRwGameForScreen(
    screen: AppScreen,
    isResumeBackgroundVisible: Boolean,
): Boolean = screen == Paused || screen == ModWindow || isResumeBackgroundVisible

internal fun shouldShowExternalRwBackgroundSurface(
    isBackgroundBattleVisible: Boolean,
    isResumeBackgroundVisible: Boolean,
    usesFrameCommandRendering: Boolean,
    usesNativeSurfaceForResumeBackground: Boolean = false,
): Boolean =
    !usesFrameCommandRendering &&
            (isBackgroundBattleVisible ||
                    (isResumeBackgroundVisible && usesNativeSurfaceForResumeBackground))

internal fun shouldHandleBattleRoomAction(screen: AppScreen): Boolean =
    screen == BattleRoom


internal fun shouldStartLiveBattleRoomInPlace(
    hasLaunchConfig: Boolean,
    isHost: Boolean,
    isNetworkMultiplayer: Boolean,
    isBattleRoomLive: Boolean,
): Boolean =
    !hasLaunchConfig && isHost && (isNetworkMultiplayer || isBattleRoomLive)

internal fun shouldUseRwCanvasFrameForFrame(
    isRwGameVisible: Boolean,
    isBackgroundBattleVisible: Boolean,
    isResumeBackgroundVisible: Boolean,
    isRwGameLoading: Boolean,
    isLastExternalFrameBackgroundVisible: Boolean,
    usesFrameCommandRendering: Boolean,
): Boolean = isBackgroundBattleVisible ||
        isResumeBackgroundVisible ||
        isLastExternalFrameBackgroundVisible ||
        isRwGameVisible || (usesFrameCommandRendering && isRwGameLoading)

internal fun shouldReturnToMainMenuAfterExternalGameClosed(
    isRwGameVisible: Boolean,
    usesFrameCommandRendering: Boolean,
    isStartingMap: Boolean,
    externalGameWasReady: Boolean,
    canResume: Boolean,
    mapLoadFailed: Boolean = false,
): Boolean =
    isRwGameVisible &&
            !usesFrameCommandRendering &&
            !isStartingMap &&
            (externalGameWasReady || mapLoadFailed) &&
            !canResume

internal fun shouldDiscardExistingGameForStart(
    startNew: Boolean,
    hasLaunchConfig: Boolean,
    usesFrameCommandRendering: Boolean,
    canResume: Boolean,
    canStartNewSessionInPlace: Boolean,
): Boolean =
    startNew &&
            !canStartNewSessionInPlace &&
            (!hasLaunchConfig || usesFrameCommandRendering || canResume)

internal enum class BattleRoomGameStartedAction {
    Ignore,
    PrepareFrameCommandGame,
    ShowExternalGame,
}

internal fun battleRoomGameStartedAction(
    currentScreen: AppScreen,
    usesFrameCommandRendering: Boolean,
    inProcessNetworkGameStarted: Boolean,
): BattleRoomGameStartedAction =
    when {
        currentScreen == InGame -> BattleRoomGameStartedAction.Ignore
        !inProcessNetworkGameStarted -> BattleRoomGameStartedAction.Ignore
        !usesFrameCommandRendering -> BattleRoomGameStartedAction.ShowExternalGame
        else -> BattleRoomGameStartedAction.PrepareFrameCommandGame
    }

internal fun mapStartFailureReturnScreen(requestedReturnScreen: AppScreen?): AppScreen =
    when (requestedReturnScreen) {
        MainMenu,
        LevelSelect,
        ReplaySelect,
        Settings,
        Multiplayer,
        Mods,
        ModWindow,
        ResourceBrowser,
        BattleRoom -> requestedReturnScreen

        Loading,
        Paused,
        InGame,
        null -> MainMenu
    }

internal enum class BackNavigationAction {
    Pause,
    ShowExitDialog,
    LevelSelect,
    BattleRoom,
    MainMenu,
    InGame,
    CloseModWindow,
}

internal fun backActionForScreen(
    screen: AppScreen,
    usesFrameCommandRendering: Boolean,
): BackNavigationAction =
    when (screen) {
        AppScreen.InGame -> if (usesFrameCommandRendering) {
            BackNavigationAction.Pause
        } else {
            BackNavigationAction.ShowExitDialog
        }

        AppScreen.Paused -> BackNavigationAction.ShowExitDialog
        AppScreen.LevelSelect -> BackNavigationAction.LevelSelect
        AppScreen.BattleRoom -> BackNavigationAction.BattleRoom
        // A mod window is opened from the running game; Back returns there rather than to the menu.
        AppScreen.ModWindow -> BackNavigationAction.CloseModWindow
        else -> BackNavigationAction.MainMenu
    }

/** A server disconnect should leave its map picker too, but not redirect unrelated screens. */
internal fun isBattleRoomOwnedScreen(screen: AppScreen, selectingRoomMap: Boolean): Boolean =
    screen == AppScreen.BattleRoom || (screen == AppScreen.LevelSelect && selectingRoomMap)
