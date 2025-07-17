package cc.azuramc.bedwars.util.reflect;

import org.apache.commons.lang3.Validate;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Easy to reflect
 *
 * @author Zoyn
 * @since 2017-12-02
 */
public final class ReflectionUtils {

    private ReflectionUtils() {
    }

    /**
     * get field objects by using field names
     *
     * @param clazz     class's object
     * @param fieldName field's name
     * @return {@link Field}
     * @throws NoSuchFieldException If the field with the specified name cannot be found
     * @see #hasField(Class, String)
     */
    public static Field getFieldByFieldName(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Field field = null;
        if (hasField(clazz, fieldName)) {
            field = clazz.getField(fieldName);
        }
        return field;
    }

    /**
     * @param classPath class's path
     * @param fieldName field's name
     * @return {@link Field}
     * @throws ClassNotFoundException If the class cannot be found
     * @throws NoSuchFieldException   If the field with the specified name cannot be found
     * @see #getFieldByFieldName(Class, String)
     */
    public static Field getFieldByFieldName(String classPath, String fieldName) throws ClassNotFoundException, NoSuchFieldException {
        return getFieldByFieldName(Class.forName(classPath), fieldName);
    }


    /**
     * get a Value field
     *
     * @param obj       object
     * @param fieldName field's name
     * @return {@link Object}
     * @throws IllegalAccessException If the field is accessible
     * @see #getFieldByFieldName(Class, String)
     */
    public static Object getValueByFieldName(Object obj, String fieldName) throws IllegalAccessException, NoSuchFieldException {
        Field field = getFieldByFieldName(obj.getClass(), fieldName);
        Object value = null;

        if (field != null) {
            if (field.isAccessible()) {
                value = field.get(obj);
            } else {
                field.setAccessible(true);
                value = field.get(obj);
                field.setAccessible(false);
            }
        }
        return value;
    }

    /**
     * set a object's Value field
     *
     * @param obj       object
     * @param fieldName field's name
     * @param value     the value to be set
     * @throws NoSuchFieldException If the field is missing
     */
    public static void setValueByFieldName(Object obj, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        if (field.isAccessible()) {
            field.set(obj, value);
        } else {
            field.setAccessible(true);
            field.set(obj, value);
            field.setAccessible(false);
        }
    }

    /**
     * get a class's constructor
     *
     * @param clazz          class's object
     * @param parameterTypes parameters
     * @return {@link Constructor}
     * @throws NoSuchMethodException If the constructor with the specified parameter types cannot be found
     * @see #hasConstructor(Class, Class[])
     */
    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameterTypes) throws NoSuchMethodException {
        Constructor constructor = null;
        if (hasConstructor(clazz, parameterTypes)) {
            constructor = clazz.getDeclaredConstructor(parameterTypes);
        }
        return constructor;
    }

    /**
     * get a class's constructor
     *
     * @param classPath      class's path
     * @param parameterTypes parameters
     * @return {@link Constructor}
     * @throws ClassNotFoundException If the class cannot be found
     * @throws NoSuchMethodException  If the constructor with the specified parameter types cannot be found
     * @see #getConstructor(Class, Class[])
     */
    public static Constructor<?> getConstructor(String classPath, Class<?>... parameterTypes) throws ClassNotFoundException, NoSuchMethodException {
        return getConstructor(Class.forName(classPath), parameterTypes);
    }


    /**
     * Constructing an object with a constructor
     *
     * @param constructor the Constructor
     * @param arguments   the constructor's arguments
     * @return {@link Object}
     * @throws IllegalAccessException    If the desired constructor cannot be accessed due to certain circumstances
     * @throws InvocationTargetException If the desired constructor cannot be invoked
     * @throws InstantiationException    If you cannot create an instance of the target class due to certain circumstances
     */
    public static Object instantiateObject(Constructor<?> constructor, Object... arguments) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return Validate.notNull(constructor).newInstance(arguments);
    }

    /**
     * get a method in a class
     *
     * @param clazz          class's object
     * @param methodName     method's name
     * @param parameterTypes the method's arguments
     * @return {@link Method}
     * @throws NoSuchMethodException If the method with the specified parameter types cannot be found
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        if (hasMethod(clazz, methodName, parameterTypes)) {
            return clazz.getDeclaredMethod(methodName, parameterTypes);
        }
        return null;
    }

    /**
     * get a method in a class
     *
     * @param classPath      class's path
     * @param methodName     method's name
     * @param parameterTypes the method's arguments
     * @return {@link Method}
     * @throws ClassNotFoundException If the class cannot be found
     * @throws NoSuchMethodException  If the method with the specified parameter types cannot be found
     * @see #getMethod(Class, String, Class[])
     */
    public static Method getMethod(String classPath, String methodName, Class<?>... parameterTypes) throws ClassNotFoundException, NoSuchMethodException {
        return getMethod(Class.forName(classPath), methodName, parameterTypes);
    }

    /**
     * Invoke a method
     *
     * @param method    the method object
     * @param object    the object will be invoke
     * @param arguments the method arguments
     * @return {@link Object}
     * @throws InvocationTargetException If the desired method cannot be invoked
     * @throws IllegalAccessException    If the desired method cannot be accessed due to certain circumstances
     */
    public static Object invokeMethod(Method method, Object object, Object... arguments) throws InvocationTargetException, IllegalAccessException {
        Validate.notNull(method);
        Object o;

        if (method.isAccessible()) {
            o = method.invoke(object, arguments);
        } else {
            method.setAccessible(true);
            o = method.invoke(object, arguments);
            method.setAccessible(false);
        }
        return o;
    }

    /**
     * Invoke a method
     *
     * @param methodName the method name
     * @param object     the object will be invoke
     * @param arguments  the method arguments
     * @return {@link Object}
     * @throws InvocationTargetException If the desired method cannot be invoked
     * @throws IllegalAccessException    If the desired method cannot be accessed due to certain circumstances
     */
    public static Object invokeMethod(String methodName, Object object, Object... arguments) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method;
        Class[] classes = new Class[0];
        for (int i = 0; i < arguments.length; i++) {
            classes[i] = arguments[i].getClass();
        }

        method = getMethod(object.getClass(), methodName, classes);
        return invokeMethod(method, object, arguments);
    }

    /**
     * check a class has a specified field
     *
     * @param clazz     class's object
     * @param fieldName field's name
     * @return return true if the field is exist
     */
    public static boolean hasField(Class<?> clazz, String fieldName) {
        boolean has;

        try {
            Validate.notNull(clazz).getDeclaredField(fieldName);
            has = true;
        } catch (NoSuchFieldException e) {
            has = false;
        }
        return has;
    }

    /**
     * check a class has a specified field
     *
     * @param clazz  class's object
     * @param filter filter obj
     * @return return true if the field is exist
     */
    public static boolean hasField(Class<?> clazz, FieldFilter filter) {
        Validate.notNull(filter);

        boolean has = false;
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (filter.accept(field)) {
                has = true;
                break;
            }
        }
        return has;
    }

    /**
     * check a class has a specified field
     *
     * @param classPath class's path
     * @param fieldName field's name
     * @return return true if the field is exist
     * @see #hasField(Class, String)
     */
    public static boolean hasField(String classPath, String fieldName) throws ClassNotFoundException {
        return hasField(Class.forName(classPath), fieldName);
    }

    /**
     * check a class has a specified constructor
     *
     * @param clazz          class's object
     * @param parameterTypes the constructor with the specified parameter types
     * @return return true if the constructor is exist
     */
    public static boolean hasConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        boolean has;
        try {
            Validate.notNull(clazz).getDeclaredConstructor(parameterTypes);
            has = true;
        } catch (NoSuchMethodException e) {
            has = false;
        }
        return has;
    }

    /**
     * check a class has a specified constructor
     *
     * @param clazz  class's object
     * @param filter filter obj
     * @return return true if the constructor is exist
     */
    public static boolean hasConstructor(Class<?> clazz, ConstructorFilter filter) {
        Validate.notNull(filter);

        boolean has = false;
        Constructor[] constructors = clazz.getDeclaredConstructors();
        for (Constructor constructor : constructors) {
            if (filter.accept(constructor)) {
                has = true;
                break;
            }
        }
        return has;
    }

    /**
     * check a class has a specified constructor
     *
     * @param classPath      class's path
     * @param parameterTypes the constructor with the specified parameter types
     * @return return true if the constructor is exist
     * @see #hasField(Class, String)
     */
    public static boolean hasConstructor(String classPath, Class<?>... parameterTypes) throws ClassNotFoundException {
        return hasConstructor(Class.forName(classPath), parameterTypes);
    }


    /**
     * check a class has a specified method
     *
     * @param clazz          class's object
     * @param methodName     method's name
     * @param parameterTypes the method with the specified parameter types
     * @return return true if the method is exist
     */
    public static boolean hasMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        boolean has;
        try {
            Validate.notNull(clazz).getDeclaredMethod(methodName, parameterTypes);
            has = true;
        } catch (NoSuchMethodException e) {
            has = false;
        }
        return has;
    }

    /**
     * check a class has a specified method
     *
     * @param clazz  class's object
     * @param filter filter obj
     * @return return true if the constructor is exist
     */
    public static boolean hasMethod(Class<?> clazz, MethodFilter filter) {
        Validate.notNull(filter);

        boolean has = false;
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (filter.accept(method)) {
                has = true;
                break;
            }
        }
        return has;
    }

    /**
     * check a class has a specified method
     *
     * @param classPath      class's path
     * @param methodName     method's name
     * @param parameterTypes the method with the specified parameter types
     * @return return true if the constructor is exist
     * @see #hasMethod(Class, String, Class[])
     */
    public static boolean hasMethod(String classPath, String methodName, Class<?>... parameterTypes) throws ClassNotFoundException {
        return hasMethod(Class.forName(classPath), methodName, parameterTypes);
    }
}