package cc.azuramc.bedwars.compat.enchantment;

import cc.azuramc.bedwars.compat.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;

public class EnchantmentUtil {
    
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
            if (!VersionUtil.isLessThan113()) {
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

    public static Enchantment DAMAGE_ALL() {
        return getEnchantment("DAMAGE_ALL", "DAMAGE_ALL", 16);
    }

    public static Enchantment PROTECTION_ENVIRONMENTAL() {
        return getEnchantment("PROTECTION_ENVIRONMENTAL", "PROTECTION", 0);
    }

    public static Enchantment DIG_SPEED() {
        return getEnchantment("DIG_SPEED", "EFFICIENCY", 32);
    }

    public static Enchantment ARROW_DAMAGE() {
        return getEnchantment("ARROW_DAMAGE", "POWER", 48);
    }

    public static Enchantment KNOCKBACK() {
        return getEnchantment("KNOCKBACK", "KNOCKBACK", 19);
    }

    public static Enchantment ARROW_PIERCING() {
        return getEnchantment("ARROW_PIERCING", "PIERCING", 34);
    }

    public static Enchantment ARROW_INFINITE() {
        return getEnchantment("ARROW_INFINITE", "INFINITY", 51);
    }

    public static Enchantment ARROW_FIRE() {
        return getEnchantment("ARROW_FIRE", "FLAME", 50);
    }

    public static Enchantment DURABILITY() {
        return getEnchantment("DURABILITY", "UNBREAKING", 34);
    }
} 