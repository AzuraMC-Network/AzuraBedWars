package cc.azuramc.bedwars.utils;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class CC {

    @Setter
    static boolean usingPlaceholderAPI = false;

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        TitleUtil.sendTitle(player, fadeIn, stay, fadeOut, color(title), color(subtitle));
    }

    public static void sendActionBar(Player player, String message) {
        ActionBarUtil.sendBar(player, color(message));
    }

    public static void sendMessage(Player player, String message) {
        message = parse(player, message);
        player.sendMessage(color(message));
    }

    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static List<String> color(List<String> lines) {
        List<String> toReturn = new ArrayList<>();
        for (String line : lines) {
            toReturn.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        return toReturn;
    }

    public static String parse(Player player, String string) {
        if (usingPlaceholderAPI) {
            try {
                string = color(PlaceholderAPI.setPlaceholders(player, string));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            string = color(string);
        }

        return string;
    }

    public static List<String> parse(Player player, List<String> lines) {
        List<String> toReturn = new ArrayList<>();
        for (String line : lines) {
            toReturn.add(parse(player, color(line)));
        }

        return toReturn;
    }

}
