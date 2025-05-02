package cc.azuramc.bedwars.command.admin;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.storage.IMapStorage;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.game.map.MapManager;
import cc.azuramc.bedwars.database.storage.MapStorageFactory;
import cc.azuramc.bedwars.util.ChatColorUtil;
import cc.azuramc.bedwars.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.List;

/**
 * @author an5w1r@163.com
 */
@Command("map")
@CommandPermission("azurabedwars.admin")
public class MapCommand {

    @Dependency
    private AzuraBedWars pluginDependency;

    private final AzuraBedWars plugin = AzuraBedWars.getInstance();
    private final MapManager mapManager = AzuraBedWars.getInstance().getMapManager();

    @DefaultFor("map")
    public void getMapCommand(BukkitCommandActor actor) {
        CommandUtil.sendLayout(actor, ChatColorUtil.CHAT_BAR);
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&b&lAzuraBedWars &8- &7v" + plugin.getDescription().getVersion() + " &8- &b起床战争 - 地图设置"));
        CommandUtil.sendLayout(actor, "");
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map create <mapName> &7创建新的地图"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map setWaiting <mapName> &7设置等待大厅位置"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map setAuthor <mapName> <authorName> &7设置地图作者名"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map setTeamPlayers <mapName> <number> &7设置队伍最大人数"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map setMinPlayers <mapName> <number> &7设置地图最小需要人数"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map setRespawn <mapName> &7设置地图重生点"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map addBase <mapName> &7增加基地出生点"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map addDrop <mapName> <type> &7增加资源点 (类型: BASE/DIAMOND/EMERALD)"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map addShop <mapName> <type> &7增加商店 (类型: ITEM/UPGRADE)"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map setPos1 <mapName> &7设置地图边界1"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map setPos2 <mapName> &7设置地图边界2"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map setUrl <url> &7设置地图文件物理路径"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map save <mapName> &7保存地图"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map load <mapName> &7加载地图配置"));
        CommandUtil.sendLayout(actor, "");
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map list <type> &7查看指定存储方式地图列表"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map migrate &7迁移所有地图数据存储方式 (类型: JSON/MYSQL)"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map migrate <源类型> <目标类型> [mapName] &7迁移地图存储方式"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map info <mapName> &7查看地图信息"));
        CommandUtil.sendLayout(actor, ChatColorUtil.CHAT_BAR);
    }

    @Subcommand("check")
    public void checkHashMap(BukkitCommandActor actor) {
        CommandUtil.sendLayout(actor, mapManager.getLoadedMaps().toString());
    }

    @Subcommand("create")
    public void createMap(BukkitCommandActor actor, String mapName) {
        mapManager.getLoadedMaps().put(mapName, new MapData(mapName));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&a地图创建成功!"));
    }

    @Subcommand("setWaiting")
    public void setWaiting(Player player, String mapName) {
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        mapData.setWaitingLocation(player.getLocation());
        player.sendMessage(ChatColorUtil.color("&a地图等待大厅设置成功!"));
    }

    @Subcommand("setRespawn")
    public void setRespawn(Player player, String mapName) {
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        mapData.setRespawnLocation(player.getLocation());
        player.sendMessage(ChatColorUtil.color("&a地图重生点设置成功!"));
    }

    @Subcommand("addBase")
    public void addBase(Player player, String mapName) {
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        mapData.addBase(player.getLocation());
        player.sendMessage(ChatColorUtil.color("&a基地出生点增加成功!"));
    }

    @Subcommand("setPos1")
    public void setPos1(Player player, String mapName) {
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        mapData.setPos1(player.getLocation());
        player.sendMessage(ChatColorUtil.color("&a设置地图边界 1 成功!"));
    }

    @Subcommand("setPos2")
    public void setPos2(Player player, String mapName) {
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        mapData.setPos2(player.getLocation());
        player.sendMessage(ChatColorUtil.color("&a设置地图边界 2 成功!"));
    }

    @Subcommand("setAuthor")
    public void setAuthor(BukkitCommandActor actor, String mapName, String authorName) {
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        mapData.setAuthor(authorName);
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&a地图作者设置成功!"));
    }

    @Subcommand("setTeamPlayers")
    public void setTeamPlayers(BukkitCommandActor actor, String mapName, int value) {
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        mapData.getPlayers().setTeam(value);
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&a队伍最大人数设置成功!"));
    }

    @Subcommand("setMinPlayers")
    public void setMinPlayers(BukkitCommandActor actor, String mapName, int value) {
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        mapData.getPlayers().setMin(value);
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&a地图最小人数置成功!"));
    }

    @Subcommand("addDrop")
    public void addDrop(Player player, String mapName, String value) {
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        mapData.addDrop(MapData.DropType.valueOf(value.toUpperCase()), player.getLocation());
        switch (MapData.DropType.valueOf(value)) {
            case BASE -> player.sendMessage(ChatColorUtil.color("&a成功添加基地资源点"));
            case DIAMOND -> player.sendMessage(ChatColorUtil.color("&a成功添加钻石资源点"));
            case EMERALD -> player.sendMessage(ChatColorUtil.color("&a成功添加绿宝石资源点"));
            default -> player.sendMessage(ChatColorUtil.color("&c添加资源点失败"));
        }
    }


    @Subcommand("setUrl")
    public void setUrl(BukkitCommandActor actor, String mapName, String fileUrl) {
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        if (mapData == null) {
            CommandUtil.sendLayout(actor, ChatColorUtil.color("&c找不到地图: " + mapName));
            return;
        }

        mapData.setFileUrl(fileUrl);
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&a地图文件URL设置成功!"));
    }

    @Subcommand("addShop")
    public void addShop(Player player, String mapName, String value) {
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        MapData.ShopType shopType = MapData.ShopType.valueOf(value.toUpperCase());
        mapData.addShop(shopType, player.getLocation());
        if (shopType == MapData.ShopType.ITEM) {
            player.sendMessage(ChatColorUtil.color("&a成功设置物品商店!"));
        } else {
            player.sendMessage(ChatColorUtil.color("&a成功设置团队升级!"));
        }
    }

    @Subcommand("preloadMap")
    public void preloadMap(BukkitCommandActor actor, String mapName) {
        mapManager.getAndLoadMapData(mapName);
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&a地图预加载完成"));
    }

    @Subcommand("list")
    public void list(BukkitCommandActor actor, String type) {
        MapStorageFactory.StorageType storageType = MapStorageFactory.StorageType.JSON;

        try {
            storageType = MapStorageFactory.StorageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            CommandUtil.sendLayout(actor, ChatColorUtil.color("&c无效的存储类型: " + type));
        }

        IMapStorage storage = MapStorageFactory.getStorage(storageType);
        List<String> mapNames = storage.getAllMapNames();

        CommandUtil.sendLayout(actor, ChatColorUtil.color("&a存储类型 [" + storageType.name() + "] 中的地图列表 (" + mapNames.size() + "):"));

        if (mapNames.isEmpty()) {
            CommandUtil.sendLayout(actor, ChatColorUtil.color("&e没有找到地图"));
        } else {
            for (String mapName : mapNames) {
                CommandUtil.sendLayout(actor, ChatColor.YELLOW + "  - " + mapName);
            }
        }
    }

    @Subcommand("migrate")
    public void migrate(BukkitCommandActor actor, String sourceTypeString, String targetTypeString, String mapName) {

        MapStorageFactory.StorageType sourceTypeEnum;
        MapStorageFactory.StorageType targetTypeEnum;

        try {
            sourceTypeEnum = MapStorageFactory.StorageType.valueOf(sourceTypeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            CommandUtil.sendLayout(actor, ChatColor.RED + "无效的源存储类型: " + sourceTypeString);
            return;
        }

        try {
            targetTypeEnum = MapStorageFactory.StorageType.valueOf(targetTypeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            CommandUtil.sendLayout(actor, ChatColor.RED + "无效的目标存储类型: " + targetTypeString);
            return;
        }

        if (sourceTypeEnum == targetTypeEnum) {
            CommandUtil.sendLayout(actor, ChatColor.RED + "源存储类型和目标存储类型不能相同");
            return;
        }


        if (!MapStorageFactory.getStorage(sourceTypeEnum).exists(mapName)) {
            CommandUtil.sendLayout(actor, ChatColor.RED + "找不到地图: " + mapName);
            return;
        }

        CommandUtil.sendLayout(actor, ChatColor.YELLOW + "开始迁移地图数据从 " + sourceTypeEnum + " 到 " + targetTypeEnum +
                (mapName != null ? " (地图: " + mapName + ")" : " (所有地图)"));

        boolean success = MapStorageFactory.migrateStorage(sourceTypeEnum, targetTypeEnum, mapName);

        if (success) {
            CommandUtil.sendLayout(actor, ChatColor.GREEN + "地图数据迁移成功");
        } else {
            CommandUtil.sendLayout(actor, ChatColor.RED + "地图数据迁移失败，请检查控制台日志");
        }
    }

    @Subcommand("info")
    public void info(BukkitCommandActor actor, String mapName) {
        MapData mapData = mapManager.getAndLoadMapData(mapName);
        if (mapData == null) {
            CommandUtil.sendLayout(actor, ChatColor.RED + "找不到地图: " + mapName);
            return;
        }
        CommandUtil.sendLayout(actor, ChatColorUtil.CHAT_BAR);
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&b地图信息"));
        CommandUtil.sendLayout(actor, " ");
        CommandUtil.sendLayout(actor, ChatColorUtil.color(" &9&l▸ &f名称: &b" + mapData.getName()));
        CommandUtil.sendLayout(actor, ChatColorUtil.color(" &9&l▸ &f作者: &b" + mapData.getAuthor()));
        CommandUtil.sendLayout(actor, ChatColorUtil.color(" &9&l▸ &f地图最小人数: &b" + mapData.getPlayers().getMin()));
        CommandUtil.sendLayout(actor, ChatColorUtil.color(" &9&l▸ &f队伍最大人数: &b" + mapData.getPlayers().getTeam()));
        CommandUtil.sendLayout(actor, " ");
        CommandUtil.sendLayout(actor, ChatColorUtil.color(" &9&l▸ &fPos1: &b" + mapData.getPos1Location()));
        CommandUtil.sendLayout(actor, ChatColorUtil.color(" &9&l▸ &fPos2: &b" + mapData.getPos2Location()));
        CommandUtil.sendLayout(actor, ChatColorUtil.color(" &9&l▸ &f大厅位置: &b" + mapData.getWaitingLocation()));
        CommandUtil.sendLayout(actor, ChatColorUtil.color(" &9&l▸ &f复活时位置: &b" + mapData.getRespawnLocation()));
        CommandUtil.sendLayout(actor, ChatColorUtil.color(" &9&l▸ &f基地出生点: &b" + mapData.getBases()));
        CommandUtil.sendLayout(actor, ChatColorUtil.color(" &9&l▸ &f基地资源点: &b" + mapData.getDrops(MapData.DropType.BASE)));
        CommandUtil.sendLayout(actor, ChatColorUtil.color(" &9&l▸ &f钻石资源点: &b" + mapData.getDrops(MapData.DropType.DIAMOND)));
        CommandUtil.sendLayout(actor, ChatColorUtil.color(" &9&l▸ &f绿宝石资源点: &b" + mapData.getDrops(MapData.DropType.EMERALD)));
        CommandUtil.sendLayout(actor, " ");
        CommandUtil.sendLayout(actor, ChatColorUtil.color(" &9&l▸ &f地图文件物理地址: &b" + mapData.getFileUrl()));
        CommandUtil.sendLayout(actor, ChatColorUtil.CHAT_BAR);
    }

    @Subcommand("save")
    public void save(BukkitCommandActor actor, String mapName) {
        if (mapManager.saveMapData(mapName)) {
            CommandUtil.sendLayout(actor, ChatColorUtil.color("&a保存成功!"));
            return;
        }
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&c地图保存失败!"));
    }

    @Subcommand("load")
    public void load(BukkitCommandActor actor, String mapName) {
        mapManager.getAndLoadMapData(mapName);
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&a地图加载成功!"));
    }
}
