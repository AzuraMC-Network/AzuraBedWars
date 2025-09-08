package cc.azuramc.bedwars.database.repository;

import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.game.GamePlayer;

import java.util.UUID;

/**
 * @author an5w1r@163.com
 */
public interface IPlayerDataRepository {

    /**
     * 创建玩家数据表
     */
    void createTable();

    /**
     * 插入新的用户记录
     *
     * @param playerData 要插入的用户对象
     * @return 插入的PlayerData对象
     */
    PlayerData insertPlayerData(PlayerData playerData);

    /**
     * 更新用户数据
     *
     * @param playerData 要更新的用户对象
     */
    void updatePlayerData(PlayerData playerData);

    /**
     * 根据 UUID 查询用户
     *
     * @param uuid       用户 UUID
     * @param gamePlayer 游戏玩家对象
     * @return 对应的 PlayerData 对象，如果不存在则返回新的 PlayerData 对象
     */
    PlayerData selectPlayerDataByUuid(UUID uuid, GamePlayer gamePlayer);
}
