package cc.azuramc.bedwars.config.object;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设置配置对象
 * <p>
 * 存储插件的基本设置信息
 * </p>
 * @author an5w1r@163.com
 */
@Data
@EqualsAndHashCode
public class SettingsConfig {
    private boolean editorMode = false;
    private String defaultMapName = "";
    private String mapStorage = "JSON";
    private boolean enabledJedisMapFeature = false;

    private DatabaseConfig database = new DatabaseConfig();

    private DisplayDamage displayDamage = new DisplayDamage();

    private SetupMap setupMap = new SetupMap();

    @Data
    public static class DatabaseConfig {
        private String host = "localhost";
        private int port = 3306;
        private String username = "root";
        private String password = "";
        private String database = "azurabw";
    }

    @Data
    public static class DisplayDamage {
        private final boolean arrowDisplayEnabled = true;
        private final boolean attackDisplayEnabled = true;
    }

    @Data
    public static class SetupMap {
        private final int teamBlockSearchRadius = 5;
    }
} 