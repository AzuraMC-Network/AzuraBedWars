package cc.azuramc.bedwars.util;

import cc.azuramc.bedwars.compat.VersionUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Set;

public class ToolSetUtil {

    /**
     * 定义受保护的工具类型
     */
    public static final Set<Material> PROTECTED_TOOLS;

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
                XMaterial.WOODEN_SWORD.get(),

                XMaterial.SHEARS.get()
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
     * 检查是否是唯一一把该类型工具
     * @param player 要检查的玩家
     * @param tool 工具ItemStack
     * @return 如果只有一个工具同类型工具则返回true
     */
    public static boolean isOnlyOneTool(Player player, ItemStack tool) {
        if (tool == null || !ToolSetUtil.PROTECTED_TOOLS.contains(tool.getType())) {
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
}
