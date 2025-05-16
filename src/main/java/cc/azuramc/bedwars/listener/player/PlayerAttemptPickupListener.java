package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.event.impl.PlayerAttemptPickupItemEvent;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

/**
 * @author ant1aura@qq.com
 */
public class PlayerAttemptPickupListener implements Listener {

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @EventHandler
    public void onAttemptPickup(PlayerAttemptPickupItemEvent event) {

        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        Item item = event.getItem();
        ItemStack itemStack = item.getItemStack();

        if (gamePlayer == null) {
            return;
        }

        if (gameManager.getGameState() != GameState.RUNNING) {
            return;
        }

        if (gamePlayer.isSpectator()) {
            return;
        }

        if (gamePlayer.isAfk()) {
            return;
        }

        PickupItemHandler.handleIngotPickup(itemStack, player, gamePlayer, item);
        PickupItemHandler.handleDiamondPickup(itemStack, player, gamePlayer, item);
        PickupItemHandler.handleEmeraldPickup(itemStack, player, gamePlayer, item);
    }
}
