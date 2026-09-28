package io.github.rwx.app

import io.github.rwx.ui.model.ModsAction
import io.github.rwx.ui.model.ResourceBrowserAction
import io.github.rwx.ui.model.ReplaySelectAction

import io.github.rwx.logger
import io.github.rwx.render.frame.GameFrame
import io.github.rwx.render.frame.GameViewport
import io.github.rwx.ui.*
import io.github.rwx.ui.model.BattleRoomAction
import io.github.rwx.ui.model.DialogUiAction
import io.github.rwx.ui.model.LevelSelectAction
import io.github.rwx.ui.model.LevelSelectMode
import io.github.rwx.ui.model.GameInputEvent
import io.github.rwx.ui.model.MainMenuAction
import io.github.rwx.ui.model.MainMenuConditions
import io.github.rwx.ui.model.ModWindowAction
import io.github.rwx.ui.model.MultiplayerAction
import io.github.rwx.ui.model.MultiplayerLobbyKind
import io.github.rwx.ui.model.PauseMenuAction
import io.github.rwx.ui.model.PauseMenuConditions
import io.github.rwx.ui.model.ResourceBrowserType
import io.github.rwx.ui.model.SettingsUiAction
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext

const val RWX_GAME_FRAME_READY_MARKER: String = "RWX_GAME_FRAME_READY"
private val ioSupervisor = SupervisorJob(ApplicationScope.job)

fun launchOnIO(name: String?=null,
               start: CoroutineStart = CoroutineStart.DEFAULT,
               exceptionHandler: (CoroutineContext, Throwable)->Unit={ _, t-> logger.error(t) },
               block: suspend CoroutineScope.() -> Unit): Job {
    val effectiveName = name ?: "IO-${System.identityHashCode(block)}"
    val ctx=ioSupervisor+Dispatchers.IO+ CoroutineName(effectiveName)+CoroutineExceptionHandler(exceptionHandler)
    return ApplicationScope.launch(ctx, start, block)
}
fun <T> asyncOnIO(
    name: String? = null,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
): Deferred<T> {
    val effectiveName = name ?: "IO-${System.identityHashCode(block)}"
    val ctx = ioSupervisor + Dispatchers.IO + CoroutineName(effectiveName)
    return ApplicationScope.async(ctx, start, block)
}
class AppSession internal constructor(
    val navigator: ScreenNavigator,
    private val uiController: AppUiController,
    private val onQuit: () -> Unit,
    private val onBack: () -> Unit,
    private val onClose: () -> Unit = {},
) : AutoCloseable {
    private var closed = false
    override fun close() {
        if (closed) return
        closed = true
        uiController.close()
        onClose()
    }
    fun navigateBack() = onBack()

    fun isFinishLoading(): Boolean =
        navigator.current != AppScreen.Loading

    fun quit() = onQuit()

    val uiState: StateFlow<AppUiState> get() = uiController.state

    fun mainMenuConditions(): MainMenuConditions = uiState.value.mainMenuConditions

    fun setComposeUiEnabled(enabled: Boolean) = uiController.setComposeEnabled(enabled)

    fun dispatchMenuAction(action: MainMenuAction) = uiController.dispatchMenuAction(action)

    fun dispatchSettingsAction(action: SettingsUiAction) = uiController.dispatchSettingsAction(action)

    fun dispatchLevelSelectAction(revision: Long, action: LevelSelectAction) = uiController.dispatchLevelSelectAction(revision, action)

    fun dispatchPauseAction(action: PauseMenuAction) = uiController.dispatchPauseAction(action)

    fun dispatchBattleRoomAction(revision: Long, action: BattleRoomAction) = uiController.dispatchBattleRoomAction(revision, action)

    fun dispatchModsAction(revision: Long, action: ModsAction) = uiController.dispatchModsAction(revision, action)

    fun dispatchResourceBrowserAction(revision: Long, action: ResourceBrowserAction) = uiController.dispatchResourceBrowserAction(revision, action)

    fun dispatchReplaySelectAction(revision: Long, action: ReplaySelectAction) = uiController.dispatchReplaySelectAction(revision, action)

    fun dispatchMultiplayerAction(lobbyKind: MultiplayerLobbyKind, revision: Long, action: MultiplayerAction) =
        uiController.dispatchMultiplayerAction(lobbyKind, revision, action)

    fun dispatchDialogAction(revision: Long, action: DialogUiAction) = uiController.dispatchDialogAction(revision, action)

    fun dispatchLoadingCancel(revision: Long) = uiController.dispatchLoadingCancel(revision)

    fun dispatchUiBack() = uiController.dispatchBack()

    fun dispatchModWindowAction(revision: Long, action: ModWindowAction) = uiController.dispatchModWindowAction(revision, action)

    /** Unconsumed input from an active non-modal game overlay. */
    fun dispatchGameInput(event: GameInputEvent) = uiController.dispatchGameInput(event)
}

fun installApp(
    viewportProvider: () -> GameViewport,
    scheduler: FrameScheduler,
    presenter: GameFramePresenter,
    options: AppOptions = AppOptions(),
    onQuit: () -> Unit = {},
): AppSession {
    val bootstrap = createAppBootstrap(options)
    val platformBridge = bootstrap.platformBridge
    val appMetadata = bootstrap.appMetadata
    val gameSession = bootstrap.gameSession
    val menuBackgroundSession = bootstrap.menuBackgroundSession
    val modRepository = bootstrap.modRepository
    val resourceBrowserRepository = bootstrap.resourceBrowserRepository
    val updateRepository = bootstrap.updateRepository
    val settingsRepository = bootstrap.settingsRepository
    val actions = bootstrap.actions
    val settingsModel = bootstrap.settingsModel
    val loadingSceneHost = bootstrap.loadingSceneHost
    val mainMenuSceneHost = bootstrap.mainMenuSceneHost
    val pauseSceneHost = bootstrap.pauseSceneHost
    val levelSelectSceneHost = bootstrap.levelSelectSceneHost
    val levelSelectViewModelFactory = bootstrap.levelSelectViewModelFactory
    val replaySelectSceneHost = bootstrap.replaySelectSceneHost
    val settingsSceneHost = bootstrap.settingsSceneHost
    val multiplayerSceneHost = bootstrap.multiplayerSceneHost
    val modsSceneHost = bootstrap.modsSceneHost
    val resourceBrowserSceneHost = bootstrap.resourceBrowserSceneHost
    val loadingDialogSceneHost = bootstrap.loadingDialogSceneHost
    val battleRoomSceneHost = bootstrap.battleRoomSceneHost
    val dialogSceneHost = bootstrap.dialogSceneHost

    var lastExternalGameFrame: GameFrame? = null
    val startupTargetScreen = if (options.initialScreen == AppScreen.Loading) {
        AppScreen.MainMenu
    } else {
        options.initialScreen
    }
    lateinit var navigator: ScreenNavigator
    lateinit var pendingStartController: PendingStartController

    fun rwGameViewport(): GameViewport = viewportProvider().resolved()

    val screenPresenter = ScreenPresenter(
        bootstrap = bootstrap,
        viewport = ::rwGameViewport,
    )

    val warmupController = WarmupController(
        gameSession = gameSession,
        menuBackgroundSession = menuBackgroundSession,
        loadingSceneHost = loadingSceneHost,
        navigateTo = { screen -> navigator.navigateTo(screen) },
        clearExternalFrame = { lastExternalGameFrame = null },
        isBackgroundBattleDemoEnabled = { settingsModel.showBackgroundBattleDemo.value },
    )

    lateinit var updateController: UpdateController
    val dialogController = DialogController(
        platformBridge = platformBridge,
        appMetadata = appMetadata,
        dialogSceneHost = dialogSceneHost,
        requestManualUpdateCheck = { updateController.request(manual = true) },
    )

    val battleRoomController = BattleRoomController(
        gameSession = gameSession,
        levelSelectViewModelFactory = levelSelectViewModelFactory,
        sceneHost = battleRoomSceneHost,
        initialMode = options.levelSelectMode ?: LevelSelectMode.Skirmish,
        showUnavailableDialog = dialogController::showUnavailable,
    )

    fun mainMenuConditions() = MainMenuConditions(
        canResume = gameSession.canResume(),
        usingMods = modRepository.hasEnabledMods(),
        isDesktop = options.isDesktop,
    )

    fun refreshMainMenu() {
        mainMenuSceneHost.updateItems(mainMenuConditions())
    }

    pendingStartController = PendingStartController(
        gameSession = gameSession,
        dialogSceneHost = dialogSceneHost,
        refreshMainMenu = ::refreshMainMenu,
        navigateTo = { screen -> navigator.navigateTo(screen) },
    )

    val gameLaunchController = GameLaunchController(
        gameSession = gameSession,
        battleRoomController = battleRoomController,
        warmupController = warmupController,
        pendingStartController = pendingStartController,
        dialogSceneHost = dialogSceneHost,
        viewport = ::rwGameViewport,
        currentScreen = { navigator.current },
        navigateTo = { screen -> navigator.navigateTo(screen) },
    )

    updateController = UpdateController(
        appMetadata = appMetadata,
        checkLatestRelease = updateRepository::checkLatestRelease,
        loadingDialogSceneHost = loadingDialogSceneHost,
        dialogSceneHost = dialogSceneHost,
        openLink = dialogController::openLink,
    )
    val multiplayerLobbyController = MultiplayerLobbyController(
        sceneHost = multiplayerSceneHost,
    )
    val inGameDialogController = InGameDialogController(
        gameSession = gameSession,
        dialogSceneHost = dialogSceneHost,
        loadingDialogSceneHost = loadingDialogSceneHost,
        currentScreen = { navigator.current },
        viewport = ::rwGameViewport,
        showUnavailableDialog = dialogController::showUnavailable
    )

    val mapController = MapController(
        gameSession = gameSession,
        storage = { platformBridge?.storage },
        viewport = ::rwGameViewport,
        pendingStartMapPath = pendingStartController::currentMapPath,
        setPendingStart = pendingStartController::set,
        navigateToInGame = { navigator.navigateTo(AppScreen.InGame) },
        currentBattleRoomSnapshotForJoin = battleRoomController::currentSnapshot,
        showUnavailableDialog = dialogController::showUnavailable,
        showDialogOverGame = inGameDialogController::showDialogOverGame,
        hideDialog = dialogSceneHost::hide,
    )

    val battleRoomJoinController = BattleRoomJoinController(
        gameSession = gameSession,
        loadingDialogSceneHost = loadingDialogSceneHost,
        onStarted = battleRoomController::markJoinedRoomStarted,
        onConnected = { snapshot ->
            if (battleRoomController.updateConnectedRoom(snapshot)) navigator.navigateTo(AppScreen.BattleRoom)
        },
        onFailed = dialogController::showUnavailable,
    )
    val multiplayerConnectionController = MultiplayerConnectionController(
        gameSession = gameSession,
        lobbyController = multiplayerLobbyController,
        battleRoomJoinController = battleRoomJoinController,
        dialogSceneHost = dialogSceneHost,
        multiplayerSceneHost = multiplayerSceneHost,
        selectHostMap = battleRoomController::selectedOrDefaultMap,
        onHostPreparing = battleRoomController::prepareHostRoom,
        updateBattleRoomFromNetwork = battleRoomController::updateFromNetwork,
        navigateToBattleRoom = { navigator.navigateTo(AppScreen.BattleRoom) },
        showUnavailableDialog = dialogController::showUnavailable,
    )
    multiplayerConnectionController.refreshLastJoinState()
    val battleRoomAdminController = BattleRoomAdminController(
        currentRoomRevision = { battleRoomSceneHost.snapshot().revision },
        gameSession = gameSession,
        dialogSceneHost = dialogSceneHost,
        updateBattleRoomFromNetwork = battleRoomController::updateFromNetwork,
        showUnavailableDialog = dialogController::showUnavailable,
    )
    val battleRoomLaunchController = BattleRoomLaunchController(
        gameSession = gameSession,
        storage = { platformBridge?.storage },
        viewport = ::rwGameViewport,
        currentScreen = { navigator.current },
        showStartNewGameDialog = gameLaunchController::showStartNewGameDialog,
        enterRwGame = gameLaunchController::enterRwGame,
        clearPendingRwStartState = warmupController::clear,
        clearPendingStartState = pendingStartController::clear,
        setPendingStartState = pendingStartController::set,
        navigateToInGame = { navigator.navigateTo(AppScreen.InGame) },
        showUnavailableDialog = dialogController::showUnavailable,
    )

    val externalGameController = ExternalGameController(
        gameSession = gameSession,
        refreshMainMenu = ::refreshMainMenu,
        navigateTo = { screen -> navigator.navigateTo(screen) },
    )

    gameSession.setInGameMenuCallbacks(object : InGameMenuCallbacks {
        override fun requestSettings() {
            inGameDialogController.requestInGameOverlay { CoreUiEventQueue.requestInGameSettings() }
        }

        override fun requestSave() {
            inGameDialogController.requestInGameOverlay { CoreUiEventQueue.requestInGameSave() }
        }

        override fun requestExportMap() {
            inGameDialogController.requestInGameOverlay { CoreUiEventQueue.requestInGameExportMap() }
        }

        override fun requestMapList() {
            inGameDialogController.requestInGameOverlay { CoreUiEventQueue.requestInGameMapList() }
        }

        override fun shouldShowMapList(): Boolean =
            mapController.canShowMapList()

        override fun requestChat(teamOnly: Boolean) {
            inGameDialogController.requestInGameOverlay { CoreUiEventQueue.requestInGameChat(teamOnly) }
        }

        override fun requestPlayerList() {
            inGameDialogController.requestInGameOverlay { CoreUiEventQueue.requestInGamePlayerList() }
        }

        override fun requestSurrender() {
            inGameDialogController.requestInGameOverlay { CoreUiEventQueue.requestInGameSurrender() }
        }

        override fun requestExit() {
            inGameDialogController.requestInGameOverlay { CoreUiEventQueue.requestInGameExit() }
        }

        override fun requestBattleRoomRefresh() {
            CoreUiEventQueue.requestBattleRoomRefresh()
        }

        override fun requestReturnToBattleRoom() {
            CoreUiEventQueue.requestInGameReturnToBattleRoom()
        }
    })

    val modsController = ModsController(
        modRepository = modRepository,
        gameSession = gameSession,
        sceneHost = modsSceneHost,
        loadingDialogSceneHost = loadingDialogSceneHost,
        dialogSceneHost = dialogSceneHost,
        onModsReloaded = {
            levelSelectViewModelFactory.invalidateCaches()
        },
    )

    val sessionActions = SessionActions(
        gameSession = gameSession,
        warmupController = warmupController,
        pendingStartController = pendingStartController,
        settingsSceneHost = settingsSceneHost,
        settingsRepository = settingsRepository,
        settingsModel = settingsModel,
        modsController = modsController,
        currentScreen = { navigator.current },
        navigateTo = { screen -> navigator.navigateTo(screen) },
        refreshMainMenu = ::refreshMainMenu,
        onQuit = onQuit,
    )
    val gameReadyController = GameReadyController(
        gameLaunchController = gameLaunchController,
        exitRwGameToMainMenu = sessionActions::exitRwGameToMainMenu,
        autoReturnMainMenuAfterGameReady = options.autoReturnMainMenuAfterGameReady,
        autoStartSecondBattleRoom = options.autoStartBattleRoomTwice
    )

    val resourceBrowserController = ResourceBrowserController(
        searchResources = resourceBrowserRepository::searchBlocking,
        downloadResource = resourceBrowserRepository::download,
        sceneHost = resourceBrowserSceneHost,
        loadingDialogSceneHost = loadingDialogSceneHost,
        dialogSceneHost = dialogSceneHost,
        showUnavailableDialog = dialogController::showUnavailable,
        onDownloaded = { type ->
            when (type) {
                ResourceBrowserType.Mod -> {
                    modsController.reloadAvailableAndRefresh()
                    // A downloaded mod may bundle maps; drop cached built-in lists so they appear.
                    levelSelectViewModelFactory.invalidateCaches()
                }

                ResourceBrowserType.Map -> {
                    levelSelectSceneHost.updateMaps(LevelSelectMode.CustomMaps)
                }
            }
        },
    )

    val screenLifecycleController = ScreenLifecycleController(
        screenPresenter = screenPresenter,
        externalFrame = { lastExternalGameFrame },
        actions = actions,
        battleRoomController = battleRoomController,
        battleRoomLaunchController = battleRoomLaunchController,
        replaySelectSceneHost = replaySelectSceneHost,
        multiplayerLobbyController = multiplayerLobbyController,
        modsController = modsController,
        resourceBrowserController = resourceBrowserController,
        settingsRepository = settingsRepository,
        settingsModel = settingsModel,
        refreshMainMenu = ::refreshMainMenu,
        autoStartSinglePlayerFromUi = options.autoStartSinglePlayerFromUi,
        autoStartBattleRoom = options.autoStartBattleRoom,
    )
    navigator = ScreenNavigator(
        initialScreen = AppScreen.Loading,
        onScreenChanged = screenLifecycleController::onScreenChanged,
    )
    val onBack: () -> Unit = {
        when (backActionForScreen(navigator.current, gameSession.usesFrameCommandRendering)) {
            BackNavigationAction.Pause -> {
                pauseSceneHost.updateItems(
                    PauseMenuConditions(
                        canSave = true,
                        isMultiplayer = gameSession.isNetworkMultiplayerActive(),
                    )
                )
                navigator.navigateTo(AppScreen.Paused)
            }

            BackNavigationAction.ShowExitDialog -> {
                inGameDialogController.showExitGameDialog(sessionActions::exitRwGameToMainMenu)
            }

            BackNavigationAction.MainMenu -> {
                sessionActions.saveCurrentScreenStateBeforeMainMenu()
                navigator.navigateTo(AppScreen.MainMenu)
            }

            BackNavigationAction.LevelSelect -> actions.levelSelect(LevelSelectAction.Back)
            BackNavigationAction.BattleRoom -> actions.battleRoom(BattleRoomAction.Back)
            BackNavigationAction.InGame -> navigator.navigateTo(AppScreen.InGame)
            BackNavigationAction.CloseModWindow -> sessionActions.closeInGameModWindow()
        }
    }
    val uiController = AppUiController(
        bootstrap = bootstrap,
        currentScreen = { navigator.current },
        mainMenuConditions = ::mainMenuConditions,
        battleBackgroundVisible = { screenPresenter.battleBackgroundVisible },
        onBack = onBack,
        closeModWindow = sessionActions::closeInGameModWindow,
        refreshInGameOverlay = { screenPresenter.updateInGameOverlay(it, navigator.current, lastExternalGameFrame) },
    )
    var frameLoop: OwnedFrameLoop? = null
    val session = AppSession(
        navigator = navigator,
        uiController = uiController,
        onClose = {
            frameLoop?.close()
            scheduler.stop()
            CoreUiEventQueue.setOverlayRequestHandler(null)
        },
        onQuit = onQuit,
        onBack = onBack,
    )
    StartupController(
        battleRoomController = battleRoomController,
        warmupController = warmupController,
    ).start(startupTargetScreen)

    ActionRouter(
        actions = actions,
        navigator = navigator,
        platformBridge = platformBridge,
        settingsRepository = settingsRepository,
        settingsModel = settingsModel,
        refreshScreen = { screenPresenter.apply(navigator.current, lastExternalGameFrame) },
        levelSelectSceneHost = levelSelectSceneHost,
        battleRoomController = battleRoomController,
        multiplayerLobbyController = multiplayerLobbyController,
        multiplayerConnectionController = multiplayerConnectionController,
        battleRoomAdminController = battleRoomAdminController,
        battleRoomLaunchController = battleRoomLaunchController,
        modsController = modsController,
        resourceBrowserController = resourceBrowserController,
        resumeRwGame = gameLaunchController::resumeRwGame,
        enterRwGame = gameLaunchController::enterRwGame,
        enterReplay = gameLaunchController::enterReplay,
        enterSavedGame = gameLaunchController::enterSavedGame,
        quitApp = sessionActions::quitApp,
        showAboutDialog = dialogController::showAboutDialog,
        clearPendingStartState = pendingStartController::clear,
        openInGameSettings = sessionActions::openInGameSettings,
        showSaveGameDialog = inGameDialogController::showSaveGameDialog,
        showExitGameDialog = inGameDialogController::showExitGameDialog,
        showInGameChatDialog = inGameDialogController::showInGameChatDialog,
        showMultiplayerPlayerList = inGameDialogController::showInGamePlayerListDialog,
        requestInGameSurrender = sessionActions::requestInGameSurrender,
        exitRwGameToMainMenu = sessionActions::exitRwGameToMainMenu,
        showUnavailableDialog = dialogController::showUnavailable,
    ).install()

    screenPresenter.apply(navigator.current, lastExternalGameFrame)

    frameLoop = FrameLoopInstaller(
        viewportProvider = ::rwGameViewport,
        scheduler = scheduler,
        onFrame = { uiController.refresh() },
        gameSession = gameSession,
        screenPresenter = screenPresenter,
        warmupController = warmupController,
        presenter = presenter,
        currentScreen = { navigator.current },
        lastExternalFrame = { lastExternalGameFrame },
        setLastExternalFrame = { frame -> lastExternalGameFrame = frame },
        multiplayerLobbyController = multiplayerLobbyController,
        battleRoomController = battleRoomController,
        battleRoomLaunchController = battleRoomLaunchController,
        resourceBrowserController = resourceBrowserController,
        inGameDialogController = inGameDialogController,
        mapController = mapController,
        sessionActions = sessionActions,
        updateController = updateController,
        battleRoomJoinController = battleRoomJoinController,
        pendingStartController = pendingStartController,
        externalGameController = externalGameController,
        modsController = modsController,
        gameReadyController = gameReadyController,
        onBattleRoomClosed = { reason, message ->
            battleRoomJoinController.handleBattleRoomClosed()
            BattleRoomUiBridge.startGamePending = false
            val showingRoom = isBattleRoomOwnedScreen(navigator.current, battleRoomController.isSelectingMapForBattleRoom)
            val returnScreen = battleRoomController.closeRoom()
            if (showingRoom) navigator.navigateTo(returnScreen)
            val text = listOfNotNull(reason, message).joinToString("\n").ifBlank { null }
            text?.let { dialogController.showUnavailable(it) }
        },
    ).install()

    StartupFinalizer(
        bootstrap = bootstrap,
        currentScreen = { navigator.current },
        multiplayerLobbyController = multiplayerLobbyController,
        resourceBrowserController = resourceBrowserController,
    ).finish()
    options.joinServer?.let { address ->
        multiplayerConnectionController.joinOriginalServer(address, roomLabel = address)
    }
    uiController.refresh(force = true)
    return session
}
