package io.github.rwx.mod.api

interface AiBehavior {
    fun registerAgent(definition: AiAgentDefinition)
    fun unregisterAgent(id: String)
    fun bindAgent(binding: AiAgentBinding)
    fun unbindAgent(scope: AiControlScope)
    fun listPlayers(): List<AiPlayerState>
    fun getPlayerState(teamId: Int): AiPlayerState?
    fun configurePlayer(config: AiPlayerConfig): AiPlayerState?
    fun setPlayerEnabled(teamId: Int, enabled: Boolean): AiPlayerState?
    fun currentObservation(scope: AiObservationScope = AiObservationScope()): AiObservationFrame
    fun submitActions(actions: List<UnitCommandRequest>): List<UnitCommandResult>
}

data class AiAgentDefinition(
    val id: String,
    val displayName: LocalizedText = LocalizedText.literal(id),
    val driver: AiAgentDriver = AiAgentDriver.External(),
    val observation: AiObservationSpec = AiObservationSpec(),
    val actionSpace: AiActionSpaceDefinition = AiActionSpaceDefinition(),
    val properties: MutableMap<String, Any?> = mutableMapOf()
)

sealed interface AiAgentDriver {
    data class External(val endpoint: String? = null, val protocol: String = "in_process") : AiAgentDriver
    object Manual : AiAgentDriver
}

data class AiAgentBinding(
    val agentId: String,
    val scope: AiControlScope
)

data class AiPlayerConfig(
    val teamId: Int,
    val enabled: Boolean = false,
    val mode: AiPlayerMode = AiPlayerMode.EXTERNAL_AGENT,
    val agentId: String? = null,
    val scope: AiControlScope = AiControlScope(teamIds = setOf(teamId)),
    val properties: MutableMap<String, Any?> = mutableMapOf()
)

data class AiPlayerState(
    val teamId: Int,
    val name: String?,
    val enabled: Boolean,
    val mode: AiPlayerMode,
    val agentId: String?,
    val controlledByGameAi: Boolean,
    val defeated: Boolean,
    val unitCount: Int,
    val credits: Double,
    val energy: Double
)

enum class AiPlayerMode {
    HUMAN,
    GAME_AI,
    EXTERNAL_AGENT
}

data class AiControlScope(
    val teamIds: Set<Int> = emptySet(),
    val unitIds: Set<Long> = emptySet(),
    val unitTypeIds: Set<String> = emptySet()
)

data class AiObservationSpec(
    val includeMap: Boolean = false,
    val includeResources: Boolean = true,
    val includeUnitOrders: Boolean = true,
    val includeHiddenEnemies: Boolean = false,
    val frameStack: Int = 1
)

data class AiActionSpaceDefinition(
    val allowedTypes: Set<UnitCommandType> = UnitCommandType.entries.toSet(),
    val maxUnitsPerAction: Int? = null,
    val maxActionsPerTick: Int? = null,
    val allowQueuedCommands: Boolean = true
)

data class AiObservationScope(
    val teamIds: Set<Int> = emptySet(),
    val includeEnemies: Boolean = true,
    val includeNeutral: Boolean = true,
    val includeDestroyed: Boolean = false,
    val center: WorldPosition? = null,
    val radius: Float? = null,
    val maxUnits: Int? = null
)

data class AiObservationFrame(
    val frameId: Long,
    val tick: Long,
    val scope: AiObservationScope,
    val teams: List<AiTeamObservation>,
    val units: List<UnitRuntimeState>,
    val resourcePools: List<WorldPosition> = emptyList(),
    val map: AiMapObservation? = null,
    val stackedFrames: List<AiObservationFrame> = emptyList(),
    val terminalTeams: Set<Int> = emptySet()
)

data class AiTeamObservation(
    val id: Int,
    val name: String?,
    val credits: Double,
    val energy: Double,
    val unitCount: Int,
    val defeated: Boolean,
    val controlledByAi: Boolean
)

data class AiMapObservation(
    val width: Int,
    val height: Int,
    val tileWidth: Int? = null,
    val tileHeight: Int? = null
)
