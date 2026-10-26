package cc.azuramc.bedwars.api.event.player;

import cc.azuramc.bedwars.api.game.IGameManager;
import cc.azuramc.bedwars.api.game.IGamePlayer;
import cc.azuramc.bedwars.api.game.IGameTeam;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A player rejoins a running game while their team still has a bed.
 * Listen to {@link Pre} or {@link Post}, both are called on the main thread.
 *
 * @author an5w1r@163.com
 */
@ApiStatus.NonExtendable
public abstract class BedwarsPlayerReconnectEvent extends Event {

    private final IGamePlayer gamePlayer;
    private final IGameTeam gameTeam;
    private final IGameManager gameManager;

    @ApiStatus.Internal
    protected BedwarsPlayerReconnectEvent(@NotNull IGamePlayer gamePlayer, @NotNull IGameTeam gameTeam,
                                          @NotNull IGameManager gameManager) {
        this.gamePlayer = gamePlayer;
        this.gameTeam = gameTeam;
        this.gameManager = gameManager;
    }

    @NotNull
    public IGamePlayer getGamePlayer() {
        return gamePlayer;
    }

    @NotNull
    public IGameTeam getGameTeam() {
        return gameTeam;
    }

    @NotNull
    public IGameManager getGameManager() {
        return gameManager;
    }

    /**
     * Called before the player respawns. Cancelling kicks the player.
     */
    public static final class Pre extends BedwarsPlayerReconnectEvent implements Cancellable {

        private static final HandlerList HANDLERS = new HandlerList();

        private boolean cancelled;

        @ApiStatus.Internal
        public Pre(@NotNull IGamePlayer gamePlayer, @NotNull IGameTeam gameTeam, @NotNull IGameManager gameManager) {
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

        @NotNull
        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }
    }

    /**
     * Called after the player respawned and the rejoin was announced.
     */
    public static final class Post extends BedwarsPlayerReconnectEvent {

        private static final HandlerList HANDLERS = new HandlerList();

        @ApiStatus.Internal
        public Post(@NotNull IGamePlayer gamePlayer, @NotNull IGameTeam gameTeam, @NotNull IGameManager gameManager) {
            super(gamePlayer, gameTeam, gameManager);
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
