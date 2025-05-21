package cc.azuramc.bedwars;

import cc.azuramc.bedwars.command.CommandRegistry;
import cc.azuramc.bedwars.config.ConfigFactory;
import cc.azuramc.bedwars.config.ConfigManager;
import cc.azuramc.bedwars.config.object.*;
import cc.azuramc.bedwars.database.connection.ConnectionPoolHandler;
import cc.azuramc.bedwars.database.storage.MapStorageFactory;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.item.special.AbstractSpecialItem;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.game.map.MapLoadManager;
import cc.azuramc.bedwars.game.map.MapManager;
import cc.azuramc.bedwars.gui.base.listener.GUIListener;
import cc.azuramc.bedwars.jedis.JedisManager;
import cc.azuramc.bedwars.jedis.event.BukkitPubSubMessageEvent;
import cc.azuramc.bedwars.jedis.listener.PubSubListener;
import cc.azuramc.bedwars.jedis.util.IPUtil;
import cc.azuramc.bedwars.jedis.util.JedisUtil;
import cc.azuramc.bedwars.listener.ListenerRegistry;
import cc.azuramc.bedwars.listener.setup.SetupItemListener;
import cc.azuramc.bedwars.scoreboard.ScoreboardManager;
import cc.azuramc.bedwars.util.SetupItemManager;
import lombok.Getter;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * AzuraBedWars插件主类
 * 负责插件的初始化、配置加载、事件注册等核心功能
 *
 * @author an5w1r@163.com
 */
@Getter
public final class AzuraBedWars extends JavaPlugin {
    private static final String DB_PLAYER_DATA = "bwdata";
    private static final String DB_PLAYER_STATS = "bwstats";
    
    /** 插件标识常量 */
    private static final String PLUGIN_PREFIX = "[AzuraBedWars] ";

    /** 玩家等级经验值映射表 */
    private static final HashMap<Integer, Integer> PLAYER_LEVEL = new HashMap<>();

    @Getter private static AzuraBedWars instance;
    @Getter private GameManager gameManager;
    @Getter private MapManager mapManager;
    @Getter private MapData mapData;
    @Getter private Economy econ = null;
    @Getter private Chat chat = null;
    @Getter private ConnectionPoolHandler connectionPoolHandler;
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
    @Getter private MapLoadManager mapLoadManager;
    @Getter private ScoreboardManager scoreboardManager;
    @Getter private SetupItemManager setupItemManager;

    @Override
    public void onEnable() {
        instance = this;
        long startTime = System.currentTimeMillis();
        
        // 初始化基础服务
        initDatabases();
        initMapSystem();
        
        // 初始化地图加载管理器
        mapLoadManager = MapLoadManager.getInstance(this);
        
        // 初始化配置系统
        initConfigSystem();
        
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
        connectionPoolHandler = new ConnectionPoolHandler();
        connectionPoolHandler.registerDatabase(DB_PLAYER_DATA);
        connectionPoolHandler.registerDatabase(DB_PLAYER_STATS);
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
        getServer().getScheduler().runTaskAsynchronously(this, pubSubListener);
        JedisManager.getInstance().getServerData().setGameType("AzuraBedWars");
        JedisManager.getInstance().getExpand().put("ver", getDescription().getVersion());
        pubSubListener.addChannel("AZURA.BW." + IPUtil.getLocalIp());
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
        // 加载地图（Jedis请求+超时处理）
        loadMapWithJedisRequest();

        // 创建并加载游戏实例
        gameManager = new GameManager(this);
        gameManager.loadGame(mapData);

        // 注册各种事件监听器
        registerEventListeners();

        // 加载特殊物品和玩家等级
        AbstractSpecialItem.loadSpecials();
        loadLevelData();

        // 配置世界设置
        configureWorlds();
        Bukkit.getLogger().info("游戏相关特性加载完成");
    }

    /**
     * 通过Jedis请求地图，等待30秒，优先加载返回地图，超时则加载默认地图
     */
    private void loadMapWithJedisRequest() {
        mapManager.preloadAllMaps();
        String responseChannel = "AZURA.BW." + IPUtil.getLocalIp();
        CompletableFuture<String> mapFuture = new CompletableFuture<>();

        // 注册一次性监听器
        Listener listener = new Listener() {
            @EventHandler
            public void onMessage(BukkitPubSubMessageEvent event) {
                if (event.getChannel().equals(responseChannel)) {
                    mapFuture.complete(event.getMessage());
                    HandlerList.unregisterAll(this);
                }
            }
        };
        getServer().getPluginManager().registerEvents(listener, this);

        Bukkit.getLogger().info("正在通过Jedis请求地图");
        JedisUtil.publish(responseChannel, "requestMap");

        String mapName = null;
        try {
            // 最多等待30秒
            mapName = mapFuture.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            Bukkit.getLogger().info("地图请求超时，使用默认地图");
        }

        // 优先加载Jedis返回地图
        if (mapName != null && !mapName.isEmpty()) {
            mapData = mapManager.loadMapAndWorld(mapName);
            if (mapData != null) {
                return;
            }
            Bukkit.getLogger().warning("Jedis返回地图加载失败，尝试加载默认地图");
        }

        // 加载默认地图
        String defaultMapName = settingsConfig.getDatabaseMapName();

        if (defaultMapName != null && !defaultMapName.isEmpty()) {
            mapData = mapManager.loadMapAndWorld(defaultMapName);
            if (mapData != null) {
                return;
            }
        }
        // 如果依然没有，尝试加载任意已加载地图
        if (!mapManager.getLoadedMaps().isEmpty()) {
            mapData = mapManager.getLoadedMaps().entrySet().iterator().next().getValue();
            if (mapData != null) {
                return;
            }
        }
        if (mapData == null) {
            Bukkit.getLogger().info("preloadMap 为空, 请先打开editorMode为服务端设置地图");
        }
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
        if (connectionPoolHandler != null) {
            connectionPoolHandler.closeAll();
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
     * 加载玩家等级经验表
     */
    private void loadLevelData() {
        // 清理先前数据
        PLAYER_LEVEL.clear();
        
        // 等级1-10
        PLAYER_LEVEL.put(1, 0);
        PLAYER_LEVEL.put(2, 10);
        PLAYER_LEVEL.put(3, 25);
        PLAYER_LEVEL.put(4, 45);
        PLAYER_LEVEL.put(5, 100);
        PLAYER_LEVEL.put(6, 220);
        PLAYER_LEVEL.put(7, 450);
        PLAYER_LEVEL.put(8, 800);
        PLAYER_LEVEL.put(9, 900);
        PLAYER_LEVEL.put(10, 1050);
        
        // 等级11-20
        PLAYER_LEVEL.put(11, 1800);
        PLAYER_LEVEL.put(12, 2600);
        PLAYER_LEVEL.put(13, 3450);
        PLAYER_LEVEL.put(14, 4200);
        PLAYER_LEVEL.put(15, 5450);
        PLAYER_LEVEL.put(16, 6150);
        PLAYER_LEVEL.put(17, 6850);
        PLAYER_LEVEL.put(18, 7550);
        PLAYER_LEVEL.put(19, 8250);
        PLAYER_LEVEL.put(20, 8900);
        
        // 等级21-30
        PLAYER_LEVEL.put(21, 10000);
        PLAYER_LEVEL.put(22, 11250);
        PLAYER_LEVEL.put(23, 12500);
        PLAYER_LEVEL.put(24, 13750);
        PLAYER_LEVEL.put(25, 15000);
        PLAYER_LEVEL.put(26, 16250);
        PLAYER_LEVEL.put(27, 17500);
        PLAYER_LEVEL.put(28, 18750);
        PLAYER_LEVEL.put(29, 20000);
        PLAYER_LEVEL.put(30, 22000);
        
        // 等级31-40
        PLAYER_LEVEL.put(31, 24000);
        PLAYER_LEVEL.put(32, 26000);
        PLAYER_LEVEL.put(33, 28000);
        PLAYER_LEVEL.put(34, 30000);
        PLAYER_LEVEL.put(35, 32000);
        PLAYER_LEVEL.put(36, 34000);
        PLAYER_LEVEL.put(37, 36000);
        PLAYER_LEVEL.put(38, 38000);
        PLAYER_LEVEL.put(39, 40000);
        PLAYER_LEVEL.put(40, 45000);
        
        // 等级41-50
        PLAYER_LEVEL.put(41, 50000);
        PLAYER_LEVEL.put(42, 55000);
        PLAYER_LEVEL.put(43, 60000);
        PLAYER_LEVEL.put(44, 65000);
        PLAYER_LEVEL.put(45, 70000);
        PLAYER_LEVEL.put(46, 75000);
        PLAYER_LEVEL.put(47, 80000);
        PLAYER_LEVEL.put(48, 85000);
        PLAYER_LEVEL.put(49, 90000);
        PLAYER_LEVEL.put(50, 100000);
    }

    /**
     * 根据经验值获取玩家等级
     * 
     * @param experience 玩家当前经验值
     * @return 玩家等级
     */
    public int getLevel(int experience) {
        // 默认最低等级为1
        int level = 1;
        
        for (Map.Entry<Integer, Integer> entry : PLAYER_LEVEL.entrySet()) {
            if (experience >= entry.getValue()) {
                level = entry.getKey();
            } else {
                break;
            }
        }
        
        return level;
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
        // 确保数据库连接已注册
        String dbName = settingsConfig.getDatabaseMapName();
        connectionPoolHandler.registerDatabase(dbName);
        
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
