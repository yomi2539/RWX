package io.github.rwx.ui

import android.os.Handler
import android.os.Looper
import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.rwx.app.AppAction
import io.github.rwx.app.AppSession
import io.github.rwx.ui.component.AppDialog
import io.github.rwx.ui.component.AppLoadingDialog
import io.github.rwx.ui.model.*
import io.github.rwx.ui.platform.PlatformComposeHost
import io.github.rwx.ui.screen.*
import io.github.rwx.ui.theme.Spacing
import io.github.rwx.ui.theme.UiTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


class AndroidComposeOverlay(
    initialState: AppUiState,
    hostFactory: (onDispose: () -> Unit) -> PlatformComposeHost,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : PlatformComposeHost {
    private val mainHandler = Handler(Looper.getMainLooper())

    init { checkMain() }

    private val state = MutableStateFlow(initialState)
    private var session: AppSession? = null
    private var disableSession: (() -> Unit)? = null
    private var disposed = false
    private val host = hostFactory(::release)

    init {
        host.setContent {
            val snapshot by state.collectAsState()
            AndroidOverlayContent(
                state = snapshot,
                onMenuAction = { session?.dispatch(AppAction.Menu(it)) },
                onSettingsAction = { session?.dispatch(AppAction.Settings(it)) },
                onBack = { session?.dispatch(AppAction.Back) },
                onLevelSelectAction = { revision, action ->
                    session?.dispatch(
                        AppAction.LevelSelect(
                            revision,
                            action
                        )
                    )
                },
                onPauseAction = { session?.dispatch(AppAction.Pause(it)) },
                onMultiplayerAction = { kind, revision, action ->
                    session?.dispatch(
                        AppAction.Multiplayer(
                            kind,
                            revision,
                            action
                        )
                    )
                },
                onBattleRoomAction = { revision, action -> session?.dispatch(AppAction.BattleRoom(revision, action)) },
                onDialogAction = { revision, action -> session?.dispatch(AppAction.Dialog(revision, action)) },
                onLoadingCancel = { session?.dispatch(AppAction.LoadingCancel(it)) },
                onModsAction = { revision, action -> session?.dispatch(AppAction.Mods(revision, action)) },
                onResourceBrowserAction = { revision, action ->
                    session?.dispatch(
                        AppAction.ResourceBrowser(
                            revision,
                            action
                        )
                    )
                },
                onReplaySelectAction = { revision, action ->
                    session?.dispatch(
                        AppAction.ReplaySelect(
                            revision,
                            action
                        )
                    )
                },
                onModWindowAction = { revision, action -> session?.dispatch(AppAction.ModWindow(revision, action)) },
            )
        }
        scope.launch {
            state.map { it.showComposeOverlay }.distinctUntilChanged().collect { visible ->
                mainHandler.post { if (!disposed) host.setVisible(visible) }
            }
        }
    }

    fun attach(session: AppSession) {
        if (!isMain()) {
            mainHandler.post { attach(session) }
            return
        }
        if (disposed || this.session === session) return
        check(this.session == null) { "The overlay is already attached to a session" }
        this.session = session
        bindState(session.uiState, session::setComposeUiEnabled)
    }

    internal fun bindState(sessionState: StateFlow<AppUiState>, setEnabled: (Boolean) -> Unit) {
        checkMain()
        if (disposed) return
        check(disableSession == null) { "The overlay state is already bound" }
        disableSession = { setEnabled(false) }
        scope.launch {
            sessionState.dropWhile { !it.composeEnabled }.collect { state.value = it }
        }
        setEnabled(true)
    }

    override val isVisible: Boolean get() = host.isVisible

    override fun setContent(content: @Composable () -> Unit) {
        checkMain()
        if (!disposed) host.setContent(content)
    }

    override fun setVisible(visible: Boolean) {
        checkMain()
        if (!disposed) host.setVisible(visible)
    }

    override fun requestFocus() {
        checkMain()
        if (!disposed) host.requestFocus()
    }

    override fun dispose() {
        checkMain()
        if (disposed) return
        try {
            host.dispose()
        } finally {
            release()
        }
    }

    private fun release() {
        if (disposed) return
        disposed = true
        scope.cancel()
        val disable = disableSession
        disableSession = null
        session = null
        disable?.invoke()
    }

    private fun checkMain() {
        check(isMain()) { "Compose overlay lifecycle operations must run on the main thread" }
    }

    private fun isMain(): Boolean = Looper.getMainLooper().isCurrentThread
}

@Composable
internal fun AndroidOverlayContent(
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
) {
    val screenStateHolder = rememberSaveableStateHolder()
    val dialogStateHolder = rememberSaveableStateHolder()
    val roomStateKey = state.battleRoom?.takeIf { state.screen == AppScreen.BattleRoom }?.let { "BattleRoom:${it.revision}" }
    UiTheme(state.colorSchemeId) {
        Box(Modifier.fillMaxSize()) {
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
                                SettingsScreen(it, state.settingsPages, onSettingsAction, enableAnimations = state.enableAnimations)
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
