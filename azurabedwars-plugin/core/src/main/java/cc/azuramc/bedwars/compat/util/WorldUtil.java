package cc.azuramc.bedwars.compat.util;

import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.util.nms.ReflectionUtil;
import org.bukkit.GameRule;
import org.bukkit.World;

/**
 * @author an5w1r@163.com
 */
public class WorldUtil {

    public static World setWorldRules(World world) {
        world.setAutoSave(false);

        if (VersionUtil.isGreaterOrEqual(1, 21, 11)) {
            world.setGameRule(GameRule.SPAWN_MOBS, false);
            world.setGameRule(GameRule.FIRE_SPREAD_RADIUS_AROUND_PLAYER, 0);
            world.setGameRule(GameRule.SHOW_ADVANCEMENT_MESSAGES, false);
            return world;
        }

        if (VersionUtil.isGreaterOrEqual(1, 13)) {
            // 使用 ReflectionUtil 调用 setGameRule 方法以兼容不同版本
            try {
                // 尝试获取 GameRule 枚举值
                GameRule<?> doMobSpawning = getGameRule("DO_MOB_SPAWNING");
                GameRule<?> doFireTick = getGameRule("DO_FIRE_TICK");
                GameRule<?> announceAdvancements = getGameRule("ANNOUNCE_ADVANCEMENTS");

                if (doMobSpawning != null) {
                    ReflectionUtil.invokeMethod(world, "setGameRule", doMobSpawning, false);
                }
                if (doFireTick != null) {
                    ReflectionUtil.invokeMethod(world, "setGameRule", doFireTick, false);
                }
                if (announceAdvancements != null) {
                    ReflectionUtil.invokeMethod(world, "setGameRule", announceAdvancements, false);
                }
            } catch (Exception e) {
                // 如果反射失败，回退到字符串方式
                setGameRuleByString(world, "doMobSpawning", "false");
                setGameRuleByString(world, "doFireTick", "false");
                setGameRuleByString(world, "announceAdvancements", "false");
            }
            return world;
        }

        // 1.13 以下版本使用字符串方式
        setGameRuleByString(world, "doMobSpawning", "false");
        setGameRuleByString(world, "doFireTick", "false");
        setGameRuleByString(world, "announceAdvancements", "false");
        return world;
    }

    /**
     * 通过 ReflectionUtil 获取 GameRule 枚举值
     */
    private static GameRule<?> getGameRule(String name) {
        try {
            return ReflectionUtil.getStaticField(GameRule.class, name);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 使用 ReflectionUtil 调用 setGameRuleValue 方法
     */
    private static void setGameRuleByString(World world, String rule, String value) {
        try {
            ReflectionUtil.invokeMethod(world, "setGameRuleValue", rule, value);
        } catch (Exception e) {
            // 静默失败，某些版本可能不支持
        }
    }
}
