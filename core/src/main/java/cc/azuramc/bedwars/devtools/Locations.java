package cc.azuramc.bedwars.devtools;

import cc.azuramc.bedwars.game.map.MapData;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * The {@code at} argument shared by tools that take a position
 *
 * @author an5w1r@163.com
 */
public final class Locations {

    private static final int TARGET_DISTANCE = 64;

    private Locations() {
    }

    public static JsonObject schema(String description) {
        JsonObject properties = new JsonObject();
        properties.add("player", McpTool.Builder.type("string", "Use where this online player stands, with their facing"));
        properties.add("target", McpTool.Builder.type("boolean", "With player: use the top of the block the player looks at instead"));
        properties.add("world", McpTool.Builder.type("string", "World name, required without player"));
        properties.add("x", McpTool.Builder.type("number", "Required without player"));
        properties.add("y", McpTool.Builder.type("number", "Required without player"));
        properties.add("z", McpTool.Builder.type("number", "Required without player"));
        properties.add("yaw", McpTool.Builder.type("number", "Facing, 0 south, 90 west, 180 north, -90 east"));
        properties.add("pitch", McpTool.Builder.type("number", "0 looks straight ahead"));
        properties.add("center", McpTool.Builder.type("boolean", "Snap to the block center and the facing to the nearest 90 degrees, like /map align"));
        JsonObject schema = McpTool.Builder.type("object", description + ". Give either player or world with x, y, z");
        schema.add("properties", properties);
        return schema;
    }

    /**
     * Must run on the main thread
     */
    public static Location resolve(JsonObject at) {
        Args args = new Args(at);
        Location location;
        if (args.has("player")) {
            Player player = Bukkit.getPlayerExact(args.string("player"));
            if (player == null) {
                throw new ToolError("Player " + args.string("player") + " is not online, use player_list");
            }
            location = player.getLocation();
            if (args.bool("target", false)) {
                Block block = player.getTargetBlock((Set<Material>) null, TARGET_DISTANCE);
                if (block == null || block.getType() == Material.AIR) {
                    throw new ToolError(player.getName() + " is not looking at a block within " + TARGET_DISTANCE + " blocks");
                }
                location = block.getLocation().add(0.5, 1, 0.5);
                location.setYaw(player.getLocation().getYaw());
            }
        } else {
            String worldName = args.string("world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                throw new ToolError("World " + worldName + " is not loaded, use world_list");
            }
            location = new Location(world, number(at, "x"), number(at, "y"), number(at, "z"));
        }
        if (at.has("yaw")) {
            location.setYaw((float) number(at, "yaw"));
        }
        if (at.has("pitch")) {
            location.setPitch((float) number(at, "pitch"));
        }
        if (args.bool("center", false)) {
            location.setX(location.getBlockX() + 0.5);
            location.setZ(location.getBlockZ() + 0.5);
            location.setYaw(Math.round(location.getYaw() / 90) * 90f);
            location.setPitch(0);
        }
        return location;
    }

    private static double number(JsonObject at, String key) {
        if (!at.has(key) || !at.get(key).isJsonPrimitive() || !at.getAsJsonPrimitive(key).isNumber()) {
            throw new ToolError("at." + key + " must be a number");
        }
        return at.get(key).getAsDouble();
    }

    public static JsonObject toJson(@Nullable Location location) {
        if (location == null) {
            return null;
        }
        return toJson(location.getWorld() == null ? null : location.getWorld().getName(),
                location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public static JsonObject toJson(@Nullable MapData.RawLocation location) {
        if (location == null) {
            return null;
        }
        return toJson(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    private static JsonObject toJson(String world, double x, double y, double z, float yaw, float pitch) {
        JsonObject json = new JsonObject();
        json.addProperty("world", world);
        json.addProperty("x", round(x));
        json.addProperty("y", round(y));
        json.addProperty("z", round(z));
        json.addProperty("yaw", round(yaw));
        json.addProperty("pitch", round(pitch));
        return json;
    }

    public static JsonArray block(Block block) {
        JsonArray xyz = new JsonArray();
        xyz.add(new JsonPrimitive(block.getX()));
        xyz.add(new JsonPrimitive(block.getY()));
        xyz.add(new JsonPrimitive(block.getZ()));
        return xyz;
    }

    private static double round(double value) {
        return Math.round(value * 100) / 100.0;
    }
}
