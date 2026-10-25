package cc.azuramc.bedwars.config;

import lombok.Value;

/**
 * @author an5w1r@163.com
 */
@Value
public class ConfigDocument<T> {
    String id;
    /** Relative to the plugin data folder */
    String path;
    Class<T> type;
    ApplyMode applyMode;
    /** Written as {@code config-version}, raised when the layout changes and a migration step is added */
    int version;
}
