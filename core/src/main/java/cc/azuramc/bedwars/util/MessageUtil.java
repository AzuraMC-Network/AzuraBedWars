package cc.azuramc.bedwars.util;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.LangConfig;
import cc.azuramc.bedwars.game.map.MapData;
import com.cryptomorin.xseries.messages.ActionBar;
import com.cryptomorin.xseries.messages.Titles;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
public final class MessageUtil {

    private static final DecimalFormat COORDINATE_FORMAT = new DecimalFormat("0.00");
    private static final DecimalFormat ANGLE_FORMAT = new DecimalFormat("0.0");

    @Setter
    private static boolean usingPlaceholderAPI = false;

    public static final String CHAT_BAR = ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH
            + "------------------------------------------------";


    /**
     * Fills {name} placeholders of a lang text, pairs are a name and then its value
     */
    @NotNull
    public static String fill(@NotNull String text, Object... pairs) {
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            text = text.replace("{" + pairs[i] + "}", String.valueOf(pairs[i + 1]));
        }
        return text;
    }

    public static void sendTitle(@NotNull Player player, @Nullable String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut) {
        if (title == null && subtitle == null) {
            return;
        }
        title = parse(player, title);
        subtitle = parse(player, subtitle);
        Titles.sendTitle(player, fadeIn, stay, fadeOut, color(title), color(subtitle));
    }

    public static void sendActionBar(@NotNull Player player, @NotNull String message) {
        message = parse(player, message);
        ActionBar.sendActionBar(player, color(message));
    }

    public static void sendMessage(@NotNull Player player, @NotNull String message) {
        message = parse(player, message);
        player.sendMessage(color(message));
    }

    @NotNull
    public static String color(@Nullable String string) {
        if (string == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    @NotNull
    public static List<String> color(@NotNull List<String> lines) {
        List<String> toReturn = new ArrayList<>();
        for (String line : lines) {
            toReturn.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        return toReturn;
    }

    @NotNull
    public static String[] color(@NotNull String[] lines) {
        String[] colored = new String[lines.length];
        for (int i = 0; i < lines.length; i++) {
            colored[i] = ChatColor.translateAlternateColorCodes('&', lines[i]);
        }
        return colored;
    }


    @NotNull
    public static String parse(@Nullable Player player, @Nullable String string) {
        if (string != null) {
            string = AzuraBedWars.getInstance().getExtensions().replace(string, player);
        }
        if (usingPlaceholderAPI) {
            if (string != null) {
                string = color(PlaceholderAPI.setPlaceholders(player, string));
            }
        } else {
            string = color(string);
        }

        return string != null ? string : "";
    }

    @NotNull
    public static List<String> parse(@Nullable Player player, @NotNull List<String> lines) {
        List<String> toReturn = new ArrayList<>();
        for (String line : lines) {
            toReturn.add(parse(player, color(line)));
        }

        return toReturn;
    }

    @NotNull
    public static ChatColor getLastChatColor(@Nullable String string) {
        if (string == null || string.isEmpty()) {
            return ChatColor.RESET;
        }

        final char colorChar = ChatColor.COLOR_CHAR;

        int length = string.length();

        for (int index = length - 1; index > -1; index--) {
            char section = string.charAt(index);
            if (section == colorChar && index < length - 1) {
                char c = string.charAt(index + 1);
                ChatColor color = ChatColor.getByChar(c);
                if (color != null) {
                    return color;
                }
            }
        }

        return ChatColor.RESET;
    }

    @NotNull
    public static String formatLocation(@Nullable Location location) {
        if (location == null) {
            return AzuraBedWars.getInstance().getLang().getMap().getNotSet();
        }
        return formatLocation(location.getWorld() != null ? location.getWorld().getName() : null,
                location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    @NotNull
    public static String formatLocation(@Nullable MapData.RawLocation rawLocation) {
        if (rawLocation == null) {
            return AzuraBedWars.getInstance().getLang().getMap().getNotSet();
        }
        return formatLocation(rawLocation.getWorld(), rawLocation.getX(), rawLocation.getY(), rawLocation.getZ(),
                rawLocation.getYaw(), rawLocation.getPitch());
    }

    private static String formatLocation(@Nullable String world, double x, double y, double z, float yaw, float pitch) {
        return String.format("&b%s&f: &b%s, %s, %s &7(Yaw: %s, Pitch: %s)",
                world != null ? world : AzuraBedWars.getInstance().getLang().getMap().getUnknownWorld(),
                COORDINATE_FORMAT.format(x), COORDINATE_FORMAT.format(y), COORDINATE_FORMAT.format(z),
                ANGLE_FORMAT.format(yaw), ANGLE_FORMAT.format(pitch));
    }

    @NotNull
    public static List<String> formatLocationList(@Nullable List<MapData.RawLocation> locations) {
        LangConfig.Maps lang = AzuraBedWars.getInstance().getLang().getMap();
        if (locations == null || locations.isEmpty()) {
            return Collections.singletonList(lang.getNone());
        }
        List<String> result = new ArrayList<>();
        for (int i = 0; i < locations.size(); i++) {
            result.add(fill(lang.getBaseLine(), "number", i + 1, "location", formatLocation(locations.get(i))));
        }
        return result;
    }
}
