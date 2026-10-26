package cc.azuramc.bedwars.command.user;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.util.CommandUtil;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.BukkitCommandActor;

/**
 * @author ant1aura@qq.com
 */
@Command({"displaydamage", "dd"})
public class ToggleDamageDisplayCommand {

    @DefaultFor({"displaydamage", "dd"})
    public void toggleDisplayDamage(BukkitCommandActor actor) {
        CommandUtil.sendLayout(actor, AzuraBedWars.getInstance().getLang().getCommand().getDamageDisplayUsage());
    }

    @Subcommand("arrow")
    public void toggleArrowDamage(Player player) {

        if (!AzuraBedWars.getInstance().getGameManager().isArrowDisplayEnabled()) {
            player.sendMessage(AzuraBedWars.getInstance().getLang().getCommand().getDamageDisplayDisabled());
            return;
        }

        if (AzuraBedWars.getInstance().getGameManager().getGameState() != GameState.RUNNING) {
            player.sendMessage(AzuraBedWars.getInstance().getLang().getCommand().getOnlyInGame());
            return;
        }

        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        if (gamePlayer == null) {
            return;
        }

        if (gamePlayer.isViewingArrowDamage()) {
            gamePlayer.setViewingArrowDamage(false);
            player.sendMessage(AzuraBedWars.getInstance().getLang().getCommand().getArrowDamageOff());
        } else {
            gamePlayer.setViewingArrowDamage(true);
            player.sendMessage(AzuraBedWars.getInstance().getLang().getCommand().getArrowDamageOn());
        }
    }

    @Subcommand("attack")
    public void toggleAttackDamage(Player player) {

        if (!AzuraBedWars.getInstance().getGameManager().isAttackDisplayEnabled()) {
            player.sendMessage(AzuraBedWars.getInstance().getLang().getCommand().getDamageDisplayDisabled());
            return;
        }

        if (AzuraBedWars.getInstance().getGameManager().getGameState() != GameState.RUNNING) {
            player.sendMessage(AzuraBedWars.getInstance().getLang().getCommand().getOnlyInGame());
            return;
        }

        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        if (gamePlayer == null) {
            return;
        }

        if (gamePlayer.isViewingAttackDamage()) {
            gamePlayer.setViewingAttackDamage(false);
            player.sendMessage(AzuraBedWars.getInstance().getLang().getCommand().getAttackDamageOff());
        } else {
            gamePlayer.setViewingAttackDamage(true);
            player.sendMessage(AzuraBedWars.getInstance().getLang().getCommand().getAttackDamageOn());
        }
    }
}
