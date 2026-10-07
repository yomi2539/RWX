package io.github.rwx.p2p

import java.net.Socket
import kotlin.system.exitProcess

object P2PDiagnosticsMain {
    @JvmStatic
    fun main(args: Array<String>) {
        val mode = args.firstOrNull() ?: "discover"
        val timeoutMs = args.getOrNull(1)?.toLongOrNull() ?: 60000L
        when (mode) {
            "discover" -> discover(timeoutMs)
            "join-first" -> joinFirst(timeoutMs)
            "service-publish-test" -> servicePublishTest(timeoutMs)
            "service-delete-test" -> serviceDeleteTest(timeoutMs)
            else -> {
                println("Usage: p2pDiagnostics [discover|join-first|service-publish-test|service-delete-test] [timeoutMs]")
                exitProcess(2)
            }
        }
    }

    private fun discover(timeoutMs: Long) {
        val lobby = P2PLobbyService.getInstance()
        try {
            lobby.startIfNeeded()
            val rooms = waitForRooms(lobby, timeoutMs)
            println("P2P_DIAG rooms=${rooms.size}")
            rooms.forEachIndexed { index, room ->
                println("P2P_DIAG room[$index] id=${room.roomId} host=${room.hostClientId} by=${room.createdBy} state=${room.gameState} map=${room.mapPath}")
                println("P2P_DIAG room[$index] webrtc=${room.webrtcSignaling} ice=${room.webrtcIceServers.joinToString(",")}")
            }
            if (rooms.isEmpty()) exitProcess(1)
        } finally {
            lobby.stopSession()
        }
        exitProcess(0)
    }

    private fun joinFirst(timeoutMs: Long) {
        val lobby = P2PLobbyService.getInstance()
        try {
            lobby.startIfNeeded()
            val room = waitForRooms(lobby, timeoutMs).firstOrNull() ?: run {
                println("P2P_DIAG no rooms discovered")
                exitProcess(1)
            }
            println("P2P_DIAG joining room id=${room.roomId} host=${room.hostClientId}")
            val address = lobby.prepareJoin(room.roomId!!)
            println("P2P_DIAG local proxy=$address")
            val host = address.substringBefore(':')
            val port = address.substringAfter(':').toInt()
            val socket = Socket(host, port)
            try {
                socket.tcpNoDelay = true
                socket.soTimeout = timeoutMs.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                println("P2P_DIAG connected to local proxy")
                Thread.sleep(timeoutMs.coerceAtMost(15000L))
                println("P2P_DIAG join probe completed")
            } finally {
                socket.close()
            }
        } finally {
            lobby.stopSession()
        }
        exitProcess(0)
    }

    private fun waitForRooms(lobby: P2PLobbyService, timeoutMs: Long): List<P2PRoomAdvertisement> {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val rooms = lobby.getRooms()
            if (rooms.isNotEmpty()) return rooms
            lobby.requestRefresh()
            Thread.sleep(1000L)
        }
        return lobby.getRooms()
    }

    private fun servicePublishTest(timeoutMs: Long) {
        val room = createServiceDiagnosticRoom(timeoutMs)
        val publisher = servicePublisherOrExit()
        if (publisher.publishRoom(room)) {
            println("P2P_DIAG service publish succeeded room=${room.roomId}")
            exitProcess(0)
        }
        println("P2P_DIAG service publish failed room=${room.roomId}")
        exitProcess(1)
    }

    private fun serviceDeleteTest(timeoutMs: Long) {
        val room = createServiceDiagnosticRoom(timeoutMs)
        val publisher = servicePublisherOrExit()
        if (publisher.closeRoom(room)) {
            println("P2P_DIAG service delete succeeded room=${room.roomId}")
            exitProcess(0)
        }
        println("P2P_DIAG service delete failed room=${room.roomId}")
        exitProcess(1)
    }

    private fun servicePublisherOrExit(): P2PServicePublisher {
        val serviceConfig = P2PConfigLoader.load().discovery.service
        val publisher = P2PServicePublisher(serviceConfig.publish, serviceConfig.urls)
        if (!publisher.isEnabled()) {
            println("P2P_DIAG lobby service publish disabled or missing urls")
            exitProcess(1)
        }
        return publisher
    }

    private fun createServiceDiagnosticRoom(timeoutMs: Long): P2PRoomAdvertisement {
        val now = System.currentTimeMillis()
        return P2PRoomAdvertisement().apply {
            roomId = "diag-${now}"
            hostClientId = "QmDiagLobbyServiceTest"
            createdBy = "diagnostics"
            gameVersionCode = 1
            gameVersionString = "diagnostics"
            mapPath = "diagnostics"
            gameState = "battleroom"
            webrtcSignaling = "gossip"
            expiresAtMs = now + timeoutMs.coerceAtLeast(30000L)
            seq = 1
        }
    }
}
