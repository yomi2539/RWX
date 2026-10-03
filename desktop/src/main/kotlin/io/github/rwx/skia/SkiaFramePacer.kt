package io.github.rwx.skia

import java.util.concurrent.CancellationException
import java.util.concurrent.locks.LockSupport

/** Call only on the pacing worker: the UI thread must remain free to draw and process input. */
internal class SkiaFramePacer(
    private val intervalNanos: Long,
    private val clockNanos: () -> Long = System::nanoTime,
    private val waitNanos: (Long) -> Unit = { LockSupport.parkNanos(it) },
) {
    private var deadline: Long? = null

    init {
        require(intervalNanos > 0L)
    }

    fun awaitNextFrame() {
        var now = clockNanos()
        val target = deadline ?: now
        while (target - now > 0L) {
            if (Thread.currentThread().isInterrupted) throw CancellationException("Frame pacing interrupted")
            waitNanos(target - now)
            now = clockNanos()
        }
        // Keep the phase after small timer overruns; skip missed frames after a stall.
        // A frame already delayed by vsync must not incur another full-frame wait.
        val next = target + intervalNanos
        deadline = if (next > now) next else now + intervalNanos
    }
}
