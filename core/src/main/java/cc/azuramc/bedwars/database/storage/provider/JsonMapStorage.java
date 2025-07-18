package cc.azuramc.bedwars.database.storage.provider;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.storage.IMapStorage;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON文件存储实现
 * 使用JSON文件存储和读取地图数据
 *
 * @author an5w1r@163.com
 */
public class JsonMapStorage implements IMapStorage {
    private final Gson gson;
    private final File mapDirectory;

    public JsonMapStorage() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.mapDirectory = new File(AzuraBedWars.getInstance().getDataFolder(), "maps");
        if (!this.mapDirectory.exists()) {
            boolean created = this.mapDirectory.mkdirs();
            if (!created) {
                AzuraBedWars.getInstance().getLogger().severe("无法创建地图数据目录：" + this.mapDirectory.getAbsolutePath());
            }
        }
    }
    
    private File getMapFile(String mapName) {
        return new File(mapDirectory, mapName + ".json");
    }
    
    @Override
    public boolean saveMap(String mapName, MapData mapData) {
        if (mapName == null || mapData == null) {
            return false;
        }
        
        File mapFile = getMapFile(mapName);
        
        try (FileOutputStream fos = new FileOutputStream(mapFile);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw)) {
            
            String json = gson.toJson(mapData);
            writer.write(json);
            return true;
        } catch (IOException e) {
            LoggerUtil.error("保存地图数据到JSON文件时出错: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public MapData loadMap(String mapName) {
        File mapFile = getMapFile(mapName);
        
        if (!mapFile.exists()) {
            return null;
        }
        
        try {
            String jsonContent = readFileContent(mapFile);
            MapData mapData = gson.fromJson(jsonContent, MapData.class);
            mapData.setName(mapName);
            return mapData;
        } catch (Exception e) {
            LoggerUtil.error("从JSON文件加载地图数据时出错: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public boolean deleteMap(String mapName) {
        File mapFile = getMapFile(mapName);
        return !mapFile.exists() || mapFile.delete();
    }
    
    @Override
    public boolean exists(String mapName) {
        return getMapFile(mapName).exists();
    }
    
    @Override
    public List<String> getAllMapNames() {
        List<String> mapNames = new ArrayList<>();

        // 获取所有JSON文件并提取地图名称
        File[] files = mapDirectory.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                String filename = file.getName();
                // 去掉.json后缀
                mapNames.add(filename.substring(0, filename.length() - 5));
            }
        }
        
        return mapNames;
    }
    
    @Override
    public boolean migrateTo(IMapStorage targetStorage, String mapName) {
        if (targetStorage == null) {
            return false;
        }
        
        // 如果指定了地图名称，只迁移单个地图
        if (mapName != null) {
            if (!exists(mapName)) {
                return false;
            }
            
            MapData mapData = loadMap(mapName);
            return mapData != null && targetStorage.saveMap(mapName, mapData);
        }
        
        // 否则迁移所有地图
        boolean allSuccess = true;
        for (String name : getAllMapNames()) {
            MapData mapData = loadMap(name);
            if (mapData == null || !targetStorage.saveMap(name, mapData)) {
                allSuccess = false;
            }
        }
        
        return allSuccess;
    }
    
    /**
     * 读取文件内容
     */
    private String readFileContent(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {
            
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            return content.toString();
        }
    }
} 