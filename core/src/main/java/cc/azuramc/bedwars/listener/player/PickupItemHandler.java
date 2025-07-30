package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
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
     * @param item 物品实体
     * @return 如果已处理返回true
     */
    public static boolean handleBedPickup(ItemStack itemStack, Item item) {
        if (!itemStack.getType().name().toUpperCase().contains("BED")) {
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
        if (gamePlayer.getGameTeam().isHasSharpnessUpgrade()) {
            itemStack.addEnchantment(XEnchantment.SHARPNESS.get(), 1);
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
     * @param itemStack 物品堆
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
     * 处理金铁锭的拾取
     *
     * @param itemStack 物品堆
     * @param gamePlayer 游戏玩家对象
     * @param item 物品实体
     * @return 如果已处理返回true
     */
    public static boolean handleIngotPickup(ItemStack itemStack, GamePlayer gamePlayer, Item item) {
        boolean isIngot = itemStack.getType() == XMaterial.IRON_INGOT.get() || itemStack.getType() == XMaterial.GOLD_INGOT.get();

        if (!isIngot) {
            return false;
        }

        Player player = gamePlayer.getPlayer();
        int xp = calculateIngotXp(itemStack, gamePlayer);

        // 根据游戏模式处理拾取效果
        if (gamePlayer.getGameModeType() == GameModeType.DEFAULT) {
            item.remove();
            gamePlayer.playSound(XSound.ENTITY_PLAYER_LEVELUP.get(), 10, 15F);
            player.getInventory().addItem(new ItemStack(itemStack.getType(), itemStack.getAmount()));
        } else if (gamePlayer.getGameModeType() == GameModeType.EXPERIENCE) {
            item.remove();
            gamePlayer.playSound(XSound.ENTITY_PLAYER_LEVELUP.get(), 10, 15F);
            player.setLevel(player.getLevel() + xp);
        }

        // 处理团队拾取效果
        handleTeamIngotPickup(gamePlayer, itemStack, xp);

        return true;
    }

    /**
     * 计算锭的经验值
     */
    private static int calculateIngotXp(ItemStack itemStack, GamePlayer gamePlayer) {
        int xp = itemStack.getAmount();

        if (itemStack.getType() == XMaterial.IRON_INGOT.get()) {
            gamePlayer.addResourceExperience("IRON", xp);
        } else {
            xp = xp * 3;
            gamePlayer.addResourceExperience("GOLD", xp);
        }

        return xp;
    }

    /**
     * 处理团队锭拾取
     */
    private static void handleTeamIngotPickup(GamePlayer gamePlayer, ItemStack itemStack, int xp) {
        Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName();
        Player player = gamePlayer.getPlayer();
        for (Entity entity : player.getNearbyEntities(2, 2, 2)) {
            if (entity instanceof Player players) {
                gamePlayer.playSound(XSound.ENTITY_PLAYER_LEVELUP.get(), 10, 15F);

                GamePlayer nearbyPlayer = GamePlayer.get(players.getUniqueId());
                if (nearbyPlayer.getGameModeType() == GameModeType.DEFAULT) {
                    players.getInventory().addItem(new ItemStack(itemStack.getType(), itemStack.getAmount()));
                } else {
                    players.setLevel(players.getLevel() + xp);
                }
            }
        }
    }

    /**
     * 处理钻石的拾取
     *
     * @param itemStack 物品堆
     * @param gamePlayer 游戏玩家对象
     * @param item 物品实体
     * @return 如果已处理返回true
     */
    public static boolean handleDiamondPickup(ItemStack itemStack, GamePlayer gamePlayer, Item item) {
        if (itemStack.getType() != XMaterial.DIAMOND.get()) {
            return false;
        }

        Player player = gamePlayer.getPlayer();
        PlayerData playerData = gamePlayer.getPlayerData();

        // 默认模式下不做特殊处理
        if (playerData.getMode() == GameModeType.DEFAULT) {
            return false;
        }

        // 经验模式下处理成经验值
        double xp = itemStack.getAmount() * 40;
        xp = applyVipBonus(player, xp);

        item.remove();
        player.setLevel((int) (player.getLevel() + xp));
        gamePlayer.addResourceExperience("DIAMOND", (int) xp);
        gamePlayer.playSound(XSound.ENTITY_PLAYER_LEVELUP.get(), 10, 15F);

        return true;
    }

    /**
     * 处理绿宝石的拾取
     *
     * @param itemStack 物品堆
     * @param gamePlayer 游戏玩家对象
     * @param item 物品实体
     * @return 如果已处理返回true
     */
    public static boolean handleEmeraldPickup(ItemStack itemStack, GamePlayer gamePlayer, Item item) {
        if (itemStack.getType() != XMaterial.EMERALD.get()) {
            return false;
        }

        Player player = gamePlayer.getPlayer();
        PlayerData playerData = gamePlayer.getPlayerData();

        // 默认模式下不做特殊处理
        if (playerData.getMode() == GameModeType.DEFAULT) {
            return false;
        }

        // 经验模式下处理成经验值
        double xp = itemStack.getAmount() * 80;
        xp = applyVipBonus(player, xp);

        item.remove();
        player.setLevel((int) (player.getLevel() + xp));
        gamePlayer.addResourceExperience("EMERALD", (int) xp);
        gamePlayer.playSound(XSound.ENTITY_PLAYER_LEVELUP.get(), 10, 15F);

        return true;
    }

    /**
     * 应用VIP经验加成
     */
    private static double applyVipBonus(Player player, double xp) {
//        if (player.hasPermission("azurabedwars.xp.vip1")) {
//            xp = xp + (xp * 1.1);
//        } else if (player.hasPermission("azurabedwars.xp.vip2")) {
//            xp = xp + (xp * 1.2);
//        } else if (player.hasPermission("azurabedwars.xp.vip3")) {
//            xp = xp + (xp * 1.4);
//        } else if (player.hasPermission("azurabedwars.xp.vip4")) {
//            xp = xp + (xp * 1.8);
//        }
        return xp;
    }
}
