package cc.azuramc.bedwars.util.reflect;

import java.lang.reflect.Field;

/**
 * @author Zoyn
 * @since 2017-12-02
 */
public interface FieldFilter {

    boolean accept(Field field);

}
