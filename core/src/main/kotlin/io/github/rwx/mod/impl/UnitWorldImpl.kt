package io.github.rwx.mod.impl

import com.corrodinggames.rts.game.PlayerTeam
import com.corrodinggames.rts.game.units.BaseUnit
import com.corrodinggames.rts.game.units.OrderableUnit
import com.corrodinggames.rts.game.units.UnitMovementType
import com.corrodinggames.rts.game.units.custom.CustomUnit
import com.corrodinggames.rts.game.units.custom.CustomUnitConfig
import com.corrodinggames.rts.game.units.custom.logic.CustomAction
import com.corrodinggames.rts.game.units.g.SpecialActionBlockEffect
import com.corrodinggames.rts.gameFramework.GameEngine
import io.github.rwx.mod.api.*
import io.github.rwx.mod.isJvmModSimulationAllowed
import java.util.*

internal class UnitWorldImpl(
    private val backend: UnitWorldBackend = EngineUnitWorldBackend(),
) : UnitWorld {
    fun activate(nativeUnits: Map<String, CustomUnitConfig>) {
        (backend as? EngineUnitWorldBackend)?.activate(nativeUnits)
    }

    override fun state(ref: UnitRuntimeRef): UnitRuntimeState? =
        backend.states(includeDestroyed = false).firstOrNull { it.ref.instanceId == ref.instanceId }

    override fun query(query: UnitQuery): List<UnitRuntimeState> {
        val radiusSquared = query.radius?.let { it * it }
        return backend.states(query).asSequence()
            .filter { state -> query.teamId == null || state.ref.teamId == query.teamId }
            .filter { state ->
                query.definitionIds.isEmpty() || state.ref.definitionId in query.definitionIds
            }
            .filter { state ->
                val center = query.center ?: return@filter true
                val limit = radiusSquared ?: return@filter true
                val dx = state.position.x - center.x
                val dy = state.position.y - center.y
                dx * dx + dy * dy <= limit
            }
            .sortedBy { it.ref.instanceId }
            .toList()
    }

    override fun teleport(ref: UnitRuntimeRef, destination: WorldPosition): Boolean {
        if (!isJvmModSimulationAllowed()) return false
        return backend.teleport(ref.instanceId, destination)
    }

    override fun spawn(request: UnitSpawnRequest): UnitRuntimeRef? {
        if (!isJvmModSimulationAllowed()) return null
        return backend.spawn(request)
    }

    override fun update(ref: UnitRuntimeRef, update: UnitRuntimeUpdate): Boolean {
        if (!isJvmModSimulationAllowed()) return false
        return backend.update(ref.instanceId, update)
    }

    internal fun selectedUnits(): List<UnitRuntimeState> = backend.selectedStates()
}

internal interface UnitWorldBackend {
    fun states(includeDestroyed: Boolean): List<UnitRuntimeState>
    fun states(query: UnitQuery): List<UnitRuntimeState> = states(query.includeDestroyed)
    fun selectedStates(): List<UnitRuntimeState> = emptyList()
    fun teleport(instanceId: Long, destination: WorldPosition): Boolean
    fun spawn(request: UnitSpawnRequest): UnitRuntimeRef?
    fun update(instanceId: Long, update: UnitRuntimeUpdate): Boolean
}

private class EngineUnitWorldBackend : UnitWorldBackend {
    private val definitionIds = IdentityHashMap<CustomUnitConfig, String>()
    private val nativeUnits = linkedMapOf<String, CustomUnitConfig>()

    fun activate(nativeUnits: Map<String, CustomUnitConfig>) {
        definitionIds.clear()
        this.nativeUnits.clear()
        this.nativeUnits.putAll(nativeUnits)
        nativeUnits.forEach { (definitionId, nativeType) -> definitionIds[nativeType] = definitionId }
    }

    override fun states(includeDestroyed: Boolean): List<UnitRuntimeState> {
        val liveUnits = BaseUnit.bE
        val units = liveUnits.a()
        return buildList(liveUnits.size) {
            for (index in 0 until liveUnits.size) {
                val unit = units[index] ?: continue
                if (includeDestroyed || !unit.isDestroyed) add(unit.toRuntimeState())
            }
        }
    }

    override fun states(query: UnitQuery): List<UnitRuntimeState> {
        val radiusSquared = query.radius?.let { it * it }
        val liveUnits = BaseUnit.bE
        val units = liveUnits.a()
        return buildList {
            for (index in 0 until liveUnits.size) {
                val unit = units[index] ?: continue
                if (!query.includeDestroyed && unit.isDestroyed) continue
                if (query.teamId != null && unit.team?.teamId != query.teamId) continue
                if (query.definitionIds.isNotEmpty() && unit.modDefinitionId() !in query.definitionIds) continue
                if (radiusSquared != null) {
                    val center = requireNotNull(query.center)
                    val dx = unit.posX - center.x
                    val dy = unit.posY - center.y
                    if (dx * dx + dy * dy > radiusSquared) continue
                }
                add(unit.toRuntimeState())
            }
        }
    }

    override fun selectedStates(): List<UnitRuntimeState> {
        val selectedUnits = GameEngine.getInstance().gameUI?.selectedUnitsList ?: return emptyList()
        val units = selectedUnits.a()
        return buildList(selectedUnits.size) {
            for (index in 0 until selectedUnits.size) {
                val unit = units[index] ?: continue
                if (unit.isSelected && !unit.isDestroyed) add(unit.toRuntimeState())
            }
        }
    }

    override fun teleport(instanceId: Long, destination: WorldPosition): Boolean {
        val liveUnits = BaseUnit.bE
        val units = liveUnits.a()
        for (index in 0 until liveUnits.size) {
            val unit = units[index] ?: continue
            if (unit.objectId != instanceId || unit.isDestroyed) continue
            unit.posZ = destination.height
            unit.f(destination.x, destination.y)
            return true
        }
        return false
    }

    override fun spawn(request: UnitSpawnRequest): UnitRuntimeRef? {
        val nativeType = nativeUnits[request.unitId] ?: return null
        val team = PlayerTeam.k(request.teamId) ?: return null
        val unit = nativeType.a()
        unit.posX = request.position.x
        unit.posY = request.position.y
        unit.posZ = request.position.height
        unit.h(request.rotationDegrees)
        request.initialHealth?.let { unit.currentHealth = it.coerceAtMost(unit.maxHealth) }
        unit.buildProgress = request.constructionProgress
        unit.paidBuildProgress = request.constructionProgress
        unit.setUnitTeam(team)
        unit.setCommandTargetUnit(null)
        if (unit is OrderableUnit) {
            unit.br()
            if (unit.bI()) GameEngine.getInstance().pathfindingEngine.a(unit)
        }
        PlayerTeam.c(unit)
        return UnitRuntimeRef(
            instanceId = unit.objectId,
            definitionId = request.unitId,
            teamId = request.teamId,
        )
    }

    override fun update(instanceId: Long, update: UnitRuntimeUpdate): Boolean {
        val unit = findLiveUnit(instanceId) ?: return false
        update.health?.let { unit.currentHealth = it.coerceAtMost(unit.maxHealth) }
        update.constructionProgress?.let { progress ->
            unit.r(progress)
            unit.paidBuildProgress = progress
        }
        return true
    }

    private fun findLiveUnit(instanceId: Long): BaseUnit? {
        val liveUnits = BaseUnit.bE
        val units = liveUnits.a()
        for (index in 0 until liveUnits.size) {
            val unit = units[index] ?: continue
            if (unit.objectId == instanceId && !unit.isDestroyed) return unit
        }
        return null
    }

    private fun BaseUnit.toRuntimeState(): UnitRuntimeState {
        val definitionId = modDefinitionId()
        val unitType = r()
        val orderableUnit = this as? OrderableUnit
        val availableActions = orderableUnit?.toAvailableActions().orEmpty()
        return UnitRuntimeState(
            ref = UnitRuntimeRef(
                instanceId = objectId,
                definitionId = definitionId,
                teamId = team?.teamId ?: -1,
            ),
            position = WorldPosition(posX, posY, posZ),
            rotationDegrees = rotationSpeed,
            typeId = unitType.v(),
            health = currentHealth,
            maxHealth = maxHealth,
            shield = shield,
            maxShield = unitEnergyMax,
            energy = currentEnergy,
            maxEnergy = orderableUnit?.bd() ?: 0f,
            armor = (this as? CustomUnit)?.y?.armour ?: unitArmor,
            constructionProgress = buildProgress,
            radius = radius,
            destroyed = isDestroyed,
            orderable = orderableUnit != null,
            canMove = getMovementType() != UnitMovementType.NONE,
            canAttack = canAttack(),
            isBuilding = unitType.isBuildingUnit(),
            isBuilder = availableActions.any(UnitAvailableAction::unitTypeIsBuilding),
            isFactory = availableActions.any { it.unitTypeId != null && !it.unitTypeIsBuilding },
            currentTargetId = attackTargetUnit?.objectId,
            tags = getTags()?.a
                ?.map { nativeTag -> nativeTag.toString() }
                ?.toSet()
                .orEmpty(),
            availableActions = availableActions,
        )
    }

    private fun BaseUnit.modDefinitionId(): String? =
        (this as? CustomUnit)?.let { definitionIds[it.unitConfig] }

    private fun OrderableUnit.toAvailableActions(): List<UnitAvailableAction> =
        availableActions.map { action ->
            val unitType = action.attachedUnitType ?: action.getUnitType()
            val cooldown = SpecialActionBlockEffect.b(this, action.actionId)
            val cooldownRemainingTicks = cooldown?.d()?.coerceAtLeast(0) ?: 0
            UnitAvailableAction(
                actionId = action.getActionIdString(),
                displayName = runCatching { action.getDisplayName() }.getOrNull(),
                actionType = runCatching { action.getActionType().name }.getOrNull(),
                unitTypeId = unitType?.v(),
                unitTypeIsBuilding = unitType?.isBuildingUnit() ?: false,
                queueSize = action.queueSize,
                available = cooldownRemainingTicks == 0 && !action.isNotAvailable(this) && action.b(this),
                queued = action.isQueuable,
                active = action.isActivated(this),
                stockpileCount = (action as? CustomAction)
                    ?.takeIf { it.actionDef.displayRemainingStockpile }
                    ?.getActiveCount(this, true)
                    ?.coerceAtLeast(0),
                cooldownRemainingTicks = cooldownRemainingTicks,
                cooldownRemainingFraction = cooldown?.c()?.coerceIn(0f, 1f) ?: 0f,
            )
        }
}
