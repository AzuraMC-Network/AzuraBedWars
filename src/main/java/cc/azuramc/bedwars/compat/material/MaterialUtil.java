package cc.azuramc.bedwars.compat.material;

import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.utils.ItemBuilderUtil;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;

/**
 * 材质兼容性工具类
 * 用于处理不同Minecraft版本(1.8-1.21)的Material名称变化和数据值问题
 * By An5w1r_
 */
public class MaterialUtil {

    /**
     * 获取兼容的材质
     * @param newName 新版本(1.13+)的材质名称
     * @param oldName 旧版本(1.8-1.12)的材质名称
     * @return 对应当前服务器版本的有效Material
     */
    public static Material getMaterial(String newName, String oldName) {
        try {
            if (!VersionUtil.isLessThan113()) {
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

    
    /**
     * 获取染色玻璃
     * @param color 颜色 (0-15)
     * @return 染色玻璃物品堆
     */
    public static ItemStack getStainedGlass(int color) {
        if (!VersionUtil.isLessThan113()) {
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
        if (!VersionUtil.isLessThan113()) {
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
     * @param colorName 颜色 (仅新版本适用)
     * @return 床物品堆
     */
    public static Material getBed(String colorName) {
        if (!VersionUtil.isLessThan113()) {
            try {
                // 1.13+ 使用枚举名称，如WHITE_BED
                return Material.valueOf(colorName + "_BED");
            } catch (Exception e) {
                return Material.RED_BED;
            }
        } else {
            return Material.valueOf("BED");
        }
    }

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

    public static Material BOW() {
        return Material.BOW;
    }
    
    public static Material CROSSBOW() {
        return getMaterial("CROSSBOW", "BOW");
    }

    public static Material SHEARS() {
        return getMaterial("SHEARS", "SHEARS");
    }

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
        return getMaterial("COMPARATOR", "REDSTONE_COMPARATOR");
    }
    
    public static Material RED_WOOL() {
        if (!VersionUtil.isLessThan113()) {
            return Material.valueOf("RED_WOOL");
        } else {
            return Material.valueOf("WOOL");
        }
    }
    
    /**
     * 获取彩色羊毛
     * @param color 颜色 (0-15)
     * @return 对应颜色的羊毛
     */
    public static ItemStack getColoredWool(int color) {
        if (!VersionUtil.isLessThan113()) {
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

    public static Material FIREBALL() {
        return getMaterial("FIRE_CHARGE", "FIREBALL");
    }

    public static Material WATER_BUCKET() {
        return getMaterial("WATER_BUCKET", "WATER_BUCKET");
    }

    public static Material GLASS_BOTTLE() {
        return getMaterial("GLASS_BOTTLE", "GLASS_BOTTLE");
    }

    public static Material TERRACOTTA() {
        return getMaterial("TERRACOTTA", "HARD_CLAY");
    }

    public static Material GLASS() {
        return getMaterial("GLASS", "GLASS");
    }

    public static Material END_STONE() {
        return getMaterial("END_STONE", "ENDER_STONE");
    }

    public static Material LADDER() {
        return getMaterial("LADDER", "LADDER");
    }

    public static Material SIGN() {
        return getMaterial("OAK_SIGN", "SIGN");
    }

    public static Material OAK_PLANKS() {
        return getMaterial("OAK_PLANKS", "WOOD");
    }

    public static Material SLIME_BLOCK() {
        return getMaterial("SLIME_BLOCK", "SLIME_BLOCK");
    }

    public static Material COBWEB() {
        return getMaterial("COBWEB", "WEB");
    }

    public static Material OBSIDIAN() {
        return getMaterial("OBSIDIAN", "OBSIDIAN");
    }

    public static Material NETHER_STAR() {
        return getMaterial("NETHER_STAR", "NETHER_STAR");
    }

    public static Material GOLDEN_APPLE() {
        return getMaterial("GOLDEN_APPLE", "GOLDEN_APPLE");
    }

    public static Material POTION() {
        return getMaterial("POTION", "POTION");
    }

    public static Material ARROW() {
        return getMaterial("ARROW", "ARROW");
    }

    public static Material COOKED_PORKCHOP() {
        return getMaterial("COOKED_PORKCHOP", "GRILLED_PORK");
    }

    public static Material COOKED_BEEF() {
        return getMaterial("COOKED_BEEF", "COOKED_BEEF");
    }

    public static Material CARROT() {
        return getMaterial("CARROT", "CARROT_ITEM");
    }

    public static Material BREWING_STAND() {
        return getMaterial("BREWING_STAND", "BREWING_STAND_ITEM");
    }

    public static Material STICK() {
        return getMaterial("STICK", "STICK");
    }

    public static Material BLAZE_ROD() {
        return getMaterial("BLAZE_ROD", "BLAZE_ROD");
    }

    public static Material GUNPOWDER() {
        return getMaterial("GUNPOWDER", "SULPHUR");
    }

    public static Material GOLDEN_BOOTS() {
        return getMaterial("GOLDEN_BOOTS", "GOLD_BOOTS");
    }

    public static Material COMPARATOR() {
        return getMaterial("COMPARATOR", "REDSTONE_COMPARATOR");
    }
} 