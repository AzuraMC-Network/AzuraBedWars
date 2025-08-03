package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

/**
 * @author An5w1r@163.com
 */
public class PlayerUseBucketListener implements Listener {

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @EventHandler
    public void onBucket(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player);

        if (gamePlayer == null || gameManager.getGameState() != GameState.RUNNING) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(AzuraBedWars.getInstance(), () -> player.getInventory().removeItem(XMaterial.BUCKET.parseItem()), 2L);
    }

    @EventHandler
    public void onMilk(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() != XMaterial.MILK_BUCKET.get())
            return;

        Player p = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(p);

        if (gamePlayer == null || gameManager.getGameState() != GameState.RUNNING)
            return;

        Bukkit.getScheduler().runTaskLater(AzuraBedWars.getInstance(), () -> p.getInventory().remove(Material.BUCKET), 2L);
    }

}
