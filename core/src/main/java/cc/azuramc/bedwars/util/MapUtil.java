package cc.azuramc.bedwars.util;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.game.team.GameTeam;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.lang.reflect.Method;

/**
 * @author an5w1r@163.com
 */
public class MapUtil {

    private static final int TEAM_SPAWN_PROTECTION_RADIUS = 8;
    private static final int RESOURCE_SPAWN_PROTECTION_RADIUS = 5;
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
            if (gameTeam.getSpawnLocation().distance(block.getLocation()) <= TEAM_SPAWN_PROTECTION_RADIUS) {
                return true;
            }
        }

        // 检查地图区域保护
        if (GAME_MANAGER.getMapData().hasRegion(block.getLocation())) {
            return true;
        }

        // 检查钻石资源点保护
        for (Location location : GAME_MANAGER.getMapData().getDropLocations(MapData.DropType.DIAMOND)) {
            if (location.distance(block.getLocation()) <= RESOURCE_SPAWN_PROTECTION_RADIUS) {
                return true;
            }
        }

        // 检查绿宝石资源点保护
        for (Location location : GAME_MANAGER.getMapData().getDropLocations(MapData.DropType.EMERALD)) {
            if (location.distance(block.getLocation()) <= RESOURCE_SPAWN_PROTECTION_RADIUS) {
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
            if (gameTeam.getSpawnLocation().distance(location) <= TEAM_SPAWN_PROTECTION_RADIUS) {
                return true;
            }
        }

        // 检查资源点保护
        for (MapData.RawLocation rawLocation : GAME_MANAGER.getMapData().getDrops()) {
            if (rawLocation.toLocation().distance(location) <= RESOURCE_SPAWN_PROTECTION_RADIUS) {
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
     * 设置方块的数据值（跨版本兼容）
     * 兼容从1.8到最新版本的Minecraft服务器
     *
     * @param block 需要设置的方块
     * @param data 数据值
     */
    public static void setBlockData(Block block, byte data) {
        try {
            if (VersionUtil.isLessThan113()) {
                // 1.8 - 1.12版本使用反射调用setData方法
                Method setDataMethod = Block.class.getMethod("setData", byte.class);
                setDataMethod.invoke(block, data);
            } else {
                // 1.13+ 版本不使用 BlockData API，改为使用回退方法
                fallbackSetBlockData(block, data);
            }
        } catch (Exception e) {
            LoggerUtil.warn("无法设置方块数据: " + e.getMessage());
            if (Bukkit.getPluginManager().isPluginEnabled("AzuraBedWars")) {
                LoggerUtil.info("尝试使用替代方法设置方块数据...");
                try {
                    // 尝试使用替代方法
                    fallbackSetBlockData(block, data);
                } catch (Exception ex) {
                    LoggerUtil.warn("替代方法也失败: " + ex.getMessage());
                }
            }
        }
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

    /**
     * 根据数据值获取对应的颜色名称
     *
     * @param data 颜色数据值
     * @return 颜色名称
     */
    private static String getColorNameFromData(byte data) {
        return switch (data & 0xF) { // 只使用低4位
            case 0 -> "WHITE";
            case 1 -> "ORANGE";
            case 2 -> "MAGENTA";
            case 3 -> "LIGHT_BLUE";
            case 4 -> "YELLOW";
            case 5 -> "LIME";
            case 6 -> "PINK";
            case 7 -> "GRAY";
            case 8 -> "SILVER";
            case 9 -> "CYAN";
            case 10 -> "PURPLE";
            case 11 -> "BLUE";
            case 12 -> "BROWN";
            case 13 -> "GREEN";
            case 14 -> "RED";
            case 15 -> "BLACK";
            default -> "WHITE";
        };
    }

    /**
     * 回退方法：尝试使用其他方式设置方块数据
     *
     * @param block 需要设置的方块
     * @param data 数据值
     */
    private static void fallbackSetBlockData(Block block, byte data) {
        // 尝试使用NMS或其他方法
        try {
            // 对于羊毛等染色方块，我们可以直接替换为对应颜色的方块
            Material type = block.getType();
            if (type.name().toUpperCase().contains("WOOL")) {
                String colorName = getColorNameFromData(data);
                Material newType = Material.getMaterial(colorName + "_WOOL");
                if (newType != null) {
                    block.setType(newType);
                    return;
                }
            }

            // 最后的回退方案：重新放置相同类型的方块
            // 这不会保留数据值，但至少保证方块类型正确
            if (block.getType() != Material.AIR) {
                Material currentType = block.getType();
                block.setType(currentType);
            }

        } catch (Exception e) {
            LoggerUtil.warn("回退设置方块数据方法失败: " + e.getMessage());
        }
    }
}
