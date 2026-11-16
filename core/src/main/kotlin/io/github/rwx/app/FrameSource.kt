package io.github.rwx.app

internal sealed interface FrameSource {
    data object Game : FrameSource
    data object BackgroundBattle : FrameSource
    data object ResumeBackground : FrameSource
    data object Loading : FrameSource
    data object Empty : FrameSource

    companion object {
        fun from(
            presentation: ScreenPresentation,
            isJoinPending: Boolean,
            isStartupMenuBackgroundLoading: Boolean,
            isEngineLoading: Boolean,
        ): FrameSource = when {
            isJoinPending -> Empty
            isStartupMenuBackgroundLoading || isEngineLoading -> Loading
            else -> when (presentation.gameSurface) {
                GameSurface.InGame -> Game
                GameSurface.MenuBackground, GameSurface.PreparingMenuBackground -> BackgroundBattle
                GameSurface.ResumeBackground -> ResumeBackground
                GameSurface.Hidden -> Empty
            }
        }
    }
}
