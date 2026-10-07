package io.github.rwx.ui.model

import io.github.rwx.i18n.I18n
import io.github.rwx.ui.component.Icon


data class DialogButton(
    val label: String,
    val onPress: (() -> Unit)? = null,
    val onInputPress: ((String) -> Unit)? = null,
    val onFormPress: ((Map<String, String>) -> Unit)? = null,
)


data class Dialog(
    val title: String,
    val message: String,
    val messageLabel: String? = null,
    val buttons: List<DialogButton> = listOf(DialogButton(I18n.common.ok())),
    val infoRows: List<DialogInfoRow> = emptyList(),
    val listItems: List<DialogListItem> = emptyList(),
    val textInput: DialogTextInput? = null,
    val form: DialogForm? = null,
    val scrollableMessage: Boolean = false,
    val scrollableForm: Boolean = false,
    val compactOnAndroid: Boolean = true,
    /** Optional cancellation callback to run on Escape (for example a pending password request). */
    val dismissButtonIndex: Int? = null,
)

data class DialogListItem(
    val text: String,
    val colorIndex: Int = -1,
)

data class DialogInfoRow(
    val icon: Icon,
    val label: String,
    val value: String,
    val emphasis: Boolean = false,
    val onPress: (() -> Unit)? = null,
)

data class DialogTextInput(
    val initialText: String = "",
    val hint: String = "",
    val password: Boolean = false,
    val trailingIcon: Icon? = null,
    val trailingIconTooltip: String? = null,
    val onTrailingIconPress: ((setValue: (String) -> Unit) -> Unit)? = null,
    /** Completes once with a choice, or null on cancellation. Results may arrive on any thread. */
    val onChooseInput: ((complete: (DialogInputChoice?) -> Unit) -> Unit)? = null,
)

data class DialogForm(
    val fields: List<DialogFormField>,
)

sealed interface DialogFormField {
    val id: String
    val label: String

    data class Choice(
        override val id: String,
        override val label: String,
        val options: List<DialogFormOption>,
        val selectedIndex: Int = 0,
    ) : DialogFormField

    data class Toggle(
        override val id: String,
        override val label: String,
        val checked: Boolean,
    ) : DialogFormField

    data class Text(
        override val id: String,
        override val label: String,
        val initialText: String,
        val hint: String = "",
    ) : DialogFormField
}

data class DialogFormOption(
    val label: String,
    val value: String,
) {
    override fun toString(): String = label
}

/** The store owns a selected resource until submission, replacement, editing away, or dismissal. */
class DialogInputChoice(
    val value: String,
    val submittedValue: String = value,
    private val release: () -> Unit = {},
) {
    private val disposed = java.util.concurrent.atomic.AtomicBoolean(false)
    internal fun dispose() {
        if (disposed.compareAndSet(false, true)) {
            runCatching(release).onFailure { error ->
                io.github.rwx.logger.warn(error) { "Unable to release a dialog input selection" }
            }
        }
    }
}
