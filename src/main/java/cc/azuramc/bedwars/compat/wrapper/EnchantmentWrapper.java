package cc.azuramc.bedwars.compat.wrapper;

import cc.azuramc.bedwars.compat.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Map;

public class EnchantmentWrapper {

    private static final Map<String, Enchantment> ENCHANTMENT_CACHE = new HashMap<>();
    
    /**
     * 获取附魔，兼容新旧版本Minecraft
     * @param oldName 旧版本(1.8-1.12)附魔名称
     * @param newName 新版本(1.13+)附魔名称
     * @param id 旧版本附魔ID
     * @return 附魔实例
     */
    public static Enchantment getEnchantment(String oldName, String newName, int id) {
        // 先检查缓存
        String cacheKey = oldName + ":" + newName;
        if (ENCHANTMENT_CACHE.containsKey(cacheKey)) {
            return ENCHANTMENT_CACHE.get(cacheKey);
        }
        
        Enchantment result = null;
        try {
            if (VersionUtil.isLessThan113()) {
                // 旧版本尝试直接通过名称获取
                try {
                    result = Enchantment.getByName(oldName);
                } catch (Exception e) {
                    //尝试通过ID获取
//                    return Enchantment.getById(id);
                }
            } else {
                // 新版本尝试通过名称获取(Minecraft 1.13+)
                try {
                    java.lang.reflect.Field field = Enchantment.class.getDeclaredField(newName);
                    field.setAccessible(true);
                    result = (Enchantment) field.get(null);
                } catch (Exception e) {
                    // 尝试通过老名称获取
                    try {
                        java.lang.reflect.Field field = Enchantment.class.getDeclaredField(oldName);
                        field.setAccessible(true);
                        result = (Enchantment) field.get(null);
                    } catch (Exception ex) {
                        // 失败后静默回退
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("获取附魔失败: " + oldName + " 或 " + newName + " (ID: " + id + ")");
        }
        
        // 如果上面的方法都失败了，使用原始的遍历方式作为回退策略
        if (result == null) {
            for (Enchantment enchantment : Enchantment.values()) {
                if (enchantment.getName().equalsIgnoreCase(oldName) || 
                    enchantment.getName().equalsIgnoreCase(newName)) {
                    result = enchantment;
                    break;
                }
            }
        }
        
        // 将结果存入缓存
        if (result != null) {
            ENCHANTMENT_CACHE.put(cacheKey, result);
        }
        
        return result;
    }

    // ===================== 武器和工具附魔 =====================
    
    /**
     * 锋利 (攻击伤害)
     */
    public static Enchantment DAMAGE_ALL() {
        return getEnchantment("DAMAGE_ALL", "DAMAGE_ALL", 16);
    }
    
    /**
     * 效率 (挖掘速度)
     */
    public static Enchantment DIG_SPEED() {
        return getEnchantment("EFFICIENCY", "DIG_SPEED", 32);
    }
    
    /**
     * 击退
     */
    public static Enchantment KNOCKBACK() {
        return getEnchantment("KNOCKBACK", "KNOCKBACK", 19);
    }
    
    /**
     * 耐久
     */
    public static Enchantment DURABILITY() {
        return getEnchantment("UNBREAKING", "DURABILITY", 34);
    }
    
    // ===================== 弓箭附魔 =====================
    
    /**
     * 力量 (弓箭伤害)
     */
    public static Enchantment ARROW_DAMAGE() {
        return getEnchantment("POWER", "ARROW_DAMAGE", 48);
    }
    
    /**
     * 无限 (箭矢无限)
     */
    public static Enchantment ARROW_INFINITE() {
        return getEnchantment("INFINITY", "ARROW_INFINITE", 51);
    }
    
    /**
     * 火矢
     */
    public static Enchantment ARROW_FIRE() {
        return getEnchantment("FLAME", "ARROW_FIRE", 50);
    }
    
    /**
     * 穿透 (1.14+)
     */
    public static Enchantment ARROW_PIERCING() {
        return getEnchantment("PIERCING", "ARROW_PIERCING", 34);
    }
    
    // ===================== 防具附魔 =====================
    
    /**
     * 保护
     */
    public static Enchantment PROTECTION_ENVIRONMENTAL() {
        return getEnchantment("PROTECTION", "PROTECTION_ENVIRONMENTAL", 0);
    }
    
    /**
     * 火焰保护
     */
    public static Enchantment PROTECTION_FIRE() {
        return getEnchantment("FIRE_PROTECTION", "PROTECTION_FIRE", 1);
    }
    
    /**
     * 摔落保护
     */
    public static Enchantment PROTECTION_FALL() {
        return getEnchantment("FEATHER_FALLING", "PROTECTION_FALL", 2);
    }
    
    /**
     * 爆炸保护
     */
    public static Enchantment PROTECTION_EXPLOSIONS() {
        return getEnchantment("BLAST_PROTECTION", "PROTECTION_EXPLOSIONS", 3);
    }
    
    /**
     * 弹射物保护
     */
    public static Enchantment PROTECTION_PROJECTILE() {
        return getEnchantment("PROJECTILE_PROTECTION", "PROTECTION_PROJECTILE", 4);
    }
} 