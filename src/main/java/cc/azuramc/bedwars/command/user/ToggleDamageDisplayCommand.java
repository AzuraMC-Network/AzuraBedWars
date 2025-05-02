package cc.azuramc.bedwars.command.user;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.util.ChatColorUtil;
import cc.azuramc.bedwars.util.CommandUtil;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.BukkitCommandActor;

/**
 * @author ant1aura@qq.com
 */
@Command({"displaydamage", "dd"})
public class ToggleDamageDisplayCommand {

    @Dependency
    private AzuraBedWars pluginDependency;

    @DefaultFor({"displaydamage", "dd"})
    public void toggleDisplayDamage(BukkitCommandActor actor) {
        CommandUtil.sendLayout(actor, "&c用法: /displaydamage <arrow|attack>");
    }

    @Subcommand("arrow")
    public void toggleArrowDamage(Player player) {

        if (!AzuraBedWars.getInstance().getGameManager().isArrowDisplayEnabled()) {
            player.sendMessage(ChatColorUtil.color("&c该功能未启用！"));
            return;
        }

        if (AzuraBedWars.getInstance().getGameManager().getGameState() != GameState.RUNNING) {
            player.sendMessage(ChatColorUtil.color("&c该命令只能在游戏开始后使用！"));
            return;
        }

        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

        if (gamePlayer.isViewingArrowDamage()) {
            gamePlayer.setViewingArrowDamage(false);
            player.sendMessage(ChatColorUtil.color("&c弓箭伤害显示已关闭！"));
        } else {
            gamePlayer.setViewingArrowDamage(true);
            player.sendMessage(ChatColorUtil.color("&a弓箭伤害显示已开启！"));
        }
    }

    @Subcommand("attack")
    public void toggleAttackDamage(Player player) {

        if (!AzuraBedWars.getInstance().getGameManager().isAttackDisplayEnabled()) {
            player.sendMessage(ChatColorUtil.color("&c该功能未启用！"));
            return;
        }

        if (AzuraBedWars.getInstance().getGameManager().getGameState() != GameState.RUNNING) {
            player.sendMessage(ChatColorUtil.color("&c该命令只能在游戏开始后使用！"));
            return;
        }

        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

        if (gamePlayer.isViewingAttackDamage()) {
            gamePlayer.setViewingAttackDamage(false);
            player.sendMessage(ChatColorUtil.color("&c攻击伤害显示已关闭！"));
        } else {
            gamePlayer.setViewingAttackDamage(true);
            player.sendMessage(ChatColorUtil.color("&a攻击伤害显示已开启！"));
        }
    }
}
