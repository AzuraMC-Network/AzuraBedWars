package cc.azuramc.bedwars.enums;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;

@Getter
public enum TeamColor {
    RED(Color.fromRGB(255, 85, 85), ChatColor.RED, DyeColor.RED, "§c红"),
    BLUE(Color.fromRGB(85, 85, 255), ChatColor.BLUE, DyeColor.LIGHT_BLUE, "§9蓝"),
    GREEN(Color.fromRGB(85, 255, 85), ChatColor.GREEN, DyeColor.LIME, "§a绿"),
    YELLOW(Color.fromRGB(255, 255, 85), ChatColor.YELLOW, DyeColor.YELLOW, "§e黄"),
    AQUA(Color.fromRGB(85, 255, 255), ChatColor.AQUA, DyeColor.CYAN, "§b青"),
    WHITE(Color.WHITE, ChatColor.WHITE, DyeColor.WHITE, "§f白"),
    PINK(Color.fromRGB(255, 85, 255), ChatColor.LIGHT_PURPLE, DyeColor.PINK, "§d粉"),
    GRAY(Color.fromRGB(85, 85, 85), ChatColor.DARK_GRAY, DyeColor.GRAY, "§8灰");


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
