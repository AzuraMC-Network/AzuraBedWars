package cc.azuramc.bedwars.api.game.map;

import org.bukkit.Location;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * The map of the game. The location lists are fresh snapshots on each call.
 *
 * @author an5w1r@163.com
 */
@ApiStatus.NonExtendable
public interface IMapData {

    /**
     * @return bases times players per team
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    int getMaxPlayers();

    /**
     * @return true when the map has everything a game needs
     */
    boolean isValid();

    /**
     * @return the higher Y of the two region corners, 80 when neither is set
     */
    double getHigherY();

    @Nullable
    Location getPos1Location();

    @Nullable
    Location getPos2Location();

    @NotNull
    @Unmodifiable
    List<Location> getBaseLocations();

    @NotNull
    @Unmodifiable
    List<Location> getAllDropLocations();

    @NotNull
    @Unmodifiable
    List<Location> getAllShopLocations();
}
