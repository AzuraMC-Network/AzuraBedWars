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
 * @author An5w1r@163.com
 */
public class WoolUtil {

    /**
     * 设置Block为指定颜色的羊毛方块
     * @param block block
     * @param teamColor 颜色
     */
    public static void setWoolBlockColor(Block block, TeamColor teamColor) {
        if (VersionUtil.isLessThan113()) {
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
     * @param block block
     * @param teamColor 颜色
     * @param x x
     * @param y y
     * @param z z
     */
    public static void setWoolBlockColor(Block block, TeamColor teamColor, int x, int y, int z) {
        if (VersionUtil.isLessThan113()) {
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

        if (!VersionUtil.isLessThan113()) {
            // 1.13+版本的羊毛命名格式为 COLOR_WOOL
            String colorName = blockType.substring(0, blockType.length() - 5);
            // 映射颜色名称到TeamColor
            return switch (colorName) {
                case "RED" -> TeamColor.RED;
                case "BLUE" -> TeamColor.BLUE;
                case "GREEN" -> TeamColor.GREEN;
                case "LIME" -> TeamColor.LIME;
                case "YELLOW" -> TeamColor.YELLOW;
                case "CYAN" -> TeamColor.CYAN;
                case "LIGHT_BLUE" -> TeamColor.LIGHT_BLUE;
                case "WHITE" -> TeamColor.WHITE;
                case "PINK" -> TeamColor.PINK;
                case "MAGENTA" -> TeamColor.MAGENTA;
                case "PURPLE" -> TeamColor.PURPLE;
                case "GRAY" -> TeamColor.GRAY;
                case "BLACK" -> TeamColor.BLACK;
                case "ORANGE" -> TeamColor.ORANGE;
                case "BROWN" -> TeamColor.BROWN;
                default -> null;
            };
        } else if ("WOOL".equals(blockType)) {
            // 1.12-版本需要检查数据值
            @SuppressWarnings("deprecation")
            byte data = block.getData();

            // 根据羊毛的数据值映射到TeamColor
            return switch (data) {
                case 0 -> TeamColor.WHITE;
                case 1 -> TeamColor.ORANGE;
                case 2 -> TeamColor.MAGENTA;
                case 3 -> TeamColor.LIGHT_BLUE;
                case 4 -> TeamColor.YELLOW;
                case 5 -> TeamColor.LIME;
                case 6 -> TeamColor.PINK;
                case 7 -> TeamColor.GRAY;
                case 9 -> TeamColor.CYAN;
                case 10 -> TeamColor.PURPLE;
                case 11 -> TeamColor.BLUE;
                case 12 -> TeamColor.BROWN;
                case 13 -> TeamColor.GREEN;
                case 14 -> TeamColor.RED;
                case 15 -> TeamColor.BLACK;
                default -> null;
            };
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
        return switch (teamColor) {
            case WHITE -> 0;
            case ORANGE -> 1;
            case MAGENTA -> 2;
            case LIGHT_BLUE -> 3;
            case YELLOW -> 4;
            case LIME -> 5;
            case PINK -> 6;
            case GRAY -> 7;
            case CYAN -> 9;
            case PURPLE -> 10;
            case BLUE -> 11;
            case BROWN -> 12;
            case GREEN -> 13;
            case RED -> 14;
            case BLACK -> 15;
            default -> 0;
        };
    }

    /**
     * 获取指定颜色的羊毛物品
     *
     * @param teamColor 队伍颜色
     * @return 对应颜色的羊毛物品
     */
    public static ItemStack getColoredWool(TeamColor teamColor) {
        if (VersionUtil.isLessThan113()) {
            return new ItemStack(Material.valueOf("WOOL"), 1, getWoolDataFromTeamColor(teamColor));
        } else {
            return XMaterial.matchXMaterial(teamColor.getDyeColor().toString() + "_WOOL")
                    .orElse(XMaterial.WHITE_WOOL)
                    .parseItem();
        }
    }
}
