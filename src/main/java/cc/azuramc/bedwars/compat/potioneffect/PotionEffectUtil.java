package cc.azuramc.bedwars.compat.potioneffect;

import cc.azuramc.bedwars.compat.VersionUtil;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class PotionEffectUtil {
    
    // 药水效果缓存
    private static final Map<String, PotionEffectType> EFFECT_CACHE = new HashMap<>();

    /**
     * 获取跳跃提升效果
     * @return 跳跃提升效果
     */
    public static PotionEffectType JUMP_BOOST() {
        // 检查缓存
        if (EFFECT_CACHE.containsKey("JUMP_BOOST")) {
            return EFFECT_CACHE.get("JUMP_BOOST");
        }
        
        try {
            PotionEffectType result;
            if (!VersionUtil.isLessThan113()) {
                result = (PotionEffectType) PotionEffectType.class.getDeclaredField("JUMP").get(null);
            } else {
                result = (PotionEffectType) PotionEffectType.class.getDeclaredField("JUMP_BOOST").get(null);
            }
            
            // 存入缓存
            if (result != null) {
                EFFECT_CACHE.put("JUMP_BOOST", result);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取速度效果
     * @return 速度效果
     */
    public static PotionEffectType SPEED() {
        // 检查缓存
        if (EFFECT_CACHE.containsKey("SPEED")) {
            return EFFECT_CACHE.get("SPEED");
        }
        
        try {
            PotionEffectType result = (PotionEffectType) PotionEffectType.class.getDeclaredField("SPEED").get(null);
            
            // 存入缓存
            if (result != null) {
                EFFECT_CACHE.put("SPEED", result);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }
} 