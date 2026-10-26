package cc.azuramc.bedwars.api.extension;

import cc.azuramc.bedwars.api.game.IGamePlayer;
import cc.azuramc.bedwars.api.game.IGameTeam;
import org.jetbrains.annotations.NotNull;

/**
 * An effect of a team upgrade level in upgrades.yml, written as {@code type: <plugin>:<id>}
 *
 * @param <S> settings bound from the {@code settings} section of the effect
 * @author an5w1r@163.com
 */
@FunctionalInterface
public interface UpgradeEffect<S> {

    /**
     * Called once when the team buys the level
     */
    void apply(@NotNull IGameTeam team, @NotNull S settings);

    /**
     * Called each time a member of a team that owns the level spawns or respawns
     */
    default void onSpawn(@NotNull IGamePlayer player, @NotNull S settings) {
    }
}
