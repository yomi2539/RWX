package io.github.rwx.mod.api

data class ProjectileObserverBinding(
    val observerId: String,
    val variantId: String,
)

data class ProjectileInstanceId(
    val objectId: Long,
    val generation: Int,
)

enum class ProjectileLifecyclePhase {
    STARTED,
    UPDATED,
    ENDED,
}

data class ProjectileLifecycleContext(
    val instanceId: ProjectileInstanceId,
    val phase: ProjectileLifecyclePhase,
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val ageTicks: Float,
    val previousAgeTicks: Float?,
    val remainingTicks: Float,
    val previousRemainingTicks: Float?,
    val variantId: String,
)

fun interface ProjectileObserver {
    fun onLifecycle(context: ProjectileLifecycleContext)
}
