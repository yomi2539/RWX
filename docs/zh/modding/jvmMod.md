# JVM 模组

RWX JVM 模组是以独立类加载器加载的 JAR，只依赖平台无关的 `mod-api` 契约。不要引用 `core`、`desktop`、`android` 或
`com.corrodinggames.*` 内部类。

## 构建依赖

```kotlin
dependencies {
    compileOnly(files("libs/mod-api-<version>.jar"))
}
```

Kotlin、`mod-api` 和 Compose 运行时由宿主在运行时提供，不要打进 JAR。

## mod.toml

JAR 根目录须包含 `mod.toml` 和入口类，资源放在 `assets/`。

```toml
id = "example"
name = "Example Mod"
entrypoint = "com.example.ExampleMod"
version = "1.0.0"
author = "Author"
description = "Example JVM mod"
minGameVersion = "1.0.4"
thumbnail = "assets/thumbnail.png"
dependencies = ["another_mod"]
priority = "0"
```

| 字段             | 必填 | 说明                                                |
|------------------|------|-----------------------------------------------------|
| `id`             | 是   | 模组身份和默认内容命名空间                          |
| `name`           | 是   | 显示名称                                            |
| `entrypoint`     | 是   | `JvmMod` 子类全限定类名，必须有 public 无参构造函数 |
| `version`        | 否   | 模组版本，默认空字符串                              |
| `author`         | 否   | 作者                                                |
| `description`    | 否   | 描述                                                |
| `minGameVersion` | 否   | 最低 RWX 版本，默认 `1.0.4`                         |
| `thumbnail`      | 否   | 模组列表页缩略图资源路径，默认空字符串              |
| `dependencies`   | 否   | 依赖模组 ID 数组，依赖项会先初始化                  |
| `priority`       | 否   | 同层级初始化优先级，需用带引号的整数                |

> **注意**
> 缺失依赖只记录警告并继续加载。依赖环中的模组会被跳过。模组身份以 `mod.toml` 的 `id` 为准，与文件名无关。

## 入口与生命周期

```kotlin
class ExampleMod : JvmMod() {
    override fun init() {
        api.game.log(LogLevel.INFO, "Loading " + manifest.name)
        // 注册内容、渲染器、声音、UI 和运行时动作
    }

    override fun dispose() {
        // 释放不由 RWX 注册表拥有的资源
    }
}
```

加载器在调用 `init()` 前注入：

- `manifest` — `ModManifest`（JVM 模组为 `JvmModManifest`），包含 `mod.toml` 元数据
- `api` — `Api`，宿主提供的全部模组服务
- `classLoader` — 此模组的隔离类加载器，可用于读取 JAR 内资源
- `type` — 固定为 `JVM`

生命周期只有两个方法：

- `init()` — 模组启用后调用一次，完成全部声明和注册
- `dispose()` — 模组卸载、重载或初始化失败后调用

> **注意**
> `init()` 返回后，RWX 才会提取资源、编译单位声明并装入原生单位系统。`init()` 抛出异常时模组不会激活，RWX 会注销已登记的运行时资源并调用
> `dispose()`。

## Api 服务总览

```kotlin
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
```

| 服务           | 状态     | 说明                                                                |
|----------------|----------|---------------------------------------------------------------------|
| `game`         | 已接入   | Tick、日志、调度、队伍状态、队伍动作、投射物与开火周期观察器        |
| `assets`       | 已接入   | 模组资源和外部挂载目录读取                                          |
| `units`        | 已接入   | 原生单位 Spec 声明和 RWX Extension 侧车                             |
| `unitWorld`    | 已接入   | 确定性单位快照、查询、创建、Action 与传送                           |
| `commands`     | 已接入   | 向实时单位提交联机同步的原生命令                                    |
| `graphics`     | 部分接入 | 纹理、各类渲染器、单位 Shader 已接入；通用动画和 Render Pass 为占位 |
| `ui`           | 已接入   | 游戏内菜单、Compose 窗口与 HUD、消息和世界坐标选择                  |
| `audio`        | 部分接入 | 声音注册和播放已接入；音乐注册为占位                                |
| `localization` | 已接入   | 运行时词条注册、查找和参数替换                                      |
| `ai`           | 已接入   | AI 玩家配置、观察帧和命令提交                                       |
| `maps`         | 仅契约   | 地图和 Tileset 注册，尚未加入地图目录                               |
| `rules`        | 仅契约   | AI Profile、Game Mode 和全局规则尚未应用                            |

## 公共值类型

### ID 与命名空间

内容和全局运行时 ID 使用 `namespace:path`，命名空间建议始终等于 `manifest.id`。

| 类型                                                 | 格式                                                 |
|------------------------------------------------------|------------------------------------------------------|
| `UnitId`、`WeaponId`、`EffectId`                     | 小写命名空间限定，如 `example:scout`                 |
| `TeamActionId`、`TeamFlagId`、`ModWindowId`、`HudId` | 小写命名空间限定                                     |
| `ProjectileObserverId`、`TurretFireCycleObserverId`  | 小写命名空间限定                                     |
| `Tag`、`UnitEventBindingId`                          | 单位声明内的本地 ID                                  |
| `UnitActionId`                                       | 字母或数字开头，仅允许字母、数字、点、下划线和连字符 |
| `RendererId`、`RenderVariantId`、`TextureId`         | 非空且首尾无空格                                     |
| `ResourceId`                                         | 字母或数字开头，可包含 `_.:/#-`                      |
| `UnitInstanceId`                                     | 非负运行时对象 ID                                    |

原生 String 字段引用 Mod 内容时使用转换属性：

- `UnitId.nativeName` — 完整原生单位名
- `WeaponId.nativeName`、`EffectId.nativeName` — 单位本地的原生 Section 名
- `ResourcePath.nativePath` — 无前缀时自动添加 `ROOT:`
- `TeamFlagId.unitTag` — 与同步队伍 Flag 状态镜像的原生 Tag

### 时间、成本和文本

- `Ticks` — 非负模拟 Tick，`40.ticks` 等价于 `Ticks(40)`
- `Cost` — `Map<ResourceId, Double>`；`Cost.ZERO` 表示无成本
- `LocalizedText.key(key)` 引用词条，`literal(value)` 使用字面值，`bilingual(en, zh)` 创建中英双语文本
- `RgbaColor` 通道范围 `0..255`；`RgbaColor.rgb(r, g, b)` 默认 alpha 为 255
