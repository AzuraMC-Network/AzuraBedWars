package cc.azuramc.bedwars.command.user;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.listener.chat.ChatListener;
import cc.azuramc.bedwars.util.CommandUtil;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.bukkit.BukkitCommandActor;

/**
 * @author ant1aura@qq.com
 */
public class ShoutCommand {

    @Command("shout")
    public void shoutInGame(BukkitCommandActor actor, @Default("") String message) {
        // 控制台执行
        if (actor.isConsole()) {
            CommandUtil.sendLayout(actor, AzuraBedWars.getInstance().getLang().getCommand().getPlayersOnly());
            return;
        }

        GameManager gameManager = AzuraBedWars.getInstance().getGameManager();
        GamePlayer gamePlayer = GamePlayer.get(actor.getUniqueId());
        if (gamePlayer == null) {
            return;
        }

        // 游戏未开始时使用
        if (gameManager.getGameState() != GameState.RUNNING) {
            CommandUtil.sendLayout(actor, AzuraBedWars.getInstance().getLang().getCommand().getOnlyInGame());
            return;
        }

        // 消息为空时
        if (message.isEmpty()) {
            CommandUtil.sendLayout(actor, AzuraBedWars.getInstance().getLang().getCommand().getShoutUsage());
            return;
        }

        // 发全局消息
        ChatListener.handleInGameChat(gamePlayer, AzuraBedWars.getInstance().getGameplayConfig().getChat().getGlobalChatPrefix() + message);
    }
}
