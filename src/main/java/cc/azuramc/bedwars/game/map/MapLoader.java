package cc.azuramc.bedwars.game.map;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.jedis.JedisManager;
import cc.azuramc.bedwars.jedis.event.BukkitPubSubMessageEvent;
import cc.azuramc.bedwars.jedis.util.IPUtil;
import cc.azuramc.bedwars.jedis.util.JedisUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 地图加载管理器
 * 负责处理地图加载的整个流程，包括加载状态管理、错误处理和日志记录
 *
 * @author an5w1r@163.com
 */
public class MapLoader {
    private static final String LOG_PREFIX = "[MapLoader] ";
    private static final String MAP_CHANNEL = "AZURA.BW." + IPUtil.getLocalIp();

    private final AzuraBedWars plugin;
    private final GameManager gameManager;
    private final MapManager mapManager;
    private final JedisManager jedisManager;

    private boolean isMapLoading = false;
    @Getter private String currentLoadingMap = null;

    private static final long DEFAULT_WAIT_TIME = 60;

    public MapLoader(AzuraBedWars plugin) {
        this.plugin = plugin;
        this.gameManager = plugin.getGameManager();
        this.mapManager = plugin.getMapManager();
        this.jedisManager = plugin.getJedisManager();
    }

    /**
     * 加载指定地图
     */
    public void loadMap() {
        if (plugin.getSettingsConfig().isEnabledJedisMapFeature()) {
            CompletableFuture<String> mapLoaderFuture = new CompletableFuture<>();

            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onPubSubMessage(BukkitPubSubMessageEvent event) {
                    if (event.getChannel().equals(MAP_CHANNEL)) {
                        mapLoaderFuture.complete(event.getMessage());
                        Bukkit.getLogger().info(LOG_PREFIX + "开始加载地图: " + event.getMessage());
                        HandlerList.unregisterAll(this);
                    }
                }
            }, plugin);

            Bukkit.getLogger().info("正在通过Jedis请求地图");
            JedisUtil.publish(MAP_CHANNEL, "requestMap");

            String mapName = null;
            try {
                mapName = mapLoaderFuture.get(DEFAULT_WAIT_TIME, TimeUnit.SECONDS);
            } catch (Exception e) {
                Bukkit.getLogger().info("请求超时 加载备用方案");
            }

            if (mapName != null && !mapName.isEmpty()) {
                plugin.setMapData(mapManager.loadMapAndWorld(mapName));
                if (plugin.getMapData() != null) {
                    return;
                }
                Bukkit.getLogger().warning("Jedis地图加载失败 尝试加载默认地图");
            }
        }

        String defaultMapName = plugin.getSettingsConfig().getDefaultMapName();
        if (defaultMapName != null && !defaultMapName.isEmpty()) {
            plugin.setMapData(mapManager.loadMapAndWorld(defaultMapName));
            if (plugin.getMapData() != null) {
                Bukkit.getLogger().info(LOG_PREFIX + "默认地图加载成功");
                return;
            }
        }

        // 如果依然没有，尝试加载任意已加载地图
        if (!plugin.getMapManager().getLoadedMaps().isEmpty()) {
            String anyMapName = plugin.getMapManager().getLoadedMaps().keySet().iterator().next();
            plugin.setMapData(mapManager.loadMapAndWorld(anyMapName));
            if (plugin.getMapData() != null) {
                Bukkit.getLogger().info(LOG_PREFIX + "由于未设置地图 自动选择已加载地图: " + anyMapName);
                return;
            }
        }

        Bukkit.getLogger().info(LOG_PREFIX + "所有地图加载尝试均失败，请先打开editorMode为服务端设置地图");
    }


    /**
     * 尝试加载默认地图
     */
    public void loadDefaultMap() {

    }
} 