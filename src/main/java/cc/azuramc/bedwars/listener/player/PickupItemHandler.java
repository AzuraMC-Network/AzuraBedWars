package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.wrapper.EnchantmentWrapper;
import cc.azuramc.bedwars.compat.wrapper.MaterialWrapper;
import cc.azuramc.bedwars.compat.wrapper.SoundWrapper;
import cc.azuramc.bedwars.database.profile.PlayerProfile;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
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
     * @param player 玩家
     * @param gamePlayer 游戏玩家对象
     * @return 如果玩家不能拾取返回true
     */
    public static boolean isPickupDisabled(Player player, GamePlayer gamePlayer) {
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
        if (itemStack.getType() != MaterialWrapper.BED()) {
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
     * @param player     玩家
     * @param gamePlayer 游戏玩家对象
     */
    public static void handleSwordPickup(ItemStack itemStack, Player player, GamePlayer gamePlayer) {
        // 检查是否是剑类物品
        boolean isSword = itemStack.getType() == MaterialWrapper.WOODEN_SWORD() || 
                          itemStack.getType() == MaterialWrapper.STONE_SWORD() || 
                          itemStack.getType() == MaterialWrapper.IRON_SWORD() || 
                          itemStack.getType() == MaterialWrapper.DIAMOND_SWORD();
        
        if (!isSword) {
            return;
        }
        
        // 添加锋利附魔
        if (gamePlayer.getGameTeam().isHasSharpenedEnchant()) {
            itemStack.addEnchantment(EnchantmentWrapper.DAMAGE_ALL(), 1);
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
                if (Objects.requireNonNull(player.getInventory().getItem(i)).getType() == MaterialWrapper.WOODEN_SWORD()) {
                    player.getInventory().setItem(i, new ItemStack(MaterialWrapper.AIR()));
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
        boolean isResource = itemStack.getType() == Material.IRON_INGOT || 
                            itemStack.getType() == Material.GOLD_INGOT || 
                            itemStack.getType() == Material.DIAMOND || 
                            itemStack.getType() == Material.EMERALD;
        
        // 玩家挂机状态不能拾取资源
        return isResource && gamePlayer.isAfk();
    }
    
    /**
     * 处理金铁锭的拾取
     * 
     * @param itemStack 物品堆
     * @param player 玩家
     * @param gamePlayer 游戏玩家对象
     * @param item 物品实体
     * @return 如果已处理返回true
     */
    public static boolean handleIngotPickup(ItemStack itemStack, Player player, GamePlayer gamePlayer, Item item) {
        boolean isIngot = itemStack.getType() == Material.IRON_INGOT || itemStack.getType() == Material.GOLD_INGOT;
        
        if (!isIngot) {
            return false;
        }
        
        PlayerProfile playerProfile = gamePlayer.getPlayerProfile();
        int xp = calculateIngotXp(itemStack, gamePlayer);
        
        // 根据游戏模式处理拾取效果
        if (playerProfile.getGameModeType() == GameModeType.DEFAULT) {
            item.remove();
            SoundWrapper.playLevelUpSound(player);
            player.getInventory().addItem(new ItemStack(itemStack.getType(), itemStack.getAmount()));
        } else if (playerProfile.getGameModeType() == GameModeType.EXPERIENCE) {
            item.remove();
            SoundWrapper.playLevelUpSound(player);
            player.setLevel(player.getLevel() + xp);
        }
        
        // 处理团队拾取效果
        if (itemStack.hasItemMeta()) {
            handleTeamIngotPickup(player, itemStack, xp);
        }
        
        return true;
    }
    
    /**
     * 计算锭的经验值
     */
    private static int calculateIngotXp(ItemStack itemStack, GamePlayer gamePlayer) {
        int xp = itemStack.getAmount();

        if (itemStack.getType() == Material.IRON_INGOT) {
            gamePlayer.addExperience("IRON", xp);
        } else {
            xp = xp * 3;
            gamePlayer.addExperience("GOLD", xp);
        }
        
        return xp;
    }
    
    /**
     * 处理团队锭拾取
     */
    private static void handleTeamIngotPickup(Player player, ItemStack itemStack, int xp) {
        Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName();
        for (Entity entity : player.getNearbyEntities(2, 2, 2)) {
            if (entity instanceof Player players) {
                players.playSound(players.getLocation(), SoundWrapper.get("LEVEL_UP", "ENTITY_PLAYER_LEVELUP"), 10, 15);
                
                GamePlayer nearbyPlayer = GamePlayer.get(players.getUniqueId());
                if (nearbyPlayer.getPlayerProfile().getGameModeType() == GameModeType.DEFAULT) {
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
     * @param player 玩家
     * @param gamePlayer 游戏玩家对象
     * @param item 物品实体
     * @return 如果已处理返回true
     */
    public static boolean handleDiamondPickup(ItemStack itemStack, Player player, GamePlayer gamePlayer, Item item) {
        if (itemStack.getType() != Material.DIAMOND) {
            return false;
        }
        
        PlayerProfile playerProfile = gamePlayer.getPlayerProfile();
        
        // 默认模式下不做特殊处理
        if (playerProfile.getGameModeType() == GameModeType.DEFAULT) {
            return false;
        }
        
        // 经验模式下处理成经验值
        double xp = itemStack.getAmount() * 40;
        xp = applyVipBonus(player, xp);
        
        item.remove();
        player.setLevel((int) (player.getLevel() + xp));
        gamePlayer.addExperience("DIAMOND", (int) xp);
        SoundWrapper.playLevelUpSound(player);
        
        return true;
    }
    
    /**
     * 处理绿宝石的拾取
     * 
     * @param itemStack 物品堆
     * @param player 玩家
     * @param gamePlayer 游戏玩家对象
     * @param item 物品实体
     * @return 如果已处理返回true
     */
    public static boolean handleEmeraldPickup(ItemStack itemStack, Player player, GamePlayer gamePlayer, Item item) {
        if (itemStack.getType() != Material.EMERALD) {
            return false;
        }
        
        PlayerProfile playerProfile = gamePlayer.getPlayerProfile();
        
        // 默认模式下不做特殊处理
        if (playerProfile.getGameModeType() == GameModeType.DEFAULT) {
            return false;
        }
        
        // 经验模式下处理成经验值
        double xp = itemStack.getAmount() * 80;
        xp = applyVipBonus(player, xp);
        
        item.remove();
        player.setLevel((int) (player.getLevel() + xp));
        gamePlayer.addExperience("EMERALD", (int) xp);
        SoundWrapper.playLevelUpSound(player);
        
        return true;
    }
    
    /**
     * 应用VIP经验加成
     */
    private static double applyVipBonus(Player player, double xp) {
        if (player.hasPermission("azurabedwars.xp.vip1")) {
            xp = xp + (xp * 1.1);
        } else if (player.hasPermission("azurabedwars.xp.vip2")) {
            xp = xp + (xp * 1.2);
        } else if (player.hasPermission("azurabedwars.xp.vip3")) {
            xp = xp + (xp * 1.4);
        } else if (player.hasPermission("azurabedwars.xp.vip4")) {
            xp = xp + (xp * 1.8);
        }
        return xp;
    }
} 