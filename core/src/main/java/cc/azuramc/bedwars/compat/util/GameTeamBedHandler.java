package cc.azuramc.bedwars.compat.util;

import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.game.GameTeam;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author An5w1r@163.com
 */
public class GameTeamBedHandler {

    private GameTeam gameTeam;
    /** 搜索床的范围 */
    private static final int BED_SEARCH_RADIUS = GameTeam.BED_SEARCH_RADIUS;
    /** 默认床朝向 */
    private static final BlockFace DEFAULT_BED_FACE = GameTeam.DEFAULT_BED_FACE;

    /**
     * 初始化床相关字段
     */
    public GameTeamBedHandler(GameTeam gameTeam) {
        this.gameTeam = gameTeam;

        // 查找床方块
        List<Block> bedBlocks = findBedBlocks();

        if (bedBlocks.size() >= 2) {
            determineBedParts(bedBlocks.get(0), bedBlocks.get(1));
        } else {
            // 如果找不到足够的床方块，初始化为默认值
            setDefaultBedValues();
        }
    }

    /**
     * 查找团队出生点附近的床方块
     *
     * @return 床方块列表
     */
    private List<Block> findBedBlocks() {
        List<Block> bedBlocks = new ArrayList<>();

        for (int x = -BED_SEARCH_RADIUS; x < BED_SEARCH_RADIUS; x++) {
            for (int y = -BED_SEARCH_RADIUS; y < BED_SEARCH_RADIUS; y++) {
                for (int z = -BED_SEARCH_RADIUS; z < BED_SEARCH_RADIUS; z++) {
                    Block block = gameTeam.getSpawnLocation().clone().add(x, y, z).getBlock();
                    if (isBedBlock(block)) {
                        bedBlocks.add(block);

                        // 只需要找到2个床方块即可返回
                        if (bedBlocks.size() >= 2) {
                            return bedBlocks;
                        }
                    }
                }
            }
        }

        return bedBlocks;
    }

    /**
     * 确定床的头部和脚部，以及朝向
     *
     * @param block1 第一个床方块
     * @param block2 第二个床方块
     */
    private void determineBedParts(Block block1, Block block2) {
        if (!VersionUtil.isLessThan113()) {
            determineBedsForNewVersions(block1, block2);
        } else {
            determineBedsForOldVersions(block1, block2);
        }
    }

    /**
     * 为1.13+版本确定床的头部和脚部
     *
     * @param block1 第一个床方块
     * @param block2 第二个床方块
     */
    private void determineBedsForNewVersions(Block block1, Block block2) {
        try {
            // 使用反射获取方法和类，而不是直接引用BlockData API
            Method getBlockDataMethod = Block.class.getMethod("getBlockData");
            Object bedData1 = getBlockDataMethod.invoke(block1);
            Object bedData2 = getBlockDataMethod.invoke(block2);

            // 如果不是Bed类型，则抛出异常以便回退到备用方法
            if (!"org.bukkit.block.data.type.Bed".equals(bedData1.getClass().getName())) {
                throw new IllegalArgumentException("Block is not a bed");
            }

            // 获取Part枚举和getPart方法
            Class<?> bedClass = Class.forName("org.bukkit.block.data.type.Bed");
            Method getPartMethod = bedClass.getMethod("getPart");
            Method getFacingMethod = bedClass.getMethod("getFacing");

            // 获取Part枚举类
            Class<?> partEnum = Class.forName("org.bukkit.block.data.type.Bed$Part");
            Object headPart = null;

            // 获取HEAD枚举值
            for (Object enumConstant : partEnum.getEnumConstants()) {
                if ("HEAD".equals(enumConstant.toString())) {
                    headPart = enumConstant;
                    break;
                }
            }

            // 获取床部件类型和朝向
            Object part1 = getPartMethod.invoke(bedData1);
            Object part2 = getPartMethod.invoke(bedData2);

            if (part1.equals(headPart)) {
                gameTeam.setBedHead(block1);
                gameTeam.setBedFeet(block2);
                gameTeam.setBedFace((BlockFace) getFacingMethod.invoke(bedData1));
            } else {
                gameTeam.setBedFeet(block1);
                gameTeam.setBedHead(block2);
                gameTeam.setBedFace((BlockFace) getFacingMethod.invoke(bedData2));
            }
        } catch (Exception e) {
            // 如果反射失败或出错，使用默认值
            setFallbackBedValues(block1, block2);
        }
    }

    /**
     * 为1.8-1.12版本确定床的头部和脚部
     *
     * @param block1 第一个床方块
     * @param block2 第二个床方块
     */
    @SuppressWarnings("deprecation")
    private void determineBedsForOldVersions(Block block1, Block block2) {
        // 首先尝试使用反射方法安全获取Bed数据
        try {
            // 获取方块状态和数据
            BlockState state1 = block1.getState();
            BlockState state2 = block2.getState();

            // 检查是否可以获取MaterialData
            if (state1.getData() instanceof org.bukkit.material.Bed bedData1 &&
                    state2.getData() instanceof org.bukkit.material.Bed bedData2) {

                if (bedData1.isHeadOfBed()) {
                    gameTeam.setBedHead(block1);
                    gameTeam.setBedFeet(block2);
                    gameTeam.setBedFace(bedData1.getFacing());
                    return;
                } else if (bedData2.isHeadOfBed()) {
                    gameTeam.setBedFeet(block1);
                    gameTeam.setBedHead(block2);
                    gameTeam.setBedFace(bedData2.getFacing());
                    return;
                }
            }

            // 如果上述方法无法确定床头床脚，尝试使用方块类型名称判断
            String type1 = block1.getType().name();
            String type2 = block2.getType().name();

            // 某些版本使用BED_BLOCK，有些版本会在名称中包含HEAD或FOOT
            if (type1.contains("HEAD") || state1.getData().toString().contains("HEAD")) {
                gameTeam.setBedHead(block1);
                gameTeam.setBedFeet(block2);
                // 尝试从方块朝向判断床的朝向
                gameTeam.setBedFace(getBedFacingFromBlock(block1));
                return;
            } else if (type2.contains("HEAD") || state2.getData().toString().contains("HEAD")) {
                gameTeam.setBedHead(block2);
                gameTeam.setBedFeet(block1);
                gameTeam.setBedFace(getBedFacingFromBlock(block2));
                return;
            }

            // 如果依然无法确定，基于方块的相对位置进行猜测
            if (areBlocksAligned(block1, block2)) {
                // 简单赋值，保证有值
                gameTeam.setBedHead(block1);
                gameTeam.setBedFeet(block2);
                gameTeam.setBedFace(getEstimatedFacing(block1, block2));
                return;
            }

            // 所有方法都失败，使用默认值
            setFallbackBedValues(block1, block2);
        } catch (Exception e) {
            // 如果出错，使用默认值
            setFallbackBedValues(block1, block2);
        }
    }

    /**
     * 从方块状态尝试获取床的朝向
     *
     * @param block 床方块
     * @return 床的朝向，如果无法确定则返回默认朝向
     */
    private BlockFace getBedFacingFromBlock(Block block) {
        try {
            // 尝试从方块数据中获取朝向信息
            BlockState state = block.getState();
            if (state.getData() instanceof org.bukkit.material.Directional) {
                return ((org.bukkit.material.Directional) state.getData()).getFacing();
            }

            // 尝试使用9x和8x服务器的朝向数据
            byte data = block.getData();
            // 0: 头朝南, 1: 头朝西, 2: 头朝北, 3: 头朝东
            return switch (data & 0x3) {
                case 0 -> BlockFace.SOUTH;
                case 1 -> BlockFace.WEST;
                case 2 -> BlockFace.NORTH;
                case 3 -> BlockFace.EAST;
                default -> DEFAULT_BED_FACE;
            };
        } catch (Exception e) {
            return DEFAULT_BED_FACE;
        }
    }

    /**
     * 检查两个方块是否在同一水平线上
     *
     * @param block1 第一个方块
     * @param block2 第二个方块
     * @return 如果两个方块在同一水平线上返回true
     */
    private boolean areBlocksAligned(Block block1, Block block2) {
        return block1.getY() == block2.getY() &&
                (Math.abs(block1.getX() - block2.getX()) == 1 && block1.getZ() == block2.getZ() ||
                        Math.abs(block1.getZ() - block2.getZ()) == 1 && block1.getX() == block2.getX());
    }

    /**
     * 根据两个方块的相对位置估计床的朝向
     *
     * @param block1 第一个方块
     * @param block2 第二个方块
     * @return 估计的床朝向
     */
    private BlockFace getEstimatedFacing(Block block1, Block block2) {
        if (block1.getX() < block2.getX()) {
            return BlockFace.EAST;
        }
        if (block1.getX() > block2.getX()) {
            return BlockFace.WEST;
        }
        if (block1.getZ() < block2.getZ()) {
            return BlockFace.SOUTH;
        }
        if (block1.getZ() > block2.getZ()) {
            return BlockFace.NORTH;
        }
        return DEFAULT_BED_FACE;
    }

    /**
     * 当无法确定床头床脚时的备用方法
     *
     * @param block1 第一个床方块
     * @param block2 第二个床方块
     */
    private void setFallbackBedValues(Block block1, Block block2) {
        gameTeam.setBedHead(block1);
        gameTeam.setBedFeet(block2);
        gameTeam.setBedFace(DEFAULT_BED_FACE);
    }

    /**
     * 当找不到床时设置默认值
     */
    private void setDefaultBedValues() {
        gameTeam.setBedHead(gameTeam.getSpawnLocation().getBlock());
        gameTeam.setBedFeet(gameTeam.getSpawnLocation().getBlock());
        gameTeam.setBedFace(DEFAULT_BED_FACE);
    }

    /**
     * 判断方块是否为床方块，兼容新旧版本
     *
     * @param block 待检查的方块
     * @return 如果是床方块返回true，否则返回false
     */
    private boolean isBedBlock(Block block) {
        return block.getType().name().toUpperCase().contains("BED");
    }

}
