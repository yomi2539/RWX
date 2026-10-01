package io.github.rwx.mod.api

import kotlin.collections.Map

interface Map {
    fun info(): MapInfo?
    fun tileAt(x: Int, y: Int): TerrainTile?
    fun addOverlay(x: Int, y: Int, binding: TerrainOverlayBinding)
    fun removeOverlay(x: Int, y: Int, rendererId: RendererId)
    fun overlayAt(x: Int, y: Int, rendererId: RendererId): TerrainOverlayBinding?
    fun overlaysAt(x: Int, y: Int): List<TerrainOverlayBinding>
    fun registerMap(definition: MapDefinition)
    fun registerTileset(definition: TilesetDefinition)
}

data class MapDefinition(
    val id: String,
    val name: LocalizedText,
    val file: ResourcePath,
    val preview: ResourcePath? = null,
    val tags: Set<String> = emptySet()
)

data class TilesetDefinition(
    val id: String,
    val image: ResourcePath,
    val properties: MutableMap<String, Any?> = mutableMapOf()
)

interface Rule {
    fun registerAiProfile(definition: AiProfileDefinition)
    fun registerGameMode(definition: GameModeDefinition)
    fun setGlobalRule(key: String, value: Any?)
}

data class AiProfileDefinition(
    val id: String,
    val properties: MutableMap<String, Any?> = mutableMapOf()
)

data class GameModeDefinition(
    val id: String,
    val displayName: LocalizedText,
    val properties: MutableMap<String, Any?> = mutableMapOf()
)

interface Localization {
    fun register(locale: String, entries: kotlin.collections.Map<String, String>)
    fun translate(key: String, locale: String? = null, args: kotlin.collections.Map<String, Any?> = emptyMap()): String
}

interface Audio {
    fun registerSound(id: String, file: ResourcePath, properties: kotlin.collections.Map<String, Any?> = emptyMap())
    fun registerMusic(id: String, file: ResourcePath, properties: Map<String, Any?> = emptyMap())
    fun playSound(id: String, x: Float? = null, y: Float? = null, volume: Float = 1f)
}
