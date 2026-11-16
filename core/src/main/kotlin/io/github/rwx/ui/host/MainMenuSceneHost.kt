package io.github.rwx.ui.host

import io.github.rwx.ui.model.*

class MainMenuSceneHost(
    private val model: SettingsModel = SettingsModel(),
    private val onAction: (MainMenuAction) -> Unit = {},
) {
    var items: List<MainMenuItem> = emptyList()
        private set

    fun updateItems(conditions: MainMenuConditions) {
        val next = MainMenuViewModel.items(conditions)
        if (next != items) {
            items = next
        }
    }

    /** Snapshot for the Compose overlay; the returned list is immutable. */
    fun snapshotItems(): List<MainMenuItem> = items

    /** Dispatches a menu action to the handler. Kept separate so click routing is testable. */
    fun dispatch(action: MainMenuAction) {
        onAction(action)
    }

}
