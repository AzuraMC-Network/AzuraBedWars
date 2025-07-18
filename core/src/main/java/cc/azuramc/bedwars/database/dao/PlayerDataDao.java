package cc.azuramc.bedwars.database.dao;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.database.entity.PlayerDataTableKey;
import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.orm.AzuraOrmClient;
import cc.azuramc.orm.builder.DataType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * @author an5w1r@163.com
 */
public class PlayerDataDao {
    
    public AzuraOrmClient ormClient;
    
    public PlayerDataDao(AzuraBedWars plugin) {
        this.ormClient = plugin.getOrmClient();
    }

    /**
     * 建表
     */
    public void createPlayerDataTable() throws SQLException {

        try (Connection conn = ormClient.getConnection()) {
            PreparedStatement createUsersStmt = ormClient.createTable(conn)
                    .createTable(PlayerDataTableKey.tableName)
                    .ifNotExists()
                    .addIdColumn()
                    .column(PlayerDataTableKey.name, DataType.VARCHAR_NOT_NULL(36))
                    .column(PlayerDataTableKey.uuid, DataType.VARCHAR_NOT_NULL(36))
                    .column(PlayerDataTableKey.mode, DataType.VARCHAR_NOT_NULL(20))
                    .column(PlayerDataTableKey.kills, DataType.Type.INT.getSql(), DataType.DEFAULT(0))
                    .column(PlayerDataTableKey.assists, DataType.Type.INT.getSql(), DataType.DEFAULT(0))
                    .column(PlayerDataTableKey.deaths, DataType.Type.INT.getSql(), DataType.DEFAULT(0))
                    .column(PlayerDataTableKey.finalKills, DataType.Type.INT.getSql(), DataType.DEFAULT(0))
                    .column(PlayerDataTableKey.destroyedBeds, DataType.Type.INT.getSql(), DataType.DEFAULT(0))
                    .column(PlayerDataTableKey.wins, DataType.Type.INT.getSql(), DataType.DEFAULT(0))
                    .column(PlayerDataTableKey.losses, DataType.Type.INT.getSql(), DataType.DEFAULT(0))
                    .column(PlayerDataTableKey.games, DataType.Type.INT.getSql(), DataType.DEFAULT(0))
                    .column(PlayerDataTableKey.shopDataJson, DataType.Type.TEXT.getSql(), DataType.DEFAULT("{}"))
                    .addTimestamps()
                    .engine("InnoDB")
                    .charset("utf8mb4")
                    .collate("utf8mb4_unicode_ci")
                    .prepare();

            createUsersStmt.executeUpdate();
            createUsersStmt.close();
        }
    }

    /**
     * 插入新的用户记录
     * @param playerData 要插入的用户对象 (id 会在数据库中生成)
     * @return 插入成功后，带有生成ID的用户对象
     */
    public PlayerData insertPlayerData(PlayerData playerData) throws SQLException {

        try (Connection conn = ormClient.getConnection()) {
            PreparedStatement insertUsersStmt = ormClient.insert(conn)
                    .insertInto(PlayerDataTableKey.tableName)
                    .values(PlayerDataTableKey.name, playerData.getName())
                    .values(PlayerDataTableKey.uuid, playerData.getUuid().toString())
                    .values(PlayerDataTableKey.mode, playerData.getMode().toString())
                    .values(PlayerDataTableKey.kills, playerData.getKills())
                    .values(PlayerDataTableKey.deaths, playerData.getDeaths())
                    .values(PlayerDataTableKey.assists, playerData.getAssists())
                    .values(PlayerDataTableKey.finalKills, playerData.getFinalKills())
                    .values(PlayerDataTableKey.destroyedBeds, playerData.getDestroyedBeds())
                    .values(PlayerDataTableKey.wins, playerData.getWins())
                    .values(PlayerDataTableKey.losses, playerData.getLosses())
                    .values(PlayerDataTableKey.games, playerData.getGames())
                    .values(PlayerDataTableKey.shopDataJson, playerData.getShopDataJson() != null ? playerData.getShopDataJson() : "{}")
                    .values(PlayerDataTableKey.createdAt, playerData.getCreatedAt())
                    .values(PlayerDataTableKey.updatedAt, playerData.getUpdatedAt())
                    .prepare();

            int affectedRows = insertUsersStmt.executeUpdate();
            insertUsersStmt.close();
            
            if (affectedRows > 0) {
                // 插入成功后，通过UUID查询获取生成的ID
                int generatedId = selectPlayerDataIdByUuid(playerData.getUuid());
                if (generatedId > 0) {
                    playerData.setId(generatedId);
                }
            }
        }
        
        return playerData;
    }

    /**
     * 更新用户数据
     * @param playerData 要更新的用户对象
     * @return 更新成功后，带有生成ID的用户对象
     */
    public PlayerData updatePlayerData(PlayerData playerData) throws SQLException {
        try (Connection conn = ormClient.getConnection()) {
            PreparedStatement updateUsersStmt = ormClient.update(conn)
                    .update(PlayerDataTableKey.tableName)
                    .set(PlayerDataTableKey.name, playerData.getName())
                    .set(PlayerDataTableKey.uuid, playerData.getUuid().toString())
                    .set(PlayerDataTableKey.mode, playerData.getMode().toString())
                    .set(PlayerDataTableKey.kills, playerData.getKills())
                    .set(PlayerDataTableKey.deaths, playerData.getDeaths())
                    .set(PlayerDataTableKey.assists, playerData.getAssists())
                    .set(PlayerDataTableKey.finalKills, playerData.getFinalKills())
                    .set(PlayerDataTableKey.destroyedBeds, playerData.getDestroyedBeds())
                    .set(PlayerDataTableKey.wins, playerData.getWins())
                    .set(PlayerDataTableKey.losses, playerData.getLosses())
                    .set(PlayerDataTableKey.games, playerData.getGames())
                    .set(PlayerDataTableKey.shopDataJson, playerData.getShopDataJson() != null ? playerData.getShopDataJson() : "{}")
                    .set(PlayerDataTableKey.createdAt, playerData.getCreatedAt())
                    .set(PlayerDataTableKey.updatedAt, playerData.getUpdatedAt())
                    .whereEquals(PlayerDataTableKey.id, playerData.getId())
                    .prepare();

            updateUsersStmt.executeUpdate();
            updateUsersStmt.close();
        }
        return playerData;
    }


    /**
     * 根据 ID 查询用户
     * @param id 用户 ID
     * @return 对应的 PlayerData 对象，如果不存在则返回 null
     */
    public PlayerData selectPlayerDataById(int id) throws SQLException {
        try (Connection conn = ormClient.getConnection()) {
            PreparedStatement selectUsersStmt = ormClient.select(conn)
                    .from(PlayerDataTableKey.tableName)
                    .select(PlayerDataTableKey.name,
                            PlayerDataTableKey.uuid,
                            PlayerDataTableKey.mode,
                            PlayerDataTableKey.kills,
                            PlayerDataTableKey.deaths,
                            PlayerDataTableKey.assists,
                            PlayerDataTableKey.finalKills,
                            PlayerDataTableKey.destroyedBeds,
                            PlayerDataTableKey.wins,
                            PlayerDataTableKey.losses,
                            PlayerDataTableKey.games,
                            PlayerDataTableKey.shopDataJson,
                            PlayerDataTableKey.createdAt,
                            PlayerDataTableKey.updatedAt)
                    .whereEquals(PlayerDataTableKey.id, String.valueOf(id))
                    .prepare();

            ResultSet resultSet = selectUsersStmt.executeQuery();
            PlayerData playerData = new PlayerData();
            playerData.setId(id);

            while (resultSet.next()) {
                playerData.setName(resultSet.getString(PlayerDataTableKey.name));
                playerData.setUuid(UUID.fromString(resultSet.getString(PlayerDataTableKey.uuid)));
                playerData.setMode(GameModeType.valueOf(resultSet.getString(PlayerDataTableKey.mode).toUpperCase()));
                playerData.setKills(resultSet.getInt(PlayerDataTableKey.kills));
                playerData.setDeaths(resultSet.getInt(PlayerDataTableKey.deaths));
                playerData.setAssists(resultSet.getInt(PlayerDataTableKey.assists));
                playerData.setFinalKills(resultSet.getInt(PlayerDataTableKey.finalKills));
                playerData.setDestroyedBeds(resultSet.getInt(PlayerDataTableKey.destroyedBeds));
                playerData.setWins(resultSet.getInt(PlayerDataTableKey.wins));
                playerData.setLosses(resultSet.getInt(PlayerDataTableKey.losses));
                playerData.setGames(resultSet.getInt(PlayerDataTableKey.games));
                playerData.setShopDataJson(resultSet.getString(PlayerDataTableKey.shopDataJson));
                playerData.setCreatedAt(resultSet.getTimestamp(PlayerDataTableKey.createdAt));
                playerData.setUpdatedAt(resultSet.getTimestamp(PlayerDataTableKey.updatedAt));
            }

            resultSet.close();
            selectUsersStmt.close();

            return playerData;
        }
    }

    /**
     * 根据 UUID 查询用户
     * @param uuid 用户 UUID
     * @return 对应的 PlayerData 对象，如果不存在则返回 -1
     */
    public int selectPlayerDataIdByUuid(UUID uuid) throws SQLException {
        try (Connection conn = ormClient.getConnection()) {
            PreparedStatement selectUsersStmt = ormClient.select(conn)
                    .from(PlayerDataTableKey.tableName)
                    .select(PlayerDataTableKey.id)
                    .whereEquals(PlayerDataTableKey.uuid, uuid.toString())
                    .prepare();

            ResultSet resultSet = selectUsersStmt.executeQuery();
            int userId = -1;
            
            if (resultSet.next()) {
                userId = resultSet.getInt(PlayerDataTableKey.id);
            }
            
            resultSet.close();
            selectUsersStmt.close();

            return userId;
        }
    }


}
