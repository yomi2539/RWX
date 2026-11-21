package io.github.rwx.mod.api

interface UnitCommands {
    fun submit(command: UnitCommandRequest): UnitCommandResult

    fun submit(commands: List<UnitCommandRequest>): List<UnitCommandResult> = commands.map(::submit)
}

data class UnitCommandRequest(
    val id: String = "",
    val teamId: Int,
    val unitIds: List<Long>,
    val type: UnitCommandType,
    val target: UnitCommandTarget? = null,
    val queued: Boolean = false,
    val highPriority: Boolean = false,
    val stopCurrentAction: Boolean = false,
    val actionId: String? = null,
    val buildUnitTypeId: String? = null,
    val buildQueueSize: Int = 1,
    val properties: kotlin.collections.Map<String, Any?> = emptyMap(),
) {
    init {
        require(unitIds.isNotEmpty()) { "Unit command requires at least one unit" }
        require(buildQueueSize > 0) { "Build queue size must be positive" }
    }
}

enum class UnitCommandType {
    MOVE,
    ATTACK_MOVE,
    ATTACK_UNIT,
    BUILD,
    REPAIR_UNIT,
    GUARD_UNIT,
    PATROL,
    RECLAIM_UNIT,
    LOAD_INTO_UNIT,
    LOAD_UP_UNIT,
    STOP,
    SET_RALLY,
    CUSTOM_ACTION,
    OTHER,
}

sealed interface UnitCommandTarget {
    data class Point(val position: WorldPosition) : UnitCommandTarget
    data class Unit(val instanceId: Long) : UnitCommandTarget
}

data class UnitCommandResult(
    val commandId: String,
    val accepted: Boolean,
    val message: String? = null,
)
