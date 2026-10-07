package io.github.rwx.p2p

import io.github.rwx.map.PortalTransferMessage
import kotlinx.serialization.Serializable

object FeatureIds {
    const val FEATURE_CHANNEL = "rwxFeatureChannel"
    const val AREA_CONTROL = "areaControl"
    const val MAP_LINKS = "mapLinks"

    fun currentClientFeatures(): List<String> = listOf(
        FEATURE_CHANNEL,
        AREA_CONTROL,
        MAP_LINKS,
    )
}

@Serializable
data class FeatureMessage(
    var magic: String = "rwx-feature-channel",
    var schema: Int = 1,
    var roomId: String? = null,
    var fromPeerId: String? = null,
    var toPeerId: String? = null,
    var type: String = "hello",
    var gameVersionCode: Int = 0,
    var gameVersionString: String? = null,
    var features: List<String> = emptyList(),
    var mapPath: String? = null,
    var requiredFeatures: List<String> = emptyList(),
    var multiMapAssignments: MultiMapAssignments? = null,
    var portalTransfer: PortalTransferMessage? = null,
) {
    fun isValid(): Boolean =
        magic == "rwx-feature-channel" &&
                schema == 1 &&
                !roomId.isNullOrBlank() &&
                !fromPeerId.isNullOrBlank() &&
                type.isNotBlank()
}
