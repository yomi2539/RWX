package io.github.rwx.skia

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.awt.RenderSettings.SkiaSurface
import io.github.rwx.logger
import io.github.rwx.ui.platform.PlatformComposeHost
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.SkiaLayerAnalytics
import java.util.concurrent.atomic.AtomicReference
import javax.swing.SwingUtilities

@OptIn(ExperimentalComposeUiApi::class)
fun createSkiaGamePanel(isVsyncEnabled: Boolean? = null): ComposePanel =
    ComposePanel(
        skiaLayerAnalytics = GpuBackendAnalytics(),
        renderSettings = SkiaSurface(isVsyncEnabled = isVsyncEnabled),
    ).apply {
        name = "skia-game"
        isVisible = false
        disableSkikoPeriodicFullGc()
    }

private fun disableSkikoPeriodicFullGc() {
    runCatching {
        val watcher = Class.forName("org.jetbrains.skiko.FrameWatcher")
        val instance = watcher.getField("INSTANCE").get(null)
        watcher.getMethod("setMinFramesToRenderer", Int::class.javaPrimitiveType)
            .invoke(instance, Int.MAX_VALUE)
    }.onFailure { error ->
        logger.warn(error) { "Unable to disable Skiko FrameWatcher periodic GC" }
    }
}

@OptIn(ExperimentalSkikoApi::class)
private class GpuBackendAnalytics : SkiaLayerAnalytics {
    private val attemptedApi = AtomicReference<GraphicsApi?>(null)

    override fun renderer(skikoVersion: String, os: OS, api: GraphicsApi): SkiaLayerAnalytics.RendererAnalytics {
        val previous = attemptedApi.getAndSet(api)
        if (previous == null || previous == api) {
            logger.info { "Initializing Skia game surface: $api (skiko $skikoVersion, $os)" }
        } else {
            logger.warn { "Skia game surface backend fallback: $previous -> $api" }
        }
        return SkiaLayerAnalytics.RendererAnalytics.Empty
    }

    override fun device(
        skikoVersion: String,
        os: OS,
        api: GraphicsApi,
        deviceName: String?,
    ): SkiaLayerAnalytics.DeviceAnalytics = object : SkiaLayerAnalytics.DeviceAnalytics {
        override fun afterFirstFrameRender() {
            val device = deviceName ?: "unknown device"
            if (api == GraphicsApi.SOFTWARE_FAST || api == GraphicsApi.SOFTWARE_COMPAT) {
                logger.warn { "Skia game surface is using software rendering: $api ($device)" }
            } else {
                logger.info { "Skia game surface backend active: $api ($device)" }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
class SkiaOverlayPanelHost(
    private val panel: ComposePanel,
    private val overlayContent: MutableState<(@Composable () -> Unit)?>,
) : PlatformComposeHost {
    override val isVisible: Boolean get() = panel.isVisible

    override fun setContent(content: @Composable () -> Unit) {
        check(SwingUtilities.isEventDispatchThread()) { "Set Skia overlay content on the EDT" }
        overlayContent.value = content
    }

    override fun setVisible(visible: Boolean) = Unit

    override fun requestFocus() {
        check(SwingUtilities.isEventDispatchThread()) { "Request Skia overlay focus on the EDT" }
        panel.requestFocusInWindow()
    }

    override fun dispose() {
        check(SwingUtilities.isEventDispatchThread()) { "Dispose the Skia overlay host on the EDT" }
        panel.dispose()
    }
}
