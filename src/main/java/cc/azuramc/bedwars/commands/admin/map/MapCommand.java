package cc.azuramc.bedwars.commands.admin.map;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.map.MapData;
import cc.azuramc.bedwars.utils.CC;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.HashMap;
import java.util.Map;


@Command({"bedwars map", "bw map", "azurabedwars map"})
@CommandPermission("azurabedwars.admin")
public class MapCommand {

    @Dependency
    private AzuraBedWars pluginDependency;

    private final AzuraBedWars plugin = AzuraBedWars.getInstance();

    private final Map<String, MapData> maps = new HashMap<>();

    @DefaultFor({"bedwars map", "bw map", "azurabedwars map"})
    public void getMapCommand(Player player) {
        player.sendMessage(CC.color("&b&lAzuraBedWars &8- &7v" + plugin.getDescription().getVersion() + " &8- &b起床战争 - 地图设置"));
        player.sendMessage("");
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> create &7创建新的地图"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> setAuthor <authorName> &7设置地图作者名"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> setTeamPlayers <number> &7设置队伍最大人数"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> setMinPlayers <number> &7设置地图最小需要人数"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> setRange <value> &7设置出生点保护范围"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> setRespawn &7设置地图重生点"));
        player.sendMessage("");
    }

    @Subcommand("create")
    public void createMap(Player player, String mapName) {
        maps.put(mapName, new MapData());
    }

}
