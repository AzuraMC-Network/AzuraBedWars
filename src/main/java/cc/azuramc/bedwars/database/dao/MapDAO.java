package cc.azuramc.bedwars.database.dao;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.DatabaseConstants;
import cc.azuramc.bedwars.database.query.QueryBuilder;
import cc.azuramc.bedwars.game.map.MapData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 地图数据DAO类
 * 负责地图数据的数据库操作
 * 
 * @author an5w1r@163.com
 */
public class MapDAO {
    private static final Logger LOGGER = Logger.getLogger(MapDAO.class.getName());
    private static final String MAP_TABLE = AzuraBedWars.MAP_TABLE_NAME;
    
    private final DatabaseManager databaseManager;
    private final Gson gson;
    
    /**
     * 单例实例
     */
    private static MapDAO instance;
    
    /**
     * 获取MapDAO单例实例
     * 
     * @return MapDAO实例
     */
    public static synchronized MapDAO getInstance() {
        if (instance == null) {
            instance = new MapDAO();
        }
        return instance;
    }
    
    /**
     * 私有构造函数，防止外部实例化
     */
    private MapDAO() {
        this.databaseManager = DatabaseManager.getInstance();
        this.gson = new GsonBuilder().create();
        
        // 确保表存在
        ensureTableExists();
    }
    
    /**
     * 确保地图表存在
     */
    private void ensureTableExists() {
        try {
            QueryBuilder queryBuilder = new QueryBuilder()
                .table(MAP_TABLE);
            
            databaseManager.executeUpdate(queryBuilder.buildCreateTableQuery(DatabaseConstants.MAP_TABLE_DEFINITION), null);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "创建地图表时出错", e);
        }
    }
    
    /**
     * 保存地图数据
     * 
     * @param mapName 地图名称
     * @param mapData 地图数据
     * @return 是否成功
     */
    public boolean saveMap(String mapName, MapData mapData) {
        if (mapName == null || mapData == null) {
            return false;
        }
        
        try {
            String jsonData = gson.toJson(mapData);
            
            if (exists(mapName)) {
                QueryBuilder queryBuilder = new QueryBuilder()
                    .table(MAP_TABLE)
                    .update(DatabaseConstants.MAP_COL_DISPLAY_NAME, mapData.getDisplayName())
                    .update(DatabaseConstants.MAP_COL_MIN_PLAYERS, mapData.getMinPlayers())
                    .update(DatabaseConstants.MAP_COL_MAX_PLAYERS, mapData.getMaxPlayers())
                    .update(DatabaseConstants.MAP_COL_TEAMS, mapData.getTeams())
                    .update(DatabaseConstants.MAP_COL_DATA, jsonData)
                    .update(DatabaseConstants.MAP_COL_AUTHOR, mapData.getAuthor())
                    .where(DatabaseConstants.MAP_COL_NAME + " = ?", mapName);
                
                int affected = databaseManager.executeUpdate(queryBuilder);
                return affected > 0;
            } else {
                QueryBuilder queryBuilder = new QueryBuilder()
                    .table(MAP_TABLE)
                    .insert(DatabaseConstants.MAP_COL_NAME, mapName)
                    .insert(DatabaseConstants.MAP_COL_DISPLAY_NAME, mapData.getDisplayName())
                    .insert(DatabaseConstants.MAP_COL_MIN_PLAYERS, mapData.getMinPlayers())
                    .insert(DatabaseConstants.MAP_COL_MAX_PLAYERS, mapData.getMaxPlayers())
                    .insert(DatabaseConstants.MAP_COL_TEAMS, mapData.getTeams())
                    .insert(DatabaseConstants.MAP_COL_DATA, jsonData)
                    .insert(DatabaseConstants.MAP_COL_AUTHOR, mapData.getAuthor());
                
                int affected = databaseManager.executeInsert(queryBuilder);
                return affected > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "保存地图数据时出错", e);
            return false;
        }
    }
    
    /**
     * 异步保存地图数据
     * 
     * @param mapName 地图名称
     * @param mapData 地图数据
     * @return 异步任务
     */
    public CompletableFuture<Boolean> saveMapAsync(String mapName, MapData mapData) {
        return CompletableFuture.supplyAsync(() -> saveMap(mapName, mapData));
    }
    
    /**
     * 加载地图数据
     * 
     * @param mapName 地图名称
     * @return 地图数据
     */
    public MapData loadMap(String mapName) {
        try {
            QueryBuilder queryBuilder = new QueryBuilder()
                .table(MAP_TABLE)
                .select("*")
                .where(DatabaseConstants.MAP_COL_NAME + " = ?", mapName);
            
            return databaseManager.executeQuery(queryBuilder, resultSet -> {
                try {
                    if (resultSet.next()) {
                        String jsonData = resultSet.getString(DatabaseConstants.MAP_COL_DATA);
                        MapData mapData = gson.fromJson(jsonData, MapData.class);
                        mapData.setName(mapName);
                        
                        // 设置其他属性
                        String author = resultSet.getString(DatabaseConstants.MAP_COL_AUTHOR);
                        if (author != null && !author.isEmpty()) {
                            mapData.setAuthor(author);
                        }
                        
                        return mapData;
                    }
                    return null;
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "加载地图数据时出错", e);
                    return null;
                }
            });
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "加载地图数据时出错", e);
            return null;
        }
    }
    
    /**
     * 异步加载地图数据
     * 
     * @param mapName 地图名称
     * @return 异步任务
     */
    public CompletableFuture<MapData> loadMapAsync(String mapName) {
        return CompletableFuture.supplyAsync(() -> loadMap(mapName));
    }
    
    /**
     * 删除地图数据
     * 
     * @param mapName 地图名称
     * @return 是否成功
     */
    public boolean deleteMap(String mapName) {
        try {
            QueryBuilder queryBuilder = new QueryBuilder()
                .table(MAP_TABLE)
                .where(DatabaseConstants.MAP_COL_NAME + " = ?", mapName);
            
            int affected = databaseManager.executeDelete(queryBuilder);
            return affected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "删除地图数据时出错", e);
            return false;
        }
    }
    
    /**
     * 异步删除地图数据
     * 
     * @param mapName 地图名称
     * @return 异步任务
     */
    public CompletableFuture<Boolean> deleteMapAsync(String mapName) {
        return CompletableFuture.supplyAsync(() -> deleteMap(mapName));
    }
    
    /**
     * 检查地图是否存在
     * 
     * @param mapName 地图名称
     * @return 是否存在
     */
    public boolean exists(String mapName) {
        try {
            QueryBuilder queryBuilder = new QueryBuilder()
                .table(MAP_TABLE)
                .select("1")
                .where(DatabaseConstants.MAP_COL_NAME + " = ?", mapName);
            
            return databaseManager.exists(queryBuilder);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "检查地图是否存在时出错", e);
            return false;
        }
    }
    
    /**
     * 获取所有地图名称
     * 
     * @return 地图名称列表
     */
    public List<String> getAllMapNames() {
        try {
            QueryBuilder queryBuilder = new QueryBuilder()
                .table(MAP_TABLE)
                .select(DatabaseConstants.MAP_COL_NAME);
            
            return databaseManager.executeQuery(queryBuilder, resultSet -> {
                List<String> names = new ArrayList<>();
                try {
                    while (resultSet.next()) {
                        names.add(resultSet.getString(DatabaseConstants.MAP_COL_NAME));
                    }
                    return names;
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "获取所有地图名称时出错", e);
                    return names;
                }
            });
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "获取所有地图名称时出错", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 异步获取所有地图名称
     * 
     * @return 异步任务
     */
    public CompletableFuture<List<String>> getAllMapNamesAsync() {
        return CompletableFuture.supplyAsync(this::getAllMapNames);
    }
    
    /**
     * 迁移地图数据到另一个存储
     * 
     * @param targetDAO 目标DAO
     * @param mapName 指定地图名称，如果为null则迁移所有地图
     * @return 是否成功
     */
    public boolean migrateTo(MapDAO targetDAO, String mapName) {
        if (targetDAO == null) {
            return false;
        }
        
        // 如果指定了地图名称，只迁移单个地图
        if (mapName != null) {
            if (!exists(mapName)) {
                return false;
            }
            
            MapData mapData = loadMap(mapName);
            return mapData != null && targetDAO.saveMap(mapName, mapData);
        }
        
        // 否则迁移所有地图
        boolean allSuccess = true;
        for (String name : getAllMapNames()) {
            MapData mapData = loadMap(name);
            if (mapData == null || !targetDAO.saveMap(name, mapData)) {
                allSuccess = false;
            }
        }
        
        return allSuccess;
    }
    
    /**
     * 异步迁移地图数据
     * 
     * @param targetDAO 目标DAO
     * @param mapName 指定地图名称，如果为null则迁移所有地图
     * @return 异步任务
     */
    public CompletableFuture<Boolean> migrateToAsync(MapDAO targetDAO, String mapName) {
        return CompletableFuture.supplyAsync(() -> migrateTo(targetDAO, mapName));
    }
} 