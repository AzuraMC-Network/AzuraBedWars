package cc.azuramc.bedwars.config.example;

import cc.azuramc.bedwars.config.ConfigFactory;
import cc.azuramc.bedwars.config.ConfigManager;
import cc.azuramc.bedwars.config.object.*;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * YAML配置系统使用示例
 *
 * 这个类展示了如何在插件中使用新的YAML配置系统
 *
 * @author AzuraBedWars Team
 */
public class YamlConfigExample {

    private ConfigManager configManager;
    private SettingsConfig settingsConfig;
    private EventSettingsConfig eventSettingsConfig;
    private ResourceSpawnConfig resourceSpawnConfig;
    private MessageConfig messageConfig;
    private ItemConfig itemConfig;
    private PlayerConfig playerConfig;
    private TeamUpgradeConfig teamUpgradeConfig;

    /**
     * 初始化YAML配置系统
     * 在插件的 onEnable() 方法中调用
     *
     * @param plugin 插件实例
     */
    public void initConfigSystem(JavaPlugin plugin) {
        // 1. 创建配置管理器
        configManager = new ConfigManager(plugin);

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

        // 4. 初始化为 YAML 格式（推荐）
        configFactory.initializeDefaultsAsYaml(configManager);

        // 或者使用 JSON 格式（向后兼容）
        // configFactory.initializeDefaults(configManager);

        // 或者明确指定格式
        // configFactory.initializeDefaults(configManager, ConfigFactory.ConfigFormat.YAML);

        // 5. 获取配置对象供应用程序使用
        settingsConfig = configManager.getConfig("settings", SettingsConfig.class);
        eventSettingsConfig = configManager.getConfig("eventSettings", EventSettingsConfig.class);
        resourceSpawnConfig = configManager.getConfig("tasks", ResourceSpawnConfig.class);
        messageConfig = configManager.getConfig("message", MessageConfig.class);
        itemConfig = configManager.getConfig("items", ItemConfig.class);
        playerConfig = configManager.getConfig("player", PlayerConfig.class);
        teamUpgradeConfig = configManager.getConfig("teamUpgrade", TeamUpgradeConfig.class);

        // 6. 保存所有配置文件（首次启动时会生成配置文件）
        configManager.saveAll();

        plugin.getLogger().info("配置系统初始化完成！");
        plugin.getLogger().info("配置文件位置: " + configManager.getConfigDir().getAbsolutePath());
    }

    /**
     * 重新加载所有配置
     * 可以在 /bw reload 命令中调用
     */
    public void reloadConfigs() {
        configManager.reloadAll();

        // 重新获取配置对象
        settingsConfig = configManager.getConfig("settings", SettingsConfig.class);
        eventSettingsConfig = configManager.getConfig("eventSettings", EventSettingsConfig.class);
        resourceSpawnConfig = configManager.getConfig("tasks", ResourceSpawnConfig.class);
        messageConfig = configManager.getConfig("message", MessageConfig.class);
        itemConfig = configManager.getConfig("items", ItemConfig.class);
        playerConfig = configManager.getConfig("player", PlayerConfig.class);
        teamUpgradeConfig = configManager.getConfig("teamUpgrade", TeamUpgradeConfig.class);
    }

    /**
     * 保存所有配置
     */
    public void saveConfigs() {
        configManager.saveAll();
    }

    /**
     * 示例：修改配置并保存
     */
    public void exampleModifyConfig() {
        // 获取配置
        SettingsConfig settings = configManager.getConfig("settings", SettingsConfig.class);

        // 修改配置
        if (settings != null) {
            settings.setDebugMode(true);
            settings.setMaxHealth(30);

            // 保存配置
            configManager.saveConfig("settings");
        }
    }

    /**
     * 示例：云存储 - 导出配置到云端
     *
     * @return JSON字符串
     */
    public String exportToCloud() {
        // 将配置转换为JSON字符串
        String settingsJson = configManager.configToJson("settings");

        // 这里可以上传到云存储服务
        // cloudStorageService.upload("azurabw/settings.json", settingsJson);

        return settingsJson;
    }

    /**
     * 示例：云存储 - 从云端导入配置
     *
     * @param json 从云端下载的JSON字符串
     */
    public void importFromCloud(String json) {
        // 从JSON加载配置（会自动保存为本地YAML文件）
        configManager.configFromJson("settings", json);

        // 重新获取配置对象
        settingsConfig = configManager.getConfig("settings", SettingsConfig.class);
    }

    // Getters for config objects

    public SettingsConfig getSettingsConfig() {
        return settingsConfig;
    }

    public EventSettingsConfig getEventSettingsConfig() {
        return eventSettingsConfig;
    }

    public ResourceSpawnConfig getResourceSpawnConfig() {
        return resourceSpawnConfig;
    }

    public MessageConfig getMessageConfig() {
        return messageConfig;
    }

    public ItemConfig getItemConfig() {
        return itemConfig;
    }

    public PlayerConfig getPlayerConfig() {
        return playerConfig;
    }

    public TeamUpgradeConfig getTeamUpgradeConfig() {
        return teamUpgradeConfig;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
