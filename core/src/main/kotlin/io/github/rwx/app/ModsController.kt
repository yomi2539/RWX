package io.github.rwx.app

import io.github.rwx.PlatformBridge
import io.github.rwx.i18n.I18n
import io.github.rwx.logger
import io.github.rwx.mod.ModRepository
import io.github.rwx.p2p.transfer.TransferOperation
import io.github.rwx.session.GameSession
import io.github.rwx.ui.host.DialogSceneHost
import io.github.rwx.ui.host.LoadingDialogSceneHost
import io.github.rwx.ui.host.ModsSceneHost
import io.github.rwx.ui.model.Dialog
import io.github.rwx.ui.model.LoadingDialogHandle
import io.github.rwx.ui.model.DialogButton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.koin.mp.KoinPlatform.getKoin
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.milliseconds

internal class ModsController(
    private val modRepository: ModRepository,
    private val gameSession: GameSession,
    private val sceneHost: ModsSceneHost,
    private val loadingDialogSceneHost: LoadingDialogSceneHost,
    private val dialogSceneHost: DialogSceneHost,
    private val onModsReloaded: () -> Unit = {},
) {
    private var reloadLoading = false
    private var reloadDialogVisible = false
    private var loadingHandle: LoadingDialogHandle? = null
    private var reloadJob: Job? = null
    private val reloadResult = AtomicReference<ModsReloadResult?>(null)

    fun refresh(statusText: String = "") {
        sceneHost.updateMods(modRepository.listMods(), statusText)
    }

    fun consumeNotice(noticeRevision: Long) {
        sceneHost.consumeNotice(noticeRevision)
    }

    fun applyChangesAndRefresh() {
        if (TransferOperation.active) return
        modRepository.applyChanges()
        refresh()
    }

    fun reloadAvailableAndRefresh() {
        if (TransferOperation.active) return
        modRepository.reloadAvailableMods()
        refresh()
    }

    fun disableAllAndRefresh() {
        if (TransferOperation.active) return
        modRepository.disableAll()
        refresh()
    }

    fun toggleEnabledAndRefresh(modId: String) {
        if (TransferOperation.active) return
        modRepository.toggleEnabled(modId)
        refresh()
    }

    fun deleteAndRefresh(modId: String) {
        if (TransferOperation.active) return
        val deleted = modRepository.delete(modId)
        refresh(if (deleted) "" else I18n.mods.delete.failed())
    }

    fun showImportDialog() {
        if (TransferOperation.active) return
        dialogSceneHost.show(modImportDialog(getKoin().get<PlatformBridge>().filePickerHost) { path ->
            val result = modRepository.importMod(path)
            refresh(result.message)
        })
    }

    fun showDetails(modId: String, error: Boolean) {
        val mod = sceneHost.snapshot().mods.singleOrNull { it.id == modId } ?: return
        val text = if (error) mod.errorMessage else mod.description
        if (text.isNullOrBlank()) return
        dialogSceneHost.show(Dialog(
            title = mod.name,
            message = text,
            messageLabel = if (error) I18n.mods.error() else I18n.mods.description(),
            scrollableMessage = true,
            buttons = listOf(DialogButton(I18n.common.close())),
        ))
    }

    fun reloadWithDialog() {
        if (TransferOperation.active) return
        if (reloadLoading) {
            return
        }
        reloadLoading = true
        reloadResult.set(null)

        val job = launchOnIO("mods-reload") {
            val result = try {
                modRepository.applyChanges()
                val handledByBackend = gameSession.requestReloadTransfer()
                if (handledByBackend) {
                    waitForLoadingText("Mods reloaded", timeoutMillis = 60_000L)
                } else {
                    modRepository.reloadAppliedMods()
                }
                ModsReloadResult.Success
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                ModsReloadResult.Failed(error)
            }
            reloadResult.set(result)
        }
        reloadJob = job
        job.invokeOnCompletion { cause ->
            if (cause is CancellationException) {
                reloadResult.compareAndSet(null, ModsReloadResult.Cancelled)
            }
        }
        reloadDialogVisible = true
        loadingHandle = loadingDialogSceneHost.showProgress(
            title = I18n.mods.reload.title(),
            message = I18n.mods.reload.message(),
            progress = 0.05f,
        ) {
            cancelReload(job)
        }
    }

    fun driveReload(): Boolean {
        if (reloadLoading && reloadDialogVisible) {
            val status = gameSession.loadingStatus()
            loadingHandle?.let { handle ->
                loadingDialogSceneHost.updateProgress(
                    message = status.text.ifBlank { I18n.mods.reload.message() },
                    progress = status.progress ?: 0.05f,
                    handle = handle,
                )
            }
        }
        reloadResult.getAndSet(null)?.let { result ->
            if (reloadLoading) {
                finishReload(result)
            }
        }
        return reloadLoading
    }

    private fun hideLoading() {
        val handle = loadingHandle
        loadingHandle = null
        handle?.let(loadingDialogSceneHost::hide)
    }

    private fun cancelReload(job: Job) {
        if (!reloadLoading || reloadJob !== job) {
            return
        }
        reloadDialogVisible = false
        hideLoading()
        job.cancel()
    }

    private suspend fun waitForLoadingText(expectedText: String, timeoutMillis: Long) {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (System.currentTimeMillis() < deadline) {
            val status = gameSession.loadingStatus()
            if (status.text == expectedText && (status.progress ?: 0.0f) >= 1.0f) {
                return
            }
            delay(50L.milliseconds)
        }
        throw IllegalStateException("Timed out waiting for $expectedText")
    }

    private fun finishReload(result: ModsReloadResult) {
        reloadLoading = false
        reloadJob = null
        if (reloadDialogVisible) {
            reloadDialogVisible = false
            hideLoading()
        }
        when (result) {
            ModsReloadResult.Success -> {
                onModsReloaded()
                refresh(I18n.mods.reload.done())
            }

            ModsReloadResult.Cancelled -> Unit
            is ModsReloadResult.Failed -> {
                val error = result.error
                logger.warn(error) { "Unable to reload mods" }
                refresh(I18n.mods.reload.failed(error.message ?: error.javaClass.simpleName))
            }
        }
    }
}

private sealed interface ModsReloadResult {
    data object Success : ModsReloadResult
    data object Cancelled : ModsReloadResult
    data class Failed(val error: Throwable) : ModsReloadResult
}
