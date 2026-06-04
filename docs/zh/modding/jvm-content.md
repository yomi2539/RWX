# 内容 API

`api.units` 用来声明自定义单位。单位声明由若干原生 INI Section 和一组 RWX 扩展组成。

## 注册单位

```kotlin
api.units.unit("scout") {
    // Section 与 Extension
}

val vehicle = unitTemplate {
    movement { movementType = MovementType.LAND }
}

api.units.unit("tank", vehicle) {
    core { maxHp = 400 }
}
```

`unitTemplate { }` 返回可复用的 `UnitTemplate`，多个模板按参数顺序应用后再执行当前配置。

## Section 入口

| 入口                           | 对应原生 Section           |
|--------------------------------|----------------------------|
| `core {}`                      | `[core]`                   |
| `graphics {}`                  | `[graphics]`               |
| `movement {}`                  | `[movement]`               |
| `attack {}`                    | `[attack]`                 |
| `ai {}`                        | `[ai]`                     |
| `resource(name) {}`            | `[resource_name]`          |
| `globalResource(name) {}`      | `[global_resource_name]`   |
| `action(name) {}`              | `[action_name]`            |
| `hiddenAction(name) {}`        | `[hiddenAction_name]`      |
| `turret(name) {}`              | `[turret_name]`            |
| `projectile(name) {}`          | `[projectile_name]`        |
| `effect(name) {}`              | `[effect_name]`            |
| `decal(name) {}`               | `[decal_name]`             |
| `attachment(name) {}`          | `[attachment_name]`        |
| `leg(name) {}`、`arm(name) {}` | `[leg_name]`、`[arm_name]` |
| `placementRule(name) {}`       | `[placementRule_name]`     |
| `animation(name) {}`           | `[animation_name]`         |
| `canBuild(name) {}`            | `[canBuild_name]`          |
| `template(name) {}`            | `[template_name]`          |
| `rawSection(name, values)`     | 直接写入任意 Section       |
| `extension {}`                 | 不生成 INI                 |
| `on(...)` / `listen(...)`      | 单位事件绑定               |

同名入口重复调用会修改同一个 Section。

## 一个单位

```kotlin
api.units.unit("scout") {
    core {
        displayText = "Scout"
        displayTextLang = mapOf("zh" to "侦察车")
        maxHp = 180
        mass = 600
        radius = 10
        price = 250
        buildSpeed = "6s"
        tags = listOf("vehicle", "scout")
    }
    graphics {
        image = "assets/units/scout.png"
        imageSmoothing = true
    }
    movement {
        movementType = MovementType.LAND
        moveSpeed = 2.1f
        maxTurnSpeed = 4f
    }
}
```

## 武器

武器由 `attack`、`turret` 和 `projectile` 三个 Section 组合：

```kotlin
api.units.unit("artillery") {
    attack {
        canAttack = true
        canAttackLandUnits = "true"
        maxAttackRange = 260f
    }
    turret("main") {
        y = 8f
        projectile = "shell"
        delay = 75f
        limitingRange = 260f
    }
    projectile("shell") {
        directDamage = 35
        areaDamage = 12
        areaRadius = 28
        life = 120
        speed = 4f
        image = "assets/projectiles/shell.png"
    }
}
```

`mutator(name) { }` 可以给投射物追加原生 Mutator，例如追踪、分裂等。

## Extension

`extension {}` 放 RWX 专属数据，不会写进 INI：

```kotlin
extension {
    shaderId = "unit_material"
    renderBinding = UnitRenderBinding("unit_overlay", "default")
    damageAvoidChance = 0.15f

    // 受击判定框，相对单位中心
    exposureOffsetX = 0f
    exposureOffsetY = -2f
    exposureWidth = 24f
    exposureHeight = 18f

    projectile("shell") {
        renderBinding = ProjectileRenderBinding("projectile", "shell")
        observerBinding = ProjectileObserverBinding("projectile-audio", "shell")
        directDamageAmount = 40f
        areaDamageAmount = 15f
    }
    turret("main") {
        preFireDuration = 8
        postFireDuration = 12
        observerBinding = TurretFireCycleObserverBinding("fire-cycle", "default")
    }
    effect("native_dust") {
        renderBinding = EffectRenderBinding("effect", "dust")
    }
}
```

- `renderBinding` 把单位、投射物、炮塔前摇/后摇、效果连接到自定义渲染器
- `damageAvoidChance`、`exposure*` 调整闪避和受击范围
- 投射物的 `directDamageAmount`、`areaDamageAmount` 等覆盖原生伤害；`rayDamage*` 让瞬时投射物按射线结算
- `preFireDuration` / `postFireDuration` 为 0 时不产生对应的开火窗口

`projectile(name)`、`turret(name)`、`effect(name)` 必须引用同一单位中已声明的同名 Section。

## Action

`action(name)` 和 `hiddenAction(name)` 暴露原生 Action 字段：

```kotlin
action("activate") {
    id = "activate"
    text = "Activate"
    iconImage = "assets/ui/activate.png"
    price = "energy=10"
    addActionCooldownTime = "12"
    alsoQueueAction = "activate_apply"
}
hiddenAction("activate_apply") {
    buildSpeed = "0.2s"
    addResources = "charge=1"
}
```

可见 Action 的运行时 ID 可以从单位快照的 `UnitAvailableAction.actionId` 读取；隐藏 Action 需要自己指定稳定的 `id`
，提交命令时使用相同的值。

## 单位事件

两个入口都在单位声明内使用：

- `on(id, event, condition) { spawnEffect(...) }` — 声明式动作，由引擎直接执行
- `listen(id, event, condition) { context -> ... }` — 回调，在事件发生时执行 Kotlin 代码

```kotlin
on("spawn_dust", UnitEvent.Created) {
    spawnEffect("native_dust")
}

listen(
    "low_health",
    UnitEvent.TookDamage(),
    condition = unitCondition { self.healthRatio lessThan 0.3f },
) { event ->
    val damage = event.payload as? UnitEventPayload.Damage ?: return@listen
    event.api.game.info("自爆倒计时: ${damage.amount}")
}
```

常用事件：`Created`、`CompleteAndActive`、`Destroyed`、`KilledUnit`、`QueuedUnitFinished`、`QueueItemAdded`、
`QueueItemCancelled`、`ActionCompleted`、`Teleported`、`TouchTargetSuccess`、`WaypointGivenByPlayer`、`TeamChanged`、
`TransportingUnit`、`TransportRemovedUnit`、`EnteredTransport`、`LeftTransport`、`TookDamage`、`MessageReceived`、
`AttachmentRemoved`。

`UnitEventContext` 提供 `api`、`tick`、`self`、可选的 `source` / `target`、`event` 类型和 `payload`。`source` / `target` 只包含
ID，需要详细状态时用 `api.unitWorld.state(ref)` 查询。

`condition` 使用 `unitCondition {}`：

```kotlin
val condition = unitCondition {
    (self.healthRatio lessThan 0.5f) and (target.exists) and (self.hasTag("vehicle"))
}
```

可用属性包括生命、护盾、能量、位置、速度、存活时间等的比值或绝对值，以及 `isDestroyed`、`isMoving`、`isAttacking`、
`isBuildComplete`；用 `and`、`or`、`not`、`allOf`、`anyOf` 组合。
