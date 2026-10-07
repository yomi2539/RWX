package io.github.rwx.p2p.transfer

import com.corrodinggames.rts.game.units.custom.CustomUnitConfig
import com.corrodinggames.rts.gameFramework.GameEngine
import com.corrodinggames.rts.gameFramework.file.FileHelper
import kotlinx.serialization.Serializable
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

object TransferRevision {
    private val value = AtomicLong()
    val current: Long get() = value.get()
    @JvmStatic fun changed() { value.incrementAndGet() }
}

object TransferOperation {
    private val held = AtomicBoolean()
    val active: Boolean get() = held.get()
    fun acquire(): Boolean = held.compareAndSet(false, true)
    fun release() { held.set(false) }
}

@Serializable
data class ModSelectionSnapshot(
    val settings: String,
    val settingsVersion: Int,
    val lastModCount: Int,
    val disabled: Map<String, Boolean>,
)

data class LoadedModSource(
    val uuid: String,
    val title: String,
    val path: String,
    val units: List<RequiredUnit>,
    val abstractPath: String = path
)
data class ModEngineSnapshot(
    val revision: Long,
    val gameVersion: Int,
    val usesMods: Boolean,
    val hasJvmMods: Boolean,
    val hasLoadErrors: Boolean,
    val sources: List<LoadedModSource>,
    val units: List<RequiredUnit>,
    val activeUnits: List<RequiredUnit>,
    val selection: ModSelectionSnapshot,
)

object ModTransferEngine {
    fun capture(engine: GameEngine): ModEngineSnapshot {
        val manager = engine.modManager
        val active = manager.mods.filter { it.isEnabled }
        val jvmIds = manager.jvmMods.mapNotNull { it.manifest?.id }.toSet()
        val configs = synchronized(CustomUnitConfig.allConfigs) {
            CustomUnitConfig.allConfigs.filter { it.modInfo == null || it.modInfo.isEnabled }
        }
        val sources = active.filter { !it.isBuiltIn && !it.isCoreMod && it.id !in jvmIds }.map { mod ->
            transferRequire(mod.firstError == null, TransferErrorCode.LOAD_FAILED)
            val rawPath = mod.path ?: mod.sourceFolder
            LoadedModSource(mod.uuid, mod.displayTitle,
                FileHelper.convertAbstractPath(rawPath),
                configs.filter { it.modInfo === mod }.map { RequiredUnit(it.name, it.configHash) },
                abstractPath = rawPath
            )
        }
        return ModEngineSnapshot(TransferRevision.current, engine.getVersionCode(true),
            engine.networkEngine?.requireActiveMods == true, active.any { it.id in jvmIds },
            manager.mods.any { !it.disabled && it.firstError != null }, sources,
            configs.map { RequiredUnit(it.name, it.configHash) },
            CustomUnitConfig.activeConfigs.map { RequiredUnit(it.name, it.configHash) },
            ModSelectionSnapshot(engine.settingsEngine.modSettings, engine.settingsEngine.modSettingsVersion,
                engine.settingsEngine.lastModCount, manager.mods.associate { it.uuid to it.disabled }))
    }

    fun select(engine: GameEngine, existingIds: Set<String>, installedNames: Set<String>): Boolean {
        engine.modManager.loadAllMods()
        for (mod in engine.modManager.mods) {
            if (!mod.isBuiltIn && !mod.isCoreMod) {
                mod.disabled = mod.uuid !in existingIds && mod.dirName !in installedNames
            }
        }
        engine.modManager.saveModSelection()
        engine.settingsEngine.save()
        return installedNames.all { name -> engine.modManager.mods.any { it.dirName == name && !it.disabled } }
    }

    fun restoreSelection(engine: GameEngine, snapshot: ModSelectionSnapshot) {
        engine.modManager.mods.forEach { mod -> mod.disabled = snapshot.disabled[mod.uuid] ?: true }
        engine.settingsEngine.modSettings = snapshot.settings
        engine.settingsEngine.modSettingsVersion = snapshot.settingsVersion
        engine.settingsEngine.lastModCount = snapshot.lastModCount
        engine.settingsEngine.save()
    }

    fun removeInstalled(engine: GameEngine, names: Set<String>, snapshot: ModSelectionSnapshot) {
        engine.modManager.mods.filter { it.dirName in names }.forEach(engine.modManager::removeMod)
        restoreSelection(engine, snapshot)
    }
}
