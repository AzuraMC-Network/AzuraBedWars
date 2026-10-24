package cc.azuramc.bedwars.database.dialect;

/**
 * @author an5w1r@163.com
 */
public sealed interface SqlDialect permits MySqlDialect {

    /**
     * 获取方言名称
     */
    String getDialectName();

    /**
     * 包装标识符（表名、列名）
     */
    String wrapIdentifier(String name);

    /**
     * 获取整数类型
     */
    String getIntType();

    /**
     * 获取双精度浮点类型
     */
    String getDoubleType();

    /**
     * 获取字符串类型（带长度）
     */
    String getVarcharType(int size);

    /**
     * 获取文本类型
     */
    String getTextType();

    /**
     * 获取时间戳类型
     */
    String getTimestampType();

    /**
     * 获取布尔类型
     */
    String getBooleanType();

    /**
     * 获取主键自增列定义
     */
    String getPrimaryKeyAutoIncrement();

    /**
     * 获取主键定义（非自增）
     */
    String getPrimaryKeyDefinition(String type);

    /**
     * 获取当前时间戳默认值表达式
     */
    String getDefaultCurrentTimestamp();

    /**
     * 获取更新时自动更新时间戳的表达式（某些数据库不支持）
     */
    default String getOnUpdateCurrentTimestamp() {
        return "";
    }

    /**
     * 获取 NOT NULL 约束
     */
    default String getNotNull() {
        return "NOT NULL";
    }

    /**
     * 获取表选项（引擎、字符集等）
     */
    default String getTableOptions() {
        return "";
    }

    /**
     * 获取 LIMIT 子句
     */
    default String getLimitClause(int limit) {
        return "LIMIT " + limit;
    }

    /**
     * 是否支持 IF NOT EXISTS
     */
    default boolean supportsIfNotExists() {
        return true;
    }
}
