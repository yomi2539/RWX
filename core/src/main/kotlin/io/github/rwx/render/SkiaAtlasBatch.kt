package io.github.rwx.render

import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.VertexMode

/** Ordered nearest-neighbour image quads, accumulated only on an untransformed retained target. */
internal class SkiaAtlasBatch(private val buffers: SkiaAtlasBuffers) : AutoCloseable {
    private val positions = FloatArray(MAX_QUADS * 12)
    private val coordinates = FloatArray(MAX_QUADS * 12)
    private var count = 0
    private var image: Image? = null
    private var originalPaint: Paint? = null
    private var batchPaint: Paint? = null

    fun add(
        canvas: Canvas, image: Image, paint: Paint,
        sl: Float, st: Float, sr: Float, sb: Float,
        dl: Float, dt: Float, dr: Float, db: Float,
    ) {
        if (this.image !== image || originalPaint != paint || count == positions.size) {
            flush(canvas)
            this.image = image
            originalPaint = paint.makeClone()
            batchPaint = paint.makeClone().apply {
                image.makeShader(null).use { shader = it }
            }
        }
        quad(positions, dl, dt, dr, db)
        quad(coordinates, sl, st, sr, sb)
        count += 12
    }

    private fun quad(values: FloatArray, l: Float, t: Float, r: Float, b: Float) {
        values[count] = l; values[count + 1] = t
        values[count + 2] = r; values[count + 3] = t
        values[count + 4] = r; values[count + 5] = b
        values[count + 6] = l; values[count + 7] = t
        values[count + 8] = r; values[count + 9] = b
        values[count + 10] = l; values[count + 11] = b
    }

    fun flush(canvas: Canvas) {
        if (count == 0) return
        val vertices: FloatArray
        val uv: FloatArray
        if (count == positions.size) {
            vertices = positions
            uv = coordinates
        } else {
            val partial = buffers.get(count)
            positions.copyInto(partial.positions, endIndex = count)
            coordinates.copyInto(partial.coordinates, endIndex = count)
            vertices = partial.positions
            uv = partial.coordinates
        }
        // Skia copies the mesh into the picture; these staging arrays can be reused.
        canvas.drawVertices(
            VertexMode.TRIANGLES, vertices, texCoords = uv,
            blendMode = BlendMode.MODULATE, paint = checkNotNull(batchPaint),
        )
        close()
    }

    override fun close() {
        count = 0
        image = null
        originalPaint?.close()
        originalPaint = null
        batchPaint?.close()
        batchPaint = null
    }

    private companion object {
        const val MAX_QUADS = 2048
    }
}

/** Exact-sized JNI staging arrays shared on the render thread; native draws copy them. */
internal class SkiaAtlasBuffers {
    class Buffer(size: Int) {
        val positions = FloatArray(size)
        val coordinates = FloatArray(size)
    }

    private val entries = LinkedHashMap<Int, Buffer>(16, .75f, true)
    private var bytes = 0

    fun get(size: Int): Buffer = entries[size] ?: Buffer(size).also { buffer ->
        entries[size] = buffer
        bytes += size * Float.SIZE_BYTES * 2
        val oldest = entries.entries.iterator()
        while (bytes > MAX_BYTES) {
            bytes -= oldest.next().key * Float.SIZE_BYTES * 2
            oldest.remove()
        }
    }

    fun clear() {
        entries.clear()
        bytes = 0
    }

    private companion object {
        const val MAX_BYTES = 4 * 1024 * 1024
    }
}
