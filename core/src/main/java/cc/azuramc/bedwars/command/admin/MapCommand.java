package cc.azuramc.bedwars.command.admin;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.LangConfig;
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
        List<String> lines = new ArrayList<>();
        lines.add(MessageUtil.CHAT_BAR);
        for (String line : lang().getHelp()) {
            lines.add(MessageUtil.fill(line, "version", plugin.getPlugin().getDescription().getVersion(), "range", (int) REMOVE_RANGE));
        }
        lines.add(MessageUtil.CHAT_BAR);
        send(sender, lines);
    }

    @Subcommand("create")
    public void create(CommandSender sender, String mapName) {
        if (!mapName.matches(NAME_PATTERN)) {
            reply(sender, lang().getInvalidName());
            return;
        }
        if (mapManager.exists(mapName)) {
            reply(sender, MessageUtil.fill(lang().getExists(), "map", mapName));
            return;
        }
        if (!mapManager.save(new MapData(mapName))) {
            reply(sender, lang().getSaveFailed());
            return;
        }
        reply(sender, MessageUtil.fill(lang().getCreated(), "map", mapName));
    }

    @Subcommand("delete")
    @AutoComplete("@mapNames")
    public void delete(CommandSender sender, String mapName, @Default("") String confirm) {
        if (mapManager.getMap(mapName) == null) {
            reply(sender, MessageUtil.fill(lang().getNotFound(), "map", mapName));
            return;
        }
        if (!confirm.equals("confirm")) {
            reply(sender, MessageUtil.fill(lang().getDeleteConfirm(), "map", mapName));
            return;
        }
        reply(sender, mapManager.delete(mapName) ? MessageUtil.fill(lang().getDeleted(), "map", mapName) : lang().getDeleteFailed());
    }

    @Subcommand("setUrl")
    @AutoComplete("@mapNames @pathVariables")
    public void setUrl(CommandSender sender, String mapName, String fileUrl) {
        edit(sender, mapName, map -> map.setFileUrl(fileUrl.trim()), map -> {
            String error = MapCheck.findWorldFolderError(map);
            return MessageUtil.fill(lang().getFolderSet(), "folder", map.getWorldFolder())
                    + (error == null ? "" : MessageUtil.fill(lang().getFolderProblem(), "problem", error));
        });
    }

    @Subcommand("setWaiting")
    @AutoComplete("@mapNames")
    public void setWaiting(Player player, String mapName) {
        edit(player, mapName, map -> map.setWaitingLocation(player.getLocation()), map -> lang().getWaitingSet());
    }

    @Subcommand("setRespawn")
    @AutoComplete("@mapNames")
    public void setRespawn(Player player, String mapName) {
        edit(player, mapName, map -> map.setRespawnLocation(player.getLocation()), map -> lang().getRespawnSet());
    }

    @Subcommand("setPos1")
    @AutoComplete("@mapNames")
    public void setPos1(Player player, String mapName) {
        edit(player, mapName, map -> map.setPos1(player.getLocation()), map -> lang().getPos1Set());
    }

    @Subcommand("setPos2")
    @AutoComplete("@mapNames")
    public void setPos2(Player player, String mapName) {
        edit(player, mapName, map -> map.setPos2(player.getLocation()), map -> lang().getPos2Set());
    }

    @Subcommand("addBase")
    @AutoComplete("@mapNames")
    public void addBase(Player player, String mapName) {
        edit(player, mapName, map -> map.addBase(player.getLocation()), map -> {
            int radius = plugin.getGameplayConfig().getTeamBlockSearchRadius();
            Object color = MapData.findWoolColor(player.getLocation(), radius);
            return color == null ? MessageUtil.fill(lang().getBaseAddedNoWool(), "number", map.getBases().size(), "radius", radius)
                    : MessageUtil.fill(lang().getBaseAdded(), "number", map.getBases().size(), "color", color);
        });
    }

    @Subcommand("addDrop")
    @AutoComplete("@mapNames @dropTypes")
    public void addDrop(Player player, String mapName, String type) {
        MapData.DropType dropType = parse(player, MapData.DropType.class, type);
        if (dropType != null) {
            edit(player, mapName, map -> map.addDrop(dropType, player.getLocation()), map -> MessageUtil.fill(lang().getDropAdded(), "type", dropType, "count", map.getDrops(dropType)));
        }
    }

    @Subcommand("addShop")
    @AutoComplete("@mapNames @shopTypes")
    public void addShop(Player player, String mapName, String type) {
        MapData.ShopType shopType = parse(player, MapData.ShopType.class, type);
        if (shopType != null) {
            edit(player, mapName, map -> map.addShop(shopType, player.getLocation()), map -> MessageUtil.fill(lang().getShopAdded(), "type", shopType, "count", map.getShops(shopType)));
        }
    }

    @Subcommand("remove")
    @AutoComplete("@mapNames")
    public void remove(Player player, String mapName) {
        MapData map = mapManager.getMap(mapName);
        if (map == null) {
            reply(player, MessageUtil.fill(lang().getNotFound(), "map", mapName));
            return;
        }
        MapData.RawLocation removed = map.removeNearest(player.getLocation(), REMOVE_RANGE);
        if (removed == null) {
            reply(player, MessageUtil.fill(lang().getNothingToRemove(), "range", (int) REMOVE_RANGE));
            return;
        }
        String what = removed instanceof MapData.DropRawLocation ? MessageUtil.fill(lang().getDrop(), "type", ((MapData.DropRawLocation) removed).getDropType())
                : removed instanceof MapData.ShopRawLocation ? MessageUtil.fill(lang().getShop(), "type", ((MapData.ShopRawLocation) removed).getShopType()) : lang().getBase();
        reply(player, mapManager.save(map) ? MessageUtil.fill(lang().getRemoved(), "what", what, "location", MessageUtil.formatLocation(removed)) : lang().getSaveFailed());
    }

    @Subcommand("setAuthor")
    @AutoComplete("@mapNames *")
    public void setAuthor(CommandSender sender, String mapName, String author) {
        if (author.length() > 24) {
            reply(sender, lang().getAuthorTooLong());
            return;
        }
        edit(sender, mapName, map -> map.setAuthor(author), map -> MessageUtil.fill(lang().getAuthorSet(), "author", author));
    }

    @Subcommand("setTeamPlayers")
    @AutoComplete("@mapNames *")
    public void setTeamPlayers(CommandSender sender, String mapName, int value) {
        if (value < 1 || value > 8) {
            reply(sender, lang().getTeamPlayersRange());
            return;
        }
        edit(sender, mapName, map -> map.getPlayers().setTeam(value), map -> MessageUtil.fill(lang().getTeamPlayersSet(), "players", value, "capacity", map.getMaxPlayers()));
    }

    @Subcommand("setMinPlayers")
    @AutoComplete("@mapNames *")
    public void setMinPlayers(CommandSender sender, String mapName, int value) {
        if (value < 2) {
            reply(sender, lang().getMinPlayersRange());
            return;
        }
        edit(sender, mapName, map -> map.getPlayers().setMin(value), map -> MessageUtil.fill(lang().getMinPlayersSet(), "players", value));
    }

    @Subcommand("check")
    @AutoComplete("@mapNames")
    public void check(CommandSender sender, String mapName) {
        MapData map = mapManager.getMap(mapName);
        if (map == null) {
            reply(sender, MessageUtil.fill(lang().getNotFound(), "map", mapName));
            return;
        }
        MapCheck check = MapCheck.of(map);
        List<String> lines = new ArrayList<>();
        lines.add(MessageUtil.CHAT_BAR);
        lines.add(MessageUtil.fill(check.getErrors().isEmpty() ? lang().getReady() : lang().getNotReady(), "map", mapName));
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
            reply(sender, MessageUtil.fill(lang().getNotFound(), "map", mapName));
            return;
        }
        String bases = String.join("\n", MessageUtil.formatLocationList(map.getBases()));
        List<String> lines = new ArrayList<>();
        lines.add(MessageUtil.CHAT_BAR);
        for (String line : lang().getInfo()) {
            lines.add(MessageUtil.fill(line, "name", map.getName(), "author", map.getAuthor(),
                    "min", map.getPlayers().getMin(), "team", map.getPlayers().getTeam(), "capacity", map.getMaxPlayers(),
                    "height", map.getHigherY(), "pos1", MessageUtil.formatLocation(map.getRegion().getPos1()),
                    "pos2", MessageUtil.formatLocation(map.getRegion().getPos2()),
                    "waiting", MessageUtil.formatLocation(map.getWaitingLocation()),
                    "respawn", MessageUtil.formatLocation(map.getRespawnLocation()), "bases", bases,
                    "base-drops", map.getDrops(MapData.DropType.BASE), "diamond-drops", map.getDrops(MapData.DropType.DIAMOND),
                    "emerald-drops", map.getDrops(MapData.DropType.EMERALD), "item-shops", map.getShops(MapData.ShopType.ITEM),
                    "upgrade-shops", map.getShops(MapData.ShopType.UPGRADE), "folder", map.getWorldFolder()));
        }
        lines.add(MessageUtil.CHAT_BAR);
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
                MessageUtil.fill(lang().getListHeader(), "storage", storageType, "count", mapNames.size()),
                ""
        ));
        if (mapNames.isEmpty()) {
            lines.add(lang().getListEmpty());
        }
        mapNames.forEach(name -> lines.add(" &7  - &a" + name));
        lines.add(MessageUtil.CHAT_BAR);
        send(sender, lines);
    }

    @Subcommand("load")
    @AutoComplete("@mapNames")
    public void load(CommandSender sender, String mapName) {
        reply(sender, MessageUtil.fill(mapManager.reload(mapName) != null ? lang().getReloaded() : lang().getNotInStorage(), "map", mapName));
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
            reply(sender, lang().getSameStorage());
            return;
        }
        IMapStorage sourceStorage = MapStorageFactory.getStorage(source);
        String onlyMap = mapName.equals("ALL") ? null : mapName;
        if (onlyMap != null && !sourceStorage.exists(onlyMap)) {
            reply(sender, MessageUtil.fill(lang().getNotInSource(), "storage", source, "map", onlyMap));
            return;
        }
        boolean success = MapStorageFactory.migrate(sourceStorage, MapStorageFactory.getStorage(target), onlyMap);
        String maps = onlyMap == null ? lang().getAllMaps() : MessageUtil.fill(lang().getOneMap(), "map", onlyMap);
        reply(sender, success ? MessageUtil.fill(lang().getMigrated(), "maps", maps, "from", source, "to", target) : lang().getMigrateFailed());
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
        reply(player, MessageUtil.fill(lang().getAligned(), "x", location.getX(), "y", location.getY(), "z", location.getZ(),
                "yaw", location.getYaw(), "pitch", location.getPitch()));
    }

    private void edit(CommandSender sender, String mapName, Consumer<MapData> change, Function<MapData, String> done) {
        MapData map = mapManager.getMap(mapName);
        if (map == null) {
            reply(sender, MessageUtil.fill(lang().getNotFoundCreate(), "map", mapName));
            return;
        }
        change.accept(map);
        if (!mapManager.save(map)) {
            reply(sender, lang().getSaveFailed());
            return;
        }
        reply(sender, done.apply(map));
    }

    private <E extends Enum<E>> E parse(CommandSender sender, Class<E> type, String value) {
        try {
            return Enum.valueOf(type, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            String valid = Arrays.stream(type.getEnumConstants()).map(Enum::name).collect(Collectors.joining(", "));
            reply(sender, MessageUtil.fill(lang().getInvalidType(), "value", value, "available", valid));
            return null;
        }
    }

    private boolean isAvailable(CommandSender sender, MapStorageType type) {
        if (type == MapStorageType.MYSQL && !MapStorageFactory.isMysqlAvailable()) {
            reply(sender, lang().getMysqlUnavailable());
            return false;
        }
        return true;
    }

    private static LangConfig.Maps lang() {
        return AzuraBedWars.getInstance().getLang().getMap();
    }

    private static void reply(CommandSender sender, String message) {
        sender.sendMessage(MessageUtil.color(message));
    }

    private static void send(CommandSender sender, List<String> lines) {
        MessageUtil.color(lines).forEach(sender::sendMessage);
    }
}
