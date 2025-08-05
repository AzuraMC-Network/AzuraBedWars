package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * @author An5w1r@163.com
 */
public class PlayerDrinkMilkListener implements Listener {

    private final AzuraBedWars plugin;

    public PlayerDrinkMilkListener(AzuraBedWars plugin) {
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

        if (itemStack.getType() != XMaterial.MILK_BUCKET.get()) {
            return;
        }

        LoggerUtil.debug("PlayerDrinkMilkListener$onDrink | " + player.getName() + " now has trap protection");

        event.setCancelled(true);
        if (VersionUtil.isVersion1_8())
            event.getPlayer().setItemInHand(null);
        else {
            if (event.getHand() == EquipmentSlot.HAND)
                event.getPlayer().getInventory().setItemInMainHand(null);
            else
                event.getPlayer().getInventory().setItemInOffHand(null);
        }

        gamePlayer.sendMessage("&a你喝下了魔法牛奶，将会在30s内不受陷阱效果影响");
        gamePlayer.startTrapProtectionTask();
    }
}
