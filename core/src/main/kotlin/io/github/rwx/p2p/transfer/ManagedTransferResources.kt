package io.github.rwx.p2p.transfer

import com.corrodinggames.rts.gameFramework.file.FileHelper
import com.corrodinggames.rts.gameFramework.mod.ModInfo
import java.io.File
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

object ManagedTransferResources {
    private val archives = ConcurrentHashMap.newKeySet<Path>()
    fun register(path: Path) { archives.add(path.toAbsolutePath().normalize()) }
    fun unregister(path: Path) { archives.remove(path.toAbsolutePath().normalize()) }
    private fun normalized(path: String): Path = File(FileHelper.convertAbstractPath(path)).toPath().toAbsolutePath().normalize()

    @JvmStatic
    fun isManaged(mod: ModInfo): Boolean = mod.dirName?.endsWith(".rwmod") == true &&
        mod.path?.let { normalized(it) in archives } == true

    @JvmStatic
    fun allows(mod: ModInfo, target: String): Boolean {
        val root = normalized(mod.path)
        val resolved = normalized(target)
        return resolved.startsWith(root) || resolved.startsWith(normalized("units"))
    }

    @JvmStatic
    fun limit(archive: String, expectedSize: Long, input: InputStream): InputStream {
        if (!archive.substringAfterLast('/').substringAfterLast('\\').endsWith(".rwmod") || normalized(archive) !in archives) return input
        if (expectedSize !in 0..TransferLimits.UNPACKED_BYTES) {
            input.close()
            throw IOException("Invalid transfer resource size")
        }
        return object : FilterInputStream(input) {
            private var remaining = expectedSize
            private fun consumed(count: Long) {
                if (count > remaining) throw IOException("Transfer resource exceeds declared size")
                remaining -= count
            }
            override fun read(): Int {
                val value = `in`.read()
                if (value >= 0) consumed(1)
                return value
            }
            override fun read(bytes: ByteArray, offset: Int, length: Int): Int {
                val count = `in`.read(bytes, offset, minOf(length.toLong(), remaining + 1).toInt())
                if (count > 0) consumed(count.toLong())
                return count
            }
            override fun skip(count: Long): Long {
                val skipped = `in`.skip(minOf(count, remaining + 1))
                consumed(skipped)
                return skipped
            }
            override fun available(): Int = minOf(`in`.available().toLong(), remaining).toInt()
            override fun markSupported(): Boolean = false
            override fun reset() = throw IOException("Transfer resource stream cannot reset")
        }
    }
}
