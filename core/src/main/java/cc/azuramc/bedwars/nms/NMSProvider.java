package cc.azuramc.bedwars.nms;

import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.util.LoggerUtil;
import lombok.Getter;

/**
 * @author An5w1r@163.com
 */
@Getter
public class NMSProvider {
    private NMSAccess access;
    private String nmsVersion;

    public NMSAccess setup() {
        this.nmsVersion = VersionUtil.getNmsVersion();
        this.access = createNMSAccess(nmsVersion);

        if (this.access == null) {
            LoggerUtil.warn("NMS支持未找到 (" + this.nmsVersion + ")! 启用兼容模式 可能存在意外问题!");
            access = new CompatibilityModeNMS();
        } else {
            LoggerUtil.info("正在使用受支持的版本! (" + this.nmsVersion + ")");
        }

        return access;
    }

    private NMSAccess createNMSAccess(String version) {
        try {
            return (NMSAccess) Class.forName(this.getClass().getPackage().getName() + ".NMS_" + version).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            LoggerUtil.warn("未受支持的版本: " + e.getMessage());
        } catch (InstantiationException | IllegalAccessException e) {
            LoggerUtil.warn("创建NMS访问失败: " + e.getMessage());
        }
        return null;
    }
}
