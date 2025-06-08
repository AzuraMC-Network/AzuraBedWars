package cc.azuramc.bedwars.database.service;

import cc.azuramc.bedwars.database.dao.PlayerDataDao;
import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.orm.manager.ChangeManager;

import java.sql.SQLException;
import java.util.HashMap;

/**
 * @author an5w1r@163.com
 */
public class PlayerDataService {

    /** 存储GamePlayer与对应PlayerData的ID关系 */
    public static HashMap<GamePlayer, Integer> playerIdMap = new HashMap<>();

    /** 存储GamePlayer与对应PlayerData的关系 */
    public static HashMap<GamePlayer, PlayerData> playerDataMap = new HashMap<>();

    public static ChangeManager<PlayerData> changeManager = new ChangeManager<>(playerDataList -> {
        playerDataList.forEach(playerData -> {
            try {
                PlayerDataDao.updatePlayerData(playerData);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    },
    // 每8个进行一次更新
    8,
    // 3分钟
    180000);

    /**
     * 建表
     */
    public static void createTable() {
        try {
            PlayerDataDao.createPlayerDataTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据 GamePlayer 查询用户
     * @param gamePlayer GamePlayer对象
     * @return 对应的 PlayerData 对象，如果不存在则返回 null
     */
    public static PlayerData selectPlayerData(GamePlayer gamePlayer) {
        try {
            int playerId = PlayerDataDao.selectPlayerDataIdByUuid(gamePlayer.getUuid());
            PlayerData playerData = PlayerDataDao.selectPlayerDataById(playerId);
            playerIdMap.put(gamePlayer, playerId);
            playerDataMap.put(gamePlayer, playerData);
            return playerData;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 插入新的用户记录
     * @param playerData 要插入的用户对象 (id 会在数据库中生成)
     * @return 插入成功后，带有生成ID的用户对象
     */
    public static PlayerData insertPlayerData(PlayerData playerData) {
        try {
            return PlayerDataDao.insertPlayerData(playerData);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 更新用户数据
     * @param playerData 要更新的用户对象
     * @return 更新成功后，带有生成ID的用户对象
     */
    public static PlayerData updatePlayerData(PlayerData playerData) {
        try {
            return PlayerDataDao.updatePlayerData(playerData);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
