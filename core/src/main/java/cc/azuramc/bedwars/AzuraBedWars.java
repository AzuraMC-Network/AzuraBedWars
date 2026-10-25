package cc.azuramc.bedwars;

import cc.azuramc.bedwars.api.AzuraBedWarsAPI;
import cc.azuramc.bedwars.api.game.IGamePlayer;
import cc.azuramc.bedwars.api.nms.NMSAccess;
import cc.azuramc.bedwars.bootstrap.PluginEntrypoint;
import cc.azuramc.bedwars.command.CommandRegistry;
import cc.azuramc.bedwars.config.ApplyMode;
import cc.azuramc.bedwars.config.ConfigProblem;
import cc.azuramc.bedwars.config.ConfigService;
import cc.azuramc.bedwars.config.migrate.LegacyConfigMigrator;
import cc.azuramc.bedwars.config.object.*;
import cc.azuramc.bedwars.config.object.shop.ShopCategoryConfig;
import cc.azuramc.bedwars.config.object.shop.ShopMenuConfig;
import cc.azuramc.bedwars.config.object.shop.TierChains;
import cc.azuramc.bedwars.database.provider.DatabaseProviderFactory;
import cc.azuramc.bedwars.devtools.Devtools;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.item.special.AbstractSpecialItem;
import cc.azuramc.bedwars.game.level.PlayerLevelManager;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.game.map.MapManager;
import cc.azuramc.bedwars.gui.base.listener.GUIListener;
import cc.azuramc.bedwars.listener.ListenerRegistry;
import cc.azuramc.bedwars.listener.packet.InvisibilityPacketListener;
import cc.azuramc.bedwars.listener.setup.SetupItemListener;
import cc.azuramc.bedwars.nms.NMSProvider;
import cc.azuramc.bedwars.scoreboard.ScoreboardManager;
import cc.azuramc.bedwars.shop.ItemSpecs;
import cc.azuramc.bedwars.shop.ShopValidator;
import cc.azuramc.bedwars.upgrade.UpgradeValidator;
import cc.azuramc.bedwars.util.LoggerUtil;
import cc.azuramc.bedwars.util.bstats.Metrics;
import cc.azuramc.bedwars.util.nms.NMSMapping;
import cc.azuramc.bedwars.util.nms.ReflectionUtil;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.event.Event;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * AzuraBedWars插件主类
 * 负责插件的初始化、配置加载、事件注册等核心功能
 *
 * @author an5w1r@163.com
 */
@Getter
public final class AzuraBedWars implements PluginEntrypoint {
    private static final String PLUGIN_PREFIX = "[AzuraBedWars] ";

    @Getter
    private static AzuraBedWars instance;

    private final JavaPlugin plugin;
    private Chat chat = null;
    private ConfigService configService;
    private DatabaseProviderFactory databaseProviderFactory;
    private Devtools devtools;
    private Economy econ = null;
    private GameManager gameManager;
    private LuckPerms luckPermsApi;
    /** True when editorMode is on or no map can run */
    private boolean editing;
    private MapManager mapManager;
    private NMSAccess nmsAccess;
    private NMSProvider nmsProvider;
    private ScoreboardManager scoreboardManager;
    private SetupItemListener setupItemListener;

    public AzuraBedWars(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(plugin));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        instance = this;
        PacketEvents.getAPI().init();
        // 初始化配置系统
        initConfigSystem();
        devtools = new Devtools(this);
        if (getDevtoolsConfig().isEnabled()) {
            devtools.captureLogs();
        }

        // ReflectionUtil初始化版本
        ReflectionUtil.initializeVersions();
        NMSMapping.initNmsMapping();

        setupNMSSupport();

        // 初始化基础服务
        initDatabases();
        mapManager = new MapManager();

        SettingsConfig settings = getSettingsConfig();
        MapData mapData = settings.isEditorMode() ? null : mapManager.loadGameMap(settings.getDefaultMapName());
        editing = mapData == null;
        if (editing) {
            LoggerUtil.warn(settings.isEditorMode() ? "当前处于编辑模式 /map 设置地图" : "没有可以运行的地图 已进入编辑模式 用 /map 设置地图后重启");
            initEditorFeatures();
        } else {
            initGameFeatures(mapData);
        }

        initCommands();
        initDevtools();
        // bStats
        loadBStats();

        Bukkit.getConsoleSender().sendMessage(PLUGIN_PREFIX + "加载完成耗时 " + (System.currentTimeMillis() - startTime) + " ms");
    }

    @Override
    public void onDisable() {
        if (devtools != null) {
            devtools.stop();
        }

        if (gameManager != null) {
            gameManager.getTimeline().stop();
        }

        // 关闭数据库服务
        if (databaseProviderFactory != null) {
            databaseProviderFactory.getPlayerDataService().shutdown();
            databaseProviderFactory.getDatabaseProvider().shutdown();
        }

        PacketEvents.getAPI().terminate();
    }

    /**
     * 初始化数据库连接
     */
    private void initDatabases() {
        databaseProviderFactory = new DatabaseProviderFactory(this);
    }

    /**
     * 初始化命令处理器
     */
    private void initCommands() {
        new CommandRegistry(this);
    }

    private void initDevtools() {
        if (getDevtoolsConfig().isEnabled()) {
            String error = devtools.start();
            if (error != null) {
                LoggerUtil.error("MCP devtools did not start: " + error);
            }
        }
    }

    /**
     * 初始化编辑模式下的功能
     */
    private void initEditorFeatures() {
        setupItemListener = new SetupItemListener();
        Bukkit.getPluginManager().registerEvents(setupItemListener, plugin);
    }

    /**
     * 初始化游戏相关功能
     */
    private void initGameFeatures(MapData mapData) {
        LoggerUtil.info("开始加载游戏相关特性...");
        // 注册GUI监听器
        new GUIListener(this);

        // Hook Vault Chat and Econ
        hookVault();
        hookLuckPerms();

        // 创建游戏管理器
        gameManager = new GameManager(this);
        gameManager.loadGame(mapData);

        // 注册各种事件监听器
        registerEventListeners();
        registerBungeeChannel();

        // 加载特殊物品和玩家等级
        AbstractSpecialItem.loadSpecials();
        PlayerLevelManager.loadLevelData();

        // 配置世界设置
        configureWorlds();

        LoggerUtil.info("游戏相关特性加载完成");
    }

    /**
     * 注册所有事件监听器
     */
    private void registerEventListeners() {
        new ListenerRegistry(this);

        // 创建并初始化计分板管理器
        scoreboardManager = new ScoreboardManager(gameManager);
        scoreboardManager.initialize(plugin);

        PacketEvents.getAPI().getEventManager().registerListener(new InvisibilityPacketListener());
    }

    /**
     * 注册BungeeCord通信频道
     */
    private void registerBungeeChannel() {
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
    }

    /**
     * 配置游戏世界设置
     */
    private void configureWorlds() {
        Bukkit.getWorlds().forEach(world -> {
            world.setAutoSave(false);
            world.setDifficulty(Difficulty.NORMAL);
        });
    }

    /**
     * 在主线程上执行任务
     *
     * @param runnable 要执行的任务
     */
    public void mainThreadRunnable(Runnable runnable) {
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    private void setupNMSSupport() {
        // 注册 API 门面实现，供 NMS 版本模块与第三方通过 AzuraBedWarsAPI 访问运行时，
        // 而无需依赖 core 具体类（玩家查找委托 GamePlayer，日志委托 LoggerUtil）
        AzuraBedWarsAPI.setProvider(new AzuraBedWarsAPI.Provider() {
            @Override
            public IGamePlayer getPlayer(java.util.UUID uuid) {
                return GamePlayer.get(uuid);
            }

            @Override
            public void info(String message) {
                LoggerUtil.info(message);
            }

            @Override
            public void warn(String message) {
                LoggerUtil.warn(message);
            }

            @Override
            public void warn(String message, Throwable throwable) {
                LoggerUtil.warn(message, throwable);
            }

            @Override
            public void error(String message) {
                LoggerUtil.error(message);
            }

            @Override
            public void error(String message, Throwable throwable) {
                LoggerUtil.error(message, throwable);
            }

            @Override
            public void debug(String message) {
                LoggerUtil.debug(message);
            }
        });

        nmsProvider = new NMSProvider();
        nmsAccess = nmsProvider.setup();
    }

    private void hookLuckPerms() {
        if (plugin.getServer().getPluginManager().getPlugin("LuckPerms") != null) {
            luckPermsApi = LuckPermsProvider.get();
        }
    }

    private void hookVault() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            setupEconomy();
            setupChat();
        }
    }

    /**
     * 设置经济系统
     */
    private void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            econ = rsp.getProvider();
        }
    }

    /**
     * 设置聊天系统
     */
    private void setupChat() {
        RegisteredServiceProvider<Chat> rsp = plugin.getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp != null) {
            chat = rsp.getProvider();
        }
    }

    /**
     * 调用Bukkit事件
     *
     * @param event 要调用的事件
     */
    public void callEvent(Event event) {
        plugin.getServer().getPluginManager().callEvent(event);
    }

    private void initConfigSystem() {
        configService = new ConfigService(plugin.getDataFolder(),
                () -> gameManager != null && gameManager.getGameState() != GameState.WAITING);
        configService.register("settings", "settings.yml", SettingsConfig.class, ApplyMode.LIVE, 1);
        configService.register("database", "database.yml", DatabaseConfig.class, ApplyMode.RESTART, 1);
        configService.register("gameplay", "gameplay.yml", GameplayConfig.class, ApplyMode.LIVE, 1);
        configService.register("display", "display.yml", DisplayConfig.class, ApplyMode.LIVE, 1);
        configService.register("generators", "generators.yml", GeneratorConfig.class, ApplyMode.NEXT_GAME, 2);
        configService.addMigration("generators", 1, old -> GeneratorConfig.fromVersion1(old, configService.content("timeline.yml")));
        configService.register("timeline", "timeline.yml", TimelineConfig.class, ApplyMode.NEXT_GAME, 2);
        configService.addMigration("timeline", 1, TimelineConfig::fromVersion1);
        configService.register("items", "items.yml", ItemConfig.class, ApplyMode.LIVE, 1);
        configService.registerTemplate("upgrades", "upgrades.yml", UpgradeConfig.class, ApplyMode.NEXT_GAME, 2, "upgrades");
        configService.addMigration("upgrades", 1, old -> UpgradeConfig.fromVersion1(old, ConfigService.bundled("upgrades.yml")));
        configService.register("messages", "messages.yml", MessageConfig.class, ApplyMode.LIVE, 1);
        configService.registerTemplate("shop/menu", "shop/menu.yml", ShopMenuConfig.class, ApplyMode.LIVE, 1, "shop/menu");
        configService.registerTemplate("shop/tiers", "shop/tiers.yml", TierChains.class, ApplyMode.NEXT_GAME, 1, "shop/tiers");
        configService.registerDirectory("shop/categories", ShopCategoryConfig.class, ApplyMode.LIVE, 1, "shop/category",
                "blocks.yml", "melee.yml", "armor.yml", "tools.yml", "ranged.yml", "potions.yml", "utility.yml", "convert.yml");
        configService.addValidator(new ShopValidator(ItemSpecs.NAMES));
        configService.addValidator(new UpgradeValidator(ItemSpecs.NAMES));
        configService.register(Devtools.DOCUMENT_ID, "devtools.yml", DevtoolsConfig.class, ApplyMode.LIVE, 1);

        LegacyConfigMigrator.migrateIfPresent(plugin.getDataFolder(), configService);
        logProblems(configService.loadAll());
    }

    public static void logProblems(List<ConfigProblem> problems) {
        int errors = 0;
        for (ConfigProblem problem : problems) {
            if (problem.isError()) {
                errors++;
                LoggerUtil.error("Config " + problem);
            } else {
                LoggerUtil.warn("Config " + problem);
            }
        }
        if (!problems.isEmpty()) {
            LoggerUtil.warn("Config check found " + errors + " errors and " + (problems.size() - errors)
                    + " warnings, values with errors use their defaults. Run /bw config check after fixing them");
        }
    }

    public SettingsConfig getSettingsConfig() {
        return configService.get(SettingsConfig.class);
    }

    public DatabaseConfig getDatabaseConfig() {
        return configService.get(DatabaseConfig.class);
    }

    public GameplayConfig getGameplayConfig() {
        return configService.get(GameplayConfig.class);
    }

    public DisplayConfig getDisplayConfig() {
        return configService.get(DisplayConfig.class);
    }

    public GeneratorConfig getGeneratorConfig() {
        return configService.get(GeneratorConfig.class);
    }

    public TimelineConfig getTimelineConfig() {
        return configService.get(TimelineConfig.class);
    }

    public ItemConfig getItemConfig() {
        return configService.get(ItemConfig.class);
    }

    public UpgradeConfig getUpgradeConfig() {
        return configService.get(UpgradeConfig.class);
    }

    public MessageConfig getMessageConfig() {
        return configService.get(MessageConfig.class);
    }

    public DevtoolsConfig getDevtoolsConfig() {
        return configService.get(DevtoolsConfig.class);
    }

    /**
     * bStats class load
     */
    public void loadBStats() {
        int pluginId = 26773;
        Metrics metrics = new Metrics(plugin, pluginId);
    }
}
