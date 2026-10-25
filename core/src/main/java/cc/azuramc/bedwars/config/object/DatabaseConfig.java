package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.config.annotation.Comment;
import cc.azuramc.bedwars.config.annotation.Range;
import cc.azuramc.bedwars.config.annotation.Raw;
import cc.azuramc.bedwars.database.provider.DatabaseType;
import lombok.Data;

/**
 * @author an5w1r@163.com
 */
@Data
@Comment({"数据库配置", "用于存储玩家数据和统计信息, 修改后需要重启服务器"})
public class DatabaseConfig {

    @Comment({"数据库类型", "可选: MYSQL, MONGODB"})
    private DatabaseType databaseType = DatabaseType.MYSQL;

    @Comment("数据库主机地址")
    @Raw
    private String host = "localhost";

    @Comment("数据库端口")
    @Range(min = 1, max = 65535)
    private int port = 3306;

    @Comment("数据库用户名")
    @Raw
    private String username = "root";

    @Comment("数据库密码")
    @Raw
    private String password = "123456";

    @Comment("数据库名称")
    @Raw
    private String database = "azurabw";
}
