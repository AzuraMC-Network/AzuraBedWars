package cc.azuramc.bedwars.game;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;

/**
 * 游戏中的队伍颜色枚举
 * 支持所有Minecraft中的羊毛颜色
 * 
 * @author an5w1r@163.com
 */

@Getter
public enum TeamColor {
    // 主要队伍颜色 (传统颜色)
    RED(Color.fromRGB(255, 85, 85), ChatColor.RED, DyeColor.RED, "§c红"),
    BLUE(Color.fromRGB(85, 85, 255), ChatColor.BLUE, DyeColor.BLUE, "§9蓝"),
    GREEN(Color.fromRGB(85, 255, 85), ChatColor.GREEN, DyeColor.LIME, "§a绿"),
    YELLOW(Color.fromRGB(255, 255, 85), ChatColor.YELLOW, DyeColor.YELLOW, "§e黄"),
    AQUA(Color.fromRGB(85, 255, 255), ChatColor.AQUA, DyeColor.CYAN, "§b青"),
    WHITE(Color.WHITE, ChatColor.WHITE, DyeColor.WHITE, "§f白"),
    PINK(Color.fromRGB(255, 85, 255), ChatColor.LIGHT_PURPLE, DyeColor.PINK, "§d粉"),
    GRAY(Color.fromRGB(85, 85, 85), ChatColor.DARK_GRAY, DyeColor.GRAY, "§8灰"),

    // 扩展颜色
    ORANGE(Color.fromRGB(255, 165, 0), ChatColor.GOLD, DyeColor.ORANGE, "§6橙"),
    MAGENTA(Color.fromRGB(255, 0, 255), ChatColor.LIGHT_PURPLE, DyeColor.MAGENTA, "§5粉"),
    LIGHT_BLUE(Color.fromRGB(135, 206, 250), ChatColor.BLUE, DyeColor.LIGHT_BLUE, "§b青"),
    LIME(Color.fromRGB(50, 205, 50), ChatColor.GREEN, DyeColor.LIME, "§a绿"),
    PURPLE(Color.fromRGB(128, 0, 128), ChatColor.DARK_PURPLE, DyeColor.PURPLE, "§5紫"),
    CYAN(Color.fromRGB(0, 255, 255), ChatColor.DARK_AQUA, DyeColor.CYAN, "§3青"),
    BLACK(Color.BLACK, ChatColor.BLACK, DyeColor.BLACK, "§0黑"),
    BROWN(Color.fromRGB(139, 69, 19), ChatColor.DARK_RED, DyeColor.BROWN, "§4棕");

    private final ChatColor chatColor;
    private final Color color;
    private final DyeColor dyeColor;
    private final String name;

    TeamColor(Color color, ChatColor chatColor, DyeColor dyeColor, String name) {
        this.chatColor = chatColor;
        this.color = color;
        this.dyeColor = dyeColor;
        this.name = name;
    }
}
