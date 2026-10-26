package cc.azuramc.bedwars.database.provider.mysql;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.DatabaseConfig;
import cc.azuramc.bedwars.database.provider.DatabaseType;
import cc.azuramc.bedwars.database.provider.IDatabaseProvider;
import cc.azuramc.bedwars.database.repository.IDatabaseVersionRepository;
import cc.azuramc.bedwars.database.repository.IPlayerDataRepository;
import cc.azuramc.bedwars.database.repository.mysql.MySQLDatabaseVersionRepository;
import cc.azuramc.bedwars.database.repository.mysql.MySQLPlayerDataRepository;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

/**
 * @author an5w1r@163.com
 */
@Getter
public class MySQLDatabaseProvider implements IDatabaseProvider {

    private static final String JDBC_URL_TEMPLATE = "jdbc:mysql://%s:%s/%s?useSSL=false&createDatabaseIfNotExist=true";

    private final AzuraBedWars plugin;
    private HikariDataSource dataSource;

    public MySQLDatabaseProvider(AzuraBedWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean initialize() {
        try {
            DatabaseConfig database = plugin.getDatabaseConfig();
            String jdbcUrl = String.format(JDBC_URL_TEMPLATE,
                    database.getHost(),
                    database.getPort(),
                    database.getDatabase()
            );

            HikariConfig config = new HikariConfig();
            // DriverManager cannot see drivers inside the isolated class loader
            config.setDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource");
            config.addDataSourceProperty("url", jdbcUrl);
            config.setUsername(database.getUsername());
            config.setPassword(database.getPassword());
            config.setMaximumPoolSize(25);
            config.setMinimumIdle(5);
            config.setConnectionTimeout(10000L);
            config.setIdleTimeout(300000L);
            config.setMaxLifetime(900000L);
            config.setLeakDetectionThreshold(30000L);
            config.setPoolName("AzuraBedWars-MySQL-Pool");
            config.setRegisterMbeans(true);
            dataSource = new HikariDataSource(config);

            LoggerUtil.info("Connected to MySQL");
            return true;
        } catch (Exception e) {
            LoggerUtil.error("Cannot connect to MySQL: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void shutdown() {
        if (dataSource != null) {
            try {
                dataSource.close();
                LoggerUtil.info("Closed the MySQL connection");
            } catch (Exception e) {
                LoggerUtil.error("Error while closing the MySQL connection: " + e.getMessage());
            }
        }
    }

    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.MYSQL;
    }

    @Override
    public IPlayerDataRepository createPlayerDataRepository() {
        return new MySQLPlayerDataRepository(dataSource);
    }

    @Override
    public IDatabaseVersionRepository createDatabaseVersionRepository() {
        return new MySQLDatabaseVersionRepository(dataSource);
    }
}
