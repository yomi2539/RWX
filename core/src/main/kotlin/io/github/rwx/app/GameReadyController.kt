package io.github.rwx.app

import io.github.rwx.logger
import io.github.rwx.ui.AppScreen

internal class GameReadyController(
    private val gameLaunchController: GameLaunchController,
    private val exitRwGameToMainMenu: () -> Unit,
    autoReturnMainMenuAfterGameReady: Boolean,
    autoStartSecondBattleRoom: Boolean,
    private val onFirstGameFrameReady: () -> Unit = {},
) {
    private var gameFrameReadyMarkerPrinted = false
    private var renderedGameFrameCount = 0
    private var pendingAutoReturnMainMenuAfterGameReady = autoReturnMainMenuAfterGameReady
    private var pendingAutoStartSecondBattleRoom = autoStartSecondBattleRoom

    fun drive(
        isRenderLoopFrame: Boolean,
        currentScreen: AppScreen,
        isVisibleRwGameReady: Boolean,
    ) {
        if (isRenderLoopFrame && isVisibleRwGameReady) {
            renderedGameFrameCount += 1
            if (!gameFrameReadyMarkerPrinted && renderedGameFrameCount >= 2) {
                gameFrameReadyMarkerPrinted = true
                onFirstGameFrameReady()
                println(RWX_GAME_FRAME_READY_MARKER)
                System.out.flush()
                if (pendingAutoReturnMainMenuAfterGameReady) {
                    pendingAutoReturnMainMenuAfterGameReady = false
                    exitRwGameToMainMenu()
                } else if (pendingAutoStartSecondBattleRoom) {
                    pendingAutoStartSecondBattleRoom = false
                    gameFrameReadyMarkerPrinted = false
                    renderedGameFrameCount = 0
                    gameLaunchController.enterRwGame(startNew = true)
                }
            }
        } else if (currentScreen != AppScreen.InGame) {
            renderedGameFrameCount = 0
        }
    }
}
