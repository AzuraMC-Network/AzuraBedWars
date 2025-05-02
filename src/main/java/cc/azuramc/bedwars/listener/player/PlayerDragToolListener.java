package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.compat.wrapper.MaterialWrapper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author an5w1r@163.com
 */
public class PlayerDragToolListener implements Listener {

    /**
     * 定义受保护的工具类型
     */
    private static final Set<Material> PROTECTED_TOOLS = EnumSet.of(
            MaterialWrapper.DIAMOND_PICKAXE(), MaterialWrapper.GOLDEN_PICKAXE(), MaterialWrapper.IRON_PICKAXE(), MaterialWrapper.STONE_PICKAXE(), MaterialWrapper.WOODEN_PICKAXE(), Material.NETHERITE_PICKAXE,
            MaterialWrapper.DIAMOND_AXE(), MaterialWrapper.GOLDEN_AXE(), MaterialWrapper.IRON_AXE(), MaterialWrapper.STONE_AXE(), MaterialWrapper.WOODEN_AXE(), Material.NETHERITE_AXE,
            MaterialWrapper.DIAMOND_SWORD(), MaterialWrapper.GOLDEN_SWORD(), MaterialWrapper.IRON_SWORD(), MaterialWrapper.STONE_SWORD(), MaterialWrapper.WOODEN_SWORD(), Material.NETHERITE_SWORD
    );

    /**
     * 判断是否为木箱或末影箱
     * @param inv 想要判断的背包
     * @return 如果是箱子或者末影箱 则返回true
     */
    private boolean isForbiddenContainer(Inventory inv) {
        InventoryType type = inv.getType();
        return type == InventoryType.CHEST || type == InventoryType.ENDER_CHEST;
    }

    /**
     * 检查是否是唯一一把该类型工具
     * @param player 要检查的玩家
     * @param tool 工具ItemStack
     * @return 如果只有一个工具同类型工具则返回true
     */
    private boolean isOnlyOneTool(Player player, ItemStack tool) {
        if (tool == null || !PROTECTED_TOOLS.contains(tool.getType())) {
            return false;
        }

        Material type = tool.getType();
        int count = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == type) {
                count += item.getAmount();
                if (count > 1) {
                    return false;
                }
            }
        }

        return count == 1;
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
        if (isOnlyOneTool(player, item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory topInventory = event.getView().getTopInventory();
        if (!isForbiddenContainer(topInventory)) {
            return;
        }

        ItemStack dragged = event.getOldCursor();
        if (isOnlyOneTool(player, dragged)) {
            event.setCancelled(true);
        }
    }
}
