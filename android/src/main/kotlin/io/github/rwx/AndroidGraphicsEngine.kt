package io.github.rwx

import android.content.Context
import android.graphics.*
import com.corrodinggames.rts.game.ColorMode
import com.corrodinggames.rts.game.teamColorsHueType
import com.corrodinggames.rts.gameFramework.android.graphics.AndroidGraphicsContext
import com.corrodinggames.rts.gameFramework.android.graphics.BlendPaint
import com.corrodinggames.rts.gameFramework.android.graphics.C0009fo
import com.corrodinggames.rts.gameFramework.android.graphics.GraphicsInterface
import com.corrodinggames.rts.gameFramework.graphics.*
import com.corrodinggames.rts.gameFramework.graphics.BlendMode
import com.corrodinggames.rts.gameFramework.m.GraphicsUtils
import com.corrodinggames.rts.gameFramework.m.UniquePaint
import com.corrodinggames.rts.gameFramework.m.UnitTexture
import com.corrodinggames.rts.gameFramework.utility.AssetInputStream
import io.github.rwx.geometry.Rect
import io.github.rwx.geometry.RectF
import io.github.rwx.render.canvas.*
import io.github.rwx.render.frame.GameCanvasBlendMode
import io.github.rwx.logger
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.concurrent.locks.Lock
import androidx.core.graphics.createBitmap

/**
 * Android-only bridge from the core RW graphics interface to the copied Android graphics renderer.
 */
internal class AndroidGraphicsEngine(
    private val context: Context,
    private var hostBackend: AndroidRenderBackend,
    private val graphicsContext: com.corrodinggames.rts.gameFramework.m.GraphicsContext =
        AndroidGraphicsContext().also { root ->
            root.a(context.applicationContext)
            root.b()
        },
    private val textureBindings: MutableMap<Texture, TextureBinding> = IdentityHashMap(),
    private val shaderBindings: MutableMap<ShaderProgram, AndroidShaderBinding> = IdentityHashMap(),
    private val shaderSyncStack: MutableSet<ShaderProgram> =
        Collections.newSetFromMap(IdentityHashMap()),
    private val targetBinding: TextureBinding? = null,
    private val target: AndroidTargetSurface = resolveTarget(hostBackend, RenderTargetMode.DEFAULT),
    private val sharedFallbackResources: AndroidFallbackResources? = null,
) : GraphicsEngine {
    val isCpuTarget: Boolean get() = target == AndroidTargetSurface.CPU_BITMAP
    override fun backendCapabilities(): GraphicsBackendCapabilities = ANDROID_GRAPHICS_BACKEND_CAPABILITIES

    override fun supportsShaderEffects(): Boolean = target == AndroidTargetSurface.GPU


    private var viewportWidth: Int = 1
    private var viewportHeight: Int = 1
    private val fallbackTexture: Texture
    private val fallbackUnitTexture: UnitTexture
    private val fallbackResources: AndroidFallbackResources
    private val paintCache: MutableMap<io.github.rwx.render.canvas.Paint, AndroidPaintBinding> = IdentityHashMap()
    private val colorFilterCache: MutableMap<io.github.rwx.render.canvas.ColorFilter, android.graphics.ColorFilter> = IdentityHashMap()
    private val androidRectA = android.graphics.Rect()
    private val androidRectB = android.graphics.Rect()
    private val androidRectF = android.graphics.RectF()
    private var lastTexture: Texture? = null
    private var lastTextureBinding: TextureBinding? = null
    private var lastPaint: io.github.rwx.render.canvas.Paint? = null
    private var lastPaintRevision: Int = Int.MIN_VALUE
    private var lastAndroidPaint: android.graphics.Paint? = null

    init {
        fallbackResources = sharedFallbackResources ?: run {
            val unitTexture = graphicsContext.b(1, 1, true).apply {
                b()?.eraseColor(-1)
                j()
            }
            AndroidFallbackResources(
                texture = unitTexture.toCoreTexture("android/fallback"),
                unitTexture = unitTexture,
            )
        }
        fallbackTexture = fallbackResources.texture
        fallbackUnitTexture = fallbackResources.unitTexture
    }

    fun beginFrame(graphicsInterface: GraphicsInterface, width: Int, height: Int) {
        graphicsContext.a(graphicsInterface)
        a(width, height)
        graphicsContext.e()
    }

    fun endFrame() {
        graphicsContext.f()
    }

    fun recreateForRenderBackend(renderBackend: AndroidRenderBackend): AndroidGraphicsEngine =
        AndroidGraphicsEngine(
            context = context,
            hostBackend = renderBackend,
            target = resolveTarget(renderBackend, RenderTargetMode.DEFAULT),
            textureBindings = textureBindings,
            shaderBindings = shaderBindings,
            shaderSyncStack = shaderSyncStack,
        )

    override fun b(texture: Texture?): GraphicsEngine =
        createRenderTarget(texture, RenderTargetMode.DEFAULT)

    override fun b(texture: Texture?, mode: RenderTargetMode): GraphicsEngine =
        createRenderTarget(texture, mode)

    private fun createRenderTarget(texture: Texture?, mode: RenderTargetMode): GraphicsEngine {
        if (texture == null) {
            return AndroidGraphicsEngine(
                context = context,
                hostBackend = hostBackend,
                target = resolveTarget(hostBackend, mode),
                textureBindings = textureBindings,
                shaderBindings = shaderBindings,
                shaderSyncStack = shaderSyncStack,
            )
        }
        val binding = textureBinding(texture)
        val targetSurface = resolveTarget(hostBackend, mode)
        val targetContext = when (targetSurface) {
            AndroidTargetSurface.GPU -> graphicsContext.a(binding.unitTexture)
            AndroidTargetSurface.CPU_BITMAP -> graphicsContext.b(binding.unitTexture)
        }
        return AndroidGraphicsEngine(
            context = context,
            hostBackend = hostBackend,
            target = targetSurface,
            graphicsContext = targetContext,
            textureBindings = textureBindings,
            shaderBindings = shaderBindings,
            shaderSyncStack = shaderSyncStack,
            targetBinding = binding,
        ).also { targetEngine ->
            targetEngine.a(binding.unitTexture.width(), binding.unitTexture.height())
        }
    }

    override fun a(lock: Lock?) {
        if (lock != null) {
            graphicsContext.a(lock)
        }
    }

    override fun b(lock: Lock?) {
        if (lock != null) {
            graphicsContext.b(lock)
        }
    }

    override fun a(operation: DrawTimeOperation?) {
        operation ?: return
        graphicsContext.a(
            object : com.corrodinggames.rts.gameFramework.android.graphics.GraphicsOperation() {
                override fun a(callbackContext: com.corrodinggames.rts.gameFramework.m.GraphicsContext) {
                    val callbackEngine = AndroidGraphicsEngine(
                        context = context,
                        hostBackend = hostBackend,
                        target = target,
                        graphicsContext = callbackContext,
                        textureBindings = textureBindings,
                        shaderBindings = shaderBindings,
                        shaderSyncStack = shaderSyncStack,
                        sharedFallbackResources = fallbackResources,
                    )
                    callbackEngine.a(viewportWidth, viewportHeight)
                    operation.draw(callbackEngine)
                }
            },
        )
    }

    override fun a(i: Int): Texture = a(i, true)

    override fun a(i: Int, z: Boolean): Texture =
        graphicsContext.a(i, z).toCoreTexture("drawable-id/$i")

    override fun a(inputStream: InputStream?, z: Boolean): Texture {
        val sourceName = (inputStream as? AssetInputStream)?.path
            ?: "stream/${System.identityHashCode(inputStream)}"
        return graphicsContext.a(inputStream, z).toCoreTexture(sourceName)
    }

    override fun a(i: Int, i2: Int, z: Boolean): Texture = b(i, i2, z)

    override fun b(i: Int, i2: Int, z: Boolean): Texture =
        graphicsContext.b(i.coerceAtLeast(1), i2.coerceAtLeast(1), z).toCoreTexture("generated/${i}x$i2")

    override fun a(texture: Texture?, f: Float, f2: Float, f3: Float, paint: io.github.rwx.render.canvas.Paint?) {
        texture ?: return
        graphicsContext.a(textureBinding(texture).unitTexture, f, f2, f3, paint.toAndroidPaint())
    }

    override fun a(texture: Texture?, rect: Rect?, f: Float, f2: Float, f3: Float, paint: io.github.rwx.render.canvas.Paint?) {
        texture ?: return
        rect ?: return
        graphicsContext.a(
            textureBinding(texture).unitTexture,
            rect.copyTo(androidRectA),
            f,
            f2,
            f3,
            paint.toAndroidPaint()
        )
    }

    override fun a(texture: Texture?, rect: Rect?, rect2: Rect?, paint: io.github.rwx.render.canvas.Paint?) {
        texture ?: return
        rect ?: return
        rect2 ?: return
        graphicsContext.a(
            textureBinding(texture).unitTexture,
            rect.copyTo(androidRectA),
            rect2.copyTo(androidRectB),
            paint.toAndroidPaint(),
        )
    }

    override fun a(texture: Texture?, rect: Rect?, rectF: RectF?, paint: io.github.rwx.render.canvas.Paint?) {
        texture ?: return
        rect ?: return
        rectF ?: return
        graphicsContext.a(
            textureBinding(texture).unitTexture,
            rect.copyTo(androidRectA),
            rectF.copyTo(androidRectF),
            paint.toAndroidPaint(),
        )
    }

    override fun a(texture: Texture?, f: Float, f2: Float, paint: io.github.rwx.render.canvas.Paint?) {
        texture ?: return
        graphicsContext.a(textureBinding(texture).unitTexture, f, f2, paint.toAndroidPaint())
    }

    override fun a(texture: Texture?, f: Float, f2: Float, paint: io.github.rwx.render.canvas.Paint?, f3: Float, f4: Float) {
        texture ?: return
        k()
        if (f3 != 0f) {
            a(androidTextureRotation(f3), f, f2)
        }
        if (f4 != 1f) {
            a(f4, f4, f, f2)
        }
        b(texture, f, f2, paint)
        l()
    }

    override fun b(texture: Texture?, f: Float, f2: Float, paint: io.github.rwx.render.canvas.Paint?) {
        texture ?: return
        graphicsContext.b(textureBinding(texture).unitTexture, f, f2, paint.toAndroidPaint())
    }

    override fun b(texture: Texture?, rect: Rect?, rect2: Rect?, paint: io.github.rwx.render.canvas.Paint?) {
        texture ?: return
        rect ?: return
        rect2 ?: return
        androidRectA.set(rect.a, rect.b, rect.c, rect.d)
        androidRectB.set(rect2.a, rect2.b, rect2.c, rect2.d)
        graphicsContext.b(
            textureBinding(texture).unitTexture,
            androidRectA,
            androidRectB,
            paint.toAndroidPaint(),
        )
    }

    override fun a(rect: Rect?, paint: io.github.rwx.render.canvas.Paint?) {
        rect ?: return
        graphicsContext.a(rect.copyTo(androidRectA), paint.toAndroidPaint())
    }

    override fun a(texture: Texture?, rect: Rect?, paint: io.github.rwx.render.canvas.Paint?) {
        texture ?: return
        rect ?: return
        val unitTexture = textureBinding(texture).unitTexture
        GraphicsUtils.a(
            graphicsContext,
            unitTexture,
            rect.copyTo(androidRectA),
            paint.toAndroidPaint(),
            0,
            0,
            0,
            0,
        )
    }

    override fun a(texture: Texture?, rect: Rect?, paint: io.github.rwx.render.canvas.Paint?, i: Int, i2: Int, i3: Int, i4: Int) {
        texture ?: return
        rect ?: return
        graphicsContext.a(
            textureBinding(texture).unitTexture,
            rect.copyTo(androidRectA),
            paint.toAndroidPaint(),
            i,
            i2,
            i3,
            i4
        )
    }

    override fun a(texture: Texture?, rectF: RectF?, paint: io.github.rwx.render.canvas.Paint?, f: Float, f2: Float, i: Int, i2: Int) {
        texture ?: return
        rectF ?: return
        graphicsContext.a(
            textureBinding(texture).unitTexture,
            rectF.copyTo(androidRectF),
            paint.toAndroidPaint(),
            f,
            f2
        )
    }

    override fun b(i: Int) {
        graphicsContext.b(i)
    }

    override fun a(i: Int, mode: GameCanvasBlendMode?) {
        if (mode == GameCanvasBlendMode.Clear || mode == GameCanvasBlendMode.ClearAlpha) {
            graphicsContext.a(PorterDuff.Mode.CLEAR)
        } else {
            graphicsContext.b(i)
        }
    }

    override fun a(str: String?, f: Float, f2: Float, paint: io.github.rwx.render.canvas.Paint?, paint2: io.github.rwx.render.canvas.Paint?, f3: Float) {
        graphicsContext.a(str.orEmpty(), f, f2, paint.toAndroidPaint(), paint2.toAndroidPaint(), f3)
    }

    override fun a(str: String?, f: Float, f2: Float, paint: io.github.rwx.render.canvas.Paint?) {
        graphicsContext.a(str.orEmpty(), f, f2, paint.toAndroidPaint())
    }

    override fun b(rect: Rect?, paint: io.github.rwx.render.canvas.Paint?) {
        rect ?: return
        graphicsContext.b(rect.copyTo(androidRectA), paint.toAndroidPaint())
    }

    override fun a(z: Boolean) {
        graphicsContext.a(z)
    }

    override fun f() {
        graphicsContext.d()
    }

    override fun a(rectF: RectF?, paint: io.github.rwx.render.canvas.Paint?) {
        rectF ?: return
        graphicsContext.a(rectF.copyTo(androidRectF), paint.toAndroidPaint())
    }

    override fun c(rect: Rect?, paint: io.github.rwx.render.canvas.Paint?) {
        rect ?: return
        graphicsContext.c(rect.copyTo(androidRectA), paint.toAndroidPaint())
    }

    override fun a(rect: Rect?) {
        rect ?: return
        graphicsContext.a(rect.copyTo(androidRectA))
    }

    override fun a(rectF: RectF?) {
        rectF ?: return
        graphicsContext.a(rectF.copyTo(androidRectF))
    }

    override fun a(f: Float, f2: Float, f3: Float, paint: io.github.rwx.render.canvas.Paint?) {
        graphicsContext.a(f, f2, f3, paint.toAndroidPaint())
    }

    override fun b(f: Float, f2: Float, f3: Float, paint: io.github.rwx.render.canvas.Paint?) {
        graphicsContext.b(f, f2, f3, paint.toAndroidPaint())
    }

    override fun a(fArr: FloatArray?, i: Int, i2: Int, paint: io.github.rwx.render.canvas.Paint?) {
        fArr ?: return
        val points = if (i == 0) fArr else fArr.copyOfRange(i, (i + i2).coerceAtMost(fArr.size))
        graphicsContext.a(points, i2.coerceAtMost(points.size), paint.toAndroidPaint())
    }

    override fun i() {
        graphicsContext.i()
    }

    override fun j() {
        graphicsContext.j()
    }

    override fun k() {
        graphicsContext.g()
    }

    override fun l() {
        graphicsContext.h()
    }

    override fun a(f: Float, f2: Float, f3: Float) {
        graphicsContext.a(f, f2, f3)
    }

    override fun a(f: Float, f2: Float) {
        graphicsContext.a(f, f2)
    }

    override fun a(f: Float, f2: Float, f3: Float, f4: Float) {
        graphicsContext.a(f, f2, f3, f4)
    }

    override fun b(f: Float, f2: Float) {
        graphicsContext.b(f, f2)
    }

    override fun a(f: Float, f2: Float, f3: Float, f4: Float, paint: io.github.rwx.render.canvas.Paint?) {
        graphicsContext.a(f, f2, f3, f4, paint.toAndroidPaint())
    }

    override fun m(): Int = viewportWidth

    override fun n(): Int = viewportHeight

    override fun a(i: Int, i2: Int) {
        viewportWidth = i.coerceAtLeast(1)
        viewportHeight = i2.coerceAtLeast(1)
        graphicsContext.a(viewportWidth, viewportHeight)
    }

    override fun o() {
        graphicsContext.m()
    }

    override fun p() {
        graphicsContext.n()
        if (isCpuTarget) {
            targetBinding?.markCanvasTargetChanged()
        }
    }

    override fun q() = Unit

    override fun a(shaderProgram: ShaderProgram?) {
        if (!supportsShaderEffects() || shaderProgram == null) {
            return
        }
        graphicsContext.a(syncShader(shaderProgram))
    }

    override fun a(str: String?, paint: io.github.rwx.render.canvas.Paint?): Int =
        graphicsContext.a(paint.toAndroidPaint())

    override fun b(str: String?, paint: io.github.rwx.render.canvas.Paint?): Int =
        graphicsContext.a(str.orEmpty(), paint.toAndroidPaint())

    override fun r(): Texture = fallbackTexture

    override fun a(texture: Texture?, file: File?) = Unit

    private fun textureBinding(texture: Texture): TextureBinding {
        if (lastTexture === texture) {
            return checkNotNull(lastTextureBinding).also {
                it.syncUnitFromCoreIfNeeded()
                syncTextureShader(texture, it)
            }
        }
        if (texture is TeamColorTexture) {
            textureBindings[texture]?.let { return cacheLastTexture(texture, it) }
            val source = textureBinding(texture.sourceTexture()).unitTexture
            val unitTexture = com.corrodinggames.rts.gameFramework.m.TeamColorTexture(
                source,
                texture.teamColor,
                texture.colorMode.toAndroidColorMode(),
                0,
            )
            return TextureBinding(texture, unitTexture, texture.getPixelRevision()).also {
                textureBindings[texture] = it
                syncTextureShader(texture, it)
                cacheLastTexture(texture, it)
            }
        }

        val existing = textureBindings[texture]
        if (existing != null) {
            existing.syncUnitFromCoreIfNeeded()
            syncTextureShader(texture, existing)
            return cacheLastTexture(texture, existing)
        }
        val unitTexture = texture.toUnitTexture()
        return TextureBinding(texture, unitTexture, texture.getPixelRevision()).also {
            textureBindings[texture] = it
            syncTextureShader(texture, it)
            cacheLastTexture(texture, it)
        }
    }

    private fun syncTextureShader(texture: Texture, binding: TextureBinding) {
        if (!supportsShaderEffects() || texture is TeamColorTexture) {
            return
        }
        binding.unitTexture.a(texture.shaderProgram()?.let(::syncShader))
    }

    private fun syncShader(shaderProgram: ShaderProgram): C0009fo {
        val binding = shaderBindings.getOrPut(shaderProgram) {
            AndroidShaderBinding(
                shader = C0009fo().apply {
                    c = shaderProgram.name
                    e = shaderProgram.vertexSource
                    f = shaderProgram.fragmentSource
                    m = shaderProgram.programStatus
                },
            )
        }
        binding.syncSources(shaderProgram)
        if (!shaderSyncStack.add(shaderProgram)) {
            return binding.shader
        }
        try {
            shaderProgram.uniforms.forEach { uniform ->
                val texture = uniform.texture
                if (texture != null) {
                    val unitTexture = textureBinding(texture).unitTexture
                    if (uniform.g) {
                        binding.shader.b(uniform.name, unitTexture)
                    } else {
                        binding.shader.a(uniform.name, unitTexture)
                    }
                    return@forEach
                }
                when (uniform.floatValues.size) {
                    1 -> binding.shader.a(uniform.name, uniform.floatValues[0])
                    2 -> binding.shader.a(uniform.name, uniform.floatValues[0], uniform.floatValues[1])
                    4 -> binding.shader.a(
                        uniform.name,
                        uniform.floatValues[0],
                        uniform.floatValues[1],
                        uniform.floatValues[2],
                        uniform.floatValues[3]
                    )

                    else -> binding.logUnsupportedUniform(uniform.name, uniform.floatValues.size)
                }
            }
        } finally {
            shaderSyncStack.remove(shaderProgram)
        }
        return binding.shader
    }

    private fun cacheLastTexture(texture: Texture, binding: TextureBinding): TextureBinding {
        lastTexture = texture
        lastTextureBinding = binding
        return binding
    }

    private fun UnitTexture.toCoreTexture(sourceName: String): Texture {
        g = sourceName
        val texture = AndroidBackedTexture(
            unitTexture = this,
            onRelease = { releasedTexture ->
                textureBindings.remove(releasedTexture)
                if (lastTexture === releasedTexture) {
                    lastTexture = null
                    lastTextureBinding = null
                }
            },
        ).apply {
            p = this@toCoreTexture.width().coerceAtLeast(1)
            q = this@toCoreTexture.height().coerceAtLeast(1)
            m = this@toCoreTexture.m
            setSourceName(sourceName)
            updateCenter()
        }
        b()?.let { bitmap ->
            texture.setArgbPixelLoader { bitmap.readPixels() }
        }
        textureBindings[texture] = TextureBinding(texture, this, texture.getPixelRevision())
        return texture
    }

    private fun Texture.toUnitTexture(): UnitTexture {
        val width = width().coerceAtLeast(1)
        val height = height().coerceAtLeast(1)
        val bitmap = createBitmap(width, height)
        val pixels = argbPixelsCopy
        if (pixels != null) {
            bitmap.setPixels(pixels.copyOf(width * height), 0, width, 0, 0, width, height)
        }
        return UnitTexture().apply {
            a(bitmap)
            m = this@toUnitTexture.m
            w = this@toUnitTexture.w
        }
    }

    private fun io.github.rwx.render.canvas.Paint?.toAndroidPaint(): android.graphics.Paint {
        if (this == null) {
            return DEFAULT_PAINT
        }
        val androidShader = if (supportsShaderEffects()) {
            (this as? GamePaint)?.shaderProgram()?.let(::syncShader)
        } else {
            null
        }
        val revision = getStateRevision()
        if (lastPaint === this && lastPaintRevision == revision) {
            return checkNotNull(lastAndroidPaint).also { paint ->
                (paint as? BlendPaint)?.shaderProgram = androidShader
            }
        }
        val binding = paintCache.getOrPut(this) {
            AndroidPaintBinding(createAndroidPaint())
        }
        if (binding.matches(revision)) {
            return cacheLastPaint(this, revision, binding.paint).also { paint ->
                (paint as? BlendPaint)?.shaderProgram = androidShader
            }
        }
        return binding.paint.also { paint ->
            val antiAlias = (l and 1) != 0
            paint.isAntiAlias = antiAlias
            paint.isFilterBitmap = isFilterBitmap()
            paint.isDither = isDither()
            paint.isSubpixelText = isSubpixelText()
            paint.style = when (m) {
                io.github.rwx.render.canvas.Paint.Style.STROKE -> android.graphics.Paint.Style.STROKE
                io.github.rwx.render.canvas.Paint.Style.FILL_AND_STROKE -> android.graphics.Paint.Style.FILL_AND_STROKE
                else -> android.graphics.Paint.Style.FILL
            }
            val koolColorFilter = colorFilter()
            paint.color = if (target == AndroidTargetSurface.GPU) {
                n.applyColorFilter(koolColorFilter)
            } else {
                n
            }
            paint.strokeWidth = o.coerceAtLeast(0f)
            paint.textSize = q.coerceAtLeast(1f)
            paint.textAlign = when (p) {
                io.github.rwx.render.canvas.Paint.Align.CENTER -> android.graphics.Paint.Align.CENTER
                io.github.rwx.render.canvas.Paint.Align.RIGHT -> android.graphics.Paint.Align.RIGHT
                else -> android.graphics.Paint.Align.LEFT
            }
            paint.typeface = AndroidGameFontMetrics.typeface(typeface()?.key)
            paint.colorFilter = koolColorFilter.toCachedAndroidColorFilter()
            if (target == AndroidTargetSurface.GPU) {
                paint.xfermode = null
                (paint as? BlendPaint)?.blendMode = toAndroidOpenGlBlendMode()
            } else {
                (paint as? BlendPaint)?.blendMode = BlendPaint.BLEND_NORMAL
                paint.xfermode = canvasXfermode(toAndroidCanvasBlendMode())
            }
            (paint as? BlendPaint)?.shaderProgram = androidShader
            binding.capture(revision)
            cacheLastPaint(this, revision, paint)
        }
    }

    private fun cacheLastPaint(source: io.github.rwx.render.canvas.Paint, revision: Int, paint: android.graphics.Paint): android.graphics.Paint {
        lastPaint = source
        lastPaintRevision = revision
        lastAndroidPaint = paint
        return paint
    }

    private fun io.github.rwx.render.canvas.Paint.createAndroidPaint(): android.graphics.Paint =
        if (target == AndroidTargetSurface.CPU_BITMAP && this is GamePaint) {
            UniquePaint()
        } else {
            BlendPaint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        }

    private class AndroidPaintBinding(val paint: android.graphics.Paint) {
        private var revision: Int = Int.MIN_VALUE

        fun matches(sourceRevision: Int): Boolean = revision == sourceRevision

        fun capture(sourceRevision: Int) {
            revision = sourceRevision
        }
    }

    internal class AndroidShaderBinding(
        val shader: C0009fo,
    ) {
        private var vertexSource: String? = shader.e
        private var fragmentSource: String? = shader.f
        private val unsupportedUniforms = mutableSetOf<String>()

        fun syncSources(source: ShaderProgram) {
            shader.c = source.name
            shader.m = source.programStatus
            if (vertexSource != source.vertexSource || fragmentSource != source.fragmentSource) {
                vertexSource = source.vertexSource
                fragmentSource = source.fragmentSource
                shader.e = source.vertexSource
                shader.f = source.fragmentSource
                shader.k = true
            }
        }

        fun logUnsupportedUniform(name: String, size: Int) {
            if (unsupportedUniforms.add("$name/$size")) {
                logger.warn(tag = SHADER_LOG_TAG) { "Unsupported Android OpenGL uniform size=$size name=$name" }
            }
        }
    }

    private fun io.github.rwx.render.canvas.ColorFilter?.toCachedAndroidColorFilter(): android.graphics.ColorFilter? {
        val filter = this ?: return null
        return colorFilterCache[filter] ?: when (filter) {
            is MultiplyAddColorFilter ->
                if (
                    target == AndroidTargetSurface.CPU_BITMAP &&
                    filter.multiplyColor == OPAQUE_BLACK &&
                    filter.addColor == 0
                ) {
                    PorterDuffColorFilter(OPAQUE_BLACK, PorterDuff.Mode.SRC_IN)
                } else {
                    LightingColorFilter(filter.multiplyColor, filter.addColor)
                }

            is BlendColorFilter -> PorterDuffColorFilter(filter.color, filter.blendMode.toPorterDuffMode())
            else -> null
        }?.also { colorFilterCache[filter] = it }
    }

    class TextureBinding(
        private val texture: Texture,
        val unitTexture: UnitTexture,
        private var pixelRevision: Int,
    ) {
        fun syncUnitFromCoreIfNeeded() {
            unitTexture.m = texture.m
            unitTexture.w = texture.w
            val currentRevision = texture.getPixelRevision()
            if (currentRevision == pixelRevision) {
                return
            }
            val bitmap = unitTexture.b() ?: return
            val width = texture.width().coerceAtLeast(1)
            val height = texture.height().coerceAtLeast(1)
            if (bitmap.width != width || bitmap.height != height) {
                return
            }
            texture.argbPixelsCopy?.let { pixels ->
                unitTexture.k()
                bitmap.setPixels(pixels.copyOf(width * height), 0, width, 0, 0, width, height)
                unitTexture.j()
            }
            pixelRevision = currentRevision
        }

        fun markCanvasTargetChanged() {
            // Canvas writes make UnitTexture's optional direct-pixel cache stale.
            // In the original Android renderer both views lived on the same
            // texture object; the core/Android bridge must invalidate it here.
            unitTexture.k()
            (texture as? AndroidBackedTexture)?.invalidateCorePixelSnapshot()
            pixelRevision = texture.getPixelRevision()
            unitTexture.j()
        }

    }

    private class AndroidBackedTexture(
        private val unitTexture: UnitTexture,
        private val onRelease: (AndroidBackedTexture) -> Unit,
    ) : Texture() {
        private var released = false

        override fun p() {
            super.p()
            val bitmap = unitTexture.b()
            val pixels = argbPixelsCopy
            if (bitmap != null && pixels != null && pixels.size >= bitmap.width * bitmap.height) {
                bitmap.setPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            }
            unitTexture.k()
            unitTexture.j()
        }

        override fun r() {
            unitTexture.k()
            super.r()
        }

        fun invalidateCorePixelSnapshot() {
            invalidateArgbPixelSnapshot {
                unitTexture.b()?.readPixels()
            }
        }

        override fun o() {
            if (!released) {
                released = true
                unitTexture.i()
                onRelease(this)
            }
            super.o()
        }
    }

    private companion object {
        const val SHADER_LOG_TAG = "AndroidShader"
        const val OPAQUE_BLACK = -0x1000000

        val DEFAULT_PAINT: android.graphics.Paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = -1
            isFilterBitmap = false
        }

        fun Rect.copyTo(target: android.graphics.Rect): android.graphics.Rect =
            target.apply { set(a, b, c, d) }

        fun RectF.copyTo(target: android.graphics.RectF): android.graphics.RectF =
            target.apply { set(a, b, c, d) }

        fun Bitmap.readPixels(): IntArray =
            IntArray(width * height).also { pixels ->
                getPixels(pixels, 0, width, 0, 0, width, height)
            }

        fun ColorMode.toAndroidColorMode(): teamColorsHueType =
            when (this) {
                ColorMode.hueAdd -> teamColorsHueType.hueAdd
                ColorMode.hueNew -> teamColorsHueType.hueNew
                ColorMode.hueShift -> teamColorsHueType.hueShift
                ColorMode.disabled -> teamColorsHueType.disabled
                else -> teamColorsHueType.pureGreen
            }

        fun io.github.rwx.render.canvas.Paint.toAndroidOpenGlBlendMode(): Int {
            when (getBlendMode()) {
                GameCanvasBlendMode.Source -> return BlendPaint.BLEND_SOURCE
                GameCanvasBlendMode.Add -> return BlendPaint.BLEND_ADD
                GameCanvasBlendMode.Multiply -> return BlendPaint.BLEND_MULTIPLY
                GameCanvasBlendMode.Screen -> return BlendPaint.BLEND_SCREEN
                else -> Unit
            }
            return when (val filter = colorFilter()) {
                is MultiplyAddColorFilter ->
                    if (filter.usesLegacyAdditiveBlend()) BlendPaint.BLEND_LIGHTING_ADD else BlendPaint.BLEND_NORMAL

                is TeamColorFilter ->
                    when (filter.a) {
                        BlendMode.copy -> BlendPaint.BLEND_TEAM_COPY
                        BlendMode.additive -> BlendPaint.BLEND_TEAM_ADDITIVE
                        else -> BlendPaint.BLEND_NORMAL
                    }

                else -> BlendPaint.BLEND_NORMAL
            }
        }

        fun io.github.rwx.render.canvas.Paint.toAndroidCanvasBlendMode(): GameCanvasBlendMode =
            getBlendMode() ?: when (val filter = colorFilter()) {
                is TeamColorFilter ->
                    when (filter.a) {
                        BlendMode.copy -> GameCanvasBlendMode.Source
                        BlendMode.additive -> GameCanvasBlendMode.Add
                        else -> GameCanvasBlendMode.SourceOver
                    }

                else -> GameCanvasBlendMode.SourceOver
            }

        fun canvasXfermode(mode: GameCanvasBlendMode): PorterDuffXfermode? =
            if (mode == GameCanvasBlendMode.SourceOver) {
                null
            } else {
                PorterDuffXfermode(mode.toPorterDuffMode())
            }

        fun GameCanvasBlendMode.toPorterDuffMode(): PorterDuff.Mode =
            when (this) {
                GameCanvasBlendMode.Clear,
                GameCanvasBlendMode.ClearAlpha -> PorterDuff.Mode.CLEAR

                GameCanvasBlendMode.Source -> PorterDuff.Mode.SRC
                GameCanvasBlendMode.Destination -> PorterDuff.Mode.DST
                GameCanvasBlendMode.SourceIn -> PorterDuff.Mode.SRC_IN
                GameCanvasBlendMode.SourceOut -> PorterDuff.Mode.SRC_OUT
                GameCanvasBlendMode.SourceAtop -> PorterDuff.Mode.SRC_ATOP
                GameCanvasBlendMode.DestinationOver -> PorterDuff.Mode.DST_OVER
                GameCanvasBlendMode.DestinationIn -> PorterDuff.Mode.DST_IN
                GameCanvasBlendMode.DestinationOut -> PorterDuff.Mode.DST_OUT
                GameCanvasBlendMode.DestinationAtop -> PorterDuff.Mode.DST_ATOP
                GameCanvasBlendMode.Xor -> PorterDuff.Mode.XOR
                GameCanvasBlendMode.Add -> PorterDuff.Mode.ADD
                GameCanvasBlendMode.Multiply -> PorterDuff.Mode.MULTIPLY
                GameCanvasBlendMode.Screen -> PorterDuff.Mode.SCREEN
                GameCanvasBlendMode.Overlay -> PorterDuff.Mode.OVERLAY
                else -> PorterDuff.Mode.SRC_OVER
            }
    }
}

internal val ANDROID_GRAPHICS_BACKEND_CAPABILITIES = GraphicsBackendCapabilities(
    supportsLayerBufferPreRendering = false,
    requiresFogAtlasLock = true,
    requiresImageTintColorFilter = true,
)

internal enum class AndroidTargetSurface {
    CPU_BITMAP,
    GPU,
}

internal data class AndroidFallbackResources(
    val texture: Texture,
    val unitTexture: UnitTexture,
)

internal fun resolveTarget(host: AndroidRenderBackend, mode: RenderTargetMode): AndroidTargetSurface =
    if (host == AndroidRenderBackend.OPENGL_ES && mode == RenderTargetMode.DEFAULT) {
        AndroidTargetSurface.GPU
    } else {
        AndroidTargetSurface.CPU_BITMAP
    }

internal fun androidTextureRotation(rotation: Float): Float =
    if (rotation == 0f) 0f else rotation + 90f
