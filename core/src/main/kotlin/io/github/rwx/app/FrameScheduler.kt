package io.github.rwx.app

interface FrameScheduler {
    fun start(tick: () -> Unit)
    fun stop()
}
internal class OwnedFrameLoop(
    private val scheduler: FrameScheduler,
    private val clock: FrameClock,
    private val drive: (Float) -> Unit,
) : AutoCloseable {
    private var generation = 0L
    fun start() {
        val current = ++generation
        scheduler.start {
            if (generation == current) drive(clock.deltaSeconds())
        }
    }
    override fun close() {
        generation++
        scheduler.stop()
    }
}
