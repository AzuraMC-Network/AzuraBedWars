package cc.azuramc.bedwars.database.provider;

import cc.azuramc.bedwars.database.repository.IDatabaseVersionRepository;
import cc.azuramc.bedwars.database.repository.IPlayerDataRepository;

/**
 * @author an5w1r@163.com
 */
public interface IDatabaseProvider {

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

    /**
     * 创建玩家数据仓库
     *
     * @return 玩家数据仓库实例
     */
    IPlayerDataRepository createPlayerDataRepository();

    /**
     * 创建数据库版本仓库
     *
     * @return 数据库版本仓库实例
     */
    IDatabaseVersionRepository createDatabaseVersionRepository();
}
