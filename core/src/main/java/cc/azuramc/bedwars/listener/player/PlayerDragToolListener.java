package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.util.ToolSetUtil;
import com.cryptomorin.xseries.inventory.XInventoryView;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * @author an5w1r@163.com
 */
public class PlayerDragToolListener implements Listener {

    /**
     * 判断是否为木箱或末影箱
     * @param inv 想要判断的背包
     * @return 如果是箱子或者末影箱 则返回true
     */
    private boolean isForbiddenContainer(Inventory inv) {
        InventoryType type = inv.getType();
        return type == InventoryType.CHEST || type == InventoryType.ENDER_CHEST;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory targetInventory = event.getInventory();
        if (!isForbiddenContainer(targetInventory)) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (ToolSetUtil.isOnlyOneTool(player, item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory topInventory = XInventoryView.of(event.getView()).getTopInventory();
        if (!isForbiddenContainer(topInventory)) {
            return;
        }

        ItemStack dragged = event.getOldCursor();
        if (ToolSetUtil.isOnlyOneTool(player, dragged)) {
            event.setCancelled(true);
        }
    }
}
