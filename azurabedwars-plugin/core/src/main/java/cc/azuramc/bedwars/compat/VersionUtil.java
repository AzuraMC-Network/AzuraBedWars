package cc.azuramc.bedwars.compat;

import com.cryptomorin.xseries.reflection.XReflection;

public class VersionUtil {
    private static final int MAJOR_NUMBER = XReflection.MAJOR_NUMBER;
    private static final int MINOR_NUMBER = XReflection.MINOR_NUMBER;
    private static final int PATCH_NUMBER = XReflection.PATCH_NUMBER;
    private static final String PARSED_VERSION = MAJOR_NUMBER + "." + MINOR_NUMBER + "." + PATCH_NUMBER;
    private static final String NMS_VERSION = XReflection.NMS_VERSION;

    /**
     * 获取主版本号 (返回格式e.g. 1.12.2 返回 1)
     */
    public static int getMajorNumber() {
        return MAJOR_NUMBER;
    }

    /**
     * 获取次版本号 (返回格式e.g. 1.12.2 返回 12)
     */
    public static int getMinorNumber() {
        return MINOR_NUMBER;
    }

    /**
     * 获取次版本号 (返回格式e.g. 1.12.2 返回 2)
     */
    public static int getPatchNumber() {
        return PATCH_NUMBER;
    }

    /**
     * 获取服务器版本 (返回格式e.g. 1.8.8)
     */
    public static String getParsedVersion() {
        return PARSED_VERSION;
    }

    /**
     * 获取NMS版本号 (返回格式e.g. v1_8_R3)
     */
    public static String getNmsVersion() {
        return NMS_VERSION;
    }

    /**
     * 判断是否大于等于指定版本
     */
    public static boolean isGreaterOrEqual(int major, int minor) {
        if (MAJOR_NUMBER > major) {
            return true;
        } else if (MAJOR_NUMBER == major) {
            return MINOR_NUMBER >= minor;
        }
        return false;
    }

    /**
     * 判断是否小于指定版本
     */
    public static boolean isLessThan(int major, int minor) {
        return !isGreaterOrEqual(major, minor);
    }

    /**
     * 判断是否是1.13以下版本
     */
    public static boolean isLessThan1_13() {
        return isLessThan(1, 13);
    }

    /**
     * 判断是否是1.16以下版本
     */
    public static boolean isLessThan1_16() {
        return isLessThan(1, 16);
    }

    /**
     * 检查服务器版本是否是1.8.x
     *
     * @return 是否是1.8.x版本
     */
    public static boolean isVersion1_8() {
        return MINOR_NUMBER == 8;
    }
}
