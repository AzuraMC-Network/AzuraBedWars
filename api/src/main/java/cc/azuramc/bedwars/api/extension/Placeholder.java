package cc.azuramc.bedwars.api.extension;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Text written as {@code {<plugin>:<id>}} in the scoreboard, the tab list, titles and action bars
 *
 * @author an5w1r@163.com
 */
@FunctionalInterface
public interface Placeholder {

    /**
     * @param viewer null for text sent to every player at once
     */
    @NotNull
    String apply(@Nullable Player viewer);
}
