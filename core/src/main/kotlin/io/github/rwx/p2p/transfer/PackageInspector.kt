package io.github.rwx.p2p.transfer

import org.apache.commons.compress.archivers.zip.ZipFile
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.BasicFileAttributes
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal data class PackageLimits(
    val archiveBytes: Long = TransferLimits.PACKAGE_BYTES,
    val unpackedBytes: Long = TransferLimits.UNPACKED_BYTES,
    val files: Int = TransferLimits.PACKAGE_FILES,
) {
    init {
        require(archiveBytes in 1..TransferLimits.PACKAGE_BYTES)
        require(unpackedBytes in 1..TransferLimits.UNPACKED_BYTES)
        require(files in 1..TransferLimits.PACKAGE_FILES)
    }
}

internal data class PackageSummary(val unpackedBytes: Long, val fileCount: Int)
internal data class BuiltPackage(val sha256: String, val size: Long, val summary: PackageSummary)

internal object PackageInspector {
    fun validatePath(path: String): String {
        unsafe(path.isNotEmpty() && path.length <= 512 && !path.startsWith('/'))
        unsafe(path.none { it.code < 32 || it.code == 127 || it == '\\' })
        val parts = path.split('/')
        unsafe(parts.all { it.isNotEmpty() && it != "." && it != ".." })
        unsafe(!path.startsWith("META-INF/", ignoreCase = true) && parts.last().lowercase(Locale.ROOT) != "mod.toml")
        return path
    }

    fun inspectArchive(file: Path, limits: PackageLimits = PackageLimits(), checkCancelled: () -> Unit = {}): PackageSummary {
        unsafe(!Files.isSymbolicLink(file) && Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS))
        val archiveSize = Files.size(file)
        transferRequire(archiveSize in 1..limits.archiveBytes, TransferErrorCode.LIMIT_EXCEEDED)
        val paths = PackagePaths()
        var size = 0L
        var count = 0
        try {
            ZipFile.builder().setFile(file.toFile()).get().use { archive ->
                val entries = archive.entries
                while (entries.hasMoreElements()) {
                    checkCancelled()
                    val entry = entries.nextElement()
                    if (entry.isDirectory) continue
                    val path = validatePath(entry.name)
                    paths.add(path)
                    unsafe(!entry.isUnixSymlink && !entry.generalPurposeBit.usesEncryption() && archive.canReadEntryData(entry))
                    unsafe(entry.method == ZipEntry.STORED || entry.method == ZipEntry.DEFLATED)
                    transferRequire(++count <= limits.files && entry.size in 0..(limits.unpackedBytes - size), TransferErrorCode.LIMIT_EXCEEDED)
                    size += entry.size
                }
            }
        } catch (error: TransferException) {
            throw error
        } catch (error: java.io.IOException) {
            throw TransferException(TransferErrorCode.UNSAFE_PACKAGE, cause = error)
        }
        unsafe(count > 0)
        return PackageSummary(size, count)
    }

    private fun unsafe(condition: Boolean) = transferRequire(condition, TransferErrorCode.UNSAFE_PACKAGE)
}

private class PackagePaths {
    private val seen = mutableSetOf<String>()

    fun add(path: String) {
        transferRequire(seen.add(path.uppercase(Locale.ROOT)), TransferErrorCode.UNSAFE_PACKAGE)
    }
}

internal object PackageBuilder {
    fun fromDirectory(root: Path, destination: Path, checkCancelled: () -> Unit = {}): BuiltPackage {
        transferRequire(Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(root), TransferErrorCode.UNSAFE_PACKAGE)
        val digest = MessageDigest.getInstance("SHA-256")
        val paths = PackagePaths()
        var unpacked = 0L
        var count = 0
        DigestOutputStream(Files.newOutputStream(destination, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE), digest).use { output ->
            ZipOutputStream(output).use { zip ->
                Files.walk(root).use { walk ->
                    val iterator = walk.iterator()
                    while (iterator.hasNext()) {
                        checkCancelled()
                        val file = iterator.next()
                        if (file == root) continue
                        val name = PackageInspector.validatePath(root.relativize(file).joinToString("/"))
                        val attrs = Files.readAttributes(file, BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
                        transferRequire(!attrs.isSymbolicLink && (attrs.isDirectory || attrs.isRegularFile), TransferErrorCode.UNSAFE_PACKAGE)
                        paths.add(name)
                        if (attrs.isDirectory) continue
                        transferRequire(++count <= TransferLimits.PACKAGE_FILES, TransferErrorCode.LIMIT_EXCEEDED)
                        zip.putNextEntry(ZipEntry(name).apply { time = 0L })
                        Files.newInputStream(file, LinkOption.NOFOLLOW_LINKS).use { input ->
                            val buffer = ByteArray(TransferLimits.CHUNK_BYTES)
                            while (true) {
                                checkCancelled()
                                val read = input.read(buffer)
                                if (read < 0) break
                                transferRequire(read.toLong() <= TransferLimits.UNPACKED_BYTES - unpacked, TransferErrorCode.LIMIT_EXCEEDED)
                                unpacked += read
                                zip.write(buffer, 0, read)
                            }
                        }
                        zip.closeEntry()
                    }
                }
            }
        }
        transferRequire(count > 0, TransferErrorCode.UNSUPPORTED_MOD)
        val size = Files.size(destination)
        transferRequire(size in 1..TransferLimits.PACKAGE_BYTES, TransferErrorCode.LIMIT_EXCEEDED)
        return BuiltPackage(digest.digest().hex(), size, PackageSummary(unpacked, count))
    }

    fun fromArchive(source: Path, destination: Path, checkCancelled: () -> Unit = {}): BuiltPackage {
        val summary = PackageInspector.inspectArchive(source, checkCancelled = checkCancelled)
        val digest = MessageDigest.getInstance("SHA-256")
        DigestOutputStream(Files.newOutputStream(destination, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE), digest).use { target ->
            Files.newInputStream(source, LinkOption.NOFOLLOW_LINKS).use { input ->
                val buffer = ByteArray(TransferLimits.CHUNK_BYTES)
                while (true) {
                    checkCancelled()
                    val count = input.read(buffer)
                    if (count < 0) break
                    target.write(buffer, 0, count)
                }
            }
        }
        val size = Files.size(destination)
        transferRequire(size in 1..TransferLimits.PACKAGE_BYTES, TransferErrorCode.LIMIT_EXCEEDED)
        return BuiltPackage(digest.digest().hex(), size, summary)
    }
}
