package cc.azuramc.bedwars.api.game;

import cc.azuramc.bedwars.api.game.map.IMapData;
import org.bukkit.Sound;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

/**
 * The single running game of this server. Call it on the main thread only.
 *
 * @author an5w1r@163.com
 */
@ApiStatus.NonExtendable
public interface IGameManager {

    @Range(from = 0, to = Integer.MAX_VALUE)
    int getMaxPlayers();

    /**
     * @return true when the online players reach the minimum of the map
     */
    boolean hasEnoughPlayers();

    /**
     * @return true when there are enough players and at least two teams can be filled
     */
    boolean canStart();

    /**
     * @return true when at most one team has players left
     */
    boolean isOver();

    /**
     * @return true when more than one team is still alive
     */
    boolean isTie();

    /**
     * @return the only team left, or null while several teams are alive or none is
     */
    @Nullable
    IGameTeam getWinner();

    /**
     * @return a snapshot of the teams that have players left
     */
    @NotNull
    @Unmodifiable
    List<? extends IGameTeam> getAliveTeams();

    /**
     * @return every team of the map, including empty ones
     */
    @NotNull
    @UnmodifiableView
    List<? extends IGameTeam> getGameTeams();

    /**
     * @return null before a map is loaded
     */
    @Nullable
    IMapData getMapData();

    void broadcastMessage(@NotNull String... texts);

    /**
     * Times are in ticks.
     */
    void broadcastTitleToAll(@Nullable String title, @Nullable String subTitle, int fadeIn, int stay, int fadeOut);

    void broadcastSound(@NotNull Sound sound, float volume, float pitch);
}
