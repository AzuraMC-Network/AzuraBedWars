package cc.azuramc.bedwars.compat.util;

import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.game.TeamColor;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;

import java.util.Objects;

/**
 * @author an5w1r@163.com
 */
public class WoolUtil {

    /**
     * 设置Block为指定颜色的羊毛方块
     *
     * @param block     block
     * @param teamColor 颜色
     */
    @SuppressWarnings("deprecation")
    public static void setWoolBlockColor(Block block, TeamColor teamColor) {
        if (VersionUtil.isLessThan1_13()) {
            block.setType(Material.valueOf("WOOL"));
            BlockState state = block.getState();
            state.setData(new Wool(teamColor.getDyeColor()));
            state.update();
        } else {
            block.setType(Objects.requireNonNull(XMaterial.matchXMaterial(teamColor.getDyeColor().toString() + "_WOOL").orElse(XMaterial.WHITE_WOOL).get()));
        }
    }

    /**
     * 设置Block为指定颜色的羊毛方块
     *
     * @param block     block
     * @param teamColor 颜色
     * @param x         x
     * @param y         y
     * @param z         z
     */
    @SuppressWarnings("deprecation")
    public static void setWoolBlockColor(Block block, TeamColor teamColor, int x, int y, int z) {
        if (VersionUtil.isLessThan1_13()) {
            block.getRelative(x, y, z).setType(Material.valueOf("WOOL"));
            BlockState state = block.getRelative(x, y, z).getState();
            state.setData(new Wool(teamColor.getDyeColor()));
            state.update();
        } else {
            block.getRelative(x, y, z).setType(Objects.requireNonNull(XMaterial.matchXMaterial(teamColor.getDyeColor().toString() + "_WOOL").orElse(XMaterial.WHITE_WOOL).get()));
        }
    }

    /**
     * 从羊毛方块确定对应的队伍颜色
     *
     * @param block 羊毛方块
     * @return 对应的队伍颜色
     */
    public static TeamColor getTeamColorFromWoolBlock(Block block) {
        String blockType = block.getType().name();

        if (!VersionUtil.isLessThan1_13()) {
            // 1.13+版本的羊毛命名格式为 COLOR_WOOL
            String colorName = blockType.substring(0, blockType.length() - 5);
            // 映射颜色名称到TeamColor
            switch (colorName) {
                case "RED":
                    return TeamColor.RED;
                case "BLUE":
                    return TeamColor.BLUE;
                case "GREEN":
                    return TeamColor.GREEN;
                case "LIME":
                    return TeamColor.LIME;
                case "YELLOW":
                    return TeamColor.YELLOW;
                case "CYAN":
                    return TeamColor.CYAN;
                case "LIGHT_BLUE":
                    return TeamColor.LIGHT_BLUE;
                case "WHITE":
                    return TeamColor.WHITE;
                case "PINK":
                    return TeamColor.PINK;
                case "MAGENTA":
                    return TeamColor.MAGENTA;
                case "PURPLE":
                    return TeamColor.PURPLE;
                case "GRAY":
                    return TeamColor.GRAY;
                case "BLACK":
                    return TeamColor.BLACK;
                case "ORANGE":
                    return TeamColor.ORANGE;
                case "BROWN":
                    return TeamColor.BROWN;
                default:
                    return null;
            }
        } else if ("WOOL".equals(blockType)) {
            // 1.12-版本需要检查数据值
            @SuppressWarnings("deprecation")
            byte data = block.getData();

            // 根据羊毛的数据值映射到TeamColor
            switch (data) {
                case 0:
                    return TeamColor.WHITE;
                case 1:
                    return TeamColor.ORANGE;
                case 2:
                    return TeamColor.MAGENTA;
                case 3:
                    return TeamColor.LIGHT_BLUE;
                case 4:
                    return TeamColor.YELLOW;
                case 5:
                    return TeamColor.LIME;
                case 6:
                    return TeamColor.PINK;
                case 7:
                    return TeamColor.GRAY;
                case 9:
                    return TeamColor.CYAN;
                case 10:
                    return TeamColor.PURPLE;
                case 11:
                    return TeamColor.BLUE;
                case 12:
                    return TeamColor.BROWN;
                case 13:
                    return TeamColor.GREEN;
                case 14:
                    return TeamColor.RED;
                case 15:
                    return TeamColor.BLACK;
                default:
                    return null;
            }
        }

        return null;
    }

    /**
     * 低版本根据TeamColor获取BlockData
     *
     * @param teamColor 队伍颜色
     * @return Block Data 的 byte数值
     */
    public static byte getWoolDataFromTeamColor(TeamColor teamColor) {

        // 根据羊毛的数据值映射到TeamColor
        switch (teamColor) {
            case WHITE:
                return 0;
            case ORANGE:
                return 1;
            case MAGENTA:
                return 2;
            case LIGHT_BLUE:
                return 3;
            case YELLOW:
                return 4;
            case LIME:
                return 5;
            case PINK:
                return 6;
            case GRAY:
                return 7;
            case CYAN:
                return 9;
            case PURPLE:
                return 10;
            case BLUE:
                return 11;
            case BROWN:
                return 12;
            case GREEN:
                return 13;
            case RED:
                return 14;
            case BLACK:
                return 15;
            default:
                return 0;
        }
    }

    /**
     * 根据数据值获取对应的颜色名称
     *
     * @param data 颜色数据值
     * @return 颜色名称
     */
    public static String getColorNameFromData(byte data) {
        switch (data & 0xF) { // 只使用低4位
            case 0:
                return "WHITE";
            case 1:
                return "ORANGE";
            case 2:
                return "MAGENTA";
            case 3:
                return "LIGHT_BLUE";
            case 4:
                return "YELLOW";
            case 5:
                return "LIME";
            case 6:
                return "PINK";
            case 7:
                return "GRAY";
            case 8:
                return "SILVER";
            case 9:
                return "CYAN";
            case 10:
                return "PURPLE";
            case 11:
                return "BLUE";
            case 12:
                return "BROWN";
            case 13:
                return "GREEN";
            case 14:
                return "RED";
            case 15:
                return "BLACK";
            default:
                return "WHITE";
        }
    }

    /**
     * 获取指定颜色的羊毛物品
     *
     * @param teamColor 队伍颜色
     * @return 对应颜色的羊毛物品
     */
    @SuppressWarnings("deprecation")
    public static ItemStack getColoredWool(TeamColor teamColor) {
        if (VersionUtil.isLessThan1_13()) {
            return new ItemStack(Material.valueOf("WOOL"), 1, getWoolDataFromTeamColor(teamColor));
        } else {
            return XMaterial.matchXMaterial(teamColor.getDyeColor().toString() + "_WOOL")
                    .orElse(XMaterial.WHITE_WOOL)
                    .parseItem();
        }
    }
}
