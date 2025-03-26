package cc.azuramc.bedwars.commands;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.map.MapData;
import cc.azuramc.bedwars.database.map.MapDataSQL;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AdminCommand implements CommandExecutor {
    private final Map<String, MapData> maps;

    public AdminCommand() {
        maps = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("此命令仅限玩家");
            return true;
        }

        Player player = (Player) commandSender;

        if (strings.length == 0) {
            player.sendMessage("/game toWorld <地图名字> ----- 前往世界");
            player.sendMessage("/game loadWorld <地图名字> ----- 加载世界");
            player.sendMessage("/game loadGame <地图名字> ----- 加载现成配置");
            player.sendMessage(" ");
            player.sendMessage("/game setWaiting ----- 设置等待大厅");
            player.sendMessage(" ");
            player.sendMessage("/game new <地图名称> ----- 创建新地图");
            player.sendMessage("/game setAuthor <地图名称> <作者名称> ----- 设置作者名称");
            player.sendMessage("/game setTeamPlayers <地图名称> <数量> ----- 设置队伍最大人数");
            player.sendMessage("/game setMinPlayers <地图名称> <数量> ----- 设置地图最小人数");
            player.sendMessage("/game setRange <地图名称> <数量> ----- 设置出生点保护范围");
            player.sendMessage("/game setRespawn <地图名称> ----- 设置地图重生点");
            player.sendMessage("/game addBase <地图名称> ----- 增加基地出生点");
            player.sendMessage("/game addDrop <地图名称> <类型> ----- 增加基地掉落资源点 类型: B/D/E");
            player.sendMessage("/game addShop <地图名称> <类型> ----- 增加村民点 类型: 1/2");
            player.sendMessage("/game setPos1 <地图名称> ----- 设置边界1");
            player.sendMessage("/game setPos2 <地图名称> ----- 设置边界2");
            player.sendMessage(" ");
            player.sendMessage("/game info <地图名称> ----- 查看设置的参数");
            player.sendMessage("/game save <地图名称> ----- 保存设置的地图");
            return true;
        }

        if (strings[0].equalsIgnoreCase("setWaiting")) {
            Location location = player.getLocation();
            MapData.Location rawLocation = new MapData.Location();
            rawLocation.setWorld(location.getWorld().getName());
            rawLocation.setX(location.getX());
            rawLocation.setY(location.getY());
            rawLocation.setZ(location.getZ());
            rawLocation.setPitch(location.getPitch());
            rawLocation.setY(location.getYaw());
            System.out.println(new Gson().toJson(rawLocation));

            player.sendMessage("设置等待大厅成功!");
            return true;
        }

        if (strings.length == 2) {
            if (strings[0].equalsIgnoreCase("toWorld")) {
                player.teleport(Bukkit.getWorld(strings[1]).getSpawnLocation());
            }

            if (strings[0].equalsIgnoreCase("loadWorld")) {
                WorldCreator cr = new WorldCreator(strings[1]);
                cr.environment(World.Environment.NORMAL);
                World mapWorld = Bukkit.createWorld(cr);

                mapWorld.setAutoSave(false);
                mapWorld.setGameRuleValue("doMobSpawning", "false");
                mapWorld.setGameRuleValue("doFireTick", "false");
            }

            if (strings[0].equalsIgnoreCase("loadGame")) {
                maps.put(strings[1], new MapDataSQL().loadMap(strings[1]));

                player.sendMessage("配置加载成功!");
                return true;
            }

            if (strings[0].equalsIgnoreCase("new")) {
                // 确保数据库和表已创建
                try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection("bwdata")) {
                    if (connection == null) {
                        player.sendMessage("§c无法连接到数据库！");
                        return true;
                    }
                    
                    // 创建新地图
                    MapData mapData = new MapData();
                    mapData.setAuthor(player.getName());
                    maps.put(strings[1], mapData);
                    
                    // 保存到数据库
                    try (PreparedStatement stmt = connection.prepareStatement(
                            "INSERT INTO BWMaps (MapName, URL, Data) VALUES (?, ?, ?)")) {
                        stmt.setString(1, strings[1]);
                        stmt.setString(2, ""); // 暂时为空，后续保存时更新
                        stmt.setString(3, new Gson().toJson(mapData));
                        stmt.executeUpdate();
                    }
                    
                    player.sendMessage("§a地图创建成功！");
                    return true;
                } catch (SQLException e) {
                    player.sendMessage("§c创建地图失败：" + e.getMessage());
                    e.printStackTrace();
                    return true;
                }
            }

            if (strings[0].equalsIgnoreCase("setRespawn")) {
                MapData mapData = maps.get(strings[1]);
                if (mapData == null) return true;
                mapData.setReSpawn(player.getLocation());

                player.sendMessage("地图重生点设置成功！");
                return true;
            }

            if (strings[0].equalsIgnoreCase("addBase")) {
                MapData mapData = maps.get(strings[1]);
                if (mapData == null) return true;
                mapData.addBase(player.getLocation());

                player.sendMessage("基地出生点增加成功！");
                return true;
            }

            if (strings[0].equalsIgnoreCase("setPos1")) {
                MapData mapData = maps.get(strings[1]);
                if (mapData == null) return true;
                mapData.setPos1(player.getLocation());

                player.sendMessage("基地出生点增加成功！");
                return true;
            }

            if (strings[0].equalsIgnoreCase("setPos2")) {
                MapData mapData = maps.get(strings[1]);
                if (mapData == null) return true;
                mapData.setPos2(player.getLocation());

                player.sendMessage("基地出生点增加成功！");
                return true;
            }

            if (strings[0].equalsIgnoreCase("info")) {
                MapData mapData = maps.get(strings[1]);
                if (mapData == null) return true;

                player.sendMessage("名称: " + strings[1]);
                player.sendMessage("作者: " + mapData.getAuthor());
                player.sendMessage("地图最小人数: " + mapData.getPlayers().getMin());
                player.sendMessage("队伍最大人数: " + mapData.getPlayers().getTeam());
                player.sendMessage("基地出生点: " + mapData.getBases());
                player.sendMessage("");
                player.sendMessage("基地资源点: " + mapData.getDrops(MapData.DropType.BASE));
                player.sendMessage("钻石资源点: " + mapData.getDrops(MapData.DropType.DIAMOND));
                player.sendMessage("绿宝石资源点: " + mapData.getDrops(MapData.DropType.EMERALD));
                return true;
            }

            if (strings[0].equalsIgnoreCase("save")) {
                MapData mapData = maps.get(strings[1]);
                if (mapData == null) return true;

                try {
                    File file = new File(AzuraBedWars.getInstance().getDataFolder(), strings[1] + ".json");
                    file.createNewFile();

                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
                    BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
                    bufferedWriter.write(new Gson().toJson(mapData));
                    bufferedWriter.flush();
                    bufferedWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                player.sendMessage("保存成功！");
                return true;
            }
        }

        if (strings.length == 3) {
            if (strings[0].equalsIgnoreCase("setAuthor")) {
                MapData mapData = maps.get(strings[1]);
                if (mapData == null) return true;

                mapData.setAuthor(strings[2]);
                player.sendMessage("作者设置成功！");
                return true;
            }

            if (strings[0].equalsIgnoreCase("setTeamPlayers")) {
                MapData mapData = maps.get(strings[1]);
                if (mapData == null) return true;

                mapData.getPlayers().setTeam(Integer.valueOf(strings[2]));
                player.sendMessage("队伍最大人数设置成功！");
                return true;
            }

            if (strings[0].equalsIgnoreCase("setMinPlayers")) {
                MapData mapData = maps.get(strings[1]);
                if (mapData == null) return true;

                mapData.getPlayers().setMin(Integer.valueOf(strings[2]));
                player.sendMessage("地图最小人数设置成功！");
                return true;
            }

            if (strings[0].equalsIgnoreCase("setRange")) {
                //getMap(strings[1]).setRange(Integer.valueOf(strings[2]));

                player.sendMessage("出生点范围设置成功！");
                return true;
            }

            if (strings[0].equalsIgnoreCase("addDropLoc")) {
                MapData mapData = maps.get(strings[1]);
                if (mapData == null) return true;

                mapData.addDrop(MapData.DropType.valueOf(strings[2].toUpperCase()), player.getLocation());
                player.sendMessage("增加基地掉落点成功！");
                return true;
            }

            if (strings[0].equalsIgnoreCase("addShop")) {
                MapData mapData = maps.get(strings[1]);
                if (mapData == null) return true;

                mapData.addShop(MapData.ShopType.valueOf(strings[2].toUpperCase()), player.getLocation());
                player.sendMessage("增加商人成功！");
                return true;
            }
        }

        return false;
    }
}