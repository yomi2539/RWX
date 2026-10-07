package io.github.rwx.app

import io.github.rwx.PlatformStorage
import io.github.rwx.i18n.I18n
import io.github.rwx.logger
import io.github.rwx.p2p.P2PLobbyService
import io.github.rwx.p2p.PreparedP2PPeer
import io.github.rwx.p2p.transfer.*
import io.github.rwx.session.GameSession
import io.github.rwx.ui.host.DialogSceneHost
import io.github.rwx.ui.host.LoadingDialogSceneHost
import io.github.rwx.ui.model.*
import kotlinx.coroutines.*
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private const val STALE_JOIN_GRACE_NANOS = 60_000_000_000L

internal class P2PJoinPreparationController(
    private val game: GameSession,
    private val storage: () -> PlatformStorage,
    private val joining: BattleRoomJoinController,
    private val loading: LoadingDialogSceneHost,
    private val dialogs: DialogSceneHost,
    private val showError: (String) -> Unit,
) {
    private class Attempt(val roomId: String, val label: String) {
        @Volatile var peer: PreparedP2PPeer? = null
        @Volatile var transaction: InstallTransaction? = null
        @Volatile var job: Job? = null
        @Volatile var handedOff = false
        var joined = false
        @Volatile var joinedAtNanos = 0L
        var error: String? = null
        var password: String? = null
        var manifestId: String? = null
        val cancelled = AtomicBoolean()
        val cleaning = AtomicBoolean()
        var deadline = System.nanoTime() + 1_800_000_000_000L
        val downloads = mutableMapOf<String, Long>()
        val received = ConcurrentHashMap<String, Long>()
    }
    private data class Progress(val message: String, val fraction: Float? = null)
    private val lobby by lazy { P2PLobbyService.getInstance() }
    private val cache by lazy { ArtifactCache(storage().cacheDir.file.toPath().resolve("transfer")) }
    private val host by lazy { TransferHostRepository(game, storage(), lobby.transferIdentity, cache, lobby::updateTransferAdvertisement) }
    private var hosting = false
    private var hostingJob: Job? = null
    private var active: Attempt? = null
    private var loadingHandle: LoadingDialogHandle? = null
    private val progress = AtomicReference<Progress?>()
    private val events = ConcurrentLinkedQueue<() -> Unit>()

    fun join(roomId: String, label: String) {
        if (active != null || joining.isPending || game.isNetworkMultiplayerActive() || !TransferOperation.acquire()) {
            showError(I18n.multiplayer.modTransfer.busy())
            return
        }
        val attempt = Attempt(roomId, label)
        active = attempt
        loadingHandle = loading.showCircular(I18n.multiplayer.modTransfer.title(), I18n.multiplayer.modTransfer.connecting()) {
            cancel(attempt)
        }
        attempt.job = launchOnIO("prepare-room-mods") {
            try {
                val peer = lobby.preparePeer(roomId)
                attempt.peer = peer
                ensureActive()
                if (peer.room.hasMods) prepareTransfer(attempt, peer)
                ensureActive()
                val address = lobby.connectPreparedPeer(peer)
                attempt.handedOff = true
                events += {
                    if (active === attempt && !attempt.cancelled.get()) {
                        loadingHandle?.let(loading::hide)
                        loadingHandle = null
                        joining.start(address, label, I18n.multiplayer.unableToJoinP2pRoom()) {
                            check(game.joinBattleRoom(address, null, p2pSession = true))
                        }
                    }
                }
            } catch (error: Exception) {
                if (error !is CancellationException || error is TimeoutCancellationException) {
                    logger.warn(error) { "P2P room join failed for $roomId: ${error.javaClass.simpleName}: ${error.message}" }
                    val code = (error as? TransferException)?.code?.name ?: "CONNECTION_FAILED"
                    val detail = error.message?.takeIf { it.isNotBlank() && it != code }?.take(200)
                    attempt.error = I18n.multiplayer.modTransfer.failed(if (detail == null) code else "$code: $detail")
                }
            } finally {
                if (!attempt.handedOff) cleanup(attempt)
            }
        }
    }

    private suspend fun prepareTransfer(attempt: Attempt, peer: PreparedP2PPeer) {
        val summary = peer.room.transfer!!
        if (summary.state == "disabled" || summary.state == "unsupported") {
            if (!ask(attempt, I18n.multiplayer.modTransfer.manual(), I18n.multiplayer.modTransfer.joinPrepared())) throw CancellationException()
            return
        }
        val fingerprint = TransferIdentity.decode(summary.hostKey!!, maximum = TransferIdentity.MAX_KEY_BYTES).sha256()
        val trustFile = storage().localDir.file.toPath().resolve("trusted-transfer-hosts.json")
        val trusted = if (Files.isRegularFile(trustFile) && Files.size(trustFile) <= 128 * 1024) {
            runCatching { transferJson.decodeFromString<List<String>>(Files.readAllBytes(trustFile).toString(Charsets.UTF_8)) }.getOrDefault(emptyList())
        } else emptyList()
        var confirmedHost = fingerprint in trusted
        var reconnects = 0
        while (true) {
            checkDeadline(attempt)
            val connection = lobby.openTransfer(peer)
            try {
                val local = game.captureTransferState() ?: throw TransferException(TransferErrorCode.LOAD_FAILED)
                transferRequire(!local.hasJvmMods, TransferErrorCode.UNSUPPORTED_MOD)
                TransferClient(connection, attempt.roomId, fingerprint, local.gameVersion).use { client ->
                    if (client.hello()) {
                        if (!confirmedHost) {
                            if (!ask(attempt, I18n.multiplayer.modTransfer.trust(fingerprint.chunked(8).joinToString(" ")), I18n.common.ok())) throw CancellationException()
                            TransferFiles.atomicWrite(trustFile, transferJson.encodeToString((trusted + fingerprint).distinct()).utf8())
                            confirmedHost = true
                        }
                        var tries = 0
                        while (true) {
                            val password = attempt.password ?: askPassword(attempt) ?: throw CancellationException()
                            try {
                                client.authorize(password)
                                attempt.password = password
                                break
                            } catch (error: TransferException) {
                                attempt.password = null
                                if (error.code != TransferErrorCode.AUTH_FAILED || ++tries >= 3) throw error
                            }
                        }
                    }
                    val manifest = awaitManifest(attempt, client)
                    transferRequire(attempt.manifestId == null || attempt.manifestId == manifest.manifestId, TransferErrorCode.MANIFEST_CHANGED)
                    attempt.manifestId = manifest.manifestId
                    progress.set(Progress(I18n.multiplayer.modTransfer.checking()))
                    val loaded = local.units.map { it.name to it.configHash }.toSet()
                    if (manifest.requiredUnits.all { (it.name to it.configHash) in loaded }) {
                        client.validate(manifest)
                        return
                    }
                    val sources = local.sources.associateWith { source -> source.units.map { it.name to it.configHash }.toSet() }
                    val reused = mutableSetOf<String>()
                    val missing = manifest.artifacts.filter { artifact ->
                        val requirements = manifest.requiredUnits.filter { it.artifactId == artifact.artifactId }
                        val source = if (requirements.isEmpty()) null else sources.entries.firstOrNull { (_, units) ->
                            requirements.all { (it.name to it.configHash) in units }
                        }?.key
                        if (source != null) reused += source.uuid
                        source == null
                    }
                    val files = mutableMapOf<TransferArtifact, Path>()
                    missing.forEach { artifact -> cache.find(artifact)?.let { files[artifact] = it } }
                    val needed = missing.filter { it !in files }
                    needed.forEach { attempt.downloads.putIfAbsent(it.artifactSha256, it.size) }
                    cache.reserve(needed.sumOf { it.size }, files.values)
                    for (artifact in needed) {
                        checkDeadline(attempt)
                        val path = client.download(manifest, artifact, cache.root) { received ->
                            checkDeadline(attempt)
                            attempt.received[artifact.artifactSha256] = received
                            val total = attempt.downloads.values.sum()
                            val done = attempt.received.values.sum()
                            progress.set(Progress("${I18n.multiplayer.modTransfer.downloading()} ${done / 1024} / ${total / 1024} KiB",
                                if (total == 0L) null else done.toFloat() / total))
                        }
                        cache.remember(path, artifact)
                        files[artifact] = path
                    }
                    client.validate(manifest)
                    val transaction = InstallTransaction(storage(), game, cache, local.selection)
                    attempt.transaction = transaction
                    files.forEach { (artifact, path) -> transaction.install(artifact, path) }
                    progress.set(Progress(I18n.multiplayer.modTransfer.loading()))
                    val reloadWatcher = launchOnIO("transfer-reload-status") {
                        while (isActive) {
                            val status = game.loadingStatus()
                            progress.set(Progress(
                                status.text.ifBlank { I18n.multiplayer.modTransfer.loading() },
                                status.progress,
                            ))
                            delay(100.milliseconds)
                        }
                    }
                    try {
                        transaction.apply(reused, manifest)
                    } finally {
                        reloadWatcher.cancel()
                    }
                    currentCoroutineContext().ensureActive()
                    client.validate(manifest)
                    return
                }
            } catch (error: Exception) {
                val reconnectable = (error is IOException && error !is TransferException) || error is TimeoutCancellationException
                if (!reconnectable || attempt.transaction != null || reconnects++ >= 2) throw error
                delay(1.seconds)
            } finally {
                connection.close()
            }
        }
    }

    private suspend fun awaitManifest(attempt: Attempt, client: TransferClient): TransferManifest {
        while (true) {
            checkDeadline(attempt)
            try { return client.manifest() } catch (error: TransferException) {
                if (error.code != TransferErrorCode.NOT_READY && error.code != TransferErrorCode.BUSY) throw error
                progress.set(Progress(I18n.multiplayer.modTransfer.waiting()))
                delay(2.seconds)
            }
        }
    }

    private suspend fun ask(attempt: Attempt, message: String, confirm: String): Boolean {
        val answer = CompletableDeferred<Boolean>()
        val started = System.nanoTime()
        events += {
            if (active !== attempt || attempt.cancelled.get()) answer.complete(false)
            else {
                val suspension = loading.suspendCurrent()
                dialogs.show(Dialog(I18n.multiplayer.modTransfer.trustTitle(), message,
                    buttons = listOf(DialogButton(I18n.common.cancel(), onPress = { answer.complete(false) }),
                        DialogButton(confirm, onPress = { answer.complete(true) })), dismissButtonIndex = 0),
                    isValid = { active === attempt && !attempt.cancelled.get() })
                answer.invokeOnCompletion { events += { suspension?.let(loading::resume) } }
            }
        }
        return answer.await().also { attempt.deadline += System.nanoTime() - started }
    }

    private suspend fun askPassword(attempt: Attempt): String? {
        val answer = CompletableDeferred<String?>()
        val started = System.nanoTime()
        events += {
            if (active !== attempt || attempt.cancelled.get()) answer.complete(null)
            else {
                val suspension = loading.suspendCurrent()
                dialogs.show(Dialog(I18n.multiplayer.hostPassword(), I18n.multiplayer.modTransfer.password(),
                    textInput = DialogTextInput(password = true),
                    buttons = listOf(DialogButton(I18n.common.cancel(), onPress = { answer.complete(null) }),
                        DialogButton(I18n.common.ok(), onInputPress = { answer.complete(it) })), dismissButtonIndex = 0),
                    isValid = { active === attempt && !attempt.cancelled.get() })
                answer.invokeOnCompletion { events += { suspension?.let(loading::resume) } }
            }
        }
        return answer.await().also { attempt.deadline += System.nanoTime() - started }
    }

    private fun checkDeadline(attempt: Attempt) {
        if (attempt.cancelled.get()) throw CancellationException()
        if (System.nanoTime() > attempt.deadline) throw TransferException(TransferErrorCode.TIMED_OUT)
    }

    private fun cancel(attempt: Attempt) {
        if (active !== attempt) return
        attempt.cancelled.set(true)
        attempt.job?.cancel()
        showRestoring()
        if (attempt.handedOff) launchOnIO("cancel-room-preparation") { cleanup(attempt) }
    }

    fun onGameJoinFinished(success: Boolean) {
        val attempt = active ?: return
        if (success) {
            attempt.joined = true
            attempt.joinedAtNanos = System.nanoTime()
        }
        else {
            showRestoring()
            launchOnIO("restore-room-mods") { cleanup(attempt) }
        }
    }

    fun recordJoinError(message: String): Boolean {
        val attempt = active ?: return false
        attempt.error = message
        return true
    }

    private suspend fun cleanup(attempt: Attempt) = withContext(NonCancellable) {
        if (!attempt.cleaning.compareAndSet(false, true)) return@withContext
        var restored = true
        try { attempt.transaction?.restore() } catch (_: Exception) { restored = false }
        attempt.peer?.let(lobby::cancelPeer)
        attempt.password = null
        if (restored) TransferOperation.release()
        events += {
            if (active === attempt) {
                loadingHandle?.let(loading::hide)
                loadingHandle = null
                if (restored) active = null
                if (!restored) showError(I18n.multiplayer.modTransfer.recovery())
                else attempt.error?.let(showError)
            }
        }
    }

    private fun showRestoring() {
        loadingHandle?.let(loading::hide)
        loadingHandle = loading.showCircular(I18n.multiplayer.modTransfer.title(), I18n.multiplayer.modTransfer.restoring()) {
            loadingHandle = null
        }
    }

    fun drive() {
        if (hosting) host.refresh()
        healStaleJoin()
        while (true) (events.poll() ?: break).invoke()
        progress.getAndSet(null)?.let { value -> loadingHandle?.let { loading.updateProgress(value.message, value.fraction, it) } }
    }

    private fun healStaleJoin() {
        val stale = active ?: return
        if (!stale.joined || stale.job?.isCompleted == false) return
        if (System.nanoTime() - stale.joinedAtNanos < STALE_JOIN_GRACE_NANOS) return
        if (game.isNetworkMultiplayerActive()) return
        launchOnIO("heal-stale-p2p-attempt") { cleanup(stale) }
    }

    fun hostRoom(share: Boolean, ready: () -> Unit) {
        if (hostingJob != null) return
        val handle = loading.showCircular(I18n.multiplayer.hostGame(), I18n.multiplayer.modTransfer.hosting())
        loading.disableCancellation(handle)
        hostingJob = launchOnIO("host-peer-room") {
            try {
                lobby.hostCurrentServer(share)
                ensureActive()
                events += {
                    hostingJob = null
                    loading.hide(handle)
                    startHosting(share)
                    ready()
                }
            } catch (error: Exception) {
                lobby.stopSession()
                game.leaveBattleRoom()
                events += {
                    hostingJob = null
                    loading.hide(handle)
                    if (error !is CancellationException) showError(I18n.multiplayer.modTransfer.failed(
                        (error as? TransferException)?.code?.name ?: "HOST_FAILED"))
                }
            }
        }
    }

    private fun startHosting(share: Boolean) {
        val roomId = lobby.hostedRoomId() ?: return
        hosting = true
        lobby.setTransferHandler(host::accept)
        host.prepare(roomId, share, lobby.signalingServices(), lobby::hostPassword, lobby::hostIsJoinable)
    }

    fun onRoomClosed() {
        if (hosting) { host.stopRoom(); hosting = false; lobby.stopSession() }
        active?.let { attempt ->
            attempt.cancelled.set(true)
            attempt.job?.cancel()
            showRestoring()
            launchOnIO("restore-closed-room") { cleanup(attempt) }
        }
    }

    fun close() {
        active?.job?.cancel()
        if (hosting) { host.close(); hosting = false }
    }
}
