package cc.azuramc.bedwars.config.yaml;

import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author an5w1r@163.com
 */
public class YamlDeserializer {

    public void deserialize(ConfigurationSection section, Object instance) throws Exception {
        loadFromSection(section, instance);
    }

    private void loadFromSection(ConfigurationSection section, Object instance) throws Exception {
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            String yamlKey = field.getName();

            if (!section.contains(yamlKey)) {
                continue;
            }

            Class<?> fieldType = field.getType();
            Object value = loadFieldValue(section, yamlKey, fieldType);

            if (value != null) {
                field.set(instance, value);
            }
        }
    }

    private Object loadFieldValue(ConfigurationSection section, String key, Class<?> fieldType) throws Exception {
        if (isPrimitive(fieldType)) {
            return convertPrimitive(section.get(key), fieldType);
        }
        if (fieldType == String.class) {
            return section.getString(key);
        }
        if (fieldType.isArray() && fieldType.getComponentType() == int.class) {
            List<Integer> list = section.getIntegerList(key);
            return list.stream().mapToInt(Integer::intValue).toArray();
        }
        if (List.class.isAssignableFrom(fieldType)) {
            return section.getList(key);
        }
        if (Map.class.isAssignableFrom(fieldType)) {
            return loadMap(section, key);
        }

        return loadNestedObject(section, key, fieldType);
    }

    private Map<String, Object> loadMap(ConfigurationSection section, String key) {
        ConfigurationSection mapSection = section.getConfigurationSection(key);
        if (mapSection == null) {
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        for (String mapKey : mapSection.getKeys(false)) {
            map.put(mapKey, mapSection.get(mapKey));
        }
        return map;
    }

    private Object loadNestedObject(ConfigurationSection section, String key, Class<?> fieldType) throws Exception {
        ConfigurationSection nestedSection = section.getConfigurationSection(key);
        if (nestedSection == null) {
            return null;
        }
        Object nestedInstance = fieldType.getDeclaredConstructor().newInstance();
        loadFromSection(nestedSection, nestedInstance);
        return nestedInstance;
    }

    private boolean isPrimitive(Class<?> type) {
        return type.isPrimitive() ||
                type == Boolean.class ||
                type == Byte.class ||
                type == Character.class ||
                type == Short.class ||
                type == Integer.class ||
                type == Long.class ||
                type == Float.class ||
                type == Double.class;
    }

    private Object convertPrimitive(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        if (targetType == boolean.class || targetType == Boolean.class) {
            return value instanceof Boolean ? value : Boolean.parseBoolean(value.toString());
        }
        if (targetType == int.class || targetType == Integer.class) {
            return value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(value.toString());
        }
        if (targetType == long.class || targetType == Long.class) {
            return value instanceof Number ? ((Number) value).longValue() : Long.parseLong(value.toString());
        }
        if (targetType == double.class || targetType == Double.class) {
            return value instanceof Number ? ((Number) value).doubleValue() : Double.parseDouble(value.toString());
        }
        if (targetType == float.class || targetType == Float.class) {
            return value instanceof Number ? ((Number) value).floatValue() : Float.parseFloat(value.toString());
        }
        if (targetType == short.class || targetType == Short.class) {
            return value instanceof Number ? ((Number) value).shortValue() : Short.parseShort(value.toString());
        }
        if (targetType == byte.class || targetType == Byte.class) {
            return value instanceof Number ? ((Number) value).byteValue() : Byte.parseByte(value.toString());
        }
        if (targetType == char.class || targetType == Character.class) {
            String str = value.toString();
            return str.isEmpty() ? '\0' : str.charAt(0);
        }
        return value;
    }
}
