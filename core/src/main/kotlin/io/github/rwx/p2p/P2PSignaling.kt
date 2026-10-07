package io.github.rwx.p2p

import io.github.rwx.p2p.transfer.TransferIdentity
import kotlinx.serialization.Serializable
import java.util.UUID

internal fun serviceSignalingAvailable(config: P2PConfig.ServiceDiscoveryConfig): Boolean =
    config.enable && config.urls.isNotEmpty()

private fun hasValidTransferSignature(envelope: SignalEnvelope): Boolean {
    if (envelope.signerKey.isNullOrBlank() || envelope.signature.isNullOrBlank()) return false
    return runCatching {
        TransferIdentity.decode(envelope.signerKey!!, maximum = TransferIdentity.MAX_KEY_BYTES)
        TransferIdentity.decode(envelope.signature!!, maximum = TransferIdentity.MAX_SIGNATURE_BYTES)
    }.isSuccess
}

@Serializable
data class SignalEnvelope(
    var protocolVersion: Int? = null,
    var purpose: String? = null,
    var signerKey: String? = null,
    var signedPayload: String? = null,
    var signature: String? = null,
    var roomId: String? = null,
    var fromClientId: String? = null,
    var toClientId: String? = null,
    var type: String? = null,
    var sessionId: String? = null,
    var sdpType: String? = null,
    var sdp: String? = null,
    var candidateSdpMid: String? = null,
    var candidateSdpMLineIndex: Int = 0,
    var candidateSdp: String? = null,
    var messageId: String? = null,
    var seq: Long = 0,
    var createdAtMs: Long = 0,
) {
    fun isValid(): Boolean =
        protocolVersion == 1 && listOf(roomId, fromClientId, toClientId, sessionId, messageId).all {
            !it.isNullOrBlank() && it.length <= 128
        } && type in setOf("offer", "answer", "ice") && when (purpose) {
            "game" -> when (type) {
                "offer", "answer" -> sdpType.equals(type, ignoreCase = true) && !sdp.isNullOrBlank()
                "ice" -> !candidateSdp.isNullOrBlank() && candidateSdpMLineIndex in 0..65535
                else -> false
            }
            "transfer-v1" -> !signedPayload.isNullOrBlank() &&
                signedPayload!!.length <= 64 * 1024 && hasValidTransferSignature(this)
            else -> false
        }

    companion object {
        fun fromProxySignal(signal: WebRtcTunnelProxy.Signal, nowMs: Long): SignalEnvelope =
            SignalEnvelope(
                protocolVersion = 1,
                purpose = signal.purpose,
                roomId = signal.roomId,
                fromClientId = signal.fromPeerId,
                toClientId = signal.toPeerId,
                type = signal.type,
                sessionId = signal.sessionId,
                sdpType = signal.sdpType,
                sdp = signal.sdp,
                candidateSdpMid = signal.candidateSdpMid,
                candidateSdpMLineIndex = signal.candidateSdpMLineIndex,
                candidateSdp = signal.candidateSdp,
                messageId = UUID.randomUUID().toString(),
                createdAtMs = nowMs,
            )
    }

    fun toProxySignal(): WebRtcTunnelProxy.Signal {
        require(purpose == "game") { "Transfer signals require identity verification" }
        return WebRtcTunnelProxy.Signal(
            roomId = roomId,
            sessionId = sessionId,
            fromPeerId = fromClientId,
            toPeerId = toClientId,
            type = type,
            sdpType = sdpType,
            sdp = sdp,
            candidateSdpMid = candidateSdpMid,
            candidateSdpMLineIndex = candidateSdpMLineIndex,
            candidateSdp = candidateSdp,
        )
    }
}

class WebRtcSignalExchange(val roomId: String, val localClientId: String) {
    private data class EndpointState(var cursor: Long = 0, var failures: Int = 0, var lastPollMs: Long = 0)

    private var active = true
    private val endpoints = mutableMapOf<String, EndpointState>()
    private val receivedMessages = LinkedHashMap<String, Long>()

    @Synchronized
    fun isActive(): Boolean = active

    @Synchronized
    fun close() {
        active = false
        endpoints.clear()
        receivedMessages.clear()
    }

    @Synchronized
    fun prepareOutgoing(signal: WebRtcTunnelProxy.Signal, nowMs: Long): SignalEnvelope? {
        if (!active || signal.roomId != roomId || signal.fromPeerId != localClientId) return null
        return SignalEnvelope.fromProxySignal(signal, nowMs).takeIf { it.isValid() }
    }

    @Synchronized
    fun pollCursor(serviceUrl: String, nowMs: Long): Long? {
        if (!active) return null
        val endpoint = endpoints.getOrPut(serviceUrl) { EndpointState() }
        if (endpoint.failures > 0 && nowMs - endpoint.lastPollMs < backoffDelayMs(endpoint.failures)) return null
        return endpoint.cursor
    }

    @Synchronized
    fun receive(
        serviceUrl: String,
        envelopes: List<SignalEnvelope>,
        nowMs: Long,
        handle: (SignalEnvelope) -> Unit,
        onError: (Exception) -> Unit,
    ) {
        if (!active) return
        val endpoint = endpoints.getOrPut(serviceUrl) { EndpointState() }
        endpoint.lastPollMs = nowMs
        endpoint.failures = 0
        receivedMessages.entries.removeIf { nowMs - it.value >= 300_000L }
        for (envelope in envelopes.sortedBy { it.seq }) {
            if (!active) return
            if (envelope.roomId != roomId || envelope.toClientId != localClientId || envelope.seq <= endpoint.cursor) continue
            endpoint.cursor = envelope.seq
            if (envelope.fromClientId == localClientId || !envelope.isValid()) continue
            val messageId = envelope.messageId!!
            if (receivedMessages.putIfAbsent(messageId, nowMs) != null) continue
            while (receivedMessages.size > 10_000) receivedMessages.remove(receivedMessages.keys.first())
            try {
                handle(envelope)
            } catch (error: Exception) {
                onError(error)
            }
        }
    }

    @Synchronized
    fun recordFailure(serviceUrl: String, nowMs: Long) {
        if (!active) return
        val endpoint = endpoints.getOrPut(serviceUrl) { EndpointState() }
        endpoint.lastPollMs = nowMs
        endpoint.failures = (endpoint.failures + 1).coerceAtMost(3)
    }

    fun backoffDelayMs(consecutiveFailures: Int): Long = when {
        consecutiveFailures <= 0 -> 0L
        consecutiveFailures == 1 -> 2000L
        consecutiveFailures == 2 -> 4000L
        else -> 8000L
    }
}
