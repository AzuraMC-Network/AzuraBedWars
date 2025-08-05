package cc.azuramc.bedwars.compat.util;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.game.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

public class PlayerUtil {

    private static final AzuraBedWars PLUGIN = AzuraBedWars.getInstance();

    /**
     * 获取玩家手中的物品
     * @param player 玩家
     * @return 玩家手中物品
     */
    @SuppressWarnings("deprecation")
    public static ItemStack getItemInHand(Player player) {

        if (VersionUtil.isLessThan1_13()) {
            return player.getItemInHand();
        }

        return player.getInventory().getItemInMainHand();
    }

    /**
     * 设置玩家手中的物品
     * @param player 玩家
     * @param item 要设置的物品
     */
    @SuppressWarnings("deprecation")
    public static void setItemInHand(Player player, ItemStack item) {
        if (VersionUtil.isLessThan1_13()) {
            player.setItemInHand(item);
            return;
        }

        player.getInventory().setItemInMainHand(item);
    }

    public static void hidePlayer(GamePlayer gamePlayer, List<GamePlayer> targetList) {
        if (targetList.isEmpty()) {
            return;
        }

        for (GamePlayer targetPlayer : targetList) {
            if (targetList == gamePlayer) {
                continue;
            }
            hidePlayer(gamePlayer.getPlayer(), targetPlayer.getPlayer());
        }
    }

    public static void showPlayer(GamePlayer gamePlayer, List<GamePlayer> targetList) {
        if (targetList.isEmpty()) {
            return;
        }

        for (GamePlayer targetPlayer : targetList) {
            if (targetList == gamePlayer) {
                continue;
            }
            showPlayer(gamePlayer.getPlayer(), targetPlayer.getPlayer());
        }
    }

    public static void hidePlayer(List<GamePlayer> playerList, GamePlayer target) {
        if (playerList.isEmpty()) {
            return;
        }

        for (GamePlayer player : playerList) {
            if (player == target) {
                continue;
            }
            hidePlayer(player.getPlayer(), target.getPlayer());
        }
    }

    public static void showPlayer(List<GamePlayer> playerList, GamePlayer target) {
        if (playerList.isEmpty()) {
            return;
        }

        for (GamePlayer player : playerList) {
            if (player == target) {
                continue;
            }
            showPlayer(player.getPlayer(), target.getPlayer());
        }
    }

    /**
     * 隐藏玩家
     * @param player 玩家
     * @param target 准备隐藏的目标
     */
    @SuppressWarnings("deprecation")
    public static void hidePlayer(Player player, Player target) {
        if (VersionUtil.isLessThan1_13()) {
            player.hidePlayer(target);
            return;
        }
        player.hidePlayer(PLUGIN, target);
    }

    /**
     * 显示玩家
     * @param player 玩家
     * @param target 准备显示的目标
     */
    @SuppressWarnings("deprecation")
    public static void showPlayer(Player player, Player target) {
        if (VersionUtil.isLessThan1_13()) {
            player.showPlayer(target);
            return;
        }

        player.showPlayer(PLUGIN, target);
    }

    /**
     * 调用PlayerRespawnEvent
     * @param player 玩家
     * @param respawnLocation 复活地点
     */
    @SuppressWarnings("deprecation")
    public static void callPlayerRespawnEvent(Player player, Location respawnLocation) {
        if (VersionUtil.isLessThan1_13()) {
            Bukkit.getPluginManager().callEvent(new PlayerRespawnEvent(player, respawnLocation, false));
        } else {
            Bukkit.getPluginManager().callEvent(new PlayerRespawnEvent(player, respawnLocation, false,
                    false, PlayerRespawnEvent.RespawnReason.PLUGIN));
        }
    }

    /**
     * 获取玩家最大血量
     * @param player 玩家
     */
    @SuppressWarnings("deprecation")
    public static double getMaxHealth(Player player) {
        if (VersionUtil.isLessThan1_13()) {
            return player.getMaxHealth();
        }

        return Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
    }

}
