package cc.azuramc.bedwars.api.game;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.UUID;

/**
 * A player that joined this game. It stays around after the player leaves, so check {@link #isOnline()}.
 *
 * @author an5w1r@163.com
 */
@ApiStatus.NonExtendable
public interface IGamePlayer {

    /**
     * @return the Bukkit player, it may be an offline handle
     */
    @NotNull
    Player getPlayer();

    @NotNull
    UUID getUuid();

    @NotNull
    String getName();

    /**
     * @return the shown name, which may be a nickname
     */
    @NotNull
    String getNickName();

    /**
     * @return null while the player has no team yet
     */
    @Nullable
    IGameTeam getGameTeam();

    boolean isOnline();

    boolean isSpectator();

    /**
     * @return true while waiting to respawn after a death
     */
    boolean isRespawning();

    boolean isInvisible();

    boolean isAfk();

    /**
     * @return true when the player rejoined this game after leaving
     */
    boolean isReconnect();

    @Range(from = 0, to = Integer.MAX_VALUE)
    int getCurrentGameKills();

    @Range(from = 0, to = Integer.MAX_VALUE)
    int getCurrentGameFinalKills();

    @Range(from = 0, to = Integer.MAX_VALUE)
    int getCurrentGameAssists();

    @Range(from = 0, to = Integer.MAX_VALUE)
    int getCurrentGameDeaths();

    @Range(from = 0, to = Integer.MAX_VALUE)
    int getCurrentGameDestroyedBeds();

    /**
     * Does nothing while the player is offline, same for the other send methods.
     */
    void sendMessage(@NotNull String message);

    void sendActionBar(@NotNull String message);

    /**
     * Times are in ticks.
     */
    void sendTitle(@Nullable String title, @Nullable String subTitle, int fadeIn, int stay, int fadeOut);

    void playSound(@NotNull Sound sound, float volume, float pitch);
}
