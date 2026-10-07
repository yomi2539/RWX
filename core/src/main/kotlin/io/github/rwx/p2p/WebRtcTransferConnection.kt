package io.github.rwx.p2p

import io.github.rwx.p2p.transfer.TransferLimits
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.milliseconds

interface WebRtcTransferConnection : AutoCloseable {
    val sessionId: String
    val remoteClientId: String
    suspend fun awaitOpen()
    suspend fun send(bytes: ByteArray)
    suspend fun receive(): ByteArray
}

class WebRtcTransferPipe(
    override val sessionId: String,
    override val remoteClientId: String,
    private val bufferedBytes: () -> Long,
    private val sendPacket: (ByteArray) -> Unit,
    private val closeNative: () -> Unit,
) : WebRtcTransferConnection {
    private val opened = CompletableDeferred<Unit>()
    private val incoming = Channel<ByteArray>(32)
    private val writable = Channel<Unit>(Channel.CONFLATED)
    private val sendLock = Mutex()
    private val closed = AtomicBoolean(false)

    fun onOpen() {
        if (!closed.get()) opened.complete(Unit)
    }

    fun onPacket(bytes: ByteArray) {
        if (closed.get()) return
        if (bytes.size > MAX_PACKET_BYTES || !incoming.trySend(bytes).isSuccess) {
            fail(IOException("Transfer receive buffer exceeded"))
        }
    }

    fun onBufferedAmountChange() {
        writable.trySend(Unit)
    }

    fun fail(error: Throwable = IOException("Transfer channel closed")) {
        if (!closed.compareAndSet(false, true)) return
        opened.completeExceptionally(error)
        incoming.close(error)
        writable.close(error)
        closeNative()
    }

    override suspend fun awaitOpen() =
        withTimeout(TransferLimits.TRANSFER_HANDSHAKE_TIMEOUT_MS.milliseconds) { opened.await() }

    override suspend fun send(bytes: ByteArray) = sendLock.withLock {
        withTimeout(20_000L.milliseconds) {
            opened.await()
            var offset = 0
            while (offset < bytes.size) {
                while (!closed.get() && bufferedBytes() >= 256 * 1024) writable.receive()
                if (closed.get()) throw IOException("Transfer channel closed")
                val end = minOf(offset + 16 * 1024, bytes.size)
                sendPacket(bytes.copyOfRange(offset, end))
                offset = end
            }
        }
    }

    override suspend fun receive(): ByteArray = incoming.receive()
    override fun close() = fail()

    companion object {
        const val LABEL = "transfer-v1"
        const val MAX_PACKET_BYTES = 64 * 1024
    }
}
