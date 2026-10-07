package io.github.rwx

import io.github.rwx.p2p.WebRtcTransferConnection
import io.github.rwx.p2p.WebRtcTransferPipe
import io.github.rwx.p2p.WebRtcTunnelProxy
import org.webrtc.*
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

internal class AndroidTransferTransport(
    private val factory: () -> PeerConnectionFactory,
    private val configuration: (List<String>) -> PeerConnection.RTCConfiguration,
    private val release: (() -> Unit) -> Unit,
) {
    private data class Host(val room: String, val local: String, val ice: List<String>,
        val send: (WebRtcTunnelProxy.Signal) -> Unit, val accept: (WebRtcTransferConnection) -> Unit)
    private class Session(val room: String, val id: String, val local: String, val remote: String,
        val send: (WebRtcTunnelProxy.Signal) -> Unit) {
        lateinit var peer: PeerConnection
        lateinit var pipe: WebRtcTransferPipe
        var channel: DataChannel? = null
        var remoteReady = false
        val pending = mutableListOf<IceCandidate>()
        val closed = AtomicBoolean(false)
    }
    private data class Pending(val signal: WebRtcTunnelProxy.Signal, val created: Long)
    private val sessions = ConcurrentHashMap<String, Session>()
    private val pending = mutableMapOf<String, MutableList<Pending>>()
    private var host: Host? = null

    @Synchronized
    fun startHost(room: String, local: String, ice: List<String>, send: (WebRtcTunnelProxy.Signal) -> Unit,
        accept: (WebRtcTransferConnection) -> Unit) {
        stop()
        host = Host(room, local, ice, send, accept)
    }

    @Synchronized
    fun open(room: String, id: String, local: String, remote: String, ice: List<String>,
        send: (WebRtcTunnelProxy.Signal) -> Unit): WebRtcTransferConnection {
        require(!sessions.containsKey(id) && sessions.size < 4)
        val session = create(room, id, local, remote, ice, send)
        try {
            attach(session, session.peer.createDataChannel(WebRtcTransferPipe.LABEL, DataChannel.Init().apply { ordered = true }))
            session.peer.createOffer(descriptionObserver(session, "offer"), MediaConstraints())
        } catch (error: Exception) {
            session.pipe.fail(error)
            throw error
        }
        return session.pipe
    }

    @Synchronized
    fun handle(signal: WebRtcTunnelProxy.Signal) {
        if (!signal.isValid() || signal.purpose != WebRtcTransferPipe.LABEL) return
        val id = signal.sessionId!!
        val session = sessions[id]
        if (session == null) {
            val owner = host ?: return
            if (signal.roomId != owner.room || signal.toPeerId != owner.local) return
            if (signal.type == "ice") {
                val now = System.currentTimeMillis()
                pending.entries.removeIf { it.value.firstOrNull()?.created?.let { time -> now - time > 20_000 } != false }
                if (pending.size < 16 || id in pending) {
                    val list = pending.getOrPut(id) { mutableListOf() }
                    if (list.size < 256) list += Pending(signal.copy(), now)
                }
                return
            }
            if (signal.type != "offer" || sessions.size >= 4 || !signal.sdpType.equals("offer", true) || signal.sdp.isNullOrBlank()) return
            val created = create(owner.room, id, owner.local, signal.fromPeerId!!, owner.ice, owner.send)
            pending.remove(id)?.forEach { if (it.signal.fromPeerId == created.remote) addIce(created, it.signal) }
            try {
                owner.accept(created.pipe)
                setRemote(created, signal, true)
            } catch (error: Exception) { created.pipe.fail(error) }
            return
        }
        if (session.closed.get() || signal.roomId != session.room || signal.fromPeerId != session.remote || signal.toPeerId != session.local) return
        when (signal.type) {
            "answer" -> if (!session.remoteReady) setRemote(session, signal, false)
            "ice" -> addIce(session, signal)
        }
    }

    private fun create(room: String, id: String, local: String, remote: String, ice: List<String>,
        send: (WebRtcTunnelProxy.Signal) -> Unit): Session {
        val session = Session(room, id, local, remote, send)
        session.pipe = WebRtcTransferPipe(id, remote,
            bufferedBytes = { session.channel?.bufferedAmount() ?: Long.MAX_VALUE },
            sendPacket = { bytes ->
                val channel = session.channel ?: throw IOException("Transfer channel is not open")
                if (!channel.send(DataChannel.Buffer(ByteBuffer.wrap(bytes), true))) {
                    val error = IOException("Could not send transfer packet")
                    session.pipe.fail(error)
                    throw error
                }
            }, closeNative = { close(session) })
        session.peer = factory().createPeerConnection(configuration(ice), object : PeerConnection.Observer {
            override fun onSignalingChange(state: PeerConnection.SignalingState?) = Unit
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                if (state == PeerConnection.IceConnectionState.CLOSED || state == PeerConnection.IceConnectionState.FAILED) session.pipe.fail()
            }
            override fun onIceConnectionReceivingChange(receiving: Boolean) = Unit
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) = Unit
            override fun onIceCandidate(candidate: IceCandidate?) {
                if (candidate == null) return
                emit(session, WebRtcTunnelProxy.Signal(roomId = room, sessionId = id, fromPeerId = local, toPeerId = remote,
                    type = "ice", candidateSdpMid = candidate.sdpMid, candidateSdpMLineIndex = candidate.sdpMLineIndex,
                    candidateSdp = candidate.sdp, purpose = WebRtcTransferPipe.LABEL))
            }
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) = Unit
            override fun onAddStream(stream: MediaStream?) = Unit
            override fun onRemoveStream(stream: MediaStream?) = Unit
            override fun onDataChannel(channel: DataChannel?) {
                if (channel == null) return
                if (session.closed.get() || channel.label() != WebRtcTransferPipe.LABEL || session.channel != null) {
                    channel.close()
                    channel.dispose()
                    return
                }
                attach(session, channel)
            }
            override fun onRenegotiationNeeded() = Unit
            override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) = Unit
            override fun onConnectionChange(state: PeerConnection.PeerConnectionState?) {
                if (state == PeerConnection.PeerConnectionState.CLOSED || state == PeerConnection.PeerConnectionState.FAILED) session.pipe.fail()
            }
        }) ?: throw IOException("Could not create transfer peer connection")
        if (session.closed.get()) {
            session.peer.close()
            session.peer.dispose()
            throw IOException("Transfer peer closed during creation")
        }
        sessions[id] = session
        return session
    }

    private fun attach(session: Session, channel: DataChannel) {
        session.channel = channel
        channel.registerObserver(object : DataChannel.Observer {
            override fun onBufferedAmountChange(previousAmount: Long) = session.pipe.onBufferedAmountChange()
            override fun onStateChange() {
                when (channel.state()) {
                    DataChannel.State.OPEN -> session.pipe.onOpen()
                    DataChannel.State.CLOSED -> session.pipe.fail()
                    else -> Unit
                }
            }
            override fun onMessage(buffer: DataChannel.Buffer) {
                if (!buffer.binary || buffer.data.remaining() > WebRtcTransferPipe.MAX_PACKET_BYTES) {
                    session.pipe.fail(IOException("Invalid transfer packet"))
                    return
                }
                session.pipe.onPacket(ByteArray(buffer.data.remaining()).also(buffer.data::get))
            }
        })
        if (channel.state() == DataChannel.State.OPEN) session.pipe.onOpen()
    }

    private fun descriptionObserver(session: Session, type: String) = object : SimpleSdpObserver() {
        override fun onCreateSuccess(description: SessionDescription?) {
            if (session.closed.get() || description == null) return
            session.peer.setLocalDescription(object : SimpleSdpObserver() {
                override fun onSetSuccess() {
                    emit(session, WebRtcTunnelProxy.Signal(roomId = session.room, sessionId = session.id,
                        fromPeerId = session.local, toPeerId = session.remote, type = type,
                        sdpType = description.type.canonicalForm(), sdp = description.description, purpose = WebRtcTransferPipe.LABEL))
                }
                override fun onSetFailure(error: String?) = session.pipe.fail(IOException("Could not set transfer session description"))
            }, description)
        }
        override fun onCreateFailure(error: String?) = session.pipe.fail(IOException("Could not create transfer session description"))
    }

    private fun setRemote(session: Session, signal: WebRtcTunnelProxy.Signal, answer: Boolean) {
        val expected = if (answer) "offer" else "answer"
        if (!signal.sdpType.equals(expected, true) || signal.sdp.isNullOrBlank()) return
        val description = SessionDescription(if (answer) SessionDescription.Type.OFFER else SessionDescription.Type.ANSWER, signal.sdp)
        session.peer.setRemoteDescription(object : SimpleSdpObserver() {
            override fun onSetSuccess() {
                synchronized(this@AndroidTransferTransport) {
                    if (session.closed.get()) return
                    session.remoteReady = true
                    session.pending.forEach(session.peer::addIceCandidate)
                    session.pending.clear()
                    if (answer) session.peer.createAnswer(descriptionObserver(session, "answer"), MediaConstraints())
                }
            }
            override fun onSetFailure(error: String?) = session.pipe.fail(IOException("Could not accept transfer session description"))
        }, description)
    }

    private fun addIce(session: Session, signal: WebRtcTunnelProxy.Signal) {
        if (signal.candidateSdp.isNullOrBlank() || signal.candidateSdpMLineIndex !in 0..65535) return
        val candidate = IceCandidate(signal.candidateSdpMid, signal.candidateSdpMLineIndex, signal.candidateSdp)
        if (session.remoteReady) session.peer.addIceCandidate(candidate)
        else if (session.pending.size < 256) session.pending += candidate
        else session.pipe.fail(IOException("Too many transfer ICE candidates"))
    }

    private fun emit(session: Session, signal: WebRtcTunnelProxy.Signal) {
        if (!session.closed.get()) {
            try { session.send(signal) } catch (error: Exception) { session.pipe.fail(error) }
        }
    }

    private fun close(session: Session) {
        if (!session.closed.compareAndSet(false, true)) return
        sessions.remove(session.id, session)
        release {
            runCatching { session.channel?.close() }
            runCatching { session.channel?.dispose() }
            runCatching { session.peer.close() }
            runCatching { session.peer.dispose() }
        }
    }

    @Synchronized fun stop() {
        host = null
        pending.clear()
        sessions.values.toList().forEach { it.pipe.close() }
    }

    private open class SimpleSdpObserver : SdpObserver {
        override fun onCreateSuccess(description: SessionDescription?) = Unit
        override fun onSetSuccess() = Unit
        override fun onCreateFailure(error: String?) = Unit
        override fun onSetFailure(error: String?) = Unit
    }
}
