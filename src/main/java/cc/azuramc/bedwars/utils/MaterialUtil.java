package cc.azuramc.bedwars.utils;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

/**
 * 材质兼容性工具类
 * 用于处理不同Minecraft版本(1.8-1.21)的Material名称变化和数据值问题
 * By An5w1r_
 */
public class MaterialUtil {
    private static final boolean NEW_VERSION;
    
    static {
        boolean newVersion = false;
        try {
            // 1.13+版本存在Material.PLAYER_HEAD
            Material.valueOf("PLAYER_HEAD");
            newVersion = true;
        } catch (IllegalArgumentException e) {
            // 1.8-1.12版本
            newVersion = false;
        }
        NEW_VERSION = newVersion;
        
        Bukkit.getLogger().info("MaterialUtil初始化完成，当前服务器版本: " + (NEW_VERSION ? "1.13+" : "1.8-1.12"));
    }
    
    /**
     * 检查是否是新版本Minecraft (1.13+)
     * @return 如果是1.13+版本返回true
     */
    public static boolean isNewVersion() {
        return NEW_VERSION;
    }
    
    /**
     * 获取兼容的材质
     * @param newName 新版本(1.13+)的材质名称
     * @param oldName 旧版本(1.8-1.12)的材质名称
     * @return 对应当前服务器版本的有效Material
     */
    public static Material getMaterial(String newName, String oldName) {
        try {
            if (NEW_VERSION) {
                return Material.valueOf(newName);
            } else {
                return Material.valueOf(oldName);
            }
        } catch (IllegalArgumentException e) {
            try {
                return Material.valueOf(oldName);
            } catch (IllegalArgumentException ex) {
                Bukkit.getLogger().warning("无法找到材质: " + newName + " 或 " + oldName);
                return Material.STONE; // 默认返回石头
            }
        }
    }
    
    /**
     * 获取玻璃板
     * @return 玻璃板材质
     */
    public static Material GLASS_PANE() {
        return getMaterial("GLASS_PANE", "THIN_GLASS");
    }
    
    // ------ 方块材质 ------ //
    
    /**
     * 获取染色玻璃
     * @param color 颜色 (0-15)
     * @return 染色玻璃物品堆
     */
    public static ItemStack getStainedGlass(int color) {
        if (NEW_VERSION) {
            try {
                // 1.13+ 使用枚举名称
                DyeColor dyeColor = DyeColor.values()[color % 16];
                String colorName = dyeColor.toString();
                return new ItemStack(Material.valueOf(colorName + "_STAINED_GLASS"));
            } catch (Exception e) {
                return new ItemStack(GLASS());
            }
        } else {
            // 1.8-1.12 使用数据值
            return new ItemBuilderUtil()
                    .setType(Material.valueOf("STAINED_GLASS"))
                    .setDurability((short) (color % 16))
                    .getItem();
        }
    }
    
    /**
     * 获取染色玻璃板
     * @param color 颜色 (0-15)
     * @return 染色玻璃板物品堆
     */
    public static ItemStack getStainedGlassPane(int color) {
        if (NEW_VERSION) {
            try {
                // 1.13+ 使用枚举名称
                DyeColor dyeColor = DyeColor.values()[color % 16];
                String colorName = dyeColor.toString();
                return new ItemStack(Material.valueOf(colorName + "_STAINED_GLASS_PANE"));
            } catch (Exception e) {
                return new ItemStack(GLASS_PANE());
            }
        } else {
            // 1.8-1.12 使用数据值
            return new ItemBuilderUtil()
                    .setType(Material.valueOf("STAINED_GLASS_PANE"))
                    .setDurability((short) (color % 16))
                    .getItem();
        }
    }
    
    /**
     * 获取床方块
     * @param color 颜色 (仅新版本适用)
     * @return 床物品堆
     */
    public static Material getBed(String color) {
        if (NEW_VERSION) {
            try {
                // 1.13+ 使用枚举名称，如WHITE_BED
                return Material.valueOf(color + "_BED");
            } catch (Exception e) {
                return Material.RED_BED;
            }
        } else {
            // 1.8-1.12 只有一种床
            return Material.valueOf("BED");
        }
    }
    
    // ------ 工具材质 ------ //
    
    // 木质工具
    public static Material WOODEN_SWORD() {
        return getMaterial("WOODEN_SWORD", "WOOD_SWORD");
    }
    
    public static Material WOODEN_PICKAXE() {
        return getMaterial("WOODEN_PICKAXE", "WOOD_PICKAXE");
    }
    
    public static Material WOODEN_AXE() {
        return getMaterial("WOODEN_AXE", "WOOD_AXE");
    }
    
    public static Material WOODEN_SHOVEL() {
        return getMaterial("WOODEN_SHOVEL", "WOOD_SPADE");
    }
    
    // 石质工具
    public static Material STONE_SWORD() {
        return getMaterial("STONE_SWORD", "STONE_SWORD");
    }
    
    public static Material STONE_PICKAXE() {
        return getMaterial("STONE_PICKAXE", "STONE_PICKAXE");
    }
    
    public static Material STONE_AXE() {
        return getMaterial("STONE_AXE", "STONE_AXE");
    }
    
    public static Material STONE_SHOVEL() {
        return getMaterial("STONE_SHOVEL", "STONE_SPADE");
    }
    
    // 铁质工具
    public static Material IRON_SWORD() {
        return getMaterial("IRON_SWORD", "IRON_SWORD");
    }
    
    public static Material IRON_PICKAXE() {
        return getMaterial("IRON_PICKAXE", "IRON_PICKAXE");
    }
    
    public static Material IRON_AXE() {
        return getMaterial("IRON_AXE", "IRON_AXE");
    }
    
    public static Material IRON_SHOVEL() {
        return getMaterial("IRON_SHOVEL", "IRON_SPADE");
    }
    
    // 金质工具
    public static Material GOLDEN_SWORD() {
        return getMaterial("GOLDEN_SWORD", "GOLD_SWORD");
    }
    
    public static Material GOLDEN_PICKAXE() {
        return getMaterial("GOLDEN_PICKAXE", "GOLD_PICKAXE");
    }
    
    public static Material GOLDEN_AXE() {
        return getMaterial("GOLDEN_AXE", "GOLD_AXE");
    }
    
    public static Material GOLDEN_SHOVEL() {
        return getMaterial("GOLDEN_SHOVEL", "GOLD_SPADE");
    }
    
    // 钻石工具
    public static Material DIAMOND_SWORD() {
        return getMaterial("DIAMOND_SWORD", "DIAMOND_SWORD");
    }
    
    public static Material DIAMOND_PICKAXE() {
        return getMaterial("DIAMOND_PICKAXE", "DIAMOND_PICKAXE");
    }
    
    public static Material DIAMOND_AXE() {
        return getMaterial("DIAMOND_AXE", "DIAMOND_AXE");
    }
    
    public static Material DIAMOND_SHOVEL() {
        return getMaterial("DIAMOND_SHOVEL", "DIAMOND_SPADE");
    }
    
    // 弓和弩
    public static Material BOW() {
        return Material.BOW;
    }
    
    public static Material CROSSBOW() {
        return getMaterial("CROSSBOW", "BOW");
    }
    
    // 剪刀
    public static Material SHEARS() {
        return getMaterial("SHEARS", "SHEARS");
    }
    
    // ------ 装备材质 ------ //
    
    // 皮革装备
    public static Material LEATHER_HELMET() {
        return getMaterial("LEATHER_HELMET", "LEATHER_HELMET");
    }
    
    public static Material LEATHER_CHESTPLATE() {
        return getMaterial("LEATHER_CHESTPLATE", "LEATHER_CHESTPLATE");
    }
    
    public static Material LEATHER_LEGGINGS() {
        return getMaterial("LEATHER_LEGGINGS", "LEATHER_LEGGINGS");
    }
    
    public static Material LEATHER_BOOTS() {
        return getMaterial("LEATHER_BOOTS", "LEATHER_BOOTS");
    }
    
    // 锁链装备
    public static Material CHAINMAIL_HELMET() {
        return getMaterial("CHAINMAIL_HELMET", "CHAINMAIL_HELMET");
    }
    
    public static Material CHAINMAIL_CHESTPLATE() {
        return getMaterial("CHAINMAIL_CHESTPLATE", "CHAINMAIL_CHESTPLATE");
    }
    
    public static Material CHAINMAIL_LEGGINGS() {
        return getMaterial("CHAINMAIL_LEGGINGS", "CHAINMAIL_LEGGINGS");
    }
    
    public static Material CHAINMAIL_BOOTS() {
        return getMaterial("CHAINMAIL_BOOTS", "CHAINMAIL_BOOTS");
    }
    
    // 铁装备
    public static Material IRON_HELMET() {
        return getMaterial("IRON_HELMET", "IRON_HELMET");
    }
    
    public static Material IRON_CHESTPLATE() {
        return getMaterial("IRON_CHESTPLATE", "IRON_CHESTPLATE");
    }
    
    public static Material IRON_LEGGINGS() {
        return getMaterial("IRON_LEGGINGS", "IRON_LEGGINGS");
    }
    
    public static Material IRON_BOOTS() {
        return getMaterial("IRON_BOOTS", "IRON_BOOTS");
    }
    
    // 钻石装备
    public static Material DIAMOND_HELMET() {
        return getMaterial("DIAMOND_HELMET", "DIAMOND_HELMET");
    }
    
    public static Material DIAMOND_CHESTPLATE() {
        return getMaterial("DIAMOND_CHESTPLATE", "DIAMOND_CHESTPLATE");
    }
    
    public static Material DIAMOND_LEGGINGS() {
        return getMaterial("DIAMOND_LEGGINGS", "DIAMOND_LEGGINGS");
    }
    
    public static Material DIAMOND_BOOTS() {
        return getMaterial("DIAMOND_BOOTS", "DIAMOND_BOOTS");
    }
    
    // ------ 资源材质 ------ //
    
    public static Material IRON_INGOT() {
        return getMaterial("IRON_INGOT", "IRON_INGOT");
    }
    
    public static Material GOLD_INGOT() {
        return getMaterial("GOLD_INGOT", "GOLD_INGOT");
    }
    
    public static Material DIAMOND() {
        return getMaterial("DIAMOND", "DIAMOND");
    }
    
    public static Material EMERALD() {
        return getMaterial("EMERALD", "EMERALD");
    }
    
    // ------ 杂项材质 ------ //
    
    public static Material COMPASS() {
        return getMaterial("COMPASS", "COMPASS");
    }
    
    public static Material CLOCK() {
        return getMaterial("CLOCK", "WATCH");
    }
    
    public static Material ENDER_PEARL() {
        return getMaterial("ENDER_PEARL", "ENDER_PEARL");
    }
    
    public static Material PLAYER_HEAD() {
        return getMaterial("PLAYER_HEAD", "SKULL_ITEM");
    }
    
    public static Material WALL_SIGN() {
        return getMaterial("OAK_WALL_SIGN", "WALL_SIGN");
    }
    
    public static Material SLIME_BALL() {
        return getMaterial("SLIME_BALL", "SLIME_BALL");
    }
    
    public static Material REDSTONE_COMPARATOR() {
        return getMaterial("REDSTONE_COMPARATOR", "REDSTONE_COMPARATOR");
    }
    
    public static Material RED_WOOL() {
        if (NEW_VERSION) {
            return Material.valueOf("RED_WOOL");
        } else {
            return Material.valueOf("WOOL"); // 旧版本用数据值区分颜色
        }
    }
    
    /**
     * 获取彩色羊毛
     * @param color 颜色 (0-15)
     * @return 对应颜色的羊毛
     */
    public static ItemStack getColoredWool(int color) {
        if (NEW_VERSION) {
            try {
                // 1.13+ 使用枚举名称
                DyeColor dyeColor = DyeColor.values()[color % 16];
                String colorName = dyeColor.toString();
                return new ItemStack(Material.valueOf(colorName + "_WOOL"));
            } catch (Exception e) {
                return new ItemStack(Material.WHITE_WOOL);
            }
        } else {
            // 1.8-1.12 使用数据值
            return new ItemBuilderUtil()
                    .setType(Material.valueOf("WOOL"))
                    .setDurability((short) (color % 16))
                    .getItem();
        }
    }
    
    public static byte getWoolData(DyeColor dyeColor) {
        try {
            // 尝试使用反射获取数据
            return (byte) dyeColor.getClass().getMethod("getData").invoke(dyeColor);
        } catch (Exception e) {
            // 如果反射失败，返回默认值
            return 0;
        }
    }
    
    public static Material BED() {
        return getMaterial("WHITE_BED", "BED");
    }
    
    public static Material EXPERIENCE_BOTTLE() {
        return getMaterial("EXPERIENCE_BOTTLE", "EXP_BOTTLE");
    }
    
    public static Material BEACON() {
        return getMaterial("BEACON", "BEACON");
    }
    
    public static Material TRIPWIRE_HOOK() {
        return getMaterial("TRIPWIRE_HOOK", "TRIPWIRE_HOOK");
    }

    public static Material TNT() {
        return getMaterial("TNT", "TNT");
    }

    public static Material AIR() {
        return getMaterial("AIR", "AIR");
    }

    public static Material WHITE_WOOL() {
        return getMaterial("WHITE_WOOL", "WOOL");
    }

    public static ItemStack getItemInHand(Player player) {
        try {
            return player.getInventory().getItemInMainHand();
        } catch (NoSuchMethodError e) {
            return player.getItemInHand();
        }
    }

    public static Material FIREBALL() {
        return getMaterial("FIRE_CHARGE", "FIREBALL");
    }

    public static Material WATER_BUCKET() {
        return getMaterial("WATER_BUCKET", "WATER_BUCKET");
    }

    /**
     * 获取玻璃瓶
     * @return 玻璃瓶材质
     */
    public static Material GLASS_BOTTLE() {
        return getMaterial("GLASS_BOTTLE", "GLASS_BOTTLE");
    }

    /**
     * 获取陶瓦
     * @return 陶瓦材质
     */
    public static Material TERRACOTTA() {
        return getMaterial("TERRACOTTA", "HARD_CLAY");
    }

    /**
     * 获取玻璃
     * @return 玻璃材质
     */
    public static Material GLASS() {
        return getMaterial("GLASS", "GLASS");
    }

    /**
     * 获取末地石
     * @return 末地石材质
     */
    public static Material END_STONE() {
        return getMaterial("END_STONE", "ENDER_STONE");
    }

    /**
     * 获取梯子
     * @return 梯子材质
     */
    public static Material LADDER() {
        return getMaterial("LADDER", "LADDER");
    }

    /**
     * 获取告示牌
     * @return 告示牌材质
     */
    public static Material SIGN() {
        return getMaterial("OAK_SIGN", "SIGN");
    }

    /**
     * 获取橡木板
     * @return 橡木板材质
     */
    public static Material OAK_PLANKS() {
        return getMaterial("OAK_PLANKS", "WOOD");
    }

    /**
     * 获取史莱姆块
     * @return 史莱姆块材质
     */
    public static Material SLIME_BLOCK() {
        return getMaterial("SLIME_BLOCK", "SLIME_BLOCK");
    }

    /**
     * 获取蜘蛛网
     * @return 蜘蛛网材质
     */
    public static Material COBWEB() {
        return getMaterial("COBWEB", "WEB");
    }

    /**
     * 获取黑曜石
     * @return 黑曜石材质
     */
    public static Material OBSIDIAN() {
        return getMaterial("OBSIDIAN", "OBSIDIAN");
    }

    /**
     * 设置玩家手中的物品
     * @param player 玩家
     * @param item 要设置的物品
     */
    public static void setItemInHand(Player player, ItemStack item) {
        try {
            player.getInventory().setItemInMainHand(item);
        } catch (NoSuchMethodError e) {
            player.setItemInHand(item);
        }
    }

    /**
     * 获取下界之星
     * @return 下界之星材质
     */
    public static Material NETHER_STAR() {
        return getMaterial("NETHER_STAR", "NETHER_STAR");
    }

    /**
     * 获取金苹果
     * @return 金苹果材质
     */
    public static Material GOLDEN_APPLE() {
        return getMaterial("GOLDEN_APPLE", "GOLDEN_APPLE");
    }

    /**
     * 获取药水
     * @return 药水材质
     */
    public static Material POTION() {
        return getMaterial("POTION", "POTION");
    }

    /**
     * 获取箭
     * @return 箭材质
     */
    public static Material ARROW() {
        return getMaterial("ARROW", "ARROW");
    }

    /**
     * 获取熟猪排
     * @return 熟猪排材质
     */
    public static Material COOKED_PORKCHOP() {
        return getMaterial("COOKED_PORKCHOP", "GRILLED_PORK");
    }

    /**
     * 获取熟牛肉
     * @return 熟牛肉材质
     */
    public static Material COOKED_BEEF() {
        return getMaterial("COOKED_BEEF", "COOKED_BEEF");
    }

    /**
     * 获取胡萝卜
     * @return 胡萝卜材质
     */
    public static Material CARROT() {
        return getMaterial("CARROT", "CARROT_ITEM");
    }

    /**
     * 获取酿造台
     * @return 酿造台材质
     */
    public static Material BREWING_STAND() {
        return getMaterial("BREWING_STAND", "BREWING_STAND_ITEM");
    }

    /**
     * 获取木棍
     * @return 木棍材质
     */
    public static Material STICK() {
        return getMaterial("STICK", "STICK");
    }

    /**
     * 获取烈焰棒
     * @return 烈焰棒材质
     */
    public static Material BLAZE_ROD() {
        return getMaterial("BLAZE_ROD", "BLAZE_ROD");
    }

    /**
     * 获取火药
     * @return 火药材质
     */
    public static Material GUNPOWDER() {
        return getMaterial("GUNPOWDER", "SULPHUR");
    }

    /**
     * 获取金靴子
     * @return 金靴子材质
     */
    public static Material GOLDEN_BOOTS() {
        return getMaterial("GOLDEN_BOOTS", "GOLD_BOOTS");
    }

    /**
     * 获取红石比较器
     * @return 红石比较器材质
     */
    public static Material COMPARATOR() {
        return getMaterial("COMPARATOR", "REDSTONE_COMPARATOR");
    }
} 