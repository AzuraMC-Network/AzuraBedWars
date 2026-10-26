package cc.azuramc.bedwars.api.event.game;

import cc.azuramc.bedwars.api.game.IGameManager;
import cc.azuramc.bedwars.api.game.map.IMapData;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * The map of the game is loaded. Listen to {@link Pre} or {@link Post}, both are called on the main thread.
 *
 * @author an5w1r@163.com
 */
@ApiStatus.NonExtendable
public abstract class BedwarsGameLoadEvent extends Event {

    private final IGameManager gameManager;
    private final IMapData mapData;

    @ApiStatus.Internal
    protected BedwarsGameLoadEvent(@NotNull IGameManager gameManager, @NotNull IMapData mapData) {
        this.gameManager = gameManager;
        this.mapData = mapData;
    }

    @NotNull
    public IGameManager getGameManager() {
        return gameManager;
    }

    @NotNull
    public IMapData getMapData() {
        return mapData;
    }

    /**
     * Called before the map is loaded. Cancelling leaves the game without a map.
     */
    public static final class Pre extends BedwarsGameLoadEvent implements Cancellable {

        private static final HandlerList HANDLERS = new HandlerList();

        private boolean cancelled;

        @ApiStatus.Internal
        public Pre(@NotNull IGameManager gameManager, @NotNull IMapData mapData) {
            super(gameManager, mapData);
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
     * Called after the map, teams and shops are set up.
     */
    public static final class Post extends BedwarsGameLoadEvent {

        private static final HandlerList HANDLERS = new HandlerList();

        @ApiStatus.Internal
        public Post(@NotNull IGameManager gameManager, @NotNull IMapData mapData) {
            super(gameManager, mapData);
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
