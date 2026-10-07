package io.github.rwx.p2p.transfer

import kotlinx.serialization.Serializable
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import java.util.UUID

internal object TransferFiles {
    fun atomicWrite(path: Path, bytes: ByteArray) {
        transferRequire(!Files.isSymbolicLink(path), TransferErrorCode.UNSAFE_PACKAGE)
        val temporary = path.resolveSibling("${path.fileName}.${UUID.randomUUID()}.tmp")
        try {
            FileChannel.open(temporary, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE).use { channel ->
                val buffer = ByteBuffer.wrap(bytes)
                while (buffer.hasRemaining()) channel.write(buffer)
                channel.force(true)
            }
            try {
                Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING)
            }
        } finally {
            Files.deleteIfExists(temporary)
        }
    }

    fun sha256(path: Path, checkCancelled: () -> Unit = {}): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS).use { input ->
            val buffer = ByteArray(TransferLimits.CHUNK_BYTES)
            while (true) {
                checkCancelled()
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().hex()
    }
}

@Serializable
private data class DownloadCheckpoint(
    val version: Int,
    val hostIdentity: String,
    val artifactSha256: String,
    val size: Long,
    val prefixBytes: Long,
    val chunks: List<String>,
    val updatedAtMs: Long,
)

internal class ResumableDownload(
    root: Path,
    private val hostIdentity: String,
    val artifact: TransferArtifact,
    private val checkCancelled: () -> Unit = {},
) : AutoCloseable {
    val path: Path
    private val checkpointPath: Path
    private val file: RandomAccessFile
    private var checkpoint: DownloadCheckpoint
    private val digest = MessageDigest.getInstance("SHA-256")
    private var completedHash: String? = null
    val completedBytes: Long get() = checkpoint.prefixBytes

    init {
        artifact.validate()
        transferRequire(hostIdentity.isContentHash(), TransferErrorCode.IDENTITY_MISMATCH)
        transferRequire(!Files.isSymbolicLink(root), TransferErrorCode.UNSAFE_PACKAGE)
        Files.createDirectories(root)
        val key = "$hostIdentity:${artifact.artifactSha256}:${artifact.size}:1".utf8().sha256()
        path = root.resolve("$key.part")
        checkpointPath = root.resolve("$key.json")
        transferRequire(!Files.isSymbolicLink(path) && !Files.isSymbolicLink(checkpointPath), TransferErrorCode.UNSAFE_PACKAGE)
        file = RandomAccessFile(path.toFile(), "rw")
        try {
            checkpoint = recover()
        } catch (error: Throwable) {
            file.close()
            throw error
        }
    }

    private fun recover(): DownloadCheckpoint {
        val empty = DownloadCheckpoint(1, hostIdentity, artifact.artifactSha256, artifact.size, 0, emptyList(), System.currentTimeMillis())
        val stored = if (Files.exists(checkpointPath) && Files.size(checkpointPath) <= 1024 * 1024) {
            runCatching {
                transferJson.decodeFromString(DownloadCheckpoint.serializer(), Files.readAllBytes(checkpointPath).toString(Charsets.UTF_8))
            }.getOrNull()
        } else null
        val valid = stored?.takeIf {
            it.version == 1 && it.hostIdentity == hostIdentity && it.artifactSha256 == artifact.artifactSha256 &&
                it.size == artifact.size && it.prefixBytes in 0..artifact.size &&
                (it.prefixBytes % TransferLimits.CHUNK_BYTES == 0L || it.prefixBytes == artifact.size) &&
                it.chunks.size.toLong() == (it.prefixBytes + TransferLimits.CHUNK_BYTES - 1) / TransferLimits.CHUNK_BYTES &&
                it.chunks.all(String::isContentHash)
        } ?: empty
        var recovered = 0L
        val hashes = mutableListOf<String>()
        for (hash in valid.chunks) {
            checkCancelled()
            val count = minOf(TransferLimits.CHUNK_BYTES.toLong(), artifact.size - recovered).toInt()
            if (file.length() - recovered < count) break
            val bytes = ByteArray(count)
            file.seek(recovered)
            file.readFully(bytes)
            if (bytes.sha256() != hash) break
            hashes += hash
            digest.update(bytes)
            recovered += count
        }
        file.setLength(recovered)
        file.fd.sync()
        val result = empty.copy(prefixBytes = recovered, chunks = hashes)
        writeCheckpoint(result)
        return result
    }

    fun append(offset: Long, bytes: ByteArray, chunkSha256: String) {
        checkCancelled()
        transferRequire(offset == completedBytes && offset % TransferLimits.CHUNK_BYTES == 0L)
        val remaining = artifact.size - offset
        transferRequire(remaining > 0 && bytes.size == minOf(TransferLimits.CHUNK_BYTES.toLong(), remaining).toInt())
        transferRequire(chunkSha256.isContentHash() && bytes.sha256() == chunkSha256, TransferErrorCode.HASH_MISMATCH)
        file.seek(offset)
        file.write(bytes)
        file.fd.sync()
        val next = checkpoint.copy(prefixBytes = offset + bytes.size, chunks = checkpoint.chunks + chunkSha256,
            updatedAtMs = System.currentTimeMillis())
        writeCheckpoint(next)
        digest.update(bytes)
        completedHash = null
        checkpoint = next
    }

    fun verifyComplete(): Path {
        transferRequire(completedBytes == artifact.size)
        val hash = completedHash ?: digest.digest().hex().also { completedHash = it }
        if (hash != artifact.artifactSha256) {
            file.setLength(0)
            file.fd.sync()
            Files.deleteIfExists(checkpointPath)
            checkpoint = checkpoint.copy(prefixBytes = 0, chunks = emptyList())
            digest.reset()
            completedHash = null
            throw TransferException(TransferErrorCode.HASH_MISMATCH)
        }
        return path
    }

    private fun writeCheckpoint(value: DownloadCheckpoint) {
        TransferFiles.atomicWrite(checkpointPath, transferJson.encodeToString(DownloadCheckpoint.serializer(), value).utf8())
    }

    override fun close() = file.close()
}
