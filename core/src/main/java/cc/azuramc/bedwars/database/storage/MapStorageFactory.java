package cc.azuramc.bedwars.database.storage;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.provider.IDatabaseProvider;
import cc.azuramc.bedwars.database.provider.mysql.MySQLDatabaseProvider;
import cc.azuramc.bedwars.database.storage.provider.JsonMapStorage;
import cc.azuramc.bedwars.database.storage.provider.MySQLMapStorage;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.util.LoggerUtil;

import java.util.Collections;

/**
 * @author an5w1r@163.com
 */
public class MapStorageFactory {

    private static final String MAP_TABLE_NAME = "bw_map";

    private static JsonMapStorage jsonMapStorage;
    private static MySQLMapStorage mysqlMapStorage;

    /**
     * Falls back to JSON when MySQL is not the database
     */
    public static MapStorageType getDefaultType() {
        MapStorageType type = AzuraBedWars.getInstance().getSettingsConfig().getMapStorage();
        if (type == MapStorageType.MYSQL && !isMysqlAvailable()) {
            LoggerUtil.error("map-storage is MYSQL but database-type in database.yml is not MySQL, maps are stored as JSON");
            return MapStorageType.JSON;
        }
        return type;
    }

    public static IMapStorage getDefaultStorage() {
        return getStorage(getDefaultType());
    }

    public static boolean isMysqlAvailable() {
        return AzuraBedWars.getInstance().getDatabaseProviderFactory().getDatabaseProvider() instanceof MySQLDatabaseProvider;
    }

    public static IMapStorage getStorage(MapStorageType type) {
        if (type == MapStorageType.JSON) {
            if (jsonMapStorage == null) {
                jsonMapStorage = new JsonMapStorage();
            }
            return jsonMapStorage;
        }
        if (mysqlMapStorage == null) {
            IDatabaseProvider provider = AzuraBedWars.getInstance().getDatabaseProviderFactory().getDatabaseProvider();
            mysqlMapStorage = new MySQLMapStorage(((MySQLDatabaseProvider) provider).getDataSource(), MAP_TABLE_NAME);
        }
        return mysqlMapStorage;
    }

    /**
     * @param mapName null copies every map
     * @return false when any map failed to copy
     */
    public static boolean migrate(IMapStorage source, IMapStorage target, String mapName) {
        boolean success = true;
        for (String name : mapName != null ? Collections.singletonList(mapName) : source.getAllMapNames()) {
            MapData map = source.loadMap(name);
            if (map == null || !target.saveMap(name, map)) {
                LoggerUtil.warn("Cannot migrate map " + name);
                success = false;
            }
        }
        return success;
    }
}
