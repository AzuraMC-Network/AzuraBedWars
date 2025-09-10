package cc.azuramc.bedwars.database.storage.provider;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.provider.mysql.MySQLDatabaseProvider;
import cc.azuramc.bedwars.database.storage.IMapStorage;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.util.LoggerUtil;
import cc.azuramc.orm.AzuraOrmClient;
import cc.azuramc.orm.builder.ColumnDefinitionBuilder;
import cc.azuramc.orm.builder.DataType;
import cc.azuramc.orm.builder.PreparedStatementBuildManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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
    private final AzuraOrmClient ormClient;

    /**
     * 创建一个MySQL存储实现
     *
     * @param plugin    插件实例
     * @param tableName 表名称
     */
    public MySQLMapStorage(AzuraBedWars plugin, String tableName) {
        this.gson = new GsonBuilder().create();
        MySQLDatabaseProvider mySQLDatabaseProvider = (MySQLDatabaseProvider) plugin.getDatabaseProviderFactory().getDatabaseProvider();
        this.ormClient = mySQLDatabaseProvider.getOrmClient();
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
        try (Connection conn = ormClient.getConnection()) {
            PreparedStatementBuildManager buildManager = new PreparedStatementBuildManager(conn, false);
            PreparedStatement createTableStmt = buildManager.createTable(tableName)
                    .ifNotExists()
                    .addIdColumn()
                    .column(mapKey, ColumnDefinitionBuilder.of(DataType.Type.VARCHAR).size(255).notNull().build())
                    .column("json_data", ColumnDefinitionBuilder.of(DataType.Type.TEXT).build())
                    .engine("InnoDB")
                    .charset("utf8mb4")
                    .collate("utf8mb4_unicode_ci")
                    .index(mapKey)
                    .prepare();

            buildManager.execute(createTableStmt);
        }
    }

    @Override
    public boolean saveMap(String mapName, MapData mapData) {
        if (mapName == null || mapData == null) {
            return false;
        }
        String jsonData = gson.toJson(mapData);
        try (Connection connection = ormClient.getConnection()) {
            PreparedStatementBuildManager buildManager = new PreparedStatementBuildManager(connection, false);
            if (exists(mapName)) {
                PreparedStatement updateStmt = buildManager.update(tableName)
                        .set("json_data", jsonData)
                        .whereEquals("map_name", mapName)
                        .prepare();

                buildManager.execute(updateStmt);
            } else {
                PreparedStatement insertStmt = buildManager.insertInto(tableName)
                        .values("map_name", mapName)
                        .values("json_data", jsonData)
                        .prepare();

                buildManager.execute(insertStmt);
            }
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save map: " + mapName, e);
        }
    }

    @Override
    public MapData loadMap(String mapName) {
        try (Connection connection = ormClient.getConnection()) {
            PreparedStatementBuildManager buildManager = new PreparedStatementBuildManager(connection, false);

            Optional<MapData> result = buildManager.select()
                    .from(tableName)
                    .select("json_data")
                    .whereEquals("map_name", mapName)
                    .executeQueryForObject(rs -> {
                        String jsonData = rs.getString("json_data");
                        return gson.fromJson(jsonData, MapData.class);
                    });

            return result.orElse(null);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load map: " + mapName, e);
        }
    }

    @Override
    public boolean deleteMap(String mapName) {
        try (Connection connection = ormClient.getConnection()) {
            PreparedStatementBuildManager buildManager = new PreparedStatementBuildManager(connection, false);
            PreparedStatement deleteStmt = buildManager.deleteFrom(tableName)
                    .whereEquals("map_name", mapName)
                    .prepare();

            int rowsAffected = buildManager.execute(deleteStmt);
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete map: " + mapName, e);
        }
    }

    @Override
    public boolean exists(String mapName) {
        try (Connection connection = ormClient.getConnection()) {
            PreparedStatementBuildManager buildManager = new PreparedStatementBuildManager(connection, false);

            return buildManager.select()
                    .from(tableName)
                    .select("map_name")
                    .whereEquals("map_name", mapName)
                    .executeQueryForExists();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check if map exists: " + mapName, e);
        }
    }

    @Override
    public List<String> getAllMapNames() {
        try (Connection connection = ormClient.getConnection()) {
            PreparedStatementBuildManager buildManager = new PreparedStatementBuildManager(connection, false);

            return buildManager.select()
                    .from(tableName)
                    .select("map_name")
                    .executeQueryForList(rs -> rs.getString("map_name"));
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
