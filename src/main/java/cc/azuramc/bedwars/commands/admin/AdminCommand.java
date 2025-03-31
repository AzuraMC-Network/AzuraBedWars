package cc.azuramc.bedwars.commands.admin;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.map.data.MapData;
import cc.azuramc.bedwars.utils.CC;
import org.bukkit.*;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.Objects;

@Command({"bedwars", "bw", "azurabedwars"})
@CommandPermission("azurabedwars.admin")
public class AdminCommand {

    @Dependency
    private AzuraBedWars pluginDependency;

    private final AzuraBedWars plugin = AzuraBedWars.getInstance();

    @DefaultFor({"bedwars", "bw", "azurabedwars"})
    public void getHelpCommand(Player player) {
        player.sendMessage(CC.CHAT_BAR);
        player.sendMessage(CC.color("&b&lAzuraBedWars &8- &7v" + plugin.getDescription().getVersion() + " &8- &b起床战争 - 指令帮助"));
        player.sendMessage("");
        player.sendMessage(CC.color("&7 • &f/bw map &7查看地图相关指令帮助"));
        player.sendMessage(CC.color("&7 • &f/bw setWaiting &7设置等待大厅位置"));
        player.sendMessage(CC.color("&7 • &f/bw toWorld <worldName> &7前往世界"));
        player.sendMessage(CC.color("&7 • &f/bw loadWorld <worldName> &7加载世界"));
        player.sendMessage(CC.CHAT_BAR);
    }

    @Subcommand("map")
    public void getMapCommandHelp(Player player) {
        player.sendMessage(CC.CHAT_BAR);
        player.sendMessage(CC.color("&b&lAzuraBedWars &8- &7v" + plugin.getDescription().getVersion() + " &8- &b起床战争 - 地图设置"));
        player.sendMessage("");
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> create &7创建新的地图"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> setAuthor <authorName> &7设置地图作者名"));
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

    @Subcommand("setWaiting")
    public void setWaiting(Player player) {
        Location location = player.getLocation();
        MapData.RawLocation rawLocation = new MapData.RawLocation();
        rawLocation.setWorld(Objects.requireNonNull(location.getWorld()).getName());
        rawLocation.setX(location.getX());
        rawLocation.setY(location.getY());
        rawLocation.setZ(location.getZ());
        rawLocation.setPitch(location.getPitch());
        rawLocation.setYaw(location.getYaw());

        player.sendMessage("设置等待大厅成功!");
    }

    @Subcommand("toWorld {worldName}")
    public void toWorld(Player player, String worldName) {
        player.teleport(Objects.requireNonNull(Bukkit.getWorld(worldName)).getSpawnLocation());
    }

    @Subcommand("loadWorld {worldName}")
    public void loadWorld(Player player, String worldName) {
        WorldCreator creator = new WorldCreator(worldName);
        creator.environment(World.Environment.NORMAL);
        World mapWorld = Bukkit.createWorld(creator);

        if (mapWorld != null) {
            mapWorld.setAutoSave(false);
            mapWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            mapWorld.setGameRule(GameRule.DO_FIRE_TICK, false);
            mapWorld.setGameRuleValue("doMobSpawning", "false");
            mapWorld.setGameRuleValue("doFireTick", "false");
        }
    }

}
