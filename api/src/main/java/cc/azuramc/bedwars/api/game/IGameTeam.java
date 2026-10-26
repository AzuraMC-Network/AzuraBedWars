package cc.azuramc.bedwars.api.game;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * One team of the map. Empty teams exist too and count as dead.
 *
 * @author an5w1r@163.com
 */
@ApiStatus.NonExtendable
public interface IGameTeam {

    /**
     * @return the name with its color code
     */
    @NotNull
    String getName();

    @NotNull
    String getNameWithoutColor();

    @NotNull
    ChatColor getChatColor();

    @NotNull
    DyeColor getDyeColor();

    @NotNull
    Color getColor();

    @NotNull
    Location getSpawnLocation();

    @Range(from = 0, to = Integer.MAX_VALUE)
    int getMaxPlayers();

    /**
     * @return true when the bed is gone, so dead members will not respawn
     */
    boolean isDestroyed();

    /**
     * @return true when the team already used its rejuvenation bed
     */
    boolean isRejuvenated();

    /**
     * @return true when no member is online and playing
     */
    boolean isDead();

    boolean isFull();

    /**
     * @return the number of members that are online and playing
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    int getActivePlayerCount();

    @Contract("null -> false")
    boolean isInTeam(@Nullable IGamePlayer gamePlayer);

    /**
     * @return null when the bed is intact or was not broken by a player
     */
    @Nullable
    IGamePlayer getDestroyPlayer();

    /**
     * @return a snapshot of every member, including offline ones
     */
    @NotNull
    @Unmodifiable
    List<? extends IGamePlayer> getGamePlayers();

    /**
     * @return a snapshot of the members that are online and playing
     */
    @NotNull
    @Unmodifiable
    List<? extends IGamePlayer> getAlivePlayers();
}
