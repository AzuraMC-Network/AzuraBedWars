package cc.azuramc.bedwars.database.provider.mysql;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.SettingsConfig;
import cc.azuramc.bedwars.database.provider.DatabaseType;
import cc.azuramc.bedwars.database.provider.IDatabaseProvider;
import cc.azuramc.bedwars.database.repository.IDatabaseVersionRepository;
import cc.azuramc.bedwars.database.repository.IPlayerDataRepository;
import cc.azuramc.bedwars.database.repository.mysql.MySQLDatabaseVersionRepository;
import cc.azuramc.bedwars.database.repository.mysql.MySQLPlayerDataRepository;
import cc.azuramc.bedwars.util.LoggerUtil;
import cc.azuramc.orm.AzuraORM;
import cc.azuramc.orm.AzuraOrmClient;
import cc.azuramc.orm.config.DatabaseConfig;
import lombok.Getter;

/**
 * @author an5w1r@163.com
 */
@Getter
public class MySQLDatabaseProvider implements IDatabaseProvider {

    private static final String JDBC_URL_TEMPLATE = "jdbc:mysql://%s:%s/%s";

    private final AzuraBedWars plugin;
    private AzuraOrmClient ormClient;

    public MySQLDatabaseProvider(AzuraBedWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean initialize() {
        try {
            SettingsConfig.DatabaseConfig database = plugin.getSettingsConfig().getDatabase();
            String jdbcUrl = JDBC_URL_TEMPLATE.formatted(
                    database.getHost(),
                    database.getPort(),
                    database.getDatabase()
            );

            DatabaseConfig config = new DatabaseConfig()
                    .setUrl(jdbcUrl)
                    .setUsername(database.getUsername())
                    .setPassword(database.getPassword())
                    .setMaximumPoolSize(25)
                    .setMinimumIdle(5)
                    .setConnectionTimeout(10000L)
                    .setIdleTimeout(300000L)
                    .setMaxLifetime(900000L)
                    .setLeakDetectionThreshold(30000L)
                    .setPoolName("AzuraBedWars-MySQL-Pool")
                    .setRegisterMbeans(true)
                    .setUseSSL(false)
                    .setAutoCommit(true);

            AzuraORM.initialize(config, true);
            ormClient = AzuraORM.getClient();

            LoggerUtil.info("MySQL数据库连接初始化成功");
            return true;
        } catch (Exception e) {
            LoggerUtil.error("MySQL数据库连接初始化失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void shutdown() {
        if (ormClient != null) {
            try {
                ormClient.close();
                LoggerUtil.info("MySQL数据库连接已关闭");
            } catch (Exception e) {
                LoggerUtil.error("关闭MySQL数据库连接时出错: " + e.getMessage());
            }
        }
    }

    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.MYSQL;
    }

    @Override
    public IPlayerDataRepository createPlayerDataRepository() {
        return new MySQLPlayerDataRepository(ormClient);
    }

    @Override
    public IDatabaseVersionRepository createDatabaseVersionRepository() {
        return new MySQLDatabaseVersionRepository(ormClient);
    }
}
