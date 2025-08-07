package cc.azuramc.bedwars.command;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.command.admin.AdminCommand;
import cc.azuramc.bedwars.command.admin.MapCommand;
import cc.azuramc.bedwars.command.admin.SetupCommand;
import cc.azuramc.bedwars.command.exception.CommandExceptionHandler;
import cc.azuramc.bedwars.command.user.ShoutCommand;
import cc.azuramc.bedwars.command.user.StartCommand;
import cc.azuramc.bedwars.command.user.ToggleDamageDisplayCommand;
import cc.azuramc.bedwars.game.GameManager;
import lombok.Getter;
import revxrsal.commands.bukkit.BukkitCommandHandler;

/**
 * 命令注册管理类
 *
 * @author an5w1r@163.com
 */
public class CommandRegistry {

    @Getter
    private final BukkitCommandHandler handler;
    private final AzuraBedWars plugin;

    public CommandRegistry(AzuraBedWars plugin) {
        this.plugin = plugin;
        handler = BukkitCommandHandler.create(plugin);
        handler.registerDependency(AzuraBedWars.class, plugin);
        handler.setExceptionHandler(new CommandExceptionHandler());

        registerCommonCommands();

        if (plugin.getSettingsConfig().isEditorMode()) {
            registerEditorCommands();
        } else {
            registerGameCommands();
        }
    }

    /**
     * 注册所有模式下都可用的命令
     */
    private void registerCommonCommands() {
        handler.register(new AdminCommand());
    }

    /**
     * 注册编辑模式下的命令
     */
    private void registerEditorCommands() {
        handler.register(new MapCommand());
        handler.register(new SetupCommand(plugin));
    }

    /**
     * 注册游戏模式下的命令
     */
    private void registerGameCommands() {
        handler.register(new StartCommand());
        handler.register(new ShoutCommand());

        // 检查游戏管理器是否已初始化
        GameManager gameManager = plugin.getGameManager();
        if (gameManager != null && gameManager.isArrowDisplayEnabled() && gameManager.isAttackDisplayEnabled()) {
            handler.register(new ToggleDamageDisplayCommand());
        }
    }

}
