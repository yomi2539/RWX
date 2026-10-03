package io.github.rwx.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.rwx.i18n.I18n
import io.github.rwx.ui.component.itemAppear
import io.github.rwx.ui.model.*
import io.github.rwx.ui.theme.LocalColorScheme
import io.github.rwx.ui.theme.Spacing

@Composable
internal fun KeyBindingsContent(
    state: KeyBindingsState,
    onAction: (SettingsUiAction) -> Unit,
    modifier: Modifier = Modifier,
    enableAnimations: Boolean = true,
) {
    val palette = LocalColorScheme.current.palette
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(I18n.settings.keybindings.hint())
        state.activeCapture?.let { capture ->
            val row = state.rows.firstOrNull { it.index == capture.target.index }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(I18n.settings.keybindings.pressKeyFor(row?.name.orEmpty()), Modifier.weight(1f).testTag("key-capture-status"))
                TextButton(
                    onClick = { onAction(SettingsUiAction.CancelKeyCapture(capture.requestId)) },
                    modifier = Modifier.testTag("key-capture-cancel"),
                ) { Text(I18n.common.cancel()) }
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f).testTag("key-bindings-list"),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            contentPadding = PaddingValues(bottom = Spacing.lg),
        ) {
            items(state.rows, key = { "${it.index}:${it.bindingId}" }) { row ->
                Box(Modifier.then(if (enableAnimations) Modifier.animateItem() else Modifier).then(Modifier.itemAppear(enableAnimations))) {
                    if (row.isSection) {
                        Text(row.name, style = MaterialTheme.typography.titleLarge, color = palette.textPrimary,
                            modifier = Modifier.padding(vertical = Spacing.sm).testTag("key-section:${row.bindingId}"))
                    } else {
                        Surface(color = palette.surfaceSunken, shape = MaterialTheme.shapes.medium) {
                            Column(Modifier.fillMaxWidth().padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                Text(row.name, style = MaterialTheme.typography.titleMedium)
                                // Stack slots on narrow windows instead of clipping the clear buttons.
                                BoxWithConstraints {
                                    if (maxWidth < 560.dp) {
                                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                            BindingSlot(row, 0, state.activeCapture, onAction)
                                            BindingSlot(row, 1, state.activeCapture, onAction)
                                        }
                                    } else {
                                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                                            BindingSlot(row, 0, state.activeCapture, onAction, Modifier.weight(1f))
                                            BindingSlot(row, 1, state.activeCapture, onAction, Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BindingSlot(
    row: KeyBindingRowState,
    slot: Int,
    capture: KeyBindingCapture?,
    onAction: (SettingsUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalColorScheme.current.palette
    val target = row.target(slot)
    val active = capture?.target == target
    val text = if (slot == 0) row.primaryText else row.secondaryText
    val overlaps = if (slot == 0) row.primaryOverlaps else row.secondaryOverlaps
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
        Text(if (slot == 0) I18n.settings.keybindings.primary() else I18n.settings.keybindings.secondary(), style = MaterialTheme.typography.labelMedium)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            OutlinedButton(
                onClick = { onAction(SettingsUiAction.BeginKeyCapture(KeyBindingCapture(target))) },
                border = BorderStroke(1.dp, if (overlaps) palette.danger else if (active) palette.primary else palette.borderSubtle),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (active) palette.primaryContainer else palette.surfaceBase,
                    contentColor = if (overlaps) palette.danger else palette.textPrimary,
                ),
                modifier = Modifier.weight(1f).testTag("key-bind:${row.bindingId}:$slot"),
            ) { Text(if (active) I18n.settings.keybindings.pressKey() else text.ifBlank { I18n.settings.keybindings.none() }) }
            TextButton(
                onClick = { onAction(SettingsUiAction.ClearKeyBinding(target)) },
                modifier = Modifier.testTag("key-clear:${row.bindingId}:$slot"),
            ) { Text(I18n.settings.keybindings.clear()) }
        }
        if (overlaps) Text(I18n.settings.keybindings.conflict(), color = palette.danger,
            style = MaterialTheme.typography.labelSmall, modifier = Modifier.testTag("key-conflict:${row.bindingId}:$slot"))
    }
}
