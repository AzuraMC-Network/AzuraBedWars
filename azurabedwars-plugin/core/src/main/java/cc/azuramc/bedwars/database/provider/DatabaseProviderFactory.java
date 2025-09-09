package cc.azuramc.bedwars.database.provider;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.SettingsConfig;
import cc.azuramc.bedwars.database.provider.mongodb.MongoDatabaseProvider;
import cc.azuramc.bedwars.database.provider.mysql.MySQLDatabaseProvider;
import cc.azuramc.bedwars.database.repository.IDatabaseVersionRepository;
import cc.azuramc.bedwars.database.repository.IPlayerDataRepository;
import cc.azuramc.bedwars.database.repository.mongodb.MongoPlayerDataRepository;
import cc.azuramc.bedwars.database.repository.mysql.MySQLDatabaseVersionRepository;
import cc.azuramc.bedwars.database.repository.mysql.MySQLPlayerDataRepository;
import cc.azuramc.bedwars.database.service.DatabaseVersionService;
import cc.azuramc.bedwars.database.service.PlayerDataService;
import lombok.Getter;

/**
 * @author an5w1r@163.com
 */
@Getter
public class DatabaseProviderFactory {

    private final AzuraBedWars plugin;
    private IDatabaseProvider databaseProvider;

    private IPlayerDataRepository playerDataRepository;
    private IDatabaseVersionRepository databaseVersionRepository;

    private PlayerDataService playerDataService;
    private DatabaseVersionService databaseVersionService;

    public DatabaseProviderFactory(AzuraBedWars plugin) {
        this.plugin = plugin;
        createProvider(plugin);
    }

    /**
     * 创建数据库提供者
     *
     * @param plugin 插件实例
     */
    public void createProvider(AzuraBedWars plugin) {
        SettingsConfig.DatabaseConfig databaseConfig = plugin.getSettingsConfig().getDatabase();

        switch (DatabaseType.valueOf(databaseConfig.getDatabaseType().toUpperCase())) {
            case MYSQL:
                this.databaseProvider = new MySQLDatabaseProvider(plugin);
                databaseProvider.initialize();
                this.playerDataRepository = new MySQLPlayerDataRepository(plugin.getOrmClient());
                this.databaseVersionRepository = new MySQLDatabaseVersionRepository(plugin.getOrmClient());
                break;
            case MONGODB:
                this.databaseProvider = new MongoDatabaseProvider(plugin, databaseConfig);
                databaseProvider.initialize();
                this.playerDataRepository = new MongoPlayerDataRepository();
                break;
            default:
                break;
        }

        playerDataService = new PlayerDataService(playerDataRepository);
        databaseVersionService = new DatabaseVersionService(databaseVersionRepository);
    }
}
