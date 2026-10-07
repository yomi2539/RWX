package io.github.rwx.p2p.transfer

import com.corrodinggames.rts.gameFramework.GameEngine
import io.github.rwx.PlatformStorage
import io.github.rwx.logger
import io.github.rwx.session.GameSession
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.koin.mp.KoinPlatform.getKoin
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@Serializable
private data class InstalledArtifact(val name: String, val cacheName: String, val artifact: TransferArtifact,
                                     val moved: Boolean, val modified: Long = 0)
@Serializable
private data class InstallJournal(val version: Int, val selection: ModSelectionSnapshot, val installed: List<InstalledArtifact>)

internal class InstallTransaction(
    private val storage: PlatformStorage,
    private val session: GameSession,
    private val cache: ArtifactCache,
    selection: ModSelectionSnapshot,
) {
    private val units = storage.unitsDir.file.toPath()
    private val journalPath = journalPath(storage)
    private var journal = InstallJournal(1, selection, emptyList())
    private var applied = false

    init {
        transferRequire(!Files.exists(journalPath), TransferErrorCode.RECOVERY_REQUIRED)
        Files.createDirectories(units)
        Files.createDirectories(journalPath.parent)
        save()
    }

    fun install(artifact: TransferArtifact, source: Path): String {
        transferRequire(source.parent == cache.root && Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS))
        val stem = sanitizedBasename(artifact.displayName, artifact.artifactSha256)
        var name = "$stem-${artifact.artifactSha256}.rwmod"
        var counter = 2
        while (Files.exists(units.resolve(name))) {
            name = "$stem-${artifact.artifactSha256}-$counter.rwmod"
            counter++
        }
        val target = units.resolve(name)
        var entry = InstalledArtifact(name, source.fileName.toString(), artifact, moved = true)
        journal = journal.copy(installed = journal.installed + entry)
        save()
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE)
        } catch (_: AtomicMoveNotSupportedException) {
            entry = entry.copy(moved = false)
            journal = journal.copy(installed = journal.installed.dropLast(1) + entry)
            save()
            val staging = storage.rootDir.file.toPath().resolve(".transfer-staging")
            Files.createDirectories(staging)
            val temporary = staging.resolve(name)
            try {
                Files.copy(source, temporary)
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE)
            } finally {
                Files.deleteIfExists(temporary)
            }
        }
        entry = entry.copy(modified = Files.getLastModifiedTime(target).toMillis())
        journal = journal.copy(installed = journal.installed.dropLast(1) + entry)
        save()
        ManagedTransferResources.register(target)
        return name
    }

    suspend fun apply(existingIds: Set<String>, manifest: TransferManifest) {
        applied = true
        transferRequire(session.selectTransferArtifacts(existingIds, journal.installed.map { it.name }.toSet()), TransferErrorCode.LOAD_FAILED)
        withContext(NonCancellable) {
            transferRequire(session.requestReloadTransfer(), TransferErrorCode.LOAD_FAILED)
        }
        val state = session.captureTransferState() ?: throw TransferException(TransferErrorCode.LOAD_FAILED)
        val present = state.units.map { it.name to it.configHash }.toSet()
        val mismatched = manifest.requiredUnits.filter { (it.name to it.configHash) !in present }
        if (mismatched.isNotEmpty() || state.hasLoadErrors) {
            val matched = manifest.requiredUnits.size - mismatched.size
            throw TransferException(TransferErrorCode.LOAD_FAILED,
                "units $matched/${manifest.requiredUnits.size} matched" +
                    (if (state.hasLoadErrors) ", local load errors" else "") +
                    (if (mismatched.isNotEmpty()) ", first mismatches: ${mismatched.take(3).joinToString { it.name }}" else ""))
        }
    }

    suspend fun restore() = withContext(NonCancellable) {
        if (applied) {
            transferRequire(session.restoreTransfer(journal.selection), TransferErrorCode.RECOVERY_REQUIRED)
            transferRequire(session.requestReloadTransfer(), TransferErrorCode.RECOVERY_REQUIRED)
        }
        restoreFiles(storage, journal, cache)
        transferRequire(session.removeTransferEntries(journal.installed.map { it.name }.toSet(), journal.selection),
            TransferErrorCode.RECOVERY_REQUIRED)
        Files.deleteIfExists(journalPath)
    }

    private fun save() = TransferFiles.atomicWrite(journalPath, transferJson.encodeToString(InstallJournal.serializer(), journal).utf8())

    companion object {
        internal fun journalPath(storage: PlatformStorage): Path = storage.localDir.file.toPath().resolve("transfer-state/session.json")
        private val installedName = Regex("[\\p{L}\\p{N}_ ][\\p{L}\\p{N}._ \\-]{0,40}-[0-9a-f]{64}(?:-\\d+)?\\.rwmod")
        private val cacheName = Regex("[0-9a-f-]+\\.(part|rwmod)")

        internal fun sanitizedBasename(displayName: String, sha256: String): String {
            var stem = displayName.substringAfterLast('/').substringAfterLast('\\')
            stem = stem.replace(Regex("\\s+"), " ")
            stem = stem.filter { it.code >= 0x20 && it.code != 0x7F }.trim().trimStart('.')
            val mapped = StringBuilder(stem.length)
            for (c in stem) {
                mapped.append(if (c.isLetterOrDigit() || c == '.' || c == '-' || c == '_' || c == ' ') c else '_')
            }
            stem = mapped.toString().replace(Regex("_+"), "_").trimStart('.').trimEnd('.', ' ', '-', '_').trimStart('.')
            if (stem.isEmpty()) stem = "mod"
            val suffixLen = 1 + sha256.length + ".rwmod".length
            val maxStem = (100 - suffixLen).coerceAtLeast(3)
            if (stem.length > maxStem) {
                stem = stem.substring(0, maxStem).trimEnd('.', ' ', '-', '_')
            }
            return stem.ifEmpty { "mod" }
        }

        @JvmStatic
        fun recoverBeforeScan(engine: GameEngine) {
            val storage = getKoin().get<PlatformStorage>()
            val path = journalPath(storage)
            if (!Files.exists(path)) return
            transferRequire(Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && Files.size(path) <= 2 * 1024 * 1024,
                TransferErrorCode.RECOVERY_REQUIRED)
            val journal = try {
                transferJson.decodeFromString(InstallJournal.serializer(), Files.readAllBytes(path).toString(Charsets.UTF_8))
            } catch (error: Exception) {
                throw TransferException(TransferErrorCode.RECOVERY_REQUIRED, cause = error)
            }
            transferRequire(journal.version == 1 && journal.installed.size <= TransferLimits.ARTIFACTS, TransferErrorCode.RECOVERY_REQUIRED)
            val cache = ArtifactCache(storage.cacheDir.file.toPath().resolve("transfer"))
            restoreFiles(storage, journal, cache)
            engine.settingsEngine.modSettings = journal.selection.settings
            engine.settingsEngine.modSettingsVersion = journal.selection.settingsVersion
            engine.settingsEngine.lastModCount = journal.selection.lastModCount
            engine.settingsEngine.save()
            Files.delete(path)
        }

        private fun restoreFiles(storage: PlatformStorage, journal: InstallJournal, cache: ArtifactCache) {
            com.corrodinggames.rts.gameFramework.utility.RwmodFileLoader.closeTransferArchives(
                journal.installed.map { storage.unitsDir.resolve(it.name).path })
            for (entry in journal.installed) {
                transferRequire(installedName.matches(entry.name) &&
                    cacheName.matches(entry.cacheName), TransferErrorCode.RECOVERY_REQUIRED)
                val target = storage.unitsDir.file.toPath().resolve(entry.name)
                if (!Files.exists(target)) continue
                transferRequire(!Files.isSymbolicLink(target) && Files.size(target) == entry.artifact.size &&
                    (entry.modified == 0L || Files.getLastModifiedTime(target).toMillis() == entry.modified), TransferErrorCode.RECOVERY_REQUIRED)
                if (entry.moved) {
                    val source = cache.root.resolve(entry.cacheName)
                    transferRequire(!Files.exists(source), TransferErrorCode.RECOVERY_REQUIRED)
                    Files.move(target, source)
                    cache.remember(source, entry.artifact)
                } else {
                    Files.delete(target)
                }
                ManagedTransferResources.unregister(target)
            }
        }
    }
}

object TransferRecovery {

    @JvmStatic fun beforeInitialScanOrReset(engine: GameEngine) {
        try {
            InstallTransaction.recoverBeforeScan(engine)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            logger.error(error){"Discarding corrupt transfer journal"}
            val storage = getKoin().get<PlatformStorage>()
            runCatching { Files.deleteIfExists(InstallTransaction.journalPath(storage)) }
        }
    }
}
