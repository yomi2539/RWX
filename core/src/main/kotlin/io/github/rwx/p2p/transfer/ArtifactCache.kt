package io.github.rwx.p2p.transfer

import kotlinx.serialization.Serializable
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path

@Serializable
private data class CachedArtifact(val fileName: String, val sha256: String, val size: Long, val modified: Long,
                                  val unpackedBytes: Long, val fileCount: Int)

internal class ArtifactCache(val root: Path) {
    private val index = root.resolve("verified.json")
    private val entries = mutableMapOf<String, CachedArtifact>()

    init {
        transferRequire(!Files.isSymbolicLink(root), TransferErrorCode.UNSAFE_PACKAGE)
        Files.createDirectories(root)
        if (Files.isRegularFile(index, LinkOption.NOFOLLOW_LINKS) && Files.size(index) <= 2 * 1024 * 1024) {
            runCatching { transferJson.decodeFromString<List<CachedArtifact>>(Files.readAllBytes(index).toString(Charsets.UTF_8)) }
                .getOrNull()?.filter { it.sha256.isContentHash() && safeName(it.fileName) }
                ?.forEach { entries[it.sha256] = it }
        }
    }

    @Synchronized
    fun find(artifact: TransferArtifact): Path? {
        val entry = entries[artifact.artifactSha256] ?: return null
        val path = root.resolve(entry.fileName)
        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) || Files.size(path) != entry.size ||
            Files.getLastModifiedTime(path).toMillis() != entry.modified) {
            entries.remove(artifact.artifactSha256)
            return null
        }
        transferRequire(entry.size == artifact.size && entry.unpackedBytes == artifact.unpackedBytes && entry.fileCount == artifact.fileCount,
            TransferErrorCode.UNSAFE_PACKAGE)
        return path
    }

    @Synchronized
    fun remember(path: Path, artifact: TransferArtifact) {
        transferRequire(path.parent == root && safeName(path.fileName.toString()) && Files.size(path) == artifact.size)
        entries[artifact.artifactSha256] = CachedArtifact(path.fileName.toString(), artifact.artifactSha256, artifact.size,
            Files.getLastModifiedTime(path).toMillis(), artifact.unpackedBytes, artifact.fileCount)
        TransferFiles.atomicWrite(index, transferJson.encodeToString(entries.values.toList()).utf8())
    }

    @Synchronized
    fun reserve(bytes: Long, protected: Collection<Path> = emptyList()) {
        transferRequire(bytes in 0..TransferLimits.TOTAL_BYTES, TransferErrorCode.LIMIT_EXCEEDED)
        val keep = protected.toMutableSet().apply {
            add(index)
            protected.filter { it.fileName.toString().endsWith(".part") }.forEach {
                add(it.resolveSibling(it.fileName.toString().removeSuffix(".part") + ".json"))
            }
        }
        val candidates = Files.list(root).use { stream ->
            stream.filter { it != index && Files.isRegularFile(it, LinkOption.NOFOLLOW_LINKS) &&
                (safeName(it.fileName.toString()) || Regex("[0-9a-f]{64}\\.json").matches(it.fileName.toString())) }
                .iterator().asSequence().toList()
        }.sortedBy { Files.getLastModifiedTime(it).toMillis() }
        var used = candidates.sumOf { Files.size(it) }
        val now = System.currentTimeMillis()
        for (path in candidates) {
            if (path in keep) continue
            if (used + bytes <= TransferLimits.CACHE_BYTES && now - Files.getLastModifiedTime(path).toMillis() < TransferLimits.CACHE_TTL_MS) continue
            val size = Files.size(path)
            Files.delete(path)
            used -= size
        }
        transferRequire(used + bytes <= TransferLimits.CACHE_BYTES, TransferErrorCode.LIMIT_EXCEEDED)
        val usableSpace = runCatching { root.toFile().usableSpace }.getOrDefault(Long.MAX_VALUE)
        transferRequire(usableSpace >= bytes + 256L * 1024 * 1024, TransferErrorCode.DISK_FULL)
    }

    private fun safeName(value: String): Boolean = Regex("[0-9a-f-]+\\.(part|rwmod|tmp)").matches(value)
}
