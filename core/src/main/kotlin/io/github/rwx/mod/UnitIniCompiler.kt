package io.github.rwx.mod

import io.github.rwx.mod.api.*

internal data class UnitIniCompilation(
    val fileName: String,
    val content: String,
    val warnings: List<String>,
    val extensionBindings: UnitExtensionBindings,
)

internal data class UnitExtensionBindings(
    val unit: UnitExtensionBinding,
    val projectiles: Map<String, ProjectileExtensionBinding>,
    val turrets: Map<String, TurretExtensionBinding>,
    val effects: Map<String, EffectExtensionBinding>,
)

internal data class UnitExtensionBinding(
    val damageAvoidChance: Float?,
    val exposureOffsetX: Float?,
    val exposureOffsetY: Float?,
    val exposureWidth: Float?,
    val exposureHeight: Float?,
)

internal data class ProjectileExtensionBinding(
    val renderer: ProjectileRenderBinding?,
    val observer: ProjectileObserverBinding?,
    val directDamageAmount: Float?,
    val areaDamageAmount: Float?,
    val directDamageHitRateBonus: Float?,
    val areaDamageHitRateBonus: Float?,
    val areaDamageExcludeDirectHit: Boolean?,
    val rayDamage: Boolean?,
    val rayDamageRange: Float?,
    val rayDamageWidth: Float?,
    val rayDamageTargetWidthFactor: Float?,
    val rayDamageHitEffectOffsetFactor: Float?,
    val rayDamageSecondaryTargetTags: List<String>?,
    val directDamageArmourIgnoreAmount: Float?,
    val areaDamageArmourIgnoreAmount: Float?,
)

internal data class TurretExtensionBinding(
    val preFireDuration: Int,
    val postFireDuration: Int,
    val preFireRenderer: PreFireRenderBinding?,
    val postFireRenderer: PostFireRenderBinding?,
    val observer: TurretFireCycleObserverBinding?,
)

internal data class EffectExtensionBinding(val renderer: EffectRenderBinding)

private class MutableUnitExtensionBindings {
    var unit = UnitExtensionBinding(null, null, null, null, null)
    val projectiles = linkedMapOf<String, ProjectileExtensionBinding>()
    val turrets = linkedMapOf<String, TurretExtensionBinding>()
    val effects = linkedMapOf<String, EffectExtensionBinding>()

    fun freeze() = UnitExtensionBindings(unit, projectiles.toMap(), turrets.toMap(), effects.toMap())
}

internal object UnitIniCompiler {
    fun compile(definition: UnitDefinition, fileIndex: Int = 0): UnitIniCompilation {
        val document = IniDocumentWriter()
        val warnings = mutableListOf<String>()
        val extensionBindings = MutableUnitExtensionBindings()

        val isDerived = definition.iniSections
            .firstOrNull { it.name == "core" }
            ?.values?.values?.containsKey("copyFrom") == true
        if (!isDerived) {
            writeCore(document)
            writeGraphics(document)
            writeMovement(document)
        }
        writeNativeExtensionBindings(definition, extensionBindings)
        definition.iniSections.forEach { section ->
            applyIniSpec(document.section(section.name), section.values)
        }
        val core = document.section("core")
        core["name"] = definition.id
        collectSpecWarnings(definition, warnings)
        return UnitIniCompilation(
            fileName = "${fileIndex}_${safeFileName(definition.id)}.ini",
            content = document.render(),
            warnings = warnings,
            extensionBindings = extensionBindings.freeze(),
        )
    }

    private fun safeFileName(id: String): String =
        id.replace(INVALID_FILE_NAME_CHARACTER, "_").ifBlank { "unit" }

    private val INVALID_FILE_NAME_CHARACTER = Regex("""[\\/:*?"<>|\p{Cc}]""")

    private fun writeCore(document: IniDocumentWriter) {
        val core = document.section("core")
        core["class"] = "CustomUnitMetadata"
        core["price"] = "0"
        core["maxHp"] = "1"
        core["mass"] = "1"
        core["radius"] = "1"
        core["displayRadius"] = "1"
        core["buildSpeed"] = "1"
        core["armour"] = "0"
        core["fogOfWarSightRange"] = "0"
        core["isBuilding"] = "false"
        core["tags"] = ""
        core["explodeOnDeath"] = "true"
        core["hideScorchMark"] = "false"
    }

    private fun writeGraphics(document: IniDocumentWriter) {
        val graphics = document.section("graphics")
        graphics["total_frames"] = "1"
        graphics["image"] = "ROOT:units/missing_unit/missing.png"
        graphics["teamColoringMode"] = "disabled"
        graphics["imageSmoothing"] = "false"
        graphics["imageSmoothingWhenZoomedIn"] = "false"
        graphics["imageScale"] = "1.0"
    }

    private fun writeMovement(document: IniDocumentWriter) {
        val movement = document.section("movement")
        movement["movementType"] = "NONE"
        movement["moveSpeed"] = "1.0"
        movement["moveAccelerationSpeed"] = "0.0"
        movement["moveDecelerationSpeed"] = "0.0"
        movement["maxTurnSpeed"] = "4.0"
        movement["reverseSpeedPercentage"] = "0.6"

        val attack = document.section("attack")
        attack["canAttack"] = "false"
        attack["attackMovement"] = "normal"
    }

    private fun writeNativeExtensionBindings(
        definition: UnitDefinition,
        bindings: MutableUnitExtensionBindings,
    ) {
        bindings.unit = UnitExtensionBinding(
            damageAvoidChance = definition.extension.damageAvoidChance,
            exposureOffsetX = definition.extension.exposureOffsetX,
            exposureOffsetY = definition.extension.exposureOffsetY,
            exposureWidth = definition.extension.exposureWidth,
            exposureHeight = definition.extension.exposureHeight,
        )
        val sectionNames = definition.iniSections.mapTo(hashSetOf()) { it.name }
        definition.extension.projectiles.forEach { (name, extension) ->
            require("projectile_$name" in sectionNames) {
                "Unit ${definition.id} extends missing native projectile section '$name'"
            }
            bindings.projectiles[name] = ProjectileExtensionBinding(
                renderer = extension.renderBinding,
                observer = extension.observerBinding,
                directDamageAmount = extension.directDamageAmount,
                areaDamageAmount = extension.areaDamageAmount,
                directDamageHitRateBonus = extension.directDamageHitRateBonus,
                areaDamageHitRateBonus = extension.areaDamageHitRateBonus,
                areaDamageExcludeDirectHit = extension.areaDamageExcludeDirectHit,
                rayDamage = extension.rayDamage,
                rayDamageRange = extension.rayDamageRange,
                rayDamageWidth = extension.rayDamageWidth,
                rayDamageTargetWidthFactor = extension.rayDamageTargetWidthFactor,
                rayDamageHitEffectOffsetFactor = extension.rayDamageHitEffectOffsetFactor,
                rayDamageSecondaryTargetTags = extension.rayDamageSecondaryTargetTags,
                directDamageArmourIgnoreAmount = extension.directDamageArmourIgnoreAmount,
                areaDamageArmourIgnoreAmount = extension.areaDamageArmourIgnoreAmount,
            )
        }
        definition.extension.turrets.forEach { (name, extension) ->
            require("turret_$name" in sectionNames) {
                "Unit ${definition.id} extends missing native turret section '$name'"
            }
            bindings.turrets[name] = TurretExtensionBinding(
                preFireDuration = extension.preFireDuration,
                postFireDuration = extension.postFireDuration,
                preFireRenderer = extension.preFireRenderBinding,
                postFireRenderer = extension.postFireRenderBinding,
                observer = extension.observerBinding,
            )
        }
        definition.extension.effects.forEach { (name, extension) ->
            require("effect_$name" in sectionNames) {
                "Unit ${definition.id} extends missing native effect section '$name'"
            }
            bindings.effects[name] = EffectExtensionBinding(
                requireNotNull(extension.renderBinding) {
                    "Unit ${definition.id} effect extension '$name' must define renderBinding"
                },
            )
        }
    }

    private fun applyIniSpec(section: MutableMap<String, String>, spec: IniSpecValues) {
        spec.values.forEach { (key, value) -> section[key] = value.toIniValue() }
    }

    private fun collectSpecWarnings(
        definition: UnitDefinition,
        warnings: MutableList<String>,
    ) {
        val collected = mutableListOf<String>()
        definition.iniSections.forEach { section ->
            section.values.warnings.forEach { collected += "[${section.name}] $it" }
        }
        warnings += collected.distinct()
    }

    private fun Any?.toIniValue(): String = when (this) {
        null -> ""
        is Boolean, is Number -> toString()
        is Enum<*> -> name
        is Iterable<*> -> joinToString(",") { it.toIniValue() }
        else -> toString()
    }
}

private class IniDocumentWriter {
    private val sections = linkedMapOf<String, LinkedHashMap<String, String>>()

    fun section(name: String): LinkedHashMap<String, String> =
        sections.getOrPut(name) { linkedMapOf() }

    fun render(): String = buildString {
        sections.forEach { (name, values) ->
            append('[').append(name).append("]\n")
            values.forEach { (key, value) ->
                append(key).append(": ").append(value).append('\n')
            }
            append('\n')
        }
    }
}
