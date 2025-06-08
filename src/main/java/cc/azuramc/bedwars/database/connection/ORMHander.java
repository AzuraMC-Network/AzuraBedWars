package cc.azuramc.bedwars.database.connection;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.SettingsConfig;
import cc.azuramc.orm.AzuraORM;
import cc.azuramc.orm.AzuraOrmClient;
import cc.azuramc.orm.config.DatabaseConfig;
import lombok.Getter;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author an5w1r@163.com
 */
public class ORMHander {

    @Getter private AzuraOrmClient ormClient;

    public ORMHander() {
        SettingsConfig.DatabaseConfig database = AzuraBedWars.getInstance().getSettingsConfig().getDatabase();

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
                .setAutoCommit(false);

        ormClient = new AzuraOrmClient("AzuraBedWars");
        ormClient.initialize(config);
    }

    public Connection getConnection() throws SQLException {
        return ormClient.getConnection();
    }

    public void shutdown() {
        AzuraORM.shutdownAll();
    }
}
