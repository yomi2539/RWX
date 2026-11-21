package io.github.rwx.mod

import com.corrodinggames.rts.game.units.BaseUnit
import com.corrodinggames.rts.game.units.UnitCommand
import com.corrodinggames.rts.game.units.custom.*
import com.corrodinggames.rts.game.units.custom.logic.CustomActionDef
import com.corrodinggames.rts.gameFramework.GameEngine
import io.github.rwx.logger
import io.github.rwx.mod.api.*
import io.github.rwx.mod.impl.ApiImpl
import java.util.IdentityHashMap
import kotlin.collections.ArrayDeque
import kotlin.math.sqrt
import com.corrodinggames.rts.game.units.UnitCommandType as NativeUnitCommandType

internal fun UnitDefinition.nativeActionIdsByNativeId(
    resolveNativeId: (displayName: String) -> String?,
): Map<String, String> =
    iniSections.mapNotNull { section ->
        when {
            section.name.startsWith("action_") -> section.name.removePrefix("action_")
            section.name.startsWith("hiddenAction_") -> section.name.removePrefix("hiddenAction_")
            else -> null
        }?.let { displayName ->
            resolveNativeId(displayName)?.let { nativeId -> nativeId to displayName }
        }
    }.toMap()

internal fun CustomActionDef.nativeActionId(): String =
    name ?: "${stringId.orEmpty()}_$id"

/** Deterministic simulation-thread dispatcher for typed JVM unit events. */
object UnitEventRuntime {
    private const val MAX_WORK_ITEMS_PER_TICK = 4096

    private val registrations = IdentityHashMap<CustomUnitConfig, Registration>()
    private val queue = ArrayDeque<QueuedEvent>()
    private val reportedFailures = hashSetOf<BindingKey>()
    private val unitBirthTicks = hashMapOf<Long, Long>()
    private var activePath: Map<BindingKey, Int> = emptyMap()

    internal fun activate(
        api: ApiImpl,
        nativeUnits: Map<String, CustomUnitConfig>,
        definitions: List<UnitDefinition>,
    ) {
        val definitionsById = definitions.associateBy { it.id }
        val prepared = nativeUnits.map { (unitId, nativeType) ->
            val definition = checkNotNull(definitionsById[unitId]) {
                "Missing JVM unit declaration for activated native unit $unitId"
            }
            val effectTemplates = referencedEffects(definition).associateWith { effectId ->
                nativeType.resolveEffect("CUSTOM:$effectId")
            }
            nativeType to Registration(api, definition, nativeType, effectTemplates)
        }
        registrations.entries.removeIf { it.value.api === api }
        prepared.forEach { (nativeType, registration) -> registrations[nativeType] = registration }
    }

    @JvmStatic
    fun unregister(api: ApiImpl) {
        registrations.entries.removeIf { it.value.api === api }
    }

    @JvmStatic
    fun clear() {
        registrations.clear()
        queue.clear()
        reportedFailures.clear()
        unitBirthTicks.clear()
        activePath = emptyMap()
    }

    @JvmStatic
    fun enqueue(
        self: CustomUnit,
        nativeEvent: UnitEventType,
        relatedUnit: BaseUnit?,
        nativeTags: AnimationSet?,
    ) {
        if (!acceptsEvents()) return
        if (nativeEvent == UnitEventType.tookDamage || nativeEvent == UnitEventType.newWaypointGivenByPlayer) return
        val kind = nativeEvent.toApiKind()
        val (source, target) = routeRelatedUnit(kind, relatedUnit)
        val tags = nativeTags.toApiTags()
        val payload = when (kind) {
            UnitEventKind.QUEUE_ITEM_ADDED,
            UnitEventKind.QUEUE_ITEM_CANCELLED -> UnitEventPayload.Queue(tags)

            UnitEventKind.MESSAGE_RECEIVED -> UnitEventPayload.Message(tags)
            else -> UnitEventPayload.None
        }
        enqueue(self, kind, source, target, payload)
    }

    @JvmStatic
    fun enqueueDamage(
        self: CustomUnit,
        source: BaseUnit?,
        projectileTags: AnimationSet?,
        amount: Float,
    ) {
        if (!acceptsEvents()) return
        val tags = projectileTags.toApiTags()
        enqueue(
            self = self,
            kind = UnitEventKind.TOOK_DAMAGE,
            source = source,
            target = null,
            payload = UnitEventPayload.Damage(
                amount = amount,
                damageTags = tags,
                projectileTags = tags,
            ),
        )
    }

    @JvmStatic
    fun enqueueWaypoint(self: CustomUnit, command: UnitCommand) {
        if (!acceptsEvents()) return
        val target = command.targetUnit
        enqueue(
            self = self,
            kind = UnitEventKind.WAYPOINT_GIVEN_BY_PLAYER,
            source = null,
            target = target,
            payload = UnitEventPayload.Waypoint(
                commandType = command.commandType.toApiCommandType(),
                targetPosition = if (target == null) {
                    WorldPosition(command.targetX, command.targetY)
                } else {
                    null
                },
                queued = command.isQueued,
            ),
        )
    }

    @JvmStatic
    fun enqueueActionCompleted(
        self: CustomUnit,
        actionUnitConfig: CustomUnitConfig,
        nativeActionId: String,
        targetX: Float?,
        targetY: Float?,
        target: BaseUnit?,
    ) {
        if (!acceptsEvents()) return
        val registration = registrations[actionUnitConfig] ?: return
        val actionId = registration.actionIdsByNativeId[nativeActionId] ?: return
        enqueue(
            registration = registration,
            self = self,
            kind = UnitEventKind.ACTION_COMPLETED,
            source = null,
            target = target,
            payload = UnitEventPayload.Action(
                actionId = actionId,
                targetPosition = if (targetX != null && targetY != null) {
                    WorldPosition(targetX, targetY)
                } else {
                    null
                },
            ),
        )
    }

    @JvmStatic
    fun drain() {
        if (!acceptsEvents()) {
            queue.clear()
            activePath = emptyMap()
            return
        }
        if (queue.isEmpty()) return
        val policy = UnitEventDispatchPolicy<BindingKey>(MAX_WORK_ITEMS_PER_TICK)
        while (queue.isNotEmpty()) {
            if (!policy.consumeWorkItem()) {
                reportOverflow()
                return
            }
            val event = queue.removeFirst()
            if (event.self.pendingPortalTransfer != null || event.source?.pendingPortalTransfer != null ||
                event.target?.pendingPortalTransfer != null
            ) continue
            val registration = event.registration
            val selfState = NativeUnitState(event.self)
            val sourceState = event.source?.let(::NativeUnitState)
            val targetState = event.target?.let(::NativeUnitState)
            val evaluationContext = UnitConditionEvaluationContext(selfState, sourceState, targetState)

            registration.definition.eventHandlers.forEach { binding ->
                if (!UnitEventMatcher.matches(binding.event, event.kind, event.payload)) return@forEach
                if (!UnitConditionEvaluator.evaluate(binding.condition, evaluationContext)) return@forEach
                val key = BindingKey(registration.api.manifest.id, registration.definition.id, binding.id)
                val bindingPath = policy.enterBinding(event.path, key, binding.recursionLimit) ?: return@forEach
                if (!policy.consumeWorkItem()) {
                    reportOverflow()
                    return
                }
                executeBinding(
                    registration = registration,
                    binding = binding,
                    event = event,
                    key = key,
                    bindingPath = bindingPath,
                    context = UnitEventContext(
                        api = registration.api,
                        tick = event.tick,
                        self = event.self.toRuntimeRef(registration.definition.id),
                        source = event.source?.toRuntimeRef(),
                        target = event.target?.toRuntimeRef(),
                        event = event.kind,
                        payload = event.payload,
                    ),
                )
            }
            if (event.kind == UnitEventKind.DESTROYED) {
                unitBirthTicks.remove(event.self.objectId)
            }
        }
    }

    private fun enqueue(
        self: CustomUnit,
        kind: UnitEventKind,
        source: BaseUnit?,
        target: BaseUnit?,
        payload: UnitEventPayload,
    ) {
        if (!acceptsEvents()) return
        val registration = registrations[self.unitConfig] ?: return
        enqueue(registration, self, kind, source, target, payload)
    }

    private fun acceptsEvents(): Boolean =
        registrations.isNotEmpty() &&
                isJvmModSimulationAllowed()

    private fun enqueue(
        registration: Registration,
        self: CustomUnit,
        kind: UnitEventKind,
        source: BaseUnit?,
        target: BaseUnit?,
        payload: UnitEventPayload,
    ) {
        // Ordinary destruction still emits events; transfer disposal has an explicit pending marker.
        if (self.pendingPortalTransfer != null || source?.pendingPortalTransfer != null ||
            target?.pendingPortalTransfer != null
        ) return
        val tick = GameEngine.getInstance().currentTick.toLong()
        unitBirthTicks.putIfAbsent(self.objectId, tick)
        queue.addLast(
            QueuedEvent(
                tick = tick,
                registration = registration,
                self = self,
                source = source,
                target = target,
                kind = kind,
                payload = payload,
                path = activePath,
            )
        )
    }

    private fun executeBinding(
        registration: Registration,
        binding: UnitEventBinding,
        event: QueuedEvent,
        key: BindingKey,
        bindingPath: Map<BindingKey, Int>,
        context: UnitEventContext,
    ) {
        val previousPath = activePath
        activePath = bindingPath
        try {
            binding.actions.forEach { action -> executeAction(registration, action, event) }
            binding.listener?.handle(context)
        } catch (error: Throwable) {
            val message = "JVM mod ${registration.api.manifest.id}: unit event binding " +
                    "${registration.definition.id}/${binding.id} failed"
            logger.error(error) { message }
            if (reportedFailures.add(key)) {
                registration.api.reportUnitEventFailure("$message: ${error.message ?: error.javaClass.simpleName}")
            }
        } finally {
            activePath = previousPath
        }
    }

    private fun executeAction(registration: Registration, action: UnitEventAction, event: QueuedEvent) {
        when (action) {
            is UnitEventAction.SpawnEffect -> {
                val subject = event.subject(action.subject) ?: return
                val template = checkNotNull(registration.effectTemplates[action.effectId]) {
                    "Missing native effect ${action.effectId}"
                }
                spawn(template, subject)
            }
        }
    }

    private fun spawn(template: EffectTemplate, subject: BaseUnit) {
        template.a(
            subject.posX,
            subject.posY,
            subject.posZ,
            subject.rotationSpeed,
            subject,
            0,
            0,
        )
    }

    private fun reportOverflow() {
        val dropped = queue.size
        queue.clear()
        activePath = emptyMap()
        logger.error {
            "JVM unit event dispatcher exceeded $MAX_WORK_ITEMS_PER_TICK work items in one tick; " +
                    "dropped $dropped queued events"
        }
    }

    private fun referencedEffects(definition: UnitDefinition): Set<String> =
        definition.eventHandlers.flatMap { binding ->
            binding.actions.mapNotNull { action ->
                when (action) {
                    is UnitEventAction.SpawnEffect -> action.effectId
                }
            }
        }.toSet()

    private fun routeRelatedUnit(kind: UnitEventKind, related: BaseUnit?): Pair<BaseUnit?, BaseUnit?> = when (kind) {
        UnitEventKind.KILLED_UNIT,
        UnitEventKind.QUEUED_UNIT_FINISHED,
        UnitEventKind.TOUCH_TARGET_SUCCESS,
        UnitEventKind.TRANSPORTING_UNIT,
        UnitEventKind.TRANSPORT_REMOVED_UNIT -> null to related

        UnitEventKind.ENTERED_TRANSPORT,
        UnitEventKind.LEFT_TRANSPORT,
        UnitEventKind.MESSAGE_RECEIVED,
        UnitEventKind.ATTACHMENT_REMOVED -> related to null

        else -> related to null
    }

    private fun BaseUnit.toRuntimeRef(explicitDefinitionId: String? = null): UnitRuntimeRef {
        val definitionId =
            explicitDefinitionId ?: (this as? CustomUnit)?.let { registrations[it.unitConfig]?.definition?.id }
        return UnitRuntimeRef(
            instanceId = objectId,
            definitionId = definitionId,
            teamId = team?.teamId ?: -1,
        )
    }

    private fun QueuedEvent.subject(subject: UnitSubject): BaseUnit? = when (subject) {
        UnitSubject.SELF -> self
        UnitSubject.SOURCE -> source
        UnitSubject.TARGET -> target
    }

    private fun AnimationSet?.toApiTags(): Set<String> = this?.a
        ?.map { nativeTag -> nativeTag.toString() }
        ?.toSet()
        .orEmpty()

    private fun UnitEventType.toApiKind(): UnitEventKind = when (this) {
        UnitEventType.created -> UnitEventKind.CREATED
        UnitEventType.completeAndActive -> UnitEventKind.COMPLETE_AND_ACTIVE
        UnitEventType.destroyed -> UnitEventKind.DESTROYED
        UnitEventType.killedAnyUnit -> UnitEventKind.KILLED_UNIT
        UnitEventType.queuedUnitFinished -> UnitEventKind.QUEUED_UNIT_FINISHED
        UnitEventType.queueItemAdded -> UnitEventKind.QUEUE_ITEM_ADDED
        UnitEventType.queueItemCancelled -> UnitEventKind.QUEUE_ITEM_CANCELLED
        UnitEventType.teleported -> UnitEventKind.TELEPORTED
        UnitEventType.touchTargetSuccess -> UnitEventKind.TOUCH_TARGET_SUCCESS
        UnitEventType.newWaypointGivenByPlayer -> UnitEventKind.WAYPOINT_GIVEN_BY_PLAYER
        UnitEventType.teamChanged -> UnitEventKind.TEAM_CHANGED
        UnitEventType.transportingNewUnit -> UnitEventKind.TRANSPORTING_UNIT
        UnitEventType.transportUnloadedOrRemovedUnit -> UnitEventKind.TRANSPORT_REMOVED_UNIT
        UnitEventType.tookDamage -> UnitEventKind.TOOK_DAMAGE
        UnitEventType.enteredTransport -> UnitEventKind.ENTERED_TRANSPORT
        UnitEventType.leftTransport -> UnitEventKind.LEFT_TRANSPORT
        UnitEventType.newMessage -> UnitEventKind.MESSAGE_RECEIVED
        UnitEventType.attachmentRemoved -> UnitEventKind.ATTACHMENT_REMOVED
    }

    private fun NativeUnitCommandType.toApiCommandType(): UnitCommandType = when (this) {
        NativeUnitCommandType.move -> UnitCommandType.MOVE
        NativeUnitCommandType.attackMove -> UnitCommandType.ATTACK_MOVE
        NativeUnitCommandType.attack -> UnitCommandType.ATTACK_UNIT
        NativeUnitCommandType.build -> UnitCommandType.BUILD
        NativeUnitCommandType.repair -> UnitCommandType.REPAIR_UNIT
        NativeUnitCommandType.guard,
        NativeUnitCommandType.guardAt,
        NativeUnitCommandType.follow -> UnitCommandType.GUARD_UNIT

        NativeUnitCommandType.patrol -> UnitCommandType.PATROL
        NativeUnitCommandType.reclaim -> UnitCommandType.RECLAIM_UNIT
        NativeUnitCommandType.loadInto -> UnitCommandType.LOAD_INTO_UNIT
        NativeUnitCommandType.loadUp -> UnitCommandType.LOAD_UP_UNIT
        NativeUnitCommandType.triggerAction,
        NativeUnitCommandType.triggerActionWhenInRange -> UnitCommandType.CUSTOM_ACTION

        NativeUnitCommandType.unloadAt,
        NativeUnitCommandType.touchTarget,
        NativeUnitCommandType.setPassiveTarget -> UnitCommandType.OTHER
    }

    private data class Registration(
        val api: ApiImpl,
        val definition: UnitDefinition,
        val nativeType: CustomUnitConfig,
        val effectTemplates: Map<String, EffectTemplate>,
    ) {
        val actionIdsByNativeId: Map<String, String> = definition.nativeActionIdsByNativeId { displayName ->
            nativeType.findCustomActionDefByDisplayName(displayName)?.nativeActionId()
        }
    }

    private data class QueuedEvent(
        val tick: Long,
        val registration: Registration,
        val self: CustomUnit,
        val source: BaseUnit?,
        val target: BaseUnit?,
        val kind: UnitEventKind,
        val payload: UnitEventPayload,
        val path: Map<BindingKey, Int>,
    )

    private data class BindingKey(
        val modId: String,
        val definitionId: String,
        val bindingId: String,
    )

    private class NativeUnitState(private val unit: BaseUnit) : UnitConditionState {
        override fun number(property: UnitNumberProperty): Float = when (property) {
            UnitNumberProperty.HEALTH -> unit.currentHealth
            UnitNumberProperty.MAX_HEALTH -> unit.maxHealth
            UnitNumberProperty.HEALTH_RATIO -> ratio(unit.currentHealth, unit.maxHealth)
            UnitNumberProperty.SHIELD -> unit.shield
            UnitNumberProperty.MAX_SHIELD -> unit.unitEnergyMax
            UnitNumberProperty.SHIELD_RATIO -> ratio(unit.shield, unit.unitEnergyMax)
            UnitNumberProperty.ENERGY -> (unit as? CustomUnit)?.currentEnergy ?: unit.currentEnergy
            UnitNumberProperty.MAX_ENERGY -> (unit as? CustomUnit)?.y?.maxEnergy ?: unit.unitEnergyMax
            UnitNumberProperty.ENERGY_RATIO -> {
                val current = (unit as? CustomUnit)?.currentEnergy ?: unit.currentEnergy
                val maximum = (unit as? CustomUnit)?.y?.maxEnergy ?: unit.unitEnergyMax
                ratio(current, maximum)
            }

            UnitNumberProperty.X -> unit.posX
            UnitNumberProperty.Y -> unit.posY
            UnitNumberProperty.SPEED -> sqrt(unit.velocityX * unit.velocityX + unit.velocityY * unit.velocityY)
            UnitNumberProperty.AGE_TICKS -> {
                val currentTick = GameEngine.getInstance().currentTick.toLong()
                val birthTick = unitBirthTicks[unit.objectId]
                if (birthTick != null) {
                    (currentTick - birthTick).coerceAtLeast(0L).toFloat()
                } else {
                    val ageMillis = (GameEngine.getInstance().gameTimeMillis - unit.unitCreationTime).coerceAtLeast(0L)
                    ageMillis / MILLIS_PER_TICK
                }
            }
        }

        override fun boolean(property: UnitBooleanProperty): Boolean = when (property) {
            UnitBooleanProperty.DESTROYED -> unit.isDestroyed
            UnitBooleanProperty.MOVING -> unit.velocityX != 0f || unit.velocityY != 0f
            UnitBooleanProperty.ATTACKING -> unit.attackTargetUnit != null
            UnitBooleanProperty.BUILD_COMPLETE -> unit.buildProgress >= 1f
        }

        override fun hasTag(tag: String): Boolean =
            unit.getTags()?.a?.any { it.toString() == tag.lowercase() } == true

        private fun ratio(value: Float, maximum: Float): Float = if (maximum > 0f) value / maximum else 0f
    }

    private const val MILLIS_PER_TICK = 1000f / 60f
}

internal class UnitEventDispatchPolicy<K>(private val maxWorkItems: Int) {
    private var workItems = 0

    fun consumeWorkItem(): Boolean = ++workItems <= maxWorkItems

    fun enterBinding(path: Map<K, Int>, key: K, recursionLimit: Int): Map<K, Int>? {
        val depth = path[key] ?: 0
        if (depth > recursionLimit) return null
        return path + (key to depth + 1)
    }
}
