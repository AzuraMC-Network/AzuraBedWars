package cc.azuramc.bedwars.database.service;

import cc.azuramc.bedwars.AzuraBedWars;
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

    private PlayerDataDao playerDataDao;

    public PlayerDataService(AzuraBedWars plugin) {
        this.playerDataDao = plugin.getPlayerDataDao();
    }

    /** 存储GamePlayer与对应ID(主键)关系 */
    public HashMap<GamePlayer, Integer> playerIdMap = new HashMap<>();

    /** 存储GamePlayer与对应PlayerData的关系 */
    public HashMap<GamePlayer, PlayerData> playerDataMap = new HashMap<>();

    public ChangeManager<PlayerData> changeManager = new ChangeManager<>(playerDataList -> {
        playerDataList.forEach(playerData -> {
            try {
                playerDataDao.updatePlayerData(playerData);
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
            PlayerData playerData = playerDataMap.get(gamePlayer);
            int playerId = playerIdMap.getOrDefault(gamePlayer, -1);
            
            if(playerData == null || playerId == -1) {
                // 先通过UUID获取玩家ID
                playerId = playerDataDao.selectPlayerDataIdByUuid(gamePlayer.getUuid());
                
                if (playerId > 0) {
                    // 如果找到了ID，根据ID查询完整数据
                    playerData = playerDataDao.selectPlayerDataById(playerId);
                    
                    // 缓存数据
                    playerIdMap.put(gamePlayer, playerId);
                    playerDataMap.put(gamePlayer, playerData);
                } else {
                    // 如果没有找到数据 说明是新玩家 这里不创建 让GamePlayer自己处理
                    return null;
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
     * @param playerData 要插入的用户对象 (id 会在数据库中生成)
     * @return 插入成功后，带有生成ID的用户对象
     */
    public PlayerData insertPlayerData(PlayerData playerData) {
        try {
            return playerDataDao.insertPlayerData(playerData);
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
    public PlayerData updatePlayerData(PlayerData playerData) {
        try {
            return playerDataDao.updatePlayerData(playerData);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 缓存玩家数据
     * @param gamePlayer 游戏玩家
     * @param playerData 玩家数据
     */
    public void cachePlayerData(GamePlayer gamePlayer, PlayerData playerData) {
        if (playerData != null && playerData.getId() > 0) {
            playerDataMap.put(gamePlayer, playerData);
            playerIdMap.put(gamePlayer, playerData.getId());
        }
    }
}
