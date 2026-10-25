package cc.azuramc.bedwars.config.yaml;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author an5w1r@163.com
 */
public final class ConfigKeys {

    /** Reserved root key, not part of any config object */
    public static final String VERSION_KEY = "config-version";

    private static final Map<Class<?>, Map<String, Field>> FIELDS = new ConcurrentHashMap<>();

    private ConfigKeys() {
    }

    /**
     * {@code autoTPSlot} becomes {@code auto-tp-slot}, {@code level2RefreshSecond} becomes {@code level2-refresh-second}
     */
    public static String toKebab(String camel) {
        StringBuilder out = new StringBuilder(camel.length() + 8);
        for (int i = 0; i < camel.length(); i++) {
            char c = camel.charAt(i);
            if (Character.isUpperCase(c)) {
                boolean afterLower = i > 0 && !Character.isUpperCase(camel.charAt(i - 1));
                boolean acronymEnd = i > 0 && i + 1 < camel.length()
                        && Character.isUpperCase(camel.charAt(i - 1)) && Character.isLowerCase(camel.charAt(i + 1));
                if (afterLower || acronymEnd) {
                    out.append('-');
                }
                out.append(Character.toLowerCase(c));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    /**
     * Config keys of a config object in declaration order
     */
    public static Map<String, Field> fields(Class<?> type) {
        return FIELDS.computeIfAbsent(type, t -> {
            Map<String, Field> fields = new LinkedHashMap<>();
            for (Field field : t.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) || field.isSynthetic()) {
                    continue;
                }
                field.setAccessible(true);
                fields.put(toKebab(field.getName()), field);
            }
            return fields;
        });
    }

    public static boolean isScalar(Class<?> type) {
        return type.isPrimitive() || type == String.class || type.isEnum()
                || Number.class.isAssignableFrom(type) || type == Boolean.class || type == Character.class;
    }

    public static Class<?> rawType(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
        return Object.class;
    }

    /**
     * Type argument {@code index} of a generic field type, {@code String} when the field is not parameterized
     */
    public static Type typeArgument(Type type, int index) {
        if (type instanceof ParameterizedType) {
            Type[] arguments = ((ParameterizedType) type).getActualTypeArguments();
            if (index < arguments.length) {
                return arguments[index];
            }
        }
        return String.class;
    }

    public static Object newInstance(Class<?> type) {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Config type " + type.getName() + " needs a public no-arg constructor", e);
        }
    }

    @Nullable
    public static String closest(String input, Collection<String> candidates) {
        String lower = input.toLowerCase();
        String best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (String candidate : candidates) {
            int distance = distance(lower, candidate.toLowerCase());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best != null && bestDistance <= Math.max(2, input.length() / 3) ? best : null;
    }

    private static int distance(String a, String b) {
        int[] previous = new int[b.length() + 1];
        int[] current = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            previous[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[b.length()];
    }
}
