package cc.azuramc.bedwars.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Settings of an extension referenced by another field of the same object, typed by the schema
 *
 * @author an5w1r@163.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExtensionSettings {

    /**
     * {@code <field>=<kind>}, the key of the field holding the reference and the extension kind it names
     */
    String[] value();
}
