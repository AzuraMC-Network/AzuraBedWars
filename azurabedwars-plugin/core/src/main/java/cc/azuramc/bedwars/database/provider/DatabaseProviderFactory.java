package cc.azuramc.bedwars.database.provider;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.SettingsConfig;
import cc.azuramc.bedwars.database.provider.mongodb.MongoDatabaseProvider;
import cc.azuramc.bedwars.database.provider.mysql.MySQLDatabaseProvider;

/**
 * @author an5w1r@163.com
 */
public class DatabaseProviderFactory {

    private static IDatabaseProvider currentProvider;

    /**
     * 获取当前数据库提供者
     *
     * @param plugin 插件实例
     * @return 数据库提供者实例
     */
    public static IDatabaseProvider getProvider(AzuraBedWars plugin) {
        if (currentProvider == null) {
            currentProvider = createProvider(plugin);
        }
        return currentProvider;
    }

    /**
     * 创建数据库提供者
     *
     * @param plugin 插件实例
     * @return 数据库提供者实例
     */
    private static IDatabaseProvider createProvider(AzuraBedWars plugin) {
        SettingsConfig.DatabaseConfig db = plugin.getSettingsConfig().getDatabase();

        return switch (DatabaseType.valueOf(db.getDatabaseType().toUpperCase())) {
            case MYSQL -> new MySQLDatabaseProvider(plugin);
            case MONGODB -> new MongoDatabaseProvider(plugin, db);
            default -> null;
        };
    }

    /**
     * 设置数据库提供者（用于测试或特殊场景）
     *
     * @param provider 数据库提供者实例
     */
    public static void setProvider(IDatabaseProvider provider) {
        currentProvider = provider;
    }

    /**
     * 获取当前数据库类型
     *
     * @return 数据库类型名称
     */
    public static DatabaseType getCurrentDatabaseType() {
        return currentProvider != null ? currentProvider.getDatabaseType() : DatabaseType.UNKNOWN;
    }
}
