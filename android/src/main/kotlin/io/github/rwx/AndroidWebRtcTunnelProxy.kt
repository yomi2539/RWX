package io.github.rwx

import android.content.Context
import com.corrodinggames.rts.gameFramework.GameEngine
import com.corrodinggames.rts.gameFramework.network.NetworkEngine
import io.github.rwx.p2p.FeatureIds
import io.github.rwx.p2p.FeatureMessage
import io.github.rwx.p2p.P2PJson
import io.github.rwx.p2p.WebRtcTunnelProxy
import org.webrtc.*
import java.io.IOException
import java.net.*
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean

class AndroidWebRtcTunnelProxy(
    context: Context,
    private val config: WebRtcTunnelProxy.ProxyConfig = WebRtcTunnelProxy.ProxyConfig(),
) : WebRtcTunnelProxy {
    private data class ClientSession(
        val roomId: String,
        val sessionId: String,
        val localPeerId: String,
        val remotePeerId: String,
        val peerConnection: PeerConnection,
        var dataChannel: DataChannel? = null,
        var featureDataChannel: DataChannel? = null,
        var socket: Socket? = null,
        val openFuture: CompletableFuture<Unit> = CompletableFuture(),
        val pendingCandidates: MutableList<IceCandidate> = CopyOnWriteArrayList(),
        val closed: AtomicBoolean = AtomicBoolean(false),
        val featureReceiveBuffer: StringBuilder = StringBuilder(),
    )

    private val appContext = context.applicationContext
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

    companion object {
        private const val GAME_DATA_CHANNEL_LABEL = "rwx-game"
        private const val FEATURE_DATA_CHANNEL_LABEL = "rwx-feature"
        private val factoryInitialized = AtomicBoolean(false)
    }

    private fun createExecutor(): ScheduledExecutorService =
        Executors.newScheduledThreadPool(config.executorThreads.coerceAtLeast(1)) { runnable ->
            Thread(runnable, "AndroidWebRtcTunnelProxy").also { it.isDaemon = true }
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

    override fun stop() {
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
        val session = try {
            createSession(roomId, sessionId, localPeerId, remotePeerId)
        } catch (error: Exception) {
            GameEngine.log("Android WebRTC session creation failed: ${error.message}")
            runCatching { socket.close() }
            return
        }
        session.socket = socket
        sessions[sessionId] = session
        applyPendingIceSignals(session)

        val dataChannel = session.peerConnection.createDataChannel(
            GAME_DATA_CHANNEL_LABEL,
            DataChannel.Init().apply { ordered = true },
        )
        session.dataChannel = dataChannel
        registerDataChannel(session, dataChannel)

        val featureDataChannel = session.peerConnection.createDataChannel(
            FEATURE_DATA_CHANNEL_LABEL,
            DataChannel.Init().apply { ordered = true },
        )
        session.featureDataChannel = featureDataChannel
        registerDataChannel(session, featureDataChannel)

        session.peerConnection.createOffer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(description: SessionDescription?) {
                val sessionDescription = description ?: return closeSession(session)
                session.peerConnection.setLocalDescription(object : SimpleSdpObserver() {
                    override fun onSetSuccess() {
                        sendSessionDescription(session, "offer", sessionDescription)
                    }

                    override fun onSetFailure(error: String?) {
                        closeSession(session)
                    }
                }, sessionDescription)
            }

            override fun onCreateFailure(error: String?) {
                closeSession(session)
            }
        }, MediaConstraints())

        try {
            session.openFuture.get(config.openTimeoutMs, TimeUnit.MILLISECONDS)
        } catch (error: Exception) {
            GameEngine.log("Android WebRTC tunnel failed: ${error.message}")
            closeSession(session)
        }
    }

    private fun handleHostOffer(signal: WebRtcTunnelProxy.Signal, localPeerId: String) {
        if (signal.roomId != hostRoomId || signal.sdp.isNullOrBlank() || signal.sdpType.isNullOrBlank()) return
        val remotePeerId = signal.fromPeerId ?: return
        val sessionId = signal.sessionId ?: return
        val session = try {
            createSession(signal.roomId!!, sessionId, localPeerId, remotePeerId)
        } catch (error: Exception) {
            GameEngine.log("Android WebRTC host session creation failed: ${error.message}")
            return
        }
        sessions[sessionId] = session
        applyPendingIceSignals(session)

        val remoteDescription = SessionDescription(parseSdpType(signal.sdpType!!), signal.sdp!!)
        session.peerConnection.setRemoteDescription(object : SimpleSdpObserver() {
            override fun onSetSuccess() {
                flushPendingCandidates(session)
                session.peerConnection.createAnswer(object : SimpleSdpObserver() {
                    override fun onCreateSuccess(description: SessionDescription?) {
                        val sessionDescription = description ?: return closeSession(session)
                        session.peerConnection.setLocalDescription(object : SimpleSdpObserver() {
                            override fun onSetSuccess() {
                                sendSessionDescription(session, "answer", sessionDescription)
                            }

                            override fun onSetFailure(error: String?) {
                                closeSession(session)
                            }
                        }, sessionDescription)
                    }

                    override fun onCreateFailure(error: String?) {
                        closeSession(session)
                    }
                }, MediaConstraints())
            }

            override fun onSetFailure(error: String?) {
                closeSession(session)
            }
        }, remoteDescription)
    }

    private fun handleClientAnswer(session: ClientSession, signal: WebRtcTunnelProxy.Signal) {
        if (signal.sdp.isNullOrBlank() || signal.sdpType.isNullOrBlank()) return
        val description = SessionDescription(parseSdpType(signal.sdpType!!), signal.sdp!!)
        session.peerConnection.setRemoteDescription(object : SimpleSdpObserver() {
            override fun onSetSuccess() {
                flushPendingCandidates(session)
            }

            override fun onSetFailure(error: String?) {
                closeSession(session)
            }
        }, description)
    }

    private fun addRemoteIce(session: ClientSession, signal: WebRtcTunnelProxy.Signal) {
        val sdp = signal.candidateSdp ?: return
        val candidate = IceCandidate(signal.candidateSdpMid, signal.candidateSdpMLineIndex, sdp)
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
        remotePeerId: String,
    ): ClientSession {
        val peerConnection = factory?.createPeerConnection(createRtcConfig(), object : PeerConnection.Observer {
            override fun onSignalingChange(state: PeerConnection.SignalingState?) = Unit

            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                GameEngine.log("Android WebRTC ICE connection state session=$sessionId state=$state")
                if (state == PeerConnection.IceConnectionState.FAILED ||
                    state == PeerConnection.IceConnectionState.CLOSED
                ) {
                    sessions[sessionId]?.let { closeSession(it) }
                }
            }

            override fun onIceConnectionReceivingChange(receiving: Boolean) = Unit

            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {
                GameEngine.log("Android WebRTC ICE gathering state session=$sessionId state=$state")
            }

            override fun onIceCandidate(candidate: IceCandidate?) {
                if (candidate == null) return
                GameEngine.log("Android WebRTC ICE candidate session=$sessionId mid=${candidate.sdpMid} index=${candidate.sdpMLineIndex} sdp=${candidate.sdp}")
                signalSender?.invoke(
                    WebRtcTunnelProxy.Signal(
                        roomId = roomId,
                        sessionId = sessionId,
                        fromPeerId = localPeerId,
                        toPeerId = remotePeerId,
                        type = "ice",
                        candidateSdpMid = candidate.sdpMid,
                        candidateSdpMLineIndex = candidate.sdpMLineIndex,
                        candidateSdp = candidate.sdp,
                    )
                )
            }

            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) = Unit

            override fun onAddStream(stream: MediaStream?) = Unit

            override fun onRemoveStream(stream: MediaStream?) = Unit

            override fun onDataChannel(dataChannel: DataChannel?) {
                val channel = dataChannel ?: return
                val session = sessions[sessionId] ?: return
                if (channel.label() == FEATURE_DATA_CHANNEL_LABEL) {
                    session.featureDataChannel = channel
                } else {
                    session.dataChannel = channel
                }
                registerDataChannel(session, channel)
            }

            override fun onRenegotiationNeeded() = Unit

            override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) = Unit

            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                GameEngine.log("Android WebRTC peer connection state session=$sessionId state=$newState")
                if (newState == PeerConnection.PeerConnectionState.FAILED ||
                    newState == PeerConnection.PeerConnectionState.CLOSED
                ) {
                    sessions[sessionId]?.let { closeSession(it) }
                }
            }
        }) ?: throw IOException("Android WebRTC failed to create peer connection")
        return ClientSession(roomId, sessionId, localPeerId, remotePeerId, peerConnection)
    }

    private fun registerDataChannel(session: ClientSession, dataChannel: DataChannel) {
        if (dataChannel.label() == FEATURE_DATA_CHANNEL_LABEL) {
            registerFeatureDataChannel(session, dataChannel)
            return
        }
        dataChannel.registerObserver(object : DataChannel.Observer {
            override fun onBufferedAmountChange(previousAmount: Long) = Unit

            override fun onStateChange() {
                if (session.closed.get()) return
                val state = runCatching { dataChannel.state() }.getOrNull() ?: return
                GameEngine.log("Android WebRTC DataChannel state session=${session.sessionId} state=$state")
                if (state == DataChannel.State.OPEN) {
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
                } else if (state == DataChannel.State.CLOSED) {
                    closeSession(session)
                }
            }

            override fun onMessage(buffer: DataChannel.Buffer?) {
                val messageBuffer = buffer ?: return
                val socket = session.socket ?: return
                val data = ByteArray(messageBuffer.data.remaining())
                messageBuffer.data.get(data)
                try {
                    socket.outputStream.write(data)
                    socket.outputStream.flush()
                } catch (_: Exception) {
                    closeSession(session)
                }
            }
        })
    }

    private fun registerFeatureDataChannel(session: ClientSession, dataChannel: DataChannel) {
        dataChannel.registerObserver(object : DataChannel.Observer {
            override fun onBufferedAmountChange(previousAmount: Long) = Unit

            override fun onStateChange() {
                if (session.closed.get()) return
                val state = runCatching { dataChannel.state() }.getOrNull() ?: return
                GameEngine.log("Android WebRTC RWX feature DataChannel state session=${session.sessionId} state=$state")
                if (state == DataChannel.State.OPEN && session.localPeerId != hostPeerId) {
                    sendFeatureMessage(session, "hello")
                }
            }

            override fun onMessage(buffer: DataChannel.Buffer?) {
                val messageBuffer = buffer ?: return
                val data = ByteArray(messageBuffer.data.remaining())
                messageBuffer.data.get(data)
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
                        GameEngine.log("Android WebRTC RWX feature message decode failed: ${error.message}")
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
        if (runCatching { dataChannel.state() }.getOrNull() != DataChannel.State.OPEN) {
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
            dataChannel.send(DataChannel.Buffer(ByteBuffer.wrap(encoded.toByteArray(Charsets.UTF_8)), true))
        }.onFailure { error ->
            GameEngine.log("Android WebRTC RWX feature message send failed: ${error.message}")
        }
    }

    private fun handleFeatureMessage(session: ClientSession, message: FeatureMessage) {
        if (!message.isValid() || message.roomId != session.roomId) {
            return
        }
        if (message.toPeerId != null && message.toPeerId != session.localPeerId) {
            return
        }
        when (message.type) {
            "hello" -> {
                message.fromPeerId?.let { peerFeatures[it] = message.features.toSet() }
                GameEngine.log(
                    "Android WebRTC RWX feature hello from=${message.fromPeerId} features=${
                        message.features.joinToString(
                            ","
                        )
                    }"
                )
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
                GameEngine.log(
                    "Android WebRTC RWX feature welcome from=${message.fromPeerId} features=${
                        message.features.joinToString(
                            ","
                        )
                    }"
                )
            }

            "mapFeatures" -> {
                handleWebRtcMapFeatures(message)
            }

            else -> {
                GameEngine.log("Android WebRTC RWX feature message type=${message.type} from=${message.fromPeerId}")
            }
        }
    }

    private fun handleWebRtcMapFeatures(message: FeatureMessage) {
        val supportedFeatures = FeatureIds.currentClientFeatures().toSet()
        val missing = message.requiredFeatures.distinct().filterNot { supportedFeatures.contains(it) }
        if (missing.isEmpty()) {
            GameEngine.log(
                "Android WebRTC RWX map features ok map=${message.mapPath} required=${
                    message.requiredFeatures.joinToString(
                        ","
                    )
                }"
            )
        } else {
            val text = "RWX map requires unsupported features: ${missing.joinToString(",")}"
            GameEngine.log(text)
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
                dataChannel.send(DataChannel.Buffer(ByteBuffer.wrap(buffer.copyOf(length)), true))
            }
        } catch (_: Exception) {
        } finally {
            closeSession(session)
        }
    }

    private fun sendSessionDescription(session: ClientSession, type: String, description: SessionDescription) {
        GameEngine.log("Android WebRTC sending $type session=${session.sessionId} from=${session.localPeerId} to=${session.remotePeerId}")
        signalSender?.invoke(
            WebRtcTunnelProxy.Signal(
                roomId = session.roomId,
                sessionId = session.sessionId,
                fromPeerId = session.localPeerId,
                toPeerId = session.remotePeerId,
                type = type,
                sdpType = description.type.name,
                sdp = description.description,
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
                if (factoryInitialized.compareAndSet(false, true)) {
                    PeerConnectionFactory.initialize(
                        PeerConnectionFactory.InitializationOptions.builder(appContext)
                            .setEnableInternalTracer(false)
                            .createInitializationOptions()
                    )
                }
                PeerConnectionFactory.builder()
                    .setOptions(PeerConnectionFactory.Options())
                    .createPeerConnectionFactory()
            } catch (error: Throwable) {
                logger.error(error) { "Failed to initialize Android WebRTC native library" }
                throw IOException(
                    "Failed to initialize Android WebRTC native library: ${rootCauseMessage(error)}",
                    error
                )
            }
        }
        if (executor.isShutdown) {
            executor = createExecutor()
        }
    }

    private fun createRtcConfig(): PeerConnection.RTCConfiguration =
        PeerConnection.RTCConfiguration(
            normalizeIceServers(iceServerUrls).map(::createIceServer)
        )

    private fun createIceServer(value: String): PeerConnection.IceServer {
        val normalized = normalizeTurnCredentialUrl(value)
        val builder = PeerConnection.IceServer.builder(normalized.url)
        if (normalized.username != null) builder.setUsername(normalized.username)
        if (normalized.password != null) builder.setPassword(normalized.password)
        return builder.createIceServer()
    }

    private data class NormalizedIceServer(
        val url: String,
        val username: String? = null,
        val password: String? = null,
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

    private fun parseSdpType(value: String): SessionDescription.Type =
        when (value.lowercase()) {
            "offer" -> SessionDescription.Type.OFFER
            "answer" -> SessionDescription.Type.ANSWER
            "pranswer" -> SessionDescription.Type.PRANSWER
            else -> SessionDescription.Type.valueOf(value.uppercase())
        }

    private open class SimpleSdpObserver : SdpObserver {
        override fun onCreateSuccess(description: SessionDescription?) = Unit
        override fun onSetSuccess() = Unit
        override fun onCreateFailure(error: String?) = Unit
        override fun onSetFailure(error: String?) = Unit
    }

    private fun rootCauseMessage(throwable: Throwable): String {
        var current = throwable
        while (current.cause != null) current = current.cause!!
        return current.message ?: current.javaClass.name
    }
}
