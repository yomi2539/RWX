package io.github.rwx.mod.api

data class TerrainShaderDefinition(
    val id: String,
    val fragment: String,
    val sksl: String,
    val noise: String,
)

data class MapInfo(val widthTiles: Int, val heightTiles: Int, val tileWidth: Float, val tileHeight: Float)

data class TerrainTile(
    val water: Boolean,
    val lava: Boolean,
    val cliff: Boolean,
    val blocksBuildings: Boolean,
)

data class TerrainOverlayBinding(
    val rendererId: String,
    val variantId: String,
)

fun interface TerrainOverlayRenderer {
    fun render(ctx: TerrainOverlayContext, canvas: RenderCanvas)
}

data class TerrainOverlayContext(
    val x: Int,
    val y: Int,
    val centerX: Float,
    val centerY: Float,
    val width: Float,
    val height: Float,
    val gameTimeMillis: Long,
    val variantId: String,
)
