package io.github.rwx.ui.model

import io.github.rwx.ui.AppScreen

data class MultiplayerRoomItem(
    val roomId: String,
    val hostName: String,
    val mapName: String,
    val playersLabel: String,
    val versionLabel: String,
    val requiresPassword: Boolean,
    val hasMods: Boolean,
    val transportLabel: String,
    val stateLabel: String,
    val joinAddress: String = roomId,
    val originalServerId: String? = null,
    val requiresJoinInput: Boolean = false,
    val joinInputHint: String = "",
    val infoText: String = "",
)

enum class MultiplayerLobbyKind(val label: String) {
    Original("Original RW"),
    P2P("RWX P2P"),
}


data class MultiplayerRoomListModel(
    val title: String,
    val lobbyKind: MultiplayerLobbyKind,
    val rooms: List<MultiplayerRoomItem>,
    val statusText: String = "",
    val revision: Long = 0,
    val isRefreshing: Boolean = false,
    val errorText: String? = null,
    val lastJoinAddress: String? = null,
) {
    /** Scope actions to the lobby the user actually saw, while allowing a newer tab choice. */
    internal fun resolveAction(requestLobby: MultiplayerLobbyKind, requestRevision: Long, action: MultiplayerAction): MultiplayerAction? {
        if (action == MultiplayerAction.Back || action == MultiplayerAction.ConfigurePlayerName || action is MultiplayerAction.SwitchLobby || action == MultiplayerAction.JoinLastGame) {
            return action
        }
        if (requestLobby != lobbyKind || requestRevision != revision) return null
        if (action is MultiplayerAction.JoinRoom) {
            return action.takeIf { errorText == null && it.roomId.isNotBlank() && rooms.any { room -> room.roomId == it.roomId } }
        }
        if (action is MultiplayerAction.JoinDirectWithAddress) {
            return action.takeIf { it.address.isNotBlank() }
        }
        return action
    }
}

/** Actions the user can take on the multiplayer room-list screen. */
sealed interface MultiplayerAction {
    /** Navigate back to the main menu. */
    data object Back : MultiplayerAction

    /** Refresh the room list. */
    data object Refresh : MultiplayerAction

    /** Switch between the original RW lobby and RWX's P2P lobby. */
    data class SwitchLobby(val lobbyKind: MultiplayerLobbyKind) : MultiplayerAction

    /** Host a game in the active lobby. */
    data object HostGame : MultiplayerAction

    /** Join by a manually-entered address / room code in the active lobby. */
    data object JoinDirect : MultiplayerAction


    data class JoinDirectWithAddress(val address: String) : MultiplayerAction

    /** Configure the player name used by multiplayer sessions. */
    data object ConfigurePlayerName : MultiplayerAction

    /** Join a specific room by its ID. */
    data class JoinRoom(val roomId: String) : MultiplayerAction

    /** Rejoin the last address used in the active lobby. */
    data object JoinLastGame : MultiplayerAction
}

/** Outcome produced by [MultiplayerNavigation] for each [MultiplayerAction]. */
sealed interface MultiplayerOutcome {
    data class Navigate(val screen: AppScreen) : MultiplayerOutcome
    data object RefreshRequested : MultiplayerOutcome
    data class SwitchLobby(val lobbyKind: MultiplayerLobbyKind) : MultiplayerOutcome
    data object HostGameRequested : MultiplayerOutcome
    data object JoinDirectRequested : MultiplayerOutcome
    data class JoinDirectWithAddressRequested(val address: String) : MultiplayerOutcome
    data object ConfigurePlayerNameRequested : MultiplayerOutcome
    data class JoinRoom(val roomId: String) : MultiplayerOutcome
    data object JoinLastGameRequested : MultiplayerOutcome
}

/** Pure function that maps each [MultiplayerAction] to its [MultiplayerOutcome]. */
object MultiplayerNavigation {
    fun outcomeFor(action: MultiplayerAction): MultiplayerOutcome = when (action) {
        MultiplayerAction.Back -> MultiplayerOutcome.Navigate(AppScreen.MainMenu)
        MultiplayerAction.Refresh -> MultiplayerOutcome.RefreshRequested
        is MultiplayerAction.SwitchLobby -> MultiplayerOutcome.SwitchLobby(action.lobbyKind)
        MultiplayerAction.HostGame -> MultiplayerOutcome.HostGameRequested
        MultiplayerAction.JoinDirect -> MultiplayerOutcome.JoinDirectRequested
        is MultiplayerAction.JoinDirectWithAddress -> MultiplayerOutcome.JoinDirectWithAddressRequested(action.address)
        MultiplayerAction.ConfigurePlayerName -> MultiplayerOutcome.ConfigurePlayerNameRequested
        is MultiplayerAction.JoinRoom -> MultiplayerOutcome.JoinRoom(action.roomId)
        MultiplayerAction.JoinLastGame -> MultiplayerOutcome.JoinLastGameRequested
    }
}

/** Shared labels for both renderers; P/M are single-letter flags, not translated. */
fun MultiplayerRoomItem.markers(): String = buildString {
    if (requiresPassword) append("P")
    if (hasMods) {
        if (isNotEmpty()) append(" ")
        append("M")
    }
}

fun MultiplayerRoomItem.statusLabel(): String =
    "$playersLabel | $stateLabel | $versionLabel | $transportLabel ${markers()}".trim()

/** Compact rows show flags as icons; the state text stays marker-free. */
fun MultiplayerRoomItem.statusLabelWithoutMarkers(): String =
    "$playersLabel | $stateLabel | $versionLabel | $transportLabel".trim()
