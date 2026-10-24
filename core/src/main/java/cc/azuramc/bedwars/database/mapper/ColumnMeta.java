package cc.azuramc.bedwars.database.mapper;

import java.lang.reflect.Field;

/**
 * 列元数据记录
 *
 * @param field         字段
 * @param columnName    列名
 * @param primaryKey    是否为主键
 * @param autoIncrement 是否自增
 * @param type          类型覆盖
 * @param defaultValue  默认值
 * @param size          长度
 * @param nullable      是否允许为空
 * @param updatable     是否可更新
 * @author an5w1r@163.com
 */
public record ColumnMeta(
        Field field,
        String columnName,
        boolean primaryKey,
        boolean autoIncrement,
        String type,
        String defaultValue,
        int size,
        boolean nullable,
        boolean updatable
) {
    /**
     * 获取字段的 Java 类型
     */
    public Class<?> getFieldType() {
        return field.getType();
    }

    /**
     * 获取字段名
     */
    public String getFieldName() {
        return field.getName();
    }
}
