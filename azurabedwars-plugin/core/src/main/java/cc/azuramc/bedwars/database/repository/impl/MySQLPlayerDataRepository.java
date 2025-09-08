package cc.azuramc.bedwars.database.repository.impl;

import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.database.entity.PlayerDataTableKey;
import cc.azuramc.bedwars.database.provider.IDatabaseProvider;
import cc.azuramc.bedwars.database.repository.IPlayerDataRepository;
import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.orm.builder.ColumnDefinitionBuilder;
import cc.azuramc.orm.builder.DataType;
import cc.azuramc.orm.builder.PreparedStatementBuildManager;
import cc.azuramc.orm.mapper.ResultMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/**
 * @author an5w1r@163.com
 */
public class MySQLPlayerDataRepository implements IPlayerDataRepository {

    private final IDatabaseProvider databaseProvider;

    public MySQLPlayerDataRepository(IDatabaseProvider databaseProvider) {
        this.databaseProvider = databaseProvider;
    }

    @Override
    public void createTable() {
        try (Connection connection = databaseProvider.getOrmClient().getConnection()) {
            PreparedStatementBuildManager buildManager = new PreparedStatementBuildManager(connection, false);
            PreparedStatement createTableStmt = buildManager.createTable(PlayerDataTableKey.tableName)
                    .ifNotExists()
                    .column(PlayerDataTableKey.id, ColumnDefinitionBuilder.Common.primaryKeyInt())
                    .column(PlayerDataTableKey.name, ColumnDefinitionBuilder.of(DataType.Type.VARCHAR).size(16).notNull().build())
                    .column(PlayerDataTableKey.uuid, ColumnDefinitionBuilder.of(DataType.Type.VARCHAR).size(36).notNull().build())
                    .column(PlayerDataTableKey.mode, ColumnDefinitionBuilder.of(DataType.Type.VARCHAR).size(16).notNull().build())
                    .column(PlayerDataTableKey.level, ColumnDefinitionBuilder.of(DataType.Type.INT).defaultValue("1").build())
                    .column(PlayerDataTableKey.experience, ColumnDefinitionBuilder.of(DataType.Type.DOUBLE).defaultValue("0.0").build())
                    .column(PlayerDataTableKey.kills, ColumnDefinitionBuilder.of(DataType.Type.INT).defaultValue("0").build())
                    .column(PlayerDataTableKey.deaths, ColumnDefinitionBuilder.of(DataType.Type.INT).defaultValue("0").build())
                    .column(PlayerDataTableKey.assists, ColumnDefinitionBuilder.of(DataType.Type.INT).defaultValue("0").build())
                    .column(PlayerDataTableKey.finalKills, ColumnDefinitionBuilder.of(DataType.Type.INT).defaultValue("0").build())
                    .column(PlayerDataTableKey.finalDeaths, ColumnDefinitionBuilder.of(DataType.Type.INT).defaultValue("0").build())
                    .column(PlayerDataTableKey.destroyedBeds, ColumnDefinitionBuilder.of(DataType.Type.INT).defaultValue("0").build())
                    .column(PlayerDataTableKey.wins, ColumnDefinitionBuilder.of(DataType.Type.INT).defaultValue("0").build())
                    .column(PlayerDataTableKey.ties, ColumnDefinitionBuilder.of(DataType.Type.INT).defaultValue("0").build())
                    .column(PlayerDataTableKey.losses, ColumnDefinitionBuilder.of(DataType.Type.INT).defaultValue("0").build())
                    .column(PlayerDataTableKey.games, ColumnDefinitionBuilder.of(DataType.Type.INT).defaultValue("0").build())
                    .column(PlayerDataTableKey.shopDataJson, ColumnDefinitionBuilder.of(DataType.Type.TEXT).build())
                    .column(PlayerDataTableKey.createdAt, ColumnDefinitionBuilder.Common.createdAt())
                    .column(PlayerDataTableKey.updatedAt, ColumnDefinitionBuilder.Common.updatedAt())
                    .index("idx_name", PlayerDataTableKey.name)
                    .index("idx_uuid", PlayerDataTableKey.uuid)
                    .index("idx_created_at", PlayerDataTableKey.createdAt)
                    .engine("InnoDB")
                    .charset("utf8mb4")
                    .prepare();

            buildManager.execute(createTableStmt);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create table: " + PlayerDataTableKey.tableName, e);
        }
    }

    @Override
    public PlayerData insertPlayerData(PlayerData playerData) {
        try (Connection connection = databaseProvider.getOrmClient().getConnection()) {
            PreparedStatementBuildManager buildManager = new PreparedStatementBuildManager(connection, false);
            PreparedStatement insertStmt = buildManager.insertInto(PlayerDataTableKey.tableName)
                    .values(PlayerDataTableKey.name, playerData.getName())
                    .values(PlayerDataTableKey.uuid, playerData.getUuid().toString())
                    .values(PlayerDataTableKey.mode, playerData.getMode().name())
                    .values(PlayerDataTableKey.level, playerData.getLevel())
                    .values(PlayerDataTableKey.experience, playerData.getExperience())
                    .values(PlayerDataTableKey.kills, playerData.getKills())
                    .values(PlayerDataTableKey.deaths, playerData.getDeaths())
                    .values(PlayerDataTableKey.assists, playerData.getAssists())
                    .values(PlayerDataTableKey.finalKills, playerData.getFinalKills())
                    .values(PlayerDataTableKey.finalDeaths, playerData.getFinalDeaths())
                    .values(PlayerDataTableKey.destroyedBeds, playerData.getDestroyedBeds())
                    .values(PlayerDataTableKey.wins, playerData.getWins())
                    .values(PlayerDataTableKey.ties, playerData.getTies())
                    .values(PlayerDataTableKey.losses, playerData.getLosses())
                    .values(PlayerDataTableKey.games, playerData.getGames())
                    .values(PlayerDataTableKey.shopDataJson, playerData.getShopDataJson())
                    .values(PlayerDataTableKey.createdAt, playerData.getCreatedAt())
                    .values(PlayerDataTableKey.updatedAt, playerData.getUpdatedAt())
                    .prepare();

            buildManager.execute(insertStmt);
            return playerData;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert player data", e);
        }
    }

    @Override
    public void updatePlayerData(PlayerData playerData) {
        try (Connection connection = databaseProvider.getOrmClient().getConnection()) {
            PreparedStatementBuildManager buildManager = new PreparedStatementBuildManager(connection, false);
            PreparedStatement updateStmt = buildManager.update(PlayerDataTableKey.tableName)
                    .set(PlayerDataTableKey.name, playerData.getName())
                    .set(PlayerDataTableKey.level, playerData.getLevel())
                    .set(PlayerDataTableKey.experience, playerData.getExperience())
                    .set(PlayerDataTableKey.kills, playerData.getKills())
                    .set(PlayerDataTableKey.deaths, playerData.getDeaths())
                    .set(PlayerDataTableKey.assists, playerData.getAssists())
                    .set(PlayerDataTableKey.finalKills, playerData.getFinalKills())
                    .set(PlayerDataTableKey.finalDeaths, playerData.getFinalDeaths())
                    .set(PlayerDataTableKey.destroyedBeds, playerData.getDestroyedBeds())
                    .set(PlayerDataTableKey.wins, playerData.getWins())
                    .set(PlayerDataTableKey.ties, playerData.getTies())
                    .set(PlayerDataTableKey.losses, playerData.getLosses())
                    .set(PlayerDataTableKey.games, playerData.getGames())
                    .set(PlayerDataTableKey.shopDataJson, playerData.getShopDataJson())
                    .set(PlayerDataTableKey.updatedAt, playerData.getUpdatedAt())
                    .whereEquals(PlayerDataTableKey.id, String.valueOf(playerData.getId()))
                    .prepare();

            buildManager.execute(updateStmt);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update player data", e);
        }
    }

    @Override
    public PlayerData selectPlayerDataByUuid(UUID uuid, GamePlayer gamePlayer) {
        try (Connection connection = databaseProvider.getOrmClient().getConnection()) {
            PreparedStatementBuildManager buildManager = new PreparedStatementBuildManager(connection, false);

            ResultMapper<PlayerData> playerDataMapper = rs -> {
                PlayerData playerData = new PlayerData(gamePlayer);
                playerData.setId(rs.getInt(PlayerDataTableKey.id));
                playerData.setName(rs.getString(PlayerDataTableKey.name));
                playerData.setUuid(UUID.fromString(rs.getString(PlayerDataTableKey.uuid)));
                playerData.setMode(GameModeType.valueOf(rs.getString(PlayerDataTableKey.mode).toUpperCase()));
                playerData.setLevel(rs.getInt(PlayerDataTableKey.level));
                playerData.setExperience(rs.getDouble(PlayerDataTableKey.experience));
                playerData.setKills(rs.getInt(PlayerDataTableKey.kills));
                playerData.setDeaths(rs.getInt(PlayerDataTableKey.deaths));
                playerData.setAssists(rs.getInt(PlayerDataTableKey.assists));
                playerData.setFinalKills(rs.getInt(PlayerDataTableKey.finalKills));
                playerData.setFinalDeaths(rs.getInt(PlayerDataTableKey.finalDeaths));
                playerData.setDestroyedBeds(rs.getInt(PlayerDataTableKey.destroyedBeds));
                playerData.setWins(rs.getInt(PlayerDataTableKey.wins));
                playerData.setTies(rs.getInt(PlayerDataTableKey.ties));
                playerData.setLosses(rs.getInt(PlayerDataTableKey.losses));
                playerData.setGames(rs.getInt(PlayerDataTableKey.games));
                playerData.setShopDataJson(rs.getString(PlayerDataTableKey.shopDataJson));
                playerData.setCreatedAt(rs.getTimestamp(PlayerDataTableKey.createdAt));
                playerData.setUpdatedAt(rs.getTimestamp(PlayerDataTableKey.updatedAt));
                return playerData;
            };

            Optional<PlayerData> result = buildManager.select()
                    .from(PlayerDataTableKey.tableName)
                    .select(PlayerDataTableKey.id,
                            PlayerDataTableKey.name,
                            PlayerDataTableKey.uuid,
                            PlayerDataTableKey.mode,
                            PlayerDataTableKey.level,
                            PlayerDataTableKey.experience,
                            PlayerDataTableKey.kills,
                            PlayerDataTableKey.deaths,
                            PlayerDataTableKey.assists,
                            PlayerDataTableKey.finalKills,
                            PlayerDataTableKey.finalDeaths,
                            PlayerDataTableKey.destroyedBeds,
                            PlayerDataTableKey.wins,
                            PlayerDataTableKey.ties,
                            PlayerDataTableKey.losses,
                            PlayerDataTableKey.games,
                            PlayerDataTableKey.shopDataJson,
                            PlayerDataTableKey.createdAt,
                            PlayerDataTableKey.updatedAt)
                    .whereEquals(PlayerDataTableKey.uuid, uuid.toString())
                    .executeQueryForObject(playerDataMapper);

            return result.orElse(new PlayerData(gamePlayer));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to select player data by uuid", e);
        }
    }
}
