package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.compat.VersionUtil;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.inventory.XInventoryView;
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
    private static final Set<Material> PROTECTED_TOOLS;
    
    static {
        // 初始化基本工具集合
        EnumSet<Material> tools = EnumSet.of(
            XMaterial.DIAMOND_PICKAXE.get(), 
            XMaterial.GOLDEN_PICKAXE.get(), 
            XMaterial.IRON_PICKAXE.get(), 
            XMaterial.STONE_PICKAXE.get(), 
            XMaterial.WOODEN_PICKAXE.get(),
            
            XMaterial.DIAMOND_AXE.get(), 
            XMaterial.GOLDEN_AXE.get(), 
            XMaterial.IRON_AXE.get(), 
            XMaterial.STONE_AXE.get(), 
            XMaterial.WOODEN_AXE.get(),
            
            XMaterial.DIAMOND_SWORD.get(), 
            XMaterial.GOLDEN_SWORD.get(), 
            XMaterial.IRON_SWORD.get(), 
            XMaterial.STONE_SWORD.get(), 
            XMaterial.WOODEN_SWORD.get()
        );
        
        // 只有1.16+版本才添加下界合金工具
        if (!VersionUtil.isLessThan116()) {
            // 确保材质存在再添加
            Material netheritePickaxe = XMaterial.NETHERITE_PICKAXE.get();
            Material netheriteAxe = XMaterial.NETHERITE_AXE.get();
            Material netheriteSword = XMaterial.NETHERITE_SWORD.get();
            
            if (netheritePickaxe != null) {
                tools.add(netheritePickaxe);
            }
            if (netheriteAxe != null) {
                tools.add(netheriteAxe);
            }
            if (netheriteSword != null) {
                tools.add(netheriteSword);
            }
        }
        
        PROTECTED_TOOLS = tools;
    }

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

        Inventory topInventory = XInventoryView.of(event.getView()).getTopInventory();
        if (!isForbiddenContainer(topInventory)) {
            return;
        }

        ItemStack dragged = event.getOldCursor();
        if (isOnlyOneTool(player, dragged)) {
            event.setCancelled(true);
        }
    }
}
