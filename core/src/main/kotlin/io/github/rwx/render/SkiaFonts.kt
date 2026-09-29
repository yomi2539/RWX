package io.github.rwx.render

import io.github.rwx.render.canvas.Paint
import org.jetbrains.skia.Data
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.TextBlob
import org.jetbrains.skia.TextLine
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.shaper.FontMgrRunIterator
import org.jetbrains.skia.shaper.HbIcuScriptRunIterator
import org.jetbrains.skia.shaper.IcuBidiRunIterator
import org.jetbrains.skia.shaper.Shaper
import org.jetbrains.skia.shaper.ShapingOptions
import org.jetbrains.skia.shaper.TextBlobBuilderRunHandler
import org.jetbrains.skia.shaper.TrivialBidiRunIterator
import org.jetbrains.skia.shaper.TrivialFontRunIterator
import org.jetbrains.skia.shaper.TrivialLanguageRunIterator
import org.jetbrains.skia.shaper.TrivialScriptRunIterator

internal class ShapedText : AutoCloseable {
    val width: Float
    private val ascentValue: Lazy<Float>
    private val heightValue: Lazy<Float>
    val ascent: Float get() = ascentValue.value
    val height: Float get() = heightValue.value
    private val line: TextLine?
    private val lazyBlob: Lazy<TextBlob?>?
    private var directBlob: TextBlob?
    val blob: TextBlob? get() = directBlob ?: lazyBlob?.value

    constructor(line: TextLine) {
        this.line = line
        width = line.width
        ascentValue = lazy(LazyThreadSafetyMode.NONE) { line.ascent }
        heightValue = lazy(LazyThreadSafetyMode.NONE) { line.height }
        lazyBlob = lazy(LazyThreadSafetyMode.NONE) { line.textBlob }
        directBlob = null
    }

    constructor(blob: TextBlob?, width: Float, ascent: Float, height: Float) {
        line = null
        lazyBlob = null
        directBlob = blob
        this.width = width
        ascentValue = lazy(LazyThreadSafetyMode.NONE) { ascent }
        heightValue = lazy(LazyThreadSafetyMode.NONE) { height }
    }

    override fun close() {
        if (lazyBlob?.isInitialized() == true) lazyBlob?.value?.close()
        directBlob?.close()
        directBlob = null
        line?.close()
    }
}

/** Bounded caches: changing counters and chat text must not retain native objects indefinitely. */
internal class SkiaFonts(private val readAsset: (String) -> ByteArray?) : AutoCloseable {
    private val typefaces = mutableMapOf<String, ResolvedTypeface>()
    private val fonts = LinkedHashMap<FontKey, Font>(16, 0.75f, true)
    private val lines = LinkedHashMap<Pair<FontKey, String>, ShapedText>(16, 0.75f, true)
    private var shaper: Shaper? = null

    private fun legacyShaper(): Shaper =
        shaper ?: Shaper.makeShapeDontWrapOrReorder(FontMgr.default).also { shaper = it }

    fun line(text: String, paint: Paint?): ShapedText = line(text, keyFor(text, paint))

    private fun line(text: String, key: FontKey): ShapedText =
        lines.getOrPut(key to text) {
            // TextLine keeps shaping advances and fallback-font metrics together, and
            // its blob is baseline-relative. Only materialize the blob when drawing.
            val shaped = ShapedText(legacyShaper().shapeLine(text, fontFor(key)))
            trim(fonts, 64) { it.close() }
            shaped
        }.also { trim(lines, 256) { it.close() } }

    /** Consume each row before advancing: later rows may evict its cached native line. */
    fun shapedLines(text: String, paint: Paint?): Sequence<ShapedText?> {
        val key = keyFor(text, paint)
        return text.split('\n').asSequence().map { row ->
            val clean = row.trimEnd('\r')
            if (clean.isEmpty()) null else line(clean, key)
        }
    }

    /** Recommended baseline-to-baseline advance for the full text's font. */
    fun lineSpacing(text: String, paint: Paint?): Float =
        fontFor(keyFor(text, paint)).spacing

    private fun keyFor(text: String, paint: Paint?): FontKey =
        FontKey(
            paint?.typeface()?.key ?: "default:0",
            (paint?.textSize() ?: 16f).coerceAtLeast(0f),
            text.any { it.code > 255 },
        )

    private fun fontFor(key: FontKey): Font =
        fonts.getOrPut(key) {
            val resolved = typeface(key)
            Font(resolved.face, key.size).apply {
                isEmboldened = resolved.syntheticStyle and 1 != 0
                skewX = if (resolved.syntheticStyle and 2 != 0) -0.25f else 0f
            }
        }

    private fun typeface(key: FontKey): ResolvedTypeface {
        val family = key.typeface.substringBeforeLast(':')
        val style = key.typeface.substringAfterLast(':').toIntOrNull() ?: 0
        val cacheKey = if (key.unicode) "unicode:$style" else key.typeface
        typefaces[cacheKey]?.let { return it }
        val fontStyle = when (style) {
            1 -> FontStyle.BOLD
            2 -> FontStyle.ITALIC
            3 -> FontStyle.BOLD_ITALIC
            else -> FontStyle.NORMAL
        }
        val path = when {
            key.unicode -> "font/DroidSansFallback.ttf"
            family == "default" || family == "sans-serif" -> "font/Roboto-Regular.ttf"
            else -> null
        }
        val bytes = path?.let(readAsset)
        val bundledFace = if (bytes != null) {
            Data.makeFromBytes(bytes).use { FontMgr.default.makeFromData(it) }
        } else null
        val face = bundledFace
            ?: FontMgr.default.matchFamilyStyle(if (family == "default") "sans-serif" else family, fontStyle)
            ?: FontMgr.default.matchFamilyStyle("sans-serif", fontStyle)
        // Only regular faces are bundled. Synthesize their requested style instead
        // of discarding them and passing a missing default family to Font.
        val resolved = ResolvedTypeface(face, if (bundledFace != null) style else 0)
        typefaces[cacheKey] = resolved
        return resolved
    }

    override fun close() {
        lines.values.forEach { it.close() }
        fonts.values.forEach { it.close() }
        shaper?.close()
        shaper = null
        typefaces.values.forEach { it.face?.close() }
        lines.clear()
        fonts.clear()
        typefaces.clear()
    }

    private fun <K, V> trim(cache: MutableMap<K, V>, limit: Int, close: (V) -> Unit) {
        if (cache.size > limit) {
            val iterator = cache.entries.iterator()
            close(iterator.next().value)
            iterator.remove()
        }
    }

    private data class FontKey(val typeface: String, val size: Float, val unicode: Boolean)
    private data class ResolvedTypeface(val face: Typeface?, val syntheticStyle: Int)
}
