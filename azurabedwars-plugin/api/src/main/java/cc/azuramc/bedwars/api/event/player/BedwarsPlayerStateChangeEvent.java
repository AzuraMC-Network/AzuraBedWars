package cc.azuramc.bedwars.api.event.player;

import cc.azuramc.bedwars.api.game.IGameManager;
import cc.azuramc.bedwars.api.game.IGamePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * 玩家状态变化事件
 *
 * @author an5w1r@163.com
 */
public abstract class BedwarsPlayerStateChangeEvent extends Event {

    private final IGameManager gameManager;
    private final IGamePlayer gamePlayer;
    private final StateChangeType stateChangeType;

    /**
     * @param gameManager     游戏管理器
     * @param gamePlayer      状态变化的玩家
     * @param stateChangeType 状态变化类型
     */
    protected BedwarsPlayerStateChangeEvent(
            @NotNull IGameManager gameManager,
            @NotNull IGamePlayer gamePlayer,
            @NotNull StateChangeType stateChangeType
    ) {
        this.gameManager = gameManager;
        this.gamePlayer = gamePlayer;
        this.stateChangeType = stateChangeType;
    }

    /**
     * 获取游戏管理器
     *
     * @return 游戏管理器（不为 null）
     */
    @NotNull
    public IGameManager getGameManager() {
        return gameManager;
    }

    /**
     * 获取状态变化的玩家
     *
     * @return 玩家（不为 null）
     */
    @NotNull
    public IGamePlayer getGamePlayer() {
        return gamePlayer;
    }

    /**
     * 获取状态变化类型
     *
     * @return 状态变化类型（不为 null）
     */
    @NotNull
    public StateChangeType getStateChangeType() {
        return stateChangeType;
    }

    /**
     * 状态变化「之前」触发，可取消以阻止状态变化。
     */
    public static final class Pre extends BedwarsPlayerStateChangeEvent implements Cancellable {

        private static final HandlerList HANDLERS = new HandlerList();

        private boolean cancelled = false;

        public Pre(
                @NotNull IGameManager gameManager,
                @NotNull IGamePlayer gamePlayer,
                @NotNull StateChangeType stateChangeType
        ) {
            super(gameManager, gamePlayer, stateChangeType);
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }

        @NotNull
        public static HandlerList getHandlerList() {
            return HANDLERS;
        }

        @Override
        public @NotNull HandlerList getHandlers() {
            return HANDLERS;
        }
    }

    /**
     * 状态变化「之后」触发，用于通知。
     */
    public static final class Post extends BedwarsPlayerStateChangeEvent {

        private static final HandlerList HANDLERS = new HandlerList();

        public Post(
                @NotNull IGameManager gameManager,
                @NotNull IGamePlayer gamePlayer,
                @NotNull StateChangeType stateChangeType
        ) {
            super(gameManager, gamePlayer, stateChangeType);
        }

        @NotNull
        public static HandlerList getHandlerList() {
            return HANDLERS;
        }

        @Override
        public @NotNull HandlerList getHandlers() {
            return HANDLERS;
        }
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
