package cc.azuramc.bedwars.command.user;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.util.MessageUtil;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

/**
 * @author ant1aura@qq.com
 */
public class StartCommand {

    @Command({"azurabedwars start", "bedwars start", "abw start", "bw start"})
    @CommandPermission("azurabedwars.forcestart")
    public void forceStartGame(BukkitCommandActor actor) {
        GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

        if (gameManager.getGameState() != GameState.WAITING) {
            actor.getSender().sendMessage(AzuraBedWars.getInstance().getLang().getCommand().getGameStarted());
            return;
        }

        if (!gameManager.canStart()) {
            int min = gameManager.getMapData().getPlayers().getMin();
            actor.getSender().sendMessage(MessageUtil.fill(AzuraBedWars.getInstance().getLang().getCommand().getNotEnoughPlayers(), "min", min));
            return;
        }

        gameManager.start();
    }
}
