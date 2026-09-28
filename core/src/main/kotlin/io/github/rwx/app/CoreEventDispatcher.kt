package io.github.rwx.app

import io.github.rwx.ui.AppScreen
import io.github.rwx.ui.CoreUiEvent
import io.github.rwx.ui.CoreUiEventQueue

internal class CoreEventDispatcher(
    private val currentScreen: () -> AppScreen,
    private val multiplayerLobbyController: MultiplayerLobbyController,
    private val battleRoomController: BattleRoomController,
    private val battleRoomLaunchController: BattleRoomLaunchController,
    private val resourceBrowserController: ResourceBrowserController,
    private val inGameDialogController: InGameDialogController,
    private val mapController: MapController,
    private val openInGameSettings: () -> Unit,
    private val requestInGameSurrender: () -> Unit,
    private val exitRwGameToMainMenu: () -> Unit,
    private val returnRwGameToBattleRoom: () -> Unit,
    private val openInGameModWindow: () -> Unit,
    private val closeInGameModWindow: () -> Unit,
    private val refreshMenuBackground: () -> Unit,
    private val onBattleRoomClosed: (reason: String?, message: String?) -> Unit,
) {
    fun drain(isRenderLoopFrame: Boolean) {
        CoreUiEventQueue.drain { event ->
            when (event) {
                CoreUiEvent.OriginalRoomListRefresh -> multiplayerLobbyController.handleOriginalRoomListRefresh()
                CoreUiEvent.P2PRoomListRefresh -> multiplayerLobbyController.handleP2PRoomListRefresh()
                CoreUiEvent.BattleRoomRefresh -> battleRoomController.updateFromNetwork()
                CoreUiEvent.BattleRoomGameStarted -> battleRoomLaunchController.handleBattleRoomGameStarted()
                CoreUiEvent.ResourceBrowserSearchCompleted -> resourceBrowserController.handleSearchCompleted()
                CoreUiEvent.ResourceBrowserDownloadProgress -> resourceBrowserController.handleDownloadProgress()
                CoreUiEvent.ResourceBrowserDownloadCompleted -> resourceBrowserController.handleDownloadCompleted()
                CoreUiEvent.MenuBackgroundReady -> {
                    if (supportsBackgroundBattle(currentScreen())) refreshMenuBackground()
                }
                CoreUiEvent.InGameSaveRequested -> inGameDialogController.showSaveGameDialog()
                CoreUiEvent.InGameExportMapRequested -> inGameDialogController.showExportMapDialog()
                CoreUiEvent.InGameSettingsRequested -> openInGameSettings()
                CoreUiEvent.InGameSurrenderRequested -> requestInGameSurrender()
                CoreUiEvent.InGameExitRequested ->
                    inGameDialogController.showExitGameDialog(exitRwGameToMainMenu)

                CoreUiEvent.InGameReturnToBattleRoomRequested -> {
                    returnRwGameToBattleRoom()
                    battleRoomController.returnToRoom()
                }

                is CoreUiEvent.InGameChatRequested -> inGameDialogController.showInGameChatDialog(event.teamOnly)
                CoreUiEvent.InGamePlayerListRequested -> inGameDialogController.showInGamePlayerListDialog()
                is CoreUiEvent.InGameMapJumpRequested -> mapController.jumpToLinkedMap(event)
                CoreUiEvent.InGameMapListRequested -> mapController.showMapSwitchDialog()
                CoreUiEvent.InGameModWindowRequested -> openInGameModWindow()
                CoreUiEvent.InGameModWindowBackRequested -> closeInGameModWindow()
                is CoreUiEvent.InGameMapPortalTransferRequested -> mapController.handlePortalTransfer(event.transfer)
                is CoreUiEvent.MessageDialogRequested -> inGameDialogController.showLegacyMessageDialog(event)
                is CoreUiEvent.PasswordDialogRequested -> inGameDialogController.showLegacyPasswordDialog(event)
                is CoreUiEvent.FormDialogRequested -> inGameDialogController.showLegacyFormDialog(event)
                is CoreUiEvent.BattleRoomChatMessage ->
                    battleRoomController.appendChat(event.text, event.teamColorIndex)

                is CoreUiEvent.BattleRoomClosed -> onBattleRoomClosed(event.reason, event.message)
            }
        }
    }
}
