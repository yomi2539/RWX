package io.github.rwx.app

import io.github.rwx.i18n.I18n
import io.github.rwx.logger
import io.github.rwx.session.BattleRoomSnapshot
import io.github.rwx.session.GameSession
import io.github.rwx.ui.host.LoadingDialogSceneHost
import io.github.rwx.ui.model.LoadingDialogHandle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.milliseconds

internal class BattleRoomJoinController(
    private val gameSession: GameSession,
    private val loadingDialogSceneHost: LoadingDialogSceneHost,
    private val onStarted: () -> Unit,
    private val onConnected: (BattleRoomSnapshot?) -> Unit,
    private val onFailed: (String) -> Unit,
    private val launchMonitor: (suspend () -> Unit) -> Job = { work -> launchOnIO("battleroom-join") { work() } },
) {
    private var pendingJoin: PendingBattleRoomJoin? = null
    private val observation = AtomicReference<JoinObservation?>(null)
    private var monitorJob: Job? = null
    private var loadingHandle: LoadingDialogHandle? = null
    var onSettled: (Boolean) -> Unit = {}

    val isPending: Boolean
        get() = pendingJoin != null

    fun start(
        address: String,
        roomLabel: String,
        failurePrefix: String,
        requestJoin: () -> Unit,
    ) {
        val trimmedAddress = address.trim()
        if (trimmedAddress.isBlank()) return
        clearPending()
        hideLoading()
        runCatching {
            val joinToken = "$trimmedAddress:${System.nanoTime()}"
            onStarted()
            observation.set(JoinObservation(joinToken))
            pendingJoin = PendingBattleRoomJoin(
                token = joinToken,
                address = trimmedAddress,
                roomLabel = roomLabel,
                failurePrefix = failurePrefix,
                startedAtNanos = System.nanoTime(),
            )
            monitorJob = launchMonitor {
                monitor(
                    token = joinToken,
                    requestJoin = requestJoin,
                    failurePrefix = failurePrefix,
                )
            }
            loadingHandle = loadingDialogSceneHost.showCircular(
                title = I18n.multiplayer.joiningRoom(),
                message = I18n.multiplayer.connectingTo(roomLabel),
            ) {
                if (pendingJoin?.token == joinToken) {
                    clearPending()
                    hideLoading()
                    gameSession.cancelBattleRoomJoin()
                    onSettled(false)
                }
            }
        }.onFailure { error ->
            clearPending()
            hideLoading()
            logger.warn(error) { "Battle room join failed" }
            onFailed("$failurePrefix: ${error.message ?: error.javaClass.simpleName}")
            onSettled(false)
        }
    }

    fun drive(nowNanos: Long = System.nanoTime()) {
        val pending = pendingJoin ?: return
        val latest = observation.get()?.takeIf { it.token == pending.token }
        val latestProbe = latest?.probe
        val joinError = latest?.errorMessage ?: latestProbe?.errorMessage
        when (
            battleRoomJoinPollResult(
                startedAtNanos = pending.startedAtNanos,
                nowNanos = nowNanos,
                hasBattleRoomSnapshot = latestProbe?.hasJoinedSnapshot == true,
                isJoinInProgress = latestProbe?.isJoinInProgress ?: true,
                errorMessage = joinError,
            )
        ) {
            BattleRoomJoinPollResult.Connecting -> Unit
            BattleRoomJoinPollResult.Connected -> {
                clearPending()
                hideLoading()
                onConnected(latestProbe?.snapshot)
                onSettled(true)
            }

            BattleRoomJoinPollResult.Failed -> {
                fail("${pending.failurePrefix}: ${joinError ?: "connection failed"}")
            }

            BattleRoomJoinPollResult.TimedOut -> {
                fail("${pending.failurePrefix}: connection timed out")
            }
        }
    }

    fun handleBattleRoomClosed() {
        if (pendingJoin == null) return
        clearPending()
        gameSession.cancelBattleRoomJoin()
        hideLoading()
        onSettled(false)
    }

    private suspend fun monitor(
        token: String,
        requestJoin: () -> Unit,
        failurePrefix: String,
    ) {
        if (observation.get()?.token != token) return
        runCatching {
            gameSession.cancelBattleRoomJoin()
            requestJoin()
        }.onFailure { error ->
            logger.warn(error) { "Battle room join request failed" }
            observation.updateAndGet { current ->
                if (current?.token == token) current.copy(errorMessage = "$failurePrefix: ${error.message ?: error.javaClass.simpleName}") else current
            }
            return
        }
        while (observation.get()?.token == token) {
            val snapshot = gameSession.currentBattleRoom()
            val isJoinInProgress = gameSession.isJoiningBattleRoom
            val hasJoinedSnapshot = snapshot != null &&
                    snapshot.players.isNotEmpty()
            val errorMessage = gameSession.latestBattleRoomJoinError
            val probe = PendingBattleRoomJoinProbe(
                    snapshot = snapshot,
                    hasJoinedSnapshot = hasJoinedSnapshot,
                    isJoinInProgress = isJoinInProgress,
                    errorMessage = errorMessage,
                )
            observation.updateAndGet { current ->
                if (current?.token == token) current.copy(probe = probe) else current
            }
            if (!errorMessage.isNullOrBlank() || (hasJoinedSnapshot && !isJoinInProgress)) {
                return
            }
            delay(BATTLE_ROOM_JOIN_POLL_INTERVAL_MS.milliseconds)
        }
    }

    private fun fail(message: String) {
        clearPending()
        gameSession.cancelBattleRoomJoin()
        hideLoading()
        onFailed(message)
        onSettled(false)
    }

    private fun hideLoading() {
        val handle = loadingHandle
        loadingHandle = null
        handle?.let(loadingDialogSceneHost::hide)
    }

    private fun clearPending() {
        pendingJoin = null
        observation.set(null)
        monitorJob?.cancel()
        monitorJob = null
    }
}

private data class PendingBattleRoomJoin(
    val token: String,
    val address: String,
    val roomLabel: String,
    val failurePrefix: String,
    val startedAtNanos: Long,
)

private data class PendingBattleRoomJoinProbe(
    val snapshot: BattleRoomSnapshot? = null,
    val hasJoinedSnapshot: Boolean = false,
    val isJoinInProgress: Boolean = false,
    val errorMessage: String? = null,
)

private data class JoinObservation(
    val token: String,
    val probe: PendingBattleRoomJoinProbe = PendingBattleRoomJoinProbe(isJoinInProgress = true),
    val errorMessage: String? = null,
)
