package cc.azuramc.bedwars.database.storage;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.storage.provider.JsonMapStorage;
import cc.azuramc.bedwars.database.storage.provider.MySQLMapStorage;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * 地图存储工厂类
 * 用于创建和获取不同类型的存储实现
 *
 * @author an5w1r@163.com
 */
public class MapStorageFactory {
    
    /**
     * 存储类型枚举
     */
    public enum StorageType {
        /**
         * JSON存储地图信息格式
         */
        JSON,
        /**
         * MYSQL存储地图信息格式
         */
        MYSQL
    }
    
    private static JsonMapStorage jsonMapStorage;
    private static MySQLMapStorage mysqlMapStorage;
    
    /**
     * 获取JSON存储实现
     * @return JSON存储实现
     */
    public static JsonMapStorage getJsonStorage() {
        if (jsonMapStorage == null) {
            jsonMapStorage = new JsonMapStorage();
        }
        return jsonMapStorage;
    }
    
    /**
     * 获取MySQL存储实现
     * @return MySQL存储实现
     */
    public static MySQLMapStorage getMysqlStorage() {
        if (mysqlMapStorage == null) {
            FileConfiguration config = AzuraBedWars.getInstance().getConfig();
            String database = config.getString("database.maps.name", "bwdata");
            String table = config.getString("database.maps.table", "BWMaps");
            mysqlMapStorage = new MySQLMapStorage(database, table);
        }
        return mysqlMapStorage;
    }
    
    /**
     * 获取默认存储实现
     * 根据配置文件中的设置返回相应的存储实现
     * @return 默认存储实现
     */
    public static IMapStorage getDefaultStorage() {
        FileConfiguration config = AzuraBedWars.getInstance().getConfig();
        String storageTypeStr = config.getString("map.storage", "JSON");
        
        try {
            StorageType storageType = StorageType.valueOf(storageTypeStr.toUpperCase());
            return getStorage(storageType);
        } catch (IllegalArgumentException e) {
            // 配置错误，默认使用JSON
            return getJsonStorage();
        }
    }
    
    /**
     * 根据存储类型获取相应的存储实现
     * @param storageType 存储类型
     * @return 存储实现
     */
    public static IMapStorage getStorage(StorageType storageType) {
        return switch (storageType) {
            case JSON -> getJsonStorage();
            case MYSQL -> getMysqlStorage();
            default -> getJsonStorage();
        };
    }
    
    /**
     * 将地图数据从一种存储方式迁移到另一种
     * @param sourceType 源存储类型
     * @param targetType 目标存储类型
     * @param mapName 地图名称，如果为null则迁移所有地图
     * @return 是否迁移成功
     */
    public static boolean migrateStorage(StorageType sourceType, StorageType targetType, String mapName) {
        if (sourceType == targetType) {
            // 相同类型无需迁移
            return true;
        }
        
        IMapStorage source = getStorage(sourceType);
        IMapStorage target = getStorage(targetType);
        
        return source.migrateTo(target, mapName);
    }
} 