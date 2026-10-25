package cc.azuramc.bedwars.command.admin;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.storage.IMapStorage;
import cc.azuramc.bedwars.database.storage.MapStorageFactory;
import cc.azuramc.bedwars.database.storage.MapStorageType;
import cc.azuramc.bedwars.game.map.MapCheck;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.game.map.MapManager;
import cc.azuramc.bedwars.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Every edit is saved to storage right away
 *
 * @author an5w1r@163.com
 */
@Command("map")
@CommandPermission("azurabedwars.admin")
public class MapCommand {

    private static final String NAME_PATTERN = "^[a-zA-Z0-9_\\-]{1,30}$";
    private static final double REMOVE_RANGE = 3;

    private final AzuraBedWars plugin = AzuraBedWars.getInstance();
    private final MapManager mapManager = plugin.getMapManager();

    @DefaultFor("map")
    public void help(CommandSender sender) {
        send(sender, Arrays.asList(
                MessageUtil.CHAT_BAR,
                "&b&lAzuraBedWars &8- &7v" + plugin.getPlugin().getDescription().getVersion() + " &8- &b起床战争 - 地图设置",
                "",
                "&7 • &f/map create <地图> &7创建地图",
                "&7 • &f/map setUrl <地图> <路径> &7设置地图世界文件夹",
                "&7 •  &9&l▸ &f可用变量 {$serverDir} 服务器根目录 {$baseDir} plugins {$pluginDir} plugins/AzuraBedWars",
                "&7 • &f/map setWaiting <地图> &7在脚下设置等待大厅",
                "&7 • &f/map setRespawn <地图> &7在脚下设置观战重生点",
                "&7 • &f/map setPos1 <地图> &7 /map setPos2 <地图> &7设置地图边界 较高的一角决定建筑限高",
                "&7 • &f/map addBase <地图> &7在脚下添加基地 颜色取附近羊毛",
                "&7 • &f/map addDrop <地图> <BASE/DIAMOND/EMERALD> &7添加资源点",
                "&7 • &f/map addShop <地图> <ITEM/UPGRADE> &7添加商店",
                "&7 • &f/map remove <地图> &7删除 " + (int) REMOVE_RANGE + " 格内最近的基地 资源点或商店",
                "&7 • &f/map setAuthor <地图> <作者> &7设置作者",
                "&7 • &f/map setTeamPlayers <地图> <人数> &7设置每队人数",
                "&7 • &f/map setMinPlayers <地图> <人数> &7设置开局最少人数",
                "&7 • &f/map check <地图> &7检查地图还缺什么",
                "&7 • &f/map info <地图> &7查看地图信息",
                "&7 • &f/map list [JSON/MYSQL] &7查看地图列表",
                "&7 • &f/map load <地图> &7从存储重新读取地图",
                "&7 • &f/map delete <地图> confirm &7删除地图数据 不删除世界文件",
                "&7 • &f/map migrate <源类型> <目标类型> [地图] &7迁移地图存储",
                "&7 • &f/map align &7把位置和视角对齐到整数",
                MessageUtil.CHAT_BAR
        ));
    }

    @Subcommand("create")
    public void create(CommandSender sender, String mapName) {
        if (!mapName.matches(NAME_PATTERN)) {
            reply(sender, "&c地图名只能包含字母 数字 下划线和连字符 最多 30 个字符");
            return;
        }
        if (mapManager.exists(mapName)) {
            reply(sender, "&c地图 " + mapName + " 已存在");
            return;
        }
        if (!mapManager.save(new MapData(mapName))) {
            reply(sender, "&c地图保存失败 详见控制台");
            return;
        }
        reply(sender, "&a地图 " + mapName + " 已创建 下一步 /map setUrl " + mapName + " <世界文件夹>");
    }

    @Subcommand("delete")
    @AutoComplete("@mapNames")
    public void delete(CommandSender sender, String mapName, @Default("") String confirm) {
        if (mapManager.getMap(mapName) == null) {
            reply(sender, "&c找不到地图 " + mapName);
            return;
        }
        if (!confirm.equals("confirm")) {
            reply(sender, "&e将删除地图 " + mapName + " 的配置 世界文件夹不受影响 确认请输入 /map delete " + mapName + " confirm");
            return;
        }
        reply(sender, mapManager.delete(mapName) ? "&a地图 " + mapName + " 已删除" : "&c删除失败 详见控制台");
    }

    @Subcommand("setUrl")
    @AutoComplete("@mapNames @pathVariables")
    public void setUrl(CommandSender sender, String mapName, String fileUrl) {
        edit(sender, mapName, map -> map.setFileUrl(fileUrl.trim()), map -> {
            String error = MapCheck.findWorldFolderError(map);
            return "地图文件夹已设置为 " + map.getWorldFolder() + (error == null ? "" : " &e但" + error);
        });
    }

    @Subcommand("setWaiting")
    @AutoComplete("@mapNames")
    public void setWaiting(Player player, String mapName) {
        edit(player, mapName, map -> map.setWaitingLocation(player.getLocation()), map -> "等待大厅已设置");
    }

    @Subcommand("setRespawn")
    @AutoComplete("@mapNames")
    public void setRespawn(Player player, String mapName) {
        edit(player, mapName, map -> map.setRespawnLocation(player.getLocation()), map -> "观战重生点已设置");
    }

    @Subcommand("setPos1")
    @AutoComplete("@mapNames")
    public void setPos1(Player player, String mapName) {
        edit(player, mapName, map -> map.setPos1(player.getLocation()), map -> "地图边界 1 已设置");
    }

    @Subcommand("setPos2")
    @AutoComplete("@mapNames")
    public void setPos2(Player player, String mapName) {
        edit(player, mapName, map -> map.setPos2(player.getLocation()), map -> "地图边界 2 已设置");
    }

    @Subcommand("addBase")
    @AutoComplete("@mapNames")
    public void addBase(Player player, String mapName) {
        edit(player, mapName, map -> map.addBase(player.getLocation()), map -> {
            int radius = plugin.getSettingsConfig().getTeamBlockSearchRadius();
            Object color = MapData.findWoolColor(player.getLocation(), radius);
            return "基地 #" + map.getBases().size() + " 已添加 " + (color == null ? "&e附近 " + radius + " 格内没有羊毛 开局将自动分配颜色" : "附近羊毛 " + color);
        });
    }

    @Subcommand("addDrop")
    @AutoComplete("@mapNames @dropTypes")
    public void addDrop(Player player, String mapName, String type) {
        MapData.DropType dropType = parse(player, MapData.DropType.class, type);
        if (dropType != null) {
            edit(player, mapName, map -> map.addDrop(dropType, player.getLocation()), map -> dropType + " 资源点已添加 共 " + map.getDrops(dropType) + " 个");
        }
    }

    @Subcommand("addShop")
    @AutoComplete("@mapNames @shopTypes")
    public void addShop(Player player, String mapName, String type) {
        MapData.ShopType shopType = parse(player, MapData.ShopType.class, type);
        if (shopType != null) {
            edit(player, mapName, map -> map.addShop(shopType, player.getLocation()), map -> shopType + " 商店已添加 共 " + map.getShops(shopType) + " 个");
        }
    }

    @Subcommand("remove")
    @AutoComplete("@mapNames")
    public void remove(Player player, String mapName) {
        MapData map = mapManager.getMap(mapName);
        if (map == null) {
            reply(player, "&c找不到地图 " + mapName);
            return;
        }
        MapData.RawLocation removed = map.removeNearest(player.getLocation(), REMOVE_RANGE);
        if (removed == null) {
            reply(player, "&c" + (int) REMOVE_RANGE + " 格内没有基地 资源点或商店");
            return;
        }
        String what = removed instanceof MapData.DropRawLocation ? ((MapData.DropRawLocation) removed).getDropType() + " 资源点"
                : removed instanceof MapData.ShopRawLocation ? ((MapData.ShopRawLocation) removed).getShopType() + " 商店" : "基地";
        reply(player, mapManager.save(map) ? "&a已删除 " + what + " " + MessageUtil.formatLocation(removed) : "&c地图保存失败 详见控制台");
    }

    @Subcommand("setAuthor")
    @AutoComplete("@mapNames *")
    public void setAuthor(CommandSender sender, String mapName, String author) {
        if (author.length() > 24) {
            reply(sender, "&c作者名最多 24 个字符");
            return;
        }
        edit(sender, mapName, map -> map.setAuthor(author), map -> "作者已设置为 " + author);
    }

    @Subcommand("setTeamPlayers")
    @AutoComplete("@mapNames *")
    public void setTeamPlayers(CommandSender sender, String mapName, int value) {
        if (value < 1 || value > 8) {
            reply(sender, "&c每队人数必须在 1 到 8 之间");
            return;
        }
        edit(sender, mapName, map -> map.getPlayers().setTeam(value), map -> "每队人数已设置为 " + value + " 地图容量 " + map.getMaxPlayers());
    }

    @Subcommand("setMinPlayers")
    @AutoComplete("@mapNames *")
    public void setMinPlayers(CommandSender sender, String mapName, int value) {
        if (value < 2) {
            reply(sender, "&c开局最少人数至少为 2");
            return;
        }
        edit(sender, mapName, map -> map.getPlayers().setMin(value), map -> "开局最少人数已设置为 " + value);
    }

    @Subcommand("check")
    @AutoComplete("@mapNames")
    public void check(CommandSender sender, String mapName) {
        MapData map = mapManager.getMap(mapName);
        if (map == null) {
            reply(sender, "&c找不到地图 " + mapName);
            return;
        }
        MapCheck check = MapCheck.of(map);
        List<String> lines = new ArrayList<>();
        lines.add(MessageUtil.CHAT_BAR);
        lines.add(check.getErrors().isEmpty() ? "&a地图 " + mapName + " 可以开始游戏" : "&c地图 " + mapName + " 还不能开始游戏");
        check.getErrors().forEach(error -> lines.add(" &c✘ " + error));
        check.getWarnings().forEach(warning -> lines.add(" &e! " + warning));
        check.getBaseLines().forEach(line -> lines.add(" &7" + line));
        lines.add(MessageUtil.CHAT_BAR);
        send(sender, lines);
    }

    @Subcommand("info")
    @AutoComplete("@mapNames")
    public void info(CommandSender sender, String mapName) {
        MapData map = mapManager.getMap(mapName);
        if (map == null) {
            reply(sender, "&c找不到地图 " + mapName);
            return;
        }
        List<String> lines = new ArrayList<>(Arrays.asList(
                MessageUtil.CHAT_BAR,
                "&b地图信息",
                "",
                " &9&l▸ &f名称: &b" + map.getName(),
                " &9&l▸ &f作者: &b" + map.getAuthor(),
                " &9&l▸ &f开局最少人数: &b" + map.getPlayers().getMin(),
                " &9&l▸ &f每队人数: &b" + map.getPlayers().getTeam() + " &7容量 " + map.getMaxPlayers(),
                " &9&l▸ &f建筑限高: &b" + map.getHigherY(),
                "",
                " &9&l▸ &fPos1: " + MessageUtil.formatLocation(map.getRegion().getPos1()),
                " &9&l▸ &fPos2: " + MessageUtil.formatLocation(map.getRegion().getPos2()),
                " &9&l▸ &f等待大厅: " + MessageUtil.formatLocation(map.getWaitingLocation()),
                " &9&l▸ &f观战重生点: " + MessageUtil.formatLocation(map.getRespawnLocation()),
                "",
                " &9&l▸ &f基地:"
        ));
        lines.addAll(MessageUtil.formatLocationList(map.getBases()));
        lines.addAll(Arrays.asList(
                "",
                " &9&l▸ &f基地资源点: &b" + map.getDrops(MapData.DropType.BASE),
                " &9&l▸ &f钻石资源点: &b" + map.getDrops(MapData.DropType.DIAMOND),
                " &9&l▸ &f绿宝石资源点: &b" + map.getDrops(MapData.DropType.EMERALD),
                " &9&l▸ &f物品商店: &b" + map.getShops(MapData.ShopType.ITEM),
                " &9&l▸ &f升级商店: &b" + map.getShops(MapData.ShopType.UPGRADE),
                "",
                " &9&l▸ &f地图文件夹: &b" + map.getWorldFolder(),
                MessageUtil.CHAT_BAR
        ));
        send(sender, lines);
    }

    @Subcommand("list")
    @AutoComplete("@storageTypes")
    public void list(CommandSender sender, @Default("CURRENT") String type) {
        MapStorageType storageType = type.equals("CURRENT") ? MapStorageFactory.getDefaultType() : parse(sender, MapStorageType.class, type);
        if (storageType == null || !isAvailable(sender, storageType)) {
            return;
        }
        List<String> mapNames = MapStorageFactory.getStorage(storageType).getAllMapNames();
        List<String> lines = new ArrayList<>(Arrays.asList(
                MessageUtil.CHAT_BAR,
                "&b存储类型 " + storageType + " 中的地图 共 " + mapNames.size() + " 个",
                ""
        ));
        if (mapNames.isEmpty()) {
            lines.add(" &e没有找到地图");
        }
        mapNames.forEach(name -> lines.add(" &7  - &a" + name));
        lines.add(MessageUtil.CHAT_BAR);
        send(sender, lines);
    }

    @Subcommand("load")
    @AutoComplete("@mapNames")
    public void load(CommandSender sender, String mapName) {
        reply(sender, mapManager.reload(mapName) != null ? "&a已从存储重新读取地图 " + mapName : "&c存储中找不到地图 " + mapName);
    }

    @Subcommand("migrate")
    @AutoComplete("@storageTypes @storageTypes @mapNames")
    public void migrate(CommandSender sender, String sourceType, String targetType, @Default("ALL") String mapName) {
        MapStorageType source = parse(sender, MapStorageType.class, sourceType);
        MapStorageType target = parse(sender, MapStorageType.class, targetType);
        if (source == null || target == null || !isAvailable(sender, source) || !isAvailable(sender, target)) {
            return;
        }
        if (source == target) {
            reply(sender, "&c源存储类型和目标存储类型不能相同");
            return;
        }
        IMapStorage sourceStorage = MapStorageFactory.getStorage(source);
        String onlyMap = mapName.equals("ALL") ? null : mapName;
        if (onlyMap != null && !sourceStorage.exists(onlyMap)) {
            reply(sender, "&c" + source + " 中找不到地图 " + onlyMap);
            return;
        }
        boolean success = MapStorageFactory.migrate(sourceStorage, MapStorageFactory.getStorage(target), onlyMap);
        reply(sender, success ? "&a已把" + (onlyMap == null ? "全部地图" : "地图 " + onlyMap) + "从 " + source + " 迁移到 " + target : "&c部分地图迁移失败 详见控制台");
    }

    @Subcommand("align")
    public void align(Player player) {
        Location location = player.getLocation();
        location.setX(Math.round(location.getX()));
        location.setY(Math.round(location.getY()));
        location.setZ(Math.round(location.getZ()));
        location.setYaw(Math.round(location.getYaw() / 90) * 90);
        location.setPitch(Math.round(location.getPitch() / 90) * 90);
        player.teleport(location);
        reply(player, "&a已对齐到 " + location.getX() + ", " + location.getY() + ", " + location.getZ()
                + " &7Yaw " + location.getYaw() + " Pitch " + location.getPitch());
    }

    private void edit(CommandSender sender, String mapName, Consumer<MapData> change, Function<MapData, String> done) {
        MapData map = mapManager.getMap(mapName);
        if (map == null) {
            reply(sender, "&c找不到地图 " + mapName + " 先用 /map create " + mapName + " 创建");
            return;
        }
        change.accept(map);
        if (!mapManager.save(map)) {
            reply(sender, "&c地图保存失败 详见控制台");
            return;
        }
        reply(sender, "&a" + done.apply(map));
    }

    private <E extends Enum<E>> E parse(CommandSender sender, Class<E> type, String value) {
        try {
            return Enum.valueOf(type, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            String valid = Arrays.stream(type.getEnumConstants()).map(Enum::name).collect(Collectors.joining(", "));
            reply(sender, "&c无效的类型 " + value + " 可选 " + valid);
            return null;
        }
    }

    private boolean isAvailable(CommandSender sender, MapStorageType type) {
        if (type == MapStorageType.MYSQL && !MapStorageFactory.isMysqlAvailable()) {
            reply(sender, "&cMYSQL 地图存储需要 database.databaseType 为 MySQL");
            return false;
        }
        return true;
    }

    private static void reply(CommandSender sender, String message) {
        sender.sendMessage(MessageUtil.color(message));
    }

    private static void send(CommandSender sender, List<String> lines) {
        MessageUtil.color(lines).forEach(sender::sendMessage);
    }
}
