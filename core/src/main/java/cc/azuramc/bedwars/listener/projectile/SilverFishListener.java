package cc.azuramc.bedwars.listener.projectile;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameState;
import com.cryptomorin.xseries.XEntityType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.Objects;

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

        Objects.requireNonNull(location.getWorld()).spawnEntity(location, Objects.requireNonNull(XEntityType.SILVERFISH.get()));
    }
}
