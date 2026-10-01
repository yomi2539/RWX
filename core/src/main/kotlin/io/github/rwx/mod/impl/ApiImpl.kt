package io.github.rwx.mod.impl

import com.corrodinggames.rts.game.PlayerTeam
import com.corrodinggames.rts.game.units.UnitTypeEnum
import com.corrodinggames.rts.game.units.custom.AnimationTag
import com.corrodinggames.rts.game.units.custom.CustomProjectileTemplate
import com.corrodinggames.rts.game.units.custom.CustomUnitConfig
import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope
import com.corrodinggames.rts.gameFramework.GameEngine
import com.corrodinggames.rts.gameFramework.file.FileHelper
import com.corrodinggames.rts.gameFramework.graphics.ShaderProgram
import com.corrodinggames.rts.gameFramework.graphics.ShaderUniformValueType
import com.corrodinggames.rts.gameFramework.graphics.Texture
import com.corrodinggames.rts.gameFramework.mod.ModInfo
import androidx.compose.runtime.Composable
import io.github.rwx.PlatformBridge
import io.github.rwx.logger
import io.github.rwx.mod.*
import io.github.rwx.mod.api.*
import io.github.rwx.mod.asset.JvmModAssetBridge
import io.github.rwx.mod.asset.JvmModAssetKeyStore
import io.github.rwx.mod.asset.JvmModAssetStore
import io.github.rwx.mod.asset.OnlineAssetRevocationListFetcher
import io.github.rwx.mod.assets.AssetVaultFormat
import io.github.rwx.mod.assets.EmptyAssetCredentialResolver
import io.github.rwx.mod.registry.*
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset
import java.util.Locale
import java.util.zip.ZipFile
import kotlin.collections.ArrayDeque
import io.github.rwx.mod.api.Map as ApiMap

class ApiImpl private constructor(
    val manifest: ModManifest,
    private val jarFile: File,
    private val workDir: File,
    internal val platformBridge: PlatformBridge?,
    private val assetStore: JvmModAssetStore,
) : Api, AutoCloseable {
    private val unitApi = UnitApiImpl()
    private val unitWorldApi = UnitWorldImpl()
    private val commandApi = UnitCommandsImpl(this)
    private val activeNativeUnits = linkedMapOf<UnitId, CustomUnitConfig>()
    private val shaderDefinitions = linkedMapOf<String, ShaderDefinition>()
    private val shaderPrograms = linkedMapOf<String, ShaderProgram>()
    private val unitExtensionBindings = linkedMapOf<UnitId, UnitExtensionBindings>()
    private var engineModInfo: ModInfo? = null

    override val game: Game = GameImpl(this)
    override val assets: Asset = AssetImpl(workDir, assetStore)
    override val units: Units = unitApi
    override val unitWorld: UnitWorld = unitWorldApi
    override val commands: UnitCommands = commandApi
    override val maps: ApiMap = MapApi(this)
    override val rules: Rule = RecordingRule()
    override val localization: io.github.rwx.mod.api.Localization = Localization()
    override val audio: Audio = ModAudio(this)
    override val graphics: Graphics = RecordingGraphics(this)
    override val ui: Ui = RecordingUi(this)
    override val ai: io.github.rwx.mod.api.AiBehavior = AiBehavior(commandApi, unitWorldApi)

    private val shaderBindings = linkedMapOf<String, String?>()

    fun materializeDeclarations() {
        checkNotNull(engineModInfo) { "JVM mod ${manifest.id} must be registered with ModManager before loading content" }
        extractJarResources()
        AudioRegistry.materialize(this)
        applyUnitDeclarations()
    }

    fun verifyUnitDeclarationsActivated() {
        val ownerModInfo = checkNotNull(engineModInfo) {
            "JVM mod ${manifest.id} is not registered with ModManager"
        }
        val registeredTypes = UnitTypeEnum.ae.toSet()
        val activeByName = CustomUnitConfig.activeConfigs.associateBy { it.name }
        val resolved = linkedMapOf<UnitId, CustomUnitConfig>()
        val missing = mutableListOf<String>()
        var resolvedBuildLinks = 0
        var verifiedStartingUnits = 0

        unitApi.units().forEach { definition ->
            val engineName = UnitIniCompiler.engineName(definition.id)
            val nativeType = activeByName[engineName]
            if (nativeType == null || nativeType !in registeredTypes) {
                missing += "${definition.id.value} ($engineName)"
            } else {
                check(nativeType.modInfo === ownerModInfo) {
                    "JVM mod ${manifest.id}: native unit ${definition.id.value} is not owned by its engine mod"
                }
                val probe = nativeType.createCustomUnit(true)
                check(probe.r() === nativeType) {
                    "JVM mod ${manifest.id}: native unit factory returned the wrong type for ${definition.id.value}"
                }
                val nativeBuildTargets = (1..3)
                    .flatMap { techLevel -> nativeType.a(techLevel) }
                    .mapNotNull { action -> action.unitType?.getUnitTypeDescriptionShort() }
                    .toSet()
                val expectedBuildTargets = definition.iniSections.asSequence()
                    .filter { it.name.startsWith("canBuild_") }
                    .flatMap { section ->
                        when (val names = section.values.values["name"]) {
                            is Iterable<*> -> names.asSequence().filterIsInstance<String>()
                            is String -> sequenceOf(names)
                            else -> emptySequence()
                        }
                    }
                    .toSet()
                check(nativeBuildTargets.containsAll(expectedBuildTargets)) {
                    val unresolved = expectedBuildTargets - nativeBuildTargets
                    "JVM mod ${manifest.id}: native build links unresolved for ${definition.id.value}: " +
                            unresolved.joinToString()
                }
                val isPickableStartingUnit = definition.iniSections
                    .firstOrNull { it.name == "core" }
                    ?.values
                    ?.values
                    ?.get("isPickableStartingUnit") == true
                if (isPickableStartingUnit) {
                    check(nativeType.isPickableStartingUnit && nativeType in CustomUnitConfig.configsById) {
                        "JVM mod ${manifest.id}: ${definition.id.value} was not registered as a starting unit"
                    }
                    verifiedStartingUnits++
                }
                resolvedBuildLinks += expectedBuildTargets.size
                resolved[definition.id] = nativeType
            }
        }

        check(missing.isEmpty()) {
            "JVM mod ${manifest.id}: native unit activation failed for ${missing.joinToString()}"
        }
        activeNativeUnits.clear()
        activeNativeUnits.putAll(resolved)
        unitWorldApi.activate(resolved)
        UnitEventRuntime.activate(this, resolved, unitApi.units())
        val appliedExtensionBindings = applyUnitExtensionBindings(resolved)
        val appliedUnitRenderers = RenderRegistry.activateUnitBindings(this, resolved, unitApi.units())
        val appliedShaders = applyUnitShaderBindings(resolved)
        logger.info {
            "JVM mod ${manifest.id}: activated and instantiated ${resolved.size} native unit types " +
                    "with $resolvedBuildLinks resolved build links, $verifiedStartingUnits starting units, " +
                    "$appliedExtensionBindings native-object extension bindings, " +
                    "$appliedUnitRenderers unit renderer bindings, and $appliedShaders unit shader bindings"
        }
    }

    private fun applyUnitDeclarations() {
        val generatedDirectory = workDir.resolve("units/_jvm_generated")
        var generatedCount = 0
        unitExtensionBindings.clear()

        unitApi.units().forEach { definition ->
            runCatching {
                val compilation = UnitIniCompiler.compile(definition)
                generatedDirectory.mkdirs()
                generatedDirectory.resolve(compilation.fileName).writeText(compilation.content)
                unitExtensionBindings[definition.id] = compilation.extensionBindings
                compilation.warnings.forEach { warning ->
                    logger.warn { "JVM mod ${manifest.id}: $warning" }
                    engineModInfo?.addWarning(warning)
                }
                generatedCount++
            }.onFailure { error ->
                engineModInfo?.addError(
                    "Failed to compile unit ${definition.id.value}: ${error.message ?: error.javaClass.simpleName}"
                )
                logger.error(error) {
                    "JVM mod ${manifest.id}: failed to compile unit ${definition.id.value} to native RWX content"
                }
            }
        }
        logger.info { "JVM mod ${manifest.id}: generated $generatedCount native unit definitions for engine mod loading" }
    }

    private fun applyUnitExtensionBindings(nativeUnits: Map<UnitId, CustomUnitConfig>): Int {
        var applied = 0
        unitExtensionBindings.forEach { (unitId, bindings) ->
            val nativeUnit = checkNotNull(nativeUnits[unitId]) {
                "Missing native unit for extension bindings: ${unitId.value}"
            }
            val exposureWidth = bindings.unit.exposureWidth
            val exposureHeight = bindings.unit.exposureHeight
            require((exposureWidth == null) == (exposureHeight == null)) {
                "exposureWidth and exposureHeight must be set together for ${unitId.value}"
            }
            val avoidChance = bindings.unit.damageAvoidChance
            if (avoidChance != null || exposureWidth != null) {
                require(avoidChance == null || avoidChance in 0f..1f) {
                    "damageAvoidChance must be between 0 and 1 for ${unitId.value}"
                }
                DamageRegistry.bindUnit(
                    owner = this,
                    config = nativeUnit,
                    damage = DamageRegistry.UnitDamage(
                        avoidChance = avoidChance ?: 0f,
                        exposure = if (exposureWidth != null && exposureHeight != null) {
                            require(exposureWidth >= 0f && exposureHeight >= 0f) {
                                "exposureWidth and exposureHeight cannot be negative for ${unitId.value}"
                            }
                            DamageRegistry.UnitExposure(
                                offsetX = bindings.unit.exposureOffsetX ?: 0f,
                                offsetY = bindings.unit.exposureOffsetY ?: 0f,
                                width = exposureWidth,
                                height = exposureHeight,
                            )
                        } else {
                            null
                        },
                    ),
                )
            }
            bindings.projectiles.forEach { (name, binding) ->
                val projectile = checkNotNull(nativeUnit.findProjectileTemplateByName(name)) {
                    "Missing projectile $name for extension binding on ${unitId.value}"
                }
                projectile.renderExtensionId = binding.renderer?.rendererId?.value
                projectile.renderExtensionVariant = binding.renderer?.variantId?.value
                projectile.projectileObserverId = binding.observer?.observerId?.value
                projectile.projectileObserverVariant = binding.observer?.variantId?.value
                bindProjectileDamage(projectile, binding, unitId)
                applied++
            }
            bindings.turrets.forEach { (name, binding) ->
                val turret = checkNotNull(nativeUnit.findProjectileConfigByName(name)) {
                    "Missing turret $name for extension binding on ${unitId.value}"
                }
                turret.preFireDuration = binding.preFireDuration.toFloat()
                turret.postFireDuration = binding.postFireDuration.toFloat()
                turret.preFireRendererId = binding.preFireRenderer?.rendererId?.value
                turret.preFireRendererVariant = binding.preFireRenderer?.variantId?.value
                turret.postFireRendererId = binding.postFireRenderer?.rendererId?.value
                turret.postFireRendererVariant = binding.postFireRenderer?.variantId?.value
                turret.turretFireCycleObserverId = binding.observer?.observerId?.value
                turret.turretFireCycleObserverVariant = binding.observer?.variantId?.value
                applied++
            }
            bindings.effects.forEach { (name, binding) ->
                val effect = nativeUnit.resolveEffect("CUSTOM:$name")
                effect.renderExtensionId = binding.renderer.rendererId.value
                effect.renderExtensionVariant = binding.renderer.variantId.value
                applied++
            }
        }
        return applied
    }

    /**
     * Hands a projectile's damage extensions to [DamageRegistry].
     *
     * Area damage that only exists as an extension needs the native area-damage flag raised
     * here: the parser decides that from the INI's own `areaDamage`, which a mod binding a
     * fractional amount never sets, and without it the engine skips the area sweep entirely.
     */
    private fun bindProjectileDamage(
        projectile: CustomProjectileTemplate,
        binding: ProjectileExtensionBinding,
        unitId: UnitId,
    ) {
        val damage = DamageRegistry.ProjectileDamage(
            directDamageAmount = binding.directDamageAmount,
            areaDamageAmount = binding.areaDamageAmount,
            directDamageHitRateBonus = binding.directDamageHitRateBonus,
            areaDamageHitRateBonus = binding.areaDamageHitRateBonus,
            areaDamageExcludeDirectHit = binding.areaDamageExcludeDirectHit ?: false,
            directDamageArmourIgnoreAmount = binding.directDamageArmourIgnoreAmount,
            areaDamageArmourIgnoreAmount = binding.areaDamageArmourIgnoreAmount,
            rayDamage = binding.rayDamage ?: false,
            rayDamageRange = binding.rayDamageRange ?: 0f,
            rayDamageWidth = binding.rayDamageWidth ?: 0f,
            rayDamageTargetWidthFactor = binding.rayDamageTargetWidthFactor ?: 0f,
            rayDamageHitEffectOffsetFactor = binding.rayDamageHitEffectOffsetFactor,
            rayDamageSecondaryTargetTags = binding.rayDamageSecondaryTargetTags
                ?.let { AnimationTag.a(it.joinToString(",")) },
        )
        if (damage == EMPTY_PROJECTILE_DAMAGE) return
        require(!damage.rayDamage || projectile.I) {
            "rayDamage requires an instant projectile for ${unitId.value}"
        }
        runCatching { DamageRegistry.bindProjectile(this, projectile, damage) }
            .getOrElse { error ->
                throw IllegalArgumentException("${error.message} for ${unitId.value}", error)
            }
        val areaAmount = binding.areaDamageAmount
        if (areaAmount != null && areaAmount != 0f && projectile.i > 0) {
            projectile.hasSplashDamage = true
        }
    }

    fun bindEngineModInfo(modInfo: ModInfo) {
        engineModInfo = modInfo
        modInfo.uuid = manifest.id
        modInfo.dirName = jarFile.name
        modInfo.shortName = manifest.id
        modInfo.title = manifest.name
        modInfo.name = manifest.name
        modInfo.description = manifest.description
        modInfo.minVersion = manifest.minGameVersionName
        modInfo.path = jarFile.absolutePath
        modInfo.sourceFolder = workDir.absolutePath
        modInfo.found = true
        modInfo.storageDescription = "JVM Mod: ${jarFile.name}"
        modInfo.dataRefreshed = true
        JvmModAssetBridge.mount(modInfo, workDir, assetStore)
    }

    private fun extractJarResources() {
        if (workDir.exists()) workDir.deleteRecursively()
        workDir.mkdirs()
        val zip = ZipFile(jarFile)
        zip.use { zip ->
            val entries = zip.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (entry.isDirectory) continue
                val name = entry.name
                if (!name.startsWith("assets/")) continue
                val normalized = runCatching {
                    AssetVaultFormat.normalizeAssetPath(name)
                }.getOrNull() ?: continue
                if (!normalized.startsWith("assets/")) continue
                val outFile = workDir.resolve(normalized).normalize()
                if (!outFile.path.startsWith(workDir.path)) continue
                outFile.parentFile?.mkdirs()
                zip.getInputStream(entry).use { input -> outFile.outputStream().use { output -> input.copyTo(output) } }
            }
        }
    }

    internal fun bindShader(unitId: String, shaderId: String?) {
        require(unitId.isNotBlank() && unitId == unitId.trim()) { "Unit shader binding id must be non-blank and trimmed" }
        shaderId?.let {
            require(it.isNotBlank() && it == it.trim()) { "Shader binding id must be non-blank and trimmed" }
        }
        shaderBindings[unitId] = shaderId
    }

    internal fun registerShader(definition: ShaderDefinition) {
        require(definition.id.isNotBlank() && definition.id == definition.id.trim()) {
            "Shader id must be non-blank and trimmed"
        }
        require(definition.fragment.value.isNotBlank()) { "Shader fragment path must be non-blank: ${definition.id}" }
        require(definition.uniforms.map { it.name }.toSet().size == definition.uniforms.size) {
            "Shader uniforms must have unique names: ${definition.id}"
        }
        check(definition.id !in shaderDefinitions) { "Shader is already registered: ${definition.id}" }
        shaderDefinitions[definition.id] = definition
    }

    internal fun reportUnitEventFailure(message: String) {
        engineModInfo?.addWarning(message)
    }

    internal fun selectedUnits(): List<UnitRuntimeState> = unitWorldApi.selectedUnits()

    private fun applyUnitShaderBindings(nativeUnits: Map<UnitId, CustomUnitConfig>): Int {
        val requestedBindings = linkedMapOf<String, String?>()
        unitApi.units().forEach { definition ->
            definition.extension.shaderId?.let { requestedBindings[definition.id.value] = it }
        }
        requestedBindings.putAll(shaderBindings)
        var applied = 0
        requestedBindings.forEach { (unitId, shaderId) ->
            val nativeUnit = nativeUnits.entries.firstOrNull { it.key.value == unitId }?.value
            if (nativeUnit == null) {
                val message = "JVM mod ${manifest.id}: shader binding references unknown unit $unitId"
                engineModInfo?.addError(message)
                logger.error { message }
                return@forEach
            }
            runCatching {
                val shader = shaderId?.let { id ->
                    shaderPrograms.getOrPut(id) {
                        createShaderProgram(
                            checkNotNull(shaderDefinitions[id]) {
                                "Shader binding for $unitId references unregistered shader $id"
                            }
                        )
                    }
                }
                bindShaderToUnitTextures(nativeUnit, shader)
                applied++
            }.onFailure { error ->
                val message = "JVM mod ${manifest.id}: failed to bind shader to $unitId: ${error.message}"
                engineModInfo?.addError(message)
                logger.error(error) { message }
            }
        }
        return applied
    }

    private fun createShaderProgram(definition: ShaderDefinition): ShaderProgram {
        require(ShaderTarget.UNIT in definition.targets) {
            "Shader ${definition.id} does not target units"
        }
        val fragment = resolvePackagedResourcePath(definition.fragment)
        val shader = definition.vertex?.let { vertex ->
            ShaderProgram().apply {
                a(resolvePackagedResourcePath(vertex).absolutePath, fragment.absolutePath)
            }
        } ?: ShaderProgram(fragment.absolutePath)
        definition.uniforms.forEach { uniform -> applyUniformDefault(shader, uniform) }
        return shader
    }

    private fun applyUniformDefault(shader: ShaderProgram, definition: ShaderUniformDefinition) {
        val value = definition.defaultValue ?: return
        val uniform = shader.a(definition.name)
        when (definition.type) {
            ShaderUniformType.FLOAT -> uniform.a((value as Number).toFloat())
            ShaderUniformType.VEC2 -> numericValues(value, 2).also { uniform.a(it[0], it[1]) }
            ShaderUniformType.VEC3 -> numericValues(value, 3).also { uniform.a(it[0], it[1], it[2]) }
            ShaderUniformType.VEC4 -> numericValues(value, 4).also { uniform.a(it[0], it[1], it[2], it[3]) }
            ShaderUniformType.INT -> uniform.a(
                floatArrayOf((value as Number).toInt().toFloat()),
                ShaderUniformValueType.INTEGER,
            )

            ShaderUniformType.BOOL -> uniform.a(
                floatArrayOf(if (value as Boolean) 1f else 0f),
                ShaderUniformValueType.INTEGER,
            )

            ShaderUniformType.MAT4 -> uniform.a(numericValues(value, 16), ShaderUniformValueType.MATRIX4)
            ShaderUniformType.TEXTURE -> {
                val resource = when (value) {
                    is ResourcePath -> value
                    is String -> ResourcePath(value)
                    else -> error("Texture uniform ${definition.name} must use ResourcePath or String")
                }
                val texture = openPackagedResource(resource).use { input ->
                    GameEngine.getInstance().renderGraphicsEngine.a(input, true)
                }
                shader.a(definition.name, texture)
            }
        }
    }

    private fun numericValues(value: Any, expectedSize: Int): FloatArray {
        val values = when (value) {
            is FloatArray -> value
            is DoubleArray -> FloatArray(value.size) { value[it].toFloat() }
            is IntArray -> FloatArray(value.size) { value[it].toFloat() }
            is Array<*> -> value.map { it as Number }.map(Number::toFloat).toFloatArray()
            is Iterable<*> -> value.map { it as Number }.map(Number::toFloat).toFloatArray()
            else -> error("Expected $expectedSize numeric values, got ${value.javaClass.simpleName}")
        }
        require(values.size == expectedSize) { "Expected $expectedSize numeric values, got ${values.size}" }
        return values
    }

    private fun bindShaderToUnitTextures(unit: CustomUnitConfig, shader: ShaderProgram?) {
        val textures = linkedSetOf<Texture>()
        textures += unit.baseTexture
        textures += unit.teamColoredBaseTextures
        textures.forEach { it.a(shader) }
    }

    internal fun openPackagedResource(path: ResourcePath): InputStream {
        assetStore.open(path.value)?.let { return it }
        val file = resolvePackagedResourcePath(path)
        require(file.isFile) { "Missing packaged resource: ${path.value}" }
        return file.inputStream()
    }

    private fun resolvePackagedResourcePath(path: ResourcePath): File {
        val root = workDir.canonicalFile
        val relative = normalizedPackagedPath(path)
        val file = root.resolve(relative).canonicalFile
        require(file.path == root.path || file.path.startsWith(root.path + File.separator)) {
            "Resource escapes the mod directory: ${path.value}"
        }
        require(file.isFile || assetStore.exists(relative)) { "Missing packaged resource: ${path.value}" }
        return file
    }

    internal fun loadRenderTexture(path: ResourcePath, options: TextureOptions): Texture {
        val texture = openPackagedResource(path).use { input ->
            GameEngine.getInstance().renderGraphicsEngine.a(input, true)
        }
        texture.setPremultipliedAlpha(options.premultiplyAlpha)
        return texture
    }

    private fun normalizedPackagedPath(path: ResourcePath): String =
        AssetVaultFormat.normalizeAssetPath(path.value).also { normalized ->
            require(normalized.startsWith("assets/")) {
                "JVM mod packaged resources must be under assets/: ${path.value}"
            }
        }

    override fun close() {
        engineModInfo?.let(JvmModAssetBridge::unmount)
        engineModInfo = null
        assetStore.close()
    }

    companion object {
        /** A binding that carries no damage extensions, so nothing needs registering. */
        private val EMPTY_PROJECTILE_DAMAGE = DamageRegistry.ProjectileDamage()

        @JvmStatic
        fun create(
            manifest: ModManifest,
            jarFile: File,
            platformBridge: PlatformBridge? = null,
        ): ApiImpl {
            val root = File(FileHelper.getCachePath(), "jvm-mod-api/${sanitizePathSegment(manifest.id)}")
            val keyStore = platformBridge?.storage?.let(::JvmModAssetKeyStore)
            val resolver = keyStore ?: EmptyAssetCredentialResolver
            val assetStore = JvmModAssetStore(
                jarFile,
                manifest.id,
                resolver,
                OnlineAssetRevocationListFetcher,
            )
            return try {
                assetStore.requireAuthorized()
                ApiImpl(manifest, jarFile, root, platformBridge, assetStore)
            } catch (error: Throwable) {
                assetStore.close()
                throw error
            }
        }

        private fun sanitizePathSegment(value: String): String = value
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9._-]"), "_")
            .ifBlank { "mod" }
    }
}

private class GameImpl(private val api: Api) : Game {
    override val gameVersion: String get() = GameEngine.getInstance().version
    override val tick: Long get() = GameEngine.getInstance().currentTick.toLong()
    override val headless: Boolean get() = false
    override fun log(level: LogLevel, message: String, cause: Throwable?) {
        val tag = "JVM_MOD:${(api as ApiImpl).manifest.name}"
        when (level) {
            LogLevel.DEBUG -> logger.debug(tag) { message }
            LogLevel.INFO -> logger.info(tag) { message }
            LogLevel.WARN -> logger.warn(cause, tag) { message }
            LogLevel.ERROR -> logger.error(cause, tag) { message }
        }
    }

    override fun schedule(delayTicks: Long, task: ModTask): ScheduledTask =
        ModScheduler.schedule(api, delayTicks, null, task)

    override fun repeat(intervalTicks: Long, task: ModTask): ScheduledTask =
        ModScheduler.schedule(api, intervalTicks, intervalTicks, task)

    override fun localTeam(): TeamState? = GameEngine.getInstance().playerTeam?.toTeamState()

    override fun team(teamId: Int): TeamState? = PlayerTeam.k(teamId)?.toTeamState()

    override fun registerTeamAction(id: TeamActionId, handler: TeamActionHandler) {
        TeamActionRegistry.register(api as ApiImpl, id, handler)
    }

    override fun requestTeamAction(id: TeamActionId, targetPosition: WorldPosition?): Boolean =
        TeamActionRegistry.request(GameEngine.getInstance().playerTeam, id, targetPosition)

    override fun registerProjectileObserver(id: ProjectileObserverId, observer: ProjectileObserver) {
        ProjectileObserverRegistry.register(api as ApiImpl, id, observer)
    }

    override fun registerTurretFireCycleObserver(
        id: TurretFireCycleObserverId,
        observer: TurretFireCycleObserver,
    ) {
        TurretFireCycleObserverRegistry.register(api as ApiImpl, id, observer)
    }
}

private class AssetImpl(
    private val root: File,
    private val store: JvmModAssetStore,
) : Asset {
    private val mounts = linkedMapOf<String, File>()

    override fun mount(namespace: String, root: ResourcePath) {
        require(namespace.isNotBlank()) { "Asset mount namespace must not be blank" }
        mounts[namespace] = File(root.value.removePrefix("file:")).canonicalFile
    }

    override fun open(path: ResourcePath): io.github.rwx.mod.api.ResourceStream {
        resolveMount(path)?.let { return ResourceStream(it.inputStream()) }
        val normalized = requireNotNull(normalizedPackagedAssetPathOrNull(path)) {
            "JVM mod packaged resources must be under assets/: ${path.value}"
        }
        store.open(normalized)?.let { return ResourceStream(it) }
        return ResourceStream(resolveLocal(normalized).inputStream())
    }

    override fun exists(path: ResourcePath): Boolean {
        resolveMount(path)?.let { return it.exists() }
        val normalized = normalizedPackagedAssetPathOrNull(path) ?: return false
        return store.exists(normalized) || resolveLocal(normalized).exists()
    }

    override fun list(path: ResourcePath): List<ResourcePath> {
        resolveMount(path)?.let { directory ->
            return directory.listFiles()?.map { ResourcePath(it.path) } ?: emptyList()
        }
        val normalized = normalizedPackagedAssetPathOrNull(path) ?: return emptyList()
        val packaged = store.list(normalized).map(::ResourcePath)
        val local = resolveLocal(normalized).listFiles()?.map { ResourcePath(it.path) }.orEmpty()
        return (packaged + local).distinctBy(ResourcePath::value)
    }

    private fun resolveMount(path: ResourcePath): File? {
        val value = path.value
        val namespaceIndex = value.indexOf(':')
        if (namespaceIndex > 0) {
            val namespace = value.substring(0, namespaceIndex)
            val localPath = value.substring(namespaceIndex + 1)
            mounts[namespace]?.let { return safeResolve(it, localPath) }
        }
        return null
    }

    private fun normalizedPackagedAssetPathOrNull(path: ResourcePath): String? =
        runCatching { AssetVaultFormat.normalizeAssetPath(path.value) }
            .getOrNull()
            ?.takeIf { it == "assets" || it.startsWith("assets/") }

    private fun resolveLocal(normalizedPath: String): File = safeResolve(root, normalizedPath)

    private fun safeResolve(base: File, relative: String): File {
        val canonicalBase = base.canonicalFile
        val file = canonicalBase.resolve(relative).canonicalFile
        require(file.path == canonicalBase.path || file.path.startsWith(canonicalBase.path + File.separator)) {
            "Resource escapes its mounted directory: $relative"
        }
        return file
    }
}

private class ResourceStream(private val input: InputStream) : io.github.rwx.mod.api.ResourceStream {
    override fun readBytes(): ByteArray = input.readBytes()
    override fun readText(charset: String): String = input.bufferedReader(Charset.forName(charset)).readText()
    override fun close() = input.close()
}

private class Localization : io.github.rwx.mod.api.Localization {
    private val entries = linkedMapOf<String, MutableMap<String, String>>()
    override fun register(locale: String, entries: Map<String, String>) {
        this.entries.getOrPut(locale) { linkedMapOf() }.putAll(entries)
    }

    override fun translate(key: String, locale: String?, args: Map<String, Any?>): String {
        val value = locale?.let { entries[it]?.get(key) } ?: entries.values.firstNotNullOfOrNull { it[key] } ?: key
        return args.entries.fold(value) { text, (name, arg) -> text.replace("{$name}", arg.toString()) }
    }
}

private class RecordingGraphics(private val api: ApiImpl) : Graphics {
    override fun registerTexture(id: TextureId, file: ResourcePath, options: TextureOptions) =
        RenderRegistry.registerTexture(api, id, file, options)

    override fun registerProjectileRenderer(id: RendererId, renderer: ProjectileRenderer) =
        RenderRegistry.registerProjectileRenderer(api, id, renderer)

    override fun registerPreFireRenderer(id: RendererId, renderer: PreFireRenderer) =
        RenderRegistry.registerPreFireRenderer(api, id, renderer)

    override fun registerPostFireRenderer(id: RendererId, renderer: PostFireRenderer) =
        RenderRegistry.registerPostFireRenderer(api, id, renderer)

    override fun registerEffectRenderer(id: RendererId, renderer: EffectRenderer) =
        RenderRegistry.registerEffectRenderer(api, id, renderer)

    override fun registerUnitRenderer(id: RendererId, renderer: UnitRenderer) =
        RenderRegistry.registerUnitRenderer(api, id, renderer)

    override fun registerTerrainOverlayRenderer(id: RendererId, renderer: TerrainOverlayRenderer) =
        TerrainRegistry.registerOverlayRenderer(api, id, renderer)

    override fun registerTerrainShader(definition: TerrainShaderDefinition) =
        TerrainRegistry.registerShader(api, definition)

    override fun registerAnimation(definition: AnimationDefinition) {}
    override fun registerShader(definition: ShaderDefinition) = api.registerShader(definition)

    override fun setUnitShader(unitId: String, shaderId: String?) = api.bindShader(unitId, shaderId)
    override fun addRenderPass(definition: RenderPassDefinition) {}
}

private class MapApi(private val owner: ApiImpl) : ApiMap {
    override fun info(): MapInfo? = GameEngine.getInstance()?.tileMap?.let {
        MapInfo(it.tileCountX, it.tileCountY, it.tileWorldSizeX.toFloat(), it.tileWorldSizeY.toFloat())
    }

    override fun tileAt(x: Int, y: Int): TerrainTile? {
        val map = GameEngine.getInstance()?.tileMap ?: return null
        if (x !in 0 until map.tileCountX || y !in 0 until map.tileCountY) return null
        val tile = map.getTileAt(x, y) ?: return null
        val occupied = com.corrodinggames.rts.game.units.buildings.BaseBuilding.b(x, y) != null
        return TerrainTile(
            tile.isWater && !tile.isWaterBridge, tile.isLava, tile.isCliff,
            tile.blocksBuildingPlacement || tile.hasLargeObject || occupied
        )
    }

    override fun addOverlay(x: Int, y: Int, binding: TerrainOverlayBinding) {
        val map = GameEngine.getInstance()?.tileMap ?: return
        TerrainRegistry.addOverlay(owner, map, x, y, binding)
    }

    override fun removeOverlay(x: Int, y: Int, rendererId: RendererId) {
        val map = GameEngine.getInstance()?.tileMap ?: return
        TerrainRegistry.removeOverlay(owner, map, x, y, rendererId)
    }

    override fun overlayAt(x: Int, y: Int, rendererId: RendererId): TerrainOverlayBinding? {
        val map = GameEngine.getInstance()?.tileMap ?: return null
        if (x !in 0 until map.tileCountX || y !in 0 until map.tileCountY) return null
        return TerrainRegistry.overlayAt(owner, map, x, y, rendererId)
    }

    override fun overlaysAt(x: Int, y: Int): List<TerrainOverlayBinding> {
        val map = GameEngine.getInstance()?.tileMap ?: return emptyList()
        if (x !in 0 until map.tileCountX || y !in 0 until map.tileCountY) return emptyList()
        return TerrainRegistry.overlaysAt(owner, map, x, y)
    }

    override fun registerMap(definition: MapDefinition) {
        logger.info { "JVM mod map registration recorded: ${definition.id}" }
    }

    override fun registerTileset(definition: TilesetDefinition) {
        logger.info { "JVM mod tileset registration recorded: ${definition.id}" }
    }
}

private class RecordingRule : Rule {
    override fun registerAiProfile(definition: AiProfileDefinition) {}
    override fun registerGameMode(definition: GameModeDefinition) {}
    override fun setGlobalRule(key: String, value: Any?) {}
}

private class ModAudio(private val api: ApiImpl) : Audio {
    override fun registerSound(id: String, file: ResourcePath, properties: Map<String, Any?>) {
        AudioRegistry.registerSound(api, id, file, properties)
    }

    override fun registerMusic(id: String, file: ResourcePath, properties: Map<String, Any?>) {}
    override fun playSound(id: String, x: Float?, y: Float?, volume: Float) {
        AudioRegistry.playSound(api, id, x, y, volume)
    }
}

private class AiBehavior(
    private val commands: UnitCommands,
    private val unitWorld: UnitWorld,
) : io.github.rwx.mod.api.AiBehavior {
    private val agents = linkedMapOf<String, AiAgentDefinition>()
    private val bindings = mutableListOf<AiAgentBinding>()
    private val playerConfigs = linkedMapOf<Int, AiPlayerConfig>()
    private var nextFrameId = 1L
    private val actionCounters = linkedMapOf<Int, Pair<Long, Int>>()
    private val frameHistory = linkedMapOf<ObservationKey, ArrayDeque<AiObservationFrame>>()

    private data class ObservationKey(
        val scope: AiObservationScope,
        val spec: AiObservationSpec
    )

    override fun registerAgent(definition: AiAgentDefinition) {
        agents[definition.id] = definition
    }

    override fun unregisterAgent(id: String) {
        agents.remove(id)
        bindings.removeAll { it.agentId == id }
    }

    override fun bindAgent(binding: AiAgentBinding) {
        require(agents.containsKey(binding.agentId)) { "AI agent is not registered: ${binding.agentId}" }
        bindings.removeAll { it.scope == binding.scope }
        bindings += binding
    }

    override fun unbindAgent(scope: AiControlScope) {
        bindings.removeAll { it.scope == scope }
    }

    override fun listPlayers(): List<AiPlayerState> =
        (0 until PlayerTeam.TEAM_NEUTRAL).mapNotNull { teamId -> getPlayerState(teamId) }

    override fun getPlayerState(teamId: Int): AiPlayerState? =
        PlayerTeam.k(teamId)?.toAiPlayerState(playerConfigs[teamId])

    override fun configurePlayer(config: AiPlayerConfig): AiPlayerState? {
        val team = PlayerTeam.k(config.teamId) ?: return null
        if (config.agentId != null && config.agentId !in agents) {
            logger.warn { "Configuring AI player ${config.teamId} with unregistered agent ${config.agentId}" }
        }
        playerConfigs[config.teamId] = config
        team.isTeamControlledByAI = config.enabled && config.mode == AiPlayerMode.GAME_AI
        team.teamAIHint = config.agentId
        config.agentId?.let { agentId ->
            val binding = AiAgentBinding(agentId, config.scope)
            bindings.removeAll { it.scope == config.scope }
            bindings += binding
        }
        return team.toAiPlayerState(config)
    }

    override fun setPlayerEnabled(teamId: Int, enabled: Boolean): AiPlayerState? {
        val current = playerConfigs[teamId] ?: AiPlayerConfig(teamId = teamId)
        return configurePlayer(current.copy(enabled = enabled))
    }

    override fun currentObservation(scope: AiObservationScope): AiObservationFrame {
        val gameEngine = GameEngine.getInstance()
        val spec = resolveObservationSpec(scope)
        val teams = observeTeams(scope)
        val visibilityRadius = resolveVisibilityRadius(scope, spec)
        val units = observeUnits(scope, spec, visibilityRadius)
        val resourcePools = if (spec.includeResources) observeResourcePools(scope) else emptyList()
        val map = if (spec.includeMap) observeMap() else null
        val frame = AiObservationFrame(
            frameId = nextFrameId++,
            tick = gameEngine.currentTick.toLong(),
            scope = scope,
            teams = teams,
            units = units,
            resourcePools = resourcePools,
            map = map,
            terminalTeams = observeTerminalTeams(teams)
        )
        return applyFrameStack(scope, spec, frame)
    }

    override fun submitActions(actions: List<UnitCommandRequest>): List<UnitCommandResult> {
        if (actions.isEmpty()) return emptyList()
        val currentTick = GameEngine.getInstance().currentTick.toLong()
        val actionCounts = linkedMapOf<Int, Int>()
        val results = ArrayList<UnitCommandResult>(actions.size)
        for (action in actions) {
            val rejection = validateAction(action, currentTick, actionCounts)
            if (rejection != null) {
                results += action.rejected(rejection)
                continue
            }
            val result = submitAction(action)
            results += result
            if (result.accepted) {
                updateActionCount(action.teamId, currentTick, actionCounts)
            }
        }
        return results
    }

    private fun validateAction(
        action: UnitCommandRequest,
        currentTick: Long,
        actionCounts: MutableMap<Int, Int>
    ): String? {
        val config = playerConfigs[action.teamId] ?: return null
        if (config.mode != AiPlayerMode.EXTERNAL_AGENT) return null
        if (!config.enabled) return "AI player is disabled"
        validateScope(action, config.scope)?.let { return it }

        val actionSpace = config.agentId?.let { agents[it] }?.actionSpace ?: AiActionSpaceDefinition()
        if (action.type !in actionSpace.allowedTypes) return "Action type not allowed"
        if (!actionSpace.allowQueuedCommands && action.queued) return "Queued commands not allowed"
        val maxUnits = actionSpace.maxUnitsPerAction
        if (maxUnits != null && action.unitIds.size > maxUnits) return "Too many units for action"
        val maxActions = actionSpace.maxActionsPerTick
        if (maxActions != null) {
            val currentCount = currentActionCount(action.teamId, currentTick, actionCounts)
            if (currentCount >= maxActions) return "Action limit exceeded for tick"
        }
        return null
    }

    private fun validateScope(action: UnitCommandRequest, scope: AiControlScope): String? {
        if (scope.teamIds.isNotEmpty() && action.teamId !in scope.teamIds) {
            return "Team not in control scope"
        }
        if (scope.unitIds.isNotEmpty() && action.unitIds.any { it !in scope.unitIds }) {
            return "Unit not in control scope"
        }
        if (scope.unitTypeIds.isNotEmpty()) {
            for (unitId in action.unitIds) {
                val state = unitWorld.state(
                    UnitRuntimeRef(unitId, definitionId = null, teamId = action.teamId)
                ) ?: return "Unit not found: ${unitId.value}"
                if (state.typeId !in scope.unitTypeIds) {
                    return "Unit type not in control scope"
                }
            }
        }
        return null
    }

    private fun currentActionCount(
        teamId: Int,
        currentTick: Long,
        actionCounts: MutableMap<Int, Int>
    ): Int {
        return actionCounts.getOrPut(teamId) {
            val stored = actionCounters[teamId]
            if (stored != null && stored.first == currentTick) stored.second else 0
        }
    }

    private fun updateActionCount(
        teamId: Int,
        currentTick: Long,
        actionCounts: MutableMap<Int, Int>
    ) {
        val nextCount = currentActionCount(teamId, currentTick, actionCounts) + 1
        actionCounts[teamId] = nextCount
        actionCounters[teamId] = currentTick to nextCount
    }

    private fun observeTeams(scope: AiObservationScope): List<AiTeamObservation> =
        (0 until PlayerTeam.TEAM_NEUTRAL)
            .mapNotNull { PlayerTeam.k(it) }
            .filter { scope.teamIds.isEmpty() || it.teamId in scope.teamIds || scope.includeEnemies }
            .map {
                AiTeamObservation(
                    id = it.teamId,
                    name = it.teamName,
                    credits = it.credits,
                    energy = it.energy,
                    unitCount = it.teamUnitCountInt + it.teamBuildingCountInt,
                    defeated = it.isTeamDefeated || it.isTeamWipedOut,
                    controlledByAi = it.isTeamControlledByAI
                )
            }

    private fun observeUnits(
        scope: AiObservationScope,
        spec: AiObservationSpec,
        visibilityRadius: Float
    ): List<UnitRuntimeState> {
        val maxUnits = scope.maxUnits ?: Int.MAX_VALUE
        val candidates = unitWorld.query(
            UnitQuery(
                includeDestroyed = scope.includeDestroyed,
                center = scope.center,
                radius = scope.radius,
            )
        ).filter { scope.includes(it) }
        val filtered = if (spec.includeHiddenEnemies) {
            candidates
        } else {
            filterHiddenEnemies(candidates, scope, visibilityRadius)
        }
        val observed = ArrayList<UnitRuntimeState>()
        for (unit in filtered) {
            observed += if (spec.includeUnitOrders) unit else unit.copy(currentTargetId = null)
            if (observed.size >= maxUnits) break
        }
        return observed
    }

    private fun filterHiddenEnemies(
        units: List<UnitRuntimeState>,
        scope: AiObservationScope,
        visibilityRadius: Float
    ): List<UnitRuntimeState> {
        if (visibilityRadius <= 0f) return units
        if (scope.teamIds.isEmpty()) return units
        val allies = units.filter { unit -> unit.ref.teamId in scope.teamIds }
        if (allies.isEmpty()) return units
        val radiusSq = visibilityRadius * visibilityRadius
        return units.filter { unit ->
            val teamId = unit.ref.teamId
            if (teamId < 0 || teamId in scope.teamIds) return@filter true
            allies.any { ally ->
                val dx = unit.position.x - ally.position.x
                val dy = unit.position.y - ally.position.y
                (dx * dx + dy * dy) <= radiusSq
            }
        }
    }

    private fun resolveObservationSpec(scope: AiObservationScope): AiObservationSpec {
        val teamId = scope.teamIds.singleOrNull() ?: return AiObservationSpec()
        val agentId = playerConfigs[teamId]?.agentId ?: return AiObservationSpec()
        return agents[agentId]?.observation ?: AiObservationSpec()
    }

    private fun resolveVisibilityRadius(scope: AiObservationScope, spec: AiObservationSpec): Float {
        if (spec.includeHiddenEnemies) return 0f
        val teamId = scope.teamIds.singleOrNull() ?: return 0f
        val agentId = playerConfigs[teamId]?.agentId
        val agent = agentId?.let { agents[it] }
        val override = agent?.properties?.get("visibilityRadius") as? Number
        return override?.toFloat() ?: 0f
    }

    private fun applyFrameStack(
        scope: AiObservationScope,
        spec: AiObservationSpec,
        frame: AiObservationFrame
    ): AiObservationFrame {
        val stackSize = spec.frameStack
        if (stackSize <= 1) return frame
        val key = ObservationKey(scope, spec)
        val history = frameHistory.getOrPut(key) { ArrayDeque() }
        val cleanFrame = frame.copy(stackedFrames = emptyList())
        history.addLast(cleanFrame)
        while (history.size > stackSize) {
            history.removeFirst()
        }
        val stackedFrames = history.toList().dropLast(1)
        return frame.copy(stackedFrames = stackedFrames)
    }

    private fun observeResourcePools(scope: AiObservationScope): List<WorldPosition> {
        if (!scope.includeNeutral) return emptyList()
        val tileMap = GameEngine.getInstance().tileMap ?: return emptyList()
        val pools = ArrayList<WorldPosition>()
        for (tileX in 0 until tileMap.tileCountX) {
            for (tileY in 0 until tileMap.tileCountY) {
                val tile = tileMap.getPathingOverrideTileAt(tileX, tileY) ?: continue
                if (!tile.isResourcePool) continue
                tileMap.setCursorTileIndexFromTileIndex(tileX, tileY)
                val worldX = tileMap.cursorTileX + tileMap.halfTileWorldSizeX.toFloat()
                val worldY = tileMap.cursorTileY + tileMap.halfTileWorldSizeY.toFloat()
                if (!scope.includesPosition(worldX, worldY)) continue
                pools += WorldPosition(worldX, worldY)
            }
        }
        return pools
    }

    private fun observeMap(): AiMapObservation? {
        val tileMap = GameEngine.getInstance().tileMap ?: return null
        val tileWidth = (tileMap.halfTileWorldSizeX * 2).takeIf { it > 0 }
        val tileHeight = (tileMap.halfTileWorldSizeY * 2).takeIf { it > 0 }
        return AiMapObservation(
            width = tileMap.tileCountX,
            height = tileMap.tileCountY,
            tileWidth = tileWidth,
            tileHeight = tileHeight
        )
    }

    private fun observeTerminalTeams(teams: List<AiTeamObservation>): Set<Int> {
        val terminalTeams = teams.filter { it.defeated }.mapTo(linkedSetOf()) { it.id }
        teams.filter { !it.defeated }.forEach { team ->
            val enemiesAlive =
                teams.any { other -> other.id != team.id && !other.defeated && areEnemies(team.id, other.id) }
            if (!enemiesAlive) terminalTeams += team.id
        }
        return terminalTeams
    }

    private fun PlayerTeam.toAiPlayerState(config: AiPlayerConfig?): AiPlayerState = AiPlayerState(
        teamId = teamId,
        name = teamName,
        enabled = config?.enabled ?: isTeamControlledByAI,
        mode = config?.mode ?: if (isTeamControlledByAI) AiPlayerMode.GAME_AI else AiPlayerMode.HUMAN,
        agentId = config?.agentId ?: teamAIHint,
        controlledByGameAi = isTeamControlledByAI,
        defeated = isTeamDefeated || isTeamWipedOut,
        unitCount = teamUnitCountInt + teamBuildingCountInt,
        credits = credits,
        energy = energy
    )

    private fun submitAction(action: UnitCommandRequest): UnitCommandResult {
        return runCatching { commands.submit(action) }
            .getOrElse { error -> action.rejected(error.message ?: error.javaClass.simpleName) }
    }

    private fun AiObservationScope.includes(unit: UnitRuntimeState): Boolean {
        val teamId = unit.ref.teamId
        if (teamId in teamIds) return true
        if (teamIds.isEmpty()) return includeNeutral || teamId >= 0
        if (teamId < 0 || teamId >= PlayerTeam.TEAM_NEUTRAL) return includeNeutral
        val team = PlayerTeam.k(teamId) ?: return includeNeutral
        return includeEnemies && teamIds.none { selectedTeamId -> PlayerTeam.k(selectedTeamId)?.d(team) == true }
    }

    private fun AiObservationScope.includesPosition(x: Float, y: Float): Boolean {
        val center = center ?: return true
        val radius = radius ?: return true
        val dx = x - center.x
        val dy = y - center.y
        return dx * dx + dy * dy <= radius * radius
    }

    private fun areEnemies(teamId: Int, otherTeamId: Int): Boolean {
        val team = PlayerTeam.k(teamId) ?: return false
        val otherTeam = PlayerTeam.k(otherTeamId) ?: return false
        return !team.d(otherTeam)
    }

    private fun UnitCommandRequest.rejected(message: String): UnitCommandResult =
        UnitCommandResult(id, false, message)
}

private class RecordingUi(private val api: ApiImpl) : Ui {
    override fun addInGameMenuItem(menuId: Int?, text: LocalizedText, onClick: (Int) -> Unit) {
        UiRegistry.addInGameMenuItem(api, menuId, text, onClick)
    }

    override fun removeInGameMenuItem(menuId: Int) {
        UiRegistry.removeInGameMenuItem(menuId)
    }

    override fun selectedUnits(): List<UnitRuntimeState> = api.selectedUnits()

    override fun registerHud(id: HudId, order: Int, content: @Composable () -> Unit) {
        UiRegistry.registerHud(api, id, order, content)
    }

    override fun unregisterHud(id: HudId) {
        UiRegistry.unregisterHud(api, id)
    }

    override fun setNativeHudVisible(visible: Boolean) {
        UiRegistry.setNativeHudVisible(api, visible)
    }

    override fun registerWindow(
        id: ModWindowId,
        title: LocalizedText,
        content: @Composable (ModWindowContext) -> Unit,
    ) {
        UiRegistry.registerWindow(api, id, title, content)
    }

    override fun openWindow(id: ModWindowId) {
        UiRegistry.openWindow(id)
    }

    override fun closeWindow() {
        UiRegistry.closeWindow()
    }

    override fun refreshWindow() {
        UiRegistry.refreshWindow()
    }

    override fun showMessage(message: LocalizedText, durationTicks: Int) {
        GameEngine.getInstance().alert(message.literal ?: message.key ?: VariableScope.nullOrMissingString)
    }

    override fun requestWorldPosition(
        onSelected: (WorldPosition) -> Unit,
        onCancelled: () -> Unit,
    ): WorldPositionSelection = UiRegistry.requestWorldPosition(api, onSelected, onCancelled)
}

private fun Any?.toIniValue(): String = when (this) {
    null -> VariableScope.nullOrMissingString
    is ResourcePath -> value
    is LocalizedText -> literal ?: key ?: VariableScope.nullOrMissingString
    is Cost -> toIniValue()
    is Boolean, is Number -> toString()
    is Iterable<*> -> joinToString(",") { it.toIniValue() }
    else -> toString()
}

private fun Cost.toIniValue(): String = resources.entries.joinToString(",") { (key, value) ->
    if (key.value == "credits") value.toInt().toString() else "${key.value}=$value"
}
