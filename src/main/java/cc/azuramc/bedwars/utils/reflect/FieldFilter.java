package cc.azuramc.bedwars.utils.reflect;

import java.lang.reflect.Field;

public interface FieldFilter {

    boolean accept(Field field);

}
