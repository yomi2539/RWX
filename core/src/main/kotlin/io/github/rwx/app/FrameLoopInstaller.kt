package io.github.rwx.app

import io.github.rwx.render.frame.GameFrame
import io.github.rwx.render.frame.GameViewport
import io.github.rwx.session.GameSession
import io.github.rwx.ui.AppScreen
import io.github.rwx.ui.CoreUiEventQueue

internal class FrameLoopInstaller(
    private val viewportProvider: () -> GameViewport,
    private val scheduler: FrameScheduler,
    private val clock: FrameClock = SystemNanoFrameClock(),
    private val onFrame: () -> Unit,
    private val gameSession: GameSession,
    private val screenPresenter: ScreenPresenter,
    private val warmupController: WarmupController,
    private val presenter: GameFramePresenter,
    private val currentScreen: () -> AppScreen,
    private val lastExternalFrame: () -> GameFrame?,
    private val setLastExternalFrame: (GameFrame) -> Unit,
    private val multiplayerLobbyController: MultiplayerLobbyController,
    private val battleRoomController: BattleRoomController,
    private val battleRoomLaunchController: BattleRoomLaunchController,
    private val resourceBrowserController: ResourceBrowserController,
    private val inGameDialogController: InGameDialogController,
    private val mapController: MapController,
    private val sessionActions: SessionActions,
    private val updateController: UpdateController,
    private val battleRoomJoinController: BattleRoomJoinController,
    private val pendingStartController: PendingStartController,
    private val externalGameController: ExternalGameController,
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
            openInGameSettings = sessionActions::openInGameSettings,
            requestInGameSurrender = sessionActions::requestInGameSurrender,
            exitRwGameToMainMenu = sessionActions::exitRwGameToMainMenu,
            returnRwGameToBattleRoom = sessionActions::returnRwGameToBattleRoom,
            openInGameModWindow = sessionActions::openInGameModWindow,
            closeInGameModWindow = sessionActions::closeInGameModWindow,
            refreshMenuBackground = {
                screenPresenter.apply(currentScreen(), lastExternalFrame())
            },
            onBattleRoomClosed = onBattleRoomClosed,
        )
        val frameRenderController = FrameRenderController(
            gameSession = gameSession,
            shouldShowBackgroundBattle = screenPresenter::shouldShowBackgroundBattle,
            warmupController = warmupController,
            presenter = presenter,
            lastExternalFrame = lastExternalFrame,
            setLastExternalFrame = setLastExternalFrame,
        )
        val frameDriver = FrameDriver(
            gameSession = gameSession,
            currentScreen = currentScreen,
            canvasViewport = { viewportProvider().resolved() },
            coreEventDispatcher = coreEventDispatcher,
            updateController = updateController,
            battleRoomController = battleRoomController,
            battleRoomLaunchController = battleRoomLaunchController,
            mapController = mapController,
            battleRoomJoinController = battleRoomJoinController,
            pendingStartController = pendingStartController,
            externalGameController = externalGameController,
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
