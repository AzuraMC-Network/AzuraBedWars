package cc.azuramc.bedwars.util;

import com.cryptomorin.xseries.messages.ActionBar;
import com.cryptomorin.xseries.messages.Titles;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
public final class ChatColorUtil {

    @Setter
    static boolean usingPlaceholderAPI = false;

    public static final String CHAT_BAR = ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH
            + "------------------------------------------------";

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Titles.sendTitle(player, fadeIn, stay, fadeOut, color(title), color(subtitle));
    }

    public static void sendActionBar(Player player, String message) {
        ActionBar.sendActionBar(player, color(message));
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

    public static String[] color(String[] lines) {
        String[] colored = new String[lines.length];
        for (int i = 0; i < lines.length; i++) {
            colored[i] = ChatColor.translateAlternateColorCodes('&', lines[i]);
        }
        return colored;
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
