package cc.azuramc.bedwars.command.user;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

/**
 * @author ant1aura@qq.com
 */
public class StartCommand {

    @Dependency
    private AzuraBedWars pluginDependency;

    @Command({"bedwars start", "bw start", "azurabedwars start"})
    @CommandPermission("azurabedwars.forcestart")
    public void forceStartGame(BukkitCommandActor actor) {
        GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

        gameManager.setForceStarted(true);
        gameManager.start();
    }
}
