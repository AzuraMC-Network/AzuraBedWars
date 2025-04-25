package cc.azuramc.bedwars.jedis;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.jedis.data.ServerData;
import cc.azuramc.bedwars.jedis.data.ServerType;
import cc.azuramc.bedwars.jedis.listener.NameListener;
import cc.azuramc.bedwars.jedis.listener.ServerListener;
import cc.azuramc.bedwars.jedis.util.IPUtil;
import cc.azuramc.bedwars.jedis.util.JedisUtil;
import cc.azuramc.bedwars.jedis.util.JsonUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

/**
 * Jedis管理器
 * 负责管理服务器状态、Redis通信和服务器生命周期
 */
public class JedisManager {
    // 常量定义
    private static final long STARTUP_TIMEOUT = 300000L; // 5分钟
    private static final long EMPTY_SERVER_TIMEOUT = 300000L; // 5分钟
    private static final long TASK_INTERVAL = 1000L; // 1秒
    private static final String SERVER_MANAGER_LOG = "/data/serverManager.log";
    private static final String GAME_SERVER_MANAGER_CHANNEL = "GameServerManager";

    @Getter
    private static JedisManager instance;
    
    private final Timer timer;
    @Getter
    private ServerData serverData;
    @Getter
    private final HashMap<String, Object> expand = new HashMap<>();
    
    private final long startTime = System.currentTimeMillis();
    private long forceBOOMTime = 0L;
    private final File serverManagerFile;

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public JedisManager(AzuraBedWars plugin) {
        instance = this;
        timer = new Timer();
        serverManagerFile = new File(SERVER_MANAGER_LOG);

        initializeServerData();
        registerListeners(plugin);
        startStatusUpdateTask();
    }

    /**
     * 初始化服务器数据
     */
    private void initializeServerData() {
        serverData = new ServerData();
        serverData.setServerType(ServerType.STARTUP);
        serverData.setIp(IPUtil.getLocalIp());
    }

    /**
     * 注册事件监听器
     * @param plugin 插件实例
     */
    private void registerListeners(AzuraBedWars plugin) {
        plugin.getServer().getPluginManager().registerEvents(new ServerListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new NameListener(), plugin);
    }

    /**
     * 启动状态更新任务
     */
    private void startStatusUpdateTask() {
        timer.schedule(new TimerTask() {
            @SneakyThrows
            @Override
            public void run() {
                if (serverData.getGameType() == null) {
                    return;
                }

                handleServerStatus();
                publishServerStatus();
                handleServerManagerFile();
            }
        }, TASK_INTERVAL, TASK_INTERVAL);
    }

    /**
     * 处理服务器状态
     */
    private void handleServerStatus() {
        switch (serverData.getServerType()) {
            case STARTUP:
                handleStartupStatus();
                break;
            case RUNNING:
            case END:
                handleRunningStatus();
                break;
        }
    }

    /**
     * 处理启动状态
     */
    private void handleStartupStatus() {
        if (System.currentTimeMillis() - startTime > STARTUP_TIMEOUT) {
            if (serverManagerFile.exists()) {
                boolean deleted = serverManagerFile.delete();
                if (!deleted) {
                    Bukkit.getLogger().severe("无法删除服务器管理文件：" + serverManagerFile.getAbsolutePath());
                }
                serverData.setServerType(ServerType.END);
            }
        }
    }

    /**
     * 处理运行状态
     */
    private void handleRunningStatus() {
        if (serverData.getPlayers() == 0) {
            if (forceBOOMTime == 0) {
                forceBOOMTime = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - forceBOOMTime > EMPTY_SERVER_TIMEOUT) {
                if (serverManagerFile.exists()) {
                    boolean deleted = serverManagerFile.delete();
                    if (!deleted) {
                        Bukkit.getLogger().severe("无法删除服务器管理文件：" + serverManagerFile.getAbsolutePath());
                    }
                    serverData.setServerType(ServerType.END);
                }
            }
        }
    }

    /**
     * 发布服务器状态
     */
    private void publishServerStatus() {
        try {
            JedisUtil.publish(GAME_SERVER_MANAGER_CHANNEL, JsonUtil.getDynamicString(serverData, expand));
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "发布服务器状态失败", e);
        }
    }

    /**
     * 处理服务器管理文件
     */
    private void handleServerManagerFile() {
        try {
            if (serverData.getName() != null 
                && serverData.getServerType() == ServerType.WAITING 
                && !serverManagerFile.exists()) {
                boolean created = serverManagerFile.createNewFile();
                if (!created) {
                    Bukkit.getLogger().severe("无法创建服务器管理文件：" + serverManagerFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "处理服务器管理文件失败", e);
        }
    }

    /**
     * 关闭管理器
     */
    public void shutdown() {
        timer.cancel();
    }
}
