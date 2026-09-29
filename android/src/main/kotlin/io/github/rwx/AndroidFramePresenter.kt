package io.github.rwx

import android.app.Activity
import android.view.View
import com.corrodinggames.rts.appFramework.GameViewOpenGL
import com.corrodinggames.rts.gameFramework.android.graphics.GraphicsInterface
import io.github.rwx.render.RenderBackend
import androidx.core.view.isVisible
import java.util.Locale

internal enum class AndroidRenderBackend : RenderBackend {

    CANVAS,
    OPENGL_ES;

    override val id: String = name.lowercase(Locale.ROOT)
}

internal class AndroidPresentedFrame(
    val graphics: GraphicsInterface,
    private val submitAction: () -> Unit,
    private val cancelAction: () -> Unit,
) {
    fun submit() = submitAction()

    fun cancel() = cancelAction()
}

internal interface AndroidFramePresenter {
    val view: View

    fun isReady(): Boolean

    fun acquireFrame(): AndroidPresentedFrame?

    fun pause()

    fun resume()

    fun setVisible(visible: Boolean)
}

internal fun AndroidRenderBackend.createPresenter(activity: Activity): AndroidFramePresenter =
    when (this) {
        AndroidRenderBackend.CANVAS -> AndroidCanvasFramePresenter(CanvasGameView(activity))
        AndroidRenderBackend.OPENGL_ES -> AndroidOpenGlFramePresenter(GameViewOpenGL(activity, null))
    }

private class AndroidCanvasFramePresenter(
    private val canvasView: CanvasGameView,
) : AndroidFramePresenter {
    override val view: View
        get() = canvasView

    override fun isReady(): Boolean = canvasView.isReady()

    override fun acquireFrame(): AndroidPresentedFrame? {
        val frame = canvasView.acquireFrame() ?: return null
        return AndroidPresentedFrame(
            graphics = frame.renderer,
            submitAction = { canvasView.submitFrame(frame) },
            cancelAction = { canvasView.cancelFrame(frame) },
        )
    }

    override fun pause() {
        canvasView.paused = true
    }

    override fun resume() {
        canvasView.paused = false
        canvasView.invalidate()
    }

    override fun setVisible(visible: Boolean) {
        if (visible) resume() else pause()
    }
}

private class AndroidOpenGlFramePresenter(
    private val openGlView: GameViewOpenGL,
) : AndroidFramePresenter {
    override val view: View
        get() = openGlView

    override fun isReady(): Boolean =
        !openGlView.paused &&
                openGlView.surfaceExists &&
                openGlView.isAttachedToWindow &&
                openGlView.isVisible &&
                openGlView.width > 0 &&
                openGlView.height > 0

    override fun acquireFrame(): AndroidPresentedFrame {
        val graphics = openGlView.getNewCanvasLock(true)
        return AndroidPresentedFrame(
            graphics = graphics,
            submitAction = { openGlView.unlockAndReturnCanvas(graphics, true) },
            cancelAction = {},
        )
    }

    override fun pause() {
        openGlView.paused = true
        openGlView.onPause()
        openGlView.onParentPause()
        synchronized(GameViewOpenGL.makeActiveLock) {
            if (GameViewOpenGL.lastHeldSurfaceView === openGlView) {
                GameViewOpenGL.lastHeldSurfaceView = null
            }
        }
    }

    override fun resume() {
        openGlView.onResume()
        openGlView.onParentResume()
    }

    override fun setVisible(visible: Boolean) {
        if (visible) resume() else pause()
    }
}
