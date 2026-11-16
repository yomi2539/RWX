package io.github.rwx.app

import com.corrodinggames.rts.gameFramework.GameEngine
import io.github.rwx.geometry.Viewport
import io.github.rwx.p2p.P2PLobbyService
import io.github.rwx.session.GameSession
import io.github.rwx.ui.AppScreen

internal class FrameDriver(
    private val gameSession: GameSession,
    private val currentScreen: () -> AppScreen,
    private val canvasViewport: () -> Viewport,
    private val coreEventDispatcher: CoreEventDispatcher,
    private val updateController: UpdateController,
    private val battleRoomController: BattleRoomController,
    private val battleRoomLaunchController: BattleRoomLaunchController,
    private val mapController: MapController,
    private val battleRoomJoinController: BattleRoomJoinController,
    private val p2pPreparation: P2PJoinPreparationController,
    private val pendingStartController: PendingStartController,
    private val gameLifecycleController: GameLifecycleController,
    private val modsController: ModsController,
    private val frameRenderController: FrameRenderController,
    private val gameReadyController: GameReadyController,
) {
    private var nextBattleRoomNetworkPollMillis: Long = 0L

    fun drive(deltaSeconds: Float, isRenderLoopFrame: Boolean = false) {
        p2pPreparation.drive()
        if (modsController.driveReload()) {
            return
        }
        battleRoomLaunchController.driveLinkedMapSession()
        coreEventDispatcher.drain(isRenderLoopFrame)
        battleRoomLaunchController.driveDeferredNetworkGameStart()
        updateController.maybeRequestAutomatic(currentScreen() == AppScreen.MainMenu)
        updateController.drive()
        driveBattleRoomNetworkPolling(isRenderLoopFrame)
        driveInGameMapTransfers()
        battleRoomJoinController.drive()
        pendingStartController.drive {
            mapController.applyPendingPortalTransfers()
        }
        if (currentScreen() == AppScreen.InGame) {
            mapController.applyPendingPortalTransfers()
        }
        gameLifecycleController.drive(
            screen = currentScreen(),
            isStartingMap = pendingStartController.isPending ||
                    battleRoomJoinController.isPending ||
                    P2PLobbyService.getInstance().isLinkedMapTransition(),
        )
        val isExternalBattleRoomJoinPending = battleRoomJoinController.isPending
        val canResumeForFrame = !isExternalBattleRoomJoinPending && gameSession.canResume()
        frameRenderController.render(
            screen = currentScreen(),
            isBattleRoomJoinPending = isExternalBattleRoomJoinPending,
            canResumeForFrame = canResumeForFrame,
            canvasViewport = canvasViewport(),
            deltaSeconds = deltaSeconds,
        )
        driveMusicOutsideRwFrame(currentScreen(), deltaSeconds)
        val isVisibleRwGameReady = currentScreen() == AppScreen.InGame &&
                !isExternalBattleRoomJoinPending &&
                gameSession.isReadyForDisplay()
        gameReadyController.drive(
            isRenderLoopFrame = isRenderLoopFrame,
            currentScreen = currentScreen(),
            isVisibleRwGameReady = isVisibleRwGameReady,
        )
    }

    private fun driveBattleRoomNetworkPolling(isRenderLoopFrame: Boolean) {
        val nowMillis = System.currentTimeMillis()
        val shouldPoll = shouldPollConnectedBattleRoomNetwork(
            isRenderLoopFrame = isRenderLoopFrame,
            screen = currentScreen(),
            nowMillis = nowMillis,
            nextPollMillis = nextBattleRoomNetworkPollMillis,
        )
        if (!shouldPoll) return

        nextBattleRoomNetworkPollMillis = nowMillis + BATTLE_ROOM_NETWORK_POLL_INTERVAL_MILLIS
        battleRoomController.updateFromNetwork(refreshNetworkStatus = false)
    }

    private fun driveInGameMapTransfers() {
        if (currentScreen() != AppScreen.InGame) return

        mapController.ensureP2PAssignedMapLoaded()
        mapController.drainP2PPortalTransfers()
    }

    private fun driveMusicOutsideRwFrame(screen: AppScreen, deltaSeconds: Float) {
        if (screen == AppScreen.InGame) return
        if (gameSession.isMenuBackgroundActive()) return
        val musicManager = GameEngine.getInstance()?.musicManager ?: return
        musicManager.update((deltaSeconds * 60f).coerceIn(0f, 3f))
    }
}

private const val BATTLE_ROOM_NETWORK_POLL_INTERVAL_MILLIS = 500L

internal fun shouldPollConnectedBattleRoomNetwork(
    isRenderLoopFrame: Boolean,
    screen: AppScreen,
    nowMillis: Long,
    nextPollMillis: Long,
): Boolean =
    isRenderLoopFrame &&
            screen == AppScreen.BattleRoom &&
            nowMillis >= nextPollMillis
