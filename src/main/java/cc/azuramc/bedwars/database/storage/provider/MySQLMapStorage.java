package cc.azuramc.bedwars.database.storage.provider;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.storage.IMapStorage;
import cc.azuramc.bedwars.game.arena.MapData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL存储实现
 * 使用MySQL数据库存储和读取地图数据
 */
public class MySQLMapStorage implements IMapStorage {
    private final Gson gson;
    private final String databaseName;
    private final String tableName;
    
    /**
     * 创建一个MySQL存储实现
     * @param databaseName 数据库名称
     * @param tableName 表名称
     */
    public MySQLMapStorage(String databaseName, String tableName) {
        this.gson = new GsonBuilder().create();
        this.databaseName = databaseName;
        this.tableName = tableName;

        setupDatabase();
    }
    
    /**
     * 默认构造函数，使用默认的数据库和表名
     */
    public MySQLMapStorage() {
        this("bwdata", "BWMaps");
    }
    
    /**
     * 设置数据库表结构
     */
    private void setupDatabase() {
        try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection(databaseName)) {
            if (connection == null) {
                Bukkit.getLogger().severe("无法连接到数据库: " + databaseName);
                return;
            }
            
            // 创建地图数据表
            String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "MapName VARCHAR(64) NOT NULL UNIQUE, " +
                    "Data TEXT NOT NULL, " +
                    "URL VARCHAR(255), " +
                    "Author VARCHAR(64), " +
                    "CreateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "UpdateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")";
            
            try (PreparedStatement statement = connection.prepareStatement(createTableSQL)) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("设置数据库表结构时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean saveMap(String mapName, MapData mapData) {
        if (mapName == null || mapData == null) {
            return false;
        }
        
        try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection(databaseName)) {
            if (connection == null) {
                return false;
            }
            
            String sql;
            if (exists(mapName)) {
                // 更新现有记录
                sql = "UPDATE " + tableName + " SET Data=?, Author=? WHERE MapName=?";
            } else {
                // 插入新记录
                sql = "INSERT INTO " + tableName + " (MapName, Data, Author) VALUES (?, ?, ?)";
            }
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                String jsonData = gson.toJson(mapData);
                
                if (exists(mapName)) {
                    statement.setString(1, jsonData);
                    statement.setString(2, mapData.getAuthor());
                    statement.setString(3, mapName);
                } else {
                    statement.setString(1, mapName);
                    statement.setString(2, jsonData);
                    statement.setString(3, mapData.getAuthor());
                }
                
                int affected = statement.executeUpdate();
                return affected > 0;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("保存地图数据到MySQL时出错: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public MapData loadMap(String mapName) {
        try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection(databaseName)) {
            if (connection == null) {
                return null;
            }
            
            String sql = "SELECT * FROM " + tableName + " WHERE MapName=?";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, mapName);
                
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String jsonData = resultSet.getString("Data");
                        MapData mapData = gson.fromJson(jsonData, MapData.class);
                        mapData.setName(mapName);
                        
                        // 设置作者信息
                        String author = resultSet.getString("Author");
                        if (author != null && !author.isEmpty()) {
                            mapData.setAuthor(author);
                        }
                        
                        return mapData;
                    }
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("从MySQL加载地图数据时出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    @Override
    public boolean deleteMap(String mapName) {
        try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection(databaseName)) {
            if (connection == null) {
                return false;
            }
            
            String sql = "DELETE FROM " + tableName + " WHERE MapName=?";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, mapName);
                int affected = statement.executeUpdate();
                return affected > 0;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("从MySQL删除地图数据时出错: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean exists(String mapName) {
        try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection(databaseName)) {
            if (connection == null) {
                return false;
            }
            
            String sql = "SELECT 1 FROM " + tableName + " WHERE MapName=?";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, mapName);
                
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("检查地图是否存在时出错: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public List<String> getAllMapNames() {
        List<String> mapNames = new ArrayList<>();
        
        try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection(databaseName)) {
            if (connection == null) {
                return mapNames;
            }
            
            String sql = "SELECT MapName FROM " + tableName;
            
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                
                while (resultSet.next()) {
                    mapNames.add(resultSet.getString("MapName"));
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("获取所有地图名称时出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        return mapNames;
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
    
    /**
     * 设置地图的地址（物理位置）
     * @param mapName 地图名称
     * @param url 地图地址
     * @return 是否设置成功
     */
    public boolean setMapUrl(String mapName, String url) {
        try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection(databaseName)) {
            if (connection == null) {
                return false;
            }
            
            String sql = "UPDATE " + tableName + " SET URL=? WHERE MapName=?";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, url);
                statement.setString(2, mapName);
                
                int affected = statement.executeUpdate();
                return affected > 0;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("设置地图URL时出错: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 获取地图的地址（物理位置）
     * @param mapName 地图名称
     * @return 地图地址，如果不存在则返回null
     */
    public String getMapUrl(String mapName) {
        try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection(databaseName)) {
            if (connection == null) {
                return null;
            }
            
            String sql = "SELECT URL FROM " + tableName + " WHERE MapName=?";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, mapName);
                
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString("URL");
                    }
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("获取地图URL时出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
} 