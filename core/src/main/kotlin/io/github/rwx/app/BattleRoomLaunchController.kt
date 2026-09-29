package io.github.rwx.app

import com.corrodinggames.rts.gameFramework.GameEngine
import io.github.rwx.PlatformStorage
import io.github.rwx.i18n.I18n
import io.github.rwx.logger
import io.github.rwx.map.LinkedMapAvailability
import io.github.rwx.map.MapLinkResolver
import io.github.rwx.p2p.FeatureIds
import io.github.rwx.p2p.MapFeatureDetector
import io.github.rwx.p2p.MultiMapCoordinator
import io.github.rwx.p2p.P2PLobbyService
import io.github.rwx.render.frame.GameViewport
import io.github.rwx.session.BattleRoomLaunchConfig
import io.github.rwx.session.BattleRoomSnapshot
import io.github.rwx.session.GameSession
import io.github.rwx.session.hasActiveStartedGameConnection
import io.github.rwx.ui.AppScreen
import io.github.rwx.ui.BattleRoomUiBridge

internal class BattleRoomLaunchController(
    private val gameSession: GameSession,
    private val storage: () -> PlatformStorage?,
    private val viewport: () -> GameViewport,
    private val currentScreen: () -> AppScreen,
    private val enterRwGame: (Boolean, BattleRoomLaunchConfig?) -> Unit,
    private val clearPendingRwStartState: () -> Unit,
    private val clearPendingStartState: () -> Unit,
    private val setPendingStartState: (String, AppScreen) -> Unit,
    private val navigateToInGame: () -> Unit,
    private val showUnavailableDialog: (String) -> Unit,
) {
    fun startBattleRoomGame() {
        if (gameSession.currentBattleRoom(refreshNetworkStatus = false)?.isHost != true) {
            showUnavailableDialog("Only the current room host can start the game")
            return
        }
        val launchConfig = gameSession.loadState.pendingRendererBattleRoomConfig
        currentOriginalMultiplayerRwxMapBlockMessage()?.let { message ->
            showUnavailableDialog(message)
            return
        }
        currentLinkedMapUnavailableMessage()?.let { message ->
            showUnavailableDialog(message)
            return
        }
        P2PLobbyService.getInstance().currentMissingRequiredFeatureSummary()?.let { message ->
            showUnavailableDialog(message)
            return
        }
        val snapshot = gameSession.currentBattleRoom()
        // A live host room (network multiplayer OR a live local skirmish/sandbox room started at
        // entry) starts the game in place via startBattleRoomGame on the already-live server, so the
        // host's live spawn/team edits and player list are preserved. Only when there is no live
        // room do we fall back to enterRwGame, which builds the room from the static launch config.
        if (shouldStartLiveBattleRoomInPlace(
                hasLaunchConfig = launchConfig != null,
                isHost = snapshot?.isHost == true,
                isNetworkMultiplayer = snapshot?.isNetworkMultiplayer == true,
                isBattleRoomLive = gameSession.isBattleRoomLive(),
            ) && snapshot != null
        ) {
            if (!prepareP2PMultiMapAssignmentsForStart(snapshot)) {
                return
            }
            runCatching {
                check(gameSession.startBattleRoom()) { "Game session rejected start request" }
                if (gameSession.usesFrameCommandRendering) {
                    BattleRoomUiBridge.setupGame()
                }
                enterStartedBattleRoomGame()
            }.onFailure { error ->
                logger.warn(error) { "Unable to start RW battle room game" }
                showUnavailableDialog(
                    I18n.battleroom.unableToStartGame(error.message ?: error.javaClass.simpleName)
                )
            }
            return
        }
        enterRwGame(true, launchConfig)
    }

    fun handleBattleRoomGameStarted() {
        val networkGameStarted =
            GameEngine.getInstance()?.networkEngine?.hasActiveStartedGameConnection() == true
        when (battleRoomGameStartedAction(
            currentScreen = currentScreen(),
            usesFrameCommandRendering = gameSession.usesFrameCommandRendering,
            inProcessNetworkGameStarted = networkGameStarted,
        )) {
            BattleRoomGameStartedAction.Ignore -> {
                if (currentScreen() == AppScreen.InGame) {
                    BattleRoomUiBridge.startGamePending = false
                } else if (gameSession.currentBattleRoom()?.isNetworkMultiplayer == true) {
                    // As in the original battleroom, hold the start event until the UI can act on
                    // it (the network thread may fire it before gameHasBeenStarted is visible or
                    // while another screen is open); FrameDriver replays it every frame.
                    if (!BattleRoomUiBridge.startGamePending) {
                        logger.info { "Deferring battle room game-start callback until the UI is ready" }
                    }
                    BattleRoomUiBridge.startGamePending = true
                } else {
                    BattleRoomUiBridge.startGamePending = false
                    logger.warn {
                        "Ignoring battle room game-start callback without a started in-process network game"
                    }
                }
            }

            BattleRoomGameStartedAction.ShowExternalGame -> {
                BattleRoomUiBridge.startGamePending = false
                enterStartedBattleRoomGame()
            }

            BattleRoomGameStartedAction.PrepareFrameCommandGame -> {
                BattleRoomUiBridge.startGamePending = false
                runCatching {
                    BattleRoomUiBridge.setupGame()
                    enterStartedBattleRoomGame()
                }.onFailure { error ->
                    logger.warn(error) { "Unable to load started RW battle room game" }
                    showUnavailableDialog(
                        I18n.battleroom.unableToStartGame(error.message ?: error.javaClass.simpleName)
                    )
                }
            }
        }
    }

    /** Replays a deferred network game-start event once per frame until the UI can handle it. */
    fun driveDeferredNetworkGameStart() {
        if (!BattleRoomUiBridge.startGamePending) return
        if (gameSession.currentBattleRoom()?.isNetworkMultiplayer != true) {
            BattleRoomUiBridge.startGamePending = false
            return
        }
        handleBattleRoomGameStarted()
    }

    private fun enterStartedBattleRoomGame() {
        val mapPath = gameSession.currentBattleRoom()?.room?.mapPath?.takeIf { it.isNotBlank() }
        if (mapPath == null) {
            logger.warn { "Unable to enter started battle room without an active map" }
            showUnavailableDialog(
                I18n.battleroom.unableToStartGame("Network game is no longer active")
            )
            return
        }
        if (!gameSession.adoptStartedGameFromEngine(viewport())) {
            logger.warn { "Game session rejected the started battle room game" }
            showUnavailableDialog(
                I18n.battleroom.unableToStartGame("Renderer could not adopt the started game")
            )
            return
        }
        clearPendingRwStartState()
        if (gameSession.isMapLoaded(mapPath)) {
            clearPendingStartState()
        } else {
            setPendingStartState(mapPath, mapStartFailureReturnScreen(currentScreen()))
        }
        navigateToInGame()
    }

    private fun currentOriginalMultiplayerRwxMapBlockMessage(): String? {
        val snapshot = gameSession.currentBattleRoom() ?: return null
        if (snapshot.room.isSavedGame) return null
        if (!snapshot.isHost || !snapshot.isNetworkMultiplayer || snapshot.rwxP2PSession) {
            return null
        }
        val requiredFeatures = MapFeatureDetector.requiredFeaturesForMap(snapshot.room.mapPath)
        if (requiredFeatures.isEmpty()) {
            return null
        }
        return "RWX map modes require RWX P2P multiplayer. Switch to P2P or choose a vanilla-compatible map."
    }

    private fun currentLinkedMapUnavailableMessage(): String? {
        if (gameSession.currentBattleRoom()?.room?.isSavedGame == true) {
            return null
        }
        val mapPath = gameSession.currentBattleRoom()?.room?.mapPath?.takeIf { it.isNotBlank() }
            ?: return null
        if (FeatureIds.MAP_LINKS in MapFeatureDetector.requiredFeaturesForMap(mapPath) &&
            !LinkedMapAvailability.ENABLED
        ) {
            return LinkedMapAvailability.UNAVAILABLE_MESSAGE
        }
        val mapStorage = storage() ?: return null
        val missing = MapLinkResolver.missingLinkedMapIds(mapStorage, mapPath)
        if (missing.isEmpty()) {
            return null
        }
        return I18n.ingame.map.missing(missing.joinToString(", "))
    }

    private fun prepareP2PMultiMapAssignmentsForStart(snapshot: BattleRoomSnapshot): Boolean {
        if (snapshot.room.isSavedGame) return true
        if (!snapshot.isHost || !snapshot.isNetworkMultiplayer || !snapshot.rwxP2PSession) {
            return true
        }
        val mapPath = snapshot.room.mapPath.takeIf { it.isNotBlank() } ?: return true
        val requiredFeatures = MapFeatureDetector.requiredFeaturesForMap(mapPath)
        if (FeatureIds.MAP_LINKS !in requiredFeatures) {
            return true
        }
        if (!LinkedMapAvailability.ENABLED) {
            showUnavailableDialog(LinkedMapAvailability.UNAVAILABLE_MESSAGE)
            return false
        }
        val mapStorage = storage()
        if (mapStorage == null) {
            showUnavailableDialog("RWX multi-map requires local map storage.")
            return false
        }
        val plan = MultiMapCoordinator.createAssignmentPlan(mapStorage, mapPath, snapshot.players)
        if (plan.missingTargetMapIds.isNotEmpty()) {
            showUnavailableDialog(I18n.ingame.map.missing(plan.missingTargetMapIds.joinToString(", ")))
            return false
        }
        if (!plan.hasEnoughPlayers) {
            showUnavailableDialog(
                "RWX multi-map requires at least one non-spectator human player per map instance. " +
                        "Maps: ${plan.requiredPlayerCount}, players: ${plan.availablePlayerCount}."
            )
            return false
        }
        val assignments = plan.assignments
        if (assignments == null) {
            showUnavailableDialog("Unable to prepare RWX multi-map assignments.")
            return false
        }
        P2PLobbyService.getInstance().broadcastMultiMapAssignments(assignments)
        return true
    }
}
