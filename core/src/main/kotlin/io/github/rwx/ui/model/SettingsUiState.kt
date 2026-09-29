package io.github.rwx.ui.model

import io.github.rwx.ui.ColorSchemeId

/** A read-only copy of a settings page. Mutable settings state never crosses into the Compose thread. */
data class SettingsPageState(
    val page: SettingsPage,
    val items: List<SettingsItemState>,
    val keyBindings: KeyBindingsState? = null,
)

sealed interface SettingsItemState {
    data class Toggle(val key: String, val label: String, val checked: Boolean) : SettingsItemState
    data class Slider(
        val key: String,
        val label: String,
        val value: Float,
        val min: Float,
        val max: Float,
        val step: Float,
        val formattedValue: String,
    ) : SettingsItemState
    data class ColorScheme(val id: ColorSchemeId, val label: String, val selected: Boolean) : SettingsItemState
    data class StorageLocation(val selectedType: Int) : SettingsItemState
    data class RenderBackend(val selectedId: String) : SettingsItemState
}

/** Edits carry a page and stable key so queued input cannot change a newly selected page. */
sealed interface SettingsUiAction {
    data class SelectPage(val page: SettingsPage) : SettingsUiAction
    data class Toggle(val page: SettingsPage, val key: String, val checked: Boolean) : SettingsUiAction
    data class PreviewSlider(val page: SettingsPage, val key: String, val value: Float) : SettingsUiAction
    data class CommitSlider(val page: SettingsPage, val key: String) : SettingsUiAction
    data class SelectColorScheme(val id: ColorSchemeId) : SettingsUiAction
    data class BeginKeyCapture(val capture: KeyBindingCapture) : SettingsUiAction
    data class CaptureKey(val requestId: Long, val keyCode: Int, val modifiers: Int) : SettingsUiAction
    data class CancelKeyCapture(val requestId: Long) : SettingsUiAction
    data class ClearKeyBinding(val target: KeyBindingTarget) : SettingsUiAction
    data class SelectStorage(val storageType: Int) : SettingsUiAction
    data class SelectRenderBackend(val backendId: String) : SettingsUiAction
    data object RequestExternalStorage : SettingsUiAction
    data object Back : SettingsUiAction
}

fun SettingsPageContent.snapshot(): SettingsPageState = SettingsPageState(
    page = page,
    items = items.map { item ->
        when (item) {
            is SettingsPageItem.Toggle -> with(item.toggle) {
                SettingsItemState.Toggle(i18nText.key, i18nText(), state.value)
            }
            is SettingsPageItem.Slider -> with(item.slider) {
                SettingsItemState.Slider(i18nText.key, i18nText(), state.value, min, max, step, formatted(state.value))
            }
            is SettingsPageItem.ColorSchemeSelector -> with(item.item) {
                SettingsItemState.ColorScheme(id, label, item.selected)
            }
            is SettingsPageItem.StorageLocation -> SettingsItemState.StorageLocation(item.selectedType)
            is SettingsPageItem.RenderBackend -> SettingsItemState.RenderBackend(item.selectedId)
        }
    },
)

internal fun SettingsPageContent.applyEdit(action: SettingsUiAction): SettingsAction? = when (action) {
    is SettingsUiAction.Toggle -> if (action.page == page) {
        items.filterIsInstance<SettingsPageItem.Toggle>()
            .firstOrNull { it.toggle.i18nText.key == action.key }?.let {
                it.toggle.state.value = action.checked
                SettingsAction.ApplyChanges
            }
    } else null
    is SettingsUiAction.PreviewSlider -> if (action.page == page && action.value.isFinite()) {
        items.filterIsInstance<SettingsPageItem.Slider>()
            .firstOrNull { it.slider.i18nText.key == action.key }?.let {
                it.slider.state.value = it.slider.normalize(action.value)
                SettingsAction.PreviewChanges
            }
    } else null
    is SettingsUiAction.CommitSlider -> if (action.page == page) {
        items.filterIsInstance<SettingsPageItem.Slider>()
            .firstOrNull { it.slider.i18nText.key == action.key }?.let {
                SettingsAction.ApplyChanges
            }
    } else null
    else -> null
}
