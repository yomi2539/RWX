# 游戏运行时

## Game

`api.game` 提供 Tick、日志、调度、队伍状态和生命周期观察：

```kotlin
api.game.info("hello")
api.game.schedule(40) { api.game.info("1 秒后执行") }
api.game.repeat(20) { /* 每 20 tick 执行一次 */ }

val task = api.game.repeat(80) { /* ... */ }
task.cancel()
```

队伍动作由模组声明，再由本地队伍请求执行，适合做需要联机同步的开关、技能或购买：

```kotlin
api.game.registerTeamAction("activate_scan") { team ->
    if (team.trySpend(Cost(mapOf(Cost.CREDITS_ID to 100.0)))) {
        team.grantFlag("scan_active")
    }
}

api.game.requestTeamAction("activate_scan", position)
```

`TeamState` 可以读取 `resource(id)` 和 `hasFlag(id)`；`TeamActionContext` 还可以 `trySpend`、`addResource`、`grantFlag`、
`revokeFlag`。

> 影响模拟的逻辑请使用 `tick`、`schedule`、`repeat`，不要依赖系统时间、线程 sleep 或不可复现的随机数。

## 单位世界

`api.unitWorld` 用于查询和修改实时单位：

```kotlin
fun state(ref: UnitRuntimeRef): UnitRuntimeState?
fun query(query: UnitQuery = UnitQuery()): List<UnitRuntimeState>
fun teleport(ref: UnitRuntimeRef, destination: WorldPosition): Boolean
fun spawn(request: UnitSpawnRequest): UnitRuntimeRef?
fun update(ref: UnitRuntimeRef, update: UnitRuntimeUpdate): Boolean
```

```kotlin
val nearby = api.unitWorld.query(
    UnitQuery(center = position, radius = 120f, teamId = 1)
)

val ref = api.unitWorld.spawn(
    UnitSpawnRequest(
        unitId = "scout",
        teamId = 1,
        position = WorldPosition(400f, 300f),
        constructionProgress = 0f,
    )
)

api.unitWorld.update(ref!!, UnitRuntimeUpdate(constructionProgress = 0.5f))
```

`UnitRuntimeState` 包含位置、朝向、类型、生命/护盾/能量/护甲、建造进度、碰撞半径、能力标志、当前目标、Tags、可用 Action 和扩展属性。

`UnitQuery` 支持队伍、单位类型、是否包含已摧毁单位和圆形范围过滤。`UnitSpawnRequest` 的 `unitId` 必须是当前模组声明的单位。

> 单位引用可能失效，使用前用 `state(ref)` 重新确认；查询和修改应在事件回调、调度任务或队伍动作等模拟回调中执行，不要从渲染器或
> Compose 组合中调用。

## Audio

```kotlin
api.audio.registerSound(
    "shoot",
    "assets/audio/shoot.ogg",
    mapOf("minimumVolume" to 0.2f),
)
api.audio.playSound("shoot", x = 320f, y = 240f, volume = 0.8f)
```

不传 `x` / `y` 时按非定位声音播放。`registerMusic` 目前只保存数据。

## Localization

```kotlin
api.localization.register("zh-CN", mapOf("example.hello" to "你好，{name}"))
val text = api.localization.translate("example.hello", "zh-CN", mapOf("name" to "指挥官"))
```

找不到词条时返回 key。窗口和 HUD 可以结合 `ModWindowContext.locale` 使用。

## Assets

```kotlin
api.assets.mount("media", "/path/to/dir")
api.assets.exists("media:units/scout.png")
val bytes = api.assets.open("media:units/scout.png").use { it.readBytes() }
```

普通路径从模组 `assets/` 读取；`挂载名:路径` 优先从对应挂载目录读取。挂载目录只在本机可读，不会被联机同步。

## 地图与地形

```kotlin
val info = api.maps.info()          // 尺寸和瓦片大小
val tile = api.maps.tileAt(x, y)    // water / lava / cliff / blocksBuildings
```

`api.maps.addOverlay(x, y, TerrainOverlayBinding("marker", "default"))` 可以在瓦片上叠加自定义绘制，`removeOverlay`、
`overlayAt`、`overlaysAt` 用于管理这些覆盖物。渲染部分见[渲染与效果](jvm-rendering.md)。

`api.maps.registerMap(...)`、`registerTileset(...)` 和 `api.rules.*` 目前只保存数据，不会直接改变当前对局。

## 资源加密

模组 JAR 中的 `assets/` 可以选择性加密，避免被随意解包：

| 模式           | 打包参数                                   | 玩家需要                  |
|----------------|--------------------------------------------|---------------------------|
| 内嵌密钥       | `--embedded-key`                           | 无                        |
| 共享密钥       | `--symmetric-key author.rwxkey`            | 同一个 `.rwxkey`          |
| PKI 个人许可证 | `--pki-author`、`--recipient`、`--crl-url` | `.rwxpub` + `.rwxlicense` |

```shell
./gradlew :mod-tools:run --args='keygen symmetric author.rwxkey'
./gradlew :mod-tools:run --args='encrypt mod.jar mod-protected.jar --symmetric-key author.rwxkey'
```

PKI 流程：

```shell
# 作者身份
./gradlew :mod-tools:run --args='keygen authority author.rwxauthor author.rwxpub --name ExampleAuthor'
# 为玩家签发许可证
./gradlew :mod-tools:run --args='pki issue author.rwxauthor alice.rwxcert alice.rwxlicense --subject Alice --days 365'
# 发布吊销列表
./gradlew :mod-tools:run --args='pki crl author.rwxauthor current.rwxcrl --valid-hours 24'
# 打包
./gradlew :mod-tools:run --args='encrypt mod.jar mod-protected.jar --pki-author author.rwxauthor --recipient alice.rwxcert --crl-url https://mods.example.com/current.rwxcrl'
```

`.rwxauthor` 含根私钥，不能分发或提交；PKI 模组加载时会拉取证书吊销列表，证书被吊销或验证失败会导致加载失败。
