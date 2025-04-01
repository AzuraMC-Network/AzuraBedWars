package cc.azuramc.bedwars.compat;

import org.bukkit.Bukkit;

public class VersionUtil {
    private static final String VERSION;
    private static final int MAJOR_VERSION;
    private static final int MINOR_VERSION;
    
    static {
        String version = "1_13"; // 默认版本
        int majorVersion = 1;
        int minorVersion = 13;
        
        try {
            version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            
            // 尝试解析版本号，格式一般为v1_XX_RX
            String[] versionParts = version.split("_");
            if (versionParts.length >= 3) {
                // 确保版本号是数字
                if (versionParts[1].matches("\\d+")) {
                    majorVersion = Integer.parseInt(versionParts[1]);
                }
                
                // 处理特殊情况：有些服务器版本格式为v1_XX_RX，我们需要确保只获取数字部分
                String minorStr = versionParts[2];
                if (minorStr.matches("\\d+")) {
                    minorVersion = Integer.parseInt(minorStr);
                } else {
                    // 如果包含非数字，则尝试提取数字部分
                    StringBuilder digits = new StringBuilder();
                    for (char c : minorStr.toCharArray()) {
                        if (Character.isDigit(c)) {
                            digits.append(c);
                        } else {
                            break;
                        }
                    }
                    if (!digits.isEmpty()) {
                        minorVersion = Integer.parseInt(digits.toString());
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("无法解析服务器版本: " + e.getMessage());
            Bukkit.getLogger().warning("使用默认版本: 1.13");
        }
        
        VERSION = version;
        MAJOR_VERSION = majorVersion;
        MINOR_VERSION = minorVersion;
    }

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
        return MAJOR_VERSION > major;
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