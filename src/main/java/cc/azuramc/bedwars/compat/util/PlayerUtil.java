package cc.azuramc.bedwars.compat.util;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerUtil {

    private static final AzuraBedWars plugin = AzuraBedWars.getInstance();

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

    /**
     * 隐藏玩家
     * @param player 玩家
     * @param target 准备隐藏的目标
     */
    public static void hidePlayer(Player player, Player target) {
        if (VersionUtil.isLessThan113()) {
            player.hidePlayer(target);
        } else {
            player.hidePlayer(plugin, target);
        }
    }

    /**
     * 显示玩家
     * @param player 玩家
     * @param target 准备显示的目标
     */
    public static void showPlayer(Player player, Player target) {
        if (VersionUtil.isLessThan113()) {
            player.showPlayer(target);
        } else {
            player.showPlayer(plugin, target);
        }
    }

    /**
     * 调用PlayerRespawnEvent
     * @param player 玩家
     * @param respawnLocation 复活地点
     */
    public static void callPlayerRespawnEvent(Player player, Location respawnLocation) {
        if (VersionUtil.isLessThan113()) {
            Bukkit.getPluginManager().callEvent(new PlayerRespawnEvent(player, respawnLocation, false));
        } else {
            Bukkit.getPluginManager().callEvent(new PlayerRespawnEvent(player, respawnLocation, false,
                    false, PlayerRespawnEvent.RespawnReason.PLUGIN));
        }
    }

}
