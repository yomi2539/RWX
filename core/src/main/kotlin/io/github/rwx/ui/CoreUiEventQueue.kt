package io.github.rwx.ui

import io.github.rwx.logger
import com.corrodinggames.rts.gameFramework.network.PasswordHandler
import io.github.rwx.map.PortalTransferMessage
import io.github.rwx.map.TransferredUnit
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

fun interface FormDialogSubmitHandler {
    fun submit(values: Map<String, String>)
}

data class FormDialogTextFieldRequest(
    val id: String,
    val label: String,
    val initialText: String = "",
    val hint: String = "",
)

sealed interface CoreUiEvent {
    data object OriginalRoomListRefresh : CoreUiEvent
    data object P2PRoomListRefresh : CoreUiEvent
    data object BattleRoomRefresh : CoreUiEvent
    data object BattleRoomGameStarted : CoreUiEvent
    data object ResourceBrowserSearchCompleted : CoreUiEvent
    data object ResourceBrowserDownloadProgress : CoreUiEvent
    data object ResourceBrowserDownloadCompleted : CoreUiEvent
    data object MenuBackgroundReady : CoreUiEvent
    data object InGameExitRequested : CoreUiEvent
    data object InGameReturnToBattleRoomRequested : CoreUiEvent
    data object InGameSettingsRequested : CoreUiEvent
    data object InGameSaveRequested : CoreUiEvent
    data object InGameExportMapRequested : CoreUiEvent
    data object InGameSurrenderRequested : CoreUiEvent
    data class InGameChatRequested(val teamOnly: Boolean) : CoreUiEvent
    data object InGamePlayerListRequested : CoreUiEvent
    data class InGameMapJumpRequested(
        val targetMapId: String,
        val targetPortalId: String?,
    ) : CoreUiEvent

    data object InGameMapListRequested : CoreUiEvent
    data object InGameModWindowRequested : CoreUiEvent
    data object InGameModWindowBackRequested : CoreUiEvent
    data class InGameMapPortalTransferRequested(
        val transfer: PortalTransferMessage,
    ) : CoreUiEvent

    data class BattleRoomChatMessage(
        val text: String,
        /** Author team id used for the chat tint; -1 for system/no team (matches the engine's color arg). */
        val teamColorIndex: Int = -1,
    ) : CoreUiEvent

    /** The network layer closed the battle room (kicked, server shut down, connection lost). */
    data class BattleRoomClosed(
        val reason: String?,
        val message: String?,
    ) : CoreUiEvent

    data class MessageDialogRequested(
        val title: String,
        val message: String,
    ) : CoreUiEvent

    data class PasswordDialogRequested(
        val handler: PasswordHandler,
        val title: String,
        val prompt: String,
        val confirmButtonLabel: String,
        val cancelButtonLabel: String,
    ) : CoreUiEvent

    data class FormDialogRequested(
        val handler: FormDialogSubmitHandler,
        val title: String,
        val message: String,
        val fields: List<FormDialogTextFieldRequest>,
        val confirmButtonLabel: String,
        val cancelButtonLabel: String,
    ) : CoreUiEvent
}

object CoreUiEventQueue {
    private val debugSlickMenu = System.getenv("RWX_DEBUG_SLICK_MENU") == "1"
    private val pending = ConcurrentLinkedQueue<CoreUiEvent>()
    private val originalRoomListRefreshPending = AtomicBoolean(false)
    private val p2pRoomListRefreshPending = AtomicBoolean(false)
    private val battleRoomRefreshPending = AtomicBoolean(false)

    @Volatile
    private var overlayRequestHandler: ((CoreUiEvent) -> Unit)? = null

    init {
        preloadSingletonEvents()
    }

    private fun preloadSingletonEvents() {
        // Load nested singleton event classes before long-running game callbacks need them.
        arrayOf<CoreUiEvent>(
            CoreUiEvent.OriginalRoomListRefresh,
            CoreUiEvent.P2PRoomListRefresh,
            CoreUiEvent.BattleRoomRefresh,
            CoreUiEvent.BattleRoomGameStarted,
            CoreUiEvent.ResourceBrowserSearchCompleted,
            CoreUiEvent.ResourceBrowserDownloadProgress,
            CoreUiEvent.ResourceBrowserDownloadCompleted,
            CoreUiEvent.MenuBackgroundReady,
            CoreUiEvent.InGameExitRequested,
            CoreUiEvent.InGameReturnToBattleRoomRequested,
            CoreUiEvent.InGamePlayerListRequested,
            CoreUiEvent.InGameSettingsRequested,
            CoreUiEvent.InGameSaveRequested,
            CoreUiEvent.InGameExportMapRequested,
            CoreUiEvent.InGameSurrenderRequested,
            CoreUiEvent.InGameMapListRequested,
            CoreUiEvent.InGameModWindowRequested,
            CoreUiEvent.InGameModWindowBackRequested,
        ).forEach { it.javaClass.name }
    }

    private fun enqueue(event: CoreUiEvent) {
        if (debugSlickMenu) {
            logger.debug {
                "RWX_DEBUG_SLICK_MENU CoreUiEventQueue.enqueue event=$event" +
                        " sizeBefore=${pending.size}" +
                        " thread=${Thread.currentThread().name}"
            }
        }
        pending.add(event)
        overlayRequestHandler?.let { handler ->
            runCatching {
                handler(event)
            }.onFailure { error ->
                logger.warn(error) { "Core UI overlay request failed" }
            }
        }
    }

    fun setOverlayRequestHandler(handler: ((CoreUiEvent) -> Unit)?) {
        overlayRequestHandler = handler
    }

    @JvmStatic
    fun requestOriginalRoomListRefresh() {
        enqueueSingleton(
            event = CoreUiEvent.OriginalRoomListRefresh,
            pendingFlag = originalRoomListRefreshPending,
        )
    }

    @JvmStatic
    fun requestP2PRoomListRefresh() {
        enqueueSingleton(
            event = CoreUiEvent.P2PRoomListRefresh,
            pendingFlag = p2pRoomListRefreshPending,
        )
    }

    @JvmStatic
    fun requestBattleRoomRefresh() {
        enqueueSingleton(
            event = CoreUiEvent.BattleRoomRefresh,
            pendingFlag = battleRoomRefreshPending,
        )
    }

    @JvmStatic
    fun requestBattleRoomStartGame() {
        requestBattleRoomGameStarted()
    }

    @JvmStatic
    fun requestBattleRoomGameStarted() {
        enqueue(CoreUiEvent.BattleRoomGameStarted)
    }

    fun requestResourceBrowserSearchCompleted() {
        enqueue(CoreUiEvent.ResourceBrowserSearchCompleted)
    }

    fun requestResourceBrowserDownloadProgress() {
        enqueue(CoreUiEvent.ResourceBrowserDownloadProgress)
    }

    fun requestResourceBrowserDownloadCompleted() {
        enqueue(CoreUiEvent.ResourceBrowserDownloadCompleted)
    }

    fun requestMenuBackgroundReady() {
        enqueue(CoreUiEvent.MenuBackgroundReady)
    }

    @JvmStatic
    fun requestInGameExit() {
        enqueue(CoreUiEvent.InGameExitRequested)
    }

    @JvmStatic
    fun requestInGameReturnToBattleRoom() {
        enqueue(CoreUiEvent.InGameReturnToBattleRoomRequested)
    }

    @JvmStatic
    fun requestInGameSettings() {
        enqueue(CoreUiEvent.InGameSettingsRequested)
    }

    @JvmStatic
    fun requestInGameSave() {
        enqueue(CoreUiEvent.InGameSaveRequested)
    }

    @JvmStatic
    fun requestInGameExportMap() {
        enqueue(CoreUiEvent.InGameExportMapRequested)
    }

    @JvmStatic
    fun requestInGameSurrender() {
        enqueue(CoreUiEvent.InGameSurrenderRequested)
    }

    @JvmStatic
    fun requestInGameChat(teamOnly: Boolean) {
        enqueue(CoreUiEvent.InGameChatRequested(teamOnly))
    }

    @JvmStatic
    fun requestInGamePlayerList() {
        enqueue(CoreUiEvent.InGamePlayerListRequested)
    }

    @JvmStatic
    fun requestInGameMapJump(targetMapId: String?, targetPortalId: String?) {
        val mapId = targetMapId?.takeIf { it.isNotBlank() } ?: return
        enqueue(CoreUiEvent.InGameMapJumpRequested(mapId, targetPortalId?.takeIf { it.isNotBlank() }))
    }

    @JvmStatic
    fun requestInGameMapList() {
        enqueue(CoreUiEvent.InGameMapListRequested)
    }

    @JvmStatic
    fun requestInGameModWindow() {
        enqueue(CoreUiEvent.InGameModWindowRequested)
    }

    @JvmStatic
    fun requestInGameModWindowBack() {
        enqueue(CoreUiEvent.InGameModWindowBackRequested)
    }

    @JvmStatic
    fun requestInGameMapPortalTransfer(
        sourceMapPath: String?,
        targetMapId: String?,
        targetPortalId: String?,
        unitTypeId: String?,
        teamId: Int,
        healthFraction: Float,
        direction: Float,
    ) {
        val mapId = targetMapId?.takeIf { it.isNotBlank() } ?: return
        val typeId = unitTypeId?.takeIf { it.isNotBlank() } ?: return
        enqueue(
            CoreUiEvent.InGameMapPortalTransferRequested(
                PortalTransferMessage(
                    sourceMapPath = sourceMapPath?.takeIf { it.isNotBlank() },
                    targetMapId = mapId,
                    targetPortalId = targetPortalId?.takeIf { it.isNotBlank() },
                    units = listOf(
                        TransferredUnit(
                            unitTypeId = typeId,
                            teamId = teamId,
                            healthFraction = healthFraction.coerceIn(0.01f, 1.0f),
                            direction = direction,
                        )
                    ),
                )
            )
        )
    }

    @JvmStatic
    fun appendBattleRoomChatMessage(text: String, teamColorIndex: Int = -1) {
        enqueue(CoreUiEvent.BattleRoomChatMessage(text, teamColorIndex))
    }

    @JvmStatic
    fun requestBattleRoomClosed(reason: String?, message: String?) {
        enqueue(
            CoreUiEvent.BattleRoomClosed(
                reason = reason?.takeIf { it.isNotBlank() },
                message = message?.takeIf { it.isNotBlank() },
            )
        )
    }

    @JvmStatic
    fun requestMessageDialog(title: String?, message: String?) {
        enqueue(
            CoreUiEvent.MessageDialogRequested(
                title = title?.takeIf { it.isNotBlank() } ?: "Message",
                message = message.orEmpty(),
            )
        )
    }

    @JvmStatic
    fun requestPasswordDialog(
        handler: PasswordHandler,
        title: String?,
        prompt: String?,
        confirmButtonLabel: String?,
        cancelButtonLabel: String?,
    ) {
        enqueue(
            CoreUiEvent.PasswordDialogRequested(
                handler = handler,
                title = title?.takeIf { it.isNotBlank() } ?: "Password Required",
                prompt = prompt?.takeIf { it.isNotBlank() } ?: "This server requires a password to join",
                confirmButtonLabel = confirmButtonLabel?.takeIf { it.isNotBlank() } ?: "OK",
                cancelButtonLabel = cancelButtonLabel?.takeIf { it.isNotBlank() } ?: "Cancel",
            )
        )
    }

    @JvmStatic
    fun requestFormDialog(
        title: String?,
        message: String?,
        fieldIds: Array<String>,
        labels: Array<String>,
        initialTexts: Array<String>,
        hints: Array<String>,
        confirmButtonLabel: String?,
        cancelButtonLabel: String?,
        handler: FormDialogSubmitHandler,
    ) {
        val count = minOf(fieldIds.size, labels.size)
        val fields = (0 until count).map { index ->
            FormDialogTextFieldRequest(
                id = fieldIds[index],
                label = labels[index],
                initialText = initialTexts.getOrNull(index).orEmpty(),
                hint = hints.getOrNull(index).orEmpty(),
            )
        }
        enqueue(
            CoreUiEvent.FormDialogRequested(
                handler = handler,
                title = title?.takeIf { it.isNotBlank() } ?: "Input",
                message = message.orEmpty(),
                fields = fields,
                confirmButtonLabel = confirmButtonLabel?.takeIf { it.isNotBlank() } ?: "OK",
                cancelButtonLabel = cancelButtonLabel?.takeIf { it.isNotBlank() } ?: "Cancel",
            )
        )
    }

    fun drain(handler: (CoreUiEvent) -> Unit) {
        while (true) {
            val event = pending.poll() ?: break
            when (event) {
                CoreUiEvent.OriginalRoomListRefresh -> originalRoomListRefreshPending.set(false)
                CoreUiEvent.P2PRoomListRefresh -> p2pRoomListRefreshPending.set(false)
                CoreUiEvent.BattleRoomRefresh -> battleRoomRefreshPending.set(false)
                else -> Unit
            }
            if (debugSlickMenu) {
                logger.debug {
                    "RWX_DEBUG_SLICK_MENU CoreUiEventQueue.drain event=$event" +
                            " sizeAfter=${pending.size}" +
                            " thread=${Thread.currentThread().name}"
                }
            }
            handler(event)
        }
    }

    private fun enqueueSingleton(event: CoreUiEvent, pendingFlag: AtomicBoolean) {
        if (!pendingFlag.compareAndSet(false, true)) return
        enqueue(event)
    }
}
