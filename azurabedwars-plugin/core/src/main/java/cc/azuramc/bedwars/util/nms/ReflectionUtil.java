package cc.azuramc.bedwars.util.nms;

import cc.azuramc.bedwars.util.LoggerUtil;
import com.cryptomorin.xseries.reflection.XReflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author An5w1r@163.com
 */
public class ReflectionUtil {

    private static final ConcurrentMap<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Field> FIELD_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Constructor<?>> CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();

    // NMS和OBC包名缓存
    public static String nmsPackage;
    public static String obcPackage;

    public static void initializeVersions() {
        try {
            nmsPackage = XReflection.NMS_PACKAGE;
            obcPackage = XReflection.CRAFTBUKKIT_PACKAGE;
            LoggerUtil.debug("ReflectionUtil$initializeVersions | nmsPackage: " + nmsPackage);
            LoggerUtil.debug("ReflectionUtil$initializeVersions | obcPackage: " + obcPackage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取NMS类
     *
     * @param className 类名
     * @return Class对象
     */
    public static Class<?> getNMSClass(String className) {
        String fullName = nmsPackage + "." + className;
        return getClass(fullName);
    }

    /**
     * 获取OBC类
     *
     * @param className 类名
     * @return Class对象
     */
    public static Class<?> getOBCClass(String className) {
        String fullName = obcPackage + "." + className;
        return getClass(fullName);
    }

    /**
     * 获取类（带缓存）
     *
     * @param className 完整类名
     * @return Class对象
     */
    public static Class<?> getClass(String className) {
        return CLASS_CACHE.computeIfAbsent(className, name -> {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("无法找到类: " + name, e);
            }
        });
    }

    /**
     * 获取字段（带缓存）
     *
     * @param clazz     目标类
     * @param fieldName 字段名
     * @return Field对象
     */
    public static Field getField(Class<?> clazz, String fieldName) {
        String key = clazz.getName() + "#" + fieldName;
        return FIELD_CACHE.computeIfAbsent(key, k -> {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                // 尝试在父类中查找
                return getFieldFromParent(clazz, fieldName);
            }
        });
    }

    /**
     * 从父类中递归查找字段
     */
    private static Field getFieldFromParent(Class<?> clazz, String fieldName) {
        Class<?> parent = clazz.getSuperclass();
        if (parent != null && parent != Object.class) {
            try {
                Field field = parent.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                return getFieldFromParent(parent, fieldName);
            }
        }
        throw new RuntimeException("无法找到字段: " + fieldName + " 在类 " + clazz.getName());
    }

    /**
     * 设置字段值
     *
     * @param target    目标对象
     * @param fieldName 字段名
     * @param value     新值
     */
    public static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = getField(target.getClass(), fieldName);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("设置字段失败: " + fieldName, e);
        }
    }

    /**
     * 获取字段值
     *
     * @param target    目标对象
     * @param fieldName 字段名
     * @return 字段值
     */
    public static <T> T getField(Object target, String fieldName) {
        try {
            Field field = getField(target.getClass(), fieldName);
            return (T) field.get(target);
        } catch (Exception e) {
            throw new RuntimeException("获取字段失败: " + fieldName, e);
        }
    }

    /**
     * 设置静态字段值
     *
     * @param clazz     目标类
     * @param fieldName 字段名
     * @param value     新值
     */
    public static void setStaticField(Class<?> clazz, String fieldName, Object value) {
        try {
            Field field = getField(clazz, fieldName);
            field.set(null, value);
        } catch (Exception e) {
            throw new RuntimeException("设置静态字段失败: " + fieldName, e);
        }
    }

    /**
     * 获取静态字段值
     *
     * @param clazz     目标类
     * @param fieldName 字段名
     * @return 字段值
     */
    public static <T> T getStaticField(Class<?> clazz, String fieldName) {
        try {
            Field field = getField(clazz, fieldName);
            return (T) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException("获取静态字段失败: " + fieldName, e);
        }
    }

    /**
     * 获取方法（带缓存）
     *
     * @param clazz      目标类
     * @param methodName 方法名
     * @param paramTypes 参数类型
     * @return Method对象
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        String key = clazz.getName() + "#" + methodName + "#" + Arrays.toString(paramTypes);
        return METHOD_CACHE.computeIfAbsent(key, k -> {
            try {
                Method method = clazz.getDeclaredMethod(methodName, paramTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException e) {
                return getMethodFromParent(clazz, methodName, paramTypes);
            }
        });
    }

    /**
     * 从父类中递归查找方法
     */
    private static Method getMethodFromParent(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        Class<?> parent = clazz.getSuperclass();
        if (parent != null && parent != Object.class) {
            try {
                Method method = parent.getDeclaredMethod(methodName, paramTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException e) {
                return getMethodFromParent(parent, methodName, paramTypes);
            }
        }
        throw new RuntimeException("无法找到方法: " + methodName + " 在类 " + clazz.getName());
    }

    /**
     * 调用方法
     *
     * @param target     目标对象
     * @param methodName 方法名
     * @param args       参数
     * @return 返回值
     */
    public static <T> T invokeMethod(Object target, String methodName, Object... args) {
        try {
            Class<?>[] paramTypes = getParameterTypes(args);
            Method method = getMethod(target.getClass(), methodName, paramTypes);
            return (T) method.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException("调用方法失败: " + methodName, e);
        }
    }

    /**
     * 调用静态方法
     *
     * @param clazz      目标类
     * @param methodName 方法名
     * @param args       参数
     * @return 返回值
     */
    public static <T> T invokeStaticMethod(Class<?> clazz, String methodName, Object... args) {
        try {
            Class<?>[] paramTypes = getParameterTypes(args);
            Method method = getMethod(clazz, methodName, paramTypes);
            return (T) method.invoke(null, args);
        } catch (Exception e) {
            throw new RuntimeException("调用静态方法失败: " + methodName, e);
        }
    }

    /**
     * 获取构造器（带缓存）
     *
     * @param clazz      目标类
     * @param paramTypes 参数类型
     * @return Constructor对象
     */
    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... paramTypes) {
        String key = clazz.getName() + "#constructor#" + Arrays.toString(paramTypes);
        return CONSTRUCTOR_CACHE.computeIfAbsent(key, k -> {
            try {
                Constructor<?> constructor = clazz.getDeclaredConstructor(paramTypes);
                constructor.setAccessible(true);
                return constructor;
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("无法找到构造器在类 " + clazz.getName(), e);
            }
        });
    }

    /**
     * 创建实例
     *
     * @param clazz 目标类
     * @param args  构造参数
     * @return 实例对象
     */
    public static <T> T newInstance(Class<T> clazz, Object... args) {
        try {
            Class<?>[] paramTypes = getParameterTypes(args);
            Constructor<?> constructor = getConstructor(clazz, paramTypes);
            return (T) constructor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException("创建实例失败: " + clazz.getName(), e);
        }
    }

    /**
     * 获取参数类型数组
     */
    private static Class<?>[] getParameterTypes(Object... args) {
        if (args == null || args.length == 0) {
            return new Class<?>[0];
        }

        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                types[i] = Object.class; // 对于null参数使用Object类型
            } else {
                types[i] = args[i].getClass();
                // 处理基本类型的包装类
                types[i] = convertWrapperToPrimitive(types[i]);
            }
        }
        return types;
    }

    /**
     * 将包装类转换为基本类型
     */
    private static Class<?> convertWrapperToPrimitive(Class<?> wrapper) {
        if (wrapper == Integer.class) return int.class;
        if (wrapper == Long.class) return long.class;
        if (wrapper == Double.class) return double.class;
        if (wrapper == Float.class) return float.class;
        if (wrapper == Boolean.class) return boolean.class;
        if (wrapper == Byte.class) return byte.class;
        if (wrapper == Short.class) return short.class;
        if (wrapper == Character.class) return char.class;
        return wrapper;
    }

    /**
     * 检查类是否存在
     *
     * @param className 类名
     * @return 是否存在
     */
    public static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 检查字段是否存在
     *
     * @param clazz     目标类
     * @param fieldName 字段名
     * @return 是否存在
     */
    public static boolean hasField(Class<?> clazz, String fieldName) {
        try {
            getField(clazz, fieldName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查方法是否存在
     *
     * @param clazz      目标类
     * @param methodName 方法名
     * @param paramTypes 参数类型
     * @return 是否存在
     */
    public static boolean hasMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        try {
            getMethod(clazz, methodName, paramTypes);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 清空所有缓存
     */
    public static void clearAllCaches() {
        CLASS_CACHE.clear();
        FIELD_CACHE.clear();
        METHOD_CACHE.clear();
        CONSTRUCTOR_CACHE.clear();
    }

    /**
     * 获取缓存统计信息
     */
    public static String getCacheStats() {
        return String.format(
                "缓存统计 - 类: %d, 字段: %d, 方法: %d, 构造器: %d",
                CLASS_CACHE.size(),
                FIELD_CACHE.size(),
                METHOD_CACHE.size(),
                CONSTRUCTOR_CACHE.size()
        );
    }

    /**
     * 获取玩家的NMS对象
     *
     * @param player Bukkit玩家对象
     * @return NMS玩家对象
     */
    public static Object getNMSPlayer(Object player) {
        try {
            Method getHandle = getMethod(player.getClass(), "getHandle");
            return getHandle.invoke(player);
        } catch (Exception e) {
            throw new RuntimeException("获取NMS玩家失败", e);
        }
    }

    /**
     * 获取任意CraftBukkit对象的NMS对象
     *
     * @param craftObject CraftBukkit对象
     * @return NMS对象
     */
    public static Object getNMSObject(Object craftObject) {
        try {
            Method getHandle = getMethod(craftObject.getClass(), "getHandle");
            return getHandle.invoke(craftObject);
        } catch (Exception e) {
            throw new RuntimeException("获取NMS对象失败: " + craftObject.getClass().getSimpleName(), e);
        }
    }
}
