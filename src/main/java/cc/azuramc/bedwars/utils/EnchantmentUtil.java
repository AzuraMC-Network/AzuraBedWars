package cc.azuramc.bedwars.utils;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;

/**
 * 附魔兼容性工具类
 * 用于处理不同Minecraft版本(1.8-1.21)的Enchantment变化问题
 * By An5w1r_
 */
public class EnchantmentUtil {
    private static final boolean NEW_VERSION;
    
    static {
        boolean newVersion = false;
        try {
            // 1.13+版本存在Material.PLAYER_HEAD
            Class.forName("org.bukkit.Material").getDeclaredField("PLAYER_HEAD");
            newVersion = true;
        } catch (Exception e) {
            // 1.8-1.12版本
            newVersion = false;
        }
        NEW_VERSION = newVersion;
        
        Bukkit.getLogger().info("EnchantmentUtil初始化完成，当前服务器版本: " + (NEW_VERSION ? "1.13+" : "1.8-1.12"));
    }
    
    /**
     * 检查是否是新版本Minecraft (1.13+)
     * @return 如果是1.13+版本返回true
     */
    public static boolean isNewVersion() {
        return NEW_VERSION;
    }
    
    /**
     * 获取附魔，兼容新旧版本Minecraft
     * @param newName 新版本名称
     * @param oldName 旧版本名称
     * @param id 旧版本附魔ID
     * @return 附魔实例
     */
    public static Enchantment getEnchantment(String newName, String oldName, int id) {
        try {
            // 先尝试通过名称获取
            for (Enchantment enchantment : Enchantment.values()) {
                if (enchantment.getName().equalsIgnoreCase(newName) || 
                    enchantment.getName().equalsIgnoreCase(oldName)) {
                    return enchantment;
                }
            }
            
            // 尝试通过valueOf获取(新版本)
            if (NEW_VERSION) {
                try {
                    return (Enchantment) Enchantment.class.getDeclaredField(newName).get(null);
                } catch (Exception e) {
                    // 忽略错误
                }
            }
            
            // 尝试通过getByName获取(旧版本)
            try {
                java.lang.reflect.Method getByNameMethod = Enchantment.class.getMethod("getByName", String.class);
                Enchantment result = (Enchantment) getByNameMethod.invoke(null, oldName);
                if (result != null) return result;
            } catch (Exception e) {
                // 忽略错误
            }
            
            // 尝试通过ID获取(旧版本)
            try {
                java.lang.reflect.Method getByIdMethod = Enchantment.class.getMethod("getById", int.class);
                return (Enchantment) getByIdMethod.invoke(null, id);
            } catch (Exception e) {
                // 忽略错误
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("获取附魔失败: " + newName + "/" + oldName + " (ID: " + id + ")");
        }
        return null;
    }
    
    // ------ 常用附魔 ------ //
    
    /**
     * 获取锋利附魔
     * @return 锋利附魔
     */
    public static Enchantment DAMAGE_ALL() {
        return getEnchantment("DAMAGE_ALL", "DAMAGE_ALL", 16);
    }
    
    /**
     * 获取保护附魔
     * @return 保护附魔
     */
    public static Enchantment PROTECTION_ENVIRONMENTAL() {
        return getEnchantment("PROTECTION_ENVIRONMENTAL", "PROTECTION", 0);
    }
    
    /**
     * 获取效率附魔
     * @return 效率附魔
     */
    public static Enchantment DIG_SPEED() {
        return getEnchantment("DIG_SPEED", "EFFICIENCY", 32);
    }
    
    /**
     * 获取力量附魔
     * @return 力量附魔
     */
    public static Enchantment ARROW_DAMAGE() {
        return getEnchantment("ARROW_DAMAGE", "POWER", 48);
    }
    
    /**
     * 获取冲击附魔
     * @return 冲击附魔
     */
    public static Enchantment KNOCKBACK() {
        return getEnchantment("KNOCKBACK", "KNOCKBACK", 19);
    }
    
    /**
     * 获取穿透附魔
     * @return 穿透附魔
     */
    public static Enchantment ARROW_PIERCING() {
        return getEnchantment("ARROW_PIERCING", "PIERCING", 34);
    }
    
    /**
     * 获取无限附魔
     * @return 无限附魔
     */
    public static Enchantment ARROW_INFINITE() {
        return getEnchantment("ARROW_INFINITE", "INFINITY", 51);
    }
    
    /**
     * 获取火焰附魔
     * @return 火焰附魔
     */
    public static Enchantment ARROW_FIRE() {
        return getEnchantment("ARROW_FIRE", "FLAME", 50);
    }
    
    /**
     * 获取耐久附魔
     * @return 耐久附魔
     */
    public static Enchantment DURABILITY() {
        return getEnchantment("DURABILITY", "UNBREAKING", 34);
    }
} 