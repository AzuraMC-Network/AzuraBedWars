package cc.azuramc.bedwars.commands;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.commands.admin.AdminCommand;
import cc.azuramc.bedwars.commands.admin.MapCommand;
import cc.azuramc.bedwars.commands.exception.CommandExceptionHandler;
import cc.azuramc.bedwars.commands.user.ShoutCommand;
import cc.azuramc.bedwars.commands.user.StartCommand;
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
    }
}
