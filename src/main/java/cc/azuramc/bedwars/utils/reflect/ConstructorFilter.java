package cc.azuramc.bedwars.utils.reflect;

import java.lang.reflect.Constructor;

public interface ConstructorFilter {

    boolean accept(Constructor constructor);

}
