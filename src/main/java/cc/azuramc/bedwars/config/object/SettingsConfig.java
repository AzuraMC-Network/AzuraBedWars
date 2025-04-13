package cc.azuramc.bedwars.config.object;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设置配置对象
 * <p>
 * 存储插件的基本设置信息
 * </p>
 */
@Data
@EqualsAndHashCode
public class SettingsConfig {
    private boolean editorMode = false;
    private String defaultMapName = "";
    private String mapStorage = "JSON";
    private String databaseMapName = "bwdata";
    private String databaseMapTable = "maps";
} 