package io.github.rwx.render

import com.corrodinggames.rts.gameFramework.graphics.Texture
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Picture
import org.jetbrains.skia.Surface

/** A raster render target with lazy, unpremultiplied ARGB readback. Use on the render thread. */
class SkiaTexture internal constructor(
    width: Int,
    height: Int,
    hasAlpha: Boolean,
    private val createSibling: (Int, Int, Boolean) -> SkiaTexture,
    private val atlasBuffers: SkiaAtlasBuffers,
    private val onClose: (SkiaTexture) -> Unit,
) : Texture(), AutoCloseable {
    private val surface = Surface.makeRasterN32Premul(width, height)
    private var snapshot: Image? = null
    private var uploadedRevision = 0
    private var uploadedPremultiplied = false
    private val pixelLoader = { readPixels() }
    internal var recordingEnabled = false
        private set
    private val atlasBatch = lazy(LazyThreadSafetyMode.NONE) { SkiaAtlasBatch(atlasBuffers) }
    private var recorded: SkiaRecordedTarget? = null
    private var rasterCurrent = true
    private var cleared = false
    var isClosed: Boolean = false
        private set

    init {
        p = width
        q = height
        m = hasAlpha
        updateCenter()
        surface.canvas.clear(if (hasAlpha) 0 else 0xff000000.toInt())
        rendered()
    }

    internal fun surface(): Surface {
        synchronizePixels()
        materializeRecording()
        return surface
    }

    private fun synchronizePixels() {
        check(!isClosed) { "Texture is closed" }
        if (uploadedRevision != getPixelRevision() || uploadedPremultiplied != usesPremultipliedAlpha()) {
            cleared = false
            val pixels = argbPixelsCopy
            discardRecording()
            discardSnapshot()
            if (pixels != null) {
                require(pixels.size == Math.multiplyExact(p, q)) { "Texture pixel count does not match its dimensions" }
                Bitmap().use { bitmap ->
                    val info = ImageInfo(
                        p, q, ColorType.RGBA_8888,
                        if (usesPremultipliedAlpha()) ColorAlphaType.PREMUL else ColorAlphaType.UNPREMUL
                    )
                    check(bitmap.installPixels(info, pixels.toRgbaBytes(), p * 4)) { "Cannot upload texture pixels" }
                    surface.writePixels(bitmap, 0, 0)
                }
            }
            uploadedRevision = getPixelRevision()
            uploadedPremultiplied = usesPremultipliedAlpha()
        }
    }

    internal fun enableRecording() {
        check(!isClosed) { "Texture is closed" }
        recordingEnabled = true
    }

    internal fun markCleared() {
        cleared = true
    }

    internal fun canReplaceRecording(): Boolean {
        synchronizePixels()
        return recordingEnabled && cleared
    }

    internal fun recordedPicture(): Picture? {
        synchronizePixels()
        return recorded?.snapshot()
    }

    internal fun canvasForDrawing(sourceImage: Image? = null, replaceContents: Boolean = false): Canvas {
        if (!recordingEnabled) return surfaceForDrawing(sourceImage).canvas
        return recordingForDrawing(sourceImage, replaceContents).canvas()
    }

    internal fun recordImage(
        image: Image, paint: org.jetbrains.skia.Paint,
        sl: Float, st: Float, sr: Float, sb: Float, dl: Float, dt: Float, dr: Float, db: Float,
    ): Boolean {
        // Advanced blend modes can need a destination read between overlapping
        // quads; preserve separate draws instead of merging them into one mesh.
        if (!recordingEnabled || paint.blendMode.ordinal > org.jetbrains.skia.BlendMode.SCREEN.ordinal) return false
        recordingForDrawing(image, false).addImage(image, paint, sl, st, sr, sb, dl, dt, dr, db)
        rendered()
        return true
    }

    private fun recordingForDrawing(sourceImage: Image?, replaceContents: Boolean): SkiaRecordedTarget {
        synchronizePixels()
        if (replaceContents) discardRecording()
        // Bound incremental histories such as scorch marks. Full terrain redraws
        // start with a clear and never need this CPU checkpoint.
        if (!replaceContents && recorded?.generations?.let { it >= 8 } == true) {
            materializeRecording()
            discardRecording()
        }
        val batch = recorded ?: SkiaRecordedTarget(p, q, if (replaceContents) null else image(), atlasBatch)
            .also { recorded = it }
        if (snapshot !== sourceImage) discardSnapshot()
        rasterCurrent = false
        return batch
    }

    internal fun flush() {
        if (recorded != null) recorded!!.snapshot() else surface().flushAndSubmit()
    }

    private fun materializeRecording() {
        if (rasterCurrent) return
        discardSnapshot()
        surface.canvas.clear(0)
        surface.canvas.drawPicture(checkNotNull(recorded).snapshot())
        rasterCurrent = true
    }

    private fun discardRecording() {
        recorded?.close()
        recorded = null
        rasterCurrent = true
    }

    internal fun surfaceForDrawing(sourceImage: Image? = null): Surface {
        val target = surface()
        discardRecording()
        // Drop our private reference before writing so Skia can reuse the pixels.
        // External readers (e.g. a recorded frame) still trigger native copy-on-write.
        // A self-blit needs this wrapper alive until drawImageRect has consumed it.
        if (snapshot !== sourceImage) discardSnapshot()
        return target
    }

    internal fun image(): Image {
        surface()
        return snapshot ?: surface.makeImageSnapshot().also { snapshot = it }
    }

    internal fun rendered() {
        check(!isClosed) { "Texture is closed" }
        cleared = false
        discardSnapshot()
        setPremultipliedAlpha(false)
        invalidateArgbPixelSnapshot(pixelLoader)
        uploadedRevision = getPixelRevision()
        uploadedPremultiplied = false
    }

    private fun discardSnapshot() {
        snapshot?.close()
        snapshot = null
    }

    private fun readPixels(): IntArray {
        check(!isClosed) { "Texture is closed" }
        materializeRecording()
        return Bitmap().use { bitmap ->
            val info = ImageInfo(p, q, ColorType.RGBA_8888, ColorAlphaType.UNPREMUL)
            check(bitmap.allocPixels(info)) { "Cannot allocate texture readback" }
            check(surface.readPixels(bitmap, 0, 0)) { "Cannot read texture pixels" }
            val bytes = checkNotNull(bitmap.readPixels(info, p * 4))
            IntArray(p * q) { index ->
                val offset = index * 4
                ((bytes[offset + 3].toInt() and 255) shl 24) or
                        ((bytes[offset].toInt() and 255) shl 16) or
                        ((bytes[offset + 1].toInt() and 255) shl 8) or
                        (bytes[offset + 2].toInt() and 255)
            }
        }
    }

    override fun clone(): SkiaTexture = a(p, q, true)

    override fun a(width: Int, height: Int, copyPixels: Boolean): SkiaTexture {
        check(!isClosed) { "Texture is closed" }
        val result = createSibling(width, height, m)
        result.o = o
        if (copyPixels) {
            val source = checkNotNull(argbPixelsCopy)
            val pixels = IntArray(width * height)
            for (y in 0 until minOf(height, q)) {
                source.copyInto(pixels, y * width, y * p, y * p + minOf(width, p))
            }
            result.setCommittedArgbPixels(pixels)
            result.setPremultipliedAlpha(usesPremultipliedAlpha())
        }
        return result
    }

    override fun a(x: Int, y: Int): Int {
        check(!isClosed) { "Texture is closed" }
        return super.a(x, y)
    }

    override fun a(x: Int, y: Int, color: Int) {
        check(!isClosed) { "Texture is closed" }
        super.a(x, y, color)
    }

    override fun p() {
        check(!isClosed) { "Texture is closed" }
        super.p()
    }

    override fun o() = close()

    override fun close() {
        if (isClosed) return
        isClosed = true
        snapshot?.close()
        snapshot = null
        discardRecording()
        surface.close()
        invalidateArgbPixelSnapshot()
        super.o()
        onClose(this)
    }
}

internal fun IntArray.toRgbaBytes(): ByteArray = ByteArray(Math.multiplyExact(size, 4)).also { bytes ->
    forEachIndexed { index, argb ->
        val offset = index * 4
        bytes[offset] = (argb ushr 16).toByte()
        bytes[offset + 1] = (argb ushr 8).toByte()
        bytes[offset + 2] = argb.toByte()
        bytes[offset + 3] = (argb ushr 24).toByte()
    }
}
