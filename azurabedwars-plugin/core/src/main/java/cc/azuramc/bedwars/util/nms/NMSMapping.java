package cc.azuramc.bedwars.util.nms;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;

import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author An5w1r@163.com
 */
public class NMSMapping {
    private static final Map<String, Map<String, Map<String, String>>> mappings = new HashMap<>();
    private static String version;

    static {
        try {
            version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

            InputStreamReader reader = new InputStreamReader(
                    Objects.requireNonNull(NMSMapping.class.getResourceAsStream("/nms-mappings.json")), StandardCharsets.UTF_8
            );
            Type type = new TypeToken<Map<String, Map<String, Map<String, String>>>>() {
            }.getType();
            mappings.putAll(new Gson().fromJson(reader, type));
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取指定类的完整类名
     *
     * @param nmsSimpleName NMS 类名（不带包）
     * @return 完整的类名，如果没有映射则返回简单类名
     */
    public static String getClassName(String nmsSimpleName) {
        try {
            Map<String, Map<String, String>> classMapping = mappings.get(nmsSimpleName);
            if (classMapping != null && classMapping.containsKey("className")) {
                String fullClassName = classMapping.get("className").get(version);
                if (fullClassName != null) {
                    return fullClassName;
                }
            }
            // 如果没有找到映射 回退到使用ReflectionUtil的默认行为
            return nmsSimpleName;
        } catch (Exception e) {
            return nmsSimpleName;
        }
    }

    /**
     * 获取指定逻辑方法对应的 Method（带缓存）
     *
     * @param nmsSimpleName NMS 类名（不带包）
     * @param logicName     逻辑方法标识
     * @param paramTypes    参数类型
     */
    public static Method getMethod(String nmsSimpleName, String logicName, Class<?>... paramTypes) {
        try {
            String realName = mappings.get(nmsSimpleName).get(logicName).get(version);
            if (realName == null) {
                throw new RuntimeException("No mapping for " + nmsSimpleName + "." + logicName + " in " + version);
            }

            Class<?> clazz = getClassByMapping(nmsSimpleName);
            return ReflectionUtil.getMethod(clazz, realName, paramTypes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get method mapping for " + nmsSimpleName + "." + logicName, e);
        }
    }

    /**
     * 获取指定逻辑字段对应的 Field（带缓存）
     *
     * @param nmsSimpleName NMS 类名（不带包）
     * @param logicName     逻辑字段标识
     */
    public static Field getField(String nmsSimpleName, String logicName) {
        try {
            String realName = mappings.get(nmsSimpleName).get(logicName).get(version);
            if (realName == null) {
                throw new RuntimeException("No mapping for " + nmsSimpleName + "." + logicName + " in " + version);
            }

            Class<?> clazz = getClassByMapping(nmsSimpleName);
            return ReflectionUtil.getField(clazz, realName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field mapping for " + nmsSimpleName + "." + logicName, e);
        }
    }

    /**
     * 根据映射获取类对象
     *
     * @param nmsSimpleName NMS 类名（不带包）
     * @return Class对象
     */
    private static Class<?> getClassByMapping(String nmsSimpleName) {
        String fullClassName = getClassName(nmsSimpleName);
        if (fullClassName.equals(nmsSimpleName)) {
            // 没有找到完整类名映射 使用ReflectionUtil的默认行为
            return ReflectionUtil.getNMSClass(nmsSimpleName);
        } else {
            // 使用完整类名直接获取类
            return ReflectionUtil.getClass(fullClassName);
        }
    }

}

