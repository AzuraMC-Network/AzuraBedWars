package cc.azuramc.bedwars.devtools;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;

/**
 * One MCP tool, its handler runs on an HTTP worker thread and hops to the main thread through {@link Devtools#sync}
 *
 * @author an5w1r@163.com
 */
@Getter
public final class McpTool {

    public interface Handler {
        JsonElement call(Args args) throws Exception;
    }

    private final String name;
    private final String description;
    private final JsonObject inputSchema;
    /** Writes are refused while non-admin players are online, see {@code allow-with-players} */
    private final boolean readOnly;
    private final boolean destructive;
    private final Handler handler;

    private McpTool(String name, String description, JsonObject inputSchema, boolean readOnly, boolean destructive, Handler handler) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
        this.readOnly = readOnly;
        this.destructive = destructive;
        this.handler = handler;
    }

    public static Builder read(String name, String description) {
        return new Builder(name, description, true, false);
    }

    public static Builder write(String name, String description) {
        return new Builder(name, description, false, false);
    }

    public static Builder destructive(String name, String description) {
        return new Builder(name, description, false, true);
    }

    public static McpTool external(String name, String description, JsonObject inputSchema, boolean readOnly, Handler handler) {
        return new McpTool(name, description, inputSchema, readOnly, false, handler);
    }

    public JsonObject describe() {
        JsonObject tool = new JsonObject();
        tool.addProperty("name", name);
        tool.addProperty("description", description);
        tool.add("inputSchema", inputSchema);
        JsonObject annotations = new JsonObject();
        annotations.addProperty("readOnlyHint", readOnly);
        annotations.addProperty("destructiveHint", destructive);
        annotations.addProperty("openWorldHint", false);
        tool.add("annotations", annotations);
        return tool;
    }

    public static final class Builder {
        private final String name;
        private final String description;
        private final boolean readOnly;
        private final boolean destructive;
        private final JsonObject properties = new JsonObject();
        private final JsonArray required = new JsonArray();

        private Builder(String name, String description, boolean readOnly, boolean destructive) {
            this.name = name;
            this.description = description;
            this.readOnly = readOnly;
            this.destructive = destructive;
        }

        public Builder string(String key, String description, boolean required) {
            return param(key, type("string", description), required);
        }

        public Builder integer(String key, String description, boolean required) {
            return param(key, type("integer", description), required);
        }

        public Builder bool(String key, String description, boolean required) {
            return param(key, type("boolean", description), required);
        }

        public Builder choice(String key, String description, boolean required, String... values) {
            JsonObject schema = type("string", description);
            JsonArray options = new JsonArray();
            for (String value : values) {
                options.add(new JsonPrimitive(value));
            }
            schema.add("enum", options);
            return param(key, schema, required);
        }

        public Builder param(String key, JsonObject schema, boolean required) {
            properties.add(key, schema);
            if (required) {
                this.required.add(new JsonPrimitive(key));
            }
            return this;
        }

        public McpTool handle(Handler handler) {
            JsonObject schema = new JsonObject();
            schema.addProperty("type", "object");
            schema.add("properties", properties);
            if (required.size() > 0) {
                schema.add("required", required);
            }
            return new McpTool(name, description, schema, readOnly, destructive, handler);
        }

        public static JsonObject type(String type, String description) {
            JsonObject schema = new JsonObject();
            schema.addProperty("type", type);
            schema.addProperty("description", description);
            return schema;
        }
    }
}
