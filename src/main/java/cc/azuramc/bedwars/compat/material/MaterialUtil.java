package cc.azuramc.bedwars.compat.material;

import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.utils.ItemBuilderUtil;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;

public class MaterialUtil {

    /**
     * 获取兼容的材质
     * @param oldName 旧版本(1.8-1.12)的材质名称
     * @param newName 新版本(1.13+)的材质名称
     * @return 对应当前服务器版本的有效Material
     */
    public static Material getMaterial(String oldName, String newName) {
        try {
            if (VersionUtil.isLessThan113()) {
                return Material.valueOf(oldName);
            } else {
                return Material.valueOf(newName);
            }
        } catch (IllegalArgumentException e) {
            try {
                // 尝试使用另一个名称
                return VersionUtil.isLessThan113() ? 
                       Material.valueOf(newName) : Material.valueOf(oldName);
            } catch (IllegalArgumentException ex) {
                Bukkit.getLogger().warning("无法找到材质: " + oldName + " 或 " + newName);
                return Material.STONE; // 默认返回石头
            }
        }
    }
    
    /**
     * 获取玻璃板
     * @return 玻璃板材质
     */
    public static Material GLASS_PANE() {
        return getMaterial("THIN_GLASS", "GLASS_PANE");
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
        return getMaterial("WOOD_SWORD", "WOODEN_SWORD");
    }
    
    public static Material WOODEN_PICKAXE() {
        return getMaterial("WOOD_PICKAXE", "WOODEN_PICKAXE");
    }
    
    public static Material WOODEN_AXE() {
        return getMaterial("WOOD_AXE", "WOODEN_AXE");
    }
    
    public static Material WOODEN_SHOVEL() {
        return getMaterial("WOOD_SPADE", "WOODEN_SHOVEL");
    }

    public static Material STONE_SWORD() {
        return Material.STONE_SWORD;
    }
    
    public static Material STONE_PICKAXE() {
        return Material.STONE_PICKAXE;
    }
    
    public static Material STONE_AXE() {
        return Material.STONE_AXE;
    }
    
    public static Material STONE_SHOVEL() {
        return getMaterial("STONE_SPADE", "STONE_SHOVEL");
    }

    public static Material IRON_SWORD() {
        return Material.IRON_SWORD;
    }
    
    public static Material IRON_PICKAXE() {
        return Material.IRON_PICKAXE;
    }
    
    public static Material IRON_AXE() {
        return Material.IRON_AXE;
    }
    
    public static Material IRON_SHOVEL() {
        return getMaterial("IRON_SPADE", "IRON_SHOVEL");
    }

    public static Material GOLDEN_SWORD() {
        return getMaterial("GOLD_SWORD", "GOLDEN_SWORD");
    }
    
    public static Material GOLDEN_PICKAXE() {
        return getMaterial("GOLD_PICKAXE", "GOLDEN_PICKAXE");
    }
    
    public static Material GOLDEN_AXE() {
        return getMaterial("GOLD_AXE", "GOLDEN_AXE");
    }
    
    public static Material GOLDEN_SHOVEL() {
        return getMaterial("GOLD_SPADE", "GOLDEN_SHOVEL");
    }

    public static Material DIAMOND_SWORD() {
        return Material.DIAMOND_SWORD;
    }
    
    public static Material DIAMOND_PICKAXE() {
        return Material.DIAMOND_PICKAXE;
    }
    
    public static Material DIAMOND_AXE() {
        return Material.DIAMOND_AXE;
    }
    
    public static Material DIAMOND_SHOVEL() {
        return getMaterial("DIAMOND_SPADE", "DIAMOND_SHOVEL");
    }

    public static Material BOW() {
        return Material.BOW;
    }
    
    public static Material CROSSBOW() {
        return getMaterial("BOW", "CROSSBOW");
    }

    public static Material SHEARS() {
        return Material.SHEARS;
    }

    public static Material LEATHER_HELMET() {
        return Material.LEATHER_HELMET;
    }
    
    public static Material LEATHER_CHESTPLATE() {
        return Material.LEATHER_CHESTPLATE;
    }
    
    public static Material LEATHER_LEGGINGS() {
        return Material.LEATHER_LEGGINGS;
    }
    
    public static Material LEATHER_BOOTS() {
        return Material.LEATHER_BOOTS;
    }

    public static Material CHAINMAIL_HELMET() {
        return Material.CHAINMAIL_HELMET;
    }
    
    public static Material CHAINMAIL_CHESTPLATE() {
        return Material.CHAINMAIL_CHESTPLATE;
    }
    
    public static Material CHAINMAIL_LEGGINGS() {
        return Material.CHAINMAIL_LEGGINGS;
    }
    
    public static Material CHAINMAIL_BOOTS() {
        return Material.CHAINMAIL_BOOTS;
    }

    public static Material IRON_HELMET() {
        return Material.IRON_HELMET;
    }
    
    public static Material IRON_CHESTPLATE() {
        return Material.IRON_CHESTPLATE;
    }
    
    public static Material IRON_LEGGINGS() {
        return Material.IRON_LEGGINGS;
    }
    
    public static Material IRON_BOOTS() {
        return Material.IRON_BOOTS;
    }

    public static Material DIAMOND_HELMET() {
        return Material.DIAMOND_HELMET;
    }
    
    public static Material DIAMOND_CHESTPLATE() {
        return Material.DIAMOND_CHESTPLATE;
    }
    
    public static Material DIAMOND_LEGGINGS() {
        return Material.DIAMOND_LEGGINGS;
    }
    
    public static Material DIAMOND_BOOTS() {
        return Material.DIAMOND_BOOTS;
    }
    
    public static Material IRON_INGOT() {
        return Material.IRON_INGOT;
    }
    
    public static Material GOLD_INGOT() {
        return Material.GOLD_INGOT;
    }
    
    public static Material DIAMOND() {
        return Material.DIAMOND;
    }
    
    public static Material EMERALD() {
        return Material.EMERALD;
    }
    
    public static Material COMPASS() {
        return Material.COMPASS;
    }
    
    public static Material CLOCK() {
        return getMaterial("WATCH", "CLOCK");
    }
    
    public static Material ENDER_PEARL() {
        return Material.ENDER_PEARL;
    }
    
    public static Material PLAYER_HEAD() {
        return getMaterial("SKULL_ITEM", "PLAYER_HEAD");
    }
    
    public static Material WALL_SIGN() {
        return getMaterial("WALL_SIGN", "OAK_WALL_SIGN");
    }
    
    public static Material SLIME_BALL() {
        return Material.SLIME_BALL;
    }
    
    public static Material REDSTONE_COMPARATOR() {
        return getMaterial("REDSTONE_COMPARATOR", "COMPARATOR");
    }
    
    public static Material RED_WOOL() {
        return getMaterial("WOOL", "RED_WOOL");
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
                return new ItemStack(WHITE_WOOL());
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
        return (byte) dyeColor.getWoolData();
    }
    
    public static Material BED() {
        return getMaterial("BED", "RED_BED");
    }
    
    public static Material EXPERIENCE_BOTTLE() {
        return getMaterial("EXP_BOTTLE", "EXPERIENCE_BOTTLE");
    }
    
    public static Material BEACON() {
        return Material.BEACON;
    }
    
    public static Material TRIPWIRE_HOOK() {
        return Material.TRIPWIRE_HOOK;
    }

    public static Material TNT() {
        return Material.TNT;
    }

    public static Material AIR() {
        return Material.AIR;
    }

    public static Material WHITE_WOOL() {
        return getMaterial("WOOL", "WHITE_WOOL");
    }

    public static Material FIREBALL() {
        return getMaterial("FIREBALL", "FIRE_CHARGE");
    }

    public static Material WATER_BUCKET() {
        return Material.WATER_BUCKET;
    }

    public static Material GLASS_BOTTLE() {
        return Material.GLASS_BOTTLE;
    }

    public static Material TERRACOTTA() {
        return getMaterial("HARD_CLAY", "TERRACOTTA");
    }

    public static Material GLASS() {
        return Material.GLASS;
    }

    public static Material END_STONE() {
        return getMaterial("ENDER_STONE", "END_STONE");
    }

    public static Material LADDER() {
        return Material.LADDER;
    }

    public static Material SIGN() {
        return getMaterial("SIGN", "OAK_SIGN");
    }

    public static Material OAK_PLANKS() {
        return getMaterial("WOOD", "OAK_PLANKS");
    }

    public static Material SLIME_BLOCK() {
        return getMaterial("SLIME_BLOCK", "SLIME_BLOCK");
    }

    public static Material COBWEB() {
        return getMaterial("WEB", "COBWEB");
    }

    public static Material OBSIDIAN() {
        return Material.OBSIDIAN;
    }

    public static Material NETHER_STAR() {
        return Material.NETHER_STAR;
    }

    public static Material GOLDEN_APPLE() {
        return getMaterial("GOLDEN_APPLE", "GOLDEN_APPLE");
    }

    public static Material POTION() {
        return Material.POTION;
    }

    public static Material ARROW() {
        return Material.ARROW;
    }

    public static Material COOKED_PORKCHOP() {
        return getMaterial("GRILLED_PORK", "COOKED_PORKCHOP");
    }

    public static Material COOKED_BEEF() {
        return getMaterial("COOKED_BEEF", "COOKED_BEEF");
    }

    public static Material CARROT() {
        return getMaterial("CARROT_ITEM", "CARROT");
    }

    public static Material BREWING_STAND() {
        return getMaterial("BREWING_STAND", "BREWING_STAND");
    }

    public static Material STICK() {
        return Material.STICK;
    }

    public static Material BLAZE_ROD() {
        return Material.BLAZE_ROD;
    }

    public static Material GUNPOWDER() {
        return getMaterial("SULPHUR", "GUNPOWDER");
    }

    public static Material GOLDEN_BOOTS() {
        return getMaterial("GOLD_BOOTS", "GOLDEN_BOOTS");
    }

    public static Material COMPARATOR() {
        return getMaterial("REDSTONE_COMPARATOR", "COMPARATOR");
    }
} 