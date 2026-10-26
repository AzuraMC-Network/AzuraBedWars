package cc.azuramc.bedwars.command.exception;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.LangConfig;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.exception.BukkitExceptionAdapter;
import revxrsal.commands.bukkit.exception.InvalidPlayerException;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.MissingArgumentException;
import revxrsal.commands.exception.NoPermissionException;

/**
 * @author an5w1r@163.com
 */
public class CommandExceptionHandler extends BukkitExceptionAdapter {

    @Override
    public void missingArgument(@NotNull CommandActor actor,
                                @NotNull MissingArgumentException exception) {
        actor.error(lang().getMissingArgument());
    }

    @Override
    public void invalidPlayer(@NotNull CommandActor actor,
                              @NotNull InvalidPlayerException exception) {
        actor.error(lang().getPlayerOffline());
    }

    @Override
    public void noPermission(@NotNull CommandActor actor,
                             @NotNull NoPermissionException exception) {
        actor.error(lang().getNoPermission());
    }

    private static LangConfig.Command lang() {
        return AzuraBedWars.getInstance().getLang().getCommand();
    }
}
