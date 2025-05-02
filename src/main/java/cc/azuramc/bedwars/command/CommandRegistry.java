package cc.azuramc.bedwars.command;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.command.admin.AdminCommand;
import cc.azuramc.bedwars.command.admin.MapCommand;
import cc.azuramc.bedwars.command.exception.CommandExceptionHandler;
import cc.azuramc.bedwars.command.user.ShoutCommand;
import cc.azuramc.bedwars.command.user.StartCommand;
import cc.azuramc.bedwars.command.user.ToggleDamageDisplayCommand;
import revxrsal.commands.bukkit.BukkitCommandHandler;

public class CommandRegistry {

    private final BukkitCommandHandler handler;

    public CommandRegistry(AzuraBedWars plugin) {

        handler = BukkitCommandHandler.create(plugin);
        handler.registerDependency(AzuraBedWars.class, plugin);
        handler.setExceptionHandler(new CommandExceptionHandler());

        register();
    }

    private void register() {
        handler.register(new AdminCommand());

        handler.register(new MapCommand());
        handler.register(new StartCommand());
        handler.register(new ShoutCommand());
        if (AzuraBedWars.getInstance().getGameManager().isArrowDisplayEnabled() && AzuraBedWars.getInstance().getGameManager().isAttackDisplayEnabled()) {
            handler.register(new ToggleDamageDisplayCommand());
        }
    }
}
