package io.github.rwx.app

import com.corrodinggames.rts.gameFramework.GameEngine
import com.corrodinggames.rts.gameFramework.network.GameModeType
import io.github.rwx.PlatformStorage
import io.github.rwx.geometry.Viewport
import io.github.rwx.i18n.I18n
import io.github.rwx.map.MapLinkResolver
import io.github.rwx.map.MapMetadata
import io.github.rwx.map.PortalTransferMessage
import io.github.rwx.mod.registry.UiRegistry
import io.github.rwx.p2p.P2PLobbyService
import io.github.rwx.session.BattleRoomSnapshot
import io.github.rwx.session.GameSession
import io.github.rwx.session.MapSnapshot
import io.github.rwx.ui.AppScreen
import io.github.rwx.ui.CoreUiEvent
import io.github.rwx.ui.component.Icon
import io.github.rwx.ui.model.Dialog
import io.github.rwx.ui.model.DialogButton
import io.github.rwx.ui.model.DialogInfoRow

internal class MapController(
    private val gameSession: GameSession,
    private val storage: () -> PlatformStorage?,
    private val viewport: () -> Viewport,
    private val pendingStartMapPath: () -> String?,
    private val setPendingStart: (mapPath: String, failureReturnScreen: AppScreen) -> Unit,
    private val navigateToInGame: () -> Unit,
    private val currentBattleRoomSnapshotForJoin: () -> BattleRoomSnapshot?,
    private val showUnavailableDialog: (String) -> Unit,
    private val showDialogOverGame: (Dialog) -> Unit,
    private val hideDialog: () -> Unit,
    private val state: MapRuntimeState = MapRuntimeState(),
) {
    fun jumpToLinkedMap(event: CoreUiEvent.InGameMapJumpRequested) {
        val session = P2PLobbyService.getInstance().currentLinkedMapSession() ?: return
        val targetName = event.targetMapId.replace('\\', '/').substringAfterLast('/').removeSuffix(".tmx")
        val worldId = session.paths.entries.firstOrNull { (_, path) ->
            path.replace('\\', '/').substringAfterLast('/').removeSuffix(".tmx")
                .equals(targetName, ignoreCase = true)
        }?.key ?: session.spec?.worlds?.firstOrNull { world ->
            world.mapId.equals(event.targetMapId, ignoreCase = true) ||
                    world.mapId.equals(targetName, ignoreCase = true)
        }?.mapId ?: return
        session.requestView(worldId)
    }

    fun canShowMapList(): Boolean =
        P2PLobbyService.getInstance().currentLinkedMapSession()?.spec?.worlds?.isNotEmpty() == true ||
        state.hasKnownLinkedMaps() ||
                ((P2PLobbyService.getInstance().currentMultiMapAssignments()?.instances?.size ?: 0) > 1)

    fun showMapSwitchDialog() {
        P2PLobbyService.getInstance().currentLinkedMapSession()?.let { session ->
            showDialogOverGame(
                Dialog(
                    title = I18n.ingame.map.title(), message = I18n.ingame.map.message(),
                    infoRows = session.spec!!.worlds.map { world ->
                        DialogInfoRow(
                            icon = Icon.Map, label = world.mapId,
                            value = if (gameSession.linkedViewedMapPath == session.paths[world.mapId]) I18n.ingame.map.current() else "",
                            onPress = { hideDialog(); session.requestView(world.mapId) },
                        )
                    },
                    buttons = listOf(DialogButton(I18n.common.cancel())), scrollableMessage = true,
                )
            )
            return
        }
        val mapStorage = storage()
        if (mapStorage == null) {
            showUnavailableDialog("RWX map switching is unavailable on this platform.")
            return
        }
        val currentMapPath = gameSession.runningMapPath()
            ?: pendingStartMapPath()
            ?: currentBattleRoomSnapshotForJoin()?.room?.mapPath
        if (currentMapPath.isNullOrBlank()) {
            showUnavailableDialog(I18n.ingame.map.noActive())
            return
        }
        val rootPath = state.graphRootPath?.takeIf { root ->
            state.samePath(root, currentMapPath) ||
                    state.runtimeLinkedMapNames.keys.any { state.samePath(it, currentMapPath) } ||
                    MapLinkResolver.resolveLinkedMapGraph(mapStorage, root).maps.any {
                        state.samePath(it.mapPath, currentMapPath)
                    }
        } ?: currentMapPath
        val liveRootTargets = if (state.samePath(rootPath, currentMapPath)) {
            GameEngine.getInstance()?.missionEngine?.linkedPortalMode?.targetMapIds?.toList()
        } else {
            null
        }
        val graph = MapLinkResolver.resolveLinkedMapGraph(mapStorage, rootPath, liveRootTargets)
        val mapRows = linkedMapOf<String, String>()
        graph.maps.forEach { node ->
            mapRows[node.mapPath] = node.fileName
        }
        val runtimeMissingTargets = mutableListOf<String>()
        GameEngine.getInstance()?.missionEngine?.mapPortalTargetMapIds?.forEach { targetPathValue ->
            val targetPath = MapLinkResolver.findMapPath(mapStorage, targetPathValue)
            if (targetPath == null) {
                runtimeMissingTargets += targetPathValue
            } else {
                mapRows.putIfAbsent(targetPath, MapMetadata.getMapName(targetPath))
            }
        }
        // The current map's editor state is authoritative: drop remembered maps that its live
        // portals no longer reach, so a deleted portal stops resurfacing in this list.
        if (state.samePath(rootPath, currentMapPath)) {
            state.runtimeLinkedMapNames.keys.removeAll { cached ->
                mapRows.keys.none { state.samePath(it, cached) }
            }
        }
        state.runtimeLinkedMapNames.forEach { (path, name) ->
            mapRows.putIfAbsent(path, name)
        }
        mapRows.putIfAbsent(currentMapPath, MapMetadata.getMapName(currentMapPath))
        state.runtimeLinkedMapNames.clear()
        state.runtimeLinkedMapNames.putAll(mapRows)
        if (mapRows.size <= 1) {
            showUnavailableDialog(I18n.ingame.map.noLinked())
            return
        }
        val missingTargets = (graph.missingTargetMapIds + runtimeMissingTargets).distinct()
        if (missingTargets.isNotEmpty()) {
            showUnavailableDialog(I18n.ingame.map.missing(missingTargets.joinToString(", ")))
            return
        }
        state.graphRootPath = rootPath
        val assignments = P2PLobbyService.getInstance().currentMultiMapAssignments()
        val localPlayerId = currentLocalPlayerId()
        showDialogOverGame(
            Dialog(
                title = I18n.ingame.map.title(),
                message = I18n.ingame.map.message(),
                infoRows = mapRows.map { (mapPath, displayName) ->
                    val assignment = assignments?.instances?.firstOrNull {
                        state.samePath(it.mapPath, mapPath)
                    }
                    val isCurrent = state.samePath(mapPath, currentMapPath)
                    val isLocalSimulator = assignment != null && assignment.simulatorPlayerId == localPlayerId
                    DialogInfoRow(
                        icon = Icon.Map,
                        label = displayName,
                        value = when {
                            isCurrent -> I18n.ingame.map.current()
                            assignment != null -> I18n.ingame.map.sim(assignment.simulatorPlayerName)
                            else -> mapPath.substringAfterLast('/')
                        },
                        emphasis = isCurrent || isLocalSimulator,
                        onPress = {
                            switchMap(mapPath)
                        },
                    )
                },
                buttons = listOf(DialogButton(I18n.common.cancel())),
                scrollableMessage = true,
            )
        )
    }

    fun handlePortalTransfer(transfer: PortalTransferMessage) {
        if (gameSession.hasLinkedRoom) return
        val targetMapPath = storage()?.let { MapLinkResolver.findMapPath(it, transfer.targetMapId) }
        if (targetMapPath == null) {
            showUnavailableDialog(I18n.ingame.map.notFound(transfer.targetMapId))
            return
        }
        if (GameEngine.getInstance()?.isNetworkGameActive() == true) {
            P2PLobbyService.getInstance().broadcastPortalTransfer(transfer)
            GameEngine.getInstance()?.gameUI?.showMediumPriorityMessage(I18n.ingame.toast.portalSent())
            return
        }
        state.queuePortalTransfer(targetMapPath, transfer)
        GameEngine.getInstance()?.gameUI?.showMediumPriorityMessage(
            I18n.ingame.toast.transferred(MapMetadata.getMapName(targetMapPath))
        )
    }

    fun drainP2PPortalTransfers() {
        if (gameSession.hasLinkedRoom) {
            P2PLobbyService.getInstance().drainPortalTransfers(); return
        }
        P2PLobbyService.getInstance().drainPortalTransfers().forEach { transfer ->
            val targetMapPath = storage()?.let {
                MapLinkResolver.findMapPath(it, transfer.targetMapId)
            } ?: return@forEach
            if (!shouldSimulateMapLocally(targetMapPath)) {
                return@forEach
            }
            state.queuePortalTransfer(targetMapPath, transfer)
        }
    }

    fun applyPendingPortalTransfers() {
        if (gameSession.hasLinkedRoom) return
        val currentMapPath = gameSession.runningMapPath() ?: return
        if (!gameSession.isMapLoaded(currentMapPath)) {
            return
        }
        val transfers = state.removePortalTransfersFor(currentMapPath) ?: return
        transfers.groupBy { it.targetPortalId }.forEach { (portalId, groupedTransfers) ->
            val units = groupedTransfers.flatMap { it.units }
            gameSession.injectTransferredUnits(units, portalId)
        }
    }

    fun ensureP2PAssignedMapLoaded() {
        if (gameSession.hasLinkedRoom) return
        val engine = GameEngine.getInstance() ?: return
        val networkEngine = engine.networkEngine ?: return
        if (!engine.isNetworkGameActive() || !networkEngine.p2pSession) {
            return
        }
        val localPlayerId = currentLocalPlayerId() ?: return
        val assignment = P2PLobbyService.getInstance().currentMultiMapAssignments()
            ?.instances
            ?.firstOrNull { it.simulatorPlayerId == localPlayerId }
            ?: return
        if (state.samePath(gameSession.runningMapPath(), assignment.mapPath) ||
            state.samePath(pendingStartMapPath(), assignment.mapPath)
        ) {
            return
        }
        if (state.samePath(state.appliedP2PAssignedMapPath, assignment.mapPath) &&
            gameSession.isPreparingMap(assignment.mapPath)
        ) {
            return
        }
        requestP2PAssignedMap(assignment.mapPath)
    }

    private fun switchMap(targetMapPath: String) {
        hideDialog()
        P2PLobbyService.getInstance().currentLinkedMapSession()?.let { session ->
            session.paths.entries.firstOrNull {
                state.samePath(
                    it.value,
                    targetMapPath
                )
            }?.key?.let(session::requestView)
            return
        }
        val currentMapPath = gameSession.runningMapPath()
        if (state.samePath(currentMapPath, targetMapPath)) {
            return
        }
        if (GameEngine.getInstance()?.isNetworkGameActive() == true) {
            val assignment = P2PLobbyService.getInstance().currentMultiMapAssignments()
                ?.instances
                ?.firstOrNull { state.samePath(it.mapPath, targetMapPath) }
            val localPlayerId = currentLocalPlayerId()
            if (assignment == null || assignment.simulatorPlayerId != localPlayerId) {
                showUnavailableDialog(
                    I18n.ingame.map.unavailable(assignment?.simulatorPlayerName ?: I18n.multiplayer.unknown())
                )
                return
            }
            requestP2PAssignedMap(targetMapPath)
            return
        }
        currentMapPath?.let { mapPath ->
            gameSession.captureMapSnapshot()?.let { snapshot ->
                state.putSnapshot(mapPath, snapshot)
            }
        }
        val snapshot = state.snapshotFor(targetMapPath)
        queueMapLoad(targetMapPath, snapshot)
    }

    private fun requestP2PAssignedMap(targetMapPath: String) {
        queueMapLoad(targetMapPath)
        state.appliedP2PAssignedMapPath = targetMapPath
    }

    private fun queueMapLoad(targetMapPath: String, snapshot: MapSnapshot? = null) {
        // These load APIs create/restore a local world. A multiplayer world needs a coordinated
        // map transition; calling them here would disconnect the room and reset its players.
        if (gameSession.isNetworkMultiplayerActive()) {
            showUnavailableDialog("network multiplayer is not active")
            return
        }
        val battleRoomConfig = if (snapshot == null) {
            gameSession.captureCurrentBattleRoomConfig()?.let { current ->
                current.copy(
                    room = current.room.copy(
                        mapPath = targetMapPath,
                        options = current.room.options.clone().apply {
                            // The source can be a saved game; the destination here is a TMX map.
                            gameModeType = if (MapMetadata.isFromSdCard(targetMapPath)) {
                                GameModeType.customMap
                            } else {
                                GameModeType.skirmishMap
                            }
                            mapPath = targetMapPath.replace('\\', '/').substringAfterLast('/')
                        },
                    ),
                )
            }
        } else {
            null
        }
        when {
            snapshot != null -> gameSession.requestMapSnapshot(snapshot)
            battleRoomConfig != null -> {
                check(gameSession.prepareLocalBattleRoom(battleRoomConfig)) {
                    "Game session rejected local battle room map switch"
                }
            }

            else -> gameSession.requestMap(targetMapPath)
        }
        setPendingStart(targetMapPath, AppScreen.InGame)
        navigateToInGame()
        gameSession.setGameVisible(
            true,
            viewport(),
            uiOverlay = UiRegistry.hasActiveHudLayers(),
        )
    }

    private fun shouldSimulateMapLocally(mapPath: String): Boolean {
        val engine = GameEngine.getInstance()
        val networkEngine = engine?.networkEngine
        if (engine?.isNetworkGameActive() != true || networkEngine?.p2pSession != true) {
            return true
        }
        val localPlayerId = currentLocalPlayerId() ?: return false
        return P2PLobbyService.getInstance().currentMultiMapAssignments()
            ?.instances
            ?.any { state.samePath(it.mapPath, mapPath) && it.simulatorPlayerId == localPlayerId }
            ?: false
    }

    private fun currentLocalPlayerId(): String? {
        gameSession.currentBattleRoom()?.players?.firstOrNull { it.isLocal }?.id?.let { return it }
        val engine = GameEngine.getInstance() ?: return null
        return engine.networkEngine?.localPlayerTeam?.teamId?.toString()
            ?: engine.playerTeam?.teamId?.toString()
    }
}
