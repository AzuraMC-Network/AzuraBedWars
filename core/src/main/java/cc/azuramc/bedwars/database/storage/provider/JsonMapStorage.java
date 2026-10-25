package cc.azuramc.bedwars.database.storage.provider;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.storage.IMapStorage;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * One plugins/AzuraBedWars/maps/&lt;name&gt;.json per map
 *
 * @author an5w1r@163.com
 */
public class JsonMapStorage implements IMapStorage {

    private static final String SUFFIX = ".json";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File mapDirectory;

    public JsonMapStorage() {
        this.mapDirectory = new File(AzuraBedWars.getInstance().getPlugin().getDataFolder(), "maps");
        if (!mapDirectory.isDirectory() && !mapDirectory.mkdirs()) {
            LoggerUtil.error("无法创建地图目录 " + mapDirectory.getAbsolutePath());
        }
    }

    private File getMapFile(String mapName) {
        return new File(mapDirectory, mapName + SUFFIX);
    }

    @Override
    public boolean saveMap(String mapName, MapData mapData) {
        File file = getMapFile(mapName);
        File temp = new File(mapDirectory, mapName + SUFFIX + ".tmp");
        try {
            Files.write(temp.toPath(), gson.toJson(mapData).getBytes(StandardCharsets.UTF_8));
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            LoggerUtil.error("保存地图文件 " + file.getPath() + " 失败", e);
            return false;
        }
    }

    @Override
    public MapData loadMap(String mapName) {
        File file = getMapFile(mapName);
        if (!file.isFile()) {
            return null;
        }
        try {
            MapData mapData = gson.fromJson(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8), MapData.class);
            if (mapData == null) {
                LoggerUtil.error("地图文件 " + file.getPath() + " 是空的");
                return null;
            }
            mapData.setName(mapName);
            return mapData;
        } catch (Exception e) {
            LoggerUtil.error("读取地图文件 " + file.getPath() + " 失败", e);
            return null;
        }
    }

    @Override
    public boolean deleteMap(String mapName) {
        File file = getMapFile(mapName);
        return !file.exists() || file.delete();
    }

    @Override
    public boolean exists(String mapName) {
        return getMapFile(mapName).exists();
    }

    @Override
    public List<String> getAllMapNames() {
        List<String> mapNames = new ArrayList<>();
        File[] files = mapDirectory.listFiles((dir, name) -> name.endsWith(SUFFIX));
        if (files != null) {
            for (File file : files) {
                mapNames.add(file.getName().substring(0, file.getName().length() - SUFFIX.length()));
            }
        }
        return mapNames;
    }
}
