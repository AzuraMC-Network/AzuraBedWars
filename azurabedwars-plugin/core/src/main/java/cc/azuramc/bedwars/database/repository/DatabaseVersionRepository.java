package cc.azuramc.bedwars.database.repository;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.entity.DatabaseVersion;
import cc.azuramc.bedwars.database.entity.DatabaseVersionTableKey;
import cc.azuramc.orm.AzuraOrmClient;
import cc.azuramc.orm.builder.ColumnDefinitionBuilder;
import cc.azuramc.orm.builder.DataType;
import cc.azuramc.orm.builder.PreparedStatementBuildManager;
import cc.azuramc.orm.mapper.ResultMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

/**
 * @author An5w1r@163.com
 */
public class DatabaseVersionRepository {

    private final AzuraOrmClient ormClient;

    public DatabaseVersionRepository(AzuraBedWars plugin) {
        this.ormClient = plugin.getOrmClient();
    }

    /**
     * 创建数据库版本表
     */
    public void createTable() {
        try (Connection connection = ormClient.getConnection()) {
            PreparedStatementBuildManager buildManager = new PreparedStatementBuildManager(connection, false);
            PreparedStatement createTableStmt = buildManager.createTable(DatabaseVersionTableKey.tableName)
                    .ifNotExists()
                    .column(DatabaseVersionTableKey.version, ColumnDefinitionBuilder.of(DataType.Type.INT).build())
                    .engine("InnoDB")
                    .charset("utf8mb4")
                    .prepare();

            buildManager.execute(createTableStmt);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create table: " + DatabaseVersionTableKey.tableName, e);
        }
    }

    /**
     * 获取当前数据库版本
     *
     * @return 当前版本号，如果没有记录则返回-1
     */
    public int getCurrentVersion() throws SQLException {
        try (Connection connection = ormClient.getConnection()) {
            PreparedStatementBuildManager buildManager = new PreparedStatementBuildManager(connection, false);

            Optional<Integer> result = buildManager.select()
                    .from(DatabaseVersionTableKey.tableName)
                    .select(DatabaseVersionTableKey.version)
                    .limit(1)
                    .executeQueryForObject(rs -> rs.getInt(DatabaseVersionTableKey.version));

            return result.orElse(-1);
        }
    }

    /**
     * 查询数据库版本记录
     *
     * @return DatabaseVersion对象，如果没有记录则返回null
     */
    public DatabaseVersion selectDatabaseVersion() {
        try (Connection connection = ormClient.getConnection()) {
            PreparedStatementBuildManager buildManager = new PreparedStatementBuildManager(connection, false);

            ResultMapper<DatabaseVersion> mapper = rs -> {
                DatabaseVersion databaseVersion = new DatabaseVersion();
                databaseVersion.setVersion(rs.getInt(DatabaseVersionTableKey.version));
                return databaseVersion;
            };

            Optional<DatabaseVersion> result = buildManager.select()
                    .from(DatabaseVersionTableKey.tableName)
                    .select(DatabaseVersionTableKey.version)
                    .limit(1)
                    .executeQueryForObject(mapper);

            return result.orElse(null);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to select database version", e);
        }
    }

    /**
     * 插入新的版本记录
     *
     * @param version 版本号
     */
    public void insertVersion(int version) throws SQLException {
        try (Connection conn = ormClient.getConnection()) {
            PreparedStatementBuildManager buildManager = new PreparedStatementBuildManager(conn, false);
            PreparedStatement insertStmt = buildManager.insertInto(DatabaseVersionTableKey.tableName)
                    .values(DatabaseVersionTableKey.version, version)
                    .prepare();

            buildManager.execute(insertStmt);
        }
    }

    /**
     * 插入数据库版本记录
     *
     * @param databaseVersion 数据库版本对象
     */
    public void insertDatabaseVersion(DatabaseVersion databaseVersion) {
        try (Connection connection = ormClient.getConnection()) {
            PreparedStatement insertStmt = ormClient.insert(connection)
                    .insertInto(DatabaseVersionTableKey.tableName)
                    .values(DatabaseVersionTableKey.version, databaseVersion.getVersion())
                    .prepare();

            insertStmt.executeUpdate();
            insertStmt.close();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert database version", e);
        }
    }

    /**
     * 更新版本记录（更新第一条记录）
     *
     * @param version 版本号
     */
    public void updateVersion(int version) throws SQLException {
        try (Connection conn = ormClient.getConnection()) {
            PreparedStatementBuildManager buildManager = new PreparedStatementBuildManager(conn, false);
            PreparedStatement updateStmt = buildManager.update(DatabaseVersionTableKey.tableName)
                    .set(DatabaseVersionTableKey.version, version)
                    .whereEquals("id", "1")
                    .prepare();

            buildManager.execute(updateStmt);
        }
    }

    /**
     * 更新数据库版本记录
     *
     * @param databaseVersion 数据库版本对象
     */
    public void updateDatabaseVersion(DatabaseVersion databaseVersion) {
        try (Connection connection = ormClient.getConnection()) {
            PreparedStatementBuildManager buildManager = new PreparedStatementBuildManager(connection, false);
            PreparedStatement updateStmt = buildManager.update(DatabaseVersionTableKey.tableName)
                    .set(DatabaseVersionTableKey.version, databaseVersion.getVersion())
                    .prepare();

            buildManager.execute(updateStmt);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update database version", e);
        }
    }

    /**
     * 检查版本表是否存在记录
     *
     * @return 如果存在记录返回true，否则返回false
     */
    public boolean hasVersionRecord() throws SQLException {
        try (Connection connection = ormClient.getConnection()) {
            PreparedStatementBuildManager buildManager = new PreparedStatementBuildManager(connection, false);

            return buildManager.select()
                    .from(DatabaseVersionTableKey.tableName)
                    .select(DatabaseVersionTableKey.version)
                    .limit(1)
                    .executeQueryForExists();
        }
    }
}
