package io.github.rwx.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.rwx.app.AppSession
import io.github.rwx.ui.platform.PlatformComposeHost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.launch
import javax.swing.SwingUtilities

/** Owns in-frame Compose content from bootstrap through the application session. */
class DesktopComposeOverlay constructor(
    initialState: AppUiState,
    hostFactory: (onDispose: () -> Unit) -> PlatformComposeHost,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val onStateChanged: (AppUiState) -> Unit = {},
) : PlatformComposeHost {
    init { checkEdt() }

    private val state = MutableStateFlow(initialState)
    private var session: AppSession? = null
    private var disableSession: (() -> Unit)? = null
    private var disposed = false
    private val host = hostFactory(::release)

    init {
        host.setContent {
            val snapshot by state.collectAsState()
            ComposeOverlayContent(
                state = snapshot,
                onMenuAction = { session?.dispatchMenuAction(it) },
                onSettingsAction = { session?.dispatchSettingsAction(it) },
                onBack = { session?.dispatchUiBack() },
                onLevelSelectAction = { revision, action -> session?.dispatchLevelSelectAction(revision, action) },
                onPauseAction = { session?.dispatchPauseAction(it) },
                onMultiplayerAction = { kind, revision, action -> session?.dispatchMultiplayerAction(kind, revision, action) },
                onBattleRoomAction = { revision, action -> session?.dispatchBattleRoomAction(revision, action) },
                onDialogAction = { revision, action -> session?.dispatchDialogAction(revision, action) },
                onLoadingCancel = { session?.dispatchLoadingCancel(it) },
                onModsAction = { revision, action -> session?.dispatchModsAction(revision, action) },
                onResourceBrowserAction = { revision, action -> session?.dispatchResourceBrowserAction(revision, action) },
                onReplaySelectAction = { revision, action -> session?.dispatchReplaySelectAction(revision, action) },
                onModWindowAction = { revision, action -> session?.dispatchModWindowAction(revision, action) },
                onGameInput = { event -> session?.dispatchGameInput(event) },
            )
        }
        // Hidden ComposePanels have no composition yet. Drive visibility independently, and
        // avoid scheduling a native window operation for every loading-progress update.
        var lastVisible: Boolean? = null
        scope.launch {
            state.collect { snapshot ->
                SwingUtilities.invokeLater {
                    if (!disposed) {
                        onStateChanged(snapshot)
                        if (lastVisible != snapshot.showComposeOverlay) {
                            lastVisible = snapshot.showComposeOverlay
                            host.setVisible(snapshot.showComposeOverlay)
                        }
                    }
                }
            }
        }
    }

    fun attach(session: AppSession) {
        checkEdt()
        if (disposed || this.session === session) return
        check(this.session == null) { "The overlay is already attached to a session" }
        this.session = session
        bindState(session.uiState, session::setComposeUiEnabled)
    }

    internal fun bindState(sessionState: StateFlow<AppUiState>, setEnabled: (Boolean) -> Unit) {
        checkEdt()
        if (disposed) return
        check(disableSession == null) { "The overlay state is already bound" }
        disableSession = { setEnabled(false) }
        scope.launch {
            // Enabling is queued on the app dispatcher. Keep the bootstrap screen until its first
            // acknowledgement rather than hiding the window for the session's initial false.
            sessionState.dropWhile { !it.composeEnabled }.collect { state.value = it }
        }
        setEnabled(true)
    }

    override val isVisible: Boolean get() = host.isVisible
    override fun setContent(content: @Composable () -> Unit) {
        checkEdt()
        if (!disposed) host.setContent(content)
    }
    override fun setVisible(visible: Boolean) {
        checkEdt()
        if (!disposed) host.setVisible(visible)
    }
    override fun requestFocus() {
        checkEdt()
        if (!disposed) host.requestFocus()
    }
    override fun dispose() {
        checkEdt()
        if (disposed) return
        try { host.dispose() } finally { release() }
    }

    private fun release() {
        checkEdt()
        if (disposed) return
        disposed = true
        scope.cancel()
        val disable = disableSession
        disableSession = null
        session = null
        disable?.invoke()
    }

    private fun checkEdt() {
        check(SwingUtilities.isEventDispatchThread()) { "Compose overlay lifecycle operations must run on the EDT" }
    }
}
