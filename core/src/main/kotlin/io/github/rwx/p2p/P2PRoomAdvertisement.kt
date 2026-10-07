package io.github.rwx.p2p

import com.corrodinggames.rts.gameFramework.GameEngine
import io.github.rwx.p2p.transfer.TransferAdvertisement
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class P2PRoomAdvertisement(
    var magic: String = "rwx-p2p-lobby",
    @kotlinx.serialization.Required var schema: Int = 2,
    var type: String = "room_announce",
    var roomId: String? = null,
    var hostClientId: String? = null,
    var transfer: TransferAdvertisement? = null,
    var createdBy: String? = null,
    var gameVersionCode: Int = 0,
    var gameVersionString: String? = null,
    var requiresPassword: Boolean = false,
    var mapPath: String? = null,
    var gameMode: String? = null,
    var requiredRwxFeatures: MutableList<String> = mutableListOf(),
    var gameState: String? = null,
    var currentPlayers: Int = 0,
    var maxPlayers: Int = 8,
    var hasMods: Boolean = false,
    var modsRequired: String? = null,
    var webrtcSignaling: String? = null,
    var webrtcIceServers: MutableList<String> = mutableListOf(),
    var expiresAtMs: Long = 0,
    var seq: Long = 0,
    @Transient var lastSeenTimeMs: Long = 0,
) {
    fun isValid(): Boolean {
        return magic == "rwx-p2p-lobby" && schema == 2 && !roomId.isNullOrBlank() &&
            !hostClientId.isNullOrBlank() && webrtcSignaling == "service" && transfer?.isValid() == true
    }

    fun isExpired(nowMs: Long): Boolean = expiresAtMs in 1..<nowMs

    fun isVersionCompatible(): Boolean {
        val gameEngine = GameEngine.getInstance() ?: return false
        return gameEngine.getVersionCode(true) == gameVersionCode
    }

    fun getMapDisplayName(): String {
        val value = mapPath ?: return "<No Map>"
        val fileName = value.substringAfterLast('/').substringAfterLast('\\')
        return fileName.ifBlank { value }
    }

    fun getInfoText(): String {
        val parts = mutableListOf<String>()
        parts += "Host: ${createdBy ?: "?"}"
        parts += "Map: ${getMapDisplayName()}"
        parts += buildString {
            append("Version: v")
            append(gameVersionString ?: "?")
            if (!isVersionCompatible()) {
                append(" (different game version!)")
            }
        }
        if (requiresPassword) {
            parts += "Password Required"
        }
        if (hasMods && !modsRequired.isNullOrBlank()) {
            parts += "Mods Needed: $modsRequired"
        }
        if (requiredRwxFeatures.isNotEmpty()) {
            parts += "RWX Features: ${requiredRwxFeatures.joinToString(", ")}"
        }
        if (webrtcIceServers.isNotEmpty()) {
            parts += "ICE Servers:"
            webrtcIceServers.forEach { parts += " - $it" }
        }
        return parts.joinToString("\n")
    }
}
