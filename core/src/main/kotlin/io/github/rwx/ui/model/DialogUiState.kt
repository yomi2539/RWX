package io.github.rwx.ui.model

import io.github.rwx.ui.component.Icon

/** Callback-free modal state. Business callbacks and the editable draft stay on the frontend. */
data class DialogUiState(
    val revision: Long,
    val title: String,
    val message: String,
    val messageLabel: String? = null,
    val buttonLabels: List<String> = emptyList(),
    val textInput: DialogInputState? = null,
    val fields: List<DialogFormField> = emptyList(),
    val formValues: Map<String, String> = emptyMap(),
    val listItems: List<DialogListItem> = emptyList(),
    val infoRows: List<DialogInfoRowState> = emptyList(),
    val interactionRevision: Long = 0,
    val isChoosingInput: Boolean = false,
    val dismissButtonIndex: Int? = null,
)

data class DialogInputState(
    val value: String,
    val hint: String,
    val trailingIcon: Icon? = null,
    val trailingIconTooltip: String? = null,
    val canChooseInput: Boolean = false,
    val valueRevision: Long = 0,
    val password: Boolean = false,
)

data class DialogInfoRowState(val icon: Icon, val label: String, val value: String, val emphasis: Boolean, val isClickable: Boolean)

sealed interface DialogUiAction {
    data class EditInput(val value: String, val valueRevision: Long? = null) : DialogUiAction
    data class EditField(val id: String, val value: String) : DialogUiAction
    data class PressButton(
        val index: Int,
        val inputText: String = "",
        val formValues: Map<String, String> = emptyMap(),
        val inputValueRevision: Long? = null,
    ) : DialogUiAction
    data class PressInfoRow(val index: Int, val interactionRevision: Long) : DialogUiAction
    data class ChooseInput(val interactionRevision: Long) : DialogUiAction
    data object Dismiss : DialogUiAction
}

/** Also used by Kool, so renderer handoffs do not reset or fork an in-progress form. */
data class DialogDraft(val inputText: String, val formValues: Map<String, String>)

data class DialogPresentation(
    val revision: Long, val dialog: Dialog, val draft: DialogDraft,
    val interactionRevision: Long = 0, val inputValueRevision: Long = 0, val isChoosingInput: Boolean = false,
)

/** Frontend-confined modal lifetime, draft validation and callback dispatch; no renderer dependency. */
class DialogStateStore(private val onChanged: () -> Unit = {}) {
    private var postResult: (() -> Unit) -> Unit = { it() }
    constructor(postResult: (() -> Unit) -> Unit, onChanged: () -> Unit) : this(onChanged) {
        this.postResult = postResult
    }
    private data class Entry(
        val revision: Long,
        val dialog: Dialog,
        var draft: DialogDraft,
        val isValid: () -> Boolean,
        var submitted: Boolean = false,
        var invoking: Boolean = false,
        var interactionRevision: Long = 0,
        var inputValueRevision: Long = 0,
        var pickerRequest: Long? = null,
        var selection: DialogInputChoice? = null,
    )

    private var nextRevision = 0L
    private var nextPickerRequest = 0L
    private var entry: Entry? = null
    val isVisible: Boolean get() = entry != null
    val presentation: DialogPresentation?
        get() = entry?.let { DialogPresentation(it.revision, it.dialog, it.draft, it.interactionRevision, it.inputValueRevision, it.pickerRequest != null) }

    fun show(dialog: Dialog, isValid: () -> Boolean = { true }) {
        // Copy collections once: neither a caller nor another UI thread may mutate a published form.
        val copy = dialog.copy(
            buttons = dialog.buttons.toList(),
            infoRows = dialog.infoRows.toList(),
            listItems = dialog.listItems.toList(),
            form = dialog.form?.let { form -> DialogForm(form.fields.map { field ->
                if (field is DialogFormField.Choice) field.copy(options = field.options.toList()) else field
            }) },
        )
        val previous = entry
        entry = Entry(++nextRevision, copy, DialogDraft(
            copy.textInput?.initialText.orEmpty(),
            copy.form?.fields?.associate { it.id to it.initialFormValue() }.orEmpty(),
        ), isValid)
        previous?.takeUnless { it.submitted || it.invoking }?.selection?.dispose()
        onChanged()
    }

    fun hide(revision: Long? = null): Boolean {
        val current = entry ?: return false
        if (revision != null && current.revision != revision) return false
        entry = null
        if (!current.submitted && !current.invoking) current.selection?.dispose()
        onChanged()
        return true
    }

    fun refreshValidity() {
        val current = entry ?: return
        if (!current.isValid()) hide(current.revision)
    }

    /** Ambiguous fields and old setter-only picker contracts retain the legacy renderer. */
    fun snapshot(): DialogUiState? {
        refreshValidity()
        val current = entry ?: return null
        val dialog = current.dialog
        val fields = dialog.form?.fields.orEmpty()
        if (dialog.textInput?.let { it.onTrailingIconPress != null && it.onChooseInput == null } == true ||
            fields.map { it.id }.distinct().size != fields.size) return null
        return DialogUiState(
            revision = current.revision,
            title = dialog.title,
            message = dialog.message,
            messageLabel = dialog.messageLabel,
            buttonLabels = dialog.buttons.map { it.label },
            textInput = dialog.textInput?.let {
                DialogInputState(current.draft.inputText, it.hint, it.trailingIcon, it.trailingIconTooltip,
                    it.onChooseInput != null, current.inputValueRevision, it.password
                )
            },
            fields = fields,
            formValues = current.draft.formValues,
            listItems = dialog.listItems,
            infoRows = dialog.infoRows.map { DialogInfoRowState(it.icon, it.label, it.value, it.emphasis, it.onPress != null) },
            interactionRevision = current.interactionRevision,
            isChoosingInput = current.pickerRequest != null,
            dismissButtonIndex = dialog.dismissButtonIndex,
        )
    }

    fun dispatch(revision: Long, action: DialogUiAction): Boolean {
        refreshValidity()
        val current = entry?.takeIf { it.revision == revision && !it.submitted && !it.invoking } ?: return false
        when (action) {
            is DialogUiAction.EditInput -> {
                if (current.dialog.textInput == null || current.pickerRequest != null ||
                    (action.valueRevision != null && action.valueRevision != current.inputValueRevision)) return false
                if (current.selection?.value != action.value) {
                    current.selection?.dispose()
                    current.selection = null
                }
                current.draft = current.draft.copy(inputText = action.value)
                onChanged()
            }
            is DialogUiAction.EditField -> {
                if (current.pickerRequest != null) return false
                val field = current.dialog.form?.fields?.find { it.id == action.id } ?: return false
                if (!field.acceptsFormValue(action.value)) return false
                current.draft = current.draft.copy(formValues = current.draft.formValues + (action.id to action.value))
                onChanged()
            }
            is DialogUiAction.PressButton -> {
                val button = current.dialog.buttons.getOrNull(action.index) ?: return false
                if (current.pickerRequest != null && action.index != current.dialog.dismissButtonIndex) return false
                if (button.onInputPress != null && action.inputValueRevision != null &&
                    action.inputValueRevision != current.inputValueRevision) return false
                val fields = current.dialog.form?.fields.orEmpty()
                // Only declared fields cross the bridge; hidden host overrides cannot be injected.
                val values = fields.associate { it.id to (action.formValues[it.id] ?: current.draft.formValues[it.id] ?: it.initialFormValue()) }
                if (button.onFormPress != null && fields.any { !it.acceptsFormValue(values.getValue(it.id)) }) return false
                val input = current.selection?.takeIf { it.value == action.inputText }?.submittedValue ?: action.inputText
                invokeAndClose(current, button, input, values)
            }
            is DialogUiAction.PressInfoRow -> {
                if (current.pickerRequest != null || action.interactionRevision != current.interactionRevision) return false
                val callback = current.dialog.infoRows.getOrNull(action.index)?.onPress ?: return false
                invokeInteraction(current, callback)
            }
            is DialogUiAction.ChooseInput -> {
                if (current.pickerRequest != null || action.interactionRevision != current.interactionRevision) return false
                val choose = current.dialog.textInput?.onChooseInput ?: return false
                val request = ++nextPickerRequest
                current.pickerRequest = request
                invokeInteraction(current) {
                    try {
                        choose { result -> postResult { finishChoice(current.revision, request, result) } }
                    } catch (error: Throwable) {
                        finishChoice(current.revision, request, null)
                        throw error
                    }
                }
            }
            DialogUiAction.Dismiss -> {
                val button = current.dialog.dismissButtonIndex?.let(current.dialog.buttons::getOrNull)
                invokeAndClose(current, button, current.draft.inputText, current.draft.formValues)
            }
        }
        return true
    }

    private fun invokeInteraction(current: Entry, callback: () -> Unit) {
        current.invoking = true
        current.interactionRevision++
        onChanged()
        try { callback() } finally {
            current.invoking = false
            if (entry !== current) current.selection?.dispose()
            onChanged()
        }
    }

    private fun finishChoice(revision: Long, request: Long, result: DialogInputChoice?) {
        refreshValidity()
        val current = entry
        if (current == null || current.revision != revision || current.pickerRequest != request || current.submitted) {
            if (current?.selection !== result) result?.dispose()
            return
        }
        current.pickerRequest = null
        current.interactionRevision++
        if (result != null) {
            if (current.selection !== result) current.selection?.dispose()
            current.selection = result
            current.draft = current.draft.copy(inputText = result.value)
            current.inputValueRevision++
        }
        onChanged()
    }

    private fun invokeAndClose(current: Entry, button: DialogButton?, input: String, values: Map<String, String>) {
        // Claim the old dialog before invoking code that may synchronously show another one.
        current.submitted = true
        try {
            when {
                button?.onFormPress != null -> button.onFormPress.invoke(values)
                button?.onInputPress != null -> button.onInputPress.invoke(input)
                else -> button?.onPress?.invoke()
            }
        } finally {
            // Even a callback that hides/replaces its dialog may still be reading the chosen file.
            current.selection?.dispose()
            hide(current.revision)
        }
    }
}

fun DialogFormField.initialFormValue(): String = when (this) {
    is DialogFormField.Choice -> options.getOrNull(selectedIndex)?.value ?: options.firstOrNull()?.value.orEmpty()
    is DialogFormField.Toggle -> checked.toString()
    is DialogFormField.Text -> initialText
}

private fun DialogFormField.acceptsFormValue(value: String): Boolean = when (this) {
    is DialogFormField.Choice -> options.any { it.value == value } || (options.isEmpty() && value.isEmpty())
    is DialogFormField.Toggle -> value.toBooleanStrictOrNull() != null
    is DialogFormField.Text -> true
}
