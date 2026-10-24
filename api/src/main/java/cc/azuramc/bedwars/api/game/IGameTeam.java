package cc.azuramc.bedwars.api.game;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 游戏队伍 API 接口
 *
 * @author an5w1r@163.com
 */
public interface IGameTeam {

    /**
     * 获取队伍名称（含颜色符号）
     *
     * @return 队伍名称（不为 null）
     */
    @NotNull
    String getName();

    /**
     * 获取队伍名称（移除前缀颜色符号）
     *
     * @return 队伍名称（不为 null）
     */
    @NotNull
    String getNameWithoutColor();

    /**
     * 获取队伍聊天颜色
     *
     * @return 聊天颜色（不为 null）
     */
    @NotNull
    ChatColor getChatColor();

    /**
     * 获取队伍染料颜色
     *
     * @return 染料颜色（不为 null）
     */
    @NotNull
    DyeColor getDyeColor();

    /**
     * 获取队伍颜色对象
     *
     * @return 颜色对象（不为 null）
     */
    @NotNull
    Color getColor();

    /**
     * 获取队伍出生点
     *
     * @return 出生点位置（不为 null）
     */
    @NotNull
    Location getSpawnLocation();

    /**
     * 获取队伍最大玩家数
     *
     * @return 最大玩家数
     */
    int getMaxPlayers();

    /**
     * 队伍是否拥有床
     *
     * @return 拥有床返回 true
     */
    boolean isHasBed();

    /**
     * 队伍床是否已被摧毁
     *
     * @return 已摧毁返回 true
     */
    boolean isDestroyed();

    /**
     * 队伍床是否被摧毁（综合判定）
     *
     * @return 被摧毁返回 true
     */
    boolean isBedDestroyed();

    /**
     * 队伍是否已全员淘汰
     *
     * @return 全员淘汰返回 true
     */
    boolean isDead();

    /**
     * 队伍是否已满员
     *
     * @return 满员返回 true
     */
    boolean isFull();

    /**
     * 获取队伍当前存活玩家数量
     *
     * @return 存活玩家数量
     */
    int getActivePlayerCount();

    /**
     * 判断指定玩家是否属于本队伍
     *
     * @param gamePlayer 待判断的玩家，可为 null
     * @return 属于本队伍返回 true
     */
    boolean isInTeam(@Nullable IGamePlayer gamePlayer);

    /**
     * 获取摧毁本队床的玩家
     *
     * @return 摧毁者，未被摧毁时为 null
     */
    @Nullable
    IGamePlayer getDestroyPlayer();

    /**
     * 获取队伍全部玩家
     *
     * @return 玩家列表（不为 null）
     */
    @NotNull
    List<? extends IGamePlayer> getGamePlayers();

    /**
     * 获取队伍存活玩家
     *
     * @return 存活玩家列表（不为 null）
     */
    @NotNull
    List<? extends IGamePlayer> getAlivePlayers();
}
