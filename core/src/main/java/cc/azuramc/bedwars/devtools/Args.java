package cc.azuramc.bedwars.devtools;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.Nullable;

/**
 * Arguments of a tool call, every getter reports a wrong or missing argument as a {@link ToolError}
 *
 * @author an5w1r@163.com
 */
public final class Args {

    private final JsonObject json;

    public Args(JsonObject json) {
        this.json = json;
    }

    public JsonObject json() {
        return json;
    }

    public boolean has(String key) {
        return json.has(key) && !json.get(key).isJsonNull();
    }

    public String string(String key) {
        String value = optString(key);
        if (value == null) {
            throw new ToolError("Missing argument " + key);
        }
        return value;
    }

    @Nullable
    public String optString(String key) {
        if (!has(key)) {
            return null;
        }
        JsonElement value = json.get(key);
        if (!value.isJsonPrimitive()) {
            throw new ToolError("Argument " + key + " must be a string");
        }
        return value.getAsString();
    }

    public int integer(String key, int defaultValue) {
        if (!has(key)) {
            return defaultValue;
        }
        JsonElement value = json.get(key);
        if (!value.isJsonPrimitive() || !((JsonPrimitive) value).isNumber()) {
            throw new ToolError("Argument " + key + " must be a number");
        }
        return value.getAsInt();
    }

    public int integer(String key) {
        if (!has(key)) {
            throw new ToolError("Missing argument " + key);
        }
        return integer(key, 0);
    }

    public boolean bool(String key, boolean defaultValue) {
        if (!has(key)) {
            return defaultValue;
        }
        JsonElement value = json.get(key);
        if (!value.isJsonPrimitive() || !((JsonPrimitive) value).isBoolean()) {
            throw new ToolError("Argument " + key + " must be true or false");
        }
        return value.getAsBoolean();
    }

    public JsonObject object(String key) {
        if (!has(key) || !json.get(key).isJsonObject()) {
            throw new ToolError("Argument " + key + " must be an object");
        }
        return json.getAsJsonObject(key);
    }

    public <E extends Enum<E>> E choice(String key, Class<E> type) {
        String value = string(key);
        for (E constant : type.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(value)) {
                return constant;
            }
        }
        StringBuilder valid = new StringBuilder();
        for (E constant : type.getEnumConstants()) {
            valid.append(valid.length() == 0 ? "" : ", ").append(constant.name());
        }
        throw new ToolError("Argument " + key + " must be one of " + valid + ", got " + value);
    }
}
