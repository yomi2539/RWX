package io.github.rwx.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.rwx.i18n.I18n
import io.github.rwx.ui.component.MapPreviewImage
import io.github.rwx.ui.component.MapPreviewLoader
import io.github.rwx.ui.component.defaultMapPreviewLoader
import io.github.rwx.ui.component.itemAppear
import io.github.rwx.ui.component.pressScale
import io.github.rwx.ui.component.withStableKeys
import io.github.rwx.ui.component.Icon
import io.github.rwx.ui.model.*
import io.github.rwx.ui.theme.Corners
import io.github.rwx.ui.theme.Layout
import io.github.rwx.ui.theme.Spacing
import io.github.rwx.ui.theme.LocalColorScheme
import io.github.rwx.ui.theme.toComposeColor
import io.github.rwx.ui.theme.toUiColor

/** Presentation only: room commands, permission checks, and configuration dialogs stay in core. */
@Composable
fun BattleRoomScreen(
    model: BattleRoomModel,
    onAction: (BattleRoomAction) -> Unit,
    previewLoader: MapPreviewLoader = defaultMapPreviewLoader,
    enableAnimations: Boolean = true,
) {
    val palette = LocalColorScheme.current.palette
    BoxWithConstraints(Modifier.fillMaxSize().background(palette.panelOverlay)) {
        val compact = maxWidth < 600.dp
        val viewportMinH = maxHeight
        val contentPadding = Spacing.sm
        val rowSpacing = Spacing.sm
        Column(
            Modifier.widthIn(max = 1600.dp).fillMaxSize().align(Alignment.Center)
                .padding(contentPadding).imePadding(),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(rowSpacing)) {
                OutlinedButton(onClick = { onAction(BattleRoomAction.Back) }, modifier = Modifier.testTag("battle-room-back")) {
                    Text(I18n.common.back())
                }
                Text(model.info.mapName, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (!model.isAvailable) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(I18n.battleroom.unavailable(), modifier = Modifier.testTag("battle-room-unavailable"))
                }
            } else {
                val pageTag = if (compact) "battle-room-page" else "battle-room-landscape"
                val infoWeight = if (compact) 0.42f else 0.35f
                val playersWeight = if (compact) 0.58f else 0.65f
                val previewH = if (compact) 96.dp else 120.dp
                Column(
                    Modifier.fillMaxWidth().weight(1f)
                        .verticalScroll(rememberScrollState())
                        .testTag(pageTag),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = viewportMinH).testTag("battle-room-top"),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        RoomInfo(
                            model, onAction, previewLoader,
                            Modifier.weight(infoWeight).fillMaxHeight(),
                            enableAnimations = enableAnimations,
                            mapPreviewHeight = previewH,
                        )
                        RoomPlayers(model, onAction, Modifier.weight(playersWeight).fillMaxHeight(), enableAnimations)
                    }
                    RoomChat(model, onAction, Modifier.fillMaxWidth().heightIn(min = 240.dp))
                    Spacer(Modifier.fillMaxWidth().height(88.dp))
                }
            }
        }
        if (model.isHost && model.isAvailable) {
            Box(Modifier.widthIn(max = 1600.dp).fillMaxSize().align(Alignment.Center)) {
                FloatingActionButton(
                    onClick = { onAction(BattleRoomAction.AddAI) },
                    modifier = Modifier.align(Alignment.BottomStart).padding(Spacing.md).testTag("battle-room-add-ai"),
                    shape = CircleShape,
                    containerColor = palette.primaryContainer,
                    contentColor = palette.onPrimary,
                ) {
                    Icon(Icon.AddPerson, Layout.contentIconSize, palette.onPrimary)
                }
                FloatingActionButton(
                    onClick = { onAction(BattleRoomAction.Start) },
                    modifier = Modifier.align(Alignment.BottomEnd).padding( Spacing.md).testTag("battle-room-start"),
                    shape = CircleShape,
                    containerColor = palette.primaryContainer,
                    contentColor = palette.onPrimary,
                ) {
                    Icon(Icon.Start, Layout.contentIconSize, palette.onPrimary)
                }
            }
        }
    }
}

@Composable
private fun RoomInfo(
    model: BattleRoomModel,
    onAction: (BattleRoomAction) -> Unit,
    loader: MapPreviewLoader,
    modifier: Modifier = Modifier,
    enableAnimations: Boolean = true,
    mapPreviewHeight: Dp = 160.dp,
    contentPadding: Dp = Spacing.md,
) {
    val palette = LocalColorScheme.current.palette
    Column(modifier.fillMaxWidth().testTag("battle-room-info").border(1.dp, palette.borderSubtle, RoundedCornerShape(Corners.sm)).padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Box(Modifier.fillMaxWidth().height(mapPreviewHeight).testTag("battle-room-map")
            .clickable(enabled = model.isHost, role = Role.Button, onClick = { onAction(BattleRoomAction.SelectMap) })
            .pressScale(enableAnimations && model.isHost)) {
            MapPreviewImage(model.info.mapPreviewAssetPath, model.mapRevision, Modifier.fillMaxSize(), loader)
        }
        Text(model.info.mapTypeLabel, color = palette.textSecondary)
        model.info.rwxModeLabel?.let { Text(it, color = palette.primary, modifier = Modifier.testTag("battle-room-mode")) }
        model.info.rwxCompatibilityLabel?.let { Text(it, color = palette.textSecondary, modifier = Modifier.testTag("battle-room-compatibility")) }
        model.info.detailLines.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
        if (model.isHost) {
            OutlinedButton(onClick = { onAction(BattleRoomAction.OpenOptions) }, modifier = Modifier.fillMaxWidth().testTag("battle-room-options")) {
                Text(I18n.battleroom.options())
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RoomPlayers(
    model: BattleRoomModel,
    onAction: (BattleRoomAction) -> Unit,
    modifier: Modifier,
    enableAnimations: Boolean = true,
) {
    val palette = LocalColorScheme.current.palette
    Column(modifier.testTag("battle-room-players"), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Row(Modifier.fillMaxWidth().background(palette.surfaceRaised).padding(Spacing.sm)) {
            Text(I18n.battleroom.heading.name(), Modifier.weight(0.45f), style = MaterialTheme.typography.labelLarge)
            Text(I18n.battleroom.heading.spawn(), Modifier.weight(0.16f), style = MaterialTheme.typography.labelLarge)
            Text(I18n.battleroom.heading.team(), Modifier.weight(0.19f), style = MaterialTheme.typography.labelLarge)
            Text(I18n.battleroom.heading.ping(), Modifier.weight(0.20f), style = MaterialTheme.typography.labelLarge)
        }
        if (model.players.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(Spacing.lg), contentAlignment = Alignment.Center) { Text(I18n.battleroom.noPlayers(), Modifier.testTag("battle-room-no-players")) }
        } else {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                model.players.forEach { player ->
                    Box(Modifier.itemAppear(enableAnimations)) {
                    val status = when {
                        player.isSpectator -> I18n.battleroom.status.spectator()
                        !player.isReady -> I18n.battleroom.status.notReady()
                        player.isAI -> I18n.battleroom.status.ai()
                        player.isLocal -> I18n.battleroom.status.you()
                        else -> I18n.battleroom.status.ready()
                    }
                    val nameColor = if (player.isSpectator || !player.isReady) palette.textDisabled else teamColor(player.nameColorIndex, palette.textPrimary)
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = Layout.compactBarMinHeight).background(palette.surfaceSunken)
                            .testTag("battle-room-player:${player.id}").semantics { stateDescription = status }
                            .clickable(enabled = model.canConfigurePlayer(player.id), role = Role.Button,
                                onClick = { onAction(BattleRoomAction.SelectPlayer(player.id)) }).padding(Spacing.sm)
                            .pressScale(enableAnimations && model.canConfigurePlayer(player.id)),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(player.name, Modifier.weight(0.45f), color = nameColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(player.spawnLabel, Modifier.weight(0.16f), color = if (player.isSpectator) palette.textDisabled else teamColor(player.spawnColorIndex, palette.textSecondary))
                        Text(player.teamLabel, Modifier.weight(0.19f), color = if (player.isSpectator) palette.textDisabled else teamColor(player.teamColorIndex, palette.textSecondary))
                        Text(player.pingLabel, Modifier.weight(0.20f), color = if (player.isSpectator) palette.textDisabled else palette.textSecondary)
                    }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomChat(model: BattleRoomModel, onAction: (BattleRoomAction) -> Unit, modifier: Modifier) {
    val palette = LocalColorScheme.current.palette
    var draft by rememberSaveable { mutableStateOf("") }
    val logState = rememberLazyListState()
    val followingLatest = logState.layoutInfo.visibleItemsInfo.let { visible ->
        visible.isEmpty() || visible.last().index >= model.chatLines.lastIndex - 1
    }
    LaunchedEffect(model.chatLines.size) {
        if (model.chatLines.isNotEmpty() && followingLatest) logState.scrollToItem(model.chatLines.lastIndex)
    }
    fun send() {
        val text = draft.trim()
        if (text.isNotEmpty()) {
            onAction(BattleRoomAction.SendChat(text))
            draft = ""
        }
    }
    Column(modifier.testTag("battle-room-chat"), verticalArrangement = Arrangement.spacedBy(Spacing.tight)) {
        Text(I18n.battleroom.chat(), style = MaterialTheme.typography.titleMedium, color = palette.primary)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = draft, onValueChange = { draft = it }, singleLine = true,
                label = { Text(I18n.battleroom.sendMessage()) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { send() }),
                modifier = Modifier.weight(1f).testTag("battle-room-chat-input"),
            )
            Button(onClick = ::send, enabled = draft.isNotBlank(), modifier = Modifier.testTag("battle-room-send")) { Text(I18n.battleroom.send()) }
        }
        if (model.chatLines.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f).background(palette.surfaceSunken), contentAlignment = Alignment.Center) {
                Text(I18n.battleroom.emptyChat(), color = palette.textSecondary)
            }
        } else {
            val chatKeys = remember(model.chatLines) { model.chatLines.withStableKeys { it.text } }
            LazyColumn(Modifier.fillMaxWidth().weight(1f).background(palette.surfaceSunken).testTag("battle-room-chat-log"), state = logState) {
                itemsIndexed(model.chatLines, key = { index, _ -> chatKeys[index] }) { _, line ->
                    val color = battleRoomChatColorIndexFor(line, model.players)?.let { teamColor(it, palette.textPrimary) } ?: palette.textSecondary
                    Text(line.text, Modifier.fillMaxWidth().padding(Spacing.xs), color = color, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

private fun teamColor(index: Int, fallback: Color): Color =
    BattleRoomTeamColors.colorFor(index, fallback.toUiColor()).toComposeColor()
