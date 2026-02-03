package cc.azuramc.bedwars.database.entity;

import cc.azuramc.bedwars.database.annotation.Column;
import cc.azuramc.bedwars.database.annotation.QueryField;
import cc.azuramc.bedwars.database.annotation.Table;
import lombok.Data;

/**
 * @author an5w1r@163.com
 */
@Data
@Table("database_version")
public class DatabaseVersion {

    /**
     * 查询键枚举
     */
    public enum Query {
        BY_VERSION
    }

    @QueryField("BY_VERSION")
    @Column(value = "version", nullable = false)
    private int version;
}
