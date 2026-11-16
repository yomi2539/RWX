package io.github.rwx.ui

import io.github.rwx.ui.model.*

/** Immutable presentation state shared by the legacy frontend and the Compose host. */
data class AppUiState(
    val screen: AppScreen = AppScreen.Loading,
    val composeEnabled: Boolean = false,
    val mainMenuConditions: MainMenuConditions = MainMenuConditions(),
    val mainMenuItems: List<MainMenuItem> = emptyList(),
    val colorSchemeId: ColorSchemeId = ColorSchemeRegistry.defaultSchemeId,
    val enableAnimations: Boolean = true,
    val overlayOpacity: Float = DEFAULT_OVERLAY_OPACITY,
    val menuBackgroundVisible: Boolean = false,
    val legacyOverlayVisible: Boolean = false,
    val settings: SettingsPageState? = null,
    val settingsPages: List<SettingsPage> = emptyList(),
    val levelSelect: LevelSelectUiState? = null,
    val pauseMenuItems: List<PauseMenuItem> = emptyList(),
    val multiplayer: MultiplayerRoomListModel? = null,
    val battleRoom: BattleRoomModel? = null,
    val dialog: DialogUiState? = null,
    val loadingDialog: LoadingDialogUiState? = null,
    val mods: ModsUiState? = null,
    val resourceBrowser: ResourceBrowserModel? = null,
    val replaySelect: ReplaySelectUiState? = null,
    val loading: LoadingUiState? = null,
    val modWindow: ModWindowUiState? = null,
    /** Present only while at least one mod HUD layer is registered and the screen draws the HUD. */
    val modHud: ModHudUiState? = null,
) {
    /** Modal input never reaches the page, even before an async action has been acknowledged. */
    val canInteractWithScreen: Boolean get() = showComposeOverlay && dialog == null && loadingDialog == null

    /** Non-modal in-game overlays leave unconsumed input to the live game. */
    val forwardsInputToGame: Boolean get() = screen == AppScreen.InGame && canInteractWithScreen

    /** The native owner must also stay visible for legacy modals during a renderer handoff. */
    val inGameOverlay: InGameOverlayState?
        get() = if (screen != AppScreen.InGame) null else {
            val modal = legacyOverlayVisible || dialog != null || loadingDialog != null
            InGameOverlayState(visible = showComposeOverlay || modal, pausesGame = modal)
        }

    val showComposeOverlay: Boolean
        get() = composeEnabled && !legacyOverlayVisible && when (screen) {
            AppScreen.MainMenu -> true
            AppScreen.Loading -> loading != null
            AppScreen.LevelSelect -> levelSelect != null
            AppScreen.Paused -> pauseMenuItems.isNotEmpty()
            AppScreen.Multiplayer -> multiplayer != null
            AppScreen.BattleRoom -> battleRoom != null
            AppScreen.Mods -> mods != null
            AppScreen.ResourceBrowser -> resourceBrowser != null
            AppScreen.ReplaySelect -> replaySelect != null
            AppScreen.ModWindow -> modWindow != null
            AppScreen.InGame -> modHud?.layers?.isNotEmpty() == true ||
                dialog != null || loadingDialog != null
            AppScreen.Settings -> settings != null &&
                (settings.page != SettingsPage.KeyBindings || settings.keyBindings != null)
        }
}

data class InGameOverlayState(val visible: Boolean, val pausesGame: Boolean)
