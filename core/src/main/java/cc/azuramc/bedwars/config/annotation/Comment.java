package cc.azuramc.bedwars.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author an5w1r@163.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Comment {
    /**
     * 注释内容，支持多行
     *
     * @return 注释行数组
     */
    String[] value();
}
