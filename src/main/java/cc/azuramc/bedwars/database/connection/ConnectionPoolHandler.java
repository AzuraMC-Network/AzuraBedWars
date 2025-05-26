package cc.azuramc.bedwars.database.connection;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.SettingsConfig;
import cc.azuramc.bedwars.database.DatabaseConstants;
import cc.azuramc.bedwars.database.dao.DatabaseManager;
import cc.azuramc.bedwars.database.query.QueryBuilder;
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
    private boolean initialized = false;

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
            config.setMaximumPoolSize(DatabaseConstants.POOL_MAX_SIZE);
            config.setMinimumIdle(DatabaseConstants.POOL_MIN_IDLE);
            config.setIdleTimeout(DatabaseConstants.POOL_IDLE_TIMEOUT);
            config.setConnectionTimeout(DatabaseConstants.POOL_CONNECTION_TIMEOUT);
            config.setMaxLifetime(DatabaseConstants.POOL_MAX_LIFETIME);
            
            // 创建数据源
            this.dataSource = new HikariDataSource(config);
            
            // 标记初始化完成
            this.initialized = true;
            
            // 注意：不再在构造函数中调用createTables()
            // 在AzuraBedWars主类中完成ConnectionPoolHandler初始化后调用
            
        } catch (SQLException e) {
            Bukkit.getLogger().severe("创建数据库 " + database.getDatabase() + " 失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 创建所需的数据库表
     */
    public void createTables() {
        if (!initialized || dataSource == null) {
            Bukkit.getLogger().severe("数据源未初始化，无法创建数据表");
            return;
        }
        
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // 创建玩家统计表
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + AzuraBedWars.PLAYER_DATA_TABLE + " (" 
                + DatabaseConstants.PLAYER_STATS_TABLE_DEFINITION + ")");
            
            // 创建玩家商店设置表
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + AzuraBedWars.PLAYER_SHOP_TABLE + " (" 
                + DatabaseConstants.PLAYER_SHOP_TABLE_DEFINITION + ")");
            
            // 创建观战者设置表
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + AzuraBedWars.SPECTATOR_SETTINGS_TABLE + " (" 
                + DatabaseConstants.SPECTATOR_TABLE_DEFINITION + ")");
            
            // 创建地图表
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + AzuraBedWars.MAP_TABLE_NAME + " (" 
                + DatabaseConstants.MAP_TABLE_DEFINITION + ")");
            
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
        if (!initialized || dataSource == null) {
            throw new SQLException("数据源未初始化");
        }
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
