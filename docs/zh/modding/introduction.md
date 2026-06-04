# 模组开发入门

RWX 支持 RW 风格的 INI 模组，也支持用 Java/Kotlin 编写的 JVM 模组。

JVM 模组通过 `mod-api` 访问游戏，内容包括：

- [JVM 模组](jvmMod.md) — 打包、`mod.toml`、生命周期与可用的服务
- [内容 API](jvm-content.md) — 声明单位、武器、扩展和事件
- [单位命令](jvm-unit-commands.md) — 向单位下达移动、攻击、建造等命令
- [渲染与效果](jvm-rendering.md) — 自定义渲染器、地形覆盖和 Shader
- [游戏运行时](jvm-runtime.md) — 调度、单位查询、音频、资源、地图与加密
- [Compose UI 与 HUD](jvm-ui.md) — 菜单、HUD、窗口和坐标选择
- [AI](jvm-ai.md) — AI 玩家、观察帧和命令提交

文档只说明接口的用法和效果；具体的内部行为可能随版本调整。
