package cc.azuramc.bedwars.database.annotation;

import java.lang.annotation.*;

/**
 * @author an5w1r@163.com
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(QueryFields.class)
public @interface QueryField {
    /**
     * 查询键名称，用于区分不同的查询条件
     * 如 "byUuid", "byName" 等
     */
    String value();
}
