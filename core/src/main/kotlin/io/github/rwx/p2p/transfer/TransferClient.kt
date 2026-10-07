package io.github.rwx.p2p.transfer

import io.github.rwx.p2p.WebRtcTransferConnection
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import java.nio.ByteBuffer
import java.nio.file.Path
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

class TransferWire(private val connection: WebRtcTransferConnection) : AutoCloseable {
    private val decoder = TransferFrameDecoder()
    private val pending = ArrayDeque<TransferFrame>()
    private val readLock = Mutex()

    suspend fun send(header: TransferHeader, payload: ByteArray = byteArrayOf()) {
        connection.awaitOpen()
        connection.send(TransferFrameCodec.encode(TransferFrame(header, payload)))
    }

    suspend fun receive(waitForRequest: Boolean = false): TransferFrame = readLock.withLock {
        var firstPacket = true
        while (pending.isEmpty()) {
            val timeout = if (firstPacket && waitForRequest) 1_800_000L
                else if (firstPacket) TransferLimits.TRANSFER_HANDSHAKE_TIMEOUT_MS else 20_000L
            val packet = withTimeout(timeout.milliseconds) { connection.receive() }
            firstPacket = false
            decoder.accept(ByteBuffer.wrap(packet)) {
                transferRequire(pending.size < 2)
                pending.addLast(it)
            }
        }
        pending.removeFirst()
    }

    override fun close() = connection.close()
}

class TransferClient(
    connection: WebRtcTransferConnection,
    private val roomId: String,
    private val hostIdentity: String,
    private val gameVersion: Int,
) : AutoCloseable {
    private val wire = TransferWire(connection)
    private val requests = Mutex()
    private var nextId = 0L
    private val nonce = UUID.randomUUID().toString()

    suspend fun hello(): Boolean {
        val response = request { TransferHeader.Hello(it, listOf(1), roomId, nonce) }.header as? TransferHeader.HelloAck
            ?: throw TransferException(TransferErrorCode.PROTOCOL_ERROR)
        transferRequire(response.version == 1 && response.roomId == roomId && response.attemptNonce == nonce)
        transferRequire(response.hostIdentity == hostIdentity, TransferErrorCode.IDENTITY_MISMATCH)
        return response.authRequired
    }

    suspend fun authorize(password: String) {
        transferRequire(request { TransferHeader.Authorize(it, password) }.header is TransferHeader.Authorized)
    }

    suspend fun manifest(): TransferManifest {
        val response = request { TransferHeader.ManifestRequest(it) }.header as? TransferHeader.Manifest
            ?: throw TransferException(TransferErrorCode.PROTOCOL_ERROR)
        return response.manifest.also { it.validate(roomId, hostIdentity, gameVersion) }
    }

    suspend fun validate(manifest: TransferManifest) {
        val response = request { TransferHeader.ValidateManifest(it, manifest.manifestId) }.header as? TransferHeader.ManifestValid
            ?: throw TransferException(TransferErrorCode.PROTOCOL_ERROR)
        transferRequire(response.manifestId == manifest.manifestId)
    }

    internal suspend fun download(manifest: TransferManifest, artifact: TransferArtifact, cache: Path,
                                  progress: (Long) -> Unit): Path {
        transferRequire(artifact in manifest.artifacts)
        ResumableDownload(cache, hostIdentity, artifact).use { download ->
            progress(download.completedBytes)
            while (download.completedBytes < artifact.size) {
                val offset = download.completedBytes
                val length = minOf(TransferLimits.CHUNK_BYTES.toLong(), artifact.size - offset).toInt()
                var failures = 0
                while (true) {
                    try {
                        val response = request { TransferHeader.ChunkRequest(it, manifest.manifestId, artifact.artifactId, offset, length) }
                        val chunk = response.header as? TransferHeader.Chunk ?: throw TransferException(TransferErrorCode.PROTOCOL_ERROR)
                        transferRequire(chunk.manifestId == manifest.manifestId && chunk.artifactId == artifact.artifactId &&
                            chunk.offset == offset && chunk.length == length)
                        download.append(offset, response.payload, chunk.chunkSha256)
                        break
                    } catch (error: TransferException) {
                        if (error.code != TransferErrorCode.HASH_MISMATCH || failures >= 2) throw error
                        delay((500L shl failures++).milliseconds)
                    }
                }
                progress(download.completedBytes)
            }
            val path = download.verifyComplete()
            PackageInspector.inspectArchive(path, PackageLimits(
                unpackedBytes = artifact.unpackedBytes, files = artifact.fileCount))
            return path
        }
    }

    private suspend fun request(header: (Long) -> TransferHeader): TransferFrame = requests.withLock {
        val id = ++nextId
        wire.send(header(id))
        val response = wire.receive()
        transferRequire(response.header.requestId == id)
        val error = response.header as? TransferHeader.Error
        if (error != null) throw TransferException(error.code)
        response
    }

    override fun close() = wire.close()
}
