package io.github.rwx.app

import io.github.rwx.ui.AppScreen
import io.github.rwx.ui.AppScreen.*

internal fun supportsBackgroundBattle(screen: AppScreen): Boolean =
    screen != BattleRoom &&
            screen != Loading &&
            screen != Paused &&
            screen != InGame &&
            screen != ModWindow

internal fun shouldStartLiveBattleRoomInPlace(
    hasLaunchConfig: Boolean,
    isHost: Boolean,
    isNetworkMultiplayer: Boolean,
    isBattleRoomLive: Boolean,
): Boolean =
    !hasLaunchConfig && isHost && (isNetworkMultiplayer || isBattleRoomLive)

internal fun shouldReturnToMainMenuAfterGameClosed(
    isGameVisible: Boolean,
    isStartingMap: Boolean,
    gameWasReady: Boolean,
    canResume: Boolean,
    mapLoadFailed: Boolean = false,
): Boolean =
    isGameVisible &&
            !isStartingMap &&
            (gameWasReady || mapLoadFailed) &&
            !canResume

internal enum class BattleRoomGameStartedAction {
    Ignore,
    ShowGame,
}

internal fun battleRoomGameStartedAction(
    currentScreen: AppScreen,
    inProcessNetworkGameStarted: Boolean,
): BattleRoomGameStartedAction =
    when {
        currentScreen == InGame -> BattleRoomGameStartedAction.Ignore
        !inProcessNetworkGameStarted -> BattleRoomGameStartedAction.Ignore
        else -> BattleRoomGameStartedAction.ShowGame
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
    ShowExitDialog,
    LevelSelect,
    BattleRoom,
    MainMenu,
    CloseModWindow,
}

internal fun backActionForScreen(
    screen: AppScreen,
): BackNavigationAction =
    when (screen) {
        AppScreen.InGame -> BackNavigationAction.ShowExitDialog

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
