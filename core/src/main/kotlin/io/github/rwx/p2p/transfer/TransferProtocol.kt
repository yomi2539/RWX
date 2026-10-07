package io.github.rwx.p2p.transfer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.CodingErrorAction
import java.security.MessageDigest
import java.util.UUID

object TransferLimits {
    const val VERSION = 1
    const val MAX_SIGNALS_PER_SESSION = 100_000
    const val MAX_SIGNED_PAYLOAD_BYTES = 48 * 1024
    const val TRANSFER_HANDSHAKE_TIMEOUT_MS = 60_000L
    const val CHUNK_BYTES = 64 * 1024
    const val HEADER_BYTES = 8 * 1024
    const val MANIFEST_BYTES = 2 * 1024 * 1024
    const val FRAME_BYTES = 4 + MANIFEST_BYTES + CHUNK_BYTES
    const val ARTIFACTS = 256
    const val UNITS = 16_384
    const val PACKAGE_BYTES = 512L * 1024 * 1024
    const val TOTAL_BYTES = 2L * 1024 * 1024 * 1024
    const val UNPACKED_BYTES = 2L * 1024 * 1024 * 1024
    const val TOTAL_UNPACKED_BYTES = 8L * 1024 * 1024 * 1024
    const val PACKAGE_FILES = 10_000
    const val TOTAL_FILES = 50_000
    const val CACHE_BYTES = 4L * 1024 * 1024 * 1024
    const val CACHE_TTL_MS = 7L * 24 * 60 * 60 * 1000
}

@Serializable
enum class TransferErrorCode {
    UNSUPPORTED_VERSION, UNSUPPORTED_MOD, PROTOCOL_ERROR, IDENTITY_MISMATCH,
    AUTH_FAILED, ROOM_CLOSED, MANIFEST_CHANGED, BUSY, NOT_READY,
    LIMIT_EXCEEDED, DISK_FULL, HASH_MISMATCH, UNSAFE_PACKAGE, LOAD_FAILED,
    RECOVERY_REQUIRED,
    TIMED_OUT,
}

class TransferException(val code: TransferErrorCode, detail: String = code.name, cause: Throwable? = null) :
    IOException(detail, cause)

internal fun transferRequire(condition: Boolean, code: TransferErrorCode = TransferErrorCode.PROTOCOL_ERROR) {
    if (!condition) throw TransferException(code)
}

internal val transferJson = Json { ignoreUnknownKeys = true; encodeDefaults = true }
private val hashPattern = Regex("[0-9a-f]{64}")
private val idPattern = Regex("[A-Za-z0-9_-]{1,128}")
internal fun String.isContentHash(): Boolean = hashPattern.matches(this)
internal fun String.isTransferId(): Boolean = idPattern.matches(this)
internal fun String.utf8(): ByteArray = try {
    val bytes = Charsets.UTF_8.newEncoder().onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT).encode(java.nio.CharBuffer.wrap(this))
    ByteArray(bytes.remaining()).also(bytes::get)
} catch (error: java.nio.charset.CharacterCodingException) {
    throw TransferException(TransferErrorCode.PROTOCOL_ERROR, cause = error)
}
internal fun ByteArray.sha256(): String = MessageDigest.getInstance("SHA-256").digest(this).hex()
internal fun ByteArray.hex(): String = joinToString("") { "%02x".format(it.toInt() and 255) }

@Serializable
data class TransferArtifact(
    val artifactId: String,
    val displayName: String,
    val version: String?,
    val kind: String,
    val format: String,
    val artifactSha256: String,
    val size: Long,
    val unpackedBytes: Long,
    val fileCount: Int,
) {
    fun validate() {
        transferRequire(artifactId.isTransferId())
        transferRequire(displayName.isNotBlank() && displayName.utf8().size <= 256)
        transferRequire(version == null || version.utf8().size <= 128)
        transferRequire(kind == "legacy-data" && format == "rwmod", TransferErrorCode.UNSUPPORTED_MOD)
        transferRequire(artifactSha256.isContentHash())
        transferRequire(size in 1..TransferLimits.PACKAGE_BYTES, TransferErrorCode.LIMIT_EXCEEDED)
        transferRequire(unpackedBytes in 1..TransferLimits.UNPACKED_BYTES, TransferErrorCode.LIMIT_EXCEEDED)
        transferRequire(fileCount in 1..TransferLimits.PACKAGE_FILES, TransferErrorCode.LIMIT_EXCEEDED)
    }
}

@Serializable
data class RequiredUnit(val name: String, val configHash: Int, val artifactId: String? = null)

@Serializable
data class TransferManifest(
    val protocolVersion: Int,
    val roomId: String,
    val hostIdentity: String,
    val manifestId: String,
    val gameVersionCode: Int,
    val packageFormatVersion: Int,
    val artifacts: List<TransferArtifact>,
    val requiredUnits: List<RequiredUnit>,
    val totalBytes: Long,
) {
    fun validate(expectedRoom: String, expectedHost: String, expectedGameVersion: Int) {
        transferRequire(protocolVersion == TransferLimits.VERSION && packageFormatVersion == 1,
            TransferErrorCode.UNSUPPORTED_VERSION)
        transferRequire(roomId == expectedRoom && roomId.isTransferId())
        transferRequire(hostIdentity == expectedHost && hostIdentity.isContentHash(), TransferErrorCode.IDENTITY_MISMATCH)
        transferRequire(runCatching { UUID.fromString(manifestId).toString() == manifestId }.getOrDefault(false))
        transferRequire(gameVersionCode == expectedGameVersion, TransferErrorCode.UNSUPPORTED_VERSION)
        transferRequire(artifacts.size <= TransferLimits.ARTIFACTS && requiredUnits.size <= TransferLimits.UNITS,
            TransferErrorCode.LIMIT_EXCEEDED)
        artifacts.forEach(TransferArtifact::validate)
        val artifactIds = artifacts.map { it.artifactId }.toSet()
        transferRequire(artifactIds.size == artifacts.size)
        requiredUnits.forEach {
            transferRequire(it.name.isNotBlank() && it.name.utf8().size <= 256)
            transferRequire(it.artifactId == null || it.artifactId in artifactIds)
        }
        transferRequire(artifacts.sumOf { it.size } == totalBytes && totalBytes in 0..TransferLimits.TOTAL_BYTES,
            TransferErrorCode.LIMIT_EXCEEDED)
        transferRequire(artifacts.sumOf { it.unpackedBytes } <= TransferLimits.TOTAL_UNPACKED_BYTES,
            TransferErrorCode.LIMIT_EXCEEDED)
        transferRequire(artifacts.sumOf { it.fileCount } <= TransferLimits.TOTAL_FILES, TransferErrorCode.LIMIT_EXCEEDED)
    }
}

@Serializable
sealed class TransferHeader {
    abstract val requestId: Long

    @Serializable @SerialName("Hello")
    data class Hello(override val requestId: Long, val versions: List<Int>, val roomId: String, val attemptNonce: String) : TransferHeader()
    @Serializable @SerialName("HelloAck")
    data class HelloAck(override val requestId: Long, val version: Int, val roomId: String, val hostIdentity: String,
        val attemptNonce: String, val authRequired: Boolean) : TransferHeader()
    @Serializable @SerialName("Authorize")
    data class Authorize(override val requestId: Long, val password: String) : TransferHeader()
    @Serializable @SerialName("Authorized")
    data class Authorized(override val requestId: Long) : TransferHeader()
    @Serializable @SerialName("ManifestRequest")
    data class ManifestRequest(override val requestId: Long, val knownManifestId: String? = null) : TransferHeader()
    @Serializable @SerialName("Manifest")
    data class Manifest(override val requestId: Long, val manifest: TransferManifest) : TransferHeader()
    @Serializable @SerialName("ChunkRequest")
    data class ChunkRequest(override val requestId: Long, val manifestId: String, val artifactId: String,
                            val offset: Long, val length: Int) : TransferHeader()
    @Serializable @SerialName("Chunk")
    data class Chunk(override val requestId: Long, val manifestId: String, val artifactId: String,
                     val offset: Long, val length: Int, val chunkSha256: String) : TransferHeader()
    @Serializable @SerialName("ValidateManifest")
    data class ValidateManifest(override val requestId: Long, val manifestId: String) : TransferHeader()
    @Serializable @SerialName("ManifestValid")
    data class ManifestValid(override val requestId: Long, val manifestId: String) : TransferHeader()
    @Serializable @SerialName("Cancel")
    data class Cancel(override val requestId: Long, val manifestId: String? = null) : TransferHeader()
    @Serializable @SerialName("Error")
    data class Error(override val requestId: Long, val code: TransferErrorCode, val retryable: Boolean,
        val retryAfterMs: Long? = null) : TransferHeader()
}

data class TransferFrame(val header: TransferHeader, val payload: ByteArray = byteArrayOf())

object TransferFrameCodec {
    fun encode(frame: TransferFrame): ByteArray {
        validatePayload(frame)
        val header = transferJson.encodeToString(TransferHeader.serializer(), frame.header).utf8()
        validateHeaderSize(frame.header, header.size)
        val length = 4 + header.size + frame.payload.size
        return ByteBuffer.allocate(4 + length).order(ByteOrder.BIG_ENDIAN)
            .putInt(length).putInt(header.size).put(header).put(frame.payload).array()
    }

    internal fun decode(body: ByteArray): TransferFrame {
        val buffer = ByteBuffer.wrap(body).order(ByteOrder.BIG_ENDIAN)
        transferRequire(buffer.remaining() >= 4)
        val headerLength = buffer.int
        transferRequire(headerLength in 1..TransferLimits.MANIFEST_BYTES && headerLength <= buffer.remaining())
        val headerBytes = ByteArray(headerLength).also(buffer::get)
        val header = try {
            val json = Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(headerBytes)).toString()
            transferJson.decodeFromString(TransferHeader.serializer(), json)
        } catch (error: Exception) {
            throw TransferException(TransferErrorCode.PROTOCOL_ERROR, cause = error)
        }
        validateHeaderSize(header, headerLength)
        transferRequire(buffer.remaining() <= TransferLimits.CHUNK_BYTES)
        return TransferFrame(header, ByteArray(buffer.remaining()).also(buffer::get)).also(::validatePayload)
    }

    private fun validateHeaderSize(header: TransferHeader, length: Int) {
        val maximum = if (header is TransferHeader.Manifest) TransferLimits.MANIFEST_BYTES else TransferLimits.HEADER_BYTES
        transferRequire(length in 1..maximum)
    }

    private fun validatePayload(frame: TransferFrame) {
        transferRequire(frame.header.requestId > 0)
        val chunk = frame.header as? TransferHeader.Chunk
        if (chunk == null) {
            transferRequire(frame.payload.isEmpty())
        } else {
            transferRequire(chunk.length in 1..TransferLimits.CHUNK_BYTES && chunk.length == frame.payload.size)
            transferRequire(chunk.offset >= 0 && chunk.offset % TransferLimits.CHUNK_BYTES == 0L)
            transferRequire(chunk.chunkSha256.isContentHash())
            transferRequire(frame.payload.sha256() == chunk.chunkSha256, TransferErrorCode.HASH_MISMATCH)
        }
    }
}

class TransferFrameDecoder {
    private val lengthBytes = ByteArray(4)
    private var lengthRead = 0
    private var body: ByteArray? = null
    private var bodyRead = 0

    fun accept(bytes: ByteBuffer, receive: (TransferFrame) -> Unit) {
        while (bytes.hasRemaining()) {
            if (body == null) {
                val count = minOf(4 - lengthRead, bytes.remaining())
                bytes.get(lengthBytes, lengthRead, count)
                lengthRead += count
                if (lengthRead < 4) continue
                val length = ByteBuffer.wrap(lengthBytes).order(ByteOrder.BIG_ENDIAN).int
                transferRequire(length in 5..TransferLimits.FRAME_BYTES)
                body = ByteArray(length)
                bodyRead = 0
            }
            val current = body!!
            val count = minOf(current.size - bodyRead, bytes.remaining())
            bytes.get(current, bodyRead, count)
            bodyRead += count
            if (bodyRead == current.size) {
                body = null
                lengthRead = 0
                bodyRead = 0
                receive(TransferFrameCodec.decode(current))
            }
        }
    }

    fun finish() {
        transferRequire(lengthRead == 0 && body == null)
    }
}
