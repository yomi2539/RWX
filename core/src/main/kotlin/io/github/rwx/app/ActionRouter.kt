package io.github.rwx.app

import io.github.rwx.PlatformBridge
import io.github.rwx.i18n.I18n
import io.github.rwx.logger
import io.github.rwx.session.BattleRoomLaunchConfig
import io.github.rwx.settings.GameSettingsRepository
import io.github.rwx.ui.AppScreen
import io.github.rwx.ui.MainMenuNavigation
import io.github.rwx.ui.MainMenuOutcome
import io.github.rwx.ui.ScreenNavigator
import io.github.rwx.ui.host.LevelSelectSceneHost
import io.github.rwx.ui.model.*

internal class ActionRouter(
    private val actions: ActionHandlers,
    private val navigator: ScreenNavigator,
    private val platformBridge: PlatformBridge?,
    private val settingsRepository: GameSettingsRepository,
    private val settingsModel: SettingsModel,
    private val refreshScreen: () -> Unit,
    private val levelSelectSceneHost: LevelSelectSceneHost,
    private val battleRoomController: BattleRoomController,
    private val multiplayerLobbyController: MultiplayerLobbyController,
    private val multiplayerConnectionController: MultiplayerConnectionController,
    private val battleRoomAdminController: BattleRoomAdminController,
    private val battleRoomLaunchController: BattleRoomLaunchController,
    private val modsController: ModsController,
    private val resourceBrowserController: ResourceBrowserController,
    private val resumeRwGame: () -> Unit,
    private val enterRwGame: (startNew: Boolean, launchConfig: BattleRoomLaunchConfig?) -> Unit,
    private val enterReplay: (ReplayEntry) -> Unit,
    private val enterSavedGame: (String) -> Unit,
    private val quitApp: () -> Unit,
    private val showAboutDialog: () -> Unit,
    private val clearPendingStartState: () -> Unit,
    private val openInGameSettings: () -> Unit,
    private val showSaveGameDialog: () -> Unit,
    private val showExitGameDialog: (() -> Unit) -> Unit,
    private val showInGameChatDialog: (Boolean) -> Unit,
    private val showMultiplayerPlayerList: () -> Unit,
    private val requestInGameSurrender: () -> Unit,
    private val exitRwGameToMainMenu: () -> Unit,
    private val showUnavailableDialog: (String) -> Unit,
) {
    fun install() {
        actions.menu = ::handleMenuAction
        actions.levelSelect = ::handleLevelSelectAction
        actions.replaySelect = ::handleReplaySelectAction
        actions.settings = ::handleSettingsAction
        actions.battleRoom = ::handleBattleRoomAction
        actions.pause = ::handlePauseAction
        actions.multiplayer = ::handleMultiplayerAction
        actions.mods = ::handleModsAction
        actions.resourceBrowser = ::handleResourceBrowserAction
    }

    private fun handleMenuAction(action: MainMenuAction) {
        when (val outcome = MainMenuNavigation.outcomeFor(action)) {
            is MainMenuOutcome.Navigate -> {
                if (action == MainMenuAction.Continue && outcome.screen == AppScreen.InGame) {
                    resumeRwGame()
                } else if (action == MainMenuAction.Sandbox) {
                    if (battleRoomController.prepareSandboxGame()) {
                        navigator.navigateTo(AppScreen.BattleRoom)
                    }
                } else if (outcome.screen == AppScreen.InGame) {
                    enterRwGame(true, null)
                } else {
                    navigator.navigateTo(outcome.screen)
                }
            }

            is MainMenuOutcome.NavigateToLevelSelect -> {
                val mode = outcome.mode ?: battleRoomController.selectedMode
                battleRoomController.selectedMode = mode
                levelSelectSceneHost.updateMaps(mode)
                navigator.navigateTo(AppScreen.LevelSelect)
            }

            MainMenuOutcome.QuitApp -> quitApp()
            MainMenuOutcome.ShowAboutDialog -> showAboutDialog()
        }
    }

    private fun handleLevelSelectAction(action: LevelSelectAction) {
        if (action is LevelSelectAction.SelectMode) {
            battleRoomController.selectedMode = action.mode
            levelSelectSceneHost.updateMaps(action.mode)
            return
        }
        when (val outcome = LevelSelectNavigation.outcomeFor(action)) {
            is LevelSelectOutcome.Navigate -> {
                if (battleRoomController.isSelectingMapForBattleRoom) {
                    battleRoomController.isSelectingMapForBattleRoom = false
                    navigator.navigateTo(AppScreen.BattleRoom)
                } else {
                    navigator.navigateTo(outcome.screen)
                }
            }

            is LevelSelectOutcome.StartGame -> handleLevelSelectStartGame(outcome.map)
        }
    }

    private fun handleLevelSelectStartGame(map: MapEntry) {
        if (map.isSavedGame) {
            if (battleRoomController.isSelectingMapForBattleRoom) {
                battleRoomController.selectBattleRoomMap(map)
                navigator.navigateTo(AppScreen.BattleRoom)
            } else {
                enterSavedGame(map.fileName)
            }
            return
        }
        logger.info { "Entering battle room for map: ${map.fileName}" }
        if (battleRoomController.isSelectingMapForBattleRoom) {
            battleRoomController.selectBattleRoomMap(map)
            navigator.navigateTo(AppScreen.BattleRoom)
            return
        }
        battleRoomController.prepareForMap(map)
        navigator.navigateTo(AppScreen.BattleRoom)
    }

    private fun handleReplaySelectAction(action: ReplaySelectAction) {
        if (navigator.current != AppScreen.ReplaySelect) return
        when (val outcome = ReplaySelectNavigation.outcomeFor(action)) {
            is ReplaySelectOutcome.Navigate -> navigator.navigateTo(outcome.screen)
            is ReplaySelectOutcome.WatchReplay -> enterReplay(outcome.replay)
        }
    }

    private fun handleSettingsAction(action: SettingsAction) {
        when (val outcome = SettingsNavigation.outcomeFor(action)) {
            SettingsOutcome.PreviewChanges -> settingsRepository.applyLive(settingsModel)
            SettingsOutcome.ApplyChanges -> {
                settingsRepository.saveFrom(settingsModel)
                refreshScreen()
            }
            is SettingsOutcome.Navigate -> {
                settingsRepository.discardChanges(settingsModel)
                navigator.navigateTo(outcome.screen)
            }
        }
    }

    private fun handleBattleRoomAction(action: BattleRoomAction) {
        if (!shouldHandleBattleRoomAction(navigator.current)) {
            return
        }
        val allowedAction = battleRoomController.resolveCurrentAction(action) ?: return
        when (val outcome = BattleRoomNavigation.outcomeFor(allowedAction)) {
            BattleRoomOutcome.Close -> {
                clearPendingStartState()
                navigator.navigateTo(battleRoomController.closeRoom())
            }

            BattleRoomOutcome.StartGame -> battleRoomLaunchController.startBattleRoomGame()
            is BattleRoomOutcome.SendChat -> battleRoomAdminController.sendBattleRoomChatMessage(outcome.message)
            BattleRoomOutcome.AddAI -> battleRoomAdminController.addAiToBattleRoom()
            BattleRoomOutcome.OpenGameOptions -> battleRoomAdminController.showBattleRoomOptionsDialog()
            BattleRoomOutcome.OpenMapSelect -> {
                battleRoomController.isSelectingMapForBattleRoom = true
                levelSelectSceneHost.updateMaps(battleRoomController.selectedMode)
                navigator.navigateTo(AppScreen.LevelSelect)
            }

            is BattleRoomOutcome.OpenPlayerConfig -> battleRoomAdminController.showPlayerConfigDialog(outcome.playerId)
        }
    }

    private fun handlePauseAction(action: PauseMenuAction) {
        when (val outcome = PauseMenuNavigation.outcomeFor(action)) {
            is PauseMenuOutcome.Navigate -> navigator.navigateTo(outcome.screen)
            PauseMenuOutcome.SaveGame -> showSaveGameDialog()
            PauseMenuOutcome.OpenSettings -> openInGameSettings()
            PauseMenuOutcome.Chat -> {
                navigator.navigateTo(AppScreen.InGame)
                showInGameChatDialog(false)
            }

            PauseMenuOutcome.Players -> {
                navigator.navigateTo(AppScreen.InGame)
                showMultiplayerPlayerList()
            }

            PauseMenuOutcome.Surrender -> requestInGameSurrender()
            PauseMenuOutcome.ExitGame -> showExitGameDialog(exitRwGameToMainMenu)
        }
    }

    private fun handleMultiplayerAction(action: MultiplayerAction) {
        when (val outcome = MultiplayerNavigation.outcomeFor(action)) {
            is MultiplayerOutcome.Navigate -> navigator.navigateTo(outcome.screen)
            MultiplayerOutcome.RefreshRequested -> multiplayerLobbyController.requestRefresh()
            is MultiplayerOutcome.SwitchLobby -> multiplayerLobbyController.switchLobby(outcome.lobbyKind)
            MultiplayerOutcome.HostGameRequested -> multiplayerConnectionController.hostMultiplayerGame()
            MultiplayerOutcome.JoinDirectRequested -> multiplayerConnectionController.showJoinDirectDialog()
            is MultiplayerOutcome.JoinDirectWithAddressRequested ->
                multiplayerConnectionController.joinDirectWithAddress(outcome.address)
            MultiplayerOutcome.JoinLastGameRequested -> multiplayerConnectionController.joinLastGame()
            MultiplayerOutcome.ConfigurePlayerNameRequested -> multiplayerConnectionController.showPlayerNameDialog()
            is MultiplayerOutcome.JoinRoom -> multiplayerConnectionController.showJoinRoomDialog(outcome.roomId)
        }
    }

    private fun handleModsAction(action: ModsAction) {
        if (navigator.current != AppScreen.Mods) return
        when (val outcome = ModsNavigation.outcomeFor(action)) {
            is ModsOutcome.Navigate -> {
                modsController.applyChangesAndRefresh()
                navigator.navigateTo(outcome.screen)
            }

            ModsOutcome.ReloadRequested -> modsController.reloadWithDialog()
            ModsOutcome.ImportRequested -> modsController.showImportDialog()
            ModsOutcome.DisableAllRequested -> modsController.disableAllAndRefresh()
            ModsOutcome.ApplyRequested -> {
                modsController.applyChangesAndRefresh()
                navigator.navigateTo(AppScreen.MainMenu)
            }

            is ModsOutcome.ToggleEnable -> modsController.toggleEnabledAndRefresh(outcome.modId)
            is ModsOutcome.Delete -> modsController.deleteAndRefresh(outcome.modId)
            is ModsOutcome.ShowDescription -> modsController.showDetails(outcome.modId, error = false)
            is ModsOutcome.ShowError -> modsController.showDetails(outcome.modId, error = true)
        }
    }

    private fun handleResourceBrowserAction(action: ResourceBrowserAction) {
        if (navigator.current != AppScreen.ResourceBrowser) return
        when (val outcome = ResourceBrowserNavigation.outcomeFor(action)) {
            is ResourceBrowserOutcome.Navigate -> navigator.navigateTo(outcome.screen)
            ResourceBrowserOutcome.SearchRequested -> resourceBrowserController.requestSearch(append = false)
            is ResourceBrowserOutcome.SearchSubmitted -> resourceBrowserController.submitSearch(outcome.keyword)
            ResourceBrowserOutcome.LoadMoreRequested -> resourceBrowserController.requestSearch(append = true)
            is ResourceBrowserOutcome.TypeSelected -> resourceBrowserController.selectType(outcome.type)
            is ResourceBrowserOutcome.OpenLink -> {
                val url = outcome.item.bbsUrl
                if (url.isNullOrBlank() || platformBridge?.openUrl(url) != true) {
                    showUnavailableDialog(I18n.mainmenu.about.openLinkFailed(url.orEmpty()))
                }
            }

            is ResourceBrowserOutcome.Download -> resourceBrowserController.download(outcome.item)
        }
    }
}
