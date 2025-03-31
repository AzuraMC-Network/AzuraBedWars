package cc.azuramc.bedwars.map;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.map.data.MapData;
import cc.azuramc.bedwars.map.mysql.MySQLMapStorage;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 地图管理器
 * 整合了地图的加载、保存、物理文件管理等功能
 */
@Getter
public class MapManager {
    
    private final IMapStorage defaultStorage;

    private final Map<String, MapData> loadedMaps = new HashMap<>();
    
    /**
     * 创建地图管理器
     */
    public MapManager() {
        this.defaultStorage = MapStorageFactory.getDefaultStorage();
    }
    
    /**
     * 加载地图数据
     * @param mapName 地图名称
     * @return 地图数据对象，如果不存在则返回null
     */
    public MapData loadMapData(String mapName) {
        // 如果已经加载过，直接返回缓存
        if (loadedMaps.containsKey(mapName)) {
            return loadedMaps.get(mapName);
        }
        
        // 否则从存储加载
        MapData mapData = defaultStorage.loadMap(mapName);
        if (mapData != null) {
            mapData.setName(mapName);
            loadedMaps.put(mapName, mapData);
        }
        
        return mapData;
    }
    
    /**
     * 保存地图数据
     * @param mapName 地图名称
     * @param mapData 地图数据对象
     * @return 是否保存成功
     */
    public boolean saveMapData(String mapName, MapData mapData) {
        if (defaultStorage.saveMap(mapName, mapData)) {
            loadedMaps.put(mapName, mapData);
            return true;
        }
        return false;
    }

    /**
     * 保存地图数据
     * @param mapName 地图名称
     * @return 是否保存成功
     */
    public boolean saveMapData(String mapName) {
        MapData mapData = loadedMaps.get(mapName);
        if (defaultStorage.saveMap(mapName, mapData)) {
            loadedMaps.put(mapName, mapData);
            return true;
        }
        return false;
    }
    
    /**
     * 预加载所有地图数据到内存
     */
    public void preloadAllMaps() {
        List<String> mapNames = defaultStorage.getAllMapNames();
        for (String mapName : mapNames) {
            MapData mapData = defaultStorage.loadMap(mapName);
            if (mapData != null) {
                mapData.setName(mapName);
                loadedMaps.put(mapName, mapData);
            }
        }
    }
    
    /**
     * 获取所有已加载的地图
     * @return 地图名称到地图数据的映射
     */
    public Map<String, MapData> getLoadedMaps() {
        return new HashMap<>(loadedMaps);
    }
    
    /**
     * 判断地图数据是否存在
     * @param mapName 地图名称
     * @return 是否存在
     */
    public boolean exists(String mapName) {
        return loadedMaps.containsKey(mapName) || defaultStorage.exists(mapName);
    }
    
    /**
     * 删除地图数据
     * @param mapName 地图名称
     * @return 是否删除成功
     */
    public boolean deleteMapData(String mapName) {
        loadedMaps.remove(mapName);
        return defaultStorage.deleteMap(mapName);
    }
    
    /**
     * 加载地图的物理文件并创建世界
     * 该方法加载地图数据，并将物理文件复制到适当位置，然后创建Bukkit世界
     * @param mapName 地图名称
     * @param mapUrl 地图物理文件的URL（路径）
     * @return 创建的世界，如果失败则返回null
     */
    public World loadMapWorld(String mapName, String mapUrl) {
        if (mapName == null || mapUrl == null) {
            return null;
        }
        
        try {
            // 检查目标目录是否存在，存在则删除
            File targetDir = new File(mapName);
            if (targetDir.exists()) {
                FileUtils.deleteDirectory(targetDir);
            }
            
            // 复制地图文件
            FileUtils.copyDirectory(new File(mapUrl), targetDir);
            
            // 创建世界
            WorldCreator worldCreator = new WorldCreator(mapName);
            worldCreator.environment(World.Environment.NORMAL);
            World mapWorld = Bukkit.createWorld(worldCreator);
            
            // 设置世界属性
            if (mapWorld != null) {
                mapWorld.setAutoSave(false);
                mapWorld.setGameRuleValue("doMobSpawning", "false");
                mapWorld.setGameRuleValue("doFireTick", "false");
                return mapWorld;
            }
        } catch (IOException e) {
            Bukkit.getLogger().severe("加载地图世界时出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * 加载地图和世界
     * 从存储加载地图数据，并创建对应的世界
     * @param mapName 地图名称
     * @return 地图数据对象，如果失败则返回null
     */
    public MapData loadMapAndWorld(String mapName) {
        // 从MySQL存储实现获取地图URL
        String mapUrl = null;
        if (defaultStorage instanceof MySQLMapStorage) {
            mapUrl = ((MySQLMapStorage) defaultStorage).getMapUrl(mapName);
        }
        
        if (mapUrl == null || mapUrl.isEmpty()) {
            Bukkit.getLogger().warning("地图 " + mapName + " 没有设置物理路径(URL)");
            return null;
        }
        
        // 加载地图数据
        MapData mapData = loadMapData(mapName);
        if (mapData == null) {
            return null;
        }
        
        // 加载世界
        World world = loadMapWorld(mapName, mapUrl);
        if (world == null) {
            return null;
        }
        
        return mapData;
    }
    
    /**
     * 设置地图的物理路径
     * @param mapName 地图名称
     * @param mapUrl 地图物理路径
     * @return 是否设置成功
     */
    public boolean setMapUrl(String mapName, String mapUrl) {
        // 只有MySQL存储才支持设置URL
        if (defaultStorage instanceof MySQLMapStorage) {
            return ((MySQLMapStorage) defaultStorage).setMapUrl(mapName, mapUrl);
        }
        
        return false;
    }
    
    /**
     * 获取地图的物理路径
     * @param mapName 地图名称
     * @return 地图物理路径，如果不存在则返回null
     */
    public String getMapUrl(String mapName) {
        // 只有MySQL存储才支持获取URL
        if (defaultStorage instanceof MySQLMapStorage) {
            return ((MySQLMapStorage) defaultStorage).getMapUrl(mapName);
        }
        
        return null;
    }
    
    /**
     * 获取等待大厅的位置
     * 该方法从配置中获取等待大厅的位置并加载对应的世界
     * @return 等待大厅的位置，如果不存在则返回null
     */
    public org.bukkit.Location getWaitingLocation() {
        // 从配置中获取等待大厅信息
        try {
            if (defaultStorage instanceof MySQLMapStorage) {
                MySQLMapStorage mysqlStorage = (MySQLMapStorage) defaultStorage;
                
                // 获取等待大厅地图URL
                String worldUrl = null;
                try (java.sql.Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection("bwdata")) {
                    java.sql.PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM BWConfig WHERE configKey=?");
                    preparedStatement.setString(1, "WaitingMapURL");
                    java.sql.ResultSet resultSet = preparedStatement.executeQuery();
                    
                    if (resultSet.next()) {
                        worldUrl = resultSet.getString("object");
                    }
                    
                    preparedStatement.close();
                    resultSet.close();
                }
                
                if (worldUrl == null || worldUrl.isEmpty()) {
                    return null;
                }
                
                // 获取世界名称
                String worldName = new File(worldUrl).getName();
                
                // 加载世界
                World world = loadMapWorld(worldName, worldUrl);
                if (world == null) {
                    return null;
                }
                
                // 获取等待位置
                try (java.sql.Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection("bwdata")) {
                    java.sql.PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM BWConfig WHERE configKey=?");
                    preparedStatement.setString(1, "WaitingLoc");
                    java.sql.ResultSet resultSet = preparedStatement.executeQuery();
                    
                    if (resultSet.next()) {
                        MapData.RawLocation location = new com.google.gson.Gson().fromJson(resultSet.getString("object"), MapData.RawLocation.class);
                        location.setWorld(worldName);
                        return location.toLocation();
                    }
                    
                    preparedStatement.close();
                    resultSet.close();
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("获取等待位置时出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
} 