package cc.azuramc.bedwars.database.map;

import cc.azuramc.bedwars.AzuraBedWars;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MapDataSQL implements Listener {
    private final Gson GSON = new Gson();
    private boolean load = false;

    public MapData loadMap(String mapName) {
        MapData mapData = null;

        try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection("bwdata")) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM BWMaps Where MapName=?");
            preparedStatement.setString(1, mapName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String name = resultSet.getString("MapName");
                String url = resultSet.getString("URL");
                mapData = GSON.fromJson(resultSet.getString("Data"), MapData.class);

                if (new File(name).exists()) {
                    new File(name).delete();
                }

                FileUtils.copyDirectory(new File(url), new File(name));

                WorldCreator cr = new WorldCreator(name);
                cr.environment(World.Environment.NORMAL);
                World mapWorld = Bukkit.createWorld(cr);
                mapWorld.setAutoSave(false);
                mapWorld.setGameRuleValue("doMobSpawning", "false");
                mapWorld.setGameRuleValue("doFireTick", "false");
            }

            preparedStatement.close();
            resultSet.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        return mapData;
    }

    public MapData.RawLocation getWaitingLoc() {
        MapData.RawLocation rawLocation = null;
        String wordName = null;

        try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection("bwdata")) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM BWConfig Where configKey=?");
            preparedStatement.setString(1, "WaitingMapURL");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String URL = resultSet.getString("object");
                wordName = new File(URL).getName();

                if (new File(wordName).exists()) {
                    new File(wordName).delete();
                }

                FileUtils.copyDirectory(new File(URL), new File(wordName));

                WorldCreator cr = new WorldCreator(wordName);
                cr.environment(World.Environment.NORMAL);
                World mapWorld = Bukkit.createWorld(cr);

                mapWorld.setAutoSave(false);
                mapWorld.setGameRuleValue("doMobSpawning", "false");
                mapWorld.setGameRuleValue("doFireTick", "false");
            }

            preparedStatement = connection.prepareStatement("SELECT * FROM BWConfig Where configKey=?");
            preparedStatement.setString(1, "WaitingLoc");
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                rawLocation = GSON.fromJson(resultSet.getString("object"), MapData.RawLocation.class);
                rawLocation.setWorld(wordName);
            }

            preparedStatement.close();
            resultSet.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        return rawLocation;
    }

}
