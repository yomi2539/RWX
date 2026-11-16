package io.github.rwx.app

import com.corrodinggames.rts.gameFramework.network.GameRoomSettings
import io.github.rwx.i18n.I18n
import io.github.rwx.logger
import io.github.rwx.session.GameSession
import io.github.rwx.session.resolveBattleRoomPlayerSnapshot
import io.github.rwx.ui.host.DialogSceneHost
import io.github.rwx.ui.model.Dialog
import io.github.rwx.ui.model.DialogButton

internal class BattleRoomAdminController(
    private val currentRoomRevision: () -> Long,
    private val gameSession: GameSession,
    private val dialogSceneHost: DialogSceneHost,
    private val updateBattleRoomFromNetwork: () -> Unit,
    private val showUnavailableDialog: (String) -> Unit,
) {
    fun addAiToBattleRoom() {
        if (gameSession.currentBattleRoom() == null) {
            showUnavailableDialog(I18n.battleroom.admin.addAiRequires())
            return
        }
        runCatching {
            check(gameSession.addBattleRoomAi(1)) { "Game session rejected add AI request" }
            updateBattleRoomFromNetwork()
        }.onFailure { error ->
            logger.warn(error) { "Add AI failed" }
            showUnavailableDialog(I18n.battleroom.admin.addAiFailed(error.message ?: error.javaClass.simpleName))
        }
    }

    fun showBattleRoomOptionsDialog() {
        val snapshot = gameSession.currentBattleRoom()
        val initialOptions = snapshot?.room?.options
        if (initialOptions == null || !snapshot.isHost) {
            showUnavailableDialog(I18n.battleroom.admin.optionsRequires())
            return
        }
        val revision = currentRoomRevision()
        dialogSceneHost.show(
            Dialog(
                title = I18n.battleroom.options(),
                message = I18n.battleroom.admin.optionsHint(),
                form = battleRoomOptionsForm(
                    initialOptions,
                    maxPlayers = gameSession.currentBattleRoom()?.maxPlayers ?: DEFAULT_MAX_PLAYERS,
                ),
                buttons = listOf(
                    DialogButton(I18n.mods.apply(), onFormPress = ::applyBattleRoomOptionsForm),
                    DialogButton(I18n.common.cancel()),
                ),
                scrollableForm = true,
                compactOnAndroid = true,
            ),
            isValid = { currentRoomRevision() == revision && gameSession.currentBattleRoom(refreshNetworkStatus = false)?.isHost == true },
        )
    }

    fun showPlayerConfigDialog(playerId: String) {
        val snapshot = gameSession.currentBattleRoom()
        val player = snapshot?.players
            ?.let { players -> resolveBattleRoomPlayerSnapshot(playerId, players) }
        if (player == null || (!snapshot.isHost && !player.isLocal)) {
            showUnavailableDialog(I18n.battleroom.admin.playerRequires())
            return
        }
        val revision = currentRoomRevision()
        val canKick = snapshot.isHost && !player.isLocal
        val buttons = buildList {
            add(
                DialogButton(
                    I18n.mods.apply(),
                    onFormPress = { values ->
                        runCatching {
                            val spawn = values["spawn"]?.toIntOrNull()
                            val team = values["team"]?.toIntOrNull()
                            val startingUnits = values["startingUnits"]?.toIntOrNull()
                            val aiDifficulty = values["aiDifficulty"]?.toIntOrNull()
                            check(
                                gameSession.configureBattleRoomPlayer(
                                    player.id, spawn, team, startingUnits, aiDifficulty,
                                )
                            ) {
                                "Game session rejected player config request"
                            }
                            updateBattleRoomFromNetwork()
                        }.onFailure { error ->
                            logger.warn(error) { "Apply player config failed" }
                            showUnavailableDialog(
                                I18n.battleroom.admin.applyPlayerFailed(error.message ?: error.javaClass.simpleName),
                            )
                        }
                    },
                ),
            )
            if (canKick) {
                add(DialogButton(if (player.isAI) I18n.battleroom.admin.removeAi() else I18n.battleroom.admin.kick()) { kickBattleRoomPlayer(player.id) })
            }
            add(DialogButton(I18n.common.cancel()))
        }
        dialogSceneHost.show(
            Dialog(
                title = I18n.battleroom.admin.playerTitle(),
                message = I18n.battleroom.admin.playerHint(),
                form = playerConfigForm(player, snapshot.room.options, isHost = snapshot.isHost),
                buttons = buttons,
                compactOnAndroid = true,
            ),
            isValid = {
                val current = gameSession.currentBattleRoom(refreshNetworkStatus = false)
                val member = current?.players?.let { resolveBattleRoomPlayerSnapshot(player.id, it) }
                currentRoomRevision() == revision && current?.isHost == snapshot.isHost && member != null &&
                    member.isLocal == player.isLocal && (current.isHost || member.isLocal)
            },
        )
    }

    private fun kickBattleRoomPlayer(playerId: String) {
        runCatching {
            check(gameSession.kickBattleRoomPlayer(playerId)) { "Game session rejected kick request" }
            updateBattleRoomFromNetwork()
        }.onFailure { error ->
            logger.warn(error) { "Kick player failed" }
            showUnavailableDialog(I18n.battleroom.admin.kickFailed(error.message ?: error.javaClass.simpleName))
        }
    }

    fun sendBattleRoomChatMessage(message: String) {
        val text = message.trim()
        if (text.isBlank()) return
        if (gameSession.currentBattleRoom() == null) {
            showUnavailableDialog(I18n.battleroom.admin.chatRequires())
            return
        }
        runCatching {
            check(gameSession.sendBattleRoomMessage(text)) { "Game session rejected chat request" }
        }.onFailure { error ->
            logger.warn(error) { "Send battle room chat failed" }
            showUnavailableDialog(I18n.battleroom.admin.chatFailed(error.message ?: error.javaClass.simpleName))
        }
    }

    private fun applyBattleRoomOptionsForm(values: Map<String, String>) {
        val base = gameSession.currentBattleRoom()?.room?.options ?: GameRoomSettings()
        val options = values.toGameRoomSettings(base)
        val layout = values["teamLayout"]?.toBattleRoomTeamLayoutOrNull()
        if (gameSession.currentBattleRoom() == null) {
            showUnavailableDialog(I18n.battleroom.admin.optionsRequires())
            return
        }
        runCatching {
            check(gameSession.applyBattleRoomOptions(options)) {
                "Game session rejected battle room options"
            }
            values["maxPlayers"]?.toIntOrNull()?.let { maxPlayers ->
                gameSession.setBattleRoomMaxPlayers(maxPlayers)
            }
            layout?.let {
                check(gameSession.applyBattleRoomTeamLayout(it)) {
                    "Game session rejected team layout request"
                }
            }
            updateBattleRoomFromNetwork()
        }.onFailure { error ->
            logger.warn(error) { "Apply battle room options failed" }
            showUnavailableDialog(I18n.battleroom.admin.applyOptionsFailed(error.message ?: error.javaClass.simpleName))
        }
    }
}
