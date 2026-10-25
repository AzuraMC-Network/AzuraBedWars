package cc.azuramc.bedwars.devtools;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.ConfigDocument;
import cc.azuramc.bedwars.config.ConfigService;
import cc.azuramc.bedwars.devtools.tool.ConfigTools;
import cc.azuramc.bedwars.game.map.MapData;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Guides, file resources and prompt templates that teach the AI client the plugin's rules
 *
 * @author an5w1r@163.com
 */
public final class McpContent {

    private static final String GUIDE = "azura://guide/";
    private static final String CONFIG = "azura://config/";
    private static final String MAP = "azura://map/";
    /** Must match the files in resources/devtools */
    private static final String[][] GUIDES = {
            {"concepts", "Plugin concepts", "Modes, teams, map points, config files and when changes apply. Read before editing"},
            {"map-setup", "Map setup guide", "Steps and checks to turn a built world into a playable map"}
    };

    private final AzuraBedWars plugin;
    private final Devtools devtools;

    McpContent(AzuraBedWars plugin, Devtools devtools) {
        this.plugin = plugin;
        this.devtools = devtools;
    }

    String instructions() {
        return "Tools of a live AzuraBedWars BedWars server for Minecraft. Read azura://guide/concepts before the first change."
                + " Start with server_info. Every config change is validated and backed up, history_rollback undoes it."
                + " Positions can come from an online player, so ask the admin to stand where a point belongs."
                + " Talk to the admin in their language.";
    }

    JsonArray resources() {
        JsonArray resources = new JsonArray();
        for (String[] guide : GUIDES) {
            resources.add(resource(GUIDE + guide[0], guide[1], guide[2], "text/markdown"));
        }
        return resources;
    }

    JsonArray resourceTemplates() {
        JsonArray templates = new JsonArray();
        JsonObject config = resource(null, "Config file", "Current text of a config document, ids from config_list", "application/yaml");
        config.addProperty("uriTemplate", CONFIG + "{id}");
        templates.add(config);
        JsonObject map = resource(null, "Map data", "Stored JSON of a map, names from map_list", "application/json");
        map.addProperty("uriTemplate", MAP + "{name}");
        templates.add(map);
        return templates;
    }

    private static JsonObject resource(@Nullable String uri, String name, String description, String mimeType) {
        JsonObject resource = new JsonObject();
        if (uri != null) {
            resource.addProperty("uri", uri);
        }
        resource.addProperty("name", name);
        resource.addProperty("description", description);
        resource.addProperty("mimeType", mimeType);
        return resource;
    }

    static String mimeType(String uri) {
        if (uri.startsWith(CONFIG)) {
            return "application/yaml";
        }
        return uri.startsWith(MAP) ? "application/json" : "text/markdown";
    }

    /**
     * @return null for unknown resources
     */
    @Nullable
    String read(String uri) throws Exception {
        if (uri.startsWith(GUIDE)) {
            for (String[] guide : GUIDES) {
                if (uri.equals(GUIDE + guide[0])) {
                    return classpath("/devtools/" + guide[0] + ".md");
                }
            }
            return null;
        }
        if (uri.startsWith(CONFIG)) {
            String id = uri.substring(CONFIG.length());
            return devtools.sync(() -> {
                ConfigDocument<?> document = plugin.getConfigService().document(id);
                if (document == null || Devtools.DOCUMENT_ID.equals(id)) {
                    return null;
                }
                return ConfigTools.hideSecrets(ConfigService.read(plugin.getConfigService().file(document)));
            });
        }
        if (uri.startsWith(MAP)) {
            String name = uri.substring(MAP.length());
            return devtools.sync(() -> {
                MapData map = plugin.getMapManager().getMap(name);
                return map == null ? null : new GsonBuilder().setPrettyPrinting().create().toJson(map);
            });
        }
        return null;
    }

    JsonArray prompts() {
        JsonArray prompts = new JsonArray();
        JsonObject setupMap = prompt("setup-map", "Walk the admin through turning a built world into a playable map");
        JsonArray arguments = new JsonArray();
        JsonObject name = new JsonObject();
        name.addProperty("name", "map");
        name.addProperty("description", "Map name, new or existing");
        name.addProperty("required", true);
        arguments.add(name);
        setupMap.add("arguments", arguments);
        prompts.add(setupMap);
        prompts.add(prompt("audit-config", "Read every config document and report contradictions and doubtful values"));
        return prompts;
    }

    private static JsonObject prompt(String name, String description) {
        JsonObject prompt = new JsonObject();
        prompt.addProperty("name", name);
        prompt.addProperty("description", description);
        return prompt;
    }

    /**
     * @return null for unknown prompts
     */
    @Nullable
    JsonObject prompt(String name, JsonObject arguments) {
        String text;
        if ("setup-map".equals(name)) {
            String map = arguments.has("map") ? arguments.get("map").getAsString() : "game";
            text = "Help me set up the BedWars map \"" + map + "\". Read azura://guide/map-setup first and follow its steps in order."
                    + " Call server_info to see whether the server is in editor mode. Create the map if it does not exist."
                    + " For each position ask me to stand there and tell you, then read my position with player_list."
                    + " After each step run map_check and tell me what is still missing. At the end show map_markers so I can"
                    + " check every point in game.";
        } else if ("audit-config".equals(name)) {
            text = "Audit the AzuraBedWars configuration. Read azura://guide/concepts, then config_list and config_read for every"
                    + " document, and config_validate each one. Report errors first, then values that contradict each other or look"
                    + " unreasonable for a BedWars server, with the file, key and a suggested value. Change nothing until I agree.";
        } else {
            return null;
        }
        JsonObject content = new JsonObject();
        content.addProperty("type", "text");
        content.addProperty("text", text);
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.add("content", content);
        JsonArray messages = new JsonArray();
        messages.add(message);
        JsonObject result = new JsonObject();
        result.add("description", new JsonPrimitive(name));
        result.add("messages", messages);
        return result;
    }

    private static String classpath(String path) throws IOException {
        try (InputStream input = McpContent.class.getResourceAsStream(path)) {
            if (input == null) {
                throw new IOException("Missing resource " + path);
            }
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                bytes.write(buffer, 0, read);
            }
            return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
