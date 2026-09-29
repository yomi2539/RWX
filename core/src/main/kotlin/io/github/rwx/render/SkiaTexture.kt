package io.github.rwx.render

import com.corrodinggames.rts.gameFramework.graphics.Texture
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Surface

/** A raster render target with lazy, unpremultiplied ARGB readback. Use on the render thread. */
class SkiaTexture internal constructor(
    width: Int,
    height: Int,
    hasAlpha: Boolean,
    private val createSibling: (Int, Int, Boolean) -> SkiaTexture,
    private val onClose: (SkiaTexture) -> Unit,
) : Texture(), AutoCloseable {
    private val surface = Surface.makeRasterN32Premul(width, height)
    private var snapshot: Image? = null
    private var uploadedRevision = 0
    private var uploadedPremultiplied = false
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
        check(!isClosed) { "Texture is closed" }
        if (uploadedRevision != getPixelRevision() || uploadedPremultiplied != usesPremultipliedAlpha()) {
            val pixels = argbPixelsCopy
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
            snapshot?.close()
            snapshot = null
            uploadedRevision = getPixelRevision()
            uploadedPremultiplied = usesPremultipliedAlpha()
        }
        return surface
    }

    internal fun image(): Image {
        surface()
        return snapshot ?: surface.makeImageSnapshot().also { snapshot = it }
    }

    internal fun rendered() {
        check(!isClosed) { "Texture is closed" }
        snapshot?.close()
        snapshot = null
        setPremultipliedAlpha(false)
        invalidateArgbPixelSnapshot { readPixels() }
        uploadedRevision = getPixelRevision()
        uploadedPremultiplied = false
    }

    private fun readPixels(): IntArray {
        check(!isClosed) { "Texture is closed" }
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
