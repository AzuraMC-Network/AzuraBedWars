package cc.azuramc.bedwars.game.map;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.SettingsConfig;
import cc.azuramc.bedwars.game.TeamColor;
import cc.azuramc.bedwars.util.MapUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * What a map is missing before it can run, block checks only run while the map's world is loaded
 *
 * @author an5w1r@163.com
 */
@Getter
public final class MapCheck {

    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private final List<String> baseLines = new ArrayList<>();

    private MapCheck() {
    }

    public static MapCheck of(MapData map) {
        MapCheck check = new MapCheck();
        check.checkLocations(map);
        check.checkPlayers(map);
        String folderError = findWorldFolderError(map);
        if (folderError != null) {
            check.errors.add(folderError);
        }
        MapData.RawLocation pos1 = map.getRegion().getPos1();
        if (pos1 != null && Bukkit.getWorld(pos1.getWorld()) != null) {
            check.checkBlocks(map);
        } else if (pos1 != null) {
            check.warnings.add("世界 " + pos1.getWorld() + " 未加载 跳过床和羊毛检查");
        }
        return check;
    }

    private void checkLocations(MapData map) {
        MapData.Region region = map.getRegion();
        if (map.getWaitingLocation() == null) {
            errors.add("未设置等待大厅 /map setWaiting");
        }
        if (map.getRespawnLocation() == null) {
            errors.add("未设置观战重生点 /map setRespawn");
        }
        if (region.getPos1() == null || region.getPos2() == null) {
            errors.add("未设置地图边界 /map setPos1 与 /map setPos2");
        }
        if (map.getBases().size() < 2) {
            errors.add("至少需要 2 个基地 当前 " + map.getBases().size() + " 个");
        }
        if (map.getBases().size() > TeamColor.values().length) {
            errors.add("基地最多 " + TeamColor.values().length + " 个 当前 " + map.getBases().size() + " 个");
        }
        if (map.getDrops(MapData.DropType.BASE) == 0) {
            errors.add("没有基地资源点 /map addDrop <地图> BASE");
        }
        if (map.getDrops(MapData.DropType.DIAMOND) == 0) {
            warnings.add("没有钻石资源点");
        }
        if (map.getDrops(MapData.DropType.EMERALD) == 0) {
            warnings.add("没有绿宝石资源点");
        }
        if (map.getShops(MapData.ShopType.ITEM) == 0) {
            warnings.add("没有物品商店");
        }
        if (map.getShops(MapData.ShopType.UPGRADE) == 0) {
            warnings.add("没有团队升级商店");
        }
        if (!Bukkit.getWorlds().isEmpty() && Bukkit.getWorlds().get(0).getName().equals(map.getName())) {
            errors.add("地图名不能与主世界 " + map.getName() + " 同名");
        }
        if (map.getAuthor() == null) {
            warnings.add("未设置作者 /map setAuthor");
        }
        if (region.getPos1() == null || region.getPos2() == null) {
            return;
        }

        String world = region.getPos1().getWorld();
        if (!world.equals(region.getPos2().getWorld())) {
            errors.add("边界 1 和边界 2 不在同一个世界");
            return;
        }
        checkInRegion(map, "观战重生点", map.getRespawnLocation());
        for (int i = 0; i < map.getBases().size(); i++) {
            checkInRegion(map, "基地 #" + (i + 1), map.getBases().get(i));
        }
        for (MapData.DropRawLocation drop : map.getDrops()) {
            checkInRegion(map, drop.getDropType() + " 资源点", drop);
        }
        for (MapData.ShopRawLocation shop : map.getShops()) {
            checkInRegion(map, shop.getShopType() + " 商店", shop);
        }
    }

    private void checkInRegion(MapData map, String name, MapData.RawLocation point) {
        if (point == null) {
            return;
        }
        if (!point.getWorld().equals(map.getRegion().getPos1().getWorld())) {
            errors.add(name + " 在世界 " + point.getWorld() + " 不在边界所在的世界 " + map.getRegion().getPos1().getWorld());
            return;
        }
        Location location = new Location(null, point.getX(), point.getY(), point.getZ());
        if (map.isOutsideRegion(location)) {
            warnings.add(name + " 在地图边界外 玩家在边界外无法放置方块");
        }
    }

    private void checkPlayers(MapData map) {
        int maxPlayers = map.getMaxPlayers();
        if (map.getBases().size() >= 2 && map.getPlayers().getMin() > maxPlayers) {
            errors.add("最少人数 " + map.getPlayers().getMin() + " 超过地图容量 " + maxPlayers + " 游戏永远无法开始");
        }
    }

    /**
     * @return null when the folder can be copied into the game world
     */
    public static String findWorldFolderError(MapData map) {
        File folder = map.getWorldFolder();
        if (folder == null) {
            return "未设置地图文件夹 /map setUrl";
        }
        if (!new File(folder, "level.dat").isFile()) {
            return "地图文件夹不是一个世界存档 找不到 " + new File(folder, "level.dat").getPath();
        }
        try {
            File gameWorld = new File(Bukkit.getWorldContainer(), map.getName()).getCanonicalFile();
            if (folder.getCanonicalFile().equals(gameWorld)) {
                return "地图文件夹就是游戏世界目录 " + gameWorld.getPath() + " 开局复制时会被清空 请把地图放到其它目录";
            }
        } catch (IOException e) {
            return "无法解析地图文件夹 " + folder.getPath() + " " + e.getMessage();
        }
        return null;
    }

    private void checkBlocks(MapData map) {
        SettingsConfig settings = AzuraBedWars.getInstance().getSettingsConfig();
        List<TeamColor> colors = new ArrayList<>();
        List<Location> baseDrops = new ArrayList<>();
        List<Block> firstBeds = new ArrayList<>();
        for (int i = 0; i < map.getBases().size(); i++) {
            Location base = map.getBases().get(i).toLocation();
            if (base.getWorld() == null) {
                continue;
            }
            String name = "基地 #" + (i + 1);

            TeamColor color = MapData.findWoolColor(base, settings.getTeamBlockSearchRadius());
            if (color == null) {
                warnings.add(name + " 附近 " + settings.getTeamBlockSearchRadius() + " 格内没有羊毛 将自动分配颜色");
            } else if (colors.contains(color)) {
                warnings.add(name + " 与其它基地都识别为 " + color.name() + " 将自动换一个颜色");
            }
            colors.add(color);

            List<Block> beds = findBedBlocks(base, settings.getBedSearchRadius());
            if (beds.isEmpty()) {
                errors.add(name + " 附近 " + settings.getBedSearchRadius() + " 格内没有床");
            } else if (firstBeds.contains(beds.get(0))) {
                errors.add(name + " 与其它基地识别到同一张床");
            } else if (beds.size() > 2) {
                warnings.add(name + " 附近有不止一张床 可能识别到别队的床");
            }
            firstBeds.add(beds.isEmpty() ? null : beds.get(0));

            Location drop = map.getClosestDrop(base, MapData.DropType.BASE);
            if (drop != null && baseDrops.contains(drop)) {
                warnings.add(name + " 与其它基地共用同一个基地资源点");
            }
            baseDrops.add(drop);

            baseLines.add(name + " 羊毛 " + (color == null ? "无" : color.name()) + " 床方块 " + beds.size()
                    + " 资源点距离 " + (drop == null ? "无" : String.format("%.1f", drop.distance(base))));
        }
    }

    /** Same scan order as GameTeamBedHandler so the first block is the bed the team gets */
    private static List<Block> findBedBlocks(Location center, int radius) {
        World world = center.getWorld();
        List<Block> beds = new ArrayList<>();
        for (int x = -radius; x < radius; x++) {
            for (int y = -radius; y < radius; y++) {
                for (int z = -radius; z < radius; z++) {
                    Block block = world.getBlockAt(center.getBlockX() + x, center.getBlockY() + y, center.getBlockZ() + z);
                    if (MapUtil.isBed(block.getType())) {
                        beds.add(block);
                    }
                }
            }
        }
        return beds;
    }
}
