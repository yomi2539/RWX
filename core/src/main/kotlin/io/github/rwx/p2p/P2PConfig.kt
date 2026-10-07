package io.github.rwx.p2p

import io.github.rwx.logger
import io.github.rwx.PlatformStorage
import kotlinx.serialization.Serializable
import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.TomlComment
import net.peanuuutz.tomlkt.decodeFromString
import org.koin.mp.KoinPlatform.getKoin
import java.io.File

@Serializable
data class P2PConfig(
    val webrtc: WebRtcConfig = WebRtcConfig(),
    val discovery: DiscoveryConfig = DiscoveryConfig(),
    val lobby: LobbyConfig = LobbyConfig()
) {
    val webrtcIceServers: List<String>
        get() = webrtc.iceServers

    val webRtcProxy: WebRtcTunnelProxy.ProxyConfig
        get() = WebRtcTunnelProxy.ProxyConfig(
            bufferSize = webrtc.proxy.bufferSize,
            openTimeoutMs = webrtc.proxy.openTimeoutMs,
            socketConnectTimeoutMs = webrtc.proxy.socketConnectTimeoutMs,
            socketReadTimeoutMs = webrtc.proxy.socketReadTimeoutMs,
            executorThreads = webrtc.proxy.executorThreads
        )

    @Serializable
    data class WebRtcConfig(
        @TomlComment("WebRTC DataChannel ICE servers. TURN entries may use turn:user:pass@host:port.")
        val iceServers: List<String> = WebRtcTunnelProxy.DEFAULT_ICE_SERVERS,
        val proxy: WebRtcProxyConfig = WebRtcProxyConfig()
    )

    @Serializable
    data class WebRtcProxyConfig(
        val bufferSize: Int = 65536,
        val openTimeoutMs: Long = 60000L,
        val socketConnectTimeoutMs: Int = 5000,
        val socketReadTimeoutMs: Int = 30000,
        val executorThreads: Int = 4
    )

    @Serializable
    data class DiscoveryConfig(
        val service: ServiceDiscoveryConfig = ServiceDiscoveryConfig(),
    )

    @Serializable
    data class ServiceDiscoveryConfig(
        @TomlComment("HTTP lobby service endpoints for room discovery. Each URL can be a base URL or /rooms.")
        val enable: Boolean = true,
        val urls: List<String> = listOf(
            "https://p2p-lobby-services.shuangx339.workers.dev"
        ),
        val refreshIntervalMs: Long = 15000L,
        val timeoutMs: Int = 15000,
        val maxBytes: Int = 262144,
        val maxRoomsPerUrl: Int = 200,
        val publish: ServicePublishConfig = ServicePublishConfig(),
        val signalPollIntervalMs: Long = 2000L,
        val signalMaxEnvelopes: Int = 20
    )

    @Serializable
    data class ServicePublishConfig(
        @TomlComment("Optional: publish this client's hosted room to the lobby service.")
        val enable: Boolean = true,
        val timeoutMs: Int = 15000,
        val updateIntervalMs: Long = 15000L,
        val roomTtlMs: Long = 600000L,
        val deleteOnClose: Boolean = true
    )

    @Serializable
    data class LobbyConfig(
        val roomAnnounceIntervalMs: Long = 3000L,
        val roomTtlMs: Long = 15000L
    )

    fun normalized(): P2PConfig {
        return copy(
            webrtc = webrtc.copy(
                iceServers = webrtc.iceServers.filter { isIceServer(it) }.distinct()
                    .ifEmpty { WebRtcTunnelProxy.DEFAULT_ICE_SERVERS }
            ),
            discovery = discovery.copy(
                service = discovery.service.copy(
                    urls = discovery.service.urls
                        .filter { it.startsWith("http://") || it.startsWith("https://") }
                        .distinct(),
                    signalPollIntervalMs = discovery.service.signalPollIntervalMs.coerceIn(500L, 10000L),
                    signalMaxEnvelopes = discovery.service.signalMaxEnvelopes.coerceIn(1, 200),
                )
            )
        )
    }

    companion object {
        private fun isIceServer(value: String): Boolean {
            return value.startsWith("stun:") || value.startsWith("turn:") || value.startsWith("turns:")
        }
    }
}

object P2PConfigLoader {
    private const val CONFIG_FILE_NAME = "p2p.toml"
    private val toml = Toml { ignoreUnknownKeys = true }

    fun load(): P2PConfig {
        val file = File(getKoin().get<PlatformStorage>().rootDir.file, CONFIG_FILE_NAME)
        if (!file.exists()) {
            runCatching { file.writeText(defaultConfigText(), Charsets.UTF_8) }
                .onFailure { logger.warn(it) { "Failed to create $CONFIG_FILE_NAME: ${it.message}" } }
        }

        return runCatching { toml.decodeFromString<P2PConfig>(file.readText(Charsets.UTF_8)).normalized() }
            .onFailure { logger.warn(it) { "Failed to parse $CONFIG_FILE_NAME: ${it.message}" } }
            .getOrElse { P2PConfig().normalized() }
    }

    private fun defaultConfigText(): String {
        return "# RWX P2P configuration\n# Edit this file locally.\n\n" +
                toml.encodeToString(P2PConfig.serializer(), P2PConfig())
    }
}
