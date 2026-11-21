package io.github.rwx.mod.api

data class WorldPosition(
    val x: Float,
    val y: Float,
    val height: Float = 0f,
) {
    init {
        require(x.isFinite() && y.isFinite() && height.isFinite()) {
            "World position coordinates must be finite"
        }
    }
}

data class UnitRuntimeState(
    val ref: UnitRuntimeRef,
    val position: WorldPosition,
    val rotationDegrees: Float,
    val typeId: String = ref.definitionId.orEmpty(),
    val health: Float = 0f,
    val maxHealth: Float = 0f,
    val shield: Float = 0f,
    val maxShield: Float = 0f,
    val energy: Float = 0f,
    val maxEnergy: Float = 0f,
    val armor: Float = 0f,
    val constructionProgress: Float = 1f,
    val radius: Float = 0f,
    val destroyed: Boolean = false,
    val orderable: Boolean = false,
    val canMove: Boolean? = null,
    val canAttack: Boolean? = null,
    val isBuilding: Boolean = false,
    val isBuilder: Boolean = false,
    val isFactory: Boolean = false,
    val currentTargetId: Long? = null,
    val tags: Set<String> = emptySet(),
    val availableActions: List<UnitAvailableAction> = emptyList(),
    val properties: kotlin.collections.Map<String, Any?> = emptyMap(),
)

data class UnitAvailableAction(
    val actionId: String,
    val displayName: String? = null,
    val actionType: String? = null,
    val unitTypeId: String? = null,
    val unitTypeIsBuilding: Boolean = false,
    val queueSize: Int = 1,
    val available: Boolean = true,
    val queued: Boolean = false,
    val active: Boolean = false,
    val stockpileCount: Int? = null,
    val cooldownRemainingTicks: Int = 0,
    val cooldownRemainingFraction: Float = 0f,
    val properties: kotlin.collections.Map<String, Any?> = emptyMap(),
)

data class UnitQuery(
    val teamId: Int? = null,
    val definitionIds: Set<String> = emptySet(),
    val includeDestroyed: Boolean = false,
    val center: WorldPosition? = null,
    val radius: Float? = null,
) {
    init {
        require(radius == null || center != null) { "Unit query radius requires a center" }
        require(radius == null || radius.isFinite() && radius >= 0f) {
            "Unit query radius must be finite and non-negative"
        }
    }
}

data class UnitSpawnRequest(
    val unitId: String,
    val teamId: Int,
    val position: WorldPosition,
    val rotationDegrees: Float = 0f,
    val initialHealth: Float? = null,
    val constructionProgress: Float = 1f,
) {
    init {
        require(rotationDegrees.isFinite()) { "Unit spawn rotation must be finite" }
        require(initialHealth == null || initialHealth.isFinite() && initialHealth >= 0f) {
            "Unit spawn health must be finite and non-negative"
        }
        require(constructionProgress.isFinite() && constructionProgress in 0f..1f) {
            "Unit spawn construction progress must be between 0 and 1"
        }
    }
}

data class UnitRuntimeUpdate(
    val health: Float? = null,
    val constructionProgress: Float? = null,
) {
    init {
        require(health != null || constructionProgress != null) {
            "Unit runtime update must contain at least one value"
        }
        require(health == null || health.isFinite() && health >= 0f) {
            "Unit health must be finite and non-negative"
        }
        require(constructionProgress == null || constructionProgress.isFinite() && constructionProgress in 0f..1f) {
            "Unit construction progress must be between 0 and 1"
        }
    }
}

interface UnitWorld {
    /** Returns the current live state, or null when the unit is destroyed or no longer tracked. */
    fun state(ref: UnitRuntimeRef): UnitRuntimeState?

    /** Returns matching units ordered by [UnitRuntimeRef.instanceId]. */
    fun query(query: UnitQuery = UnitQuery()): List<UnitRuntimeState>

    /** Returns false when the referenced unit is no longer live. */
    fun teleport(ref: UnitRuntimeRef, destination: WorldPosition): Boolean

    /** Creates one unit declared by this Mod. */
    fun spawn(request: UnitSpawnRequest): UnitRuntimeRef?

    /** Applies primitive runtime values to a live unit. */
    fun update(ref: UnitRuntimeRef, update: UnitRuntimeUpdate): Boolean
}
