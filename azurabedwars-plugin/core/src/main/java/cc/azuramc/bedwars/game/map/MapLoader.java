package cc.azuramc.bedwars.game.map;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.util.LoggerUtil;
import lombok.Getter;

/**
 * 地图加载管理器
 * 负责处理地图加载的整个流程，包括加载状态管理、错误处理和日志记录
 *
 * @author an5w1r@163.com
 */
@Getter
public class MapLoader {
    private static final String LOG_PREFIX = "[MapLoader] ";

    private final AzuraBedWars plugin;
    private final GameManager gameManager;
    private final MapManager mapManager;

    private final boolean isMapLoading = false;
    private final String currentLoadingMap = null;

    private static final long DEFAULT_WAIT_TIME = 60;

    public MapLoader(AzuraBedWars plugin) {
        this.plugin = plugin;
        this.gameManager = plugin.getGameManager();
        this.mapManager = plugin.getMapManager();
    }

    /**
     * 加载指定地图
     */
    public void loadMap() {
        String defaultMapName = plugin.getSettingsConfig().getDefaultMapName();
        if (defaultMapName != null && !defaultMapName.isEmpty()) {
            plugin.setMapData(mapManager.loadMapAndWorld(defaultMapName));
            if (plugin.getMapData() != null) {
                LoggerUtil.info(LOG_PREFIX + "默认地图加载成功");
                return;
            }
        }

        // 如果依然没有，尝试加载任意已加载地图
        if (!plugin.getMapManager().getLoadedMaps().isEmpty()) {
            String anyMapName = plugin.getMapManager().getLoadedMaps().keySet().iterator().next();
            plugin.setMapData(mapManager.loadMapAndWorld(anyMapName));
            if (plugin.getMapData() != null) {
                LoggerUtil.info(LOG_PREFIX + "由于未设置地图 自动选择已加载地图: " + anyMapName);
                return;
            }
        }

        LoggerUtil.info(LOG_PREFIX + "所有地图加载尝试均失败，请先打开editorMode为服务端设置地图");
    }


    /**
     * 尝试加载默认地图
     */
    public void loadDefaultMap() {

    }
}
