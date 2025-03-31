package cc.azuramc.bedwars.commands.admin.map;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.map.MapData;
import cc.azuramc.bedwars.utils.CC;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.io.*;
import java.nio.charset.StandardCharsets;
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

    @Subcommand("{mapName} create")
    public void createMap(Player player, String mapName) {
        maps.put(mapName, new MapData());
    }

    @Subcommand("{mapName} setRespawn")
    public void setRespawn(Player player, String mapName) {
        MapData mapData = maps.get(mapName);
        mapData.setReSpawn(player.getLocation());
        player.sendMessage(CC.color("&a地图重生点设置成功!"));
    }

    @Subcommand("{mapName} addBase")
    public void addBase(Player player, String mapName) {
        MapData mapData = maps.get(mapName);
        mapData.addBase(player.getLocation());
        player.sendMessage(CC.color("&a基地出生点增加成功!"));
    }

    @Subcommand("{mapName} setPos1")
    public void setPos1(Player player, String mapName) {
        MapData mapData = maps.get(mapName);
        mapData.setPos1(player.getLocation());
        player.sendMessage(CC.color("&a设置地图边界 1 成功!"));
    }

    @Subcommand("{mapName} setPos2")
    public void setPos2(Player player, String mapName) {
        MapData mapData = maps.get(mapName);
        mapData.setPos1(player.getLocation());
        player.sendMessage(CC.color("&a设置地图边界 2 成功!"));
    }

    @Subcommand("{mapName} setRespawn {authorName}")
    public void setRespawn(Player player, String mapName, String authorName) {
        MapData mapData = maps.get(mapName);
        mapData.setAuthor(authorName);
        player.sendMessage(CC.color("&a地图作者设置成功!"));
    }

    @Subcommand("{mapName} setTeamPlayers {value}")
    public void setTeamPlayers(Player player, String mapName, int value) {
        MapData mapData = maps.get(mapName);
        mapData.getPlayers().setTeam(value);
        player.sendMessage(CC.color("&a队伍最大人数设置成功!"));
    }

    @Subcommand("{mapName} setMinPlayers {value}")
    public void setMinPlayers(Player player, String mapName, int value) {
        MapData mapData = maps.get(mapName);
        mapData.getPlayers().setMin(value);
        player.sendMessage(CC.color("&a地图最小人数置成功!"));
    }

    @Subcommand("{mapName} addDropLoc {value}")
    public void addDropLoc(Player player, String mapName, String value) {
        MapData mapData = maps.get(mapName);
        mapData.addDrop(MapData.DropType.valueOf(value.toUpperCase()), player.getLocation());
        player.sendMessage(CC.color("&a增加基地掉落点成功!"));
    }

    @Subcommand("{mapName} addShop {value}")
    public void addShop(Player player, String mapName, String value) {
        MapData mapData = maps.get(mapName);
        mapData.addShop(MapData.ShopType.valueOf(value.toUpperCase()), player.getLocation());
        player.sendMessage(CC.color("&a增加商人成功!"));
    }

    @Subcommand("{mapName} info")
    public void info(Player player, String mapName) {
        MapData mapData = maps.get(mapName);
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

    @Subcommand("{mapName} save")
    public void save(Player player, String mapName) {
        MapData mapData = maps.get(mapName);

        try {
            File file = new File(AzuraBedWars.getInstance().getDataFolder(), mapName + ".json");
            boolean successful = file.createNewFile();
            if (!successful) {
                player.sendMessage(CC.color("&c地图创建失败!"));
            }

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(new Gson().toJson(mapData));
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        player.sendMessage(CC.color("&a保存成功!"));
    }

    @Subcommand("{mapName} load")
    public void load(Player player, String mapName) {
        File file = new File(AzuraBedWars.getInstance().getDataFolder(), mapName + ".json");

        if (!file.exists()) {
            player.sendMessage("§c找不到地图文件: " + mapName + ".json");
        }

        try {
            // 从JSON文件读取数据
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                jsonContent.append(line);
            }
            bufferedReader.close();

            // 将JSON转换为MapData对象
            MapData mapData = new Gson().fromJson(jsonContent.toString(), MapData.class);
            maps.put(mapName, mapData);

        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(CC.color("&c读取地图文件时出错: " + e.getMessage()));
        }
        player.sendMessage("配置加载成功!");
    }

}
