# AI

`api.ai` 用来配置 AI 玩家、读取观察帧和提交单位命令。

```kotlin
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
```

## Agent 与玩家

```kotlin
api.ai.registerAgent(
    AiAgentDefinition(
        id = "bot",
        displayName = LocalizedText.literal("Example Bot"),
        driver = AiAgentDriver.Manual,
    ),
)

api.ai.bindAgent(
    AiAgentBinding(
        agentId = "bot",
        scope = AiControlScope(teamIds = setOf(1)),
    ),
)

api.ai.configurePlayer(
    AiPlayerConfig(
        teamId = 1,
        enabled = true,
        mode = AiPlayerMode.EXTERNAL_AGENT,
        agentId = "bot",
    ),
)
```

`AiControlScope` 可以按队伍、单位实例或单位类型限制控制范围。`AiPlayerMode` 为 `HUMAN`、`GAME_AI`、`EXTERNAL_AGENT`
。这个接口只提供进程内数据和命令契约，不负责连接外部进程。

`AiPlayerState` 返回玩家名称、启用状态、模式、AI 控制状态、败北状态、单位数和资源。

## 观察帧

```kotlin
val frame = api.ai.currentObservation(
    AiObservationScope(teamIds = setOf(1), includeEnemies = true),
)
```

`AiObservationScope` 可以筛选队伍、敌人、中立单位、已摧毁单位、范围和最大数量。`AiObservationFrame` 包含：

- `frameId`、`tick`、`scope`
- `teams` — 各队伍的资源、单位数、败北和 AI 状态
- `units` — 与 `api.unitWorld` 相同的单位快照
- `map` — 可选的宽高和瓦片尺寸
- `stackedFrames`、`terminalTeams`

`AiObservationSpec` 控制是否包含地图、资源、单位命令、隐藏敌人以及堆叠多少帧。

## 提交命令

```kotlin
val frame = api.ai.currentObservation(AiObservationScope(teamIds = setOf(1)))
val units = frame.units.filter { it.ref.teamId == 1 && !it.destroyed }

val results = api.ai.submitActions(
    units.map { unit ->
        UnitCommandRequest(
            teamId = 1,
            unitIds = listOf(unit.ref.instanceId),
            type = UnitCommandType.ATTACK_MOVE,
            target = UnitCommandTarget.Point(WorldPosition(500f, 500f)),
        )
    },
)
```

命令格式与 `api.commands` 相同，但 `submitActions` 会额外应用控制范围、Action Space 和观察过滤。`ai.submitActions` 与
`api.commands` 的区别只在 AI 限制，命令本身的含义一致。
