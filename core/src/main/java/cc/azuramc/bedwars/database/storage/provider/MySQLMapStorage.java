package cc.azuramc.bedwars.database.storage.provider;

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
 * One row per map in bw_map, map data stored as JSON text
 *
 * @author an5w1r@163.com
 */
public class MySQLMapStorage implements IMapStorage {
    private static final String MAP_KEY = "map_name";
    private static final String MAP_DATA_KEY = "json_data";

    private final Gson gson = new GsonBuilder().create();
    private final String tableName;
    private final DataSource dataSource;

    public MySQLMapStorage(DataSource dataSource, String tableName) {
        this.dataSource = dataSource;
        this.tableName = tableName;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS " + tableName + " (\n"
                    + "  `id` INT PRIMARY KEY AUTO_INCREMENT,\n"
                    + "  `" + MAP_KEY + "` VARCHAR(255) NOT NULL,\n"
                    + "  `" + MAP_DATA_KEY + "` TEXT\n"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
        } catch (SQLException e) {
            LoggerUtil.error("Cannot create the map table " + tableName, e);
        }
    }

    @Override
    public boolean saveMap(String mapName, MapData mapData) {
        String jsonData = gson.toJson(mapData);
        String sql = exists(mapName)
                ? "UPDATE " + tableName + " SET `" + MAP_DATA_KEY + "` = ? WHERE `" + MAP_KEY + "` = ?"
                : "INSERT INTO " + tableName + " (`" + MAP_DATA_KEY + "`, `" + MAP_KEY + "`) VALUES (?, ?)";
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
             PreparedStatement ps = connection.prepareStatement("SELECT `" + MAP_DATA_KEY + "` FROM " + tableName + " WHERE `" + MAP_KEY + "` = ?")) {
            ps.setString(1, mapName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? gson.fromJson(rs.getString(MAP_DATA_KEY), MapData.class) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load map: " + mapName, e);
        }
    }

    @Override
    public boolean deleteMap(String mapName) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("DELETE FROM " + tableName + " WHERE `" + MAP_KEY + "` = ?")) {
            ps.setString(1, mapName);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete map: " + mapName, e);
        }
    }

    @Override
    public boolean exists(String mapName) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM " + tableName + " WHERE `" + MAP_KEY + "` = ? LIMIT 1")) {
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
             PreparedStatement ps = connection.prepareStatement("SELECT `" + MAP_KEY + "` FROM " + tableName);
             ResultSet rs = ps.executeQuery()) {
            List<String> names = new ArrayList<>();
            while (rs.next()) {
                names.add(rs.getString(MAP_KEY));
            }
            return names;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get all map names", e);
        }
    }
}
