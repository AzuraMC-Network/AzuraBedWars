package cc.azuramc.bedwars.listeners.block;

import cc.azuramc.bedwars.compat.wrapper.MaterialWrapper;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class TNTPlacementHandler {

    /**
     * 处理TNT放置
     *
     * @param event 方块放置事件
     * @param player 玩家
     */
    public static void handleTNTPlacement(BlockPlaceEvent event, Player player) {
        event.setCancelled(true);
        Block block = event.getBlock();
        block.setType(MaterialWrapper.AIR());

        // 生成已激活的TNT实体
        TNTPrimed tnt = block.getWorld().spawn(block.getLocation().add(0.5D, 0.0D, 0.5D), TNTPrimed.class);
        tnt.setVelocity(new Vector(0, 0, 0));

        // 减少玩家物品栏中的TNT数量
        consumeItem(player, MaterialWrapper.TNT());
    }

    /**
     * 减少玩家物品栏中指定物品的数量
     *
     * @param player 玩家
     * @param material 物品类型
     */
    private static void consumeItem(Player player, Material material) {
        ItemStack item = PlayerUtil.getItemInHand(player);
        if (item != null && item.getType() == material) {
            if (item.getAmount() == 1) {
                PlayerUtil.setItemInHand(player, null);
            } else {
                item.setAmount(item.getAmount() - 1);
            }
        }
    }
}
