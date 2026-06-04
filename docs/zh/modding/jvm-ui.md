# Compose UI 与 HUD

`api.ui` 提供游戏内菜单、HUD、模组窗口、提示消息和世界坐标选择。内容都是 Compose `@Composable`，可以直接使用 Compose
的布局、控件和状态。

```kotlin
interface Ui {
    fun addInGameMenuItem(menuId: Int? = null, text: LocalizedText, onClick: (id: Int) -> Unit)
    fun removeInGameMenuItem(menuId: Int)
    fun selectedUnits(): List<UnitRuntimeState>
    fun registerHud(id: String, order: Int = 0, content: @Composable () -> Unit)
    fun unregisterHud(id: String)
    fun setNativeHudVisible(visible: Boolean)
    fun registerWindow(
        id: String,
        title: LocalizedText,
        content: @Composable (context: ModWindowContext) -> Unit,
    )
    fun openWindow(id: String)
    fun closeWindow()
    fun refreshWindow()
    fun showMessage(message: LocalizedText, durationTicks: Int = 180)
    fun requestWorldPosition(
        onSelected: (WorldPosition) -> Unit,
        onCancelled: () -> Unit = {},
    ): WorldPositionSelection
}
```

模组卸载时，RWX 会自动清理该模组注册的菜单项、HUD、窗口和坐标选择。

## HUD

`registerHud` 把 Compose 图层叠到战场上。多个图层按 `order` 从小到大绘制，未消费的输入会继续传给游戏。

```kotlin
api.ui.registerHud("battle", order = 10) {
    Box(Modifier.fillMaxSize()) {
        Text(
            text = "Tick: ${api.game.tick}",
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
        )
    }
}
```

需要隐藏原生资源栏、Action 栏和迷你地图时调用 `api.ui.setNativeHudVisible(false)`，世界渲染、镜头和框选仍会保留：

```kotlin
override fun init() {
    api.ui.setNativeHudVisible(false)
    api.ui.registerHud("battle") { BattleHud() }
}

override fun dispose() {
    api.ui.setNativeHudVisible(true)
}
```

`selectedUnits()` 返回本机玩家当前选中的单位快照，适合做选择面板。它只反映本机 UI 状态，不要用于联机同步的决策。

## 窗口

```kotlin
api.ui.registerWindow("status", LocalizedText.literal("状态")) { context ->
    Column(Modifier.fillMaxWidth()) {
        Text("Tick: ${context.api.game.tick}")
        Button(onClick = context::close) {
            Text("Close")
        }
    }
}

api.ui.addInGameMenuItem(text = LocalizedText.literal("Status")) {
    api.ui.openWindow("status")
}
```

`ModWindowContext` 提供 `api`、当前 `locale`、`refresh()` 和 `close()`。宿主负责标题和返回按钮，`content` 填满剩余区域。

`showMessage` 在战场上显示一条提示，`durationTicks` 控制显示时长。

## 世界坐标选择

```kotlin
api.ui.requestWorldPosition(
    onSelected = { position ->
        api.game.requestTeamAction("activate_scan", position)
    },
    onCancelled = {
        api.ui.showMessage(LocalizedText.literal("已取消"))
    },
)
```

左键确认，右键或 Escape 取消。返回的 `WorldPositionSelection` 可以查看 `active` 状态或主动 `cancel()`。

> HUD、窗口和菜单回调运行在 UI 线程，不要直接修改模拟。需要同步的操作走队伍动作或 `api.commands`，状态通过 Compose state
> 流入界面。
