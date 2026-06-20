package cc.azuramc.bedwars.api.game;

import cc.azuramc.bedwars.api.game.map.IMapData;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 游戏管理器 API 接口
 *
 * @author an5w1r@163.com
 */
public interface IGameManager {

    /**
     * 获取本局最大玩家数
     *
     * @return 最大玩家数
     */
    int getMaxPlayers();

    /**
     * 是否已有足够玩家开始游戏
     *
     * @return 满足返回 true
     */
    boolean hasEnoughPlayers();

    /**
     * 当前是否满足开始条件
     *
     * @return 满足返回 true
     */
    boolean canStart();

    /**
     * 游戏是否已结束
     *
     * @return 已结束返回 true
     */
    boolean isOver();

    /**
     * 是否为平局
     *
     * @return 平局返回 true
     */
    boolean isTie();

    /**
     * 获取获胜队伍
     *
     * @return 获胜队伍，平局或无获胜者时为 null
     */
    @Nullable
    IGameTeam getWinner();

    /**
     * 获取存活队伍列表
     *
     * @return 存活队伍列表（不为 null）
     */
    @NotNull
    List<? extends IGameTeam> getAliveTeams();

    /**
     * 获取全部队伍列表
     *
     * @return 队伍列表（不为 null）
     */
    @NotNull
    List<? extends IGameTeam> getGameTeams();

    /**
     * 获取本局全部参与玩家
     *
     * @return 玩家列表（不为 null）
     */
    @NotNull
    List<? extends IGamePlayer> getAllInGamePlayers();

    /**
     * 获取地图数据
     *
     * @return 地图数据，未加载时为 null
     */
    @Nullable
    IMapData getMapData();

    /**
     * 向所有玩家广播聊天消息
     *
     * @param texts 消息内容
     */
    void broadcastMessage(String... texts);

    /**
     * 向所有玩家广播标题
     *
     * @param title    主标题
     * @param subTitle 副标题
     * @param fadeIn   淡入时间（tick）
     * @param stay     停留时间（tick）
     * @param fadeOut  淡出时间（tick）
     */
    void broadcastTitleToAll(String title, String subTitle, Integer fadeIn, Integer stay, Integer fadeOut);

    /**
     * 向所有玩家广播音效
     *
     * @param sound  音效
     * @param volume 音量
     * @param pitch  音调
     */
    void broadcastSound(Sound sound, float volume, float pitch);
}
