package cc.azuramc.bedwars.api.event.game;

import cc.azuramc.bedwars.api.game.IGameManager;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * 游戏开始事件
 *
 * @author an5w1r@163.com
 */
public abstract class BedwarsGameStartEvent extends Event {

    private final IGameManager gameManager;

    /**
     * @param gameManager 游戏管理器
     */
    protected BedwarsGameStartEvent(@NotNull IGameManager gameManager) {
        this.gameManager = gameManager;
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
     * 游戏正式开始「之前」触发，可取消以阻止开始。
     */
    public static final class Pre extends BedwarsGameStartEvent implements Cancellable {

        private static final HandlerList HANDLERS = new HandlerList();

        private boolean cancelled = false;

        public Pre(@NotNull IGameManager gameManager) {
            super(gameManager);
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
     * 游戏已开始「之后」触发，用于通知。
     */
    public static final class Post extends BedwarsGameStartEvent {

        private static final HandlerList HANDLERS = new HandlerList();

        public Post(@NotNull IGameManager gameManager) {
            super(gameManager);
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
