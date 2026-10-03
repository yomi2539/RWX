package io.github.rwx.ui.screen

import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.rwx.i18n.I18n
import io.github.rwx.ui.component.itemAppear
import io.github.rwx.ui.component.pressScale
import io.github.rwx.ui.component.withStableKeys
import io.github.rwx.ui.component.Icon
import io.github.rwx.ui.model.*
import io.github.rwx.ui.theme.Corners
import io.github.rwx.ui.theme.Layout
import io.github.rwx.ui.theme.LocalColorScheme
import io.github.rwx.ui.theme.Spacing

/** Lobby rendering only. Confirmation, direct addresses, hosting and player names use existing core dialogs. */
@Composable
fun MultiplayerScreen(
    state: MultiplayerRoomListModel,
    onAction: (MultiplayerAction) -> Unit,
    enableAnimations: Boolean = true,
) {
    val palette = LocalColorScheme.current.palette
    BoxWithConstraints(Modifier.fillMaxSize().background(palette.panelOverlay).semantics { paneTitle = state.title }) {
        val shortWindow = maxHeight < Layout.shortHeightBreakpoint
        val compactRooms = maxWidth < 960.dp
        // Short windows (phone landscape) give most of the height to the room list.
        val headerButtonHeight = if (shortWindow) 36.dp else null
        val listSpacing = if (shortWindow) Spacing.xs else Spacing.sm
        val cardMinHeight = if (shortWindow) Layout.compactBarMinHeight else Layout.menuButtonHeight
        val cardPadding = if (shortWindow) Spacing.sm else Spacing.md
        fun Modifier.headerButton(): Modifier =
            if (headerButtonHeight != null) this.height(headerButtonHeight) else this
        Column(
            Modifier.widthIn(max = 1560.dp).fillMaxSize().align(Alignment.Center)
                .padding(if (shortWindow) Spacing.sm else Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(listSpacing),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedButton(onClick = { onAction(MultiplayerAction.Back) }, modifier = Modifier.headerButton().testTag("multiplayer-back")) {
                    Text(I18n.common.back())
                }
                MultiplayerLobbyKind.entries.forEach { kind ->
                    FilterChip(
                        selected = state.lobbyKind == kind,
                        onClick = { onAction(MultiplayerAction.SwitchLobby(kind)) },
                        label = { Text(kind.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        modifier = Modifier.weight(1f).testTag("multiplayer-lobby-${kind.name}"),
                    )
                }
                IconButton(
                    onClick = { onAction(MultiplayerAction.ConfigurePlayerName) },
                    modifier = Modifier.testTag("multiplayer-player-name")
                        .semantics { contentDescription = I18n.multiplayer.lobbySettings() },
                ) {
                    Icon(Icon.Settings, Layout.contentIconSize, palette.primary)
                }
            }
            var joinAddress by remember(state.lobbyKind) { mutableStateOf("") }
            fun submitJoin() {
                if (joinAddress.isNotBlank()) onAction(MultiplayerAction.JoinDirectWithAddress(joinAddress))
            }
            OutlinedTextField(
                value = joinAddress,
                onValueChange = { joinAddress = it },
                modifier = Modifier.fillMaxWidth().testTag("multiplayer-join-input"),
                placeholder = {
                    Text(
                        when (state.lobbyKind) {
                            MultiplayerLobbyKind.Original -> I18n.multiplayer.joinServerHint()
                            MultiplayerLobbyKind.P2P -> I18n.multiplayer.p2pRoomIdHint()
                        },
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = ::submitJoin,
                        modifier = Modifier.testTag("multiplayer-join-submit"),
                    ) {
                        Icon(Icon.Send, Layout.contentIconSize, palette.primary)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submitJoin() }),
            )
            if (state.isRefreshing) {
                LinearProgressIndicator(Modifier.fillMaxWidth().testTag("multiplayer-refreshing"))
            }
            val status = state.errorText ?: state.statusText
            if (state.rooms.isNotEmpty()) {
                if (!compactRooms) {
                    MultiplayerRoomHeaderRow(
                        cardPadding,
                        listOf(
                            I18n.multiplayer.heading.host() to 2f,
                            I18n.multiplayer.heading.map() to 3f,
                            I18n.multiplayer.heading.players() to 1f,
                            I18n.multiplayer.heading.state() to 1.2f,
                            I18n.multiplayer.heading.version() to 1f,
                            I18n.multiplayer.heading.transport() to 1.2f,
                            "" to 0.6f,
                        ),
                    )
                }
            }
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                if (state.rooms.isEmpty()) {
                    Text(
                        status.ifBlank { if (state.isRefreshing) I18n.multiplayer.searching() else I18n.multiplayer.noRooms() },
                        color = if (state.errorText != null) palette.danger else palette.textSecondary,
                        modifier = Modifier.testTag("multiplayer-status"),
                    )
                } else {
                    key(state.lobbyKind) {
                        val roomKeys = remember(state.rooms) { state.rooms.withStableKeys { it.roomId } }
                        LazyColumn(
                            state = rememberLazyListState(),
                            modifier = Modifier.fillMaxSize().testTag("multiplayer-rooms"),
                            verticalArrangement = Arrangement.spacedBy(listSpacing),
                            contentPadding = PaddingValues(bottom = Layout.pageBottomReserve),
                        ) {
                            itemsIndexed(state.rooms, key = { index, _ -> roomKeys[index] }) { _, room ->
                                Box(Modifier.then(if (enableAnimations) Modifier.animateItem() else Modifier).then(Modifier.itemAppear(enableAnimations))) {
                                    MultiplayerRoomCard(room, compactRooms, state.errorText == null, enableAnimations, cardMinHeight, cardPadding) {
                                        onAction(MultiplayerAction.JoinRoom(room.roomId))
                                    }
                                }
                            }
                        }
                    }
                }
                FloatingActionButton(
                    onClick = { onAction(MultiplayerAction.HostGame) },
                    modifier = Modifier.align(Alignment.BottomStart).padding(Spacing.md).testTag("multiplayer-host"),
                    shape = CircleShape,
                    containerColor = palette.primaryContainer,
                    contentColor = palette.onPrimary,
                ) {
                    Icon(Icon.Add, Layout.contentIconSize, palette.onPrimary)
                }
                if (!state.lastJoinAddress.isNullOrBlank()) {
                    ExtendedFloatingActionButton(
                        onClick = { onAction(MultiplayerAction.JoinLastGame) },
                        modifier = Modifier.align(Alignment.BottomCenter).padding(Spacing.md)
                            .testTag("multiplayer-join-last"),
                        containerColor = palette.primaryContainer,
                        contentColor = palette.onPrimary,
                    ) {
                        Icon(Icon.Replay, Layout.contentIconSize, palette.onPrimary)
                        Spacer(Modifier.width(Spacing.sm))
                        Text(I18n.multiplayer.joinLastGame())
                    }
                }
                FloatingActionButton(
                    onClick = { onAction(MultiplayerAction.Refresh) },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(Spacing.md).testTag("multiplayer-refresh"),
                    shape = CircleShape,
                    containerColor = palette.primaryContainer,
                    contentColor = palette.onPrimary,
                ) {
                    if (state.isRefreshing) {
                        CircularProgressIndicator(Modifier.size(24.dp), color = palette.onPrimary, strokeWidth = 2.dp)
                    } else {
                        Icon(Icon.Refresh, Layout.contentIconSize, palette.onPrimary)
                    }
                }
            }
            if (state.rooms.isNotEmpty() && status.isNotBlank()) {
                Text(status, color = if (state.errorText != null) palette.danger else palette.textSecondary,
                    modifier = Modifier.testTag("multiplayer-status"))
            }
        }
    }
}

@Composable
private fun MultiplayerRoomCard(
    room: MultiplayerRoomItem,
    compact: Boolean,
    available: Boolean,
    enableAnimations: Boolean = true,
    minHeight: Dp = Layout.menuButtonHeight,
    padding: Dp = Spacing.md,
    onJoin: () -> Unit,
) {
    val palette = LocalColorScheme.current.palette
    val flagsDescription = listOfNotNull(
        I18n.battleroom.flag.password().takeIf { room.requiresPassword },
        I18n.battleroom.flag.mods().takeIf { room.hasMods },
    ).joinToString(", ")
    val modifier = Modifier.fillMaxWidth().heightIn(min = minHeight)
        .background(palette.surfaceSunken, RoundedCornerShape(Corners.sm))
        .border(1.dp, palette.borderSubtle, RoundedCornerShape(Corners.sm))
        .testTag("multiplayer-room:${room.roomId}")
        .clickable(enabled = available && room.roomId.isNotBlank(), role = Role.Button, onClick = onJoin)
        .padding(padding)
        .pressScale(enableAnimations)
    if (compact) {
        val singleLineDescription =
            "${room.hostName} | ${room.mapName}, ${room.statusLabel()}" +
                flagsDescription.takeIf { it.isNotEmpty() }?.let { ", $it" }.orEmpty()
        Box(
            modifier.semantics { contentDescription = singleLineDescription },
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(room.hostName, Modifier.weight(2f).basicMarquee(iterations = Int.MAX_VALUE, animationMode = MarqueeAnimationMode.Immediately),
                    color = palette.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(room.mapName, Modifier.weight(3f).basicMarquee(iterations = Int.MAX_VALUE, animationMode = MarqueeAnimationMode.Immediately),
                    color = palette.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(room.playersLabel, Modifier.weight(1.3f), color = palette.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(Modifier.weight(1.2f), verticalAlignment = Alignment.CenterVertically) {
                    Text(room.stateLabel, Modifier.weight(1f), color = palette.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    RoomFlagIcons(room)
                }
            }
        }
    } else {
        val desktopDescription =
            "${room.hostName} | ${room.mapName}, ${room.statusLabel()}" +
                flagsDescription.takeIf { it.isNotEmpty() }?.let { ", $it" }.orEmpty()
        Box(
            modifier.semantics { contentDescription = desktopDescription },
            contentAlignment = Alignment.Center,
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                RoomCell(room.hostName, Modifier.weight(2f))
                RoomCell(room.mapName, Modifier.weight(3f))
                RoomCell(room.playersLabel, Modifier.weight(1f))
                RoomCell(room.stateLabel, Modifier.weight(1.2f))
                RoomCell(room.versionLabel, Modifier.weight(1f))
                RoomCell(room.transportLabel, Modifier.weight(1.2f))
                Box(Modifier.weight(0.6f), contentAlignment = Alignment.Center) {
                    RoomFlagIcons(room)
                }
            }
        }
    }
}

@Composable
private fun RoomFlagIcons(room: MultiplayerRoomItem) {
    val palette = LocalColorScheme.current.palette
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        if (room.requiresPassword) Icon(Icon.Password, Layout.flagIconSize, palette.textSecondary, Modifier.testTag("room-flag-password"))
        if (room.hasMods) Icon(Icon.Mods, Layout.flagIconSize, palette.textSecondary, Modifier.testTag("room-flag-mods"))
    }
}

@Composable
private fun MultiplayerRoomHeaderRow(contentPadding: Dp, cells: List<Pair<String, Float>>) {
    val palette = LocalColorScheme.current.palette
    Row(
        Modifier.fillMaxWidth().padding(horizontal = contentPadding).testTag("multiplayer-rooms-header"),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        cells.forEach { (text, weight) ->
            Text(text, Modifier.weight(weight), color = palette.secondary, style = MaterialTheme.typography.labelLarge,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun RoomCell(text: String, modifier: Modifier) {
    Text(text, modifier.basicMarquee(iterations = Int.MAX_VALUE, animationMode = MarqueeAnimationMode.Immediately),
        color = LocalColorScheme.current.palette.textPrimary,
        style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
}
