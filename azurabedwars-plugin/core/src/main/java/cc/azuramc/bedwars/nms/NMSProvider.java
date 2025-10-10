package cc.azuramc.bedwars.nms;

import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.util.LoggerUtil;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;

/**
 * @author an5w1r@163.com
 */
@Getter
public class NMSProvider {
    private NMSAccess access;
    private String nmsVersion;
    private boolean isExperimentalVersionSupport;

    public NMSAccess setup() {
        if (VersionUtil.isGreaterOrEqual(1, 21, 10)) {
            isExperimentalVersionSupport = true;
        }
        this.nmsVersion = VersionUtil.getNmsVersion();
        this.access = createNMSAccess(nmsVersion);

        if (this.access == null) {
            LoggerUtil.warn("NMS支持未找到 (" + this.nmsVersion + ")! 启用兼容模式 可能存在意外问题!");
            access = new CompatibilityModeNMS();
        } else if (isExperimentalVersionSupport) {
            LoggerUtil.info("正在使用实验性版本支持 1.21.10 或更新的Minecraft版本");
        } else {
            LoggerUtil.info("正在使用受支持的版本! (" + this.nmsVersion + ")");
        }

        return access;
    }

    private NMSAccess createNMSAccess(String version) {

        try {
            if (isExperimentalVersionSupport) {
                return (NMSAccess) Class.forName(this.getClass().getPackage().getName() + ".v1_21_10.NMS_v1_21_10").getDeclaredConstructor().newInstance();
            }
            return (NMSAccess) Class.forName(this.getClass().getPackage().getName() + "." + version + ".NMS_" + version).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            LoggerUtil.warn("未受支持的版本: " + e.getMessage());
        } catch (InstantiationException | IllegalAccessException e) {
            LoggerUtil.warn("创建NMS访问失败: " + e.getMessage());
        } catch (InvocationTargetException e) {
            LoggerUtil.error("调用构造方法失败: " + e.getMessage());
        }
        return null;
    }
}
