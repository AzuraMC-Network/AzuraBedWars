package cc.azuramc.bedwars.popuptower;

import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.game.TeamColor;
import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.material.Wool;

import java.util.Objects;

public class NewPlaceBlock {
    public NewPlaceBlock(Block block, String xyz, TeamColor color, boolean ladder, int data) {
        int x = Integer.parseInt(xyz.split(", ")[0]);
        int y = Integer.parseInt(xyz.split(", ")[1]);
        int z = Integer.parseInt(xyz.split(", ")[2]);
        if (block.getRelative(x, y, z).getType().equals(Material.AIR)) {
            if (!ladder)
                placeTowerBlocks(block, color, x, y, z);
            else
                placeLadder(block, x, y, z, data);
        }

    }

    private void placeTowerBlocks(Block block, TeamColor color, int x, int y, int z){
        if (VersionUtil.isLessThan113()) {
            block.getRelative(x, y, z).setType(Material.valueOf("WOOL"));
            BlockState state = block.getRelative(x, y, z).getState();
            state.setData(new Wool(color.getDyeColor()));
            state.update();
        } else {
            block.getRelative(x, y, z).setType(Objects.requireNonNull(XMaterial.matchXMaterial(color.getDyeColor().toString() + "_WOOL").orElse(XMaterial.WHITE_WOOL).get()));
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
