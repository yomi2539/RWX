package io.github.rwx.app

import com.corrodinggames.rts.gameFramework.local.Locale
import io.github.rwx.i18n.I18n
import io.github.rwx.logger
import io.github.rwx.mod.registry.UiRegistry
import io.github.rwx.render.frame.GameViewport
import io.github.rwx.session.GameSession
import io.github.rwx.ui.AppScreen
import io.github.rwx.ui.CoreUiEvent
import io.github.rwx.ui.host.DialogSceneHost
import io.github.rwx.ui.host.LoadingDialogSceneHost
import io.github.rwx.ui.model.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal class InGameDialogController(
    private val gameSession: GameSession,
    private val dialogSceneHost: DialogSceneHost,
    private val loadingDialogSceneHost: LoadingDialogSceneHost,
    private val currentScreen: () -> AppScreen,
    private val viewport: () -> GameViewport,
    private val showUnavailableDialog: (String) -> Unit
) {
    fun showDialogOverGame(dialog: Dialog) {
        val screen = currentScreen()
        val shouldOverlayGameCanvas = screen == AppScreen.InGame && !gameSession.usesFrameCommandRendering

        fun restoreGameCanvas() {
            if (currentScreen() == AppScreen.InGame && !gameSession.usesFrameCommandRendering) {
                gameSession.setGameVisible(
                    true,
                    viewport(),
                    uiOverlay = UiRegistry.hasActiveHudLayers(),
                    pausedBackground = false,
                )
            }
        }

        fun DialogButton.restoreGameAfterPress(): DialogButton {
            if (!shouldOverlayGameCanvas) return this
            return copy(
                onPress = if (onInputPress == null && onFormPress == null) {
                    {
                        try {
                            onPress?.invoke()
                        } finally {
                            restoreGameCanvas()
                        }
                        Unit
                    }
                } else {
                    onPress
                },
                onInputPress = onInputPress?.let { original ->
                    { input: String ->
                        try {
                            original(input)
                        } finally {
                            restoreGameCanvas()
                        }
                    }
                },
                onFormPress = onFormPress?.let { original ->
                    { values: Map<String, String> ->
                        try {
                            original(values)
                        } finally {
                            restoreGameCanvas()
                        }
                    }
                },
            )
        }

        if (shouldOverlayGameCanvas) {
            gameSession.setGameVisible(true, viewport(), uiOverlay = true, pausedBackground = true)
        }
        dialogSceneHost.show(
            dialog.copy(buttons = dialog.buttons.map { it.restoreGameAfterPress() })
        )
    }

    fun showLegacyMessageDialog(event: CoreUiEvent.MessageDialogRequested) {
        showDialogOverGame(
            Dialog(
                title = event.title,
                message = event.message,
                buttons = listOf(DialogButton(I18n.common.ok())),
            )
        )
    }

    fun showLegacyPasswordDialog(event: CoreUiEvent.PasswordDialogRequested) {
        val loadingDialogSuspended = loadingDialogSceneHost.suspendCurrent()
        showDialogOverGame(
            Dialog(
                title = event.title,
                message = event.prompt,
                textInput = DialogTextInput(hint = event.title),
                dismissButtonIndex = 1,
                buttons = listOf(
                    DialogButton(
                        label = event.confirmButtonLabel,
                        onInputPress = { input ->
                            runCatching {
                                event.handler.submitPassword(input)
                            }.onFailure { error ->
                                logger.warn(error) { "Legacy password dialog submit failed" }
                                showUnavailableDialog("Unable to submit input: ${error.message ?: error.javaClass.simpleName}")
                            }.also {
                                loadingDialogSuspended?.let(loadingDialogSceneHost::resume)
                            }
                        },
                    ),
                    DialogButton(
                        label = event.cancelButtonLabel,
                        onPress = {
                            runCatching {
                                event.handler.cancelPasswordEntry()
                            }.onFailure { error ->
                                logger.warn(error) { "Legacy password dialog cancel failed" }
                            }.also {
                                loadingDialogSuspended?.let(loadingDialogSceneHost::resume)
                            }
                        },
                    ),
                ),
            )
        )
    }

    fun showLegacyFormDialog(event: CoreUiEvent.FormDialogRequested) {
        showDialogOverGame(
            Dialog(
                title = event.title,
                message = event.message,
                form = DialogForm(
                    event.fields.map { field ->
                        DialogFormField.Text(
                            id = field.id,
                            label = field.label,
                            initialText = field.initialText,
                            hint = field.hint,
                        )
                    }
                ),
                buttons = listOf(
                    DialogButton(
                        label = event.confirmButtonLabel,
                        onFormPress = { values ->
                            runCatching {
                                event.handler.submit(values)
                            }.onFailure { error ->
                                logger.warn(error) { "Legacy form dialog submit failed" }
                                showUnavailableDialog("Unable to submit input: ${error.message ?: error.javaClass.simpleName}")
                            }
                        },
                    ),
                    DialogButton(event.cancelButtonLabel),
                ),
                scrollableForm = event.fields.size > 6,
            )
        )
    }

    fun showSaveGameDialog() {
        showDialogOverGame(
            Dialog(
                title = I18n.ingame.save.title(),
                message = I18n.ingame.save.message(),
                textInput = DialogTextInput(
                    initialText = "rwx_save",
                    hint = I18n.ingame.save.hint(),
                ),
                buttons = listOf(
                    DialogButton(
                        label = I18n.common.save(),
                        onInputPress = { name ->
                            val saveName = name.trim().ifBlank { "rwx_save" }
                            gameSession.requestSaveGame(saveName)
                        },
                    ),
                    DialogButton(I18n.common.cancel()),
                ),
            ),
        )
    }

    fun showExportMapDialog() {
        val initialName = defaultExportMapName()
        showDialogOverGame(
            Dialog(
                title = I18n.ingame.export.title(),
                message = I18n.ingame.export.message(),
                textInput = DialogTextInput(
                    initialText = initialName,
                    hint = I18n.ingame.export.hint(),
                ),
                buttons = listOf(
                    DialogButton(
                        label = "Export",
                        onInputPress = { name ->
                            val exportName = name.trim().ifBlank { initialName }
                            gameSession.requestExportMap(exportName)
                        },
                    ),
                    DialogButton(I18n.common.cancel()),
                ),
            ),
        )
    }

    fun showInGameChatDialog(teamOnly: Boolean) {
        val history = gameSession.multiplayerChatHistory()
        showDialogOverGame(
            Dialog(
                title = if (teamOnly) I18n.ingame.chat.teamChat() else I18n.battleroom.chat(),
                message = if (history.isEmpty()) I18n.ingame.chat.empty() else "",
                listItems = history.asReversed().map { line ->
                    DialogListItem(line.text, line.teamColorIndex)
                },
                textInput = DialogTextInput(hint = I18n.ingame.chat.hint()),
                buttons = listOf(
                    DialogButton(
                        label = I18n.battleroom.send(),
                        onInputPress = { message ->
                            val text = message.trim()
                            if (text.isNotBlank()) {
                                gameSession.requestChatMessage(text, teamOnly)
                            }
                        },
                    ),
                    DialogButton(I18n.common.cancel()),
                ),
            ),
        )
    }

    fun showInGamePlayerListDialog() {
        val players = gameSession.multiplayerPlayerList()
        showDialogOverGame(
            Dialog(
                title = I18n.battleroom.players(),
                message = if (players.isEmpty()) I18n.ingame.players.empty() else "",
                listItems = players.map { player ->
                    val ping = inGamePlayerPingSuffix(player.pingLabel)
                    DialogListItem(
                        text = "${player.name}  #${player.spawnLabel}  Team ${player.teamLabel}$ping",
                        colorIndex = player.nameColorIndex,
                    )
                },
                buttons = listOf(DialogButton(I18n.battleroom.close())),
            )
        )
    }

    fun showExitGameDialog(onExit: () -> Unit) {
        val multiplayer = gameSession.runningMultiplayerExitInfo()
        val title = when {
            multiplayer?.isHost == true -> Locale.get("menus.ingame.multiplayerClose.title")
            multiplayer != null -> Locale.get("menus.ingame.multiplayerClose.titleDisconnect")
            else -> I18n.pausemenu.exitGame()
        }
        val message = when {
            multiplayer?.isHost == true -> Locale.get("menus.ingame.multiplayerClose.messageEndGame")
            multiplayer != null -> Locale.get("menus.ingame.multiplayerClose.messageDisconnect")
            else -> I18n.ingame.exit.message()
        }
        val exitLabel = when {
            multiplayer?.isHost == true -> Locale.get("menus.ingame.exitGame")
            multiplayer != null -> Locale.get("menus.ingame.multiplayerClose.disconnectButton")
            else -> I18n.mainmenu.exit()
        }
        val buttons = buildList {
            add(
                DialogButton(exitLabel) {
                    if (multiplayer != null) {
                        gameSession.disconnectRunningMultiplayer()
                    }
                    onExit()
                }
            )
            if (multiplayer?.isHost == true) {
                add(
                    DialogButton(Locale.get("menus.ingame.multiplayerClose.returnToBattleroom")) {
                        if (!gameSession.scheduleReturnToBattleRoom()) {
                            showUnavailableDialog("Unable to schedule return to battle room")
                        }
                    }
                )
            }
            add(DialogButton(I18n.common.cancel()) {
                if (!gameSession.isEngineGameLoaded()) {
                    if (multiplayer != null) {
                        gameSession.disconnectRunningMultiplayer()
                    }
                    onExit()
                }
            })
        }
        showDialogOverGame(
            Dialog(
                title = title,
                message = message,
                buttons = buttons,
            ),
        )
    }

    fun requestInGameOverlay(enqueue: () -> Unit) {

        enqueue()
        if (currentScreen() == AppScreen.InGame && !gameSession.usesFrameCommandRendering) {

            gameSession.setGameVisible(true, viewport(), uiOverlay = true, pausedBackground = true)
        }
    }

    fun requestOverlayForQueuedEvent(event: CoreUiEvent) {
        if (!requiresGameOverlay(event)) return
        if (currentScreen() != AppScreen.InGame || gameSession.usesFrameCommandRendering) return

        gameSession.setGameVisible(true, viewport(), uiOverlay = true, pausedBackground = true)
    }

    private fun defaultExportMapName(): String {
        val mapName = gameSession.currentMapDisplayName()
            ?.takeIf { it.isNotBlank() }
            ?: gameSession.runningMapPath()
                ?.substringAfterLast('/')
                ?.removeSuffix(".tmx")
                ?.replace('_', ' ')
                ?.takeIf { it.isNotBlank() }
            ?: "Map"
        val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMM yyyy"))
        return "New $mapName - $date".replace("  ", " ")
    }
}

internal fun requiresGameOverlay(event: CoreUiEvent): Boolean = when (event) {
    CoreUiEvent.InGameExitRequested,
    CoreUiEvent.InGameReturnToBattleRoomRequested,
    CoreUiEvent.InGameSettingsRequested,
    CoreUiEvent.InGameSaveRequested,
    CoreUiEvent.InGameExportMapRequested,
    CoreUiEvent.InGameSurrenderRequested,
    is CoreUiEvent.InGameChatRequested,
    CoreUiEvent.InGamePlayerListRequested,
    is CoreUiEvent.InGameMapJumpRequested,
    CoreUiEvent.InGameMapListRequested,
    CoreUiEvent.InGameModWindowRequested,
    is CoreUiEvent.MessageDialogRequested,
    is CoreUiEvent.PasswordDialogRequested,
    is CoreUiEvent.FormDialogRequested -> true

    else -> false
}

internal fun inGamePlayerPingSuffix(pingLabel: String): String {
    val label = pingLabel.trim()
    if (label.isEmpty()) return ""
    return if (label.toIntOrNull() != null) "  ${label}ms" else "  $label"
}
