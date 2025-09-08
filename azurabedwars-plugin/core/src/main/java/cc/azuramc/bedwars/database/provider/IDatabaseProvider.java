package cc.azuramc.bedwars.database.provider;

import cc.azuramc.orm.AzuraOrmClient;

/**
 * @author an5w1r@163.com
 */
public interface IDatabaseProvider {

    /**
     * 获取数据库连接客户端
     *
     * @return AzuraOrmClient实例
     */
    AzuraOrmClient getOrmClient();

    /**
     * 初始化数据库连接
     *
     * @return 是否初始化成功
     */
    boolean initialize();

    /**
     * 关闭数据库连接
     */
    void shutdown();

    /**
     * 获取数据库类型
     *
     * @return 数据库类型
     */
    DatabaseType getDatabaseType();
}
