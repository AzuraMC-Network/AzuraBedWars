package cc.azuramc.bedwars.database.dao;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.database.entity.PlayerDataTableKey;
import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.orm.AzuraOrmClient;
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
public class PlayerDataDao {

    private final AzuraOrmClient ormClient;

    public PlayerDataDao(AzuraBedWars plugin) {
        this.ormClient = plugin.getOrmClient();
    }

    /**
     * 建表
     */
    public void createTable() {
        try (Connection connection = ormClient.getConnection()) {
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
                    .engine("InnoDB")
                    .charset("utf8mb4")
                    .prepare();

            buildManager.execute(createTableStmt);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create table: " + PlayerDataTableKey.tableName, e);
        }
    }

    /**
     * 插入新的用户记录
     *
     * @param playerData 要插入的用户对象
     * @return 插入的PlayerData对象
     */
    public PlayerData insertPlayerData(PlayerData playerData) {
        try (Connection connection = ormClient.getConnection()) {
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
                    .prepare();

            buildManager.execute(insertStmt);
            return playerData;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert player data", e);
        }
    }

    public PlayerData insertPlayerDataWithRawJDBC(PlayerData playerData) {
        String sql = "INSERT INTO " + PlayerDataTableKey.tableName + " (" +
                PlayerDataTableKey.name + ", " +
                PlayerDataTableKey.uuid + ", " +
                PlayerDataTableKey.mode + ", " +
                PlayerDataTableKey.level + ", " +
                PlayerDataTableKey.experience + ", " +
                PlayerDataTableKey.kills + ", " +
                PlayerDataTableKey.deaths + ", " +
                PlayerDataTableKey.assists + ", " +
                PlayerDataTableKey.finalKills + ", " +
                PlayerDataTableKey.finalDeaths + ", " +
                PlayerDataTableKey.destroyedBeds + ", " +
                PlayerDataTableKey.wins + ", " +
                PlayerDataTableKey.ties + ", " +
                PlayerDataTableKey.losses + ", " +
                PlayerDataTableKey.games + ", " +
                PlayerDataTableKey.shopDataJson +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = ormClient.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, playerData.getName());
            stmt.setString(2, playerData.getUuid().toString());
            stmt.setString(3, playerData.getMode().name());
            stmt.setInt(4, playerData.getLevel());
            stmt.setDouble(5, playerData.getExperience());
            stmt.setInt(6, playerData.getKills());
            stmt.setInt(7, playerData.getDeaths());
            stmt.setInt(8, playerData.getAssists());
            stmt.setInt(9, playerData.getFinalKills());
            stmt.setInt(10, playerData.getFinalDeaths());
            stmt.setInt(11, playerData.getDestroyedBeds());
            stmt.setInt(12, playerData.getWins());
            stmt.setInt(13, playerData.getTies());
            stmt.setInt(14, playerData.getLosses());
            stmt.setInt(15, playerData.getGames());
            stmt.setString(16, playerData.getShopDataJson());

            // 执行插入操作
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                return playerData;
            } else {
                throw new RuntimeException("No rows were inserted");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert player data with raw JDBC", e);
        }
    }

    /**
     * 更新用户数据
     *
     * @param playerData 要更新的用户对象
     */
    public void updatePlayerData(PlayerData playerData) {
        try (Connection connection = ormClient.getConnection()) {
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
                    .whereEquals(PlayerDataTableKey.id, String.valueOf(playerData.getId()))
                    .prepare();

            buildManager.execute(updateStmt);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update player data", e);
        }
    }


    /**
     * 根据 ID 查询用户
     *
     * @param id 用户 ID
     * @return 对应的 PlayerData 对象，如果不存在则返回 null
     */
    public PlayerData selectPlayerDataById(int id, GamePlayer gamePlayer) {
        try (Connection connection = ormClient.getConnection()) {
            PreparedStatementBuildManager buildManager = new PreparedStatementBuildManager(connection, false);

            // 创建ResultMapper来映射PlayerData
            ResultMapper<PlayerData> playerDataMapper = rs -> {
                PlayerData playerData = new PlayerData(gamePlayer);
                playerData.setId(id);
                playerData.setName(rs.getString(PlayerDataTableKey.name));
                playerData.setUuid(UUID.fromString(rs.getString(PlayerDataTableKey.uuid)));
                playerData.setMode(GameModeType.valueOf(rs.getString(PlayerDataTableKey.mode).toUpperCase()));
                playerData.setLevel(rs.getInt(PlayerDataTableKey.level));
                playerData.setExperience(rs.getInt(PlayerDataTableKey.experience));
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
                    .select(PlayerDataTableKey.name,
                            PlayerDataTableKey.uuid,
                            PlayerDataTableKey.mode,
                            PlayerDataTableKey.level,
                            PlayerDataTableKey.kills,
                            PlayerDataTableKey.experience,
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
                    .whereEquals(PlayerDataTableKey.id, String.valueOf(id))
                    .executeQueryForObject(playerDataMapper);

            return result.orElse(new PlayerData(gamePlayer));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to select player data by id", e);
        }
    }

    /**
     * 根据 UUID 查询用户
     *
     * @param uuid 用户 UUID
     * @return 对应的 PlayerData 对象，如果不存在则返回 -1
     */
    public int selectPlayerDataIdByUuid(UUID uuid) {
        try (Connection connection = ormClient.getConnection()) {
            PreparedStatementBuildManager buildManager = new PreparedStatementBuildManager(connection, false);

            Optional<Integer> result = buildManager.select()
                    .from(PlayerDataTableKey.tableName)
                    .select(PlayerDataTableKey.id)
                    .whereEquals(PlayerDataTableKey.uuid, uuid.toString())
                    .executeQueryForObject(rs -> rs.getInt(PlayerDataTableKey.id));

            return result.orElse(-1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to select player data id by uuid", e);
        }
    }


}
