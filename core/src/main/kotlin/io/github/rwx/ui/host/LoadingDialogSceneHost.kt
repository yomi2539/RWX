package io.github.rwx.ui.host

import io.github.rwx.ui.model.*

class LoadingDialogSceneHost(
    @Suppress("unused") private val model: SettingsModel = SettingsModel(),
    private val onVisibilityChanged: (Boolean) -> Unit = {},
) {
    private var composeOwned = false
    private val store = LoadingDialogStateStore(::syncPresentation)
    val isVisible: Boolean get() = store.isVisible
    fun snapshot(): LoadingDialogUiState? = store.snapshot()

    fun showProgress(title: String, message: String, progress: Float, onDismiss: (() -> Unit)? = null): LoadingDialogHandle =
        store.show(title, message, progress, onDismiss)

    fun showCircular(title: String, message: String, onDismiss: (() -> Unit)? = null): LoadingDialogHandle =
        store.show(title, message, null, onDismiss)

    fun updateProgress(message: String, progress: Float?, handle: LoadingDialogHandle? = null): Boolean = store.updateProgress(message, progress, handle)
    fun hide() { store.hide() }
    fun hide(handle: LoadingDialogHandle): Boolean = store.hide(handle)
    fun disableCancellation(handle: LoadingDialogHandle) = store.disableCancellation(handle)
    fun cancel(revision: Long): Boolean = store.cancel(revision)
    fun suspendCurrent(): LoadingDialogSuspension? = store.suspendCurrent()
    fun resume(token: LoadingDialogSuspension): Boolean = store.resume(token)

    /** Compatibility for synchronous legacy callers; async prompts should retain a suspension token. */
    fun temporarilyHide(): Boolean = isVisible && store.suspendCurrent() != null
    fun restoreFromTemporaryHide() { store.currentSuspension?.let(store::resume) }

    fun setComposeOwned(owned: Boolean) {
        if (composeOwned == owned) return
        composeOwned = owned
        notifyVisibility()
    }

    private fun syncPresentation() {
        notifyVisibility()
    }

    private fun notifyVisibility(notifyVisibility: Boolean = true) {
        if (notifyVisibility) onVisibilityChanged(isVisible && !composeOwned)
    }

    companion object { const val LOADING_DIALOG_SCENE_NAME: String = "loading-dialog" }
}
