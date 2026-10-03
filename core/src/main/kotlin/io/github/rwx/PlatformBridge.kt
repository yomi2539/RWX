package io.github.rwx

import org.apache.http.client.HttpClient
import org.apache.http.impl.client.DefaultHttpClient
import java.io.File
import java.io.InputStream

interface PlatformBridge {
    val storage: PlatformStorage
    val preferenceStorage: PreferenceStorage
    val appMetadata: AppMetadata
    val logger: AppLogger
    val crashReporter: CrashReporter
    val isMobilePlatform: Boolean
    val displayDensity: Float
        get() = 1f
    val audio: PlatformAudio
        get() = NoopPlatformAudio
    var filePickerHost: PlatformFilePickerHost?

    /**
     * Create an isolated ClassLoader for a mod JAR file.
     * Desktop: URLClassLoader over JVM .class files.
     * Android: DexClassLoader over classes.dex inside the JAR.
     */
    fun createModClassLoader(modFile: File, parent: ClassLoader): ClassLoader

    /**
     * Drop cached classloader artifacts (extracted dex / optimized odex) that do not belong
     * to any currently loaded mod. No-op by default; Android prunes its private caches.
     */
    fun cleanupModClassLoaderCache(activeModFiles: Collection<File>) = Unit

    @Suppress("DEPRECATION")
    fun createMasterServerHttpClient(): HttpClient = DefaultHttpClient()

    fun closeMasterServerHttpClient(httpClient: HttpClient) = Unit

    fun openUrl(url: String): Boolean = false
}

interface PlatformAudio {
    fun registerSound(id: String, file: File)
    fun registerSound(id: String, data: ByteArray, fileName: String)
    fun registerBuiltInSound(id: String, name: String): Boolean
    fun unregisterSound(id: String)
    fun playSound(id: String, volume: Float, pan: Float = 0f, pitch: Float = 1f)
}

object NoopPlatformAudio : PlatformAudio {
    override fun registerSound(id: String, file: File) = Unit
    override fun registerSound(id: String, data: ByteArray, fileName: String) = Unit
    override fun registerBuiltInSound(id: String, name: String): Boolean = false
    override fun unregisterSound(id: String) = Unit
    override fun playSound(id: String, volume: Float, pan: Float, pitch: Float) = Unit
}

data class AppMetadata(
    val packageName: String = DEFAULT_PACKAGE_NAME,
    val installerPackageName: String = DEFAULT_INSTALLER_PACKAGE_NAME,
    val versionName: String = VERSION_NAME,
    val versionCode: Int = VERSION_CODE,
    val compatibleCoreVersionCode: Int = COMPATIBLE_CORE_VERSION_CODE,
    val signature: String? = null,
) {
    companion object {
        const val DEFAULT_PACKAGE_NAME = "com.corrodinggames.rts.gdx"
        const val DEFAULT_INSTALLER_PACKAGE_NAME = "java-gdx"
        const val VERSION_NAME = "1.1.2"
        val VERSION_CODE = VERSION_NAME.split('.', '-', '+')
            .take(3)
            .map { it.toIntOrNull() ?: 0 }
            .let { parts ->
                (parts[0] * 10_000) +
                        (parts[1] * 100) +
                        parts[2]
            }
        const val COMPATIBLE_CORE_VERSION_CODE = 176
    }
}
interface PlatformStorage {

    val rootDir: StorageLocation
    val localDir: StorageLocation
    val cacheDir: StorageLocation
    val modsDir: StorageLocation
    val unitsDir: StorageLocation
    val mapsDir: StorageLocation
    val savesDir: StorageLocation
    val replaysDir: StorageLocation

    fun location(kind: StorageKind): StorageLocation
    fun location(virtualPath: String, file: File): StorageLocation {
        return StorageLocation(virtualPath, file.absoluteFile)
    }

    fun resolveVirtualPath(virtualPath: String): File? {
        val normalized = virtualPath.trimAssetPath().replace(mergedPathTagRegex, "")
        val resolved = listOf(
            cacheDir,
            mapsDir,
            unitsDir,
            savesDir,
            replaysDir,
            modsDir,
            localDir,
            rootDir,
        ).firstNotNullOfOrNull { location ->
            location.resolveLocationPath(normalized)
        }
        return resolved ?: when {
            normalized.startsWith("/SD/") -> File(rootDir.file, normalized.substring("/SD/".length))
            else -> null
        }
    }

    fun listAssets(prefix: String): List<String>
    fun assetFileExists(path: String): Boolean
    fun readAssetBytes(path: String): ByteArray? = null
    fun openAssetStream(path: String): InputStream? = readAssetBytes(path)?.inputStream()
    fun createDirectories()
}

fun String.trimAssetPath(): String = trim().replace('\\', '/').trimEnd('/')

private val mergedPathTagRegex = Regex("""\[(?:INTERNAL|EXTERNAL|NULL)-PATH]/""")

enum class StorageKind {
    ROOT,
    LOCAL,
    CACHE,
    MODS,
    UNITS,
    MAPS,
    SAVES,
    REPLAYS,
}

data class StorageLocation(
    val virtualPath: String,
    val file: File,
) {
    fun resolve(relativePath: String): File = File(file, relativePath)

    fun resolveLocationPath(path: String): File? {
        val normalizedRoot = virtualPath.trimAssetPath()
        val normalizedPath = path.trimAssetPath()
        if (normalizedPath == normalizedRoot) {
            return file
        }
        val childPrefix = "$normalizedRoot/"
        if (!normalizedPath.startsWith(childPrefix)) {
            return null
        }
        return resolve(normalizedPath.substring(childPrefix.length))
    }
}

interface AppLogger {
    fun debug(tag: String?, message: String)
    fun info(tag: String?, message: String)
    fun warn(tag: String?, message: String, throwable: Throwable? = null)
    fun error(tag: String?, message: String, throwable: Throwable? = null)
}

interface CrashReporter {
    fun recordException(throwable: Throwable)
    fun log(message: String)
    fun setUserId(userId: String)
    fun setCustomKey(key: String, value: String)
}
