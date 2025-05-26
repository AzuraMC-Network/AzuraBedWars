package cc.azuramc.bedwars.database.storage.provider;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.dao.MapDAO;
import cc.azuramc.bedwars.database.storage.IMapStorage;
import cc.azuramc.bedwars.game.map.MapData;
import org.bukkit.Bukkit;

import java.util.List;

/**
 * MySQL存储实现
 * 使用MySQL数据库存储和读取地图数据
 *
 * @author an5w1r@163.com
 */
public class MySQLMapStorage implements IMapStorage {
    private final MapDAO mapDAO;
    
    /**
     * 创建一个MySQL存储实现
     * @param databaseName 数据库名称
     * @param tableName 表名称
     */
    public MySQLMapStorage(String databaseName, String tableName) {
        // 使用MapDAO替代直接的数据库操作
        this.mapDAO = MapDAO.getInstance();
    }
    
    @Override
    public boolean saveMap(String mapName, MapData mapData) {
        if (mapName == null || mapData == null) {
            return false;
        }
        
        return mapDAO.saveMap(mapName, mapData);
    }
    
    @Override
    public MapData loadMap(String mapName) {
        return mapDAO.loadMap(mapName);
    }
    
    @Override
    public boolean deleteMap(String mapName) {
        return mapDAO.deleteMap(mapName);
    }
    
    @Override
    public boolean exists(String mapName) {
        return mapDAO.exists(mapName);
    }
    
    @Override
    public List<String> getAllMapNames() {
        return mapDAO.getAllMapNames();
    }
    
    @Override
    public boolean migrateTo(IMapStorage targetStorage, String mapName) {
        if (targetStorage == null) {
            return false;
        }
        
        // 如果指定了地图名称，只迁移单个地图
        if (mapName != null) {
            if (!exists(mapName)) {
                return false;
            }
            
            MapData mapData = loadMap(mapName);
            return mapData != null && targetStorage.saveMap(mapName, mapData);
        }
        
        // 否则迁移所有地图
        boolean allSuccess = true;
        for (String name : getAllMapNames()) {
            MapData mapData = loadMap(name);
            if (mapData == null || !targetStorage.saveMap(name, mapData)) {
                allSuccess = false;
            }
        }
        
        return allSuccess;
    }
} 