package io.github.rwx.ui.model

data class ModUiEntry(
    val id: String,
    val name: String,
    val isEnabled: Boolean,
    val description: String = "",
    val author: String? = null,
    val version: String? = null,
    val errorMessage: String? = null,
    val path: String = "",
    val thumbnail: String? = null,
)

data class ModsUiState(
    val revision: Long = 0,
    val mods: List<ModUiEntry> = emptyList(),
    val noticeText: String = "",
    val noticeRevision: Long = 0,
) {
    internal fun resolveAction(requestRevision: Long, action: ModsAction): ModsAction? {
        if (action == ModsAction.Back) return action
        if (action is ModsAction.ConsumeNotice) return action
        if (requestRevision != revision) return null
        val modId = when (action) {
            is ModsAction.ToggleEnable -> action.modId
            is ModsAction.Delete -> action.modId
            is ModsAction.ShowDescription -> action.modId
            is ModsAction.ShowError -> action.modId
            else -> return action
        }
        val mod = mods.singleOrNull { it.id == modId && modId.isNotBlank() } ?: return null
        return action.takeUnless {
            (it is ModsAction.ShowDescription && mod.description.isBlank()) ||
                (it is ModsAction.ShowError && mod.errorMessage.isNullOrBlank())
        }
    }
}

internal fun ModEntry.snapshot() = ModUiEntry(id, name, isEnabled, description, author, version, errorMessage, path, thumbnail)
