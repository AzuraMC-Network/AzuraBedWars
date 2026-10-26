package cc.azuramc.bedwars.database.storage;

import cc.azuramc.bedwars.game.map.MapData;

import java.util.List;

/**
 * @author an5w1r@163.com
 */
public interface IMapStorage {

    boolean saveMap(String mapName, MapData mapData);

    /**
     * @return null when the map does not exist or cannot be read
     */
    MapData loadMap(String mapName);

    boolean deleteMap(String mapName);

    boolean exists(String mapName);

    List<String> getAllMapNames();
}
