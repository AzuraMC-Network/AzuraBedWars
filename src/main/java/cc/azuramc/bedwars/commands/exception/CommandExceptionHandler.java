package cc.azuramc.bedwars.commands.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.exception.BukkitExceptionAdapter;
import revxrsal.commands.bukkit.exception.InvalidPlayerException;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.MissingArgumentException;
import revxrsal.commands.exception.NoPermissionException;

public class CommandExceptionHandler extends BukkitExceptionAdapter {

    @Override
    public void missingArgument(@NotNull CommandActor actor,
                                @NotNull MissingArgumentException exception) {
        actor.error("&c缺少参数");
    }

    @Override
    public void invalidPlayer(@NotNull CommandActor actor,
                              @NotNull InvalidPlayerException exception) {
        actor.error("&c指定的玩家不在线");
    }

    @Override
    public void noPermission(@NotNull CommandActor actor,
                             @NotNull NoPermissionException exception) {
        actor.error("&c权限不足");
    }
}
