package cc.azuramc.bedwars.commands.admin.map;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.map.IMapStorage;
import cc.azuramc.bedwars.map.data.MapData;
import cc.azuramc.bedwars.map.MapManager;
import cc.azuramc.bedwars.map.MapStorageFactory;
import cc.azuramc.bedwars.utils.CC;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.List;


@Command({"bedwars map", "bw map", "azurabedwars map"})
@CommandPermission("azurabedwars.admin")
public class MapCommand {

    @Dependency
    private AzuraBedWars pluginDependency;

    private final AzuraBedWars plugin = AzuraBedWars.getInstance();
    private final MapManager mapManager = AzuraBedWars.getInstance().getMapManager();

    @DefaultFor({"bedwars map", "bw map", "azurabedwars map"})
    public void getMapCommand(Player player) {
        player.sendMessage(CC.CHAT_BAR);
        player.sendMessage(CC.color("&b&lAzuraBedWars &8- &7v" + plugin.getDescription().getVersion() + " &8- &b起床战争 - 地图设置"));
        player.sendMessage("");
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> create &7创建新的地图"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> setWaiting &7设置等待大厅位置"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> setAuthor <authorName> &7设置地图作者名"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> setTeamPlayers <number> &7设置队伍最大人数"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> setMinPlayers <number> &7设置地图最小需要人数"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> setRespawn &7设置地图重生点"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> addBase &7增加基地出生点"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> addDrop <type> &7增加资源点 (类型: BASE/DIAMOND/EMERALD)"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> addShop <type> &7增加商店 (类型: ITEM/UPGRADE)"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> setPos1 &7设置地图边界1"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> setPos2 &7设置地图边界2"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> save &7保存地图"));
        player.sendMessage(CC.color("&7 • &f/bw map <mapName> load &7加载地图配置"));
        player.sendMessage("");
        player.sendMessage(CC.color("&7 • &f/bw map list <type> &7查看指定存储方式地图列表"));
        player.sendMessage(CC.color("&7 • &f/bw map migrate &7迁移所有地图数据存储方式 (类型: JSON/MYSQL)"));
        player.sendMessage(CC.color("&7 • &f/bw map migrate <源类型> <目标类型> [mapName] &7迁移地图存储方式"));
        player.sendMessage(CC.color("&7 • &f/bw map info <mapName> &7查看地图信息"));
        player.sendMessage(CC.CHAT_BAR);
    }

    @Subcommand("<mapName> create")
    public void createMap(Player player, String mapName) {
        mapManager.getLoadedMaps().put(mapName, new MapData());
    }

    @Subcommand("<mapName> setWaiting")
    public void setWaiting(Player player, String mapName) {
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        mapData.setWaitingLocation(player.getLocation());
        player.sendMessage(CC.color("&a地图等待大厅设置成功!"));
    }

    @Subcommand("<mapName> setRespawn")
    public void setRespawn(Player player, String mapName) {
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        mapData.setReSpawn(player.getLocation());
        player.sendMessage(CC.color("&a地图重生点设置成功!"));
    }

    @Subcommand("<mapName> addBase")
    public void addBase(Player player, String mapName) {
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        mapData.addBase(player.getLocation());
        player.sendMessage(CC.color("&a基地出生点增加成功!"));
    }

    @Subcommand("<mapName> setPos1")
    public void setPos1(Player player, String mapName) {
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        mapData.setPos1(player.getLocation());
        player.sendMessage(CC.color("&a设置地图边界 1 成功!"));
    }

    @Subcommand("<mapName> setPos2")
    public void setPos2(Player player, String mapName) {
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        mapData.setPos2(player.getLocation());
        player.sendMessage(CC.color("&a设置地图边界 2 成功!"));
    }

    @Subcommand("<mapName> setRespawn {authorName}")
    public void setRespawn(Player player, String mapName, String authorName) {
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        mapData.setAuthor(authorName);
        player.sendMessage(CC.color("&a地图作者设置成功!"));
    }

    @Subcommand("<mapName> setTeamPlayers <value>")
    public void setTeamPlayers(Player player, String mapName, int value) {
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        mapData.getPlayers().setTeam(value);
        player.sendMessage(CC.color("&a队伍最大人数设置成功!"));
    }

    @Subcommand("<mapName> setMinPlayers <value>")
    public void setMinPlayers(Player player, String mapName, int value) {
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        mapData.getPlayers().setMin(value);
        player.sendMessage(CC.color("&a地图最小人数置成功!"));
    }

    @Subcommand("<mapName> addDropLoc <value>")
    public void addDropLoc(Player player, String mapName, String value) {
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        mapData.addDrop(MapData.DropType.valueOf(value.toUpperCase()), player.getLocation());
        player.sendMessage(CC.color("&a增加基地掉落点成功!"));
    }

    @Subcommand("<mapName> addShop <value>")
    public void addShop(Player player, String mapName, String value) {
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        mapData.addShop(MapData.ShopType.valueOf(value.toUpperCase()), player.getLocation());
        player.sendMessage(CC.color("&a增加商人成功!"));
    }

    @Subcommand("list <type>")
    public void list(Player player, String type) {
        MapStorageFactory.StorageType storageType = MapStorageFactory.StorageType.JSON;

        try {
            storageType = MapStorageFactory.StorageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(CC.color("&c无效的存储类型: " + type));
        }

        IMapStorage storage = MapStorageFactory.getStorage(storageType);
        List<String> mapNames = storage.getAllMapNames();

        player.sendMessage(CC.color("&a存储类型 [" + storageType.name() + "] 中的地图列表 (" + mapNames.size() + "):"));

        if (mapNames.isEmpty()) {
            player.sendMessage(CC.color("&e没有找到地图"));
        } else {
            for (String mapName : mapNames) {
                player.sendMessage(ChatColor.YELLOW + "  - " + mapName);
            }
        }
    }

    @Subcommand("migrate <sourceType> <targetType> <mapName>")
    public void migrate(Player player, String sourceTypeString, String targetTypeString, String mapName) {

        MapStorageFactory.StorageType sourceTypeEnum;
        MapStorageFactory.StorageType targetTypeEnum;

        try {
            sourceTypeEnum = MapStorageFactory.StorageType.valueOf(sourceTypeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "无效的源存储类型: " + sourceTypeString);
            return;
        }

        try {
            targetTypeEnum = MapStorageFactory.StorageType.valueOf(targetTypeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "无效的目标存储类型: " + targetTypeString);
            return;
        }

        if (sourceTypeEnum == targetTypeEnum) {
            player.sendMessage(ChatColor.RED + "源存储类型和目标存储类型不能相同");
            return;
        }


        if (!MapStorageFactory.getStorage(sourceTypeEnum).exists(mapName)) {
            player.sendMessage(ChatColor.RED + "找不到地图: " + mapName);
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "开始迁移地图数据从 " + sourceTypeEnum + " 到 " + targetTypeEnum +
                (mapName != null ? " (地图: " + mapName + ")" : " (所有地图)"));

        boolean success = MapStorageFactory.migrateStorage(sourceTypeEnum, targetTypeEnum, mapName);

        if (success) {
            player.sendMessage(ChatColor.GREEN + "地图数据迁移成功");
        } else {
            player.sendMessage(ChatColor.RED + "地图数据迁移失败，请检查控制台日志");
        }

    }

    @Subcommand("info <mapName>")
    public void info(Player player, String mapName) {
        MapData mapData = MapStorageFactory.getDefaultStorage().loadMap(mapName);
        if (mapData == null) {
            player.sendMessage(ChatColor.RED + "找不到地图: " + mapName);
            return;
        }
        player.sendMessage(CC.CHAT_BAR);
        player.sendMessage(CC.color("&b地图信息"));
        player.sendMessage(" ");
        player.sendMessage(CC.color(" &9&l▸ &f名称: &b" + mapData.getName()));
        player.sendMessage(CC.color(" &9&l▸ &f作者: &b" + mapData.getAuthor()));
        player.sendMessage(CC.color(" &9&l▸ &f地图最小人数: &b" + mapData.getPlayers().getMin()));
        player.sendMessage(CC.color(" &9&l▸ &f队伍最大人数: &b" + mapData.getPlayers().getTeam()));
        player.sendMessage(" ");
        player.sendMessage(CC.color(" &9&l▸ &f基地出生点: &b" + mapData.getBases()));
        player.sendMessage(CC.color(" &9&l▸ &f基地资源点: &b" + mapData.getDrops(MapData.DropType.BASE)));
        player.sendMessage(CC.color(" &9&l▸ &f钻石资源点: &b" + mapData.getDrops(MapData.DropType.DIAMOND)));
        player.sendMessage(CC.color(" &9&l▸ &f绿宝石资源点: &b" + mapData.getDrops(MapData.DropType.EMERALD)));
        player.sendMessage(CC.CHAT_BAR);
    }

    @Subcommand("<mapName} save")
    public void save(Player player, String mapName) {
        if (mapManager.saveMapData(mapName)) {
            player.sendMessage(CC.color("&a保存成功!"));
            return;
        }
        player.sendMessage(CC.color("&c地图保存失败!"));
    }

    @Subcommand("<mapName} load")
    public void load(Player player, String mapName) {
        mapManager.getMapData(mapName);
    }

}
