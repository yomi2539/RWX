package io.github.rwx.ui.model

import com.corrodinggames.rts.gameFramework.GameEngine
import com.corrodinggames.rts.gameFramework.GameSaver
import com.corrodinggames.rts.gameFramework.file.FileHelper
import io.github.rwx.PlatformStorage
import io.github.rwx.i18n.I18n
import io.github.rwx.i18n.I18nText
import io.github.rwx.p2p.FeatureIds
import io.github.rwx.p2p.MapFeatureDetector
import io.github.rwx.ui.AppScreen
import java.io.File


enum class LevelSelectMode(
    private val text: I18nText,
    val assetSubdir: String?,
) {
    Campaign(I18n.singleplayer.campaign, "normal"),
    Skirmish(I18n.singleplayer.skirmish, "skirmish"),
    Challenge(I18n.singleplayer.challenge, "challenge"),
    Survival(I18n.singleplayer.survival, "survival"),
    CustomMaps(I18n.singleplayer.customMaps, null),
    SavedGames(I18n.singleplayer.loadSave, null),
    ;

    val label: String
        get() = text()

    override fun toString(): String = label
}

data class MapEntry(
    val mapAssetPath: String,
    val playerCount: Int? = null,
    val requiredRwxFeatures: List<String> = emptyList(),
    val type: LevelSelectMode = LevelSelectMode.Skirmish,
    val previewOverride: String? = null,
    val hasSiblingIndex: Boolean = false,
) {
    val previewAssetPath: String? =
        if (type == LevelSelectMode.SavedGames) {
            null
        } else if (hasSiblingIndex) {
            previewOverride
        } else {
            previewOverride ?: cachedPreview(mapAssetPath)
        }
    val fileName: String
        get() = mapAssetPath.trimEnd('/', '\\').substringAfterLast('/')
    val displayName: String
        get() = displayName(fileName)
    val isSavedGame: Boolean get() = type == LevelSelectMode.SavedGames

    companion object {
        private val previewCacheLock = Any()
        private val previewCache = HashMap<String, String?>()

        internal fun cachedPreview(mapPath: String): String? = synchronized(previewCacheLock) {
            if (previewCache.containsKey(mapPath)) previewCache[mapPath] else null
        }

        internal fun cachePreview(mapPath: String, preview: String?) = synchronized(previewCacheLock) {
            previewCache[mapPath] = preview
        }

        private val leadingPlayerTagRegex = Regex("""^\[(?:z;)?[po]\d+]\s*""", RegexOption.IGNORE_CASE)
        private val sortPrefixRegex = Regex("""^[a-z]\d+;""", RegexOption.IGNORE_CASE)
        private val whitespaceRegex = Regex("""\s+""")

        /** Strip the sort prefix (e.g. "l030;") and replace underscores with spaces. */
        fun displayName(fileName: String): String {
            val withoutExt = fileName.removeSuffix(".tmx")
            val afterPrefix = withoutExt.replace(sortPrefixRegex, "")
            return afterPrefix
                .replace(leadingPlayerTagRegex, "")
                .replace('_', ' ')
                .replace(whitespaceRegex, " ")
                .trim()
        }
    }
}

data class LevelSelectFilterOption(
    val label: String,
    val playerCount: Int? = null,
    val rwxOnly: Boolean = false,
) {
    override fun toString(): String = label

    companion object {
        val All: LevelSelectFilterOption
            get() = LevelSelectFilterOption(I18n.levelselect.filter.all())
    }
}

enum class LevelSelectSortOption(private val text: I18nText) {
    Default(I18n.levelselect.sort.default),
    Name(I18n.levelselect.sort.name),
    Players(I18n.levelselect.sort.players),
    ;

    override fun toString(): String = text()
}

object LevelSelectMapBrowser {
    fun filterOptions(maps: List<MapEntry>): List<LevelSelectFilterOption> {
        val playerCounts = maps.mapNotNull { it.playerCount }.distinct().sorted()
        return buildList {
            addAll(listOf(LevelSelectFilterOption.All))
            playerCounts.mapTo(this) { count -> LevelSelectFilterOption("$count ${I18n.levelselect.players()}", count) }
            addAll(listOf(LevelSelectFilterOption(I18n.levelselect.filter.rwxModes(), rwxOnly = true)))
        }
    }

    fun visibleMaps(
        maps: List<MapEntry>,
        query: String,
        filter: LevelSelectFilterOption,
        sort: LevelSelectSortOption,
    ): List<MapEntry> {
        val trimmedQuery = query.trim()
        val textFiltered = if (trimmedQuery.isBlank()) {
            maps
        } else {
            maps.filter {
                it.displayName.contains(trimmedQuery, ignoreCase = true) ||
                        it.fileName.contains(trimmedQuery, ignoreCase = true)
            }
        }
        val filtered = when {
            filter.rwxOnly -> textFiltered.filter { it.requiredRwxFeatures.isNotEmpty() }
            filter.playerCount != null -> textFiltered.filter { it.playerCount == filter.playerCount }
            else -> textFiltered
        }

        return when (sort) {
            LevelSelectSortOption.Default -> filtered
            LevelSelectSortOption.Name -> filtered.sortedBy { it.displayName.lowercase() }
            LevelSelectSortOption.Players -> filtered.sortedWith(
                compareBy<MapEntry> { it.playerCount ?: Int.MAX_VALUE }
                    .thenBy { it.displayName.lowercase() }
            )
        }
    }
}

/**
 * Scans the bundled map assets for the given [mode] and returns entries sorted by file name.
 * Platform-specific asset enumeration and Kool loader paths stay behind [PlatformStorage].
 */
class LevelSelectViewModel(
    val mode: LevelSelectMode,
    private val storage: PlatformStorage,
    private val savedGamesDirectory: () -> File = {
        GameSaver.getSaveFile("", "saves/", false)
    },
    private val customMapPathsProvider: (() -> List<String>)? = null,

    ) {
    @Volatile
    private var builtInItems: List<MapEntry>? = null

    fun items(): List<MapEntry> {
        if (mode == LevelSelectMode.SavedGames) {
            return savedGameItems()
        }
        if (mode == LevelSelectMode.CustomMaps) {
            return customMapItems()
        }
        builtInItems?.let { return it }
        return synchronized(this) {
            builtInItems ?: builtInMapItems().also { builtInItems = it }
        }
    }

    fun invalidateCache() {
        synchronized(this) {
            builtInItems = null
        }
    }

    private fun builtInMapItems(): List<MapEntry> {
        val prefix = "maps/${requireNotNull(mode.assetSubdir)}"
        val all = storage.listAssets(prefix)
            .map { it.replace('\\', '/').trim('/') }
        val lowerIndex = HashMap<String, String>(all.size)
        for (path in all) {
            lowerIndex.getOrPut(path.lowercase()) { path }
        }
        return all.asSequence()
            .filter { it.endsWith(".tmx") }
            .sortedBy { it.substringAfterLast('/') }
            .map { mapPath ->
                val preview = findBuiltInPreview(mapPath, lowerIndex)
                MapEntry.cachePreview(mapPath, preview)
                val fileName = mapPath.substringAfterLast('/')
                MapEntry(
                    mapAssetPath = mapPath,
                    playerCount = playerCount(fileName),
                    requiredRwxFeatures = MapFeatureDetector.requiredFeaturesForMap(mapPath),
                    previewOverride = preview,
                    hasSiblingIndex = true,
                )
            }
            .toList()
    }

    private fun findBuiltInPreview(tmxPath: String, lowerIndex: Map<String, String>): String? {
        val base = tmxPath.removeSuffix(".tmx")
        for (suffix in SIBLING_PREVIEW_SUFFIXES) {
            lowerIndex[(base + "_map" + suffix).lowercase()]?.let { return it }
        }
        return null
    }

    private fun customMapItems(): List<MapEntry> {
        val providerPaths = customMapPathsProvider?.invoke()
        if (providerPaths != null) {
            val distinctSorted = providerPaths.distinct().sortedBy { it.lowercase() }
            val dirSiblings = HashMap<String, Set<String>>()
            for (path in distinctSorted) {
                val dir = path.substringBeforeLast('/', "")
                dirSiblings.getOrPut(dir) {
                    if (dir.isEmpty()) emptySet()
                    else runCatching { FileHelper.listFiles(dir)?.toSet() }.getOrNull() ?: emptySet()
                }
            }
            val fileMaps = distinctSorted
                .map { path -> engineCustomMapEntry(path, dirSiblings[path.substringBeforeLast('/', "")]) }
            return fileMaps + modMapItems()
        }
        val fileMaps = engineCustomMapPaths(ENGINE_CUSTOM_MAP_ROOT, depth = 0)
            .distinctBy { it.first }
            .sortedBy { it.first.lowercase() }
            .map { (mapPath, siblings) -> engineCustomMapEntry(mapPath, siblings) }
        return fileMaps + modMapItems()
    }

    private fun engineCustomMapPaths(folder: String, depth: Int): List<Pair<String, Set<String>>> {
        if (depth > MAX_CUSTOM_MAP_SCAN_DEPTH) return emptyList()
        val entries = runCatching { FileHelper.listFiles(folder) }.getOrNull() ?: return emptyList()
        val siblingSet: Set<String> = entries.toSet()
        val result = mutableListOf<Pair<String, Set<String>>>()
        for (entry in entries) {
            if (entry.startsWith('.')) continue
            val childPath = folder.trimEnd('/') + "/" + entry
            if (entry.endsWith(".tmx", ignoreCase = true)) {
                result += childPath to siblingSet
            } else if (runCatching { FileHelper.isDirectory(childPath) }.getOrDefault(false)) {
                result += engineCustomMapPaths(childPath, depth + 1)
            }
        }
        return result
    }

    private fun engineCustomMapEntry(mapPath: String, siblingNames: Set<String>? = null): MapEntry {
        val fileName = mapPath.substringAfterLast('/')
        val siblingPreview = if (siblingNames != null) {
            resolveSiblingPreview(mapPath, siblingNames)
        } else {
            null
        }
        if (siblingNames != null) {
            MapEntry.cachePreview(mapPath, siblingPreview)
        }
        return MapEntry(
            mapAssetPath = mapPath,
            playerCount = playerCount(fileName),
            requiredRwxFeatures = MapFeatureDetector.requiredFeaturesForMap(mapPath),
            previewOverride = siblingPreview,
            hasSiblingIndex = siblingNames != null,
        )
    }

    private fun resolveSiblingPreview(mapPath: String, siblingNames: Set<String>): String? {
        val cleanMapPath = mapPath.stripMergedTag()
        val fileName = cleanMapPath.substringAfterLast('/')
        if (fileName.length < 4) return null
        val fileBase = fileName.substring(0, fileName.length - 4)
        val dirPrefix = cleanMapPath.substringBeforeLast('/', "")
        val prefix = if (dirPrefix.isEmpty()) "" else dirPrefix + "/"
        val cleanSiblings = siblingNames.map { it.stripMergedTag().substringAfterLast('/') }
        for (suffix in SIBLING_PREVIEW_SUFFIXES) {
            val candidate = fileBase + "_map" + suffix
            val realName = cleanSiblings.firstOrNull { it.equals(candidate, ignoreCase = true) }
            if (realName != null) return prefix + realName
        }
        return null
    }

    private fun String.stripMergedTag(): String {
        var result = this
        for (tag in MERGED_PATH_TAGS) {
            result = result.replace(tag, "")
        }
        return result
    }

    private fun savedGameItems(): List<MapEntry> {
        val saveRoot = savedGamesDirectory()
        val rootPath = saveRoot.absolutePath
        return FileHelper.listFiles(rootPath)
            ?.asSequence()
            ?.filter { it.endsWith(".rwsave", ignoreCase = true) }
            ?.map { name ->
                val path = rootPath.trimEnd('/', '\\') + "/" + name
                MapEntry(
                    mapAssetPath = path,
                    playerCount = null,
                    type = LevelSelectMode.SavedGames
                ) to FileHelper.getLastModified(path)
            }
            ?.sortedByDescending { (_, modifiedAt) -> modifiedAt }
            ?.map { (entry, _) -> entry }
            ?.toList()
            ?: emptyList()
    }

    private fun modMapItems(): List<MapEntry> {
        val manager = GameEngine.getInstance()?.modManager ?: return emptyList()
        val entries = manager.addExtraMapsForPath(null, CUSTOM_LEVELS_DIR) ?: return emptyList()
        return entries
            .filter { it.startsWith(MOD_PATH_PREFIX) }
            .map {
                val mapPath = "$CUSTOM_LEVELS_DIR/$it"
                val fileName = mapPath.substringAfterLast('/')
                MapEntry(
                    mapAssetPath = mapPath,
                    playerCount = playerCount(fileName),
                    requiredRwxFeatures = MapFeatureDetector.requiredFeaturesForMap(mapPath),
                    previewOverride = MapEntry.cachedPreview(mapPath),
                    hasSiblingIndex = true,
                )
            }
    }

    fun mapEntry(mapAssetPath: String): MapEntry {
        val fileName = mapAssetPath.substringAfterLast('/')
        return MapEntry(
            mapAssetPath = mapAssetPath,
            playerCount = playerCount(fileName),
            requiredRwxFeatures = MapFeatureDetector.requiredFeaturesForMap(mapAssetPath),
            previewOverride = MapEntry.cachedPreview(mapAssetPath),
            hasSiblingIndex = true,
        )
    }

    companion object {
        private const val MAX_CUSTOM_MAP_SCAN_DEPTH: Int = 8
        private const val ENGINE_CUSTOM_MAP_ROOT: String = "/SD/rustedWarfare/maps"

        /** `LevelGroupSelectActivity.customLevelsDir` — the path mod maps get registered against. */
        private const val CUSTOM_LEVELS_DIR: String = "/SD/rusted_warfare_maps"
        private const val MOD_PATH_PREFIX: String = "MOD|"

        private val SIBLING_PREVIEW_SUFFIXES: List<String> = listOf(".png", ".jpg", ".jpeg")

        private val MERGED_PATH_TAGS: List<String> = listOf(
            "[INTERNAL-PATH]/",
            "[EXTERNAL-PATH]/",
            "[NULL-PATH]/",
            "[INTERNAL-PATH]",
            "[EXTERNAL-PATH]",
            "[NULL-PATH]",
        )

        private val playerCountRegex = Regex("""p(\d+)|(\d+)p""", RegexOption.IGNORE_CASE)

        fun playerCount(fileName: String): Int? {
            val match = playerCountRegex.find(fileName) ?: return null
            return (match.groupValues.getOrNull(1)?.takeIf { it.isNotEmpty() }
                ?: match.groupValues.getOrNull(2))?.toIntOrNull()
        }
    }
}

fun MapEntry.playerLabel(): String =
    if (isSavedGame) {
        I18n.singleplayer.loadSave()
    } else {
        CompatibilityLabel() ?: playerCount?.let { "${it}p" } ?: I18n.levelselect.scenario()
    }


fun MapEntry.ModeLabel(): String? = requiredRwxFeatures.ModeLabel()

fun MapEntry.CompatibilityLabel(): String? =
    ModeLabel()?.let { "${I18n.levelselect.compatibility.singlePlayer()} / ${I18n.levelselect.compatibility.p2p()}" }

fun List<String>.ModeLabel(): String? =
    when {
        contains(FeatureIds.AREA_CONTROL) -> I18n.levelselect.mode.areaControl()
        contains(FeatureIds.MAP_LINKS) -> I18n.levelselect.mode.mapLinks()
        else -> firstOrNull { it.startsWith("mode:") }
            ?.substringAfter("mode:")
            ?.takeIf { it.isNotBlank() }
            ?.toDisplayModeLabel()
    }

private fun String.toDisplayModeLabel(): String =
    trim()
        .replace('-', ' ')
        .replace('_', ' ')
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .joinToString(" ") { word -> word.replaceFirstChar { it.titlecase() } }

interface LevelSelectViewModelFactory {
    fun create(mode: LevelSelectMode): LevelSelectViewModel

    /** Drops any cached built-in map lists so the next [create] re-scans (e.g. after a mod reload). */
    fun invalidateCaches() {}
}

fun interface LevelSelectActionHandler {
    fun onAction(action: LevelSelectAction)
}

sealed interface LevelSelectAction {
    data class SelectMap(val map: MapEntry) : LevelSelectAction
    data class SelectMode(val mode: LevelSelectMode) : LevelSelectAction
    data object Back : LevelSelectAction
}

sealed interface LevelSelectOutcome {
    data class Navigate(val screen: AppScreen) : LevelSelectOutcome
    data class StartGame(val map: MapEntry) : LevelSelectOutcome
}

object LevelSelectNavigation {
    fun outcomeFor(action: LevelSelectAction): LevelSelectOutcome = when (action) {
        is LevelSelectAction.SelectMap -> LevelSelectOutcome.StartGame(action.map)
        is LevelSelectAction.SelectMode -> LevelSelectOutcome.Navigate(AppScreen.LevelSelect)
        LevelSelectAction.Back -> LevelSelectOutcome.Navigate(AppScreen.MainMenu)
    }
}
