package cc.azuramc.bedwars.commands.admin.map;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.utils.CC;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({"bedwars map storage", "bw map storage", "azurabedwars map storage"})
@CommandPermission("azurabedwars.admin")
public class MapStorage {
    @Dependency
    private AzuraBedWars pluginDependency;

    private final AzuraBedWars plugin = AzuraBedWars.getInstance();

    @DefaultFor({"bedwars map storage", "bw map storage", "azurabedwars map storage"})
    public void getMapCommand(Player player) {
        player.sendMessage(CC.CHAT_BAR);
        player.sendMessage(CC.color("&b&lAzuraBedWars &8- &7v" + plugin.getDescription().getVersion() + " &8- &b起床战争 - 地图设置"));
        player.sendMessage("");
        player.sendMessage(CC.color("&7 • &f/bw map storage list &7查看地图数据列表"));
        player.sendMessage(CC.color("&7 • &f/bw map storage migrate &7迁移数据存储方式"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> setTeamPlayers <number> &7设置队伍最大人数"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> setMinPlayers <number> &7设置地图最小需要人数"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> setRespawn &7设置地图重生点"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> addBase &7增加基地出生点"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> addDrop <type> &7增加资源点 (类型: BASE/DIAMOND/EMERALD)"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> addShop <type> &7增加商店 (类型: ITEM/UPGRADE)"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> setPos1 &7设置地图边界1"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> setPos2 &7设置地图边界2"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> info &7查看地图信息"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> save &7保存地图数据"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> load &7加载地图数据"));
        player.sendMessage(CC.CHAT_BAR);
    }
}
