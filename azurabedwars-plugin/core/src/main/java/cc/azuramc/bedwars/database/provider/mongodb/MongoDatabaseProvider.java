package cc.azuramc.bedwars.database.provider.mongodb;

/*
 * @Author Irina
 * @Date 2025/9/9 13:44
 */

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.SettingsConfig;
import cc.azuramc.bedwars.database.provider.DatabaseType;
import cc.azuramc.bedwars.database.provider.IDatabaseProvider;
import cc.azuramc.bedwars.util.LoggerUtil;
import cc.azuramc.orm.AzuraOrmClient;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Getter
public class MongoDatabaseProvider implements IDatabaseProvider {

    private final AzuraBedWars plugin;
    private final SettingsConfig.DatabaseConfig databaseConfig;

    private boolean initialized = false;
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    public MongoDatabaseProvider(AzuraBedWars plugin, SettingsConfig.DatabaseConfig databaseConfig) {
        this.plugin = plugin;
        this.databaseConfig = databaseConfig;
    }

    @Override
    public AzuraOrmClient getOrmClient() {
        if (!initialized) {
            throw new IllegalStateException("Database provider not initialized");
        }
        return null;
    }

    @Override
    public boolean initialize() {
        try {
//            DatabaseConfig config = new DatabaseConfig()
//                    .setUrl("mongodb://" + database.getHost() + ":" + database.getPort() + "/" + database.getDatabase())
//                    .setUsername(database.getUsername())
//                    .setPassword(database.getPassword())
//                    .setMaximumPoolSize(50)
//                    .setMinPoolSize(5);

            String url = "mongodb://" +
                    databaseConfig.getUsername() + ":" +
                    databaseConfig.getPassword() + "@" +
                    databaseConfig.getHost() + ":" +
                    databaseConfig.getPort() + "/" +
                    databaseConfig.getDatabase();

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(url))
                    .applyToConnectionPoolSettings(pool -> {
                        pool.maxSize(50);
                        pool.minSize(5);
                        pool.maxWaitTime(30, TimeUnit.SECONDS);
                        pool.maxConnectionLifeTime(900, TimeUnit.SECONDS);
                        pool.maxConnectionIdleTime(300, TimeUnit.SECONDS);
                    })
                    .applicationName("AzuraBedWars-Pool")
                    .build();

            mongoClient = MongoClients.create(settings);
            mongoDatabase = mongoClient.getDatabase(databaseConfig.getDatabase());

            initialized = true;
            LoggerUtil.info("MongoDB 数据库连接初始化成功");
            return true;
        } catch (Exception e) {
            LoggerUtil.error("MongoDB 数据库连接初始化失败　" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void shutdown() {
        if (mongoClient == null) return;
        try {
            mongoClient.close();
            LoggerUtil.info("MongoDB 数据库连接关闭成功");
        } catch (Exception e) {
            LoggerUtil.error("MongoDB 数据库连接在关闭时出现了一项错误　" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.MONGODB;
    }
}
