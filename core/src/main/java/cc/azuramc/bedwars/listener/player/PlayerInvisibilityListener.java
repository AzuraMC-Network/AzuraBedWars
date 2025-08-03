package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.util.LoggerUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author An5w1r@163.com
 */
public class PlayerInvisibilityListener implements Listener {

    private final AzuraBedWars plugin;
    private static final Map<GamePlayer, BukkitRunnable> invisibilityTasks = new HashMap<>();

    public PlayerInvisibilityListener(AzuraBedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDrink(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        if (plugin.getGameManager().getGameState() != GameState.RUNNING) {
            return;
        }

        GamePlayer gamePlayer = GamePlayer.get(player);

        ItemStack itemStack = event.getItem();
        if (itemStack.getType() == Material.AIR || itemStack.getItemMeta() == null) {
            return;
        }

        String itemName =  itemStack.getItemMeta().getDisplayName();
        if (itemName == null || itemName.isEmpty()) {
            return;
        }

        if (itemName.contains("隐身")) {
            LoggerUtil.debug("PlayerInvisibilityListener$onDrink | invisible player is " + player.getName());

            // 如果玩家已经隐身，取消之前的任务
            if (gamePlayer.isInvisible()) {
                gamePlayer.endInvisibility();
            }

            gamePlayer.startInvisibilityTask();
        }
    }

}
