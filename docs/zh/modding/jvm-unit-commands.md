# 单位命令

`api.commands` 向实时单位提交原生命令。

```kotlin
val result = api.commands.submit(
    UnitCommandRequest(
        id = "move.42",
        teamId = unit.teamId,
        unitIds = listOf(unit.instanceId),
        type = UnitCommandType.MOVE,
        target = UnitCommandTarget.Point(WorldPosition(640f, 320f)),
        queued = true,
    ),
)

if (!result.accepted) {
    api.game.warning(result.message ?: "Command rejected")
}
```

`submit` 返回的 `accepted` 表示请求已进入交付队列，不代表单位一定能完成命令。列表重载会按顺序逐条提交，前面的请求不会因为后面的失败而回滚。

## 请求字段

| 字段                | 说明                                                  |
|---------------------|-------------------------------------------------------|
| `id`                | 调用方自带的关联 ID，会原样返回                       |
| `teamId`            | 所有受命单位的队伍                                    |
| `unitIds`           | 单位实例 ID 列表，重复项会被忽略                      |
| `type`              | `UnitCommandType`                                     |
| `target`            | `UnitCommandTarget.Point` 或 `UnitCommandTarget.Unit` |
| `queued`            | 追加到队列，而不是替换当前命令                        |
| `highPriority`      | 高优先级                                              |
| `stopCurrentAction` | 先停止当前动作                                        |
| `actionId`          | `CUSTOM_ACTION` 使用的 Action ID                      |
| `buildUnitTypeId`   | `BUILD` 使用的单位类型                                |
| `buildQueueSize`    | 建造数量，默认 1                                      |
| `properties`        | 预留扩展字段                                          |

`unitIds` 不能为空，`buildQueueSize` 必须为正数。

## 命令类型

| 类型                                                                                         | 需要                                |
|----------------------------------------------------------------------------------------------|-------------------------------------|
| `MOVE`、`ATTACK_MOVE`、`PATROL`、`SET_RALLY`                                                 | 点目标                              |
| `ATTACK_UNIT`、`REPAIR_UNIT`、`GUARD_UNIT`、`RECLAIM_UNIT`、`LOAD_INTO_UNIT`、`LOAD_UP_UNIT` | 单位目标                            |
| `BUILD`                                                                                      | 点目标、`buildUnitTypeId`、可选数量 |
| `STOP`                                                                                       | 无                                  |
| `CUSTOM_ACTION`                                                                              | `actionId`                          |
| `OTHER`                                                                                      | 保留值，会被拒绝                    |

## Action ID

`CUSTOM_ACTION` 需要原生 Action ID：

- 可见 Action 用单位快照里 `UnitAvailableAction.actionId` 的值
- 隐藏 Action 在 `hiddenAction` 的 `id` 中指定一个稳定值，提交时使用同一个值

## 使用建议

- 命令影响模拟，应在事件回调、调度任务或队伍动作中提交，不要从渲染器或 Compose 组合中提交
- 保存的单位引用可能失效，使用前通过 `api.unitWorld.state(ref)` 重新确认
- 联机时各端要从相同的同步事件生成相同的命令
- `api.commands` 不应用 AI 控制范围和 Action Space，需要限制时使用 `api.ai.submitActions`
