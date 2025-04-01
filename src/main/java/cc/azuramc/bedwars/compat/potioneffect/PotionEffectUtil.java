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
    
    /**
     * 获取抗火效果
     * @return 抗火效果
     */
    public static PotionEffectType FIRE_RESISTANCE() {
        // 检查缓存
        if (EFFECT_CACHE.containsKey("FIRE_RESISTANCE")) {
            return EFFECT_CACHE.get("FIRE_RESISTANCE");
        }
        
        try {
            PotionEffectType result = (PotionEffectType) PotionEffectType.class.getDeclaredField("FIRE_RESISTANCE").get(null);
            
            // 存入缓存
            if (result != null) {
                EFFECT_CACHE.put("FIRE_RESISTANCE", result);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取力量效果
     * @return 力量效果
     */
    public static PotionEffectType STRENGTH() {
        // 检查缓存
        if (EFFECT_CACHE.containsKey("STRENGTH")) {
            return EFFECT_CACHE.get("STRENGTH");
        }
        
        try {
            PotionEffectType result;
            if (!VersionUtil.isLessThan113()) {
                result = (PotionEffectType) PotionEffectType.class.getDeclaredField("STRENGTH").get(null);
            } else {
                result = (PotionEffectType) PotionEffectType.class.getDeclaredField("INCREASE_DAMAGE").get(null);
            }
            
            // 存入缓存
            if (result != null) {
                EFFECT_CACHE.put("STRENGTH", result);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取隐身效果
     * @return 隐身效果
     */
    public static PotionEffectType INVISIBILITY() {
        // 检查缓存
        if (EFFECT_CACHE.containsKey("INVISIBILITY")) {
            return EFFECT_CACHE.get("INVISIBILITY");
        }
        
        try {
            PotionEffectType result = (PotionEffectType) PotionEffectType.class.getDeclaredField("INVISIBILITY").get(null);
            
            // 存入缓存
            if (result != null) {
                EFFECT_CACHE.put("INVISIBILITY", result);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取水下呼吸效果
     * @return 水下呼吸效果
     */
    public static PotionEffectType WATER_BREATHING() {
        // 检查缓存
        if (EFFECT_CACHE.containsKey("WATER_BREATHING")) {
            return EFFECT_CACHE.get("WATER_BREATHING");
        }
        
        try {
            PotionEffectType result = (PotionEffectType) PotionEffectType.class.getDeclaredField("WATER_BREATHING").get(null);
            
            // 存入缓存
            if (result != null) {
                EFFECT_CACHE.put("WATER_BREATHING", result);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取夜视效果
     * @return 夜视效果
     */
    public static PotionEffectType NIGHT_VISION() {
        // 检查缓存
        if (EFFECT_CACHE.containsKey("NIGHT_VISION")) {
            return EFFECT_CACHE.get("NIGHT_VISION");
        }
        
        try {
            PotionEffectType result;
            if (!VersionUtil.isLessThan113()) {
                result = (PotionEffectType) PotionEffectType.class.getDeclaredField("NIGHT_VISION").get(null);
            } else {
                result = (PotionEffectType) PotionEffectType.class.getDeclaredField("NIGHT_VISION").get(null);
            }
            
            // 存入缓存
            if (result != null) {
                EFFECT_CACHE.put("NIGHT_VISION", result);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取指定名称的药水效果类型
     * 用于获取不常用或自定义的药水效果类型
     * 
     * @param oldName 1.8-1.12版本的名称
     * @param newName 1.13+版本的名称
     * @return 药水效果类型
     */
    public static PotionEffectType getCompatibleEffect(String oldName, String newName) {
        // 生成缓存键名
        String cacheKey = oldName + "_" + newName;
        
        // 检查缓存
        if (EFFECT_CACHE.containsKey(cacheKey)) {
            return EFFECT_CACHE.get(cacheKey);
        }
        
        try {
            PotionEffectType result;
            if (!VersionUtil.isLessThan113()) {
                result = PotionEffectType.getByName(newName);
            } else {
                result = PotionEffectType.getByName(oldName);
            }
            
            // 存入缓存
            if (result != null) {
                EFFECT_CACHE.put(cacheKey, result);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 检查给定的药水效果类型是否存在
     * 
     * @param effectType 要检查的药水效果类型
     * @return 如果存在返回true，否则返回false
     */
    public static boolean hasEffect(PotionEffectType effectType) {
        return effectType != null;
    }
    
    /**
     * 清除药水效果缓存
     * 在重载插件或服务器配置时使用
     */
    public static void clearCache() {
        EFFECT_CACHE.clear();
    }
} 