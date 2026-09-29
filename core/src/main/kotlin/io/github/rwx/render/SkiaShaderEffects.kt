package io.github.rwx.render

import com.corrodinggames.rts.gameFramework.GameEngine
import com.corrodinggames.rts.gameFramework.graphics.GamePaint
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


internal class SkiaShaderEffects(private val readAsset: (String) -> ByteArray?) : AutoCloseable {
    private val lock = Any()
    private val cache = mutableMapOf<String, RuntimeEffect?>()
    private var closed = false

    fun effectFor(program: ShaderProgram): RuntimeEffect? {
        val name = program.name
        synchronized(lock) {
            if (cache.containsKey(name)) return cache[name]
            if (closed) return null
            val effect = compile(name)
            cache[name] = effect
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
        } catch (_: Exception) {
            GameEngine.log("shader(" + program.name + "): draw failed, falling back to unshaded")
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
                val scaleX = src.width / dst.width
                val scaleY = src.height / dst.height
                val local = Matrix33(
                    scaleX, 0f, src.left - dst.left * scaleX,
                    0f, scaleY, src.top - dst.top * scaleY,
                    0f, 0f, 1f,
                )
                children.add(image.makeShader(FilterTileMode.CLAMP, FilterTileMode.CLAMP, sampling, local))
                builder.child("u_texture", children.last())
                return builder.makeShader()
            } catch (e: Exception) {
                children.forEach { runCatching { it.close() } }
                throw e
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
                children.add(childImage.makeShader(FilterTileMode.CLAMP, FilterTileMode.CLAMP, sampling, null))
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
            GameEngine.log("shader($name): SkSL unavailable, draws fall back to unshaded: " + e.message)
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
