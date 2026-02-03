package cc.azuramc.bedwars.database.repository.mysql;

import cc.azuramc.bedwars.database.dialect.SqlGenerator;
import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.database.mapper.EntityMapper;
import cc.azuramc.bedwars.database.repository.IPlayerDataRepository;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.orm.AzuraOrmClient;

import java.sql.*;
import java.util.UUID;

/**
 * @author an5w1r@163.com
 */
public class MySQLPlayerDataRepository implements IPlayerDataRepository {

    private final AzuraOrmClient ormClient;

    private static final SqlGenerator SQL = SqlGenerator.mysql();

    private static final String CREATE_TABLE_SQL = SQL.generateCreateTableSql(
            PlayerData.class,
            "INDEX idx_name (`name`)",
            "INDEX idx_uuid (`uuid`)",
            "INDEX idx_created_at (`created_at`)"
    );
    private static final String INSERT_SQL = SQL.generateInsertSql(PlayerData.class);
    private static final String UPDATE_SQL = SQL.generateUpdateSql(PlayerData.class);
    private static final String SELECT_BY_UUID_SQL = SQL.generateSelectByQueryKeySql(PlayerData.class, PlayerData.Query.BY_UUID.name());

    public MySQLPlayerDataRepository(AzuraOrmClient ormClient) {
        this.ormClient = ormClient;
    }

    @Override
    public void createTable() {
        try (Connection conn = ormClient.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_TABLE_SQL);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create table: player_data", e);
        }
    }

    @Override
    public PlayerData insertPlayerData(PlayerData playerData) {
        try (Connection conn = ormClient.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            EntityMapper.setInsertParameters(ps, playerData);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    playerData.setId(String.valueOf(keys.getInt(1)));
                }
            }
            return playerData;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert player data", e);
        }
    }

    @Override
    public void updatePlayerData(PlayerData playerData) {
        try (Connection conn = ormClient.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {

            EntityMapper.setUpdateParameters(ps, playerData);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update player data", e);
        }
    }

    @Override
    public PlayerData selectPlayerDataByUuid(UUID uuid, GamePlayer gamePlayer) {
        try (Connection conn = ormClient.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_UUID_SQL)) {

            ps.setString(1, uuid.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return EntityMapper.fromResultSet(rs, PlayerData.class, gamePlayer);
                }
                return new PlayerData(gamePlayer);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to select player data by uuid", e);
        }
    }
}
