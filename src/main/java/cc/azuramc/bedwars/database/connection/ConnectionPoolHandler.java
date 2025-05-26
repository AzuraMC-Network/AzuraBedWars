package cc.azuramc.bedwars.database.connection;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.SettingsConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库连接池处理器
 * 使用HikariCP连接池管理数据库连接
 *
 * @author an5w1r@163.com
 */
@Getter
public class ConnectionPoolHandler {
    private HikariDataSource dataSource;

    public ConnectionPoolHandler() {
        SettingsConfig.DatabaseConfig database = AzuraBedWars.getInstance().getSettingsConfig().getDatabase();
        
        // 首先尝试连接到MySQL服务器（不指定数据库）
        HikariConfig tempConfig = new HikariConfig();
        tempConfig.setJdbcUrl(String.format("jdbc:mysql://%s:%d/", 
            database.getHost(),
            database.getPort()));
        tempConfig.setUsername(database.getUsername());
        tempConfig.setPassword(database.getPassword());
        
        try (HikariDataSource tempDataSource = new HikariDataSource(tempConfig);
             Connection connection = tempDataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // 尝试创建数据库
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + database.getDatabase());
            
            // 关闭临时连接
            tempDataSource.close();

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s", 
                database.getHost(),
                database.getPort(),
                database.getDatabase()));
            config.setUsername(database.getUsername());
            config.setPassword(database.getPassword());
            
            // 连接池配置
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(5);
            config.setIdleTimeout(300000);
            config.setConnectionTimeout(20000);
            config.setMaxLifetime(1200000);
            
            // 创建数据源
            this.dataSource = new HikariDataSource(config);
            
            // 创建所需的表
            createTables();
            
        } catch (SQLException e) {
            Bukkit.getLogger().severe("创建数据库 " + database.getDatabase() + " 失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 创建所需的数据库表
     */
    private void createTables() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // 创建玩家统计表
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS " + AzuraBedWars.PLAYER_DATA_TABLE + " (" +
                "name VARCHAR(36) PRIMARY KEY," +
                "mode VARCHAR(20) NOT NULL," +
                "kills INT DEFAULT 0," +
                "deaths INT DEFAULT 0," +
                "destroyedBeds INT DEFAULT 0," +
                "wins INT DEFAULT 0," +
                "loses INT DEFAULT 0," +
                "games INT DEFAULT 0," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")"
            );

            // 创建玩家商店设置表
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS " + AzuraBedWars.PLAYER_SHOP_TABLE + " (" +
                "name VARCHAR(36) PRIMARY KEY," +
                "data TEXT NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "FOREIGN KEY (name) REFERENCES bw_players_stats(name) ON DELETE CASCADE" +
                ")"
            );

            // 创建观战者设置表
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS " + AzuraBedWars.SPECTATOR_SETTINGS_TABLE + " (" +
                "name VARCHAR(36) PRIMARY KEY," +
                "speed INT DEFAULT 0," +
                "autoTp BOOLEAN DEFAULT false," +
                "nightVision BOOLEAN DEFAULT false," +
                "firstPerson BOOLEAN DEFAULT true," +
                "hideOther BOOLEAN DEFAULT false," +
                "fly BOOLEAN DEFAULT false," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "FOREIGN KEY (name) REFERENCES bw_players_stats(name) ON DELETE CASCADE" +
                ")"
            );

            // 创建地图表
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS " + AzuraBedWars.MAP_TABLE_NAME + " (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(32) NOT NULL UNIQUE," +
                "display_name VARCHAR(64) NOT NULL," +
                "min_players INT NOT NULL," +
                "max_players INT NOT NULL," +
                "teams INT NOT NULL," +
                "data TEXT NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")"
            );

        } catch (SQLException e) {
            Bukkit.getLogger().severe("创建数据库表失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取数据库连接
     * 
     * @return 数据库连接
     * @throws SQLException SQL异常
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * 关闭连接池
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
