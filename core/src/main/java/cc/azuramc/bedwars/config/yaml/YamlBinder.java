package cc.azuramc.bedwars.config.yaml;

import cc.azuramc.bedwars.config.ConfigProblem;
import cc.azuramc.bedwars.api.config.Range;
import cc.azuramc.bedwars.api.config.Raw;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Binds a YAML node tree onto a config object. A value that cannot be used is reported and keeps its default
 *
 * @author an5w1r@163.com
 */
public final class YamlBinder {

    private static final Object INVALID = new Object();

    private final String file;
    private final boolean translateColors;
    @Getter
    private final List<ConfigProblem> problems = new ArrayList<>();

    public YamlBinder(String file, boolean translateColors) {
        this.file = file;
        this.translateColors = translateColors;
    }

    /**
     * @return null for an empty document or a syntax error, which is reported
     */
    @Nullable
    public Node parse(String text) {
        String source = text.startsWith("﻿") ? text.substring(1) : text;
        try {
            return new Yaml().compose(new StringReader(source));
        } catch (MarkedYAMLException e) {
            Mark mark = e.getProblemMark() != null ? e.getProblemMark() : e.getContextMark();
            int line = mark != null ? mark.getLine() + 1 : 0;
            int column = mark != null ? mark.getColumn() + 1 : 0;
            String[] lines = source.split("\n", -1);
            boolean tab = line > 0 && line <= lines.length && lines[line - 1].indexOf('\t') >= 0;
            String message = tab ? "Tabs cannot indent YAML, use spaces" : "Invalid YAML, " + e.getProblem();
            problems.add(new ConfigProblem(ConfigProblem.Severity.ERROR, file, line, column, "", message));
            return null;
        } catch (YAMLException e) {
            error(null, "", "Invalid YAML, " + e.getMessage());
            return null;
        }
    }

    public <T> T bind(@Nullable Node root, Class<T> type) {
        return bind(root, type, "");
    }

    /**
     * @param path key path of {@code root} in its file, empty for the whole file
     */
    public <T> T bind(@Nullable Node root, Class<T> type, String path) {
        return bindOnto(root, type.cast(ConfigKeys.newInstance(type)), path);
    }

    /**
     * Keys missing from {@code root} keep the values of {@code instance}
     */
    public <T> T bindOnto(@Nullable Node root, T instance, String path) {
        boolean top = path.isEmpty();
        if (root instanceof MappingNode) {
            if (instance instanceof Map) {
                bindMap((MappingNode) root, castMap(instance), ConfigKeys.typeArgument(instance.getClass(), 1), path, top);
            } else {
                bindObject((MappingNode) root, instance, path, top);
            }
        } else if (root != null && !Tag.NULL.equals(root.getTag())) {
            error(root, path, top ? "Expected keys and values at the top level" : "Expected a section of keys and values");
        }
        if (translateColors) {
            translateDefaults(instance);
        }
        return instance;
    }

    /**
     * Defaults of omitted keys still hold {@code &} codes, translating twice leaves a value unchanged
     */
    @SuppressWarnings("unchecked")
    private static Object translateDefaults(@Nullable Object value) {
        if (value instanceof String) {
            return ChatColor.translateAlternateColorCodes('&', (String) value);
        }
        if (value instanceof List) {
            ((List<Object>) value).replaceAll(YamlBinder::translateDefaults);
        } else if (value instanceof Map) {
            ((Map<Object, Object>) value).replaceAll((key, item) -> translateDefaults(item));
        } else if (value != null && !ConfigKeys.isScalar(value.getClass()) && !(value instanceof Node) && !value.getClass().isArray()) {
            for (Field field : ConfigKeys.fields(value.getClass()).values()) {
                if (field.isAnnotationPresent(Raw.class)) {
                    continue;
                }
                try {
                    Object current = field.get(value);
                    Object translated = translateDefaults(current);
                    if (!Objects.equals(translated, current)) {
                        field.set(value, translated);
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object map) {
        return (Map<String, Object>) map;
    }

    private void bindMap(MappingNode node, Map<String, Object> map, Type valueType, String path, boolean root) {
        for (NodeTuple tuple : node.getValue()) {
            String key = tuple.getKeyNode() instanceof ScalarNode ? ((ScalarNode) tuple.getKeyNode()).getValue() : null;
            if (key == null) {
                error(tuple.getKeyNode(), path, "A key must be plain text");
                continue;
            }
            if (root && ConfigKeys.VERSION_KEY.equals(key)) {
                continue;
            }
            String childPath = path.isEmpty() ? key : path + "." + key;
            if (map.containsKey(key)) {
                warn(tuple.getKeyNode(), childPath, "Duplicate key, the last one is used");
            }
            Object value = convert(tuple.getValueNode(), valueType, null, childPath, null);
            if (value != INVALID) {
                map.put(key, value);
            }
        }
    }

    private void bindObject(MappingNode node, Object target, String path, boolean root) {
        Map<String, Field> fields = ConfigKeys.fields(target.getClass());
        Set<String> seen = new HashSet<>();
        for (NodeTuple tuple : node.getValue()) {
            if (!(tuple.getKeyNode() instanceof ScalarNode)) {
                error(tuple.getKeyNode(), path, "A key must be plain text");
                continue;
            }
            String key = ((ScalarNode) tuple.getKeyNode()).getValue();
            String childPath = path.isEmpty() ? key : path + "." + key;
            if (root && ConfigKeys.VERSION_KEY.equals(key)) {
                continue;
            }
            if (!seen.add(key)) {
                warn(tuple.getKeyNode(), childPath, "Duplicate key, the last one is used");
            }
            Field field = fields.get(key);
            if (field == null) {
                String guess = ConfigKeys.closest(key, fields.keySet());
                warn(tuple.getKeyNode(), childPath, "Unknown key, ignored" + (guess != null ? ". Did you mean " + guess + "?" : ""));
                continue;
            }
            try {
                Object value = convert(tuple.getValueNode(), field.getGenericType(), field.get(target), childPath, field);
                if (value != INVALID && inRange(field, value, tuple.getValueNode(), childPath)) {
                    field.set(target, value);
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private Object convert(Node node, Type type, @Nullable Object current, String path, @Nullable Field field) {
        Class<?> raw = ConfigKeys.rawType(type);
        if (Node.class.isAssignableFrom(raw)) {
            return node;
        }
        if (ConfigKeys.isScalar(raw)) {
            if (!(node instanceof ScalarNode)) {
                error(node, path, "Expected a single value, not a list or section");
                return INVALID;
            }
            return convertScalar((ScalarNode) node, raw, path, field);
        }
        if (raw.isArray()) {
            Object list = convertList(node, raw.getComponentType(), path, field);
            if (list == INVALID) {
                return INVALID;
            }
            List<?> items = (List<?>) list;
            Object array = Array.newInstance(raw.getComponentType(), items.size());
            for (int i = 0; i < items.size(); i++) {
                Array.set(array, i, items.get(i));
            }
            return array;
        }
        if (List.class.isAssignableFrom(raw)) {
            return convertList(node, ConfigKeys.typeArgument(type, 0), path, field);
        }
        if (Map.class.isAssignableFrom(raw)) {
            if (!(node instanceof MappingNode)) {
                error(node, path, "Expected a section of keys and values");
                return INVALID;
            }
            Map<String, Object> map = raw.isInterface() ? new LinkedHashMap<String, Object>() : castMap(ConfigKeys.newInstance(raw));
            bindMap((MappingNode) node, map, ConfigKeys.typeArgument(type, 1), path, false);
            return map;
        }
        Method fromString = ConfigKeys.fromString(raw);
        if (fromString != null && node instanceof ScalarNode) {
            if (Tag.NULL.equals(node.getTag())) {
                error(node, path, "Missing value");
                return INVALID;
            }
            try {
                return fromString.invoke(null, ((ScalarNode) node).getValue().trim());
            } catch (InvocationTargetException e) {
                error(node, path, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                return INVALID;
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        if (!(node instanceof MappingNode)) {
            error(node, path, fromString != null ? "Expected a single value or a section of keys and values" : "Expected a section of keys and values");
            return INVALID;
        }
        Object instance = current != null ? current : ConfigKeys.newInstance(raw);
        bindObject((MappingNode) node, instance, path, false);
        return instance;
    }

    private Object convertList(Node node, Type elementType, String path, @Nullable Field field) {
        List<Object> list = new ArrayList<>();
        if (node instanceof ScalarNode && ConfigKeys.rawType(elementType) == String.class) {
            if (!Tag.NULL.equals(node.getTag())) {
                Object value = convert(node, elementType, null, path, field);
                if (value != INVALID) {
                    list.add(value);
                }
            }
            return list;
        }
        if (!(node instanceof SequenceNode)) {
            error(node, path, "Expected a list");
            return INVALID;
        }
        List<Node> items = ((SequenceNode) node).getValue();
        for (int i = 0; i < items.size(); i++) {
            Object value = convert(items.get(i), elementType, null, path + "[" + i + "]", field);
            if (value != INVALID) {
                list.add(value);
            }
        }
        return list;
    }

    private Object convertScalar(ScalarNode node, Class<?> type, String path, @Nullable Field field) {
        String value = node.getValue();
        if (Tag.NULL.equals(node.getTag())) {
            if (type == String.class) {
                return "";
            }
            error(node, path, "Missing value");
            return INVALID;
        }
        if (type == String.class) {
            boolean raw = field != null && field.isAnnotationPresent(Raw.class);
            return translateColors && !raw ? ChatColor.translateAlternateColorCodes('&', value) : value;
        }
        String trimmed = value.trim();
        if (type == boolean.class || type == Boolean.class) {
            switch (trimmed.toLowerCase(Locale.ROOT)) {
                case "true":
                case "yes":
                case "on":
                    return true;
                case "false":
                case "no":
                case "off":
                    return false;
                default:
                    error(node, path, "Expected true or false, got " + value);
                    return INVALID;
            }
        }
        if (type.isEnum()) {
            List<String> names = new ArrayList<>();
            for (Object constant : type.getEnumConstants()) {
                Enum<?> e = (Enum<?>) constant;
                if (e.name().equalsIgnoreCase(trimmed)) {
                    return e;
                }
                names.add(e.name());
            }
            String guess = ConfigKeys.closest(trimmed, names);
            error(node, path, "Unknown value " + value + ", expected one of " + String.join(", ", names)
                    + (guess != null ? ". Did you mean " + guess + "?" : ""));
            return INVALID;
        }
        if (type == char.class || type == Character.class) {
            if (value.isEmpty()) {
                error(node, path, "Expected a character");
                return INVALID;
            }
            return value.charAt(0);
        }
        try {
            if (type == int.class || type == Integer.class) {
                return Integer.parseInt(trimmed);
            }
            if (type == long.class || type == Long.class) {
                return Long.parseLong(trimmed);
            }
            if (type == short.class || type == Short.class) {
                return Short.parseShort(trimmed);
            }
            if (type == byte.class || type == Byte.class) {
                return Byte.parseByte(trimmed);
            }
            if (type == double.class || type == Double.class) {
                return Double.parseDouble(trimmed);
            }
            if (type == float.class || type == Float.class) {
                return Float.parseFloat(trimmed);
            }
        } catch (NumberFormatException e) {
            boolean whole = type != double.class && type != Double.class && type != float.class && type != Float.class;
            error(node, path, (whole ? "Expected a whole number, got " : "Expected a number, got ") + value);
            return INVALID;
        }
        error(node, path, "Unsupported config type " + type.getSimpleName());
        return INVALID;
    }

    private boolean inRange(Field field, Object value, Node node, String path) {
        Range range = field.getAnnotation(Range.class);
        if (range == null || !(value instanceof Number)) {
            return true;
        }
        double number = ((Number) value).doubleValue();
        if (number >= range.min() && number <= range.max()) {
            return true;
        }
        error(node, path, value + " is out of range " + format(range.min()) + " to " + format(range.max()) + ", the default is used");
        return false;
    }

    private static String format(double bound) {
        if (Math.abs(bound) == Double.MAX_VALUE) {
            return "any";
        }
        return bound == Math.rint(bound) ? String.valueOf((long) bound) : String.valueOf(bound);
    }

    private void error(@Nullable Node node, String path, String message) {
        add(ConfigProblem.Severity.ERROR, node, path, message);
    }

    private void warn(Node node, String path, String message) {
        add(ConfigProblem.Severity.WARNING, node, path, message);
    }

    private void add(ConfigProblem.Severity severity, @Nullable Node node, String path, String message) {
        Mark mark = node != null ? node.getStartMark() : null;
        problems.add(new ConfigProblem(severity, file,
                mark != null ? mark.getLine() + 1 : 0, mark != null ? mark.getColumn() + 1 : 0, path, message));
    }

    /**
     * The node of a key path, such as {@code chat.global-chat-cooldown} or {@code tiers[2].price}
     */
    @Nullable
    public static Node find(@Nullable Node root, String path) {
        Node node = root;
        for (String segment : path.split("\\.")) {
            int bracket = segment.indexOf('[');
            String key = bracket < 0 ? segment : segment.substring(0, bracket);
            if (!key.isEmpty()) {
                node = child(node, key);
            }
            while (bracket >= 0 && node != null) {
                int close = segment.indexOf(']', bracket);
                if (close < 0 || !(node instanceof SequenceNode)) {
                    return null;
                }
                int index;
                try {
                    index = Integer.parseInt(segment.substring(bracket + 1, close));
                } catch (NumberFormatException e) {
                    return null;
                }
                List<Node> items = ((SequenceNode) node).getValue();
                node = index >= 0 && index < items.size() ? items.get(index) : null;
                bracket = segment.indexOf('[', close);
            }
        }
        return node;
    }

    @Nullable
    private static Node child(@Nullable Node node, String key) {
        if (!(node instanceof MappingNode)) {
            return null;
        }
        Node found = null;
        for (NodeTuple tuple : ((MappingNode) node).getValue()) {
            if (tuple.getKeyNode() instanceof ScalarNode && key.equals(((ScalarNode) tuple.getKeyNode()).getValue())) {
                found = tuple.getValueNode();
            }
        }
        return found;
    }
}
