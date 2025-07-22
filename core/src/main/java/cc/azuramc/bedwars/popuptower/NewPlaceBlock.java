package cc.azuramc.bedwars.popuptower;

import cc.azuramc.bedwars.game.TeamColor;
import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class NewPlaceBlock {
    public NewPlaceBlock(Block b, String xyz, TeamColor color, Player p, boolean ladder, int ladderdata) {
        int x = Integer.parseInt(xyz.split(", ")[0]);
        int y = Integer.parseInt(xyz.split(", ")[1]);
        int z = Integer.parseInt(xyz.split(", ")[2]);
        if (b.getRelative(x, y, z).getType().equals(Material.AIR)) {

            if (!ladder)
                placeTowerBlocks(b, color, x, y, z);
            else
                placeLadder(b, x, y, z, ladderdata);
        }

    }

    private void placeTowerBlocks(org.bukkit.block.Block block, TeamColor color, int x, int y, int z){
        block.getRelative(x, y, z).setType(XMaterial.WHITE_WOOL.get());
    }

    private void placeLadder(org.bukkit.block.Block block, int x, int y, int z, int ladderdata){
        block.getRelative(x, y, z).setType(Material.LADDER);
        XBlock.setDirection(block, getFaceByData(ladderdata));
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
