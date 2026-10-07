package io.github.rwx.p2p

import io.github.rwx.logger
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

enum class P2PDiscoverySource(val id: String) {
    MDNS("mdns"),
    SERVICE("service"),
    MANUAL("manual")
}

data class P2PDiscoveryResult(
    val room: P2PRoomAdvertisement,
    val source: P2PDiscoverySource,
    val trustScore: Int = 0,
    val receivedAtMs: Long = System.currentTimeMillis()
)

class P2PServiceDiscoveryProvider(
    private val config: P2PConfig.ServiceDiscoveryConfig,
) {
    fun fetchRooms(): List<P2PDiscoveryResult> {
        if (!config.enable || config.urls.isEmpty()) return emptyList()
        val results = mutableListOf<P2PDiscoveryResult>()
        for (url in config.urls) {
            results += fetchRooms(url)
        }
        return results
    }

    private fun fetchRooms(url: String): List<P2PDiscoveryResult> {
        return runCatching {
            val bytes = fetchBytes(url)
            val rooms = parseRooms(bytes)
            val now = System.currentTimeMillis()
            rooms
                .asSequence()
                .take(config.maxRoomsPerUrl.coerceAtLeast(1))
                .filter { it.isValid() }
                .filterNot { it.isExpired(now) }
                .map {
                    it.lastSeenTimeMs = now
                    P2PDiscoveryResult(it, P2PDiscoverySource.SERVICE, trustScore = 30, receivedAtMs = now)
                }
                .toList()
        }.onFailure { e ->
            logger.warn(e) { "Lobby service discovery failed: $url - ${e.message}" }
        }.getOrDefault(emptyList())
    }

    fun fetchCapabilities(baseUrl: String): io.github.rwx.p2p.transfer.SignalingCapabilities {
        val bytes = fetchSignalBytes(normalizeRoomsUrl(baseUrl).removeSuffix("/rooms") + "/capabilities")
        return P2PJson.decodeFromString(io.github.rwx.p2p.transfer.SignalingCapabilities.serializer(), bytes.toString(Charsets.UTF_8))
    }

    fun fetchSignals(baseUrl: String, roomId: String, sinceSeq: Long, toClientId: String): List<SignalEnvelope> {
        if (!config.enable) return emptyList()
        val limit = config.signalMaxEnvelopes.coerceIn(1, 200)
        val url = buildSignalUrl(baseUrl, roomId) + "?sinceSeq=$sinceSeq&limit=$limit&to=" +
            java.net.URLEncoder.encode(toClientId, "UTF-8")
        val bytes = fetchSignalBytes(url)
        return P2PJson.decodeFromString(
            kotlinx.serialization.builtins.ListSerializer(SignalEnvelope.serializer()),
            bytes.toString(Charsets.UTF_8),
        ).sortedBy { it.seq }.take(limit)
    }

    private fun fetchSignalBytes(url: String): ByteArray {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = config.timeoutMs.coerceAtLeast(1000)
        connection.readTimeout = config.timeoutMs.coerceAtLeast(1000)
        connection.instanceFollowRedirects = false
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("User-Agent", "P2P-Signaling")
        try {
            val status = connection.responseCode
            if (status !in 200..299) throw IOException("Lobby service signal HTTP $status")
            return connection.inputStream.use { readBytesLimited(it, config.maxBytes.coerceAtLeast(1024)) }
        } finally {
            connection.disconnect()
        }
    }

    private fun fetchBytes(url: String): ByteArray {
        val connection = URL(normalizeRoomsUrl(url)).openConnection() as HttpURLConnection
        connection.connectTimeout = config.timeoutMs.coerceAtLeast(1000)
        connection.readTimeout = config.timeoutMs.coerceAtLeast(1000)
        connection.instanceFollowRedirects = true
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("User-Agent", "RWX-P2P-Discovery")
        return connection.inputStream.use { readBytesLimited(it, config.maxBytes.coerceAtLeast(1024)) }
    }

    private fun parseRooms(bytes: ByteArray): List<P2PRoomAdvertisement> {
        return P2PJson.decodeFromString(bytes.toString(Charsets.UTF_8))
    }
}

class P2PServicePublisher(
    private val config: P2PConfig.ServicePublishConfig,
    private val serviceUrls: List<String>,
) {
    fun isEnabled(): Boolean {
        return config.enable && serviceUrls.isNotEmpty()
    }

    fun publishRoom(room: P2PRoomAdvertisement?): Boolean {
        if (!isEnabled() || room == null) return false
        val now = System.currentTimeMillis()
        val payload = room.copy(
            lastSeenTimeMs = now,
            expiresAtMs = now + config.roomTtlMs.coerceAtLeast(config.updateIntervalMs * 3)
        )
        var success = false
        for (url in serviceUrls) {
            if (upsertRoom(url, payload)) {
                success = true
            }
        }
        logger.debug { "room schema:\n $payload" }
        return success
    }

    fun closeRoom(room: P2PRoomAdvertisement?): Boolean {
        if (!isEnabled() || !config.deleteOnClose || room == null) return false
        val roomId = room.roomId ?: return false
        var success = false
        for (url in serviceUrls) {
            if (deleteRoom(url, roomId)) {
                success = true
            }
        }
        return success
    }

    private fun upsertRoom(baseUrl: String, room: P2PRoomAdvertisement): Boolean {
        val roomId = room.roomId ?: return false
        return runCatching {
            val url = buildRoomUrl(baseUrl, roomId, upsert = true)
            request(url, "PUT", P2PJson.encodeToString(room).encodeToByteArray())
            true
        }.onFailure { e ->
            logger.warn(e) { "Lobby service publish failed: $baseUrl - ${e.message}" }
        }.getOrDefault(false)
    }

    fun postSignal(baseUrl: String, roomId: String, envelope: SignalEnvelope): Boolean {
        if (serviceUrls.isEmpty()) return false
        return runCatching {
            require(envelope.roomId == roomId && envelope.isValid()) { "Invalid signal envelope" }
            val url = buildSignalUrl(baseUrl, roomId)
            request(url, "PUT", P2PJson.encodeToString(SignalEnvelope.serializer(), envelope).encodeToByteArray())
            true
        }.onFailure { e ->
            logger.warn(e) { "Lobby service signal publish failed: $baseUrl - ${e.message}" }
        }.getOrDefault(false)
    }

    private fun deleteRoom(baseUrl: String, roomId: String): Boolean {
        return runCatching {
            val url = buildRoomUrl(baseUrl, roomId, upsert = false)
            request(url, "DELETE", null)
            true
        }.onFailure { e ->
            logger.warn(e) { "Lobby service delete failed: $baseUrl - ${e.message}" }
        }.getOrDefault(false)
    }

    private fun buildRoomUrl(baseUrl: String, roomId: String, upsert: Boolean): String {
        val roomsUrl = normalizeRoomsUrl(baseUrl)
        val suffix = "/$roomId"
        val query = if (upsert) "?upsert=1" else ""
        return roomsUrl + suffix + query
    }

    private fun request(url: String, method: String, body: ByteArray?): ByteArray {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.connectTimeout = config.timeoutMs.coerceAtLeast(1000)
        connection.readTimeout = config.timeoutMs.coerceAtLeast(1000)
        connection.instanceFollowRedirects = true
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("User-Agent", "RWX-P2P-Discovery")
        if (body != null) {
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use { it.write(body) }
        }
        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val bytes = stream?.use { readBytesLimited(it, 65536) } ?: ByteArray(0)
        if (status !in 200..299) {
            throw IOException("Lobby service HTTP $status: ${bytes.toString(Charsets.UTF_8).take(300)}")
        }
        return bytes
    }
}

private fun normalizeBaseUrl(url: String): String {
    return url.trim().trimEnd('/')
}

private fun normalizeRoomsUrl(url: String): String {
    val base = normalizeBaseUrl(url)
    if (base.isBlank()) return base
    return if (base.endsWith("/rooms")) base else "$base/rooms"
}

private fun buildSignalUrl(baseUrl: String, roomId: String): String {
    require(roomId.matches(Regex("[A-Za-z0-9_-]{1,128}"))) { "Invalid signal room ID" }
    return "${normalizeRoomsUrl(baseUrl)}/$roomId/signals"
}

private fun readBytesLimited(input: InputStream, maxBytes: Int): ByteArray {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(8192)
    val limit = maxBytes.coerceAtLeast(1024)
    var total = 0
    while (true) {
        val read = input.read(buffer)
        if (read < 0) break
        total += read
        if (total > limit) {
            throw IllegalArgumentException("Lobby service response is too large")
        }
        output.write(buffer, 0, read)
    }
    return output.toByteArray()
}
