package io.github.rwx.skia

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.skiaCanvas
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import io.github.rwx.app.OverlayGameInputSink
import io.github.rwx.ui.AppScreen
import io.github.rwx.ui.AppUiState
import io.github.rwx.ui.input.desktopGameKeyCode
import io.github.rwx.ui.model.GameInputEvent
import io.github.rwx.ui.model.GamePointerButton
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SkiaGameView(
    session: SkiaGameSession,
    modifier: Modifier = Modifier,
    running: Boolean = true,
    inputEnabled: Boolean = true,
) {
    var frameNanos by remember(session) { mutableLongStateOf(0L) }
    val lastDrawNanos = remember(session) { AtomicLong(0L) }
    val input = remember(session) { OverlayGameInputSink(session) }
    val focusRequester = remember(session) { FocusRequester() }
    val density = LocalDensity.current.density
    SideEffect { session.setInputCoordinateScale(density) }
    val acceptsInput = running && inputEnabled && LocalWindowInfo.current.isWindowFocused
    DisposableEffect(input, acceptsInput) {
        onDispose { input.release() }
    }
    LaunchedEffect(session, running) {
        if (running) {
            Executors.newSingleThreadExecutor { task ->
                Thread(task, "skia-frame-pacer").apply { isDaemon = true }
            }.asCoroutineDispatcher().use { dispatcher ->
                withContext(dispatcher) {
                    val pacer = SkiaFramePacer(FRAME_BUDGET_NANOS)
                    while (isActive) {
                        pacer.awaitNextFrame()
                        withFrameNanos { frameNanos = it }
                    }
                }
            }
        }
    }
    LaunchedEffect(session, acceptsInput) {
        if (acceptsInput) focusRequester.requestFocus()
    }
    Canvas(
        modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .onFocusChanged { if (!it.isFocused) input.release() }
            .focusable()
            .onPointerEvent(PointerEventType.Press) {
                if (!acceptsInput) return@onPointerEvent
                val button = currentEvent.button.toGameButton() ?: return@onPointerEvent
                focusRequester.requestFocus()
                currentEvent.changes.firstOrNull { it.pressed }?.let { change ->
                    input.handle(
                        GameInputEvent.PointerButton(
                            change.position.x / density,
                            change.position.y / density,
                            button,
                            true
                        )
                    )
                }
            }
            .onPointerEvent(PointerEventType.Release) {
                if (!acceptsInput) return@onPointerEvent
                val button = currentEvent.button.toGameButton() ?: return@onPointerEvent
                currentEvent.changes.firstOrNull()?.let { change ->
                    input.handle(
                        GameInputEvent.PointerButton(
                            change.position.x / density,
                            change.position.y / density,
                            button,
                            false
                        )
                    )
                }
            }
            .onPointerEvent(PointerEventType.Move) {
                if (!acceptsInput) return@onPointerEvent
                currentEvent.changes.firstOrNull()?.let { change ->
                    input.handle(GameInputEvent.PointerMove(change.position.x / density, change.position.y / density))
                }
            }
            .onPointerEvent(PointerEventType.Scroll) {
                if (!acceptsInput) return@onPointerEvent
                val notches = -(currentEvent.changes.firstOrNull()?.scrollDelta?.y ?: 0f)
                if (notches != 0f) input.handle(GameInputEvent.Scroll(notches))
            }
            .onKeyEvent { event ->
                if (!acceptsInput) return@onKeyEvent false
                val awtEvent = event.awtEventOrNull ?: return@onKeyEvent false
                val code = skiaGameKeyCode(awtEvent.keyCode, awtEvent.keyLocation) ?: return@onKeyEvent false
                val isDown = when (event.type) {
                    KeyEventType.KeyDown -> true
                    KeyEventType.KeyUp -> false
                    else -> return@onKeyEvent false
                }
                input.handle(GameInputEvent.Key(code, isDown))
                true
            }
    ) {
        val width = size.width.roundToInt()
        val height = size.height.roundToInt()
        val timestamp = frameNanos
        if (width > 0 && height > 0 && timestamp != 0L) {
            val previous = lastDrawNanos.getAndSet(timestamp)
            val deltaSeconds = if (previous == 0L) {
                1f / 60f
            } else {
                ((timestamp - previous) / 1_000_000_000.0).toFloat().coerceIn(0f, 0.25f)
            }
            drawIntoCanvas { canvas ->
                session.renderInto(canvas.skiaCanvas, width, height, deltaSeconds)
            }
        }
    }
}

private const val TARGET_FRAME_RATE = 300
private const val FRAME_BUDGET_NANOS = 1_000_000_000L / TARGET_FRAME_RATE

private fun PointerButton?.toGameButton(): GamePointerButton? = when (this) {
    null, PointerButton.Primary -> GamePointerButton.Left
    PointerButton.Secondary -> GamePointerButton.Right
    PointerButton.Tertiary -> GamePointerButton.Middle
    else -> null
}

internal fun acceptsDirectSkiaInput(state: AppUiState?): Boolean =
    state?.screen == AppScreen.InGame && state.inGameOverlay?.visible != true

internal fun skiaGameKeyCode(code: Int, location: Int): Int? = desktopGameKeyCode(code, location)
