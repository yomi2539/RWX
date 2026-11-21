package io.github.rwx.mod.registry

import androidx.compose.runtime.Composable
import com.corrodinggames.rts.gameFramework.GameEngine
import io.github.rwx.i18n.I18n
import io.github.rwx.mod.api.LocalizedText
import io.github.rwx.mod.api.ModWindowContext
import io.github.rwx.mod.api.WorldPosition
import io.github.rwx.mod.api.WorldPositionSelection
import io.github.rwx.mod.impl.ApiImpl
import io.github.rwx.ui.CoreUiEventQueue
import io.github.rwx.ui.model.ModHudLayerUiState
import io.github.rwx.ui.model.ModHudUiState
import io.github.rwx.ui.model.ModWindowUiState

object UiRegistry : OwnedRegistry {
    private const val MENU_ID_BASE = 26000

    /**
     * All state here shares one monitor: the object itself, which is what [Synchronized]
     * on these methods and `synchronized(ModUiRegistry)` in [SelectionHandle] already take.
     * The tables are handed the same monitor so a teardown that spans several of them
     * cannot interleave with a lookup.
     */
    private val lock = this
    private val items = RegistrationTable<Int, RegisteredMenuItem>("Mod menu item", lock)
    private val hudLayers = RegistrationTable<String, ModHudLayerUiState>("Mod HUD", lock)
    private val windows = RegistrationTable<String, RegisteredWindow>("Mod window", lock)
    private val nativeHudHiddenOwners = linkedSetOf<ApiImpl>()
    private var activeWindowId: String? = null
    private var worldPositionSelection: RegisteredWorldPositionSelection? = null

    /**
     * Published snapshots. Mods register from any thread; the frontend reads these once per frame
     * and only republishes when the value changes, so they are rebuilt on change rather than per read.
     */
    @Volatile
    private var hudSnapshot = ModHudUiState()

    @Volatile
    private var windowRevision = 0L

    @JvmStatic
    fun addInGameMenuItem(owner: ApiImpl, menuId: Int?, label: LocalizedText, callback: (Int) -> Unit): Int =
        synchronized(lock) {
            val resolvedId = menuId ?: generateSequence(MENU_ID_BASE) { it + 1 }.first { it !in items }
            items.put(owner, resolvedId, RegisteredMenuItem(resolvedId, label, callback))
            resolvedId
        }

    @JvmStatic
    fun removeInGameMenuItem(menuId: Int) {
        items.remove(menuId)
    }

    @Synchronized
    fun registerHud(owner: ApiImpl, id: String, order: Int, content: @Composable () -> Unit) {
        hudLayers.register(owner, id, ModHudLayerUiState(id, order, content))
        publishHud()
    }

    @Synchronized
    fun unregisterHud(owner: ApiImpl, id: String) {
        val registration = hudLayers.owned(id) ?: return
        check(registration.owner === owner) { "Mod HUD is owned by another mod: ${id}" }
        hudLayers.remove(id)
        publishHud()
    }

    @Synchronized
    fun setNativeHudVisible(owner: ApiImpl, visible: Boolean) {
        if (visible) {
            nativeHudHiddenOwners.remove(owner)
        } else {
            nativeHudHiddenOwners.add(owner)
        }
    }

    @JvmStatic
    @Synchronized
    fun isNativeHudVisible(): Boolean = nativeHudHiddenOwners.isEmpty()

    /** Layers in composition order; the same value is returned until a registration changes. */
    fun hudSnapshot(): ModHudUiState = hudSnapshot

    @Synchronized
    fun hasActiveHudLayers(): Boolean = !hudLayers.isEmpty()

    @JvmStatic
    @Synchronized
    fun clear() {
        items.clear()
        hudLayers.clear()
        publishHud()
        nativeHudHiddenOwners.clear()
        windows.clear()
        activeWindowId = null
        windowRevision++
        worldPositionSelection = null
    }

    @JvmStatic
    fun getInGameMenuItems(): List<InGameMenuItem> = items.snapshot().values.map { item ->
        InGameMenuItem(
            item.menuId,
            item.label.resolve(I18n.currentLocale.toLanguageTag()),
        )
    }

    @JvmStatic
    fun handleInGameMenuSelection(menuId: Int): Boolean {
        val callback = items[menuId]?.callback ?: return false
        callback(menuId)
        return true
    }

    @Synchronized
    fun registerWindow(
        owner: ApiImpl,
        id: String,
        title: LocalizedText,
        content: @Composable (context: ModWindowContext) -> Unit,
    ) {
        windows.register(owner, id, RegisteredWindow(owner, title, content, WindowContext(owner)))
    }

    @Synchronized
    fun openWindow(id: String) {
        check(id in windows) { "Mod window is not registered: ${id}" }
        activeWindowId = id
        windowRevision++
        GameEngine.getInstance()?.gameUI?.isDraggingSelection = false
        CoreUiEventQueue.requestInGameModWindow()
    }

    @Synchronized
    fun closeWindow() {
        deactivateWindow()
        CoreUiEventQueue.requestInGameModWindowBack()
    }

    @Synchronized
    fun deactivateWindow() {
        if (activeWindowId == null) return
        activeWindowId = null
        windowRevision++
    }

    /** A new revision makes the renderer rebuild the active window's content from scratch. */
    @Synchronized
    fun refreshWindow() {
        windowRevision++
    }

    @Synchronized
    fun windowSnapshot(): ModWindowUiState {
        val registration = activeWindowId?.let { windows[it] } ?: return ModWindowUiState(windowRevision)
        return ModWindowUiState(
            revision = windowRevision,
            title = registration.title.resolve(I18n.currentLocale.toLanguageTag()),
            content = registration.content,
            context = registration.context,
        )
    }

    @Synchronized
    fun requestWorldPosition(
        owner: ApiImpl,
        onSelected: (WorldPosition) -> Unit,
        onCancelled: () -> Unit,
    ): WorldPositionSelection {
        check(worldPositionSelection == null) { "A world-position selection is already active" }
        val registration = RegisteredWorldPositionSelection(owner, onSelected, onCancelled)
        worldPositionSelection = registration
        return SelectionHandle(registration)
    }

    @JvmStatic
    @Synchronized
    fun hasActiveWorldPositionSelection(): Boolean = worldPositionSelection != null

    @JvmStatic
    fun selectWorldPosition(position: WorldPosition): Boolean {
        val registration = synchronized(this) {
            worldPositionSelection?.also { worldPositionSelection = null }
        } ?: return false
        registration.onSelected(position)
        return true
    }

    @JvmStatic
    fun cancelWorldPositionSelection(): Boolean {
        val registration = synchronized(this) {
            worldPositionSelection?.also { worldPositionSelection = null }
        } ?: return false
        registration.onCancelled()
        return true
    }

    @Synchronized
    override fun unregister(owner: ApiImpl) {
        items.removeOwned(owner)
        val removedWindows = windows.removeOwned(owner)
        if (activeWindowId in removedWindows.keys) {
            activeWindowId = null
            windowRevision++
        }
        if (hudLayers.removeOwned(owner).isNotEmpty()) publishHud()
        nativeHudHiddenOwners.remove(owner)
        if (worldPositionSelection?.owner === owner) worldPositionSelection = null
    }

    private fun publishHud() {
        hudSnapshot = ModHudUiState.of(hudSnapshot.revision + 1, hudLayers.snapshot().values)
    }

    private data class RegisteredMenuItem(
        val menuId: Int,
        val label: LocalizedText,
        val callback: (Int) -> Unit,
    )

    private class RegisteredWindow(
        val owner: ApiImpl,
        val title: LocalizedText,
        val content: @Composable (context: ModWindowContext) -> Unit,
        /** Created once so per-frame snapshots of the same window compare equal. */
        val context: ModWindowContext,
    )

    private data class RegisteredWorldPositionSelection(
        val owner: ApiImpl,
        val onSelected: (WorldPosition) -> Unit,
        val onCancelled: () -> Unit,
    )

    private class SelectionHandle(
        private val registration: RegisteredWorldPositionSelection,
    ) : WorldPositionSelection {
        override val active: Boolean
            get() = synchronized(UiRegistry) {
                worldPositionSelection === registration
            }

        override fun cancel() {
            val callback = synchronized(UiRegistry) {
                if (worldPositionSelection !== registration) return
                worldPositionSelection = null
                registration.onCancelled
            }
            callback()
        }
    }

    private class WindowContext(private val owner: ApiImpl) : ModWindowContext {
        override val api = owner
        override val locale: String get() = I18n.currentLocale.toLanguageTag()
        override fun refresh() = refreshWindow()
        override fun close() = closeWindow()
    }

    data class InGameMenuItem(
        val menuId: Int,
        val label: String
    )
}
