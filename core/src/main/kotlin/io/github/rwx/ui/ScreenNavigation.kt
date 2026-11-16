package io.github.rwx.ui

/**
 * The top-level screens the app can show. Neutral screen state — no gameplay, no rendering. The
 * render backend is driven separately; this only decides what UI layer is shown.
 */
enum class AppScreen {
    MainMenu,
    LevelSelect,
    ReplaySelect,
    Settings,
    Loading,
    Paused,
    InGame,
    Multiplayer,
    Mods,
    ResourceBrowser,
    BattleRoom,
    ModWindow,
}

/** Which rendering layers are shown on a given [AppScreen]. Plain data so the policy is testable. */
data class ScreenVisibility(
    val world: Boolean,
    val hud: Boolean,
    val mainMenu: Boolean,
    val levelSelect: Boolean,
    val replaySelect: Boolean = false,
    val settings: Boolean,
    val loading: Boolean = false,
    val paused: Boolean,
    val multiplayer: Boolean = false,
    val mods: Boolean = false,
    val resourceBrowser: Boolean = false,
    val battleRoom: Boolean = false,
)

object AppScreenLayout {
    fun visibilityFor(screen: AppScreen): ScreenVisibility = when (screen) {
        AppScreen.MainMenu -> ScreenVisibility(
            world = true,
            hud = false,
            mainMenu = true,
            levelSelect = false,
            replaySelect = false,
            settings = false,
            paused = false,
            multiplayer = false
        )

        AppScreen.LevelSelect -> ScreenVisibility(
            world = true,
            hud = false,
            mainMenu = false,
            levelSelect = true,
            replaySelect = false,
            settings = false,
            paused = false,
            multiplayer = false
        )

        AppScreen.ReplaySelect -> ScreenVisibility(
            world = true,
            hud = false,
            mainMenu = false,
            levelSelect = false,
            replaySelect = true,
            settings = false,
            paused = false,
            multiplayer = false
        )

        AppScreen.Settings -> ScreenVisibility(
            world = true,
            hud = false,
            mainMenu = false,
            levelSelect = false,
            replaySelect = false,
            settings = true,
            loading = false,
            paused = false,
            multiplayer = false
        )

        AppScreen.Loading -> ScreenVisibility(
            world = false,
            hud = false,
            mainMenu = false,
            levelSelect = false,
            replaySelect = false,
            settings = false,
            loading = true,
            paused = false,
            multiplayer = false
        )

        AppScreen.Paused -> ScreenVisibility(
            world = true,
            hud = true,
            mainMenu = false,
            levelSelect = false,
            replaySelect = false,
            settings = false,
            loading = false,
            paused = true,
            multiplayer = false
        )

        AppScreen.InGame -> ScreenVisibility(
            world = true,
            hud = true,
            mainMenu = false,
            levelSelect = false,
            replaySelect = false,
            settings = false,
            loading = false,
            paused = false,
            multiplayer = false
        )

        AppScreen.Multiplayer -> ScreenVisibility(
            world = true,
            hud = false,
            mainMenu = false,
            levelSelect = false,
            replaySelect = false,
            settings = false,
            loading = false,
            paused = false,
            multiplayer = true
        )

        AppScreen.Mods -> ScreenVisibility(
            world = true,
            hud = false,
            mainMenu = false,
            levelSelect = false,
            replaySelect = false,
            settings = false,
            loading = false,
            paused = false,
            multiplayer = false,
            mods = true
        )

        AppScreen.BattleRoom -> ScreenVisibility(
            world = true,
            hud = false,
            mainMenu = false,
            levelSelect = false,
            replaySelect = false,
            settings = false,
            loading = false,
            paused = false,
            multiplayer = false,
            mods = false,
            battleRoom = true
        )

        AppScreen.ResourceBrowser -> ScreenVisibility(
            world = true,
            hud = false,
            mainMenu = false,
            levelSelect = false,
            replaySelect = false,
            settings = false,
            loading = false,
            paused = false,
            multiplayer = false,
            mods = false,
            resourceBrowser = true,
            battleRoom = false
        )

        AppScreen.ModWindow -> ScreenVisibility(
            world = true,
            hud = false,
            mainMenu = false,
            levelSelect = false,
            replaySelect = false,
            settings = false,
            loading = false,
            paused = false,
            multiplayer = false,
            mods = false,
            resourceBrowser = false,
            battleRoom = false,
        )
    }
}


class ScreenNavigator(
    initialScreen: AppScreen = AppScreen.MainMenu,
    private val onScreenChanged: (AppScreen) -> Unit = {},
) {
    var current: AppScreen = initialScreen
        private set

    fun navigateTo(screen: AppScreen) {
        if (screen != current) {
            current = screen
            onScreenChanged(screen)
        }
    }
}
