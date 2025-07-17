package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

/**
 * @author An5w1r@163.com
 */
public class PlayerInvisibilityListener implements Listener {

    private AzuraBedWars plugin;

    public PlayerInvisibilityListener(AzuraBedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDrink(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

//        for (GameTeam gameTeam : plugin.getGameManager().getGameTeams()) {
//
//        }
    }
}
