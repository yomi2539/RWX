package io.github.rwx.mod.registry

import com.corrodinggames.rts.game.map.LayerBufferCell
import com.corrodinggames.rts.game.map.TileMap
import com.corrodinggames.rts.gameFramework.GameEngine
import com.corrodinggames.rts.gameFramework.graphics.*
import io.github.rwx.geometry.Rect
import io.github.rwx.geometry.RectF
import io.github.rwx.mod.api.RendererId
import io.github.rwx.mod.api.TerrainOverlayBinding
import io.github.rwx.mod.api.TerrainOverlayContext
import io.github.rwx.mod.api.TerrainOverlayRenderer
import io.github.rwx.mod.api.TerrainShaderDefinition
import io.github.rwx.mod.impl.ApiImpl
import io.github.rwx.mod.registry.RenderRegistry.EngineRenderCanvas
import io.github.rwx.render.canvas.Paint
import java.util.IdentityHashMap
import kotlin.math.ceil
import kotlin.math.floor

object TerrainRegistry : OwnedRegistry {
    private data class Registration(val owner: ApiImpl, val definition: TerrainShaderDefinition)
    private data class Mask(val texture: Texture, val graphics: GraphicsEngine, var dirty: Boolean = true)
    private data class OverlayEntry(val owner: ApiImpl, val binding: TerrainOverlayBinding)
    private data class OverlayRendererEntry(val owner: ApiImpl, val renderer: TerrainOverlayRenderer)

    private var registration: Registration? = null
    private var backend: GraphicsEngine? = null
    private var shader: ShaderProgram? = null
    private var noise: Texture? = null
    private val masks = IdentityHashMap<LayerBufferCell, Mask>()
    private val overlays = IdentityHashMap<TileMap, MutableMap<Long, MutableMap<String, OverlayEntry>>>()
    private val overlayRenderers = linkedMapOf<String, OverlayRendererEntry>()
    private val failures = ModFailureLog("Terrain material")

    @Synchronized
    fun registerShader(owner: ApiImpl, definition: TerrainShaderDefinition) {
        check(registration == null) { "A terrain shader is already registered" }
        require(definition.id.substringBefore(':') == owner.manifest.id)
        registration = Registration(owner, definition)
        TileMap.layerBufferManager.invalidateAllCells()
    }

    @Synchronized
    fun registerOverlayRenderer(owner: ApiImpl, id: RendererId, renderer: TerrainOverlayRenderer) {
        check(id.value !in overlayRenderers) { "Terrain overlay renderer is already registered: ${id.value}" }
        overlayRenderers[id.value] = OverlayRendererEntry(owner, renderer)
    }

    @Synchronized
    override fun unregister(owner: ApiImpl) {
        if (registration?.owner === owner) {
            releaseGraphics()
            registration = null
        }
        overlayRenderers.entries.removeAll { (_, entry) -> entry.owner === owner }
        overlays.values.forEach { cells ->
            cells.values.forEach { slots -> slots.entries.removeAll { (_, entry) -> entry.owner === owner } }
            cells.entries.removeAll { (_, slots) -> slots.isEmpty() }
        }
        TileMap.layerBufferManager.invalidateAllCells()
    }

    @JvmStatic
    @Synchronized
    fun releaseGraphics() {
        masks.values.forEach { it.graphics.q(); it.texture.releaseTexture() }
        masks.clear()
        shader?.programStatus = 1
        shader = null
        noise?.releaseTexture()
        noise = null
        backend = null
    }

    @JvmStatic
    @Synchronized
    fun unloadMap() {
        overlays.clear()
    }

    @JvmStatic
    @Synchronized
    fun invalidateCell(cell: LayerBufferCell) {
        masks[cell]?.dirty = true
    }

    @Synchronized
    fun addOverlay(owner: ApiImpl, map: TileMap, x: Int, y: Int, binding: TerrainOverlayBinding) {
        require(x in 0 until map.tileCountX && y in 0 until map.tileCountY)
        val key = key(x, y)
        val entries = overlays.getOrPut(map) { HashMap() }
        val slots = entries.getOrPut(key) { HashMap() }
        val slotKey = binding.rendererId.value
        val previous = slots[slotKey]
        if (previous?.binding == binding) return
        slots[slotKey] = OverlayEntry(owner, binding)
        TileMap.layerBufferManager.invalidateTileArea(x, y, false)
    }

    @Synchronized
    fun removeOverlay(owner: ApiImpl, map: TileMap, x: Int, y: Int, rendererId: RendererId) {
        require(x in 0 until map.tileCountX && y in 0 until map.tileCountY)
        val key = key(x, y)
        val slots = overlays[map]?.get(key) ?: return
        val entry = slots[rendererId.value] ?: return
        check(entry.owner === owner)
        slots.remove(rendererId.value)
        if (slots.isEmpty()) {
            overlays[map]?.remove(key)
            if (overlays[map]?.isEmpty() == true) overlays.remove(map)
        }
        TileMap.layerBufferManager.invalidateTileArea(x, y, false)
    }

    @Synchronized
    fun overlayAt(owner: ApiImpl, map: TileMap, x: Int, y: Int, rendererId: RendererId): TerrainOverlayBinding? {
        if (x !in 0 until map.tileCountX || y !in 0 until map.tileCountY) return null
        return overlays[map]?.get(key(x, y))?.get(rendererId.value)?.binding
    }

    @Synchronized
    fun overlaysAt(owner: ApiImpl, map: TileMap, x: Int, y: Int): List<TerrainOverlayBinding> {
        if (x !in 0 until map.tileCountX || y !in 0 until map.tileCountY) return emptyList()
        val slots = overlays[map]?.get(key(x, y)) ?: return emptyList()
        if (slots.isEmpty()) return emptyList()
        return slots.values.map { it.binding }.sortedBy { it.rendererId.value }
    }

    @JvmStatic
    @Synchronized
    fun drawOverlay(map: TileMap, x: Int, y: Int, graphics: GraphicsEngine, bounds: RectF) {
        val slots = overlays[map]?.get(key(x, y)) ?: return
        if (slots.isEmpty()) return
        val snapshot = slots.values.map { it.binding }.sortedBy { it.rendererId.value }
        val width = bounds.c - bounds.a
        val height = bounds.d - bounds.b
        val gameTimeMillis = GameEngine.getInstance().gameTimeMillis.toLong()
        for ((rendererId, variantId) in snapshot) {
            val entry = overlayRenderers[rendererId.value] ?: continue
            val ctx = TerrainOverlayContext(
                x = x,
                y = y,
                centerX = (bounds.a + bounds.c) / 2f,
                centerY = (bounds.b + bounds.d) / 2f,
                width = width,
                height = height,
                gameTimeMillis = gameTimeMillis,
                variantId = variantId,
            )
            val canvas = EngineRenderCanvas(graphics)
            try {
                failures.runSafelyOncePerId("terrain-overlay:${rendererId.value}") {
                    entry.renderer.render(ctx, canvas)
                }
            } finally {
                canvas.finish()
            }
        }
    }

    @JvmStatic
    @Synchronized
    fun drawCell(
        cell: LayerBufferCell, map: TileMap, graphics: GraphicsEngine, resources: GraphicsEngine,
        texture: Texture, source: Rect, destination: RectF, paint: Paint, renderScale: Float,
    ): Boolean {
        val registered = registration ?: return false
        if (texture.A()) return false
        var rendered = false
        failures.runSafelyOncePerId(registered.definition.id) {
            if (backend !== resources) {
                releaseGraphics(); backend = resources
            }
            val program = shader ?: createShader(registered, resources).also { shader = it }
            val mask = masks.getOrPut(cell) {
                val image = resources.a(cell.cellLayerTexture.width(), cell.cellLayerTexture.height(), true)
                Mask(image, resources.b(image, RenderTargetMode.IMMEDIATE))
            }
            if (mask.dirty) {
                updateMask(mask, cell, map, renderScale)
                mask.dirty = false
            }
            val sx = (source.c - source.a) / (destination.c - destination.a)
            val sy = (source.d - source.b) / (destination.d - destination.b)
            program.a("u_material", mask.texture)
            program.b("u_textureSize", texture)
            program.a("u_canvasOrigin", destination.a, destination.b)
            program.a("u_sourceOrigin", source.a.toFloat(), source.b.toFloat())
            program.a("u_sourceScale", sx, sy)
            program.a(
                "u_worldOrigin", (cell.worldLeft + source.a / renderScale) * 8f / map.tileWorldSizeX,
                -(cell.worldTop + source.b / renderScale) * 8f / map.tileWorldSizeY
            )
            program.a(
                "u_worldPerPixel",
                sx / renderScale * 8f / map.tileWorldSizeX,
                sy / renderScale * 8f / map.tileWorldSizeY
            )
            program.a("u_time", GameEngine.getInstance().gameTimeMillis * .06f)
            val shaded = GamePaint().apply { a(paint.isFilterBitmap()); c(paint.f()); a(program) }
            graphics.a(texture, source, destination, shaded)
            rendered = true
        }
        return rendered
    }

    private fun createShader(entry: Registration, resources: GraphicsEngine): ShaderProgram {
        val definition = entry.definition
        noise = entry.owner.openPackagedResource(definition.noise).use { resources.a(it, true) }
        return object : ShaderProgram() {
            override fun f() = Unit
        }.apply {
            name = definition.id
            vertexSource =
                "#version 130\nvarying vec2 v_texCoords; varying vec4 v_color;\nvoid main(){gl_Position=gl_ProjectionMatrix*gl_ModelViewMatrix*gl_Vertex;v_texCoords=gl_MultiTexCoord0.xy;v_color=gl_Color;}"
            fragmentSource =
                entry.owner.openPackagedResource(definition.fragment).bufferedReader().use { it.readText() }
            skslSource = entry.owner.openPackagedResource(definition.sksl).bufferedReader().use { it.readText() }
            programStatus = 0
            a("u_noise").apply { a(noise); repeatTexture = true; linearTexture = true }
            b("u_noiseSize", noise)
        }
    }

    private fun updateMask(mask: Mask, cell: LayerBufferCell, map: TileMap, scale: Float) {
        val graphics = mask.graphics
        graphics.o()
        val width = mask.texture.width() / scale
        val height = mask.texture.height() / scale
        val x0 = floor(cell.worldLeft.toFloat() / map.tileWorldSizeX).toInt().coerceAtLeast(0)
        val y0 = floor(cell.worldTop.toFloat() / map.tileWorldSizeY).toInt().coerceAtLeast(0)
        val x1 = ceil((cell.worldLeft + width) / map.tileWorldSizeX).toInt().coerceAtMost(map.tileCountX)
        val y1 = ceil((cell.worldTop + height) / map.tileWorldSizeY).toInt().coerceAtMost(map.tileCountY)
        val fog = GameEngine.getInstance().playerTeam?.fogOfWarData
        val paint = Paint()
        for (x in x0 until x1) for (y in y0 until y1) {
            val tile = map.getTileAt(x, y) ?: continue
            if (map.fogEnabled && fog != null && fog[x][y].toInt() != 0) continue
            if (tile.isWater && !tile.isWaterBridge) paint.a(255, 255, 0, 0)
            else if (tile.isLava && !tile.isCliff) paint.a(255, 0, 255, 0)
            else continue
            graphics.a(
                RectF(
                    (x * map.tileWorldSizeX - cell.worldLeft) * scale,
                    (y * map.tileWorldSizeY - cell.worldTop) * scale,
                    ((x + 1) * map.tileWorldSizeX - cell.worldLeft) * scale,
                    ((y + 1) * map.tileWorldSizeY - cell.worldTop) * scale
                ), paint
            )
        }
        graphics.p()
    }

    private fun key(x: Int, y: Int) = (x.toLong() shl 32) or (y.toLong() and 0xffffffffL)
}
