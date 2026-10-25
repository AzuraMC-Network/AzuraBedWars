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
import cc.azuramc.bedwars.database.provider.DatabaseProviderFactory;
import cc.azuramc.bedwars.database.storage.MapStorageFactory;
import cc.azuramc.bedwars.database.storage.MapStorageType;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.item.special.AbstractSpecialItem;
import cc.azuramc.bedwars.game.level.PlayerLevelManager;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.game.map.MapLoader;
import cc.azuramc.bedwars.game.map.MapManager;
import cc.azuramc.bedwars.gui.base.listener.GUIListener;
import cc.azuramc.bedwars.listener.ListenerRegistry;
import cc.azuramc.bedwars.listener.packet.InvisibilityPacketListener;
import cc.azuramc.bedwars.listener.setup.SetupItemListener;
import cc.azuramc.bedwars.nms.NMSProvider;
import cc.azuramc.bedwars.scoreboard.ScoreboardManager;
import cc.azuramc.bedwars.util.LoggerUtil;
import cc.azuramc.bedwars.util.SetupItemManager;
import cc.azuramc.bedwars.util.bstats.Metrics;
import cc.azuramc.bedwars.util.nms.NMSMapping;
import cc.azuramc.bedwars.util.nms.ReflectionUtil;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import lombok.Setter;
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
    private Economy econ = null;
    private GameManager gameManager;
    private LuckPerms luckPermsApi;
    @Setter
    private MapData mapData;
    private MapLoader mapLoader;
    private MapManager mapManager;
    private NMSAccess nmsAccess;
    private NMSProvider nmsProvider;
    private ScoreboardManager scoreboardManager;
    private SetupItemManager setupItemManager;

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

        // ReflectionUtil初始化版本
        ReflectionUtil.initializeVersions();
        NMSMapping.initNmsMapping();

        setupNMSSupport();

        // 初始化基础服务
        initDatabases();
        initMapSystem();

        // 根据配置决定加载游戏模式还是编辑模式
        if (getSettingsConfig().isEditorMode() || mapManager.getLoadedMaps().isEmpty()) {
            plugin.getLogger().info("当前处于编辑模式(editorMode)或未发现可用地图 取消游戏相关特性加载");
            initEditorFeatures();
        } else {
            initGameFeatures();
        }

        initCommands();
        // bStats
        loadBStats();

        Bukkit.getConsoleSender().sendMessage(PLUGIN_PREFIX + "加载完成耗时 " + (System.currentTimeMillis() - startTime) + " ms");
    }

    @Override
    public void onDisable() {

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
     * 初始化地图系统
     */
    private void initMapSystem() {
        mapManager = new MapManager();
        mapManager.preloadAllMaps();
    }

    /**
     * 初始化命令处理器
     */
    private void initCommands() {
        new CommandRegistry(this);
    }

    /**
     * 初始化编辑模式下的功能
     */
    private void initEditorFeatures() {
        // 初始化地图编辑工具
        setupItemManager = new SetupItemManager();

        new SetupItemListener(this);

        plugin.getLogger().info("地图编辑工具已加载");
    }

    /**
     * 初始化游戏相关功能
     */
    private void initGameFeatures() {
        LoggerUtil.info("开始加载游戏相关特性...");
        // 注册GUI监听器
        new GUIListener(this);

        // Hook Vault Chat and Econ
        hookVault();
        hookLuckPerms();

        // 初始化地图存储
        initMapStorage();

        // 初始化地图加载管理器
        mapLoader = new MapLoader(this);

        // 创建游戏管理器
        gameManager = new GameManager(this);

        // 加载地图
        mapLoader.loadMap();
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

    /**
     * 初始化地图存储系统
     */
    private void initMapStorage() {

        // 初始化默认存储
        MapStorageFactory.getDefaultStorage();

        MapStorageType storageType = getSettingsConfig().getMapStorage();
        plugin.getLogger().info("地图存储系统已初始化，使用 " + storageType + " 作为默认存储方式");
    }

    private void initConfigSystem() {
        configService = new ConfigService(plugin.getDataFolder(),
                () -> gameManager != null && gameManager.getGameState() != GameState.WAITING);
        configService.register("settings", "settings.yml", SettingsConfig.class, ApplyMode.LIVE, 1);
        configService.register("database", "database.yml", DatabaseConfig.class, ApplyMode.RESTART, 1);
        configService.register("gameplay", "gameplay.yml", GameplayConfig.class, ApplyMode.LIVE, 1);
        configService.register("display", "display.yml", DisplayConfig.class, ApplyMode.LIVE, 1);
        configService.register("generators", "generators.yml", GeneratorConfig.class, ApplyMode.NEXT_GAME, 1);
        configService.register("timeline", "timeline.yml", TimelineConfig.class, ApplyMode.NEXT_GAME, 1);
        configService.register("items", "items.yml", ItemConfig.class, ApplyMode.LIVE, 1);
        configService.register("upgrades", "upgrades.yml", UpgradeConfig.class, ApplyMode.LIVE, 1);
        configService.register("messages", "messages.yml", MessageConfig.class, ApplyMode.LIVE, 1);

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

    /**
     * bStats class load
     */
    public void loadBStats() {
        int pluginId = 26773;
        Metrics metrics = new Metrics(plugin, pluginId);
    }
}
