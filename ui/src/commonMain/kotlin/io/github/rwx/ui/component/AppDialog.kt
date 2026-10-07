package io.github.rwx.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.rwx.ui.model.*
import io.github.rwx.ui.theme.Corners
import io.github.rwx.ui.theme.Layout
import io.github.rwx.ui.theme.LocalColorScheme
import io.github.rwx.ui.theme.Spacing
import io.github.rwx.ui.theme.toComposeColor
import io.github.rwx.ui.theme.toUiColor

/** A real modal focus/input boundary, not a clickable scrim over the underlying page. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDialog(state: DialogUiState, enableAnimations: Boolean = true, onAction: (DialogUiAction) -> Unit) {
    // Local mirrors keep typing responsive while frontend acknowledgements are in flight. On a
    // renderer handoff they are recreated from the shared draft, including any edits made by Kool.
    var inputText by remember(state.revision, state.textInput?.valueRevision) { mutableStateOf(state.textInput?.value.orEmpty()) }
    var values by remember(state.revision) { mutableStateOf(state.formValues) }
    var submitted by remember(state.revision) { mutableStateOf(false) }
    var interactionPending by remember(state.revision, state.interactionRevision, state.isChoosingInput) { mutableStateOf(false) }
    val busy = interactionPending || state.isChoosingInput
    var escapePressedHere by remember(state.revision) { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val inputFocusRequester = remember { FocusRequester() }
    val palette = LocalColorScheme.current.palette
    val appear = remember(state.revision) { MutableTransitionState(false).apply { targetState = true } }
    fun dispatch(action: DialogUiAction) {
        if (submitted) return
        if (action is DialogUiAction.PressInfoRow || action is DialogUiAction.ChooseInput) {
            if (busy || interactionPending) return
            interactionPending = true
        }
        if (action is DialogUiAction.PressButton || action == DialogUiAction.Dismiss) submitted = true
        onAction(action)
    }
    Dialog(
        onDismissRequest = { dispatch(DialogUiAction.Dismiss) },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            // Handle down/up as a pair. A dropdown's consumed Escape release must not close us.
            dismissOnBackPress = false,
        ),
    ) {
        BoxWithConstraints(
            Modifier.fillMaxSize().padding(Spacing.lg)
                .testTag("app-dialog").semantics { paneTitle = state.title }
                .focusRequester(focusRequester)
                .onPreviewKeyEvent { event ->
                    if (event.key != Key.Escape) false else when (event.type) {
                        KeyEventType.KeyDown -> { escapePressedHere = true; true }
                        KeyEventType.KeyUp -> {
                            val handled = escapePressedHere
                            escapePressedHere = false
                            if (handled) dispatch(DialogUiAction.Dismiss)
                            handled
                        }
                        else -> false
                    }
                }.focusable(),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                appear,
                enter = if (enableAnimations) scaleIn() + fadeIn() else EnterTransition.None,
                exit = ExitTransition.None,
            ) {
            Surface(
                modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth().heightIn(max = maxHeight),
                shape = RoundedCornerShape(Corners.lg),
                color = palette.surfaceBase,
                tonalElevation = Layout.dialogElevation,
            ) {
                Column(Modifier.fillMaxWidth().padding(Layout.dialogContentPadding), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    Text(state.title, style = MaterialTheme.typography.headlineSmall, color = palette.primary)
                    Column(
                        Modifier.fillMaxWidth().weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()).testTag("dialog-body"),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        state.messageLabel?.takeIf { it.isNotBlank() }?.let {
                            Text(it, style = MaterialTheme.typography.labelLarge, color = palette.primary)
                        }
                        if (state.message.isNotBlank()) Text(state.message, color = palette.textSecondary)
                        state.infoRows.forEachIndexed { index, row ->
                            RichDialogRow(row, !submitted && !busy, Modifier.testTag("dialog-info-$index")) {
                                dispatch(DialogUiAction.PressInfoRow(index, state.interactionRevision))
                            }
                        }
                        state.listItems.forEach { item ->
                            Text(item.text, color = BattleRoomTeamColors.colorFor(item.colorIndex, palette.textSecondary.toUiColor()).toComposeColor())
                        }
                        state.textInput?.let { input ->
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it; dispatch(DialogUiAction.EditInput(it, input.valueRevision)) },
                                placeholder = { Text(input.hint) },
                                singleLine = true,
                                visualTransformation = if (input.password) androidx.compose.ui.text.input.PasswordVisualTransformation()
                                else androidx.compose.ui.text.input.VisualTransformation.None,
                                enabled = !submitted && !busy,
                                trailingIcon = if (input.trailingIcon != null || input.canChooseInput) {{
                                    val icon = input.trailingIcon ?: Icon.Import
                                    val description = input.trailingIconTooltip?.takeIf { it.isNotBlank() } ?: input.hint.ifBlank { icon.name }
                                    if (input.canChooseInput) {
                                        TooltipBox(
                                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                                            tooltip = { PlainTooltip { Text(description) } },
                                            state = rememberTooltipState(),
                                        ) {
                                            androidx.compose.material3.IconButton(
                                                onClick = { dispatch(DialogUiAction.ChooseInput(state.interactionRevision)) },
                                                enabled = !submitted && !busy,
                                                modifier = Modifier.testTag("dialog-choose-input").semantics { contentDescription = description },
                                            ) { AssetIcon(icon, Layout.contentIconSize, palette.primary) }
                                        }
                                    } else AssetIcon(icon, Layout.contentIconSize, palette.primary)
                                }} else null,
                                modifier = Modifier.fillMaxWidth().testTag("dialog-input").focusRequester(inputFocusRequester),
                            )
                        }
                        state.fields.forEach { field ->
                            key(field.id) {
                                DialogField(field, values[field.id] ?: field.initialFormValue(), !submitted && !busy) { value ->
                                    values = values + (field.id to value)
                                    dispatch(DialogUiAction.EditField(field.id, value))
                                }
                            }
                        }
                    }
                    FlowRow(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm, Alignment.End),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        state.buttonLabels.forEachIndexed { index, label ->
                            OutlinedButton(
                                onClick = { dispatch(DialogUiAction.PressButton(index, inputText, values.toMap(), state.textInput?.valueRevision)) },
                                enabled = !submitted && (!busy || index == state.dismissButtonIndex),
                                modifier = Modifier.testTag("dialog-button-$index"),
                            ) { Text(label) }
                        }
                    }
                    }
                }
            }
            // BoxWithConstraints subcomposes its fields during measurement. Requesting focus in
            // the parent Dialog effect can run before the input's FocusRequester is attached.
            LaunchedEffect(state.revision, state.isChoosingInput, state.textInput?.valueRevision) {
                if (!state.isChoosingInput) {
                    if (state.textInput != null) inputFocusRequester.requestFocus() else focusRequester.requestFocus()
                }
            }
        }
    }
}

@Composable
private fun DialogField(field: DialogFormField, value: String, enabled: Boolean, onChange: (String) -> Unit) {
    when (field) {
        is DialogFormField.Choice -> {
            var expanded by remember { mutableStateOf(false) }
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(field.label, style = MaterialTheme.typography.labelLarge)
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        enabled = enabled && field.options.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().testTag("dialog-field-${field.id}"),
                    ) { Text(field.options.firstOrNull { it.value == value }?.label.orEmpty()) }
                    DropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
                        field.options.forEachIndexed { index, option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = { expanded = false; onChange(option.value) },
                                modifier = Modifier.testTag("dialog-option-${field.id}-$index"),
                            )
                        }
                    }
                }
            }
        }
        is DialogFormField.Toggle -> {
            val checked = value.toBooleanStrictOrNull() ?: field.checked
            Row(
                Modifier.fillMaxWidth().testTag("dialog-field-${field.id}")
                    .toggleable(checked, enabled = enabled, role = Role.Switch, onValueChange = { onChange(it.toString()) })
                    .padding(vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Text(field.label, modifier = Modifier.weight(1f))
                Switch(checked, onCheckedChange = null, enabled = enabled)
            }
        }
        is DialogFormField.Text -> OutlinedTextField(
            value = value,
            onValueChange = onChange,
            label = { Text(field.label) },
            placeholder = { Text(field.hint) },
            singleLine = true,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().testTag("dialog-field-${field.id}"),
        )
    }
}

@Composable
private fun RichDialogRow(row: DialogInfoRowState, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val palette = LocalColorScheme.current.palette
    Row(
        modifier.fillMaxWidth().background(palette.surfaceSunken, RoundedCornerShape(Corners.sm))
            .then(if (row.isClickable) Modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick) else Modifier)
            .semantics(mergeDescendants = true) {}.padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        AssetIcon(row.icon, 26.dp, if (row.emphasis) palette.secondary else palette.primary)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(row.label, style = MaterialTheme.typography.labelLarge, color = palette.textPrimary)
            Text(row.value, color = if (row.emphasis) palette.primary else palette.textSecondary,
                fontWeight = if (row.emphasis) FontWeight.SemiBold else FontWeight.Normal)
        }
    }
}
