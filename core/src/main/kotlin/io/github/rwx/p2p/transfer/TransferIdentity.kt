package io.github.rwx.p2p.transfer

import io.github.rwx.p2p.SignalEnvelope
import io.github.rwx.p2p.WebRtcTunnelProxy
import kotlinx.serialization.Serializable
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermissions
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

@Serializable
private data class StoredTransferIdentity(val privateKey: String, val publicKey: String)

@Serializable
data class TransferSignalPayload(
    val signal: WebRtcTunnelProxy.Signal,
    val senderIdentity: String,
    val recipientIdentity: String,
    val nonce: String,
    val sequence: Long,
    val issuedAtMs: Long,
    val expiresAtMs: Long,
)

data class VerifiedTransferSignal(val payload: TransferSignalPayload, val senderKey: String)

class TransferIdentity private constructor(private val keys: KeyPair) {
    val publicKey: String = Base64.getEncoder().encodeToString(keys.public.encoded)
    val fingerprint: String = keys.public.encoded.sha256()

    fun sign(signal: WebRtcTunnelProxy.Signal, recipientIdentity: String, nonce: String, sequence: Long,
        nowMs: Long = System.currentTimeMillis()): SignalEnvelope {
        transferRequire(signal.purpose == "transfer-v1" && recipientIdentity.isContentHash() && nonce.isTransferId() && sequence >= 0)
        val payload = TransferSignalPayload(signal, fingerprint, recipientIdentity, nonce, sequence, nowMs, nowMs + 120_000)
        val bytes = transferJson.encodeToString(TransferSignalPayload.serializer(), payload).utf8()
        val signature = Signature.getInstance("SHA256withECDSA").run { initSign(keys.private); update(bytes); sign() }
        return SignalEnvelope.fromProxySignal(signal, nowMs).copy(
            sdpType = null, sdp = null, candidateSdpMid = null, candidateSdpMLineIndex = 0, candidateSdp = null,
            signerKey = publicKey, signedPayload = Base64.getEncoder().encodeToString(bytes),
            signature = Base64.getEncoder().encodeToString(signature),
        )
    }

    fun verify(envelope: SignalEnvelope, roomId: String, localClientId: String, expectedKey: String? = null,
        nowMs: Long = System.currentTimeMillis()): VerifiedTransferSignal {
        transferRequire(envelope.isValid() && envelope.purpose == "transfer-v1")
        val keyText = envelope.signerKey!!
        transferRequire(expectedKey == null || keyText == expectedKey, TransferErrorCode.IDENTITY_MISMATCH)
        val rawKey = decode(keyText, maximum = MAX_KEY_BYTES)
        val bytes = decode(envelope.signedPayload!!, maximum = TransferLimits.MAX_SIGNED_PAYLOAD_BYTES)
        val signed = decode(envelope.signature!!, maximum = MAX_SIGNATURE_BYTES)
        val key = KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(rawKey))
        transferRequire(Signature.getInstance("SHA256withECDSA").run { initVerify(key); update(bytes); verify(signed) },
            TransferErrorCode.IDENTITY_MISMATCH)
        val payload = try {
            transferJson.decodeFromString(TransferSignalPayload.serializer(), bytes.toString(Charsets.UTF_8))
        } catch (error: Exception) {
            throw TransferException(TransferErrorCode.PROTOCOL_ERROR, cause = error)
        }
        val signal = payload.signal
        transferRequire(payload.senderIdentity == rawKey.sha256() && payload.recipientIdentity == fingerprint,
            TransferErrorCode.IDENTITY_MISMATCH)
        transferRequire(signal.isValid() && signal.purpose == "transfer-v1" && signal.roomId == roomId &&
            signal.roomId == envelope.roomId && signal.toPeerId == localClientId && signal.toPeerId == envelope.toClientId &&
            signal.fromPeerId == envelope.fromClientId && signal.sessionId == envelope.sessionId && signal.type == envelope.type)
        transferRequire(payload.nonce.isTransferId() && payload.sequence in 0..TransferLimits.MAX_SIGNALS_PER_SESSION)
        transferRequire(when (signal.type) {
            "offer", "answer" -> signal.sdpType.equals(signal.type, ignoreCase = true) && !signal.sdp.isNullOrBlank()
            "ice" -> !signal.candidateSdp.isNullOrBlank() && signal.candidateSdpMLineIndex in 0..65535
            else -> false
        })
        transferRequire(payload.issuedAtMs >= 0 && payload.issuedAtMs <= nowMs + 30_000 &&
            payload.expiresAtMs >= nowMs - 30_000 && payload.expiresAtMs > payload.issuedAtMs &&
            payload.expiresAtMs - payload.issuedAtMs <= 120_000, TransferErrorCode.IDENTITY_MISMATCH)
        return VerifiedTransferSignal(payload, keyText)
    }

    companion object {
        internal const val MAX_KEY_BYTES = 256
        internal const val MAX_SIGNATURE_BYTES = 256

        fun create(): TransferIdentity {
            val generator = KeyPairGenerator.getInstance("EC")
            generator.initialize(ECGenParameterSpec("secp256r1"))
            return TransferIdentity(generator.generateKeyPair())
        }

        fun loadOrCreate(localDirectory: Path): TransferIdentity {
            val root = localDirectory.resolve("peer-identity-ec")
            transferRequire(!Files.isSymbolicLink(root), TransferErrorCode.IDENTITY_MISMATCH)
            Files.createDirectories(root)
            Files.getFileAttributeView(root, PosixFileAttributeView::class.java)?.setPermissions(PosixFilePermissions.fromString("rwx------"))
            val file = root.resolve("identity.json")
            transferRequire(!Files.isSymbolicLink(file), TransferErrorCode.IDENTITY_MISMATCH)
            if (Files.exists(file)) {
                transferRequire(Files.size(file) <= 4096, TransferErrorCode.IDENTITY_MISMATCH)
                val stored = transferJson.decodeFromString(StoredTransferIdentity.serializer(), Files.readAllBytes(file).toString(Charsets.UTF_8))
                val factory = KeyFactory.getInstance("EC")
                val identity = TransferIdentity(KeyPair(
                    factory.generatePublic(X509EncodedKeySpec(decode(stored.publicKey, maximum = MAX_KEY_BYTES))),
                    factory.generatePrivate(PKCS8EncodedKeySpec(decode(stored.privateKey, maximum = 512))),
                ))
                val challenge = "identity-key-pair".utf8()
                val proof = Signature.getInstance("SHA256withECDSA").run { initSign(identity.keys.private); update(challenge); sign() }
                transferRequire(Signature.getInstance("SHA256withECDSA").run { initVerify(identity.keys.public); update(challenge); verify(proof) },
                    TransferErrorCode.IDENTITY_MISMATCH)
                return identity
            }
            val identity = create()
            val stored = StoredTransferIdentity(Base64.getEncoder().encodeToString(identity.keys.private.encoded),
                Base64.getEncoder().encodeToString(identity.keys.public.encoded))
            TransferFiles.atomicWrite(file, transferJson.encodeToString(StoredTransferIdentity.serializer(), stored).utf8())
            Files.getFileAttributeView(file, PosixFileAttributeView::class.java)?.setPermissions(PosixFilePermissions.fromString("rw-------"))
            return identity
        }

        internal fun decode(value: String, exact: Int? = null, maximum: Int = 64 * 1024): ByteArray {
            transferRequire(value.length <= ((maximum + 2) / 3) * 4)
            val bytes = try { Base64.getDecoder().decode(value) } catch (error: IllegalArgumentException) {
                throw TransferException(TransferErrorCode.PROTOCOL_ERROR, cause = error)
            }
            transferRequire(bytes.size <= maximum && (exact == null || bytes.size == exact) &&
                Base64.getEncoder().encodeToString(bytes) == value)
            return bytes
        }
    }
}

class TransferSignalReplayGuard {
    private data class Session(val key: String, val nonce: String, val expiresAt: Long, val seen: MutableSet<Long> = mutableSetOf())
    private val sessions = mutableMapOf<String, Session>()
    private val nonces = mutableMapOf<String, Long>()

    @Synchronized
    fun accept(signal: VerifiedTransferSignal, nowMs: Long = System.currentTimeMillis()): Boolean {
        nonces.entries.removeIf { it.value < nowMs - 30_000 }
        sessions.entries.removeIf { it.value.expiresAt < nowMs - 30_000 }
        val payload = signal.payload
        val id = payload.signal.sessionId!!
        val current = sessions[id]
        if (current == null) {
            if (payload.nonce in nonces || sessions.size >= 32) return false
            nonces[payload.nonce] = payload.expiresAtMs
            sessions[id] = Session(signal.senderKey, payload.nonce, payload.expiresAtMs, mutableSetOf(payload.sequence))
            return true
        }
        return current.key == signal.senderKey && current.nonce == payload.nonce && current.seen.add(payload.sequence)
    }

    @Synchronized fun close(sessionId: String) { sessions.remove(sessionId) }
    @Synchronized fun clear() { sessions.clear(); nonces.clear() }
}
