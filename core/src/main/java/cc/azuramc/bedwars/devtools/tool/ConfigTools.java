package cc.azuramc.bedwars.devtools.tool;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.ApplyResult;
import cc.azuramc.bedwars.config.ConfigDocument;
import cc.azuramc.bedwars.config.ConfigProblem;
import cc.azuramc.bedwars.config.ConfigService;
import cc.azuramc.bedwars.devtools.Args;
import cc.azuramc.bedwars.devtools.Devtools;
import cc.azuramc.bedwars.devtools.McpTool;
import cc.azuramc.bedwars.devtools.ToolError;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Config documents through {@link ConfigService}, plus the history of config files and maps
 *
 * @author an5w1r@163.com
 */
public final class ConfigTools {

    /** Values of keys like password are never shown to the AI client */
    private static final Pattern SECRET_LINE = Pattern.compile("(?m)^([ \\t]*[\\w-]*(?:password|secret|token)[\\w-]*[ \\t]*:[ \\t]*)(?=\\S)(.*)$");
    static final String HIDDEN = "\"<hidden>\"";
    static final String MAP_HISTORY_PREFIX = "map-";
    private static final Pattern STAMP = Pattern.compile("^(.*\\.)(\\d{8}-\\d{6}-\\d{3})$");

    private final AzuraBedWars plugin;
    private final Devtools devtools;
    private final ConfigService service;

    public ConfigTools(AzuraBedWars plugin, Devtools devtools) {
        this.plugin = plugin;
        this.devtools = devtools;
        this.service = plugin.getConfigService();
    }

    public List<McpTool> tools() {
        return Arrays.asList(
                McpTool.read("config_list", "Every config document: id, file, when changes take effect and the file hash."
                                + " Read azura://guide/concepts once before editing")
                        .handle(args -> devtools.sync(this::list)),
                McpTool.read("config_schema", "JSON Schema of a document or one key in it: types, defaults, descriptions, ranges and allowed values")
                        .string("id", "Document id from config_list", true)
                        .string("path", "Dotted key path to narrow the schema, such as chat.global-chat-cooldown", false)
                        .handle(args -> devtools.sync(() -> schema(args))),
                McpTool.read("config_read", "Current text of a document with its hash, keep the hash for config_set or config_write")
                        .string("id", "Document id", true)
                        .handle(args -> devtools.sync(() -> read(args))),
                McpTool.read("config_validate", "Check a document without changing anything, either the file on disk or a proposed text")
                        .string("id", "Document id", true)
                        .string("text", "Proposed full file text, leave out to check the file on disk", false)
                        .handle(args -> devtools.sync(() -> validate(args))),
                McpTool.write("config_set", "Change scalar values in place and apply them, keeping comments and layout."
                                + " All values change together or none do. Lists and new nested keys need config_write")
                        .string("id", "Document id", true)
                        .param("values", valuesSchema(), true)
                        .string("expected_hash", "Hash from config_read, the change is refused when the file changed since", false)
                        .string("reason", "Why, kept in the audit log", false)
                        .handle(args -> devtools.sync(() -> set(args))),
                McpTool.write("config_write", "Replace a whole document after validating it, the old file goes to history")
                        .string("id", "Document id", true)
                        .string("text", "Full new file text", true)
                        .string("expected_hash", "Hash from config_read, required so edits made meanwhile are not lost", true)
                        .string("reason", "Why, kept in the audit log", false)
                        .handle(args -> devtools.sync(() -> write(args))),
                McpTool.write("config_reload", "Reread documents from disk after someone edited the files by hand")
                        .string("id", "Document id, leave out for all", false)
                        .handle(args -> devtools.sync(() -> reload(args))),
                McpTool.read("history_list", "Backups taken before each config or map change, newest first")
                        .string("target", "Document id or map:<name>, leave out for all", false)
                        .handle(args -> devtools.sync(() -> historyList(args))),
                McpTool.read("history_read", "Text of one backup")
                        .string("entry", "Entry name from history_list", true)
                        .handle(args -> devtools.sync(() -> historyRead(args))),
                McpTool.destructive("history_rollback", "Restore a config file or map from a backup, the current state is backed up first")
                        .string("entry", "Entry name from history_list", true)
                        .string("reason", "Why, kept in the audit log", false)
                        .handle(args -> devtools.sync(() -> rollback(args)))
        );
    }

    private static JsonObject valuesSchema() {
        JsonObject schema = McpTool.Builder.type("object", "Dotted key path to new value, such as {\"debug-mode\": true, \"chat.global-chat-cooldown\": 5}");
        schema.add("additionalProperties", new JsonObject());
        return schema;
    }

    private JsonElement list() throws IOException {
        JsonArray documents = new JsonArray();
        for (ConfigDocument<?> document : visibleDocuments()) {
            JsonObject json = new JsonObject();
            json.addProperty("id", document.getId());
            json.addProperty("file", document.getPath());
            json.addProperty("applies", describe(document));
            json.addProperty("type", document.getType().getSimpleName());
            json.addProperty("hash", service.hash(document.getId()));
            documents.add(json);
        }
        return documents;
    }

    private static String describe(ConfigDocument<?> document) {
        switch (document.getApplyMode()) {
            case LIVE:
                return "LIVE: right away, except keys marked as needing a restart";
            case NEXT_GAME:
                return "NEXT_GAME: when the next game starts, a running game keeps the old values";
            default:
                return "RESTART: only after a server restart";
        }
    }

    private JsonElement schema(Args args) {
        JsonObject schema = service.schema(document(args.string("id")).getId());
        String path = args.optString("path");
        if (path == null || path.isEmpty()) {
            return schema;
        }
        JsonObject node = schema;
        for (String key : path.split("\\.")) {
            while (node.has("items") && !node.has("properties")) {
                node = node.getAsJsonObject("items");
            }
            JsonObject properties = node.getAsJsonObject("properties");
            if (properties == null || !properties.has(key)) {
                throw new ToolError("No key " + key + " in " + path + (properties == null ? "" : ", keys here: " + keys(properties)));
            }
            node = properties.getAsJsonObject(key);
        }
        return node;
    }

    private static String keys(JsonObject object) {
        List<String> keys = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            keys.add(entry.getKey());
        }
        return String.join(", ", keys);
    }

    private JsonElement read(Args args) throws IOException {
        ConfigDocument<?> document = document(args.string("id"));
        JsonObject json = new JsonObject();
        json.addProperty("id", document.getId());
        json.addProperty("file", document.getPath());
        json.addProperty("applies", describe(document));
        json.addProperty("hash", service.hash(document.getId()));
        json.addProperty("text", hideSecrets(ConfigService.read(service.file(document))));
        return json;
    }

    public static String hideSecrets(String text) {
        return SECRET_LINE.matcher(text).replaceAll("$1" + Matcher.quoteReplacement(HIDDEN));
    }

    private JsonElement validate(Args args) throws IOException {
        ConfigDocument<?> document = document(args.string("id"));
        String text = args.has("text") ? args.string("text") : ConfigService.read(service.file(document));
        List<ConfigProblem> problems = service.validate(document.getId(), text);
        JsonObject json = new JsonObject();
        json.addProperty("valid", !hasError(problems));
        json.add("problems", problems(problems));
        return json;
    }

    private JsonElement set(Args args) {
        ConfigDocument<?> document = document(args.string("id"));
        Map<String, Object> values = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : args.object("values").entrySet()) {
            values.put(entry.getKey(), scalar(entry.getKey(), entry.getValue()));
        }
        if (values.isEmpty()) {
            throw new ToolError("values is empty");
        }
        return result(service.setValues(document.getId(), values, args.optString("expected_hash")));
    }

    static Object scalar(String key, JsonElement value) {
        if (!value.isJsonPrimitive()) {
            throw new ToolError(key + " is not a scalar, change lists and sections with config_write");
        }
        JsonPrimitive primitive = value.getAsJsonPrimitive();
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        }
        if (primitive.isNumber()) {
            BigDecimal number = primitive.getAsBigDecimal();
            if (number.signum() == 0 || number.stripTrailingZeros().scale() <= 0) {
                return number.longValueExact();
            }
            return number.doubleValue();
        }
        return primitive.getAsString();
    }

    private JsonElement write(Args args) {
        ConfigDocument<?> document = document(args.string("id"));
        String text = args.string("text");
        if (text.contains(HIDDEN)) {
            throw new ToolError("The text still contains " + HIDDEN + " placeholders from config_read."
                    + " Change other keys with config_set, which leaves hidden values alone");
        }
        return result(service.write(document.getId(), text, args.string("expected_hash")));
    }

    private JsonElement reload(Args args) {
        JsonArray results = new JsonArray();
        if (args.has("id")) {
            results.add(result(service.reload(document(args.string("id")).getId())));
        } else {
            for (ConfigDocument<?> document : visibleDocuments()) {
                results.add(result(service.reload(document.getId())));
            }
        }
        return results;
    }

    private JsonElement historyList(Args args) {
        String target = args.optString("target");
        File[] files = historyDirectory().listFiles();
        List<File> entries = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                String owner = owner(file.getName());
                if (owner != null && (target == null || target.equals(owner))) {
                    entries.add(file);
                }
            }
        }
        entries.sort((a, b) -> stamp(b.getName()).compareTo(stamp(a.getName())));
        JsonArray list = new JsonArray();
        for (File file : entries) {
            JsonObject json = new JsonObject();
            json.addProperty("entry", file.getName());
            json.addProperty("target", owner(file.getName()));
            json.addProperty("time", stamp(file.getName()));
            json.addProperty("bytes", file.length());
            list.add(json);
        }
        return list;
    }

    private JsonElement historyRead(Args args) throws IOException {
        File file = historyFile(args.string("entry"));
        String text = ConfigService.read(file);
        return new JsonPrimitive(owner(file.getName()).startsWith("map:") ? text : hideSecrets(text));
    }

    private JsonElement rollback(Args args) throws IOException {
        File file = historyFile(args.string("entry"));
        String owner = owner(file.getName());
        String text = ConfigService.read(file);
        if (owner.startsWith("map:")) {
            return new JsonPrimitive(MapTools.restore(plugin, owner.substring(4), text));
        }
        return result(service.write(owner, text, null));
    }

    private File historyDirectory() {
        return new File(plugin.getPlugin().getDataFolder(), ConfigService.HISTORY_DIR);
    }

    private File historyFile(String entry) {
        File file = new File(historyDirectory(), entry);
        if (entry.contains("/") || entry.contains("\\") || !file.isFile() || owner(entry) == null) {
            throw new ToolError("No history entry " + entry + ", use history_list");
        }
        return file;
    }

    /**
     * @return the document id or {@code map:<name>} a backup belongs to, null for unknown files
     */
    @Nullable
    private String owner(String entry) {
        Matcher matcher = STAMP.matcher(entry);
        if (!matcher.matches()) {
            return null;
        }
        String prefix = matcher.group(1);
        if (prefix.startsWith(MAP_HISTORY_PREFIX) && prefix.endsWith(".json.")) {
            return "map:" + prefix.substring(MAP_HISTORY_PREFIX.length(), prefix.length() - ".json.".length());
        }
        for (ConfigDocument<?> document : visibleDocuments()) {
            if (ConfigService.historyPrefix(document).equals(prefix)) {
                return document.getId();
            }
        }
        return null;
    }

    private static String stamp(String entry) {
        Matcher matcher = STAMP.matcher(entry);
        return matcher.matches() ? matcher.group(2) : "";
    }

    private List<ConfigDocument<?>> visibleDocuments() {
        List<ConfigDocument<?>> documents = new ArrayList<>();
        for (ConfigDocument<?> document : service.documents()) {
            if (!Devtools.DOCUMENT_ID.equals(document.getId())) {
                documents.add(document);
            }
        }
        return documents;
    }

    private ConfigDocument<?> document(String id) {
        for (ConfigDocument<?> document : visibleDocuments()) {
            if (document.getId().equals(id)) {
                return document;
            }
        }
        List<String> ids = new ArrayList<>();
        for (ConfigDocument<?> document : visibleDocuments()) {
            ids.add(document.getId());
        }
        throw new ToolError("No config document " + id + ", available: " + String.join(", ", ids));
    }

    static JsonObject result(ApplyResult result) {
        JsonObject json = new JsonObject();
        json.addProperty("document", result.getDocumentId());
        json.addProperty("status", result.getStatus().name());
        switch (result.getStatus()) {
            case APPLIED:
                json.addProperty("meaning", "Saved and in effect");
                break;
            case NEXT_GAME:
                json.addProperty("meaning", "Saved, the running game keeps the old values until the next game");
                break;
            case RESTART_REQUIRED:
                json.addProperty("meaning", "Saved, restartPaths only take effect after a server restart");
                break;
            default:
                json.addProperty("meaning", "Nothing was changed, fix the problems and try again");
        }
        JsonArray restartPaths = new JsonArray();
        for (String path : result.getRestartPaths()) {
            restartPaths.add(new JsonPrimitive(path));
        }
        json.add("restartPaths", restartPaths);
        json.add("problems", problems(result.getProblems()));
        return json;
    }

    private static JsonArray problems(List<ConfigProblem> problems) {
        JsonArray array = new JsonArray();
        for (ConfigProblem problem : problems) {
            JsonObject json = new JsonObject();
            json.addProperty("severity", problem.getSeverity().name());
            json.addProperty("file", problem.getFile());
            json.addProperty("line", problem.getLine());
            json.addProperty("column", problem.getColumn());
            json.addProperty("path", problem.getPath());
            json.addProperty("message", problem.getMessage());
            array.add(json);
        }
        return array;
    }

    private static boolean hasError(List<ConfigProblem> problems) {
        for (ConfigProblem problem : problems) {
            if (problem.isError()) {
                return true;
            }
        }
        return false;
    }
}
