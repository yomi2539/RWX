package io.github.rwx.app

import io.github.rwx.mod.registry.UiRegistry
import io.github.rwx.ui.AppScreen
import io.github.rwx.ui.AppScreenLayout
import io.github.rwx.ui.AppUiState
import io.github.rwx.ui.InGameOverlayState
import io.github.rwx.ui.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** The frontend owns all mutable game/UI state; other UI threads only receive snapshots. */
internal class AppUiController(
    private val bootstrap: AppBootstrap,
    private val currentScreen: () -> AppScreen,
    private val mainMenuConditions: () -> MainMenuConditions,
    private val menuBackgroundVisible: () -> Boolean,
    private val onBack: () -> Unit,
    private val closeModWindow: () -> Unit,
    /** Synchronizes the native owner for HUD, notifications, modals and legacy fallback. */
    private val refreshInGameOverlay: (InGameOverlayState?) -> Unit,
    private val gameInputSink: OverlayGameInputSink = OverlayGameInputSink(bootstrap.gameSession),
) {
    private var composeEnabled = false
    private val dialogOwnership = DialogUiOwnership()
    private var inGameOverlay: InGameOverlayState? = null

    fun dispatch(appAction: AppAction) = when (appAction) {
        is AppAction.Menu -> submit {
            if (canDispatchPageAction(AppScreen.MainMenu) &&
                MainMenuViewModel.items(mainMenuConditions()).any { it.action == appAction.action }
            ) {
                bootstrap.actions.menu(appAction.action)
            }
        }

        is AppAction.LevelSelect -> submit {
            if (canDispatchPageAction(AppScreen.LevelSelect)) {
                bootstrap.levelSelectSceneHost.snapshot().resolveAction(appAction.revision, appAction.action)
                    ?.let(bootstrap.actions.levelSelect)
            }
        }

        is AppAction.Pause -> submit {
            if (canDispatchPageAction(AppScreen.Paused) &&
                bootstrap.pauseSceneHost.snapshotItems().any { it.action == appAction.action }
            ) {
                bootstrap.actions.pause(appAction.action)
            }
        }

        is AppAction.Multiplayer -> submit {
            if (canDispatchPageAction(AppScreen.Multiplayer)) {
                bootstrap.multiplayerSceneHost.snapshot()
                    .resolveAction(appAction.lobbyKind, appAction.revision, appAction.action)
                    ?.let(bootstrap.actions.multiplayer)
            }
        }

        is AppAction.BattleRoom -> submit {
            if (canDispatchPageAction(AppScreen.BattleRoom)) {
                bootstrap.battleRoomSceneHost.snapshot().resolveAction(appAction.revision, appAction.action)
                    ?.let(bootstrap.actions.battleRoom)
            }
        }

        is AppAction.Mods -> submit {
            if (canDispatchPageAction(AppScreen.Mods)) {
                bootstrap.modsSceneHost.snapshot().resolveAction(appAction.revision, appAction.action)
                    ?.let(bootstrap.actions.mods)
            }
        }

        is AppAction.ResourceBrowser -> submit {
            if (canDispatchPageAction(AppScreen.ResourceBrowser)) {
                bootstrap.resourceBrowserSceneHost.currentModel().resolveAction(appAction.revision, appAction.action)
                    ?.let(bootstrap.actions.resourceBrowser)
            }
        }

        is AppAction.ReplaySelect -> submit {
            if (canDispatchPageAction(AppScreen.ReplaySelect)) {
                bootstrap.replaySelectSceneHost.snapshot().resolveAction(appAction.revision, appAction.action)
                    ?.let(bootstrap.actions.replaySelect)
            }
        }

        AppAction.Back -> submit {
            if (currentScreen() != AppScreen.Loading && canDispatchPageAction(mutableState.value.screen)) {
                // Escape first ends a mod's world-position pick, exactly like the legacy key listener.
                if (!bootstrap.settingsSceneHost.keyBindingEditor.cancelCapture() && !UiRegistry.cancelWorldPositionSelection()) onBack()
            }
        }

        is AppAction.ModWindow -> submit {
            if (canDispatchPageAction(AppScreen.ModWindow) && UiRegistry.windowSnapshot().revision == appAction.revision) {
                when (appAction.action) {
                    ModWindowAction.Close -> closeModWindow()
                }
            }
        }

        is AppAction.GameInput -> scope.launch {
            val published = mutableState.value
            val forwarding = composeEnabled && published.forwardsInputToGame && currentScreen() == AppScreen.InGame &&
                    !bootstrap.dialogSceneHost.isVisible && !bootstrap.loadingDialogSceneHost.isVisible
            if (appAction.event == GameInputEvent.ReleaseAll || !forwarding) gameInputSink.release()
            else gameInputSink.handle(appAction.event)
        }

        is AppAction.LoadingCancel -> submit {
            val published = mutableState.value
            if (composeEnabled && published.showComposeOverlay && published.loadingDialog?.revision == appAction.revision) {
                bootstrap.loadingDialogSceneHost.cancel(appAction.revision)
            }
        }

        is AppAction.Settings -> dispatchSettingsAction(appAction.action)
        is AppAction.Dialog -> dispatchDialogAction(appAction.revision, appAction.action)
    }

    private fun dialogOwner(): DialogUiOwner {
        val screen = currentScreen()
        val inRoom = screen == AppScreen.BattleRoom
        return DialogUiOwner(
            screen,
            if (inRoom) bootstrap.battleRoomSceneHost.snapshot().revision else null,
            if (inRoom) bootstrap.gameSession.currentBattleRoom(refreshNetworkStatus = false)?.isHost else null,
        )
    }

    private fun canDispatchPageAction(screen: AppScreen): Boolean =
        composeEnabled && mutableState.value.canInteractWithScreen && currentScreen() == screen &&
                !bootstrap.dialogSceneHost.isVisible && !bootstrap.loadingDialogSceneHost.isVisible

    private val scope =
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Main.immediate)

    fun close() {
        scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
        gameInputSink.release()
    }

    private val mutableState = MutableStateFlow(AppUiState())
    val state = mutableState.asStateFlow()

    fun setComposeEnabled(enabled: Boolean) = submit {
        if (composeEnabled != enabled) bootstrap.settingsSceneHost.keyBindingEditor.cancelCapture()
        composeEnabled = enabled
    }


    fun dispatchSettingsAction(action: SettingsUiAction) = submit {
        if (action is SettingsUiAction.CancelKeyCapture) {
            bootstrap.settingsSceneHost.keyBindingEditor.cancelCapture(action.requestId)
            return@submit
        }
        if (!canDispatchPageAction(AppScreen.Settings)) return@submit
        when (action) {
            is SettingsUiAction.SelectPage -> {
                if (action.page in visibleSettingsPages()) bootstrap.settingsSceneHost.showPage(action.page)
            }

            is SettingsUiAction.SelectColorScheme -> {
                val page = bootstrap.settingsSceneHost.pageContent()
                if (page.page == SettingsPage.Theme &&
                    page.items.filterIsInstance<SettingsPageItem.ColorSchemeSelector>().any { it.item.id == action.id }
                ) {
                    bootstrap.settingsModel.selectedColorSchemeId.value = action.id
                    bootstrap.actions.settings(SettingsAction.ApplyChanges)
                }
            }

            is SettingsUiAction.BeginKeyCapture -> bootstrap.settingsSceneHost.keyBindingEditor.beginCapture(action.capture)
            is SettingsUiAction.CaptureKey -> bootstrap.settingsSceneHost.keyBindingEditor.captureKey(
                action.requestId,
                action.keyCode,
                action.modifiers
            )

            is SettingsUiAction.ClearKeyBinding -> bootstrap.settingsSceneHost.keyBindingEditor.clear(action.target)
            is SettingsUiAction.SelectRenderBackend -> {
                val pageContent = bootstrap.settingsSceneHost.pageContent()
                if (pageContent.page == SettingsPage.Display &&
                    pageContent.items.filterIsInstance<SettingsPageItem.RenderBackend>().isNotEmpty()
                ) {
                    val normalized = if (action.backendId.lowercase() == "skia") "skia" else "slick"
                    bootstrap.settingsModel.desktopRenderBackend.value = normalized
                    bootstrap.actions.settings(SettingsAction.ApplyChanges)
                }
            }

            is SettingsUiAction.SelectStorage -> {
                val page = bootstrap.settingsSceneHost.pageContent()
                if (page.page == SettingsPage.Display &&
                    page.items.filterIsInstance<SettingsPageItem.StorageLocation>().isNotEmpty() &&
                    (action.storageType == 0 || action.storageType == 2)
                ) {
                    bootstrap.settingsModel.storageType.value = action.storageType
                    bootstrap.actions.settings(SettingsAction.ApplyChanges)
                }
            }

            is SettingsUiAction.RequestExternalStorage -> {
                val host = bootstrap.platformBridge?.filePickerHost ?: return@submit
                host.requestExternalStorage { selection ->
                    submit {
                        if (selection == null) return@submit
                        if (!canDispatchPageAction(AppScreen.Settings)) return@submit
                        bootstrap.settingsRepository.saveExternalStorageLink(selection.uri)
                        bootstrap.settingsModel.storageType.value = 2
                        bootstrap.actions.settings(SettingsAction.ApplyChanges)
                    }
                }
            }

            SettingsUiAction.Back -> {
                bootstrap.settingsSceneHost.keyBindingEditor.cancelCapture()
                bootstrap.actions.settings(SettingsAction.Back)
            }

            else -> bootstrap.settingsSceneHost.pageContent().applyEdit(action)?.let(bootstrap.actions.settings)
        }
    }

    fun dispatchDialogAction(revision: Long, action: DialogUiAction) = submit {
        val published = mutableState.value
        if (published.dialog?.revision != revision || !dialogOwnership.canDispatch(
                revision,
                dialogOwner()
            )
        ) return@submit
        // Let already queued edits finish during a renderer handoff; never submit behind a legacy modal.
        val isEdit = action is DialogUiAction.EditInput || action is DialogUiAction.EditField
        if (!isEdit && (!published.showComposeOverlay || !composeEnabled ||
                    bootstrap.loadingDialogSceneHost.isVisible)
        ) return@submit
        bootstrap.dialogSceneHost.dispatch(revision, action)
    }


    private fun submit(action: () -> Unit) {
        scope.launch {
            runCatching {
                action()
            }.also {
                refresh(force = true)
            }
        }
    }

    /** Called after the frame driver, including changes originating in legacy screens or the engine. */
    fun refresh(force: Boolean = false) {
        val dialogHost = bootstrap.dialogSceneHost
        val loadingHost = bootstrap.loadingDialogSceneHost
        dialogHost.refreshValidity()
        // Timer callbacks may have opened a modal or navigated; read live state afterwards.
        val screen = currentScreen()
        // Notifications are non-modal and must not interrupt key capture or page editing.
        bootstrap.settingsSceneHost.setActive(screen == AppScreen.Settings && !dialogHost.isVisible && !loadingHost.isVisible)
        if (!composeEnabled && !force) return
        var dialog = dialogHost.snapshot()
        val owner = dialogOwner()
        if (dialog != null && dialogOwnership.shouldDismiss(dialog.revision, owner)) {
            dialogHost.hide(dialog.revision)
            dialog = null
        }
        val loadingDialog = loadingHost.snapshot()
        val legacyOverlayVisible = dialogHost.isVisible && dialog == null
        val conditions = mainMenuConditions()
        val modHud =
            UiRegistry.hudSnapshot().takeIf { AppScreenLayout.visibilityFor(screen).hud && it.layers.isNotEmpty() }
        val snapshot = AppUiState(
            screen = screen,
            composeEnabled = composeEnabled,
            mainMenuConditions = conditions,
            mainMenuItems = MainMenuViewModel.items(conditions),
            colorSchemeId = bootstrap.settingsModel.selectedColorSchemeId.value,
            enableAnimations = bootstrap.settingsModel.enableAnimations.value,
            overlayOpacity = bootstrap.settingsModel.overlayOpacity.value,
            menuBackgroundVisible = menuBackgroundVisible(),
            legacyOverlayVisible = legacyOverlayVisible,
            dialog = dialog,
            loadingDialog = loadingDialog,
            settings = if (screen == AppScreen.Settings) bootstrap.settingsSceneHost.snapshot() else null,
            settingsPages = visibleSettingsPages(),
            levelSelect = if (screen == AppScreen.LevelSelect) bootstrap.levelSelectSceneHost.snapshot() else null,
            pauseMenuItems = if (screen == AppScreen.Paused) bootstrap.pauseSceneHost.snapshotItems() else emptyList(),
            multiplayer = if (screen == AppScreen.Multiplayer) bootstrap.multiplayerSceneHost.snapshot() else null,
            battleRoom = if (screen == AppScreen.BattleRoom) bootstrap.battleRoomSceneHost.snapshot() else null,
            mods = if (screen == AppScreen.Mods) bootstrap.modsSceneHost.snapshot() else null,
            resourceBrowser = if (screen == AppScreen.ResourceBrowser) bootstrap.resourceBrowserSceneHost.currentModel() else null,
            replaySelect = if (screen == AppScreen.ReplaySelect) bootstrap.replaySelectSceneHost.snapshot() else null,
            loading = if (screen == AppScreen.Loading) bootstrap.loadingSceneHost.snapshot() else null,
            modWindow = if (screen == AppScreen.ModWindow) UiRegistry.windowSnapshot() else null,
            modHud = modHud,
        )
        if (!snapshot.forwardsInputToGame) gameInputSink.release()
        // A modal callback may restore the game and immediately open another modal. Re-apply
        // on identity changes too, even if both overlays have the same visibility/pause flags.
        val previous = mutableState.value
        if (inGameOverlay != snapshot.inGameOverlay ||
            (screen == AppScreen.InGame &&
                    (dialog?.revision != previous.dialog?.revision || loadingDialog?.revision != previous.loadingDialog?.revision))
        ) {
            inGameOverlay = snapshot.inGameOverlay
            refreshInGameOverlay(inGameOverlay)
        }
        dialogOwnership.publish(dialog?.revision, owner, snapshot.showComposeOverlay && loadingDialog == null)
        dialogHost.setComposeOwned(snapshot.showComposeOverlay && snapshot.dialog != null)
        loadingHost.setComposeOwned(snapshot.showComposeOverlay && loadingDialog != null)
        // Keep the native canvases alive. Only suppress the legacy UI scenes actually replaced by Compose.
        mutableState.value = snapshot
    }
}
