package cc.azuramc.bedwars.database.connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
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

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://localhost/" + database);
            config.setUsername("root");
            config.setPassword("s*6tlO68FnEbyBn4");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            try {
                HikariDataSource hikariDataSource = new HikariDataSource(config);
                pools.put(database, hikariDataSource);
            } catch (Exception e) {
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
