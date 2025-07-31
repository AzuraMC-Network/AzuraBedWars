package cc.azuramc.bedwars.util;

import cc.azuramc.bedwars.AzuraBedWars;
import org.bukkit.Bukkit;

/**
 * @author An5w1r
 */
public class LoggerUtil {

    private static final String LOGGER_PREFIX = "[AzuraBedWars] ";

    public static final String ADMIN_PERMISSION = "azurabedwars.admin";

    public static void info(String message) {
        Bukkit.getLogger().info(LOGGER_PREFIX + message);
    }

    public static void warn(String message) {
        Bukkit.getLogger().warning(LOGGER_PREFIX + message);
    }

    public static void error(String message) {
        Bukkit.getLogger().severe(LOGGER_PREFIX + message);
    }

    public static void debug(String message) {
        if (!AzuraBedWars.getInstance().getSettingsConfig().isDebugMode()) {
            return;
        }
        Bukkit.broadcast(message, ADMIN_PERMISSION);
        info(LOGGER_PREFIX + "[Debugger] " + message);
    }

    public static void printChat(String message) {
        info("[Chat] " + message);
    }
}
