package cc.azuramc.bedwars.game.map;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * 地图加载管理器
 * 负责处理地图加载的整个流程，包括加载状态管理、错误处理和日志记录
 *
 * @author an5w1r@163.com
 */
public class MapLoadManager {
    private static final String LOG_PREFIX = "[MapLoad] ";
    private static MapLoadManager instance;
    
    private final AzuraBedWars plugin;
    private final GameManager gameManager;
    private final MapManager mapManager;
    
    private boolean isMapLoading = false;
    @Getter
    private String currentLoadingMap = null;

    private CompletableFuture<String> mapLoadRequestFuture;
    private static final long DEFAULT_WAIT_TIME = 30;
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;
    
    private MapLoadManager(AzuraBedWars plugin) {
        this.plugin = plugin;
        this.gameManager = plugin.getGameManager();
        this.mapManager = plugin.getMapManager();
        this.mapLoadRequestFuture = new CompletableFuture<>();
    }
    
    /**
     * 获取地图加载管理器实例
     * @param plugin 插件实例
     * @return 地图加载管理器实例
     */
    public static MapLoadManager getInstance(AzuraBedWars plugin) {
        if (instance == null) {
            instance = new MapLoadManager(plugin);
        }
        return instance;
    }
    
    /**
     * 加载指定地图
     * @param mapName 地图名称
     * @return 是否成功开始加载
     */
    public boolean loadMap(String mapName) {
        if (isMapLoading) {
            Bukkit.getLogger().warning(LOG_PREFIX + "地图正在加载中，忽略重复的加载请求: " + mapName);
            return false;
        }
        
        try {
            isMapLoading = true;
            currentLoadingMap = mapName;
            Bukkit.getLogger().info(LOG_PREFIX + "开始加载地图: " + mapName);
            
            // 在主线程中执行地图加载
            plugin.mainThreadRunnable(() -> {
                try {
                    // 加载地图数据
                    MapData mapData = mapManager.getAndLoadMapData(mapName);
                    if (mapData == null) {
                        Bukkit.getLogger().severe(LOG_PREFIX + "无法加载地图数据: " + mapName);
                        return;
                    }
                    
                    // 设置地图名称
                    mapData.setName(mapName);
                    
                    // 加载游戏
                    gameManager.loadGame(mapData);
                    Bukkit.getLogger().info(LOG_PREFIX + "地图加载成功: " + mapName);
                } catch (Exception e) {
                    Bukkit.getLogger().log(Level.SEVERE, LOG_PREFIX + "加载地图时发生错误: " + mapName, e);
                } finally {
                    isMapLoading = false;
                    currentLoadingMap = null;
                }
            });
            
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, LOG_PREFIX + "处理地图加载请求时发生错误", e);
            isMapLoading = false;
            currentLoadingMap = null;
            return false;
        }
    }
    
    /**
     * 等待地图加载请求，如果超时则使用默认地图
     * @param defaultMapName 默认地图名称
     * @param waitTime 等待时间
     * @param timeUnit 时间单位
     * @return 最终加载的地图名称
     */
    public String waitForMapLoadRequest(String defaultMapName, long waitTime, TimeUnit timeUnit) {
        Bukkit.getLogger().info(LOG_PREFIX + "等待地图加载请求，超时时间: " + waitTime + " " + timeUnit);
        
        try {
            // 等待地图加载请求
            String requestedMap = mapLoadRequestFuture.get(waitTime, timeUnit);
            Bukkit.getLogger().info(LOG_PREFIX + "收到地图加载请求: " + requestedMap);
            return requestedMap;
        } catch (Exception e) {
            Bukkit.getLogger().warning(LOG_PREFIX + "等待地图加载请求超时，使用默认地图: " + defaultMapName);
            return defaultMapName;
        }
    }
    
    /**
     * 使用默认等待时间等待地图加载请求
     * @param defaultMapName 默认地图名称
     * @return 最终加载的地图名称
     */
    public String waitForMapLoadRequest(String defaultMapName) {
        return waitForMapLoadRequest(defaultMapName, DEFAULT_WAIT_TIME, DEFAULT_TIME_UNIT);
    }
    
    /**
     * 提交地图加载请求
     * @param mapName 地图名称
     */
    public void submitMapLoadRequest(String mapName) {
        if (!mapLoadRequestFuture.isDone()) {
            mapLoadRequestFuture.complete(mapName);
        }
    }
    
    /**
     * 重置地图加载请求等待器
     */
    public void resetMapLoadRequest() {
        mapLoadRequestFuture = new CompletableFuture<>();
    }

    /**
     * 检查是否正在加载地图
     * @return 是否正在加载地图
     */
    public boolean isMapLoading() {
        return isMapLoading;
    }
} 