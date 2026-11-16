<center>

![logo](logo.svg)

# Rusted Warfare eXtension

Rebuilding and extending Rusted Warfare as an open-source cross-platform RTS game

----

![GitHub Created At](https://img.shields.io/github/created-at/yomi2539/RWX?color=blue&style=for-the-badge)
[![Discord](https://img.shields.io/discord/1352880561215246376?style=for-the-badge&logo=discord)](https://discord.gg/q2amh4Gt3f)
[![License](https://img.shields.io/github/license/yomi2539/RWX?style=for-the-badge&color=blue)](./LICENSE)

[![Downloads](https://img.shields.io/github/downloads/yomi2539/RWX/total?style=flat-square&color=e74c3c)](https://github.com/yomi2539/RWX/releases)
[![Netlify Status](https://api.netlify.com/api/v1/badges/5c73d6b0-e2f9-46d7-a0d2-271b8f81b6b2/deploy-status)](https://app.netlify.com/projects/rwx-docs/deploys)

</center>

English, [简体中文](README_zh.md)

> **Project Status**: Actively maintained. Issues and pull requests are welcome.

## Highlights

- **Desktop + Android** supported
- **P2P multiplayer**: A brand-new way to play together
- **Area Control** mode: capture zones, continuous scoring, new win conditions
- **Linked Maps**: connect multiple maps with portals and transfer units between them
- **JVM modding** for deeper gameplay extensions

## Roadmap

### Platforms

- [x] Desktop build (Windows / Linux / macOS)
- [x] Android build

### Multiplayer

- [x] P2P-based multiplayer system
- [ ] Dedicated relay / fallback path improvements

### Gameplay features

- [x] Area Control mode
- [x] Linked Maps / map portals
- [ ] Balance / UX polish for Area Control and Linked Maps

## Building

Java 25 is required. Common release tasks:

```bash
# Current-platform desktop distribution via Compose Multiplatform built-in packaging
# (app image plus OS installers under desktop/build/compose/binaries)
./gradlew :desktop:packageDistributionForCurrentOS

# Current-platform single JAR (Compose uber jar, current OS natives only)
./gradlew :desktop:packageUberJarForCurrentOS

# Android APK
./gradlew :android:assembleRelease
```

See the [workflow](.github/workflows/ci.yml) and the
[getting started guide](https://rwx-docs.netlify.app/tutorial/getting-started) for more details.

## Thanks

The UI design references [RWPP](https://github.com/RWPP-Team/RWPP)

## Disclaimer

This is an unofficial extension project for Rusted Warfare, aiming to extend game functionality and gameplay through a
modern technology stack. All related assets used in this project belong to their original authors.
For educational and research purposes only, commercial use is prohibited.


---
