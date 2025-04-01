package cc.azuramc.bedwars.compat;

import org.bukkit.Bukkit;

public class VersionUtil {
    private static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    private static final int MAJOR_VERSION = Integer.parseInt(VERSION.split("_")[1]);
    private static final int MINOR_VERSION = Integer.parseInt(VERSION.split("_")[2]);

    /**
     * 获取服务器版本
     */
    public static String getVersion() {
        return VERSION;
    }

    /**
     * 获取主版本号
     */
    public static int getMajorVersion() {
        return MAJOR_VERSION;
    }

    /**
     * 获取次版本号
     */
    public static int getMinorVersion() {
        return MINOR_VERSION;
    }

    /**
     * 判断是否大于等于指定版本
     */
    public static boolean isGreaterOrEqual(int major, int minor) {
        return MAJOR_VERSION > major || (MAJOR_VERSION == major && MINOR_VERSION >= minor);
    }

    /**
     * 判断是否小于指定版本
     */
    public static boolean isLessThan(int major, int minor) {
        return !isGreaterOrEqual(major, minor);
    }

    /**
     * 判断是否是1.13及以下版本
     */
    public static boolean isLessThan113() {
        return isLessThan(1, 13);
    }

    /**
     * 判断是否是1.16及以下版本
     */
    public static boolean isLessThan116() {
        return isLessThan(1, 16);
    }

    /**
     * 判断是否是1.19及以上版本
     */
    public static boolean isLatestVersion() {
        return isGreaterOrEqual(1, 19);
    }
}