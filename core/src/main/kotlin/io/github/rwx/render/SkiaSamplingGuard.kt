package io.github.rwx.render

import kotlin.math.abs
import kotlin.math.floor

/** Keeps mesh draws away from ambiguous nearest-neighbour sampling boundaries. */
internal class SkiaSamplingGuard {
    private val keys = IntArray(CACHE_SIZE * 3)
    private val answers = ByteArray(CACHE_SIZE)

    fun allowsBatch(start: Float, end: Float, texels: Float): Boolean {
        // A terrain grid repeats each axis's coordinates across every row/column.
        // All inputs are part of the key; collisions only trigger another scan.
        val startBits = start.toRawBits()
        val endBits = end.toRawBits()
        val texelBits = texels.toRawBits()
        var hash = (startBits * 31 + endBits) * 31 + texelBits
        hash = (hash xor (hash ushr 16)) * -0x7a143595
        val slot = (hash xor (hash ushr 13)) and (CACHE_SIZE - 1)
        val key = slot * 3
        val answer = answers[slot].toInt()
        if (answer != 0 && keys[key] == startBits && keys[key + 1] == endBits && keys[key + 2] == texelBits) {
            return answer == 2
        }
        val allowed = checkEdges(start, end, texels)
        keys[key] = startBits
        keys[key + 1] = endBits
        keys[key + 2] = texelBits
        answers[slot] = if (allowed) 2 else 1
        return allowed
    }

    private fun checkEdges(start: Float, end: Float, texels: Float): Boolean {
        if (!unambiguousPixelEdge(start) || !unambiguousPixelEdge(end)) return false
        val extent = end - start
        if (extent == texels && start == floor(start)) return true
        // Interpolated mesh coordinates and image rectangles can choose opposite
        // texels on a tie. Large images use the regular image path.
        if (texels > 64f) return false
        for (index in 1 until texels.toInt()) {
            if (!unambiguousPixelEdge((start.toDouble() + index * extent.toDouble() / texels).toFloat())) return false
        }
        return true
    }

    private fun unambiguousPixelEdge(value: Float): Boolean = value.isFinite() &&
            abs(value - floor(value) - .5f) > maxOf(.0001f, Math.ulp(value) * 4)

    private companion object {
        const val CACHE_SIZE = 512
    }
}
