package cc.azuramc.bedwars.database.provider;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.SettingsConfig;
import cc.azuramc.bedwars.database.provider.mongodb.MongoDatabaseProvider;
import cc.azuramc.bedwars.database.provider.mysql.MySQLDatabaseProvider;
import cc.azuramc.bedwars.database.repository.IDatabaseVersionRepository;
import cc.azuramc.bedwars.database.repository.IPlayerDataRepository;
import cc.azuramc.bedwars.database.service.DatabaseVersionService;
import cc.azuramc.bedwars.database.service.PlayerDataService;
import lombok.Getter;

/**
 * 数据库提供者工厂
 *
 * @author an5w1r@163.com
 */
@Getter
public class DatabaseProviderFactory {

    private final AzuraBedWars plugin;
    private final IDatabaseProvider databaseProvider;

    private final IPlayerDataRepository playerDataRepository;
    private final IDatabaseVersionRepository databaseVersionRepository;

    private final PlayerDataService playerDataService;
    private final DatabaseVersionService databaseVersionService;

    public DatabaseProviderFactory(AzuraBedWars plugin) {
        this.plugin = plugin;

        this.databaseProvider = createProvider(plugin);
        databaseProvider.initialize();

        this.playerDataRepository = databaseProvider.createPlayerDataRepository();
        this.databaseVersionRepository = databaseProvider.createDatabaseVersionRepository();

        this.playerDataService = new PlayerDataService(playerDataRepository);
        this.databaseVersionService = new DatabaseVersionService(databaseVersionRepository);
    }

    /**
     * 根据配置创建对应的数据库提供者
     *
     * @param plugin 插件实例
     * @return 数据库提供者
     */
    private IDatabaseProvider createProvider(AzuraBedWars plugin) {
        SettingsConfig.DatabaseConfig config = plugin.getSettingsConfig().getDatabase();
        DatabaseType type = DatabaseType.valueOf(config.getDatabaseType().toUpperCase());

        return switch (type) {
            case MYSQL -> new MySQLDatabaseProvider(plugin);
            case MONGODB -> new MongoDatabaseProvider(plugin, config);
            default -> throw new IllegalArgumentException("Unsupported database type: " + type);
        };
    }
}
