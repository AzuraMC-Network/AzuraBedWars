package cc.azuramc.bedwars.game.arena;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MapData {
    private final Players players;
    private final Region region;
    private final List<RawLocation> bases;
    private final List<DropRawLocation> drops;
    private final List<ShopRawLocation> shops;
    @Setter
    private transient String name;
    @Setter
    private String author;
    private RawLocation waitingLocation;
    private RawLocation reSpawn;

    public MapData(String mapName) {
        this.name = mapName;
        this.players = new Players();
        this.region = new Region();
        this.bases = new ArrayList<>();
        this.drops = new ArrayList<>();
        this.shops = new ArrayList<>();
    }

    public void setWaitingLocation(Location location) {
        RawLocation rawLocation = new RawLocation();
        rawLocation.setWorld(location.getWorld().getName());
        rawLocation.setX(location.getX());
        rawLocation.setY(location.getY());
        rawLocation.setZ(location.getZ());
        rawLocation.setPitch(location.getPitch());
        rawLocation.setYaw(location.getYaw());
        waitingLocation = rawLocation;
    }

    public void setReSpawn(Location location) {
        RawLocation rawLocation = new RawLocation();
        rawLocation.setWorld(location.getWorld().getName());
        rawLocation.setX(location.getX());
        rawLocation.setY(location.getY());
        rawLocation.setZ(location.getZ());
        rawLocation.setPitch(location.getPitch());
        rawLocation.setYaw(location.getYaw());
        reSpawn = rawLocation;
    }

    public void addBase(Location location) {
        RawLocation rawLocation = new RawLocation();
        rawLocation.setWorld(location.getWorld().getName());
        rawLocation.setX(location.getX());
        rawLocation.setY(location.getY());
        rawLocation.setZ(location.getZ());
        rawLocation.setPitch(location.getPitch());
        rawLocation.setYaw(location.getYaw());
        bases.add(rawLocation);
    }

    public void setPos1(Location location) {
        RawLocation rawLocation = new RawLocation();
        rawLocation.setWorld(location.getWorld().getName());
        rawLocation.setX(location.getX());
        rawLocation.setY(location.getY());
        rawLocation.setZ(location.getZ());
        rawLocation.setPitch(location.getPitch());
        rawLocation.setYaw(location.getYaw());
        region.setPos1(rawLocation);
    }

    public void setPos2(Location location) {
        RawLocation rawLocation = new RawLocation();
        rawLocation.setWorld(location.getWorld().getName());
        rawLocation.setX(location.getX());
        rawLocation.setY(location.getY());
        rawLocation.setZ(location.getZ());
        rawLocation.setPitch(location.getPitch());
        rawLocation.setYaw(location.getYaw());
        region.setPos2(rawLocation);
    }

    public void addDrop(DropType dropType, Location location) {
        DropRawLocation dropLocation = new DropRawLocation();
        dropLocation.setWorld(location.getWorld().getName());
        dropLocation.setX(location.getX());
        dropLocation.setY(location.getY());
        dropLocation.setZ(location.getZ());
        dropLocation.setPitch(location.getPitch());
        dropLocation.setYaw(location.getYaw());
        dropLocation.setDropType(dropType);
        this.drops.add(dropLocation);
    }

    public void addShop(ShopType shopType, Location location) {
        ShopRawLocation shopLocation = new ShopRawLocation();
        shopLocation.setWorld(location.getWorld().getName());
        shopLocation.setX(location.getX());
        shopLocation.setY(location.getY());
        shopLocation.setZ(location.getZ());
        shopLocation.setPitch(location.getPitch());
        shopLocation.setYaw(location.getYaw());
        shopLocation.setShopType(shopType);
        this.shops.add(shopLocation);
    }

    public Integer getDrops(DropType dropType) {
        return Math.toIntExact(drops.stream().filter((e) -> e.getDropType() == dropType).count());
    }

    public Integer getShops(ShopType shopType) {
        return Math.toIntExact(shops.stream().filter((e) -> e.getShopType() == shopType).count());
    }

    public List<Location> getDropLocations(DropType dropType) {
        return drops.stream().filter((e) -> e.getDropType() == dropType).map(RawLocation::toLocation).collect(Collectors.toList());
    }

    public List<Location> getShopLocations(ShopType shopType) {
        return shops.stream().filter((e) -> e.getShopType() == shopType).map(RawLocation::toLocation).collect(Collectors.toList());
    }

    /**
     * 获取重生点的Bukkit Location对象
     * @return 地图重生点位置
     */
    public Location getReSpawnLocation() {
        return reSpawn != null ? reSpawn.toLocation() : null;
    }

    /**
     * 获取所有基地的Bukkit Location对象列表
     * @return 地图所有基地位置的列表
     */
    public List<Location> getBaseLocations() {
        return bases.stream().map(RawLocation::toLocation).collect(Collectors.toList());
    }

    /**
     * 获取指定索引的基地Bukkit Location对象
     * @param index 基地索引
     * @return 指定索引的基地位置，如果索引无效则返回null
     */
    public Location getBaseLocation(int index) {
        return (index >= 0 && index < bases.size()) ? bases.get(index).toLocation() : null;
    }

    /**
     * 获取地图区域的第一个点
     * @return 区域第一个点的Bukkit Location对象
     */
    public Location getPos1Location() {
        return region.getPos1() != null ? region.getPos1().toLocation() : null;
    }

    /**
     * 获取地图区域的第二个点
     * @return 区域第二个点的Bukkit Location对象
     */
    public Location getPos2Location() {
        return region.getPos2() != null ? region.getPos2().toLocation() : null;
    }

    /**
     * 获取所有掉落点的Bukkit Location对象列表
     * @return 地图所有掉落点位置的列表
     */
    public List<Location> getAllDropLocations() {
        return drops.stream().map(RawLocation::toLocation).collect(Collectors.toList());
    }

    /**
     * 获取所有商店的Bukkit Location对象列表
     * @return 地图所有商店位置的列表
     */
    public List<Location> getAllShopLocations() {
        return shops.stream().map(RawLocation::toLocation).collect(Collectors.toList());
    }

    /**
     * 删除指定位置的基地
     * @param location 要删除的基地位置
     * @return 是否成功删除
     */
    public boolean removeBase(Location location) {
        if (location == null) return false;
        
        for (int i = 0; i < bases.size(); i++) {
            RawLocation base = bases.get(i);
            Location baseLoc = base.toLocation();
            if (baseLoc.getBlockX() == location.getBlockX() &&
                baseLoc.getBlockY() == location.getBlockY() &&
                baseLoc.getBlockZ() == location.getBlockZ()) {
                bases.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * 删除指定位置的掉落点
     * @param location 要删除的掉落点位置
     * @return 是否成功删除
     */
    public boolean removeDrop(Location location) {
        if (location == null) return false;
        
        for (int i = 0; i < drops.size(); i++) {
            RawLocation drop = drops.get(i);
            Location dropLoc = drop.toLocation();
            if (dropLoc.getBlockX() == location.getBlockX() &&
                dropLoc.getBlockY() == location.getBlockY() &&
                dropLoc.getBlockZ() == location.getBlockZ()) {
                drops.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * 删除指定位置的商店
     * @param location 要删除的商店位置
     * @return 是否成功删除
     */
    public boolean removeShop(Location location) {
        if (location == null) return false;
        
        for (int i = 0; i < shops.size(); i++) {
            RawLocation shop = shops.get(i);
            Location shopLoc = shop.toLocation();
            if (shopLoc.getBlockX() == location.getBlockX() &&
                shopLoc.getBlockY() == location.getBlockY() &&
                shopLoc.getBlockZ() == location.getBlockZ()) {
                shops.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean isValid() {
        return region.getPos1() != null && 
               region.getPos2() != null && 
               reSpawn != null && 
               !bases.isEmpty() &&
               players.getTeam() != null &&
               players.getMin() != null;
    }

    public List<Location> loadMap() {
        List<Location> blocks = new ArrayList<>();
        Location pos1 = region.getPos1().toLocation();
        Location pos2 = region.getPos2().toLocation();
        for (int x = Math.min(pos1.getBlockX(), pos2.getBlockX()); x <= Math.max(pos1.getBlockX(), pos2.getBlockX()); x++) {
            for (int y = Math.min(pos1.getBlockY(), pos2.getBlockY()); y <= Math.max(pos1.getBlockY(), pos2.getBlockY()); y++) {
                for (int z = Math.min(pos1.getBlockZ(), pos2.getBlockZ()); z <= Math.max(pos1.getBlockZ(), pos2.getBlockZ()); z++) {
                    Block block = new Location(pos1.getWorld(), x, y, z).getBlock();

                    // 使用版本兼容的方式检查方块类型
                    if (isSkippableBlock(block)) {
                        continue;
                    }
                    blocks.add(block.getLocation());
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

    public boolean hasRegion(Location location) {
        Location pos1 = region.getPos1().toLocation();
        Location pos2 = region.getPos2().toLocation();

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
        Location pos1 = region.getPos1().toLocation();
        Location pos2 = region.getPos2().toLocation();

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
        ITEM,
        UPGRADE
    }

    @Data
    public static class RawLocation {
        private String world;
        private double x;
        private double y;
        private double z;
        private float pitch;
        private float yaw;

        public Location toLocation() {
            return new Location(Bukkit.getWorld(world), x, y, z, pitch, yaw);
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
    public class Players {
        private Integer team;
        private Integer min;
    }

    @Getter
    @Setter
    public class Region {
        private RawLocation pos1;
        private RawLocation pos2;
    }
}
