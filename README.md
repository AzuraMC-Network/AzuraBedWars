# AzuraBedWars

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.8+-green.svg)](https://www.minecraft.net/)
[![Spigot](https://img.shields.io/badge/Spigot-API-blue.svg)](https://www.spigotmc.org/)
[![License](https://img.shields.io/badge/License-AGPL--3.0-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)

[English](README.md) | [中文](README_CN.md)

---

A BedWars plugin currently under development

## Project Introduction

AzuraBedWars is a Minecraft bed wars plugin based on Spigot/Paper. This project is currently under development.

### Preface

- This project is not yet complete and still needs many features to be finished. It is planned to be maintained until
  version 2.0.0
- We plan to support versions 1.8-1.21+, but unfortunately, development and testing are currently only done on version
  1.8.8
- If you use this project, you are responsible for any issues you encounter. We only recommend using it for testing
  purposes, not for production environments
- If you have suggestions or find issues, you can raise them in issues, and we will consider supporting them as soon as
  possible
- I am not very familiar with BukkitAPI, etc., so there may be many ambiguous places in the project

### Project Structure

- `azurabedwars-dashboard` — A Spring Boot web dashboard for dynamically managing plugin configurations and server
  settings.
- `azurabedwars-loader` — A auto downloader/updater for the azurabedwars-plugin.
- `azurabedwars-plugin` — The core Minecraft BedWars plugin running on the server.

## Performance Showcase

- This is the spark profiler result after a 16-player game completion. Perhaps this is what can attract you to choose
  our project (the censored parts are anti-cheat plugins)

![Performance Test Results](image/spark.png)

## Quick Start

### Demo Server

- [AzuraBedWars-DemoServer](https://github.com/MindsMaster/AzuraBedWars-DemoServer)
- You can download the packaged demo server here to use directly

### Requirements

- **Java**: 17 or higher
- **Minecraft**: 1.8.8
- **Data Storage**: MySQL (required)
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

## Contact

- **Email**: an5w1r@163.com
- **QQ Group**: 1046048297

## Disclaimer

This project is currently in development and may have unstable features. Please test thoroughly before using in
production environments.

---

⭐ If this project helps you, please give us a star! 
