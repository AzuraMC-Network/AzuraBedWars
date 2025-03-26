package cc.azuramc.bedwars;

import cc.azuramc.bedwars.listeners.*;
import cc.azuramc.bedwars.commands.AdminCommand;
import cc.azuramc.bedwars.commands.StartCommand;
import cc.azuramc.bedwars.database.map.MapData;
import cc.azuramc.bedwars.database.map.MapDataSQL;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.scoreboards.GameBoard;
import cc.azuramc.bedwars.scoreboards.LobbyBoard;
import cc.azuramc.bedwars.specials.SpecialItem;
import cc.azuramc.bedwars.database.mysql.ConnectionPoolHandler;
import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.event.Event;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

@Getter
public final class AzuraBedWars extends JavaPlugin {
    public static HashMap<Integer, Integer> playerLevel = new HashMap<>();
    @Getter
    private static AzuraBedWars instance;
    @Getter
    private Game game;
    @Getter
    @Setter
    private MapData mapData;
    @Getter
    private Economy econ = null;
    @Getter
    private Chat chat = null;
    @Getter
    private ConnectionPoolHandler connectionPoolHandler;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        long time = System.currentTimeMillis();
        
        // 初始化连接池
        connectionPoolHandler = new ConnectionPoolHandler();
        
        // 注册数据库
        getConnectionPoolHandler().registerDatabase("bwdata");
        getConnectionPoolHandler().registerDatabase("bwstats");
        
        // 初始化数据库表
        initDatabase();
        
        // 初始化游戏实例
        game = new Game(this, null);
        
        // 设置经济系统
        setupEconomy();
        setupChat();
        
        // 注册事件监听器
        Bukkit.getPluginManager().registerEvents(new JoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new QuitListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
        Bukkit.getPluginManager().registerEvents(new ReSpawnListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new DamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new BlockListener(instance), this);
        Bukkit.getPluginManager().registerEvents(new ServerListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChunkListener(), this);
        Bukkit.getPluginCommand("start").setExecutor(new StartCommand());
        Bukkit.getPluginManager().registerEvents(new LobbyBoard(game), this);
        Bukkit.getPluginManager().registerEvents(new GameBoard(game), this);
        
        // 加载特殊物品和等级
        SpecialItem.loadSpecials();
        loadLevel();
        
        // 设置世界属性
        Bukkit.getWorlds().forEach(world -> {
            world.setAutoSave(false);
            world.setDifficulty(Difficulty.NORMAL);
        });
        
        // 注册命令
        Bukkit.getPluginCommand("game").setExecutor(new AdminCommand());
        
        Bukkit.getConsoleSender().sendMessage("[AzuraBedWars] 加载完成耗时 " + (System.currentTimeMillis() - time) + " ms");
    }
    
    private void initDatabase() {
        try {
            // 使用bwdata数据库执行初始化
            try (Connection connection = connectionPoolHandler.getConnection("bwdata")) {
                if (connection == null) {
                    getLogger().severe("无法连接到bwdata数据库！");
                    return;
                }
                
                // 创建数据库
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("CREATE DATABASE IF NOT EXISTS bwdata");
                    stmt.execute("CREATE DATABASE IF NOT EXISTS bwstats");
                }
                
                // 创建表
                try (Statement stmt = connection.createStatement()) {
                    // bwdata数据库的表
                    stmt.execute("USE bwdata");
                    stmt.execute("CREATE TABLE IF NOT EXISTS BWMaps (" +
                            "MapName VARCHAR(36) PRIMARY KEY," +
                            "URL TEXT NOT NULL," +
                            "Data TEXT NOT NULL" +
                            ")");
                    stmt.execute("CREATE TABLE IF NOT EXISTS BWConfig (" +
                            "configKey VARCHAR(36) PRIMARY KEY," +
                            "object TEXT NOT NULL" +
                            ")");
                    
                    // bwstats数据库的表
                    stmt.execute("USE bwstats");
                    stmt.execute("CREATE TABLE IF NOT EXISTS bw_stats_players (" +
                            "Name VARCHAR(36) PRIMARY KEY," +
                            "Mode VARCHAR(20) NOT NULL," +
                            "kills INT DEFAULT 0," +
                            "deaths INT DEFAULT 0," +
                            "destroyedBeds INT DEFAULT 0," +
                            "wins INT DEFAULT 0," +
                            "loses INT DEFAULT 0," +
                            "games INT DEFAULT 0" +
                            ")");
                    stmt.execute("CREATE TABLE IF NOT EXISTS bw_shop_players (" +
                            "Name VARCHAR(36) PRIMARY KEY," +
                            "data TEXT NOT NULL" +
                            ")");
                    stmt.execute("CREATE TABLE IF NOT EXISTS bw_spectator_settings (" +
                            "Name VARCHAR(36) PRIMARY KEY," +
                            "speed INT DEFAULT 0," +
                            "autoTp BOOLEAN DEFAULT FALSE," +
                            "nightVision BOOLEAN DEFAULT FALSE," +
                            "firstPerson BOOLEAN DEFAULT TRUE," +
                            "hideOther BOOLEAN DEFAULT FALSE," +
                            "fly BOOLEAN DEFAULT FALSE" +
                            ")");
                }
            }
            
            getLogger().info("数据库初始化完成");
            
        } catch (Exception e) {
            getLogger().severe("初始化数据库失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    public void mainThreadRunnable(Runnable runnable) {
        Bukkit.getScheduler().runTask(this, runnable);
    }

    private void loadLevel() {
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

    public int getLevel(int level) {
        int playerLevel = 0;
        for (Map.Entry<Integer, Integer> entry : AzuraBedWars.playerLevel.entrySet()) {
            if (level > entry.getValue()) {
                playerLevel = entry.getKey();
            } else break;
        }
        return playerLevel;
    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            econ = rsp.getProvider();
        }
    }

    private void setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp != null) {
            chat = rsp.getProvider();
        }
    }

    public void callEvent(Event event) {
        getServer().getPluginManager().callEvent(event);
    }

}
