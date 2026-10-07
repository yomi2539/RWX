package io.github.rwx.p2p

import kotlinx.serialization.Serializable
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference

interface WebRtcTunnelProxy {
    data class ProxyConfig(
        val bufferSize: Int = 65536,
        val openTimeoutMs: Long = 20000L,
        val socketConnectTimeoutMs: Int = 5000,
        val socketReadTimeoutMs: Int = 30000,
        val executorThreads: Int = 4,
    )

    @Serializable
    data class Signal(
        var magic: String = "rwx-p2p-webrtc",
        var schema: Int = 1,
        var roomId: String? = null,
        var sessionId: String? = null,
        var fromPeerId: String? = null,
        var toPeerId: String? = null,
        var type: String? = null,
        var sdpType: String? = null,
        var sdp: String? = null,
        var candidateSdpMid: String? = null,
        var candidateSdpMLineIndex: Int = 0,
        var candidateSdp: String? = null,
        var purpose: String = "game",
    ) {
        fun isValid(): Boolean =
            magic == "rwx-p2p-webrtc" &&
                    schema == 1 &&
                    !roomId.isNullOrBlank() &&
                    !sessionId.isNullOrBlank() &&
                    !fromPeerId.isNullOrBlank() &&
                    !toPeerId.isNullOrBlank() &&
                    !type.isNullOrBlank()
    }

    fun startHostSide(
        roomId: String,
        localPeerId: String,
        gamePort: Int,
        iceServers: List<String>,
        sendSignal: (Signal) -> Unit,
    )

    fun startClientSide(
        roomId: String,
        localPeerId: String,
        hostPeerId: String,
        iceServers: List<String>,
        sendSignal: (Signal) -> Unit,
    ): Int

    fun startTransferHost(
        roomId: String,
        localClientId: String,
        iceServers: List<String>,
        sendSignal: (Signal) -> Unit,
        accept: (WebRtcTransferConnection) -> Unit,
    )

    fun openTransfer(
        roomId: String,
        sessionId: String,
        localClientId: String,
        hostClientId: String,
        iceServers: List<String>,
        sendSignal: (Signal) -> Unit,
    ): WebRtcTransferConnection

    fun handleTransferSignal(signal: Signal)
    fun stopTransfers()
    fun stop()

    fun broadcastMapFeatures(mapPath: String?, requiredFeatures: List<String>)
    fun broadcastFeatureMessage(message: FeatureMessage, excludeClientId: String? = null)
    fun setFeatureReceiver(receiver: (FeatureMessage) -> Unit)

    fun getConnectedPeerIds(): Set<String>

    fun peerFeatureSnapshot(): Map<String, Set<String>>

    fun handleSignal(signal: Signal)

    companion object {
        val DEFAULT_ICE_SERVERS = listOf(
            "stun:stun.l.google.com:19302",
            "stun:stun1.l.google.com:19302",
        )

        private val factory = AtomicReference<WebRtcTunnelProxyFactory>(
            WebRtcTunnelProxyFactory { config -> UnsupportedWebRtcTunnelProxy(config) },
        )

        fun registerFactory(factory: WebRtcTunnelProxyFactory) {
            this.factory.set(factory)
        }

        fun create(config: ProxyConfig = ProxyConfig()): WebRtcTunnelProxy =
            factory.get().create(config)
    }
}

fun interface WebRtcTunnelProxyFactory {
    fun create(config: WebRtcTunnelProxy.ProxyConfig): WebRtcTunnelProxy
}

private class UnsupportedWebRtcTunnelProxy(
    private val config: WebRtcTunnelProxy.ProxyConfig,
) : WebRtcTunnelProxy {
    override fun startHostSide(
        roomId: String,
        localPeerId: String,
        gamePort: Int,
        iceServers: List<String>,
        sendSignal: (WebRtcTunnelProxy.Signal) -> Unit,
    ) {
        throw unsupported()
    }

    override fun startClientSide(
        roomId: String,
        localPeerId: String,
        hostPeerId: String,
        iceServers: List<String>,
        sendSignal: (WebRtcTunnelProxy.Signal) -> Unit,
    ): Int {
        throw unsupported()
    }

    override fun startTransferHost(roomId: String, localClientId: String, iceServers: List<String>,
        sendSignal: (WebRtcTunnelProxy.Signal) -> Unit, accept: (WebRtcTransferConnection) -> Unit) = throw unsupported()

    override fun openTransfer(roomId: String, sessionId: String, localClientId: String, hostClientId: String,
        iceServers: List<String>, sendSignal: (WebRtcTunnelProxy.Signal) -> Unit): WebRtcTransferConnection = throw unsupported()

    override fun handleTransferSignal(signal: WebRtcTunnelProxy.Signal) = Unit
    override fun stopTransfers() = Unit
    override fun stop() = Unit

    override fun broadcastMapFeatures(mapPath: String?, requiredFeatures: List<String>) = Unit
    override fun broadcastFeatureMessage(message: FeatureMessage, excludeClientId: String?) = Unit
    override fun setFeatureReceiver(receiver: (FeatureMessage) -> Unit) = Unit

    override fun getConnectedPeerIds(): Set<String> = emptySet()

    override fun peerFeatureSnapshot(): Map<String, Set<String>> = emptyMap()

    override fun handleSignal(signal: WebRtcTunnelProxy.Signal) = Unit

    private fun unsupported(): IOException =
        IOException("WebRTC DataChannel transport is not available on this platform: $config")
}
