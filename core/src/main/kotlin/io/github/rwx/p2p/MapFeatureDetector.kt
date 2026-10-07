package io.github.rwx.p2p

import com.corrodinggames.rts.game.map.TileMap
import io.github.rwx.logger
import com.corrodinggames.rts.gameFramework.mission.AreaControlMode
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import javax.xml.parsers.DocumentBuilderFactory

object MapFeatureDetector {
    /**
     * Upper bound for a single lightweight probe. Big custom maps can reach
     * several MB (mostly base64 layer data), so the list path must never hold
     * a whole map in memory.
     */
    private const val LIGHTWEIGHT_READ_LIMIT_BYTES = 64 * 1024

    private val requiredFeaturesByPath = ConcurrentHashMap<String, List<String>>()

    /**
     * Keys whose cached entry came from a full DOM parse ([ensureRequiredFeaturesForMap])
     * rather than the lightweight prefix probe. A lightweight entry is provisional:
     * the prefix may not reach `map_info` on multi-MB maps, which the list tolerates
     * as "unknown features" but room hosting must not.
     */
    private val exactPaths = ConcurrentHashMap.newKeySet<String>()

    fun requiredFeaturesForMap(mapPath: String?): List<String> {
        val path = mapPath?.trim()?.takeIf { it.isNotBlank() } ?: return emptyList()
        return requiredFeaturesByPath.computeIfAbsent(cacheKey(path)) {
            detectLightweightFeatures(it)
        }
    }

    /**
     * Full-parse detection for consumers that cannot tolerate "unknown"
     * (room hosting / P2P handshake). Runs the original exact DOM detection and
     * overwrites any provisional lightweight entry cached by [requiredFeaturesForMap].
     * Stays a plain blocking call so existing call sites keep working unchanged.
     */
    fun ensureRequiredFeaturesForMap(mapPath: String?): List<String> {
        val path = mapPath?.trim()?.takeIf { it.isNotBlank() } ?: return emptyList()
        val key = cacheKey(path)
        val cached = requiredFeaturesByPath[key]
        if (cached != null && key in exactPaths) {
            return cached
        }
        val exact = detectExactFeatures(path)
        requiredFeaturesByPath[key] = exact
        exactPaths += key
        return exact
    }

    internal fun clearCacheForTests() {
        requiredFeaturesByPath.clear()
        exactPaths.clear()
    }

    private fun cacheKey(path: String): String =
        path.replace('\\', '/')

    /**
     * List-path probe: reads at most [LIGHTWEIGHT_READ_LIMIT_BYTES] from the head
     * of the stream and matches feature keywords as plain text. Never builds a DOM,
     * never holds more than 64KB per map, and always closes the stream via `use{}`.
     */
    private fun detectLightweightFeatures(path: String): List<String> {
        return runCatching {
            TileMap.openMapInputStreamWithMovedFallback(path)?.use { input ->
                detectFromHead(readHeadUtf8(input, LIGHTWEIGHT_READ_LIMIT_BYTES))
            } ?: emptyList()
        }.getOrElse { error ->
            logger.warn(error) { "RWX map feature detection failed for $path: ${error.message}" }
            emptyList()
        }
    }

    private fun detectExactFeatures(path: String): List<String> {
        return runCatching {
            TileMap.openMapInputStreamWithMovedFallback(path)?.use { input ->
                val builderFactory = DocumentBuilderFactory.newInstance()
                builderFactory.isValidating = false
                val builder = builderFactory.newDocumentBuilder()
                builder.setEntityResolver { _, _ -> InputSource(ByteArrayInputStream(ByteArray(0))) }
                val document = builder.parse(input)
                val mapInfo = findMapInfo(document.documentElement) ?: return@use emptyList()
                val mode = readProperty(mapInfo, AreaControlMode.MAP_INFO_PROPERTY)
                    ?: readProperty(mapInfo, AreaControlMode.MAP_INFO_PROPERTY_ALIAS)
                val features = mutableListOf<String>()
                when {
                    AreaControlMode.isAreaControlMode(mode) -> features += FeatureIds.AREA_CONTROL
                    !mode.isNullOrBlank() -> features += "mode:${mode.trim()}"
                }
                if (hasMapLinks(mapInfo, document.documentElement)) {
                    features += FeatureIds.MAP_LINKS
                }
                features.distinct()
            } ?: emptyList()
        }.getOrElse { error ->
            logger.warn(error) { "RWX map feature detection failed for $path: ${error.message}" }
            emptyList()
        }
    }

    private fun readHeadUtf8(input: InputStream, limitBytes: Int): String {
        val buffer = ByteArray(8192)
        val out = ByteArrayOutputStream(minOf(limitBytes, LIGHTWEIGHT_READ_LIMIT_BYTES))
        var remaining = limitBytes
        while (remaining > 0) {
            val read = input.read(buffer, 0, minOf(buffer.size, remaining))
            if (read < 0) {
                break
            }
            out.write(buffer, 0, read)
            remaining -= read
        }
        return out.toString(Charsets.UTF_8.name())
    }

    private fun detectFromHead(head: String): List<String> {
        val lower = head.lowercase(Locale.ROOT)
        val features = mutableListOf<String>()
        val mode = propertyValueFromHead(head, AreaControlMode.MAP_INFO_PROPERTY)
            ?: propertyValueFromHead(head, AreaControlMode.MAP_INFO_PROPERTY_ALIAS)
        when {
            mode != null && AreaControlMode.isAreaControlMode(mode) -> features += FeatureIds.AREA_CONTROL
            !mode.isNullOrBlank() -> features += "mode:${mode.trim()}"
        }
        if (lower.contains("rwxmaplinks") || lower.contains("rwx_map_links") ||
            lower.contains("rwx_map_portal")
        ) {
            features += FeatureIds.MAP_LINKS
        }
        return features.distinct()
    }

    private fun propertyValueFromHead(head: String, propertyName: String): String? {
        var match = propertyOpenTag(propertyName).find(head)
        while (match != null) {
            val tag = match.value
            val attr = propertyValueAttribute.find(tag)?.groupValues?.getOrNull(1)?.trim()
            if (!attr.isNullOrEmpty()) {
                return attr
            }
            if (!tag.trimEnd().endsWith("/>")) {
                val text = head.substring(match.range.last + 1).substringBefore('<').trim()
                if (text.isNotEmpty()) {
                    return text
                }
            }
            match = match.next()
        }
        return null
    }

    private fun propertyOpenTag(propertyName: String): Regex =
        Regex(
            "<property\\b[^<>]*name\\s*=\\s*[\"']" + Regex.escape(propertyName) + "[\"'][^<>]*>",
            RegexOption.IGNORE_CASE
        )

    private val propertyValueAttribute =
        Regex("value\\s*=\\s*[\"']([^\"']*)[\"']", RegexOption.IGNORE_CASE)

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

    private fun readProperty(objectElement: Element, name: String): String? {
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

    private fun hasMapLinks(mapInfo: Element, root: Element): Boolean {
        if (readProperty(mapInfo, "rwxMapLinks") != null || readProperty(mapInfo, "rwx_map_links") != null) {
            return true
        }
        val objects = root.getElementsByTagName("object")
        for (index in 0 until objects.length) {
            val element = objects.item(index) as? Element ?: continue
            if (element.getAttribute("type").equals("rwx_map_portal", ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}
