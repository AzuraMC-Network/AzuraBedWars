package cc.azuramc.bedwars.compat;

import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionUtil {

    private static final int MAJOR_NUMBER;
    private static final int MINOR_NUMBER;
    private static final int PATCH_NUMBER;
    private static final String PARSED_VERSION;

    /**
     * CraftBukkit NMS 版本字符串，例如 v1_20_R3。
     * Modern Paper (1.20.6+) 和年份版本体系服务端移除了版本前缀，此时为 null（Mojang Namespace 模式）
     */
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
        String patch = m.group(3);
        PATCH_NUMBER = (patch == null || patch.isEmpty()) ? 0 : Integer.parseInt(patch);
        PARSED_VERSION = MAJOR_NUMBER + "." + MINOR_NUMBER + "." + PATCH_NUMBER;

        NMS_VERSION = findNmsVersion();
    }

    /**
     * 扫描已加载的 Package，查找 CraftBukkit NMS 版本字符串（如 v1_20_R3）。
     * 若未找到（Paper 1.20.6+ / 年份版本体系），返回 null，表示 Mojang Namespace 模式。
     */
    private static String findNmsVersion() {
        for (Package pack : Package.getPackages()) {
            String name = pack.getName();
            if (name.startsWith("org.bukkit.craftbukkit.v")) {
                String version = name.split("\\.")[3];
                // 验证 CraftPlayer 存在，排除 Forge+Bukkit 混合端干扰
                try {
                    Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
                    return version;
                } catch (ClassNotFoundException ignored) {
                    // 此 package 并非真正的 CraftBukkit，继续扫描
                }
            }
        }
        return null;
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

    /**
     * 获取 CraftBukkit NMS 版本字符串 (e.g. v1_20_R3)。
     * Mojang Namespace 服务端返回 null。
     */
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
