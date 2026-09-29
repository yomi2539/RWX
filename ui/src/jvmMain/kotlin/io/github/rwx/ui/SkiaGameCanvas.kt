package io.github.rwx.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.skiaCanvas
import com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
import io.github.rwx.PlatformStorage
import io.github.rwx.render.SkiaGraphicsEngine
import kotlinx.coroutines.isActive
import kotlin.math.roundToInt

@Composable
fun rememberSkiaGraphicsEngine(storage: PlatformStorage): SkiaGraphicsEngine {
    val renderer = remember(storage) { SkiaGraphicsEngine(storage) }
    DisposableEffect(renderer) { onDispose { renderer.close() } }
    return renderer
}

@Composable
fun SkiaGameCanvas(
    renderer: SkiaGraphicsEngine,
    modifier: Modifier = Modifier,
    running: Boolean = true,
    onFrame: (GraphicsEngine, Long) -> Unit,
) {
    var frameTime by remember(renderer) { mutableLongStateOf(0L) }
    val drawFrame by rememberUpdatedState(onFrame)
    LaunchedEffect(renderer, running) {
        if (running) while (isActive) withFrameNanos { frameTime = it }
    }
    Canvas(modifier) {
        val width = size.width.roundToInt()
        val height = size.height.roundToInt()
        val timestamp = frameTime
        if (width > 0 && height > 0) {
            drawIntoCanvas { canvas ->
                renderer.render(canvas.skiaCanvas, width, height) { drawFrame(it, timestamp) }
            }
        }
    }
}
