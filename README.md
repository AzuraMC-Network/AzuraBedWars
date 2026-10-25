# AzuraBedWars

[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://www.oracle.com/java/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.8--1.21%2B-green.svg)](https://www.minecraft.net/)
[![Spigot](https://img.shields.io/badge/Spigot-API-blue.svg)](https://www.spigotmc.org/)
[![License](https://img.shields.io/badge/License-AGPL--3.0-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)

[English](README.md) | [中文](README_CN.md)

---

A BedWars plugin currently under development

## Project Introduction

AzuraBedWars is a Minecraft bed wars plugin based on Spigot/Paper. This project is currently under development.

### Branches

- **master** — This branch contains the code for release builds.  
  Updates are pushed at the beginning of each month, usually from the 1st to the 3rd.
- **develop** — This branch is for daily updates and community pull requests.  
  It may contain unstable or experimental features.

### Preface

- This project is not yet complete and still needs many features to be finished. It is planned to be maintained until
  version 2.0.0
- This project provides NMS support for Minecraft versions 1.8–1.21+, and we plan to actively maintain support for
  future releases.
- If you use this project, you are responsible for any issues you encounter. We only recommend using it for testing
  purposes, not for production environments
- If you have suggestions or find issues, you can raise them in issues, and we will consider supporting them as soon as
  possible
- I am not very familiar with BukkitAPI, etc., so there may be many ambiguous places in the project

### Project Structure

- `api` — Public API, events and the NMS SPI
- `bootstrap` — Plugin entry that loads the game and its libraries in an isolated class loader
- `core` — Game logic running on the server
- `nms` — Per-version NMS implementations
- `plugin` — Packages the regular and offline plugin jars
- `examples/grapple` — Sample extension plugin, see [Writing extensions](docs/extensions.md)

Build with `./gradlew build`. Two jars are written to `plugin/build/libs/`:

- `AzuraBedWars-<version>.jar` downloads its libraries on first start into `plugins/AzuraBedWars/libraries/`, repositories are set in `config.yml`
- `AzuraBedWars-<version>-offline.jar` embeds every library for servers without internet access

Both jars check GitHub releases on startup and download a newer version of the same jar into `plugins/update/`, which the server installs on the next restart. Set `updater.enabled` to `false` in `config.yml` to turn this off.

## Performance Showcase

- This is the spark profiler result after a 16-player game completion. Perhaps this is what can attract you to choose
  our project (the censored parts are anti-cheat plugins)

![Performance Test Results](docs/images/spark.png)

## Quick Start

### Demo Server

- [AzuraBedWars-DemoServer](https://github.com/MindsMaster/AzuraBedWars-DemoServer)
- You can download the packaged demo server here to use directly

### Requirements

- **Java**: 21 or higher
- **Minecraft**: 1.8.8 - 1.21.11
- **Data Storage**: MySQL or MongoDB (required)
- **Dependencies**:
  - PacketEvents (required)
  - Vault (optional)
  - LuckPerms (optional)

## Contributing

We welcome all forms of contributions! Whether it's reporting bugs, suggesting new features, or submitting code, we are
very grateful.

### How to Contribute

1. **Fork the project**
2. **Create a feature branch** (`git checkout -b feature/YourFeatureName`)
3. **Commit your changes** (`git commit -m 'Add some AmazingFeature'`)
4. **Push to the branch** (`git push origin feature/YourFeatureName`)
5. **Create a Pull Request (please push to the develop branch)**

### Code Standards

- Follow Java coding conventions
- Use meaningful variable and method names
- Add appropriate comments
- Ensure code passes all tests

## License

This project is licensed under the GNU Affero General Public License v3.0 (AGPL-3.0)

## Authors

- **An5w1r_** - Main Developer
- **Ant1Aura** - Contributor
- **ImCur_** - Contributor
- **All0ra** - Contributor
- **Irina** - Contributor

## Contact

- **Email**: an5w1r@163.com
- **QQ Group**: 1046048297

## Disclaimer

This project is currently in development and may have unstable features. Please test thoroughly before using in
production environments.

---

⭐ If this project helps you, please give us a star! 
