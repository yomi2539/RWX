# Compose UI 与 HUD

`api.ui` 提供游戏内菜单、HUD、模组窗口、消息和世界坐标选择。HUD 与窗口内容均为 Compose `@Composable` 函数，可直接使用 Compose
的布局、控件和状态 API。

## 接口总览

```kotlin
interface Ui {
    fun addInGameMenuItem(menuId: Int? = null, text: LocalizedText, onClick: (id: Int) -> Unit)
    fun removeInGameMenuItem(menuId: Int)
    fun selectedUnits(): List<UnitRuntimeState>
    fun registerHud(id: HudId, order: Int = 0, content: @Composable () -> Unit)
    fun unregisterHud(id: HudId)
    fun setNativeHudVisible(visible: Boolean)
    fun registerWindow(
        id: ModWindowId,
        title: LocalizedText,
        content: @Composable (context: ModWindowContext) -> Unit,
    )
    fun openWindow(id: ModWindowId)
    fun closeWindow()
    fun refreshWindow()
    fun showMessage(message: LocalizedText, durationTicks: Int = 180)
    fun requestWorldPosition(
        onSelected: (WorldPosition) -> Unit,
        onCancelled: () -> Unit = {},
    ): WorldPositionSelection
}
```

`HudId` 和 `ModWindowId` 都要求小写的 `namespace:path`，同一 ID 全局只能注册一次。模组卸载时宿主会自动移除其注册的所有菜单项、HUD、窗口和坐标选择。

## 战场 HUD

`registerHud()` 把 Compose 图层加入战场 HUD。全屏盒子内按 `order` 升序、再按 `HudId` 排序合成，后绘制的图层在上方；图层未消费的指针与按键输入会透传给游戏。

```kotlin
api.ui.registerHud(HudId("example:battle"), order = 10) {
    Box(Modifier.fillMaxSize()) {
        Text(
            text = "Tick: ${api.game.tick}",
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        )
    }
}
```

内容在 UI 线程组合，通过快照或 Compose state 读取游戏状态。外部数据变化时用 `remember` / `mutableStateOf` 持有并更新；需要逐帧读取时把
tick 等状态提升为 Compose state，避免在组合外直接触碰引擎对象。

> **注意**
> HUD 内容不能注册游戏内容、分配长期资源或提交模拟操作。

## 隐藏原生 HUD

`setNativeHudVisible(false)` 隐藏原生资源栏、单位 Action、迷你地图等，但保留世界渲染、镜头、框选和战场命令输入。只要有任意模组请求隐藏，原生
HUD 就保持隐藏。

```kotlin
override fun init() {
    api.ui.setNativeHudVisible(false)
    api.ui.registerHud(HudId("example:battle")) { BattleHud() }
}

override fun dispose() {
    api.ui.unregisterHud(HudId("example:battle"))
    api.ui.setNativeHudVisible(true)
}
```

## 已选中单位

`selectedUnits()` 返回本机玩家当前选中的存活单位快照，顺序与原生选择列表一致。

```kotlin
api.ui.registerHud(HudId("example:selection")) {
    val unit = api.ui.selectedUnits().singleOrNull()
    Text(unit?.let { "HP ${it.health.toInt()} / ${it.maxHealth.toInt()}" } ?: "")
}
```

> **注意**
> `selectedUnits()` 只读取本机 UI 选择，不属于确定性模拟状态。联机中其他客户端可能有不同选择，不能用于确定性决策。

## 模组窗口

`registerWindow()` 注册一个全屏窗口，宿主负责绘制标题与返回按钮，`content` 填充中间的响应式列。`openWindow()` 显示该页面，
`closeWindow()` 返回游戏，`refreshWindow()` 从头重建内容。

`ModWindowContext` 提供：

- `api` — 拥有该窗口的 `Api`
- `locale` — 当前 BCP 47 locale tag
- `refresh()` — 从头重建当前窗口
- `close()` — 关闭窗口并返回游戏

```kotlin
val windowId = ModWindowId("example:status")

api.ui.registerWindow(windowId, LocalizedText.bilingual("Status", "状态")) { context ->
    Column(Modifier.fillMaxWidth()) {
        Text("Tick: ${context.api.game.tick}")
        Button(onClick = { context.close() }) {
            Text("Close")
        }
    }
}

api.ui.addInGameMenuItem(menuId = 42001, text = LocalizedText.bilingual("Status", "状态")) {
    api.ui.openWindow(windowId)
}
```

## 游戏内菜单

- `addInGameMenuItem(menuId?, text, onClick)` — 向原生游戏内菜单加入一项；省略 `menuId` 时宿主从 `26000` 开始分配，并把实际
  ID 传给 `onClick`
- `removeInGameMenuItem(menuId)` — 主动移除菜单项（卸载时自动清理）

## 世界坐标选择

`requestWorldPosition()` 启动一次世界坐标选择。左键确认，右键或 Escape 取消；全局同时只能有一个请求。返回的
`WorldPositionSelection.active` 表示请求是否仍在等待，`cancel()` 可主动取消。

```kotlin
api.ui.requestWorldPosition(
    onSelected = { position ->
        api.game.requestTeamAction(TeamActionId("example:activate_scan"), position)
    },
    onCancelled = {
        api.ui.showMessage(LocalizedText.literal("Selection cancelled"))
    },
)
```

> **注意**
> HUD、窗口、菜单和坐标选择回调不能直接修改模拟。自定义同步操作应调用 `requestTeamAction()`，再在已注册的队伍动作处理器中验证并执行；原生单位命令通过
> UI 事件回调调用 `api.commands` 提交。

## 资源与线程

HUD 与窗口内容运行在 UI 线程，应保持短时执行且不阻塞；不要在组合中触碰确定性模拟对象或提交命令。需要与模拟交互时走队伍动作或
`api.commands`，状态通过 Compose state 流入组合。
