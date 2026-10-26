package cc.azuramc.bedwars.game.map;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.GameplayConfig;
import cc.azuramc.bedwars.config.object.LangConfig;
import cc.azuramc.bedwars.game.TeamColor;
import cc.azuramc.bedwars.util.MapUtil;
import cc.azuramc.bedwars.util.MessageUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
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
            check.warnings.add(MessageUtil.fill(lang().getWorldNotLoaded(), "world", pos1.getWorld()));
        }
        return check;
    }

    private void checkLocations(MapData map) {
        MapData.Region region = map.getRegion();
        if (map.getWaitingLocation() == null) {
            errors.add(lang().getNoWaiting());
        }
        if (map.getRespawnLocation() == null) {
            errors.add(lang().getNoRespawn());
        }
        if (region.getPos1() == null || region.getPos2() == null) {
            errors.add(lang().getNoRegion());
        }
        if (map.getBases().size() < 2) {
            errors.add(MessageUtil.fill(lang().getTooFewBases(), "count", map.getBases().size()));
        }
        if (map.getBases().size() > TeamColor.values().length) {
            errors.add(MessageUtil.fill(lang().getTooManyBases(), "max", TeamColor.values().length, "count", map.getBases().size()));
        }
        if (map.getDrops(MapData.DropType.BASE) == 0) {
            errors.add(lang().getNoBaseDrop());
        }
        if (map.getDrops(MapData.DropType.DIAMOND) == 0) {
            warnings.add(lang().getNoDiamondDrop());
        }
        if (map.getDrops(MapData.DropType.EMERALD) == 0) {
            warnings.add(lang().getNoEmeraldDrop());
        }
        if (map.getShops(MapData.ShopType.ITEM) == 0) {
            warnings.add(lang().getNoItemShop());
        }
        if (map.getShops(MapData.ShopType.UPGRADE) == 0) {
            warnings.add(lang().getNoUpgradeShop());
        }
        if (!Bukkit.getWorlds().isEmpty() && Bukkit.getWorlds().get(0).getName().equals(map.getName())) {
            errors.add(MessageUtil.fill(lang().getSameAsMainWorld(), "map", map.getName()));
        }
        if (map.getAuthor() == null) {
            warnings.add(lang().getNoAuthor());
        }
        if (region.getPos1() == null || region.getPos2() == null) {
            return;
        }

        String world = region.getPos1().getWorld();
        if (!world.equals(region.getPos2().getWorld())) {
            errors.add(lang().getRegionWorlds());
            return;
        }
        checkInRegion(map, lang().getRespawn(), map.getRespawnLocation());
        for (int i = 0; i < map.getBases().size(); i++) {
            checkInRegion(map, MessageUtil.fill(lang().getBase(), "number", i + 1), map.getBases().get(i));
        }
        for (MapData.DropRawLocation drop : map.getDrops()) {
            checkInRegion(map, MessageUtil.fill(lang().getDrop(), "type", drop.getDropType()), drop);
        }
        for (MapData.ShopRawLocation shop : map.getShops()) {
            checkInRegion(map, MessageUtil.fill(lang().getShop(), "type", shop.getShopType()), shop);
        }
    }

    private void checkInRegion(MapData map, String name, MapData.RawLocation point) {
        if (point == null) {
            return;
        }
        if (!point.getWorld().equals(map.getRegion().getPos1().getWorld())) {
            errors.add(MessageUtil.fill(lang().getOtherWorld(), "name", name, "world", point.getWorld(), "region-world", map.getRegion().getPos1().getWorld()));
            return;
        }
        Location location = new Location(null, point.getX(), point.getY(), point.getZ());
        if (map.isOutsideRegion(location)) {
            warnings.add(MessageUtil.fill(lang().getOutsideRegion(), "name", name));
        }
    }

    private void checkPlayers(MapData map) {
        int maxPlayers = map.getMaxPlayers();
        if (map.getBases().size() >= 2 && map.getPlayers().getMin() > maxPlayers) {
            errors.add(MessageUtil.fill(lang().getMinAboveCapacity(), "min", map.getPlayers().getMin(), "capacity", maxPlayers));
        }
    }

    /**
     * @return null when the folder can be copied into the game world
     */
    public static String findWorldFolderError(MapData map) {
        File folder = map.getWorldFolder();
        if (folder == null) {
            return lang().getNoFolder();
        }
        if (!new File(folder, "level.dat").isFile()) {
            return MessageUtil.fill(lang().getNotAWorld(), "file", new File(folder, "level.dat").getPath());
        }
        try {
            File gameWorld = new File(Bukkit.getWorldContainer(), map.getName()).getCanonicalFile();
            if (folder.getCanonicalFile().equals(gameWorld)) {
                return MessageUtil.fill(lang().getFolderIsGameWorld(), "folder", gameWorld.getPath());
            }
        } catch (IOException e) {
            return MessageUtil.fill(lang().getFolderUnreadable(), "folder", folder.getPath(), "error", e.getMessage());
        }
        return null;
    }

    private void checkBlocks(MapData map) {
        GameplayConfig gameplay = AzuraBedWars.getInstance().getGameplayConfig();
        List<TeamColor> colors = new ArrayList<>();
        List<Location> baseDrops = new ArrayList<>();
        List<Block> claimedBeds = new ArrayList<>();
        for (int i = 0; i < map.getBases().size(); i++) {
            Location base = map.getBases().get(i).toLocation();
            if (base.getWorld() == null) {
                continue;
            }
            String name = MessageUtil.fill(lang().getBase(), "number", i + 1);

            TeamColor color = MapData.findWoolColor(base, gameplay.getTeamBlockSearchRadius());
            if (color == null) {
                warnings.add(MessageUtil.fill(lang().getNoWool(), "name", name, "radius", gameplay.getTeamBlockSearchRadius()));
            } else if (colors.contains(color)) {
                warnings.add(MessageUtil.fill(lang().getSameColor(), "name", name, "color", color.name()));
            }
            colors.add(color);

            List<Block> beds = findBedBlocks(base, gameplay.getBedSearchRadius());
            List<Block> teamBed = pickTeamBed(base, beds);
            if (teamBed.isEmpty()) {
                errors.add(MessageUtil.fill(lang().getNoBed(), "name", name, "radius", gameplay.getBedSearchRadius()));
            } else if (teamBed.stream().anyMatch(claimedBeds::contains)) {
                errors.add(MessageUtil.fill(lang().getSharedBed(), "name", name));
            } else if (beds.size() > 2) {
                warnings.add(MessageUtil.fill(lang().getSeveralBeds(), "name", name));
            }
            claimedBeds.addAll(teamBed);

            Location drop = map.getClosestDrop(base, MapData.DropType.BASE);
            if (drop != null && baseDrops.contains(drop)) {
                warnings.add(MessageUtil.fill(lang().getSharedDrop(), "name", name));
            }
            baseDrops.add(drop);

            baseLines.add(MessageUtil.fill(lang().getBaseLine(), "name", name, "color", color == null ? lang().getNone() : color.name(),
                    "beds", beds.size(), "distance", drop == null ? lang().getNone() : String.format("%.1f", drop.distance(base))));
        }
    }

    private static LangConfig.MapCheck lang() {
        return AzuraBedWars.getInstance().getLang().getMapCheck();
    }

    private static List<Block> findBedBlocks(Location center, int radius) {
        World world = center.getWorld();
        List<Block> beds = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = world.getBlockAt(center.getBlockX() + x, center.getBlockY() + y, center.getBlockZ() + z);
                    if (MapUtil.isBed(block.getType())) {
                        beds.add(block);
                    }
                }
            }
        }
        return beds;
    }

    /** Must match the bed choice in GameTeamBedHandler */
    private static List<Block> pickTeamBed(Location spawn, List<Block> beds) {
        Block spawnBlock = spawn.getBlock();
        Block nearest = beds.stream()
                .min(Comparator.comparingDouble(bed -> bed.getLocation().distanceSquared(spawnBlock.getLocation())))
                .orElse(null);
        List<Block> teamBed = new ArrayList<>();
        if (nearest == null) {
            return teamBed;
        }
        teamBed.add(nearest);
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
            Block other = nearest.getRelative(face);
            if (MapUtil.isBed(other.getType())) {
                teamBed.add(other);
                break;
            }
        }
        return teamBed;
    }
}
