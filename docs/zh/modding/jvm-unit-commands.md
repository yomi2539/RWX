# 单位命令

`api.commands` 是向实时单位提交联机同步原生命令的入口。

## 提交命令

```kotlin
interface UnitCommands {
    fun submit(command: UnitCommandRequest): UnitCommandResult
    fun submit(commands: List<UnitCommandRequest>): List<UnitCommandResult>
}
```

列表重载按顺序逐条提交，某条被拒绝不会回滚之前已接受的请求。

示例：

```kotlin
val result = api.commands.submit(
    UnitCommandRequest(
        id = "example.move.42",
        teamId = unit.teamId,
        unitIds = listOf(unit.instanceId),
        type = UnitCommandType.MOVE,
        target = UnitCommandTarget.Point(WorldPosition(640f, 320f)),
        queued = true,
    )
)

if (!result.accepted) {
    api.game.log(LogLevel.WARN, result.message ?: "Command rejected")
}
```

## 请求字段

| 字段                | 说明                                                                                          |
|---------------------|-----------------------------------------------------------------------------------------------|
| `id`                | 调用方定义的关联 ID，可为空，原样返回为 `commandId`                                           |
| `teamId`            | 所有受命单位的所属队伍                                                                        |
| `unitIds`           | 非空的 `UnitInstanceId` 列表，重复项会被忽略                                                  |
| `type`              | `UnitCommandType` 原生命令类型                                                                |
| `target`            | 可选的 `UnitCommandTarget.Point` 或 `UnitCommandTarget.Unit`                                  |
| `queued`            | 追加到队列而不是替换当前队列                                                                  |
| `highPriority`      | 标记为高优先级                                                                                |
| `stopCurrentAction` | 请求停止当前正在执行的动作                                                                    |
| `actionId`          | `CUSTOM_ACTION` 使用的运行时原生 ActionId；可见 Action 从 `UnitAvailableAction.actionId` 读取 |
| `buildUnitTypeId`   | `BUILD` 使用的原生或自定义单位类型名                                                          |
| `buildQueueSize`    | 正整数建造数量，默认为 1                                                                      |
| `properties`        | 通用扩展值，当前原生命令执行忽略未知 key                                                      |

> **注意**
> 构造请求时会拒绝空 `unitIds` 或非正数 `buildQueueSize`。`UnitCommandResult.accepted` 表示 RWX
> 已将请求收入原生命令交付队列，不代表单位最终能完成命令。

## 命令类型

| `UnitCommandType` | 必需数据                                         |
|-------------------|--------------------------------------------------|
| `MOVE`            | 点目标                                           |
| `ATTACK_MOVE`     | 点目标                                           |
| `ATTACK_UNIT`     | 单位目标                                         |
| `BUILD`           | 点目标、`buildUnitTypeId`，可选 `buildQueueSize` |
| `REPAIR_UNIT`     | 单位目标                                         |
| `GUARD_UNIT`      | 单位目标                                         |
| `PATROL`          | 点目标                                           |
| `RECLAIM_UNIT`    | 单位目标                                         |
| `LOAD_INTO_UNIT`  | 单位目标                                         |
| `LOAD_UP_UNIT`    | 单位目标                                         |
| `STOP`            | 无目标                                           |
| `SET_RALLY`       | 点目标                                           |
| `CUSTOM_ACTION`   | 非空 `actionId`；目标可为空、点或单位            |
| `OTHER`           | 保留值，当前会被拒绝                             |

## Action ID 说明

`action(name)` / `hiddenAction(name)` 的 Section 后缀不是命令 ActionId。显式 `id = "activate"` 会被原生引擎解析为
`cactivate`；省略 `id` 时生成依赖解析顺序的 `_N`。

- 可见 Action：提交单位快照中 `UnitAvailableAction.actionId` 的值
- 隐藏 Action：为该 Action 设置稳定的 Spec `id`，再提交原生 `c<id>`

## 确定性规则

- 只在单位监听器、调度任务、同步队伍动作等确定性回调中提交影响模拟的命令
- 不要从 Renderer 或 Compose HUD / 窗口组合代码直接提交命令
- 对相同模拟状态必须按固定顺序生成相同请求
- 接近使用点时通过 `api.unitWorld` 重新解析单位，已保存引用可能失效
- `api.commands` 不应用 AI 控制范围或 `AiActionSpaceDefinition`，这些限制只属于 `AiBehavior.submitActions()`
