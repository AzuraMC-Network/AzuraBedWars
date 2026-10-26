package cc.azuramc.bedwars.devtools.tool;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.ConfigService;
import cc.azuramc.bedwars.devtools.Args;
import cc.azuramc.bedwars.devtools.Devtools;
import cc.azuramc.bedwars.devtools.Locations;
import cc.azuramc.bedwars.devtools.McpTool;
import cc.azuramc.bedwars.devtools.ToolError;
import cc.azuramc.bedwars.game.TeamColor;
import cc.azuramc.bedwars.game.map.MapCheck;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.game.map.MapManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Same edits as /map, with positions given as coordinates or taken from an online player
 *
 * @author an5w1r@163.com
 */
public final class MapTools {

    private static final String NAME_PATTERN = "^[a-zA-Z0-9_\\-]{1,30}$";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final AzuraBedWars plugin;
    private final Devtools devtools;

    public MapTools(AzuraBedWars plugin, Devtools devtools) {
        this.plugin = plugin;
        this.devtools = devtools;
    }

    private enum Field {
        WAITING, RESPAWN, POS1, POS2, AUTHOR, TEAM_PLAYERS, MIN_PLAYERS, WORLD_FOLDER
    }

    private enum Kind {
        BASE, DROP, SHOP
    }

    public List<McpTool> tools() {
        return Arrays.asList(
                McpTool.read("map_list", "Stored maps and whether each one can run a game")
                        .handle(args -> devtools.sync(this::list)),
                McpTool.read("map_get", "Everything stored for a map. Base order is team order. Point indexes are what map_point_remove takes")
                        .string("name", "Map name", true)
                        .handle(args -> devtools.sync(() -> get(args))),
                McpTool.read("map_check", "What a map still needs before a game can run on it, the same check the plugin runs at startup."
                                + " Bed and wool checks only run while the map's world is loaded")
                        .string("name", "Map name", true)
                        .handle(args -> devtools.sync(() -> check(args))),
                McpTool.write("map_create", "Create an empty map, then set world_folder, waiting, respawn, pos1, pos2 and add points")
                        .string("name", "Letters, digits, _ and -, up to 30 characters. Must differ from the main world name", true)
                        .handle(args -> devtools.sync(() -> create(args))),
                McpTool.write("map_set", "Set one map field. waiting, respawn, pos1 and pos2 take at; author, team_players,"
                                + " min_players and world_folder take value. pos1 and pos2 are opposite region corners, the higher one is the build limit")
                        .string("name", "Map name", true)
                        .choice("field", "Field to set", true, "waiting", "respawn", "pos1", "pos2", "author", "team_players", "min_players", "world_folder")
                        .param("at", Locations.schema("Position for waiting, respawn, pos1 or pos2"), false)
                        .string("value", "Value for author, team_players (1 to 8), min_players (at least 2) or world_folder"
                                + " (world save folder copied at game start, may use {$serverDir}, {$baseDir} for plugins/ and {$pluginDir})", false)
                        .handle(args -> devtools.sync(() -> set(args))),
                McpTool.write("map_point_add", "Add a team base (spawn point, team color comes from nearby wool), a resource drop or a shop NPC")
                        .string("name", "Map name", true)
                        .choice("kind", "Point kind", true, "base", "drop", "shop")
                        .string("type", "drop: BASE (team iron and gold), DIAMOND or EMERALD. shop: ITEM or UPGRADE", false)
                        .param("at", Locations.schema("Position, shops face the given yaw"), true)
                        .handle(args -> devtools.sync(() -> addPoint(args))),
                McpTool.write("map_point_remove", "Remove a base, drop or shop by its index from map_get")
                        .string("name", "Map name", true)
                        .choice("kind", "Point kind", true, "base", "drop", "shop")
                        .integer("index", "Index from map_get, starting at 0", true)
                        .handle(args -> devtools.sync(() -> removePoint(args))),
                McpTool.write("map_markers", "Show floating labels at every point of a map so the admin can check them in game, or hide them")
                        .string("name", "Map name, needed to show", false)
                        .bool("show", "false hides all labels, default true", false)
                        .integer("seconds", "How long labels stay, default 300", false)
                        .handle(args -> devtools.sync(() -> markers(args)))
        );
    }

    private JsonElement list() {
        JsonArray maps = new JsonArray();
        for (String name : plugin.getMapManager().getMapNames()) {
            MapData map = plugin.getMapManager().getMap(name);
            MapCheck check = MapCheck.of(map);
            JsonObject json = new JsonObject();
            json.addProperty("name", name);
            json.addProperty("ready", check.getErrors().isEmpty());
            json.addProperty("errors", check.getErrors().size());
            json.addProperty("warnings", check.getWarnings().size());
            json.addProperty("default", name.equals(plugin.getSettingsConfig().getDefaultMapName()));
            maps.add(json);
        }
        return maps;
    }

    private JsonElement get(Args args) {
        MapData map = map(args.string("name"));
        JsonObject json = new JsonObject();
        json.addProperty("name", map.getName());
        json.addProperty("author", map.getAuthor());
        json.addProperty("worldFolder", map.getFileUrl());
        File folder = map.getWorldFolder();
        json.addProperty("worldFolderResolved", folder == null ? null : folder.getPath());
        JsonObject players = new JsonObject();
        players.addProperty("perTeam", map.getPlayers().getTeam());
        players.addProperty("min", map.getPlayers().getMin());
        players.addProperty("max", map.getMaxPlayers());
        json.add("players", players);
        json.add("waiting", Locations.toJson(map.getWaitingLocation()));
        json.add("respawn", Locations.toJson(map.getRespawnLocation()));
        json.add("pos1", Locations.toJson(map.getRegion().getPos1()));
        json.add("pos2", Locations.toJson(map.getRegion().getPos2()));

        int radius = plugin.getGameplayConfig().getTeamBlockSearchRadius();
        JsonArray bases = new JsonArray();
        for (int i = 0; i < map.getBases().size(); i++) {
            MapData.RawLocation base = map.getBases().get(i);
            JsonObject point = point(i, base);
            if (Bukkit.getWorld(base.getWorld()) != null) {
                TeamColor color = MapData.findWoolColor(base.toLocation(), radius);
                point.addProperty("nearbyWool", color == null ? "none" : color.name());
            }
            bases.add(point);
        }
        json.add("bases", bases);
        JsonArray drops = new JsonArray();
        for (int i = 0; i < map.getDrops().size(); i++) {
            JsonObject point = point(i, map.getDrops().get(i));
            point.addProperty("type", map.getDrops().get(i).getDropType().name());
            drops.add(point);
        }
        json.add("drops", drops);
        JsonArray shops = new JsonArray();
        for (int i = 0; i < map.getShops().size(); i++) {
            JsonObject point = point(i, map.getShops().get(i));
            point.addProperty("type", map.getShops().get(i).getShopType().name());
            shops.add(point);
        }
        json.add("shops", shops);
        return json;
    }

    private static JsonObject point(int index, MapData.RawLocation location) {
        JsonObject point = new JsonObject();
        point.addProperty("index", index);
        point.add("location", Locations.toJson(location));
        return point;
    }

    private JsonElement check(Args args) {
        MapCheck check = MapCheck.of(map(args.string("name")));
        JsonObject json = new JsonObject();
        json.addProperty("ready", check.getErrors().isEmpty());
        json.add("errors", strings(check.getErrors()));
        json.add("warnings", strings(check.getWarnings()));
        json.add("bases", strings(check.getBaseLines()));
        return json;
    }

    private JsonElement create(Args args) {
        requireEditing();
        String name = args.string("name");
        if (!name.matches(NAME_PATTERN)) {
            throw new ToolError("Map names use letters, digits, _ and -, up to 30 characters");
        }
        if (plugin.getMapManager().exists(name)) {
            throw new ToolError("Map " + name + " already exists");
        }
        if (!Bukkit.getWorlds().isEmpty() && Bukkit.getWorlds().get(0).getName().equals(name)) {
            throw new ToolError("The main world is called " + name + ", a map with that name cannot be copied at game start");
        }
        if (!plugin.getMapManager().save(new MapData(name))) {
            throw new ToolError("Saving the map failed, see logs_tail");
        }
        return new JsonPrimitive("Map " + name + " created. Next: map_set world_folder, waiting, respawn, pos1, pos2");
    }

    private JsonElement set(Args args) {
        Field field = args.choice("field", Field.class);
        String name = args.string("name");
        switch (field) {
            case WAITING:
            case RESPAWN:
            case POS1:
            case POS2:
                Location location = Locations.resolve(args.object("at"));
                edit(name, map -> {
                    if (field == Field.WAITING) {
                        map.setWaitingLocation(location);
                    } else if (field == Field.RESPAWN) {
                        map.setRespawnLocation(location);
                    } else if (field == Field.POS1) {
                        map.setPos1(location);
                    } else {
                        map.setPos2(location);
                    }
                });
                return new JsonPrimitive(field.name().toLowerCase() + " set to " + Locations.toJson(location));
            case AUTHOR:
                String author = args.string("value");
                if (author.length() > 24) {
                    throw new ToolError("Author names have at most 24 characters");
                }
                edit(name, map -> map.setAuthor(author));
                return new JsonPrimitive("Author set to " + author);
            case TEAM_PLAYERS:
                int team = number(args.string("value"));
                if (team < 1 || team > 8) {
                    throw new ToolError("team_players must be between 1 and 8");
                }
                MapData teamMap = edit(name, map -> map.getPlayers().setTeam(team));
                return new JsonPrimitive("Players per team set to " + team + ", the map holds " + teamMap.getMaxPlayers());
            case MIN_PLAYERS:
                int min = number(args.string("value"));
                if (min < 2) {
                    throw new ToolError("min_players must be at least 2");
                }
                edit(name, map -> map.getPlayers().setMin(min));
                return new JsonPrimitive("Minimum players to start set to " + min);
            default:
                String folder = args.string("value").trim();
                MapData folderMap = edit(name, map -> map.setFileUrl(folder));
                String error = MapCheck.findWorldFolderError(folderMap);
                return new JsonPrimitive("World folder set to " + folderMap.getWorldFolder() + (error == null ? "" : ". Problem: " + error));
        }
    }

    private JsonElement addPoint(Args args) {
        Kind kind = args.choice("kind", Kind.class);
        Location location = Locations.resolve(args.object("at"));
        MapData map;
        String added;
        switch (kind) {
            case BASE:
                map = edit(args.string("name"), m -> m.addBase(location));
                int radius = plugin.getGameplayConfig().getTeamBlockSearchRadius();
                TeamColor color = MapData.findWoolColor(location, radius);
                added = "Base " + (map.getBases().size() - 1) + " added, " + (color == null
                        ? "no wool within " + radius + " blocks so the team color is picked at game start" : "nearby wool " + color.name());
                break;
            case DROP:
                MapData.DropType dropType = args.choice("type", MapData.DropType.class);
                map = edit(args.string("name"), m -> m.addDrop(dropType, location));
                added = dropType + " drop " + (map.getDrops().size() - 1) + " added";
                break;
            default:
                MapData.ShopType shopType = args.choice("type", MapData.ShopType.class);
                map = edit(args.string("name"), m -> m.addShop(shopType, location));
                added = shopType + " shop " + (map.getShops().size() - 1) + " added";
        }
        return new JsonPrimitive(added + " at " + Locations.toJson(location) + ". " + map.getBases().size() + " bases, "
                + map.getDrops().size() + " drops, " + map.getShops().size() + " shops");
    }

    private JsonElement removePoint(Args args) {
        Kind kind = args.choice("kind", Kind.class);
        int index = args.integer("index");
        MapData current = map(args.string("name"));
        List<? extends MapData.RawLocation> points = kind == Kind.BASE ? current.getBases()
                : kind == Kind.DROP ? current.getDrops() : current.getShops();
        if (index < 0 || index >= points.size()) {
            throw new ToolError("No " + kind.name().toLowerCase() + " with index " + index + ", there are " + points.size());
        }
        MapData.RawLocation removed = points.get(index);
        edit(current.getName(), map -> {
            if (kind == Kind.BASE) {
                map.getBases().remove(index);
            } else if (kind == Kind.DROP) {
                map.getDrops().remove(index);
            } else {
                map.getShops().remove(index);
            }
        });
        return new JsonPrimitive("Removed " + kind.name().toLowerCase() + " " + index + " at " + Locations.toJson(removed)
                + (kind == Kind.BASE ? ". Later bases moved up one index, so their team order changed" : ""));
    }

    private JsonElement markers(Args args) {
        if (!args.bool("show", true)) {
            return new JsonPrimitive("Removed " + devtools.getMarkers().clear() + " labels");
        }
        MapData map = map(args.string("name"));
        int seconds = args.integer("seconds", 300);
        if (seconds < 1 || seconds > 3600) {
            throw new ToolError("seconds must be between 1 and 3600");
        }
        List<Location> locations = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        addMarker(locations, labels, map.getWaitingLocation(), "Waiting lobby");
        addMarker(locations, labels, map.getRespawnLocation(), "Spectator respawn");
        addMarker(locations, labels, map.getRegion().getPos1(), "Region pos1");
        addMarker(locations, labels, map.getRegion().getPos2(), "Region pos2");
        for (int i = 0; i < map.getBases().size(); i++) {
            addMarker(locations, labels, map.getBases().get(i), "Base " + i);
        }
        for (int i = 0; i < map.getDrops().size(); i++) {
            addMarker(locations, labels, map.getDrops().get(i), map.getDrops().get(i).getDropType() + " drop " + i);
        }
        for (int i = 0; i < map.getShops().size(); i++) {
            addMarker(locations, labels, map.getShops().get(i), map.getShops().get(i).getShopType() + " shop " + i);
        }
        // stands in unloaded chunks would outlive clear
        int skipped = 0;
        for (int i = locations.size() - 1; i >= 0; i--) {
            Location location = locations.get(i);
            if (!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
                locations.remove(i);
                labels.remove(i);
                skipped++;
            }
        }
        if (locations.isEmpty()) {
            throw new ToolError("No point of map " + map.getName() + " is in a loaded chunk, teleport a player there first");
        }
        devtools.getMarkers().show(locations, labels, seconds);
        return new JsonPrimitive("Showing " + locations.size() + " labels for " + seconds + " seconds"
                + (skipped > 0 ? ", " + skipped + " points in unloaded chunks left out" : ""));
    }

    private static void addMarker(List<Location> locations, List<String> labels, MapData.RawLocation point, String label) {
        if (point != null && Bukkit.getWorld(point.getWorld()) != null) {
            locations.add(point.toLocation());
            labels.add(label);
        }
    }

    private MapData map(String name) {
        MapData map = plugin.getMapManager().getMap(name);
        if (map == null) {
            throw new ToolError("No map " + name + ", maps: " + String.join(", ", plugin.getMapManager().getMapNames()));
        }
        return map;
    }

    private void requireEditing() {
        if (!plugin.isEditing()) {
            throw new ToolError("Maps can only be edited in editor mode, the running game uses the map."
                    + " Set editor-mode: true in settings.yml with config_set and restart the server");
        }
    }

    /**
     * Backs up the stored map, applies the change and saves it
     */
    private MapData edit(String name, Consumer<MapData> change) {
        requireEditing();
        MapData map = map(name);
        String before = GSON.toJson(map);
        change.accept(map);
        save(plugin, map, before);
        return map;
    }

    private static void save(AzuraBedWars plugin, MapData map, String before) {
        try {
            ConfigService.backup(plugin.getPlugin().getDataFolder(), ConfigTools.MAP_HISTORY_PREFIX + map.getName() + ".json.",
                    before.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            plugin.getMapManager().reload(map.getName());
            throw new ToolError("Could not back up the map, nothing was changed: " + e.getMessage());
        }
        if (!plugin.getMapManager().save(map)) {
            plugin.getMapManager().reload(map.getName());
            throw new ToolError("Saving the map failed, see logs_tail");
        }
    }

    /**
     * Must run on the main thread
     */
    static String restore(AzuraBedWars plugin, String name, String json) {
        if (!plugin.isEditing()) {
            throw new ToolError("Maps can only be restored in editor mode");
        }
        MapData restored;
        try {
            restored = GSON.fromJson(json, MapData.class);
        } catch (JsonParseException e) {
            throw new ToolError("The backup is not a valid map: " + e.getMessage());
        }
        restored.setName(name);
        MapManager maps = plugin.getMapManager();
        MapData current = maps.getMap(name);
        if (current == null) {
            if (!maps.save(restored)) {
                throw new ToolError("Saving the map failed, see logs_tail");
            }
        } else {
            save(plugin, restored, GSON.toJson(current));
        }
        return "Map " + name + " restored";
    }

    private static int number(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new ToolError(value + " is not a whole number");
        }
    }

    private static JsonArray strings(List<String> values) {
        JsonArray array = new JsonArray();
        for (String value : values) {
            array.add(new JsonPrimitive(value));
        }
        return array;
    }
}
