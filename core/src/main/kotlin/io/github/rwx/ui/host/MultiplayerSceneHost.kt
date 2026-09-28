package io.github.rwx.ui.host

import io.github.rwx.ui.model.MultiplayerAction
import io.github.rwx.ui.model.MultiplayerLobbyKind
import io.github.rwx.ui.model.MultiplayerRoomItem
import io.github.rwx.ui.model.MultiplayerRoomListModel

class MultiplayerSceneHost(
    private val onAction: (MultiplayerAction) -> Unit = {},
) {
    private var rooms: List<MultiplayerRoomItem> = emptyList()
    private var statusText = ""
    private var lobbyKind = MultiplayerLobbyKind.Original
    private var isRefreshing = false
    private var errorText: String? = null
    private var revision = 0L
    private var lastJoinByLobby: Map<MultiplayerLobbyKind, String> = emptyMap()

    fun setLastJoin(lobbyKind: MultiplayerLobbyKind, address: String) {
        val trimmed = address.trim()
        if (trimmed.isBlank()) return
        lastJoinByLobby = lastJoinByLobby + (lobbyKind to trimmed)
    }

    fun clearLastJoin(lobbyKind: MultiplayerLobbyKind) {
        lastJoinByLobby = lastJoinByLobby - lobbyKind
    }

    fun beginRefresh(kind: MultiplayerLobbyKind) {
        revision++
        if (kind != lobbyKind) rooms = emptyList()
        lobbyKind = kind
        isRefreshing = true
        errorText = null
        statusText = "Searching for rooms..."
    }

    fun snapshot(): MultiplayerRoomListModel = MultiplayerRoomListModel(
        title = "Multiplayer Rooms",
        lobbyKind = lobbyKind,
        rooms = rooms.toList(),
        statusText = statusText,
        revision = revision,
        isRefreshing = isRefreshing,
        errorText = errorText,
        lastJoinAddress = lastJoinByLobby[lobbyKind]?.takeIf { it.isNotBlank() },
    )

    fun updateRooms(
        rooms: List<MultiplayerRoomItem>,
        statusText: String = "",
        lobbyKind: MultiplayerLobbyKind = this.lobbyKind,
        isRefreshing: Boolean = false,
        errorText: String? = null,
    ) {
        if (this.lobbyKind != lobbyKind) revision++
        this.lobbyKind = lobbyKind
        this.isRefreshing = isRefreshing
        this.errorText = errorText
        this.statusText = statusText
        this.rooms = rooms.toList()
    }

    fun dispatch(action: MultiplayerAction) = onAction(action)
}
