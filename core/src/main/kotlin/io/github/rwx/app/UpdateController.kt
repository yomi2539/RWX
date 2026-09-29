package io.github.rwx.app

import io.github.rwx.AppMetadata
import io.github.rwx.i18n.I18n
import io.github.rwx.logger
import io.github.rwx.net.UpdateCheckResponse
import io.github.rwx.net.UpdateRelease
import io.github.rwx.ui.component.Icon
import io.github.rwx.ui.host.DialogSceneHost
import io.github.rwx.ui.host.LoadingDialogSceneHost
import io.github.rwx.ui.model.Dialog
import io.github.rwx.ui.model.DialogButton
import io.github.rwx.ui.model.LoadingDialogHandle
import io.github.rwx.ui.model.DialogInfoRow
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CancellationException

internal class UpdateController(
    private val appMetadata: AppMetadata,
    private val checkLatestRelease: suspend (String) -> Result<UpdateCheckResponse>,
    private val loadingDialogSceneHost: LoadingDialogSceneHost,
    private val dialogSceneHost: DialogSceneHost,
    private val openLink: (String) -> Unit,
    private val launchCheck: (suspend () -> Unit) -> Unit = { work -> launchOnIO("updates-check") { work() } },
) {
    private val result = AtomicReference<UpdateCheckResult?>(null)
    private val manualRequested = AtomicBoolean(false)
    private var loadingHandle: LoadingDialogHandle? = null
    private var inProgress = false
    private var automaticStarted = false

    fun maybeRequestAutomatic(isMainMenu: Boolean) {
        if (automaticStarted || !isMainMenu) return
        automaticStarted = true
        request(manual = false)
    }

    fun request(manual: Boolean) {
        if (manual) {
            manualRequested.set(true)
            loadingHandle = loadingDialogSceneHost.showCircular(
                title = I18n.update.checkingTitle(),
                message = I18n.update.checkingMessage(),
            )
        }
        if (inProgress) return

        inProgress = true
        launchCheck {
            val response = try {
                checkLatestRelease(appMetadata.versionName)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                Result.failure(error)
            }
            result.set(
                UpdateCheckResult(
                    manual = manual,
                    response = response.getOrNull(),
                    error = response.exceptionOrNull(),
                )
            )
        }
    }

    fun drive() {
        result.getAndSet(null)?.let(::handleResult)
    }

    private fun handleResult(result: UpdateCheckResult) {
        inProgress = false
        // A manual request can arrive after IO has published its result but before this frame.
        val manual = manualRequested.getAndSet(false) || result.manual
        if (manual) {
            val handle = loadingHandle
            loadingHandle = null
            handle?.let(loadingDialogSceneHost::hide)
        }
        val error = result.error
        if (error != null) {
            logger.warn(error) { "Unable to check GitHub releases" }
            if (manual) {
                dialogSceneHost.show(
                    Dialog(
                        title = I18n.update.failedTitle(),
                        message = I18n.update.failedMessage(error.message ?: error.javaClass.simpleName),
                        buttons = listOf(DialogButton(I18n.common.ok())),
                    ),
                )
            }
            return
        }

        val response = result.response ?: return
        val latestRelease = response.latestRelease
        if (response.isUpdateAvailable && latestRelease != null) {
            showAvailableDialog(latestRelease)
        } else if (manual) {
            dialogSceneHost.show(
                Dialog(
                    title = I18n.update.latestTitle(),
                    message = I18n.update.latestMessage(appMetadata.versionName),
                    buttons = listOf(DialogButton(I18n.common.ok())),
                ),
            )
        }
    }

    private fun showAvailableDialog(release: UpdateRelease) {
        val releaseNotes = release.body.trim().ifBlank { I18n.update.noReleaseNotes() }
        val rows = buildList {
            if (release.prerelease) {
                add(
                    DialogInfoRow(
                        icon = Icon.Version,
                        label = I18n.update.prerelease(),
                        value = release.displayVersion,
                        emphasis = true,
                    )
                )
            }
            release.publishedAt?.takeIf { it.isNotBlank() }?.let { publishedAt ->
                add(
                    DialogInfoRow(
                        icon = Icon.Version,
                        label = I18n.update.publishedAt(),
                        value = publishedAt,
                    )
                )
            }
            add(
                DialogInfoRow(
                    icon = Icon.Github,
                    label = I18n.update.releasePage(),
                    value = release.htmlUrl,
                    emphasis = true,
                    onPress = { openLink(release.htmlUrl) },
                )
            )
        }
        dialogSceneHost.show(
            Dialog(
                title = I18n.update.availableTitle(),
                message = I18n.update.availableMessage(release.displayVersion, releaseNotes),
                infoRows = rows,
                scrollableMessage = true,
                buttons = listOf(
                    DialogButton(I18n.update.openRelease()) { openLink(release.htmlUrl) },
                    DialogButton(I18n.common.ok()),
                ),
            ),
        )
    }
}

private data class UpdateCheckResult(
    val manual: Boolean,
    val response: UpdateCheckResponse?,
    val error: Throwable?,
)
