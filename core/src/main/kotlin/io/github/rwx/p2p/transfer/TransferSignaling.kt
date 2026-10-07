package io.github.rwx.p2p.transfer

import io.github.rwx.p2p.SignalEnvelope
import io.github.rwx.p2p.WebRtcTunnelProxy
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class TransferSignaling(
    private val identity: TransferIdentity,
    private val localClientId: String,
) {
    private data class Peer(
        val roomId: String,
        val clientId: String,
        val publicKey: String,
        val nonce: String,
        val expiresAt: Long,
        val sequence: AtomicLong = AtomicLong(),
    )
    private val peers = ConcurrentHashMap<String, Peer>()
    private val replay = TransferSignalReplayGuard()

    fun prepareClient(roomId: String, sessionId: String, hostClientId: String, hostKey: String,
        nowMs: Long = System.currentTimeMillis()) {
        TransferIdentity.decode(hostKey, maximum = TransferIdentity.MAX_KEY_BYTES)
        transferRequire(peers.putIfAbsent(sessionId, Peer(roomId, hostClientId, hostKey,
            UUID.randomUUID().toString(), nowMs + 120_000)) == null)
    }

    fun encode(signal: WebRtcTunnelProxy.Signal): SignalEnvelope {
        val peer = peers[signal.sessionId] ?: throw TransferException(TransferErrorCode.ROOM_CLOSED)
        transferRequire(signal.roomId == peer.roomId && signal.fromPeerId == localClientId && signal.toPeerId == peer.clientId)
        val sequence = peer.sequence.getAndIncrement()
        transferRequire(sequence <= TransferLimits.MAX_SIGNALS_PER_SESSION, TransferErrorCode.LIMIT_EXCEEDED)
        return identity.sign(signal, TransferIdentity.decode(peer.publicKey, maximum = TransferIdentity.MAX_KEY_BYTES).sha256(), peer.nonce, sequence)
    }

    @Synchronized
    fun decode(envelope: SignalEnvelope, hostedRoomId: String?, nowMs: Long = System.currentTimeMillis()): WebRtcTunnelProxy.Signal? {
        peers.entries.removeIf { it.value.expiresAt < nowMs - 30_000 }
        val id = envelope.sessionId ?: return null
        val peer = peers[id]
        if (peer == null && (hostedRoomId == null || envelope.roomId != hostedRoomId)) return null
        val roomId = peer?.roomId ?: hostedRoomId!!
        val verified = identity.verify(envelope, roomId, localClientId, peer?.publicKey, nowMs)
        val payload = verified.payload
        if (peer != null && (payload.nonce != peer.nonce || payload.signal.fromPeerId != peer.clientId)) return null
        if (!replay.accept(verified, nowMs)) return null
        if (peer == null) {
            peers[id] = Peer(roomId, envelope.fromClientId!!, verified.senderKey, payload.nonce, payload.expiresAtMs)
        }
        return payload.signal
    }

    fun peerIdentity(sessionId: String): String? = peers[sessionId]?.let {
        TransferIdentity.decode(it.publicKey, maximum = TransferIdentity.MAX_KEY_BYTES).sha256()
    }

    fun close(sessionId: String) {
        peers.remove(sessionId)
        replay.close(sessionId)
    }

    fun closeRoom(roomId: String) {
        peers.entries.filter { it.value.roomId == roomId }.forEach { close(it.key) }
    }
}
