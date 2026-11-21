package io.github.rwx.mod.registry

import com.corrodinggames.rts.game.Projectile
import com.corrodinggames.rts.game.ProjectileTemplate
import com.corrodinggames.rts.game.units.custom.CustomProjectileTemplate
import io.github.rwx.mod.api.ProjectileInstanceId
import io.github.rwx.mod.api.ProjectileLifecycleContext
import io.github.rwx.mod.api.ProjectileLifecyclePhase
import io.github.rwx.mod.api.ProjectileObserver
import io.github.rwx.mod.impl.ApiImpl

object ProjectileObserverRegistry : OwnedRegistry {
    private data class ActiveProjectile(
        val observerId: String,
        val variantId: String,
        val generation: Int,
        val lastAge: Float,
        val lastRemaining: Float,
    )

    private val observers = RegistrationTable<String, ProjectileObserver>("Projectile observer")
    private val active = linkedMapOf<Long, ActiveProjectile>()
    private val failures = ModFailureLog("Mod projectile observer")

    fun register(owner: ApiImpl, id: String, observer: ProjectileObserver) {
        observers.register(owner, id, observer)
    }

    @JvmStatic
    fun update(projectile: Projectile, projectileTemplate: ProjectileTemplate) {
        val custom = projectileTemplate as? CustomProjectileTemplate ?: return
        val observerId = custom.projectileObserverId ?: return
        val variantId = custom.projectileObserverVariant ?: return
        val observer = observers[observerId] ?: return
        val previous = active[projectile.objectId]
        val restarted = previous != null && (
                previous.observerId != observerId ||
                        previous.variantId != variantId ||
                        projectile.age + RESTART_EPSILON < previous.lastAge
                )
        if (restarted) {
            dispatch(projectile, previous, ProjectileLifecyclePhase.ENDED, observers[previous.observerId])
        }
        val current = if (previous == null || restarted) {
            ActiveProjectile(
                observerId = observerId,
                variantId = variantId,
                generation = (previous?.generation ?: -1) + 1,
                lastAge = projectile.age,
                lastRemaining = projectile.lifeTimer,
            )
        } else {
            previous.copy(lastAge = projectile.age, lastRemaining = projectile.lifeTimer)
        }
        active[projectile.objectId] = current
        dispatch(
            projectile = projectile,
            state = current,
            phase = if (previous == null || restarted) {
                ProjectileLifecyclePhase.STARTED
            } else {
                ProjectileLifecyclePhase.UPDATED
            },
            observer = observer,
            previousAge = if (previous == null || restarted) null else previous.lastAge,
            previousRemaining = if (previous == null || restarted) null else previous.lastRemaining,
        )
    }

    @JvmStatic
    fun end(projectile: Projectile) {
        val state = active.remove(projectile.objectId) ?: return
        dispatch(projectile, state, ProjectileLifecyclePhase.ENDED, observers[state.observerId])
    }

    override fun unregister(owner: ApiImpl) {
        val removedIds = observers.removeOwned(owner).keys
        active.entries.removeAll { it.value.observerId in removedIds }
    }

    private fun dispatch(
        projectile: Projectile,
        state: ActiveProjectile,
        phase: ProjectileLifecyclePhase,
        observer: ProjectileObserver?,
        previousAge: Float? = state.lastAge,
        previousRemaining: Float? = state.lastRemaining,
    ) {
        observer ?: return
        val target = projectile.targetUnit
        val endX = target?.posX ?: projectile.targetX
        val endY = target?.posY ?: projectile.targetY
        failures.runSafely(state.observerId) {
            observer.onLifecycle(
                ProjectileLifecycleContext(
                    instanceId = ProjectileInstanceId(projectile.objectId, state.generation),
                    phase = phase,
                    startX = projectile.posX,
                    startY = projectile.posY,
                    endX = endX,
                    endY = endY,
                    ageTicks = projectile.age.coerceAtLeast(0f),
                    previousAgeTicks = previousAge,
                    remainingTicks = projectile.lifeTimer.coerceAtLeast(0f),
                    previousRemainingTicks = previousRemaining,
                    variantId = state.variantId,
                )
            )
        }
    }

    private const val RESTART_EPSILON = 0.001f
}
