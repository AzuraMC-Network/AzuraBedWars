package cc.azuramc.bedwars.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author an5w1r@163.com
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    /**
     * 数据库列名
     */
    String value();

    /**
     * 是否为主键
     */
    boolean primaryKey() default false;

    /**
     * 是否自增
     */
    boolean autoIncrement() default false;

    /**
     * 覆盖默认类型推断
     */
    String type() default "";

    /**
     * 默认值
     */
    String defaultValue() default "";

    /**
     * VARCHAR 等类型的长度
     */
    int size() default 0;

    /**
     * 是否允许为空
     */
    boolean nullable() default true;

    /**
     * 是否可更新（UPDATE 时是否包含该字段）
     * 例如 created_at 字段设置为 false
     */
    boolean updatable() default true;
}
