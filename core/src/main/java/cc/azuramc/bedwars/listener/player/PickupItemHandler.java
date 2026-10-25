package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.*;
import cc.azuramc.bedwars.game.economy.EconomyMode;
import cc.azuramc.bedwars.util.MapUtil;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

/**
 * 物品拾取逻辑处理工具类
 * 统一处理两种物品拾取事件的共同逻辑
 *
 * @author an5w1r@163.com
 */
public class PickupItemHandler {

    private static final GameManager GAME_MANAGER = AzuraBedWars.getInstance().getGameManager();

    /**
     * 检查玩家是否能够拾取物品的基本条件
     *
     * @param gamePlayer 游戏玩家
     * @return 如果玩家不能拾取返回true
     */
    public static boolean isPickupDisabled(GamePlayer gamePlayer) {
        // 旁观者不能拾取物品
        if (gamePlayer.isSpectator()) {
            return true;
        }

        // 游戏未在运行状态不能拾取物品
        return GAME_MANAGER.getGameState() != GameState.RUNNING;
    }

    /**
     * 处理床的拾取
     *
     * @param itemStack 物品堆
     * @param item      物品实体
     * @return 如果已处理返回true
     */
    public static boolean handleBedPickup(ItemStack itemStack, Item item) {
        if (!MapUtil.isBed(itemStack.getType())) {
            return false;
        }

        // 如果床有自定义元数据，允许拾取
        if (itemStack.hasItemMeta()) {
            Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName();
            return false;
        }

        // 删除床并取消拾取
        item.remove();
        return true;
    }

    /**
     * 处理剑的拾取
     *
     * @param itemStack  物品堆
     * @param gamePlayer 游戏玩家对象
     */
    public static void handleSwordPickup(ItemStack itemStack, GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();
        // 检查是否是剑类物品
        boolean isSword = itemStack.getType() == XMaterial.WOODEN_SWORD.get() ||
                itemStack.getType() == XMaterial.STONE_SWORD.get() ||
                itemStack.getType() == XMaterial.IRON_SWORD.get() ||
                itemStack.getType() == XMaterial.DIAMOND_SWORD.get();

        if (!isSword) {
            return;
        }

        // 添加锋利附魔
        GameTeam gameTeam = gamePlayer.getGameTeam();
        if (gameTeam == null) {
            return;
        }
        if (gameTeam.getUpgradeManager().hasSharpnessUpgrade()) {
            if (XEnchantment.SHARPNESS.get() != null) {
                itemStack.addEnchantment(XEnchantment.SHARPNESS.get(), 1);
            }
        }

        // 移除木剑
        removeWoodenSwordFromInventory(player);
    }

    /**
     * 从玩家背包中移除木剑
     */
    private static void removeWoodenSwordFromInventory(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            if (player.getInventory().getItem(i) != null) {
                if (Objects.requireNonNull(player.getInventory().getItem(i)).getType() == XMaterial.WOODEN_SWORD.get()) {
                    player.getInventory().setItem(i, new ItemStack(Material.AIR));
                    break;
                }
            }
        }
    }

    /**
     * 检查AFK玩家的资源拾取
     *
     * @param itemStack  物品堆
     * @param gamePlayer 游戏玩家对象
     * @return 如果玩家挂机且尝试拾取资源返回true
     */
    public static boolean checkAfkResourcePickup(ItemStack itemStack, GamePlayer gamePlayer) {
        boolean isResource = itemStack.getType() == XMaterial.IRON_INGOT.get() ||
                itemStack.getType() == XMaterial.GOLD_INGOT.get() ||
                itemStack.getType() == XMaterial.DIAMOND.get() ||
                itemStack.getType() == XMaterial.EMERALD.get();

        // 玩家挂机状态不能拾取资源
        return isResource && gamePlayer.isAfk();
    }

    /**
     * @return true when the economy took the resource
     */
    public static boolean handleResourcePickup(ItemStack itemStack, GamePlayer gamePlayer, Item item) {
        EconomyMode economy = GAME_MANAGER.getEconomy();
        if (!economy.deposit(gamePlayer, itemStack)) {
            return false;
        }
        item.remove();

        boolean isIngot = itemStack.getType() == XMaterial.IRON_INGOT.get() || itemStack.getType() == XMaterial.GOLD_INGOT.get();
        GameTeam gameTeam = gamePlayer.getGameTeam();
        if (!isIngot || gameTeam == null) {
            return true;
        }

        // nearby teammates get a copy
        for (Entity entity : gamePlayer.getPlayer().getNearbyEntities(2, 2, 2)) {
            if (!(entity instanceof Player)) {
                continue;
            }
            GamePlayer nearbyPlayer = GamePlayer.get(entity.getUniqueId());
            if (nearbyPlayer != null && gameTeam.getGamePlayers().contains(nearbyPlayer)) {
                economy.deposit(nearbyPlayer, itemStack);
            }
        }
        return true;
    }
}
