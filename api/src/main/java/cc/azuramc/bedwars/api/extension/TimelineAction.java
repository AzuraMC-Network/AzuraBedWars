package cc.azuramc.bedwars.api.extension;

import cc.azuramc.bedwars.api.game.IGameManager;
import org.jetbrains.annotations.NotNull;

/**
 * An action of a stage in timeline.yml, written as {@code type: <plugin>:<id>}
 *
 * @param <S> settings bound from the {@code settings} section of the action
 * @author an5w1r@163.com
 */
@FunctionalInterface
public interface TimelineAction<S> {

    void run(@NotNull IGameManager game, @NotNull S settings);
}
