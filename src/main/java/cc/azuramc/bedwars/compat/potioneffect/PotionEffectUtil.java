package cc.azuramc.bedwars.compat.potioneffect;

import cc.azuramc.bedwars.compat.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class PotionEffectUtil {

    private static final Map<String, PotionEffectType> EFFECT_CACHE = new HashMap<>();

    /**
     * 获取兼容的药水效果
     * @param oldName 旧版本(1.8-1.12)药水效果名称
     * @param newName 新版本(1.13+)药水效果名称
     * @return 对应当前服务器版本的药水效果
     */
    public static PotionEffectType get(String oldName, String newName) {
        // 检查缓存
        String cacheKey = oldName + ":" + newName;
        if (EFFECT_CACHE.containsKey(cacheKey)) {
            return EFFECT_CACHE.get(cacheKey);
        }
        
        PotionEffectType result = null;
        try {
            if (VersionUtil.isLessThan113()) {
                result = (PotionEffectType) PotionEffectType.class.getDeclaredField(oldName).get(null);
            } else {
                result = (PotionEffectType) PotionEffectType.class.getDeclaredField(newName).get(null);
            }
            
            // 存入缓存
            if (result != null) {
                EFFECT_CACHE.put(cacheKey, result);
            }
        } catch (Exception ex) {
            Bukkit.getLogger().warning("获取药水效果失败: " + oldName + " 或 " + newName);
        }

        return result;
    }
    
    /**
     * 获取跳跃提升效果
     * @return 跳跃提升效果
     */
    public static PotionEffectType JUMP_BOOST() {
        return get("JUMP", "JUMP_BOOST");
    }

    /**
     * 获取速度效果
     * @return 速度效果
     */
    public static PotionEffectType SPEED() {
        return get("SPEED", "SPEED");
    }
    
    /**
     * 获取生命恢复效果
     * @return 生命恢复效果
     */
    public static PotionEffectType REGENERATION() {
        return get("REGENERATION", "REGENERATION");
    }
    
    /**
     * 获取力量效果
     * @return 力量效果
     */
    public static PotionEffectType INCREASE_DAMAGE() {
        return get("INCREASE_DAMAGE", "INCREASE_DAMAGE");
    }
    
    /**
     * 获取抗性提升效果
     * @return 抗性提升效果
     */
    public static PotionEffectType DAMAGE_RESISTANCE() {
        return get("DAMAGE_RESISTANCE", "DAMAGE_RESISTANCE"); 
    }
} 