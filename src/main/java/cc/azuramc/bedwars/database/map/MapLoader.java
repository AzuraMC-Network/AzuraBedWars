package cc.azuramc.bedwars.database.map;

import cc.azuramc.bedwars.AzuraBedWars;
import com.google.gson.Gson;
import org.bukkit.Bukkit;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class MapLoader {

    public static MapData loadMapFromJson(String mapName) {
        File file = new File(AzuraBedWars.getInstance().getDataFolder(), mapName + ".json");

        try {
            StringBuilder jsonContent = readFileContent(file);
            MapData mapData = new Gson().fromJson(jsonContent.toString(), MapData.class);
            mapData.setName(mapName);


            Bukkit.getLogger().info("§a地图 " + mapName + " 配置加载成功!");

            return mapData;

        } catch (Exception e) {
            Bukkit.getLogger().warning("§c读取地图文件时出错: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取文件内容的辅助方法
     *
     * @param file 要读取的文件
     * @return 文件内容
     * @throws IOException 文件读取异常
     */
    private static StringBuilder readFileContent(File file) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file);
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                jsonContent.append(line);
            }
            return jsonContent;
        }
    }
}