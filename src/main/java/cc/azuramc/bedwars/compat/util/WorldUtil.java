package cc.azuramc.bedwars.compat.util;

import cc.azuramc.bedwars.compat.VersionUtil;
import org.bukkit.GameRule;
import org.bukkit.World;

/**
 * @author an5w1r@163.com
 */
public class WorldUtil {

    @SuppressWarnings("deprecation")
    public static World setWorldRules(World world) {
        world.setAutoSave(false);
        if (!VersionUtil.isLessThan113()) {
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            world.setGameRule(GameRule.DO_FIRE_TICK, false);
        } else {
            world.setGameRuleValue("doMobSpawning", "false");
            world.setGameRuleValue("doFireTick", "false");
        }
        return world;
    }
}
