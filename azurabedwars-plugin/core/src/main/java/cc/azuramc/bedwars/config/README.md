# AzuraBedWars 配置系统使用指南

## 概述

AzuraBedWars 的配置系统支持两种格式：
- **YAML格式** - 推荐使用，用户友好，易于手动编辑
- **JSON格式** - 保留用于向后兼容和云存储场景

核心优势：
- ✅ 对外暴露YAML格式（用户友好）
- ✅ 内部使用Java类操作（类型安全）
- ✅ 支持JSON序列化（云存储兼容）
- ✅ **字段注释功能**（使用 @ConfigComment 注解）
- ✅ 自动转换和验证
- ✅ 线程安全

## 架构设计

```
┌─────────────────────────────────────────────────────────┐
│                      用户编辑层                           │
│                    *.yml 文件                            │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                  YamlConfigHandler                       │
│              (负责YAML文件读写)                           │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                   YamlConverter                          │
│           (YAML ↔ Java Object 转换)                     │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                  Java 配置类                             │
│         (SettingsConfig, MessageConfig 等)              │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                  Gson (JSON序列化)                       │
│                (用于云存储)                               │
└─────────────────────────────────────────────────────────┘
```

## 核心类说明

### 1. IConfigHandler 接口
配置处理器的统一接口，支持多种格式。

```java
public interface IConfigHandler<T> {
    T load(T defaultInstance);         // 加载配置
    void save(Object instance);         // 保存配置
    String toJson(Object instance);     // 序列化为JSON（云存储）
    T fromJson(String json);            // 从JSON反序列化（云存储）
}
```

### 2. YamlConfigHandler 类
YAML格式配置处理器，推荐使用。

**特点：**
- 读写YAML文件
- 自动添加文件头注释
- **自动读取 @ConfigComment 注解并为字段添加注释**
- 详细的错误日志
- 支持JSON序列化

### 3. ConfigHandler 类
JSON格式配置处理器，保留用于向后兼容。

### 4. YamlConverter 类
YAML与Java对象之间的转换工具。

**转换流程：**
- YAML → Map → JSON → Java Object
- Java Object → JSON → Map → YAML

### 5. ConfigManager 类
配置管理器，支持多种格式。

**核心功能：**
- 注册和管理配置
- 加载、保存、重载配置
- JSON序列化支持（云存储）

### 6. ConfigFactory 类
配置工厂，使用工厂模式创建配置。

**支持的格式：**
- `ConfigFormat.YAML` - YAML格式
- `ConfigFormat.JSON` - JSON格式

## 使用方法

### 方式一：使用 YAML 格式（推荐）

在插件主类中：

```java
private void initConfigSystem() {
    // 1. 创建配置管理器
    configManager = new ConfigManager(this);

    // 2. 创建配置工厂
    ConfigFactory configFactory = new ConfigFactory();

    // 3. 注册所有配置对象供应商
    configFactory.registerSupplier("settings", SettingsConfig::new);
    configFactory.registerSupplier("eventSettings", EventSettingsConfig::new);
    configFactory.registerSupplier("tasks", ResourceSpawnConfig::new);
    configFactory.registerSupplier("message", MessageConfig::new);
    configFactory.registerSupplier("items", ItemConfig::new);
    configFactory.registerSupplier("player", PlayerConfig::new);
    configFactory.registerSupplier("teamUpgrade", TeamUpgradeConfig::new);

    // 4. 初始化为 YAML 格式
    configFactory.initializeDefaultsAsYaml(configManager);
    // 或者使用: configFactory.initializeDefaults(configManager, ConfigFormat.YAML);

    // 5. 获取配置对象
    settingsConfig = configManager.getConfig("settings", SettingsConfig.class);
    eventSettingsConfig = configManager.getConfig("eventSettings", EventSettingsConfig.class);
    resourceSpawnConfig = configManager.getConfig("tasks", ResourceSpawnConfig.class);
    messageConfig = configManager.getConfig("message", MessageConfig.class);
    itemConfig = configManager.getConfig("items", ItemConfig.class);
    playerConfig = configManager.getConfig("player", PlayerConfig.class);
    teamUpgradeConfig = configManager.getConfig("teamUpgrade", TeamUpgradeConfig.class);

    // 6. 保存所有配置文件
    configManager.saveAll();
}
```

这将在 `plugins/AzuraBedWars/config/` 目录下生成：
- `settings.yml`
- `eventSettings.yml`
- `tasks.yml`
- `message.yml`
- `items.yml`
- `player.yml`
- `teamUpgrade.yml`

### 方式二：使用 JSON 格式（向后兼容）

```java
// 使用默认的 JSON 格式
configFactory.initializeDefaults(configManager);
// 或者明确指定: configFactory.initializeDefaults(configManager, ConfigFormat.JSON);
```

### 方式三：手动创建配置处理器

```java
// 创建 YAML 配置处理器
YamlConfigHandler<SettingsConfig> yamlHandler = new YamlConfigHandler<>(
    new File(getDataFolder(), "config/settings.yml"),
    SettingsConfig.class
);

// 或创建 JSON 配置处理器
ConfigHandler<SettingsConfig> jsonHandler = new ConfigHandler<>(
    new File(getDataFolder(), "config/settings.json"),
    SettingsConfig.class
);

// 注册到配置管理器
configManager.registerConfig("settings", yamlHandler, new SettingsConfig());
```

## 配置操作

### 1. 获取配置

```java
SettingsConfig settings = configManager.getConfig("settings", SettingsConfig.class);
```

### 2. 修改配置

```java
SettingsConfig settings = configManager.getConfig("settings", SettingsConfig.class);
settings.setDebugMode(true);
settings.setMaxHealth(30);
```

### 3. 保存配置

```java
// 保存单个配置
configManager.saveConfig("settings");

// 保存所有配置
configManager.saveAll();
```

### 4. 重载配置

```java
// 重载单个配置
configManager.reloadConfig("settings");

// 重载所有配置
configManager.reloadAll();
```

## 云存储支持

虽然本地使用YAML格式，但系统完整保留了JSON序列化能力，用于云存储等场景。

### 1. 导出配置到云存储

```java
// 将配置转换为JSON字符串
String json = configManager.configToJson("settings");

// 上传到云存储（示例）
cloudStorageService.upload("azurabw/settings.json", json);
```

### 2. 从云存储导入配置

```java
// 从云存储下载JSON字符串
String json = cloudStorageService.download("azurabw/settings.json");

// 从JSON加载配置（会自动保存为本地YAML文件）
configManager.configFromJson("settings", json);
```

### 3. 直接使用处理器

```java
YamlConfigHandler<SettingsConfig> handler = /* 获取handler */;

// 导出为JSON
String json = handler.toJson(settingsConfig);

// 从JSON导入
SettingsConfig config = handler.fromJson(json);
```

## YAML 文件示例

生成的YAML文件具有良好的可读性：

```yaml
# AzuraBedWars 配置文件
# 配置文件使用 YAML 格式，方便手动编辑
# 注意：修改配置后需要使用 /bw reload 命令重新加载
# 警告：请勿删除或修改配置项的键名，否则可能导致插件无法正常工作

editorMode: false
debugMode: false
teamBlockSearchRadius: 15
mapStorage: JSON
enabledJedisMapFeature: false
defaultMapName: game
bedSearchRadius: 18
bedDestroyReward: 10
maxHealth: 20
maxNoMovementTime: 45
teamSpawnProtectionRadius: 6
resourceSpawnProtectionRadius: 2
enableGameModeSelection: true
enableTeamSelection: true

database:
  databaseType: MySQL
  host: localhost
  port: 3306
  username: root
  password: '123456'
  database: azurabw
  useSSL: false

chatConfig:
  chatFormat: '&7[&e%team%&7] %player%: &f%message%'
  # ... 更多配置项
```

## 配置类定义

创建配置类时使用 Lombok 的 `@Data` 注解：

```java
import lombok.Data;

@Data
public class MyConfig {
    // 基础类型字段（会自动设置默认值）
    private boolean enabled = true;
    private int maxPlayers = 16;
    private String serverName = "AzuraBedWars";

    // 嵌套配置对象
    private DatabaseConfig database = new DatabaseConfig();

    // 嵌套类也需要 @Data 注解
    @Data
    public static class DatabaseConfig {
        private String host = "localhost";
        private int port = 3306;
        private String username = "root";
        private String password = "password";
    }
}
```

## 为配置添加注释（@ConfigComment）

使用 `@ConfigComment` 注解可以为YAML配置文件中的每个字段添加说明性注释，让用户更容易理解配置项的作用。

### 基本用法

```java
import cc.azuramc.bedwars.config.annotation.ConfigComment;
import lombok.Data;

@Data
public class MyConfig {
    @ConfigComment("是否启用调试模式")
    private boolean debugMode = false;

    @ConfigComment("服务器最大玩家数")
    private int maxPlayers = 100;

    @ConfigComment("服务器名称")
    private String serverName = "AzuraMC";
}
```

生成的YAML文件：

```yaml
# AzuraBedWars 配置文件
# ...

# 是否启用调试模式
debugMode: false

# 服务器最大玩家数
maxPlayers: 100

# 服务器名称
serverName: AzuraMC
```

### 多行注释

```java
@Data
public class MyConfig {
    @ConfigComment({
        "玩家最大生命值",
        "默认为20（10颗心）",
        "修改此值需要重启服务器"
    })
    private int maxHealth = 20;
}
```

生成的YAML文件：

```yaml
# 玩家最大生命值
# 默认为20（10颗心）
# 修改此值需要重启服务器
maxHealth: 20
```

### 嵌套配置的注释

```java
@Data
public class MyConfig {
    @ConfigComment("数据库配置")
    private DatabaseConfig database = new DatabaseConfig();

    @Data
    public static class DatabaseConfig {
        @ConfigComment("数据库类型 (MySQL/SQLite)")
        private String type = "MySQL";

        @ConfigComment("数据库主机地址")
        private String host = "localhost";

        @ConfigComment({
            "数据库端口",
            "MySQL默认: 3306",
            "PostgreSQL默认: 5432"
        })
        private int port = 3306;
    }
}
```

生成的YAML文件：

```yaml
# 数据库配置
database:
  # 数据库类型 (MySQL/SQLite)
  type: MySQL

  # 数据库主机地址
  host: localhost

  # 数据库端口
  # MySQL默认: 3306
  # PostgreSQL默认: 5432
  port: 3306
```

### 完整示例

查看示例文件了解更多用法：
- `cc.azuramc.bedwars.config.example.CommentedConfigExample`

### 注释最佳实践

1. **简洁明了**：注释应该简短且易于理解
2. **说明作用**：解释配置项的用途，而不是重复字段名
3. **提供示例**：对于复杂配置，提供示例值或格式说明
4. **注意事项**：如果修改配置有特殊要求，在注释中说明
5. **中英文**：如果面向国际用户，考虑使用英文注释

### 好的注释示例

```java
// ✅ 好的注释
@ConfigComment({
    "全局聊天冷却时间（秒）",
    "防止玩家刷屏，设置为0表示无冷却"
})
private int globalChatCooldown = 10;

// ✅ 好的注释
@ConfigComment("火球造成的伤害值（注意：实际伤害还会受到距离影响）")
private int fireballDamage = 3;

// ❌ 不好的注释（仅重复字段名）
@ConfigComment("调试模式")
private boolean debugMode = false;

// ❌ 不好的注释（过于冗长）
@ConfigComment({
    "这是一个用于控制是否启用调试模式的配置项",
    "当你想要在控制台看到更多的日志信息时",
    "可以将这个选项设置为true",
    "但是请注意不要在生产环境中启用",
    "因为这会产生大量的日志输出"
})
private boolean debugMode = false;
```

## 注意事项

1. **字段默认值**：所有配置字段都应设置默认值，确保首次创建配置文件时有合理的初始值。

2. **使用 Lombok**：配置类必须使用 `@Data` 注解，这样才能正确序列化/反序列化。

3. **嵌套对象**：嵌套的配置类也需要初始化，如 `new DatabaseConfig()`。

4. **线程安全**：ConfigManager 使用 ConcurrentHashMap，支持并发访问。

5. **错误处理**：加载失败时会返回默认实例，并记录错误日志。

6. **文件格式**：YAML文件扩展名为 `.yml`，JSON文件扩展名为 `.json`。

7. **重载时机**：修改配置后需要调用 `reloadConfig()` 或使用游戏内命令重载。

8. **云存储**：使用JSON格式进行云存储，确保跨平台兼容性。

## 迁移指南

### 从 JSON 迁移到 YAML

如果你已有JSON格式的配置文件，迁移到YAML格式：

1. **修改初始化代码**：
   ```java
   // 旧代码
   configFactory.initializeDefaults(configManager);

   // 新代码
   configFactory.initializeDefaultsAsYaml(configManager);
   ```

2. **删除旧的JSON文件**（可选）：
   ```
   plugins/AzuraBedWars/config/*.json
   ```

3. **重启服务器**，系统会自动生成YAML文件。

4. **备份重要配置**，以防数据丢失。

## 常见问题

### Q: YAML和JSON格式可以混用吗？

A: 可以。每个配置可以独立选择格式：

```java
// settings 使用 YAML
configManager.registerConfig("settings",
    new YamlConfigHandler<>(new File(..., "settings.yml"), SettingsConfig.class),
    new SettingsConfig());

// message 使用 JSON
configManager.registerConfig("message",
    new ConfigHandler<>(new File(..., "message.json"), MessageConfig.class),
    new MessageConfig());
```

### Q: 如何禁用某个配置的云存储？

A: 云存储功能是可选的，只在调用 `configToJson()` 或 `configFromJson()` 时使用。不调用这些方法即可。

### Q: YAML文件损坏怎么办？

A: 系统会自动捕获错误并使用默认配置，同时记录错误日志。你可以删除损坏的文件，重启后会自动生成新的配置文件。

### Q: 性能影响如何？

A: YAML解析略慢于JSON，但差异很小（毫秒级）。配置只在启动和重载时加载，对游戏性能影响可忽略不计。

## 总结

新的配置系统提供了最佳的灵活性：
- **用户** 可以轻松编辑 YAML 文件
- **开发者** 可以使用类型安全的 Java 对象
- **云存储** 可以使用标准的 JSON 格式

无缝集成，向后兼容，易于使用！

---

**作者**: AzuraBedWars Team
**版本**: 1.0.0
**更新日期**: 2025-11-26
