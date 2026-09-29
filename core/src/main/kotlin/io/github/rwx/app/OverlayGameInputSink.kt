package io.github.rwx.app

import com.corrodinggames.rts.gameFramework.GameEngine
import io.github.rwx.mod.api.WorldPosition
import io.github.rwx.mod.registry.UiRegistry
import io.github.rwx.session.GameSession
import io.github.rwx.ui.model.GameInputEvent
import io.github.rwx.ui.model.GamePointerButton

/**
 * Feeds unconsumed non-modal overlay input into the running game, with a single
 * active pointer and world-position selection. Frontend only.
 */
class OverlayGameInputSink(
    private val gameSession: GameSession,
    private val hasWorldPositionSelection: () -> Boolean = UiRegistry::hasActiveWorldPositionSelection,
    private val selectWorldPosition: (screenX: Float, screenY: Float) -> Boolean = ::selectEngineWorldPosition,
    private val cancelWorldPositionSelection: () -> Boolean = UiRegistry::cancelWorldPositionSelection,
) {
    private var activeButton: GamePointerButton? = null
    private var activeX = 0f
    private var activeY = 0f
    private var suppressUntilRelease = false
    private val pressedKeys = linkedSetOf<Int>()

    fun handle(event: GameInputEvent) {
        val scale = gameSession.inputCoordinateScale
        val engineEvent = if (scale == 1f) event else when (event) {
            is GameInputEvent.PointerMove -> event.copy(x = event.x * scale, y = event.y * scale)
            is GameInputEvent.PointerButton -> event.copy(x = event.x * scale, y = event.y * scale)
            else -> event
        }
        handleEngineInput(engineEvent)
    }

    private fun handleEngineInput(event: GameInputEvent) {
        when (event) {
            GameInputEvent.ReleaseAll -> release()
            is GameInputEvent.PointerMove -> {
                if (consumedBySelection(event.x, event.y, button = null)) return
                activeX = event.x
                activeY = event.y
                gameSession.movePointer(event.x, event.y)
            }

            is GameInputEvent.PointerButton -> {
                if (consumedBySelection(event.x, event.y, event)) return
                if (event.isDown) press(event) else release(event)
            }

            is GameInputEvent.Scroll -> {
                val amount = (event.notches * WHEEL_UNITS_PER_NOTCH).toInt()
                if (amount != 0) gameSession.submitMouseWheel(amount)
            }

            is GameInputEvent.Key -> {
                if (event.isDown) {
                    pressedKeys.add(event.androidKeyCode)
                    gameSession.submitKey(event.androidKeyCode, true)
                } else if (pressedKeys.remove(event.androidKeyCode)) {
                    gameSession.submitKey(event.androidKeyCode, false)
                }
            }
        }
    }

    /** Ends this input ownership period, including key-only holds and interrupted selections. */
    fun release() {
        val button = activeButton
        activeButton = null
        suppressUntilRelease = false
        if (button != null) gameSession.submitPointer(activeX, activeY, isDown = false, pointerId = button.legacyId)
        val keys = pressedKeys.toList()
        pressedKeys.clear()
        keys.forEach { gameSession.submitKey(it, false) }
    }

    private fun press(event: GameInputEvent.PointerButton) {
        activeButton?.takeIf { it != event.button }?.let { previous ->
            gameSession.submitPointer(event.x, event.y, isDown = false, pointerId = previous.legacyId)
        }
        activeButton = event.button
        activeX = event.x
        activeY = event.y
        gameSession.submitPointer(event.x, event.y, isDown = true, pointerId = event.button.legacyId)
    }

    private fun release(event: GameInputEvent.PointerButton) {
        // A release whose press was consumed by the HUD (or never seen) must not reach the game.
        if (activeButton != event.button) return
        activeButton = null
        gameSession.submitPointer(event.x, event.y, isDown = false, pointerId = event.button.legacyId)
    }

    private fun consumedBySelection(x: Float, y: Float, button: GameInputEvent.PointerButton?): Boolean {
        if (!hasWorldPositionSelection()) {
            if (!suppressUntilRelease) return false
            if (button != null && !button.isDown) suppressUntilRelease = false
            return true
        }
        suppressUntilRelease = true
        if (button?.isDown == true) {
            when (button.button) {
                GamePointerButton.Left -> selectWorldPosition(x, y)
                GamePointerButton.Right -> cancelWorldPositionSelection()
                GamePointerButton.Middle -> Unit
            }
        }
        return true
    }

    private companion object {
        const val WHEEL_UNITS_PER_NOTCH = 120f
    }
}

/** The same screen-to-world conversion the native pointer adapter applies to game input. */
internal fun selectEngineWorldPosition(screenX: Float, screenY: Float): Boolean {
    val engine = GameEngine.getInstance() ?: return false
    val zoom = engine.zoom
    if (!zoom.isFinite() || zoom <= 0f || !screenX.isFinite() || !screenY.isFinite()) return false
    return UiRegistry.selectWorldPosition(
        WorldPosition(
            x = screenX / zoom + engine.viewpointXSnapped,
            y = screenY / zoom + engine.viewpointYSnapped,
        )
    )
}
