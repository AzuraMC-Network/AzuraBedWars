package cc.azuramc.bedwars.database.repository;

import cc.azuramc.bedwars.database.entity.DatabaseVersion;

import java.sql.SQLException;

/**
 * @author An5w1r@163.com
 */
public interface IDatabaseVersionRepository {

    /**
     * 创建数据库版本表
     */
    void createTable();

    /**
     * 获取当前数据库版本
     *
     * @return 当前版本号，如果没有记录则返回-1
     * @throws SQLException SQL异常
     */
    int getCurrentVersion() throws SQLException;

    /**
     * 查询数据库版本记录
     *
     * @return DatabaseVersion对象，如果没有记录则返回null
     */
    DatabaseVersion selectDatabaseVersion();

    /**
     * 插入新的版本记录
     *
     * @param version 版本号
     * @throws SQLException SQL异常
     */
    void insertVersion(int version) throws SQLException;

    /**
     * 插入数据库版本记录
     *
     * @param databaseVersion 数据库版本对象
     */
    void insertDatabaseVersion(DatabaseVersion databaseVersion);

    /**
     * 更新版本记录（更新第一条记录）
     *
     * @param version 版本号
     * @throws SQLException SQL异常
     */
    void updateVersion(int version) throws SQLException;

    /**
     * 更新数据库版本记录
     *
     * @param databaseVersion 数据库版本对象
     */
    void updateDatabaseVersion(DatabaseVersion databaseVersion);

    /**
     * 检查版本表是否存在记录
     *
     * @return 如果存在记录返回true，否则返回false
     * @throws SQLException SQL异常
     */
    boolean hasVersionRecord() throws SQLException;
}
