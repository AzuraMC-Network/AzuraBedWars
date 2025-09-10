package cc.azuramc.bedwars.database.provider.mongodb;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.SettingsConfig;
import cc.azuramc.bedwars.database.provider.DatabaseType;
import cc.azuramc.bedwars.database.provider.IDatabaseProvider;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.bson.UuidRepresentation;

import java.util.concurrent.TimeUnit;

/**
 * @author Irina
 */
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
    public boolean initialize() {
        try {
            String url;
            if (databaseConfig.getUsername() != null && !databaseConfig.getUsername().isEmpty() &&
                    databaseConfig.getPassword() != null && !databaseConfig.getPassword().isEmpty()) {
                url = "mongodb://" +
                        databaseConfig.getUsername() + ":" +
                        databaseConfig.getPassword() + "@" +
                        databaseConfig.getHost() + ":" +
                        databaseConfig.getPort() + "/" +
                        databaseConfig.getDatabase();
            } else {
                url = "mongodb://" +
                        databaseConfig.getHost() + ":" +
                        databaseConfig.getPort() + "/" +
                        databaseConfig.getDatabase();
            }

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(url))
                    .applyToConnectionPoolSettings(pool -> {
                        pool.maxSize(50);
                        pool.minSize(5);
                        pool.maxWaitTime(30, TimeUnit.SECONDS);
                        pool.maxConnectionLifeTime(900, TimeUnit.SECONDS);
                        pool.maxConnectionIdleTime(300, TimeUnit.SECONDS);
                    })
                    .uuidRepresentation(UuidRepresentation.STANDARD)
                    .applicationName("AzuraBedWars-MongoDB-Provider")
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
