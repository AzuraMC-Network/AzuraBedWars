package cc.azuramc.bedwars.command.user;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.util.MessageUtil;
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

        if (gameManager.getGameState() != GameState.WAITING) {
            actor.getSender().sendMessage(MessageUtil.color("&c游戏已经开始或处于结算状态!"));
            return;
        }

        int min = gameManager.getMapData().getPlayers().getMin();
        if (GamePlayer.getOnlinePlayers().size() < min) {
            actor.getSender().sendMessage(MessageUtil.color("&c游戏人数不足! 最少需要" + min + "人才能&n开始游戏&c."));
            return;
        }

        gameManager.setForceStarted(true);
        gameManager.start();
    }
}
