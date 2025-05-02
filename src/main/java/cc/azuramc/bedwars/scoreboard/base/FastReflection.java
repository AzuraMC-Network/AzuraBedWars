package cc.azuramc.bedwars.scoreboard.base;

import org.bukkit.Bukkit;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 反射工具类，用于访问 CraftBukkit 和 NMS 代码
 * 支持不同版本的 Minecraft，包括 1.17+ 的重新打包的 NMS
 * 
 * @author MrMicky
 */
public final class FastReflection {
    private static final String NM_PACKAGE = "net.minecraft";
    private static final String OBC_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();
    private static final String NMS_PACKAGE = OBC_PACKAGE.replace("org.bukkit.craftbukkit", NM_PACKAGE + ".server");

    private static final MethodType VOID_METHOD_TYPE = MethodType.methodType(void.class);

    private static final boolean NMS_REPACKAGED = optionalClass(NM_PACKAGE + ".network.protocol.Packet").isPresent();
    private static final boolean MOJANG_MAPPINGS = optionalClass(NM_PACKAGE + ".network.chat.Component").isPresent();

    /**
     * Unsafe 实例（懒加载）
     */
    private static volatile Object theUnsafe;

    /**
     * 私有构造函数防止实例化
     */
    private FastReflection() {
        throw new UnsupportedOperationException("工具类不可实例化");
    }

    /**
     * 检查 NMS 是否已重新打包（1.17+）
     * 
     * @return 如果 NMS 已重新打包则返回 true
     */
    public static boolean isRepackaged() {
        return NMS_REPACKAGED;
    }

    /**
     * 获取 NMS 类的完全限定名
     * 
     * @param post1_17package 1.17+ 版本中的包名
     * @param className 类名
     * @return 完全限定的类名
     */
    public static String nmsClassName(String post1_17package, String className) {
        if (NMS_REPACKAGED) {
            String classPackage = post1_17package == null ? NM_PACKAGE : NM_PACKAGE + '.' + post1_17package;
            return classPackage + '.' + className;
        }
        return NMS_PACKAGE + '.' + className;
    }

    /**
     * 获取 NMS 类
     * 
     * @param post1_17package 1.17+ 版本中的包名
     * @param className 类名
     * @return NMS 类
     * @throws ClassNotFoundException 如果类不存在
     */
    public static Class<?> nmsClass(String post1_17package, String className) throws ClassNotFoundException {
        return Class.forName(nmsClassName(post1_17package, className));
    }

    /**
     * 获取 NMS 类，支持 Spigot/Mojang 映射
     * 
     * @param post1_17package 1.17+ 版本中的包名
     * @param spigotClass Spigot 映射的类名
     * @param mojangClass Mojang 映射的类名
     * @return NMS 类
     * @throws ClassNotFoundException 如果类不存在
     */
    public static Class<?> nmsClass(String post1_17package, String spigotClass, String mojangClass) throws ClassNotFoundException {
        return nmsClass(post1_17package, MOJANG_MAPPINGS ? mojangClass : spigotClass);
    }

    /**
     * 尝试获取 NMS 类，返回 Optional
     * 
     * @param post1_17package 1.17+ 版本中的包名
     * @param className 类名
     * @return 包含 NMS 类的 Optional，如果类不存在则为空
     */
    public static Optional<Class<?>> nmsOptionalClass(String post1_17package, String className) {
        return optionalClass(nmsClassName(post1_17package, className));
    }

    /**
     * 获取 OBC 类的完全限定名
     * 
     * @param className 类名
     * @return 完全限定的类名
     */
    public static String obcClassName(String className) {
        return OBC_PACKAGE + '.' + className;
    }

    /**
     * 获取 OBC 类
     * 
     * @param className 类名
     * @return OBC 类
     * @throws ClassNotFoundException 如果类不存在
     */
    public static Class<?> obcClass(String className) throws ClassNotFoundException {
        return Class.forName(obcClassName(className));
    }

    /**
     * 尝试获取 OBC 类，返回 Optional
     * 
     * @param className 类名
     * @return 包含 OBC 类的 Optional，如果类不存在则为空
     */
    public static Optional<Class<?>> obcOptionalClass(String className) {
        return optionalClass(obcClassName(className));
    }

    /**
     * 尝试加载类，返回 Optional
     * 
     * @param className 类的完全限定名
     * @return 包含类的 Optional，如果类不存在则为空
     */
    public static Optional<Class<?>> optionalClass(String className) {
        try {
            return Optional.of(Class.forName(className));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * 获取枚举值
     * 
     * @param enumClass 枚举类
     * @param enumName 枚举名称
     * @return 枚举值
     */
    public static Object enumValueOf(Class<?> enumClass, String enumName) {
        return Enum.valueOf(enumClass.asSubclass(Enum.class), enumName);
    }

    /**
     * 获取枚举值，如果找不到则使用回退序号
     * 
     * @param enumClass 枚举类
     * @param enumName 枚举名称
     * @param fallbackOrdinal 回退序号
     * @return 枚举值
     */
    public static Object enumValueOf(Class<?> enumClass, String enumName, int fallbackOrdinal) {
        try {
            return enumValueOf(enumClass, enumName);
        } catch (IllegalArgumentException e) {
            Object[] constants = enumClass.getEnumConstants();
            if (constants.length > fallbackOrdinal) {
                return constants[fallbackOrdinal];
            }
            throw e;
        }
    }

    /**
     * 查找满足条件的内部类
     * 
     * @param parentClass 父类
     * @param classPredicate 类筛选条件
     * @return 内部类
     * @throws ClassNotFoundException 如果没有找到匹配的内部类
     */
    static Class<?> innerClass(Class<?> parentClass, Predicate<Class<?>> classPredicate) throws ClassNotFoundException {
        for (Class<?> innerClass : parentClass.getDeclaredClasses()) {
            if (classPredicate.test(innerClass)) {
                return innerClass;
            }
        }
        throw new ClassNotFoundException("在 " + parentClass.getCanonicalName() + " 中没有找到匹配条件的类");
    }

    /**
     * 尝试获取构造函数的 MethodHandle
     * 
     * @param declaringClass 声明类
     * @param lookup MethodHandles.Lookup 实例
     * @param type 方法类型
     * @return 包含 MethodHandle 的 Optional，如果找不到则为空
     * @throws IllegalAccessException 如果访问被拒绝
     */
    static Optional<MethodHandle> optionalConstructor(Class<?> declaringClass, MethodHandles.Lookup lookup, MethodType type) throws IllegalAccessException {
        try {
            return Optional.of(lookup.findConstructor(declaringClass, type));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    /**
     * 查找包构造函数
     * 先尝试使用 MethodHandles，如果失败则回退到 Unsafe
     * 
     * @param packetClass 包类
     * @param lookup MethodHandles.Lookup 实例
     * @return 包构造函数
     * @throws Exception 如果查找失败
     */
    static PacketConstructor findPacketConstructor(Class<?> packetClass, MethodHandles.Lookup lookup) throws Exception {
        // 首先尝试使用常规构造函数
        try {
            MethodHandle constructor = lookup.findConstructor(packetClass, VOID_METHOD_TYPE);
            return constructor::invoke;
        } catch (NoSuchMethodException | IllegalAccessException e) {
            // 如果失败，尝试使用 Unsafe
        }

        // 懒加载初始化 Unsafe
        initializeUnsafe();

        // 使用 Unsafe 分配实例
        MethodType allocateMethodType = MethodType.methodType(Object.class, Class.class);
        MethodHandle allocateMethod = lookup.findVirtual(theUnsafe.getClass(), "allocateInstance", allocateMethodType);
        return () -> allocateMethod.invoke(theUnsafe, packetClass);
    }

    /**
     * 初始化 Unsafe 实例（懒加载，线程安全）
     * 
     * @throws ClassNotFoundException 如果 Unsafe 类不存在
     * @throws NoSuchFieldException 如果 theUnsafe 字段不存在
     * @throws IllegalAccessException 如果访问被拒绝
     */
    private static void initializeUnsafe() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        if (theUnsafe == null) {
            synchronized (FastReflection.class) {
                if (theUnsafe == null) {
                    Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                    Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
                    theUnsafeField.setAccessible(true);
                    theUnsafe = theUnsafeField.get(null);
                }
            }
        }
    }

    /**
     * 包构造函数接口
     */
    @FunctionalInterface
    interface PacketConstructor {
        /**
         * 调用构造函数创建一个新的包实例
         * 
         * @return 新创建的包实例
         * @throws Throwable 如果创建失败
         */
        Object invoke() throws Throwable;
    }
}
