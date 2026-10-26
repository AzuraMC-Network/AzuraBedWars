package cc.azuramc.bedwars.config.yaml;

import cc.azuramc.bedwars.api.config.Comment;
import cc.azuramc.bedwars.api.config.Range;
import cc.azuramc.bedwars.api.config.RestartRequired;
import cc.azuramc.bedwars.config.ExtensionSettings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.nodes.Node;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * JSON Schema (draft-07) of a config object, for editors and the development MCP
 *
 * @author an5w1r@163.com
 */
public final class SchemaExporter {

    private SchemaExporter() {
    }

    /**
     * @param extensions settings types of registered extensions by reference, for each extension kind
     */
    public static JsonObject export(Class<?> type, String title, Function<String, Map<String, Class<?>>> extensions) {
        JsonObject schema = Map.class.isAssignableFrom(type) ? forType(type, null, extensions) : object(type, ConfigKeys.newInstance(type), extensions);
        if (!schema.has("properties")) {
            schema.add("properties", new JsonObject());
        }
        schema.addProperty("$schema", "http://json-schema.org/draft-07/schema#");
        schema.addProperty("title", title);
        JsonObject version = new JsonObject();
        version.addProperty("type", "integer");
        version.addProperty("description", "Layout version, managed by the plugin");
        schema.getAsJsonObject("properties").add(ConfigKeys.VERSION_KEY, version);
        return schema;
    }

    private static JsonObject object(Class<?> type, @Nullable Object defaults, Function<String, Map<String, Class<?>>> extensions) {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        describe(schema, type.getAnnotation(Comment.class));
        JsonObject properties = new JsonObject();
        JsonArray extensionSchemas = new JsonArray();
        for (Map.Entry<String, Field> entry : ConfigKeys.fields(type).entrySet()) {
            Field field = entry.getValue();
            Object value = defaults != null ? get(field, defaults) : null;
            JsonObject property = forType(field.getGenericType(), value, extensions);
            ExtensionSettings settings = field.getAnnotation(ExtensionSettings.class);
            if (settings != null) {
                for (String reference : settings.value()) {
                    String key = reference.substring(0, reference.indexOf('='));
                    for (Map.Entry<String, Class<?>> extension : extensions.apply(reference.substring(key.length() + 1)).entrySet()) {
                        extensionSchemas.add(ifThen(key, extension.getKey(), entry.getKey(),
                                object(extension.getValue(), ConfigKeys.newInstance(extension.getValue()), extensions)));
                    }
                }
            }
            describe(property, field.getAnnotation(Comment.class));
            Range range = field.getAnnotation(Range.class);
            if (range != null) {
                if (range.min() != -Double.MAX_VALUE) {
                    property.addProperty("minimum", range.min());
                }
                if (range.max() != Double.MAX_VALUE) {
                    property.addProperty("maximum", range.max());
                }
            }
            if (field.isAnnotationPresent(RestartRequired.class)) {
                property.addProperty("x-restart-required", true);
            }
            properties.add(entry.getKey(), property);
        }
        schema.add("properties", properties);
        schema.addProperty("additionalProperties", false);
        if (extensionSchemas.size() > 0) {
            schema.add("allOf", extensionSchemas);
        }
        return schema;
    }

    private static JsonObject ifThen(String key, String value, String property, JsonObject schema) {
        JsonObject constant = new JsonObject();
        constant.addProperty("const", value);
        JsonObject ifProperties = new JsonObject();
        ifProperties.add(key, constant);
        JsonArray required = new JsonArray();
        required.add(new JsonPrimitive(key));
        JsonObject condition = new JsonObject();
        condition.add("properties", ifProperties);
        condition.add("required", required);

        JsonObject thenProperties = new JsonObject();
        thenProperties.add(property, schema);
        JsonObject then = new JsonObject();
        then.add("properties", thenProperties);

        JsonObject ifThen = new JsonObject();
        ifThen.add("if", condition);
        ifThen.add("then", then);
        return ifThen;
    }

    private static JsonObject forType(Type type, @Nullable Object value, Function<String, Map<String, Class<?>>> extensions) {
        Class<?> raw = ConfigKeys.rawType(type);
        JsonObject schema = new JsonObject();
        if (Node.class.isAssignableFrom(raw)) {
            return schema;
        }
        if (ConfigKeys.isScalar(raw)) {
            schema.addProperty("type", scalarType(raw));
            if (raw.isEnum()) {
                JsonArray names = new JsonArray();
                for (Object constant : raw.getEnumConstants()) {
                    names.add(new JsonPrimitive(((Enum<?>) constant).name()));
                }
                schema.add("enum", names);
            }
            if (value != null) {
                schema.add("default", primitive(value));
            }
            return schema;
        }
        if (raw.isArray() || List.class.isAssignableFrom(raw)) {
            Type element = raw.isArray() ? raw.getComponentType() : ConfigKeys.typeArgument(type, 0);
            schema.addProperty("type", "array");
            schema.add("items", forType(element, null, extensions));
            if (value != null && ConfigKeys.isScalar(ConfigKeys.rawType(element))) {
                JsonArray defaults = new JsonArray();
                int size = raw.isArray() ? Array.getLength(value) : ((List<?>) value).size();
                for (int i = 0; i < size; i++) {
                    defaults.add(primitive(raw.isArray() ? Array.get(value, i) : ((List<?>) value).get(i)));
                }
                schema.add("default", defaults);
            }
            return schema;
        }
        if (Map.class.isAssignableFrom(raw)) {
            schema.addProperty("type", "object");
            schema.add("additionalProperties", forType(ConfigKeys.typeArgument(type, 1), null, extensions));
            return schema;
        }
        JsonObject object = object(raw, value != null ? value : ConfigKeys.newInstance(raw), extensions);
        if (ConfigKeys.fromString(raw) == null) {
            return object;
        }
        JsonObject shorthand = new JsonObject();
        shorthand.addProperty("type", "string");
        JsonArray anyOf = new JsonArray();
        anyOf.add(shorthand);
        anyOf.add(object);
        schema.add("anyOf", anyOf);
        return schema;
    }

    private static String scalarType(Class<?> type) {
        if (type == boolean.class || type == Boolean.class) {
            return "boolean";
        }
        if (type == int.class || type == Integer.class || type == long.class || type == Long.class
                || type == short.class || type == Short.class || type == byte.class || type == Byte.class) {
            return "integer";
        }
        if (type == double.class || type == Double.class || type == float.class || type == Float.class) {
            return "number";
        }
        return "string";
    }

    private static JsonPrimitive primitive(Object value) {
        if (value instanceof Boolean) {
            return new JsonPrimitive((Boolean) value);
        }
        if (value instanceof Number) {
            return new JsonPrimitive((Number) value);
        }
        if (value instanceof Enum) {
            return new JsonPrimitive(((Enum<?>) value).name());
        }
        return new JsonPrimitive(value.toString());
    }

    private static void describe(JsonObject schema, @Nullable Comment comment) {
        if (comment != null) {
            schema.addProperty("description", String.join("\n", comment.value()));
        }
    }

    private static Object get(Field field, Object instance) {
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
