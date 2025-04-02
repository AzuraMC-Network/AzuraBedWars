package cc.azuramc.bedwars.compat.sound;

import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class SoundUtil {

    private static final Map<String, Sound> SOUND_CACHE = new HashMap<>();

    /**
     * 获取兼容的声音
     * @param oldName 旧版本(1.8-1.12)声音名称
     * @param newName 新版本(1.13+)声音名称
     * @return 对应当前服务器版本的声音
     */
    public static Sound get(String oldName, String newName) {
        // 检查缓存
        String cacheKey = oldName + ":" + newName;
        if (SOUND_CACHE.containsKey(cacheKey)) {
            return SOUND_CACHE.get(cacheKey);
        }
        
        Sound result = null;
        try {
            if (VersionUtil.isLessThan113()) {
                result = Sound.valueOf(oldName);
            } else {
                result = Sound.valueOf(newName);
            }
            
            // 存入缓存
            SOUND_CACHE.put(cacheKey, result);
        } catch (Exception ex) {
            Bukkit.getLogger().warning("获取声音失败: " + oldName + " 或 " + newName);
        }

        return result;
    }
    
    // 常用声音快捷方法
    public static Sound LEVEL_UP() {
        return get("LEVEL_UP", "ENTITY_PLAYER_LEVELUP");
    }
    
    public static Sound EXPLODE() {
        return get("EXPLODE", "ENTITY_GENERIC_EXPLODE");
    }
    
    public static Sound ENDERMAN_TELEPORT() {
        return get("ENDERMAN_TELEPORT", "ENTITY_ENDERMAN_TELEPORT");
    }

    public static Sound ITEM_PICKUP() {
        return get("ITEM_PICKUP", "ENTITY_ITEM_PICKUP");
    }

    public static Sound ORB_PICKUP() {
        return get("ORB_PICKUP", "ENTITY_EXPERIENCE_ORB_PICKUP");
    }

    public static Sound STEP_WOOL() {
        return get("STEP_WOOL", "BLOCK_CLOTH_STEP");
    }

    public static Sound ENDERDRAGON_HIT() {
        return get("ENDERDRAGON_HIT", "ENTITY_ENDER_DRAGON_HURT");
    }
    
    public static Sound CLICK() {
        return get("CLICK", "UI_BUTTON_CLICK");
    }

    public static Sound ENDERDRAGON_GROWL() {
        return get("ENDERDRAGON_GROWL", "ENTITY_ENDER_DRAGON_GROWL");
    }

    // 常用播放声音操作
    public static void playLevelUpSound(Player player) {
        player.playSound(player.getLocation(), LEVEL_UP(), 10, 15F);
    }

    public static void playClickSound(Player player) {
        player.playSound(player.getLocation(), CLICK(), 1, 10F);
    }

    public static void playItemPickupSound(Player player) {
        player.playSound(player.getLocation(), ITEM_PICKUP(), 1F, 1F);
    }

    public static void playOrbPickupSound(Player player) {
        player.playSound(player.getLocation(), ORB_PICKUP(), 10F, 1F);
    }

    public static void playExplodeSound(Player player) {
        player.playSound(player.getLocation(), EXPLODE(), 1, 1);
    }

    public static void playEndermanTeleportSound(Player player) {
        player.playSound(player.getLocation(), ENDERMAN_TELEPORT(), 30F, 1F);
    }

    public static void playDragonHitSound(Player player) {
        player.playSound(player.getLocation(), ENDERDRAGON_HIT(), 1, 1);
    }


    // AzuraBedWars独立

    public static void playEndermanTeleportSound(GamePlayer player) {
        player.playSound(ENDERMAN_TELEPORT(), 30F, 1F);
    }

    public static void broadcastEnderDragonGrowl(Game game) {
        game.broadcastSound(ENDERDRAGON_GROWL(), 1, 1);
    }

}
