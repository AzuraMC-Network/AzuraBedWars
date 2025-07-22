package cc.azuramc.bedwars.popuptower;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.TeamColor;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class NewPlaceBlock {
    public NewPlaceBlock(Block block, String xyz, TeamColor color, Player p, boolean ladder, int ladderdata) {
        int x = Integer.parseInt(xyz.split(", ")[0]);
        int y = Integer.parseInt(xyz.split(", ")[1]);
        int z = Integer.parseInt(xyz.split(", ")[2]);
        if (block.getRelative(x, y, z).getType().equals(Material.AIR)) {

            if (!ladder)
                placeTowerBlocks(block, color, x, y, z);
            else
                AzuraBedWars.getInstance().getNmsAccess().placeLadder(block, x, y, z, ladderdata);
        }

    }

    private void placeTowerBlocks(org.bukkit.block.Block block, TeamColor color, int x, int y, int z){
        block.getRelative(x, y, z).setType(XMaterial.WHITE_WOOL.get());
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
