# 渲染与效果

`api.graphics` 负责纹理、各类渲染器、地形覆盖和 Shader。

```kotlin
api.graphics.registerTexture(
    "status-ring",
    "assets/status-ring.png",
    TextureOptions(filter = TextureFilter.LINEAR),
)
```

可注册的渲染器：

- `registerUnitRenderer(id, renderer)` — 单位
- `registerProjectileRenderer(id, renderer)` — 投射物 / Beam
- `registerPreFireRenderer(id, renderer)` — 炮塔前摇
- `registerPostFireRenderer(id, renderer)` — 炮塔后摇
- `registerEffectRenderer(id, renderer)` — 效果
- `registerTerrainOverlayRenderer(id, renderer)` — 地形覆盖物
- `registerShader` / `registerTerrainShader` — 单位 Shader 与地形 Shader

渲染器通过 `extension` 里的 Binding 绑定到具体单位、投射物或炮塔，详见[内容 API](jvm-content.md)。

## 单位渲染器

```kotlin
api.graphics.registerUnitRenderer("status-overlay") { context, canvas ->
    if (context.layer != UnitRenderLayer.OVER_UNIT || context.destroyed) return@registerUnitRenderer
    val size = maxOf(context.drawWidth, context.drawHeight) * 1.25f
    canvas.drawTexture("status-ring", context.centerX, context.centerY, size, size)
}

api.units.unit("scout") {
    extension { renderBinding = UnitRenderBinding("status-overlay", "default") }
}
```

`UnitRenderContext` 提供实例 ID、类型 ID、队伍、屏幕中心、绘制尺寸、碰撞半径、朝向、是否已摧毁、当前绘制层和变体 ID。
`UnitRenderLayer` 为 `UNDER_UNIT`、`BODY`、`OVER_UNIT`；`UnitRenderBinding.replaceBody = true` 可以隐藏原生本体，只保留自定义绘制。

## 绘制

`RenderCanvas` 提供：

- `save()` / `restore()`
- `rotate(degrees, pivotX, pivotY)`
- `scale(scaleX, scaleY, pivotX, pivotY)`，负值可镜像
- `drawTexture(...)`、`drawTextureRegion(...)`，支持 tint、透明度和 `ALPHA` / `ADDITIVE` 混合
- `drawCircle(...)`、`drawRect(...)`，支持填充或描边

`TextureRegion(left, top, right, bottom)` 使用 0..1 的归一化坐标，适合从图集截取。`RenderCanvas` 只在回调内有效，不要在回调结束后持有。

## 投射物与开火

```kotlin
api.graphics.registerProjectileRenderer("trail") { context, canvas ->
    val centerX = (context.startX + context.endX) * 0.5f
    val centerY = (context.startY + context.endY) * 0.5f
    val length = kotlin.math.hypot(
        context.endX - context.startX,
        context.endY - context.startY,
    )
    val angle = Math.toDegrees(
        kotlin.math.atan2(context.endY - context.startY, context.endX - context.startX).toDouble(),
    ).toFloat()
    canvas.save()
    canvas.rotate(angle, centerX, centerY)
    canvas.drawTexture("trail", centerX, centerY, length, 8f)
    canvas.restore()
}
```

- `ProjectileRenderContext`：起点、终点、存活时间、剩余时间、绘制尺寸、变体
- `PreFireRenderContext` / `PostFireRenderContext`：同上并带窗口总时长；前摇的终点跟随实时目标，后摇的终点固定在开火时的弹着点
- `EffectRenderContext`：原点、存活/总时长、旋转、透明度、缩放、阴影通道、速度占比

## 地形覆盖

先注册渲染器，再往瓦片上放 Binding：

```kotlin
api.graphics.registerTerrainOverlayRenderer("marker") { context, canvas ->
    canvas.drawCircle(
        context.centerX,
        context.centerY,
        minOf(context.width, context.height) * 0.4f,
        color = RgbaColor(80, 200, 255),
        opacity = 0.6f,
    )
}

api.maps.addOverlay(12, 8, TerrainOverlayBinding("marker", "default"))
```

`TerrainOverlayContext` 提供瓦片坐标、屏幕中心、尺寸、游戏时间和变体 ID。同一个瓦片可以叠加多个模组的覆盖物。

地形 Shader 由 `registerTerrainShader(TerrainShaderDefinition(id, fragment, sksl, noise))` 注册，三个文件路径均相对
`assets/`。同一时间只能有一个地形 Shader 生效。

## 开火周期观察器

观察器适合播放声音、切换动画或触发一次性逻辑：

```kotlin
api.game.registerTurretFireCycleObserver("audio") { context ->
    when (context.phase) {
        TurretFireCyclePhase.PRE_FIRE_STARTED -> { /* 开始瞄准 */ }
        TurretFireCyclePhase.POST_FIRE_STARTED -> { /* 开火 */ }
        else -> Unit
    }
}
```

一次射击分前摇和后摇两个窗口，各自依次产生 `STARTED`、`UPDATED`、`ENDED`，共享同一个实例 ID。窗口时长为 0 时不会产生对应相位。用
`phase.isPreFire` / `phase.isPostFire` 可以只看其中一段。

投射物观察器用法相同：`api.game.registerProjectileObserver(id) { context -> ... }`，提供 `STARTED`、`UPDATED`、`ENDED`
和起终点、存活时间等信息。

对象 ID 可能复用，需要区分实例时把 `generation` 一起作为键。

## Shader

```kotlin
api.graphics.registerShader(
    ShaderDefinition(
        id = "unit_material",
        fragment = "assets/shaders/unit.frag",
        targets = setOf(ShaderTarget.UNIT),
    ),
)
api.graphics.setUnitShader("scout", "unit_material")
```

`ShaderDefinition` 支持可选 vertex、必填 fragment、uniform 列表和多个 `ShaderTarget`。`setUnitShader` 传入 `null` 解除绑定。
`registerAnimation` 和 `addRenderPass` 目前只保存数据。
