package cc.azuramc.bedwars.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;

/**
 * YAML与Java对象之间的转换工具类
 * 使用Bukkit的YamlConfiguration进行YAML操作
 * 使用Gson作为中间层实现对象序列化
 */
public class YamlConverter {

    private final Gson gson;

    public YamlConverter() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * 将Java对象转换为YamlConfiguration
     *
     * @param object 要转换的对象
     * @return YamlConfiguration对象
     */
    public YamlConfiguration toYaml(Object object) {
        YamlConfiguration yaml = new YamlConfiguration();

        // 先将对象转换为JSON字符串
        String json = gson.toJson(object);

        // 将JSON转换为Map
        @SuppressWarnings("unchecked")
        Map<String, Object> map = gson.fromJson(json, Map.class);

        // 将Map的内容设置到YamlConfiguration中
        if (map != null) {
            setMapToYaml(yaml, "", map);
        }

        return yaml;
    }

    /**
     * 将YamlConfiguration转换为Java对象
     *
     * @param yaml  YamlConfiguration对象
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 转换后的对象
     */
    public <T> T fromYaml(YamlConfiguration yaml, Class<T> clazz) {
        // 将YamlConfiguration转换为Map
        Map<String, Object> map = yamlToMap(yaml);

        // 将Map转换为JSON字符串
        String json = gson.toJson(map);

        // 将JSON字符串反序列化为对象
        return gson.fromJson(json, clazz);
    }

    /**
     * 将对象序列化为JSON字符串
     *
     * @param object 要序列化的对象
     * @return JSON字符串
     */
    public String toJson(Object object) {
        return gson.toJson(object);
    }

    /**
     * 从JSON字符串反序列化为对象
     *
     * @param json  JSON字符串
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 反序列化的对象
     */
    public <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    /**
     * 递归地将Map的内容设置到YamlConfiguration中
     *
     * @param yaml   YamlConfiguration对象
     * @param path   当前路径
     * @param map    要设置的Map
     */
    private void setMapToYaml(YamlConfiguration yaml, String path, Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String fullPath = path.isEmpty() ? key : path + "." + key;

            if (value instanceof Map) {
                // 递归处理嵌套Map
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                setMapToYaml(yaml, fullPath, nestedMap);
            } else if (value instanceof List) {
                // 处理List类型
                yaml.set(fullPath, processListValue((List<?>) value));
            } else {
                // 处理基本类型
                yaml.set(fullPath, processValue(value));
            }
        }
    }

    /**
     * 将YamlConfiguration转换为Map
     *
     * @param yaml YamlConfiguration对象
     * @return 转换后的Map
     */
    private Map<String, Object> yamlToMap(YamlConfiguration yaml) {
        return sectionToMap(yaml);
    }

    /**
     * 将ConfigurationSection转换为Map
     *
     * @param section ConfigurationSection对象
     * @return 转换后的Map
     */
    private Map<String, Object> sectionToMap(ConfigurationSection section) {
        Map<String, Object> map = new LinkedHashMap<>();

        if (section == null) {
            return map;
        }

        for (String key : section.getKeys(false)) {
            Object value = section.get(key);

            if (value instanceof ConfigurationSection) {
                // 递归处理嵌套的ConfigurationSection
                map.put(key, sectionToMap((ConfigurationSection) value));
            } else if (value instanceof List) {
                // 处理List类型
                map.put(key, processListFromYaml((List<?>) value));
            } else {
                // 处理基本类型
                map.put(key, value);
            }
        }

        return map;
    }

    /**
     * 处理List中的值
     * 将List中的嵌套Map转换为可序列化的格式
     *
     * @param list 原始List
     * @return 处理后的List
     */
    private List<?> processListValue(List<?> list) {
        List<Object> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> itemMap = (Map<String, Object>) item;
                result.add(processMapValue(itemMap));
            } else if (item instanceof List) {
                result.add(processListValue((List<?>) item));
            } else {
                result.add(processValue(item));
            }
        }
        return result;
    }

    /**
     * 处理从YAML读取的List
     *
     * @param list YAML中的List
     * @return 处理后的List
     */
    private List<?> processListFromYaml(List<?> list) {
        List<Object> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof ConfigurationSection) {
                result.add(sectionToMap((ConfigurationSection) item));
            } else if (item instanceof List) {
                result.add(processListFromYaml((List<?>) item));
            } else {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * 处理Map中的值
     *
     * @param map 原始Map
     * @return 处理后的Map
     */
    private Map<String, Object> processMapValue(Map<String, Object> map) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                result.put(key, processMapValue(nestedMap));
            } else if (value instanceof List) {
                result.put(key, processListValue((List<?>) value));
            } else {
                result.put(key, processValue(value));
            }
        }
        return result;
    }

    /**
     * 处理单个值
     * 将特殊类型转换为YAML兼容的格式
     *
     * @param value 原始值
     * @return 处理后的值
     */
    private Object processValue(Object value) {
        // Gson会将数字转换为Double，这里需要处理回Integer
        if (value instanceof Double) {
            Double d = (Double) value;
            if (d == d.intValue()) {
                return d.intValue();
            }
        }
        return value;
    }
}
