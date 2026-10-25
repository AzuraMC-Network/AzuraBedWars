package cc.azuramc.bedwars.util;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.GameplayConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.map.MapData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 * @author an5w1r@163.com
 */
public class MapUtil {

    /** Looked up per call since map checks load this class before the game exists */
    private static GameManager gameManager() {
        return AzuraBedWars.getInstance().getGameManager();
    }

    /**
     * 检查相对位置是否受保护
     *
     * @param block 方块
     * @return 如果位置受保护返回true，否则返回false
     */
    public static boolean isProtectedRelativeLocation(Block block) {
        // 检查团队出生点保护
        for (GameTeam gameTeam : gameManager().getGameTeams()) {
            if (gameTeam.getSpawnLocation().distance(block.getLocation()) <= gameplay().getTeamSpawnProtectionRadius()) {
                return true;
            }
        }

        // 检查地图区域保护
        if (gameManager().getMapData().isOutsideRegion(block.getLocation())) {
            return true;
        }

        // 检查钻石资源点保护
        for (Location location : gameManager().getMapData().getDropLocations(MapData.DropType.DIAMOND)) {
            if (location.distance(block.getLocation()) <= gameplay().getResourceSpawnProtectionRadius()) {
                return true;
            }
        }

        // 检查绿宝石资源点保护
        for (Location location : gameManager().getMapData().getDropLocations(MapData.DropType.EMERALD)) {
            if (location.distance(block.getLocation()) <= gameplay().getResourceSpawnProtectionRadius()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查区域是否受保护
     *
     * @param location 位置
     * @return 如果区域受保护返回true，否则返回false
     */
    public static boolean isProtectedArea(Location location) {
        // 检查地图区域保护
        if (gameManager().getMapData().isOutsideRegion(location)) {
            return true;
        }

        // 检查团队出生点保护
        for (GameTeam gameTeam : gameManager().getGameTeams()) {
            if (gameTeam.getSpawnLocation().distance(location) <= gameplay().getTeamSpawnProtectionRadius()) {
                return true;
            }
        }

        // 检查资源点保护
        for (MapData.RawLocation rawLocation : gameManager().getMapData().getDrops()) {
            if (rawLocation.toLocation().distance(location) <= gameplay().getResourceSpawnProtectionRadius()) {
                return true;
            }
        }

        // 商人位置保护
        for (MapData.RawLocation rawLocation : gameManager().getMapData().getShops()) {
            if (rawLocation.toLocation().distance(location) <= 3) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查方块类型是否受保护不受爆炸影响
     *
     * @param block 方块
     * @return 如果方块受保护返回true，否则返回false
     */
    public static boolean isProtectedBlockType(Block block) {
        return isStainedGlass(block) || isBedBlock(block);
    }

    /**
     * 检查方块是否为床方块
     * 兼容全版本Minecraft
     *
     * @param block 需要检查的方块
     * @return 如果是床方块返回true，否则返回false
     */
    public static boolean isBedBlock(Block block) {
        return isBed(block.getType());
    }

    /** BED and BED_BLOCK before 1.13, colored *_BED after */
    public static boolean isBed(Material material) {
        String name = material.name();
        return name.endsWith("_BED") || name.equals("BED") || name.equals("BED_BLOCK");
    }

    /**
     * 检查方块是否为染色玻璃
     *
     * @param block 需要检查的方块
     * @return 如果是染色玻璃返回true，否则返回false
     */
    private static boolean isStainedGlass(Block block) {
        return block.getType().name().contains("STAINED_GLASS");
    }

    private static GameplayConfig gameplay() {
        return AzuraBedWars.getInstance().getGameplayConfig();
    }
}
