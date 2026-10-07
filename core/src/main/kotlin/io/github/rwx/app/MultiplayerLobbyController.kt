package io.github.rwx.app

import com.corrodinggames.rts.gameFramework.network.ServerInfo
import io.github.rwx.i18n.I18n
import io.github.rwx.logger
import io.github.rwx.map.MapMetadata
import io.github.rwx.p2p.P2PRoomAdvertisement
import io.github.rwx.ui.host.MultiplayerSceneHost
import io.github.rwx.ui.model.ModeLabel
import io.github.rwx.ui.model.MultiplayerLobbyKind
import io.github.rwx.ui.model.MultiplayerRoomItem

internal class MultiplayerLobbyController(
    private val sceneHost: MultiplayerSceneHost,
    private val backend: MultiplayerLobbyBackend = EngineMultiplayerLobbyBackend,
) {
    var activeLobbyKind: MultiplayerLobbyKind = MultiplayerLobbyKind.Original
        private set

    fun roomById(roomId: String): MultiplayerRoomItem? {
        val state = sceneHost.snapshot()
        if (roomId.isBlank() || state.lobbyKind != activeLobbyKind || state.errorText != null) return null
        return state.rooms.firstOrNull { it.roomId == roomId }
    }

    fun requestRefresh() {
        val kind = activeLobbyKind
        sceneHost.beginRefresh(kind)
        try {
            val unavailable = backend.unavailableReason(kind)
            if (unavailable != null) {
                fail(kind, unavailable)
                return
            }
            backend.requestRefresh(kind)
            updateRooms(kind, isRefreshing = true)
        } catch (error: Exception) {
            reportFailure(kind, "refresh", error)
        } catch (error: LinkageError) {
            // An unavailable native transport must not cancel the frontend's coroutine scope.
            reportFailure(kind, "refresh", error)
        }
    }

    fun switchLobby(lobbyKind: MultiplayerLobbyKind) {
        // Selecting the active tab is not a new discovery attempt and must not hide an error.
        if (activeLobbyKind == lobbyKind) return
        activeLobbyKind = lobbyKind
        requestRefresh()
    }

    fun handleOriginalRoomListRefresh() = updateRooms(MultiplayerLobbyKind.Original)

    fun handleP2PRoomListRefresh() = updateRooms(MultiplayerLobbyKind.P2P)

    private fun updateRooms(kind: MultiplayerLobbyKind, isRefreshing: Boolean = false) {
        if (kind != activeLobbyKind) return
        try {
            val unavailable = backend.unavailableReason(kind)
            if (unavailable != null) {
                fail(kind, unavailable)
                return
            }
            val rooms = backend.readRooms(kind)
            sceneHost.updateRooms(
                rooms = rooms,
                statusText = when {
                    isRefreshing -> I18n.multiplayer.searching()
                    rooms.isEmpty() -> I18n.multiplayer.noRooms()
                    else -> ""
                },
                lobbyKind = kind,
                isRefreshing = isRefreshing,
            )
        } catch (error: Exception) {
            reportFailure(kind, "list read", error)
        } catch (error: LinkageError) {
            reportFailure(kind, "list read", error)
        }
    }

    private fun reportFailure(kind: MultiplayerLobbyKind, operation: String, error: Throwable) {
        logger.warn(error) { "${kind.label} room $operation failed" }
        fail(kind, unavailableMessage(kind, error.message ?: error.javaClass.simpleName))
    }

    private fun fail(kind: MultiplayerLobbyKind, message: String) {
        if (kind != activeLobbyKind) return
        // The visible list is also the authoritative join lookup: failures must not leave old rooms joinable.
        sceneHost.updateRooms(emptyList(), message, kind, errorText = message)
    }

    private fun unavailableMessage(kind: MultiplayerLobbyKind, detail: String): String =
        if (kind == MultiplayerLobbyKind.Original) "Original lobby unavailable: $detail" else "P2P unavailable: $detail"
}

internal fun p2pRoomToMultiplayerItem(room: P2PRoomAdvertisement): MultiplayerRoomItem =
    MultiplayerRoomItem(
        roomId = room.roomId.orEmpty(),
        hostName = room.createdBy ?: I18n.multiplayer.unknown(),
        mapName = room.getMapDisplayName().withRwxModeSuffix(room.requiredRwxFeatures),
        playersLabel = "${room.currentPlayers.coerceAtLeast(0)}/${room.maxPlayers.coerceAtLeast(0)}",
        versionLabel = room.gameVersionString?.let { "v$it" } ?: I18n.multiplayer.unknown(),
        requiresPassword = room.requiresPassword,
        hasMods = room.hasMods,
        transportLabel = when {
            !room.webrtcSignaling.isNullOrBlank() -> "WebRTC"
            else -> "P2P"
        },
        stateLabel = room.gameState ?: "Unknown",
        infoText = room.getInfoText(),
    )

private fun String.withRwxModeSuffix(requiredRwxFeatures: List<String>): String =
    requiredRwxFeatures.ModeLabel()?.let { "$this - $it" } ?: this

internal fun serverInfoToMultiplayerItem(server: ServerInfo): MultiplayerRoomItem =
    MultiplayerRoomItem(
        roomId = server.getConnectDescriptor(),
        hostName = server.createdBy ?: server.publicHost ?: server.lanHost ?: I18n.multiplayer.unknown(),
        mapName = server.mapPath?.let(MapMetadata::getMapName) ?: server.serverMessage ?: "<No Map>",
        playersLabel = "${server.currentPlayers.coerceAtLeast(0)}/${server.maxPlayers.coerceAtLeast(0)}",
        versionLabel = server.gameVersionString?.let { if (it == "ANY") it else "v$it" } ?: I18n.multiplayer.unknown(),
        requiresPassword = server.requiresPassword,
        hasMods = server.hasMods,
        transportLabel = when {
            server.isLanServer -> "LAN"
            server.isDedicatedServer -> "Dedicated"
            server.isPortOpen -> "Internet"
            else -> "Unknown"
        },
        stateLabel = server.gameState ?: "Unknown",
        joinAddress = server.getConnectDescriptor(),
        originalServerId = server.serverId?.takeIf { it.isNotBlank() },
        infoText = runCatching { server.getInfoText() }.getOrDefault(""),
    )
