package cc.azuramc.bedwars.compat.potioneffect;

import cc.azuramc.bedwars.compat.VersionUtil;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

/**
 * 药水效果工具类
 * 提供跨版本兼容的药水效果类型
 */
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
    
    /**
     * 获取失明效果
     * @return 失明效果
     */
    public static PotionEffectType BLINDNESS() {
        // 检查缓存
        if (EFFECT_CACHE.containsKey("BLINDNESS")) {
            return EFFECT_CACHE.get("BLINDNESS");
        }
        
        try {
            PotionEffectType result = (PotionEffectType) PotionEffectType.class.getDeclaredField("BLINDNESS").get(null);
            
            // 存入缓存
            if (result != null) {
                EFFECT_CACHE.put("BLINDNESS", result);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取挖掘疲劳效果
     * @return 挖掘疲劳效果
     */
    public static PotionEffectType MINING_FATIGUE() {
        // 检查缓存
        if (EFFECT_CACHE.containsKey("MINING_FATIGUE")) {
            return EFFECT_CACHE.get("MINING_FATIGUE");
        }
        
        try {
            PotionEffectType result;
            if (!VersionUtil.isLessThan113()) {
                result = (PotionEffectType) PotionEffectType.class.getDeclaredField("MINING_FATIGUE").get(null);
            } else {
                result = (PotionEffectType) PotionEffectType.class.getDeclaredField("SLOW_DIGGING").get(null);
            }
            
            // 存入缓存
            if (result != null) {
                EFFECT_CACHE.put("MINING_FATIGUE", result);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取急迫效果
     * @return 急迫效果
     */
    public static PotionEffectType HASTE() {
        // 检查缓存
        if (EFFECT_CACHE.containsKey("HASTE")) {
            return EFFECT_CACHE.get("HASTE");
        }
        
        try {
            PotionEffectType result;
            if (!VersionUtil.isLessThan113()) {
                result = (PotionEffectType) PotionEffectType.class.getDeclaredField("HASTE").get(null);
            } else {
                result = (PotionEffectType) PotionEffectType.class.getDeclaredField("FAST_DIGGING").get(null);
            }
            
            // 存入缓存
            if (result != null) {
                EFFECT_CACHE.put("HASTE", result);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取生命恢复效果
     * @return 生命恢复效果
     */
    public static PotionEffectType REGENERATION() {
        // 检查缓存
        if (EFFECT_CACHE.containsKey("REGENERATION")) {
            return EFFECT_CACHE.get("REGENERATION");
        }
        
        try {
            PotionEffectType result = (PotionEffectType) PotionEffectType.class.getDeclaredField("REGENERATION").get(null);
            
            // 存入缓存
            if (result != null) {
                EFFECT_CACHE.put("REGENERATION", result);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取缓慢效果
     * @return 缓慢效果
     */
    public static PotionEffectType SLOWNESS() {
        // 检查缓存
        if (EFFECT_CACHE.containsKey("SLOWNESS")) {
            return EFFECT_CACHE.get("SLOWNESS");
        }
        
        try {
            PotionEffectType result;
            if (!VersionUtil.isLessThan113()) {
                result = (PotionEffectType) PotionEffectType.class.getDeclaredField("SLOWNESS").get(null);
            } else {
                result = (PotionEffectType) PotionEffectType.class.getDeclaredField("SLOW").get(null);
            }
            
            // 存入缓存
            if (result != null) {
                EFFECT_CACHE.put("SLOWNESS", result);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }
} 