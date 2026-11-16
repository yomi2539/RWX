package io.github.rwx.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalWindowInfo
import io.github.rwx.ui.component.AppDialog
import io.github.rwx.ui.component.AppLoadingDialog
import io.github.rwx.ui.input.DesktopKeyBindingCapture
import io.github.rwx.ui.input.forwardUnconsumedInputToGame
import io.github.rwx.ui.model.*
import io.github.rwx.ui.screen.*
import io.github.rwx.ui.theme.Spacing
import io.github.rwx.ui.theme.UiTheme

/** Only migrated screens are rendered here. The host gives all other screens back to Kool. */
@Composable
fun ComposeOverlayContent(
    state: AppUiState,
    onMenuAction: (MainMenuAction) -> Unit,
    onSettingsAction: (SettingsUiAction) -> Unit,
    onBack: () -> Unit,
    onLevelSelectAction: (Long, LevelSelectAction) -> Unit = { _, _ -> },
    onPauseAction: (PauseMenuAction) -> Unit = {},
    onMultiplayerAction: (MultiplayerLobbyKind, Long, MultiplayerAction) -> Unit = { _, _, _ -> },
    onBattleRoomAction: (Long, BattleRoomAction) -> Unit = { _, _ -> },
    onDialogAction: (Long, DialogUiAction) -> Unit = { _, _ -> },
    onLoadingCancel: (Long) -> Unit = {},
    onModsAction: (Long, ModsAction) -> Unit = { _, _ -> },
    onResourceBrowserAction: (Long, ResourceBrowserAction) -> Unit = { _, _ -> },
    onReplaySelectAction: (Long, ReplaySelectAction) -> Unit = { _, _ -> },
    onModWindowAction: (Long, ModWindowAction) -> Unit = { _, _ -> },
    onGameInput: (GameInputEvent) -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }
    val screenStateHolder = rememberSaveableStateHolder()
    val dialogStateHolder = rememberSaveableStateHolder()
    val dialogKey = state.dialog?.revision
    var previousDialogKey by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(dialogKey) {
        if (dialogKey != previousDialogKey) {
            previousDialogKey?.let(dialogStateHolder::removeState)
            previousDialogKey = dialogKey
        }
    }
    val roomStateKey = state.battleRoom?.takeIf { state.screen == AppScreen.BattleRoom }?.let { "BattleRoom:${it.revision}" }
    var previousRoomKey by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(roomStateKey) {
        if (roomStateKey != null && roomStateKey != previousRoomKey) {
            previousRoomKey?.let(screenStateHolder::removeState)
            previousRoomKey = roomStateKey
        }
    }
    var escapePressedHere by remember { mutableStateOf(false) }
    val keyCaptureHandler = remember { DesktopKeyBindingCapture() }
    var pendingCapture by remember { mutableStateOf<KeyBindingCapture?>(null) }
    val keyPageVisible = state.canInteractWithScreen && state.screen == AppScreen.Settings && state.settings?.page == SettingsPage.KeyBindings
    val keyBindings = state.settings?.keyBindings
    fun currentCapture(): KeyBindingCapture? = if (!keyPageVisible) null else {
        pendingCapture?.takeIf { it.requestId > (keyBindings?.lastCaptureRequestId ?: 0L) } ?: keyBindings?.activeCapture
    }
    val activeCapture = currentCapture()
    val dispatchSettings: (SettingsUiAction) -> Unit = dispatch@{ action ->
        if (!state.canInteractWithScreen && action !is SettingsUiAction.CancelKeyCapture) return@dispatch
        when (action) {
            is SettingsUiAction.BeginKeyCapture -> {
                pendingCapture = action.capture
                focusRequester.requestFocus()
            }
            is SettingsUiAction.CaptureKey -> keyCaptureHandler.finish(action.requestId)
            is SettingsUiAction.CancelKeyCapture -> keyCaptureHandler.finish(action.requestId)
            is SettingsUiAction.ClearKeyBinding, is SettingsUiAction.SelectPage, SettingsUiAction.Back -> {
                keyCaptureHandler.finish(currentCapture()?.requestId)
                pendingCapture = null
            }
            else -> Unit
        }
        onSettingsAction(action)
    }
    val windowInfo = LocalWindowInfo.current
    val windowFocused = windowInfo.isWindowFocused
    val currentGameInput by rememberUpdatedState(onGameInput)
    val forwardGameInput = remember {
        { event: GameInputEvent ->
            // Clicking back on the game returns keyboard focus from a HUD/notification control.
            if (event is GameInputEvent.PointerButton && event.isDown) focusRequester.requestFocus()
            currentGameInput(event)
        }
    }
    val forwardsGameInput = state.forwardsInputToGame && windowFocused
    DisposableEffect(forwardsGameInput) {
        onDispose { if (forwardsGameInput) currentGameInput(GameInputEvent.ReleaseAll) }
    }
    LaunchedEffect(windowFocused, keyPageVisible) {
        if (!windowFocused) escapePressedHere = false
        if (!windowFocused || !keyPageVisible) {
            (pendingCapture ?: keyBindings?.activeCapture)?.let { onSettingsAction(SettingsUiAction.CancelKeyCapture(it.requestId)) }
            pendingCapture = null
            keyCaptureHandler.reset()
        }
    }
    LaunchedEffect(state.screen, state.showComposeOverlay, roomStateKey, dialogKey, state.loadingDialog?.revision) {
        escapePressedHere = false
        if (state.canInteractWithScreen) focusRequester.requestFocus()
    }
    UiTheme(state.colorSchemeId, overlayOpacity = state.overlayOpacity) {
        Box(Modifier.fillMaxSize().focusRequester(focusRequester).onPreviewKeyEvent { event ->
            if (!state.showComposeOverlay || !windowInfo.isWindowFocused) {
                false
            } else if (state.dialog != null || state.loadingDialog != null) {
                // Modal keys belong to AppDialog's focus boundary, never to the page/capture.
                false
            } else if (state.screen == AppScreen.Loading && event.key == Key.Escape) {
                // Full-screen startup is not a cancellable loading dialog.
                escapePressedHere = false
                true
            } else if (keyCaptureHandler.handle(event, currentCapture().takeIf { windowInfo.isWindowFocused }, dispatchSettings)) {
                escapePressedHere = false
                true
            } else if (state.screen == AppScreen.InGame) {
                escapePressedHere = false
                false
            } else if (event.key != Key.Escape) {
                false
            } else when (event.type) {
                KeyEventType.KeyDown -> {
                    escapePressedHere = true
                    true
                }
                KeyEventType.KeyUp -> {
                    // A popup may consume Escape-down and close before Escape-up arrives here.
                    // That release must not also navigate out of the underlying screen.
                    val handled = escapePressedHere
                    escapePressedHere = false
                    if (handled) {
                        val room = state.battleRoom
                        val modWindow = state.modWindow
                        if (state.screen == AppScreen.BattleRoom && room != null) {
                            onBattleRoomAction(room.revision, BattleRoomAction.Back)
                        } else if (state.screen == AppScreen.ModWindow && modWindow != null) {
                            onModWindowAction(modWindow.revision, ModWindowAction.Close)
                        } else onBack()
                    }
                    handled
                }
                else -> false
            }
        }.let { if (forwardsGameInput) it.forwardUnconsumedInputToGame(forwardGameInput) else it }.focusable()) {
            val snackbarHostState = remember { SnackbarHostState() }
            LaunchedEffect(state.mods?.noticeRevision) {
                val mods = state.mods
                val text = mods?.noticeText.orEmpty()
                val revision = mods?.noticeRevision ?: 0L
                if (mods != null && revision > 0 && text.isNotBlank()) {
                    onModsAction(mods.revision, ModsAction.ConsumeNotice(revision))
                    snackbarHostState.showSnackbar(message = text)
                }
            }
            if (state.showComposeOverlay) {
                // HUD layers sit under the pause menu and any modal, and are the whole in-game overlay.
                state.modHud?.let { ModHudOverlay(it) }
                screenStateHolder.SaveableStateProvider(roomStateKey ?: state.screen.name) {
                    val appear = remember(state.screen.name, roomStateKey) {
                        MutableTransitionState(false).apply { targetState = true }
                    }
                    val screenEnter = if (!state.enableAnimations) EnterTransition.None else when (state.screen) {
                        AppScreen.Multiplayer, AppScreen.Settings, AppScreen.Paused -> fadeIn() + slideInVertically()
                        AppScreen.MainMenu, AppScreen.Loading, AppScreen.ModWindow -> fadeIn()
                        else -> fadeIn() + expandIn()
                    }
                    AnimatedVisibility(
                        appear,
                        enter = screenEnter,
                        exit = ExitTransition.None,
                    ) {
                        when (state.screen) {
                        AppScreen.Loading -> state.loading?.let { LoadingScreen(it) }
                        AppScreen.MainMenu -> MainMenuScreen(
                            conditions = state.mainMenuConditions,
                            items = state.mainMenuItems,
                            colorSchemeId = state.colorSchemeId,
                            menuBackgroundVisible = state.menuBackgroundVisible,
                            enableAnimations = state.enableAnimations,
                            onMenuAction = { if (state.canInteractWithScreen) onMenuAction(it) },
                        )
                        AppScreen.Settings -> state.settings?.let {
                            val rendered = if (keyPageVisible && keyBindings != null) {
                                it.copy(keyBindings = keyBindings.copy(activeCapture = activeCapture))
                            } else it
                            SettingsScreen(rendered, state.settingsPages, dispatchSettings, enableAnimations = state.enableAnimations)
                        }
                        AppScreen.LevelSelect -> state.levelSelect?.let { levelSelect ->
                            LevelSelectScreen(levelSelect, onAction = { if (state.canInteractWithScreen) onLevelSelectAction(levelSelect.revision, it) }, enableAnimations = state.enableAnimations)
                        }
                        AppScreen.Paused -> PauseMenuScreen(state.pauseMenuItems) { if (state.canInteractWithScreen) onPauseAction(it) }
                        AppScreen.Multiplayer -> state.multiplayer?.let { lobby ->
                            MultiplayerScreen(lobby, onAction = { if (state.canInteractWithScreen) onMultiplayerAction(lobby.lobbyKind, lobby.revision, it) }, enableAnimations = state.enableAnimations)
                        }
                        AppScreen.BattleRoom -> state.battleRoom?.let { room ->
                            BattleRoomScreen(room, onAction = { if (state.canInteractWithScreen) onBattleRoomAction(room.revision, it) }, enableAnimations = state.enableAnimations)
                        }
                        AppScreen.Mods -> state.mods?.let { mods ->
                            ModsScreen(mods, onAction = { if (state.canInteractWithScreen) onModsAction(mods.revision, it) }, enableAnimations = state.enableAnimations)
                        }
                        AppScreen.ResourceBrowser -> state.resourceBrowser?.let { browser ->
                            ResourceBrowserScreen(browser, onAction = { if (state.canInteractWithScreen) onResourceBrowserAction(browser.revision, it) }, enableAnimations = state.enableAnimations)
                        }
                        AppScreen.ReplaySelect -> state.replaySelect?.let { replays ->
                            ReplaySelectScreen(replays, onAction = { if (state.canInteractWithScreen) onReplaySelectAction(replays.revision, it) }, enableAnimations = state.enableAnimations)
                        }
                        AppScreen.ModWindow -> state.modWindow?.let { window ->
                            ModWindowScreen(window, onAction = { if (state.canInteractWithScreen) onModWindowAction(window.revision, it) })
                        }
                        else -> Unit
                    }
                    }
                }
                val loading = state.loadingDialog
                val dialog = state.dialog
                if (loading != null) {
                    androidx.compose.runtime.key(loading.revision) {
                        AppLoadingDialog(loading, enableAnimations = state.enableAnimations) { onLoadingCancel(loading.revision) }
                    }
                } else if (dialog != null) {
                    dialogStateHolder.SaveableStateProvider(dialog.revision) {
                        AppDialog(dialog, enableAnimations = state.enableAnimations) { onDialogAction(dialog.revision, it) }
                    }
                } else {
                    SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(Spacing.lg))
                }
            }
        }
    }
}
