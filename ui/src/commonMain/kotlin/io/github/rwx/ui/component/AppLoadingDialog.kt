package io.github.rwx.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.rwx.i18n.I18n
import io.github.rwx.ui.model.LoadingDialogUiState
import io.github.rwx.ui.theme.Corners
import io.github.rwx.ui.theme.Layout
import io.github.rwx.ui.theme.LocalColorScheme
import io.github.rwx.ui.theme.Spacing
import kotlin.math.roundToInt

@Composable
fun AppLoadingDialog(state: LoadingDialogUiState, enableAnimations: Boolean = true, onCancel: () -> Unit) {
    val palette = LocalColorScheme.current.palette
    val focus = remember { FocusRequester() }
    var cancelSent by remember(state.revision) { mutableStateOf(false) }
    var escapeDown by remember(state.revision) { mutableStateOf(false) }
    val appear = remember(state.revision) { MutableTransitionState(false).apply { targetState = true } }
    fun cancel() {
        if (cancelSent || state.cancelRequested || !state.cancellable) return
        cancelSent = true
        onCancel()
    }
    Dialog(onDismissRequest = ::cancel, properties = DialogProperties(
        usePlatformDefaultWidth = false, dismissOnBackPress = false, dismissOnClickOutside = false,
    )) {
        BoxWithConstraints(
            Modifier.fillMaxSize().padding(Spacing.lg).testTag("loading-dialog")
                .semantics { paneTitle = state.title }.focusRequester(focus)
                .onPreviewKeyEvent { event ->
                    if (event.key != Key.Escape) false else when (event.type) {
                        KeyEventType.KeyDown -> { escapeDown = true; true }
                        KeyEventType.KeyUp -> {
                            val handled = escapeDown
                            escapeDown = false
                            if (handled) cancel()
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
            Surface(Modifier.widthIn(max = 680.dp).fillMaxWidth().heightIn(max = maxHeight),
                shape = RoundedCornerShape(Corners.lg), color = palette.surfaceBase, tonalElevation = Layout.dialogElevation) {
                Column(Modifier.padding(Layout.dialogContentPadding), verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
                    Text(state.title, style = MaterialTheme.typography.headlineSmall, color = palette.primary)
                    Text(state.message, color = palette.textSecondary,
                        modifier = Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState()))
                    val progress = state.progress?.takeIf { it.isFinite() }?.coerceIn(0f, 1f)
                    if (progress == null) {
                        CircularProgressIndicator(Modifier.size(40.dp).align(Alignment.CenterHorizontally).testTag("loading-indeterminate"), color = palette.primary)
                    } else {
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().testTag("loading-progress"), color = palette.primary)
                        Text("${(progress * 100).roundToInt()}%", modifier = Modifier.align(Alignment.End).testTag("loading-percent"))
                    }
                    OutlinedButton(
                        onClick = ::cancel, enabled = state.cancellable && !cancelSent && !state.cancelRequested,
                        modifier = Modifier.align(Alignment.End).testTag("loading-cancel")) { Text(I18n.common.cancel()) }
                }
            }
            }
            // Request after constrained content has been attached to the modal's focus tree.
            LaunchedEffect(state.revision) { focus.requestFocus() }
        }
    }
}
