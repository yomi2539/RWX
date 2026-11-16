<center>

![logo](logo.svg)

# Rusted Warfare eXtension

重建和扩展 Rusted Warfare 的开源跨平台 RTS 游戏

----

![GitHub Created At](https://img.shields.io/github/created-at/yomi2539/RWX?color=blue&style=for-the-badge)
[![Discord](https://img.shields.io/discord/1352880561215246376?style=for-the-badge&logo=discord)](https://discord.gg/q2amh4Gt3f)
[![QQ](https://img.shields.io/badge/QQ-982838086-orange?style=for-the-badge&logo=qq)](https://qm.qq.com/cgi-bin/qm/qr?k=kupOkNOePIjHK4sSdiJE-9YRdh3ANwum&jump_from=webapi&authKey=/fjvR18rZdV+4fe6gmVlBQkSwLZxoT0L2MYpxl8G2yph2YtqseZn2RAO556LJooZ)
[![License](https://img.shields.io/github/license/yomi2539/RWX?style=for-the-badge&color=blue)](./LICENSE)

[![Downloads](https://img.shields.io/github/downloads/yomi2539/RWX/total?style=flat-square&color=e74c3c)](https://github.com/yomi2539/RWX/releases)
[![Netlify Status](https://api.netlify.com/api/v1/badges/5c73d6b0-e2f9-46d7-a0d2-271b8f81b6b2/deploy-status)](https://app.netlify.com/projects/rwx-docs/deploys)

</center>

简体中文, [English](README.md)

> **项目状态**: 当前项目仍在积极维护中，欢迎提交 Issue 和 Pull Request。

## 亮点

- 支持 **Desktop + Android**
- **P2P 联机**：全新联机方式
- **区域控制**模式：占领区域、持续计分、改写胜负条件
- **地图联通**：用传送门连接多张地图，单位可跨图转移
- **JVM 模组**，便于更深度地扩展玩法

## 开发路线图

### 平台

- [x] Desktop 构建（Windows / Linux / macOS）
- [x] Android 构建

### 联机

- [x] 基于 P2P 的联机系统
- [ ] 中继 / 兜底连通性改进

### 玩法特性

- [x] 区域控制模式
- [x] 地图联通 / 地图传送门
- [ ] 区域控制与地图联通的平衡 / 交互打磨

## 编译

构建需要 Java 25。常用发行任务如下：

```bash
# 当前平台桌面发行包（Compose Multiplatform 内置打包：
# 应用镜像 + 系统安装包，产物在 desktop/build/compose/binaries）
./gradlew :desktop:packageDistributionForCurrentOS

# 当前平台单 JAR（Compose uber jar，仅含当前 OS 原生库）
./gradlew :desktop:packageUberJarForCurrentOS

# Android APK
./gradlew :android:assembleRelease
```

更多信息请参考 [CI/CD 配置](.github/workflows/ci.yml) 以及
[文档站快速开始](https://rwx-docs.netlify.app/zh/tutorial/getting-started)。

## 感谢

UI设计参考了[RWPP](https://github.com/RWPP-Team/RWPP)

## 免责声明

本项目为 Rusted Warfare 的非官方扩展项目，旨在通过现代技术栈扩展游戏功能和玩法。本项目所使用的相关资产均归原作者所有。仅为学习研究目的，禁止商业用途。

---
