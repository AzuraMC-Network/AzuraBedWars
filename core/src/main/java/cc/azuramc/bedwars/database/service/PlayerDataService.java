package cc.azuramc.bedwars.database.service;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.dao.PlayerDataDao;
import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.game.GamePlayer;

import java.sql.SQLException;
import java.util.HashMap;

/**
 * @author an5w1r@163.com
 */
public class PlayerDataService {

    private final PlayerDataDao playerDataDao;

    public PlayerDataService(AzuraBedWars plugin) {
        this.playerDataDao = plugin.getPlayerDataDao();
    }

    /** 存储GamePlayer与对应ID(主键)关系 */
    public HashMap<GamePlayer, Integer> playerIdMap = new HashMap<>();

    /** 存储GamePlayer与对应PlayerData的关系 */
    public HashMap<GamePlayer, PlayerData> playerDataMap = new HashMap<>();

    /**
     * 建表
     */
    public void createTable() {
        try {
            playerDataDao.createPlayerDataTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据 GamePlayer 查询用户
     * @param gamePlayer GamePlayer对象
     * @return 对应的 PlayerData 对象，如果不存在则返回 null
     */
    public PlayerData selectPlayerData(GamePlayer gamePlayer) {
        try {
            PlayerData playerData = playerDataMap.getOrDefault(gamePlayer, null);
            int playerId = playerIdMap.getOrDefault(gamePlayer, -1);

            // 如果缓存中没有数据，尝试从数据库查询
            if(playerData == null || playerId == -1) {
                // 先通过UUID获取玩家ID
                playerId = playerDataDao.selectPlayerDataIdByUuid(gamePlayer.getUuid());

                if (playerId > 0) {
                    // 如果找到了ID，根据ID查询完整数据
                    playerData = playerDataDao.selectPlayerDataById(playerId, gamePlayer);

                    // 缓存数据
                    playerIdMap.put(gamePlayer, playerId);
                    playerDataMap.put(gamePlayer, playerData);
                } else {
                    // 数据库中也没有数据，创建新的玩家数据
                    playerData = insertPlayerData(gamePlayer);

                    // 缓存新创建的数据
                    if (playerData != null) {
                        playerIdMap.put(gamePlayer, playerData.getId());
                        playerDataMap.put(gamePlayer, playerData);
                    }
                }
            }

            return playerData;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * 插入新的用户记录
     * @param gamePlayer 玩家对象
     * @return 插入成功后，带有生成ID的用户对象
     */
    public PlayerData insertPlayerData(GamePlayer gamePlayer) {
        try {
            return playerDataDao.insertPlayerData(new PlayerData(gamePlayer));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 更新用户数据
     * @param gamePlayer 玩家对象
     */
    public void updatePlayerData(GamePlayer gamePlayer) {
        try {
            PlayerData playerData = selectPlayerData(gamePlayer);
            playerDataDao.updatePlayerData(playerData);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        playerDataMap.keySet().forEach(this::updatePlayerData);
        playerDataMap.clear();
    }
}
