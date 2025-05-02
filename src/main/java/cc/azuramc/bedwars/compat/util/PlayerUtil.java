package cc.azuramc.bedwars.compat.util;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.VersionUtil;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerUtil {

    private static final AzuraBedWars PLUGIN = AzuraBedWars.getInstance();

    /**
     * 获取玩家手中的物品
     * @param player 玩家
     * @return 玩家手中物品
     */
    @SuppressWarnings("deprecation")
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
    @SuppressWarnings("deprecation")
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
    @SuppressWarnings("deprecation")
    public static void hidePlayer(Player player, Player target) {
        if (VersionUtil.isLessThan113()) {
            player.hidePlayer(target);
        } else {
            player.hidePlayer(PLUGIN, target);
        }
    }

    /**
     * 显示玩家
     * @param player 玩家
     * @param target 准备显示的目标
     */
    @SuppressWarnings("deprecation")
    public static void showPlayer(Player player, Player target) {
        if (VersionUtil.isLessThan113()) {
            player.showPlayer(target);
        } else {
            player.showPlayer(PLUGIN, target);
        }
    }

    /**
     * 调用PlayerRespawnEvent
     * @param player 玩家
     * @param respawnLocation 复活地点
     */
    @SuppressWarnings("deprecation")
    public static void callPlayerRespawnEvent(Player player, Location respawnLocation) {
        if (VersionUtil.isLessThan113()) {
            Bukkit.getPluginManager().callEvent(new PlayerRespawnEvent(player, respawnLocation, false));
        } else {
            Bukkit.getPluginManager().callEvent(new PlayerRespawnEvent(player, respawnLocation, false,
                    false, PlayerRespawnEvent.RespawnReason.PLUGIN));
        }
    }

    /**
     * 设置玩家为飞行状态
     * @param player 玩家
     */
    public static void setFlying(Player player) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ABILITIES);
        packet.getModifier().writeDefaults();
        packet.getFloat().write(0, 0.05F);
        packet.getBooleans().write(1, true);
        packet.getBooleans().write(2, true);
        protocolManager.sendServerPacket(player, packet);
    }

}
