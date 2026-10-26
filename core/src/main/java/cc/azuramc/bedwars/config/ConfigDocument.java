package cc.azuramc.bedwars.config;

import lombok.Value;
import org.jetbrains.annotations.Nullable;

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
    /** Name of the schema file and of the migration steps, shared by every file of a directory */
    String kind;
    /** Jar resource copied when the file is missing, null to write the defaults of {@link #type} */
    @Nullable
    String template;
}
