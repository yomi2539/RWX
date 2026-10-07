package io.github.rwx.ui.host

import io.github.rwx.ui.model.ModEntry
import io.github.rwx.ui.model.ModsAction
import io.github.rwx.ui.model.ModsUiState
import io.github.rwx.ui.model.snapshot


class ModsSceneHost(
    private val onAction: (ModsAction) -> Unit = {},
) {
    private var uiState = ModsUiState()
    fun updateMods(mods: List<ModEntry>, statusText: String = "") {
        val revision = uiState.revision + 1
        uiState = if (statusText.isNotBlank()) {
            ModsUiState(revision, mods.map { it.snapshot() }, statusText, uiState.noticeRevision + 1)
        } else {
            ModsUiState(revision, mods.map { it.snapshot() }, uiState.noticeText, uiState.noticeRevision)
        }
    }

    fun dispatch(action: ModsAction) = onAction(action)

    fun consumeNotice(noticeRevision: Long) {
        if (uiState.noticeRevision == noticeRevision) {
            uiState = uiState.copy(noticeText = "")
        }
    }

    fun snapshot(): ModsUiState = uiState
}
