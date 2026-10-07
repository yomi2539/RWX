package io.github.rwx.p2p.transfer

import io.github.rwx.p2p.WebRtcTransferConnection
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import java.io.RandomAccessFile
import java.nio.file.Path
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration.Companion.milliseconds

internal data class HostedSnapshot(
    val manifest: TransferManifest,
    val files: Map<String, Path>,
    val revision: Long,
)

internal class TransferServer(
    private val roomId: String,
    private val identity: String,
    private val snapshot: () -> HostedSnapshot?,
    private val password: () -> String?,
    private val available: () -> Boolean,
) {
    private data class Attempts(var failures: Int = 0, var blockedUntil: Long = 0)
    private val attempts = ConcurrentHashMap<String, Attempts>()
    private val nextBytesAt = AtomicLong()

    suspend fun serve(connection: WebRtcTransferConnection, peerIdentity: String) {
        val wire = TransferWire(connection)
        var lastId = 0L
        var hello = false
        var authorized = false
        var sessionPassword: String? = null
        var selected: HostedSnapshot? = null
        var reader: RandomAccessFile? = null
        var readerArtifact: String? = null
        try {
            while (true) {
                val frame = wire.receive(waitForRequest = true)
                val request = frame.header
                transferRequire(request.requestId > lastId)
                lastId = request.requestId
                try {
                    transferRequire(available(), TransferErrorCode.ROOM_CLOSED)
                    if (hello) transferRequire(sessionPassword == password(), TransferErrorCode.MANIFEST_CHANGED)
                    when (request) {
                        is TransferHeader.Hello -> {
                            transferRequire(!hello && request.roomId == roomId && request.attemptNonce.isTransferId())
                            transferRequire(1 in request.versions, TransferErrorCode.UNSUPPORTED_VERSION)
                            sessionPassword = password()
                            authorized = sessionPassword == null
                            hello = true
                            wire.send(TransferHeader.HelloAck(lastId, 1, roomId, identity, request.attemptNonce, !authorized))
                        }
                        is TransferHeader.Authorize -> {
                            transferRequire(hello && !authorized)
                            authorized = authorize(peerIdentity, request.password, sessionPassword!!)
                            transferRequire(authorized, TransferErrorCode.AUTH_FAILED)
                            wire.send(TransferHeader.Authorized(lastId))
                        }
                        is TransferHeader.ManifestRequest -> {
                            transferRequire(hello && authorized, TransferErrorCode.AUTH_FAILED)
                            selected = snapshot() ?: throw TransferException(TransferErrorCode.NOT_READY)
                            checkCurrent(selected)
                            wire.send(TransferHeader.Manifest(lastId, selected.manifest))
                        }
                        is TransferHeader.ValidateManifest -> {
                            transferRequire(hello && authorized, TransferErrorCode.AUTH_FAILED)
                            val current = snapshot() ?: throw TransferException(TransferErrorCode.MANIFEST_CHANGED)
                            checkCurrent(current)
                            transferRequire(request.manifestId == current.manifest.manifestId, TransferErrorCode.MANIFEST_CHANGED)
                            wire.send(TransferHeader.ManifestValid(lastId, request.manifestId))
                        }
                        is TransferHeader.ChunkRequest -> {
                            transferRequire(hello && authorized, TransferErrorCode.AUTH_FAILED)
                            val current = selected ?: throw TransferException(TransferErrorCode.PROTOCOL_ERROR)
                            checkCurrent(current)
                            transferRequire(request.manifestId == current.manifest.manifestId, TransferErrorCode.MANIFEST_CHANGED)
                            val artifact = current.manifest.artifacts.firstOrNull { it.artifactId == request.artifactId }
                                ?: throw TransferException(TransferErrorCode.PROTOCOL_ERROR)
                            transferRequire(request.offset >= 0 && request.offset < artifact.size && request.offset % TransferLimits.CHUNK_BYTES == 0L)
                            transferRequire(request.length == minOf(TransferLimits.CHUNK_BYTES.toLong(), artifact.size - request.offset).toInt())
                            if (readerArtifact != request.artifactId) {
                                reader?.close()
                                reader = RandomAccessFile(current.files.getValue(request.artifactId).toFile(), "r")
                                readerArtifact = request.artifactId
                            }
                            val bytes = ByteArray(request.length)
                            reader!!.seek(request.offset)
                            reader.readFully(bytes)
                            throttle(bytes.size)
                            wire.send(TransferHeader.Chunk(lastId, request.manifestId, request.artifactId, request.offset,
                                bytes.size, bytes.sha256()), bytes)
                        }
                        is TransferHeader.Cancel -> return
                        else -> throw TransferException(TransferErrorCode.PROTOCOL_ERROR)
                    }
                } catch (error: TransferException) {
                    val retryable = error.code in setOf(TransferErrorCode.NOT_READY, TransferErrorCode.BUSY, TransferErrorCode.AUTH_FAILED)
                    wire.send(TransferHeader.Error(lastId, error.code, retryable, if (retryable) 2000 else null))
                    if (!retryable) return
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // transport died mid-session (peer cancel/disconnect): end quietly
        } finally {
            reader?.close()
            wire.close()
        }
    }

    private fun checkCurrent(value: HostedSnapshot) {
        transferRequire(value.revision == TransferRevision.current && snapshot()?.manifest?.manifestId == value.manifest.manifestId,
            TransferErrorCode.MANIFEST_CHANGED)
    }

    private fun authorize(peer: String, provided: String, expected: String): Boolean {
        val now = System.currentTimeMillis()
        attempts.entries.removeIf { it.value.blockedUntil in 1..<now }
        if (attempts.size >= 128 && !attempts.containsKey(peer)) return false
        val state = attempts.computeIfAbsent(peer) { Attempts() }
        synchronized(state) {
            if (state.blockedUntil > now) return false
            val accepted = MessageDigest.isEqual(provided.utf8(), expected.utf8())
            if (accepted) attempts.remove(peer)
            else if (++state.failures >= 3) state.blockedUntil = now + 30_000
            return accepted
        }
    }

    private suspend fun throttle(bytes: Int) {
        val now = System.nanoTime()
        val cost = bytes.toLong() * 1_000_000_000 / (8 * 1024 * 1024)
        val at = nextBytesAt.getAndUpdate { maxOf(it, now) + cost }
        if (at > now) delay(((at - now + 999_999) / 1_000_000).milliseconds)
    }
}
