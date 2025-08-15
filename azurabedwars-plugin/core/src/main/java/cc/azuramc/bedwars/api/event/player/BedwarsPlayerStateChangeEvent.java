package cc.azuramc.bedwars.api.event.player;

import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * 玩家状态变化事件
 * 当玩家的游戏状态发生变化时触发，可以被取消以阻止状态变化
 *
 * @author an5w1r@163.com
 */
@Getter
@Setter
public class BedwarsPlayerStateChangeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    /**
     * 游戏管理器
     */
    private final GameManager gameManager;

    /**
     * 状态变化的玩家
     */
    private final GamePlayer gamePlayer;

    /**
     * 状态变化类型
     */
    private final StateChangeType stateChangeType;

    /**
     * 构造函数
     *
     * @param gameManager     游戏管理器
     * @param gamePlayer      状态变化的玩家
     * @param stateChangeType 状态变化类型
     */
    public BedwarsPlayerStateChangeEvent(GameManager gameManager, GamePlayer gamePlayer,
                                         StateChangeType stateChangeType) {
        this.gameManager = gameManager;
        this.gamePlayer = gamePlayer;
        this.stateChangeType = stateChangeType;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
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
