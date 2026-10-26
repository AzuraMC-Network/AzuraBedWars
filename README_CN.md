# AzuraBedWars

[![Java](https://img.shields.io/badge/Java-8+-orange.svg)](https://adoptium.net/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.8.8--26.3-green.svg)](https://www.minecraft.net/)
[![Server](https://img.shields.io/badge/Server-Spigot%20%7C%20Paper-blue.svg)](https://papermc.io/)
[![License](https://img.shields.io/badge/License-AGPL--3.0-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)

[English](README.md) | [中文](README_CN.md)

---

一个正在开发的BedWars(起床战争)插件

## 项目简介

AzuraBedWars是一个基于Spigot/Paper的Minecraft床战插件。该项目目前正在开发中。

### 分支说明

- **master** — 用于发布版本的代码分支。  
  更新通常在每个月的 1-3 日进行。
- **develop** — 用于接收社区的 Pull Request 和日常更新的分支。  
  其中可能包含不稳定或实验性的功能。

### 前言

- 此项目并未完成，还差很多的特性才能完成，计划维护至2.0.0版本
- 同一个 jar 支持 Spigot 和 Paper 的 Minecraft 1.8.8 到最新的 26.3，并计划持续跟进更新的版本
- 如果使用此项目请自行负责遇到的任何问题，我们只建议用来测试使用，不要放到生产环境
- 如果你有建议和发现的问题，可以在issue提出，我们会考虑尽快支持
- 本人并不是很熟悉BukkitAPI等，可能项目里有很多有歧义的地方

## 下载

[Releases 页面](https://github.com/AzuraMC-Network/AzuraBedWars/releases) 的每个版本都有两个 jar，任选其一：

- `AzuraBedWars-<version>.jar` 首次启动时把依赖下载到 `plugins/AzuraBedWars/libraries/`，下载仓库在 `config.yml` 里配置
- `AzuraBedWars-<version>-offline.jar` 内置全部依赖，适合无法联网的服务器

两种 jar 启动时都会检查 GitHub release，把同类型的新版本下载到 `plugins/update/`，服务器下次重启时自动替换。在 `config.yml` 里把 `updater.enabled` 设为 `false` 可以关闭。

## 性能展示

- 这是在16人对局完成后的spark profiler结果，或许这是能吸引您选择我们项目的原因(打码的都是反作弊插件)

![性能测试结果](docs/images/spark.png)

## 快速开始

### 成品测试端

- [AzuraBedWars-DemoServer](https://github.com/MindsMaster/AzuraBedWars-DemoServer)
- 你可以在这里打包下载成品测试端来直接使用

### 运行要求

- **服务端**: Spigot 或 Paper，Minecraft 1.8.8 到 26.3。以后的新版本会用最接近的模块加载，并提示未经测试
- **Java**: 插件本身支持 Java 8 及以上，按你的 Minecraft 版本所需的 Java 版本来即可

  | Minecraft | Java |
  | --- | --- |
  | 1.8.8 - 1.16.5 | 8+ |
  | 1.17.x | 16+ |
  | 1.18 - 1.20.4 | 17+ |
  | 1.20.5 - 1.21.11 | 21+ |
  | 26.x | 25+ |

- **数据存储**: MySQL 或 MongoDB (必须)
- **依赖插件**:
    - PacketEvents (必装)
    - Vault (可选)
    - LuckPerms (可选)

### 配置

首次启动时会在 `plugins/AzuraBedWars/` 下生成配置文件：

| 文件 | 内容 |
| --- | --- |
| `config.yml` | 依赖下载仓库与自动更新，只在启动时读取 |
| `settings.yml` | 语言、经济模式、地图存储、大厅服 |
| `database.yml` | MySQL 或 MongoDB 连接 |
| `gameplay.yml`、`display.yml`、`items.yml` | 游戏规则、计分板与 Tab 列表、特殊道具 |
| `generators.yml` | 资源生成点 |
| `timeline.yml` | 游戏阶段及其触发的动作 |
| `upgrades.yml` | 团队升级与陷阱 |
| `shop/` | 商店菜单、装备等级链，每个分类一个文件 |
| `lang/` | 玩家与管理员文本，`en_US.yml` 和 `zh_CN.yml` |
| `devtools.yml` | 开发模式 MCP，见下文 |

- 新安装默认 `en_US`。把 `settings.yml` 里的 `language` 改成 `zh_CN` 即可使用中文文本，之后缺失的配置文件也会按中文模板生成
- 从旧版本升级的服务器会在首次启动时自动转换旧的配置和消息文件，并保留中文文本
- `/bw reload [文件]` 重载配置。错误会标出文件和行号，有错误的文件不会生效。部分改动要等下一局或重启才生效，命令会告诉你是哪些
- `/bw config check` 检查全部文件，`/bw config schema` 把 JSON schema 写到 `schema/`，供支持 YAML language server 的编辑器使用
- 插件覆盖过的文件都会在 `.history/` 里保留之前的版本

### 扩展

其他插件可以添加商店物品、购买动作与条件、升级与陷阱效果、时间线动作和占位符，服主再通过配置文件把它们放进游戏。见 [Writing extensions](docs/extensions.md) 和 [钩爪示例](examples/grapple)。

### 开发模式 MCP

在开发服上，AzuraBedWars 可以开启一个 [MCP](https://modelcontextprotocol.io/) 端点，让 Claude Code 等 AI 客户端读写配置文件和地图、检查地图、扫描世界、在游戏里显示标记。默认关闭，生产服请保持关闭。

1. 执行 `/bw devtools on`，或者在 `devtools.yml` 里设置 `enabled: true` 后执行 `/bw devtools restart`
2. 执行 `/bw devtools status`，会输出端点地址和一条带令牌、可直接粘贴的 `claude mcp add` 命令

端点监听 `127.0.0.1:25599`，除非打开 `allow-remote`，否则只接受本机连接。令牌保存在 `plugins/AzuraBedWars/devtools/token`，`/bw devtools rotate` 可以更换。有非管理员玩家在线时会拒绝写入，每次改动都可以从 `.history/` 回滚。

## 构建

```
./gradlew build
```

运行 Gradle 需要 JDK 17 或更高版本，构建本身使用 JDK 25 toolchain，缺失时会自动下载。两个 jar 都输出到 `plugin/build/libs/`。

### 结构解释

- `api` — 对外 API、事件与 NMS SPI
- `bootstrap` — 插件入口，在隔离类加载器里加载游戏本体与依赖
- `core` — 服务端运行的游戏逻辑
- `nms` — 各版本的 NMS 实现
- `plugin` — 打包常规版与离线版插件 jar
- `examples/grapple` — 扩展插件示例，见 [Writing extensions](docs/extensions.md)

## 贡献

我们欢迎所有形式的贡献！无论是报告bug、提出新功能建议，还是提交代码，我们都非常感激。

### 如何贡献

1. **Fork项目**
2. **创建功能分支** (`git checkout -b feature/YourFeatureName`)
3. **提交更改** (`git commit -m 'Add some AmazingFeature'`)
4. **推送到分支** (`git push origin feature/YourFeatureName`)
5. **创建Pull Request (请推送至develop分支)**

### 代码规范

- 遵循Java编码规范
- 使用有意义的变量和方法名
- 添加适当的注释
- 确保代码通过所有测试

## 许可证

本项目采用GNU Affero General Public License v3.0 (AGPL-3.0) 许可证

## 作者

- **An5w1r_** - 主要开发者
- **Ant1Aura** - 贡献者
- **ImCur_** - 贡献者
- **All0ra** - 贡献者
- **Irina** - 贡献者

## 联系方式

- **邮箱**: an5w1r@163.com
- **QQ群**: 1046048297

## 免责声明

本项目目前处于开发阶段，可能存在不稳定的功能。在生产环境中使用前，请充分测试。

---

⭐ 如果这个项目对你有帮助，请给我们一个星标！ 
