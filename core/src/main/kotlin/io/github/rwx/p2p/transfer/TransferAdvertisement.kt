package io.github.rwx.p2p.transfer

import kotlinx.serialization.Serializable
import java.net.URI

@Serializable
data class TransferAdvertisement(
    val versions: List<Int> = emptyList(),
    val transport: String? = null,
    val hostKey: String? = null,
    val signalingServices: List<String> = emptyList(),
    val state: String? = null,
    val manifestId: String? = null,
    val artifactCount: Int? = null,
    val totalBytes: Long? = null,
) {
    fun isValid(): Boolean = runCatching {
        transferRequire(versions == listOf(1) && transport == "webrtc-datachannel")
        transferRequire(hostKey != null)
        TransferIdentity.decode(hostKey!!, maximum = TransferIdentity.MAX_KEY_BYTES)
        transferRequire(signalingServices.size in 1..8 && signalingServices.distinct().size == signalingServices.size)
        signalingServices.forEach { value ->
            val uri = URI(value)
            transferRequire(uri.scheme in setOf("http", "https") && uri.host != null && uri.userInfo == null && uri.fragment == null)
        }
        transferRequire(state in setOf("disabled", "preparing", "ready", "unsupported"))
        transferRequire(state != "ready" || manifestId?.isTransferId() == true)
        transferRequire(artifactCount == null || artifactCount in 0..TransferLimits.ARTIFACTS)
        transferRequire(totalBytes == null || totalBytes in 0..TransferLimits.TOTAL_BYTES)
        true
    }.getOrDefault(false)
}

@Serializable
data class SignalingCapabilities(
    val signalingVersions: List<Int>,
    val signalPurposes: List<String>,
    val maxSignalBytes: Int,
) {
    fun supportsTransfers(): Boolean = 1 in signalingVersions && "game" in signalPurposes &&
        "transfer-v1" in signalPurposes && maxSignalBytes >= 64 * 1024
}
