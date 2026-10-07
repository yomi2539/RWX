package io.github.rwx.map

import com.corrodinggames.rts.game.map.TileMap
import io.github.rwx.logger
import io.github.rwx.PlatformStorage
import org.w3c.dom.Element
import org.xml.sax.InputSource
import org.xml.sax.SAXParseException
import org.xml.sax.helpers.DefaultHandler
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

object MapLinkResolver {
    private const val MAP_ID_PROPERTY = "rwxMapId"
    private const val MAP_ID_PROPERTY_ALIAS = "rwx_map_id"
    private const val LINKS_PROPERTY = "rwxMapLinks"
    private const val LINKS_PROPERTY_ALIAS = "rwx_map_links"
    private const val PORTAL_OBJECT_TYPE = "rwx_map_portal"

    private val assetPrefixes = listOf(
        "maps/skirmish",
        "maps/normal",
        "maps/challenge",
        "maps/survival",
        "maps/testing",
    )

    fun findMapPathById(storage: PlatformStorage, targetMapId: String?): String? {
        val id = targetMapId?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return buildMapIndex(storage).byId[normalizeId(id)]?.mapPath
    }

    fun missingLinkedMapIds(storage: PlatformStorage, sourceMapPath: String?): List<String> {
        return resolveLinkedMapGraph(storage, sourceMapPath).missingTargetMapIds
    }

    fun resolveLinkedMapGraph(storage: PlatformStorage, sourceMapPath: String?): LinkedMapGraph {
        val rootPath = sourceMapPath?.trim()?.takeIf { it.isNotEmpty() }
            ?: return LinkedMapGraph(rootMapPath = null)
        val index = buildMapIndex(storage)
        val maps = mutableListOf<LinkedMapNode>()
        val missing = linkedSetOf<String>()
        val visited = linkedSetOf<String>()
        val queue = ArrayDeque<String>()
        queue.add(rootPath)

        while (!queue.isEmpty()) {
            val mapPath = queue.removeFirst()
            val normalizedPath = normalizeMapPath(mapPath)
            if (!visited.add(normalizedPath)) {
                continue
            }
            val indexedEntry = index.byPath[normalizedPath]
            val info = indexedEntry?.info ?: readInfo(mapPath)
            val targetMapIds = info?.targetMapIds.orEmpty().distinct()
            maps += LinkedMapNode(
                mapId = info?.explicitMapId ?: existingMapIds(mapPath).firstOrNull().orEmpty(),
                mapPath = mapPath,
                targetMapIds = targetMapIds,
            )
            targetMapIds.forEach { targetId ->
                val target = index.byId[normalizeId(targetId)]
                if (target == null) {
                    missing += targetId
                } else if (normalizeMapPath(target.mapPath) !in visited) {
                    queue.add(target.mapPath)
                }
            }
        }
        return LinkedMapGraph(
            rootMapPath = rootPath,
            maps = maps,
            missingTargetMapIds = missing.toList(),
        )
    }

    fun readInfo(mapPath: String?): MapLinkInfo? {
        val path = mapPath?.takeIf { it.isNotBlank() } ?: return null
        return runCatching {
            TileMap.openMapInputStreamWithMovedFallback(path)?.use { parseInfo(it) }
        }.getOrElse { error ->
            logger.warn(error) { "RWX map link read failed for $path: ${error.message}" }
            null
        }
    }

    private fun buildMapIndex(storage: PlatformStorage): MapIndex {
        val byId = linkedMapOf<String, IndexedMap>()
        val byPath = linkedMapOf<String, IndexedMap>()
        fun addMap(path: String, info: MapLinkInfo?) {
            val normalizedPath = normalizeMapPath(path)
            if (normalizedPath in byPath) {
                return
            }
            val ids = existingMapIds(path).toMutableList()
            info?.explicitMapId?.let(ids::add)
            val entry = IndexedMap(path, info)
            byPath[normalizedPath] = entry
            ids.forEach { id ->
                byId.putIfAbsent(normalizeId(id), entry)
            }
        }
        assetPrefixes.forEach { prefix ->
            storage.listAssets(prefix)
                .asSequence()
                .map { it.replace('\\', '/').trim('/') }
                .filter { it.endsWith(".tmx", ignoreCase = true) }
                .forEach { path ->
                    addMap(path, readInfo(path))
                }
        }
        storage.mapsDir.file
            .takeIf(File::isDirectory)
            ?.walkTopDown()
            ?.filter { it.isFile && it.extension.equals("tmx", ignoreCase = true) }
            ?.forEach mapFile@{ file ->
                val relative = file.relativeTo(storage.mapsDir.file).invariantSeparatorsPath
                val virtualPath = storage.mapsDir.virtualPath.trimEnd('/') + "/" + relative
                val info = runCatching { parseInfo(file.inputStream()) }
                    .getOrElse { error ->
                        logger.warn(error) { "RWX map link read failed for $virtualPath: ${error.message}" }
                        return@mapFile
                    }
                addMap(virtualPath, info)
            }
        return MapIndex(byId = byId, byPath = byPath)
    }

    private fun parseInfo(input: InputStream): MapLinkInfo {
        input.use {
            val factory = DocumentBuilderFactory.newInstance()
            factory.isValidating = false
            val builder = factory.newDocumentBuilder()
            builder.setEntityResolver { _, _ -> InputSource(ByteArrayInputStream(ByteArray(0))) }
            builder.setErrorHandler(object : DefaultHandler() {
                override fun error(exception: SAXParseException) {
                    throw exception
                }

                override fun fatalError(exception: SAXParseException) {
                    throw exception
                }
            })
            val document = builder.parse(it)
            val root = document.documentElement
            val mapInfo = findMapInfo(root)
            val explicitMapId = readProperty(mapInfo, MAP_ID_PROPERTY)
                ?: readProperty(mapInfo, MAP_ID_PROPERTY_ALIAS)
            val linkText = readProperty(mapInfo, LINKS_PROPERTY)
                ?: readProperty(mapInfo, LINKS_PROPERTY_ALIAS)
            val targetIds = linkedSetOf<String>()
            parseLinkedTargetIds(linkText).forEach(targetIds::add)
            val objects = root.getElementsByTagName("object")
            for (index in 0 until objects.length) {
                val element = objects.item(index) as? Element ?: continue
                if (!element.getAttribute("type").equals(PORTAL_OBJECT_TYPE, ignoreCase = true)) {
                    continue
                }
                readProperty(element, "targetMapId")?.let(targetIds::add)
            }
            return MapLinkInfo(explicitMapId = explicitMapId, targetMapIds = targetIds.toList())
        }
    }

    private fun existingMapIds(mapPath: String): List<String> {
        val normalizedPath = mapPath.replace('\\', '/').trim()
        val fileName = normalizedPath.substringAfterLast('/')
        val baseName = fileName.removeSuffix(".tmx")
        return listOf(normalizedPath, fileName, baseName)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    private fun normalizeId(id: String): String =
        id.replace('\\', '/')
            .trim()
            .removeSuffix(".tmx")
            .replace(Regex("\\s+"), " ")
            .lowercase()

    private fun normalizeMapPath(mapPath: String): String =
        mapPath.replace('\\', '/').trim().lowercase()

    private fun parseLinkedTargetIds(text: String?): List<String> {
        if (text.isNullOrBlank()) {
            return emptyList()
        }
        return text.split('\n', ';')
            .mapNotNull { entry ->
                val parts = entry.trim().split('|', ',').map { it.trim() }.filter { it.isNotEmpty() }
                when {
                    parts.size >= 2 -> parts[1]
                    parts.size == 1 -> parts[0]
                    else -> null
                }
            }
    }

    private fun findMapInfo(root: Element): Element? {
        val objects = root.getElementsByTagName("object")
        for (index in 0 until objects.length) {
            val element = objects.item(index) as? Element ?: continue
            if (element.getAttribute("name").equals("map_info", ignoreCase = true)) {
                return element
            }
        }
        return null
    }

    private fun readProperty(objectElement: Element?, name: String): String? {
        if (objectElement == null) {
            return null
        }
        val properties = objectElement.getElementsByTagName("property")
        for (index in 0 until properties.length) {
            val property = properties.item(index) as? Element ?: continue
            if (!property.getAttribute("name").equals(name, ignoreCase = true)) {
                continue
            }
            val value = if (property.hasAttribute("value")) {
                property.getAttribute("value")
            } else {
                property.textContent
            }
            return value?.trim()?.takeIf { it.isNotEmpty() }
        }
        return null
    }
}

data class MapLinkInfo(
    val explicitMapId: String?,
    val targetMapIds: List<String>,
)

data class LinkedMapGraph(
    val rootMapPath: String?,
    val maps: List<LinkedMapNode> = emptyList(),
    val missingTargetMapIds: List<String> = emptyList(),
)

data class LinkedMapNode(
    val mapId: String,
    val mapPath: String,
    val targetMapIds: List<String>,
) {
    val fileName: String get() = mapPath.replace('\\', '/').substringAfterLast('/')
}

private data class MapIndex(
    val byId: Map<String, IndexedMap>,
    val byPath: Map<String, IndexedMap>,
)

private data class IndexedMap(
    val mapPath: String,
    val info: MapLinkInfo?,
)
