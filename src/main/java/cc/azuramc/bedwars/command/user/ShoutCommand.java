package cc.azuramc.bedwars.command.user;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.listener.chat.ChatListener;
import cc.azuramc.bedwars.util.CommandUtil;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.bukkit.BukkitCommandActor;

public class ShoutCommand {

    @Dependency
    private AzuraBedWars pluginDependency;

    @Command("shout")
    public void shoutInGame(BukkitCommandActor actor, @Default("") String message) {
        // 控制台执行
        if (actor.isConsole()) {
            CommandUtil.sendLayout(actor, "&c该命令只能由玩家执行！");
            return;
        }

        GameManager gameManager = AzuraBedWars.getInstance().getGameManager();
        GamePlayer gamePlayer = GamePlayer.get(actor.getUniqueId());

        // 游戏未开始时使用
        if (gameManager.getGameState() != GameState.RUNNING) {
            CommandUtil.sendLayout(actor, "&c该命令只能在游戏开始后使用！");
            return;
        }

        // 消息为空时
        if (message.isEmpty()) {
            CommandUtil.sendLayout(actor, "&c用法: /shout <message>");
            return;
        }

        // 发全局消息
        ChatListener.handleInGameChat(gamePlayer.getPlayer(), gamePlayer, ChatListener.GLOBAL_CHAT_PREFIX + message);
    }
}
