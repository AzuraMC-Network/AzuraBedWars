package cc.azuramc.bedwars.compat.util;

import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.game.team.GameTeam;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
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
            Bukkit.getLogger().warning("销毁床时出现异常: " + e.getMessage());
            try {
                // 反射调用
                setBlockTypeUsingReflection(gameTeam.getBedHead());
                setBlockTypeUsingReflection(gameTeam.getBedFeet());
            } catch (Exception ex) {
                Bukkit.getLogger().severe("无法销毁床: " + ex.getMessage());
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
                if (!VersionUtil.isLessThan113()) {
                    org.bukkit.block.data.type.Bed bedBlock = (org.bukkit.block.data.type.Bed) targetBlock.getBlockData();

                    if (bedBlock.getPart() == org.bukkit.block.data.type.Bed.Part.FOOT) {
                        bedFeet = targetBlock;
                        bedHead = getBedNeighborBlock(bedFeet);
                    } else {
                        bedHead = targetBlock;
                        bedFeet = getBedNeighborBlock(bedHead);
                    }
                } else {
                    org.bukkit.material.Bed bedBlock = (org.bukkit.material.Bed) targetBlock.getState().getData();

                    if (!bedBlock.isHeadOfBed()) {
                        bedFeet = targetBlock;
                        bedHead = getBedNeighborBlock(bedFeet);
                    } else {
                        bedHead = targetBlock;
                        bedFeet = getBedNeighborBlock(bedHead);
                    }
                }

                bedHead.setType(Material.AIR);
                bedFeet.setType(Material.AIR);
            } catch (Exception e) {
                targetBlock.setType(Material.AIR);
            }
        } else {
            targetBlock.setType(Material.AIR);
        }
    }

    private static Block getBedNeighborBlock(Block head) {
        if (isBedBlock(head.getRelative(BlockFace.EAST))) {
            return head.getRelative(BlockFace.EAST);
        } else if (isBedBlock(head.getRelative(BlockFace.WEST))) {
            return head.getRelative(BlockFace.WEST);
        } else if (isBedBlock(head.getRelative(BlockFace.SOUTH))) {
            return head.getRelative(BlockFace.SOUTH);
        } else {
            return head.getRelative(BlockFace.NORTH);
        }
    }

    private static boolean isBedBlock(Block isBed) {
        if (isBed == null) {
            return false;
        }

        return isBed.getType().name().toUpperCase().contains("BED");
    }

}
