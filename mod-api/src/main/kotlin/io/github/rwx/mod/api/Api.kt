package io.github.rwx.mod.api

interface Api {
    val game: Game
    val assets: Asset
    val units: Units
    val unitWorld: UnitWorld
    val commands: UnitCommands
    val maps: Map
    val rules: Rule
    val localization: Localization
    val audio: Audio
    val graphics: Graphics
    val ui: Ui
    val ai: AiBehavior
}

interface Game {
    val gameVersion: String
    val tick: Long
    val headless: Boolean

    fun log(level: LogLevel, message: String, cause: Throwable? = null)
    fun info(message: String) = log(LogLevel.INFO, message)
    fun debug(message: String) = log(LogLevel.DEBUG, message)
    fun warning(message: String, cause: Throwable? = null) = log(LogLevel.WARN, message, cause)
    fun error(message: String, cause: Throwable? = null) = log(LogLevel.ERROR, message, cause)
    fun schedule(delayTicks: Long, task: ModTask): ScheduledTask
    fun repeat(intervalTicks: Long, task: ModTask): ScheduledTask
    fun localTeam(): TeamState?
    fun team(teamId: Int): TeamState?
    fun registerTeamAction(id: String, handler: TeamActionHandler)
    fun requestTeamAction(id: String, targetPosition: WorldPosition? = null): Boolean
    fun registerProjectileObserver(id: String, observer: ProjectileObserver)
    fun registerTurretFireCycleObserver(id: String, observer: TurretFireCycleObserver)
}

interface TeamState {
    val teamId: Int
    fun resource(id: String): Double
    fun hasFlag(id: String): Boolean
}

fun interface TeamActionHandler {
    fun execute(context: TeamActionContext)
}

interface TeamActionContext : TeamState {
    val targetPosition: WorldPosition?

    fun trySpend(cost: Cost): Boolean
    fun addResource(id: String, amount: Double)
    fun grantFlag(id: String)
    fun revokeFlag(id: String)
}

enum class LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR
}

fun interface ModTask {
    fun run(context: Api)
}

interface ScheduledTask {
    val cancelled: Boolean

    fun cancel()
}

/**
 * Resolves mod resources. Mounted paths use `namespace:relative`; packaged paths are relative to
 * the mod's `assets/` directory.
 */
interface Asset {
    /** Mounts a platform filesystem directory under [namespace]. */
    fun mount(namespace: String, root: String)
    fun open(path: String): ResourceStream
    fun exists(path: String): Boolean
    fun list(path: String): List<String>
}

interface ResourceStream : AutoCloseable {
    fun readBytes(): ByteArray
    fun readText(charset: String = "UTF-8"): String
}
