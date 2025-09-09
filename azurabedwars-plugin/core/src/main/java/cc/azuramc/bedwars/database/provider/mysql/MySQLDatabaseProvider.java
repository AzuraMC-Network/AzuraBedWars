package cc.azuramc.bedwars.database.provider.mysql;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.SettingsConfig;
import cc.azuramc.bedwars.database.provider.DatabaseType;
import cc.azuramc.bedwars.database.provider.IDatabaseProvider;
import cc.azuramc.bedwars.util.LoggerUtil;
import cc.azuramc.orm.AzuraORM;
import cc.azuramc.orm.AzuraOrmClient;
import cc.azuramc.orm.config.DatabaseConfig;

/**
 * @author an5w1r@163.com
 */
public class MySQLDatabaseProvider implements IDatabaseProvider {

    private final AzuraBedWars plugin;
    private AzuraOrmClient ormClient;

    public MySQLDatabaseProvider(AzuraBedWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean initialize() {
        try {
            SettingsConfig.DatabaseConfig database = plugin.getSettingsConfig().getDatabase();
            DatabaseConfig config = new DatabaseConfig()
                    .setUrl("jdbc:mysql://" + database.getHost() + ":"
                            + database.getPort() + "/" + database.getDatabase())
                    .setUsername(database.getUsername())
                    .setPassword(database.getPassword())
                    .setMaximumPoolSize(25)
                    .setMinimumIdle(5)
                    .setConnectionTimeout(10000L)
                    .setIdleTimeout(300000L)
                    .setMaxLifetime(900000L)
                    .setLeakDetectionThreshold(30000L)
                    .setPoolName("AzuraBedWars-Pool")
                    .setRegisterMbeans(true)
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
}
