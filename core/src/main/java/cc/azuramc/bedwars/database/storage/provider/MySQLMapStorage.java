package cc.azuramc.bedwars.database.storage.provider;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.provider.mysql.MySQLDatabaseProvider;
import cc.azuramc.bedwars.database.storage.IMapStorage;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL存储实现
 * 使用MySQL数据库存储和读取地图数据
 *
 * @author an5w1r@163.com
 */
public class MySQLMapStorage implements IMapStorage {
    private final Gson gson;
    private static final String mapKey = "map_name";
    private final String tableName;
    private static final String mapDataKey = "json_data";
    private final DataSource dataSource;

    /**
     * 创建一个MySQL存储实现
     *
     * @param plugin    插件实例
     * @param tableName 表名称
     */
    public MySQLMapStorage(AzuraBedWars plugin, String tableName) {
        this.gson = new GsonBuilder().create();
        MySQLDatabaseProvider mySQLDatabaseProvider = (MySQLDatabaseProvider) plugin.getDatabaseProviderFactory().getDatabaseProvider();
        this.dataSource = mySQLDatabaseProvider.getDataSource();
        this.tableName = tableName;

        try {
            setupDatabase();
        } catch (SQLException e) {
            LoggerUtil.error("设置数据库表结构时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 设置数据库表结构
     */
    private void setupDatabase() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS " + tableName + " (\n"
                    + "  `id` INT PRIMARY KEY AUTO_INCREMENT,\n"
                    + "  `" + mapKey + "` VARCHAR(255) NOT NULL,\n"
                    + "  `" + mapDataKey + "` TEXT\n"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
        }
    }

    @Override
    public boolean saveMap(String mapName, MapData mapData) {
        if (mapName == null || mapData == null) {
            return false;
        }
        String jsonData = gson.toJson(mapData);
        String sql = exists(mapName)
                ? "UPDATE " + tableName + " SET `" + mapDataKey + "` = ? WHERE `" + mapKey + "` = ?"
                : "INSERT INTO " + tableName + " (`" + mapDataKey + "`, `" + mapKey + "`) VALUES (?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, jsonData);
            ps.setString(2, mapName);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save map: " + mapName, e);
        }
    }

    @Override
    public MapData loadMap(String mapName) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT `" + mapDataKey + "` FROM " + tableName + " WHERE `" + mapKey + "` = ?")) {
            ps.setString(1, mapName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? gson.fromJson(rs.getString(mapDataKey), MapData.class) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load map: " + mapName, e);
        }
    }

    @Override
    public boolean deleteMap(String mapName) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("DELETE FROM " + tableName + " WHERE `" + mapKey + "` = ?")) {
            ps.setString(1, mapName);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete map: " + mapName, e);
        }
    }

    @Override
    public boolean exists(String mapName) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM " + tableName + " WHERE `" + mapKey + "` = ? LIMIT 1")) {
            ps.setString(1, mapName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check if map exists: " + mapName, e);
        }
    }

    @Override
    public List<String> getAllMapNames() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT `" + mapKey + "` FROM " + tableName);
             ResultSet rs = ps.executeQuery()) {
            List<String> names = new ArrayList<>();
            while (rs.next()) {
                names.add(rs.getString(mapKey));
            }
            return names;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get all map names", e);
        }
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
