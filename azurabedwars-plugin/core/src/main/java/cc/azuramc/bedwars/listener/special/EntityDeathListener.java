package cc.azuramc.bedwars.listener.special;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * @author ImCur_
 */
public class EntityDeathListener implements Listener {

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        // 只处理非玩家实体的死亡
        if (e.getEntity() instanceof Player)
            return;

        if (gameManager.getGameState() != GameState.RUNNING) {
            return;
        }

        e.setDroppedExp(0);
    }

}
