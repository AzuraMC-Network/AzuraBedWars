package cc.azuramc.bedwars.database.map;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MapData {
    private final Players players;
    private final Region region;
    private final List<Location> bases;
    private final List<DropLocation> drops;
    private final List<ShopLocation> shops;
    @Setter
    private transient String name;
    @Setter
    private String author;
    private Location reSpawn;

    public MapData() {
        this.players = new Players();
        this.region = new Region();
        this.bases = new ArrayList<>();
        this.drops = new ArrayList<>();
        this.shops = new ArrayList<>();
    }

    public void setReSpawn(org.bukkit.Location location) {
        Location rawLocation = new Location();
        rawLocation.setWorld(location.getWorld().getName());
        rawLocation.setX(location.getX());
        rawLocation.setY(location.getY());
        rawLocation.setZ(location.getZ());
        rawLocation.setPitch(location.getPitch());
        rawLocation.setY(location.getYaw());
        reSpawn = rawLocation;
    }

    public void addBase(org.bukkit.Location location) {
        Location rawLocation = new Location();
        rawLocation.setWorld(location.getWorld().getName());
        rawLocation.setX(location.getX());
        rawLocation.setY(location.getY());
        rawLocation.setZ(location.getZ());
        rawLocation.setPitch(location.getPitch());
        rawLocation.setY(location.getYaw());
        bases.add(rawLocation);
    }

    public void setPos1(org.bukkit.Location location) {
        Location rawLocation = new Location();
        rawLocation.setWorld(location.getWorld().getName());
        rawLocation.setX(location.getX());
        rawLocation.setY(location.getY());
        rawLocation.setZ(location.getZ());
        rawLocation.setPitch(location.getPitch());
        rawLocation.setY(location.getYaw());
        region.setPos1(rawLocation);
    }

    public void setPos2(org.bukkit.Location location) {
        Location rawLocation = new Location();
        rawLocation.setWorld(location.getWorld().getName());
        rawLocation.setX(location.getX());
        rawLocation.setY(location.getY());
        rawLocation.setZ(location.getZ());
        rawLocation.setPitch(location.getPitch());
        rawLocation.setY(location.getYaw());
        region.setPos2(rawLocation);
    }

    public void addDrop(DropType dropType, org.bukkit.Location location) {
        DropLocation dropLocation = new DropLocation();
        dropLocation.setWorld(location.getWorld().getName());
        dropLocation.setX(location.getX());
        dropLocation.setY(location.getY());
        dropLocation.setZ(location.getZ());
        dropLocation.setPitch(location.getPitch());
        dropLocation.setY(location.getYaw());
        dropLocation.setDropType(dropType);
        this.drops.add(dropLocation);
    }

    public void addShop(ShopType shopType, org.bukkit.Location location) {
        ShopLocation shopLocation = new ShopLocation();
        shopLocation.setWorld(location.getWorld().getName());
        shopLocation.setX(location.getX());
        shopLocation.setY(location.getY());
        shopLocation.setZ(location.getZ());
        shopLocation.setPitch(location.getPitch());
        shopLocation.setY(location.getYaw());
        shopLocation.setShopType(shopType);
        this.shops.add(shopLocation);
    }

    public Integer getDrops(DropType dropType) {
        return Math.toIntExact(drops.stream().filter((e) -> e.getDropType() == dropType).count());
    }

    public Integer getShops(ShopType shopType) {
        return Math.toIntExact(shops.stream().filter((e) -> e.getShopType() == shopType).count());
    }

    public List<org.bukkit.Location> getDropLocations(DropType dropType) {
        return drops.stream().filter((e) -> e.getDropType() == dropType).map(Location::toLocation).collect(Collectors.toList());
    }

    public List<org.bukkit.Location> getShopLocations(ShopType shopType) {
        return shops.stream().filter((e) -> e.getShopType() == shopType).map(Location::toLocation).collect(Collectors.toList());
    }

    public List<org.bukkit.Location> loadMap() {
        List<org.bukkit.Location> blocks = new ArrayList<>();
        org.bukkit.Location pos1 = region.getPos1().toLocation();
        org.bukkit.Location pos2 = region.getPos2().toLocation();
        for (int x = Math.min(pos1.getBlockX(), pos2.getBlockX()); x <= Math.max(pos1.getBlockX(), pos2.getBlockX()); x++) {
            for (int y = Math.min(pos1.getBlockY(), pos2.getBlockY()); y <= Math.max(pos1.getBlockY(), pos2.getBlockY()); y++) {
                for (int z = Math.min(pos1.getBlockZ(), pos2.getBlockZ()); z <= Math.max(pos1.getBlockZ(), pos2.getBlockZ()); z++) {
                    Block block = new org.bukkit.Location(pos1.getWorld(), x, y, z).getBlock();

                    if (block != null) {
                        // 使用版本兼容的方式检查方块类型
                        if (isSkippableBlock(block)) {
                            continue;
                        }
                        System.out.println(x + ", " + y + ", " + z);
                        blocks.add(block.getLocation());
                    }
                }
            }
        }

        return blocks;
    }

    /**
     * 检查方块是否应该被跳过（不加入到blocks列表中）
     * 此方法兼容1.8和1.21版本
     */
    private boolean isSkippableBlock(Block block) {
        Material type = block.getType();
        String typeName = type.name();

        // 检查AIR（空气方块）- 两个版本都有
        if (type == Material.AIR) {
            return true;
        }

        // 检查BED_BLOCK（床方块）
        // 在1.13+版本中变为BED、RED_BED等
        if (typeName.equals("BED_BLOCK") || typeName.contains("_BED")) {
            return true;
        }

        // 检查LONG_GRASS（长草）
        // 在1.13+版本中变为GRASS、TALL_GRASS等
        if (typeName.equals("LONG_GRASS") || typeName.equals("GRASS") ||
                typeName.equals("TALL_GRASS") || typeName.contains("_GRASS")) {
            return true;
        }

        // 检查DEAD_BUSH（枯灌木）
        // 在1.13+版本中名称保持一致，但为了安全起见使用字符串比较
        if (typeName.equals("DEAD_BUSH") || typeName.contains("DEAD_BUSH")) {
            return true;
        }

        return false;
    }

    public boolean hasRegion(org.bukkit.Location location) {
        org.bukkit.Location pos1 = region.getPos1().toLocation();
        org.bukkit.Location pos2 = region.getPos2().toLocation();

        int x1 = pos1.getBlockX();
        int x2 = pos2.getBlockX();
        int y1 = pos1.getBlockY();
        int y2 = pos2.getBlockY();
        int z1 = pos1.getBlockZ();
        int z2 = pos2.getBlockZ();

        int minY = Math.min(y1, y2) - 1;
        int maxY = Math.max(y1, y2) + 1;
        int minZ = Math.min(z1, z2) - 1;
        int maxZ = Math.max(z1, z2) + 1;
        int minX = Math.min(x1, x2) - 1;
        int maxX = Math.max(x1, x2) + 1;

        if (location.getX() > minX && location.getX() < maxX) {
            if (location.getY() > minY && location.getY() < maxY) {
                return !(location.getZ() > minZ) || !(location.getZ() < maxZ);
            }
        }
        return true;
    }

    public boolean chunkIsInRegion(double x, double z) {
        org.bukkit.Location pos1 = region.getPos1().toLocation();
        org.bukkit.Location pos2 = region.getPos2().toLocation();

        int x1 = pos1.getBlockX();
        int x2 = pos2.getBlockX();
        int z1 = pos1.getBlockZ();
        int z2 = pos2.getBlockZ();

        int minZ = Math.min(z1, z2) - 1;
        int maxZ = Math.max(z1, z2) + 1;
        int minX = Math.min(x1, x2) - 1;
        int maxX = Math.max(x1, x2) + 1;

        return (x >= minX && x <= maxX && z >= minZ && z <= maxZ);
    }

    public enum DropType {
        BASE, DIAMOND, EMERALD
    }

    public enum ShopType {
        ITEM, UPDATE
    }

    @Data
    public static class Location {
        private String world;
        private double x;
        private double y;
        private double z;
        private float pitch;
        private float yaw;

        public org.bukkit.Location toLocation() {
//            return new org.bukkit.Location(Bukkit.getWorld(world), x, y, z, pitch, yaw);
            return new org.bukkit.Location(Bukkit.getWorld("world"), 0.5, 180, 0.5, 0, 180);
        }
    }

    @Getter
    @Setter
    public static class DropLocation extends Location {
        private DropType dropType;
    }

    @Getter
    @Setter
    public static class ShopLocation extends Location {
        private ShopType shopType;
    }

    @Getter
    @Setter
    public class Players {
        private Integer team;
        private Integer min;
    }

    @Getter
    @Setter
    public class Region {
        private Location pos1;
        private Location pos2;
    }
}
