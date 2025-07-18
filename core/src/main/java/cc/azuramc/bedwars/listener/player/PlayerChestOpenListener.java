package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.team.GameTeam;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * @author An5w1r@163.com
 */
public class PlayerChestOpenListener implements Listener {
    @EventHandler
    public void onChestOpen(PlayerInteractEvent event) {

        if (AzuraBedWars.getInstance().getGameManager().getGameState() != GameState.RUNNING) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        Material blockType = null;
        if (block != null) {
            blockType = block.getType();
        }

        // 只对木箱进行判断
        if (blockType != XMaterial.CHEST.get()) {
            return;
        }

        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player);

        if (gamePlayer.isSpectator()) {
            event.setCancelled(true);
            return;
        }

        for (GameTeam team : AzuraBedWars.getInstance().getGameManager().getGameTeams()) {
            if (block != null && team.getSpawnLocation().distance(block.getLocation()) <= 18) {
                if (!team.getAlivePlayers().isEmpty() && !team.isInTeam(gamePlayer)) {
                    event.setCancelled(true);
                    gamePlayer.sendMessage("&c只有该队伍的玩家可以打开这个箱子");
                }
                break;
            }
        }
    }
}
