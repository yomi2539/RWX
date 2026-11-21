package io.github.rwx.mod.registry

import com.corrodinggames.rts.game.PlayerTeam
import com.corrodinggames.rts.game.units.actions.ActionId
import com.corrodinggames.rts.game.units.custom.AnimationTag
import com.corrodinggames.rts.gameFramework.GameEngine
import io.github.rwx.geometry.PointF
import io.github.rwx.mod.CommandQueue
import io.github.rwx.mod.api.*
import io.github.rwx.mod.impl.ApiImpl

object TeamActionRegistry : OwnedRegistry {
    const val SYSTEM_ACTION_TYPE = 300

    private val lock = Any()
    private val actions = RegistrationTable<String, TeamActionHandler>("Team action", lock)

    fun register(owner: ApiImpl, id: String, handler: TeamActionHandler) {
        actions.register(owner, id, handler)
    }

    override fun unregister(owner: ApiImpl) {
        synchronized(lock) {
            actions.removeOwned(owner)
            CommandQueue.cancel(owner)
        }
    }

    fun clear() = actions.clear()

    @JvmStatic
    fun request(team: PlayerTeam?, id: String, targetPosition: WorldPosition?): Boolean {
        if (team == null) return false
        synchronized(lock) {
            val owner = actions.owned(id)?.owner ?: return false
            CommandQueue.enqueue(owner) {
                val command = GameEngine.getInstance().commandController.newCommandForTeam(team)
                command.isSystemAction = true
                command.systemActionType = SYSTEM_ACTION_TYPE
                command.actionId = ActionId.intern(id)
                targetPosition?.let { position ->
                    command.targetPoint = PointF(position.x, position.y)
                    command.systemFloat = position.height
                }
            }
        }
        return true
    }

    @JvmStatic
    fun execute(team: PlayerTeam?, actionId: String?, targetPoint: PointF?, targetHeight: Float): Boolean {
        if (team == null || actionId == null) return false
        val handler = actions[actionId] ?: return false
        val targetPosition = targetPoint?.let { WorldPosition(it.x, it.y, targetHeight) }
        handler.execute(TeamActionContextImpl(team, targetPosition))
        return true
    }
}

internal fun PlayerTeam.toTeamState(): TeamState = TeamStateImpl(this)

private open class TeamStateImpl(protected val team: PlayerTeam) : TeamState {
    override val teamId: Int get() = team.teamId

    override fun resource(id: String): Double = when (id) {
        Cost.CREDITS_ID -> team.credits
        else -> 0.0
    }

    override fun hasFlag(id: String): Boolean {
        val tags = team.getTeamAnimationSet() ?: return false
        return AnimationTag.a(AnimationTag.c(id), tags)
    }
}

private class TeamActionContextImpl(
    team: PlayerTeam,
    override val targetPosition: WorldPosition?,
) : TeamStateImpl(team), TeamActionContext {
    override fun trySpend(cost: Cost): Boolean {
        if (cost.resources.keys.any { it != Cost.CREDITS_ID }) return false
        val credits = cost.resources.entries.sumOf { it.value }
        if (team.credits < credits) return false
        team.credits -= credits
        return true
    }

    override fun addResource(id: String, amount: Double) {
        require(id == Cost.CREDITS_ID) { "Runtime custom resources are not supported yet: $id" }
        team.credits += amount
    }

    override fun grantFlag(id: String) {
        team.b(AnimationTag.a(id))
    }

    override fun revokeFlag(id: String) {
        team.c(AnimationTag.a(id))
    }
}
