package cc.azuramc.bedwars.api.game;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 游戏玩家 API 接口
 *
 * @author an5w1r@163.com
 */
public interface IGamePlayer {

    /**
     * 获取对应的 Bukkit 玩家对象
     * <p>始终返回非 null 的引用；该玩家是否在线/可用请通过 {@link #isOnline()} 判断，
     * 而非对返回值判空。
     *
     * @return Bukkit 玩家（不为 null）
     */
    @NotNull
    Player getPlayer();

    /**
     * 获取玩家 UUID
     *
     * @return 玩家 UUID（不为 null）
     */
    @NotNull
    UUID getUuid();

    /**
     * 获取玩家原始名称
     *
     * @return 玩家名称（不为 null）
     */
    @NotNull
    String getName();

    /**
     * 获取玩家显示名称（可能为昵称）
     *
     * @return 玩家显示名称（不为 null）
     */
    @NotNull
    String getNickName();

    /**
     * 获取玩家所属队伍
     *
     * @return 玩家所属队伍，未分配时为 null
     */
    @Nullable
    IGameTeam getGameTeam();

    /**
     * 玩家是否在线
     *
     * @return 在线返回 true
     */
    boolean isOnline();

    /**
     * 玩家是否为旁观者
     *
     * @return 旁观者返回 true
     */
    boolean isSpectator();

    /**
     * 玩家是否处于复活中状态
     *
     * @return 复活中返回 true
     */
    boolean isRespawning();

    /**
     * 玩家是否处于隐身状态
     *
     * @return 隐身返回 true
     */
    boolean isInvisible();

    /**
     * 玩家是否处于挂机状态
     *
     * @return 挂机返回 true
     */
    boolean isAfk();

    /**
     * 玩家是否为重连玩家
     *
     * @return 重连返回 true
     */
    boolean isReconnect();

    /**
     * 获取本局击杀数
     *
     * @return 本局击杀数
     */
    int getCurrentGameKills();

    /**
     * 获取本局最终击杀数
     *
     * @return 本局最终击杀数
     */
    int getCurrentGameFinalKills();

    /**
     * 获取本局助攻数
     *
     * @return 本局助攻数
     */
    int getCurrentGameAssists();

    /**
     * 获取本局死亡数
     *
     * @return 本局死亡数
     */
    int getCurrentGameDeaths();

    /**
     * 获取本局摧毁床数
     *
     * @return 本局摧毁床数
     */
    int getCurrentGameDestroyedBeds();

    /**
     * 向玩家发送聊天消息
     *
     * @param message 消息内容
     */
    void sendMessage(@NotNull String message);

    /**
     * 向玩家发送动作栏消息
     *
     * @param message 消息内容
     */
    void sendActionBar(@NotNull String message);

    /**
     * 向玩家发送标题
     *
     * @param title    主标题
     * @param subTitle 副标题
     * @param fadeIn   淡入时间（tick）
     * @param stay     停留时间（tick）
     * @param fadeOut  淡出时间（tick）
     */
    void sendTitle(@Nullable String title, @Nullable String subTitle, int fadeIn, int stay, int fadeOut);

    /**
     * 向玩家播放音效
     *
     * @param sound  音效
     * @param volume 音量
     * @param pitch  音调
     */
    void playSound(@NotNull Sound sound, float volume, float pitch);
}
