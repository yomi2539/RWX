package io.github.rwx

import io.github.rwx.render.RenderBackend
import java.util.Locale

internal enum class DesktopRenderBackend : RenderBackend {
    Slick,
    Skia;

    override val id: String = name.lowercase(Locale.ROOT)

    companion object {
        fun fromId(rendererId: String?): DesktopRenderBackend {
            val normalized = rendererId?.trim()?.lowercase().orEmpty()
            return when (normalized) {
                "", Slick.name.lowercase() -> Slick
                Skia.name.lowercase() -> Skia
                else -> throw IllegalArgumentException("Unsupported desktop renderer: $rendererId")
            }
        }

        fun selectedId(cliBackendId: String?, storedBackendId: String?): DesktopRenderBackend {
            cliBackendId?.trim()?.takeIf { it.isNotEmpty() }?.let { return fromId(it) }
            return fromId(storedBackendId)
        }
    }
}
