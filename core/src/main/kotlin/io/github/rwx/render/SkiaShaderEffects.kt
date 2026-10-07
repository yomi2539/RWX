package io.github.rwx.render

import com.corrodinggames.rts.gameFramework.graphics.GamePaint
import io.github.rwx.logger
import com.corrodinggames.rts.gameFramework.graphics.ShaderProgram
import com.corrodinggames.rts.gameFramework.graphics.ShaderUniform
import com.corrodinggames.rts.gameFramework.graphics.ShaderUniformValueType
import com.corrodinggames.rts.gameFramework.graphics.TeamColorShader
import com.corrodinggames.rts.gameFramework.graphics.Texture
import io.github.rwx.render.canvas.Paint
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.Image
import org.jetbrains.skia.Matrix33
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder
import org.jetbrains.skia.SamplingMode
import org.jetbrains.skia.Shader
import org.jetbrains.skia.Rect as SkiaRect

val ShaderProgram.disposed: Boolean
    get() = this.programStatus != 0

internal class SkiaShaderEffects(private val readAsset: (String) -> ByteArray?) : AutoCloseable {
    private val lock = Any()
    private val cache = mutableMapOf<ShaderProgram, RuntimeEffect?>()
    private var closed = false

    fun effectFor(program: ShaderProgram): RuntimeEffect? {
        synchronized(lock) {
            cache.entries.removeAll { (shader, effect) ->
                (effect != null && shader.disposed).also { if (it) effect?.close() }
            }
            if (closed || program.disposed) return null
            if (cache.containsKey(program)) return cache[program]
            val effect = program.skslSource?.let(RuntimeEffect::makeForShader) ?: compile(program.name)
            cache[program] = effect
            return effect
        }
    }

    fun shaderForDraw(
        paint: Paint?,
        texture: Texture,
        image: Image,
        src: SkiaRect,
        dst: SkiaRect,
        sampling: SamplingMode,
        resolveImage: (Texture) -> Image?,
    ): Shader? {
        val program = (paint as? GamePaint)?.shaderProgram() ?: texture.B() ?: return null
        if (program is TeamColorShader && !program.a()) return null
        val effect = effectFor(program) ?: return null
        return try {
            program.a(paint, texture)
            buildShader(program, image, src, dst, sampling, resolveImage, effect)
        } catch (error: Exception) {
            if (program.skslSource != null) throw error
            logger.warn(error) { "shader(" + program.name + "): draw failed, falling back to unshaded" }
            null
        }
    }

    private fun buildShader(
        program: ShaderProgram,
        image: Image,
        src: SkiaRect,
        dst: SkiaRect,
        sampling: SamplingMode,
        resolveImage: (Texture) -> Image?,
        effect: RuntimeEffect,
    ): Shader {
        val builder = RuntimeShaderBuilder(effect)
        try {
            val children = mutableListOf<Shader>()
            try {
                for (uniform in program.uniforms) {
                    bindUniform(builder, uniform, sampling, resolveImage, children)
                }
                val scaleX = dst.width / src.width
                val scaleY = dst.height / src.height
                val local = Matrix33(
                    scaleX, 0f, dst.left - src.left * scaleX,
                    0f, scaleY, dst.top - src.top * scaleY,
                    0f, 0f, 1f,
                )
                children.add(image.makeShader(FilterTileMode.CLAMP, FilterTileMode.CLAMP, sampling, local))
                builder.child("u_texture", children.last())
                return builder.makeShader()
            } finally {
                children.forEach { it.close() }
            }
        } finally {
            runCatching { builder.close() }
        }
    }

    private fun bindUniform(
        builder: RuntimeShaderBuilder,
        uniform: ShaderUniform,
        sampling: SamplingMode,
        resolveImage: (Texture) -> Image?,
        children: MutableList<Shader>,
    ) {
        val texture = uniform.texture
        if (texture != null) {
            if (uniform.g) {
                val size = resolveImage(texture) ?: return
                builder.uniform(uniform.name, size.width.toFloat(), size.height.toFloat())
            } else {
                val childImage = resolveImage(texture) ?: return
                val tile = if (uniform.repeatTexture) FilterTileMode.REPEAT else FilterTileMode.CLAMP
                val filter = if (uniform.linearTexture) SamplingMode.LINEAR else SamplingMode.DEFAULT
                children.add(childImage.makeShader(tile, tile, filter, null))
                builder.child(uniform.name, children.last())
            }
            return
        }
        val values = uniform.floatValues
        when (uniform.valueType) {
            ShaderUniformValueType.INTEGER -> builder.uniform(uniform.name, values.getOrElse(0) { 0f }.toInt())
            ShaderUniformValueType.MATRIX4 -> builder.uniform(uniform.name, values.copyOf())
            else -> when (values.size) {
                1 -> builder.uniform(uniform.name, values[0])
                2 -> builder.uniform(uniform.name, values[0], values[1])
                3 -> builder.uniform(uniform.name, values[0], values[1], values[2])
                4 -> builder.uniform(uniform.name, values[0], values[1], values[2], values[3])
                else -> builder.uniform(uniform.name, values.copyOf())
            }
        }
    }

    private fun compile(name: String): RuntimeEffect? {
        return try {
            val bytes = readAsset("shaders/$name.sksl")
                ?: readAsset("assets/shaders/$name.sksl")
                ?: throw IllegalStateException("missing shader source")
            RuntimeEffect.makeForShader(bytes.toString(Charsets.UTF_8))
        } catch (e: Exception) {
            logger.warn(e) { "shader($name): SkSL unavailable, draws fall back to unshaded: " + e.message }
            null
        }
    }

    override fun close() {
        synchronized(lock) {
            if (closed) return
            closed = true
            for (effect in cache.values) {
                try {
                    effect?.close()
                } catch (_: Exception) {
                }
            }
            cache.clear()
        }
    }
}
