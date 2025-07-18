package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

/**
 * @author ant1aura@qq.com
 */
public class PlayerResourcePutListener implements Listener {

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player);
        Action action = event.getAction();

        if (gamePlayer == null
                || gameManager.getGameState() != GameState.RUNNING
                || !player.isSneaking()
                || event.getItem() == null
                || action != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) return;

        Inventory targetInventory = null;
        Material blockType = block.getType();

        switch (blockType) {
            case CHEST:
            case TRAPPED_CHEST:
                if (block.getState() instanceof Chest) {
                    targetInventory = ((Chest) block.getState()).getInventory();
                }
                break;
            case ENDER_CHEST:
                targetInventory = player.getEnderChest();
                break;
            default:
                return;
        }

        if (targetInventory == null) return;

        ItemStack item = event.getItem();
        ItemStack resource;
        switch (item.getType()) {
            case IRON_INGOT:
            case GOLD_INGOT:
            case DIAMOND:
            case EMERALD:
                resource = item;
                break;
            default:
                return;
        }

        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item1 = inventory.getItem(i);
            if (item1 != null && item1.getType() == resource.getType()) {
                HashMap<Integer, ItemStack> left = targetInventory.addItem(item1.clone());
                if (left.isEmpty()) {
                    inventory.setItem(i, null);
                } else {
                    inventory.setItem(i, left.get(0));
                }
            }
        }
    }
}
