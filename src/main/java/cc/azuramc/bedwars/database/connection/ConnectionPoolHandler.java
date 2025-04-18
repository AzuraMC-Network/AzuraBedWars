package cc.azuramc.bedwars.database.connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionPoolHandler {
    private final Map<String, HikariDataSource> pools = new HashMap<>();
    private final List<String> databases = new ArrayList<>();

    public ConnectionPoolHandler() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        loadPools();
    }

    public void loadPools() {
        for (String database : databases) {
            if (pools.containsKey(database)) {
                continue;
            }

            // 首先尝试连接到MySQL服务器（不指定数据库）
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://localhost/");
            config.setUsername("root");
            config.setPassword("s*6tlO68FnEbyBn4");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            try (HikariDataSource tempDataSource = new HikariDataSource(config);
                 Connection connection = tempDataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                
                // 尝试创建数据库
                statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + database);
                Bukkit.getLogger().info("成功创建数据库: " + database);
                
                // 关闭临时连接
                tempDataSource.close();
                
                // 创建新的连接池（指定数据库）
                config.setJdbcUrl("jdbc:mysql://localhost/" + database);
                HikariDataSource hikariDataSource = new HikariDataSource(config);
                pools.put(database, hikariDataSource);
                
            } catch (Exception e) {
                Bukkit.getLogger().severe("创建数据库 " + database + " 失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void registerDatabase(String databaseName) {
        if (databases.contains(databaseName)) {
            return;
        }

        databases.add(databaseName);
        loadPools();
    }

    public void unregisterDatabase(String databaseName) {
        databases.remove(databaseName);
    }

    public Connection getConnection(String databaseName) {
        try {
            HikariDataSource dataSource = pools.get(databaseName);
            if (dataSource == null) {
                return null;
            }

            return dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void closeAll() {
        pools.values().forEach(HikariDataSource::close);
    }
}
