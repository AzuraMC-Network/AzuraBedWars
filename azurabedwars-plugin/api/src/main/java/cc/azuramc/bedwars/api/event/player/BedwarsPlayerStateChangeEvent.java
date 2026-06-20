package cc.azuramc.bedwars.api.event.player;

import cc.azuramc.bedwars.api.game.IGameManager;
import cc.azuramc.bedwars.api.game.IGamePlayer;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * 玩家状态变化事件
 *
 * @author an5w1r@163.com
 */
@Getter
public class BedwarsPlayerStateChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * 游戏管理器
     */
    @NotNull
    private final IGameManager gameManager;

    /**
     * 状态变化的玩家
     */
    @NotNull
    private final IGamePlayer gamePlayer;

    /**
     * 状态变化类型
     */
    @NotNull
    private final StateChangeType stateChangeType;

    /**
     * @param gameManager     游戏管理器
     * @param gamePlayer      状态变化的玩家
     * @param stateChangeType 状态变化类型
     */
    public BedwarsPlayerStateChangeEvent(
            @NotNull IGameManager gameManager,
            @NotNull IGamePlayer gamePlayer,
            @NotNull StateChangeType stateChangeType
    ) {
        this.gameManager = gameManager;
        this.gamePlayer = gamePlayer;
        this.stateChangeType = stateChangeType;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * 状态变化类型枚举
     */
    public enum StateChangeType {
        /**
         * 旁观者状态变化
         */
        SPECTATOR,

        /**
         * 复活状态变化
         */
        RESPAWNING,

        /**
         * 隐身状态变化
         */
        INVISIBLE,

        /**
         * 重连状态变化
         */
        RECONNECT
    }
}
