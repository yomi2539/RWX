# JVM 模组

JVM 模组是一个由独立类加载器加载的 JAR，只依赖平台无关的 `mod-api`。

## 构建依赖

```kotlin
dependencies {
    compileOnly(files("libs/mod-api.jar"))
}
```

Kotlin、`mod-api` 和 Compose 运行时由宿主提供，不要打进 JAR。

## build.gradle.kts 模板

```kotlin
plugins {
    kotlin("jvm") version "2.4.20"
    // 用到 @Composable 的 HUD/窗口时需要
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.20"
}

val d8: Configuration by configurations.creating { isTransitive = false }

dependencies {
    compileOnly(files("libs/mod-api.jar"))
    compileOnly("org.jetbrains.compose.runtime:runtime:1.12.1")
    d8("com.android.tools:r8:8.13.6")
}

val modName = "example-mod.jar"

val dexDir = layout.buildDirectory.dir("dex")

val dex by tasks.registering {
    dependsOn(tasks.classes)
    val classDirs = sourceSets.main.get().output.classesDirs
    inputs.files(classDirs)
    outputs.dir(dexDir)
    doLast {
        val out = dexDir.get().asFile
        out.deleteRecursively()
        out.mkdirs()
        javaexec {
            classpath = d8
            mainClass.set("com.android.tools.r8.D8")
            args("--release", "--min-api", "28", "--output", out.absolutePath)
            args(classDirs.files.map { it.absolutePath })
        }
    }
}

// mod.toml 和 assets/ 放在 src/main/resources 下
tasks.jar {
    archiveFileName.set(modName)
    dependsOn(dex)
    from(dexDir)
}
```

执行 `./gradlew jar`，产物在 `build/libs/example-mod.jar`。Kotlin、Compose 和 D8 的版本按当前游戏使用的版本调整。

## 目录结构

```text
example.jar
├── mod.toml
├── com/example/ExampleMod.class
└── assets/
    └── units/scout.png
```

所有资源路径都相对 JAR 内的 `assets/` 目录。

## mod.toml

```toml
id = "example"
name = "Example Mod"
entrypoint = "com.example.ExampleMod"
version = "1.0.0"
author = "Author"
description = "Example JVM mod"
minGameVersion = "1.1.4"
thumbnail = "assets/thumbnail.png"
dependencies = ["another_mod"]
priority = 0
```

| 字段             | 必填 | 说明                                        |
|------------------|------|---------------------------------------------|
| `id`             | 是   | 模组身份                                    |
| `name`           | 是   | 显示名称                                    |
| `entrypoint`     | 是   | `JvmMod` 子类全限定名，需要 public 无参构造 |
| `version`        | 否   | 模组版本                                    |
| `author`         | 否   | 作者                                        |
| `description`    | 否   | 描述                                        |
| `minGameVersion` | 否   | 最低游戏版本                                |
| `thumbnail`      | 否   | 模组列表缩略图路径                          |
| `dependencies`   | 否   | 依赖的模组 ID，依赖项先初始化               |
| `priority`       | 否   | 同层初始化顺序，值越大越先初始化            |

缺失依赖只会记录警告；依赖成环的模组会被跳过。

## 入口与生命周期

```kotlin
class ExampleMod : JvmMod() {
    override fun init() {
        api.game.info("Loaded " + manifest.name)
        // 注册内容、渲染器、声音和 UI
    }

    override fun dispose() {
        // 释放不由 RWX 管理的资源
    }
}
```

加载器会在调用 `init()` 前注入：

- `manifest` — `mod.toml` 元数据
- `api` — 全部模组服务
- `classLoader` — 当前模组的类加载器

`init()` 在模组启用后调用一次，`dispose()` 在卸载、重载或初始化失败后调用。通过 `api` 注册的内容会在卸载时由 RWX 注销。

## Api 服务

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

| 服务           | 用途                                     |
|----------------|------------------------------------------|
| `game`         | Tick、日志、调度、队伍动作、生命周期观察 |
| `assets`       | 读取 JAR 资源、挂载外部目录              |
| `units`        | 声明自定义单位                           |
| `unitWorld`    | 查询单位，创建、传送、修改生命与建造进度 |
| `commands`     | 向实时单位提交原生命令                   |
| `graphics`     | 纹理、渲染器、Shader、地形覆盖           |
| `ui`           | 菜单、HUD、窗口、消息、世界坐标选择      |
| `audio`        | 声音注册与播放                           |
| `localization` | 运行时词条                               |
| `ai`           | AI 玩家、观察帧与命令提交                |
| `maps`         | 地图与瓦片查询、地形覆盖、地图注册       |
| `rules`        | AI Profile、Game Mode、全局规则          |

部分接口目前只保存数据或仍在校准，实际效果以当前游戏版本为准。
