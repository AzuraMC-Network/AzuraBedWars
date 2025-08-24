package cc.azuramc.bedwars.loader.util;

import cc.azuramc.bedwars.loader.AzuraBedWarsLoader;
import org.bukkit.Bukkit;

/**
 * @author An5w1r@163.com
 */
public class LoggerUtil {

    private static final String LOGGER_PREFIX = "[AzuraBedWarsLoader] ";

    public static void info(String message) {
        Bukkit.getLogger().info(LOGGER_PREFIX + message);
    }

    public static void warn(String message) {
        Bukkit.getLogger().warning(LOGGER_PREFIX + message);
    }

    public static void error(String message) {
        Bukkit.getLogger().severe(LOGGER_PREFIX + message);
    }

    public static void verbose(String message) {
        if (!AzuraBedWarsLoader.getInstance().getConfigManager().isVerbose()) {
            return;
        }

        Bukkit.getLogger().info(LOGGER_PREFIX + "[VERBOSE] " + message);
    }
}
