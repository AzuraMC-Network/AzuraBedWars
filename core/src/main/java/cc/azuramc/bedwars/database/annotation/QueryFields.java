package cc.azuramc.bedwars.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * QueryField 的容器注解，支持在同一字段上使用多个 @QueryField
 *
 * @author an5w1r@163.com
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryFields {
    QueryField[] value();
}
