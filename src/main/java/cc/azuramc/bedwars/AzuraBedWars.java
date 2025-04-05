package cc.azuramc.bedwars;

import cc.azuramc.bedwars.commands.CommandHandler;
import cc.azuramc.bedwars.listeners.*;
import cc.azuramc.bedwars.map.data.MapData;
import cc.azuramc.bedwars.map.MapManager;
import cc.azuramc.bedwars.map.MapStorageFactory;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.scoreboards.GameBoard;
import cc.azuramc.bedwars.scoreboards.LobbyBoard;
import cc.azuramc.bedwars.specials.SpecialItem;
import cc.azuramc.bedwars.database.mysql.ConnectionPoolHandler;
import cc.azuramc.bedwars.utils.gui.GUIListener;
import lombok.Getter;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.event.Event;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * AzuraBedWars插件主类
 * <p>
 * 负责插件的初始化、配置加载、事件注册等核心功能
 * </p>
 */
@Getter
public final class AzuraBedWars extends JavaPlugin {
    // 数据库相关常量
    private static final String DB_PLAYER_DATA = "bwdata";
    private static final String DB_PLAYER_STATS = "bwstats";
    
    // 配置相关常量
    private static final String CONFIG_EDITOR_MODE = "editorMode";
    private static final String CONFIG_DEFAULT_MAP = "defaultMapName";
    private static final String CONFIG_MAP_STORAGE = "map.storage";
    private static final String CONFIG_DB_MAP_NAME = "database.maps.name";
    private static final String CONFIG_DB_MAP_TABLE = "database.maps.table";
    
    // 默认值常量
    private static final String DEFAULT_STORAGE_TYPE = "JSON";
    private static final String DEFAULT_DB_NAME = "bwdata";
    private static final String DEFAULT_TABLE_NAME = "BWMaps";
    
    // 插件标识常量
    private static final String PLUGIN_PREFIX = "[AzuraBedWars] ";

    /** 玩家等级经验值映射表 */
    private static final HashMap<Integer, Integer> playerLevel = new HashMap<>();
    
    /** 插件实例 */
    @Getter
    private static AzuraBedWars instance;
    
    /** 游戏实例 */
    @Getter
    private Game game;
    
    /** 地图管理器 */
    @Getter
    private MapManager mapManager;
    
    /** 当前地图数据 */
    @Getter
    private MapData mapData;
    
    /** 经济系统接口 */
    @Getter
    private Economy econ = null;
    
    /** 聊天系统接口 */
    @Getter
    private Chat chat = null;
    
    /** 数据库连接池管理器 */
    @Getter
    private ConnectionPoolHandler connectionPoolHandler;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        long startTime = System.currentTimeMillis();
        
        // 初始化基础服务
        initDatabases();
        initMapSystem();
        initCommands();
        
        // 不在编辑模式下初始化游戏功能
        if (!getConfig().getBoolean(CONFIG_EDITOR_MODE)) {
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
        new CommandHandler(this);
    }

    /**
     * 初始化游戏相关功能
     */
    private void initGameFeatures() {
        // 注册GUI监听器
        new GUIListener(this);

        // 设置经济和聊天系统
        setupEconomy();
        setupChat();

        // 初始化地图存储和加载默认地图
        initMapStorage();
        loadDefaultMap();

        // 创建并加载游戏实例
        game = new Game(this);
        game.loadGame(mapData);

        // 注册各种事件监听器
        registerEventListeners();

        // 加载特殊物品和玩家等级
        SpecialItem.loadSpecials();
        loadLevelData();

        // 配置世界设置
        configureWorlds();
    }

    /**
     * 加载默认地图
     */
    private void loadDefaultMap() {
        mapManager.preloadAllMaps();
        String defaultMapName = getConfig().getString(CONFIG_DEFAULT_MAP);
        
        if (defaultMapName != null) {
            mapData = mapManager.getAndLoadMapData(defaultMapName);
        } else if (!mapManager.getLoadedMaps().isEmpty()) {
            mapData = mapManager.getLoadedMaps().entrySet().iterator().next().getValue();
        }
        
        if (mapData == null) {
            Bukkit.getLogger().info("preloadMap 为空, 请先打开editorMode为服务端设置地图");
        }
    }

    /**
     * 注册所有事件监听器
     */
    private void registerEventListeners() {
        // 玩家监听器
        Bukkit.getPluginManager().registerEvents(new JoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new QuitListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
        Bukkit.getPluginManager().registerEvents(new RespawnListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new DamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new AFKListener(), this);
        Bukkit.getPluginManager().registerEvents(new EggBridgeListener(instance), this);

        // 游戏世界监听器
        Bukkit.getPluginManager().registerEvents(new BlockListener(instance), this);
        Bukkit.getPluginManager().registerEvents(new ServerListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChunkListener(), this);
        
        // 记分板监听器
        Bukkit.getPluginManager().registerEvents(new LobbyBoard(game), this);
        Bukkit.getPluginManager().registerEvents(new GameBoard(game), this);
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
        
        if (game != null && game.getEventManager() != null) {
            game.getEventManager().stop();
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
        playerLevel.clear();
        
        // 等级1-10
        playerLevel.put(1, 0);
        playerLevel.put(2, 10);
        playerLevel.put(3, 25);
        playerLevel.put(4, 45);
        playerLevel.put(5, 100);
        playerLevel.put(6, 220);
        playerLevel.put(7, 450);
        playerLevel.put(8, 800);
        playerLevel.put(9, 900);
        playerLevel.put(10, 1050);
        
        // 等级11-20
        playerLevel.put(11, 1800);
        playerLevel.put(12, 2600);
        playerLevel.put(13, 3450);
        playerLevel.put(14, 4200);
        playerLevel.put(15, 5450);
        playerLevel.put(16, 6150);
        playerLevel.put(17, 6850);
        playerLevel.put(18, 7550);
        playerLevel.put(19, 8250);
        playerLevel.put(20, 8900);
        
        // 等级21-30
        playerLevel.put(21, 10000);
        playerLevel.put(22, 11250);
        playerLevel.put(23, 12500);
        playerLevel.put(24, 13750);
        playerLevel.put(25, 15000);
        playerLevel.put(26, 16250);
        playerLevel.put(27, 17500);
        playerLevel.put(28, 18750);
        playerLevel.put(29, 20000);
        playerLevel.put(30, 22000);
        
        // 等级31-40
        playerLevel.put(31, 24000);
        playerLevel.put(32, 26000);
        playerLevel.put(33, 28000);
        playerLevel.put(34, 30000);
        playerLevel.put(35, 32000);
        playerLevel.put(36, 34000);
        playerLevel.put(37, 36000);
        playerLevel.put(38, 38000);
        playerLevel.put(39, 40000);
        playerLevel.put(40, 45000);
        
        // 等级41-50
        playerLevel.put(41, 50000);
        playerLevel.put(42, 55000);
        playerLevel.put(43, 60000);
        playerLevel.put(44, 65000);
        playerLevel.put(45, 70000);
        playerLevel.put(46, 75000);
        playerLevel.put(47, 80000);
        playerLevel.put(48, 85000);
        playerLevel.put(49, 90000);
        playerLevel.put(50, 100000);
    }

    /**
     * 根据经验值获取玩家等级
     * 
     * @param experience 玩家当前经验值
     * @return 玩家等级
     */
    public int getLevel(int experience) {
        int level = 1; // 默认最低等级为1
        
        for (Map.Entry<Integer, Integer> entry : playerLevel.entrySet()) {
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
        ensureConfigDefaults();
        
        // 确保数据库连接已注册
        String dbName = getConfig().getString(CONFIG_DB_MAP_NAME, DEFAULT_DB_NAME);
        connectionPoolHandler.registerDatabase(dbName);
        
        // 初始化默认存储
        MapStorageFactory.getDefaultStorage();
        
        String storageType = getConfig().getString(CONFIG_MAP_STORAGE, DEFAULT_STORAGE_TYPE);
        getLogger().info("地图存储系统已初始化，使用 " + storageType + " 作为默认存储方式");
    }
    
    /**
     * 确保配置文件中包含必要的默认设置
     */
    private void ensureConfigDefaults() {
        boolean configChanged = false;
        
        if (!getConfig().contains(CONFIG_MAP_STORAGE)) {
            getConfig().set(CONFIG_MAP_STORAGE, DEFAULT_STORAGE_TYPE);
            configChanged = true;
        }
        
        if (!getConfig().contains(CONFIG_DB_MAP_NAME)) {
            getConfig().set(CONFIG_DB_MAP_NAME, DEFAULT_DB_NAME);
            configChanged = true;
        }
        
        if (!getConfig().contains(CONFIG_DB_MAP_TABLE)) {
            getConfig().set(CONFIG_DB_MAP_TABLE, DEFAULT_TABLE_NAME);
            configChanged = true;
        }
        
        if (configChanged) {
            saveConfig();
        }
    }
}
