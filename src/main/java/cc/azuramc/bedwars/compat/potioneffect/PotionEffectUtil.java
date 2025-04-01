package cc.azuramc.bedwars.compat.potioneffect;

import org.bukkit.potion.PotionEffectType;

/**
 * 药水效果兼容性工具类
 * 用于处理不同Minecraft版本(1.8-1.21)的PotionEffectType名称变化
 * By An5w1r_
 */
public class PotionEffectUtil {
    private static final boolean NEW_VERSION;
    
    static {
        boolean newVersion = false;
        try {
            // 1.13+版本存在PotionEffectType.JUMP
            Class.forName("org.bukkit.potion.PotionEffectType").getDeclaredField("JUMP");
            newVersion = true;
        } catch (Exception e) {
            // 1.8-1.12版本
            newVersion = false;
        }
        NEW_VERSION = newVersion;
    }
    
    /**
     * 获取跳跃提升效果
     * @return 跳跃提升效果
     */
    public static PotionEffectType JUMP_BOOST() {
        try {
            if (NEW_VERSION) {
                return (PotionEffectType) PotionEffectType.class.getDeclaredField("JUMP").get(null);
            } else {
                return (PotionEffectType) PotionEffectType.class.getDeclaredField("JUMP_BOOST").get(null);
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取速度效果
     * @return 速度效果
     */
    public static PotionEffectType SPEED() {
        try {
            return (PotionEffectType) PotionEffectType.class.getDeclaredField("SPEED").get(null);
        } catch (Exception e) {
            return null;
        }
    }
} 