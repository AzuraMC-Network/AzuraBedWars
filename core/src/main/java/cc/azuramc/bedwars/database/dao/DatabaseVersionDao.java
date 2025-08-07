package cc.azuramc.bedwars.database.dao;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.entity.DatabaseVersionTableKey;
import cc.azuramc.orm.AzuraOrmClient;
import cc.azuramc.orm.builder.DataType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author An5w1r@163.com
 */
public class DatabaseVersionDao {

    private final AzuraOrmClient ormClient;

    public DatabaseVersionDao(AzuraBedWars plugin) {
        this.ormClient = plugin.getOrmClient();
    }

    /**
     * 创建数据库版本表
     */
    public void createDatabaseVersionTable() throws SQLException {
        try (Connection conn = ormClient.getConnection()) {
            PreparedStatement createTableStmt = ormClient.createTable(conn)
                    .createTable(DatabaseVersionTableKey.tableName)
                    .ifNotExists()
                    .addIdColumn()
                    .column(DatabaseVersionTableKey.version, DataType.Type.INT.getSql(), DataType.DEFAULT(1))
                    .engine("InnoDB")
                    .charset("utf8mb4")
                    .collate("utf8mb4_unicode_ci")
                    .prepare();

            createTableStmt.executeUpdate();
            createTableStmt.close();
        }
    }

    /**
     * 获取当前数据库版本
     *
     * @return 当前版本号，如果没有记录则返回-1
     */
    public int getCurrentVersion() throws SQLException {
        try (Connection conn = ormClient.getConnection()) {
            PreparedStatement selectStmt = ormClient.select(conn)
                    .from(DatabaseVersionTableKey.tableName)
                    .select(DatabaseVersionTableKey.version)
                    .limit(1)
                    .prepare();

            ResultSet resultSet = selectStmt.executeQuery();
            int version = -1;

            if (resultSet.next()) {
                version = resultSet.getInt(DatabaseVersionTableKey.version);
            }

            resultSet.close();
            selectStmt.close();

            return version;
        }
    }

    /**
     * 插入新的版本记录
     *
     * @param version 版本号
     */
    public void insertVersion(int version) throws SQLException {
        try (Connection conn = ormClient.getConnection()) {
            PreparedStatement insertStmt = ormClient.insert(conn)
                    .insertInto(DatabaseVersionTableKey.tableName)
                    .values(DatabaseVersionTableKey.version, version)
                    .prepare();

            insertStmt.executeUpdate();
            insertStmt.close();
        }
    }

    /**
     * 更新版本记录（更新第一条记录）
     *
     * @param version 版本号
     */
    public void updateVersion(int version) throws SQLException {
        try (Connection conn = ormClient.getConnection()) {
            PreparedStatement updateStmt = ormClient.update(conn)
                    .update(DatabaseVersionTableKey.tableName)
                    .set(DatabaseVersionTableKey.version, version)
                    .whereEquals("id", "1")
                    .prepare();

            updateStmt.executeUpdate();
            updateStmt.close();
        }
    }

    /**
     * 检查版本表是否存在记录
     *
     * @return 如果存在记录返回true，否则返回false
     */
    public boolean hasVersionRecord() throws SQLException {
        try (Connection conn = ormClient.getConnection()) {
            PreparedStatement selectStmt = ormClient.select(conn)
                    .from(DatabaseVersionTableKey.tableName)
                    .select(DatabaseVersionTableKey.version)
                    .limit(1)
                    .prepare();

            ResultSet resultSet = selectStmt.executeQuery();
            boolean hasRecord = resultSet.next();

            resultSet.close();
            selectStmt.close();

            return hasRecord;
        }
    }
}
