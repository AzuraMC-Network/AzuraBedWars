package cc.azuramc.bedwars.api.event.player;

import cc.azuramc.bedwars.api.game.IGameManager;
import cc.azuramc.bedwars.api.game.IGamePlayer;
import cc.azuramc.bedwars.api.game.IGameTeam;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * 玩家重连事件族
 *
 * @author an5w1r@163.com
 */
public abstract class BedwarsPlayerReconnectEvent extends Event {

    private final IGamePlayer gamePlayer;
    private final IGameTeam gameTeam;
    private final IGameManager gameManager;

    /**
     * @param gamePlayer  重连的玩家
     * @param gameTeam    玩家所属队伍
     * @param gameManager 游戏管理器
     */
    protected BedwarsPlayerReconnectEvent(
            @NotNull IGamePlayer gamePlayer,
            @NotNull IGameTeam gameTeam,
            @NotNull IGameManager gameManager
    ) {
        this.gamePlayer = gamePlayer;
        this.gameTeam = gameTeam;
        this.gameManager = gameManager;
    }

    /**
     * 获取重连的玩家
     *
     * @return 玩家（不为 null）
     */
    @NotNull
    public IGamePlayer getGamePlayer() {
        return gamePlayer;
    }

    /**
     * 获取玩家所属的队伍
     *
     * @return 队伍（不为 null）
     */
    @NotNull
    public IGameTeam getGameTeam() {
        return gameTeam;
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
     * 重连处理「之前」触发，可取消以拒绝该玩家重连。
     */
    public static final class Pre extends BedwarsPlayerReconnectEvent implements Cancellable {

        private static final HandlerList HANDLERS = new HandlerList();

        private boolean cancelled = false;

        public Pre(
                @NotNull IGamePlayer gamePlayer,
                @NotNull IGameTeam gameTeam,
                @NotNull IGameManager gameManager
        ) {
            super(gamePlayer, gameTeam, gameManager);
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
     * 重连处理完成「之后」触发，用于通知。
     */
    public static final class Post extends BedwarsPlayerReconnectEvent {

        private static final HandlerList HANDLERS = new HandlerList();

        public Post(
                @NotNull IGamePlayer gamePlayer,
                @NotNull IGameTeam gameTeam,
                @NotNull IGameManager gameManager
        ) {
            super(gamePlayer, gameTeam, gameManager);
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
}
