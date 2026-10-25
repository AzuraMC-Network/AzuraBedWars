package cc.azuramc.bedwars.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author an5w1r@163.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Range {
    double min() default -Double.MAX_VALUE;

    double max() default Double.MAX_VALUE;
}
