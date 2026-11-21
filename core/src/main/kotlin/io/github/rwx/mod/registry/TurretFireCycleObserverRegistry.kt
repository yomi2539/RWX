package io.github.rwx.mod.registry

import com.corrodinggames.rts.game.units.custom.CustomUnit
import com.corrodinggames.rts.game.units.custom.TurretConfig
import io.github.rwx.mod.api.TurretFireCycleContext
import io.github.rwx.mod.api.TurretFireCycleInstanceId
import io.github.rwx.mod.api.TurretFireCycleObserver
import io.github.rwx.mod.api.TurretFireCyclePhase
import io.github.rwx.mod.impl.ApiImpl

/**
 * Dispatches the two windows either side of a turret's shot: pre-fire wind-up and
 * post-fire recovery.
 *
 * Both are derived from state the engine already keeps per turret, so nothing here advances
 * the simulation — a peer that never loads the mod stays in lockstep with one that does:
 *
 *  - pre-fire reads `movementLevels[i].speed`, the warm-up counter climbing to `warmup`;
 *  - post-fire reads `movementLevels[i].rotation`, the reload counter set to the full reload
 *    delay at the instant of the shot and decaying to zero.
 *
 * The windows are keyed by turret slot rather than by projectile, because a turret that fires
 * no projectile (or several) still has exactly one cycle.
 */
object TurretFireCycleObserverRegistry : OwnedRegistry {
    private data class InstanceKey(val sourceObjectId: Long, val turretIndex: Int)

    /** Which side of the shot a window sits on; each runs its own STARTED/UPDATED/ENDED. */
    private enum class Window { PRE_FIRE, POST_FIRE }

    private data class Snapshot(
        val observerId: String,
        val variantId: String,
        val generation: Int,
        val startX: Float,
        val startY: Float,
        val endX: Float,
        val endY: Float,
        val age: Float,
        val remaining: Float,
        val duration: Float,
    )

    private val observers =
        RegistrationTable<String, TurretFireCycleObserver>("Turret fire cycle observer")
    private val active = linkedMapOf<Pair<InstanceKey, Window>, Snapshot>()
    private val generations = mutableMapOf<InstanceKey, Int>()
    private val failures = ModFailureLog("Mod turret fire cycle observer")

    /**
     * Where each turret was last aiming, in world space.
     *
     * Recoil has to play out against the point the shot was taken at, which the turret itself
     * stops reporting the moment it re-aims or its target dies. Recorded while a target is
     * live, then read back when the post-fire window opens.
     *
     * Concurrent because the simulation writes it while [aimPointOf] is read from the render
     * pass; every other map here is only ever touched on the simulation thread.
     */
    private val aimPoints = java.util.concurrent.ConcurrentHashMap<InstanceKey, AimPoint>()

    /** A world-space aim point, before any per-consumer height or viewpoint adjustment. */
    data class AimPoint(val x: Float, val y: Float, val z: Float)

    /**
     * The point [turretIndex] of [sourceObjectId] was aiming at when it last had a target,
     * or null if it never has. Lets the renderer draw recoil against exactly the point the
     * observer reports, instead of recomputing it and drifting.
     */
    @JvmStatic
    fun aimPointOf(sourceObjectId: Long, turretIndex: Int): AimPoint? =
        aimPoints[InstanceKey(sourceObjectId, turretIndex)]

    fun register(owner: ApiImpl, id: String, observer: TurretFireCycleObserver) {
        observers.register(owner, id, observer)
    }

    @JvmStatic
    fun update(unit: CustomUnit) {
        unit.unitConfig.turrets.forEachIndexed { turretIndex, turret ->
            val observerId = turret?.turretFireCycleObserverId ?: return@forEachIndexed
            val variantId = turret.turretFireCycleObserverVariant ?: return@forEachIndexed
            val key = InstanceKey(unit.objectId, turretIndex)
            // Resolve the observer before touching any state: recording a window for an id
            // nobody registered would strand it, because teardown finds owned entries by id.
            if (observerId !in observers) {
                forget(key)
                return@forEachIndexed
            }
            rememberAimPoint(unit, turretIndex, key)
            updatePreFire(unit, turret, turretIndex, key, observerId, variantId)
            updatePostFire(unit, turret, turretIndex, key, observerId, variantId)
            // A destroyed unit never fires again, so its cycle state can go too.
            if (unit.isDestroyed) {
                generations.remove(key)
                aimPoints.remove(key)
            }
        }
    }

    private fun rememberAimPoint(unit: CustomUnit, turretIndex: Int, key: InstanceKey) {
        val target = unit.movementLevels[turretIndex].targetUnit ?: return
        aimPoints[key] = AimPoint(target.posX, target.posY, target.posZ)
    }

    /**
     * The wind-up, measured by how far the warm-up counter has climbed past the point where
     * the pre-fire window opens.
     */
    private fun updatePreFire(
        unit: CustomUnit,
        turret: TurretConfig,
        turretIndex: Int,
        key: InstanceKey,
        observerId: String,
        variantId: String,
    ) {
        val movement = unit.movementLevels[turretIndex]
        val target = movement.targetUnit
        val phaseStart = (turret.warmup - turret.preFireDuration).coerceAtLeast(0f)
        val age = movement.speed - phaseStart
        val isActive = !unit.isDestroyed && target != null && turret.preFireDuration > 0f && age > 0f
        if (!isActive) {
            endWindow(key, Window.PRE_FIRE)
            return
        }
        val muzzle = unit.D(turretIndex)
        advance(
            key = key,
            window = Window.PRE_FIRE,
            observerId = observerId,
            variantId = variantId,
            startX = muzzle.x,
            startY = muzzle.y,
            endX = target.posX,
            endY = target.posY,
            age = age,
            duration = turret.preFireDuration,
            // A shot that restarts its wind-up is a new cycle, so it takes a new generation.
            startsNewCycle = true,
        )
    }

    /**
     * The recovery, measured by how far the reload counter has decayed from the value the
     * engine stamped on it at the instant of the shot.
     *
     * The target is deliberately not re-read here: recoil plays out against where the shot
     * was aimed, which must stay put even if that target dies or the turret re-aims during
     * the window.
     */
    private fun updatePostFire(
        unit: CustomUnit,
        turret: TurretConfig,
        turretIndex: Int,
        key: InstanceKey,
        observerId: String,
        variantId: String,
    ) {
        val movement = unit.movementLevels[turretIndex]
        val reloadDelay = unit.b(turretIndex)
        val age = reloadDelay - movement.rotation
        val isActive = !unit.isDestroyed &&
                turret.postFireDuration > 0f &&
                movement.rotation > 0f &&
                age >= 0f &&
                age < turret.postFireDuration
        if (!isActive) {
            endWindow(key, Window.POST_FIRE)
            return
        }
        val muzzle = unit.D(turretIndex)
        val previous = active[key to Window.POST_FIRE]
        // The aim point the shot was taken against. `movementLevels[i].targetX` is the
        // turret's facing in degrees, not a world coordinate, so it cannot stand in here.
        val aim = aimPoints[key]
        advance(
            key = key,
            window = Window.POST_FIRE,
            observerId = observerId,
            variantId = variantId,
            startX = muzzle.x,
            startY = muzzle.y,
            // Hold the aim point for the life of the window, even if the turret re-aims.
            endX = previous?.endX ?: aim?.x ?: muzzle.x,
            endY = previous?.endY ?: aim?.y ?: muzzle.y,
            age = age,
            duration = turret.postFireDuration,
            // Post-fire continues the cycle its pre-fire started; it never opens a new one.
            startsNewCycle = false,
        )
    }

    private fun advance(
        key: InstanceKey,
        window: Window,
        observerId: String,
        variantId: String,
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        age: Float,
        duration: Float,
        startsNewCycle: Boolean,
    ) {
        val stateKey = key to window
        val previous = active[stateKey]
        val restarted = previous != null && (
                previous.observerId != observerId ||
                        previous.variantId != variantId ||
                        age + RESTART_EPSILON < previous.age
                )
        if (restarted) {
            dispatch(key, window, previous, previous, endPhaseOf(window))
        }
        val isFirstTick = previous == null || restarted
        val generation = when {
            !isFirstTick -> previous.generation
            startsNewCycle -> (generations[key] ?: -1) + 1
            // Attach to the cycle pre-fire opened, or start one if this turret has no
            // pre-fire window at all.
            else -> generations[key] ?: 0
        }
        val current = Snapshot(
            observerId = observerId,
            variantId = variantId,
            generation = generation,
            startX = startX,
            startY = startY,
            endX = endX,
            endY = endY,
            age = age.coerceIn(0f, duration),
            remaining = (duration - age).coerceAtLeast(0f),
            duration = duration,
        )
        generations[key] = generation
        active[stateKey] = current
        dispatch(
            key = key,
            window = window,
            current = current,
            previous = if (restarted) null else previous,
            phase = if (isFirstTick) startPhaseOf(window) else updatePhaseOf(window),
        )
    }

    private fun endWindow(key: InstanceKey, window: Window) {
        val previous = active.remove(key to window) ?: return
        dispatch(key, window, previous, previous, endPhaseOf(window))
    }

    override fun unregister(owner: ApiImpl) {
        val removedIds = observers.removeOwned(owner).keys
        val orphaned = active.keys.filter { active[it]?.observerId in removedIds }.map { it.first }
        active.entries.removeAll { it.value.observerId in removedIds }
        // Drop the cycle counters too, so a turret whose mod is reloaded starts from zero
        // rather than resuming a generation the new observer never saw.
        generations.keys.removeAll(orphaned.toSet())
        aimPoints.keys.removeAll(orphaned.toSet())
    }

    /** Forgets a turret's cycle entirely, without dispatching — nobody is listening. */
    private fun forget(key: InstanceKey) {
        active.remove(key to Window.PRE_FIRE)
        active.remove(key to Window.POST_FIRE)
        generations.remove(key)
        aimPoints.remove(key)
    }

    private fun startPhaseOf(window: Window) = when (window) {
        Window.PRE_FIRE -> TurretFireCyclePhase.PRE_FIRE_STARTED
        Window.POST_FIRE -> TurretFireCyclePhase.POST_FIRE_STARTED
    }

    private fun updatePhaseOf(window: Window) = when (window) {
        Window.PRE_FIRE -> TurretFireCyclePhase.PRE_FIRE_UPDATED
        Window.POST_FIRE -> TurretFireCyclePhase.POST_FIRE_UPDATED
    }

    private fun endPhaseOf(window: Window) = when (window) {
        Window.PRE_FIRE -> TurretFireCyclePhase.PRE_FIRE_ENDED
        Window.POST_FIRE -> TurretFireCyclePhase.POST_FIRE_ENDED
    }

    private fun dispatch(
        key: InstanceKey,
        window: Window,
        current: Snapshot,
        previous: Snapshot?,
        phase: TurretFireCyclePhase,
    ) {
        val observer = observers[current.observerId] ?: return
        failures.runSafely(current.observerId) {
            observer.onLifecycle(
                TurretFireCycleContext(
                    instanceId = TurretFireCycleInstanceId(
                        key.sourceObjectId,
                        key.turretIndex,
                        current.generation,
                    ),
                    phase = phase,
                    startX = current.startX,
                    startY = current.startY,
                    endX = current.endX,
                    endY = current.endY,
                    ageTicks = current.age,
                    previousAgeTicks = previous?.age,
                    remainingTicks = current.remaining,
                    previousRemainingTicks = previous?.remaining,
                    durationTicks = current.duration,
                    variantId = current.variantId,
                )
            )
        }
    }

    private const val RESTART_EPSILON = 0.001f
}
