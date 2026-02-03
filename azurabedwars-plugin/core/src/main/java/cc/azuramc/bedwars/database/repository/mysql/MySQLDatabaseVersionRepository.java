package cc.azuramc.bedwars.database.repository.mysql;

import cc.azuramc.bedwars.database.dialect.SqlGenerator;
import cc.azuramc.bedwars.database.entity.DatabaseVersion;
import cc.azuramc.bedwars.database.mapper.EntityMapper;
import cc.azuramc.bedwars.database.repository.IDatabaseVersionRepository;
import cc.azuramc.orm.AzuraOrmClient;

import java.sql.*;

/**
 * @author an5w1r@163.com
 */
public class MySQLDatabaseVersionRepository implements IDatabaseVersionRepository {

    private final AzuraOrmClient ormClient;

    private static final SqlGenerator SQL = SqlGenerator.mysql();

    private static final String CREATE_TABLE_SQL = SQL.generateCreateTableSql(DatabaseVersion.class);
    private static final String SELECT_SQL = SQL.generateSelectWithLimitSql(DatabaseVersion.class, 1);
    private static final String INSERT_SQL = SQL.generateInsertSql(DatabaseVersion.class);
    private static final String UPDATE_SQL = SQL.generateUpdateAllSql(DatabaseVersion.class);
    private static final String EXISTS_SQL = SQL.generateExistsSql(DatabaseVersion.class);

    private static final String COL_VERSION = EntityMapper.getQueryColumn(DatabaseVersion.class, DatabaseVersion.Query.BY_VERSION.name());

    public MySQLDatabaseVersionRepository(AzuraOrmClient ormClient) {
        this.ormClient = ormClient;
    }

    @Override
    public void createTable() {
        try (Connection conn = ormClient.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_TABLE_SQL);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create table: database_version", e);
        }
    }

    @Override
    public int getCurrentVersion() throws SQLException {
        try (Connection conn = ormClient.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_SQL);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(COL_VERSION);
            }
            return -1;
        }
    }

    @Override
    public DatabaseVersion selectDatabaseVersion() {
        try (Connection conn = ormClient.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_SQL);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                DatabaseVersion dv = new DatabaseVersion();
                dv.setVersion(rs.getInt(COL_VERSION));
                return dv;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to select database version", e);
        }
    }

    @Override
    public void insertVersion(int version) throws SQLException {
        try (Connection conn = ormClient.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            ps.setInt(1, version);
            ps.executeUpdate();
        }
    }

    @Override
    public void insertDatabaseVersion(DatabaseVersion databaseVersion) {
        try (Connection conn = ormClient.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            ps.setInt(1, databaseVersion.getVersion());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert database version", e);
        }
    }

    @Override
    public void updateVersion(int version) throws SQLException {
        try (Connection conn = ormClient.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {

            ps.setInt(1, version);
            ps.executeUpdate();
        }
    }

    @Override
    public void updateDatabaseVersion(DatabaseVersion databaseVersion) {
        try (Connection conn = ormClient.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {

            ps.setInt(1, databaseVersion.getVersion());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update database version", e);
        }
    }

    @Override
    public boolean hasVersionRecord() throws SQLException {
        try (Connection conn = ormClient.getConnection();
             PreparedStatement ps = conn.prepareStatement(EXISTS_SQL);
             ResultSet rs = ps.executeQuery()) {

            return rs.next();
        }
    }
}
