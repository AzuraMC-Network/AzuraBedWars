package cc.azuramc.bedwars.nms;

import cc.azuramc.bedwars.api.nms.NMSAccess;
import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.util.LoggerUtil;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * @author an5w1r@163.com
 */
@Getter
public class NMSProvider {

    /** First Minecraft version of each nms directory */
    private static final NavigableMap<Integer, String> MODULES = new TreeMap<>();

    static {
        MODULES.put(version(1, 8, 4), "v1_8_R3");
        MODULES.put(version(1, 9, 0), "v1_9_R1");
        MODULES.put(version(1, 9, 4), "v1_9_R2");
        MODULES.put(version(1, 10, 0), "v1_10_R1");
        MODULES.put(version(1, 11, 0), "v1_11_R1");
        MODULES.put(version(1, 12, 0), "v1_12_R1");
        MODULES.put(version(1, 13, 0), "v1_13_R1");
        MODULES.put(version(1, 13, 1), "v1_13_R2");
        MODULES.put(version(1, 14, 0), "v1_14_R1");
        MODULES.put(version(1, 15, 0), "v1_15_R1");
        MODULES.put(version(1, 16, 0), "v1_16_R1");
        MODULES.put(version(1, 16, 2), "v1_16_R2");
        MODULES.put(version(1, 16, 4), "v1_16_R3");
        MODULES.put(version(1, 17, 0), "v1_17_R1");
        MODULES.put(version(1, 18, 0), "v1_18_R1");
        MODULES.put(version(1, 18, 2), "v1_18_R2");
        MODULES.put(version(1, 19, 0), "v1_19_R1");
        MODULES.put(version(1, 19, 3), "v1_19_R2");
        MODULES.put(version(1, 19, 4), "v1_19_R3");
        MODULES.put(version(1, 20, 0), "v1_20_R1");
        MODULES.put(version(1, 20, 2), "v1_20_R2");
        MODULES.put(version(1, 20, 3), "v1_20_R3");
        MODULES.put(version(1, 20, 5), "v1_20_R4");
        MODULES.put(version(1, 21, 0), "v1_21_R1");
        MODULES.put(version(1, 21, 2), "v1_21_R2");
        MODULES.put(version(1, 21, 4), "v1_21_R3");
        MODULES.put(version(1, 21, 5), "v1_21_R4");
        MODULES.put(version(1, 21, 6), "v1_21_R5");
        MODULES.put(version(1, 21, 9), "v1_21_R6");
        MODULES.put(version(1, 21, 11), "v1_21_R7");
        MODULES.put(version(26, 1, 0), "v26_1");
        MODULES.put(version(26, 2, 0), "v26_2");
    }

    /** First Minecraft version the modules were not run on */
    private static final int UNTESTED = version(26, 4, 0);

    private NMSAccess access;
    private String module;

    public NMSAccess setup() {
        int version = version(VersionUtil.getMajorNumber(), VersionUtil.getMinorNumber(), VersionUtil.getPatchNumber());
        Map.Entry<Integer, String> entry = MODULES.floorEntry(version);
        this.module = entry == null ? null : entry.getValue();
        this.access = module == null ? null : createNMSAccess(module);

        if (access == null) {
            LoggerUtil.warn("NMS支持未找到 (" + VersionUtil.getParsedVersion() + ")! 启用兼容模式 可能存在意外问题!");
            access = new CompatibilityModeNMS();
        } else if (version >= UNTESTED) {
            LoggerUtil.warn("服务端版本 " + VersionUtil.getParsedVersion() + " 尚未经过测试, 正在使用 " + module);
        } else {
            LoggerUtil.info("正在使用受支持的版本! (" + module + ")");
        }

        return access;
    }

    private NMSAccess createNMSAccess(String module) {
        try {
            return (NMSAccess) Class.forName(this.getClass().getPackage().getName() + "." + module + ".NMS_" + module).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            LoggerUtil.warn("未受支持的版本: " + e.getMessage());
        } catch (InstantiationException | IllegalAccessException e) {
            LoggerUtil.warn("创建NMS访问失败: " + e.getMessage());
        } catch (InvocationTargetException e) {
            LoggerUtil.error("调用构造方法失败: " + e.getMessage());
        }
        return null;
    }

    private static int version(int major, int minor, int patch) {
        return major * 10_000 + minor * 100 + patch;
    }
}
