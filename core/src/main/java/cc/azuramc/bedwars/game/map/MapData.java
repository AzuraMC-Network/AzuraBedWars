package cc.azuramc.bedwars.game.map;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.api.game.map.IMapData;
import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.compat.util.WoolUtil;
import cc.azuramc.bedwars.game.TeamColor;
import cc.azuramc.bedwars.util.MapUtil;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Persisted as JSON by the map storages, field names are the file format
 *
 * @author an5w1r@163.com
 */
@Data
public class MapData implements IMapData {

    private static final Set<String> BREAKABLE_PLANTS = new HashSet<>(Arrays.asList(
            "LONG_GRASS", "DEAD_BUSH", "SHORT_GRASS", "TALL_GRASS", "FERN", "LARGE_FERN"));

    private final Players players;
    private final Region region;
    private final List<RawLocation> bases;
    private final List<DropRawLocation> drops;
    private final List<ShopRawLocation> shops;
    private String name;
    private String author;
    private RawLocation waitingLocation;
    private RawLocation respawnLocation;
    /** World folder copied into the world container on game start */
    private String fileUrl;

    /** Used by Gson so fields missing from the file keep their defaults */
    private MapData() {
        this.players = new Players();
        this.region = new Region();
        this.bases = new ArrayList<>();
        this.drops = new ArrayList<>();
        this.shops = new ArrayList<>();
    }

    public MapData(String name) {
        this();
        this.name = name;
    }

    /**
     * Supports {$baseDir} for plugins, {$pluginDir} for plugins/AzuraBedWars and {$serverDir} for the server root
     */
    @Nullable
    public File getWorldFolder() {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        File dataFolder = AzuraBedWars.getInstance().getPlugin().getDataFolder().getAbsoluteFile();
        String path = fileUrl
                .replace("{$baseDir}", dataFolder.getParent())
                .replace("{$pluginDir}", dataFolder.getPath())
                .replace("{$serverDir}", new File("").getAbsolutePath());
        return new File(path).getAbsoluteFile();
    }

    public void setWaitingLocation(Location location) {
        waitingLocation = RawLocation.of(location);
    }

    public void setRespawnLocation(Location location) {
        respawnLocation = RawLocation.of(location);
    }

    public void setPos1(Location location) {
        region.setPos1(RawLocation.of(location));
    }

    public void setPos2(Location location) {
        region.setPos2(RawLocation.of(location));
    }

    public void addBase(Location location) {
        bases.add(RawLocation.of(location));
    }

    public void addDrop(DropType dropType, Location location) {
        DropRawLocation drop = RawLocation.of(location, new DropRawLocation());
        drop.setDropType(dropType);
        drops.add(drop);
    }

    public void addShop(ShopType shopType, Location location) {
        ShopRawLocation shop = RawLocation.of(location, new ShopRawLocation());
        shop.setShopType(shopType);
        shops.add(shop);
    }

    /**
     * Removes the base, drop or shop closest to the location
     *
     * @return the removed point, or null when none is within range
     */
    @Nullable
    public RawLocation removeNearest(Location location, double range) {
        RawLocation nearest = null;
        double nearestDistance = range * range;
        for (RawLocation point : points().collect(Collectors.toList())) {
            if (!point.getWorld().equals(location.getWorld().getName())) {
                continue;
            }
            double distance = point.distanceSquared(location);
            if (distance <= nearestDistance) {
                nearest = point;
                nearestDistance = distance;
            }
        }
        RawLocation removed = nearest;
        bases.removeIf(point -> point == removed);
        drops.removeIf(point -> point == removed);
        shops.removeIf(point -> point == removed);
        return removed;
    }

    /**
     * Points every location that was set in the region's world at the given world, so the map can be edited in a
     * world with another name than the copy the game runs in
     */
    public void bindWorld(String worldName) {
        String editWorld = region.getPos1().getWorld();
        Consumer<RawLocation> bind = point -> {
            if (point != null && editWorld.equals(point.getWorld())) {
                point.setWorld(worldName);
            }
        };
        bind.accept(region.getPos1());
        bind.accept(region.getPos2());
        bind.accept(waitingLocation);
        bind.accept(respawnLocation);
        points().forEach(bind);
    }

    /** Bases, drops and shops */
    public Stream<RawLocation> points() {
        return Stream.of(bases.stream(), drops.stream(), shops.stream()).flatMap(s -> s);
    }

    public int getDrops(DropType dropType) {
        return (int) drops.stream().filter(e -> e.getDropType() == dropType).count();
    }

    public int getShops(ShopType shopType) {
        return (int) shops.stream().filter(e -> e.getShopType() == shopType).count();
    }

    public List<Location> getDropLocations(DropType dropType) {
        return drops.stream().filter(e -> e.getDropType() == dropType).map(RawLocation::toLocation).collect(Collectors.toList());
    }

    public List<Location> getShopLocations(ShopType shopType) {
        return shops.stream().filter(e -> e.getShopType() == shopType).map(RawLocation::toLocation).collect(Collectors.toList());
    }

    @Nullable
    public Location getClosestDrop(Location location, DropType dropType) {
        return getDropLocations(dropType).stream()
                .min(Comparator.comparingDouble(location::distanceSquared))
                .orElse(null);
    }

    /**
     * Team color of each base from the closest wool, duplicates and bases without wool take the first unused color
     */
    public List<TeamColor> getTeamColors(int searchRadius) {
        List<TeamColor> colors = new ArrayList<>();
        for (RawLocation base : bases) {
            TeamColor color = findWoolColor(base.toLocation(), searchRadius);
            colors.add(colors.contains(color) ? null : color);
        }
        for (int i = 0; i < colors.size(); i++) {
            if (colors.get(i) == null) {
                colors.set(i, Arrays.stream(TeamColor.values()).filter(c -> !colors.contains(c)).findFirst().orElse(TeamColor.RED));
            }
        }
        return colors;
    }

    @Nullable
    public static TeamColor findWoolColor(Location location, int radius) {
        for (int distance = 0; distance <= radius; distance++) {
            for (int x = -distance; x <= distance; x++) {
                for (int y = -distance; y <= distance; y++) {
                    for (int z = -distance; z <= distance; z++) {
                        if (Math.abs(x) != distance && Math.abs(y) != distance && Math.abs(z) != distance) {
                            continue;
                        }
                        Block block = location.getBlock().getRelative(x, y, z);
                        if (block.getType().name().contains("WOOL")) {
                            return WoolUtil.getTeamColorFromWoolBlock(block);
                        }
                    }
                }
            }
        }
        return null;
    }

    @NotNull
    @Override
    public List<Location> getBaseLocations() {
        return bases.stream().map(RawLocation::toLocation).collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    @Override
    public double getHigherY() {
        if (region.getPos1() == null) {
            return region.getPos2() != null ? region.getPos2().getY() : 80;
        }
        if (region.getPos2() == null) {
            return region.getPos1().getY();
        }
        return Math.max(region.getPos1().getY(), region.getPos2().getY());
    }

    @Override
    public Location getPos1Location() {
        return region.getPos1() != null ? region.getPos1().toLocation() : null;
    }

    @Override
    public Location getPos2Location() {
        return region.getPos2() != null ? region.getPos2().toLocation() : null;
    }

    @NotNull
    @Override
    public List<Location> getAllDropLocations() {
        return drops.stream().map(RawLocation::toLocation).collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    @NotNull
    @Override
    public List<Location> getAllShopLocations() {
        return shops.stream().map(RawLocation::toLocation).collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    @Override
    public int getMaxPlayers() {
        return bases.size() * players.getTeam();
    }

    @Override
    public boolean isValid() {
        return MapCheck.of(this).getErrors().isEmpty();
    }

    /**
     * Blocks that belong to the map and stay unbreakable, plants that a placed block replaces are left out
     */
    public Set<Location> scanMapBlocks() {
        World world = Objects.requireNonNull(region.getPos1().toLocation().getWorld());
        boolean modern = VersionUtil.isGreaterOrEqual(1, 13);
        Set<Location> blocks = new HashSet<>();
        for (int x = region.getMinX(); x <= region.getMaxX(); x++) {
            for (int y = region.getMinY(); y <= region.getMaxY(); y++) {
                for (int z = region.getMinZ(); z <= region.getMaxZ(); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    String type = block.getType().name();
                    if (type.endsWith("AIR") || MapUtil.isBed(block.getType()) || BREAKABLE_PLANTS.contains(type) || modern && type.equals("GRASS")) {
                        continue;
                    }
                    blocks.add(block.getLocation());
                }
            }
        }
        return blocks;
    }

    public boolean isOutsideRegion(Location location) {
        return location.getBlockX() < region.getMinX() || location.getBlockX() > region.getMaxX()
                || location.getBlockY() < region.getMinY() || location.getBlockY() > region.getMaxY()
                || location.getBlockZ() < region.getMinZ() || location.getBlockZ() > region.getMaxZ();
    }

    public boolean containsChunk(int chunkX, int chunkZ) {
        return chunkX >= region.getMinX() >> 4 && chunkX <= region.getMaxX() >> 4
                && chunkZ >= region.getMinZ() >> 4 && chunkZ <= region.getMaxZ() >> 4;
    }

    public enum DropType {
        BASE,
        DIAMOND,
        EMERALD
    }

    public enum ShopType {
        ITEM,
        UPGRADE
    }

    @Data
    public static class RawLocation {
        private String world;
        private double x;
        private double y;
        private double z;
        private float yaw;
        private float pitch;

        public static RawLocation of(Location location) {
            return of(location, new RawLocation());
        }

        static <T extends RawLocation> T of(Location location, T raw) {
            raw.setWorld(Objects.requireNonNull(location.getWorld()).getName());
            raw.setX(location.getX());
            raw.setY(location.getY());
            raw.setZ(location.getZ());
            raw.setYaw(location.getYaw());
            raw.setPitch(location.getPitch());
            return raw;
        }

        public Location toLocation() {
            return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
        }

        public double distanceSquared(Location location) {
            double dx = x - location.getX();
            double dy = y - location.getY();
            double dz = z - location.getZ();
            return dx * dx + dy * dy + dz * dz;
        }
    }

    @Getter
    @Setter
    public static class DropRawLocation extends RawLocation {
        private DropType dropType;
    }

    @Getter
    @Setter
    public static class ShopRawLocation extends RawLocation {
        private ShopType shopType;
    }

    @Getter
    @Setter
    public static class Players {
        private int team = 1;
        private int min = 2;
    }

    @Getter
    @Setter
    public static class Region {
        private RawLocation pos1;
        private RawLocation pos2;

        int getMinX() {
            return (int) Math.floor(Math.min(pos1.getX(), pos2.getX()));
        }

        int getMaxX() {
            return (int) Math.floor(Math.max(pos1.getX(), pos2.getX()));
        }

        int getMinY() {
            return (int) Math.floor(Math.min(pos1.getY(), pos2.getY()));
        }

        int getMaxY() {
            return (int) Math.floor(Math.max(pos1.getY(), pos2.getY()));
        }

        int getMinZ() {
            return (int) Math.floor(Math.min(pos1.getZ(), pos2.getZ()));
        }

        int getMaxZ() {
            return (int) Math.floor(Math.max(pos1.getZ(), pos2.getZ()));
        }
    }
}
