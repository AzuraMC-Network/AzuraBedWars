package cc.azuramc.bedwars.listener.special;

import cc.azuramc.bedwars.game.CustomEntityManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

/**
 * @author an5w1r@163.com
 */
public class CustomEntityTargetListener implements Listener {

    /** Vanilla also targets outside the goals, such as iron golems on collision */
    @EventHandler(ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() == null) {
            return;
        }

        CustomEntityManager manager = CustomEntityManager.getByUUID(event.getEntity().getUniqueId());
        if (manager != null && !manager.isEnemy(event.getTarget())) {
            event.setCancelled(true);
        }
    }
}
