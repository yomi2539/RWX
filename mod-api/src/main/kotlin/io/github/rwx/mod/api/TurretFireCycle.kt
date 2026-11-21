package io.github.rwx.mod.api

data class TurretFireCycleObserverBinding(
    val observerId: String,
    val variantId: String,
)

/**
 * Identifies one run through a turret's fire cycle.
 *
 * A turret is a fixed slot on a unit, so [sourceObjectId] and [turretIndex] alone would name
 * every shot it ever fires. [generation] separates them: it advances each time the cycle
 * restarts, so a mod can tell a re-aimed second shot from a continuation of the first.
 */
data class TurretFireCycleInstanceId(
    val sourceObjectId: Long,
    val turretIndex: Int,
    val generation: Int,
)

/**
 * The stage a turret has reached in one shot.
 *
 * The two windows sit either side of the moment the projectile leaves the barrel: pre-fire
 * is the wind-up before it, post-fire the recovery after. Each runs its own
 * STARTED / UPDATED / ENDED sequence, so a mod that only cares about one can match on its
 * prefix, and the pair for a single shot shares one [TurretFireCycleInstanceId].
 *
 * A turret may fire with no pre-fire window (or no post-fire one) configured; the phases for
 * a window whose duration is zero never arrive.
 */
enum class TurretFireCyclePhase {
    PRE_FIRE_STARTED,
    PRE_FIRE_UPDATED,
    PRE_FIRE_ENDED,
    POST_FIRE_STARTED,
    POST_FIRE_UPDATED,
    POST_FIRE_ENDED,
    ;

    /** True for the three pre-fire phases. */
    val isPreFire: Boolean
        get() = this == PRE_FIRE_STARTED || this == PRE_FIRE_UPDATED || this == PRE_FIRE_ENDED

    /** True for the three post-fire phases. */
    val isPostFire: Boolean get() = !isPreFire
}

/**
 * One tick of a turret's fire cycle.
 *
 * [startX]/[startY] is the muzzle. [endX]/[endY] is the turret's target: during pre-fire it
 * tracks the live target, and during post-fire it holds the position the shot was taken
 * against, which stays put even if that target dies mid-recovery.
 *
 * [ageTicks] and [remainingTicks] are measured within the current window, not across the
 * whole cycle, so both run 0..[durationTicks] twice per shot.
 */
data class TurretFireCycleContext(
    val instanceId: TurretFireCycleInstanceId,
    val phase: TurretFireCyclePhase,
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val ageTicks: Float,
    val previousAgeTicks: Float?,
    val remainingTicks: Float,
    val previousRemainingTicks: Float?,
    val durationTicks: Float,
    val variantId: String,
)

fun interface TurretFireCycleObserver {
    fun onLifecycle(context: TurretFireCycleContext)
}
