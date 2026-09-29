package io.github.rwx.skia

/** Input callbacks never wait for a loader or touch the render thread's mutable input state. */
internal class SkiaGameInputQueue {
    sealed interface Event {
        data class Pointer(val x: Float, val y: Float, val down: Boolean, val button: Int) : Event
        data class Move(val x: Float, val y: Float) : Event
        data class Key(val code: Int, val down: Boolean) : Event
        data class Wheel(val amount: Int) : Event
        data object Reset : Event
    }

    private val pending = ArrayDeque<Event>()
    private var closed = false

    @Synchronized
    fun submit(event: Event, busy: Boolean = false) {
        if (closed) return
        if (busy || event == Event.Reset) {
            // Loading input is not actionable, but releases must still clear existing holds.
            pending.clear()
            pending.addLast(Event.Reset)
            return
        }
        if (event is Event.Move && pending.lastOrNull() is Event.Move) pending.removeLast()
        pending.addLast(event)
    }

    fun drain(consume: (Event) -> Unit) {
        val events = synchronized(this) {
            if (pending.isEmpty()) return
            pending.toList().also { pending.clear() }
        }
        events.forEach(consume)
    }

    @Synchronized
    fun close() {
        closed = true
        pending.clear()
    }
}
