package io.github.rwx.mod.api

import kotlin.collections.Map

interface Units {
    fun registerUnit(definition: UnitDefinition)
}

data class UnitDefinition(
    val id: String,
    val extension: UnitExtension = UnitExtension(),
    val eventHandlers: List<UnitEventBinding> = emptyList(),
    val iniSections: List<IniSpecSection> = emptyList(),
)

@UnitDsl
data class UnitExtension(
    var shaderId: String? = null,
    var renderBinding: UnitRenderBinding? = null,
    var damageAvoidChance: Float? = null,
    var exposureOffsetX: Float? = null,
    var exposureOffsetY: Float? = null,
    var exposureWidth: Float? = null,
    var exposureHeight: Float? = null,
    var projectiles: Map<String, ProjectileExtension> = linkedMapOf(),
    var turrets: Map<String, TurretExtension> = linkedMapOf(),
    var effects: Map<String, EffectExtension> = linkedMapOf(),
) {
    fun projectile(name: String, configure: ProjectileExtension.() -> kotlin.Unit) {
        projectiles.asMutable().getOrPut(requireIniSectionName(name), ::ProjectileExtension).configure()
    }

    fun turret(name: String, configure: TurretExtension.() -> kotlin.Unit) {
        turrets.asMutable().getOrPut(requireIniSectionName(name), ::TurretExtension).configure()
    }

    fun effect(name: String, configure: EffectExtension.() -> kotlin.Unit) {
        effects.asMutable().getOrPut(requireIniSectionName(name), ::EffectExtension).configure()
    }
}

@UnitDsl
data class ProjectileExtension(
    var renderBinding: ProjectileRenderBinding? = null,
    var observerBinding: ProjectileObserverBinding? = null,
    var directDamageAmount: Float? = null,
    var areaDamageAmount: Float? = null,
    var directDamageHitRateBonus: Float? = null,
    var areaDamageHitRateBonus: Float? = null,
    var areaDamageExcludeDirectHit: Boolean? = null,
    var rayDamage: Boolean? = null,
    var rayDamageRange: Float? = null,
    var rayDamageWidth: Float? = null,
    var rayDamageTargetWidthFactor: Float? = null,
    var rayDamageHitEffectOffsetFactor: Float? = null,
    var rayDamageSecondaryTargetTags: List<String>? = null,
    var directDamageArmourIgnoreAmount: Float? = null,
    var areaDamageArmourIgnoreAmount: Float? = null,
)

@UnitDsl
data class TurretExtension(
    /** Wind-up before the shot; zero disables the pre-fire window entirely. */
    var preFireDuration: Int = 0,
    /** Recovery after the shot; zero disables the post-fire window entirely. */
    var postFireDuration: Int = 0,
    var preFireRenderBinding: PreFireRenderBinding? = null,
    var postFireRenderBinding: PostFireRenderBinding? = null,
    var observerBinding: TurretFireCycleObserverBinding? = null,
)

@UnitDsl
data class EffectExtension(
    var renderBinding: EffectRenderBinding? = null,
)

private fun <V> Map<String, V>.asMutable(): MutableMap<String, V> =
    this as? MutableMap<String, V> ?: toMutableMap()
