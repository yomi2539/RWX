package io.github.rwx.ui.input

import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.input.key.*
import com.corrodinggames.rts.gameFramework.utility.SlickToAndroidKeycodes.AndroidCodes
import io.github.rwx.settings.KeyBindingInput
import java.awt.event.KeyEvent as AwtKeyEvent

internal data class BindingStroke(val keyCode: Int, val modifiers: Int)

/** Shared by the game canvas and overlays so captured key bindings work in both. */
fun desktopGameKeyCode(code: Int, location: Int = AwtKeyEvent.KEY_LOCATION_STANDARD): Int? =
    DesktopKeyCodeMapping.gameKeyCode(code, location)

/** AWT key identity, not typed characters: Shift+1 must remain Shift+1, not the '!' character. */
internal object DesktopKeyCodeMapping {
    fun stroke(event: KeyEvent): BindingStroke? {
        // The engine has no Meta/AltGr modifier bit; never silently turn those chords into plain keys.
        if (event.isMetaPressed || event.awtEventOrNull?.isAltGraphDown == true) return null
        val code = androidKeyCode(event.key.nativeKeyCode, event.key.nativeKeyLocation) ?: return null
        var modifiers = 0
        if (event.isCtrlPressed) modifiers = modifiers or KeyBindingInput.CTRL
        if (event.isShiftPressed) modifiers = modifiers or KeyBindingInput.SHIFT
        if (event.isAltPressed) modifiers = modifiers or KeyBindingInput.ALT
        return BindingStroke(code, modifiers)
    }

    /** Binding keys plus the modifier keys themselves, which the running game tracks as hotkeys. */
    fun gameKeyCode(code: Int, location: Int = AwtKeyEvent.KEY_LOCATION_STANDARD): Int? {
        androidKeyCode(code, location)?.let { return it }
        val right = location == AwtKeyEvent.KEY_LOCATION_RIGHT
        return when (code) {
            AwtKeyEvent.VK_SHIFT -> if (right) AndroidCodes.KEYCODE_SHIFT_RIGHT else AndroidCodes.KEYCODE_SHIFT_LEFT
            AwtKeyEvent.VK_CONTROL -> if (right) AndroidCodes.KEYCODE_CTRL_RIGHT else AndroidCodes.KEYCODE_CTRL_LEFT
            AwtKeyEvent.VK_ALT -> if (right) AndroidCodes.KEYCODE_ALT_RIGHT else AndroidCodes.KEYCODE_ALT_LEFT
            else -> null
        }
    }

    fun androidKeyCode(code: Int, location: Int = AwtKeyEvent.KEY_LOCATION_STANDARD): Int? {
        if (location == AwtKeyEvent.KEY_LOCATION_NUMPAD && code == AwtKeyEvent.VK_ENTER) {
            return AndroidCodes.KEYCODE_NUMPAD_ENTER
        }
        // With NumLock off, the game treats keypad navigation as navigation, not digits.
        return when (code) {
            in AwtKeyEvent.VK_A..AwtKeyEvent.VK_Z -> AndroidCodes.KEYCODE_A + code - AwtKeyEvent.VK_A
            in AwtKeyEvent.VK_0..AwtKeyEvent.VK_9 -> AndroidCodes.KEYCODE_0 + code - AwtKeyEvent.VK_0
            in AwtKeyEvent.VK_F1..AwtKeyEvent.VK_F12 -> AndroidCodes.KEYCODE_F1 + code - AwtKeyEvent.VK_F1
            in AwtKeyEvent.VK_NUMPAD0..AwtKeyEvent.VK_NUMPAD9 -> AndroidCodes.KEYCODE_NUMPAD_0 + code - AwtKeyEvent.VK_NUMPAD0
            else -> when (code) {
                AwtKeyEvent.VK_ESCAPE -> AndroidCodes.KEYCODE_ESCAPE
                AwtKeyEvent.VK_ENTER -> AndroidCodes.KEYCODE_ENTER
                AwtKeyEvent.VK_TAB -> AndroidCodes.KEYCODE_TAB
                AwtKeyEvent.VK_SPACE -> AndroidCodes.KEYCODE_SPACE
                AwtKeyEvent.VK_BACK_SPACE -> AndroidCodes.KEYCODE_DEL
                AwtKeyEvent.VK_DELETE -> AndroidCodes.KEYCODE_FORWARD_DEL
                AwtKeyEvent.VK_INSERT -> AndroidCodes.KEYCODE_INSERT
                AwtKeyEvent.VK_HOME -> AndroidCodes.KEYCODE_MOVE_HOME
                AwtKeyEvent.VK_END -> AndroidCodes.KEYCODE_MOVE_END
                AwtKeyEvent.VK_PAGE_UP -> AndroidCodes.KEYCODE_PAGE_UP
                AwtKeyEvent.VK_PAGE_DOWN -> AndroidCodes.KEYCODE_PAGE_DOWN
                AwtKeyEvent.VK_UP, AwtKeyEvent.VK_KP_UP -> AndroidCodes.KEYCODE_DPAD_UP
                AwtKeyEvent.VK_DOWN, AwtKeyEvent.VK_KP_DOWN -> AndroidCodes.KEYCODE_DPAD_DOWN
                AwtKeyEvent.VK_LEFT, AwtKeyEvent.VK_KP_LEFT -> AndroidCodes.KEYCODE_DPAD_LEFT
                AwtKeyEvent.VK_RIGHT, AwtKeyEvent.VK_KP_RIGHT -> AndroidCodes.KEYCODE_DPAD_RIGHT
                AwtKeyEvent.VK_PAUSE -> AndroidCodes.KEYCODE_BREAK
                AwtKeyEvent.VK_COMMA -> AndroidCodes.KEYCODE_COMMA
                AwtKeyEvent.VK_PERIOD -> AndroidCodes.KEYCODE_PERIOD
                AwtKeyEvent.VK_MINUS -> AndroidCodes.KEYCODE_MINUS
                AwtKeyEvent.VK_EQUALS -> AndroidCodes.KEYCODE_EQUALS
                AwtKeyEvent.VK_OPEN_BRACKET -> AndroidCodes.KEYCODE_LEFT_BRACKET
                AwtKeyEvent.VK_CLOSE_BRACKET -> AndroidCodes.KEYCODE_RIGHT_BRACKET
                AwtKeyEvent.VK_BACK_SLASH -> AndroidCodes.KEYCODE_BACKSLASH
                AwtKeyEvent.VK_SEMICOLON -> AndroidCodes.KEYCODE_SEMICOLON
                AwtKeyEvent.VK_QUOTE -> AndroidCodes.KEYCODE_APOSTROPHE
                AwtKeyEvent.VK_SLASH -> AndroidCodes.KEYCODE_SLASH
                AwtKeyEvent.VK_BACK_QUOTE -> AndroidCodes.KEYCODE_GRAVE
                AwtKeyEvent.VK_DIVIDE -> AndroidCodes.KEYCODE_NUMPAD_DIVIDE
                AwtKeyEvent.VK_MULTIPLY -> AndroidCodes.KEYCODE_NUMPAD_MULTIPLY
                AwtKeyEvent.VK_SUBTRACT -> AndroidCodes.KEYCODE_NUMPAD_SUBTRACT
                AwtKeyEvent.VK_ADD -> AndroidCodes.KEYCODE_NUMPAD_ADD
                AwtKeyEvent.VK_DECIMAL -> AndroidCodes.KEYCODE_NUMPAD_DOT
                else -> null
            }
        }
    }
}
