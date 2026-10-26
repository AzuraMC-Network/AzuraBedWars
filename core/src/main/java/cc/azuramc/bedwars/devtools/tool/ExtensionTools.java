package cc.azuramc.bedwars.devtools.tool;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.api.extension.NoSettings;
import cc.azuramc.bedwars.config.yaml.ConfigKeys;
import cc.azuramc.bedwars.config.yaml.SchemaExporter;
import cc.azuramc.bedwars.devtools.Devtools;
import cc.azuramc.bedwars.devtools.McpTool;
import cc.azuramc.bedwars.extension.ExtensionKind;
import cc.azuramc.bedwars.extension.Extensions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author an5w1r@163.com
 */
public final class ExtensionTools {

    private final AzuraBedWars plugin;
    private final Devtools devtools;

    public ExtensionTools(AzuraBedWars plugin, Devtools devtools) {
        this.plugin = plugin;
        this.devtools = devtools;
    }

    public List<McpTool> tools() {
        return Collections.singletonList(
                McpTool.read("extension_list", "Items, purchase actions and conditions, upgrade and trap effects, timeline actions and"
                                + " placeholders that other plugins registered, with where each is written in the config and the schema"
                                + " of its settings. Also lists the MCP tools other plugins added")
                        .handle(args -> devtools.sync(this::list))
        );
    }

    private JsonElement list() {
        Extensions extensions = plugin.getExtensions();
        JsonObject result = new JsonObject();
        result.addProperty("ready", extensions.isReady());
        if (!extensions.isReady()) {
            result.addProperty("note", "Plugins may still register until the first server tick");
        }
        JsonArray kinds = new JsonArray();
        for (ExtensionKind kind : ExtensionKind.values()) {
            JsonObject json = new JsonObject();
            json.addProperty("kind", kind.getLabel());
            json.addProperty("writtenAs", writtenAs(kind));
            Map<String, Class<?>> types = extensions.settingsTypes(kind);
            Map<String, String> owners = extensions.owners(kind);
            JsonArray registered = new JsonArray();
            for (Map.Entry<String, Class<?>> entry : types.entrySet()) {
                JsonObject extension = new JsonObject();
                extension.addProperty("ref", entry.getKey());
                extension.addProperty("plugin", owners.get(entry.getKey()));
                Class<?> type = entry.getValue();
                if (type != Void.class && type != NoSettings.class) {
                    JsonObject settings = SchemaExporter.export(type, entry.getKey(), key -> extensions.settingsTypes(ExtensionKind.of(key)));
                    settings.getAsJsonObject("properties").remove(ConfigKeys.VERSION_KEY);
                    extension.add("settings", settings);
                }
                registered.add(extension);
            }
            json.add("registered", registered);
            kinds.add(json);
        }
        result.add("extensions", kinds);
        JsonArray tools = new JsonArray();
        for (Extensions.Tool tool : extensions.getDevTools()) {
            JsonObject json = new JsonObject();
            json.addProperty("name", tool.getName());
            json.addProperty("plugin", tool.getPlugin().getName());
            json.addProperty("readOnly", tool.isReadOnly());
            if (devtools.isBuiltIn(tool.getName())) {
                json.addProperty("hidden", "A built-in tool has the same name");
            }
            tools.add(json);
        }
        result.add("tools", tools);
        return result;
    }

    private static String writtenAs(ExtensionKind kind) {
        switch (kind) {
            case ITEM:
                return "custom: <ref> on a shop item in shop/categories, with settings next to it";
            case PURCHASE_ACTION:
                return "action: <ref> on a shop item, runs after the price is paid";
            case CONDITION:
                return "type: <ref> in requires.conditions of a shop item";
            case UPGRADE_EFFECT:
                return "type: <ref> as an effect of an upgrade level in upgrades.yml";
            case TRAP_EFFECT:
                return "type: <ref> as an effect or alert of a trap in upgrades.yml";
            case TIMELINE_ACTION:
                return "type: <ref> as an action of a stage in timeline.yml";
            default:
                return "{<ref>} in scoreboard, tab list, title and action bar text";
        }
    }
}
