package io.github.rwx.mod.impl

import com.corrodinggames.rts.game.PlayerTeam
import com.corrodinggames.rts.game.units.BaseUnit
import com.corrodinggames.rts.game.units.OrderableUnit
import com.corrodinggames.rts.game.units.UnitType
import com.corrodinggames.rts.game.units.UnitTypeEnum
import com.corrodinggames.rts.game.units.actions.ActionId
import com.corrodinggames.rts.game.units.custom.CustomUnitConfig
import com.corrodinggames.rts.gameFramework.GameEngine
import io.github.rwx.geometry.PointF
import io.github.rwx.mod.CommandQueue
import io.github.rwx.mod.api.*

internal class UnitCommandsImpl(
    private val backend: UnitCommandBackend,
) : UnitCommands {
    constructor(owner: Any) : this(EngineUnitCommandBackend(owner))

    override fun submit(command: UnitCommandRequest): UnitCommandResult = backend.submit(command)
}

internal fun interface UnitCommandBackend {
    fun submit(command: UnitCommandRequest): UnitCommandResult
}

private class EngineUnitCommandBackend(
    private val owner: Any,
) : UnitCommandBackend {
    override fun submit(command: UnitCommandRequest): UnitCommandResult {
        val team = PlayerTeam.k(command.teamId)
            ?: return command.rejected("Unknown team: ${command.teamId}")
        val units = ArrayList<OrderableUnit>(command.unitIds.size)
        command.unitIds.distinct().forEach { instanceId ->
            val unit = findUnit(instanceId) as? OrderableUnit
                ?: return command.rejected("Orderable unit not found: ${instanceId}")
            if (unit.isTransferUnavailable) return command.rejected("Unit is unavailable: ${instanceId}")
            if (unit.team?.teamId != command.teamId) {
                return command.rejected("Unit ${instanceId} is not on team ${command.teamId}")
            }
            units += unit
        }

        val unitTarget = command.target as? UnitCommandTarget.Unit
        val point = (command.target as? UnitCommandTarget.Point)?.position
        val targetUnit = unitTarget?.instanceId?.let(::findUnit)
        val buildType = command.buildUnitTypeId?.let(::findUnitType)
        if (unitTarget != null && targetUnit == null) {
            return command.rejected("Target unit not found: ${unitTarget.instanceId}")
        }
        when (command.type) {
            UnitCommandType.MOVE,
            UnitCommandType.ATTACK_MOVE,
            UnitCommandType.BUILD,
            UnitCommandType.PATROL,
            UnitCommandType.SET_RALLY -> if (point == null) return command.rejected("Point target required")

            UnitCommandType.ATTACK_UNIT,
            UnitCommandType.REPAIR_UNIT,
            UnitCommandType.GUARD_UNIT,
            UnitCommandType.RECLAIM_UNIT,
            UnitCommandType.LOAD_INTO_UNIT,
            UnitCommandType.LOAD_UP_UNIT -> if (targetUnit == null) return command.rejected("Unit target required")

            UnitCommandType.CUSTOM_ACTION -> if (command.actionId.isNullOrBlank()) {
                return command.rejected("Custom action id required")
            }

            UnitCommandType.OTHER -> return command.rejected("Unsupported command type: OTHER")
            UnitCommandType.STOP -> Unit
        }
        if (command.type == UnitCommandType.BUILD && buildType == null) {
            return command.rejected("Unknown build unit type: ${command.buildUnitTypeId}")
        }

        CommandQueue.enqueue(owner) {
            if (units.any { it.isTransferUnavailable } || targetUnit?.isTransferUnavailable == true) return@enqueue
            val native = GameEngine.getInstance().commandController.createCommandForTeam(team)
            native.isQueued = command.queued
            native.isHighPriority = command.highPriority
            native.stopCurrentAction = command.stopCurrentAction
            units.forEach(native::addUnitToCommand)
            when (command.type) {
                UnitCommandType.MOVE -> native.setMoveTarget(point!!.x, point.y)
                UnitCommandType.ATTACK_MOVE -> native.setAttackMoveTarget(point!!.x, point.y)
                UnitCommandType.ATTACK_UNIT -> native.setAttackTarget(targetUnit)
                UnitCommandType.BUILD -> native.setBuildTarget(point!!.x, point.y, buildType, command.buildQueueSize)
                UnitCommandType.REPAIR_UNIT -> native.setRepairTarget(targetUnit)
                UnitCommandType.GUARD_UNIT -> native.setGuardTarget(targetUnit)
                UnitCommandType.PATROL -> native.setPatrolTarget(point!!.x, point.y)
                UnitCommandType.RECLAIM_UNIT -> native.setReclaimTarget(targetUnit)
                UnitCommandType.LOAD_INTO_UNIT -> native.setLoadIntoTarget(targetUnit)
                UnitCommandType.LOAD_UP_UNIT -> native.setLoadUpTarget(targetUnit)
                UnitCommandType.STOP -> native.setClearExistingOrders()
                UnitCommandType.SET_RALLY -> native.setRallyPoint(PointF(point!!.x, point.y))
                UnitCommandType.CUSTOM_ACTION -> native.setActionTarget(
                    ActionId.intern(command.actionId),
                    point?.let { PointF(it.x, it.y) },
                    targetUnit,
                )

                UnitCommandType.OTHER -> error("Validated above")
            }
        }
        return command.accepted()
    }

    private fun findUnit(instanceId: Long): BaseUnit? {
        val units = BaseUnit.bE.a()
        for (index in BaseUnit.bE.indices) {
            val unit = units[index] ?: continue
            if (unit.objectId == instanceId) return unit
        }
        return null
    }

    private fun findUnitType(typeId: String): UnitType? =
        UnitTypeEnum.getUnitTypeByName(typeId) ?: CustomUnitConfig.getUnitTypeByName(typeId)

    private fun UnitCommandRequest.accepted() = UnitCommandResult(id, true)

    private fun UnitCommandRequest.rejected(message: String) = UnitCommandResult(id, false, message)
}
