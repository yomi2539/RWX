package io.github.rwx.ui.model

import com.corrodinggames.rts.gameFramework.GameEngine
import com.corrodinggames.rts.gameFramework.KeyBinding
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import io.github.rwx.i18n.I18n
import io.github.rwx.i18n.I18nText
import io.github.rwx.ui.AppScreen
import io.github.rwx.ui.ColorSchemeId
import io.github.rwx.ui.ColorSchemeRegistry
import io.github.rwx.ui.DEFAULT_OVERLAY_OPACITY
import io.github.rwx.ui.Palette
import io.github.rwx.ui.Scheme
import io.github.rwx.ui.UiColor
import kotlin.math.roundToInt


enum class SettingsPage(private val titleText: I18nText, private val tabText: I18nText) {
    Display(I18n.settings.pages.display, I18n.settings.pages.display),
    Audio(I18n.settings.pages.audio, I18n.settings.pages.audio),
    Gameplay(I18n.settings.pages.gameplay, I18n.settings.pages.gameplay),
    KeyBindings(I18n.settings.pages.keybindings, I18n.settings.pages.keybindings),
    Theme(I18n.settings.pages.theme, I18n.settings.pages.theme),
    ;

    /** Resolved at render time so locale switches apply without rebuilding the enum. */
    val title: String get() = titleText()

    /** Resolved at render time so locale switches apply without rebuilding the enum. */
    val tabTitle: String get() = tabText()
}

sealed interface SettingsPageItem {
    data class Toggle(val toggle: SettingToggle) : SettingsPageItem
    data class Slider(val slider: SettingSlider) : SettingsPageItem
    data class ColorSchemeSelector(val item: SettingColorSchemeItem, val selected: Boolean) : SettingsPageItem
    data class StorageLocation(val selectedType: Int) : SettingsPageItem
}

sealed interface SettingsScrollRow {
    data class SectionTitle(val text: String) : SettingsScrollRow
    data class BodyText(val text: String) : SettingsScrollRow
    data class Toggle(val toggle: SettingToggle) : SettingsScrollRow
    data class Slider(val slider: SettingSlider) : SettingsScrollRow
    data class KeyBinding(val row: SettingKeyBindingRow) : SettingsScrollRow
    data class ColorSchemeSelector(val item: SettingColorSchemeItem, val selected: Boolean) : SettingsScrollRow
    data class StorageLocation(val selectedType: Int) : SettingsScrollRow
}

data class SettingsPageContent(
    val page: SettingsPage,
    val items: List<SettingsPageItem>,
)

data class SettingToggle(val i18nText: I18nText, val state: MutableState<Boolean>)

enum class AndroidStoragePreference(val storageType: Int, private val text: I18nText) {
    Internal(0, I18n.settings.storage.internal),
    External(2, I18n.settings.storage.external),
    ;

    override fun toString(): String = text()

    companion object {
        fun fromStorageType(storageType: Int): AndroidStoragePreference =
            if (storageType >= External.storageType) External else Internal
    }
}

data class SettingSlider(
    val i18nText: I18nText,
    val state: MutableState<Float>,
    val min: Float,
    val max: Float,
    val step: Float = 0.05f,
    val formatValue: (Float) -> String = { value -> "${(value * 100f).roundToInt()}%" },
) {
    fun normalize(value: Float): Float {
        val clamped = value.coerceIn(min, max)
        if (step <= 0f) return clamped
        return (min + (((clamped - min) / step).roundToInt() * step)).coerceIn(min, max)
    }

    fun formatted(value: Float): String = formatValue(normalize(value))
}

data class SettingColorSchemeItem(
    val label: String,
    val key: String,
    val id: ColorSchemeId,
    val scheme: Scheme<Palette<UiColor>>,
)

data class SettingKeyBindingRow(
    val index: Int,
    val keyBinding: KeyBinding,
    val primaryText: String,
    val secondaryText: String,
    val primaryOverlaps: Boolean,
    val secondaryOverlaps: Boolean,
    val activeSlot: Int?,
)

class SettingsModel {
    // Display & Input
    val batterySaving: MutableState<Boolean> = mutableStateOf(false)
    val highRefreshRate: MutableState<Boolean> = mutableStateOf(true)
    val slick2dFullScreen: MutableState<Boolean> = mutableStateOf(true)
    val vsync: MutableState<Boolean> = mutableStateOf(false)
    val showUnitHp: MutableState<Boolean> = mutableStateOf(true)
    val showWaypoints: MutableState<Boolean> = mutableStateOf(true)
    val showZoomButton: MutableState<Boolean> = mutableStateOf(true)
    val showFps: MutableState<Boolean> = mutableStateOf(false)
    val renderClouds: MutableState<Boolean> = mutableStateOf(false)
    val renderDoubleScale: MutableState<Boolean> = mutableStateOf(false)
    val softFogFading: MutableState<Boolean> = mutableStateOf(false)
    val shaderEffects: MutableState<Boolean> = mutableStateOf(false)
    val teamShaders: MutableState<Boolean> = mutableStateOf(false)
    val useAndroidOpenGlRenderer: MutableState<Boolean> = mutableStateOf(false)
    val showBackgroundBattleDemo: MutableState<Boolean> = mutableStateOf(true)
    val mouseCaptureEnabled: MutableState<Boolean> = mutableStateOf(false)
    val mouseSupport: MutableState<Boolean> = mutableStateOf(true)
    val keyboardSupport: MutableState<Boolean> = mutableStateOf(true)
    val gestureZoom: MutableState<Boolean> = mutableStateOf(true)
    val useCircleSelect: MutableState<Boolean> = mutableStateOf(false)
    val showUnitGroups: MutableState<Boolean> = mutableStateOf(true)
    val immersiveFullScreen: MutableState<Boolean> = mutableStateOf(true)
    val unlockedScreenRotation: MutableState<Boolean> = mutableStateOf(false)
    val classicInterface: MutableState<Boolean> = mutableStateOf(false)
    val forceEnglish: MutableState<Boolean> = mutableStateOf(false)

    // Render layers (engine-only, no UI toggle)
    val renderBackground: MutableState<Boolean> = mutableStateOf(true)
    val renderExtraLayers: MutableState<Boolean> = mutableStateOf(true)
    val showHpChanges: MutableState<Boolean> = mutableStateOf(true)
    val showUnitIcons: MutableState<Boolean> = mutableStateOf(true)
    val useMinimapAllyColors: MutableState<Boolean> = mutableStateOf(true)
    val showWarLogOnScreen: MutableState<Boolean> = mutableStateOf(true)

    // Gameplay
    val quickRally: MutableState<Boolean> = mutableStateOf(true)
    val doubleClickToAttackMove: MutableState<Boolean> = mutableStateOf(true)
    val showMapPingsOnBattlefield: MutableState<Boolean> = mutableStateOf(true)
    val showMapPingsOnMinimap: MutableState<Boolean> = mutableStateOf(true)
    val showPlayerChatInGame: MutableState<Boolean> = mutableStateOf(true)
    val showChatAndPingShortcuts: MutableState<Boolean> = mutableStateOf(true)
    val smartSelection: MutableState<Boolean> = mutableStateOf(true)
    val autosaving: MutableState<Boolean> = mutableStateOf(true)
    val udpInMultiplayer: MutableState<Boolean> = mutableStateOf(false)
    val saveMultiplayerReplays: MutableState<Boolean> = mutableStateOf(true)
    val replaysShowRecordedChat: MutableState<Boolean> = mutableStateOf(true)
    val sendReports: MutableState<Boolean> = mutableStateOf(true)

    // Audio (engine-only, no UI toggle)
    val enableSounds: MutableState<Boolean> = mutableStateOf(true)

    // Volume
    val masterVolume: MutableState<Float> = mutableStateOf(0.5f)
    val gameVolume: MutableState<Float> = mutableStateOf(1.0f)
    val interfaceVolume: MutableState<Float> = mutableStateOf(0.8f)
    val musicVolume: MutableState<Float> = mutableStateOf(0.25f)

    // Scroll
    val scrollSpeed: MutableState<Float> = mutableStateOf(1.0f)
    val edgeScrollSpeed: MutableState<Float> = mutableStateOf(1.0f)

    // Color scheme
    val selectedColorSchemeId: MutableState<ColorSchemeId> = mutableStateOf(ColorSchemeRegistry.defaultSchemeId)

    // Theme
    val enableAnimations: MutableState<Boolean> = mutableStateOf(true)
    val overlayOpacity: MutableState<Float> = mutableStateOf(DEFAULT_OVERLAY_OPACITY)

    // Android original-engine file backend.
    val storageType: MutableState<Int> = mutableStateOf(2)

}

fun SettingsModel.colorScheme(): Scheme<Palette<UiColor>> = ColorSchemeRegistry.schemeFor(selectedColorSchemeId.value)

class SettingsViewModel(val model: SettingsModel) {

    private fun displayToggles(): List<SettingToggle> = buildList {
        if (isAndroidPlatform()) {
            add(SettingToggle(I18n.settings.display.batterySaving, model.batterySaving))
            add(SettingToggle(I18n.settings.display.highRefreshRate, model.highRefreshRate))
            add(SettingToggle(I18n.settings.`interface`.unlockedScreenRotation, model.unlockedScreenRotation))
        }
        if (isPcPlatform()) {
            add(SettingToggle(I18n.settings.display.fullscreen, model.slick2dFullScreen))
            add(SettingToggle(I18n.settings.display.vsync, model.vsync))
        }
        add(SettingToggle(I18n.settings.display.showUnitHp, model.showUnitHp))
        add(SettingToggle(I18n.settings.display.showWaypoints, model.showWaypoints))
        add(SettingToggle(I18n.settings.display.showUnitIcons, model.showUnitIcons))
        add(SettingToggle(I18n.settings.display.useMinimapAllyColors, model.useMinimapAllyColors))
        if (isAndroidPlatform()) {
            add(SettingToggle(I18n.settings.display.showZoomButton, model.showZoomButton))
            add(SettingToggle(I18n.settings.`interface`.immersiveFullScreen, model.immersiveFullScreen))
            add(SettingToggle(I18n.settings.display.renderDoubleScale, model.renderDoubleScale))
        }
        add(SettingToggle(I18n.settings.display.renderClouds, model.renderClouds))
        add(SettingToggle(I18n.settings.display.showWarLogOnScreen, model.showWarLogOnScreen))
        add(SettingToggle(I18n.settings.display.showFps, model.showFps))
        if (isAndroidPlatform()) {
            add(SettingToggle(I18n.settings.`interface`.classicInterface, model.classicInterface))
        }
        if (isAndroidPlatform()) {
            add(
                SettingToggle(
                    I18n.settings.display.experimentalAndroidOpenGlRenderer,
                    model.useAndroidOpenGlRenderer,
                )
            )
        }
        if (!isAndroidPlatform() || model.useAndroidOpenGlRenderer.value) {
            add(SettingToggle(I18n.settings.display.shaderEffects, model.shaderEffects))
            add(SettingToggle(I18n.settings.display.teamShaders, model.teamShaders))
        }
        add(SettingToggle(I18n.settings.display.softFogFading, model.softFogFading))
        add(SettingToggle(I18n.settings.display.showHpChanges, model.showHpChanges))
        if (isPcPlatform()) {
            add(SettingToggle(I18n.settings.`interface`.mouseCapture, model.mouseCaptureEnabled))
        }
        if (isAndroidPlatform()) {
            add(SettingToggle(I18n.settings.`interface`.showUnitGroups, model.showUnitGroups))
            add(SettingToggle(I18n.settings.`interface`.gestureZoom, model.gestureZoom))
            add(SettingToggle(I18n.settings.`interface`.useCircleSelect, model.useCircleSelect))
            add(SettingToggle(I18n.settings.`interface`.mouseSupport, model.mouseSupport))
            add(SettingToggle(I18n.settings.`interface`.keyboardSupport, model.keyboardSupport))
        }
        add(SettingToggle(I18n.settings.`interface`.forceEnglish, model.forceEnglish))
    }

    private fun displaySliders(): List<SettingSlider> = buildList {
        add(SettingSlider(I18n.settings.display.scrollSpeed, model.scrollSpeed, min = 0.5f, max = 2.0f))
        if (isPcPlatform()) {
            add(SettingSlider(I18n.settings.display.edgeScrollSpeed, model.edgeScrollSpeed, min = 0.5f, max = 2.0f))
        }
    }

    private fun audioToggles(): List<SettingToggle> = listOf(
        SettingToggle(I18n.settings.audio.enableSounds, model.enableSounds),
    )

    private fun audioSliders(): List<SettingSlider> = listOf(
        SettingSlider(I18n.settings.audio.masterVolume, model.masterVolume, min = 0.0f, max = 1.0f),
        SettingSlider(I18n.settings.audio.gameVolume, model.gameVolume, min = 0.0f, max = 1.0f),
        SettingSlider(I18n.settings.audio.interfaceVolume, model.interfaceVolume, min = 0.0f, max = 1.0f),
        SettingSlider(I18n.settings.audio.musicVolume, model.musicVolume, min = 0.0f, max = 1.0f),
    )

    private fun themeToggles(): List<SettingToggle> = listOf(
        SettingToggle(I18n.settings.theme.enableAnimations, model.enableAnimations),
        SettingToggle(I18n.settings.display.showBackgroundBattleDemo, model.showBackgroundBattleDemo),
    )

    private fun gameplayToggles(): List<SettingToggle> = buildList {
        add(SettingToggle(I18n.settings.gameplay.quickRally, model.quickRally))
        add(SettingToggle(I18n.settings.gameplay.doubleClickToAttackMove, model.doubleClickToAttackMove))
        add(SettingToggle(I18n.settings.gameplay.smartSelection, model.smartSelection))
        if (isAndroidPlatform()) {
            add(SettingToggle(I18n.settings.gameplay.autosaving, model.autosaving))
        }
        add(SettingToggle(I18n.settings.gameplay.udpInMultiplayer, model.udpInMultiplayer))
        add(SettingToggle(I18n.settings.gameplay.showMapPingsOnBattlefield, model.showMapPingsOnBattlefield))
        add(SettingToggle(I18n.settings.gameplay.showMapPingsOnMinimap, model.showMapPingsOnMinimap))
        add(SettingToggle(I18n.settings.gameplay.showPlayerChatInGame, model.showPlayerChatInGame))
        if (isPcPlatform()) {
            add(SettingToggle(I18n.settings.gameplay.showChatAndPingShortcuts, model.showChatAndPingShortcuts))
        }
        add(SettingToggle(I18n.settings.gameplay.saveMultiplayerReplays, model.saveMultiplayerReplays))
        add(SettingToggle(I18n.settings.gameplay.replaysShowRecordedChat, model.replaysShowRecordedChat))
        add(SettingToggle(I18n.settings.`interface`.sendReports, model.sendReports))
    }

    fun items(): List<SettingToggle> = displayToggles() + audioToggles() + gameplayToggles() + themeToggles()

    fun colorSchemeItems(): List<SettingColorSchemeItem> = ColorSchemeRegistry.schemes.map { scheme ->
        SettingColorSchemeItem(
            label = scheme.displayName,
            key = "color-scheme:${scheme.id.value}",
            id = scheme.id,
            scheme = scheme,
        )
    }

    fun pageAt(index: Int): SettingsPageContent {
        val pages = visibleSettingsPages()
        val page = pages[index.coerceIn(0, pages.lastIndex)]
        return when (page) {
            SettingsPage.Display -> SettingsPageContent(
                page = page,
                items = if (isAndroidPlatform()) {
                    listOf(SettingsPageItem.StorageLocation(model.storageType.value))
                } else {
                    emptyList()
                } + displayToggles().map { SettingsPageItem.Toggle(it) } +
                        displaySliders().map { SettingsPageItem.Slider(it) },
            )

            SettingsPage.Audio -> SettingsPageContent(
                page = page,
                items = audioToggles().map { SettingsPageItem.Toggle(it) } +
                        audioSliders().map { SettingsPageItem.Slider(it) },
            )

            SettingsPage.Gameplay -> SettingsPageContent(
                page = page,
                items = gameplayToggles().map { SettingsPageItem.Toggle(it) },
            )

            SettingsPage.KeyBindings -> SettingsPageContent(
                page = page,
                items = emptyList(),
            )

            SettingsPage.Theme -> SettingsPageContent(
                page = page,
                items = themeToggles().map { SettingsPageItem.Toggle(it) } +
                        SettingsPageItem.Slider(SettingSlider(
                            I18n.settings.theme.overlayOpacity, model.overlayOpacity, min = 0f, max = 1f,
                        )) +
                        colorSchemeItems().map { item ->
                            SettingsPageItem.ColorSchemeSelector(item, model.selectedColorSchemeId.value == item.id)
                        },
            )
        }
    }
}

internal fun visibleSettingsPages(): List<SettingsPage> =
    if (isAndroidPlatform()) {
        SettingsPage.entries.filterNot { it == SettingsPage.KeyBindings }
    } else {
        SettingsPage.entries.toList()
    }

private fun isAndroidPlatform(): Boolean = GameEngine.isAndroidPlatform()

private fun isPcPlatform(): Boolean = GameEngine.isPC()

sealed interface SettingsAction {
    data object PreviewChanges : SettingsAction
    data object ApplyChanges : SettingsAction
    data object Back : SettingsAction
}

sealed interface SettingsOutcome {
    data object PreviewChanges : SettingsOutcome
    data object ApplyChanges : SettingsOutcome
    data class Navigate(val screen: AppScreen) : SettingsOutcome
}

object SettingsNavigation {
    fun outcomeFor(action: SettingsAction): SettingsOutcome = when (action) {
        SettingsAction.PreviewChanges -> SettingsOutcome.PreviewChanges
        SettingsAction.ApplyChanges -> SettingsOutcome.ApplyChanges
        SettingsAction.Back -> SettingsOutcome.Navigate(AppScreen.MainMenu)
    }
}
