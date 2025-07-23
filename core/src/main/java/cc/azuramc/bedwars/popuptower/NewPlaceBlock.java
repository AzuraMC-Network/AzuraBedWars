package cc.azuramc.bedwars.popuptower;

import cc.azuramc.bedwars.compat.util.WoolUtil;
import cc.azuramc.bedwars.game.TeamColor;
import com.cryptomorin.xseries.XBlock;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class NewPlaceBlock {
    public NewPlaceBlock(Block block, String xyz, TeamColor color, boolean ladder, int data) {
        int x = Integer.parseInt(xyz.split(", ")[0]);
        int y = Integer.parseInt(xyz.split(", ")[1]);
        int z = Integer.parseInt(xyz.split(", ")[2]);
        if (block.getRelative(x, y, z).getType().equals(Material.AIR)) {
            if (!ladder)
                WoolUtil.setWoolBlockColor(block, color, x, y, z);
            else
                placeLadder(block, x, y, z, data);
        }

    }

    public void placeLadder(Block block, int x, int y, int z, int data) {
        block.getRelative(x, y, z).setType(Material.LADDER);
        XBlock.setDirection(block.getRelative(x, y, z), getFaceByData(data));
    }

    private BlockFace getFaceByData(int data) {
        switch (data) {
            case 2 -> {
                return BlockFace.NORTH;
            }
            case 3 -> {
                return BlockFace.SOUTH;
            }
            case 4 -> {
                return BlockFace.WEST;
            }
            case 5 -> {
                return BlockFace.EAST;
            }
        }
        return BlockFace.NORTH;
    }
}
