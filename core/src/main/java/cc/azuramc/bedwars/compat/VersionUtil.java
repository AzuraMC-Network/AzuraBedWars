package cc.azuramc.bedwars.compat;

import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionUtil {

    private static final int MAJOR_NUMBER;
    private static final int MINOR_NUMBER;
    private static final int PATCH_NUMBER;
    private static final String PARSED_VERSION;

    /** Null on Paper 1.20.5+ and 26.x, which drop the versioned CraftBukkit package */
    private static final String NMS_VERSION;

    static {
        // 直接解析 Bukkit.getBukkitVersion()，例如:
        //   "1.21.4-R0.1-SNAPSHOT"  → MAJOR=1,  MINOR=21, PATCH=4
        //   "26.1.1-R0.1-SNAPSHOT"  → MAJOR=26, MINOR=1,  PATCH=1
        Matcher m = Pattern.compile("^(\\d+)\\.(\\d+)(?:\\.(\\d+))?").matcher(Bukkit.getBukkitVersion());
        if (!m.find()) {
            throw new IllegalStateException("无法解析服务端版本: \"" + Bukkit.getBukkitVersion() + '"');
        }
        MAJOR_NUMBER = Integer.parseInt(m.group(1));
        MINOR_NUMBER = Integer.parseInt(m.group(2));
        PATCH_NUMBER = m.group(3) != null ? Integer.parseInt(m.group(3)) : 0;
        PARSED_VERSION = MAJOR_NUMBER + "." + MINOR_NUMBER + "." + PATCH_NUMBER;

        String craftBukkitPackage = Bukkit.getServer().getClass().getPackage().getName();
        String lastSegment = craftBukkitPackage.substring(craftBukkitPackage.lastIndexOf('.') + 1);
        NMS_VERSION = lastSegment.startsWith("v") ? lastSegment : null;
    }

    /**
     * 获取主版本号 (e.g. "1.21.4" → 1, "26.1.1" → 26)
     */
    public static int getMajorNumber() {
        return MAJOR_NUMBER;
    }

    /**
     * 获取次版本号 (e.g. "1.21.4" → 21, "26.1.1" → 1)
     */
    public static int getMinorNumber() {
        return MINOR_NUMBER;
    }

    /**
     * 获取补丁版本号 (e.g. "1.21.4" → 4, "26.1.1" → 1)
     */
    public static int getPatchNumber() {
        return PATCH_NUMBER;
    }

    /**
     * 获取格式化版本字符串 (e.g. "1.21.4" 或 "26.1.1")
     */
    public static String getParsedVersion() {
        return PARSED_VERSION;
    }

    public static String getNmsVersion() {
        return NMS_VERSION;
    }

    /**
     * 判断是否大于等于指定版本（不含补丁）
     */
    public static boolean isGreaterOrEqual(int major, int minor) {
        if (MAJOR_NUMBER != major) {
            return MAJOR_NUMBER > major;
        }
        return MINOR_NUMBER >= minor;
    }

    /**
     * 判断是否大于等于指定版本（含补丁）
     */
    public static boolean isGreaterOrEqual(int major, int minor, int patch) {
        if (MAJOR_NUMBER != major) {
            return MAJOR_NUMBER > major;
        }
        if (MINOR_NUMBER != minor) {
            return MINOR_NUMBER > minor;
        }
        return PATCH_NUMBER >= patch;
    }

    /**
     * 判断是否小于指定版本（不含补丁）
     */
    public static boolean isLessThan(int major, int minor) {
        return !isGreaterOrEqual(major, minor);
    }

    /**
     * 判断是否小于指定版本（含补丁）
     */
    public static boolean isLessThan(int major, int minor, int patch) {
        return !isGreaterOrEqual(major, minor, patch);
    }

    /**
     * 判断是否是 1.13 以下版本
     */
    public static boolean isLessThan1_13() {
        return isLessThan(1, 13);
    }

    /**
     * 判断是否是 1.8.x
     */
    public static boolean isVersion1_8() {
        return MAJOR_NUMBER == 1 && MINOR_NUMBER == 8;
    }
}
