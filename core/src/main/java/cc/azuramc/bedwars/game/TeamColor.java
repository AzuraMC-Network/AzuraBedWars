package cc.azuramc.bedwars.game;

import cc.azuramc.bedwars.AzuraBedWars;
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
    RED(Color.fromRGB(255, 85, 85), ChatColor.RED, DyeColor.RED),
    BLUE(Color.fromRGB(85, 85, 255), ChatColor.BLUE, DyeColor.BLUE),
    GREEN(Color.fromRGB(85, 255, 85), ChatColor.GREEN, DyeColor.LIME),
    YELLOW(Color.fromRGB(255, 255, 85), ChatColor.YELLOW, DyeColor.YELLOW),
    AQUA(Color.fromRGB(85, 255, 255), ChatColor.AQUA, DyeColor.CYAN),
    WHITE(Color.WHITE, ChatColor.WHITE, DyeColor.WHITE),
    PINK(Color.fromRGB(255, 85, 255), ChatColor.LIGHT_PURPLE, DyeColor.PINK),
    GRAY(Color.fromRGB(85, 85, 85), ChatColor.DARK_GRAY, DyeColor.GRAY),

    // 扩展颜色
    ORANGE(Color.fromRGB(255, 165, 0), ChatColor.GOLD, DyeColor.ORANGE),
    MAGENTA(Color.fromRGB(255, 0, 255), ChatColor.LIGHT_PURPLE, DyeColor.MAGENTA),
    LIGHT_BLUE(Color.fromRGB(135, 206, 250), ChatColor.BLUE, DyeColor.LIGHT_BLUE),
    LIME(Color.fromRGB(50, 205, 50), ChatColor.GREEN, DyeColor.LIME),
    PURPLE(Color.fromRGB(128, 0, 128), ChatColor.DARK_PURPLE, DyeColor.PURPLE),
    CYAN(Color.fromRGB(0, 255, 255), ChatColor.DARK_AQUA, DyeColor.CYAN),
    BLACK(Color.BLACK, ChatColor.BLACK, DyeColor.BLACK),
    BROWN(Color.fromRGB(139, 69, 19), ChatColor.DARK_RED, DyeColor.BROWN);

    private final ChatColor chatColor;
    private final Color color;
    private final DyeColor dyeColor;

    TeamColor(Color color, ChatColor chatColor, DyeColor dyeColor) {
        this.chatColor = chatColor;
        this.color = color;
        this.dyeColor = dyeColor;
    }

    /**
     * Colored name from lang/
     */
    public String getName() {
        String name = AzuraBedWars.getInstance().getLang().getTeams().get(name());
        return name != null ? name : chatColor + name();
    }
}
