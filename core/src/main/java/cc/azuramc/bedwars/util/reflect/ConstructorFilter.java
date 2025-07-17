package cc.azuramc.bedwars.util.reflect;

import java.lang.reflect.Constructor;

/**
 * @author Zoyn
 * @since 2017-12-02
 */
public interface ConstructorFilter {

    boolean accept(Constructor constructor);

}
