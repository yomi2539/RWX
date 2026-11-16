package io.github.rwx.app

import io.github.rwx.geometry.Viewport
import io.github.rwx.session.GameSession
import io.github.rwx.ui.AppScreen
import io.github.rwx.ui.CoreUiEventQueue

internal class FrameLoopInstaller(
    private val viewportProvider: () -> Viewport,
    private val scheduler: FrameScheduler,
    private val clock: FrameClock = SystemNanoFrameClock(),
    private val onFrame: () -> Unit,
    private val gameSession: GameSession,
    private val screenPresenter: ScreenPresenter,
    private val startupPreparationController: StartupPreparationController,
    private val presenter: GameFramePresenter,
    private val currentScreen: () -> AppScreen,
    private val multiplayerLobbyController: MultiplayerLobbyController,
    private val battleRoomController: BattleRoomController,
    private val battleRoomLaunchController: BattleRoomLaunchController,
    private val resourceBrowserController: ResourceBrowserController,
    private val inGameDialogController: InGameDialogController,
    private val mapController: MapController,
    private val mapPathPickerController: MapPathPickerController,
    private val sessionActions: SessionActions,
    private val updateController: UpdateController,
    private val battleRoomJoinController: BattleRoomJoinController,
    private val p2pPreparation: P2PJoinPreparationController,
    private val pendingStartController: PendingStartController,
    private val modsController: ModsController,
    private val gameReadyController: GameReadyController,
    private val onBattleRoomClosed: (reason: String?, message: String?) -> Unit,
) {
    fun install(): OwnedFrameLoop {
        CoreUiEventQueue.setOverlayRequestHandler(inGameDialogController::requestOverlayForQueuedEvent)

        val coreEventDispatcher = CoreEventDispatcher(
            currentScreen = currentScreen,
            multiplayerLobbyController = multiplayerLobbyController,
            battleRoomController = battleRoomController,
            battleRoomLaunchController = battleRoomLaunchController,
            resourceBrowserController = resourceBrowserController,
            inGameDialogController = inGameDialogController,
            mapController = mapController,
            mapPathPickerController = mapPathPickerController,
            openInGameSettings = sessionActions::openInGameSettings,
            requestInGameSurrender = sessionActions::requestInGameSurrender,
            exitRwGameToMainMenu = sessionActions::exitRwGameToMainMenu,
            returnRwGameToBattleRoom = sessionActions::returnRwGameToBattleRoom,
            openInGameModWindow = sessionActions::openInGameModWindow,
            closeInGameModWindow = sessionActions::closeInGameModWindow,
            refreshMenuBackground = {
                screenPresenter.apply(currentScreen())
            },
            onBattleRoomClosed = onBattleRoomClosed,
        )
        val frameRenderController = FrameRenderController(
            gameSession = gameSession,
            screenPresenter = screenPresenter,
            startupPreparationController = startupPreparationController,
            presenter = presenter,
        )
        val frameDriver = FrameDriver(
            gameSession = gameSession,
            currentScreen = currentScreen,
            canvasViewport = { viewportProvider() },
            coreEventDispatcher = coreEventDispatcher,
            updateController = updateController,
            battleRoomController = battleRoomController,
            battleRoomLaunchController = battleRoomLaunchController,
            mapController = mapController,
            battleRoomJoinController = battleRoomJoinController,
            p2pPreparation = p2pPreparation,
            pendingStartController = pendingStartController,
            gameLifecycleController = GameLifecycleController(gameSession, sessionActions::exitRwGameToMainMenu),
            modsController = modsController,
            frameRenderController = frameRenderController,
            gameReadyController = gameReadyController,
        )
        return OwnedFrameLoop(scheduler, clock) { delta ->
            frameDriver.drive(delta, isRenderLoopFrame = true)
            onFrame()
        }.also { it.start() }
    }
}
