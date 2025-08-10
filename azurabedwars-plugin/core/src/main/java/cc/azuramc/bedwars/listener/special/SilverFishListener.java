package cc.azuramc.bedwars.listener.special;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.CustomEntityManager;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

/**
 * @author ant1aura@qq.com
 */
public class SilverFishListener implements Listener {

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @EventHandler
    public void onThrown(ProjectileHitEvent event) {
        Location location = event.getEntity().getLocation();


        if (gameManager.getGameState() != GameState.RUNNING) {
            return;
        }

        if (!(event.getEntity().getShooter() instanceof Player shooter) || !(event.getEntity() instanceof Snowball)) {
            return;
        }

        GamePlayer gamePlayer = GamePlayer.get(shooter);

        int despawn = 15;
        new CustomEntityManager(AzuraBedWars.getInstance().getNmsAccess().spawnSilverfish(location.add(0, 0.5, 0), gamePlayer,
                0.25, 8, 4), gamePlayer, despawn);
    }
}
