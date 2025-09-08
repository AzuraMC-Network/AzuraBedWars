package cc.azuramc.bedwars.database;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.provider.DatabaseProviderFactory;
import cc.azuramc.bedwars.database.provider.IDatabaseProvider;
import cc.azuramc.bedwars.database.repository.IDatabaseVersionRepository;
import cc.azuramc.bedwars.database.repository.IPlayerDataRepository;
import cc.azuramc.bedwars.database.repository.impl.MySQLDatabaseVersionRepository;
import cc.azuramc.bedwars.database.repository.impl.MySQLPlayerDataRepository;
import cc.azuramc.bedwars.database.service.DatabaseVersionService;
import cc.azuramc.bedwars.database.service.PlayerDataService;

/**
 * 数据库服务工厂
 * 负责创建和管理数据库相关的服务和仓库
 *
 * @author an5w1r@163.com
 */
public class DatabaseServiceFactory {

    private static PlayerDataService playerDataService;
    private static DatabaseVersionService databaseVersionService;
    private static IPlayerDataRepository playerDataRepository;
    private static IDatabaseVersionRepository databaseVersionRepository;

    /**
     * 初始化所有数据库服务
     *
     * @param plugin 插件实例
     */
    public static void initializeServices(AzuraBedWars plugin) {
        // 获取数据库提供者
        IDatabaseProvider databaseProvider = DatabaseProviderFactory.getProvider(plugin);

        // 初始化数据库提供者
        if (!databaseProvider.initialize()) {
            throw new RuntimeException("Failed to initialize database provider");
        }

        // 创建仓库实例
        playerDataRepository = new MySQLPlayerDataRepository(databaseProvider);
        databaseVersionRepository = new MySQLDatabaseVersionRepository(databaseProvider);

        // 创建服务实例
        playerDataService = new PlayerDataService(playerDataRepository);
        databaseVersionService = new DatabaseVersionService(databaseVersionRepository);
    }

    /**
     * 获取玩家数据服务
     *
     * @return 玩家数据服务实例
     */
    public static PlayerDataService getPlayerDataService() {
        if (playerDataService == null) {
            throw new IllegalStateException("Database services not initialized");
        }
        return playerDataService;
    }

    /**
     * 获取数据库版本服务
     *
     * @return 数据库版本服务实例
     */
    public static DatabaseVersionService getDatabaseVersionService() {
        if (databaseVersionService == null) {
            throw new IllegalStateException("Database services not initialized");
        }
        return databaseVersionService;
    }

    /**
     * 获取玩家数据仓库
     *
     * @return 玩家数据仓库实例
     */
    public static IPlayerDataRepository getPlayerDataRepository() {
        if (playerDataRepository == null) {
            throw new IllegalStateException("Database services not initialized");
        }
        return playerDataRepository;
    }

    /**
     * 获取数据库版本仓库
     *
     * @return 数据库版本仓库实例
     */
    public static IDatabaseVersionRepository getDatabaseVersionRepository() {
        if (databaseVersionRepository == null) {
            throw new IllegalStateException("Database services not initialized");
        }
        return databaseVersionRepository;
    }

    /**
     * 获取数据库提供者（用于向后兼容）
     *
     * @return 数据库提供者实例
     */
    public static IDatabaseProvider getDatabaseProvider() {
        return DatabaseProviderFactory.getProvider(null);
    }

    /**
     * 关闭所有数据库服务
     */
    public static void shutdown() {
        if (playerDataService != null) {
            playerDataService.shutdown();
            playerDataService = null;
        }

        if (databaseVersionService != null) {
            databaseVersionService = null;
        }

        if (playerDataRepository != null) {
            playerDataRepository = null;
        }

        if (databaseVersionRepository != null) {
            databaseVersionRepository = null;
        }

        DatabaseProviderFactory.reset();
    }
}
