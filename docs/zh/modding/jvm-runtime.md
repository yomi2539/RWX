# 游戏运行时

## Game

`api.game` 提供：

- `gameVersion`、`tick`、`headless` — 当前宿主状态（`headless` 当前固定为 `false`）
- `log(level, message)` — `DEBUG/INFO/WARN/ERROR` 日志
- `schedule(delayTicks, task)` — 延迟一次执行，返回可取消 `ScheduledTask`
- `repeat(intervalTicks, task)` — 按 Tick 重复执行，最小有效间隔为 1
- `localTeam()`、`team(teamId)` — 只读 `TeamState`
- `registerTeamAction(id, handler)` — 注册确定性队伍状态变更处理器
- `requestTeamAction(id, targetPosition?)` — 由本地队伍请求动作，返回 `true` 表示已入队
- `registerProjectileObserver`、`registerTurretFireCycleObserver` — 注册生命周期观察器

`TeamState` 提供 `teamId`、`resource(id)`、`hasFlag(id)`。`TeamActionContext` 可以 `trySpend`、`addResource`、`grantFlag`、
`revokeFlag`，并提供同步后的可选 `targetPosition`。

> **注意**
> 当前宿主的 `TeamState` 和 `TeamActionContext` 只支持 `credits`；读取其他资源返回 0，尝试增减自定义运行时资源会失败。

示例：

```kotlin
val activateScan = TeamActionId("example:activate_scan")
val scanActive = TeamFlagId("example:scan_active")
val price = Cost(mapOf(ResourceId("credits") to 100.0))

api.game.registerTeamAction(activateScan) { team ->
    if (team.trySpend(price)) {
        team.grantFlag(scanActive)
    }
}
```

同一 Tick 到期的调度任务按提交顺序执行。`delayTicks = 0` 的任务也在下一 Tick 执行，递归调度不会修改正在执行的任务批次。

> **注意**
> 模拟逻辑必须使用 `tick`、`schedule`、`repeat`，不要依赖 `System.currentTimeMillis()`、线程 sleep 或无固定种子的随机数。

## 单位世界

`api.unitWorld` 提供确定性的模拟线程单位快照与基础修改：

```kotlin
interface UnitWorld {
    fun state(ref: UnitRuntimeRef): UnitRuntimeState?
    fun query(query: UnitQuery = UnitQuery()): List<UnitRuntimeState>
    fun teleport(ref: UnitRuntimeRef, destination: WorldPosition): Boolean
    fun spawn(request: UnitSpawnRequest): UnitRuntimeRef?
    fun update(ref: UnitRuntimeRef, update: UnitRuntimeUpdate): Boolean
}
```

`UnitRuntimeState` 包含：位置、朝向、类型 ID、生命/护盾/能量/护甲、建造进度、碰撞半径、可命令/移动/攻击能力、建筑/建造器/工厂标志、当前攻击目标、Tags、可用
Actions 和扩展属性。

`UnitQuery` 字段：

- `teamId` — 可选队伍过滤
- `definitionIds` — 可选单位类型过滤，空集合表示不过滤
- `includeDestroyed` — 是否包含已摧毁单位
- `center` + `radius` — 范围过滤，距离按二维 x/y 计算，边界包含在结果内

`UnitSpawnRequest` 字段：

- `unitId` — 必须是调用 Mod 通过 `api.units` 声明的 `UnitId`
- `teamId`、`position`、`rotationDegrees`
- `initialHealth` — `null` 保留单位类型默认初始生命
- `constructionProgress` — `0f..1f`，`1f` 表示完成

`UnitRuntimeUpdate` 可选设置 `health` 或 `constructionProgress`，至少提供一项。

- `state(ref)` — 单位被摧毁或引用失效后返回 `null`
- `query()` — 结果按 `UnitInstanceId` 升序排列，可用于确定性冲突选择
- `teleport()` — 引用失效时返回 `false`，会触发 `UnitEvent.Teleported`
- `spawn()` — 单位类型或队伍无效时返回 `null`
- `update()` — 单位已摧毁或引用失效时返回 `false`；生命限制在最大生命以内，建造进度保持在 `0f..1f`

> **注意**
> `UnitWorld` 的查询和修改只能从 `UnitEventHandler`、`ModTask`、队伍动作处理器等确定性模拟回调调用，不能从 Compose HUD /
> 窗口组合
> 或 Renderer 回调调用。联机各端必须从相同的同步事件执行相同操作。

## Audio

`api.audio` 提供：

- `registerSound(id, file, properties)` — 注册声音；当前支持的 property 是 `minimumVolume`（`0f..1f`）
- `playSound(id, x, y, volume)` — 播放已注册声音；`x/y` 为空时按非定位声音处理
- `registerMusic(id, file, properties)` — 接口已公开，当前尚未加入音乐播放列表

声音资源放在 JAR 的 `assets/` 下，在 `init()` 中注册。

## Localization

- `Localization.register(locale, entries)` — 注册运行时词条
- `translate(key, locale, args)` — 查找指定 locale，把文本中的 `{name}` 替换为参数值，找不到时返回 key

窗口可结合 `ModWindowContext.locale` 调用 `resolve(locale)` 或 `api.localization.translate()`。

## Assets

`api.assets` 提供：

- `mount(namespace, ResourcePath)` — 把命名空间挂到外部根目录
- `open(ResourcePath)` — 返回 `ResourceStream`
- `exists(ResourcePath)`
- `list(ResourcePath)`

`ResourceStream` 支持 `readBytes()`、`readText(charset)` 和 `close()`。无命名空间路径相对模组提取目录解析；`namespace:path`
优先从对应 mount 解析。

> **注意**
> 资源挂载允许读取宿主文件系统位置，但不会通过联机同步这些文件。确定性内容应打包进模组 JAR。

## 地图与规则

- `Map.registerMap(MapDefinition)` — ID、名称、地图文件、可选预览和 tags
- `Map.registerTileset(TilesetDefinition)` — ID、图像和扩展属性
- `Rule.registerAiProfile(AiProfileDefinition)`
- `Rule.registerGameMode(GameModeDefinition)`
- `Rule.setGlobalRule(key, value)`

这些接口当前只提供数据模型，调用不会改变当前游戏行为。

## 资产加密

RWX 支持对 JAR 内 `assets/` 下的文件进行可选加密，提供三种模式：

| 模式             | 打包参数                                   | 玩家导入                  | 说明                              |
|------------------|--------------------------------------------|---------------------------|-----------------------------------|
| 对称加密，嵌入式 | `--embedded-key`                           | 无                        | 密钥存储在 JAR 中，仅阻止随意解包 |
| 对称加密，外部   | `--symmetric-key author.rwxkey`            | 相同的 `.rwxkey`          | 一个共享密钥泄露即授权所有人      |
| PKI              | `--pki-author`、`--recipient`、`--crl-url` | `.rwxpub` + `.rwxlicense` | 个人许可证，支持按证书吊销        |

所有模式均使用 AES-256-GCM 加密，每个资产生成独立随机 nonce。

生成对称密钥：

```shell
./gradlew :mod-tools:run --args='keygen symmetric author.rwxkey'
./gradlew :mod-tools:run --args='encrypt mod.jar mod-protected.jar --symmetric-key author.rwxkey'
```

PKI 初始化：

```shell
# 创建作者 CA
./gradlew :mod-tools:run --args='keygen authority author.rwxauthor author.rwxpub --name ExampleAuthor'
# 为玩家签发许可证
./gradlew :mod-tools:run --args='pki issue author.rwxauthor alice.rwxcert alice.rwxlicense --subject Alice --days 365'
# 创建初始 CRL 并发布到 HTTPS
./gradlew :mod-tools:run --args='pki crl author.rwxauthor current.rwxcrl --valid-hours 24'
# 打包
./gradlew :mod-tools:run --args='encrypt mod.jar mod-protected.jar --pki-author author.rwxauthor --recipient alice.rwxcert --crl-url https://mods.example.com/example/current.rwxcrl'
```

吊销泄露的证书：

```shell
./gradlew :mod-tools:run --args='pki revoke author.rwxauthor alice.rwxcert current.rwxcrl --reason leaked --valid-hours 24 --force'
```

PKI 文件角色：

| 扩展名        | 内容                     | 持有者                        |
|---------------|--------------------------|-------------------------------|
| `.rwxauthor`  | 作者证书和根私钥         | 仅作者，绝不分发              |
| `.rwxpub`     | 自签名作者公开信任证书   | 公开发布，玩家显式导入        |
| `.rwxcert`    | 某位玩家的已签名公开证书 | 作者保留，用于打包和吊销      |
| `.rwxlicense` | 某位玩家的证书和私钥     | 私下发送给该玩家              |
| `.rwxcrl`     | 已签名短期吊销列表       | 作者发布到包内指定 HTTPS 地址 |
| `.rwxkey`     | 共享 AES-256 密钥        | 仅用于外部对称模式            |

> **注意**
> CRL 最长有效 24 小时，必须在过期前续期并重新发布。RWX 每次加载 PKI 模组都会绕过缓存获取 CRL，网络错误或验证失败会阻止模组加载。
> `.rwxauthor` 文件包含根私钥，绝不可分发或提交到版本控制。
