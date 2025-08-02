package cc.azuramc.bedwars.game.map;

import cc.azuramc.bedwars.AzuraBedWars;
import com.cryptomorin.xseries.XMaterial;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author an5w1r@163.com
 */
@Data
public class MapData {
    private final Players players;
    private final Region region;
    private final List<RawLocation> bases;
    private final List<DropRawLocation> drops;
    private final List<ShopRawLocation> shops;
    private String name;
    private String author;
    private RawLocation waitingLocation;
    private RawLocation respawnLocation;

    private String fileUrl;

    public MapData(String mapName) {
        this.name = mapName;
        this.players = new Players();
        this.region = new Region();
        this.bases = new ArrayList<>();
        this.drops = new ArrayList<>();
        this.shops = new ArrayList<>();
    }

    /**
     * 获取处理后的文件URL，支持变量替换
     *
     * @param plugin 插件实例，用于获取插件根目录
     * @return 处理后的文件路径
     */
    public String getProcessedFileUrl(Plugin plugin) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        return processPathVariables(fileUrl, plugin);
    }

    /**
     * 获取处理后的文件URL，自动获取插件实例
     *
     * @return 处理后的文件路径
     */
    public String getProcessedFileUrl() {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        Plugin plugin = AzuraBedWars.getInstance();
        return processPathVariables(fileUrl, plugin);
    }

    /**
     * 文件路径变量支持：
     * 在fileUrl字段中可以使用以下变量来实现相对路径：
     * - {$baseDir}: 插件根目录（plugins文件夹）
     * - {$pluginDir}: 插件数据文件夹（plugins/AzuraBedWars）
     * - {$serverDir}: 服务器根目录
     * 示例：
     * - "{$baseDir}/maps/myMap" -> "plugins/maps/myMap"
     * - "{$pluginDir}/worlds/map1" -> "plugins/AzuraBedWars/worlds/map1"
     * - "{$serverDir}/custom/maps/test" -> "server/custom/maps/test"
     *
     * @param path   原始路径
     * @param plugin 插件实例
     * @return 处理后的路径
     */
    private String processPathVariables(String path, Plugin plugin) {
        if (path == null || path.isEmpty()) {
            return path;
        }

        String processedPath = path;

        if (processedPath.contains("{$baseDir}")) {
            File pluginDataFolder = plugin.getDataFolder();
            String baseDir = pluginDataFolder.getParentFile().getAbsolutePath();
            processedPath = processedPath.replace("{$baseDir}", baseDir);
        }

        if (processedPath.contains("{$pluginDir}")) {
            String pluginDir = plugin.getDataFolder().getAbsolutePath();
            processedPath = processedPath.replace("{$pluginDir}", pluginDir);
        }

        if (processedPath.contains("{$serverDir}")) {
            String serverDir = Bukkit.getWorldContainer().getParentFile().getAbsolutePath();
            processedPath = processedPath.replace("{$serverDir}", serverDir);
        }


        return processedPath;
    }

    public void setWaitingLocation(Location location) {
        RawLocation rawLocation = new RawLocation();
        rawLocation.setWorld(Objects.requireNonNull(location.getWorld()).getName());
        rawLocation.setX(location.getX());
        rawLocation.setY(location.getY());
        rawLocation.setZ(location.getZ());
        rawLocation.setPitch(location.getPitch());
        rawLocation.setYaw(location.getYaw());
        waitingLocation = rawLocation;
    }

    public void setRespawnLocation(Location location) {
        RawLocation rawLocation = new RawLocation();
        rawLocation.setWorld(Objects.requireNonNull(location.getWorld()).getName());
        rawLocation.setX(location.getX());
        rawLocation.setY(location.getY());
        rawLocation.setZ(location.getZ());
        rawLocation.setPitch(location.getPitch());
        rawLocation.setYaw(location.getYaw());
        respawnLocation = rawLocation;
    }

    public void addBase(Location location) {
        RawLocation rawLocation = new RawLocation();
        rawLocation.setWorld(Objects.requireNonNull(location.getWorld()).getName());
        rawLocation.setX(location.getX());
        rawLocation.setY(location.getY());
        rawLocation.setZ(location.getZ());
        rawLocation.setPitch(location.getPitch());
        rawLocation.setYaw(location.getYaw());
        bases.add(rawLocation);
    }

    public void setPos1(Location location) {
        RawLocation rawLocation = new RawLocation();
        rawLocation.setWorld(Objects.requireNonNull(location.getWorld()).getName());
        rawLocation.setX(location.getX());
        rawLocation.setY(location.getY());
        rawLocation.setZ(location.getZ());
        rawLocation.setPitch(location.getPitch());
        rawLocation.setYaw(location.getYaw());
        region.setPos1(rawLocation);
    }

    public void setPos2(Location location) {
        RawLocation rawLocation = new RawLocation();
        rawLocation.setWorld(Objects.requireNonNull(location.getWorld()).getName());
        rawLocation.setX(location.getX());
        rawLocation.setY(location.getY());
        rawLocation.setZ(location.getZ());
        rawLocation.setPitch(location.getPitch());
        rawLocation.setYaw(location.getYaw());
        region.setPos2(rawLocation);
    }

    public void addDrop(DropType dropType, Location location) {
        DropRawLocation dropLocation = new DropRawLocation();
        dropLocation.setWorld(Objects.requireNonNull(location.getWorld()).getName());
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
        shopLocation.setWorld(Objects.requireNonNull(location.getWorld()).getName());
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
     * 获取两个pos中较高的Y坐标 (当作建筑限高值)
     *
     * @return higher pos y
     */
    public double getHigherY() {
        // 如果其中一个位置为null，返回另一个位置的Y坐标或默认值80
        if (getPos1Location() == null) return getPos2Location() != null ? getPos2Location().getY() : 80;
        if (getPos2Location() == null) return getPos1Location().getY();
        return Math.max(getPos1Location().getY(), getPos2Location().getY());
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
     * 获取地图的最大玩家数量
     *
     * @return 地图的最大玩家数量
     */
    public int getMaxPlayers() {
        return this.getBases().size() * this.getPlayers().getTeam();
    }

    /**
     * 删除指定位置的基地
     * @param location 要删除的基地位置
     * @return 是否成功删除
     */
    public boolean removeBase(Location location) {
        if (location == null) {
            return false;
        }

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
        if (location == null) {
            return false;
        }

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
        if (location == null) {
            return false;
        }

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
        // 如果team为null，默认设置为1
        if (players.getTeam() == null) {
            players.setTeam(1);
        }

        // 如果min为null，默认设置为2
        if (players.getMin() == null) {
            players.setMin(2);
        }

        return region.getPos1() != null &&
                region.getPos2() != null &&
               respawnLocation != null &&
               !bases.isEmpty() &&
               players.getTeam() != null &&
               players.getMin() != null;
    }

    public List<Location> loadMap() {
        Location pos1 = region.getPos1().toLocation();
        Location pos2 = region.getPos2().toLocation();

        // 预先计算边界值，避免重复计算
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        // 缓存World对象，避免重复获取
        org.bukkit.World world = pos1.getWorld();

        // 预估容量，减少ArrayList扩容次数
        int estimatedSize = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
        List<Location> blocksLocation = new ArrayList<>(estimatedSize);

        // 优化的三重循环
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location blockLocation = new Location(world, x, y, z);

                    // 使用版本兼容的方式检查方块类型
                    if (canSkippedBlock(blockLocation.getBlock())) {
                        continue;
                    }
                    blocksLocation.add(blockLocation);
                }
            }
        }

        return blocksLocation;
    }

    /**
     * 检查方块是否应该被跳过（不加入到blocks列表中）
     * 此方法兼容1.8和1.21版本
     */
    private boolean canSkippedBlock(Block block) {
        Material material = block.getType();
        String materialName = material.name();

        // 检查AIR
        if (material == XMaterial.AIR.get()) {
            return true;
        }

        // 检查BED_BLOCK（床方块）
        // 在1.13+版本中变为BED、RED_BED等
        if (materialName.contains("BED")) {
            return true;
        }

        if (materialName.contains("DEAD_BUSH")) {
            return true;
        }

        return materialName.contains("LONG_GRASS");
    }

    /**
     * 检查给定位置是否满足特定区域条件。
     * 该条件为：位置的 X 和 Y 坐标在区域边界内，但 Z 坐标在区域边界外或边界上。
     *
     * @param location 要检查的位置
     * @return 如果位置满足 X、Y 在内且 Z 在外的条件，则返回 true；否则返回 false。
     */
    public boolean hasRegion(Location location) {
        // 获取区域的两个对角点位置
        Location pos1 = region.getPos1().toLocation();
        Location pos2 = region.getPos2().toLocation();

        // 获取两个点的整数坐标
        int x1 = pos1.getBlockX();
        int x2 = pos2.getBlockX();
        int y1 = pos1.getBlockY();
        int y2 = pos2.getBlockY();
        int z1 = pos1.getBlockZ();
        int z2 = pos2.getBlockZ();

        // 计算包含边界的最小和最大坐标（包含边界，所以用 <= 和 >=）
        // 这些 min/max 定义了检查范围的边界（不包含边界点本身）
        int minY = Math.min(y1, y2) - 1;
        int maxY = Math.max(y1, y2) + 1;
        int minZ = Math.min(z1, z2) - 1;
        int maxZ = Math.max(z1, z2) + 1;
        int minX = Math.min(x1, x2) - 1;
        int maxX = Math.max(x1, x2) + 1;

        // 检查 X 坐标是否在 (minX, maxX) 开区间内
        boolean withinX = location.getX() > minX && location.getX() < maxX;
        // 检查 Y 坐标是否在 (minY, maxY) 开区间内
        boolean withinY = location.getY() > minY && location.getY() < maxY;

        // 如果 X 或 Y 坐标不在定义的范围内，则直接返回 false
        if (!withinX || !withinY) {
            return false;
        }

        // 检查 Z 坐标是否在 (minZ, maxZ) 开区间的外部或边界上
        // 这等同于原始的: !(location.getZ() > minZ) || !(location.getZ() < maxZ)
        // 意味着 Z <= minZ 或者 Z >= maxZ
        // 提示: 这里是AI打上的注释 如果存在歧义请issue讨论

        // 只有当 X 和 Y 在范围内，并且 Z 在指定范围之外 (或边界上) 时，才返回 true
        return location.getZ() <= minZ || location.getZ() >= maxZ;
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
        /** 基地资源点 */
        BASE,
        /** 钻石资源点 */
        DIAMOND,
        /** 绿宝石资源点 */
        EMERALD
    }

    public enum ShopType {
        /** 物品商店 */
        ITEM,
        /** 升级商店 */
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

        public Location toLocation() {
            return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
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
        private Integer team;
        private Integer min;
    }

    @Getter
    @Setter
    public static class Region {
        private RawLocation pos1;
        private RawLocation pos2;
    }
}
