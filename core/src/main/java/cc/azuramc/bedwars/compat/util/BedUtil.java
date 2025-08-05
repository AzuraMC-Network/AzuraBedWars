package cc.azuramc.bedwars.compat.util;

import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.lang.reflect.Method;

/**
 * @author an5w1r@163.com
 */
public class BedUtil {

    /**
     * 安全销毁床方块，适用于所有版本
     */
    public static void destroyBed(GameTeam gameTeam) {
        try {
            // 设置床头和床脚方块为AIR
            if (gameTeam.getBedHead() != null && XMaterial.AIR.get() != null) {
                gameTeam.getBedHead().setType(XMaterial.AIR.get());
            }

            if (gameTeam.getBedFeet() != null && XMaterial.AIR.get() != null) {
                gameTeam.getBedFeet().setType(XMaterial.AIR.get());
            }
        } catch (Exception e) {
            LoggerUtil.warn("销毁床时出现异常: " + e.getMessage());
            try {
                // 反射调用
                setBlockTypeUsingReflection(gameTeam.getBedHead());
                setBlockTypeUsingReflection(gameTeam.getBedFeet());
            } catch (Exception ex) {
                LoggerUtil.error("无法销毁床: " + ex.getMessage());
            }
        }
    }

    /**
     * 使用反射调用旧版本的setTypeId方法设置方块为AIR
     */
    public static void setBlockTypeUsingReflection(Block block) {
        if (block == null) {
            return;
        }

        try {
            Method setTypeIdMethod = Block.class.getMethod("setTypeId", int.class);
            // AIR ID = 0
            setTypeIdMethod.invoke(block, 0);
        } catch (Exception e) {
            try {
                if (XMaterial.AIR.get() != null) {
                    block.setType(XMaterial.AIR.get());
                }
            } catch (Exception ignored) {
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static void dropTargetBlock(Block targetBlock) {
        if (isBedBlock(targetBlock)) {
            Block bedHead;
            Block bedFeet;

            try {
                // 使用兼容性代码处理床方块
                if (VersionUtil.isLessThan1_13()) {
                    // 1.8-1.12版本使用旧API
                    org.bukkit.material.Bed bedBlock = (org.bukkit.material.Bed) targetBlock.getState().getData();

                    if (!bedBlock.isHeadOfBed()) {
                        bedFeet = targetBlock;
                        bedHead = getBedNeighborBlock(bedFeet);
                    } else {
                        bedHead = targetBlock;
                        bedFeet = getBedNeighborBlock(bedHead);
                    }
                } else {
                    // 1.13+版本，但我们避免直接引用BlockData API
                    // 使用方向检测 - 此方法也适用于1.8
                    bedHead = targetBlock;
                    // 尝试寻找床的另一部分
                    bedFeet = getBedNeighborBlock(bedHead);
                    
                    // 如果第一次尝试找不到，可能现在的方块是床脚
                    if (bedFeet == null) {
                        bedFeet = targetBlock;
                        bedHead = getBedNeighborBlock(bedFeet);
                    }
                }

                // 安全销毁床的两个部分
                if (bedHead != null) {
                    bedHead.setType(Material.AIR);
                }
                if (bedFeet != null) {
                    bedFeet.setType(Material.AIR);
                }
            } catch (Exception e) {
                LoggerUtil.warn("销毁床时出错: " + e.getMessage());
                // 兜底方案：直接设置目标方块为AIR
                targetBlock.setType(Material.AIR);
            }
        } else {
            targetBlock.setType(Material.AIR);
        }
    }

    private static Block getBedNeighborBlock(Block block) {
        // 检查所有四个方向
        for (BlockFace face : new BlockFace[]{BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH}) {
            Block relative = block.getRelative(face);
            if (isBedBlock(relative)) {
                return relative;
            }
        }
        return null;
    }

    private static boolean isBedBlock(Block isBed) {
        if (isBed == null) {
            return false;
        }

        return isBed.getType().name().toUpperCase().contains("BED");
    }

}
