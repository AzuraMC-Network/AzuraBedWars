package cc.azuramc.bedwars.database.storage.provider;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.storage.IMapStorage;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.util.LoggerUtil;
import cc.azuramc.orm.AzuraOrmClient;
import cc.azuramc.orm.builder.DataType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    private final AzuraOrmClient ormClient;

    /**
     * 创建一个MySQL存储实现
     *
     * @param plugin    插件实例
     * @param tableName 表名称
     */
    public MySQLMapStorage(AzuraBedWars plugin, String tableName) {
        this.gson = new GsonBuilder().create();
        this.ormClient = plugin.getOrmClient();
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
            PreparedStatement createTableStmt = ormClient.createTable(conn)
                    .createTable(tableName)
                    .ifNotExists()
                    .addIdColumn()
                    .column(mapKey, DataType.VARCHAR_NOT_NULL(64))
                    .column("json_data", DataType.Type.TEXT.getSql())
                    .engine("InnoDB")
                    .charset("utf8mb4")
                    .collate("utf8mb4_unicode_ci")
                    .index(mapKey)
                    .prepare();

            createTableStmt.executeUpdate();
            createTableStmt.close();
        }
    }

    @Override
    public boolean saveMap(String mapName, MapData mapData) {
        if (mapName == null || mapData == null) {
            return false;
        }

        try (Connection conn = ormClient.getConnection()) {
            String jsonData = gson.toJson(mapData);

            if (exists(mapName)) {
                // 更新现有记录
                PreparedStatement updateStmt = ormClient.update(conn)
                        .update(tableName)
                        .set(mapDataKey, jsonData)
                        .whereEquals(mapKey, mapName)
                        .prepare();

                int affected = updateStmt.executeUpdate();
                updateStmt.close();
                return affected > 0;
            } else {
                // 插入新记录
                PreparedStatement insertStmt = ormClient.insert(conn)
                        .insertInto(tableName)
                        .values(mapKey, mapName)
                        .values(mapDataKey, jsonData)
                        .prepare();

                int affected = insertStmt.executeUpdate();
                insertStmt.close();
                return affected > 0;
            }
        } catch (SQLException e) {
            LoggerUtil.error("保存地图数据到MySQL时出错: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public MapData loadMap(String mapName) {
        try (Connection conn = ormClient.getConnection()) {
            PreparedStatement selectStmt = ormClient.select(conn)
                    .from(tableName)
                    .select(mapDataKey)
                    .whereEquals(mapKey, mapName)
                    .prepare();

            ResultSet resultSet = selectStmt.executeQuery();

            if (resultSet.next()) {
                String jsonData = resultSet.getString(mapDataKey);
                MapData mapData = gson.fromJson(jsonData, MapData.class);
                mapData.setName(mapName);

                resultSet.close();
                selectStmt.close();
                return mapData;
            }

            resultSet.close();
            selectStmt.close();
        } catch (SQLException e) {
            LoggerUtil.error("从MySQL加载地图数据时出错: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean deleteMap(String mapName) {
        try (Connection conn = ormClient.getConnection()) {
            PreparedStatement deleteStmt = ormClient.delete(conn)
                    .deleteFrom(tableName)
                    .whereEquals(mapKey, mapName)
                    .prepare();

            int affected = deleteStmt.executeUpdate();
            deleteStmt.close();
            return affected > 0;
        } catch (SQLException e) {
            LoggerUtil.error("从MySQL删除地图数据时出错: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean exists(String mapName) {
        try (Connection conn = ormClient.getConnection()) {
            PreparedStatement selectStmt = ormClient.select(conn)
                    .from(tableName)
                    .select("id")
                    .whereEquals(mapKey, mapName)
                    .prepare();

            ResultSet resultSet = selectStmt.executeQuery();
            boolean exists = resultSet.next();

            resultSet.close();
            selectStmt.close();
            return exists;
        } catch (SQLException e) {
            LoggerUtil.error("检查地图是否存在时出错: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<String> getAllMapNames() {
        List<String> mapNames = new ArrayList<>();

        try (Connection conn = ormClient.getConnection()) {
            PreparedStatement selectStmt = ormClient.select(conn)
                    .from(tableName)
                    .select(mapKey)
                    .prepare();

            ResultSet resultSet = selectStmt.executeQuery();

            while (resultSet.next()) {
                mapNames.add(resultSet.getString(mapKey));
            }

            resultSet.close();
            selectStmt.close();
        } catch (SQLException e) {
            LoggerUtil.error("获取所有地图名称时出错: " + e.getMessage());
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
}
