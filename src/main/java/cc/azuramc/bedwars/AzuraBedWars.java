package cc.azuramc.bedwars;

import cc.azuramc.bedwars.command.CommandRegistry;
import cc.azuramc.bedwars.config.ConfigFactory;
import cc.azuramc.bedwars.config.ConfigManager;
import cc.azuramc.bedwars.config.object.*;
import cc.azuramc.bedwars.database.connection.ORMHander;
import cc.azuramc.bedwars.database.dao.PlayerDataDao;
import cc.azuramc.bedwars.database.service.PlayerDataService;
import cc.azuramc.bedwars.database.storage.MapStorageFactory;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.item.special.AbstractSpecialItem;
import cc.azuramc.bedwars.game.level.PlayerLevelMap;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.game.map.MapLoader;
import cc.azuramc.bedwars.game.map.MapManager;
import cc.azuramc.bedwars.gui.base.listener.GUIListener;
import cc.azuramc.bedwars.jedis.JedisManager;
import cc.azuramc.bedwars.jedis.listener.PubSubListener;
import cc.azuramc.bedwars.listener.ListenerRegistry;
import cc.azuramc.bedwars.listener.setup.SetupItemListener;
import cc.azuramc.bedwars.scoreboard.ScoreboardManager;
import cc.azuramc.bedwars.util.SetupItemManager;
import cc.azuramc.orm.AzuraORM;
import cc.azuramc.orm.AzuraOrmClient;
import cc.azuramc.orm.config.DatabaseConfig;
import lombok.Getter;
import lombok.Setter;
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

    @Getter private static AzuraBedWars instance;
    @Getter private GameManager gameManager;
    @Getter private MapManager mapManager;
    @Getter @Setter private MapData mapData;
    @Getter private Economy econ = null;
    @Getter private Chat chat = null;
    @Getter private ORMHander ormHander = null;
    @Getter private ConfigManager configManager;
    @Getter private SettingsConfig settingsConfig;
    @Getter private EventConfig eventConfig;
    @Getter private TaskConfig taskConfig;
    @Getter private MessageConfig messageConfig;
    @Getter private ItemConfig itemConfig;
    @Getter private PlayerConfig playerConfig;
    @Getter private ChatConfig chatConfig;
    @Getter private ScoreboardConfig scoreboardConfig;
    @Getter private JedisManager jedisManager;
    @Getter private PubSubListener pubSubListener;
    @Getter private MapLoader mapLoader;
    @Getter private ScoreboardManager scoreboardManager;
    @Getter private SetupItemManager setupItemManager;
    @Getter private String databaseName;
    @Getter private AzuraOrmClient ormClient;
    @Getter private PlayerDataDao playerDataDao;
    @Getter private PlayerDataService playerDataService;

    public static final String MAP_TABLE_NAME = "bw_map";

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        instance = this;

        // 初始化配置系统
        initConfigSystem();

        // 初始化基础服务
        initDatabases();
        initMapSystem();
        
        // 初始化命令和通信系统
        initCommands();
        intiChannelSystem();
        
        // 根据配置决定加载游戏模式还是编辑模式
        if (settingsConfig.isEditorMode()) {
            getLogger().info("当前处于编辑模式(editorMode) 取消游戏相关特性加载");
            initEditorFeatures();
        } else {
            initGameFeatures();
        }

        Bukkit.getConsoleSender().sendMessage(PLUGIN_PREFIX + "加载完成耗时 " + (System.currentTimeMillis() - startTime) + " ms");
    }

    /**
     * 初始化数据库连接
     */
    private void initDatabases() {
        databaseName = settingsConfig.getDatabase().getDatabase();
        SettingsConfig.DatabaseConfig database = settingsConfig.getDatabase();
        DatabaseConfig config = new DatabaseConfig()
                .setUrl("jdbc:mysql://" + database.getHost() + ":"
                        + database.getPort() + "/" + database.getDatabase())
                .setUsername(database.getUsername())
                .setPassword(database.getPassword())
                .setMaximumPoolSize(25)
                .setMinimumIdle(5)
                .setConnectionTimeout(10000L)
                .setIdleTimeout(300000L)
                .setMaxLifetime(900000L)
                .setLeakDetectionThreshold(30000L)
                .setPoolName("AzuraBedWars-Pool")
                .setRegisterMbeans(true)
                .setAutoCommit(true);

        AzuraORM.initialize(config, true);
        ormClient = AzuraORM.getClient();
        playerDataDao = new PlayerDataDao(this);
        playerDataService = new PlayerDataService(this);
        ormHander = new ORMHander(this);
    }

    /**
     * 初始化地图系统
     */
    private void initMapSystem() {
        mapManager = new MapManager();
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
        
        // 设置为异步任务
        getServer().getScheduler().runTaskAsynchronously(this, pubSubListener);
        
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
        Bukkit.getLogger().info("开始加载游戏相关特性...");
        // 注册GUI监听器
        new GUIListener(this);

        // 设置经济和聊天系统
        setupEconomy();
        setupChat();

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

        // 加载特殊物品和玩家等级
        AbstractSpecialItem.loadSpecials();
        PlayerLevelMap.loadLevelData();

        // 配置世界设置
        configureWorlds();
        Bukkit.getLogger().info("游戏相关特性加载完成");
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
     * 配置游戏世界设置
     */
    private void configureWorlds() {
        Bukkit.getWorlds().forEach(world -> {
            world.setAutoSave(false);
            world.setDifficulty(Difficulty.NORMAL);
        });
    }

    @Override
    public void onDisable() {
        if (ormHander != null) {
            ormHander.shutdown();
        }
        
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
    }

    /**
     * 在主线程上执行任务
     * 
     * @param runnable 要执行的任务
     */
    public void mainThreadRunnable(Runnable runnable) {
        Bukkit.getScheduler().runTask(this, runnable);
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
        configFactory.registerSupplier("events", EventConfig::new);
        configFactory.registerSupplier("tasks", TaskConfig::new);
        configFactory.registerSupplier("messages", MessageConfig::new);
        configFactory.registerSupplier("items", ItemConfig::new);
        configFactory.registerSupplier("player", PlayerConfig::new);
        configFactory.registerSupplier("chat", ChatConfig::new);
        configFactory.registerSupplier("scoreboard", ScoreboardConfig::new);

        // 初始化默认配置
        configFactory.initializeDefaults(configManager);
        
        // 获取配置对象
        settingsConfig = configManager.getConfig("settings", SettingsConfig.class);
        eventConfig = configManager.getConfig("events", EventConfig.class);
        taskConfig = configManager.getConfig("tasks", TaskConfig.class);
        messageConfig = configManager.getConfig("messages", MessageConfig.class);
        itemConfig = configManager.getConfig("items", ItemConfig.class);
        playerConfig = configManager.getConfig("player", PlayerConfig.class);
        chatConfig = configManager.getConfig("chat", ChatConfig.class);
        scoreboardConfig = configManager.getConfig("scoreboard", ScoreboardConfig.class);

        // 保存配置
        configManager.saveAll();
    }
}
