package io.github.rwx.app

import io.github.rwx.mod.registry.UiRegistry
import io.github.rwx.session.GameSession
import io.github.rwx.settings.GameSettingsRepository
import io.github.rwx.ui.AppScreen
import io.github.rwx.ui.host.SettingsSceneHost
import io.github.rwx.ui.model.SettingsModel
import io.github.rwx.ui.model.SettingsPage

internal class SessionActions(
    private val gameSession: GameSession,
    private val warmupController: WarmupController,
    private val pendingStartController: PendingStartController,
    private val settingsSceneHost: SettingsSceneHost,
    private val settingsRepository: GameSettingsRepository,
    private val settingsModel: SettingsModel,
    private val modsController: ModsController,
    private val currentScreen: () -> AppScreen,
    private val navigateTo: (AppScreen) -> Unit,
    private val refreshMainMenu: () -> Unit,
    private val onQuit: () -> Unit,
) {
    fun exitRwGameToMainMenu() {
        warmupController.clear()
        pendingStartController.clear()
        navigateTo(AppScreen.MainMenu)
        refreshMainMenu()
    }

    fun returnRwGameToBattleRoom() {
        warmupController.clear()
        pendingStartController.clear()
        navigateTo(AppScreen.BattleRoom)
    }

    fun openInGameSettings() {
        settingsSceneHost.showPage(SettingsPage.Display)
        navigateTo(AppScreen.Settings)
    }

    fun requestInGameSurrender() {
        gameSession.requestSurrender()
        navigateTo(AppScreen.InGame)
    }

    fun openInGameModWindow() = navigateTo(AppScreen.ModWindow)

    fun closeInGameModWindow() {
        UiRegistry.deactivateWindow()
        navigateTo(AppScreen.InGame)
    }

    fun quitApp() {
        warmupController.clear()
        pendingStartController.clear()
        gameSession.discardRunningGame()
        onQuit()
    }

    fun saveCurrentScreenStateBeforeMainMenu() {
        when (currentScreen()) {
            AppScreen.Settings -> settingsRepository.saveFrom(settingsModel)
            AppScreen.Mods -> modsController.applyChangesAndRefresh()
            else -> Unit
        }
    }
}
