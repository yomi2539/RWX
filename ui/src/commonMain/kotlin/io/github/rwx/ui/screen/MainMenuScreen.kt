package io.github.rwx.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.rwx.ui.ColorSchemeId
import io.github.rwx.ui.ColorSchemeRegistry
import io.github.rwx.ui.Palette
import io.github.rwx.ui.Scheme
import io.github.rwx.ui.component.*
import io.github.rwx.ui.model.MainMenuAction
import io.github.rwx.ui.model.MainMenuConditions
import io.github.rwx.ui.model.MainMenuItem
import io.github.rwx.ui.theme.*

/** Compose rendering of the shared launcher model, using the actual host constraints. */
@Composable
fun MainMenuScreen(
    conditions: MainMenuConditions,
    items: List<MainMenuItem> = io.github.rwx.ui.model.MainMenuViewModel.items(conditions),
    colorSchemeId: ColorSchemeId = ColorSchemeRegistry.defaultSchemeId,
    battleBackgroundVisible: Boolean = false,
    enableAnimations: Boolean = true,
    onMenuAction: (MainMenuAction) -> Unit = {},
) {
    val scheme = ColorSchemeRegistry.schemeForCompose(colorSchemeId, LocalOverlayOpacity.current)

    CompositionLocalProvider(LocalColorScheme provides scheme) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            MainMenuLauncher(
                items = items,
                scheme = scheme,
                metrics = mainMenuLayoutMetrics(maxWidth, maxHeight),
                battleBackgroundVisible = battleBackgroundVisible,
                enableAnimations = enableAnimations,
                onAction = onMenuAction,
            )
        }
    }
}

private const val MAIN_MENU_SURFACE_ALPHA = 0.52f
private const val MAIN_MENU_PANEL_START_ALPHA = 0.68f
private const val MAIN_MENU_PANEL_END_ALPHA = 0.58f
private const val MAIN_MENU_HORIZONTAL_MARGIN_DP = 48f
private const val MAIN_MENU_VERTICAL_CHROME_DP = 260f
private const val MAIN_MENU_COLUMN_COUNT = 2
private val MAIN_MENU_FOOTER_BUTTON_WIDTH = 220.dp
private val MAIN_MENU_FOOTER_HEIGHT = 64.dp
private const val MAIN_MENU_SHORT_LANDSCAPE_HEIGHT_DP = 520f
private const val MAIN_MENU_HORIZONTAL_SCROLLBAR_CHROME_DP = 28f

internal data class MainMenuLayoutMetrics(
    val contentWidth: Dp,
    val menuViewportHeight: Dp,
    val isShortLandscape: Boolean,
)

@Composable
private fun MainMenuLauncher(
    items: List<MainMenuItem>,
    scheme: Scheme<Palette<Color>>,
    metrics: MainMenuLayoutMetrics,
    battleBackgroundVisible: Boolean,
    enableAnimations: Boolean,
    onAction: (MainMenuAction) -> Unit,
) {
    val menuWidth = mainMenuPanelWidth(metrics.contentWidth)
    val primaryItems =
        items.filterNot { it.action == MainMenuAction.About || it.action == MainMenuAction.Exit || it.action == MainMenuAction.Continue }
    val footerItems = items.filter { it.action == MainMenuAction.About || it.action == MainMenuAction.Exit }
    val resumeItem = items.find { it.action == MainMenuAction.Continue }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (battleBackgroundVisible) {
                    Color.Transparent
                } else {
                    scheme.palette.panelOverlayLight.copy(alpha = MAIN_MENU_SURFACE_ALPHA * LocalOverlayOpacity.current)
                }
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            MainMenuHeader(metrics.contentWidth, scheme, metrics.isShortLandscape)

            if (metrics.isShortLandscape) {
                MainMenuHorizontal(
                    items = primaryItems,
                    scheme = scheme,
                    width = metrics.contentWidth,
                    height = metrics.menuViewportHeight,
                    enableAnimations = enableAnimations,
                    onAction = onAction
                )
            } else {
                MainMenuColumns(
                    items = primaryItems,
                    scheme = scheme,
                    width = menuWidth,
                    height = metrics.menuViewportHeight,
                    enableAnimations = enableAnimations,
                    onAction = onAction
                )
            }

            MainMenuFooter(footerItems, scheme, metrics.contentWidth, onAction)
        }

        if (resumeItem != null) {
            FloatingActionButton(
                onClick = { onAction(MainMenuAction.Continue) },
                modifier = Modifier.align(Alignment.BottomEnd).padding(Spacing.lg).testTag("main-menu-resume"),
                shape = CircleShape,
                containerColor = scheme.palette.primaryContainer,
                contentColor = scheme.palette.onPrimary,
            ) {
                Icon(Icon.Continue, Layout.contentIconSize, scheme.palette.onPrimary)
            }
        }
    }
}

@Composable
internal fun MainMenuHeader(
    contentWidth: Dp,
    scheme: Scheme<Palette<Color>>,
    isShortLandscape: Boolean,
) {
    // Title with gradient
    Box(
        modifier = Modifier
            .width(contentWidth)
            .height(if (isShortLandscape) 88.dp else Layout.mainMenuTitleHeight)
            .padding(bottom = Spacing.xs)
    ) {
        GradientText(
            text = "RWX",
            fontSize = if (isShortLandscape) 64.sp else Fonts.displayTitle,
            startColor = scheme.palette.secondary,
            endColor = scheme.palette.primary,
            textAlign = TextAlign.Center
        )
    }

    // Subtitle
    Text(
        text = "Rusted Warfare Extension",
        modifier = Modifier
            .width(contentWidth)
            .padding(bottom = if (isShortLandscape) Spacing.sm else Spacing.xl),
        fontSize = Fonts.bodySmall,
        color = scheme.palette.textSecondary,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun MainMenuFooter(
    items: List<MainMenuItem>,
    scheme: Scheme<Palette<Color>>,
    width: Dp,
    onAction: (MainMenuAction) -> Unit,
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(MAIN_MENU_FOOTER_HEIGHT)
            .padding(top = Spacing.xs),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.height(Layout.menuButtonHeight),
            horizontalArrangement = Arrangement.Center
        ) {
            items.forEach { item ->
                TextIconButton(
                    label = item.label,
                    icon = item.action.menuIcon,
                    width = minOf(MAIN_MENU_FOOTER_BUTTON_WIDTH, width / items.size.coerceAtLeast(1)),
                    scheme = scheme,
                    onPressed = { onAction(item.action) }
                )
            }
        }
    }
}

@Composable
private fun MainMenuHorizontal(
    items: List<MainMenuItem>,
    scheme: Scheme<Palette<Color>>,
    width: Dp,
    height: Dp,
    enableAnimations: Boolean,
    onAction: (MainMenuAction) -> Unit,
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .padding(top = Spacing.xs, bottom = Spacing.sm)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        scheme.palette.surfaceBase.copy(alpha = MAIN_MENU_PANEL_START_ALPHA * LocalOverlayOpacity.current),
                        scheme.palette.panelOverlayDark.copy(alpha = MAIN_MENU_PANEL_END_ALPHA * LocalOverlayOpacity.current)
                    )
                ),
                shape = RoundedCornerShape(Spacing.sm)
            )
            .border(1.dp, scheme.palette.borderSubtle, RoundedCornerShape(Spacing.sm))
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(scrollState)
        ) {
            items.forEach { item ->
                Box(modifier = Modifier.width(Layout.mainMenuTileWidth)) {
                    MainMenuTileCard(item, scheme, enableAnimations) {
                        onAction(item.action)
                    }
                }
            }
        }
    }
}

@Composable
private fun MainMenuColumns(
    items: List<MainMenuItem>,
    scheme: Scheme<Palette<Color>>,
    width: Dp,
    height: Dp,
    enableAnimations: Boolean,
    onAction: (MainMenuAction) -> Unit,
) {
    val leftItems = items.filterIndexed { index, _ -> index % MAIN_MENU_COLUMN_COUNT == 0 }
    val rightItems = items.filterIndexed { index, _ -> index % MAIN_MENU_COLUMN_COUNT == 1 }
    val columnWidth = mainMenuColumnWidth(width)
    val columnsWidth = mainMenuColumnsContentWidth(columnWidth)
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .padding(top = Spacing.md, bottom = Spacing.lg)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        scheme.palette.surfaceBase.copy(alpha = MAIN_MENU_PANEL_START_ALPHA * LocalOverlayOpacity.current),
                        scheme.palette.panelOverlayDark.copy(alpha = MAIN_MENU_PANEL_END_ALPHA * LocalOverlayOpacity.current)
                    )
                ),
                shape = RoundedCornerShape(Spacing.sm)
            )
            .border(1.dp, scheme.palette.borderSubtle, RoundedCornerShape(Spacing.sm))
            .padding(vertical = Spacing.md),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Row(
                modifier = Modifier.width(columnsWidth),
                horizontalArrangement = Arrangement.spacedBy(Layout.mainMenuColumnGap),
            ) {
                MainMenuColumn(
                    items = leftItems,
                    width = columnWidth,
                    topOffset = 0.dp,
                    scheme = scheme,
                    enableAnimations = enableAnimations,
                    onAction = onAction
                )
                MainMenuColumn(
                    items = rightItems,
                    width = columnWidth,
                    topOffset = Layout.mainMenuColumnStagger,
                    scheme = scheme,
                    enableAnimations = enableAnimations,
                    onAction = onAction
                )
            }
        }
    }
}

@Composable
private fun MainMenuColumn(
    items: List<MainMenuItem>,
    width: Dp,
    topOffset: Dp,
    scheme: Scheme<Palette<Color>>,
    enableAnimations: Boolean,
    onAction: (MainMenuAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .width(width)
            .padding(top = topOffset)
    ) {
        items.forEach { item ->
            MainMenuTileCard(item, scheme, enableAnimations) {
                onAction(item.action)
            }
        }
    }
}

@Composable
private fun MainMenuTileCard(
    item: MainMenuItem,
    scheme: Scheme<Palette<Color>>,
    enableAnimations: Boolean,
    onPressed: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered = interactionSource.collectIsHoveredAsState().value

    val background = if (isHovered) scheme.palette.surfaceRaised else scheme.palette.surfaceSunken
    val border = if (isHovered) scheme.palette.primary else scheme.palette.borderSubtle
    val iconColor = if (isHovered) scheme.palette.secondary else scheme.palette.primary
    val textColor = if (isHovered) scheme.palette.primary else scheme.palette.textPrimary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Layout.mainMenuTileHeight)
            .padding(Spacing.xs)
            .background(background, RoundedCornerShape(Spacing.sm))
            .border(1.dp, border, RoundedCornerShape(Spacing.sm))
            .hoverable(interactionSource)
            .clickable(role = Role.Button, onClick = onPressed)
            .pressScale(enableAnimations)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Icon(item.action.menuIcon, Layout.mainMenuTileIconSize, iconColor)
            }
            Text(
                text = item.label,
                modifier = Modifier.fillMaxWidth(),
                fontSize = Fonts.bodySmall,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Clip
            )
        }
    }
}

internal fun mainMenuLayoutMetrics(viewportWidth: Dp, viewportHeight: Dp): MainMenuLayoutMetrics {
    val width = viewportWidth.value.coerceAtLeast(1f)
    val height = viewportHeight.value.coerceAtLeast(1f)
    val isShortLandscape = width > height && height < MAIN_MENU_SHORT_LANDSCAPE_HEIGHT_DP
    val availableWidth = (width - MAIN_MENU_HORIZONTAL_MARGIN_DP).coerceAtLeast(1f)
    val contentWidth = availableWidth.coerceAtMost(Layout.mainMenuMaxContentWidth.value).dp
    val menuHeight = if (isShortLandscape) {
        Layout.mainMenuTileHeight.value + MAIN_MENU_HORIZONTAL_SCROLLBAR_CHROME_DP
    } else {
        (height - MAIN_MENU_VERTICAL_CHROME_DP).coerceIn(1f, Layout.mainMenuMaxViewportHeight.value)
    }
    return MainMenuLayoutMetrics(contentWidth, menuHeight.dp, isShortLandscape)
}

private fun mainMenuPanelWidth(contentWidth: Dp): Dp {
    val columnsWidth = mainMenuColumnsContentWidth(Layout.mainMenuTileWidth)
    return columnsWidth.value.coerceAtMost(contentWidth.value).dp
}

private fun mainMenuColumnWidth(contentWidth: Dp): Dp {
    return contentWidth.splitEvenly(
        count = MAIN_MENU_COLUMN_COUNT,
        totalGap = Layout.mainMenuColumnGap,
        maxWidth = Layout.mainMenuTileMaxWidth
    )
}

private fun mainMenuColumnsContentWidth(columnWidth: Dp): Dp {
    return (columnWidth.value * MAIN_MENU_COLUMN_COUNT + Layout.mainMenuColumnGap.value).dp
}

/** Splits the available width without forcing a minimum that would overflow narrow hosts. */
private fun Dp.splitEvenly(count: Int, totalGap: Dp, maxWidth: Dp): Dp {
    val availableWidth = this.value - totalGap.value
    val itemWidth = availableWidth / count
    return itemWidth.coerceAtLeast(1f).coerceAtMost(maxWidth.value).dp
}

/**
 * Maps MainMenuAction to its corresponding icon.
 */
private val MainMenuAction.menuIcon: Icon
    get() = when (this) {
        MainMenuAction.Continue -> Icon.Continue
        MainMenuAction.SinglePlayer -> Icon.Map
        MainMenuAction.WatchReplay -> Icon.Replay
        MainMenuAction.Sandbox -> Icon.Sandbox
        MainMenuAction.Multiplayer,
        MainMenuAction.P2PMultiplayer -> Icon.Multiplayer
        MainMenuAction.Settings -> Icon.Settings
        MainMenuAction.Mods -> Icon.Mods
        MainMenuAction.ResourceBrowser -> Icon.Search
        MainMenuAction.About -> Icon.Help
        MainMenuAction.Exit -> Icon.Exit
    }
