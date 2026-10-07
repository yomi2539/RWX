package io.github.rwx.p2p.transfer

import io.github.rwx.PlatformStorage
import io.github.rwx.logger
import io.github.rwx.p2p.WebRtcTransferConnection
import io.github.rwx.session.GameSession
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

internal class TransferHostRepository(
    private val session: GameSession,
    private val storage: PlatformStorage,
    private val identity: TransferIdentity,
    private val cache: ArtifactCache,
    private val onAdvertisement: (String, TransferAdvertisement) -> Unit,
) : AutoCloseable {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connections = ConcurrentHashMap.newKeySet<Job>()
    private var buildJob: Job? = null
    private val buildMutex = Mutex()
    @Volatile private var room: String? = null
    @Volatile private var current: HostedSnapshot? = null
    private var server: TransferServer? = null
    @Volatile private var failure: TransferErrorCode? = null
    private var attemptedRevision = -1L
    private var restart: (() -> Unit)? = null
    private var cachedRevision = -1L
    private val cachedSources = mutableMapOf<String, Pair<TransferArtifact, Path>>()

    fun prepare(roomId: String, share: Boolean, services: List<String>, password: () -> String?, available: () -> Boolean) {
        val closing = connections.toList()
        stopRoom()
        room = roomId
        attemptedRevision = TransferRevision.current
        restart = { prepare(roomId, share, services, password, available) }
        failure = null
        server = TransferServer(roomId, identity.fingerprint, {
            failure?.let { throw TransferException(it) }
            current
        }, password, { room == roomId && available() })
        fun announce(state: String, snapshot: HostedSnapshot? = null) {
            if (room != roomId) return
            onAdvertisement(roomId, TransferAdvertisement(listOf(1), "webrtc-datachannel", identity.publicKey,
                services, state, snapshot?.manifest?.manifestId, snapshot?.manifest?.artifacts?.size, snapshot?.manifest?.totalBytes))
        }
        announce(if (share) "preparing" else "disabled")
        buildJob = scope.launch {
            closing.joinAll()
            buildMutex.withLock {
            try {
                val engine = session.captureTransferState() ?: throw TransferException(TransferErrorCode.LOAD_FAILED)
                if (engine.usesMods && !share) {
                    failure = TransferErrorCode.UNSUPPORTED_MOD
                    return@launch
                }
                transferRequire(!engine.usesMods || (!engine.hasLoadErrors && !engine.hasJvmMods), TransferErrorCode.UNSUPPORTED_MOD)
                if (cachedRevision != engine.revision) {
                    cachedSources.clear()
                    cachedRevision = engine.revision
                }
                val artifacts = mutableListOf<TransferArtifact>()
                val files = mutableMapOf<String, Path>()
                val owners = mutableMapOf<Pair<String, Int>, String>()
                if (engine.usesMods) {
                    transferRequire(engine.sources.size <= TransferLimits.ARTIFACTS, TransferErrorCode.LIMIT_EXCEEDED)
                    cache.reserve(TransferLimits.TOTAL_BYTES, cachedSources.values.map { it.second })
                    for (source in engine.sources) {
                        ensureActive()
                        val cached = cachedSources[source.uuid]?.takeIf { cache.find(it.first) != null }
                        val packaged = cached ?: run {
                            val path = cache.root.resolve("${UUID.randomUUID()}.rwmod")
                            try {
                                val sourcePath = java.io.File(source.path).toPath()
                                val check = {
                                    ensureActive()
                                    transferRequire(TransferRevision.current == engine.revision, TransferErrorCode.MANIFEST_CHANGED)
                                }
                                val built = try {
                                    if (Files.isDirectory(sourcePath)) PackageBuilder.fromDirectory(
                                        sourcePath,
                                        path,
                                        check
                                    )
                                    else PackageBuilder.fromArchive(sourcePath, path, check)
                                } catch (cancelled: CancellationException) {
                                    throw cancelled
                                } catch (fastError: Throwable) {
                                    if (fastError !is TransferException ||
                                        fastError.code != TransferErrorCode.UNSAFE_PACKAGE ||
                                        !ModSourceMaterializer.needsFallback(sourcePath)
                                    ) throw fastError
                                    ensureActive()
                                    check()
                                    val raw = source.abstractPath.ifBlank { source.path }
                                    ModSourceMaterializer.packageFallback(
                                        EngineModSourceReader,
                                        raw,
                                        path,
                                        cache.root,
                                        check
                                    )
                                }
                                val artifact = TransferArtifact(UUID.randomUUID().toString(), source.title, null, "legacy-data", "rwmod",
                                    built.sha256, built.size, built.summary.unpackedBytes, built.summary.fileCount)
                                cache.remember(path, artifact)
                                (artifact to path).also { cachedSources[source.uuid] = it }
                            } catch (error: Throwable) {
                                Files.deleteIfExists(path)
                                throw error
                            }
                        }
                        artifacts += packaged.first
                        files[packaged.first.artifactId] = packaged.second
                        source.units.forEach { owners[it.name to it.configHash] = packaged.first.artifactId }
                        transferRequire(artifacts.sumOf { it.size } <= TransferLimits.TOTAL_BYTES, TransferErrorCode.LIMIT_EXCEEDED)
                    }
                }
                val manifest = TransferManifest(1, roomId, identity.fingerprint, UUID.randomUUID().toString(), engine.gameVersion, 1,
                    artifacts, if (engine.usesMods) engine.activeUnits.map { it.copy(artifactId = owners[it.name to it.configHash]) } else emptyList(),
                    artifacts.sumOf { it.size })
                manifest.validate(roomId, identity.fingerprint, engine.gameVersion)
                TransferFrameCodec.encode(TransferFrame(TransferHeader.Manifest(1, manifest)))
                transferRequire(engine.revision == TransferRevision.current && room == roomId, TransferErrorCode.MANIFEST_CHANGED)
                val snapshot = HostedSnapshot(manifest, files, engine.revision)
                current = snapshot
                announce("ready", snapshot)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (room == roomId) {
                    current = null
                    failure = (error as? TransferException)?.code ?: TransferErrorCode.UNSUPPORTED_MOD
                    logger.warn(error) { "Transfer manifest build failed for room $roomId: ${failure?.name}" }
                }
                announce("unsupported")
            }
            }
        }
    }

    fun accept(connection: WebRtcTransferConnection, peerIdentity: String) {
        val handler = server ?: run { connection.close(); return }
        val job = scope.launch(start = CoroutineStart.LAZY) {
            try { handler.serve(connection, peerIdentity) }
            finally { connection.close() }
        }
        connections += job
        job.invokeOnCompletion { connections.remove(job) }
        job.start()
    }

    fun refresh() {
        if (room != null && buildJob?.isActive != true && attemptedRevision != TransferRevision.current) restart?.invoke()
    }

    fun stopRoom() {
        room = null
        restart = null
        current = null
        buildJob?.cancel()
        buildJob = null
        connections.toList().forEach(Job::cancel)
        server = null
    }

    override fun close() {
        stopRoom()
        scope.cancel()
    }
}
