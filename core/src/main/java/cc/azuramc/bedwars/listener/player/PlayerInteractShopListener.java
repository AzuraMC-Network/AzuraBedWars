package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.shop.gui.ItemShopGUI;
import cc.azuramc.bedwars.shop.gui.TeamShopGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

/**
 * @author an5w1r@163.com
 */
public class PlayerInteractShopListener implements Listener {

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

        if (event.getRightClicked().hasMetadata("Shop")) {
            handleItemShopInteraction(event, gamePlayer);
            return;
        }

        if (event.getRightClicked().hasMetadata("Shop2")) {
            handleTeamShopInteraction(event, gamePlayer);
        }
    }
    
    /**
     * 处理物品商店交互
     */
    private void handleItemShopInteraction(PlayerInteractEntityEvent event, GamePlayer gamePlayer) {
        event.setCancelled(true);
        if (gamePlayer.isSpectator()) {
            return;
        }
        new ItemShopGUI(gamePlayer, 0, gameManager).open();
    }
    
    /**
     * 处理队伍商店交互
     */
    private void handleTeamShopInteraction(PlayerInteractEntityEvent event, GamePlayer gamePlayer) {
        event.setCancelled(true);
        if (gamePlayer.isSpectator()) {
            return;
        }
        new TeamShopGUI(gamePlayer, gameManager).open();
    }
}