package io.github.rwx.app

import io.github.rwx.render.frame.*
import io.github.rwx.session.GameSession
import io.github.rwx.ui.AppScreen

internal class FrameRenderController(
    private val gameSession: GameSession,
    private val shouldShowBackgroundBattle: (AppScreen) -> Boolean,
    private val warmupController: WarmupController,
    private val presenter: GameFramePresenter,
    private val lastExternalFrame: () -> GameFrame?,
    private val setLastExternalFrame: (GameFrame) -> Unit,
) {
    fun render(
        screen: AppScreen,
        isExternalBattleRoomJoinPending: Boolean,
        canResumeForFrame: Boolean,
        canvasViewport: GameViewport,
        deltaSeconds: Float,
    ) {
        val isRwGameVisible = screen == AppScreen.InGame
        val isBackgroundBattleVisible = !isExternalBattleRoomJoinPending &&
                shouldShowBackgroundBattle(screen)
        val isResumeBackgroundVisible = shouldShowResumeMenuBackground(
            screen = screen,
            canResume = canResumeForFrame,
        )
        val isLastExternalFrameBackgroundVisible = supportsBackgroundBattle(screen) &&
                !gameSession.usesFrameCommandRendering &&
                lastExternalFrame() != null
        val isStartupMenuBackgroundLoading = warmupController.isStartupMenuBackgroundLoading(screen)
        val isRwGameLoading = warmupController.isRwGameLoading(screen)
        val rwCanvasFrame = when {
            isExternalBattleRoomJoinPending -> GameFrame(canvasViewport, emptyList())
            isRwGameVisible -> gameSession.updateFrame(canvasViewport, deltaSeconds)
            isBackgroundBattleVisible -> {
                gameSession.updateFrame(
                    canvasViewport,
                    deltaSeconds,
                    drainVisibleLayerBuffers = true,
                )
            }

            isResumeBackgroundVisible -> {
                resumeBackgroundFrameForSession(gameSession, canvasViewport, deltaSeconds)
            }

            isStartupMenuBackgroundLoading -> warmupController.renderStartupMenuBackgroundLoadingFrame(
                canvasViewport,
                deltaSeconds,
            )

            isRwGameLoading -> warmupController.renderRwGameLoadingFrame(
                canvasViewport,
                deltaSeconds,
            )

            else -> gameSession.currentFrame()
        }
        if (!gameSession.usesFrameCommandRendering && isRwGameVisible && rwCanvasFrame.commands.isNotEmpty()) {
            setLastExternalFrame(rwCanvasFrame)
        }
        warmupController.updateLoadingStatus(screen)
        val externalFrameBackground = if (isLastExternalFrameBackgroundVisible) {
            val currentFrame = gameSession.currentFrame()
            currentFrame.takeIf { it.commands.isNotEmpty() } ?: lastExternalFrame()
        } else {
            null
        }
        val shouldUseRwCanvasFrame = shouldUseRwCanvasFrameForFrame(
            isRwGameVisible = isRwGameVisible,
            isBackgroundBattleVisible = isBackgroundBattleVisible,
            isResumeBackgroundVisible = isResumeBackgroundVisible,
            isRwGameLoading = isRwGameLoading,
            isLastExternalFrameBackgroundVisible = isLastExternalFrameBackgroundVisible,
            usesFrameCommandRendering = gameSession.usesFrameCommandRendering,
        )
        presenter.present(
            if (shouldUseRwCanvasFrame) {
                val frame = externalFrameBackground ?: rwCanvasFrame
                // Native backends own their surface clears. Only a command-rendered frame
                // needs a fallback clear; otherwise it would cover the native game.
                if (gameSession.usesFrameCommandRendering) frame.withDefaultSurfaceClear() else frame
            } else {
                GameFrame(canvasViewport, emptyList())
            },
        )
    }
}

internal fun resumeBackgroundFrameForSession(
    gameSession: GameSession,
    canvasViewport: GameViewport,
    deltaSeconds: Float,
): GameFrame =
    if (gameSession.usesFrameCommandRendering) {
        gameSession.currentFrame()
    } else {
        gameSession.updateFrame(
            canvasViewport,
            deltaSeconds,
            drainVisibleLayerBuffers = true,
        )
    }

internal fun GameFrame.withDefaultSurfaceClear(): GameFrame {
    if (commands.any { it is GameCanvasCommand.Clear && it.renderTarget == null }) {
        return this
    }
    return copy(
        commands = listOf(
            GameCanvasCommand.Clear(
                color = GameCanvasColor(0xff000000.toInt()),
                blendMode = GameCanvasBlendMode.Source,
            ),
        ) + commands,
    )
}
