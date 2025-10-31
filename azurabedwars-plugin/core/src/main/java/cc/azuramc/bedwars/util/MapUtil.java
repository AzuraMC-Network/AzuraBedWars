package cc.azuramc.bedwars.util;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.SettingsConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.map.MapData;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 * @author an5w1r@163.com
 */
public class MapUtil {

    private static final SettingsConfig settingsConfig = AzuraBedWars.getInstance().getSettingsConfig();

    private static final GameManager GAME_MANAGER = AzuraBedWars.getInstance().getGameManager();

    /**
     * 检查相对位置是否受保护
     *
     * @param block 方块
     * @return 如果位置受保护返回true，否则返回false
     */
    public static boolean isProtectedRelativeLocation(Block block) {
        // 检查团队出生点保护
        for (GameTeam gameTeam : GAME_MANAGER.getGameTeams()) {
            if (gameTeam.getSpawnLocation().distance(block.getLocation()) <= settingsConfig.getTeamSpawnProtectionRadius()) {
                return true;
            }
        }

        // 检查地图区域保护
        if (GAME_MANAGER.getMapData().hasRegion(block.getLocation())) {
            return true;
        }

        // 检查钻石资源点保护
        for (Location location : GAME_MANAGER.getMapData().getDropLocations(MapData.DropType.DIAMOND)) {
            if (location.distance(block.getLocation()) <= settingsConfig.getResourceSpawnProtectionRadius()) {
                return true;
            }
        }

        // 检查绿宝石资源点保护
        for (Location location : GAME_MANAGER.getMapData().getDropLocations(MapData.DropType.EMERALD)) {
            if (location.distance(block.getLocation()) <= settingsConfig.getResourceSpawnProtectionRadius()) {
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
        if (GAME_MANAGER.getMapData().hasRegion(location)) {
            return true;
        }

        // 检查团队出生点保护
        for (GameTeam gameTeam : GAME_MANAGER.getGameTeams()) {
            if (gameTeam.getSpawnLocation().distance(location) <= settingsConfig.getTeamSpawnProtectionRadius()) {
                return true;
            }
        }

        // 检查资源点保护
        for (MapData.RawLocation rawLocation : GAME_MANAGER.getMapData().getDrops()) {
            if (rawLocation.toLocation().distance(location) <= settingsConfig.getResourceSpawnProtectionRadius()) {
                return true;
            }
        }

        // 商人位置保护
        for (MapData.RawLocation rawLocation : GAME_MANAGER.getMapData().getShops()) {
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
        String typeName = block.getType().name().toUpperCase();
        return typeName.contains("BED");
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

    /**
     * 将旧版本的数据值转换为BlockFace方向
     *
     * @param data 旧版本的数据值
     * @return 对应的BlockFace方向，如果无法转换则返回null
     */
    private static BlockFace convertDataToBlockFace(byte data) {
        // 此映射基于大多数方向性方块的通用规则，可能需要根据特定方块调整
        return switch (data & 0x7) { // 只使用低3位
            case 0 -> BlockFace.DOWN;
            case 1 -> BlockFace.UP;
            case 2 -> BlockFace.NORTH;
            case 3 -> BlockFace.SOUTH;
            case 4 -> BlockFace.WEST;
            case 5 -> BlockFace.EAST;
            default -> null;
        };
    }
}
