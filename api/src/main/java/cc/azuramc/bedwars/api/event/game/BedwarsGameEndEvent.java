package cc.azuramc.bedwars.api.event.game;

import cc.azuramc.bedwars.api.game.IGameManager;
import cc.azuramc.bedwars.api.game.IGameTeam;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Called once on the main thread when the game ends, either because one team is left or because time ran out.
 *
 * @author an5w1r@163.com
 */
public final class BedwarsGameEndEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final IGameManager gameManager;
    private final IGameTeam winner;
    private final List<IGameTeam> aliveTeams;

    @ApiStatus.Internal
    public BedwarsGameEndEvent(@NotNull IGameManager gameManager) {
        this.gameManager = gameManager;
        this.winner = gameManager.getWinner();
        this.aliveTeams = Collections.unmodifiableList(new ArrayList<>(gameManager.getAliveTeams()));
    }

    @NotNull
    public IGameManager getGameManager() {
        return gameManager;
    }

    /**
     * @return the winning team, null on a draw
     */
    @Nullable
    public IGameTeam getWinner() {
        return winner;
    }

    /**
     * @return the teams still in the game when it ended
     */
    @NotNull
    @Unmodifiable
    public List<IGameTeam> getAliveTeams() {
        return aliveTeams;
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
