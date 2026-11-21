package io.github.rwx.mod.api

import io.github.rwx.mod.api.specs.*

class IniSpecValues internal constructor(
    val values: kotlin.collections.Map<String, Any?>,
    val warnings: List<String> = emptyList(),
)

class IniSpecSection internal constructor(
    val name: String,
    val values: IniSpecValues,
)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class RequiredIniKey

@UnitDsl
class UnitIniBuilder internal constructor() {
    private val sections = linkedMapOf<String, Any>()
    private val rawSections = linkedMapOf<String, kotlin.collections.MutableMap<String, Any?>>()

    fun core(configure: CoreSpec.() -> Unit) = section("core", ::CoreSpec, configure)
    fun graphics(configure: GraphicsSpec.() -> Unit) = section("graphics", ::GraphicsSpec, configure)
    fun attack(configure: AttackSpec.() -> Unit) = section("attack", ::AttackSpec, configure)
    fun movement(configure: MovementSpec.() -> Unit) = section("movement", ::MovementSpec, configure)
    fun ai(configure: AiSpec.() -> Unit) = section("ai", ::AiSpec, configure)
    fun resource(name: String, configure: ResourceSpec.() -> Unit) =
        section(named("resource", name), ::ResourceSpec, configure)

    fun globalResource(name: String, configure: GlobalResourceSpec.() -> Unit) =
        section(named("global_resource", name), ::GlobalResourceSpec, configure)

    fun action(name: String, configure: ActionSpec.() -> Unit) =
        section(named("action", name), ::ActionSpec, configure)

    fun hiddenAction(name: String, configure: HiddenActionSpec.() -> Unit) =
        section(named("hiddenAction", name), ::HiddenActionSpec, configure)

    fun turret(name: String, configure: TurretSpec.() -> Unit) =
        section(named("turret", name), ::TurretSpec, configure)

    fun projectile(name: String, configure: ProjectileSpec.() -> Unit) =
        section(named("projectile", name), ::ProjectileSpec, configure)

    fun effect(name: String, configure: EffectSpec.() -> Unit) =
        section(named("effect", name), ::EffectSpec, configure)

    fun effect(name: String, spec: EffectSpec) =
        section(named("effect", name), { spec.copy() }) {}

    fun decal(name: String, configure: DecalSpec.() -> Unit) =
        section(named("decal", name), ::DecalSpec, configure)

    fun attachment(name: String, configure: AttachmentSpec.() -> Unit) =
        section(named("attachment", name), ::AttachmentSpec, configure)

    fun leg(name: String, configure: LegSpec.() -> Unit) =
        section(named("leg", name), ::LegSpec, configure)

    fun arm(name: String, configure: ArmSpec.() -> Unit) =
        section(named("arm", name), ::ArmSpec, configure)

    fun placementRule(name: String, configure: PlacementRuleSpec.() -> Unit) =
        section(named("placementRule", name), ::PlacementRuleSpec, configure)

    fun animation(name: String, configure: AnimationSpec.() -> Unit) =
        section(named("animation", name), ::AnimationSpec, configure)

    fun canBuild(name: String, configure: CanBuildSpec.() -> Unit) =
        section(named("canBuild", name), ::CanBuildSpec, configure)

    fun template(name: String, configure: TemplateSpec.() -> Unit) =
        section(named("template", name), ::TemplateSpec, configure)

    fun rawSection(name: String, values: kotlin.collections.Map<String, Any?>) {
        requireIniSectionName(name)
        rawSections.getOrPut(name) { linkedMapOf() }.putAll(values)
    }

    internal fun build(): List<IniSpecSection> {
        val merged = linkedMapOf<String, IniSpecSection>()
        sections.forEach { (name, spec) ->
            IniSpecCodec.encode(spec, includeRequiredDefaults = true, sectionName = name)
                .takeIf { it.values.isNotEmpty() }
                ?.let { merged[name] = IniSpecSection(name, it) }
        }
        rawSections.forEach { (name, values) ->
            if (values.isNotEmpty()) {
                merged[name] = IniSpecSection(name, IniSpecValues(values.toMap()))
            }
        }
        return merged.values.toList()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> section(name: String, factory: () -> T, configure: T.() -> Unit) {
        val spec = sections.getOrPut(name, factory) as T
        spec.configure()
    }

    private fun named(prefix: String, name: String): String =
        "${prefix}_${requireIniSectionName(name)}"
}

internal object IniSpecCodec {
    fun encode(
        spec: Any,
        includeRequiredDefaults: Boolean = false,
        sectionName: String? = null,
    ): IniSpecValues {
        val type = spec.javaClass
        val defaults = type.getDeclaredConstructor().newInstance()
        val keys = GeneratedIniSpecKeys.byClass[type.simpleName].orEmpty()
        val values = linkedMapOf<String, Any?>()
        type.declaredFields.forEach { field ->
            if (field.isSynthetic || java.lang.reflect.Modifier.isStatic(field.modifiers)) return@forEach
            // Android lacks AccessibleObject.trySetAccessible() (Java 9+);
            @Suppress("DEPRECATION")
            if (!field.isAccessible) {
                runCatching { field.setAccessible(true) }
            }
            val value = field.get(spec)
            val isRequired = includeRequiredDefaults &&
                    field.getAnnotation(RequiredIniKey::class.java) != null
            if (!isRequired && value == field.get(defaults)) {
                return@forEach
            }
            if (field.name == "mutators") {
                @Suppress("UNCHECKED_CAST")
                encodeMutators(values, value as? kotlin.collections.Map<String, ProjectileMutatorSpec>)
                return@forEach
            }
            val key = keys[field.name] ?: field.name
            if (value is kotlin.collections.Map<*, *> && '{' in key) {
                value.forEach { (placeholder, entry) ->
                    // Android's ICU regex engine requires escaping '}' (unlike the JVM).
                    values[key.replace(Regex("\\{[^}]+\\}"), placeholder.toString())] = entry
                }
            } else {
                values[key] = value
            }
        }
        val warnings = values.keys.mapNotNull { legacyKeyWarning(sectionName, it) }.distinct()
        return IniSpecValues(values, warnings)
    }

    private fun encodeMutators(
        target: MutableMap<String, Any?>,
        mutators: kotlin.collections.Map<String, ProjectileMutatorSpec>?,
    ) {
        mutators.orEmpty().forEach { (name, mutator) ->
            val prefix = "mutator${name}_"
            mutator.ifUnitWithTags?.let { target["${prefix}ifUnitWithTags"] = it }
            mutator.ifUnitWithoutTags?.let { target["${prefix}ifUnitWithoutTags"] = it }
            mutator.directDamageMultiplier?.let { target["${prefix}directDamageMultiplier"] = it }
            mutator.areaDamageMultiplier?.let { target["${prefix}areaDamageMultiplier"] = it }
            mutator.changedExplodeEffect?.let { target["${prefix}changedExplodeEffect"] = it }
            mutator.addResourcesDirectHit?.let { target["${prefix}addResourcesDirectHit"] = it }
            mutator.addResourcesAreaHit?.let { target["${prefix}addResourcesAreaHit"] = it }
        }
    }

    private fun legacyKeyWarning(sectionName: String?, key: String): String? {
        val normalized = key.lowercase()
        return when {
            "memory" in normalized ->
                "Native key '$key' uses legacy untyped memory; prefer synchronized typed extension state"
            sectionName == "core" && normalized == "name" ->
                "Native key 'name' in [core] is overwritten by the unit id; set the unit id instead"
            else -> null
        }
    }
}
