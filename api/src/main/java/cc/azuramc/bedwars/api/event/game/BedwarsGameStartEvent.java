package cc.azuramc.bedwars.api.event.game;

import cc.azuramc.bedwars.api.game.IGameManager;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * The game leaves the waiting lobby. Listen to {@link Pre} or {@link Post}, both are called on the main thread.
 *
 * @author an5w1r@163.com
 */
@ApiStatus.NonExtendable
public abstract class BedwarsGameStartEvent extends Event {

    private final IGameManager gameManager;

    @ApiStatus.Internal
    protected BedwarsGameStartEvent(@NotNull IGameManager gameManager) {
        this.gameManager = gameManager;
    }

    @NotNull
    public IGameManager getGameManager() {
        return gameManager;
    }

    /**
     * Called before players are moved to their teams. Cancelling keeps the game waiting.
     */
    public static final class Pre extends BedwarsGameStartEvent implements Cancellable {

        private static final HandlerList HANDLERS = new HandlerList();

        private boolean cancelled;

        @ApiStatus.Internal
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

        @NotNull
        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }
    }

    /**
     * Called after players are teleported and given their items, before the start countdown.
     */
    public static final class Post extends BedwarsGameStartEvent {

        private static final HandlerList HANDLERS = new HandlerList();

        @ApiStatus.Internal
        public Post(@NotNull IGameManager gameManager) {
            super(gameManager);
        }

        @NotNull
        public static HandlerList getHandlerList() {
            return HANDLERS;
        }

        @NotNull
        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }
    }
}
