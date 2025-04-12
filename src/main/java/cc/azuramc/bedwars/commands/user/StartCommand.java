package cc.azuramc.bedwars.commands.user;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public class StartCommand {

    @Dependency
    private AzuraBedWars pluginDependency;

    @Command({"bedwars start", "bw start", "azurabedwars start"})
    @CommandPermission("azurabedwars.forcestart")
    public void forceStartGame(BukkitCommandActor actor) {
        GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

        gameManager.setForceStart(true);
        gameManager.start();
    }
}
