package io.github.rwx.p2p

import com.corrodinggames.rts.game.PlayerTeam
import com.corrodinggames.rts.gameFramework.GameEngine
import com.corrodinggames.rts.gameFramework.network.GameModeType
import io.github.rwx.PlatformStorage
import io.github.rwx.logger
import io.github.rwx.map.PortalTransferMessage
import io.github.rwx.p2p.transfer.*
import io.github.rwx.ui.CoreUiEventQueue
import org.koin.mp.KoinPlatform.getKoin
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class PreparedP2PPeer internal constructor(
    val room: P2PRoomAdvertisement,
    val services: List<String>,
    internal val exchange: WebRtcSignalExchange,
)

class P2PLobbyService private constructor() {
    companion object {
        private val singleton by lazy { P2PLobbyService() }
        @JvmStatic fun getInstance(): P2PLobbyService = singleton
    }

    private val discoveredRooms = ConcurrentHashMap<String, P2PRoomAdvertisement>()
    private val scheduler = Executors.newScheduledThreadPool(2) { Thread(it, "lobby-service").apply { isDaemon = true } }
    private var started = false
    @JvmField var inLobby = false
    private var config = P2PConfigLoader.load()
    private var discovery = P2PServiceDiscoveryProvider(config.discovery.service)
    private var publisher = P2PServicePublisher(config.discovery.service.publish, config.discovery.service.urls)
    private var proxy = WebRtcTunnelProxy.create(config.webRtcProxy)
    private val localClientId = UUID.randomUUID().toString()
    val transferIdentity: TransferIdentity by lazy {
        TransferIdentity.loadOrCreate(getKoin().get<PlatformStorage>().localDir.file.toPath())
    }
    private val transferSignals by lazy { TransferSignaling(transferIdentity, localClientId) }
    @Volatile private var hostedRoom: P2PRoomAdvertisement? = null
    @Volatile private var prepared: PreparedP2PPeer? = null
    @Volatile private var exchange: WebRtcSignalExchange? = null
    @Volatile private var activeServices: List<String> = emptyList()
    private var senders: Map<String, ThreadPoolExecutor> = emptyMap()
    @Volatile private var transferHandler: ((WebRtcTransferConnection, String) -> Unit)? = null
    private var lastDiscovery = 0L
    private var lastPublish = 0L
    private var detectedMap: String? = null
    private var requiredFeatures: List<String> = emptyList()
    @Volatile private var assignments: MultiMapAssignments? = null
    private val portalTransfers = ConcurrentLinkedQueue<PortalTransferMessage>()

    @Synchronized
    fun startIfNeeded() {
        if (started) return
        config = P2PConfigLoader.load()
        if (!serviceSignalingAvailable(config.discovery.service)) throw IOException("Lobby signaling service is not configured")
        discovery = P2PServiceDiscoveryProvider(config.discovery.service)
        publisher = P2PServicePublisher(config.discovery.service.publish, config.discovery.service.urls)
        proxy = WebRtcTunnelProxy.create(config.webRtcProxy)
        proxy.setFeatureReceiver(::onFeatureMessage)
        senders = config.discovery.service.urls.map(::serviceKey).distinct().take(8).associateWith {
            ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, ArrayBlockingQueue(128)) { work ->
                Thread(work, "lobby-signal-send").apply { isDaemon = true }
            }
        }
        val polling = Executors.newScheduledThreadPool(senders.size.coerceAtLeast(1)) { work ->
            Thread(work, "lobby-signal-poll").apply { isDaemon = true }
        }
        senders.keys.forEach { url -> polling.scheduleWithFixedDelay({ poll(url) }, 500,
            config.discovery.service.signalPollIntervalMs, TimeUnit.MILLISECONDS) }
        scheduler.scheduleWithFixedDelay(::tick, 0, config.lobby.roomAnnounceIntervalMs, TimeUnit.MILLISECONDS)
        started = true
    }

    fun leaveLobby() { inLobby = false }
    fun setTransferHandler(handler: (WebRtcTransferConnection, String) -> Unit) { transferHandler = handler }

    fun hostCurrentServer(shareMods: Boolean = false) {
        startIfNeeded()
        val services = compatibleServices(senders.keys.toList())
        if (!publisher.isEnabled()) throw IOException("Room publishing is disabled")
        val engine = GameEngine.getInstance()
        val room = P2PRoomAdvertisement(roomId = UUID.randomUUID().toString(), hostClientId = localClientId,
            webrtcSignaling = "service", transfer = TransferAdvertisement(listOf(1), "webrtc-datachannel",
                transferIdentity.publicKey, services, if (shareMods) "preparing" else "disabled"))
        updateRoom(room)
        if (!publisher.publishRoom(room)) throw IOException("Could not publish room to a signaling service")
        synchronized(this) {
            clearExchange()
            hostedRoom = room
            prepared = null
            activeServices = services
            val current = WebRtcSignalExchange(room.roomId!!, localClientId)
            exchange = current
            try {
                proxy.startHostSide(room.roomId!!, localClientId, engine.settingsEngine.networkPort, room.webrtcIceServers) {
                    send(current, it)
                }
                proxy.startTransferHost(room.roomId!!, localClientId, room.webrtcIceServers, { send(current, it) }) { connection ->
                    val peer = transferSignals.peerIdentity(connection.sessionId)
                    val handler = transferHandler
                    if (peer == null || handler == null) connection.close()
                    else handler(wrap(connection), peer)
                }
                engine.networkEngine.p2pSession = true
                discoveredRooms[room.roomId!!] = room
                lastPublish = System.currentTimeMillis()
            } catch (error: Exception) {
                stopSession()
                throw error
            }
        }
        refreshUi()
    }

    fun preparePeer(roomId: String): PreparedP2PPeer {
        startIfNeeded()
        val room = findRoom(roomId)?.copy() ?: throw TransferException(TransferErrorCode.ROOM_CLOSED)
        if (!room.isValid()) throw TransferException(TransferErrorCode.UNSUPPORTED_VERSION)
        val remoteServices = room.transfer!!.signalingServices.map(::serviceKey).toSet()
        val services = compatibleServices(senders.keys.filter { it in remoteServices })
        synchronized(this) {
            if (hostedRoom != null || prepared != null) throw TransferException(TransferErrorCode.BUSY)
            val current = WebRtcSignalExchange(roomId, localClientId)
            clearExchange()
            exchange = current
            activeServices = services
            return PreparedP2PPeer(room, services, current).also { prepared = it }
        }
    }

    @Synchronized
    fun openTransfer(peer: PreparedP2PPeer): WebRtcTransferConnection {
        check(prepared === peer && peer.exchange.isActive())
        val id = UUID.randomUUID().toString()
        transferSignals.prepareClient(peer.room.roomId!!, id, peer.room.hostClientId!!, peer.room.transfer!!.hostKey!!)
        return try {
            wrap(proxy.openTransfer(peer.room.roomId!!, id, localClientId, peer.room.hostClientId!!, peer.room.webrtcIceServers) {
                send(peer.exchange, it)
            })
        } catch (error: Exception) {
            transferSignals.close(id)
            throw error
        }
    }

    @Synchronized
    fun connectPreparedPeer(peer: PreparedP2PPeer): String {
        check(prepared === peer && peer.exchange.isActive())
        val port = proxy.startClientSide(peer.room.roomId!!, localClientId, peer.room.hostClientId!!, peer.room.webrtcIceServers) {
            send(peer.exchange, it)
        }
        GameEngine.getInstance().networkEngine.p2pSession = true
        return "127.0.0.1:$port"
    }

    fun prepareJoin(roomId: String): String = connectPreparedPeer(preparePeer(roomId))

    @Synchronized
    fun cancelPeer(peer: PreparedP2PPeer) {
        if (prepared !== peer) return
        stopSession()
    }

    @Synchronized
    fun stopSession() {
        val room = hostedRoom
        clearExchange()
        room?.roomId?.let { discoveredRooms.remove(it) }
        room?.roomId?.let(transferSignals::closeRoom)
        prepared?.room?.roomId?.let(transferSignals::closeRoom)
        hostedRoom = null
        prepared = null
        activeServices = emptyList()
        proxy.stop()
        assignments = null
        portalTransfers.clear()
        detectedMap = null
        requiredFeatures = emptyList()
        GameEngine.getInstance()?.networkEngine?.p2pSession = false
        if (room != null) scheduler.execute { publisher.closeRoom(room) }
        refreshUi()
    }

    fun hostedRoomId(): String? = hostedRoom?.roomId
    fun hostPassword(): String? = GameEngine.getInstance()?.networkEngine?.roomPassword
    fun hostIsJoinable(): Boolean = hostedRoom?.gameState == "battleroom"
    fun signalingServices(): List<String> = activeServices

    @Synchronized
    fun updateTransferAdvertisement(roomId: String, value: TransferAdvertisement) {
        val room = hostedRoom?.takeIf { it.roomId == roomId } ?: return
        room.transfer = value
        lastPublish = 0
    }

    fun requestRefresh() { startIfNeeded(); lastDiscovery = 0; scheduler.execute(::tick) }
    fun findRoom(roomId: String): P2PRoomAdvertisement? = discoveredRooms[roomId]
    fun getRooms(): ArrayList<P2PRoomAdvertisement> = discoveredRooms.values.filter { it.isValid() && !it.isExpired(System.currentTimeMillis()) }
        .sortedWith(compareBy<P2PRoomAdvertisement> { it.gameState ?: "" }.thenBy { it.createdBy ?: "" }).toCollection(ArrayList())

    fun currentMissingRequiredFeatureSummary(): String? {
        val room = hostedRoom ?: return null
        updateRoom(room)
        val supported = proxy.peerFeatureSnapshot()
        val missing = proxy.getConnectedPeerIds().mapNotNull { peer ->
            val missing = requiredFeatures.filterNot { it in supported[peer].orEmpty() }
            if (missing.isEmpty()) null else "${peer.take(8)}=${missing.joinToString(",")}"
        }
        return missing.takeIf { it.isNotEmpty() }?.joinToString("; ", "Missing client features: ")
    }

    fun broadcastMultiMapAssignments(value: MultiMapAssignments) {
        if (hostedRoom == null) return
        assignments = value
        proxy.broadcastFeatureMessage(FeatureMessage(type = "multiMapAssignments", multiMapAssignments = value))
    }
    fun currentMultiMapAssignments(): MultiMapAssignments? = assignments
    fun broadcastPortalTransfer(value: PortalTransferMessage) {
        portalTransfers += value
        proxy.broadcastFeatureMessage(FeatureMessage(type = "portalTransfer", portalTransfer = value))
    }
    fun drainPortalTransfers(): List<PortalTransferMessage> = buildList { while (true) add(portalTransfers.poll() ?: break) }

    private fun onFeatureMessage(message: FeatureMessage) {
        when (message.type) {
            "hello" -> if (hostedRoom != null) assignments?.let {
                proxy.broadcastFeatureMessage(FeatureMessage(type = "multiMapAssignments", toPeerId = message.fromPeerId, multiMapAssignments = it))
            }
            "multiMapAssignments" -> if (hostedRoom == null && message.fromPeerId == prepared?.room?.hostClientId) assignments = message.multiMapAssignments
            "portalTransfer" -> message.portalTransfer?.let {
                portalTransfers += it
                if (hostedRoom != null) proxy.broadcastFeatureMessage(FeatureMessage(type = "portalTransfer", portalTransfer = it), message.fromPeerId)
            }
        }
    }

    private fun compatibleServices(values: List<String>): List<String> {
        val result = values.filter { runCatching { discovery.fetchCapabilities(it).supportsTransfers() }.getOrDefault(false) }
        if (result.isEmpty()) throw TransferException(TransferErrorCode.UNSUPPORTED_VERSION)
        return result
    }
    private fun serviceKey(value: String): String = value.trimEnd('/').removeSuffix("/rooms")
    private fun refreshUi() = CoreUiEventQueue.requestP2PRoomListRefresh()
    private fun clearExchange() { exchange?.close(); exchange = null; senders.values.forEach { it.queue.clear() } }

    private fun wrap(connection: WebRtcTransferConnection): WebRtcTransferConnection = object : WebRtcTransferConnection by connection {
        override fun close() { connection.close(); transferSignals.close(connection.sessionId) }
    }

    private fun send(current: WebRtcSignalExchange, signal: WebRtcTunnelProxy.Signal) {
        if (!current.isActive() || current.roomId != signal.roomId) return
        val envelope = if (signal.purpose == "transfer-v1") transferSignals.encode(signal)
            else current.prepareOutgoing(signal, System.currentTimeMillis()) ?: return
        for (url in activeServices) {
            val sender = senders[url] ?: continue
            try {
                sender.execute {
                    for (attempt in 0..2) {
                        if (!current.isActive()) return@execute
                        if (publisher.postSignal(url, current.roomId, envelope)) return@execute
                        if (attempt < 2) Thread.sleep(250L shl attempt)
                    }
                }
            } catch (_: RejectedExecutionException) { throw IOException("Signaling send queue is full") }
        }
    }

    private fun poll(url: String) {
        val current = exchange ?: return
        if (url !in activeServices) return
        val cursor = current.pollCursor(url, System.currentTimeMillis()) ?: return
        try {
            val incoming = discovery.fetchSignals(url, current.roomId, cursor, localClientId)
            current.receive(url, incoming, System.currentTimeMillis(), handle = { envelope ->
                if (envelope.purpose == "game") proxy.handleSignal(envelope.toProxySignal())
                else transferSignals.decode(envelope, hostedRoom?.roomId)?.let(proxy::handleTransferSignal)
            }, onError = { logger.warn(it) { "Rejected invalid signaling message" } })
        } catch (_: Exception) { current.recordFailure(url, System.currentTimeMillis()) }
    }

    private fun tick() {
        try {
            val now = System.currentTimeMillis()
            discoveredRooms.entries.removeIf { it.value.isExpired(now) }
            hostedRoom?.let { room ->
                updateRoom(room)
                if (now - lastPublish >= config.discovery.service.publish.updateIntervalMs) {
                    lastPublish = now
                    publisher.publishRoom(room)
                }
            }
            if (inLobby && now - lastDiscovery >= config.discovery.service.refreshIntervalMs) {
                lastDiscovery = now
                discovery.fetchRooms().forEach { result ->
                    val room = result.room
                    val previous = discoveredRooms[room.roomId]
                    if (previous == null || previous.seq <= room.seq) discoveredRooms[room.roomId!!] = room
                }
                refreshUi()
            }
        } catch (_: Exception) { logger.info { "Lobby service update failed" } }
    }

    private fun activeModTitles(engine: GameEngine): List<String> {
        return try {
            val manager = engine.modManager
            val jvmIds = manager.jvmMods.mapNotNull { it.manifest?.id }.toSet()
            manager.mods.asSequence()
                .filter { it.isEnabled }
                .filter { !it.isBuiltIn && !it.isCoreMod && it.id !in jvmIds }
                .map { it.displayTitle.replace('\n', ' ').replace('\r', ' ').trim() }
                .filter { it.isNotBlank() }
                .toList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun truncateModTitles(joined: String, maxChars: Int = 512): String {
        if (joined.length <= maxChars) return joined
        var end = maxChars
        if (joined[end - 1].isHighSurrogate()) end--
        return joined.substring(0, end)
    }

    private fun updateRoom(room: P2PRoomAdvertisement) {
        val engine = GameEngine.getInstance()
        val network = engine.networkEngine
        val map = network.selectedMapPath ?: engine.currentMapPath ?: network.roomSettings.mapPath
        if (map != detectedMap) {
            detectedMap = map
            requiredFeatures = MapFeatureDetector.requiredFeaturesForMap(map)
            proxy.broadcastMapFeatures(network.roomSettings.mapPath, requiredFeatures)
        }
        room.apply {
            type = "room_announce"
            createdBy = network.playerName
            gameVersionCode = engine.getVersionCode(true)
            gameVersionString = engine.getVersionName()
            requiresPassword = network.roomPassword != null
            mapPath = network.roomSettings.mapPath
            requiredRwxFeatures = requiredFeatures.toMutableList()
            gameMode = network.roomSettings.gameModeType?.name ?: GameModeType.entries[0].name
            gameState = when {
                network.chatOnlyMode -> "chat"
                network.gameHasBeenStarted -> "ingame"
                network.roomSettings.roomLock -> "locked"
                else -> "battleroom"
            }
            currentPlayers = network.getPlayerCount()
            maxPlayers = PlayerTeam.TEAM_NEUTRAL
            val modTitles = activeModTitles(engine)
            hasMods = network.requireActiveMods && modTitles.isNotEmpty()
            modsRequired = if (hasMods) truncateModTitles(modTitles.joinToString(", ")) else ""
            webrtcSignaling = "service"
            webrtcIceServers = config.webrtcIceServers.toMutableList()
            seq++
            lastSeenTimeMs = System.currentTimeMillis()
            expiresAtMs = lastSeenTimeMs + config.lobby.roomTtlMs
        }
    }
}
