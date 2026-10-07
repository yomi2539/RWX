package io.github.rwx.app

import com.corrodinggames.rts.gameFramework.GameEngine
import com.corrodinggames.rts.gameFramework.SettingsEngine
import io.github.rwx.i18n.I18n
import io.github.rwx.logger
import io.github.rwx.session.GameSession
import io.github.rwx.ui.host.DialogSceneHost
import io.github.rwx.ui.host.MultiplayerSceneHost
import io.github.rwx.ui.model.*

internal class MultiplayerConnectionController(
    private val gameSession: GameSession,
    private val lobbyController: MultiplayerLobbyController,
    private val battleRoomJoinController: BattleRoomJoinController,
    private val p2pPreparation: P2PJoinPreparationController,
    private val dialogSceneHost: DialogSceneHost,
    private val multiplayerSceneHost: MultiplayerSceneHost,
    private val selectHostMap: () -> MapEntry?,
    private val onHostPreparing: (MapEntry) -> Unit,
    private val updateBattleRoomFromNetwork: () -> Unit,
    private val navigateToBattleRoom: () -> Unit,
    private val showUnavailableDialog: (String) -> Unit,
) {
    fun joinOriginalServer(connectDescriptor: String, roomLabel: String = "server", serverId: String? = null) {
        if (connectDescriptor.isBlank()) return
        if (io.github.rwx.p2p.transfer.TransferOperation.active) {
            showUnavailableDialog(I18n.multiplayer.modTransfer.busy())
            return
        }
        recordLastJoin(MultiplayerLobbyKind.Original, connectDescriptor)
        battleRoomJoinController.start(
            address = connectDescriptor,
            roomLabel = roomLabel,
            failurePrefix = I18n.multiplayer.unableToJoinServer(),
        ) {
            check(gameSession.joinBattleRoom(connectDescriptor, serverId, p2pSession = false)) {
                "Game session rejected join request"
            }
        }
    }

    fun joinP2PRoom(roomId: String, roomLabel: String = "P2P room") {
        if (roomId.isBlank()) return
        recordLastJoin(MultiplayerLobbyKind.P2P, roomId)
        p2pPreparation.join(roomId, roomLabel)
    }

    fun showJoinRoomDialog(roomId: String) {
        val room = lobbyController.roomById(roomId)
        if (room == null) {
            showUnavailableDialog(I18n.multiplayer.roomNoLongerListed())
            return
        }
        if (lobbyController.activeLobbyKind == MultiplayerLobbyKind.Original && room.requiresJoinInput) {
            showOriginalRoomInputDialog(room)
            return
        }
        dialogSceneHost.show(multiplayerJoinRoomDialog(lobbyController.activeLobbyKind, room, ::joinLobbyRequest))
    }

    fun showJoinDirectDialog() {
        dialogSceneHost.show(multiplayerJoinDirectDialog(lobbyController.activeLobbyKind, ::joinLobbyRequest))
    }


    fun joinDirectWithAddress(address: String) {
        val trimmed = address.trim()
        if (trimmed.isBlank()) return
        val lobbyKind = lobbyController.activeLobbyKind
        joinLobbyRequest(
            MultiplayerJoinRequest(
                lobbyKind, trimmed,
                roomLabel = if (lobbyKind == MultiplayerLobbyKind.P2P) "P2P room" else "server",
            ),
        )
    }

    fun joinLastGame() {
        val lobbyKind = lobbyController.activeLobbyKind
        val address = lastJoinAddress(lobbyKind)
        if (address.isNullOrBlank()) {
            showUnavailableDialog(I18n.multiplayer.missingJoinInput())
            return
        }
        joinDirectWithAddress(address)
    }

    fun refreshLastJoinState() {
        lastJoinAddress(MultiplayerLobbyKind.Original)?.let {
            multiplayerSceneHost.setLastJoin(MultiplayerLobbyKind.Original, it)
        }
        lastJoinAddress(MultiplayerLobbyKind.P2P)?.let {
            multiplayerSceneHost.setLastJoin(MultiplayerLobbyKind.P2P, it)
        }
    }

    private fun lastJoinAddress(lobbyKind: MultiplayerLobbyKind): String? {
        val settings = GameEngine.getInstance()?.settingsEngine ?: SettingsEngine.getInstance()
        val raw = when (lobbyKind) {
            MultiplayerLobbyKind.Original -> settings?.lastNetworkIP
            MultiplayerLobbyKind.P2P -> settings?.lastP2PRoomId
        }
        return raw?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun recordLastJoin(lobbyKind: MultiplayerLobbyKind, address: String) {
        val trimmed = address.trim()
        if (trimmed.isBlank()) return
        multiplayerSceneHost.setLastJoin(lobbyKind, trimmed)
        runCatching {
            val settings = GameEngine.getInstance()?.settingsEngine ?: SettingsEngine.getInstance()
            if (settings != null) {
                when (lobbyKind) {
                    MultiplayerLobbyKind.Original -> settings.lastNetworkIP = trimmed
                    MultiplayerLobbyKind.P2P -> settings.lastP2PRoomId = trimmed
                }
                settings.save()
            }
        }
    }

    private fun joinLobbyRequest(request: MultiplayerJoinRequest) {
        when (request.lobbyKind) {
            MultiplayerLobbyKind.P2P -> joinP2PRoom(request.address, request.roomLabel)
            MultiplayerLobbyKind.Original -> joinOriginalServer(request.address, request.roomLabel, request.originalServerId)
        }
    }

    fun showPlayerNameDialog() {
        dialogSceneHost.show(
            Dialog(
                title = I18n.multiplayer.configurePlayerName(),
                message = I18n.multiplayer.playerNamePrompt(),
                textInput = DialogTextInput(
                    initialText = gameSession.currentMultiplayerPlayerName(),
                    hint = I18n.multiplayer.playerNameHint(),
                ),
                buttons = listOf(
                    DialogButton(
                        I18n.common.save(),
                        onInputPress = { value ->
                            if (!gameSession.updateMultiplayerPlayerName(value)) {
                                showUnavailableDialog(I18n.multiplayer.missingPlayerName())
                            }
                        },
                    ),
                    DialogButton(I18n.common.cancel()),
                ),
            ),
        )
    }

    fun hostMultiplayerGame() {
        if (io.github.rwx.p2p.transfer.TransferOperation.active) {
            showUnavailableDialog(I18n.multiplayer.modTransfer.busy())
            return
        }
        val map = selectHostMap()
        if (map == null) {
            showUnavailableDialog("No map is available to host")
            return
        }
        val lobbyKind = lobbyController.activeLobbyKind
        dialogSceneHost.show(
            Dialog(
                title = I18n.multiplayer.hostGame(),
                message = when (lobbyKind) {
                    MultiplayerLobbyKind.Original -> I18n.multiplayer.hostGameInfo(map.displayName)
                    MultiplayerLobbyKind.P2P -> I18n.multiplayer.hostP2pGameInfo(map.displayName)
                },
                form = multiplayerHostGameForm(lobbyKind),
                buttons = multiplayerHostGameButtons(map, lobbyKind),
            ),
        )
    }

    private fun hostMultiplayerGame(
        map: MapEntry,
        lobbyKind: MultiplayerLobbyKind,
        options: MultiplayerHostOptions,
    ) {
        runCatching {
            onHostPreparing(map)
            check(
                gameSession.hostBattleRoom(
                    mapPath = map.mapAssetPath,
                    savedGame = map.isSavedGame,
                    isPublic = options.isPublic,
                    password = options.password,
                    useMods = options.useMods,
                    rwxP2PSession = lobbyKind == MultiplayerLobbyKind.P2P,
                )
            ) { "Game session rejected host request" }
            gameSession.setBattleRoomMaxPlayers(options.maxPlayers)
            if (lobbyKind == MultiplayerLobbyKind.P2P) {
                p2pPreparation.hostRoom(options.shareMods) {
                    updateBattleRoomFromNetwork()
                    navigateToBattleRoom()
                }
            } else {
                updateBattleRoomFromNetwork()
                navigateToBattleRoom()
            }
        }.onFailure { error ->
            logger.warn(error) { "Host game failed" }
            showUnavailableDialog(I18n.multiplayer.hostFailed(error.message ?: error.javaClass.simpleName))
        }
    }

    private fun multiplayerHostGameButtons(
        map: MapEntry,
        lobbyKind: MultiplayerLobbyKind,
    ): List<DialogButton> = buildList {
        add(DialogButton(I18n.common.cancel()))
        when (lobbyKind) {
            MultiplayerLobbyKind.Original -> {
                add(
                    DialogButton(
                        I18n.multiplayer.hostStartPrivate(),
                        onFormPress = { values ->
                            hostMultiplayerGame(
                                map,
                                lobbyKind,
                                values.toMultiplayerHostOptions(lobbyKind, isPublic = false),
                            )
                        },
                    ),
                )
                add(
                    DialogButton(
                        I18n.multiplayer.hostStartPublic(),
                        onFormPress = { values ->
                            hostMultiplayerGame(
                                map,
                                lobbyKind,
                                values.toMultiplayerHostOptions(lobbyKind, isPublic = true),
                            )
                        },
                    ),
                )
            }

            MultiplayerLobbyKind.P2P -> add(
                DialogButton(
                    I18n.multiplayer.hostStartP2p(),
                    onFormPress = { values ->
                        hostMultiplayerGame(
                            map,
                            lobbyKind,
                            values.toMultiplayerHostOptions(lobbyKind, isPublic = false),
                        )
                    },
                ),
            )
        }
    }

    private fun showOriginalRoomInputDialog(room: MultiplayerRoomItem) {
        val hint = room.joinInputHint.ifBlank { I18n.multiplayer.joinServerHint() }
        val roomDetails = room.infoText.ifBlank { joinRoomDialogMessage(room) }
        dialogSceneHost.show(
            Dialog(
                title = I18n.multiplayer.joinServer(),
                message = listOf(
                    roomDetails,
                    I18n.multiplayer.joinServerInput(),
                ).joinToString("\n\n"),
                textInput = DialogTextInput(hint = hint),
                buttons = listOf(
                    DialogButton(I18n.common.cancel()),
                    DialogButton(
                        I18n.common.join(),
                        onInputPress = { value ->
                            val address = value.trim()
                            if (address.isBlank()) {
                                showUnavailableDialog(I18n.multiplayer.missingJoinInput())
                            } else {
                                joinOriginalServer(address, room.joinDisplayLabel())
                            }
                        },
                    ),
                ),
                scrollableMessage = true,
            )
        )
    }
}

internal data class MultiplayerHostOptions(
    val useMods: Boolean,
    val password: String?,
    val isPublic: Boolean,
    val maxPlayers: Int = DEFAULT_HOST_MAX_PLAYERS,
    val shareMods: Boolean = false,
)

internal fun multiplayerHostGameForm(lobbyKind: MultiplayerLobbyKind): DialogForm =
    DialogForm(
        fields = buildList {
            add(
                DialogFormField.Toggle(
                    id = HOST_USE_MODS_FIELD,
                    label = I18n.multiplayer.hostUseMods(),
                    checked = false,
                ),
            )
            if (lobbyKind == MultiplayerLobbyKind.P2P) {
                add(DialogFormField.Toggle(HOST_SHARE_MODS_FIELD, I18n.multiplayer.modTransfer.share(), false))
            }
            add(
                DialogFormField.Text(
                    id = HOST_PASSWORD_FIELD,
                    label = I18n.multiplayer.hostPassword(),
                    initialText = "",
                    hint = I18n.multiplayer.hostPasswordHint(),
                ),
            )
            add(
                DialogFormField.Text(
                    id = HOST_MAX_PLAYERS_FIELD,
                    label = I18n.battleroom.options.maxPlayers(),
                    initialText = DEFAULT_HOST_MAX_PLAYERS.toString(),
                    hint = "$MIN_HOST_MAX_PLAYERS-$MAX_HOST_MAX_PLAYERS",
                ),
            )
        },
    )

internal fun Map<String, String>.toMultiplayerHostOptions(
    lobbyKind: MultiplayerLobbyKind,
    isPublic: Boolean = lobbyKind == MultiplayerLobbyKind.Original,
): MultiplayerHostOptions =
    MultiplayerHostOptions(
        useMods = get(HOST_USE_MODS_FIELD)?.toBooleanStrictOrNull() ?: false,
        password = get(HOST_PASSWORD_FIELD)?.trim()?.takeIf(String::isNotBlank),
        isPublic = lobbyKind == MultiplayerLobbyKind.Original && isPublic,
        shareMods = lobbyKind == MultiplayerLobbyKind.P2P && get(HOST_SHARE_MODS_FIELD).toBoolean(),
        maxPlayers = get(HOST_MAX_PLAYERS_FIELD)?.toIntOrNull()?.coerceIn(MIN_HOST_MAX_PLAYERS, MAX_HOST_MAX_PLAYERS)
            ?: DEFAULT_HOST_MAX_PLAYERS,
    )

private const val HOST_USE_MODS_FIELD: String = "useMods"
private const val HOST_SHARE_MODS_FIELD: String = "shareMods"
private const val HOST_PASSWORD_FIELD: String = "password"
private const val HOST_MAX_PLAYERS_FIELD: String = "maxPlayers"
private const val MIN_HOST_MAX_PLAYERS: Int = 10
private const val MAX_HOST_MAX_PLAYERS: Int = 100
private const val DEFAULT_HOST_MAX_PLAYERS: Int = 10
