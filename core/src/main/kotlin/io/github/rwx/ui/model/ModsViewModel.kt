package io.github.rwx.ui.model

import io.github.rwx.mod.JvmModManifest
import io.github.rwx.mod.ModManifest
import io.github.rwx.ui.AppScreen


data class ModEntry(
    val info: ModManifest,
    val isEnabled: Boolean,
    val errorMessage: String? = null,
    val path: String = "",
) {
    val id: String get() = info.id
    val name: String get() = info.name
    val author: String? get() = (info as? JvmModManifest)?.author
    val description: String get() = info.description
    val version: String? get() = (info as? JvmModManifest)?.version
    val thumbnail: String? = (info as? JvmModManifest)?.thumbnail

}


data class ModsListModel(
    val mods: List<ModEntry>,
    val filter: String = "",
) {
    /** Mods matching the current filter (case-insensitive name match); all mods when filter blank. */
    val filtered: List<ModEntry>
        get() = filter.trim().let { needle ->
            if (needle.isEmpty()) mods
            else mods.filter { it.name.contains(needle, ignoreCase = true) }
        }

    /** Filtered mods that are currently enabled. */
    val enabled: List<ModEntry> get() = filtered.filter { it.isEnabled }

    /** Filtered mods that are currently disabled. */
    val disabled: List<ModEntry> get() = filtered.filter { !it.isEnabled }

    /** True when no mods are installed at all (drives the empty-state message). */
    val isEmpty: Boolean get() = mods.isEmpty()
}


sealed interface ModsAction {
    /** Discard navigation and return to the main menu. */
    data object Back : ModsAction

    /** Re-scan the mod source from disk. */
    data object Reload : ModsAction

    /** Open a file chooser to import a mod archive. */
    data object ImportFile : ModsAction

    /** Disable every mod. */
    data object DisableAll : ModsAction

    /** Persist enable/disable changes and leave the screen. */
    data object Apply : ModsAction

    /** Flip the enabled state of one mod. */
    data class ToggleEnable(val modId: String) : ModsAction

    /** Remove one mod from disk. */
    data class Delete(val modId: String) : ModsAction

    data class ShowDescription(val modId: String) : ModsAction
    data class ShowError(val modId: String) : ModsAction

    data class ConsumeNotice(val noticeRevision: Long) : ModsAction
}

sealed interface ModsOutcome {
    data class Navigate(val screen: AppScreen) : ModsOutcome
    data object ReloadRequested : ModsOutcome
    data object ImportRequested : ModsOutcome
    data object DisableAllRequested : ModsOutcome
    data object ApplyRequested : ModsOutcome
    data class ToggleEnable(val modId: String) : ModsOutcome
    data class Delete(val modId: String) : ModsOutcome
    data class ShowDescription(val modId: String) : ModsOutcome
    data class ShowError(val modId: String) : ModsOutcome
    data class ConsumeNotice(val noticeRevision: Long) : ModsOutcome
}


sealed interface ModsScrollRow {
    data class SectionTitle(val text: String) : ModsScrollRow
    data class Card(val mod: ModEntry) : ModsScrollRow
    data class Empty(val text: String) : ModsScrollRow
}

object ModsScrollRows {
    fun from(
        model: ModsListModel,
        enabledTitle: String,
        disabledTitle: String,
        emptyText: String,
    ): List<ModsScrollRow> = buildList {
        if (model.isEmpty) {
            add(ModsScrollRow.Empty(emptyText))
            return@buildList
        }
        add(ModsScrollRow.SectionTitle(enabledTitle))
        model.enabled.forEach { add(ModsScrollRow.Card(it)) }
        add(ModsScrollRow.SectionTitle(disabledTitle))
        model.disabled.forEach { add(ModsScrollRow.Card(it)) }
    }
}

object ModsNavigation {
    fun outcomeFor(action: ModsAction): ModsOutcome = when (action) {
        ModsAction.Back -> ModsOutcome.Navigate(AppScreen.MainMenu)
        ModsAction.Reload -> ModsOutcome.ReloadRequested
        ModsAction.ImportFile -> ModsOutcome.ImportRequested
        ModsAction.DisableAll -> ModsOutcome.DisableAllRequested
        ModsAction.Apply -> ModsOutcome.ApplyRequested
        is ModsAction.ToggleEnable -> ModsOutcome.ToggleEnable(action.modId)
        is ModsAction.Delete -> ModsOutcome.Delete(action.modId)
        is ModsAction.ShowDescription -> ModsOutcome.ShowDescription(action.modId)
        is ModsAction.ShowError -> ModsOutcome.ShowError(action.modId)
        is ModsAction.ConsumeNotice -> ModsOutcome.ConsumeNotice(action.noticeRevision)
    }
}
