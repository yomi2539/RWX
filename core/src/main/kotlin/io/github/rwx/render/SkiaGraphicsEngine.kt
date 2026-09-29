package io.github.rwx.render

import com.corrodinggames.rts.gameFramework.graphics.GraphicsBackendCapabilities
import com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
import com.corrodinggames.rts.gameFramework.graphics.ShaderProgram
import com.corrodinggames.rts.gameFramework.graphics.Texture
import com.corrodinggames.rts.gameFramework.utility.AssetInputStream
import io.github.rwx.PlatformStorage
import io.github.rwx.geometry.Rect
import io.github.rwx.geometry.RectF
import io.github.rwx.render.canvas.Paint
import io.github.rwx.render.frame.GameCanvasBlendMode
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Matrix33
import org.jetbrains.skia.SamplingMode
import org.jetbrains.skia.Rect as SkiaRect
import java.io.File
import java.io.InputStream
import java.util.concurrent.locks.Lock
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

/**
 * Skia backend for JVM Compose hosts on Windows, Linux and macOS.
 *
 * Call [render] from the host's draw callback. The Canvas is borrowed only for that call;
 * its existing density transform and clip are preserved. Texture operations and rendering
 * must be serialized on the render thread. Closing the root engine releases its textures.
 * GLSL effects are unsupported; the game selects its CPU team-colouring path.
 */
class SkiaGraphicsEngine private constructor(
    private val resources: SkiaResources,
    private val target: SkiaTexture? = null,
    private val foreignTarget: Texture? = null,
    private val ownsResources: Boolean = false,
) : GraphicsEngine, AutoCloseable {
    constructor(readAsset: (String) -> ByteArray? = { null }) : this(SkiaResources(readAsset), ownsResources = true)
    constructor(storage: PlatformStorage) : this(storage::readAssetBytes)

    private val paints = SkiaPaintAdapter()
    private var borrowedCanvas: Canvas? = null
    private var width = target?.width() ?: 1
    private var height = target?.height() ?: 1
    private var state = INITIAL_STATE
    private var appliedState: CanvasState? = null
    private var frameBaseMatrix = Matrix33.IDENTITY
    private var frameStateRestoreCount = 0
    private val states = ArrayDeque<CanvasState>()
    private var foreignRevision = foreignTarget?.getPixelRevision()
    private var closed = false

    override fun backendCapabilities() = BACKEND_CAPABILITIES

    /** Coordinates are relative to the borrowed canvas; pass the Compose DrawScope size in pixels. */
    fun render(canvas: Canvas, width: Int, height: Int, draw: (GraphicsEngine) -> Unit) {
        checkOpen()
        check(target == null && borrowedCanvas == null) { "A frame is already active or this engine has a texture target" }
        require(width > 0 && height > 0) { "Viewport dimensions must be positive" }
        this.width = width
        this.height = height
        state = INITIAL_STATE
        appliedState = null
        states.clear()
        val restoreCount = canvas.save()
        borrowedCanvas = canvas
        try {
            // Compose drawing nodes do not clip to their bounds by default. Keep clear/fill
            // operations inside this viewport without changing the caller's persistent clip.
            canvas.clipRect(SkiaRect.makeWH(width.toFloat(), height.toFloat()), false)
            frameBaseMatrix = canvas.localToDeviceAsMatrix33
            frameStateRestoreCount = canvas.save()
            draw(this)
        } finally {
            canvas.restoreToCount(restoreCount)
            borrowedCanvas = null
            state = INITIAL_STATE
            appliedState = null
            states.clear()
        }
    }

    override fun b(texture: Texture?): SkiaGraphicsEngine {
        checkOpen()
        if (texture == null) return SkiaGraphicsEngine(resources)
        if (texture is SkiaTexture) {
            check(!texture.isClosed) { "Texture is closed" }
            return SkiaGraphicsEngine(resources, texture)
        }
        val native = resources.create(texture.width(), texture.height(), texture.m)
        texture.argbPixelsCopy?.let(native::setCommittedArgbPixels)
        native.setPremultipliedAlpha(texture.usesPremultipliedAlpha())
        return SkiaGraphicsEngine(resources, native, texture)
    }

    override fun a(lock: Lock?) {
        lock?.lock()
    }

    override fun b(lock: Lock?) {
        lock?.unlock()
    }

    override fun a(i: Int): Texture = a(i, true)
    override fun a(i: Int, z: Boolean): Texture {
        checkOpen(); return resources.drawable(i)
    }

    override fun a(inputStream: InputStream?, z: Boolean): Texture {
        checkOpen()
        // The caller owns the stream, matching the other GraphicsEngine implementations.
        return inputStream?.let { resources.decode(it.readBytes(), (it as? AssetInputStream)?.path) } ?: r()
    }

    override fun a(i: Int, i2: Int, z: Boolean): SkiaTexture = b(i, i2, z)
    override fun b(i: Int, i2: Int, z: Boolean): SkiaTexture {
        checkOpen()
        return resources.create(i, i2, z)
    }

    override fun a(texture: Texture?, f: Float, f2: Float, f3: Float, paint: Paint?) {
        texture ?: return
        transformed(rotationMatrix(f3 + 90f, f, f2)) { a(texture, f, f2, paint) }
    }

    override fun a(texture: Texture?, rect: Rect?, f: Float, f2: Float, f3: Float, paint: Paint?) {
        if (texture == null || rect == null) return
        transformed(rotationMatrix(f3 + 90f, f, f2)) {
            a(
                texture, rect, RectF(
                    f - rect.width() / 2f, f2 - rect.height() / 2f,
                    f + rect.width() / 2f, f2 + rect.height() / 2f
                ), paint
            )
        }
    }

    override fun a(texture: Texture?, rect: Rect?, rect2: Rect?, paint: Paint?) {
        if (rect2 != null) a(texture, rect, RectF(rect2), paint)
    }

    override fun a(texture: Texture?, rect: Rect?, rectF: RectF?, paint: Paint?) {
        if (texture == null || rectF == null) return
        drawImage(texture, rect?.toSkia(), rectF.toSkia(), paint)
    }

    override fun a(texture: Texture?, f: Float, f2: Float, paint: Paint?) {
        texture ?: return
        b(texture, f - texture.t, f2 - texture.u, paint)
    }

    override fun a(texture: Texture?, f: Float, f2: Float, paint: Paint?, f3: Float, f4: Float) {
        texture ?: return
        transformed(rotationMatrix(if (f3 == 0f) 0f else f3 + 90f, f, f2)) {
            a(f4, f4, f, f2)
            b(texture, f, f2, paint)
        }
    }

    override fun b(texture: Texture?, f: Float, f2: Float, paint: Paint?) {
        texture ?: return
        drawImage(texture, null, SkiaRect.makeXYWH(f, f2, texture.width().toFloat(), texture.height().toFloat()), paint)
    }

    override fun b(texture: Texture?, rect: Rect?, rect2: Rect?, paint: Paint?) = a(texture, rect, rect2, paint)

    override fun a(rect: Rect?, paint: Paint?) {
        rect?.let { drawRect(it.toSkia(), paint) }
    }

    override fun b(rect: Rect?, paint: Paint?) = a(rect, paint)
    override fun a(rectF: RectF?, paint: Paint?) {
        rectF?.let { drawRect(it.toSkia(), paint) }
    }

    override fun c(rect: Rect?, paint: Paint?) {
        rect ?: return
        drawRect(SkiaRect.makeXYWH(rect.a.toFloat(), rect.b.toFloat(), rect.c.toFloat(), rect.d.toFloat()), paint)
    }

    override fun a(texture: Texture?, rect: Rect?, paint: Paint?) = a(texture, rect, paint, 0, 0, 0, 0)
    override fun a(texture: Texture?, rect: Rect?, paint: Paint?, i: Int, i2: Int, i3: Int, i4: Int) {
        if (texture != null && rect != null) tile(texture, rect.toSkia(), paint, i.toFloat(), i2.toFloat(), i3, i4)
    }

    override fun a(texture: Texture?, rectF: RectF?, paint: Paint?, f: Float, f2: Float, i: Int, i2: Int) {
        if (texture != null && rectF != null) tile(texture, rectF.toSkia(), paint, f, f2, i, i2)
    }

    override fun b(i: Int) = a(i, GameCanvasBlendMode.SourceOver)
    override fun a(i: Int, mode: GameCanvasBlendMode?) {
        withCanvas { canvas ->
            val paint = paints.configure(
                null, colorOverride = i,
                blendOverride = (mode ?: GameCanvasBlendMode.SourceOver).toSkia()
            )
            canvas.drawPaint(paint)
        }
    }

    override fun a(str: String?, f: Float, f2: Float, paint: Paint?, paint2: Paint?, f3: Float) {
        str ?: return
        if (str.isEmpty() || '\n' !in str) {
            val shaped = resources.fonts.line(str, paint)
            val left = alignedX(f, shaped.width, paint)
            drawRect(SkiaRect(left - f3, f2 - f3, left + shaped.width + f3, f2 + shaped.height + f3), paint2)
            a(str, f, f2 - shaped.ascent, paint)
            return
        }
        val rows = resources.fonts.shapedLines(str, paint)
        val width = rows.maxOf { it?.width ?: 0f }
        val spacing = resources.fonts.lineSpacing(str, paint)
        val ascent = rows.firstOrNull()?.ascent ?: 0f
        val rowCount = str.count { it == '\n' } + 1
        val left = alignedX(f, width, paint)
        drawRect(
            SkiaRect(left - f3, f2 - f3, left + width + f3, f2 + spacing * rowCount + f3),
            paint2,
        )
        drawTextRows(rows, left, f2 - ascent, spacing, paint)
    }

    override fun a(str: String?, f: Float, f2: Float, paint: Paint?) {
        if (str.isNullOrEmpty()) return
        checkOpen()
        if ('\n' !in str) {
            val shaped = resources.fonts.line(str, paint)
            withCanvas { canvas ->
                shaped.blob?.let {
                    canvas.drawTextBlob(
                        it,
                        alignedX(f, shaped.width, paint),
                        f2,
                        paints.configure(paint)
                    )
                }
            }
            return
        }
        val rows = resources.fonts.shapedLines(str, paint)
        val width = rows.maxOf { it?.width ?: 0f }
        val left = alignedX(f, width, paint)
        drawTextRows(rows, left, f2, spacing = resources.fonts.lineSpacing(str, paint), paint)
    }

    override fun a(str: String?, paint: Paint?): Int {
        checkOpen()
        val text = str.orEmpty()
        val firstRow = if ('\n' in text) text.substringBefore('\n') else text
        return ceil(resources.fonts.line(firstRow, paint).height).toInt()
    }

    override fun b(str: String?, paint: Paint?): Int {
        checkOpen()
        val text = str.orEmpty()
        if ('\n' !in text) return ceil(resources.fonts.line(text, paint).width).toInt()
        return ceil(resources.fonts.shapedLines(text, paint).maxOf { it?.width ?: 0f }).toInt()
    }

    private fun drawTextRows(
        rows: Sequence<ShapedText?>,
        left: Float,
        firstBaseline: Float,
        spacing: Float,
        paint: Paint?,
    ) {
        withCanvas { canvas ->
            val nativePaint = paints.configure(paint)
            rows.forEachIndexed { index, row ->
                row?.blob?.let { canvas.drawTextBlob(it, left, firstBaseline + spacing * index, nativePaint) }
            }
        }
    }

    // Raster targets already provide immediate drawing; no separate CPU batch is necessary.
    override fun a(z: Boolean) {
        checkOpen()
    }

    override fun f() = p()
    override fun a(rect: Rect?) = clip(rect?.toSkia())
    override fun a(rectF: RectF?) = clip(rectF?.toSkia())
    override fun a(f: Float, f2: Float, f3: Float, paint: Paint?) {
        require(f3 >= 0f && f3.isFinite()) { "Circle radius must be finite and non-negative" }
        withCanvas { it.drawCircle(f, f2, f3, paints.configure(paint)) }
    }

    override fun b(f: Float, f2: Float, f3: Float, paint: Paint?) = a(f, f2, f3, paint)
    override fun a(fArr: FloatArray?, i: Int, i2: Int, paint: Paint?) {
        fArr ?: return
        require(i >= 0 && i2 >= 0 && i <= fArr.size - i2 && i % 2 == 0 && i2 % 2 == 0) { "Invalid point range" }
        if (i2 == 0) return
        withCanvas { canvas ->
            val nativePaint = paints.configure(paint)
            for (offset in i until i + i2 step 2) canvas.drawPoint(fArr[offset], fArr[offset + 1], nativePaint)
        }
    }

    override fun i() {
        checkOpen(); states.addLast(state)
    }

    override fun j() {
        checkOpen(); if (states.isNotEmpty()) state = states.removeLast()
    }

    override fun k() = i()
    override fun l() = j()
    override fun a(f: Float, f2: Float, f3: Float) = concat(rotationMatrix(f, f2, f3))
    override fun a(f: Float, f2: Float) = concat(Matrix33.makeScale(f, f2))
    override fun a(f: Float, f2: Float, f3: Float, f4: Float) {
        b(f3, f4); a(f, f2); b(-f3, -f4)
    }

    override fun b(f: Float, f2: Float) = concat(Matrix33.makeTranslate(f, f2))
    override fun a(f: Float, f2: Float, f3: Float, f4: Float, paint: Paint?) {
        withCanvas { it.drawLine(f, f2, f3, f4, paints.configure(paint)) }
    }

    override fun m(): Int = width
    override fun n(): Int = height
    override fun a(i: Int, i2: Int) {
        checkOpen()
        require(i > 0 && i2 > 0) { "Viewport dimensions must be positive" }
        if (target == null) {
            width = i; height = i2
        }
    }

    override fun o() = a(0, GameCanvasBlendMode.Clear)
    override fun p() {
        checkOpen(); target?.surface()?.flushAndSubmit()
    }

    override fun q() = close()
    override fun a(shaderProgram: ShaderProgram?) {
        checkOpen()
        if (shaderProgram != null) resources.shaderEffects.effectFor(shaderProgram)
    }

    override fun r(): Texture {
        checkOpen(); return resources.fallback
    }

    override fun a(texture: Texture?, file: File?) {
        if (texture == null || file == null) return
        checkOpen()
        val image = resources.resolve(texture).image()
        checkNotNull(image.encodeToData(EncodedImageFormat.PNG)).use { file.writeBytes(it.bytes) }
    }

    override fun close() {
        if (closed) return
        check(borrowedCanvas == null) { "Cannot close the renderer during a frame" }
        closed = true
        paints.close()
        states.clear()
        if (foreignTarget != null) target?.close()
        if (ownsResources) resources.close()
    }

    private fun drawRect(rect: SkiaRect, paint: Paint?) = withCanvas { it.drawRect(rect, paints.configure(paint)) }
    private fun drawImage(texture: Texture, source: SkiaRect?, destination: SkiaRect, paint: Paint?) {
        checkOpen()
        // Resolve the source before touching the destination: self-blits require copy-on-write.
        val image = resources.resolve(texture).image()
        val src = source ?: SkiaRect.makeWH(image.width.toFloat(), image.height.toFloat())
        if (src.isEmpty || destination.isEmpty) return
        withCanvas { canvas ->
            val sampling =
                if (paint?.isFilterBitmap() == true || texture.o) SamplingMode.LINEAR else SamplingMode.DEFAULT
            val nativePaint = paints.configure(paint, image = true)
            // When a shader paint is used the image must come through the
            // u_texture child shader for exact UV control, so draw the effect
            // over the destination rect instead of drawImageRect.
            val shader = resources.shaderEffects.shaderForDraw(paint, texture, image, src, destination, sampling) {
                runCatching { resources.resolve(it).image() }.getOrNull()
            }
            if (shader == null) {
                canvas.drawImageRect(image, src, destination, sampling, nativePaint, true)
                return@withCanvas
            }
            shader.use { effect ->
                nativePaint.shader = effect
                try {
                    canvas.drawRect(destination, nativePaint)
                } finally {
                    nativePaint.shader = null
                }
            }
        }
    }

    private fun tile(
        texture: Texture,
        rect: SkiaRect,
        paint: Paint?,
        offsetX: Float,
        offsetY: Float,
        overlapX: Int,
        overlapY: Int
    ) {
        if (rect.isEmpty) return
        require(offsetX.isFinite() && offsetY.isFinite()) { "Tile offsets must be finite" }
        val tw = texture.width().toFloat()
        val th = texture.height().toFloat()
        val stepX = tw - overlapX
        val stepY = th - overlapY
        if (tw <= 0f || th <= 0f || stepX <= 0f || stepY <= 0f) return
        val startX = rect.left - ((offsetX % tw + tw) % tw)
        val startY = rect.top - ((offsetY % th + th) % th)
        i()
        try {
            clip(rect)
            val columns = ceil((rect.right - startX) / stepX).toInt()
            val rows = ceil((rect.bottom - startY) / stepY).toInt()
            for (x in 0 until columns) for (y in 0 until rows) {
                drawImage(texture, null, SkiaRect.makeXYWH(startX + x * stepX, startY + y * stepY, tw, th), paint)
            }
        } finally {
            j()
        }
    }

    private fun clip(rect: SkiaRect?) {
        checkOpen()
        state = state.copy(clips = if (rect == null) emptyList() else state.clips + Clip(rect, state.matrix))
    }

    private fun concat(matrix: Matrix33) {
        checkOpen(); state = state.copy(matrix = state.matrix.makeConcat(matrix))
    }

    private inline fun transformed(matrix: Matrix33, draw: () -> Unit) {
        i()
        try {
            concat(matrix); draw()
        } finally {
            j()
        }
    }

    private inline fun withCanvas(draw: (Canvas) -> Unit) {
        checkOpen()
        if (foreignTarget != null && (foreignRevision != foreignTarget.getPixelRevision() ||
                    target!!.usesPremultipliedAlpha() != foreignTarget.usesPremultipliedAlpha())
        ) {
            foreignTarget.argbPixelsCopy?.let { target!!.setCommittedArgbPixels(it) }
            target!!.setPremultipliedAlpha(foreignTarget.usesPremultipliedAlpha())
        }
        val canvas = target?.surface()?.canvas
            ?: checkNotNull(borrowedCanvas) { "Drawing requires render() or a texture target" }
        // The borrowed frame owns a save level. Keep its native state until the
        // logical state changes; reading and rebuilding it for every sprite allocates
        // several matrix arrays and repeats JNI calls for the same clip.
        val restoreCount = if (target == null) {
            if (appliedState !== state) {
                canvas.restoreToCount(frameStateRestoreCount)
                canvas.save()
                applyState(canvas, frameBaseMatrix)
                appliedState = state
            }
            null
        } else if (state === INITIAL_STATE) {
            // The common terrain batch has no transforms or clips to install.
            null
        } else {
            canvas.save().also { applyState(canvas, canvas.localToDeviceAsMatrix33) }
        }
        try {
            draw(canvas)
        } finally {
            if (restoreCount != null) canvas.restoreToCount(restoreCount)
            target?.rendered()
            if (foreignTarget != null) {
                foreignTarget.setCommittedArgbPixels(checkNotNull(target!!.argbPixelsCopy))
                foreignTarget.setPremultipliedAlpha(false)
                foreignRevision = foreignTarget.getPixelRevision()
            }
        }
    }

    private fun applyState(canvas: Canvas, base: Matrix33) {
        for ((rect, matrix) in state.clips) {
            canvas.setMatrix(base.makeConcat(matrix))
            canvas.clipRect(rect, false)
        }
        if (state !== INITIAL_STATE) canvas.setMatrix(base.makeConcat(state.matrix))
    }

    private fun checkOpen() {
        check(!closed && !resources.isClosed) { "Renderer is closed" }
    }

    private fun alignedX(x: Float, width: Float, paint: Paint?): Float = when (paint?.textAlign()) {
        Paint.Align.CENTER -> x - width / 2f
        Paint.Align.RIGHT -> x - width
        else -> x
    }

    private data class Clip(val rect: SkiaRect, val matrix: Matrix33)
    private data class CanvasState(val matrix: Matrix33 = Matrix33.IDENTITY, val clips: List<Clip> = emptyList())

    private companion object {
        val INITIAL_STATE = CanvasState()
        val BACKEND_CAPABILITIES = GraphicsBackendCapabilities(
            requiresImageTintColorFilter = false,
            supportsSmoothFogLayerBuffers = false,
        )
    }
}

private fun Rect.toSkia() = SkiaRect(a.toFloat(), b.toFloat(), c.toFloat(), d.toFloat())
private fun RectF.toSkia() = SkiaRect(a, b, c, d)

private fun rotationMatrix(degrees: Float, pivotX: Float, pivotY: Float): Matrix33 {
    val radians = Math.toRadians(degrees.toDouble())
    val cosine = cos(radians).toFloat()
    val sine = sin(radians).toFloat()
    return Matrix33(
        cosine, -sine, pivotX - pivotX * cosine + pivotY * sine,
        sine, cosine, pivotY - pivotY * cosine - pivotX * sine,
        0f, 0f, 1f,
    )
}
