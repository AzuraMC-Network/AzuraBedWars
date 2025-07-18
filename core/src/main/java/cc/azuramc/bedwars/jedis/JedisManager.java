package cc.azuramc.bedwars.jedis;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.jedis.data.ServerData;
import cc.azuramc.bedwars.jedis.data.ServerType;
import cc.azuramc.bedwars.jedis.listener.NameListener;
import cc.azuramc.bedwars.jedis.listener.ServerListener;
import cc.azuramc.bedwars.jedis.util.IPUtil;
import cc.azuramc.bedwars.jedis.util.JedisUtil;
import cc.azuramc.bedwars.jedis.util.JsonUtil;
import cc.azuramc.bedwars.util.LoggerUtil;
import lombok.Getter;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.*;

/**
 * Jedis管理器
 * 负责管理服务器状态、Redis通信和服务器生命周期
 *
 * @author an5w1r@163.com
 */
public class JedisManager {
    /** 5分钟 */
    private static final long STARTUP_TIMEOUT = 300000L;
    /** 5分钟 */
    private static final long EMPTY_SERVER_TIMEOUT = 300000L;
    /** 1秒 */
    private static final long TASK_INTERVAL = 1000L;
    private static final String SERVER_MANAGER_LOG = "/data/serverManager.log";
    private static final String GAME_SERVER_MANAGER_CHANNEL = "GameServerManager";

    private static final int CORE_POOL_SIZE = 1;
    private static final int MAX_POOL_SIZE = 1;
    private static final long KEEP_ALIVE_TIME = 0L;
    private static final int QUEUE_CAPACITY = 100;

    @Getter private static JedisManager instance;

    /**
     * 使用ThreadPoolExecutor替代Executors创建的ScheduledExecutorService
     */
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> statusUpdateTask;
    
    @Getter private ServerData serverData;
    @Getter private final HashMap<String, Object> expand = new HashMap<>();
    
    private final long startTime = System.currentTimeMillis();
    private long forceBoomTime = 0L;
    private final File serverManagerFile;

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public JedisManager(AzuraBedWars plugin) {
        instance = this;
        
        // 创建线程工厂
        ThreadFactory threadFactory = r -> {
            Thread thread = new Thread(r, "JedisManager-Thread");
            thread.setDaemon(true);
            return thread;
        };
        
        // 创建拒绝策略处理器
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();

        // 将ThreadPoolExecutor包装为ScheduledExecutorService
        this.scheduler = new ScheduledThreadPoolExecutor(
            CORE_POOL_SIZE,
            threadFactory,
            handler
        );
        
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
        // 使用scheduleAtFixedRate替代Timer.schedule
        statusUpdateTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                if (serverData.getGameType() == null) {
                    return;
                }

                handleServerStatus();
                publishServerStatus();
                handleServerManagerFile();
            } catch (Exception e) {
                // 捕获异常但不中断任务执行
                LoggerUtil.error("服务器状态更新任务执行失败");
            }
        }, 0, TASK_INTERVAL, TimeUnit.MILLISECONDS);
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
            case UNKNOWN:
            default:
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
                    LoggerUtil.error("无法删除服务器管理文件：" + serverManagerFile.getAbsolutePath());
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
            if (forceBoomTime == 0) {
                forceBoomTime = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - forceBoomTime > EMPTY_SERVER_TIMEOUT) {
                if (serverManagerFile.exists()) {
                    boolean deleted = serverManagerFile.delete();
                    if (!deleted) {
                        LoggerUtil.error("无法删除服务器管理文件：" + serverManagerFile.getAbsolutePath());
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
            LoggerUtil.error("发布服务器状态失败");
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
                    LoggerUtil.error("无法创建服务器管理文件：" + serverManagerFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("处理服务器管理文件失败");
        }
    }

    /**
     * 关闭管理器
     */
    public void shutdown() {
        // 取消任务
        if (statusUpdateTask != null) {
            statusUpdateTask.cancel(false);
        }
        
        // 关闭调度器
        if (scheduler != null) {
            try {
                scheduler.shutdown();
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
