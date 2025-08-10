package cc.azuramc.bedwars.util.nms;

import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author An5w1r@163.com
 */
public class NMSMapping {
    private static final Map<String, Map<String, Object>> mappings = new HashMap<>();
    private static String version;
    private static JsonObject rawMappings;


    public static void initNmsMapping() {
        try {
            version = VersionUtil.getNmsVersion();

            LoggerUtil.debug("NMSMapping$initNmsMapping | var version is: " + version);

            InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(NMSMapping.class.getResourceAsStream("/nms-mappings.json")), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            rawMappings = gson.fromJson(reader, JsonObject.class);

            // 解析映射数据
            for (Map.Entry<String, JsonElement> classEntry : rawMappings.entrySet()) {
                String className = classEntry.getKey();
                JsonObject classData = classEntry.getValue().getAsJsonObject();
                Map<String, Object> classMapping = new HashMap<>();

                for (Map.Entry<String, JsonElement> fieldEntry : classData.entrySet()) {
                    String fieldName = fieldEntry.getKey();
                    JsonElement fieldData = fieldEntry.getValue();
                    classMapping.put(fieldName, fieldData.getAsJsonObject());
                }
                mappings.put(className, classMapping);
            }

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
            Map<String, Object> classMapping = mappings.get(nmsSimpleName);
            if (classMapping != null && classMapping.containsKey("className")) {
                String fullClassName = resolveMapping(classMapping.get("className"));
                if (fullClassName != null) {
                    // 处理{nmsVersion}占位符
                    return fullClassName.replace("{nmsVersion}", version);
                }
            }
            // 如果没有找到映射 回退到使用ReflectionUtil的默认行为
            return nmsSimpleName;
        } catch (Exception e) {
            return nmsSimpleName;
        }
    }

    /**
     * 获取指定逻辑方法对应的 Method
     *
     * @param nmsSimpleName NMS 类名（不带包）
     * @param logicName     逻辑方法标识
     * @param paramTypes    参数类型
     */
    public static Method getMethod(String nmsSimpleName, String logicName, Class<?>... paramTypes) {
        try {
            Map<String, Object> classMapping = mappings.get(nmsSimpleName);
            if (classMapping == null || !classMapping.containsKey(logicName)) {
                throw new RuntimeException("No mapping for " + nmsSimpleName + "." + logicName);
            }

            String realName = resolveMapping(classMapping.get(logicName));
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
     * 获取指定逻辑字段对应的 Field
     *
     * @param nmsSimpleName NMS 类名（不带包）
     * @param logicName     逻辑字段标识
     */
    public static Field getField(String nmsSimpleName, String logicName) {
        try {
            Map<String, Object> classMapping = mappings.get(nmsSimpleName);
            if (classMapping == null || !classMapping.containsKey(logicName)) {
                throw new RuntimeException("No mapping for " + nmsSimpleName + "." + logicName);
            }

            String realName = resolveMapping(classMapping.get(logicName));
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
     * 解析映射值 使用版本范围格式
     *
     * @param mappingData 映射数据
     * @return 解析后的值
     */
    private static String resolveMapping(Object mappingData) {
        if (mappingData instanceof JsonObject jsonObj) {

            if (jsonObj.has("versionRanges")) {
                JsonArray versionRanges = jsonObj.getAsJsonArray("versionRanges");

                for (JsonElement rangeElement : versionRanges) {
                    JsonObject range = rangeElement.getAsJsonObject();
                    String condition = range.get("condition").getAsString();

                    int major, minor;

                    switch (condition) {
                        case "lessThan":
                            major = range.get("major").getAsInt();
                            minor = range.get("minor").getAsInt();
                            if (VersionUtil.isLessThan(major, minor)) {
                                return range.get("value").getAsString();
                            }
                            break;

                        case "greaterOrEqual":
                            major = range.get("major").getAsInt();
                            minor = range.get("minor").getAsInt();
                            boolean matches = VersionUtil.isGreaterOrEqual(major, minor);

                            // 检查排除版本
                            if (matches && range.has("excludeVersions")) {
                                JsonArray excludeVersions = range.getAsJsonArray("excludeVersions");
                                for (JsonElement excludeElement : excludeVersions) {
                                    if (version.equals(excludeElement.getAsString())) {
                                        matches = false;
                                        break;
                                    }
                                }
                            }

                            if (matches) {
                                return range.get("value").getAsString();
                            }
                            break;

                        case "specificVersions":
                            JsonArray versions = range.getAsJsonArray("versions");
                            for (JsonElement versionElement : versions) {
                                if (version.equals(versionElement.getAsString())) {
                                    return range.get("value").getAsString();
                                }
                            }
                            break;
                    }
                }
            }
        }
        return null;
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

