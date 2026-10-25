package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.api.config.Comment;
import cc.azuramc.bedwars.api.config.Range;
import cc.azuramc.bedwars.api.config.Raw;
import cc.azuramc.bedwars.database.provider.DatabaseType;
import lombok.Data;

/**
 * @author an5w1r@163.com
 */
@Data
@Comment({"Database", "Stores player data and statistics, changes apply after a restart"})
public class DatabaseConfig {

    @Comment({"Database type", "Options: MYSQL, MONGODB"})
    private DatabaseType databaseType = DatabaseType.MYSQL;

    @Comment("Host")
    @Raw
    private String host = "localhost";

    @Comment("Port")
    @Range(min = 1, max = 65535)
    private int port = 3306;

    @Comment("User name")
    @Raw
    private String username = "root";

    @Comment("Password")
    @Raw
    private String password = "123456";

    @Comment("Database name")
    @Raw
    private String database = "azurabw";
}
