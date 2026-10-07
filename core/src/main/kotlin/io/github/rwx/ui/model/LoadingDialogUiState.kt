package io.github.rwx.ui.model

@JvmInline
value class LoadingDialogHandle(val id: Long)

data class LoadingDialogSuspension internal constructor(val handle: LoadingDialogHandle, internal val generation: Long)

/** No business callback crosses the frontend/Compose boundary. Null progress is indeterminate. */
data class LoadingDialogUiState(
    val revision: Long,
    val title: String,
    val message: String,
    val progress: Float? = null,
    val cancelRequested: Boolean = false,
    val cancellable: Boolean = true,
)

/** Operation identity is independent of renderer visibility and password-prompt suspension. */
class LoadingDialogStateStore(private val onChanged: () -> Unit = {}) {
    private data class Entry(
        var state: LoadingDialogUiState,
        val onCancel: (() -> Unit)?,
        var suspension: LoadingDialogSuspension? = null,
    )
    private var entry: Entry? = null
    private var nextRevision = 0L
    private var nextSuspension = 0L
    val isVisible: Boolean get() = entry?.suspension == null && entry != null
    val currentSuspension: LoadingDialogSuspension? get() = entry?.suspension
    fun snapshot(): LoadingDialogUiState? = entry?.takeIf { it.suspension == null }?.state

    fun show(title: String, message: String, progress: Float?, onCancel: (() -> Unit)? = null): LoadingDialogHandle {
        val handle = LoadingDialogHandle(++nextRevision)
        entry = Entry(LoadingDialogUiState(handle.id, title, message, progress.normalized()), onCancel)
        onChanged()
        return handle
    }

    fun updateProgress(message: String, progress: Float?, handle: LoadingDialogHandle? = null): Boolean {
        val current = entry ?: return false
        if ((handle != null && current.state.revision != handle.id) || current.state.cancelRequested) return false
        current.state = current.state.copy(message = message.ifBlank { current.state.message }, progress = progress.normalized())
        onChanged()
        return true
    }

    fun hide(handle: LoadingDialogHandle? = null): Boolean {
        val current = entry ?: return false
        if (handle != null && current.state.revision != handle.id) return false
        entry = null
        onChanged()
        return true
    }

    fun disableCancellation(handle: LoadingDialogHandle) {
        val current = entry?.takeIf { it.state.revision == handle.id } ?: return
        current.state = current.state.copy(cancellable = false)
        onChanged()
    }

    fun cancel(revision: Long): Boolean {
        val current = entry ?: return false
        if (current.state.revision != revision || current.suspension != null || current.state.cancelRequested || !current.state.cancellable) return false
        current.state = current.state.copy(cancelRequested = true)
        try {
            onChanged()
            current.onCancel?.invoke()
        } finally {
            // A cancel callback may synchronously start another operation.
            hide(LoadingDialogHandle(revision))
        }
        return true
    }

    fun suspendCurrent(): LoadingDialogSuspension? {
        val current = entry?.takeUnless { it.state.cancelRequested } ?: return null
        // A replacement password prompt acquires its own resume token, even if already hidden.
        val token = LoadingDialogSuspension(LoadingDialogHandle(current.state.revision), ++nextSuspension)
        current.suspension = token
        onChanged()
        return token
    }

    fun resume(token: LoadingDialogSuspension): Boolean {
        val current = entry ?: return false
        if (current.suspension != token || current.state.cancelRequested) return false
        current.suspension = null
        onChanged()
        return true
    }
}

private fun Float?.normalized(): Float? = this?.takeIf { it.isFinite() }?.coerceIn(0f, 1f)
