package io.github.rwx.app

import io.github.rwx.geometry.Viewport
import io.github.rwx.session.GameSession
import io.github.rwx.ui.AppScreen

internal class FrameRenderController(
    private val gameSession: GameSession,
    private val screenPresenter: ScreenPresenter,
    private val startupPreparationController: StartupPreparationController,
    private val presenter: GameFramePresenter,
) {
    fun render(
        screen: AppScreen,
        isBattleRoomJoinPending: Boolean,
        canResumeForFrame: Boolean,
        canvasViewport: Viewport,
        deltaSeconds: Float,
    ) {
        screenPresenter.refresh(screen, canResumeForFrame)
        val source = FrameSource.from(
            presentation = screenPresenter.presentation,
            isJoinPending = isBattleRoomJoinPending,
            isStartupMenuBackgroundLoading = startupPreparationController.isStartupMenuBackgroundLoading(screen),
            isEngineLoading = startupPreparationController.isEngineLoading(screen),
        )
        when (source) {
            FrameSource.Game -> gameSession.updateFrame(canvasViewport, deltaSeconds)
            FrameSource.BackgroundBattle, FrameSource.ResumeBackground ->
                gameSession.updateFrame(canvasViewport, deltaSeconds, drainVisibleLayerBuffers = true)

            FrameSource.Loading -> when {
                startupPreparationController.isStartupMenuBackgroundLoading(screen) ->
                    startupPreparationController.renderStartupMenuBackgroundLoadingFrame(canvasViewport, deltaSeconds)
                startupPreparationController.isEngineLoading(screen) ->
                    startupPreparationController.renderEngineLoadingFrame(canvasViewport)
                else -> Unit
            }
            FrameSource.Empty -> Unit
        }
        startupPreparationController.updateLoadingStatus(screen)
        presenter.present()
    }
}
