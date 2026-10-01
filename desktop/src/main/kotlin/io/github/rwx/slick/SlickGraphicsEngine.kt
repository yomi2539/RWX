package io.github.rwx.slick

import com.corrodinggames.rts.R
import com.corrodinggames.rts.gameFramework.Utility
import com.corrodinggames.rts.gameFramework.graphics.*
import com.corrodinggames.rts.gameFramework.utility.AssetInputStream
import io.github.rwx.PlatformStorage
import io.github.rwx.geometry.Rect
import io.github.rwx.geometry.RectF
import io.github.rwx.render.canvas.*
import io.github.rwx.render.frame.*
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20
import org.newdawn.slick.*
import org.newdawn.slick.font.effects.ColorEffect
import org.newdawn.slick.imageout.ImageOut
import org.newdawn.slick.opengl.renderer.Renderer
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.IdentityHashMap
import java.util.concurrent.locks.Lock
import kotlin.collections.ArrayDeque
import kotlin.math.max
import kotlin.math.roundToInt
import java.awt.Color as AwtColor
import java.awt.Font as AwtFont

class SlickGraphicsEngine private constructor(
    private val storage: PlatformStorage,
    private val targetTexture: SlickTexture? = null,
    private val legacyTextureCache: MutableMap<Int, LegacySlickTextureCacheEntry> = mutableMapOf(),
    private val shaderState: SlickShaderState = SlickShaderState(),
) : GraphicsEngine {
    constructor(storage: PlatformStorage) : this(
        storage = storage,
        targetTexture = null,
        legacyTextureCache = mutableMapOf(),
        shaderState = SlickShaderState(),
    )

    override fun backendCapabilities(): GraphicsBackendCapabilities = BACKEND_CAPABILITIES

    override fun supportsShaderEffects(): Boolean = true

    override fun supportsPostProcessing(): Boolean = false

    override fun supportsTeamShaders(): Boolean = true

    private var graphics: Graphics? = null
    private var width: Int = targetTexture?.width() ?: 1
    private var height: Int = targetTexture?.height() ?: 1
    private var transform = TransformState()
    private val transformStack = ArrayDeque<TransformState>()
    private val paintColorScratch = Color(1f, 1f, 1f, 1f)
    private val imageColorScratch = Color(1f, 1f, 1f, 1f)
    private val clearColorScratch = Color(1f, 1f, 1f, 1f)
    private val awtFontCache = mutableMapOf<SlickFontKey, AwtFont>()
    private val fontCache = mutableMapOf<SlickFontKey, UnicodeFont>()
    private val recentUnicodeTexts = mutableMapOf<SlickFontKey, ArrayDeque<String>>()
    private val fallbackTexture: SlickTexture by lazy {
        SlickTexture(Image(ImageBuffer(1, 1)), "fallback")
    }

    fun setGraphics(graphics: Graphics, width: Int, height: Int) {
        activateGraphics(graphics)
        this.graphics = graphics
        this.width = width.coerceAtLeast(1)
        this.height = height.coerceAtLeast(1)
        applyClip(graphics)
    }

    fun resetBackendState() {
        clearSlickShader()
        legacyTextureCache.values.forEach { it.texture.o() }
        legacyTextureCache.clear()
        shaderState.currentShader = null
        shaderState.bindings.clear()
    }

    override fun b(texture: Texture?): GraphicsEngine {
        val slickTexture = texture.asSlickTexture() ?: return SlickGraphicsEngine(
            storage,
            legacyTextureCache = legacyTextureCache,
            shaderState = shaderState,
        )
        val image = slickTexture.image()
        return if (image != null) {
            SlickGraphicsEngine(storage, slickTexture, legacyTextureCache, shaderState).also {
                it.setGraphics(image.graphics, image.width, image.height)
            }
        } else {
            SlickGraphicsEngine(storage, legacyTextureCache = legacyTextureCache, shaderState = shaderState)
        }
    }

    override fun a(lock: Lock?) {
        lock?.lock()
    }

    override fun b(lock: Lock?) {
        lock?.unlock()
    }

    override fun a(i: Int): Texture = a(i, true)

    override fun a(i: Int, z: Boolean): Texture {
        val name = drawableNamesById[i] ?: return fallbackTexture
        val path = drawableAssetPath(name) ?: return fallbackTexture
        val bytes = storage.readAssetBytes(path) ?: return fallbackTexture
        return createTexture(bytes, "drawable/$name")
    }

    override fun a(inputStream: InputStream?, z: Boolean): Texture {
        val sourceKey = (inputStream as? AssetInputStream)?.getPath()
        val bytes = inputStream?.readBytes() ?: return fallbackTexture
        return createTexture(bytes, sourceKey ?: "stream/${System.identityHashCode(inputStream)}")
    }

    override fun a(i: Int, i2: Int, z: Boolean): Texture =
        try {
            SlickTexture(Image(i.coerceAtLeast(1), i2.coerceAtLeast(1)))
        } catch (error: SlickException) {
            throw RuntimeException(error)
        }

    override fun b(i: Int, i2: Int, z: Boolean): Texture {
        val image = Image(ImageBuffer(i.coerceAtLeast(1), i2.coerceAtLeast(1)))
        return SlickTexture(image)
    }

    override fun a(texture: Texture?, f: Float, f2: Float, f3: Float, paint: Paint?) {
        val resolved = texture ?: return
        val image = resolved.asSlickTexture()?.image() ?: return
        drawImageRegion(
            image = image,
            srcLeft = 0,
            srcTop = 0,
            srcRight = image.width,
            srcBottom = image.height,
            dstLeft = f - resolved.r,
            dstTop = f2 - resolved.s,
            dstRight = f - resolved.r + image.width,
            dstBottom = f2 - resolved.s + image.height,
            paint = paint,
            extraRotationDegrees = f3 + 90f,
            pivotX = f,
            pivotY = f2,
            shaderTexture = resolved,
        )
    }

    override fun a(texture: Texture?, rect: Rect?, f: Float, f2: Float, f3: Float, paint: Paint?) {
        if (rect == null) return
        val image = texture.asSlickTexture()?.image() ?: return
        drawImageRegion(
            image = image,
            srcLeft = rect.a,
            srcTop = rect.b,
            srcRight = rect.c,
            srcBottom = rect.d,
            dstLeft = f - rect.width() / 2f,
            dstTop = f2 - rect.height() / 2f,
            dstRight = f + rect.width() / 2f,
            dstBottom = f2 + rect.height() / 2f,
            paint = paint,
            extraRotationDegrees = f3,
            pivotX = f,
            pivotY = f2,
            shaderTexture = texture,
        )
    }

    override fun a(texture: Texture?, rect: Rect?, rect2: Rect?, paint: Paint?) {
        val image = texture.asSlickTexture()?.image() ?: return
        val src = rect ?: return
        val dst = rect2 ?: return
        drawImageRegion(
            image = image,
            srcLeft = src.a,
            srcTop = src.b,
            srcRight = src.c,
            srcBottom = src.d,
            dstLeft = dst.a.toFloat(),
            dstTop = dst.b.toFloat(),
            dstRight = dst.c.toFloat(),
            dstBottom = dst.d.toFloat(),
            paint = paint,
            shaderTexture = texture,
        )
    }

    override fun a(texture: Texture?, rect: Rect?, rectF: RectF?, paint: Paint?) {
        val image = texture.asSlickTexture()?.image() ?: return
        val dst = rectF ?: return
        val src = rect ?: run {
            drawImageRegion(
                image = image,
                srcLeft = 0,
                srcTop = 0,
                srcRight = image.width,
                srcBottom = image.height,
                dstLeft = dst.a,
                dstTop = dst.b,
                dstRight = dst.c,
                dstBottom = dst.d,
                paint = paint,
                shaderTexture = texture,
            )
            return
        }
        drawImageRegion(
            image = image,
            srcLeft = src.a,
            srcTop = src.b,
            srcRight = src.c,
            srcBottom = src.d,
            dstLeft = dst.a,
            dstTop = dst.b,
            dstRight = dst.c,
            dstBottom = dst.d,
            paint = paint,
            shaderTexture = texture,
        )
    }

    override fun a(texture: Texture?, f: Float, f2: Float, paint: Paint?) {
        val resolved = texture ?: return
        val image = resolved.asSlickTexture()?.image() ?: return
        drawImageRegion(
            image = image,
            srcLeft = 0,
            srcTop = 0,
            srcRight = image.width,
            srcBottom = image.height,
            dstLeft = f - resolved.t,
            dstTop = f2 - resolved.u,
            dstRight = f - resolved.t + image.width,
            dstBottom = f2 - resolved.u + image.height,
            paint = paint,
            shaderTexture = resolved,
        )
    }

    override fun a(texture: Texture?, f: Float, f2: Float, paint: Paint?, f3: Float, f4: Float) {
        val image = texture.asSlickTexture()?.image() ?: return
        drawImageRegion(
            image = image,
            srcLeft = 0,
            srcTop = 0,
            srcRight = image.width,
            srcBottom = image.height,
            dstLeft = f,
            dstTop = f2,
            dstRight = f + (image.width * f4),
            dstBottom = f2 + (image.height * f4),
            paint = paint,
            extraRotationDegrees = f3,
            pivotX = f,
            pivotY = f2,
            shaderTexture = texture,
        )
    }

    override fun b(texture: Texture?, f: Float, f2: Float, paint: Paint?) {
        val image = texture.asSlickTexture()?.image() ?: return
        drawImageRegion(
            image = image,
            srcLeft = 0,
            srcTop = 0,
            srcRight = image.width,
            srcBottom = image.height,
            dstLeft = f,
            dstTop = f2,
            dstRight = f + image.width,
            dstBottom = f2 + image.height,
            paint = paint,
            shaderTexture = texture,
        )
    }

    override fun b(texture: Texture?, rect: Rect?, rect2: Rect?, paint: Paint?) {
        a(texture, rect, rect2, paint)
    }

    override fun a(rect: Rect?, paint: Paint?) {
        if (rect == null) return
        drawRect(rect.a.toFloat(), rect.b.toFloat(), rect.width().toFloat(), rect.height().toFloat(), paint)
    }

    override fun a(texture: Texture?, rect: Rect?, paint: Paint?) {
        if (texture == null || rect == null) return
        tileTexture(texture, rect, paint, 0, 0, 0, 0)
    }

    override fun a(texture: Texture?, rect: Rect?, paint: Paint?, i: Int, i2: Int, i3: Int, i4: Int) {
        if (texture == null || rect == null) return
        tileTexture(texture, rect, paint, i, i2, i3, i4)
    }

    override fun a(texture: Texture?, rectF: RectF?, paint: Paint?, f: Float, f2: Float, i: Int, i2: Int) {
        if (texture == null || rectF == null) return
        tileTexture(texture, rectF, paint, f, f2, i, i2)
    }

    override fun b(i: Int) {
        val g = activeGraphics() ?: return
        clearSlickShader(g)
        g.background = writeResolvedColor(i, clearColorScratch)
        g.clear()
    }

    override fun a(i: Int, mode: GameCanvasBlendMode?) {
        when (mode) {
            GameCanvasBlendMode.Clear -> {
                b(i)
                runCatching { activeGraphics()?.clearAlphaMap() }
            }

            GameCanvasBlendMode.ClearAlpha -> {
                runCatching { activeGraphics()?.clearAlphaMap() }
            }

            else -> b(i)
        }
    }

    override fun a(str: String?, f: Float, f2: Float, paint: Paint?, paint2: Paint?, f3: Float) {
        val text = str ?: return
        val textWidth = b(text, paint).toFloat()
        val textHeight = a(text, paint).toFloat()
        val left = when (paint?.j()) {
            Paint.Align.CENTER -> f - (textWidth * 0.5f) - f3
            Paint.Align.RIGHT -> f - textWidth - f3
            else -> f - f3
        }
        val backgroundRect = RectF(left, f2 - f3, left + textWidth + (f3 * 2f), f2 + textHeight + f3)
        a(backgroundRect, paint2)
        a(text, f, f2 + textHeight, paint)
    }

    override fun a(str: String?, f: Float, f2: Float, paint: Paint?) {
        val g = activeGraphics() ?: return
        val text = str ?: return
        val font = fontForText(paint, text)
        val oldFont = g.font
        withPaint(paint) {
            g.font = font
            g.drawString(text, transform.x(alignedX(text, f, paint)), transform.y(f2 - (paint?.k() ?: 16f)))
        }
        g.font = oldFont
    }

    override fun b(rect: Rect?, paint: Paint?) {
        a(rect, paint)
    }

    override fun a(z: Boolean) = Unit

    override fun f() = Unit

    override fun a(rectF: RectF?, paint: Paint?) {
        if (rectF == null) return
        drawRect(rectF.a, rectF.b, rectF.width(), rectF.height(), paint)
    }

    override fun c(rect: Rect?, paint: Paint?) {
        if (rect == null) return
        drawRect(rect.a.toFloat(), rect.b.toFloat(), rect.c.toFloat(), rect.d.toFloat(), paint)
    }

    override fun a(rect: Rect?) {
        transform.clip = rect?.let { transform.rect(RectF(it)) }
        applyClip()
    }

    override fun a(rectF: RectF?) {
        transform.clip = rectF?.let { transform.rect(it) }
        applyClip()
    }

    override fun a(f: Float, f2: Float, f3: Float, paint: Paint?) {
        val g = activeGraphics() ?: return
        withPaint(paint) {
            val x = transform.x(f - f3)
            val y = transform.y(f2 - f3)
            val width = f3 * 2f * transform.scaleX
            val height = f3 * 2f * transform.scaleY
            val style = paint?.d() ?: Paint.Style.FILL
            if (style != Paint.Style.STROKE) g.fillOval(x, y, width, height)
            if (style != Paint.Style.FILL) g.drawOval(x, y, width, height)
        }
    }

    override fun b(f: Float, f2: Float, f3: Float, paint: Paint?) {
        a(f, f2, f3, paint)
    }

    override fun a(fArr: FloatArray?, i: Int, i2: Int, paint: Paint?) {
        val points = fArr ?: return
        val g = activeGraphics() ?: return
        if (i2 <= 0) return
        var offset = i.coerceAtLeast(0)
        val end = minOf(points.size - 1, i + i2)
        val size = (paint?.g() ?: 1f).coerceAtLeast(1f)
        withPaint(paint) {
            while (offset + 1 <= end) {
                g.fillRect(
                    transform.x(points[offset]),
                    transform.y(points[offset + 1]),
                    size * transform.scaleX,
                    size * transform.scaleY
                )
                offset += 2
            }
        }
    }

    override fun i() {
        transformStack.addLast(transform.copy())
    }

    override fun j() {
        if (transformStack.isNotEmpty()) {
            transform = transformStack.removeLast()
            applyClip()
        }
    }

    override fun k() = i()

    override fun l() = j()

    override fun a(f: Float, f2: Float, f3: Float) {
        transform.rotationDegrees += f
        transform.rotationPivotX = f2
        transform.rotationPivotY = f3
    }

    override fun a(f: Float, f2: Float) {
        transform.scaleX *= f
        transform.scaleY *= f2
    }

    override fun a(f: Float, f2: Float, f3: Float, f4: Float) {
        transform.translate(f3, f4)
        transform.scaleX *= f
        transform.scaleY *= f2
        transform.translate(-f3, -f4)
    }

    override fun b(f: Float, f2: Float) {
        transform.translate(f, f2)
    }

    override fun a(f: Float, f2: Float, f3: Float, f4: Float, paint: Paint?) {
        withPaint(paint) {
            activeGraphics()?.drawLine(transform.x(f), transform.y(f2), transform.x(f3), transform.y(f4))
        }
    }

    override fun m(): Int = targetTexture?.width() ?: width

    override fun n(): Int = targetTexture?.height() ?: height

    override fun a(i: Int, i2: Int) {
        width = i.coerceAtLeast(1)
        height = i2.coerceAtLeast(1)
    }

    override fun o() {
        val g = activeGraphics() ?: return
        clearSlickShader(g)
        g.clear()
    }

    override fun p() {
        runCatching {
            val g = activeGraphics()
            runCatching { g?.flushBuffer() }
            g?.flush()
        }
        targetTexture?.p()
    }

    override fun q() {
        clearSlickShader()
    }

    override fun a(shaderProgram: ShaderProgram?) {
        val g = activeGraphics() ?: return
        g.flushBuffer()
        if (shaderProgram != null) {
            compileSlickShader(shaderProgram, shaderState.bindings.getOrPut(shaderProgram) { SlickShaderBinding() })
        }
        useNoSlickShader()
        shaderState.currentShader = null
    }

    override fun a(str: String?, paint: Paint?): Int =
        fontForText(paint, str ?: "").getLineHeight()

    override fun b(str: String?, paint: Paint?): Int =
        fontForText(paint, str ?: "").getWidth(str ?: "")

    override fun r(): Texture = fallbackTexture

    override fun a(texture: Texture?, file: File?) {
        val image = texture.asSlickTexture()?.image() ?: return
        val target = file ?: return
        ImageOut.write(image, target.absolutePath)
    }

    private fun createTexture(bytes: ByteArray, sourceKey: String): Texture =
        try {
            SlickTexture(Image(ByteArrayInputStream(bytes), sourceKey, false), sourceKey)
        } catch (_: SlickException) {
            fallbackTexture
        }

    private fun drawRect(x: Float, y: Float, width: Float, height: Float, paint: Paint?) {
        val g = activeGraphics() ?: return
        val left = transform.x(x)
        val top = transform.y(y)
        val right = transform.x(x + width)
        val bottom = transform.y(y + height)
        withPaint(paint) {
            if (paint?.d() == Paint.Style.STROKE) {
                g.drawRect(left, top, right - left, bottom - top)
            } else {
                g.fillRect(left, top, right - left, bottom - top)
            }
        }
    }

    private fun drawImageRegion(
        image: Image,
        srcLeft: Int,
        srcTop: Int,
        srcRight: Int,
        srcBottom: Int,
        dstLeft: Float,
        dstTop: Float,
        dstRight: Float,
        dstBottom: Float,
        paint: Paint?,
        extraRotationDegrees: Float = 0f,
        pivotX: Float = transform.rotationPivotX,
        pivotY: Float = transform.rotationPivotY,
        shaderTexture: Texture? = null,
    ) {
        val g = activeGraphics() ?: return
        var left = transform.x(dstLeft)
        var top = transform.y(dstTop)
        var right = transform.x(dstRight)
        var bottom = transform.y(dstBottom)
        if (left > right) {
            val swap = left
            left = right
            right = swap
        }
        if (top > bottom) {
            val swap = bottom
            bottom = top
            top = swap
        }
        val rotationDegrees = transform.rotationDegrees + 90f + extraRotationDegrees
        val pivotXTransformed = transform.x(pivotX)
        val pivotYTransformed = transform.y(pivotY)
        val imageColor = colorForImage(paint)
        val shader = slickShaderFor(shaderTexture, paint)
        withSlickBlendModeForPaint(g, paint) {
            withSlickShader(g, shader, paint, shaderTexture) {
                drawTexturedQuad(
                    graphics = g,
                    image = image,
                    srcLeft = srcLeft,
                    srcTop = srcTop,
                    srcRight = srcRight,
                    srcBottom = srcBottom,
                    dstLeft = left,
                    dstTop = top,
                    dstRight = right,
                    dstBottom = bottom,
                    rotationDegrees = rotationDegrees,
                    pivotX = pivotXTransformed,
                    pivotY = pivotYTransformed,
                    color = imageColor,
                )
            }
        }
        if (imageColor.a < 0.999f) {
            g.flushBuffer()
        }
    }

    private fun drawTexturedQuad(
        graphics: Graphics,
        image: Image,
        srcLeft: Int,
        srcTop: Int,
        srcRight: Int,
        srcBottom: Int,
        dstLeft: Float,
        dstTop: Float,
        dstRight: Float,
        dstBottom: Float,
        rotationDegrees: Float,
        pivotX: Float,
        pivotY: Float,
        color: Color,
    ) {
        Graphics.setCurrent(graphics)
        val imageWidth = image.width
        val imageHeight = image.height
        val texture = image.texture ?: return
        color.bind()
        texture.bind()
        val textureWidthScale = image.textureWidth / imageWidth
        val textureHeightScale = image.textureHeight / imageHeight
        val leftU = srcLeft * textureWidthScale
        val topV = srcTop * textureHeightScale
        val rightU = srcRight * textureWidthScale
        val bottomV = srcBottom * textureHeightScale
        var topLeftX = dstLeft
        var topLeftY = dstTop
        var bottomLeftX = dstLeft
        var bottomLeftY = dstBottom
        var bottomRightX = dstRight
        var bottomRightY = dstBottom
        var topRightX = dstRight
        var topRightY = dstTop
        if (rotationDegrees != 0f) {
            val cos = Utility.fastCos(rotationDegrees)
            val sin = Utility.fastSin(rotationDegrees)

            var dx = topLeftX - pivotX
            var dy = topLeftY - pivotY
            topLeftX = (dx * cos) - (dy * sin) + pivotX
            topLeftY = (dx * sin) + (dy * cos) + pivotY

            dx = bottomLeftX - pivotX
            dy = bottomLeftY - pivotY
            bottomLeftX = (dx * cos) - (dy * sin) + pivotX
            bottomLeftY = (dx * sin) + (dy * cos) + pivotY

            dx = bottomRightX - pivotX
            dy = bottomRightY - pivotY
            bottomRightX = (dx * cos) - (dy * sin) + pivotX
            bottomRightY = (dx * sin) + (dy * cos) + pivotY

            dx = topRightX - pivotX
            dy = topRightY - pivotY
            topRightX = (dx * cos) - (dy * sin) + pivotX
            topRightY = (dx * sin) + (dy * cos) + pivotY
        }
        val renderer = Renderer.get()
        renderer.glBegin(GL11.GL_QUADS)
        renderer.glTexCoord2f(leftU, topV)
        renderer.glVertex3f(topLeftX, topLeftY, 0f)
        renderer.glTexCoord2f(leftU, bottomV)
        renderer.glVertex3f(bottomLeftX, bottomLeftY, 0f)
        renderer.glTexCoord2f(rightU, bottomV)
        renderer.glVertex3f(bottomRightX, bottomRightY, 0f)
        renderer.glTexCoord2f(rightU, topV)
        renderer.glVertex3f(topRightX, topRightY, 0f)
        renderer.glEnd()
        graphics.bindCurrentColor()
    }

    private fun tileTexture(
        texture: Texture,
        rect: Rect,
        paint: Paint?,
        offsetX: Int,
        offsetY: Int,
        trimRight: Int,
        trimBottom: Int,
    ) {
        val image = texture.asSlickTexture()?.image() ?: return
        val tileWidth = texture.m().coerceAtLeast(1)
        val tileHeight = texture.l().coerceAtLeast(1)
        var normalizedOffsetX = offsetX
        var normalizedOffsetY = offsetY
        if (normalizedOffsetX != 0) {
            normalizedOffsetX %= tileWidth
            if (normalizedOffsetX < 0) normalizedOffsetX += tileWidth
        }
        if (normalizedOffsetY != 0) {
            normalizedOffsetY %= tileHeight
            if (normalizedOffsetY < 0) normalizedOffsetY += tileHeight
        }
        val stepX = tileWidth - trimRight
        val stepY = tileHeight - trimBottom
        if (stepX <= 0 || stepY <= 0) return

        var drawX = rect.a - normalizedOffsetX
        var tileCount = 0
        while (drawX < rect.c) {
            var drawY = rect.b - normalizedOffsetY
            while (drawY < rect.d) {
                if (++tileCount > MAX_TILED_DRAWS) return
                var srcLeft = 0
                var srcTop = 0
                var dstLeft = drawX
                var dstTop = drawY
                var drawWidth = minOf(tileWidth, rect.c - drawX)
                var drawHeight = minOf(tileHeight, rect.d - drawY)
                if (dstLeft < rect.a) {
                    val clip = rect.a - dstLeft
                    srcLeft += clip
                    dstLeft += clip
                    drawWidth -= clip
                }
                if (dstTop < rect.b) {
                    val clip = rect.b - dstTop
                    srcTop += clip
                    dstTop += clip
                    drawHeight -= clip
                }
                if (drawWidth > 0 && drawHeight > 0) {
                    drawImageRegion(
                        image = image,
                        srcLeft = srcLeft,
                        srcTop = srcTop,
                        srcRight = srcLeft + drawWidth,
                        srcBottom = srcTop + drawHeight,
                        dstLeft = dstLeft.toFloat(),
                        dstTop = dstTop.toFloat(),
                        dstRight = (dstLeft + drawWidth).toFloat(),
                        dstBottom = (dstTop + drawHeight).toFloat(),
                        paint = paint,
                        shaderTexture = texture,
                    )
                }
                drawY += stepY
            }
            drawX += stepX
        }
    }

    private fun tileTexture(
        texture: Texture,
        rect: RectF,
        paint: Paint?,
        offsetX: Float,
        offsetY: Float,
        trimRight: Int,
        trimBottom: Int,
    ) {
        val image = texture.asSlickTexture()?.image() ?: return
        val tileWidth = texture.m().coerceAtLeast(1)
        val tileHeight = texture.l().coerceAtLeast(1)
        var normalizedOffsetX = offsetX
        var normalizedOffsetY = offsetY
        if (normalizedOffsetX != 0f) {
            normalizedOffsetX %= tileWidth.toFloat()
            if (normalizedOffsetX < 0f) normalizedOffsetX += tileWidth
        }
        if (normalizedOffsetY != 0f) {
            normalizedOffsetY %= tileHeight.toFloat()
            if (normalizedOffsetY < 0f) normalizedOffsetY += tileHeight
        }
        val stepX = tileWidth - trimRight
        val stepY = tileHeight - trimBottom
        if (stepX <= 0 || stepY <= 0) return

        var drawX = rect.a - normalizedOffsetX
        var tileCount = 0
        while (drawX < rect.c) {
            var drawY = rect.b - normalizedOffsetY
            while (drawY < rect.d) {
                if (++tileCount > MAX_TILED_DRAWS) return
                var srcLeft = 0
                var srcTop = 0
                var dstLeft = drawX
                var dstTop = drawY
                var drawWidth = minOf(tileWidth.toFloat(), rect.c - drawX)
                var drawHeight = minOf(tileHeight.toFloat(), rect.d - drawY)
                if (dstLeft < rect.a) {
                    val clip = rect.a - dstLeft
                    srcLeft += clip.toInt()
                    dstLeft += clip
                    drawWidth -= clip
                }
                if (dstTop < rect.b) {
                    val clip = rect.b - dstTop
                    srcTop += clip.toInt()
                    dstTop += clip
                    drawHeight -= clip
                }
                if (drawWidth > 0f && drawHeight > 0f) {
                    drawImageRegion(
                        image = image,
                        srcLeft = srcLeft,
                        srcTop = srcTop,
                        srcRight = srcLeft + drawWidth.toInt(),
                        srcBottom = srcTop + drawHeight.toInt(),
                        dstLeft = dstLeft,
                        dstTop = dstTop,
                        dstRight = dstLeft + drawWidth,
                        dstBottom = dstTop + drawHeight,
                        paint = paint,
                        shaderTexture = texture,
                    )
                }
                drawY += stepY
            }
            drawX += stepX
        }
    }

    private inline fun withPaint(paint: Paint?, block: () -> Unit) {
        val g = activeGraphics() ?: return
        clearSlickShader(g)
        g.setColorRaw(colorForPaint(paint))
        val desiredLineWidth = (paint?.g() ?: 1f).coerceAtLeast(1f)
        if (g.lineWidth != desiredLineWidth) {
            g.lineWidth = desiredLineWidth
        }
        withSlickBlendModeForPaint(g, paint, block)
    }

    private inline fun withSlickShader(
        graphics: Graphics,
        shaderProgram: ShaderProgram?,
        paint: Paint?,
        texture: Texture?,
        block: () -> Unit,
    ) {
        if (shaderProgram == null) {
            clearSlickShader(graphics)
            block()
            return
        }
        shaderProgram.f()
        val binding = shaderState.bindings.getOrPut(shaderProgram) { SlickShaderBinding() }
        val sourcesChanged = binding.sources != (shaderProgram.vertexSource to shaderProgram.fragmentSource)
        if (shaderState.currentShader !== shaderProgram || sourcesChanged) {
            graphics.flushBuffer()
            if (!compileSlickShader(shaderProgram, binding)) {
                useNoSlickShader()
                shaderState.currentShader = null
                block()
                return
            }
            shaderProgram.a(paint, texture)
            uploadShaderUniforms(shaderProgram, binding)
            shaderState.currentShader = shaderProgram
        } else {
            val callbackChanged = shaderProgram.a(paint, texture)
            if (callbackChanged || binding.hasUniformChanges(shaderProgram)) {
                graphics.flushBuffer()
                uploadShaderUniforms(shaderProgram, binding)
            }
        }
        block()
    }

    private fun slickShaderFor(texture: Texture?, paint: Paint?): ShaderProgram? {
        val paintShader = (paint as? GamePaint)?.shaderProgram()
        if (paintShader != null) {
            return paintShader
        }
        return texture?.B()
    }

    private fun clearSlickShader(graphics: Graphics? = activeGraphics()) {
        if (shaderState.currentShader == null) {
            return
        }
        graphics?.flushBuffer()
        useNoSlickShader()
        shaderState.currentShader = null
    }

    private fun useNoSlickShader() {
        GL20.glUseProgram(0)
    }

    private fun uploadShaderUniforms(shaderProgram: ShaderProgram, binding: SlickShaderBinding) {
        var textureUnit = 1
        for (shaderUniform in shaderProgram.uniforms) {
            val uniformSnapshot = shaderUniform.toSlickSnapshot()
            val uniformLocation = binding.uniformLocations.getOrPut(shaderUniform) {
                GL20.glGetUniformLocation(binding.programId, shaderUniform.name)
            }
            if (uniformLocation == -1) {
                binding.uniformSnapshots[shaderUniform] = uniformSnapshot
                if (binding.missingUniforms.add(shaderUniform)) {
                    shaderProgram.b("Unknown parameter: " + shaderUniform.name)
                    val uniformCount = GL20.glGetProgrami(binding.programId, GL20.GL_ACTIVE_UNIFORMS)
                    for (i in 0 until uniformCount) {
                        val uniformSize = BufferUtils.createIntBuffer(1)
                        val uniformType = BufferUtils.createIntBuffer(1)
                        shaderProgram.b(
                            "Possible parameter: " + GL20.glGetActiveUniform(
                                binding.programId,
                                i,
                                uniformSize,
                                uniformType
                            )
                        )
                    }
                }
                continue
            }
            val uniformTexture = shaderUniform.texture
            if (uniformTexture != null) {
                val slickTexture = uniformTexture.asSlickTexture()?.image()?.texture ?: continue
                if (shaderUniform.g) {
                    GL20.glUniform2f(
                        uniformLocation,
                        slickTexture.getTextureWidth().toFloat(),
                        slickTexture.getTextureHeight().toFloat(),
                    )
                } else {
                    GL20.glUniform1i(uniformLocation, textureUnit)
                    GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureUnit++)
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, slickTexture.getTextureID())
                    val wrap = if (shaderUniform.repeatTexture) GL11.GL_REPEAT else GL12.GL_CLAMP_TO_EDGE
                    val filter = if (shaderUniform.linearTexture) GL11.GL_LINEAR else GL11.GL_NEAREST
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, wrap)
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, wrap)
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter)
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter)
                    GL13.glActiveTexture(GL13.GL_TEXTURE0)
                }
            } else {
                when (shaderUniform.valueType) {
                    ShaderUniformValueType.INTEGER -> GL20.glUniform1i(
                        uniformLocation,
                        shaderUniform.floatValues[0].toInt()
                    )
                    ShaderUniformValueType.MATRIX4 -> {
                        val values = BufferUtils.createFloatBuffer(16)
                        values.put(shaderUniform.floatValues).flip()
                        GL20.glUniformMatrix4fv(uniformLocation, false, values)
                    }

                    ShaderUniformValueType.FLOAT -> when (shaderUniform.floatValues.size) {
                        1 -> GL20.glUniform1f(uniformLocation, shaderUniform.floatValues[0])
                        2 -> GL20.glUniform2f(
                            uniformLocation,
                            shaderUniform.floatValues[0],
                            shaderUniform.floatValues[1]
                        )
                        3 -> GL20.glUniform3f(
                            uniformLocation,
                            shaderUniform.floatValues[0],
                            shaderUniform.floatValues[1],
                            shaderUniform.floatValues[2],
                        )

                        4 -> GL20.glUniform4f(
                            uniformLocation,
                            shaderUniform.floatValues[0],
                            shaderUniform.floatValues[1],
                            shaderUniform.floatValues[2],
                            shaderUniform.floatValues[3],
                        )

                        else -> shaderProgram.b(
                            "Unhandled parameter size: " + shaderUniform.name + " - " + shaderUniform.floatValues.size
                        )
                    }
                }
            }
            binding.uniformSnapshots[shaderUniform] = uniformSnapshot
        }
    }

    private fun compileSlickShader(shaderProgram: ShaderProgram, binding: SlickShaderBinding): Boolean {
        if (shaderProgram.programStatus != 0) {
            return false
        }
        val sources = shaderProgram.vertexSource to shaderProgram.fragmentSource
        if (binding.programId != 0 && binding.sources == sources) {
            GL20.glUseProgram(binding.programId)
            return true
        }
        shaderProgram.b("Compiling shader")
        val vertexShader = compileSlickShaderPart(shaderProgram, GL20.GL_VERTEX_SHADER, shaderProgram.vertexSource)
        val fragmentShader =
            compileSlickShaderPart(shaderProgram, GL20.GL_FRAGMENT_SHADER, shaderProgram.fragmentSource)
        if (shaderProgram.programStatus != 0) {
            return false
        }
        val programId = GL20.glCreateProgram()
        if (programId == 0) {
            shaderProgram.c("could not create program; check ShaderProgram.isSupported()")
            return false
        }
        GL20.glAttachShader(programId, vertexShader)
        GL20.glAttachShader(programId, fragmentShader)
        GL20.glLinkProgram(programId)
        val linkStatus = GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS)
        val infoLog = GL20.glGetProgramInfoLog(programId, GL20.glGetProgrami(programId, GL20.GL_INFO_LOG_LENGTH))
        if (!infoLog.isNullOrEmpty()) {
            shaderProgram.d = infoLog + "\n" + shaderProgram.d
        }
        shaderProgram.d = shaderProgram.d?.trim() ?: ""
        if (linkStatus == 0) {
            shaderProgram.c(if (shaderProgram.d.isNotEmpty()) shaderProgram.d else "Could not link program")
            return false
        }
        binding.programId = programId
        binding.sources = sources
        binding.uniformLocations.clear()
        binding.missingUniforms.clear()
        binding.uniformSnapshots.clear()
        GL20.glUseProgram(programId)
        return true
    }

    private fun compileSlickShaderPart(shaderProgram: ShaderProgram, type: Int, source: String): Int {
        val shader = GL20.glCreateShader(type)
        if (shader == 0) {
            shaderProgram.c("could not create shader object; check ShaderProgram.isSupported()")
            return shader
        }
        GL20.glShaderSource(shader, source)
        GL20.glCompileShader(shader)
        val compileStatus = GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS)
        val infoLog = GL20.glGetShaderInfoLog(shader, GL20.glGetShaderi(shader, GL20.GL_INFO_LOG_LENGTH))
        if (!infoLog.isNullOrEmpty()) {
            shaderProgram.d += shaderName(type) + " compile log:\n" + infoLog + "\n"
        }
        if (compileStatus == 0) {
            shaderProgram.c(
                if (shaderProgram.d.isNotEmpty()) shaderProgram.d else "Could not compile " + shaderName(
                    type
                )
            )
        }
        return shader
    }

    private fun shaderName(type: Int): String =
        when (type) {
            GL20.GL_FRAGMENT_SHADER -> "FRAGMENT_SHADER"
            GL20.GL_VERTEX_SHADER -> "VERTEX_SHADER"
            else -> "shader"
        }

    private fun colorForPaint(paint: Paint?): Color {
        return writeResolvedColor(paint.resolvedPaintColor(), paintColorScratch)
    }

    private fun colorForImage(paint: Paint?): Color {
        return writeResolvedColor(paint.resolvedPaintColor(), imageColorScratch)
    }

    private fun writeResolvedColor(argb: Int, target: Color): Color {
        target.r = ArgbColor.b(argb) / 255f
        target.g = ArgbColor.c(argb) / 255f
        target.b = ArgbColor.d(argb) / 255f
        target.a = ArgbColor.a(argb) / 255f
        return target
    }

    private fun activeGraphics(): Graphics? {
        val g = graphics ?: return null
        activateGraphics(g)
        return g
    }

    private fun activateGraphics(g: Graphics) {
        if (lastActiveGraphics !== g) {
            Graphics.setCurrent(g)
            Color.setRebindRequired()
            lastActiveGraphics = g
            applyClip(g)
        }
    }

    private fun applyClip(g: Graphics? = graphics) {
        val active = g ?: return
        val clip = transform.clip
        if (clip == null || clip.a()) {
            active.clearClip()
        } else {
            active.setClip(
                clip.a.toInt(),
                clip.b.toInt(),
                max(1, clip.width().toInt()),
                max(1, clip.height().toInt()),
            )
        }
    }

    private data class TransformState(
        var translateX: Float = 0f,
        var translateY: Float = 0f,
        var scaleX: Float = 1f,
        var scaleY: Float = 1f,
        var rotationDegrees: Float = -90f,
        var rotationPivotX: Float = 0f,
        var rotationPivotY: Float = 0f,
        var clip: RectF? = null,
    ) {
        fun translate(x: Float, y: Float) {
            translateX += x * scaleX
            translateY += y * scaleY
        }

        fun x(value: Float): Float = value * scaleX + translateX

        fun y(value: Float): Float = value * scaleY + translateY

        fun rect(rect: RectF): RectF =
            RectF(x(rect.a), y(rect.b), x(rect.c), y(rect.d)).also { it.g() }
    }

    private inline fun withSlickBlendModeForPaint(graphics: Graphics, paint: Paint?, block: () -> Unit) {
        val blendMode = slickBlendModeForPaint(paint)
        if (blendMode == null) {
            block()
            return
        }
        graphics.flushBuffer()
        applySlickBlendMode(graphics, blendMode)
        try {
            block()
        } finally {
            graphics.flushBuffer()
            graphics.setDrawMode(Graphics.MODE_NORMAL)
        }
    }

    private fun slickBlendModeForPaint(paint: Paint?): SlickBlendMode? {
        when (paint?.getBlendMode()) {
            GameCanvasBlendMode.Source -> return SlickBlendMode.Source
            GameCanvasBlendMode.Add -> return SlickBlendMode.Add
            GameCanvasBlendMode.Multiply -> return SlickBlendMode.Multiply
            GameCanvasBlendMode.Screen -> return SlickBlendMode.Screen
            else -> Unit
        }
        return when (val filter = paint?.h()) {
            is MultiplyAddColorFilter ->
                if (filter.usesLegacyAdditiveBlend()) SlickBlendMode.LightingAdd else null

            is TeamColorFilter ->
                when (filter.a) {
                    BlendMode.copy -> SlickBlendMode.TeamCopy
                    BlendMode.additive -> SlickBlendMode.TeamAdditive
                    else -> null
                }

            else -> null
        }
    }

    private fun applySlickBlendMode(graphics: Graphics, blendMode: SlickBlendMode) {
        when (blendMode) {
            SlickBlendMode.LightingAdd,
            SlickBlendMode.Add -> {
                graphics.setDrawMode(Graphics.MODE_ADD)
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glColorMask(true, true, true, true)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE)
            }

            SlickBlendMode.TeamCopy -> {
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glColorMask(true, true, true, true)
                GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE)
            }

            SlickBlendMode.TeamAdditive -> {
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glColorMask(true, true, true, true)
                GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_ONE_MINUS_SRC_ALPHA)
            }

            SlickBlendMode.Source -> {
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glColorMask(true, true, true, true)
                GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ZERO)
            }

            SlickBlendMode.Multiply -> graphics.setDrawMode(Graphics.MODE_COLOR_MULTIPLY)
            SlickBlendMode.Screen -> graphics.setDrawMode(Graphics.MODE_SCREEN)
        }
    }

    private enum class SlickBlendMode {
        LightingAdd,
        TeamCopy,
        TeamAdditive,
        Source,
        Add,
        Multiply,
        Screen,
    }

    private enum class SlickFontFamily {
        Roboto,
        DroidSansFallback,
    }

    private data class SlickFontKey(
        val size: Int,
        val family: SlickFontFamily,
    )

    private fun fontForText(paint: Paint?, text: String): Font {
        val size = (paint?.k() ?: 16f).roundToInt().coerceAtLeast(1)
        val family = if (needsUnicodeFont(text)) {
            SlickFontFamily.DroidSansFallback
        } else {
            SlickFontFamily.Roboto
        }
        val key = SlickFontKey(size, family)
        val unicodeFont = fontCache.getOrPut(key) {
            val awtFont = awtFontFor(size, family)
            UnicodeFont(awtFont).also {
                it.effects.add(ColorEffect(AwtColor.WHITE))
                it.addAsciiGlyphs()
                runCatching { it.loadGlyphs() }
            }
        }
        if (text.isNotEmpty() && family == SlickFontFamily.DroidSansFallback) {
            val recentTexts = recentUnicodeTexts.getOrPut(key) { ArrayDeque() }
            if (text !in recentTexts) {
                val loaded = runCatching {
                    unicodeFont.addGlyphs(text)
                    unicodeFont.loadGlyphs()
                }.isSuccess
                if (loaded) {
                    recentTexts.addLast(text)
                    if (recentTexts.size > RECENT_UNICODE_TEXT_LIMIT) {
                        recentTexts.removeFirst()
                    }
                }
            }
        }
        return unicodeFont
    }

    private fun awtFontFor(size: Int, family: SlickFontFamily): AwtFont =
        awtFontCache.getOrPut(SlickFontKey(size, family)) {
            val path = when (family) {
                SlickFontFamily.Roboto -> "font/Roboto-Regular.ttf"
                SlickFontFamily.DroidSansFallback -> "font/DroidSansFallback.ttf"
            }
            val fontBytes = storage.readAssetBytes(path)
            if (fontBytes != null) {
                AwtFont.createFont(AwtFont.TRUETYPE_FONT, ByteArrayInputStream(fontBytes)).deriveFont(size.toFloat())
            } else {
                AwtFont("SansSerif", AwtFont.PLAIN, size)
            }
        }

    private fun needsUnicodeFont(text: String): Boolean {
        for (index in text.indices) {
            if (text[index].code > 255) {
                return true
            }
        }
        return false
    }

    private fun alignedX(text: String, x: Float, paint: Paint?): Float =
        when (paint?.j()) {
            Paint.Align.CENTER -> x - (b(text, paint) / 2f)
            Paint.Align.RIGHT -> x - b(text, paint)
            else -> x
        }

    private fun Texture?.asSlickTexture(): SlickTexture? {
        val resolved = this?.resolvedTexture() ?: return null
        if (resolved is SlickTexture) {
            return resolved
        }
        val width = resolved.width().coerceAtLeast(1)
        val height = resolved.height().coerceAtLeast(1)
        val sourceKey = resolved.sourceName()
        val cacheKey = resolved.d
        val pixelRevision = resolved.getPixelRevision()
        val textureRevision = resolved.e
        legacyTextureCache[cacheKey]?.let { cached ->
            if (cached.width == width &&
                cached.height == height &&
                cached.pixelRevision == pixelRevision &&
                cached.textureRevision == textureRevision &&
                cached.sourceKey == sourceKey &&
                cached.texture.image() != null
            ) {
                return cached.texture
            }
            cached.texture.o()
        }
        val image = imageFromLegacySource(sourceKey)
            ?: resolved.argbPixelsCopy
                ?.takeIf { it.size >= width * height }
                ?.let { pixels -> imageFromArgbPixels(width, height, pixels) }
            ?: return null
        val slickTexture = SlickTexture(image, sourceKey ?: "legacy-texture-$cacheKey")
        resolved.copyTextureSettingsTo(slickTexture)
        legacyTextureCache[cacheKey] = LegacySlickTextureCacheEntry(
            texture = slickTexture,
            width = width,
            height = height,
            pixelRevision = pixelRevision,
            textureRevision = textureRevision,
            sourceKey = sourceKey,
        )
        return slickTexture
    }

    private fun imageFromArgbPixels(width: Int, height: Int, pixels: IntArray): Image {
        val buffer = ImageBuffer(width, height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val argb = pixels[x + (y * width)]
                buffer.setRGBA(
                    x,
                    y,
                    (argb ushr 16) and 0xff,
                    (argb ushr 8) and 0xff,
                    argb and 0xff,
                    (argb ushr 24) and 0xff,
                )
            }
        }
        return Image(buffer)
    }

    private fun imageFromLegacySource(sourceKey: String?): Image? {
        val normalized = sourceKey
            ?.replace('\\', '/')
            ?.removePrefix("assets/")
            ?.takeIf { it.isNotBlank() }
            ?: return null
        val fileBytes = File(sourceKey).takeIf { it.isFile }?.readBytes()
        if (fileBytes != null) {
            return (createTexture(fileBytes, sourceKey) as? SlickTexture)?.image()
        }
        val assetPath = if (normalized.startsWith("drawable/") && '.' !in normalized.substringAfterLast('/')) {
            drawableAssetPath(normalized.substringAfterLast('/'))
        } else {
            legacyAssetCandidates(normalized).firstOrNull(storage::assetFileExists)
        } ?: return null
        val bytes = storage.readAssetBytes(assetPath) ?: return null
        return (createTexture(bytes, sourceKey) as? SlickTexture)?.image()
    }

    private fun legacyAssetCandidates(path: String): List<String> =
        if (path.startsWith("assets/")) {
            listOf(path, path.removePrefix("assets/"))
        } else {
            listOf(path, "assets/$path")
        }

    data class LegacySlickTextureCacheEntry(
        val texture: SlickTexture,
        val width: Int,
        val height: Int,
        val pixelRevision: Int,
        val textureRevision: Int,
        val sourceKey: String?,
    )

    private fun drawableAssetPath(drawableName: String): String? =
        imageFileNames(drawableName)
            .map { "drawable/$it" }
            .firstOrNull(storage::assetFileExists)
            ?: imageFileNames(drawableName)
                .map { "assets/drawable/$it" }
                .firstOrNull(storage::assetFileExists)

    private companion object {
        val BACKEND_CAPABILITIES = GraphicsBackendCapabilities(
            fixedLayerBufferPixelSize = 512,
            extraLayerBufferCells = 1,
            layerBufferScrollPreloadWorldMargin = 512,
            clearLayerBuffersBeforeCopy = true,
            supportsSmoothFogLayerBuffers = false,
            requiresFogAtlasLock = false,
            requiresImageTintColorFilter = false,
        )
        const val MAX_TILED_DRAWS = 2000
        const val RECENT_UNICODE_TEXT_LIMIT = 30
        var lastActiveGraphics: Graphics? = null

        val drawableNamesById: Map<Int, String> by lazy {
            R.drawable::class.java.fields.associate { field -> field.getInt(null) to field.name }
        }

        fun imageFileNames(nameWithoutExtension: String): List<String> =
            listOf(
                "$nameWithoutExtension.png",
                "$nameWithoutExtension.9.png",
                "$nameWithoutExtension.jpg",
                "$nameWithoutExtension.jpeg",
            )
    }
}

private class SlickShaderState {
    val bindings: MutableMap<ShaderProgram, SlickShaderBinding> = IdentityHashMap()
    var currentShader: ShaderProgram? = null
}

private class SlickShaderBinding {
    var programId: Int = 0
    var sources: Pair<String?, String?>? = null
    val uniformLocations: MutableMap<ShaderUniform, Int> = IdentityHashMap()
    val missingUniforms: MutableSet<ShaderUniform> = java.util.Collections.newSetFromMap(IdentityHashMap())
    val uniformSnapshots: MutableMap<ShaderUniform, SlickUniformSnapshot> = IdentityHashMap()

    fun hasUniformChanges(shaderProgram: ShaderProgram): Boolean =
        shaderProgram.uniforms.any { uniform -> uniformSnapshots[uniform] != uniform.toSlickSnapshot() }
}

private data class SlickUniformSnapshot(
    val values: List<Float>,
    val texture: Texture?,
    val textureSize: Boolean,
    val valueType: ShaderUniformValueType,
)

private fun ShaderUniform.toSlickSnapshot(): SlickUniformSnapshot =
    SlickUniformSnapshot(
        values = floatValues.toList(),
        texture = texture,
        textureSize = g,
        valueType = valueType,
    )
