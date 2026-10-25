package cc.azuramc.bedwars.api.event.player;

import cc.azuramc.bedwars.api.game.IGameManager;
import cc.azuramc.bedwars.api.game.IGamePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Called on the main thread right after one of the player's {@link StateChangeType} flags is set.
 *
 * @author an5w1r@163.com
 */
public final class BedwarsPlayerStateChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final IGameManager gameManager;
    private final IGamePlayer gamePlayer;
    private final StateChangeType stateChangeType;
    private final boolean enabled;

    @ApiStatus.Internal
    public BedwarsPlayerStateChangeEvent(@NotNull IGameManager gameManager, @NotNull IGamePlayer gamePlayer,
                                         @NotNull StateChangeType stateChangeType, boolean enabled) {
        this.gameManager = gameManager;
        this.gamePlayer = gamePlayer;
        this.stateChangeType = stateChangeType;
        this.enabled = enabled;
    }

    @NotNull
    public IGameManager getGameManager() {
        return gameManager;
    }

    @NotNull
    public IGamePlayer getGamePlayer() {
        return gamePlayer;
    }

    @NotNull
    public StateChangeType getStateChangeType() {
        return stateChangeType;
    }

    /**
     * @return the new value of the flag, it may equal the old one
     */
    public boolean isEnabled() {
        return enabled;
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

    public enum StateChangeType {
        SPECTATOR,
        RESPAWNING,
        INVISIBLE,
        RECONNECT
    }
}
