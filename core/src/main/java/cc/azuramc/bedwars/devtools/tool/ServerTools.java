package cc.azuramc.bedwars.devtools.tool;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.devtools.Args;
import cc.azuramc.bedwars.devtools.Devtools;
import cc.azuramc.bedwars.devtools.Locations;
import cc.azuramc.bedwars.devtools.McpTool;
import cc.azuramc.bedwars.devtools.ToolError;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

/**
 * @author an5w1r@163.com
 */
public final class ServerTools {

    private static final int MAX_SCAN_RADIUS = 24;
    private static final int MAX_SCAN_MATCHES = 200;

    private final AzuraBedWars plugin;
    private final Devtools devtools;

    public ServerTools(AzuraBedWars plugin, Devtools devtools) {
        this.plugin = plugin;
        this.devtools = devtools;
    }

    public List<McpTool> tools() {
        return Arrays.asList(
                McpTool.read("server_info", "Plugin and server versions, whether the plugin runs in editor mode or game mode,"
                                + " economy mode, stored maps, the running game and whether writes are allowed right now. Call this first")
                        .handle(args -> devtools.sync(this::serverInfo)),
                McpTool.read("logs_tail", "Recent AzuraBedWars log lines with stack traces, newest last. Check it after changing config")
                        .choice("level", "Lowest level to include, default INFO", false, "INFO", "WARNING", "SEVERE")
                        .integer("limit", "Lines to return, default 50", false)
                        .string("contains", "Only lines containing this text", false)
                        .handle(this::logsTail),
                McpTool.read("player_list", "Online players with world, position, facing, game mode and the block each one looks at")
                        .handle(args -> devtools.sync(this::playerList)),
                McpTool.read("world_list", "Loaded worlds with spawn point and player count")
                        .handle(args -> devtools.sync(this::worldList)),
                McpTool.read("world_scan", "Block counts in a cube around a position, plus positions of blocks matching filters such as BED or WOOL")
                        .param("at", Locations.schema("Center of the cube"), true)
                        .integer("radius", "Half the cube edge, 1 to " + MAX_SCAN_RADIUS + ", default 8", false)
                        .param("match", matchSchema(), false)
                        .handle(args -> devtools.sync(() -> worldScan(args))),
                McpTool.write("player_teleport", "Teleport an online player, usually the admin, to a position to look at it")
                        .string("player", "Online player name", true)
                        .param("to", Locations.schema("Destination"), true)
                        .handle(args -> devtools.sync(() -> teleport(args)))
        );
    }

    private static JsonObject matchSchema() {
        JsonObject schema = McpTool.Builder.type("array", "Material name parts, case insensitive, such as [\"BED\", \"WOOL\"]");
        schema.add("items", McpTool.Builder.type("string", "Part of a material name"));
        return schema;
    }

    private JsonElement serverInfo() {
        JsonObject info = new JsonObject();
        info.addProperty("plugin", plugin.getPlugin().getDescription().getVersion());
        info.addProperty("server", Bukkit.getName() + " " + Bukkit.getBukkitVersion());
        info.addProperty("java", System.getProperty("java.version"));
        info.addProperty("mode", plugin.isEditing() ? "editor" : "game");
        info.addProperty("modeNote", plugin.isEditing()
                ? "Map tools can edit maps. No game runs. Set editor-mode false in settings.yml and restart to play"
                : "A game runs on map " + plugin.getGameManager().getMapData().getName() + ". Map edits need editor mode");
        info.addProperty("economyMode", plugin.getSettingsConfig().getEconomyMode().name());
        info.addProperty("defaultMap", plugin.getSettingsConfig().getDefaultMapName());
        JsonArray maps = new JsonArray();
        for (String name : plugin.getMapManager().getMapNames()) {
            maps.add(new JsonPrimitive(name));
        }
        info.add("maps", maps);
        GameManager gameManager = plugin.getGameManager();
        if (gameManager != null) {
            JsonObject game = new JsonObject();
            game.addProperty("state", gameManager.getGameState().name());
            game.addProperty("teams", gameManager.getGameTeams().size());
            game.addProperty("aliveTeams", gameManager.getAliveTeams().size());
            info.add("game", game);
        }
        info.addProperty("onlinePlayers", Bukkit.getOnlinePlayers().size());
        String blocked = devtools.writeBlocker();
        info.addProperty("writesAllowed", blocked == null);
        if (blocked != null) {
            info.addProperty("writesBlockedBecause", blocked);
        }
        return info;
    }

    private JsonElement logsTail(Args args) {
        Level level = Level.parse(args.has("level") ? args.string("level") : "INFO");
        String contains = args.has("contains") ? args.string("contains") : "";
        List<String> lines = devtools.getLogs().tail(level, Math.max(1, args.integer("limit", 50)), contains);
        return new JsonPrimitive(lines.isEmpty() ? "No matching log lines since the endpoint started" : String.join("\n", lines));
    }

    private JsonElement playerList() {
        JsonArray players = new JsonArray();
        for (Player player : Bukkit.getOnlinePlayers()) {
            JsonObject json = new JsonObject();
            json.addProperty("name", player.getName());
            json.addProperty("admin", player.hasPermission(LoggerUtil.ADMIN_PERMISSION));
            json.addProperty("gameMode", player.getGameMode().name());
            json.add("location", Locations.toJson(player.getLocation()));
            Block target = player.getTargetBlock((Set<Material>) null, 64);
            if (target != null && target.getType() != Material.AIR) {
                JsonObject looking = new JsonObject();
                looking.addProperty("type", target.getType().name());
                looking.add("block", Locations.block(target));
                json.add("lookingAt", looking);
            }
            players.add(json);
        }
        return players;
    }

    private JsonElement worldList() {
        JsonArray worlds = new JsonArray();
        for (World world : Bukkit.getWorlds()) {
            JsonObject json = new JsonObject();
            json.addProperty("name", world.getName());
            json.addProperty("environment", world.getEnvironment().name());
            json.addProperty("players", world.getPlayers().size());
            json.add("spawn", Locations.toJson(world.getSpawnLocation()));
            worlds.add(json);
        }
        return worlds;
    }

    private JsonElement worldScan(Args args) {
        Location center = Locations.resolve(args.object("at"));
        int radius = args.integer("radius", 8);
        if (radius < 1 || radius > MAX_SCAN_RADIUS) {
            throw new ToolError("radius must be between 1 and " + MAX_SCAN_RADIUS);
        }
        String[] filters = new String[0];
        if (args.has("match")) {
            JsonArray match = args.json().getAsJsonArray("match");
            filters = new String[match.size()];
            for (int i = 0; i < match.size(); i++) {
                filters[i] = match.get(i).getAsString().toUpperCase(Locale.ROOT);
            }
        }
        Map<String, Integer> counts = new TreeMap<>();
        JsonArray matches = new JsonArray();
        int matched = 0;
        World world = center.getWorld();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = world.getBlockAt(center.getBlockX() + x, center.getBlockY() + y, center.getBlockZ() + z);
                    String type = block.getType().name();
                    if (type.endsWith("AIR")) {
                        continue;
                    }
                    Integer count = counts.get(type);
                    counts.put(type, count == null ? 1 : count + 1);
                    if (matches(type, filters)) {
                        matched++;
                        if (matches.size() < MAX_SCAN_MATCHES) {
                            JsonObject json = new JsonObject();
                            json.addProperty("type", type);
                            json.add("block", Locations.block(block));
                            matches.add(json);
                        }
                    }
                }
            }
        }
        JsonObject result = new JsonObject();
        result.add("center", Locations.toJson(center));
        result.addProperty("radius", radius);
        JsonObject countJson = new JsonObject();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            countJson.addProperty(entry.getKey(), entry.getValue());
        }
        result.add("counts", countJson);
        if (filters.length > 0) {
            result.addProperty("matched", matched);
            result.add("matches", matches);
        }
        return result;
    }

    private static boolean matches(String type, String[] filters) {
        for (String filter : filters) {
            if (type.contains(filter)) {
                return true;
            }
        }
        return false;
    }

    private JsonElement teleport(Args args) {
        Player player = Bukkit.getPlayerExact(args.string("player"));
        if (player == null) {
            throw new ToolError("Player " + args.string("player") + " is not online");
        }
        Location destination = Locations.resolve(args.object("to"));
        player.teleport(destination);
        return new JsonPrimitive("Teleported " + player.getName() + " to " + Locations.toJson(destination));
    }
}
