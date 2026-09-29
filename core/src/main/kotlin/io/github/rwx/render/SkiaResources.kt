package io.github.rwx.render

import com.corrodinggames.rts.R
import com.corrodinggames.rts.gameFramework.graphics.Texture
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint

internal class SkiaResources(val readAsset: (String) -> ByteArray?) : AutoCloseable {
    private val textures = mutableSetOf<SkiaTexture>()
    private val imports = LinkedHashMap<Texture, ImportedTexture>(16, 0.75f, true)
    val fonts = SkiaFonts(readAsset)
    val shaderEffects = SkiaShaderEffects(readAsset)
    var isClosed = false
        private set
    val fallback: SkiaTexture by lazy {
        create(2, 2, false).apply {
            setCommittedArgbPixels(intArrayOf(-0xff01, -0x1000000, -0x1000000, -0xff01))
            setSourceName("missing-image")
        }
    }

    fun create(width: Int, height: Int, alpha: Boolean): SkiaTexture {
        check(!isClosed) { "Renderer is closed" }
        require(width > 0 && height > 0) { "Texture dimensions must be positive" }
        Math.multiplyExact(Math.multiplyExact(width, height), 4)
        return SkiaTexture(width, height, alpha, ::create) { textures.remove(it) }.also { textures.add(it) }
    }

    fun decode(bytes: ByteArray, name: String? = null): SkiaTexture {
        val image = try {
            Image.makeFromEncoded(bytes)
        } catch (_: IllegalArgumentException) {
            return fallback
        }
        return image.use {
            create(it.width, it.height, !it.isOpaque).apply {
                Paint().use { paint ->
                    paint.blendMode = BlendMode.SRC
                    surface().canvas.drawImage(it, 0f, 0f, paint)
                }
                rendered()
                setSourceName(name)
            }
        }
    }

    fun drawable(id: Int): SkiaTexture {
        val name = drawableNames[id] ?: return fallback
        for (prefix in listOf("drawable/", "assets/drawable/")) {
            for (extension in listOf(".png", ".9.png", ".jpg", ".jpeg")) {
                val path = "$prefix$name$extension"
                readAsset(path)?.let { return decode(it, path) }
            }
        }
        return fallback
    }

    fun resolve(texture: Texture): SkiaTexture {
        check(!isClosed) { "Renderer is closed" }
        if (texture is SkiaTexture) {
            check(!texture.isClosed) { "Texture is closed" }
            return texture
        }
        val source = texture.resolvedTexture()
        if (source !== texture) return resolve(source)
        val cached = imports[texture]
        if (cached != null && cached.matches(texture)) return cached.texture
        cached?.texture?.close()
        val converted = create(texture.width(), texture.height(), texture.m)
        val pixels = texture.argbPixelsCopy
        if (pixels != null) {
            converted.setCommittedArgbPixels(pixels)
            converted.setPremultipliedAlpha(texture.usesPremultipliedAlpha())
        }
        imports[texture] =
            ImportedTexture(converted, texture.getPixelRevision(), texture.e, texture.usesPremultipliedAlpha())
        if (imports.size > 256) {
            val oldest = imports.entries.iterator()
            oldest.next().value.texture.close()
            oldest.remove()
        }
        return converted
    }

    override fun close() {
        if (isClosed) return
        isClosed = true
        textures.toList().forEach { it.close() }
        imports.clear()
        fonts.close()
        shaderEffects.close()
    }

    private data class ImportedTexture(
        val texture: SkiaTexture,
        val pixels: Int,
        val version: Int,
        val premultiplied: Boolean
    ) {
        fun matches(source: Texture) =
            !texture.isClosed && pixels == source.getPixelRevision() && version == source.e &&
                    texture.width() == source.width() && texture.height() == source.height() && premultiplied == source.usesPremultipliedAlpha()
    }

    private companion object {
        val drawableNames by lazy { R.drawable::class.java.fields.associate { it.getInt(null) to it.name } }
    }
}
