package io.github.rwx.session

import com.corrodinggames.rts.game.PlayerTeam
import com.corrodinggames.rts.game.map.TileMap
import com.corrodinggames.rts.game.units.UnitTypeEnum
import com.corrodinggames.rts.gameFramework.*
import com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
import com.corrodinggames.rts.gameFramework.network.*
import com.corrodinggames.rts.gameFramework.network.ChatMessage
import io.github.rwx.app.launchOnIO
import io.github.rwx.applyBattleRoomPlayerConfig
import io.github.rwx.battleRoomPlayerDisplayName
import io.github.rwx.battleRoomPlayerPingLabel
import io.github.rwx.geometry.Point
import io.github.rwx.logger
import io.github.rwx.map.MapMetadata
import io.github.rwx.map.TransferredUnit
import io.github.rwx.net.CoreUiNetworkCallbacks
import io.github.rwx.platform.CoreGameView
import io.github.rwx.render.RenderBackend
import io.github.rwx.render.frame.GameFrame
import io.github.rwx.render.frame.GameViewport
import io.github.rwx.ui.BattleRoomUiBridge
import io.github.rwx.ui.InGameMenuCallbacks
import io.github.rwx.ui.InGameMenuController
import io.github.rwx.ui.model.BattleRoomPlayer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

data class RunningMultiplayerExitInfo(
    val isHost: Boolean,
)

fun NetworkEngine.hasActiveStartedGameConnection(): Boolean =
    gameHasBeenStarted &&
            (singleplayerServer || (
                    networkGameActive &&
                            (isServer || getActiveServerConnection()?.isConnected == true)
                    ))

internal fun battleRoomPlayerTeams(): List<PlayerTeam> = PlayerTeam.getSortedTeams(true)

abstract class GameSession {
    protected val gameLock = Any()
    private val engineStartLock = Any()
    val inGameMenuController = InGameMenuController()

    protected abstract val renderBackend: RenderBackend
    protected open val defaultPreloadViewport: GameViewport = GameViewport(1280, 720)

    @Volatile
    private var rendererProfile: GameSessionRendererProfile = GameSessionRendererProfile()
    open val usesFrameCommandRendering: Boolean
        get() = rendererProfile.usesFrameCommandRendering
    open val canStartNewSessionInPlace: Boolean
        get() = rendererProfile.canStartNewSessionInPlace
    open val usesNativeSurfaceForResumeBackground: Boolean
        get() = rendererProfile.usesNativeSurfaceForResumeBackground
    open val inputCoordinateScale: Float = 1f

    @Volatile
    protected var gameEngine: GameEngine? = null

    /**
     * Every pending-request / load-progress value the session tracks, as one immutable state.
     *
     * All mutation goes through [updateLoadState] — a lock-free CAS loop — so writers on any
     * thread never block, and readers always observe a *consistent combination* of these fields
     * instead of the half-updated views the previous individual `@Volatile` fields allowed.
     * [gameLock] now guards engine access only, never this state.
     */
    data class SessionLoadState(
        val pendingMapPath: String? = null,
        val pendingMapSnapshot: MapSnapshot? = null,
        val pendingRendererBattleRoomConfig: BattleRoomLaunchConfig? = null,
        val activeRendererBattleRoomConfig: BattleRoomLaunchConfig? = null,
        val runningMapPath: String? = null,
        val asyncMapLoadPath: String? = null,
        val asyncMapLoadInProgress: Boolean = false,
        val asyncMapLoadError: Throwable? = null,
        val mapLoadGeneration: Long = 0L,
        val menuBackgroundActive: Boolean = false,
    )

    private val loadStateRef = AtomicReference(SessionLoadState())

    /** Consistent snapshot of the session's pending-request / load-progress state. */
    val loadState: SessionLoadState
        get() = loadStateRef.get()

    /**
     * Atomically applies [transform] and returns the state it installed (or the unchanged current
     * state when the transform was a no-op). [transform] may run more than once under contention,
     * so it must be pure.
     */
    protected fun updateLoadState(transform: (SessionLoadState) -> SessionLoadState): SessionLoadState {
        while (true) {
            val current = loadStateRef.get()
            val next = transform(current)
            if (next == current) return current
            if (loadStateRef.compareAndSet(current, next)) return next
        }
    }

    @Volatile
    protected var lastViewport: GameViewport = GameViewport(0, 0)

    @Volatile
    protected var lastFrame: GameFrame = GameFrame(lastViewport, emptyList())

    private val asyncEnginePreloadInProgress = AtomicBoolean(false)

    @Volatile
    protected var asyncEnginePreloadError: Throwable? = null

    /**
     * True while a mod reload owns the engine (and typically [gameLock]) for seconds. UI-facing
     * reads consult this *before* touching [gameLock] so the frame loop never parks behind the
     * reload — set it before acquiring the lock, clear it after releasing.
     */
    @Volatile
    protected var modReloadInProgress: Boolean = false

    /**
     * True while some background worker holds the engine for a long stretch (map load, engine
     * preload, mod reload). UI-facing reads return a fast fallback instead of blocking on
     * [gameLock] behind that work.
     */
    protected fun isEngineBusyForUiReads(): Boolean =
        modReloadInProgress || asyncEnginePreloadInProgress.get() || loadState.asyncMapLoadInProgress

    // Last successfully built engine-backed UI views. Rebuilds happen on battle-room UI events and
    // the app layer's 500ms network poll; while the engine is busy, readers get these instead of
    // parking on gameLock behind a multi-second load.
    private val publishedBattleRoom = AtomicReference<BattleRoomSnapshot?>(null)
    private val publishedPlayerList = AtomicReference<List<BattleRoomPlayer>>(emptyList())
    private val publishedChatHistory = AtomicReference<List<MultiplayerChatSnapshot>>(emptyList())

    @Volatile
    private var activeBattleRoomJoinConnector: SocketConnector? = null

    @Volatile
    private var cachedBattleRoomRequiredModsSummary: String? = null

    @Volatile
    private var cachedBattleRoomNetworkStatusText: String? = null

    @Volatile
    var latestBattleRoomJoinError: String? = null

    /**
     * Runs [command] against the live engine on whichever thread owns it and returns its result,
     * or null when no engine is available or the owner could not complete it in time.
     *
     * Base behavior executes inline under [gameLock]; that is correct wherever the engine's owner
     * itself serializes on [gameLock] (Android's tick, the background loaders). A backend whose
     * owner thread never takes [gameLock] — the embedded Slick render thread — overrides this to
     * hand the command to that thread, so UI-triggered engine access stops racing the game loop.
     */
    protected open fun <T> runEngineCommand(label: String, command: (GameEngine) -> T?): T? =
        synchronized(gameLock) {
            val engine = activeEngineLocked() ?: return null
            runCatching { command(engine) }
                .onFailure { error -> logger.warn(error) { "Engine command failed: $label" } }
                .getOrNull()
        }

    /** Fire-and-forget form of [runEngineCommand]: the caller never waits for the result. */
    protected open fun postEngineCommand(label: String, command: (GameEngine) -> Unit) {
        runEngineCommand(label, command)
    }

    open fun configureRendererProfile(profile: GameSessionRendererProfile) {
        rendererProfile = profile
    }

    open fun updateFrame(
        viewport: GameViewport,
        deltaSeconds: Float,
        drainVisibleLayerBuffers: Boolean = false,
    ): GameFrame = lastFrame

    open fun currentFrame(): GameFrame = lastFrame

    /** Keep the UI above the native game surface when [uiOverlay] is requested. */
    open fun setGameVisible(
        visible: Boolean,
        viewport: GameViewport,
        uiOverlay: Boolean = false,
        pausedBackground: Boolean = false,
    ) = Unit

    open fun submitPointer(
        screenX: Float,
        screenY: Float,
        isDown: Boolean,
        pointerId: Int,
    ) = Unit

    open fun movePointer(screenX: Float, screenY: Float) = Unit

    open fun submitKey(androidKeyCode: Int, isDown: Boolean) {
        // Input during a long engine hold is meaningless (nothing interactive is running) and must
        // not park the UI thread on gameLock — drop it.
        if (isEngineBusyForUiReads()) return
        postEngineCommand("key state") { engine ->
            engine.setKeyState(androidKeyCode, isDown)
        }
    }

    open fun submitMouseWheel(amount: Int) {
        if (amount == 0) return
        if (isEngineBusyForUiReads()) return
        postEngineCommand("mouse wheel") { engine ->
            engine.queueMouseWheelDelta(amount)
        }
    }

    open fun setInGameMenuCallbacks(callbacks: InGameMenuCallbacks?) {
        inGameMenuController.setCallbacks(callbacks)
    }

    open fun preload(viewport: GameViewport): GameEngine {
        lastViewport = viewport
        val engine = ensureStartedOutsideGameLock(viewport)
        synchronized(gameLock) {
            applyViewport(engine, viewport)
        }
        return engine
    }

    open fun ensureRendererEngine(
        viewport: GameViewport,
        graphicsEngine: GraphicsEngine,
        view: CoreGameView,
        platformCallbacks: PlatformCallbacks? = null,
    ): GameEngine {
        synchronized(gameLock) {
            lastViewport = viewport
            val width = viewport.width.coerceAtLeast(1)
            val height = viewport.height.coerceAtLeast(1)
            GameEngine.screenSize = Point(width, height)
            GameEngine.graphicsEngine = graphicsEngine
            GameEngine.externalGameLoopDriver = true
            val engine = ensureSessionEngineLocked {
                GameEngine.createGameEngine(platformCallbacks)
            }
            engine.renderGraphicsEngine = graphicsEngine
            engine.minimap?.bindGraphicsBackend(graphicsEngine)
            TileMap.layerBufferManager.bindGraphicsBackend(graphicsEngine)
            TileMap.bindGraphicsBackend(graphicsEngine)
            engine.colorizeLogMessage(view, false)
            view.onResume()
            engine.updateWindowResolution(width, height)
            asyncEnginePreloadInProgress.set(false)
            asyncEnginePreloadError = null
            return engine
        }
    }

    open fun prepareEngineAsync(viewport: GameViewport) {
        if (gameEngine != null) {
            return
        }
        if (!asyncEnginePreloadInProgress.compareAndSet(false, true)) {
            return
        }
        asyncEnginePreloadError = null
        lastViewport = viewport
        logger.info { "Preparing engine asynchronously" }
        launchOnIO("${renderBackend.id}-engine-preloader") {
            preloadEngineInBackground(viewport)
        }
    }

    open fun isPreparingEngine(): Boolean = asyncEnginePreloadInProgress.get()

    open fun requestMap(mapPath: String?) {
        updateLoadState {
            it.copy(
                menuBackgroundActive = false,
                pendingRendererBattleRoomConfig = null,
                activeRendererBattleRoomConfig = null,
                pendingMapSnapshot = null,
                pendingMapPath = mapPath?.takeIf { path -> path.isNotBlank() },
            )
        }
    }

    open fun requestMapSnapshot(snapshot: MapSnapshot) {
        updateLoadState {
            it.copy(
                menuBackgroundActive = false,
                pendingRendererBattleRoomConfig = snapshot.battleRoomConfig,
                pendingMapSnapshot = snapshot,
                pendingMapPath = snapshot.mapPath,
            )
        }
    }

    open fun prepareMapAsync(mapPath: String?, viewport: GameViewport) {
        val requestedMapPath = mapPath?.takeIf { it.isNotBlank() } ?: return
        if (isMapLoaded(requestedMapPath)) {
            return
        }
        val generation = tryBeginAsyncLoad(
            requestKey = requestedMapPath,
            viewport = viewport,
            resetLastFrame = { it.runningMapPath != requestedMapPath },
            mutate = { it.copy(pendingRendererBattleRoomConfig = null, activeRendererBattleRoomConfig = null) },
        ) ?: return
        logger.info { "Preparing map asynchronously: $requestedMapPath" }
        launchOnIO("${renderBackend.id}-map-loader") {
            loadMapInBackground(requestedMapPath, viewport, generation)
        }
    }

    open fun prepareSavedGameAsync(saveName: String, viewport: GameViewport) {
        val requestedSaveName = saveName.takeIf { it.isNotBlank() } ?: return
        if (isMapLoaded(requestedSaveName)) {
            return
        }
        val generation = tryBeginAsyncLoad(
            requestKey = requestedSaveName,
            viewport = viewport,
            resetLastFrame = { true },
            mutate = { it.copy(pendingRendererBattleRoomConfig = null, activeRendererBattleRoomConfig = null) },
        ) ?: return
        logger.info { "Preparing saved game asynchronously: $requestedSaveName" }
        launchOnIO("${renderBackend.id}-saved-game-loader") {
            loadSavedGameInBackground(requestedSaveName, viewport, generation)
        }
    }

    open fun prepareMapSnapshotAsync(snapshot: MapSnapshot, viewport: GameViewport) {
        if (isMapLoaded(snapshot.mapPath)) {
            return
        }
        val generation = tryBeginAsyncLoad(
            requestKey = snapshot.mapPath,
            viewport = viewport,
            resetLastFrame = { true },
            mutate = {
                it.copy(
                    pendingMapPath = snapshot.mapPath,
                    pendingMapSnapshot = snapshot,
                    pendingRendererBattleRoomConfig = snapshot.battleRoomConfig,
                )
            },
        ) ?: return
        logger.info { "Preparing map snapshot asynchronously: ${snapshot.mapPath}" }
        launchOnIO("${renderBackend.id}-map-snapshot-loader") {
            loadMapSnapshotInBackground(snapshot, viewport, generation)
        }
    }

    open fun prepareReplayAsync(replayName: String, viewport: GameViewport) {
        val requestedReplayName = replayName.takeIf { it.isNotBlank() } ?: return
        val generation = tryBeginAsyncLoad(
            requestKey = requestedReplayName,
            viewport = viewport,
            resetLastFrame = { true },
        ) ?: return
        logger.info { "Preparing replay asynchronously: $requestedReplayName" }
        launchOnIO("${renderBackend.id}-replay-loader") {
            loadReplayInBackground(requestedReplayName, viewport, generation)
        }
    }

    open fun prepareBattleRoomAsync(config: BattleRoomLaunchConfig, viewport: GameViewport) {
        val requestedMapPath = config.room.mapPath.takeIf { it.isNotBlank() } ?: return
        if (isMapLoaded(requestedMapPath) && loadState.activeRendererBattleRoomConfig == config) {
            return
        }
        val generation = tryBeginAsyncLoad(
            requestKey = requestedMapPath,
            viewport = viewport,
            resetLastFrame = { it.runningMapPath != requestedMapPath },
            mutate = {
                it.copy(
                    pendingMapPath = requestedMapPath,
                    pendingMapSnapshot = null,
                    pendingRendererBattleRoomConfig = config,
                )
            },
        ) ?: return
        logger.info { "Preparing battle room map asynchronously: $requestedMapPath" }
        launchOnIO("${renderBackend.id}-battleroom-map-loader") {
            loadMapInBackground(requestedMapPath, viewport, generation)
        }
    }

    /**
     * Registers a new asynchronous load and returns its [SessionLoadState.mapLoadGeneration]
     * ticket, or null when an identical request is already in flight. [mutate] stages the
     * request-specific pending fields inside the same atomic update.
     *
     * The ticket is what makes a superseded load harmless: the loader compares it against the
     * current generation before it publishes a frame or records an error.
     */
    private fun tryBeginAsyncLoad(
        requestKey: String,
        viewport: GameViewport,
        resetLastFrame: (SessionLoadState) -> Boolean,
        mutate: (SessionLoadState) -> SessionLoadState = { it },
    ): Long? {
        var ticket: Long? = null
        var resetFrame = false
        updateLoadState { current ->
            if (current.asyncMapLoadInProgress && current.asyncMapLoadPath == requestKey) {
                ticket = null
                current
            } else {
                resetFrame = resetLastFrame(current)
                mutate(current).copy(
                    mapLoadGeneration = current.mapLoadGeneration + 1,
                    asyncMapLoadPath = requestKey,
                    asyncMapLoadInProgress = true,
                    asyncMapLoadError = null,
                    menuBackgroundActive = false,
                ).also { ticket = it.mapLoadGeneration }
            }
        }
        val issuedTicket = ticket ?: return null
        if (resetFrame) {
            lastFrame = GameFrame(viewport, emptyList())
        }
        return issuedTicket
    }

    open fun adoptStartedGameFromEngine(viewport: GameViewport): Boolean = false

    open fun prepareLocalBattleRoom(config: BattleRoomLaunchConfig): Boolean {
        updateLoadState {
            it.copy(
                menuBackgroundActive = false,
                pendingMapPath = config.room.mapPath.takeIf { path -> path.isNotBlank() },
                pendingRendererBattleRoomConfig = config,
            )
        }
        disconnectUnstartedNetworking("starting local battleroom")
        lastFrame = GameFrame(lastViewport, emptyList())
        return true
    }


    /** Drops a network room that was opened but never started. */
    private fun disconnectUnstartedNetworking(reason: String) {
        synchronized(gameLock) {
            gameEngine?.networkEngine?.takeIf { it.networkGameActive && !it.gameHasBeenStarted }
                ?.disconnectNetworking(reason)
        }
    }

    open fun enterLocalBattleRoomLive(config: BattleRoomLaunchConfig): Boolean =
        runBattleRoomCommand("enter local battle room") { engine, networkEngine ->
            if (networkEngine.networkGameActive && !networkEngine.gameHasBeenStarted) {
                networkEngine.disconnectNetworking("starting local battleroom")
            }
            initBattleRoomMap(
                engine, config.room.mapPath, force = true,
                savedGame = config.room.isSavedGame
            )
            engine.configureLocalBattleRoom(config, "local")

            // Keep the config around so an in-game map switch / map snapshot can rebuild the room,
            // but do NOT mark the level running: the level loads only at "Start game".
            updateLoadState { it.copy(activeRendererBattleRoomConfig = config) }
            BattleRoomUiBridge.updateUI()
            true
        }

    /** True when a live engine-backed battle room is open (host or client), started or not. */
    open fun isBattleRoomLive(): Boolean {
        if (gameEngine == null && GameEngine.getInstance() == null) return false
        if (isEngineBusyForUiReads()) return publishedBattleRoom.get() != null
        return synchronized(gameLock) { currentBattleRoomFromEngineLocked() != null }
    }

    open fun prepareMenuBackgroundAsync(viewport: GameViewport) = Unit

    open fun isMenuBackgroundActive(): Boolean = loadState.menuBackgroundActive

    open fun hostBattleRoom(
        mapPath: String,
        savedGame: Boolean = false,
        isPublic: Boolean,
        password: String? = null,
        useMods: Boolean = true,
        rwxP2PSession: Boolean = false
    ): Boolean =
        runBattleRoomCommand("host battle room") { engine, networkEngine ->
            if (networkEngine.networkGameActive) {
                networkEngine.disconnectNetworking("starting new host")
            }
            engine.currentMapPath = mapPath
            networkEngine.roomPassword = password
            networkEngine.publishToMasterServer = isPublic
            networkEngine.requireActiveMods = useMods
            initBattleRoomMap(engine, mapPath, force = true, savedGame = savedGame)
            check(networkEngine.startServerHosting(false)) { "Unable to host battle room" }
            networkEngine.p2pSession = rwxP2PSession
            true
        }

    open fun joinBattleRoom(address: String, serverId: String?, p2pSession: Boolean): Boolean {
        val trimmedAddress = address.trim()
        if (trimmedAddress.isBlank()) {
            latestBattleRoomJoinError = "Missing server address"
            return false
        }
        return runBattleRoomCommand("join battle room") { engine, networkEngine ->
            activeBattleRoomJoinConnector?.a()
            activeBattleRoomJoinConnector = null
            latestBattleRoomJoinError = null
            networkEngine.serverAddress = serverId
            initBattleRoomMap(engine, networkEngine.selectedMapPath, force = false)
            lateinit var connector: SocketConnector
            connector = SocketConnector(
                trimmedAddress,
                true,
                Runnable {
                    if (activeBattleRoomJoinConnector !== connector) {
                        return@Runnable
                    }
                    val errorMessage = connector.errorMessage
                    if (errorMessage != null) {
                        latestBattleRoomJoinError = "Connection failed: $errorMessage"
                        activeBattleRoomJoinConnector = null
                        return@Runnable
                    }
                    val socket = connector.connectedSocket
                    if (socket == null) {
                        latestBattleRoomJoinError = "Connection failed: no socket returned"
                        activeBattleRoomJoinConnector = null
                        return@Runnable
                    }
                    runCatching {
                        networkEngine.disconnectNetworking("starting new")
                        networkEngine.a(socket)
                        networkEngine.p2pSession = p2pSession
                        BattleRoomUiBridge.updateUI()
                    }.onFailure { error ->
                        latestBattleRoomJoinError = "Connection failed: ${error.message ?: error.javaClass.simpleName}"
                    }
                    activeBattleRoomJoinConnector = null
                },
            )
            activeBattleRoomJoinConnector = connector
            connector.b()
            true
        }
    }

    val isJoiningBattleRoom: Boolean
        get() = activeBattleRoomJoinConnector != null

    open fun cancelBattleRoomJoin() {
        activeBattleRoomJoinConnector?.a()
        activeBattleRoomJoinConnector = null
    }

    open fun leaveBattleRoom() {
        activeBattleRoomJoinConnector?.a()
        activeBattleRoomJoinConnector = null
        latestBattleRoomJoinError = null
        publishedBattleRoom.set(null)
        publishedPlayerList.set(emptyList())
        publishedChatHistory.set(emptyList())
        updateLoadState {
            it.copy(
                pendingRendererBattleRoomConfig = null,
                pendingMapPath = null,
            )
        }
        synchronized(gameLock) {
            val engine = gameEngine ?: GameEngine.getInstance() ?: return
            engine.networkEngine?.takeIf { it.networkGameActive && !it.gameHasBeenStarted }
                ?.disconnectNetworking("left battleroom")
        }
    }

    open fun currentBattleRoom(refreshNetworkStatus: Boolean = true): BattleRoomSnapshot? {
        if (isEngineBusyForUiReads()) {
            return publishedBattleRoom.get()
        }
        // A live engine room is authoritative: never let a leftover pending draft shadow real teams.
        if (gameEngine != null || GameEngine.getInstance() != null) {
            return synchronized(gameLock) {
                currentBattleRoomFromEngineLocked(refreshNetworkStatus)
            }.also(publishedBattleRoom::set)
        }
        return loadState.pendingRendererBattleRoomConfig?.toBattleRoomSnapshot()
    }

    /**
     * Applies a battle room change to whichever room is real right now.
     *
     * Until the host presses "Start game" the room exists only as a draft config inside
     * [loadState]: a local staging area with no engine or network state, so an edit rewrites that
     * config via one atomic state update. Once the engine owns a live room the same edit becomes a
     * network command instead.
     *
     * @param editDraft returns the replacement state for a draft edit, or null to reject it.
     */
    private inline fun editBattleRoom(
        label: String,
        crossinline editDraft: (SessionLoadState, BattleRoomLaunchConfig) -> SessionLoadState?,
        noinline commandLiveRoom: (GameEngine, NetworkEngine) -> Boolean,
    ): Boolean {
        val hasLiveRoom = synchronized(gameLock) { currentBattleRoomFromEngineLocked() != null }
        if (!hasLiveRoom) {
            var applied = false
            updateLoadState { current ->
                val draft = current.pendingRendererBattleRoomConfig
                val edited = draft?.let { editDraft(current, it) }
                if (edited == null) {
                    applied = false
                    current
                } else {
                    applied = true
                    edited
                }
            }
            return applied
        }
        return runBattleRoomCommand(label, commandLiveRoom)
    }

    open fun setBattleRoomMap(mapPath: String, gameModeType: GameModeType): Boolean {
        val requestedMapPath = mapPath.takeIf { it.isNotBlank() } ?: return false
        val isSavedGame = gameModeType == GameModeType.savedGame
        return editBattleRoom(
            "set battle room map",
            editDraft = { state, draft ->
                val newOptions = draft.room.options.clone().apply {
                    this.gameModeType = gameModeType
                    this.mapPath = if (isSavedGame) requestedMapPath
                    else MapMetadata.getMapNameFromPath(requestedMapPath)
                }
                state.copy(
                    pendingMapPath = requestedMapPath,
                    pendingRendererBattleRoomConfig = draft.copy(
                        room = draft.room.copy(
                            mapPath = requestedMapPath,
                            options = newOptions,
                        ),
                    ),
                )
            },
        ) { engine, networkEngine ->
            initBattleRoomMap(engine, requestedMapPath, force = true, savedGame = isSavedGame)
            networkEngine.getEditableRoomSettings()?.let { settings ->
                settings.gameModeType = gameModeType
                settings.mapPath = if (isSavedGame) requestedMapPath else engine.currentMapFilename
                networkEngine.a(settings)
            }
            BattleRoomUiBridge.updateUI()
            true
        }
    }

    open fun addBattleRoomAi(count: Int): Boolean {
        val addCount = count.coerceAtLeast(0)
        if (addCount == 0) return true
        return editBattleRoom(
            "add battle room AI",
            editDraft = { state, draft ->
                state.copy(pendingRendererBattleRoomConfig = draft.copy(aiPlayerCount = draft.aiPlayerCount + addCount))
            },
        ) { _, networkEngine ->
            repeat(addCount) {
                networkEngine.addAIToGame()
            }
            BattleRoomUiBridge.updateUI()
            true
        }
    }

    open fun setBattleRoomMaxPlayers(maxPlayers: Int): Boolean {
        val target = maxPlayers.coerceIn(BATTLE_ROOM_MIN_MAX_PLAYERS, BATTLE_ROOM_MAX_MAX_PLAYERS)
        // Team slots are an engine-global, so there is no draft equivalent to edit.
        return runBattleRoomCommand("set battle room max players") { _, networkEngine ->
            if (!networkEngine.isServer) return@runBattleRoomCommand false
            if (PlayerTeam.TEAM_NEUTRAL == target) return@runBattleRoomCommand true
            PlayerTeam.setMaxTeamId(target, true)
            BattleRoomUiBridge.updateUI()
            true
        }
    }

    open fun applyBattleRoomTeamLayout(layout: BattleRoomTeamLayout): Boolean =
        editBattleRoom(
            "apply battle room team layout",
            editDraft = { state, draft ->
                state.copy(pendingRendererBattleRoomConfig = draft.copy(teamLayout = layout))
            },
        ) { _, networkEngine ->
            networkEngine.applyBattleRoomTeamLayout(layout)
            BattleRoomUiBridge.updateUI()
            true
        }

    open fun applyBattleRoomOptions(options: GameRoomSettings): Boolean =
        editBattleRoom(
            "apply battle room options",
            editDraft = { state, draft ->
                state.copy(pendingRendererBattleRoomConfig = draft.copy(room = draft.room.copy(options = options)))
            },
        ) { _, networkEngine ->
            networkEngine.roomSettings = options
            val settings = networkEngine.getEditableRoomSettings() ?: options
            networkEngine.a(settings)
            BattleRoomUiBridge.updateUI()
            true
        }

    open fun configureBattleRoomPlayer(
        playerId: String,
        spawn: Int?,
        team: Int?,
        startingUnits: Int? = null,
        aiDifficulty: Int? = null,
    ): Boolean =
        editBattleRoom(
            "configure battle room player",
            // A draft slot carries no per-player overrides yet, so this only reports whether the
            // player exists and returns the state unchanged.
            editDraft = { state, draft ->
                state.takeIf { resolveBattleRoomPlayerSnapshot(playerId, draft.draftBattleRoomPlayers()) != null }
            },
        ) { engine, networkEngine ->
            val player = resolveBattleRoomPlayer(playerId, engine, networkEngine)
                ?: return@editBattleRoom false
            applyBattleRoomPlayerConfig(networkEngine, player, spawn, team, startingUnits, aiDifficulty)
        }

    open fun kickBattleRoomPlayer(playerId: String): Boolean =
        editBattleRoom(
            "kick battle room player",
            // Local draft room: drop the AI slot from the pending config.
            editDraft = { state, draft ->
                val index = draftAiPlayerIndex(playerId)
                if (index == null || index <= 0 || index > draft.aiPlayerCount) {
                    null
                } else {
                    state.copy(pendingRendererBattleRoomConfig = draft.copy(aiPlayerCount = draft.aiPlayerCount - 1))
                }
            },
        ) { engine, networkEngine ->
            if (!(networkEngine.isServer || networkEngine.isProxyController)) {
                return@editBattleRoom false
            }
            val player = resolveBattleRoomPlayer(playerId, engine, networkEngine)
                ?: return@editBattleRoom false
            if (player == networkEngine.localPlayerTeam || player == engine.playerTeam) {
                return@editBattleRoom false
            }
            networkEngine.e(player)
            BattleRoomUiBridge.updateUI()
            true
        }

    open fun startBattleRoom(): Boolean =
        runBattleRoomCommand("start battle room") { _, networkEngine ->
            check(networkEngine.startBattleRoomGame()) { "Unable to start battle room game" }
            true
        }

    open fun sendBattleRoomMessage(message: String): Boolean {
        if (message.isBlank()) return false
        val hasLiveRoom = synchronized(gameLock) { currentBattleRoomFromEngineLocked() != null }
        if (!hasLiveRoom && loadState.pendingRendererBattleRoomConfig != null) {
            return true
        }
        return runBattleRoomCommand("send battle room message") { _, networkEngine ->
            networkEngine.sendChatMessage(message)
            true
        }
    }

    open fun sendBattleRoomSystemMessage(message: String): Boolean {
        return message.isNotBlank() && runBattleRoomCommand("send battle room system message") { _, networkEngine ->
            if (!networkEngine.isServer) return@runBattleRoomCommand false
            networkEngine.j(message)
            true
        }
    }


    open fun sendBattleRoomQuickCommand(command: String): Boolean {
        return command.isNotBlank() && runBattleRoomCommand("send battle room quick command") { _, networkEngine ->
            networkEngine.k(command)
            true
        }
    }

    open fun isPreparingMap(mapPath: String? = null): Boolean {
        val state = loadState
        if (!state.asyncMapLoadInProgress) {
            return false
        }
        return mapPath == null || state.asyncMapLoadPath == mapPath
    }

    open fun mapLoadError(mapPath: String? = null): Throwable? {
        val state = loadState
        val error = state.asyncMapLoadError ?: return null
        return if (mapPath == null || state.asyncMapLoadPath == mapPath) error else null
    }

    open fun loadingStatus(): GameLoadingStatus {
        asyncEnginePreloadError?.let { error ->
            return GameLoadingStatus(
                text = "Loading failed: ${error.message ?: error.javaClass.simpleName}",
                progress = 1.0f,
            )
        }
        val state = loadState
        val engine = gameEngine ?: GameEngine.getInstance()
        val fallbackText = when {
            state.asyncMapLoadInProgress -> "Loading map data"
            else -> "Loading..."
        }
        val text = engine?.getLoadingText()?.takeIf { it.isNotBlank() } ?: fallbackText
        val progress = engine?.getLoadingProgress()?.coerceIn(0.0f, 1.0f)
            ?: when {
                state.asyncMapLoadInProgress -> 0.15f
                asyncEnginePreloadInProgress.get() -> 0.05f
                else -> null
            }
        return GameLoadingStatus(text = text, progress = progress)
    }

    open fun canResume(): Boolean {
        val state = loadState
        if (state.menuBackgroundActive || state.asyncMapLoadInProgress) return false
        // Never park the frame loop behind a long engine hold: no engine or a busy engine simply
        // means "nothing to resume right now".
        val engine = gameEngine ?: return false
        if (isEngineBusyForUiReads()) return false
        return synchronized(gameLock) {
            engine.hasLoadedLevel && !engine.isMenuBackgroundMap && activeRunningMapPath(engine) != null
        }
    }

    open fun runningMapPath(): String? = loadState.runningMapPath

    open fun currentMapDisplayName(): String? {
        if (gameEngine == null || isEngineBusyForUiReads()) return null
        return synchronized(gameLock) {
            activeEngineLocked()
                ?.getCurrentMapName()
                ?.takeIf { it.isNotBlank() }
        }
    }

    open fun activeRendererBattleRoomConfig(): BattleRoomLaunchConfig {
        val state = loadState
        return state.activeRendererBattleRoomConfig ?: state.pendingRendererBattleRoomConfig
        ?: throw IllegalStateException("Unable to load renderer battle room config")
    }

    protected data class RendererPreparationSnapshot(
        val mapPath: String?,
        val mapSnapshot: MapSnapshot?,
        val battleRoomConfig: BattleRoomLaunchConfig?,
    )

    protected fun rendererPreparationSnapshot(): RendererPreparationSnapshot {
        val state = loadState
        return RendererPreparationSnapshot(
            mapPath = state.pendingRendererBattleRoomConfig?.room?.mapPath?.takeIf { it.isNotBlank() }
                ?: state.pendingMapPath?.takeIf { it.isNotBlank() },
            mapSnapshot = state.pendingMapSnapshot,
            battleRoomConfig = state.pendingRendererBattleRoomConfig,
        )
    }

    protected fun beginRendererMapPreparation(
        mapPath: String,
        mapSnapshot: MapSnapshot? = null,
        battleRoomConfig: BattleRoomLaunchConfig? = null,
    ) {
        require(mapSnapshot == null || mapSnapshot.mapPath == mapPath) {
            "Map snapshot path does not match renderer map path"
        }
        require(battleRoomConfig == null || battleRoomConfig.room.mapPath == mapPath) {
            "Battle room path does not match renderer map path"
        }
        updateLoadState {
            it.copy(
                mapLoadGeneration = it.mapLoadGeneration + 1,
                pendingMapPath = mapPath,
                pendingMapSnapshot = mapSnapshot,
                pendingRendererBattleRoomConfig = battleRoomConfig,
                activeRendererBattleRoomConfig = null,
                asyncMapLoadPath = mapPath,
                asyncMapLoadInProgress = true,
                asyncMapLoadError = null,
                runningMapPath = null,
                menuBackgroundActive = false,
            )
        }
    }

    protected fun beginRendererStandalonePreparation(requestPath: String) {
        updateLoadState {
            it.copy(
                mapLoadGeneration = it.mapLoadGeneration + 1,
                pendingMapPath = null,
                pendingMapSnapshot = null,
                pendingRendererBattleRoomConfig = null,
                activeRendererBattleRoomConfig = null,
                asyncMapLoadPath = requestPath,
                asyncMapLoadInProgress = true,
                asyncMapLoadError = null,
                runningMapPath = null,
                menuBackgroundActive = false,
            )
        }
    }

    protected fun beginRendererStartedGamePreparation(mapPath: String) {
        updateLoadState {
            it.copy(
                pendingMapPath = null,
                pendingMapSnapshot = null,
                pendingRendererBattleRoomConfig = null,
                asyncMapLoadPath = mapPath,
                asyncMapLoadInProgress = true,
                asyncMapLoadError = null,
                runningMapPath = null,
                menuBackgroundActive = false,
            )
        }
    }

    protected fun markRendererMenuBackgroundReady() {
        updateLoadState {
            it.copy(
                asyncMapLoadPath = null,
                asyncMapLoadInProgress = false,
                asyncMapLoadError = null,
                menuBackgroundActive = true,
            )
        }
    }

    protected fun markRendererMenuBackgroundError(requestPath: String, error: Throwable) {
        updateLoadState {
            it.copy(
                asyncMapLoadPath = requestPath,
                asyncMapLoadInProgress = false,
                asyncMapLoadError = error,
                menuBackgroundActive = false,
            )
        }
    }

    protected fun markRendererSurfaceStopped() {
        updateLoadState {
            it.copy(menuBackgroundActive = false)
        }
    }

    open fun markRendererMapReady(mapPath: String, viewport: GameViewport) {
        lastViewport = viewport
        updateLoadState { current ->
            val promotesBattleRoomConfig = current.pendingRendererBattleRoomConfig?.room?.mapPath == mapPath
            current.copy(
                pendingMapPath = current.pendingMapPath.takeUnless { it == mapPath },
                pendingMapSnapshot = current.pendingMapSnapshot?.takeUnless { it.mapPath == mapPath },
                pendingRendererBattleRoomConfig = if (promotesBattleRoomConfig) {
                    null
                } else {
                    current.pendingRendererBattleRoomConfig
                },
                activeRendererBattleRoomConfig = if (promotesBattleRoomConfig) {
                    current.pendingRendererBattleRoomConfig
                } else {
                    current.activeRendererBattleRoomConfig
                },
                asyncMapLoadPath = null,
                asyncMapLoadInProgress = false,
                asyncMapLoadError = null,
                menuBackgroundActive = false,
                runningMapPath = mapPath,
            )
        }
    }

    open fun markRendererMapError(mapPath: String, error: Throwable) {
        updateLoadState { current ->
            current.copy(
                pendingMapPath = current.pendingMapPath.takeUnless { it == mapPath },
                pendingRendererBattleRoomConfig = current.pendingRendererBattleRoomConfig
                    ?.takeUnless { it.room.mapPath == mapPath },
                asyncMapLoadPath = mapPath,
                asyncMapLoadInProgress = false,
                asyncMapLoadError = error,
            )
        }
    }

    open fun discardRunningGame() {
        publishedBattleRoom.set(null)
        publishedPlayerList.set(emptyList())
        publishedChatHistory.set(emptyList())
        updateLoadState {
            it.copy(
                mapLoadGeneration = it.mapLoadGeneration + 1,
                pendingMapPath = null,
                pendingMapSnapshot = null,
                pendingRendererBattleRoomConfig = null,
                activeRendererBattleRoomConfig = null,
                asyncMapLoadPath = null,
                asyncMapLoadInProgress = false,
                asyncMapLoadError = null,
                runningMapPath = null,
                menuBackgroundActive = false,
            )
        }
        asyncEnginePreloadInProgress.set(false)
        asyncEnginePreloadError = null
        lastFrame = GameFrame(lastViewport, emptyList())
        clearInputState()
        synchronized(gameLock) {
            gameEngine?.let { engine ->
                runCatching { engine.networkEngine?.disconnectNetworking("exited") }
                engine.hasLoadedLevel = false
                engine.isMenuBackgroundMap = false
                engine.isGameStarted = false
                engine.isStopped = true
                engine.isPaused = true
            }
        }
    }

    open fun requestSaveGame(name: String) {
        val saveName = name.toRwSaveName()
        postEngineCommand("save game") { engine ->
            runCatching {
                engine.gameSaver.saveGame(saveName, false)
                engine.gameUI?.showMediumPriorityMessage("Game saved")
            }.onFailure { error ->
                logger.error(error) { "Failed to save game: $saveName" }
                engine.gameUI?.showHighPriorityMessage("Save failed: ${error.message ?: error.javaClass.simpleName}")
            }
        }
    }

    open fun captureMapSnapshot(): MapSnapshot? =
        runEngineCommand("capture map snapshot") { engine ->
            if (engine.isNetworkGameActive() || !engine.hasLoadedLevel) {
                return@runEngineCommand null
            }
            val mapPath = activeRunningMapPath(engine)?.takeIf { it.isNotBlank() }
                ?: return@runEngineCommand null
            val state = loadState
            val battleRoomConfig = state.activeRendererBattleRoomConfig
                ?: state.pendingRendererBattleRoomConfig
            runCatching {
                val output = GameOutputStream()
                engine.gameSaver.writeSaveToStream(output)
                MapSnapshot(mapPath, output.toByteArray(), battleRoomConfig)
            }.onFailure { error ->
                logger.error(error) { "Failed to capture in-memory RW save for $mapPath" }
                engine.gameUI?.showHighPriorityMessage("Map snapshot failed: ${error.message ?: error.javaClass.simpleName}")
            }.getOrNull()
        }

    open fun requestExportMap(name: String) {
        val exportName = name.toRwExportMapName()
        postEngineCommand("export map") { engine ->
            val sourceMap = engine.currentMapPath?.takeIf { it.isNotBlank() }
                ?: loadState.runningMapPath?.takeIf { it.isNotBlank() }
                ?: return@postEngineCommand
            val exportPath = "/SD/rustedWarfare/maps/$exportName.tmx"
            runCatching {
                engine.tileMap.exportMapToPath(sourceMap, exportPath)
                engine.gameUI?.showMediumPriorityMessage("Map exported: $exportName")
            }.onFailure { error ->
                logger.error(error) { "Failed to export map: $exportPath" }
                engine.gameUI?.showHighPriorityMessage("Export failed: ${error.message ?: error.javaClass.simpleName}")
            }
        }
    }

    open suspend fun requestReloadMods(): Boolean {
        // Flag first, lock second: readers check the flag before touching gameLock, so the frame
        // loop keeps rendering (and the reload dialog stays clickable) for the whole reload.
        modReloadInProgress = true
        try {
            synchronized(gameLock) {
                val engine = gameEngine ?: ensureStarted(lastViewport)
                runCatching {
                    engine.beginLoadingStatus("Loading custom unit data", 12)
                    engine.loadLevel("Loading custom unit data")
                    engine.modManager.saveModSelection()
                    engine.modManager.applyAndSaveMods()
                    engine.markLoadingStatusComplete("Mods reloaded")
                    engine.settingsEngine?.save()
                }.onFailure { error ->
                    logger.error(error) { "Failed to reload mods" }
                    engine.gameUI?.showHighPriorityMessage("Mod reload failed: ${error.message ?: error.javaClass.simpleName}")
                    throw error
                }
            }
        } finally {
            modReloadInProgress = false
        }
        return true
    }

    open fun requestSurrender() {
        postEngineCommand("surrender") { engine ->
            engine.networkEngine?.sendChatMessage("-surrender")
        }
    }

    open fun requestChatMessage(message: String, teamOnly: Boolean = false) {
        val trimmed = message.trim()
        if (trimmed.isBlank()) return
        postEngineCommand("chat message") { engine ->
            engine.networkEngine?.sendChatMessage(if (teamOnly) "-t $trimmed" else trimmed)
        }
    }

    open fun isNetworkMultiplayerActive(): Boolean {
        if (gameEngine == null || isEngineBusyForUiReads()) return false
        return synchronized(gameLock) {
            activeEngineLocked()?.isNetworkGameActive() == true
        }
    }

    open fun multiplayerPlayerList(): List<BattleRoomPlayer> {
        if (isEngineBusyForUiReads()) return publishedPlayerList.get()
        return synchronized(gameLock) {
            val engine = activeEngineLocked() ?: return@synchronized emptyList()
            val networkEngine = engine.networkEngine ?: return@synchronized emptyList()
            if (!engine.isNetworkGameActive()) return@synchronized emptyList()
            battleRoomPlayerTeams().map { team ->
                team.toBattleRoomPlayer(
                    isLocal = team == networkEngine.localPlayerTeam || team == engine.playerTeam,
                )
            }
        }.also(publishedPlayerList::set)
    }

    open fun multiplayerChatHistory(): List<MultiplayerChatSnapshot> {
        if (isEngineBusyForUiReads()) return publishedChatHistory.get()
        return synchronized(gameLock) {
            val engine = activeEngineLocked() ?: return@synchronized emptyList()
            val networkEngine = engine.networkEngine ?: return@synchronized emptyList()
            if (!engine.isNetworkGameActive()) return@synchronized emptyList()
            networkEngine.chatLog.messages.mapNotNull { entry ->
                val message = entry as? ChatMessage ?: return@mapNotNull null
                MultiplayerChatSnapshot(
                    text = message.displayText,
                    teamColorIndex = message.teamColorIndex,
                )
            }
        }.also(publishedChatHistory::set)
    }

    open fun runningMultiplayerExitInfo(): RunningMultiplayerExitInfo? {
        if (gameEngine == null || isEngineBusyForUiReads()) return null
        return synchronized(gameLock) {
            val engine = activeEngineLocked() ?: return@synchronized null
            val networkEngine = engine.networkEngine ?: return@synchronized null
            if (!engine.isNetworkGameActive() || !networkEngine.gameHasBeenStarted) return@synchronized null
            RunningMultiplayerExitInfo(isHost = networkEngine.isServer)
        }
    }

    open fun disconnectRunningMultiplayer() {
        postEngineCommand("disconnect multiplayer") { engine ->
            engine.networkEngine?.disconnectNetworking("exited")
        }
    }

    open fun scheduleReturnToBattleRoom(): Boolean =
        runEngineCommand("schedule return to battle room") { engine ->
            val networkEngine = engine.networkEngine ?: return@runEngineCommand false
            if (!networkEngine.networkGameActive || !networkEngine.gameHasBeenStarted || !networkEngine.isServer) {
                return@runEngineCommand false
            }
            networkEngine.scheduleDefaultReturnToBattleroom()
            engine.gameUI?.isDraggingSelection = false
            true
        } ?: false

    open fun currentMultiplayerPlayerName(): String {
        if (isEngineBusyForUiReads()) {
            return SettingsEngine.getInstance()?.lastNetworkPlayerName ?: "Player"
        }
        return synchronized(gameLock) {
            val engine = activeEngineLocked()
            engine?.networkEngine?.playerName
                ?: engine?.settingsEngine?.lastNetworkPlayerName
                ?: SettingsEngine.getInstance()?.lastNetworkPlayerName
                ?: "Player"
        }
    }

    open fun updateMultiplayerPlayerName(name: String): Boolean {
        val normalizedName = name.trim().replace(' ', '_')
        if (normalizedName.isBlank()) return false
        runEngineCommand("update multiplayer player name") { engine ->
            engine.networkEngine?.playerName = normalizedName
            val settings = engine.settingsEngine ?: SettingsEngine.getInstance() ?: return@runEngineCommand false
            settings.lastNetworkPlayerName = normalizedName
            settings.save()
            true
        }?.let { return it }
        // No engine yet: persist to settings alone, like before.
        val settings = SettingsEngine.getInstance() ?: return false
        settings.lastNetworkPlayerName = normalizedName
        settings.save()
        return true
    }

    open fun injectTransferredUnits(units: List<TransferredUnit>, targetPortalId: String?): Int {
        if (units.isEmpty()) return 0
        return runEngineCommand("inject transferred units") { engine ->
            if (!engine.hasLoadedLevel) return@runEngineCommand 0
            val center = engine.missionEngine?.getMapPortalCenter(targetPortalId)
            val spawnX = center?.getOrNull(0) ?: (engine.viewpointX + engine.halfVisibleWorldWidth)
            val spawnY = center?.getOrNull(1) ?: (engine.viewpointY + engine.halfVisibleWorldHeight)
            var createdCount = 0
            units.forEachIndexed { index, transfer ->
                val unitType = runCatching { UnitTypeEnum.valueOf(transfer.unitTypeId) }.getOrNull()
                    ?: return@forEachIndexed
                val team = PlayerTeam.k(transfer.teamId) ?: return@forEachIndexed
                val unit = unitType.a()
                unit.posX = spawnX + ((index % 4) - 1.5f) * 28.0f
                unit.posY = spawnY + ((index / 4) - 0.5f) * 28.0f
                unit.h(transfer.direction)
                unit.setUnitTeam(team)
                unit.currentHealth = (unit.maxHealth * transfer.healthFraction.coerceIn(0.01f, 1.0f))
                    .coerceIn(1.0f, unit.maxHealth)
                unit.isActive = true
                unit.n()
                PlayerTeam.c(unit)
                GameObject.dL()
                engine.unitSpatialIndex?.a(unit)
                createdCount += 1
            }
            if (createdCount > 0) {
                engine.gameUI?.showMediumPriorityMessage("Transferred $createdCount unit(s)")
            }
            createdCount
        } ?: 0
    }

    open fun isEngineGameLoaded(): Boolean {
        val engine = gameEngine ?: return false
        synchronized(gameLock) {
            return engine.hasLoadedLevel
        }
    }

    open fun isMapLoaded(mapPath: String? = runningMapPath()): Boolean {
        if (loadState.asyncMapLoadInProgress) {
            return false
        }
        // Fast, lock-free exits for the per-frame callers: no engine or an engine owned by a
        // long-running worker cannot have the requested map ready.
        val engine = gameEngine ?: return false
        if (isEngineBusyForUiReads()) return false
        synchronized(gameLock) {
            val activeMapPath = activeRunningMapPath(engine)
            if (mapPath != null && activeMapPath != mapPath) {
                return false
            }
            return engine.hasLoadedLevel
        }
    }

    open fun isReadyForDisplay(mapPath: String? = runningMapPath()): Boolean {
        if (!isMapLoaded(mapPath)) {
            return false
        }
        return lastFrame.commands.isNotEmpty() || !TileMap.layerBufferManager.hasVisiblePendingRedraws()
    }

    abstract fun loadPendingMapNow(): GameFrame

    protected abstract fun ensureStarted(viewport: GameViewport): GameEngine

    protected abstract fun applyViewport(engine: GameEngine, viewport: GameViewport)

    protected open fun clearInputState() = Unit

    protected open fun activeRunningMapPath(engine: GameEngine): String? =
        loadState.runningMapPath
            ?: engine.networkEngine?.selectedMapPath?.takeIf { it.isNotBlank() }
            ?: engine.currentMapPath?.takeIf { it.isNotBlank() }

    protected fun activeEngineLocked(): GameEngine? {
        val sessionEngine = gameEngine
        val singletonEngine = GameEngine.getInstance()
        return when {
            sessionEngine == null -> singletonEngine?.also { gameEngine = it }
            singletonEngine == null || singletonEngine === sessionEngine -> sessionEngine
            else -> {
                logger.warn {
                    "GameSession engine reference differed from GameEngine singleton; adopting singleton"
                }
                gameEngine = singletonEngine
                singletonEngine
            }
        }
    }

    protected fun ensureSessionEngineLocked(createEngine: () -> GameEngine): GameEngine {
        activeEngineLocked()?.let { return it }
        val created = createEngine()
        val singletonEngine = GameEngine.getInstance()
        check(singletonEngine === created) {
            "GameEngine.createGameEngine returned a non-singleton engine"
        }
        gameEngine = created
        return created
    }

    private fun ensureStartedOutsideGameLock(viewport: GameViewport): GameEngine =
        synchronized(engineStartLock) engineStart@{
            synchronized(gameLock) {
                activeEngineLocked()
            }?.let { return@engineStart it }
            ensureStarted(viewport)
        }

    protected fun preloadEngineInBackground(requestedViewport: GameViewport) {
        val startedAt = System.nanoTime()
        runCatching {
            val viewport = requestedViewport.takeIf { it.width > 0 && it.height > 0 } ?: defaultPreloadViewport
            lastViewport = viewport
            val engine = ensureStartedOutsideGameLock(viewport)
            synchronized(gameLock) {
                applyViewport(engine, viewport)
            }
            logger.info { "Prepared engine asynchronously: ${elapsedMs(startedAt)}ms" }
        }.onFailure { error ->
            asyncEnginePreloadError = error
            logger.error(error) { "async engine preload failed" }
        }.also {
            asyncEnginePreloadInProgress.set(false)
        }
    }

    private fun runBattleRoomCommand(
        label: String,
        command: (GameEngine, NetworkEngine) -> Boolean,
    ): Boolean =
        runCatching {
            val viewport = lastViewport.takeIf { it.width > 0 && it.height > 0 } ?: defaultPreloadViewport
            ensureStartedOutsideGameLock(viewport)
            runEngineCommand(label) { engine ->
                val networkEngine = engine.networkEngine ?: return@runEngineCommand false
                prepareActiveBattleRoomEngine(engine)
                command(engine, networkEngine)
            } ?: false
        }.onFailure { error ->
            logger.warn(error) { "Unable to $label" }
        }.getOrDefault(false)

    private fun currentBattleRoomFromEngineLocked(refreshNetworkStatus: Boolean = true): BattleRoomSnapshot? {
        val engine = gameEngine ?: GameEngine.getInstance() ?: return null
        val networkEngine = engine.networkEngine ?: return null
        if (!networkEngine.networkGameActive && !networkEngine.isServer && !networkEngine.isProxyController) return null
        val settings = networkEngine.roomSettings
        val players = battleRoomPlayerTeams().map { team ->
            team.toBattleRoomPlayer(
                isLocal = team == networkEngine.localPlayerTeam || team == engine.playerTeam,
            )
        }
        return BattleRoomSnapshot(
            room = BattleRoomCoreConfig(
                mapPath = networkEngine.selectedMapPath ?: engine.currentMapPath ?: "",
                options = settings,
            ),
            mapDisplayName = settings.mapPath?.let(MapMetadata::getMapName) ?: "Unknown map",
            mapTypeLabel = settings.gameModeType?.a() ?: "Multiplayer",
            players = players,
            isHost = networkEngine.isServer || networkEngine.isProxyController,
            isNetworkMultiplayer = engine.isNetworkGameActive(),
            rwxP2PSession = networkEngine.p2pSession,
            requiredModsSummary = if (refreshNetworkStatus || cachedBattleRoomRequiredModsSummary == null) {
                runCatching { networkEngine.requiredModsSummary }.getOrNull()
                    .also { cachedBattleRoomRequiredModsSummary = it }
            } else {
                cachedBattleRoomRequiredModsSummary
            },
            networkStatusText = if (refreshNetworkStatus || cachedBattleRoomNetworkStatusText == null) {
                runCatching { networkEngine.getPublicIpStatusText() }.getOrNull()
                    .also { cachedBattleRoomNetworkStatusText = it }
            } else {
                cachedBattleRoomNetworkStatusText
            },
            maxPlayers = PlayerTeam.TEAM_NEUTRAL,
        )
    }

    private fun BattleRoomLaunchConfig.toBattleRoomSnapshot(): BattleRoomSnapshot = BattleRoomSnapshot(
        room = BattleRoomCoreConfig(mapPath = room.mapPath, options = room.options),
        mapDisplayName = MapMetadata.getMapName(room.mapPath),
        mapTypeLabel = when {
            sandbox -> "Sandbox"
            room.isSavedGame -> "Saved Game"
            else -> "Skirmish"
        },
        players = draftBattleRoomPlayers(),
        isHost = true
    )

    private fun prepareActiveBattleRoomEngine(engine: GameEngine) {
        updateLoadState {
            it.copy(
                menuBackgroundActive = false,
                pendingRendererBattleRoomConfig = null,
                pendingMapPath = null,
            )
        }
        engine.isStopped = false
        engine.isPaused = false
        installCoreNetworkCallbacksIfMissing(engine)
    }

    private fun installCoreNetworkCallbacksIfMissing(engine: GameEngine) {
        val networkEngine = engine.networkEngine ?: return
        if (networkEngine.callbacks !is CoreUiNetworkCallbacks) {
            networkEngine.callbacks = CoreUiNetworkCallbacks()
        }
    }

    private fun initBattleRoomMap(
        engine: GameEngine,
        mapPath: String?,
        force: Boolean,
        savedGame: Boolean = false,
    ) {
        val networkEngine = engine.networkEngine ?: return
        val resolvedPath = mapPath?.takeIf { it.isNotBlank() }
            ?: networkEngine.selectedMapPath?.takeIf { it.isNotBlank() }
            ?: "maps/skirmish/[z;p10]Crossing Large (10p).tmx"
        engine.currentMapPath = resolvedPath
        networkEngine.selectedMapPath = resolvedPath
        val settings = networkEngine.roomSettings
        if (settings.gameModeType == null || force) {
            settings.gameModeType = if (savedGame) GameModeType.savedGame else engine.getGameModeType()
        }
        if (settings.mapPath == null || force) {
            settings.mapPath = if (savedGame) resolvedPath else engine.currentMapFilename
        }
        if (networkEngine.playerName == null) {
            networkEngine.playerName = engine.settingsEngine.lastNetworkPlayerName ?: "Player"
        }
    }

    private fun resolveBattleRoomPlayer(
        playerId: String,
        engine: GameEngine,
        networkEngine: NetworkEngine,
    ): PlayerTeam? {
        val teams = battleRoomPlayerTeams()
        teams.firstOrNull { it.teamId.toString() == playerId }?.let { return it }
        if (playerId == DRAFT_LOCAL_PLAYER_ID) {
            return networkEngine.localPlayerTeam ?: engine.playerTeam
        }
        val draftIndex = draftAiPlayerIndex(playerId) ?: return null
        return teams.firstOrNull { it.teamId == draftIndex }
            ?: teams.getOrNull(draftIndex)
    }

    private fun PlayerTeam.toBattleRoomPlayer(isLocal: Boolean): BattleRoomPlayer {
        val isSpectator = isSpectatorTeamColor
        val isAi = isTeamSpectator || isTeamControlledByAI
        val teamIndex = teamColorId.takeIf { it >= 0 } ?: -1
        return BattleRoomPlayer(
            id = teamId.toString(),
            name = battleRoomPlayerDisplayName(teamName, isAi, teamId),
            spawnLabel = if (isSpectator) "Spec" else (teamId + 1).toString(),
            teamLabel = if (isSpectator) "-" else teamLabelFor(teamColorId),
            pingLabel = battleRoomPlayerPingLabel(this),
            nameColorIndex = getTeamColorIndex(),
            spawnColorIndex = teamId.takeIf { it >= 0 } ?: -1,
            teamColorIndex = teamIndex,
            isSpectator = isSpectator,
            isReady = isTeamReady,
            isAI = isAi,
            isLocal = isLocal,
            startingUnitsOverride = startingUnitsOverride,
            aiDifficultyOverride = teamAIDifficultyOverride,
        )
    }

    /**
     * Loads a level on the calling thread while holding [gameLock], then publishes its first frame.
     *
     * [generation] is the load's ticket: a newer request bumps [mapLoadGeneration], so a load that
     * finds its generation stale is discarded instead of overwriting the current frame or reporting
     * an error for a request nobody is waiting on any more.
     *
     * @param kind noun used in log lines, e.g. "map", "replay", "map snapshot".
     * @param requestKey what was asked for, logged alongside [kind].
     */
    private fun loadLevelInBackground(
        kind: String,
        requestKey: String,
        requestedViewport: GameViewport,
        generation: Long,
        load: (GameEngine) -> Unit,
    ) {
        var loadMs = 0L
        var firstFrameMs = 0L
        val startedAt = System.nanoTime()
        var loadedCurrentRequest = false
        runCatching {
            synchronized(gameLock) {
                if (generation != loadState.mapLoadGeneration) {
                    return@synchronized
                }
                val viewport = requestedViewport.takeIf { it.width > 0 && it.height > 0 } ?: defaultPreloadViewport
                lastViewport = viewport
                val engine = ensureStarted(viewport)
                applyViewport(engine, viewport)
                val loadStartedAt = System.nanoTime()
                load(engine)
                loadMs = elapsedMs(loadStartedAt)
                val firstFrameStartedAt = System.nanoTime()
                val preparedFrame = prepareFrameAfterBackgroundLoad(engine, viewport)
                firstFrameMs = elapsedMs(firstFrameStartedAt)
                if (generation == loadState.mapLoadGeneration) {
                    lastFrame = preparedFrame
                    loadedCurrentRequest = true
                }
            }
            if (!loadedCurrentRequest) {
                logger.info { "Discarded $kind preparation: $requestKey" }
                return@runCatching
            }
            logger.info {
                "Prepared $kind asynchronously: $requestKey " +
                        "(total=${elapsedMs(startedAt)}ms, load=${loadMs}ms, firstFrame=${firstFrameMs}ms)"
            }
        }.onFailure { error ->
            if (loadState.mapLoadGeneration == generation) {
                updateLoadState { if (it.mapLoadGeneration == generation) it.copy(asyncMapLoadError = error) else it }
                logger.error(error) { " async $kind load failed: $requestKey" }
            } else {
                logger.info { "Ignored stale  $kind load failure for: $requestKey" }
            }
        }.also {
            updateLoadState { if (it.mapLoadGeneration == generation) it.copy(asyncMapLoadInProgress = false) else it }
        }
    }

    private fun loadMapInBackground(mapPath: String, requestedViewport: GameViewport, generation: Long) =
        loadLevelInBackground("map", mapPath, requestedViewport, generation) { engine ->
            loadMap(engine, mapPath)
        }

    private fun loadSavedGameInBackground(
        saveName: String,
        requestedViewport: GameViewport,
        generation: Long,
    ) = loadLevelInBackground("saved game", saveName, requestedViewport, generation) { engine ->
        loadSavedGame(engine, saveName)
    }

    private fun loadReplayInBackground(replayName: String, requestedViewport: GameViewport, generation: Long) =
        loadLevelInBackground("replay", replayName, requestedViewport, generation) { engine ->
            loadReplay(engine, replayName)
        }

    private fun loadMapSnapshotInBackground(
        snapshot: MapSnapshot,
        requestedViewport: GameViewport,
        generation: Long,
    ) = loadLevelInBackground("map snapshot", snapshot.mapPath, requestedViewport, generation) { engine ->
        loadMapSnapshot(engine, snapshot)
    }

    protected open fun prepareFrameAfterBackgroundLoad(
        engine: GameEngine,
        viewport: GameViewport,
    ): GameFrame = GameFrame(viewport, emptyList())

    protected fun loadPendingMap(engine: GameEngine) {
        val staged = loadState
        val mapPath = staged.pendingMapPath ?: return
        val snapshot = staged.pendingMapSnapshot?.takeIf { it.mapPath == mapPath }
        val battleRoomConfig = staged.pendingRendererBattleRoomConfig?.takeIf { it.room.mapPath == mapPath }
        // Consume only the values read above: a request staged concurrently by another thread must
        // survive this clear instead of being wiped along with the one being handled.
        updateLoadState { current ->
            current.copy(
                pendingMapPath = current.pendingMapPath.takeUnless { it == mapPath },
                pendingMapSnapshot = current.pendingMapSnapshot
                    ?.takeUnless { it === staged.pendingMapSnapshot },
                pendingRendererBattleRoomConfig = if (staged.runningMapPath == mapPath && engine.hasLoadedLevel) {
                    current.pendingRendererBattleRoomConfig?.takeUnless { it === staged.pendingRendererBattleRoomConfig }
                } else {
                    current.pendingRendererBattleRoomConfig
                },
            )
        }
        if (staged.runningMapPath == mapPath && engine.hasLoadedLevel) {
            return
        }
        runCatching {
            if (snapshot != null) {
                loadMapSnapshot(engine, snapshot)
            } else if (battleRoomConfig != null) {
                loadBattleRoomMap(engine, battleRoomConfig)
            } else {
                loadMap(engine, mapPath)
            }
        }.onFailure { error ->
            // A successful load clears the draft itself. On failure it must be dropped here too:
            // currentBattleRoom() reads the draft before the engine, so a leaked one would keep
            // showing a room that was never created.
            if (battleRoomConfig != null) {
                updateLoadState { current ->
                    current.copy(
                        pendingRendererBattleRoomConfig = current.pendingRendererBattleRoomConfig
                            ?.takeUnless { it == battleRoomConfig },
                    )
                }
            }
            logger.error(error) { " map load failed: $mapPath" }
        }
    }

    protected open fun loadMap(engine: GameEngine, mapPath: String) {
        updateLoadState {
            it.copy(
                menuBackgroundActive = false,
                activeRendererBattleRoomConfig = null,
                runningMapPath = mapPath,
            )
        }
        engine.isStopped = false
        engine.isPaused = false
        engine.currentMapPath = mapPath
        engine.loadGame(true, GameMode.normal)
    }

    protected open fun loadSavedGame(engine: GameEngine, saveName: String) {
        updateLoadState {
            it.copy(
                menuBackgroundActive = false,
                activeRendererBattleRoomConfig = null,
                runningMapPath = saveName,
            )
        }
        engine.networkEngine?.disconnectNetworking("loading new save")
        engine.isStopped = false
        engine.isPaused = false
        check(engine.gameSaver.loadSaveFile(saveName, false)) {
            "Unable to load saved game: $saveName"
        }
        engine.applyLoadedSinglePlayerFogRules()
    }

    protected open fun loadBattleRoomMap(engine: GameEngine, config: BattleRoomLaunchConfig) {
        updateLoadState {
            it.copy(
                menuBackgroundActive = false,
                runningMapPath = config.room.mapPath,
            )
        }
        engine.currentMapPath = config.room.mapPath
        engine.isStopped = false
        engine.isPaused = false
        engine.minimap?.release()
        val networkEngine = engine.configureLocalBattleRoom(config, "") ?: run {
            if (config.room.isSavedGame) {
                loadSavedGame(engine, config.room.mapPath)
            } else {
                loadMap(engine, config.room.mapPath)
            }
            return
        }

        check(networkEngine.startBattleRoomGame()) { "Unable to start battle room game" }
        BattleRoomUiBridge.setupGame()
        val resolvedMapPath = activeRunningMapPath(engine) ?: config.room.mapPath
        updateLoadState {
            it.copy(
                activeRendererBattleRoomConfig = config,
                pendingRendererBattleRoomConfig = null,
                runningMapPath = resolvedMapPath,
            )
        }
    }

    protected open fun loadMapSnapshot(engine: GameEngine, snapshot: MapSnapshot) {
        updateLoadState {
            it.copy(
                menuBackgroundActive = false,
                runningMapPath = snapshot.mapPath,
                pendingRendererBattleRoomConfig = null,
                activeRendererBattleRoomConfig = snapshot.battleRoomConfig,
            )
        }
        engine.isStopped = false
        engine.isPaused = false
        check(engine.gameSaver.readSaveFromStream(GameInputStream(snapshot.saveBytes), false, false, false)) {
            "Unable to restore in-memory save: ${snapshot.mapPath}"
        }
        val resolvedMapPath = activeRunningMapPath(engine) ?: snapshot.mapPath
        updateLoadState {
            it.copy(
                runningMapPath = resolvedMapPath
            )
        }
    }

    protected open fun loadReplay(engine: GameEngine, replayName: String) {
        updateLoadState {
            it.copy(
                menuBackgroundActive = false,
                runningMapPath = replayName,
            )
        }
        engine.isGameStarted = false
        engine.isStopped = false
        engine.isPaused = false
        check(engine.replayEngine.loadReplay(replayName)) { "Unable to load replay: $replayName" }
    }

    protected fun elapsedMs(startedAt: Long): Long =
        (System.nanoTime() - startedAt) / 1_000_000L

    protected fun Float.toGameSpeedDelta(): Float =
        (this * 60f).coerceIn(0f, 3f)
}

// Engine team-slot bounds: PlayerTeam.setMaxTeamId rejects < 10, and TEAM_ALLIES is the hard cap.
private const val BATTLE_ROOM_MIN_MAX_PLAYERS: Int = 10
private const val BATTLE_ROOM_MAX_MAX_PLAYERS: Int = 100
