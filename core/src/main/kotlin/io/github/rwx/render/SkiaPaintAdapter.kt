package io.github.rwx.render

import com.corrodinggames.rts.gameFramework.graphics.BlendMode as LegacyBlendMode
import com.corrodinggames.rts.gameFramework.graphics.TeamColorFilter
import io.github.rwx.render.canvas.BlendColorFilter
import io.github.rwx.render.canvas.ColorFilter
import io.github.rwx.render.canvas.MultiplyAddColorFilter
import io.github.rwx.render.canvas.Paint
import io.github.rwx.render.canvas.resolvedPaintColor
import io.github.rwx.render.frame.GameCanvasBlendMode
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.ColorFilter as SkiaColorFilter
import org.jetbrains.skia.Paint as SkiaPaint
import org.jetbrains.skia.PaintMode

internal class SkiaPaintAdapter : AutoCloseable {
    private val paint = SkiaPaint()
    private var lastFilter: ColorFilter? = null
    private var lastImageTint: Int = -1
    private var nativeFilter: SkiaColorFilter? = null
    private var color: Int? = null
    private var antiAlias: Boolean? = null
    private var dither: Boolean? = null
    private var strokeWidth: Float? = null
    private var mode: PaintMode? = null
    private var blendMode: BlendMode? = null

    fun configure(
        source: Paint?,
        image: Boolean = false,
        colorOverride: Int? = null,
        blendOverride: BlendMode? = null,
    ): SkiaPaint {
        val filter = source?.colorFilter()
        // Paint is mutable (including public legacy fields), so compare values instead
        // of relying on its identity/revision. Setters cross JNI even for unchanged values.
        // Texture quads follow the desktop renderer's hard geometry edges. Bitmap
        // filtering and alpha still apply; shapes and text keep their requested AA.
        val nextAntiAlias = !image && (source?.isAntiAlias() ?: false)
        val nextDither = source?.isDither() ?: false
        val nextStrokeWidth = (source?.strokeWidth() ?: 0f).coerceAtLeast(0f)
        val nextMode = when (if (image) null else source?.style()) {
            Paint.Style.STROKE -> PaintMode.STROKE
            Paint.Style.FILL_AND_STROKE -> PaintMode.STROKE_AND_FILL
            else -> PaintMode.FILL
        }
        val nextBlendMode = blendOverride ?: source?.getBlendMode()?.toSkia() ?: when {
            filter is MultiplyAddColorFilter && filter.usesLegacyAdditiveBlend() -> BlendMode.PLUS
            filter is TeamColorFilter && filter.a == LegacyBlendMode.copy -> BlendMode.PLUS
            filter is TeamColorFilter && filter.a == LegacyBlendMode.additive -> BlendMode.SCREEN
            else -> BlendMode.SRC_OVER
        }
        val nextColor = colorOverride ?: source.resolvedPaintColor()
        // drawImageRect ignores Paint RGB. Modulate the image explicitly, leaving alpha
        // to Paint so texture transparency and effect fading are not multiplied twice.
        val nextImageTint = if (image) nextColor or 0xff000000.toInt() else -1
        if (filter !== lastFilter || nextImageTint != lastImageTint) {
            paint.colorFilter = null
            nativeFilter?.close()
            nativeFilter = createColorFilter(filter, nextImageTint)
            lastFilter = filter
            lastImageTint = nextImageTint
            paint.colorFilter = nativeFilter
        }
        if (color != nextColor) {
            paint.color = nextColor; color = nextColor
        }
        if (antiAlias != nextAntiAlias) {
            paint.isAntiAlias = nextAntiAlias; antiAlias = nextAntiAlias
        }
        if (dither != nextDither) {
            paint.isDither = nextDither; dither = nextDither
        }
        if (strokeWidth != nextStrokeWidth) {
            paint.strokeWidth = nextStrokeWidth; strokeWidth = nextStrokeWidth
        }
        if (mode != nextMode) {
            paint.mode = nextMode; mode = nextMode
        }
        if (blendMode != nextBlendMode) {
            paint.blendMode = nextBlendMode; blendMode = nextBlendMode
        }
        return paint
    }

    private fun createColorFilter(filter: ColorFilter?, imageTint: Int): SkiaColorFilter? {
        // Multiply filters are already folded into resolvedPaintColor().
        val blendFilter = if (filter is BlendColorFilter && filter.blendMode != GameCanvasBlendMode.Multiply) {
            SkiaColorFilter.makeBlend(filter.color, filter.blendMode.toSkia())
        } else null
        if (imageTint == -1) return blendFilter
        val tintFilter = SkiaColorFilter.makeBlend(imageTint, BlendMode.MODULATE)
        return blendFilter?.use { outer ->
            tintFilter.use { inner -> SkiaColorFilter.makeComposed(outer, inner) }
        }
            ?: tintFilter
    }

    override fun close() {
        paint.close()
        nativeFilter?.close()
    }
}

internal fun GameCanvasBlendMode.toSkia(): BlendMode = when (this) {
    GameCanvasBlendMode.SourceOver -> BlendMode.SRC_OVER
    // Premultiplied surfaces cannot retain visible RGB with zero alpha.
    GameCanvasBlendMode.Clear, GameCanvasBlendMode.ClearAlpha -> BlendMode.CLEAR
    GameCanvasBlendMode.Source -> BlendMode.SRC
    GameCanvasBlendMode.Destination -> BlendMode.DST
    GameCanvasBlendMode.SourceIn -> BlendMode.SRC_IN
    GameCanvasBlendMode.SourceOut -> BlendMode.SRC_OUT
    GameCanvasBlendMode.SourceAtop -> BlendMode.SRC_ATOP
    GameCanvasBlendMode.DestinationOver -> BlendMode.DST_OVER
    GameCanvasBlendMode.DestinationIn -> BlendMode.DST_IN
    GameCanvasBlendMode.DestinationOut -> BlendMode.DST_OUT
    GameCanvasBlendMode.DestinationAtop -> BlendMode.DST_ATOP
    GameCanvasBlendMode.Xor -> BlendMode.XOR
    GameCanvasBlendMode.Add -> BlendMode.PLUS
    GameCanvasBlendMode.Multiply -> BlendMode.MULTIPLY
    GameCanvasBlendMode.Screen -> BlendMode.SCREEN
    GameCanvasBlendMode.Overlay -> BlendMode.OVERLAY
}
