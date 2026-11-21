package io.github.rwx.mod.api

data class UnitEventBinding(
    val id: String,
    val event: UnitEvent,
    val condition: UnitCondition = UnitCondition.Always,
    val recursionLimit: Int = 1,
    val actions: List<UnitEventAction> = emptyList(),
    val listener: UnitEventHandler? = null,
)

sealed interface UnitEventAction {
    data class SpawnEffect(
        val effectId: String,
        val subject: UnitSubject = UnitSubject.SELF,
    ) : UnitEventAction
}

@UnitDsl
class UnitEventActionBuilder internal constructor() {
    private val actions = mutableListOf<UnitEventAction>()

    fun spawnEffect(effectId: String, at: UnitSubject = UnitSubject.SELF) {
        actions += UnitEventAction.SpawnEffect(effectId, at)
    }

    internal fun build(): List<UnitEventAction> = actions.toList()
}

sealed interface UnitEvent {
    val kind: UnitEventKind

    data object Created : UnitEvent {
        override val kind = UnitEventKind.CREATED
    }

    data object CompleteAndActive : UnitEvent {
        override val kind = UnitEventKind.COMPLETE_AND_ACTIVE
    }

    data object Destroyed : UnitEvent {
        override val kind = UnitEventKind.DESTROYED
    }

    data object KilledUnit : UnitEvent {
        override val kind = UnitEventKind.KILLED_UNIT
    }

    data object QueuedUnitFinished : UnitEvent {
        override val kind = UnitEventKind.QUEUED_UNIT_FINISHED
    }

    data class QueueItemAdded(val actionTags: Set<String> = emptySet()) : UnitEvent {
        override val kind = UnitEventKind.QUEUE_ITEM_ADDED
    }

    data class QueueItemCancelled(val actionTags: Set<String> = emptySet()) : UnitEvent {
        override val kind = UnitEventKind.QUEUE_ITEM_CANCELLED
    }

    data class ActionCompleted(val actionIds: Set<String> = emptySet()) : UnitEvent {
        override val kind = UnitEventKind.ACTION_COMPLETED
    }

    data object Teleported : UnitEvent {
        override val kind = UnitEventKind.TELEPORTED
    }

    data object TouchTargetSuccess : UnitEvent {
        override val kind = UnitEventKind.TOUCH_TARGET_SUCCESS
    }

    data object WaypointGivenByPlayer : UnitEvent {
        override val kind = UnitEventKind.WAYPOINT_GIVEN_BY_PLAYER
    }

    data object TeamChanged : UnitEvent {
        override val kind = UnitEventKind.TEAM_CHANGED
    }

    data object TransportingUnit : UnitEvent {
        override val kind = UnitEventKind.TRANSPORTING_UNIT
    }

    data object TransportRemovedUnit : UnitEvent {
        override val kind = UnitEventKind.TRANSPORT_REMOVED_UNIT
    }

    data class TookDamage(
        val damageTags: Set<String> = emptySet(),
        val projectileTags: Set<String> = emptySet(),
    ) : UnitEvent {
        override val kind = UnitEventKind.TOOK_DAMAGE
    }

    data object EnteredTransport : UnitEvent {
        override val kind = UnitEventKind.ENTERED_TRANSPORT
    }

    data object LeftTransport : UnitEvent {
        override val kind = UnitEventKind.LEFT_TRANSPORT
    }

    data class MessageReceived(val tags: Set<String> = emptySet()) : UnitEvent {
        override val kind = UnitEventKind.MESSAGE_RECEIVED
    }

    data object AttachmentRemoved : UnitEvent {
        override val kind = UnitEventKind.ATTACHMENT_REMOVED
    }
}

enum class UnitEventKind {
    CREATED,
    COMPLETE_AND_ACTIVE,
    DESTROYED,
    KILLED_UNIT,
    QUEUED_UNIT_FINISHED,
    QUEUE_ITEM_ADDED,
    QUEUE_ITEM_CANCELLED,
    ACTION_COMPLETED,
    TELEPORTED,
    TOUCH_TARGET_SUCCESS,
    WAYPOINT_GIVEN_BY_PLAYER,
    TEAM_CHANGED,
    TRANSPORTING_UNIT,
    TRANSPORT_REMOVED_UNIT,
    TOOK_DAMAGE,
    ENTERED_TRANSPORT,
    LEFT_TRANSPORT,
    MESSAGE_RECEIVED,
    ATTACHMENT_REMOVED,
}

fun interface UnitEventHandler {
    fun handle(context: UnitEventContext)
}

data class UnitEventContext(
    val api: Api,
    val tick: Long,
    val self: UnitRuntimeRef,
    val source: UnitRuntimeRef? = null,
    val target: UnitRuntimeRef? = null,
    val event: UnitEventKind,
    val payload: UnitEventPayload = UnitEventPayload.None,
)

data class UnitRuntimeRef(
    val instanceId: Long,
    val definitionId: String?,
    val teamId: Int,
)

sealed interface UnitEventPayload {
    data object None : UnitEventPayload

    data class Damage(
        val amount: Float,
        val damageTags: Set<String> = emptySet(),
        val projectileTags: Set<String> = emptySet(),
    ) : UnitEventPayload

    data class Queue(val actionTags: Set<String> = emptySet()) : UnitEventPayload

    data class Action(
        val actionId: String,
        val targetPosition: WorldPosition? = null,
    ) : UnitEventPayload

    data class Waypoint(
        val commandType: UnitCommandType,
        val targetPosition: WorldPosition? = null,
        val queued: Boolean = false,
    ) : UnitEventPayload

    data class Message(val tags: Set<String> = emptySet()) : UnitEventPayload
}
