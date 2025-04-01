package cc.azuramc.bedwars.compat.potioneffect;

import cc.azuramc.bedwars.compat.VersionUtil;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectUtil {

    public static PotionEffectType get(String v18, String v113) {
        PotionEffectType finalEffect = null;

        try {
            if (VersionUtil.isLessThan113()) {
                finalEffect = (PotionEffectType) PotionEffectType.class.getDeclaredField(v18).get(null);
            } else {
                finalEffect = (PotionEffectType) PotionEffectType.class.getDeclaredField(v113).get(null);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return finalEffect;
    }
    
    /**
     * 获取跳跃提升效果
     * @return 跳跃提升效果
     */
    public static PotionEffectType JUMP_BOOST() {
        try {
            if (VersionUtil.isLessThan113()) {
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