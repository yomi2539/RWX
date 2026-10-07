package io.github.rwx.ui.host

import io.github.rwx.logger
import io.github.rwx.i18n.I18n
import io.github.rwx.ui.model.*
import kotlinx.coroutines.*

/**
 * Frontend-owned Level Select state. Compose renders this screen from [snapshot];
 * map loading runs off the UI thread with a revision guard so stale results are dropped.
 */
class LevelSelectSceneHost(
    private val model: SettingsModel = SettingsModel(),
    private val viewModelFactory: LevelSelectViewModelFactory,
    private val onAction: LevelSelectActionHandler = LevelSelectActionHandler {},
) {
    private val loadScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var closed = false
    private val maps = mutableListOf<MapEntry>()
    private var modeTitle: String = ""
    private var currentMode: LevelSelectMode = LevelSelectMode.Skirmish
    private var availableModes: List<LevelSelectMode> = LevelSelectMode.entries
    private var isLoading: Boolean = false
    private var loadError: String? = null
    private var mapLoadJob: Job? = null
    private var mapLoadRevision: Long = 0L

    fun updateMaps(mode: LevelSelectMode, availableModes: List<LevelSelectMode>? = null) {
        if (closed) return
        availableModes?.takeIf { it.isNotEmpty() }?.let { this.availableModes = it }
        val effectiveMode = mode.takeIf { it in this.availableModes } ?: this.availableModes.first()
        val revision = ++mapLoadRevision
        mapLoadJob?.cancel()
        currentMode = effectiveMode
        modeTitle = effectiveMode.label
        isLoading = true
        loadError = null
        synchronized(maps) { maps.clear() }

        mapLoadJob = loadScope.launch {
            try {
                val viewModel = viewModelFactory.create(effectiveMode)
                val entries = withContext(Dispatchers.IO) { viewModel.items() }
                if (closed || revision != mapLoadRevision) return@launch
                synchronized(maps) {
                    maps.clear()
                    maps.addAll(entries)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (!closed && revision == mapLoadRevision) {
                    logger.warn(error) { "Failed to load maps for ${effectiveMode.name}: ${error.message}" }
                    synchronized(maps) { maps.clear() }
                    loadError = I18n.levelselect.loadError()
                }
            } finally {
                if (!closed && revision == mapLoadRevision) {
                    isLoading = false
                }
            }
        }
    }

    fun snapshot(): LevelSelectUiState = LevelSelectUiState(
        revision = mapLoadRevision,
        currentMode = currentMode,
        availableModes = availableModes,
        maps = synchronized(maps) { maps.toList() },
        isLoading = isLoading,
        loadError = loadError,
    )

    fun dispatch(action: LevelSelectAction) { if (!closed) onAction.onAction(action) }

    /** Retires this frontend owner; neither queued loads nor later actions can reopen it. */
    fun close() {
        if (closed) return
        closed = true
        mapLoadRevision++
        mapLoadJob?.cancel()
        mapLoadJob = null
        loadScope.cancel()
        synchronized(maps) { maps.clear() }
        isLoading = false
        loadError = null
    }

}
