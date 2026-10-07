package io.github.rwx.render

import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.FilterMode
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Picture
import org.jetbrains.skia.PictureRecorder
import org.jetbrains.skia.Rect

/** An immutable snapshot between batches; Skia materializes its shader on the drawing backend. */
internal class SkiaRecordedTarget(
    width: Int, height: Int, seed: Image?, private val sharedAtlas: Lazy<SkiaAtlasBatch>,
) : AutoCloseable {
    private val bounds = Rect.makeWH(width.toFloat(), height.toFloat())
    private val recorder = PictureRecorder()
    private var canvas: Canvas? = recorder.beginRecording(bounds).apply {
        clipRect(bounds, false)
        if (seed != null) drawImage(seed, 0f, 0f)
    }
    private var picture: Picture? = null
    private var atlas: SkiaAtlasBatch? = null
    var generations = 0
        private set

    fun canvas(): Canvas {
        val current = recordingCanvas()
        atlas?.flush(current)
        return current
    }

    fun addImage(
        image: Image, paint: Paint, sl: Float, st: Float, sr: Float, sb: Float,
        dl: Float, dt: Float, dr: Float, db: Float,
    ) {
        val batch = atlas ?: sharedAtlas.value.also { atlas = it }
        batch.add(recordingCanvas(), image, paint, sl, st, sr, sb, dl, dt, dr, db)
    }

    private fun recordingCanvas(): Canvas {
        canvas?.let { return it }
        val previous = checkNotNull(picture)
        val next = recorder.beginRecording(bounds)
        next.clipRect(bounds, false)
        // Drawing a picture directly would let its CLEAR/SRC operations modify the
        // destination. A picture shader isolates it just like an image snapshot.
        previous.makeShader(FilterTileMode.CLAMP, FilterTileMode.CLAMP, FilterMode.NEAREST).use { shader ->
            Paint().use { paint ->
                paint.shader = shader
                paint.blendMode = BlendMode.SRC
                next.drawRect(bounds, paint)
            }
        }
        previous.close()
        picture = null
        generations++
        canvas = next
        return next
    }

    fun snapshot(): Picture = picture ?: run {
        atlas?.flush(checkNotNull(canvas))
        recorder.finishRecordingAsPicture()
    }.also {
        picture = it
        canvas = null
    }

    override fun close() {
        atlas?.close()
        picture?.close()
        picture = null
        canvas = null
        recorder.close()
    }
}
