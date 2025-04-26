package cc.azuramc.bedwars.compat.util;

import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.compat.wrapper.MaterialWrapper;
import cc.azuramc.bedwars.game.team.GameTeam;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.lang.reflect.Method;

public class BedUtil {

    /**
     * 安全销毁床方块，适用于所有版本
     */
    public static void destroyBed(GameTeam gameTeam) {
        try {
            // 设置床头和床脚方块为AIR
            if (gameTeam.getBedHead() != null) {
                gameTeam.getBedHead().setType(Material.AIR);
            }

            if (gameTeam.getBedFeet() != null) {
                gameTeam.getBedFeet().setType(Material.AIR);
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
            setTypeIdMethod.invoke(block, 0); // AIR ID = 0
        } catch (Exception e) {
            try {
                block.setType(Material.AIR);
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

        if (!VersionUtil.isLessThan113()) {
            return isBed.getType().name().endsWith("_BED");
        } else {
            try {
                Material bedBlock = Material.valueOf("BED_BLOCK");
                return (MaterialWrapper.BED().equals(isBed.getType()) || bedBlock.equals(isBed.getType()));
            } catch (IllegalArgumentException e) {
                return MaterialWrapper.BED().equals(isBed.getType());
            }
        }
    }

}
