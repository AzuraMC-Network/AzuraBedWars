package cc.azuramc.bedwars.compat.util;

import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.util.LoggerUtil;
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
            if (trySetNewestGameRules(world)) {
                return world;
            }
        }

        if (VersionUtil.isGreaterOrEqual(1, 13)) {
            if (trySetLegacyGameRules(world)) {
                return world;
            }
        }

        trySetOldestGameRules(world);
        return world;
    }

    private static boolean trySetNewestGameRules(World world) {
        try {
            GameRule<?> spawnMobs = ReflectionUtil.getStaticField(GameRule.class, "SPAWN_MOBS");
            GameRule<?> fireSpreadRadius = ReflectionUtil.getStaticField(GameRule.class, "FIRE_SPREAD_RADIUS_AROUND_PLAYER");
            GameRule<?> showAdvancementMessages = ReflectionUtil.getStaticField(GameRule.class, "SHOW_ADVANCEMENT_MESSAGES");

            ReflectionUtil.invokeMethod(world, "setGameRule", spawnMobs, false);
            ReflectionUtil.invokeMethod(world, "setGameRule", fireSpreadRadius, 0);
            ReflectionUtil.invokeMethod(world, "setGameRule", showAdvancementMessages, false);
            LoggerUtil.debug("WorldUtil$trySetNewestGameRules | true");
            return true;
        } catch (Exception e) {
            LoggerUtil.debug("WorldUtil$trySetNewestGameRules | false");
            return false;
        }
    }

    private static boolean trySetLegacyGameRules(World world) {
        try {
            GameRule<?> doMobSpawning = ReflectionUtil.getStaticField(GameRule.class, "DO_MOB_SPAWNING");
            GameRule<?> doFireTick = ReflectionUtil.getStaticField(GameRule.class, "DO_FIRE_TICK");
            GameRule<?> announceAdvancements = ReflectionUtil.getStaticField(GameRule.class, "ANNOUNCE_ADVANCEMENTS");

            ReflectionUtil.invokeMethod(world, "setGameRule", doMobSpawning, false);
            ReflectionUtil.invokeMethod(world, "setGameRule", doFireTick, false);
            ReflectionUtil.invokeMethod(world, "setGameRule", announceAdvancements, false);
            LoggerUtil.debug("WorldUtil$trySetLegacyGameRules | true");
            return true;
        } catch (Exception e) {
            LoggerUtil.debug("WorldUtil$trySetLegacyGameRules | false");
            return false;
        }
    }

    private static void trySetOldestGameRules(World world) {
        try {
            ReflectionUtil.invokeMethod(world, "setGameRuleValue", "doMobSpawning", "false");
            ReflectionUtil.invokeMethod(world, "setGameRuleValue", "doFireTick", "false");
            ReflectionUtil.invokeMethod(world, "setGameRuleValue", "announceAdvancements", "false");
            LoggerUtil.debug("WorldUtil$trySetOldestGameRules | true");
        } catch (Exception e) {
            LoggerUtil.error("Failed to set game rules for world: " + world.getName());
            e.printStackTrace();
        }
    }
}
