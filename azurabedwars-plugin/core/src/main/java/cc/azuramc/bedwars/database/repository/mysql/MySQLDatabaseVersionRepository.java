package cc.azuramc.bedwars.database.repository.mysql;

import cc.azuramc.bedwars.database.entity.DatabaseVersion;
import cc.azuramc.bedwars.database.entity.DatabaseVersionTableKey;
import cc.azuramc.bedwars.database.provider.IDatabaseProvider;
import cc.azuramc.bedwars.database.repository.IDatabaseVersionRepository;
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
public class MySQLDatabaseVersionRepository implements IDatabaseVersionRepository {

    private final IDatabaseProvider databaseProvider;

    public MySQLDatabaseVersionRepository(IDatabaseProvider databaseProvider) {
        this.databaseProvider = databaseProvider;
    }

    @Override
    public void createTable() {
        try (Connection connection = databaseProvider.getOrmClient().getConnection()) {
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

    @Override
    public int getCurrentVersion() throws SQLException {
        try (Connection connection = databaseProvider.getOrmClient().getConnection()) {
            PreparedStatementBuildManager buildManager = new PreparedStatementBuildManager(connection, false);

            Optional<Integer> result = buildManager.select()
                    .from(DatabaseVersionTableKey.tableName)
                    .select(DatabaseVersionTableKey.version)
                    .limit(1)
                    .executeQueryForObject(rs -> rs.getInt(DatabaseVersionTableKey.version));

            return result.orElse(-1);
        }
    }

    @Override
    public DatabaseVersion selectDatabaseVersion() {
        try (Connection connection = databaseProvider.getOrmClient().getConnection()) {
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

    @Override
    public void insertVersion(int version) throws SQLException {
        try (Connection conn = databaseProvider.getOrmClient().getConnection()) {
            PreparedStatementBuildManager buildManager = new PreparedStatementBuildManager(conn, false);
            PreparedStatement insertStmt = buildManager.insertInto(DatabaseVersionTableKey.tableName)
                    .values(DatabaseVersionTableKey.version, version)
                    .prepare();

            buildManager.execute(insertStmt);
        }
    }

    @Override
    public void insertDatabaseVersion(DatabaseVersion databaseVersion) {
        try (Connection connection = databaseProvider.getOrmClient().getConnection()) {
            PreparedStatement insertStmt = databaseProvider.getOrmClient().insert(connection)
                    .insertInto(DatabaseVersionTableKey.tableName)
                    .values(DatabaseVersionTableKey.version, databaseVersion.getVersion())
                    .prepare();

            insertStmt.executeUpdate();
            insertStmt.close();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert database version", e);
        }
    }

    @Override
    public void updateVersion(int version) throws SQLException {
        try (Connection conn = databaseProvider.getOrmClient().getConnection()) {
            PreparedStatementBuildManager buildManager = new PreparedStatementBuildManager(conn, false);
            PreparedStatement updateStmt = buildManager.update(DatabaseVersionTableKey.tableName)
                    .set(DatabaseVersionTableKey.version, version)
                    .whereEquals("id", "1")
                    .prepare();

            buildManager.execute(updateStmt);
        }
    }

    @Override
    public void updateDatabaseVersion(DatabaseVersion databaseVersion) {
        try (Connection connection = databaseProvider.getOrmClient().getConnection()) {
            PreparedStatementBuildManager buildManager = new PreparedStatementBuildManager(connection, false);
            PreparedStatement updateStmt = buildManager.update(DatabaseVersionTableKey.tableName)
                    .set(DatabaseVersionTableKey.version, databaseVersion.getVersion())
                    .prepare();

            buildManager.execute(updateStmt);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update database version", e);
        }
    }

    @Override
    public boolean hasVersionRecord() throws SQLException {
        try (Connection connection = databaseProvider.getOrmClient().getConnection()) {
            PreparedStatementBuildManager buildManager = new PreparedStatementBuildManager(connection, false);

            return buildManager.select()
                    .from(DatabaseVersionTableKey.tableName)
                    .select(DatabaseVersionTableKey.version)
                    .limit(1)
                    .executeQueryForExists();
        }
    }
}
