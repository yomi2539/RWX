package io.github.rwx.p2p

import dev.onvoid.webrtc.*
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

internal class DesktopTransferTransport(
    private val factory: () -> PeerConnectionFactory,
    private val configuration: (List<String>) -> RTCConfiguration,
    private val release: (() -> Unit) -> Unit,
) {
    private data class Host(val room: String, val local: String, val ice: List<String>,
        val send: (WebRtcTunnelProxy.Signal) -> Unit, val accept: (WebRtcTransferConnection) -> Unit)
    private class Session(val room: String, val id: String, val local: String, val remote: String,
        val send: (WebRtcTunnelProxy.Signal) -> Unit) {
        lateinit var peer: RTCPeerConnection
        lateinit var pipe: WebRtcTransferPipe
        var channel: RTCDataChannel? = null
        var remoteReady = false
        val pending = mutableListOf<RTCIceCandidate>()
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
            val channel = session.peer.createDataChannel(WebRtcTransferPipe.LABEL, RTCDataChannelInit().apply { ordered = true })
            attach(session, channel)
            session.peer.createOffer(RTCOfferOptions(), descriptionObserver(session, "offer"))
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
        var session = sessions[id]
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
            session = create(owner.room, id, owner.local, signal.fromPeerId!!, owner.ice, owner.send)
            val created = session
            pending.remove(id)?.forEach {
                if (it.signal.fromPeerId == created.remote) addIce(created, it.signal)
            }
            try {
                owner.accept(created.pipe)
                setRemote(created, signal, answer = true)
            } catch (error: Exception) { created.pipe.fail(error) }
            return
        }
        if (session.closed.get() || signal.roomId != session.room || signal.fromPeerId != session.remote || signal.toPeerId != session.local) return
        when (signal.type) {
            "answer" -> if (!session.remoteReady) setRemote(session, signal, answer = false)
            "ice" -> addIce(session, signal)
        }
    }

    private fun create(room: String, id: String, local: String, remote: String, ice: List<String>,
        send: (WebRtcTunnelProxy.Signal) -> Unit): Session {
        val session = Session(room, id, local, remote, send)
        session.pipe = WebRtcTransferPipe(id, remote,
            bufferedBytes = { session.channel?.bufferedAmount ?: Long.MAX_VALUE },
            sendPacket = { bytes ->
                val channel = session.channel ?: throw IOException("Transfer channel is not open")
                try { channel.send(RTCDataChannelBuffer(ByteBuffer.wrap(bytes), true)) }
                catch (error: Exception) { session.pipe.fail(error); throw error }
            }, closeNative = { close(session) })
        session.peer = factory().createPeerConnection(configuration(ice), object : PeerConnectionObserver {
            override fun onIceCandidate(candidate: RTCIceCandidate) {
                emit(session, WebRtcTunnelProxy.Signal(roomId = room, sessionId = id, fromPeerId = local, toPeerId = remote,
                    type = "ice", candidateSdpMid = candidate.sdpMid, candidateSdpMLineIndex = candidate.sdpMLineIndex,
                    candidateSdp = candidate.sdp, purpose = WebRtcTransferPipe.LABEL))
            }
            override fun onDataChannel(channel: RTCDataChannel) {
                if (session.closed.get() || channel.label != WebRtcTransferPipe.LABEL || session.channel != null) {
                    channel.close()
                    return
                }
                attach(session, channel)
            }
            override fun onConnectionChange(state: RTCPeerConnectionState) {
                if (state == RTCPeerConnectionState.CLOSED || state == RTCPeerConnectionState.FAILED) session.pipe.fail()
            }
        })
        if (session.closed.get()) {
            session.peer.close()
            throw IOException("Transfer peer closed during creation")
        }
        sessions[id] = session
        return session
    }

    private fun attach(session: Session, channel: RTCDataChannel) {
        session.channel = channel
        channel.registerObserver(object : RTCDataChannelObserver {
            override fun onBufferedAmountChange(previousAmount: Long) = session.pipe.onBufferedAmountChange()
            override fun onStateChange() {
                when (channel.state) {
                    RTCDataChannelState.OPEN -> session.pipe.onOpen()
                    RTCDataChannelState.CLOSED -> session.pipe.fail()
                    else -> Unit
                }
            }
            override fun onMessage(buffer: RTCDataChannelBuffer) {
                if (!buffer.binary || buffer.data.remaining() > WebRtcTransferPipe.MAX_PACKET_BYTES) {
                    session.pipe.fail(IOException("Invalid transfer packet"))
                    return
                }
                session.pipe.onPacket(ByteArray(buffer.data.remaining()).also(buffer.data::get))
            }
        })
        if (channel.state == RTCDataChannelState.OPEN) session.pipe.onOpen()
    }

    private fun descriptionObserver(session: Session, type: String) = object : CreateSessionDescriptionObserver {
        override fun onSuccess(description: RTCSessionDescription) {
            if (session.closed.get()) return
            session.peer.setLocalDescription(description, object : SetSessionDescriptionObserver {
                override fun onSuccess() {
                    emit(session, WebRtcTunnelProxy.Signal(roomId = session.room, sessionId = session.id,
                        fromPeerId = session.local, toPeerId = session.remote, type = type,
                        sdpType = description.sdpType.name, sdp = description.sdp, purpose = WebRtcTransferPipe.LABEL))
                }
                override fun onFailure(error: String) = session.pipe.fail(IOException("Could not set transfer session description"))
            })
        }
        override fun onFailure(error: String) = session.pipe.fail(IOException("Could not create transfer session description"))
    }

    private fun setRemote(session: Session, signal: WebRtcTunnelProxy.Signal, answer: Boolean) {
        val expected = if (answer) "offer" else "answer"
        if (!signal.sdpType.equals(expected, true) || signal.sdp.isNullOrBlank()) return
        session.peer.setRemoteDescription(RTCSessionDescription(if (answer) RTCSdpType.OFFER else RTCSdpType.ANSWER, signal.sdp!!),
            object : SetSessionDescriptionObserver {
                override fun onSuccess() {
                    synchronized(this@DesktopTransferTransport) {
                        if (session.closed.get()) return
                        session.remoteReady = true
                        session.pending.forEach(session.peer::addIceCandidate)
                        session.pending.clear()
                        if (answer) session.peer.createAnswer(RTCAnswerOptions(), descriptionObserver(session, "answer"))
                    }
                }
                override fun onFailure(error: String) = session.pipe.fail(IOException("Could not accept transfer session description"))
            })
    }

    private fun addIce(session: Session, signal: WebRtcTunnelProxy.Signal) {
        if (signal.candidateSdp.isNullOrBlank() || signal.candidateSdpMLineIndex !in 0..65535) return
        val candidate = RTCIceCandidate(signal.candidateSdpMid, signal.candidateSdpMLineIndex, signal.candidateSdp)
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
            runCatching { session.peer.close() }
        }
    }

    @Synchronized
    fun stop() {
        host = null
        pending.clear()
        sessions.values.toList().forEach { it.pipe.close() }
    }
}
