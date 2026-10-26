package cc.azuramc.bedwars.api.extension;

import cc.azuramc.bedwars.api.game.IGamePlayer;
import cc.azuramc.bedwars.api.game.IGameTeam;
import org.jetbrains.annotations.NotNull;

/**
 * An effect or alert of a trap in upgrades.yml, written as {@code type: <plugin>:<id>}
 *
 * @param <S> settings bound from the {@code settings} section of the effect
 * @author an5w1r@163.com
 */
@FunctionalInterface
public interface TrapEffect<S> {

    /**
     * @param owner the team that bought the trap
     * @param intruder the enemy that walked into the base
     */
    void trigger(@NotNull IGameTeam owner, @NotNull IGamePlayer intruder, @NotNull S settings);
}
