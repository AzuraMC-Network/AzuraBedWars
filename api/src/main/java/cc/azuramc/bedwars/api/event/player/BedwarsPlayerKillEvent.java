package cc.azuramc.bedwars.api.event.player;

import cc.azuramc.bedwars.api.game.IGamePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Called on the main thread after a player is killed by another player.
 *
 * @author an5w1r@163.com
 */
public final class BedwarsPlayerKillEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final IGamePlayer victim;
    private final IGamePlayer killer;
    private final boolean finalKill;

    @ApiStatus.Internal
    public BedwarsPlayerKillEvent(@NotNull IGamePlayer victim, @NotNull IGamePlayer killer, boolean finalKill) {
        this.victim = victim;
        this.killer = killer;
        this.finalKill = finalKill;
    }

    @NotNull
    public IGamePlayer getVictim() {
        return victim;
    }

    @NotNull
    public IGamePlayer getKiller() {
        return killer;
    }

    /**
     * @return true when the victim's bed is gone, so the victim will not respawn
     */
    public boolean isFinalKill() {
        return finalKill;
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
