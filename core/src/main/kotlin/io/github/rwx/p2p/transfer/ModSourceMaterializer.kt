package io.github.rwx.p2p.transfer

import com.corrodinggames.rts.gameFramework.file.FileHelper
import com.corrodinggames.rts.gameFramework.utility.FileLoaderFactory
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.UUID

internal interface ModSourceReader {
    fun isDirectoryNonZip(abstractPath: String): Boolean
    fun fileExists(abstractPath: String): Boolean
    fun listDir(abstractPath: String): Array<String>?
    fun openInput(abstractPath: String): InputStream?
}

internal object EngineModSourceReader : ModSourceReader {
    override fun isDirectoryNonZip(abstractPath: String): Boolean =
        runCatching { FileHelper.isDirectoryNonZip(abstractPath) }.getOrDefault(false)

    override fun fileExists(abstractPath: String): Boolean =
        runCatching { FileHelper.fileExists(abstractPath) }.getOrDefault(false)

    override fun listDir(abstractPath: String): Array<String>? =
        runCatching { FileHelper.listFiles(abstractPath) }.getOrNull()

    override fun openInput(abstractPath: String): InputStream? {
        val converted = runCatching { FileHelper.convertAbstractPath(abstractPath) }.getOrNull()
        if (converted != null) {
            runCatching {
                FileLoaderFactory.getZipFileLoaderForPath(converted)?.openAssetInputStream(converted, true)
            }.getOrNull()?.let { return it }
        }
        runCatching { FileHelper.openFileByPath(abstractPath) }.getOrNull()?.let { return it }
        if (converted != null && converted != abstractPath) {
            runCatching { FileHelper.openFileByPath(converted) }.getOrNull()?.let { return it }
        }
        return null
    }
}

internal object ModSourceMaterializer {
    fun needsFallback(sourcePath: Path): Boolean {
        if (Files.isSymbolicLink(sourcePath)) return false
        return !Files.isRegularFile(sourcePath, LinkOption.NOFOLLOW_LINKS) &&
                !Files.isDirectory(sourcePath, LinkOption.NOFOLLOW_LINKS)
    }

    fun packageFallback(
        reader: ModSourceReader,
        rawAbstractPath: String,
        destination: Path,
        stagingRoot: Path,
        checkCancelled: () -> Unit = {},
    ): BuiltPackage {
        checkCancelled()
        if (reader.isDirectoryNonZip(rawAbstractPath)) {
            val stagingDir = stagingRoot.resolve("${UUID.randomUUID()}.tmp.d")
            try {
                materializeDirectory(reader, rawAbstractPath, stagingDir, checkCancelled)
                checkCancelled()
                return PackageBuilder.fromDirectory(stagingDir, destination, checkCancelled)
            } finally {
                deleteRecursively(stagingDir)
            }
        }
        transferRequire(reader.fileExists(rawAbstractPath), TransferErrorCode.UNSAFE_PACKAGE)
        val stagingFile = stagingRoot.resolve("${UUID.randomUUID()}.tmp")
        try {
            materializeArchive(reader, rawAbstractPath, stagingFile, checkCancelled)
            checkCancelled()
            return PackageBuilder.fromArchive(stagingFile, destination, checkCancelled)
        } finally {
            Files.deleteIfExists(stagingFile)
        }
    }

    fun materializeArchive(
        reader: ModSourceReader,
        abstractPath: String,
        stagingFile: Path,
        checkCancelled: () -> Unit = {},
    ): Path {
        checkCancelled()
        val input = reader.openInput(abstractPath) ?: throw TransferException(TransferErrorCode.UNSAFE_PACKAGE)
        try {
            Files.newOutputStream(stagingFile, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE).use { output ->
                val buffer = ByteArray(TransferLimits.CHUNK_BYTES)
                var total = 0L
                input.use { stream ->
                    while (true) {
                        checkCancelled()
                        val read = stream.read(buffer)
                        if (read < 0) break
                        transferRequire(total <= TransferLimits.PACKAGE_BYTES - read, TransferErrorCode.LIMIT_EXCEEDED)
                        total += read
                        output.write(buffer, 0, read)
                    }
                }
            }
        } catch (error: TransferException) {
            Files.deleteIfExists(stagingFile)
            throw error
        } catch (error: IOException) {
            Files.deleteIfExists(stagingFile)
            throw TransferException(TransferErrorCode.UNSAFE_PACKAGE, cause = error)
        }
        return stagingFile
    }

    fun materializeDirectory(
        reader: ModSourceReader,
        abstractPath: String,
        stagingDir: Path,
        checkCancelled: () -> Unit = {},
    ) {
        checkCancelled()
        Files.createDirectories(stagingDir)
        val total = LongArray(1)
        var count = 0
        fun copyDir(srcAbstract: String, dest: Path) {
            checkCancelled()
            val entries = reader.listDir(srcAbstract) ?: throw TransferException(TransferErrorCode.UNSAFE_PACKAGE)
            for (name in entries) {
                checkCancelled()
                transferRequire(
                    name.isNotEmpty() && '/' !in name && '\\' !in name && name != "." && name != "..",
                    TransferErrorCode.UNSAFE_PACKAGE
                )
                val childAbstract = "$srcAbstract/$name"
                val childDest = dest.resolve(name)
                if (reader.isDirectoryNonZip(childAbstract)) {
                    Files.createDirectories(childDest)
                    copyDir(childAbstract, childDest)
                } else {
                    val input = reader.openInput(childAbstract)
                        ?: throw TransferException(TransferErrorCode.UNSAFE_PACKAGE)
                    try {
                        Files.newOutputStream(childDest, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)
                            .use { output ->
                                val buffer = ByteArray(TransferLimits.CHUNK_BYTES)
                                input.use { stream ->
                                    while (true) {
                                        checkCancelled()
                                        val read = stream.read(buffer)
                                        if (read < 0) break
                                        transferRequire(
                                            total[0] <= TransferLimits.UNPACKED_BYTES - read,
                                            TransferErrorCode.LIMIT_EXCEEDED
                                        )
                                        total[0] += read
                                        output.write(buffer, 0, read)
                                    }
                                }
                            }
                    } catch (error: TransferException) {
                        Files.deleteIfExists(childDest)
                        throw error
                    } catch (error: IOException) {
                        Files.deleteIfExists(childDest)
                        throw TransferException(TransferErrorCode.UNSAFE_PACKAGE, cause = error)
                    }
                    transferRequire(++count <= TransferLimits.PACKAGE_FILES, TransferErrorCode.LIMIT_EXCEEDED)
                }
            }
        }
        try {
            copyDir(abstractPath, stagingDir)
        } catch (error: Throwable) {
            deleteRecursively(stagingDir)
            throw error
        }
    }

    fun deleteRecursively(path: Path) {
        runCatching {
            if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) return
            Files.walk(path).use { walk ->
                walk.sorted(Comparator.reverseOrder()).forEach { Files.deleteIfExists(it) }
            }
        }
    }
}
