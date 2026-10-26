package cc.azuramc.bedwars.database.provider.mongodb;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.DatabaseConfig;
import cc.azuramc.bedwars.database.provider.DatabaseType;
import cc.azuramc.bedwars.database.provider.IDatabaseProvider;
import cc.azuramc.bedwars.database.repository.IDatabaseVersionRepository;
import cc.azuramc.bedwars.database.repository.IPlayerDataRepository;
import cc.azuramc.bedwars.database.repository.mongodb.MongoDatabaseVersionRepository;
import cc.azuramc.bedwars.database.repository.mongodb.MongoPlayerDataRepository;
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

    private static final String MONGO_URL_WITH_AUTH = "mongodb://%s:%s@%s:%s/%s";
    private static final String MONGO_URL_NO_AUTH = "mongodb://%s:%s/%s";

    private final AzuraBedWars plugin;
    private final DatabaseConfig databaseConfig;

    private boolean initialized = false;
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    public MongoDatabaseProvider(AzuraBedWars plugin, DatabaseConfig databaseConfig) {
        this.plugin = plugin;
        this.databaseConfig = databaseConfig;
    }

    @Override
    public boolean initialize() {
        try {
            String url = buildConnectionUrl();

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
            LoggerUtil.info("Connected to MongoDB");
            return true;
        } catch (Exception e) {
            LoggerUtil.error("Cannot connect to MongoDB: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String buildConnectionUrl() {
        boolean hasAuth = databaseConfig.getUsername() != null && !databaseConfig.getUsername().isEmpty()
                && databaseConfig.getPassword() != null && !databaseConfig.getPassword().isEmpty();

        if (hasAuth) {
            return String.format(MONGO_URL_WITH_AUTH,
                    databaseConfig.getUsername(),
                    databaseConfig.getPassword(),
                    databaseConfig.getHost(),
                    databaseConfig.getPort(),
                    databaseConfig.getDatabase()
            );
        } else {
            return String.format(MONGO_URL_NO_AUTH,
                    databaseConfig.getHost(),
                    databaseConfig.getPort(),
                    databaseConfig.getDatabase()
            );
        }
    }

    @Override
    public void shutdown() {
        if (mongoClient == null) return;
        try {
            mongoClient.close();
            LoggerUtil.info("Closed the MongoDB connection");
        } catch (Exception e) {
            LoggerUtil.error("Error while closing the MongoDB connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.MONGODB;
    }

    @Override
    public IPlayerDataRepository createPlayerDataRepository() {
        return new MongoPlayerDataRepository(this);
    }

    @Override
    public IDatabaseVersionRepository createDatabaseVersionRepository() {
        return new MongoDatabaseVersionRepository(this);
    }
}
