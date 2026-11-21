package io.github.rwx.mod

import io.github.rwx.PlatformBridge
import io.github.rwx.logger
import io.github.rwx.mod.impl.ApiImpl
import io.github.rwx.mod.registry.ModRegistry
import io.github.rwx.mod.registry.apiImplOrNull
import net.peanuuutz.tomlkt.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile

class JvmModLoader @JvmOverloads constructor(
    private val platformClassLoader: ClassLoader = JvmModLoader::class.java.classLoader,
    private val createClassLoader: ((File, ClassLoader) -> ClassLoader)? = null,
) : AutoCloseable, KoinComponent {
    val errors: MutableList<Throwable> = mutableListOf()
    private val platformBridge: PlatformBridge by inject()
    private val discoveredMods = mutableListOf<JvmMod>()
    val loadedMods = mutableListOf<Mod>()

    fun discoverModFiles(files: Iterable<File>): List<JvmMod> {
        val currentBatch = mutableListOf<JvmMod>()
        val candidates = files.toList()
        candidates.forEach { file ->
            runCatching {
                discoverModFile(file)?.let { mod ->
                    currentBatch += mod
                }
            }.onFailure { e ->
                errors += e
                logger.error(e) { "Failed to load JVM mod from ${file.path}" }
            }
        }
        runCatching { platformBridge.cleanupModClassLoaderCache(candidates) }
            .onFailure { e -> logger.error(e) { "Failed to prune JVM mod classloader cache" } }
        return resolveDependencies(currentBatch).filterIsInstance<JvmMod>()
    }

    private fun discoverModFile(jarFile: File): JvmMod? {
        val result = readModToml(jarFile) ?: run {
            logger.warn { "Failed to load JVM mod from ${jarFile.path}" }
            return null
        }
        val metadata = result.manifest
        val entryClassName = result.entryClassName

        val api = ApiImpl.create(metadata, jarFile, platformBridge)
        val classLoader = runCatching {
            createClassLoader?.invoke(jarFile, platformClassLoader)
                ?: platformBridge
                    .createModClassLoader(jarFile, platformClassLoader)
        }.onFailure { e ->
            api.close()
            throw e
        }.getOrThrow()

        val mod = instantiateMod(entryClassName, classLoader, jarFile.name) ?: run {
            api.close()
            if (classLoader is Closeable) runCatching(classLoader::close)
            return null
        }
        try {
            mod.apply {
                this.classLoader = classLoader
                this.manifest = metadata
                this.api = api
            }
        } catch (error: Throwable) {
            api.close()
            if (classLoader is Closeable) runCatching(classLoader::close)
            throw error
        }

        discoveredMods += mod
        return mod
    }

    fun initializeMod(mod: JvmMod) {
        if (mod in loadedMods) return
        ModRegistry.register(mod)
        try {
            mod.init()
            loadedMods += mod
        } catch (error: Throwable) {
            ModRegistry.unregister(mod)
            runCatching { mod.dispose() }
            throw error
        }
    }


    data class ModTomlResult(val manifest: ModManifest, val entryClassName: String)

    private fun instantiateMod(
        className: String,
        classLoader: ClassLoader,
        jarName: String
    ): JvmMod? {
        return runCatching {
            val clazz = classLoader.loadClass(className)
            val ctor = clazz.getDeclaredConstructor()
            ctor.newInstance() as JvmMod
        }.onFailure { e ->
            when (e) {
                is ClassNotFoundException ->
                    logger.error(e) { "Entrypoint class '$className' not found in $jarName" }

                is NoSuchMethodException ->
                    logger.error(e) { "Entrypoint '$className' in $jarName must have a public no-arg constructor" }

                is ClassCastException ->
                    logger.error(e) { "Entrypoint '$className' in $jarName does not implement JvmModEntryPoint" }

                else ->
                    logger.error(e) { "Failed to instantiate entrypoint '$className' in $jarName" }
            }
        }.getOrNull()
    }


    private fun resolveDependencies(mods: List<Mod>): List<Mod> {
        val modMap = mods.associateBy { it.manifest.id }
        val adjacency = mutableMapOf<String, MutableSet<String>>()
        val inDegree = mutableMapOf<String, Int>().withDefault { 0 }
        for (mod in mods) {
            for (depId in (mod.manifest as JvmModManifest).dependencies) {
                if (depId !in modMap) {
                    logger.warn { "Mod ${mod.manifest.id} depends on missing mod $depId; skipping this dependency" }
                    continue
                }
                adjacency.getOrPut(depId) { mutableSetOf() }.add(mod.manifest.id)
                inDegree[mod.manifest.id] = inDegree.getValue(mod.manifest.id) + 1
            }
        }

        val queue = mutableListOf<Mod>()
        for (mod in mods) {
            if (inDegree.getValue(mod.manifest.id) == 0) queue.add(mod)
        }

        val sorted = mutableListOf<Mod>()
        while (queue.isNotEmpty()) {
            queue.sortByDescending { (it.manifest as JvmModManifest).priority }
            val current = queue.removeAt(0)
            sorted.add(current)
            adjacency[current.manifest.id]?.forEach { dependentId ->
                val newDegree = inDegree.getValue(dependentId) - 1
                inDegree[dependentId] = newDegree
                if (newDegree == 0) modMap[dependentId]?.let { queue.add(it) }
            }
        }

        if (sorted.size != mods.size) {
            val remaining = mods.filter { it.manifest.id !in sorted.map { m -> m.manifest.id } }
            logger.warn { "Cyclic or missing dependencies detected; skipping mods: ${remaining.joinToString { it.manifest.id }}" }
        }

        return sorted
    }

    fun disposeMod(mod: JvmMod) {
        if (loadedMods.remove(mod)) {
            runCatching { mod.dispose() }.onFailure { e ->
                logger.error(e) { "Failed to dispose mod ${mod.manifest.id}" }
            }
            ModRegistry.unregister(mod)
        }
        discoveredMods.remove(mod)
        runCatching {
            mod.apiImplOrNull()?.close()
        }.onFailure { e ->
            logger.error(e) { "Error closing assets for mod ${mod.manifest.id}" }
        }
        runCatching {
            val cl = mod.classLoader
            if (cl is Closeable) cl.close()
        }.onFailure { e ->
            logger.error(e) { "Error closing classloader for mod ${mod.manifest.id}" }
        }
    }

    override fun close() {
        loadedMods.forEach { mod ->
            runCatching { mod.dispose() }.onFailure { e ->
                logger.error(e) { "Failed to dispose mod ${mod.manifest.id}" }
            }
            ModRegistry.unregister(mod)
        }
        discoveredMods.distinct().forEach { mod ->
            runCatching {
                mod.apiImplOrNull()?.close()
            }.onFailure { e ->
                logger.error(e) { "Error closing assets for mod ${mod.manifest.id}" }
            }
            runCatching {
                val cl = mod.classLoader
                if (cl is Closeable) cl.close()
            }.onFailure { e ->
                logger.error(e) { "Error closing classloader for mod ${mod.manifest.id}" }
            }
        }
        loadedMods.clear()
        discoveredMods.clear()
    }

    companion object {
        const val JVM_MOD_MANIFEST = "mod.toml"
        private val toml = Toml { ignoreUnknownKeys = true }

        private fun readModToml(jarFile: File): ModTomlResult? =
            runCatching {
                ZipFile(jarFile).use { zip ->
                    val entry = zip.getEntry(JVM_MOD_MANIFEST) ?: return null
                    peekModToml(zip.getInputStream(entry))
                }
            }.onFailure { e ->
                logger.error(e) { "Error reading mod.toml from ${jarFile.name}" }
            }.getOrNull()

        fun peekModToml(input: InputStream): ModTomlResult? =
            runCatching {
                val reader = input.reader(Charsets.UTF_8)
                val table = reader.use { reader ->
                    toml.parseToTomlTable(reader).asTomlTable()
                }
                ModTomlResult(
                    entryClassName = table.getStringOrNull("entrypoint")?.takeIf { it.isNotBlank() } ?: return null,
                    manifest = JvmModManifest(
                        id = table.getStringOrNull("id")?.takeIf { it.isNotBlank() } ?: run {
                            logger.error { "Failed to essential id from $JVM_MOD_MANIFEST" }
                            return null
                        },
                        name = table.getStringOrNull("name").orEmpty(),
                        author = table.getStringOrNull("author").orEmpty(),
                        version = table.getStringOrNull("version").orEmpty(),
                        minGameVersionName = table.getStringOrNull("minGameVersion").orEmpty(),
                        description = table.getStringOrNull("description").orEmpty(),
                        thumbnail = table.getStringOrNull("thumbnail").orEmpty(),
                        dependencies = table.getArrayOrNull("dependencies")?.let { array ->
                            List(array.size) { array.getStringOrNull(it) }
                                .filterNotNull()
                                .filter { it.isNotBlank() }
                        } ?: emptyList(),
                        priority = table.getIntegerOrNull("priority")?.toInt() ?: 0,
                    )
                )
            }.getOrThrow()
    }
}
