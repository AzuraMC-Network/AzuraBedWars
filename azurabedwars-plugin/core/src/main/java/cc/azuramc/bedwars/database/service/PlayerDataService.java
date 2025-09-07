package cc.azuramc.bedwars.database.service;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.dao.PlayerDataDao;
import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.game.GamePlayer;

import java.util.HashMap;

/**
 * @author an5w1r@163.com
 */
public class PlayerDataService {

    private final PlayerDataDao playerDataDao;

    public PlayerDataService(AzuraBedWars plugin) {
        this.playerDataDao = plugin.getPlayerDataDao();
        this.createTable();
    }

    /**
     * 存储GamePlayer与对应PlayerData的关系
     */
    public HashMap<GamePlayer, PlayerData> playerDataMap = new HashMap<>();

    /**
     * 建表
     */
    public void createTable() {
        playerDataDao.createTable();
    }

    /**
     * 根据 GamePlayer 查询用户
     *
     * @param gamePlayer GamePlayer对象
     * @return 对应的 PlayerData 对象，如果不存在则返回 null
     */
    public PlayerData selectPlayerData(GamePlayer gamePlayer) {
        PlayerData playerData = playerDataMap.getOrDefault(gamePlayer, null);

        // 如果缓存中没有数据，尝试从数据库查询
        if (playerData == null) {
            // 直接通过UUID查询完整数据
            playerData = playerDataDao.selectPlayerDataByUuid(gamePlayer.getUuid(), gamePlayer);

            // 如果数据库中没有数据，创建新的玩家数据
            if (playerData.getId() == 0) {
                playerData = insertPlayerData(gamePlayer);
            }

            // 缓存数据
            if (playerData != null) {
                playerDataMap.put(gamePlayer, playerData);
            }
        }

        return playerData;

    }

    /**
     * 插入新的用户记录
     *
     * @param gamePlayer 玩家对象
     * @return 插入成功后，带有生成ID的用户对象
     */
    public PlayerData insertPlayerData(GamePlayer gamePlayer) {
        return playerDataDao.insertPlayerData(new PlayerData(gamePlayer));

    }

    /**
     * 更新用户数据
     *
     * @param gamePlayer 玩家对象
     */
    public void updatePlayerData(GamePlayer gamePlayer) {
        PlayerData playerData = selectPlayerData(gamePlayer);
        playerDataDao.updatePlayerData(playerData);
    }

    public void shutdown() {
        playerDataMap.keySet().forEach(this::updatePlayerData);
        playerDataMap.clear();
    }
}
