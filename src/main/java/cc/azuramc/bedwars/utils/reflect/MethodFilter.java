package cc.azuramc.bedwars.utils.reflect;

import java.lang.reflect.Method;

public interface MethodFilter {

    boolean accept(Method method);

}
