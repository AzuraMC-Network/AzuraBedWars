package cc.azuramc.bedwars.config.yaml;

import cc.azuramc.bedwars.api.config.Comment;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.nodes.Node;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Writes a config object as commented YAML. Used once to create a missing file, never to rewrite one
 *
 * @author an5w1r@163.com
 */
public final class YamlWriter {

    private static final Pattern PLAIN_KEY = Pattern.compile("[A-Za-z0-9_.-]+");

    private YamlWriter() {
    }

    /**
     * @param schemaPath relative to the written file, picked up by editors with the YAML language server
     */
    public static String write(Object instance, int version, @Nullable String schemaPath) {
        StringBuilder out = new StringBuilder();
        if (schemaPath != null) {
            out.append("# yaml-language-server: $schema=").append(schemaPath).append('\n');
        }
        Comment classComment = instance.getClass().getAnnotation(Comment.class);
        if (classComment != null) {
            for (String line : classComment.value()) {
                out.append("# ").append(line).append('\n');
            }
        }
        out.append(ConfigKeys.VERSION_KEY).append(": ").append(version).append('\n');
        if (instance instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) instance).entrySet()) {
                String key = String.valueOf(entry.getKey());
                out.append('\n').append(PLAIN_KEY.matcher(key).matches() ? key : quote(key)).append(':');
                writeValue(out, entry.getValue(), 0, true);
            }
            return out.toString();
        }
        writeObject(out, instance, 0, true, true);
        return out.toString();
    }

    private static void writeObject(StringBuilder out, Object instance, int indent, boolean spaced, boolean comments) {
        String pad = pad(indent);
        boolean first = true;
        for (Map.Entry<String, Field> entry : ConfigKeys.fields(instance.getClass()).entrySet()) {
            Object value = get(entry.getValue(), instance);
            if (value == null || value instanceof Node) {
                continue;
            }
            Comment comment = entry.getValue().getAnnotation(Comment.class);
            if (spaced || (comments && comment != null && !first)) {
                out.append('\n');
            }
            first = false;
            if (comment != null && comments) {
                for (String line : comment.value()) {
                    out.append(pad).append("# ").append(line).append('\n');
                }
            }
            out.append(pad).append(entry.getKey()).append(':');
            writeValue(out, value, indent, comments);
        }
    }

    private static void writeValue(StringBuilder out, Object value, int indent, boolean comments) {
        if (ConfigKeys.isScalar(value.getClass())) {
            out.append(' ').append(scalar(value)).append('\n');
            return;
        }
        if (value.getClass().isArray() || value instanceof List) {
            List<Object> items = items(value);
            if (items.isEmpty()) {
                out.append(" []\n");
                return;
            }
            out.append('\n');
            for (Object item : items) {
                if (ConfigKeys.isScalar(item.getClass())) {
                    out.append(pad(indent + 1)).append("- ").append(scalar(item)).append('\n');
                } else {
                    StringBuilder nested = new StringBuilder();
                    writeObject(nested, item, indent + 2, false, false);
                    out.append(pad(indent + 1)).append("- ").append(nested.substring((indent + 2) * 2));
                }
            }
            return;
        }
        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            if (map.isEmpty()) {
                out.append(" {}\n");
                return;
            }
            out.append('\n');
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                out.append(pad(indent + 1)).append(PLAIN_KEY.matcher(key).matches() ? key : quote(key)).append(':');
                writeValue(out, entry.getValue(), indent + 1, false);
            }
            return;
        }
        out.append('\n');
        writeObject(out, value, indent + 1, false, comments);
    }

    private static List<Object> items(Object value) {
        List<Object> items = new ArrayList<>();
        if (value instanceof List) {
            items.addAll((List<?>) value);
        } else {
            for (int i = 0; i < Array.getLength(value); i++) {
                items.add(Array.get(value, i));
            }
        }
        return items;
    }

    public static String scalar(Object value) {
        if (value instanceof Boolean || value instanceof Number) {
            return value.toString();
        }
        if (value instanceof Enum) {
            return ((Enum<?>) value).name();
        }
        return quote(value.toString());
    }

    /** Double quotes, the only YAML style that understands these escapes */
    private static String quote(String text) {
        return "\"" + text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }

    private static Object get(Field field, Object instance) {
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String pad(int indent) {
        StringBuilder pad = new StringBuilder();
        for (int i = 0; i < indent * 2; i++) {
            pad.append(' ');
        }
        return pad.toString();
    }
}
