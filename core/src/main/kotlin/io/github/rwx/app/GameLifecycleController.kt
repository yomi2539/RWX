package io.github.rwx.app

import io.github.rwx.session.GameSession
import io.github.rwx.ui.AppScreen

internal class GameLifecycleController(
    private val gameSession: GameSession,
    private val onGameClosed: () -> Unit,
) {
    private var gameWasReady = false

    fun drive(screen: AppScreen, isStartingMap: Boolean) {
        if (screen != AppScreen.InGame) {
            gameWasReady = false
            return
        }
        if (isStartingMap || gameSession.isPreparingEngine() || gameSession.isPreparingMap()) return

        val canResume = gameSession.canResume()
        if (canResume) gameWasReady = true
        if (shouldReturnToMainMenuAfterGameClosed(
                isGameVisible = true,
                isStartingMap = false,
                gameWasReady = gameWasReady,
                canResume = canResume,
                mapLoadFailed = gameSession.mapLoadError() != null,
            )
        ) {
            gameWasReady = false
            onGameClosed()
        }
    }
}
