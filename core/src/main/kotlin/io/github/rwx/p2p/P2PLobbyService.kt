package io.github.rwx.p2p

import com.corrodinggames.rts.game.PlayerTeam
import com.corrodinggames.rts.gameFramework.GameEngine
import com.corrodinggames.rts.gameFramework.network.GameModeType
import com.corrodinggames.rts.gameFramework.network.NetworkEngine
import io.github.rwx.logger
import io.github.rwx.map.PortalTransferMessage
import io.github.rwx.ui.CoreUiEventQueue
import io.libp2p.core.Host
import io.libp2p.core.PeerId
import io.libp2p.core.PeerInfo
import io.libp2p.core.dsl.HostBuilder
import io.libp2p.core.multiformats.Multiaddr
import io.libp2p.core.pubsub.MessageApi
import io.libp2p.core.pubsub.Topic
import io.libp2p.discovery.MDnsDiscovery
import io.libp2p.pubsub.gossip.Gossip
import io.libp2p.pubsub.gossip.builders.GossipRouterBuilder
import io.libp2p.transport.tcp.TcpTransport
import io.netty.buffer.Unpooled
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class P2PLobbyService private constructor() {
    companion object {
        private const val LOBBY_TOPIC = "rwx.p2p.lobby.v1"
        private val singleton: P2PLobbyService = P2PLobbyService()

        @JvmStatic
        fun getInstance(): P2PLobbyService = singleton
    }

    private val discoveredRooms = ConcurrentHashMap<String, P2PRoomAdvertisement>()
    private val mdnsPeers = ConcurrentHashMap<String, Long>()
    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val lobbyTopic = Topic(LOBBY_TOPIC)
    private val webrtcTopic = Topic("$LOBBY_TOPIC.webrtc")

    private var gossip: Gossip? = null
    private var host: Host? = null
    private var mDnsDiscovery: MDnsDiscovery? = null
    private var started = false

    @JvmField
    var inLobby = false
    private var p2pConfig: P2PConfig = P2PConfigLoader.load()
    private var hostedRoom: P2PRoomAdvertisement? = null
    private var joinedRoom: P2PRoomAdvertisement? = null
    private var libp2pProxy: Libp2pStreamProxy = Libp2pStreamProxy(p2pConfig.libp2pProxy)
    private var featureChannel: FeatureChannel = FeatureChannel()
    private var libp2pPortMapping: NatPortMapper.Mapping? = null
    private val natPortMapper = NatPortMapper()
    private var webRtcProxy = WebRtcTunnelProxy.create(p2pConfig.webRtcProxy)
    private var relaySupport: Libp2pRelaySupport.Components? = null
    private var serviceDiscovery = P2PServiceDiscoveryProvider(p2pConfig.discovery.service)
    private var servicePublisher = P2PServicePublisher(
        p2pConfig.discovery.service.publish,
        p2pConfig.discovery.service.urls,
    )
    private var dhtDiscovery = P2PDhtDiscoveryProvider(p2pConfig.discovery.dht)
    private var bootstrapNodeSource = P2PBootstrapNodeSource(p2pConfig.libp2pPeerSources)
    private var lastServiceDiscoveryMs = 0L
    private var lastDhtDiscoveryMs = 0L
    private var lastServicePublishMs = 0L
    private var lastDetectedMapFeaturePath: String? = null
    private var lastDetectedRequiredRwxFeatures: List<String> = emptyList()
    private var lastBroadcastMapFeaturePath: String? = null
    private var lastBroadcastRequiredRwxFeatures: List<String> = emptyList()

    @Synchronized
    @Throws(IOException::class)
    fun startIfNeeded() {
        if (started) {
            return
        }
        p2pConfig = P2PConfigLoader.load()
        libp2pProxy = Libp2pStreamProxy(p2pConfig.libp2pProxy)
        webRtcProxy = WebRtcTunnelProxy.create(p2pConfig.webRtcProxy)
        serviceDiscovery = P2PServiceDiscoveryProvider(p2pConfig.discovery.service)
        servicePublisher = P2PServicePublisher(
            p2pConfig.discovery.service.publish,
            p2pConfig.discovery.service.urls,
        )
        dhtDiscovery = P2PDhtDiscoveryProvider(p2pConfig.discovery.dht)
        bootstrapNodeSource = P2PBootstrapNodeSource(p2pConfig.libp2pPeerSources)
        val gossipProtocol = Gossip(GossipRouterBuilder().build())
        gossip = gossipProtocol
        val builtHost = runCatching {
            val hostBuilder = HostBuilder()
                .transport(::TcpTransport)
                .listen("/ip4/0.0.0.0/tcp/${p2pConfig.libp2pListenPort}")
                .protocol(gossipProtocol)
            relaySupport = Libp2pRelaySupport(p2pConfig.relay).applyTo(hostBuilder)
            hostBuilder.build()
        }.getOrElse { e ->
            logger.error(e) { "Failed to build libp2p host" }
            throw IOException("Failed to build libp2p host: ${rootCauseMessage(e)}", e)
        }
        host = builtHost.also {
            it.addConnectionHandler(gossipProtocol)
            runCatching {
                it.start().get(15L, TimeUnit.SECONDS)
            }.onFailure { e ->
                logger.error(e) { "Failed to start libp2p host" }
                throw IOException("Failed to start libp2p host: ${rootCauseMessage(e)}", e)
            }
        }
        val currentHost = host ?: throw IOException("Libp2p host was not created")
        GameEngine.log(
            "P2P libp2p host started peer=${currentHost.peerId.toBase58()} addrs=${
                currentHost.listenAddresses().joinToString(",")
            }"
        )
        if (p2pConfig.discovery.enableGossipSub) {
            gossipProtocol.subscribe(this::handleMessage, lobbyTopic)
        }
        gossipProtocol.subscribe(this::handleWebRtcSignal, webrtcTopic)
        if (p2pConfig.discovery.enableMdns) {
            mDnsDiscovery =
                MDnsDiscovery(currentHost, MDnsDiscovery.ServiceTagLocal, MDnsDiscovery.QueryInterval, null).also {
                    it.addHandler(this::handleMdnsPeerFound)
                    it.start()
                }
        }
        scheduler.scheduleWithFixedDelay(
            ::tick,
            1000L,
            p2pConfig.lobby.roomAnnounceIntervalMs,
            TimeUnit.MILLISECONDS
        )
        started = true
        connectBootstrapPeers((p2pConfig.libp2pPeers + bootstrapNodeSource.fetchPeers()).distinct())
    }

    fun leaveLobby() {
        inLobby = false
    }

    @Synchronized
    @Throws(IOException::class)
    fun hostCurrentServer() {
        ensureStarted()
        val gameEngine = GameEngine.getInstance()
        gameEngine.networkEngine.p2pSession = true
        try {
            val room = P2PRoomAdvertisement().apply {
                roomId = UUID.randomUUID().toString()
                hostPeerId = host!!.peerId.toBase58()
                createdBy = gameEngine.networkEngine.playerName
                libp2pBootstrapPeers = p2pConfig.libp2pPeers.toMutableList()
                libp2pDirectAddresses = getHostDirectAddresses(host!!).toMutableList()
                libp2pMappedAddresses = getMappedAddresses().toMutableList()
                webrtcIceServers = p2pConfig.webrtcIceServers.toMutableList()
            }
            featureChannel.startHostSide(
                host!!,
                room.roomId!!,
                host!!.peerId.toBase58(),
                FeatureIds.currentClientFeatures(),
            )
            updateRoomFromNetworkState(room)

            GameEngine.log("Starting WebRTC DataChannel proxy for game on port ${gameEngine.settingsEngine.networkPort}")
            libp2pProxy.startHostSide(host!!, gameEngine.settingsEngine.networkPort)
            ensureLibp2pPortMapping()
            updateRoomFromNetworkState(room)
            webRtcProxy.startHostSide(
                room.roomId!!,
                host!!.peerId.toBase58(),
                gameEngine.settingsEngine.networkPort,
                room.webrtcIceServers,
                ::publishWebRtcSignal
            )

            hostedRoom = room
            joinedRoom = null
            mergeDiscoveredRoom(P2PDiscoveryResult(room, P2PDiscoverySource.MANUAL, trustScore = 100))
            GameEngine.log(
                "P2P hosted room id=${room.roomId} host=${room.hostPeerId} direct=${
                    room.libp2pDirectAddresses.joinToString(
                        ","
                    )
                } mapped=${room.libp2pMappedAddresses.joinToString(",")}"
            )
            publishRoom(room)
            servicePublisher.publishRoom(room)
            lastServicePublishMs = System.currentTimeMillis()
            dhtDiscovery.publishRoom(room)
            queueRoomListRefresh()
        } catch (error: Exception) {
            clearSessionStateAfterFailedStart(gameEngine.networkEngine)
            throw error
        }
    }

    @Synchronized
    @Throws(IOException::class)
    fun prepareJoin(roomId: String): String {
        ensureStarted()
        val networkEngine = GameEngine.getInstance().networkEngine
        networkEngine.p2pSession = true
        try {
            val room = findRoom(roomId) ?: throw IOException("That P2P room is no longer available")

            GameEngine.log("Starting WebRTC DataChannel proxy to connect to host")
            val hostPeerId = room.hostPeerId ?: throw IOException("Missing host peer ID")
            val hostAddresses = parseMultiaddrs(room.allLibp2pAddresses())
            startFeatureChannelClientAsync(room, hostPeerId, hostAddresses)
            val localPort = if (room.webrtcSignaling == "gossip") {
                webRtcProxy.startClientSide(
                    room.roomId!!,
                    host!!.peerId.toBase58(),
                    hostPeerId,
                    room.webrtcIceServers,
                    ::publishWebRtcSignal
                )
            } else if (hostAddresses.isNotEmpty()) {
                libp2pProxy.startClientSide(host!!, hostPeerId, hostAddresses, localPort = 0)
            } else {
                throw IOException("P2P room has no WebRTC fallback and no direct libp2p address")
            }

            joinedRoom = room

            val connectAddress = "127.0.0.1:$localPort"
            GameEngine.log("Game client should connect to: $connectAddress (WebRTC DataChannel tunnel)")

            return connectAddress
        } catch (error: Exception) {
            clearSessionStateAfterFailedStart(networkEngine)
            throw error
        }
    }

    private fun clearSessionStateAfterFailedStart(networkEngine: NetworkEngine) {
        hostedRoom?.let { room ->
            servicePublisher.closeRoom(room)
            room.roomId?.let { discoveredRooms.remove(it) }
        }
        libp2pPortMapping?.let { natPortMapper.deleteMapping(it) }
        libp2pPortMapping = null
        hostedRoom = null
        joinedRoom = null
        libp2pProxy.stop()
        webRtcProxy.stop()
        featureChannel.stop()
        networkEngine.p2pSession = false
        lastDetectedMapFeaturePath = null
        lastDetectedRequiredRwxFeatures = emptyList()
        lastBroadcastMapFeaturePath = null
        lastBroadcastRequiredRwxFeatures = emptyList()
        queueRoomListRefresh()
    }

    private fun startFeatureChannelClientAsync(
        room: P2PRoomAdvertisement,
        hostPeerId: String,
        hostAddresses: List<Multiaddr>,
    ) {
        val currentHost = host ?: return
        val currentRoomId = room.roomId ?: return
        if (hostAddresses.isEmpty()) {
            GameEngine.log("RWX feature channel skipped: no direct libp2p address for host")
            return
        }
        Thread({
            runCatching {
                featureChannel.startClientSide(
                    currentHost,
                    currentRoomId,
                    currentHost.peerId.toBase58(),
                    hostPeerId,
                    hostAddresses,
                    FeatureIds.currentClientFeatures(),
                )
            }.onFailure { error ->
                GameEngine.log("RWX feature channel unavailable: ${rootCauseMessage(error)}")
            }
        }, "RWX-feature-channel-client").apply {
            isDaemon = true
            start()
        }
    }

    @Synchronized
    fun stopSession() {
        hostedRoom?.let { room ->
            servicePublisher.closeRoom(room)
            publishRoom(
                P2PRoomAdvertisement(
                    magic = room.magic,
                    schema = room.schema,
                    type = "room_close",
                    roomId = room.roomId,
                    hostPeerId = room.hostPeerId,
                    libp2pDirectAddresses = room.libp2pDirectAddresses,
                    libp2pMappedAddresses = room.libp2pMappedAddresses,
                    webrtcSignaling = room.webrtcSignaling,
                    webrtcIceServers = room.webrtcIceServers,
                )
            )
            room.roomId?.let { discoveredRooms.remove(it) }
        }
        libp2pPortMapping?.let { natPortMapper.deleteMapping(it) }
        libp2pPortMapping = null
        hostedRoom = null
        joinedRoom = null
        libp2pProxy.stop()
        webRtcProxy.stop()
        featureChannel.stop()
        GameEngine.getInstance().networkEngine.p2pSession = false
        lastDetectedMapFeaturePath = null
        lastDetectedRequiredRwxFeatures = emptyList()
        lastBroadcastMapFeaturePath = null
        lastBroadcastRequiredRwxFeatures = emptyList()
        queueRoomListRefresh()
    }

    @Synchronized
    @Throws(IOException::class)
    fun requestRefresh() {
        ensureStarted()
        queueRoomListRefresh()
    }

    fun getRooms(): ArrayList<P2PRoomAdvertisement> {
        val now = System.currentTimeMillis()
        return discoveredRooms.values
            .filterNot { it.isExpired(now) }
            .sortedWith(compareBy<P2PRoomAdvertisement> { it.gameState ?: "" }.thenBy { it.createdBy ?: "" })
            .toCollection(ArrayList())
    }

    @Synchronized
    fun findRoom(roomId: String): P2PRoomAdvertisement? = discoveredRooms[roomId]

    @Synchronized
    fun currentMissingRequiredFeatureSummary(): String? {
        val room = hostedRoom ?: return null
        updateRoomFromNetworkState(room)
        val requiredFeatures = lastBroadcastRequiredRwxFeatures
        if (requiredFeatures.isEmpty()) {
            return null
        }
        val connectedPeerIds = (
                libp2pProxy.getConnectedPeerIds() +
                        featureChannel.getConnectedPeerIds() +
                        webRtcProxy.getConnectedPeerIds()
                ).filter { it != room.hostPeerId }.distinct()
        if (connectedPeerIds.isEmpty()) {
            return null
        }
        val featureSnapshots = listOf(
            featureChannel.peerFeatureSnapshot(),
            webRtcProxy.peerFeatureSnapshot(),
        )
        val missingByPeer = connectedPeerIds.mapNotNull { peerId ->
            val supportedFeatures = featureSnapshots.flatMap { it[peerId].orEmpty() }.toSet()
            val missing = requiredFeatures.filterNot { supportedFeatures.contains(it) }
            if (missing.isEmpty()) {
                null
            } else {
                peerId to missing
            }
        }
        if (missingByPeer.isEmpty()) {
            return null
        }
        return buildString {
            append("Cannot start this RWX map. Missing client features: ")
            append(missingByPeer.joinToString("; ") { (peerId, missing) ->
                "${peerId.take(8)}=${missing.joinToString(",")}"
            })
        }
    }

    @Synchronized
    fun broadcastMultiMapAssignments(assignments: MultiMapAssignments) {
        featureChannel.broadcastMultiMapAssignments(assignments)
    }

    @Synchronized
    fun currentMultiMapAssignments(): MultiMapAssignments? =
        featureChannel.currentMultiMapAssignments()

    @Synchronized
    fun broadcastPortalTransfer(transfer: PortalTransferMessage) {
        featureChannel.broadcastPortalTransfer(transfer)
    }

    @Synchronized
    fun drainPortalTransfers(): List<PortalTransferMessage> =
        featureChannel.drainPortalTransfers()

    private fun ensureStarted() {
        if (!started) {
            throw IOException("P2P lobby has not been started")
        }
    }

    private fun tick() {
        try {
            pruneExpiredRooms()
            hostedRoom?.let {
                updateRoomFromNetworkState(it)
                publishRoom(it)
                publishExternalRoom(it)
                dhtDiscovery.publishRoom(it)
            }
            refreshExternalDiscovery()
        } catch (e: Exception) {
            GameEngine.log("P2P tick failed: ${e.message}")
        }
    }

    private fun refreshExternalDiscovery() {
        val now = System.currentTimeMillis()
        if (inLobby && p2pConfig.discovery.service.enable && now - lastServiceDiscoveryMs >= p2pConfig.discovery.service.refreshIntervalMs) {
            lastServiceDiscoveryMs = now
            serviceDiscovery.fetchRooms().forEach { mergeDiscoveredRoom(it) }
        }
        if (inLobby && p2pConfig.discovery.dht.enable && now - lastDhtDiscoveryMs >= p2pConfig.discovery.dht.queryIntervalMs) {
            lastDhtDiscoveryMs = now
            dhtDiscovery.fetchRooms().forEach { mergeDiscoveredRoom(it) }
        }
    }

    private fun publishExternalRoom(room: P2PRoomAdvertisement) {
        val now = System.currentTimeMillis()
        if (now - lastServicePublishMs >= p2pConfig.discovery.service.publish.updateIntervalMs) {
            lastServicePublishMs = now
            servicePublisher.publishRoom(room)
        }
    }

    private fun pruneExpiredRooms() {
        val now = System.currentTimeMillis()
        var removed = false
        val iterator = discoveredRooms.entries.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().value.isExpired(now)) {
                iterator.remove()
                removed = true
            }
        }
        if (removed) {
            queueRoomListRefresh()
        }
    }

    private fun updateRoomFromNetworkState(room: P2PRoomAdvertisement) {
        val gameEngine = GameEngine.getInstance()
        val networkEngine: NetworkEngine = gameEngine.networkEngine
        room.apply {
            type = "room_announce"
            createdBy = networkEngine.playerName
            gameVersionCode = gameEngine.getVersionCode(true)
            gameVersionString = gameEngine.getVersionName()
            requiresPassword = networkEngine.roomPassword != null
            gamePort = gameEngine.settingsEngine.networkPort
            mapPath = networkEngine.roomSettings.mapPath
            requiredRwxFeatures = publishCurrentMapFeatures(
                mapPath,
                networkEngine.selectedMapPath ?: gameEngine.currentMapPath ?: networkEngine.roomSettings.mapPath,
            ).toMutableList()
            gameMode = networkEngine.roomSettings.gameModeType?.name ?: GameModeType.entries[0].name
            gameState = when {
                networkEngine.chatOnlyMode -> "chat"
                networkEngine.gameHasBeenStarted -> "ingame"
                networkEngine.roomSettings.roomLock -> "locked"
                else -> "battleroom"
            }
            currentPlayers = networkEngine.getPlayerCount()
            maxPlayers = PlayerTeam.TEAM_NEUTRAL
            hasMods = networkEngine.requireActiveMods
            modsRequired = if (networkEngine.requireActiveMods) "Active mods required" else ""
            libp2pDirectAddresses = getHostDirectAddresses(host).toMutableList()
            libp2pMappedAddresses = getMappedAddresses().toMutableList()
            webrtcSignaling = "gossip"
            webrtcIceServers = p2pConfig.webrtcIceServers.toMutableList()
            seq += 1
            lastSeenTimeMs = System.currentTimeMillis()
            expiresAtMs = room.lastSeenTimeMs + p2pConfig.lobby.roomTtlMs
        }
    }

    private fun publishCurrentMapFeatures(displayMapPath: String?, sourceMapPath: String?): List<String> {
        val requiredFeatures = requiredFeaturesForMap(sourceMapPath)
        if (displayMapPath == lastBroadcastMapFeaturePath && requiredFeatures == lastBroadcastRequiredRwxFeatures) {
            return requiredFeatures
        }
        lastBroadcastMapFeaturePath = displayMapPath
        lastBroadcastRequiredRwxFeatures = requiredFeatures
        featureChannel.broadcastMapFeatures(displayMapPath, requiredFeatures)
        webRtcProxy.broadcastMapFeatures(displayMapPath, requiredFeatures)
        GameEngine.log("RWX map feature requirements map=$displayMapPath required=${requiredFeatures.joinToString(",")}")
        return requiredFeatures
    }

    private fun requiredFeaturesForMap(sourceMapPath: String?): List<String> {
        if (sourceMapPath == lastDetectedMapFeaturePath) {
            return lastDetectedRequiredRwxFeatures
        }
        lastDetectedMapFeaturePath = sourceMapPath
        lastDetectedRequiredRwxFeatures = MapFeatureDetector.requiredFeaturesForMap(sourceMapPath)
        return lastDetectedRequiredRwxFeatures
    }

    private fun publishRoom(room: P2PRoomAdvertisement) {
        if (!started) return
        try {
            gossip?.createPublisher(null, System.currentTimeMillis())
                ?.publish(Unpooled.wrappedBuffer(P2PJson.encodeToString(room).encodeToByteArray()), lobbyTopic)
        } catch (e: Exception) {
            GameEngine.log("Failed to publish P2P room: ${e.message}")
        }
    }

    private fun publishWebRtcSignal(signal: WebRtcTunnelProxy.Signal) {
        if (!started) return
        try {
            GameEngine.log("Publishing WebRTC signal type=${signal.type} session=${signal.sessionId} from=${signal.fromPeerId} to=${signal.toPeerId}")
            gossip?.createPublisher(null, System.currentTimeMillis())
                ?.publish(Unpooled.wrappedBuffer(P2PJson.encodeToString(signal).encodeToByteArray()), webrtcTopic)
        } catch (e: Exception) {
            GameEngine.log("Failed to publish WebRTC signal: ${e.message}")
        }
    }

    private fun handleMessage(messageApi: MessageApi) {
        try {
            val data = messageApi.data
            val bytes = ByteArray(data.readableBytes())
            data.getBytes(data.readerIndex(), bytes)
            val room = P2PJson.decodeFromString<P2PRoomAdvertisement>(bytes.toString(Charsets.UTF_8))
            if (!room.isValid()) return
            if (host?.peerId?.toBase58() == room.hostPeerId && hostedRoom?.roomId == room.roomId) return
            if (room.type == "room_close") {
                if (discoveredRooms.remove(room.roomId) != null) {
                    queueRoomListRefresh()
                }
                return
            }
            val now = System.currentTimeMillis()
            val fromPeerId = runCatching { messageApi.from?.let { PeerId(it) }?.toBase58() }.getOrNull()
            val mdnsTtlMs = p2pConfig.lobby.mdnsPeerAddressTtlMs.coerceAtLeast(1000L)
            val isMdnsPeer = fromPeerId?.let { mdnsPeers[it] }?.let { now - it <= mdnsTtlMs } == true
            val source = if (isMdnsPeer) P2PDiscoverySource.MDNS else P2PDiscoverySource.GOSSIPSUB
            val trustScore = if (isMdnsPeer) 70 else 60
            mergeDiscoveredRoom(P2PDiscoveryResult(room, source, trustScore = trustScore, receivedAtMs = now))
        } catch (e: Exception) {
            GameEngine.log("Failed to decode P2P room: ${e.message}")
        }
    }

    private fun mergeDiscoveredRoom(result: P2PDiscoveryResult) {
        val room = result.room
        if (!room.isValid()) return
        val roomId = room.roomId ?: return
        val now = result.receivedAtMs
        if (room.isExpired(now)) return
        room.lastSeenTimeMs = now
        if (!room.discoverySources.contains(result.source.id)) {
            room.discoverySources.add(result.source.id)
        }
        val previous = discoveredRooms[roomId]
        if (previous == null) {
            discoveredRooms[roomId] = room
            queueRoomListRefresh()
            return
        }
        previous.discoverySources.forEach {
            if (!room.discoverySources.contains(it)) room.discoverySources.add(it)
        }
        if (previous.seq <= room.seq) {
            discoveredRooms[roomId] = room
            queueRoomListRefresh()
        } else if (!previous.discoverySources.contains(result.source.id)) {
            previous.discoverySources.add(result.source.id)
            queueRoomListRefresh()
        }
    }

    private fun handleWebRtcSignal(messageApi: MessageApi) {
        try {
            val data = messageApi.data
            val bytes = ByteArray(data.readableBytes())
            data.getBytes(data.readerIndex(), bytes)
            val signal = P2PJson.decodeFromString<WebRtcTunnelProxy.Signal>(bytes.toString(Charsets.UTF_8))
            val localPeerId = host?.peerId?.toBase58()
            GameEngine.log("Received WebRTC signal type=${signal.type} session=${signal.sessionId} from=${signal.fromPeerId} to=${signal.toPeerId} local=$localPeerId")
            if (signal.fromPeerId == localPeerId) return
            if (signal.toPeerId != localPeerId) return
            webRtcProxy.handleSignal(signal)
        } catch (e: Exception) {
            GameEngine.log("Failed to decode WebRTC signal: ${e.message}")
        }
    }

    private fun queueRoomListRefresh() {
        CoreUiEventQueue.requestP2PRoomListRefresh()
    }

    private fun connectBootstrapPeers(peers: List<String>) {
        if (!started) return
        val currentHost = host ?: return
        peers.forEach { peer ->
            try {
                currentHost.network.connect(Multiaddr(peer))
                    .orTimeout(p2pConfig.lobby.bootstrapConnectTimeoutMs, TimeUnit.MILLISECONDS)
                    .whenComplete { _, error ->
                        if (error == null) {
                            GameEngine.log("P2P bootstrap connected: $peer")
                        } else {
                            GameEngine.log("P2P bootstrap connect failed: $peer - ${rootCauseMessage(error)}")
                        }
                    }
                GameEngine.log("P2P bootstrap connect started: $peer")
            } catch (e: Exception) {
                GameEngine.log("Failed bootstrap connect: $peer - ${e.message}")
            }
        }
    }

    private fun getHostDirectAddresses(currentHost: Host?): List<String> {
        if (currentHost == null) return emptyList()
        return currentHost.listenAddresses()
            .map { it.toString() }
            .filterNot { it.contains("/ip4/0.0.0.0/") || it.contains("/ip6/::/") }
            .distinct()
    }

    private fun ensureLibp2pPortMapping() {
        if (!p2pConfig.nat.enablePcp && !p2pConfig.nat.enableUpnp) return
        if (libp2pPortMapping != null) return
        val localPort = getHostTcpListenPort(host) ?: return
        GameEngine.log("Trying NAT port mapping for libp2p TCP port $localPort")
        libp2pPortMapping = runCatching {
            natPortMapper.mapTcpPort(
                localPort,
                p2pConfig.nat.mappingDescription,
                enablePcp = p2pConfig.nat.enablePcp,
                enableUpnp = p2pConfig.nat.enableUpnp,
                pcpLifetimeSeconds = p2pConfig.nat.pcpLifetimeSeconds,
                pcpTimeoutMs = p2pConfig.nat.pcpTimeoutMs
            )
        }.onFailure { e ->
            GameEngine.log("NAT port mapping failed: ${e.message}")
        }.getOrNull()

        libp2pPortMapping?.let {
            GameEngine.log("${it.method} mapped libp2p TCP ${it.internalPort} -> ${it.externalIp}:${it.externalPort}")
        }
    }

    private fun getMappedAddresses(): List<String> {
        val mapping = libp2pPortMapping ?: return emptyList()
        val peerId = host?.peerId ?: return emptyList()
        return listOf("/ip4/${mapping.externalIp}/tcp/${mapping.externalPort}/p2p/${peerId.toBase58()}")
    }

    private fun getHostTcpListenPort(currentHost: Host?): Int? {
        return currentHost?.listenAddresses()
            ?.map { it.toString() }
            ?.firstNotNullOfOrNull { address ->
                Regex("/tcp/(\\d+)").find(address)?.groupValues?.get(1)?.toIntOrNull()
            }
    }

    private fun P2PRoomAdvertisement.allLibp2pAddresses(): List<String> {
        return (libp2pMappedAddresses + libp2pDirectAddresses).distinct()
    }

    private fun parseMultiaddrs(values: List<String>?): List<Multiaddr> {
        return values.orEmpty().mapNotNull { value ->
            runCatching { Multiaddr(value) }.getOrNull()
        }.distinct()
    }

    private fun handleMdnsPeerFound(peerInfo: PeerInfo) {
        try {
            val currentHost = host ?: return
            mdnsPeers[peerInfo.peerId.toBase58()] = System.currentTimeMillis()
            if (peerInfo.addresses.isNotEmpty()) {
                currentHost.addressBook.addAddrs(
                    peerInfo.peerId,
                    p2pConfig.lobby.mdnsPeerAddressTtlMs,
                    *peerInfo.addresses.toTypedArray()
                )
                currentHost.network.connect(peerInfo.peerId, *peerInfo.addresses.toTypedArray())
            }
        } catch (e: Exception) {
            GameEngine.log("Failed mDNS connect: ${e.message}")
        }
    }

    private fun rootCauseMessage(throwable: Throwable): String {
        var current = throwable
        while (current.cause != null) current = current.cause!!
        return current.message ?: current.javaClass.name
    }

}
