package io.github.rwx.app

import io.github.rwx.logger
import io.github.rwx.ui.AppScreen

internal class StartupFinalizer(
    private val currentScreen: () -> AppScreen,
    private val multiplayerLobbyController: MultiplayerLobbyController,
    private val resourceBrowserController: ResourceBrowserController,
) {
    fun finish() {
        logPreparedComponents()
        refreshInitialScreenData()
    }

    private fun logPreparedComponents() {
        logger.info { "Starting RWX" }
        logger.info { "Prepared game presenter and Compose state hosts" }
    }

    private fun refreshInitialScreenData() {
        when (currentScreen()) {
            AppScreen.Multiplayer -> multiplayerLobbyController.requestRefresh()
            AppScreen.ResourceBrowser -> resourceBrowserController.requestSearch(append = false)
            else -> Unit
        }
    }
}
