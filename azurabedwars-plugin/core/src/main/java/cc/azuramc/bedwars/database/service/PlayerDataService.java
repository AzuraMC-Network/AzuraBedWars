package cc.azuramc.bedwars.database.service;

import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.database.repository.IPlayerDataRepository;
import cc.azuramc.bedwars.game.GamePlayer;

import java.util.HashMap;

/**
 * @author an5w1r@163.com
 */
public class PlayerDataService {

    private final IPlayerDataRepository playerDataRepository;

    /**
     * 存储GamePlayer与对应PlayerData的关系
     */
    public HashMap<GamePlayer, PlayerData> playerDataMap = new HashMap<>();

    public PlayerDataService(IPlayerDataRepository playerDataRepository) {
        this.playerDataRepository = playerDataRepository;
        this.createTable();
    }

    /**
     * 创建玩家数据表
     */
    public void createTable() {
        playerDataRepository.createTable();
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
            playerData = playerDataRepository.selectPlayerDataByUuid(gamePlayer.getUuid(), gamePlayer);

            // 如果数据库中没有数据，创建新的玩家数据
            String databaseId = playerData.getId();
            if (databaseId == null || databaseId.isEmpty() || databaseId.equals("0")) {
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
        return playerDataRepository.insertPlayerData(new PlayerData(gamePlayer));
    }

    /**
     * 更新用户数据
     *
     * @param gamePlayer 玩家对象
     */
    public void updatePlayerData(GamePlayer gamePlayer) {
        PlayerData playerData = selectPlayerData(gamePlayer);
        playerDataRepository.updatePlayerData(playerData);
    }

    /**
     * 关闭服务，保存所有缓存数据
     */
    public void shutdown() {
        playerDataMap.keySet().forEach(this::updatePlayerData);
        playerDataMap.clear();
    }
}
