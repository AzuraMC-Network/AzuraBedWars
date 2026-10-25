package cc.azuramc.bedwars.compat.util;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.util.MapUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;

/**
 * @author an5w1r@163.com
 */
public class GameTeamBedHandler {

    private static final BlockFace[] HORIZONTAL_FACES = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    private final GameTeam gameTeam;

    public GameTeamBedHandler(GameTeam gameTeam) {
        this.gameTeam = gameTeam;
        Block first = findBedBlock();
        Block second = first == null ? null : findOtherHalf(first);
        if (second != null) {
            determineBedParts(first, second);
        }
    }

    private Block findBedBlock() {
        int radius = AzuraBedWars.getInstance().getGameplayConfig().getBedSearchRadius();
        Block center = gameTeam.getSpawnLocation().getBlock();
        Block nearest = null;
        int nearestDistance = Integer.MAX_VALUE;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    int distance = x * x + y * y + z * z;
                    if (distance < nearestDistance) {
                        Block block = center.getRelative(x, y, z);
                        if (MapUtil.isBedBlock(block)) {
                            nearest = block;
                            nearestDistance = distance;
                        }
                    }
                }
            }
        }
        return nearest;
    }

    private static Block findOtherHalf(Block bedBlock) {
        for (BlockFace face : HORIZONTAL_FACES) {
            Block neighbor = bedBlock.getRelative(face);
            if (MapUtil.isBedBlock(neighbor)) {
                return neighbor;
            }
        }
        return null;
    }

    public static void destroyBed(GameTeam gameTeam) {
        if (gameTeam.getBedHead() != null) {
            gameTeam.getBedHead().setType(Material.AIR);
        }
        if (gameTeam.getBedFeet() != null) {
            gameTeam.getBedFeet().setType(Material.AIR);
        }
    }

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

        if (!(bedData1 instanceof Bed) || !(bedData2 instanceof Bed)) {
            throw new IllegalArgumentException("Block is not a bed");
        }
        Bed bed1 = (Bed) bedData1;
        Bed bed2 = (Bed) bedData2;

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
}
