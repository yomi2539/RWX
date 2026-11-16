package io.github.rwx.app

import io.github.rwx.geometry.Viewport
import io.github.rwx.logger
import io.github.rwx.p2p.P2PLobbyService
import io.github.rwx.ui.*
import io.github.rwx.ui.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext

const val RWX_GAME_FRAME_READY_MARKER: String = "RWX_GAME_FRAME_READY"

fun launchOnIO(name: String?=null,
               start: CoroutineStart = CoroutineStart.DEFAULT,
               exceptionHandler: (CoroutineContext, Throwable) -> Unit = { _, t ->
                   if (t !is CancellationException) logger.error(
                       t
                   )
               },
               block: suspend CoroutineScope.() -> Unit): Job {
    val effectiveName = name ?: "IO-${System.identityHashCode(block)}"
    val ctx =
        SupervisorJob(ApplicationScope.job) + Dispatchers.IO + CoroutineName(effectiveName) + CoroutineExceptionHandler(
            exceptionHandler
        )
    return ApplicationScope.launch(ctx, start, block)
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
    fun dispatch(action: AppAction) = uiController.dispatch(action)
    val uiState: StateFlow<AppUiState> get() = uiController.state
    fun navigateBack() = onBack()
    fun setComposeUiEnabled(enabled: Boolean) = uiController.setComposeEnabled(enabled)

}

fun installApp(
    viewportProvider: () -> Viewport,
    scheduler: FrameScheduler,
    presenter: GameFramePresenter,
    options: AppOptions = AppOptions(),
    onQuit: () -> Unit = {},
): AppSession {
    val bootstrap = createAppBootstrap(options)
    val platformBridge = bootstrap.platformBridge
    val appMetadata = bootstrap.appMetadata
    val gameSession = bootstrap.gameSession
    val modRepository = bootstrap.modRepository
    val resourceBrowserRepository = bootstrap.resourceBrowserRepository
    val updateRepository = bootstrap.updateRepository
    val settingsRepository = bootstrap.settingsRepository
    val actions = bootstrap.actions
    val settingsModel = bootstrap.settingsModel
    val loadingSceneHost = bootstrap.loadingSceneHost
    val mainMenuSceneHost = bootstrap.mainMenuSceneHost
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

    lateinit var p2pPreparation: P2PJoinPreparationController

    val startupTargetScreen = if (options.initialScreen == AppScreen.Loading) {
        AppScreen.MainMenu
    } else {
        options.initialScreen
    }
    lateinit var navigator: ScreenNavigator
    lateinit var pendingStartController: PendingStartController

    fun rwGameViewport(): Viewport = viewportProvider()

    val screenPresenter = ScreenPresenter(
        bootstrap = bootstrap,
        viewport = ::rwGameViewport,
    )

    val startupPreparationController = StartupPreparationController(
        gameSession = gameSession,
        loadingSceneHost = loadingSceneHost,
        navigateTo = { screen -> navigator.navigateTo(screen) },
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
        onRoomClosed = { p2pPreparation.onRoomClosed() },
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
        startupPreparationController = startupPreparationController,
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
        onFailed = { message ->
            if (!p2pPreparation.recordJoinError(message)) dialogController.showUnavailable(message)
        },
    )
    p2pPreparation = P2PJoinPreparationController(
        gameSession,
        { platformBridge?.storage ?: error("Platform storage is unavailable") }, battleRoomJoinController,
        loadingDialogSceneHost, dialogSceneHost, dialogController::showUnavailable
    )
    battleRoomJoinController.onSettled = p2pPreparation::onGameJoinFinished
    val multiplayerConnectionController = MultiplayerConnectionController(
        gameSession = gameSession,
        lobbyController = multiplayerLobbyController,
        battleRoomJoinController = battleRoomJoinController,
        p2pPreparation = p2pPreparation,
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
        enterRwGame = gameLaunchController::enterRwGame,
        clearPendingRwStartState = startupPreparationController::clear,
        clearPendingStartState = pendingStartController::clear,
        setPendingStartState = pendingStartController::set,
        navigateToInGame = { navigator.navigateTo(AppScreen.InGame) },
        showUnavailableDialog = dialogController::showUnavailable,
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
        startupPreparationController = startupPreparationController,
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
    val mapPathPickerController = MapPathPickerController(
        levelSelectSceneHost = levelSelectSceneHost,
        navigateTo = { screen -> navigator.navigateTo(screen) },
        hideDialog = dialogSceneHost::hide,
    )
    val onBack: () -> Unit = {
        when (backActionForScreen(navigator.current)) {
            BackNavigationAction.ShowExitDialog -> {
                inGameDialogController.showExitGameDialog(sessionActions::exitRwGameToMainMenu)
            }

            BackNavigationAction.MainMenu -> {
                sessionActions.saveCurrentScreenStateBeforeMainMenu()
                navigator.navigateTo(AppScreen.MainMenu)
            }

            BackNavigationAction.LevelSelect -> actions.levelSelect(LevelSelectAction.Back)
            BackNavigationAction.BattleRoom -> actions.battleRoom(BattleRoomAction.Back)
            BackNavigationAction.CloseModWindow -> sessionActions.closeInGameModWindow()
        }
    }
    val uiController = AppUiController(
        bootstrap = bootstrap,
        currentScreen = { navigator.current },
        mainMenuConditions = ::mainMenuConditions,
        menuBackgroundVisible = { screenPresenter.presentation.menuBackgroundVisible },
        onBack = onBack,
        closeModWindow = sessionActions::closeInGameModWindow,
        refreshInGameOverlay = { screenPresenter.updateInGameOverlay(it, navigator.current) },
    )
    var frameLoop: OwnedFrameLoop? = null
    val session = AppSession(
        navigator = navigator,
        uiController = uiController,
        onClose = {
            p2pPreparation.close()
            frameLoop?.close()
            scheduler.stop()
            CoreUiEventQueue.setOverlayRequestHandler(null)
        },
        onQuit = onQuit,
        onBack = onBack,
    )
    StartupController(
        battleRoomController = battleRoomController,
        startupPreparationController = startupPreparationController,
    ).start(startupTargetScreen)

    ActionRouter(
        actions = actions,
        navigator = navigator,
        platformBridge = platformBridge,
        settingsRepository = settingsRepository,
        settingsModel = settingsModel,
        refreshScreen = { screenPresenter.apply(navigator.current) },
        levelSelectSceneHost = levelSelectSceneHost,
        mapPathPickerController = mapPathPickerController,
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
        canResume = gameSession::canResume,
        showStartNewGameDialog = gameLaunchController::showStartNewGameDialog,
    ).install()

    screenPresenter.apply(navigator.current)

    frameLoop = FrameLoopInstaller(
        viewportProvider = ::rwGameViewport,
        scheduler = scheduler,
        onFrame = { uiController.refresh() },
        gameSession = gameSession,
        screenPresenter = screenPresenter,
        startupPreparationController = startupPreparationController,
        presenter = presenter,
        currentScreen = { navigator.current },
        multiplayerLobbyController = multiplayerLobbyController,
        battleRoomController = battleRoomController,
        battleRoomLaunchController = battleRoomLaunchController,
        resourceBrowserController = resourceBrowserController,
        inGameDialogController = inGameDialogController,
        mapController = mapController,
        mapPathPickerController = mapPathPickerController,
        sessionActions = sessionActions,
        updateController = updateController,
        battleRoomJoinController = battleRoomJoinController,
        p2pPreparation = p2pPreparation,
        pendingStartController = pendingStartController,
        modsController = modsController,
        gameReadyController = gameReadyController,
        onBattleRoomClosed = closed@{ reason, message ->
            if (P2PLobbyService.getInstance().isLinkedMapTransition()) return@closed
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
        currentScreen = { navigator.current },
        multiplayerLobbyController = multiplayerLobbyController,
        resourceBrowserController = resourceBrowserController,
    ).finish()
    options.joinServer?.let { address ->
        multiplayerConnectionController.joinOriginalServer(address, roomLabel = address)
    }
    options.autoReplay?.let { replayName ->
        gameLaunchController.enterReplay(
            ReplayEntry(
                id = 0,
                fileName = replayName,
                displayName = replayName,
                replayName = replayName,
                modifiedAt = "",
                sizeLabel = "",
            ),
            startNew = true,
        )
    }
    uiController.refresh(force = true)
    return session
}
