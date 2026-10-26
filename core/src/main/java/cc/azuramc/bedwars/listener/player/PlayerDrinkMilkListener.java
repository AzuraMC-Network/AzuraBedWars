package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.item.special.SpecialItems;
import cc.azuramc.bedwars.util.LoggerUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author an5w1r@163.com
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
        if (gamePlayer == null) {
            return;
        }

        ItemStack itemStack = event.getItem();
        if (itemStack.getType() == Material.AIR || itemStack.getItemMeta() == null) {
            return;
        }

        if (!SpecialItems.is(itemStack, SpecialItems.MAGIC_MILK)) {
            return;
        }

        LoggerUtil.debug("PlayerDrinkMilkListener$onDrink | " + player.getName() + " now has trap protection");

        event.setCancelled(true);
        PlayerUtil.setItemInHand(player, itemStack, true);

        gamePlayer.sendMessage(AzuraBedWars.getInstance().getLang().getItems().getMagicMilkDrunk());
        gamePlayer.startTrapProtectionTask();
    }
}
