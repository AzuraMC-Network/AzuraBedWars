package cc.azuramc.bedwars.database.dao;

import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.database.entity.PlayerDataTableKey;
import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.orm.AzuraORM;
import cc.azuramc.orm.AzuraOrmClient;
import cc.azuramc.orm.builder.DataType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author an5w1r@163.com
 */
public class PlayerDataDao {

    public static AzuraOrmClient client = AzuraORM.getClient();

    /**
     * 建表
     */
    public static void createPlayerDataTable() throws SQLException {
        try (Connection conn = client.getConnection()) {

            PreparedStatement createUsersStmt = client.createTable(conn)
                    .createTable(PlayerDataTableKey.tableName)
                    .ifNotExists()
                    .addIdColumn()
                    .column(PlayerDataTableKey.name, DataType.VARCHAR_NOT_NULL(36))
                    .column(PlayerDataTableKey.uuid, DataType.Type.BINARY.withSize(16))
                    .column(PlayerDataTableKey.mode, DataType.VARCHAR_NOT_NULL(20))
                    .column(PlayerDataTableKey.kills, DataType.DEFAULT(0))
                    .column(PlayerDataTableKey.assists, DataType.DEFAULT(0))
                    .column(PlayerDataTableKey.deaths, DataType.DEFAULT(0))
                    .column(PlayerDataTableKey.finalKills, DataType.DEFAULT(0))
                    .column(PlayerDataTableKey.destroyedBeds, DataType.DEFAULT(0))
                    .column(PlayerDataTableKey.wins, DataType.DEFAULT(0))
                    .column(PlayerDataTableKey.losses, DataType.DEFAULT(0))
                    .column(PlayerDataTableKey.games, DataType.DEFAULT(0))
                    .column(PlayerDataTableKey.shopData, DataType.TEXT_NOT_NULL())
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
    public static PlayerData insertPlayerData(PlayerData playerData) throws SQLException {
        try (Connection conn = client.getConnection()) {

            PreparedStatement insertUsersStmt = client.insert(conn)
                    .insertInto(PlayerDataTableKey.tableName)
                    .values(PlayerDataTableKey.name, playerData.getName())
                    .values(PlayerDataTableKey.uuid, playerData.getUuid())
                    .values(PlayerDataTableKey.mode, playerData.getMode())
                    .values(PlayerDataTableKey.kills, playerData.getKills())
                    .values(PlayerDataTableKey.deaths, playerData.getDeaths())
                    .values(PlayerDataTableKey.assists, playerData.getAssists())
                    .values(PlayerDataTableKey.finalKills, playerData.getFinalKills())
                    .values(PlayerDataTableKey.destroyedBeds, playerData.getDestroyedBeds())
                    .values(PlayerDataTableKey.wins, playerData.getWins())
                    .values(PlayerDataTableKey.losses, playerData.getLosses())
                    .values(PlayerDataTableKey.games, playerData.getGames())
                    .values(PlayerDataTableKey.shopData, playerData.getShopData())
                    .values(PlayerDataTableKey.createdAt, playerData.getCreatedAt())
                    .values(PlayerDataTableKey.updatedAt, playerData.getUpdatedAt())
                    .prepare();

            insertUsersStmt.executeUpdate();
            insertUsersStmt.close();
        }
        return null;
    }

    /**
     * 更新用户数据
     * @param playerData 要更新的用户对象
     * @return 更新成功后，带有生成ID的用户对象
     */
    public static PlayerData updatePlayerData(PlayerData playerData) throws SQLException {
        try (Connection conn = client.getConnection()) {

            PreparedStatement updateUsersStmt = client.update(conn)
                    .update(PlayerDataTableKey.tableName)
                    .set(PlayerDataTableKey.name, playerData.getName())
                    .set(PlayerDataTableKey.uuid, playerData.getUuid())
                    .set(PlayerDataTableKey.mode, playerData.getMode())
                    .set(PlayerDataTableKey.kills, playerData.getKills())
                    .set(PlayerDataTableKey.deaths, playerData.getDeaths())
                    .set(PlayerDataTableKey.assists, playerData.getAssists())
                    .set(PlayerDataTableKey.finalKills, playerData.getFinalKills())
                    .set(PlayerDataTableKey.destroyedBeds, playerData.getDestroyedBeds())
                    .set(PlayerDataTableKey.wins, playerData.getWins())
                    .set(PlayerDataTableKey.losses, playerData.getLosses())
                    .set(PlayerDataTableKey.games, playerData.getGames())
                    .set(PlayerDataTableKey.shopData, playerData.getShopData())
                    .set(PlayerDataTableKey.createdAt, playerData.getCreatedAt())
                    .set(PlayerDataTableKey.updatedAt, playerData.getUpdatedAt())
                    .whereEquals(PlayerDataTableKey.id, playerData.getId())
                    .prepare();

            updateUsersStmt.executeUpdate();
            updateUsersStmt.close();
        }
        return null;
    }


    /**
     * 根据 ID 查询用户
     * @param id 用户 ID
     * @return 对应的 PlayerData 对象，如果不存在则返回 null
     */
    public static PlayerData selectPlayerDataById(int id) throws SQLException {
        try (Connection conn = client.getConnection()) {

            PreparedStatement selectUsersStmt = client.select(conn)
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
                            PlayerDataTableKey.shopData,
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
                playerData.setShopData(Arrays.toString(resultSet.getString(PlayerDataTableKey.shopData).split(", ")));
                playerData.setCreatedAt(resultSet.getTimestamp(PlayerDataTableKey.createdAt));
                playerData.setUpdatedAt(resultSet.getTimestamp(PlayerDataTableKey.updatedAt));
            }

            resultSet.close();
            selectUsersStmt.close();

            return playerData;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据 UUID 查询用户
     * @param uuid 用户 UUID
     * @return 对应的 PlayerData 对象，如果不存在则返回 -1
     */
    public static int selectPlayerDataIdByUuid(UUID uuid) throws SQLException {
        try (Connection conn = client.getConnection()) {

            PreparedStatement selectUsersStmt = client.select(conn)
                    .from(PlayerDataTableKey.tableName)
                    .select(PlayerDataTableKey.id)
                    .whereEquals(PlayerDataTableKey.uuid, uuid.toString())
                    .prepare();

            ResultSet resultSet = selectUsersStmt.executeQuery();
            int userId = resultSet.getInt(PlayerDataTableKey.id);
            resultSet.close();
            selectUsersStmt.close();

            return userId;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }


}
