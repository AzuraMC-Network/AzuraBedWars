package cc.azuramc.bedwars.compat.util;

import cc.azuramc.bedwars.compat.VersionUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerUtil {

    /**
     * 获取玩家手中的物品
     * @param player 玩家
     * @return 玩家手中物品
     */
    public static ItemStack getItemInHand(Player player) {

        if (VersionUtil.isLessThan113()) {
            return player.getItemInHand();
        } else {
            return player.getInventory().getItemInMainHand();
        }
    }

    /**
     * 设置玩家手中的物品
     * @param player 玩家
     * @param item 要设置的物品
     */
    public static void setItemInHand(Player player, ItemStack item) {
        if (VersionUtil.isLessThan113()) {
            player.setItemInHand(item);
        } else {
            player.getInventory().setItemInMainHand(item);
        }
    }
}
