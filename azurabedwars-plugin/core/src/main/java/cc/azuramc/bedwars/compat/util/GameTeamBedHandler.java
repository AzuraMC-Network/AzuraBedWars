package cc.azuramc.bedwars.compat.util;

import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.game.GameTeam;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;

import java.util.ArrayList;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
public class GameTeamBedHandler {

    private GameTeam gameTeam;
    /**
     * 搜索床的范围
     */
    private static final int BED_SEARCH_RADIUS = GameTeam.BED_SEARCH_RADIUS;
    /**
     * 默认床朝向
     */
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

    public static void destroyBed(GameTeam gameTeam) {
        try {
            if (gameTeam.getBedHead() != null && XMaterial.AIR.get() != null) {
                gameTeam.getBedHead().setType(XMaterial.AIR.get());
            }

            if (gameTeam.getBedFeet() != null && XMaterial.AIR.get() != null) {
                gameTeam.getBedFeet().setType(XMaterial.AIR.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 确定床的头部和脚部，以及朝向
     *
     * @param block1 第一个床方块
     * @param block2 第二个床方块
     */
    private void determineBedParts(Block block1, Block block2) {
        if (!VersionUtil.isLessThan(1, 14)) {
            determineBedsForNewVersions(block1, block2);
        } else {
            determineBedsForOldVersions(block1, block2);
        }
    }

    private void determineBedsForNewVersions(Block block1, Block block2) {
        BlockData bedData1 = block1.getBlockData();
        BlockData bedData2 = block2.getBlockData();

        // 检查是否为床类型
        if (!(bedData1 instanceof Bed bed1) || !(bedData2 instanceof Bed bed2)) {
            throw new IllegalArgumentException("Block is not a bed");
        }

        if (bed1.getPart() == Bed.Part.HEAD) {
            gameTeam.setBedHead(block1);
            gameTeam.setBedFeet(block2);
            gameTeam.setBedFace(bed1.getFacing());
        } else {
            gameTeam.setBedFeet(block1);
            gameTeam.setBedHead(block2);
            gameTeam.setBedFace(bed2.getFacing());
        }
    }

    /**
     * 当找不到床时设置默认值
     */
    private void setDefaultBedValues() {
        gameTeam.setBedHead(gameTeam.getSpawnLocation().getBlock());
        gameTeam.setBedFeet(gameTeam.getSpawnLocation().getBlock());
        gameTeam.setBedFace(DEFAULT_BED_FACE);
    }

    @SuppressWarnings("deprecation")
    private void determineBedsForOldVersions(Block block1, Block block2) {
        org.bukkit.material.Bed bed1 = (org.bukkit.material.Bed) block1.getState().getData();
        org.bukkit.material.Bed bed2 = (org.bukkit.material.Bed) block2.getState().getData();

        if (bed1.isHeadOfBed()) {
            gameTeam.setBedHead(block1);
            gameTeam.setBedFeet(block2);
            gameTeam.setBedFace(bed1.getFacing());
        } else {
            gameTeam.setBedHead(block2);
            gameTeam.setBedFeet(block1);
            gameTeam.setBedFace(bed2.getFacing());
        }
    }

    /**
     * 判断方块是否为床方块
     *
     * @param block 待检查的方块
     * @return 如果是床方块返回true，否则返回false
     */
    private boolean isBedBlock(Block block) {
        return block.getType().name().toUpperCase().contains("BED");
    }

}
