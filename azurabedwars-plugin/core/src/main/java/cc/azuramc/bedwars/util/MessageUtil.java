package cc.azuramc.bedwars.util;

import cc.azuramc.bedwars.game.map.MapData;
import com.cryptomorin.xseries.messages.ActionBar;
import com.cryptomorin.xseries.messages.Titles;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.ArrayList;
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


    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (title == null && subtitle == null) {
            return;
        }
        title = parse(player, title);
        subtitle = parse(player, subtitle);
        Titles.sendTitle(player, fadeIn, stay, fadeOut, color(title), color(subtitle));
    }

    public static void sendActionBar(Player player, String message) {
        message = parse(player, message);
        ActionBar.sendActionBar(player, color(message));
    }

    public static void sendMessage(Player player, String message) {
        message = parse(player, message);
        player.sendMessage(color(message));
    }

    public static String color(String string) {
        if (string == null) {
            return "";
        }
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

    public static ChatColor getLastChatColor(String string) {
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

    /**
     * 格式化Location的显示格式
     * 格式: 世界名: X.XX, Y.YY, Z.ZZ (Yaw: XX.X, Pitch: XX.X)
     *
     * @param location 要格式化的位置
     * @return 格式化后的字符串，如果location为null则返回"未设置"
     */
    public static String formatLocation(Location location) {
        if (location == null) {
            return "&c未设置";
        }

        String worldName = location.getWorld() != null ? location.getWorld().getName() : "未知世界";
        String x = COORDINATE_FORMAT.format(location.getX());
        String y = COORDINATE_FORMAT.format(location.getY());
        String z = COORDINATE_FORMAT.format(location.getZ());
        String yaw = ANGLE_FORMAT.format(location.getYaw());
        String pitch = ANGLE_FORMAT.format(location.getPitch());

        return String.format("&b%s&f: &b%s, %s, %s &7(Yaw: %s, Pitch: %s)",
                worldName, x, y, z, yaw, pitch);
    }

    /**
     * 格式化RawLocation的显示格式
     * 格式: 世界名: X.XX, Y.YY, Z.ZZ (Yaw: XX.X, Pitch: XX.X)
     *
     * @param rawLocation 要格式化的原始位置
     * @return 格式化后的字符串，如果rawLocation为null则返回"未设置"
     */
    public static String formatLocation(MapData.RawLocation rawLocation) {
        if (rawLocation == null) {
            return "&c未设置";
        }

        String worldName = rawLocation.getWorld() != null ? rawLocation.getWorld() : "未知世界";
        String x = COORDINATE_FORMAT.format(rawLocation.getX());
        String y = COORDINATE_FORMAT.format(rawLocation.getY());
        String z = COORDINATE_FORMAT.format(rawLocation.getZ());
        String yaw = ANGLE_FORMAT.format(rawLocation.getYaw());
        String pitch = ANGLE_FORMAT.format(rawLocation.getPitch());

        return String.format("&b%s&f: &b%s, %s, %s &7(Yaw: %s, Pitch: %s)",
                worldName, x, y, z, yaw, pitch);
    }

    /**
     * 格式化RawLocation列表为友好的显示格式
     * 格式: 基地1: 世界名: X.XX, Y.YY, Z.ZZ (Yaw: XX.X, Pitch: XX.X)
     * 基地2: 世界名: X.XX, Y.YY, Z.ZZ (Yaw: XX.X, Pitch: XX.X)
     *
     * @param locations RawLocation列表
     * @return 格式化后的字符串列表，如果列表为空则返回包含"无"的单元素列表
     */
    public static List<String> formatLocationList(List<MapData.RawLocation> locations) {
        if (locations == null || locations.isEmpty()) {
            return List.of("&c无");
        }

        List<String> result = new ArrayList<>();
        for (int i = 0; i < locations.size(); i++) {
            MapData.RawLocation location = locations.get(i);
            result.add(String.format("    &7- 基地%d: %s", i + 1, formatLocation(location)));
        }

        return result;
    }

}
