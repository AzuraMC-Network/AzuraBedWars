package cc.azuramc.bedwars;

import cc.azuramc.bedwars.command.CommandRegistry;
import cc.azuramc.bedwars.config.ConfigFactory;
import cc.azuramc.bedwars.config.ConfigManager;
import cc.azuramc.bedwars.config.object.*;
import cc.azuramc.bedwars.database.provider.DatabaseProviderFactory;
import cc.azuramc.bedwars.database.storage.MapStorageFactory;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.item.special.AbstractSpecialItem;
import cc.azuramc.bedwars.game.level.PlayerLevelManager;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.game.map.MapLoader;
import cc.azuramc.bedwars.game.map.MapManager;
import cc.azuramc.bedwars.gui.base.listener.GUIListener;
import cc.azuramc.bedwars.jedis.JedisManager;
import cc.azuramc.bedwars.jedis.listener.PubSubListener;
import cc.azuramc.bedwars.listener.ListenerRegistry;
import cc.azuramc.bedwars.listener.setup.SetupItemListener;
import cc.azuramc.bedwars.nms.NMSAccess;
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

/**
 * AzuraBedWars插件主类
 * 负责插件的初始化、配置加载、事件注册等核心功能
 *
 * @author an5w1r@163.com
 */
@Getter
public final class AzuraBedWars extends JavaPlugin {
    private static final String PLUGIN_PREFIX = "[AzuraBedWars] ";

    @Getter
    private static AzuraBedWars instance;

    private Chat chat = null;
    private ConfigManager configManager;
    private String databaseName;
    private DatabaseProviderFactory databaseProviderFactory;
    private Economy econ = null;
    private EventSettingsConfig eventSettingsConfig;
    private GameManager gameManager;
    private ItemConfig itemConfig;
    private JedisManager jedisManager;
    private LuckPerms luckPermsApi;
    @Setter
    private MapData mapData;
    private MapLoader mapLoader;
    private MapManager mapManager;
    private MessageConfig messageConfig;
    private NMSAccess nmsAccess;
    private NMSProvider nmsProvider;
    private PlayerConfig playerConfig;
    private PubSubListener pubSubListener;
    private ResourceSpawnConfig resourceSpawnConfig;
    private ScoreboardManager scoreboardManager;
    private SettingsConfig settingsConfig;
    private SetupItemManager setupItemManager;
    private TeamUpgradeConfig teamUpgradeConfig;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
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

        if (settingsConfig.isEnabledJedisMapFeature()) {
            intiChannelSystem();
        }

        // 根据配置决定加载游戏模式还是编辑模式
        if (settingsConfig.isEditorMode() || mapManager.getLoadedMaps().isEmpty()) {
            getLogger().info("当前处于编辑模式(editorMode)或未发现可用地图 取消游戏相关特性加载");
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

        if (gameManager != null && gameManager.getGameEventManager() != null) {
            gameManager.getGameEventManager().stop();
        }

        // 保存配置
        if (configManager != null) {
            configManager.saveAll();
        }

        if (pubSubListener != null) {
            pubSubListener.poison();
        }

        if (jedisManager != null) {
            jedisManager.shutdown();
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
        databaseName = settingsConfig.getDatabase().getDatabase();
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
     * 初始化通信频道
     */
    private void intiChannelSystem() {
        jedisManager = new JedisManager(this);
        pubSubListener = new PubSubListener();

        pubSubListener.run();

        // 主线程Task
        getServer().getScheduler().runTask(this, pubSubListener);

        JedisManager.getInstance().getServerData().setGameType("AzuraBedWars");
        JedisManager.getInstance().getExpand().put("ver", getDescription().getVersion());
    }

    /**
     * 初始化编辑模式下的功能
     */
    private void initEditorFeatures() {
        // 初始化地图编辑工具
        setupItemManager = new SetupItemManager();

        new SetupItemListener(this);

        getLogger().info("地图编辑工具已加载");
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
        scoreboardManager.initialize(this);
    }

    /**
     * 注册BungeeCord通信频道
     */
    private void registerBungeeChannel() {
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
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
        Bukkit.getScheduler().runTask(this, runnable);
    }

    private void setupNMSSupport() {
        nmsProvider = new NMSProvider();
        nmsAccess = nmsProvider.setup();
    }

    private void hookLuckPerms() {
        if (getServer().getPluginManager().getPlugin("LuckPerms") != null) {
            luckPermsApi = LuckPermsProvider.get();
        }
    }

    private void hookVault() {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            setupEconomy();
            setupChat();
        }
    }

    /**
     * 设置经济系统
     */
    private void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            econ = rsp.getProvider();
        }
    }

    /**
     * 设置聊天系统
     */
    private void setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
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
        getServer().getPluginManager().callEvent(event);
    }

    /**
     * 初始化地图存储系统
     */
    private void initMapStorage() {

        // 初始化默认存储
        MapStorageFactory.getDefaultStorage();

        String storageType = settingsConfig.getMapStorage();
        getLogger().info("地图存储系统已初始化，使用 " + storageType + " 作为默认存储方式");
    }

    /**
     * 初始化配置系统
     */
    private void initConfigSystem() {
        // 创建配置管理器
        configManager = new ConfigManager(this);

        // 创建配置工厂
        ConfigFactory configFactory = new ConfigFactory();

        // 注册配置对象供应商
        configFactory.registerSupplier("settings", SettingsConfig::new);
        configFactory.registerSupplier("eventSettings", EventSettingsConfig::new);
        configFactory.registerSupplier("tasks", ResourceSpawnConfig::new);
        configFactory.registerSupplier("message", MessageConfig::new);
        configFactory.registerSupplier("items", ItemConfig::new);
        configFactory.registerSupplier("player", PlayerConfig::new);
        configFactory.registerSupplier("teamUpgrade", TeamUpgradeConfig::new);

        // 初始化默认配置
        configFactory.initializeDefaults(configManager);

        // 获取配置对象
        settingsConfig = configManager.getConfig("settings", SettingsConfig.class);
        eventSettingsConfig = configManager.getConfig("eventSettings", EventSettingsConfig.class);
        resourceSpawnConfig = configManager.getConfig("tasks", ResourceSpawnConfig.class);
        messageConfig = configManager.getConfig("message", MessageConfig.class);
        itemConfig = configManager.getConfig("items", ItemConfig.class);
        playerConfig = configManager.getConfig("player", PlayerConfig.class);
        teamUpgradeConfig = configManager.getConfig("teamUpgrade", TeamUpgradeConfig.class);

        // 保存配置
        configManager.saveAll();
    }

    /**
     * bStats class load
     */
    public void loadBStats() {
        int pluginId = 26773;
        Metrics metrics = new Metrics(this, pluginId);
    }
}
