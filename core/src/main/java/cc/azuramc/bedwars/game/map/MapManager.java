package cc.azuramc.bedwars.game.map;

import cc.azuramc.bedwars.compat.util.WorldUtil;
import cc.azuramc.bedwars.database.storage.IMapStorage;
import cc.azuramc.bedwars.database.storage.MapStorageFactory;
import cc.azuramc.bedwars.util.LoggerUtil;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Keeps every stored map in memory, edits are written back to storage immediately
 *
 * @author an5w1r@163.com
 */
public class MapManager {

    private final IMapStorage storage;
    private final Map<String, MapData> maps = new LinkedHashMap<>();

    public MapManager() {
        this.storage = MapStorageFactory.getDefaultStorage();
        for (String name : storage.getAllMapNames()) {
            MapData map = storage.loadMap(name);
            if (map != null) {
                map.setName(name);
                maps.put(name, map);
            }
        }
    }

    @Nullable
    public MapData getMap(String name) {
        return maps.get(name);
    }

    public Set<String> getMapNames() {
        return Collections.unmodifiableSet(maps.keySet());
    }

    public boolean exists(String name) {
        return maps.containsKey(name) || storage.exists(name);
    }

    public boolean save(MapData map) {
        if (!storage.saveMap(map.getName(), map)) {
            return false;
        }
        maps.put(map.getName(), map);
        return true;
    }

    public boolean delete(String name) {
        maps.remove(name);
        return storage.deleteMap(name);
    }

    /**
     * Rereads a map from storage, for files edited by hand while the server runs
     */
    @Nullable
    public MapData reload(String name) {
        MapData map = storage.loadMap(name);
        if (map == null) {
            maps.remove(name);
            return null;
        }
        map.setName(name);
        maps.put(name, map);
        return map;
    }

    /**
     * Copies the preferred map, or the first other map that passes {@link MapCheck}, into a fresh world
     *
     * @return null when no map can run
     */
    @Nullable
    public MapData loadGameMap(String preferred) {
        List<String> names = new ArrayList<>(maps.keySet());
        if (names.remove(preferred)) {
            names.add(0, preferred);
        } else if (preferred != null && !preferred.isEmpty()) {
            LoggerUtil.warn("The default map " + preferred + " does not exist");
        }

        for (String name : names) {
            MapData map = maps.get(name);
            if (hasErrors(map, false)) {
                continue;
            }
            World world = copyWorld(map);
            if (world == null) {
                continue;
            }
            map.bindWorld(world.getName());
            // beds and wool are only checked once the copied world is loaded
            if (hasErrors(map, true)) {
                Bukkit.unloadWorld(world, false);
                continue;
            }
            if (!name.equals(preferred)) {
                LoggerUtil.warn("Using map " + name + " instead");
            }
            return map;
        }
        return null;
    }

    private static boolean hasErrors(MapData map, boolean logWarnings) {
        MapCheck check = MapCheck.of(map);
        if (check.getErrors().isEmpty()) {
            if (logWarnings) {
                check.getWarnings().forEach(warning -> LoggerUtil.warn("Map " + map.getName() + ": " + warning));
            }
            return false;
        }
        LoggerUtil.warn("Map " + map.getName() + " is not set up completely and cannot be used");
        check.getErrors().forEach(error -> LoggerUtil.warn("  " + error));
        return true;
    }

    @Nullable
    private World copyWorld(MapData map) {
        String name = map.getName();
        File source = map.getWorldFolder();
        File target = new File(Bukkit.getWorldContainer(), name);

        World existing = Bukkit.getWorld(name);
        if (existing != null && !Bukkit.unloadWorld(existing, false)) {
            LoggerUtil.warn("Cannot unload the loaded world " + name + ", a map cannot have the name of the main world");
            return null;
        }
        try {
            FileUtils.deleteDirectory(target);
            FileUtils.copyDirectory(source, target);
        } catch (IOException e) {
            LoggerUtil.error("Cannot copy the world files of map " + name, e);
            return null;
        }
        // Bukkit refuses duplicate world uids
        new File(target, "uid.dat").delete();
        new File(target, "session.lock").delete();

        World world = Bukkit.createWorld(new WorldCreator(name).environment(World.Environment.NORMAL));
        if (world == null) {
            LoggerUtil.warn("Cannot load the world of map " + name);
            return null;
        }
        return WorldUtil.setWorldRules(world);
    }
}
