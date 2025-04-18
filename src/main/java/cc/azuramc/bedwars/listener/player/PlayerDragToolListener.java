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

public class PlayerDragToolListener implements Listener {

    // 定义受保护的工具类型
    private static final Set<Material> PROTECTED_TOOLS = EnumSet.of(
            MaterialWrapper.DIAMOND_PICKAXE(), MaterialWrapper.GOLDEN_PICKAXE(), MaterialWrapper.IRON_PICKAXE(), MaterialWrapper.STONE_PICKAXE(), MaterialWrapper.WOODEN_PICKAXE(), Material.NETHERITE_PICKAXE,
            MaterialWrapper.DIAMOND_AXE(), MaterialWrapper.GOLDEN_AXE(), MaterialWrapper.IRON_AXE(), MaterialWrapper.STONE_AXE(), MaterialWrapper.WOODEN_AXE(), Material.NETHERITE_AXE,
            MaterialWrapper.DIAMOND_SWORD(), MaterialWrapper.GOLDEN_SWORD(), MaterialWrapper.IRON_SWORD(), MaterialWrapper.STONE_SWORD(), MaterialWrapper.STONE_SWORD(), Material.NETHERITE_SWORD
    );

    // 判断是否为木箱或末影箱
    private boolean isForbiddenContainer(Inventory inv) {
        InventoryType type = inv.getType();
        return type == InventoryType.CHEST || type == InventoryType.ENDER_CHEST;
    }

    // 检查是否是唯一一把该类型工具
    private boolean isOnlyOneTool(Player player, ItemStack tool) {
        if (tool == null || !PROTECTED_TOOLS.contains(tool.getType())) return false;

        Material type = tool.getType();
        int count = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == type) {
                count += item.getAmount();
                if (count > 1) return false;
            }
        }

        return count == 1;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;

        ItemStack currentItem = event.getCurrentItem();

        // 拖动到木箱或末影箱里时
        if (clickedInventory != player.getInventory() && isForbiddenContainer(clickedInventory)) {
            if (isOnlyOneTool(player, currentItem)) {
                player.sendMessage("你不能将你唯一的一把工具放入箱子！");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        for (int slot : event.getRawSlots()) {
            Inventory inv = event.getView().getInventory(slot);
            if (inv == null || !isForbiddenContainer(inv)) continue;

            ItemStack draggedItem = event.getOldCursor();
            if (isOnlyOneTool(player, draggedItem)) {
                player.sendMessage("你不能将你唯一的一把工具拖入箱子！");
                event.setCancelled(true);
                return;
            }
        }
    }
}
