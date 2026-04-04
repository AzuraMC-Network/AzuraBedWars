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

    public NMSAccess setup() {
        this.nmsVersion = VersionUtil.getNmsVersion();
        this.access = createNMSAccess(nmsVersion);

        if (this.access == null) {
            LoggerUtil.warn("NMS支持未找到 (" + this.nmsVersion + ")! 启用兼容模式 可能存在意外问题!");
            access = new CompatibilityModeNMS();
        } else if (nmsVersion == null) {
            LoggerUtil.info("正在使用 Mojang Namespace 运行时 (" + VersionUtil.getParsedVersion() + ")");
        } else {
            LoggerUtil.info("正在使用受支持的版本! (" + this.nmsVersion + ")");
        }

        return access;
    }

    private NMSAccess createNMSAccess(String version) {
        try {
            if (version == null) {
                return createMojangNamespaceAccess();
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

    private NMSAccess createMojangNamespaceAccess() throws
            ClassNotFoundException,
            NoSuchMethodException,
            InstantiationException,
            IllegalAccessException,
            InvocationTargetException {
        String pkg = resolveMojangNamespacePackage();
        return (NMSAccess) Class.forName(this.getClass().getPackage().getName() + "." + pkg + ".NMS_MojangNamespace").getDeclaredConstructor().newInstance();
    }

    private String resolveMojangNamespacePackage() {
        // 年份版本体系 (26.x.x+)
        if (VersionUtil.getMajorNumber() >= 26) {
            return "mojangnamespace26_1";
        }
        // Paper Mojang Namespace (1.20.5 ~ 25.x)
        // CraftBukkit 在 25.x 发布后才舍弃 v1_XX_RX 的命名格式 此处用来兼容 Paper 从 1.20.5 到 25.x
        // https://forums.papermc.io/threads/important-dev-psa-future-removal-of-cb-package-relocation.1106/
        return "mojangnamespace";
    }
}
