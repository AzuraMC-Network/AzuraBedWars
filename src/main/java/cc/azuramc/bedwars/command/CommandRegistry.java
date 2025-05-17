package cc.azuramc.bedwars.command;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.command.admin.AdminCommand;
import cc.azuramc.bedwars.command.admin.MapCommand;
import cc.azuramc.bedwars.command.exception.CommandExceptionHandler;
import cc.azuramc.bedwars.command.user.ShoutCommand;
import cc.azuramc.bedwars.command.user.StartCommand;
import cc.azuramc.bedwars.command.user.ToggleDamageDisplayCommand;
import cc.azuramc.bedwars.game.GameManager;
import revxrsal.commands.bukkit.BukkitCommandHandler;

/**
 * @author an5w1r@163.com
 */
public class CommandRegistry {

    private final BukkitCommandHandler handler;
    private final AzuraBedWars plugin;

    public CommandRegistry(AzuraBedWars plugin) {
        this.plugin = plugin;
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
        
        // 检查游戏管理器是否已初始化
        GameManager gameManager = plugin.getGameManager();
        if (gameManager != null && gameManager.isArrowDisplayEnabled() && gameManager.isAttackDisplayEnabled()) {
            handler.register(new ToggleDamageDisplayCommand());
        }
    }
}
