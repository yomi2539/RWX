package io.github.rwx.p2p

import com.corrodinggames.rts.gameFramework.GameEngine
import com.corrodinggames.rts.gameFramework.network.NetworkEngine
import io.github.rwx.logger
import dev.onvoid.webrtc.*
import dev.onvoid.webrtc.media.audio.AudioDeviceModule
import dev.onvoid.webrtc.media.audio.AudioLayer
import java.io.IOException
import java.net.*
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean

class DesktopWebRtcTunnelProxy(
    private val config: WebRtcTunnelProxy.ProxyConfig = WebRtcTunnelProxy.ProxyConfig(),
) : WebRtcTunnelProxy {

    private data class ClientSession(
        val roomId: String,
        val sessionId: String,
        val localPeerId: String,
        val remotePeerId: String,
        val peerConnection: RTCPeerConnection,
        var dataChannel: RTCDataChannel? = null,
        var featureDataChannel: RTCDataChannel? = null,
        var socket: Socket? = null,
        val openFuture: CompletableFuture<Unit> = CompletableFuture(),
        val pendingCandidates: MutableList<RTCIceCandidate> = CopyOnWriteArrayList(),
        val closed: AtomicBoolean = AtomicBoolean(false),
        val featureReceiveBuffer: StringBuilder = StringBuilder()
    )

    private var factory: PeerConnectionFactory? = null
    private var executor: ScheduledExecutorService = createExecutor()
    private val sessions = ConcurrentHashMap<String, ClientSession>()
    private var hostRoomId: String? = null
    private var hostPeerId: String? = null
    private var hostGamePort: Int = 0
    private var signalSender: ((WebRtcTunnelProxy.Signal) -> Unit)? = null
    private var iceServerUrls: List<String> = WebRtcTunnelProxy.DEFAULT_ICE_SERVERS
    private val pendingIceSignals = ConcurrentHashMap<String, MutableList<WebRtcTunnelProxy.Signal>>()
    private var clientServerSocket: ServerSocket? = null
    private var clientRunning = AtomicBoolean(false)
    private var currentMapPath: String? = null
    private var currentRequiredFeatures: List<String> = emptyList()
    private val peerFeatures = ConcurrentHashMap<String, Set<String>>()
    private var featureReceiver: ((FeatureMessage) -> Unit)? = null
    private val modTransfers = DesktopTransferTransport({ ensureFactory(); factory!! }, ::createRtcConfig,
        { work -> executor.execute { work() } })

    companion object {
        private const val GAME_DATA_CHANNEL_LABEL = "rwx-game"
        private const val FEATURE_DATA_CHANNEL_LABEL = "rwx-feature"

    }

    private fun createExecutor(): ScheduledExecutorService {
        return Executors.newScheduledThreadPool(config.executorThreads.coerceAtLeast(1)) { r ->
            Thread(r, "WebRtcTunnelProxy").also { it.isDaemon = true }
        }
    }

    override fun startHostSide(
        roomId: String,
        localPeerId: String,
        gamePort: Int,
        iceServers: List<String>,
        sendSignal: (WebRtcTunnelProxy.Signal) -> Unit,
    ) {
        ensureFactory()
        hostRoomId = roomId
        hostPeerId = localPeerId
        hostGamePort = gamePort
        iceServerUrls = normalizeIceServers(iceServers)
        signalSender = sendSignal
    }

    override fun startClientSide(
        roomId: String,
        localPeerId: String,
        hostPeerId: String,
        iceServers: List<String>,
        sendSignal: (WebRtcTunnelProxy.Signal) -> Unit,
    ): Int {
        ensureFactory()
        iceServerUrls = normalizeIceServers(iceServers)
        signalSender = sendSignal
        clientServerSocket = ServerSocket(0)
        val port = clientServerSocket!!.localPort
        clientRunning.set(true)
        executor.execute {
            acceptClientSockets(roomId, localPeerId, hostPeerId)
        }
        return port
    }

    override fun startTransferHost(roomId: String, localClientId: String, iceServers: List<String>,
        sendSignal: (WebRtcTunnelProxy.Signal) -> Unit, accept: (WebRtcTransferConnection) -> Unit) =
        modTransfers.startHost(roomId, localClientId, iceServers, sendSignal, accept)

    override fun openTransfer(roomId: String, sessionId: String, localClientId: String, hostClientId: String,
        iceServers: List<String>, sendSignal: (WebRtcTunnelProxy.Signal) -> Unit): WebRtcTransferConnection =
        modTransfers.open(roomId, sessionId, localClientId, hostClientId, iceServers, sendSignal)

    override fun handleTransferSignal(signal: WebRtcTunnelProxy.Signal) = modTransfers.handle(signal)
    override fun stopTransfers() = modTransfers.stop()

    override fun stop() {
        stopTransfers()
        clientRunning.set(false)
        runCatching { clientServerSocket?.close() }
        clientServerSocket = null
        sessions.values.forEach { closeSession(it) }
        sessions.clear()
        peerFeatures.clear()
        hostRoomId = null
        hostPeerId = null
        hostGamePort = 0
        currentMapPath = null
        currentRequiredFeatures = emptyList()
    }

    override fun broadcastMapFeatures(mapPath: String?, requiredFeatures: List<String>) {
        currentMapPath = mapPath
        currentRequiredFeatures = requiredFeatures.distinct()
        sessions.values.forEach { session ->
            sendFeatureMessage(
                session = session,
                type = "mapFeatures",
                mapPath = currentMapPath,
                requiredFeatures = currentRequiredFeatures,
            )
        }
    }

    override fun setFeatureReceiver(receiver: (FeatureMessage) -> Unit) { featureReceiver = receiver }

    override fun broadcastFeatureMessage(message: FeatureMessage, excludeClientId: String?) {
        sessions.values.forEach { session ->
            if (session.remotePeerId != excludeClientId && (message.toPeerId == null || message.toPeerId == session.remotePeerId)) {
                val channel = session.featureDataChannel ?: return@forEach
                if (channel.state != RTCDataChannelState.OPEN) return@forEach
                val encoded = P2PJson.encodeToString(message.copy(roomId = session.roomId,
                    fromPeerId = session.localPeerId, toPeerId = session.remotePeerId)) + "\n"
                runCatching { channel.send(RTCDataChannelBuffer(ByteBuffer.wrap(encoded.toByteArray(Charsets.UTF_8)), true)) }
            }
        }
    }

    override fun getConnectedPeerIds(): Set<String> =
        sessions.values.map { it.remotePeerId }.toSet()

    override fun peerFeatureSnapshot(): Map<String, Set<String>> = peerFeatures.toMap()

    override fun handleSignal(signal: WebRtcTunnelProxy.Signal) {
        if (!signal.isValid()) return
        val localPeerId = hostPeerId
        if (localPeerId != null && signal.toPeerId == localPeerId && signal.type == "offer") {
            handleHostOffer(signal, localPeerId)
            return
        }
        val sessionId = signal.sessionId ?: return
        val session = sessions[sessionId]
        if (session == null) {
            if (signal.type == "ice") {
                var signals = pendingIceSignals[sessionId]
                if (signals == null) {
                    val newSignals = CopyOnWriteArrayList<WebRtcTunnelProxy.Signal>()
                    signals = pendingIceSignals.putIfAbsent(sessionId, newSignals) ?: newSignals
                }
                signals.add(signal)
            }
            return
        }
        when (signal.type) {
            "answer" -> handleClientAnswer(session, signal)
            "ice" -> addRemoteIce(session, signal)
        }
    }

    private fun acceptClientSockets(roomId: String, localPeerId: String, remotePeerId: String) {
        val serverSocket = clientServerSocket ?: return
        while (clientRunning.get() && !serverSocket.isClosed) {
            val socket = try {
                serverSocket.accept()
            } catch (_: Exception) {
                break
            }
            executor.execute {
                startClientSession(roomId, localPeerId, remotePeerId, socket)
            }
        }
    }

    private fun startClientSession(roomId: String, localPeerId: String, remotePeerId: String, socket: Socket) {
        val sessionId = UUID.randomUUID().toString()
        val session = createSession(roomId, sessionId, localPeerId, remotePeerId)
        session.socket = socket
        sessions[sessionId] = session
        applyPendingIceSignals(session)

        val dataChannel = session.peerConnection.createDataChannel(GAME_DATA_CHANNEL_LABEL, RTCDataChannelInit().apply {
            ordered = true
        })
        session.dataChannel = dataChannel
        registerDataChannel(session, dataChannel)
        val featureDataChannel =
            session.peerConnection.createDataChannel(FEATURE_DATA_CHANNEL_LABEL, RTCDataChannelInit().apply {
                ordered = true
            })
        session.featureDataChannel = featureDataChannel
        registerDataChannel(session, featureDataChannel)

        session.peerConnection.createOffer(RTCOfferOptions(), object : CreateSessionDescriptionObserver {
            override fun onSuccess(description: RTCSessionDescription) {
                session.peerConnection.setLocalDescription(description, object : SetSessionDescriptionObserver {
                    override fun onSuccess() {
                        sendSessionDescription(session, "offer", description)
                    }

                    override fun onFailure(error: String) {
                        closeSession(session)
                    }
                })
            }

            override fun onFailure(error: String) {
                closeSession(session)
            }
        })

        try {
            session.openFuture.get(config.openTimeoutMs, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            logger.info { "WebRTC tunnel failed: ${e.message}" }
            closeSession(session)
        }
    }

    private fun handleHostOffer(signal: WebRtcTunnelProxy.Signal, localPeerId: String) {
        if (signal.roomId != hostRoomId || signal.sdp.isNullOrBlank() || signal.sdpType.isNullOrBlank()) return
        val remotePeerId = signal.fromPeerId ?: return
        val sessionId = signal.sessionId ?: return
        val session = createSession(signal.roomId!!, sessionId, localPeerId, remotePeerId)
        sessions[sessionId] = session
        applyPendingIceSignals(session)

        val remoteDescription = RTCSessionDescription(RTCSdpType.valueOf(signal.sdpType!!.uppercase()), signal.sdp!!)
        session.peerConnection.setRemoteDescription(remoteDescription, object : SetSessionDescriptionObserver {
            override fun onSuccess() {
                flushPendingCandidates(session)
                session.peerConnection.createAnswer(RTCAnswerOptions(), object : CreateSessionDescriptionObserver {
                    override fun onSuccess(description: RTCSessionDescription) {
                        session.peerConnection.setLocalDescription(description, object : SetSessionDescriptionObserver {
                            override fun onSuccess() {
                                sendSessionDescription(session, "answer", description)
                            }

                            override fun onFailure(error: String) {
                                closeSession(session)
                            }
                        })
                    }

                    override fun onFailure(error: String) {
                        closeSession(session)
                    }
                })
            }

            override fun onFailure(error: String) {
                closeSession(session)
            }
        })
    }

    private fun handleClientAnswer(session: ClientSession, signal: WebRtcTunnelProxy.Signal) {
        if (signal.sdp.isNullOrBlank() || signal.sdpType.isNullOrBlank()) return
        val description = RTCSessionDescription(RTCSdpType.valueOf(signal.sdpType!!.uppercase()), signal.sdp!!)
        session.peerConnection.setRemoteDescription(description, object : SetSessionDescriptionObserver {
            override fun onSuccess() {
                flushPendingCandidates(session)
            }

            override fun onFailure(error: String) {
                closeSession(session)
            }
        })
    }

    private fun addRemoteIce(session: ClientSession, signal: WebRtcTunnelProxy.Signal) {
        val sdp = signal.candidateSdp ?: return
        val candidate = RTCIceCandidate(signal.candidateSdpMid, signal.candidateSdpMLineIndex, sdp)
        if (session.peerConnection.remoteDescription == null) {
            session.pendingCandidates += candidate
        } else {
            session.peerConnection.addIceCandidate(candidate)
        }
    }

    private fun applyPendingIceSignals(session: ClientSession) {
        pendingIceSignals.remove(session.sessionId)?.forEach { addRemoteIce(session, it) }
    }

    private fun flushPendingCandidates(session: ClientSession) {
        session.pendingCandidates.forEach { session.peerConnection.addIceCandidate(it) }
        session.pendingCandidates.clear()
    }

    private fun createSession(
        roomId: String,
        sessionId: String,
        localPeerId: String,
        remotePeerId: String
    ): ClientSession {
        val peerConnection = factory!!.createPeerConnection(createRtcConfig(), object : PeerConnectionObserver {
            override fun onIceCandidate(candidate: RTCIceCandidate) {
                logger.info { "WebRTC ICE candidate session=$sessionId mid=${candidate.sdpMid} index=${candidate.sdpMLineIndex} server=${candidate.serverUrl} sdp=${candidate.sdp}" }
                signalSender?.invoke(
                    WebRtcTunnelProxy.Signal(
                        roomId = roomId,
                        sessionId = sessionId,
                        fromPeerId = localPeerId,
                        toPeerId = remotePeerId,
                        type = "ice",
                        candidateSdpMid = candidate.sdpMid,
                        candidateSdpMLineIndex = candidate.sdpMLineIndex,
                        candidateSdp = candidate.sdp
                    )
                )
            }

            override fun onDataChannel(dataChannel: RTCDataChannel) {
                val session = sessions[sessionId] ?: return
                when (dataChannel.label) {
                    FEATURE_DATA_CHANNEL_LABEL -> session.featureDataChannel = dataChannel
                    GAME_DATA_CHANNEL_LABEL -> session.dataChannel = dataChannel
                    else -> { dataChannel.close(); return }
                }
                registerDataChannel(session, dataChannel)
            }

            override fun onConnectionChange(state: RTCPeerConnectionState) {
                logger.info { "WebRTC peer connection state session=$sessionId state=$state" }
                if (state == RTCPeerConnectionState.FAILED || state == RTCPeerConnectionState.CLOSED) {
                    sessions[sessionId]?.let { closeSession(it) }
                }
            }

            override fun onIceConnectionChange(state: RTCIceConnectionState) {
                logger.info { "WebRTC ICE connection state session=$sessionId state=$state" }
            }

            override fun onIceGatheringChange(state: RTCIceGatheringState) {
                logger.info { "WebRTC ICE gathering state session=$sessionId state=$state" }
            }

            override fun onIceCandidateError(event: RTCPeerConnectionIceErrorEvent) {
                logger.info { "WebRTC ICE candidate error session=$sessionId url=${event.url} address=${event.address}:${event.port} code=${event.errorCode} text=${event.errorText}" }
            }
        })
        return ClientSession(roomId, sessionId, localPeerId, remotePeerId, peerConnection)
    }

    private fun registerDataChannel(session: ClientSession, dataChannel: RTCDataChannel) {
        if (dataChannel.label == FEATURE_DATA_CHANNEL_LABEL) {
            registerFeatureDataChannel(session, dataChannel)
            return
        }
        if (dataChannel.label != GAME_DATA_CHANNEL_LABEL) { dataChannel.close(); return }
        dataChannel.registerObserver(object : RTCDataChannelObserver {
            override fun onBufferedAmountChange(previousAmount: Long) {}

            override fun onStateChange() {
                if (session.closed.get()) return
                val state = runCatching { dataChannel.state }.getOrNull() ?: return
                logger.info { "WebRTC DataChannel state session=${session.sessionId} state=$state" }
                if (state == RTCDataChannelState.OPEN) {
                    if (session.socket == null && hostGamePort > 0) {
                        try {
                            session.socket = Socket().apply {
                                connect(InetSocketAddress("127.0.0.1", hostGamePort), config.socketConnectTimeoutMs)
                                soTimeout = config.socketReadTimeoutMs
                                tcpNoDelay = true
                            }
                        } catch (_: Exception) {
                            closeSession(session)
                            return
                        }
                    }
                    session.openFuture.complete(Unit)
                    session.socket?.let {
                        executor.execute { relaySocketToDataChannel(session) }
                    }
                } else if (state == RTCDataChannelState.CLOSED) {
                    closeSession(session)
                }
            }

            override fun onMessage(buffer: RTCDataChannelBuffer) {
                val socket = session.socket ?: return
                val data = ByteArray(buffer.data.remaining())
                buffer.data.get(data)
                try {
                    socket.outputStream.write(data)
                    socket.outputStream.flush()
                } catch (_: Exception) {
                    closeSession(session)
                }
            }
        })
    }

    private fun registerFeatureDataChannel(session: ClientSession, dataChannel: RTCDataChannel) {
        dataChannel.registerObserver(object : RTCDataChannelObserver {
            override fun onBufferedAmountChange(previousAmount: Long) {}

            override fun onStateChange() {
                if (session.closed.get()) return
                val state = runCatching { dataChannel.state }.getOrNull() ?: return
                logger.info { "WebRTC RWX feature DataChannel state session=${session.sessionId} state=$state" }
                if (state == RTCDataChannelState.OPEN && session.localPeerId != hostPeerId) {
                    sendFeatureMessage(session, "hello")
                }
            }

            override fun onMessage(buffer: RTCDataChannelBuffer) {
                val data = ByteArray(buffer.data.remaining())
                buffer.data.get(data)
                session.featureReceiveBuffer.append(data.toString(Charsets.UTF_8))
                while (true) {
                    val endIndex = session.featureReceiveBuffer.indexOf("\n")
                    if (endIndex == -1) {
                        return
                    }
                    val line = session.featureReceiveBuffer.substring(0, endIndex).trim()
                    session.featureReceiveBuffer.delete(0, endIndex + 1)
                    if (line.isEmpty()) {
                        continue
                    }
                    runCatching {
                        handleFeatureMessage(session, P2PJson.decodeFromString<FeatureMessage>(line))
                    }.onFailure { error ->
                        logger.info { "WebRTC RWX feature message decode failed: ${error.message}" }
                    }
                }
            }
        })
    }

    private fun sendFeatureMessage(
        session: ClientSession,
        type: String,
        mapPath: String? = null,
        requiredFeatures: List<String> = emptyList(),
    ) {
        val dataChannel = session.featureDataChannel ?: return
        if (runCatching { dataChannel.state }.getOrNull() != RTCDataChannelState.OPEN) {
            return
        }
        val gameEngine = GameEngine.getInstance()
        val message = FeatureMessage(
            roomId = session.roomId,
            fromPeerId = session.localPeerId,
            toPeerId = session.remotePeerId,
            type = type,
            gameVersionCode = gameEngine.getVersionCode(true),
            gameVersionString = gameEngine.getVersionName(),
            features = FeatureIds.currentClientFeatures(),
            mapPath = mapPath,
            requiredFeatures = requiredFeatures.distinct(),
        )
        val encoded = P2PJson.encodeToString(message) + "\n"
        runCatching {
            dataChannel.send(RTCDataChannelBuffer(ByteBuffer.wrap(encoded.toByteArray(Charsets.UTF_8)), true))
        }.onFailure { error ->
            logger.info { "WebRTC RWX feature message send failed: ${error.message}" }
        }
    }

    private fun handleFeatureMessage(session: ClientSession, message: FeatureMessage) {
        if (!message.isValid() || message.roomId != session.roomId) {
            return
        }
        if (message.toPeerId != null && message.toPeerId != session.localPeerId) {
            return
        }
        if (message.fromPeerId != session.remotePeerId) return
        featureReceiver?.invoke(message)
        when (message.type) {
            "hello" -> {
                message.fromPeerId?.let { peerFeatures[it] = message.features.toSet() }
                logger.info {
                    "WebRTC RWX feature hello from=${message.fromPeerId} features=${
                        message.features.joinToString(
                            ","
                        )
                    }"
                }
                sendFeatureMessage(session, "welcome")
                if (session.localPeerId == hostPeerId && (currentMapPath != null || currentRequiredFeatures.isNotEmpty())) {
                    sendFeatureMessage(
                        session = session,
                        type = "mapFeatures",
                        mapPath = currentMapPath,
                        requiredFeatures = currentRequiredFeatures,
                    )
                }
            }

            "welcome" -> {
                message.fromPeerId?.let { peerFeatures[it] = message.features.toSet() }
                logger.info {
                    "WebRTC RWX feature welcome from=${message.fromPeerId} features=${
                        message.features.joinToString(
                            ","
                        )
                    }"
                }
            }

            "mapFeatures" -> {
                handleWebRtcMapFeatures(message)
            }

            else -> {
                logger.info { "WebRTC RWX feature message type=${message.type} from=${message.fromPeerId}" }
            }
        }
    }

    private fun handleWebRtcMapFeatures(message: FeatureMessage) {
        val supportedFeatures = FeatureIds.currentClientFeatures().toSet()
        val missing = message.requiredFeatures.distinct().filterNot { supportedFeatures.contains(it) }
        if (missing.isEmpty()) {
            logger.info {
                "WebRTC RWX map features ok map=${message.mapPath} required=${
                    message.requiredFeatures.joinToString(
                        ","
                    )
                }"
            }
        } else {
            val text = "RWX map requires unsupported features: ${missing.joinToString(",")}"
            logger.info { text }
            NetworkEngine.reportDesync(text)
        }
    }

    private fun relaySocketToDataChannel(session: ClientSession) {
        val socket = session.socket ?: return
        val dataChannel = session.dataChannel ?: return
        val buffer = ByteArray(config.bufferSize)
        try {
            while (!session.closed.get()) {
                val length = try {
                    socket.inputStream.read(buffer)
                } catch (_: SocketTimeoutException) {
                    continue
                }
                if (length < 0) break
                if (length == 0) continue
                dataChannel.send(RTCDataChannelBuffer(ByteBuffer.wrap(buffer.copyOf(length)), true))
            }
        } catch (_: Exception) {
        } finally {
            closeSession(session)
        }
    }

    private fun sendSessionDescription(session: ClientSession, type: String, description: RTCSessionDescription) {
        logger.info { "WebRTC sending $type session=${session.sessionId} from=${session.localPeerId} to=${session.remotePeerId}" }
        signalSender?.invoke(
            WebRtcTunnelProxy.Signal(
                roomId = session.roomId,
                sessionId = session.sessionId,
                fromPeerId = session.localPeerId,
                toPeerId = session.remotePeerId,
                type = type,
                sdpType = description.sdpType.name,
                sdp = description.sdp
            )
        )
    }

    private fun closeSession(session: ClientSession) {
        if (!session.closed.compareAndSet(false, true)) return
        sessions.remove(session.sessionId)
        pendingIceSignals.remove(session.sessionId)
        peerFeatures.remove(session.remotePeerId)
        session.openFuture.cancel(false)
        runCatching { session.socket?.close() }
        runCatching { session.peerConnection.close() }
    }

    private fun ensureFactory() {
        if (factory == null) {
            factory = try {
                PeerConnectionFactory(AudioDeviceModule(AudioLayer.kDummyAudio))
            } catch (e: Throwable) {
                logger.error(e) { "Failed to initialize WebRTC native library" }
                throw IOException("Failed to initialize WebRTC native library: ${rootCauseMessage(e)}", e)
            }
        }
        if (executor.isShutdown) {
            executor = createExecutor()
        }
    }

    private fun createRtcConfig(servers: List<String> = iceServerUrls): RTCConfiguration {
        return RTCConfiguration().apply {
            normalizeIceServers(servers).forEach { url ->
                iceServers.add(createIceServer(url))
            }
        }
    }

    private fun createIceServer(value: String): RTCIceServer {
        val normalized = normalizeTurnCredentialUrl(value)
        val server = RTCIceServer()
        server.urls.add(normalized.url)
        if (normalized.username != null) server.username = normalized.username
        if (normalized.password != null) server.password = normalized.password
        return server
    }

    private data class NormalizedIceServer(
        val url: String,
        val username: String? = null,
        val password: String? = null
    )

    private fun normalizeTurnCredentialUrl(value: String): NormalizedIceServer {
        if (!value.startsWith("turn:") && !value.startsWith("turns:")) return NormalizedIceServer(value)
        return runCatching {
            val scheme = value.substringBefore(':')
            val rest = value.substringAfter(':')
            val userInfo = rest.substringBefore('@')
            if (userInfo == rest) return@runCatching NormalizedIceServer(value)
            val hostPart = rest.substringAfter('@')
            val username = URI("x://$userInfo").userInfo?.substringBefore(':') ?: userInfo.substringBefore(':')
            val password = URI("x://$userInfo").userInfo?.substringAfter(':', "") ?: userInfo.substringAfter(':', "")
            NormalizedIceServer("$scheme:$hostPart", username, password)
        }.getOrElse {
            NormalizedIceServer(value)
        }
    }

    private fun normalizeIceServers(values: List<String>): List<String> {
        val servers = values.map { it.trim() }
            .filter { it.startsWith("stun:") || it.startsWith("turn:") || it.startsWith("turns:") }
            .distinct()
        return servers.ifEmpty { WebRtcTunnelProxy.DEFAULT_ICE_SERVERS }
    }

    private fun rootCauseMessage(throwable: Throwable): String {
        var current = throwable
        while (current.cause != null) current = current.cause!!
        return current.message ?: current.javaClass.name
    }
}
