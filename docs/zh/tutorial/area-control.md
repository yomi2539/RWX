# 区域控制

区域控制是 RWX 的玩法模式：双方争夺 **控制区域（control zone）**。持续占领区域会按间隔获得分数，先达到分数上限的一方获胜。

内置示例地图：

- `maps/skirmish/[p4]RWX Circle Control Test (4p).tmx`

## 游玩流程

1. 单位进入区域并开始占领。
2. 占领完成后，区域按计分间隔持续加分。
3. 区域可被争夺，也可被中立化。
4. 某一方分数达到 `areaControlScoreLimit` 后结束。

模式会从地图元数据自动识别，并通过 RWX 特性通道广播为 `areaControl`。

## 地图制作

在地图中放置 `map_info` 对象并设置模式：

```xml
<object name="map_info" x="40" y="41" width="203" height="122">
  <properties>
    <property name="type" value="skirmish"/>
    <property name="rwxMode" value="areaControl"/>
    <property name="areaControlScoreLimit" value="180"/>
    <property name="areaControlScoreInterval" value="3"/>
  </properties>
</object>
```

属性说明：

| 属性                                         | 是否必须 | 说明                   |
|----------------------------------------------|----------|------------------------|
| `rwxMode` / `rwx_mode`                       | 是       | 值必须为 `areaControl` |
| `areaControlScoreLimit` / `scoreLimit`       | 否       | 目标分数               |
| `areaControlScoreInterval` / `scoreInterval` | 否       | 默认计分间隔（秒）     |

### 控制区域

每个区域是一个 `type="control_zone"` 的地图对象：

```xml
<object name="Northwest" type="control_zone" x="200" y="200" width="420" height="420">
  <properties>
    <property name="id" value="NW"/>
    <property name="shape" value="circle"/>
    <property name="captureTime" value="8"/>
    <property name="neutralizeTime" value="4"/>
    <property name="scoreRate" value="1"/>
    <property name="scoreInterval" value="3"/>
    <property name="groundOnly" value="true"/>
  </properties>
</object>
```

| 属性             | 说明                            |
|------------------|---------------------------------|
| `id`             | 区域稳定 ID                     |
| `shape`          | 例如 `circle`（使用对象包围盒） |
| `captureTime`    | 完全占领所需秒数                |
| `neutralizeTime` | 中立化所需秒数                  |
| `scoreRate`      | 每次计分获得的分数              |
| `scoreInterval`  | 该区域计分间隔（秒）            |
| `groundOnly`     | 为 true 时只统计地面单位        |

区域控制地图至少需要一个 `control_zone`。

## 联机注意

- 客户端通过 RWX 特性通道声明是否支持该模式。
- 使用区域控制地图的房间，要求对端支持 `areaControl`。
- 非 RWX 客户端无法正确识别此模式。

## 相关文档

- [地图联通](./linked-maps.md)
- [P2P 联机](./p2p.md)
