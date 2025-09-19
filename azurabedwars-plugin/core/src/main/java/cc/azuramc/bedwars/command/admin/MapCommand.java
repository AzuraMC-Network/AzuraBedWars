package cc.azuramc.bedwars.command.admin;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.storage.IMapStorage;
import cc.azuramc.bedwars.database.storage.MapStorageFactory;
import cc.azuramc.bedwars.database.storage.MapStorageType;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.game.map.MapManager;
import cc.azuramc.bedwars.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.ArrayList;
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

    /**
     * 检查是否处于编辑模式，如果不是则向用户发送消息并返回true
     */
    private boolean checkEditorMode(Player player) {
        if (!plugin.getSettingsConfig().isEditorMode()) {
            player.sendMessage(MessageUtil.color("&c错误: 该命令只能在编辑模式下使用"));
            player.sendMessage(MessageUtil.color("&c请在配置文件中设置 editorMode: true 并重启服务器"));
            return true;
        }
        return false;
    }

    private boolean validateMapName(Player player, String mapName) {
        if (mapName == null || mapName.trim().isEmpty()) {
            player.sendMessage(MessageUtil.color("&c地图名称不能为空!"));
            return true;
        }
        if (mapName.length() > 30) {
            player.sendMessage(MessageUtil.color("&c地图名称长度不能超过30个字符!"));
            return true;
        }
        if (!mapName.matches("^[a-zA-Z0-9_\\-]+$")) {
            player.sendMessage(MessageUtil.color("&c地图名称只能包含字母、数字、下划线和连字符!"));
            return true;
        }
        return false;
    }

    @DefaultFor("map")
    public void getMapCommand(Player player) {
        if (checkEditorMode(player)) {
            return;
        }

        List<String> messageList = MessageUtil.color(List.of(
                MessageUtil.CHAT_BAR,
                "&b&lAzuraBedWars &8- &7v" + plugin.getDescription().getVersion() + " &8- &b起床战争 - 地图设置",
                "",
                "&7 • &f/map create <mapName> &7创建新的地图",
                "&7 • &f/map setWaiting <mapName> &7设置等待大厅位置",
                "&7 • &f/map setAuthor <mapName> <authorName> &7设置地图作者名",
                "&7 • &f/map setTeamPlayers <mapName> <number> &7设置队伍最大人数",
                "&7 • &f/map setMinPlayers <mapName> <number> &7设置地图最小需要人数",
                "&7 • &f/map setRespawn <mapName> &7设置地图重生点",
                "&7 • &f/map addBase <mapName> &7增加基地出生点",
                "&7 • &f/map addDrop <mapName> <type> &7增加资源点 (类型: BASE/DIAMOND/EMERALD)",
                "&7 • &f/map addShop <mapName> <type> &7增加商店 (类型: ITEM/UPGRADE)",
                "&7 • &f/map setPos1 <mapName> &7设置地图边界1",
                "&7 • &f/map setPos2 <mapName> &7设置地图边界2",
                "&7 • &f/map setUrl <url> &7设置地图文件物理路径",
                "&7 •  &9&l▸ &f可用变量: ",
                "&7 •  &9&l▸ &f- {$baseDir}: 插件根目录(plugins文件夹)",
                "&7 •  &9&l▸ &f- {$pluginDir}: 插件数据文件夹(plugins/AzuraBedWars)",
                "&7 •  &9&l▸ &f- {$serverDir}: 服务器根目录",
                "&7 •",
                "&7 • &f/map save <mapName> &7保存地图",
                "&7 • &f/map load <mapName> &7加载地图配置",
                "&7 •",
                "&7 • &f/map list <type> &7查看指定存储方式地图列表",
                "&7 • &f/map migrate &7迁移所有地图数据存储方式 (类型: JSON/MYSQL)",
                "&7 • &f/map migrate <源类型> <目标类型> [mapName] &7迁移地图存储方式",
                "&7 • &f/map info <mapName> &7查看地图信息",
                "&7 • &f/map align &7对齐玩家位置和视角为整数值",
                MessageUtil.CHAT_BAR
        ));

        messageList.forEach(player::sendMessage);
    }

    @Subcommand("check")
    public void checkHashMap(Player player) {
        if (checkEditorMode(player)) {
            return;
        }
        player.sendMessage(mapManager.getLoadedMaps().toString());
    }

    @Subcommand("create")
    public void createMap(Player player, String mapName) {
        if (checkEditorMode(player)) {
            return;
        }
        if (mapName == null || mapName.trim().isEmpty()) {
            player.sendMessage(MessageUtil.color("&c地图名称不能为空!"));
            return;
        }
        if (mapName.length() > 30) {
            player.sendMessage(MessageUtil.color("&c地图名称长度不能超过30个字符!"));
            return;
        }
        if (!mapName.matches("^[a-zA-Z0-9_\\-]+$")) {
            player.sendMessage(MessageUtil.color("&c地图名称只能包含字母、数字、下划线和连字符!"));
            return;
        }
        if (mapManager.getLoadedMaps().containsKey(mapName)) {
            player.sendMessage(MessageUtil.color("&c地图 '" + mapName + "' 已存在!"));
            return;
        }
        mapManager.getLoadedMaps().put(mapName, new MapData(mapName));
        mapManager.saveMapData(mapName);
        player.sendMessage(MessageUtil.color("&a地图创建成功!"));
    }

    @Subcommand("setWaiting")
    public void setWaiting(Player player, String mapName) {
        if (checkEditorMode(player) || validateMapName(player, mapName)) {
            return;
        }
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        if (mapData == null) {
            player.sendMessage(MessageUtil.color("&c找不到地图: " + mapName));
            return;
        }
        mapData.setWaitingLocation(player.getLocation());
        player.sendMessage(MessageUtil.color("&a地图等待大厅设置成功!"));
    }

    @Subcommand("setRespawn")
    public void setRespawn(Player player, String mapName) {
        if (checkEditorMode(player) || validateMapName(player, mapName)) {
            return;
        }
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        if (mapData == null) {
            player.sendMessage(MessageUtil.color("&c找不到地图: " + mapName));
            return;
        }
        mapData.setRespawnLocation(player.getLocation());
        player.sendMessage(MessageUtil.color("&a地图重生点设置成功!"));
    }

    @Subcommand("addBase")
    public void addBase(Player player, String mapName) {
        if (checkEditorMode(player) || validateMapName(player, mapName)) {
            return;
        }
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        if (mapData == null) {
            player.sendMessage(MessageUtil.color("&c找不到地图: " + mapName));
            return;
        }
        mapData.addBase(player.getLocation());
        player.sendMessage(MessageUtil.color("&a基地出生点增加成功!"));
    }

    @Subcommand("setPos1")
    public void setPos1(Player player, String mapName) {
        if (checkEditorMode(player) || validateMapName(player, mapName)) {
            return;
        }
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        if (mapData == null) {
            player.sendMessage(MessageUtil.color("&c找不到地图: " + mapName));
            return;
        }
        mapData.setPos1(player.getLocation());
        player.sendMessage(MessageUtil.color("&a设置地图边界 1 成功!"));
    }

    @Subcommand("setPos2")
    public void setPos2(Player player, String mapName) {
        if (checkEditorMode(player) || validateMapName(player, mapName)) {
            return;
        }
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        if (mapData == null) {
            player.sendMessage(MessageUtil.color("&c找不到地图: " + mapName));
            return;
        }
        mapData.setPos2(player.getLocation());
        player.sendMessage(MessageUtil.color("&a设置地图边界 2 成功!"));
    }

    @Subcommand("setAuthor")
    public void setAuthor(Player player, String mapName, String authorName) {
        if (checkEditorMode(player) || validateMapName(player, mapName)) {
            return;
        }
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        if (mapData == null) {
            player.sendMessage(MessageUtil.color("&c找不到地图: " + mapName));
            return;
        }
        if (authorName == null || authorName.trim().isEmpty()) {
            player.sendMessage(MessageUtil.color("&c作者名不能为空!"));
            return;
        }
        if (authorName.length() > 24) {
            player.sendMessage(MessageUtil.color("&c作者名长度不能超过24个字符!"));
            return;
        }
        mapData.setAuthor(authorName.trim());
        player.sendMessage(MessageUtil.color("&a地图作者设置成功!"));
    }

    @Subcommand("setTeamPlayers")
    public void setTeamPlayers(Player player, String mapName, int value) {
        if (checkEditorMode(player) || validateMapName(player, mapName)) {
            return;
        }
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        if (mapData == null) {
            player.sendMessage(MessageUtil.color("&c找不到地图: " + mapName));
            return;
        }
        if (value < 1 || value > 8) {
            player.sendMessage(MessageUtil.color("&c队伍人数必须在1-8之间!"));
            return;
        }
        mapData.getPlayers().setTeam(value);
        player.sendMessage(MessageUtil.color("&a队伍最大人数设置成功!"));
    }

    @Subcommand("setMinPlayers")
    public void setMinPlayers(Player player, String mapName, int value) {
        if (checkEditorMode(player) || validateMapName(player, mapName)) {
            return;
        }
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        if (mapData == null) {
            player.sendMessage(MessageUtil.color("&c找不到地图: " + mapName));
            return;
        }
        if (value < 2 || value > 16) {
            player.sendMessage(MessageUtil.color("&c最小人数必须在2-16之间!"));
            return;
        }
        mapData.getPlayers().setMin(value);
        player.sendMessage(MessageUtil.color("&a地图最小人数设置成功!"));
    }

    @Subcommand("addDrop")
    public void addDrop(Player player, String mapName, String value) {
        if (checkEditorMode(player) || validateMapName(player, mapName)) {
            return;
        }
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        if (mapData == null) {
            player.sendMessage(MessageUtil.color("&c找不到地图: " + mapName));
            return;
        }

        try {
            MapData.DropType dropType = MapData.DropType.valueOf(value.toUpperCase());
            mapData.addDrop(dropType, player.getLocation());
            switch (dropType) {
                case BASE -> player.sendMessage(MessageUtil.color("&a成功添加基地资源点"));
                case DIAMOND -> player.sendMessage(MessageUtil.color("&a成功添加钻石资源点"));
                case EMERALD -> player.sendMessage(MessageUtil.color("&a成功添加绿宝石资源点"));
                default -> player.sendMessage(MessageUtil.color("&c添加资源点失败"));
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage(MessageUtil.color("&c无效的资源点类型: " + value));
            player.sendMessage(MessageUtil.color("&7有效的类型: &aBASE&7, &aDIAMOND&7, &aEMERALD"));
        }
    }


    @Subcommand("setUrl")
    public void setUrl(Player player, String mapName, String fileUrl) {
        if (checkEditorMode(player) || validateMapName(player, mapName)) {
            return;
        }
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        if (mapData == null) {
            player.sendMessage(MessageUtil.color("&c找不到地图: " + mapName));
            return;
        }
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            player.sendMessage(MessageUtil.color("&c文件URL不能为空!"));
            return;
        }
        if (fileUrl.length() > 200) {
            player.sendMessage(MessageUtil.color("&c文件URL长度不能超过200个字符!"));
            return;
        }

        mapData.setFileUrl(fileUrl.trim());
        player.sendMessage(MessageUtil.color("&a地图文件URL设置成功!"));
    }

    @Subcommand("addShop")
    public void addShop(Player player, String mapName, String value) {
        if (checkEditorMode(player) || validateMapName(player, mapName)) {
            return;
        }
        MapData mapData = mapManager.getLoadedMaps().get(mapName);
        if (mapData == null) {
            player.sendMessage(MessageUtil.color("&c找不到地图: " + mapName));
            return;
        }

        try {
            MapData.ShopType shopType = MapData.ShopType.valueOf(value.toUpperCase());
            mapData.addShop(shopType, player.getLocation());
            if (shopType == MapData.ShopType.ITEM) {
                player.sendMessage(MessageUtil.color("&a成功设置物品商店!"));
            } else {
                player.sendMessage(MessageUtil.color("&a成功设置团队升级!"));
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage(MessageUtil.color("&c无效的商店类型: " + value));
            player.sendMessage(MessageUtil.color("&7有效的类型: &aITEM&7, &aUPGRADE"));
        }
    }

    @Subcommand("preloadMap")
    public void preloadMap(Player player, String mapName) {
        if (checkEditorMode(player) || validateMapName(player, mapName)) {
            return;
        }
        mapManager.getAndLoadMapData(mapName);
        player.sendMessage(MessageUtil.color("&a地图预加载完成"));
    }

    @Subcommand("list")
    public void list(Player player, String type) {
        if (checkEditorMode(player)) {
            return;
        }
        MapStorageType storageType;

        try {
            storageType = MapStorageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(MessageUtil.color("&c无效的存储类型: " + type));
            player.sendMessage(MessageUtil.color("&7有效的类型: &aJSON&7, &aMYSQL"));
            return;
        }

        IMapStorage storage = MapStorageFactory.getStorage(storageType);
        List<String> mapNames = storage.getAllMapNames();

        player.sendMessage(MessageUtil.color("&a存储类型 [" + storageType.name() + "] 中的地图列表 (" + mapNames.size() + "):"));

        if (mapNames.isEmpty()) {
            player.sendMessage(MessageUtil.color("&e没有找到地图"));
        } else {
            for (String mapName : mapNames) {
                player.sendMessage(ChatColor.YELLOW + "  - " + mapName);
            }
        }
    }

    @Subcommand("migrate")
    public void migrate(Player player, String sourceTypeString, String targetTypeString, String mapName) {
        if (checkEditorMode(player)) {
            return;
        }

        MapStorageType sourceTypeEnum;
        MapStorageType targetTypeEnum;

        try {
            sourceTypeEnum = MapStorageType.valueOf(sourceTypeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "无效的源存储类型: " + sourceTypeString);
            return;
        }

        try {
            targetTypeEnum = MapStorageType.valueOf(targetTypeString.toUpperCase());
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

    @Subcommand("info")
    public void info(Player player, String mapName) {
        if (checkEditorMode(player) || validateMapName(player, mapName)) {
            return;
        }
        MapData mapData = mapManager.getAndLoadMapData(mapName);
        if (mapData == null) {
            player.sendMessage(ChatColor.RED + "找不到地图: " + mapName);
            return;
        }
        List<String> messageList = new ArrayList<>();
        messageList.addAll(MessageUtil.color(List.of(
                MessageUtil.CHAT_BAR,
                "&b地图信息",
                "",
                " &9&l▸ &f名称: &b" + mapData.getName(),
                " &9&l▸ &f作者: &b" + mapData.getAuthor(),
                " &9&l▸ &f地图最小人数: &b" + mapData.getPlayers().getMin(),
                " &9&l▸ &f队伍最大人数: &b" + mapData.getPlayers().getTeam(),
                "",
                " &9&l▸ &fPos1: " + MessageUtil.formatLocation(mapData.getRegion().getPos1()),
                " &9&l▸ &fPos2: " + MessageUtil.formatLocation(mapData.getRegion().getPos2()),
                " &9&l▸ &f大厅位置: " + MessageUtil.formatLocation(mapData.getWaitingLocation()),
                " &9&l▸ &f复活时位置: " + MessageUtil.formatLocation(mapData.getRespawnLocation()),
                "",
                " &9&l▸ &f基地出生点:"
        )));

        messageList.addAll(MessageUtil.color(MessageUtil.formatLocationList(mapData.getBases())));

        messageList.addAll(MessageUtil.color(List.of(
                "",
                " &9&l▸ &f基地资源点: &b" + mapData.getDrops(MapData.DropType.BASE),
                " &9&l▸ &f钻石资源点: &b" + mapData.getDrops(MapData.DropType.DIAMOND),
                " &9&l▸ &f绿宝石资源点: &b" + mapData.getDrops(MapData.DropType.EMERALD),
                "",
                " &7&l注意: &7坐标精度已限制为2位小数",
                "",
                " &9&l▸ &f地图文件物理地址: &b" + mapData.getProcessedFileUrl(),
                MessageUtil.CHAT_BAR
        )));

        messageList.forEach(player::sendMessage);
    }

    @Subcommand("save")
    public void save(Player player, String mapName) {
        if (checkEditorMode(player) || validateMapName(player, mapName)) {
            return;
        }
        if (mapManager.saveMapData(mapName)) {
            player.sendMessage(MessageUtil.color("&a保存成功!"));
            return;
        }
        player.sendMessage(MessageUtil.color("&c地图保存失败!"));
    }

    @Subcommand("load")
    public void load(Player player, String mapName) {
        if (checkEditorMode(player) || validateMapName(player, mapName)) {
            return;
        }
        mapManager.getAndLoadMapData(mapName);
        player.sendMessage(MessageUtil.color("&a地图加载成功!"));
    }

    @Subcommand("align")
    public void alignPlayer(Player player) {
        if (checkEditorMode(player)) {
            return;
        }
        // 获取当前位置
        Location loc = player.getLocation();

        // 对齐到最近的整数位置
        double x = Math.round(loc.getX());
        double y = Math.round(loc.getY());
        double z = Math.round(loc.getZ());

        // 对齐视角为90度的倍数 (0, 90, 180, 270)
        float yaw = Math.round(loc.getYaw() / 90) * 90;
        float pitch = Math.round(loc.getPitch() / 90) * 90;

        // 创建新位置
        Location newLoc = new Location(loc.getWorld(), x, y, z, yaw, pitch);

        // 传送玩家
        player.teleport(newLoc);

        player.sendMessage(MessageUtil.color("&a位置已对齐到整数坐标! &7X: " + x + ", Y: " + y + ", Z: " + z));
        player.sendMessage(MessageUtil.color("&a视角已对齐到90度! &7Yaw: " + yaw + ", Pitch: " + pitch));
    }
}
