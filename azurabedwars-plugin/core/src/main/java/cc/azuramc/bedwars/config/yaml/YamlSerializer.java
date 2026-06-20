package cc.azuramc.bedwars.config.yaml;

import cc.azuramc.bedwars.config.annotation.Comment;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

/**
 * @author an5w1r@163.com
 */
public class YamlSerializer {

    public String serialize(Object instance) throws Exception {
        StringBuilder result = new StringBuilder();

        Comment classComment = instance.getClass().getAnnotation(Comment.class);
        if (classComment != null) {
            for (String comment : classComment.value()) {
                result.append("# ").append(comment).append("\n");
            }
            result.append("\n");
        }

        serializeObject(result, instance, 0);
        return result.toString();
    }

    private void serializeObject(StringBuilder result, Object instance, int indentLevel) throws Exception {
        String indent = getIndent(indentLevel);
        boolean isFirst = true;

        for (Field field : instance.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            Object value = field.get(instance);

            if (value == null) {
                continue;
            }

            Comment comment = field.getAnnotation(Comment.class);
            if (comment != null) {
                if (!isFirst) {
                    result.append("\n");
                }
                for (String line : comment.value()) {
                    result.append(indent).append("# ").append(line).append("\n");
                }
            }

            result.append(indent).append(field.getName()).append(":");

            Class<?> fieldType = field.getType();

            if (isPrimitive(fieldType) || fieldType == String.class) {
                result.append(" ").append(serializeValue(value)).append("\n");
            } else if (fieldType.isArray() && fieldType.getComponentType() == int.class) {
                serializeIntArray(result, (int[]) value, indent);
            } else if (List.class.isAssignableFrom(fieldType)) {
                serializeList(result, (List<?>) value, indent);
            } else if (Map.class.isAssignableFrom(fieldType)) {
                serializeMap(result, (Map<?, ?>) value, indent);
            } else {
                result.append("\n");
                serializeObject(result, value, indentLevel + 1);
            }

            isFirst = false;
        }
    }

    private void serializeIntArray(StringBuilder result, int[] arr, String indent) {
        result.append("\n");
        for (int item : arr) {
            result.append(indent).append("  - ").append(item).append("\n");
        }
    }

    private void serializeList(StringBuilder result, List<?> list, String indent) {
        if (list.isEmpty()) {
            result.append(" []\n");
        } else {
            result.append("\n");
            for (Object item : list) {
                result.append(indent).append("  - ").append(serializeValue(item)).append("\n");
            }
        }
    }

    private void serializeMap(StringBuilder result, Map<?, ?> map, String indent) {
        if (map.isEmpty()) {
            result.append(" {}\n");
        } else {
            result.append("\n");
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                result.append(indent).append("  ").append(entry.getKey()).append(": ").append(serializeValue(entry.getValue())).append("\n");
            }
        }
    }

    private String serializeValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Boolean || value instanceof Number) {
            return value.toString();
        }
        String str = value.toString();
        return "\"" + escapeString(str) + "\"";
    }

    /**
     * 转义后的字符串，保存YAML时必须使用双引号解析而不可使用单引号，否则不解析转义
     */
    private String escapeString(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String getIndent(int level) {
        return " ".repeat(Math.max(0, level * 2));
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
}
