package cc.azuramc.bedwars.config.migrate;

import cc.azuramc.bedwars.config.ConfigDocument;
import cc.azuramc.bedwars.config.ConfigProblem;
import cc.azuramc.bedwars.config.ConfigService;
import cc.azuramc.bedwars.config.yaml.ConfigKeys;
import cc.azuramc.bedwars.config.yaml.YamlBinder;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.google.gson.Gson;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Moves the files of {@code plugins/AzuraBedWars/config/}, written by versions before the current layout,
 * into the new documents and keeps the old directory as a backup
 *
 * @author an5w1r@163.com
 */
public final class LegacyConfigMigrator {

    private static final String LEGACY_DIR = "config";
    /** File names used by the old layout */
    private static final List<String> LEGACY_IDS = Arrays.asList(
            "settings", "eventSettings", "tasks", "message", "items", "player", "teamUpgrade");

    private LegacyConfigMigrator() {
    }

    public static void migrateIfPresent(File dataFolder, ConfigService service) {
        File legacyDir = new File(dataFolder, LEGACY_DIR);
        if (!legacyDir.isDirectory()) {
            return;
        }
        Map<String, Map<String, Object>> legacy = new LinkedHashMap<>();
        for (String id : LEGACY_IDS) {
            legacy.put(id, load(legacyDir, id));
        }

        int migrated = 0;
        for (Map.Entry<String, Map<String, Object>> entry : convert(legacy).entrySet()) {
            ConfigDocument<?> document = service.document(entry.getKey());
            if (document == null || entry.getValue().isEmpty() || service.file(document).exists()) {
                continue;
            }
            YamlBinder binder = new YamlBinder(document.getPath(), false);
            Object value = binder.bind(binder.parse(new Yaml().dump(entry.getValue())), document.getType());
            for (ConfigProblem problem : binder.getProblems()) {
                if (problem.isError()) {
                    LoggerUtil.warn("Old config value not migrated, the default is used: " + problem);
                }
            }
            try {
                ConfigService.writeAtomically(service.file(document), service.render(document, value));
                migrated++;
            } catch (IOException e) {
                LoggerUtil.error("Cannot write " + document.getPath() + " while migrating the old config", e);
                return;
            }
        }

        File backup = new File(dataFolder, "config-backup-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()));
        if (legacyDir.renameTo(backup)) {
            LoggerUtil.info("Migrated the old config into " + migrated + " new files, the old files are kept in " + backup.getName());
        } else {
            LoggerUtil.warn("Migrated the old config into " + migrated + " new files, but could not rename " + legacyDir + ", delete or move it to stop this message");
        }
    }

    private static Map<String, Map<String, Object>> convert(Map<String, Map<String, Object>> legacy) {
        Map<String, Object> settings = legacy.get("settings");
        Map<String, Object> player = legacy.get("player");
        Map<String, Map<String, Object>> target = new LinkedHashMap<>();

        target.put("settings", pick(settings, "editor-mode", "debug-mode", "economy-mode", "map-storage",
                "default-map-name", "enable-team-selection", "play-again-command"));
        target.put("database", section(settings, "database"));

        Map<String, Object> gameplay = pick(settings, "max-health", "max-no-movement-time", "team-spawn-protection-radius",
                "resource-spawn-protection-radius", "team-block-search-radius", "bed-search-radius", "bed-destroy-reward", "fire-ball");
        putIfPresent(gameplay, "chat", settings.get("chat-config"));
        putIfPresent(gameplay, "player-death", player.get("player-death"));
        putIfPresent(gameplay, "player-respawn", player.get("player-respawn"));
        putIfPresent(gameplay, "spectator", player.get("spectator"));
        rename(section(section(gameplay, "spectator"), "setting-gui"), "night-version-slot", "night-vision-slot");
        target.put("gameplay", gameplay);

        Map<String, Object> display = pick(settings, "game-scoreboard", "lobby-scoreboard", "game-end-scoreboard", "display-damage");
        Map<String, Object> tabList = new LinkedHashMap<>();
        putIfPresent(tabList, "waiting", settings.get("waiting-state"));
        putIfPresent(tabList, "running", settings.get("running-state"));
        putIfPresent(tabList, "ending", settings.get("ending-state"));
        putIfPresent(display, "tab-list", tabList.isEmpty() ? null : tabList);
        Object lobbyLines = section(display, "lobby-scoreboard").get("lines");
        if (lobbyLines instanceof List) {
            // lobby line written by versions with a per-player economy
            @SuppressWarnings("unchecked")
            List<Object> lines = (List<Object>) lobbyLines;
            lines.replaceAll(line -> "&f你的模式: &a{mode}".equals(line) ? "&f模式: &a{mode}" : line);
        }
        target.put("display", display);

        // spawn intervals and iron or gold stack sizes of tasks.yml were never read, so they are left out
        target.put("generators", pick(legacy.get("tasks"), "max-diamond-stack-level1", "max-diamond-stack-level2",
                "max-diamond-stack-level3", "max-emerald-stack-level1", "max-emerald-stack-level2", "max-emerald-stack-level3",
                "diamond-name", "emerald-name", "time-remaining-format", "level-i", "level-ii", "level-iii"));
        target.put("timeline", legacy.get("eventSettings"));
        target.put("items", legacy.get("items"));
        target.put("upgrades", legacy.get("teamUpgrade"));

        Map<String, Object> messages = legacy.get("message");
        rename(section(messages, "spectator"), "night-version-enabled", "night-vision-enabled");
        rename(section(messages, "spectator"), "night-version-disabled", "night-vision-disabled");
        target.put("messages", messages);
        return target;
    }

    private static Map<String, Object> load(File legacyDir, String id) {
        try {
            File yml = new File(legacyDir, id + ".yml");
            if (yml.isFile()) {
                return asMap(normalize(new Yaml().load(ConfigService.read(yml))));
            }
            File json = new File(legacyDir, id + ".json");
            if (json.isFile()) {
                return asMap(normalize(new Gson().fromJson(ConfigService.read(json), Map.class)));
            }
        } catch (Exception e) {
            LoggerUtil.warn("Cannot read old config " + id + ", its values are not migrated: " + e.getMessage());
        }
        return new LinkedHashMap<>();
    }

    /**
     * Old files used Java field names as keys and stored colors already translated to {@code §}
     */
    private static Object normalize(Object value) {
        if (value instanceof Map) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                map.put(ConfigKeys.toKebab(String.valueOf(entry.getKey())), normalize(entry.getValue()));
            }
            return map;
        }
        if (value instanceof List) {
            List<Object> list = new ArrayList<>();
            for (Object item : (List<?>) value) {
                list.add(normalize(item));
            }
            return list;
        }
        if (value instanceof String) {
            return ((String) value).replace('§', '&');
        }
        if (value instanceof Double && (Double) value == Math.rint((Double) value)) {
            return ((Double) value).longValue();
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : new LinkedHashMap<>();
    }

    private static Map<String, Object> section(Map<String, Object> map, String key) {
        return asMap(map.get(key));
    }

    private static Map<String, Object> pick(Map<String, Object> source, String... keys) {
        Map<String, Object> picked = new LinkedHashMap<>();
        for (String key : keys) {
            putIfPresent(picked, key, source.get(key));
        }
        return picked;
    }

    private static void putIfPresent(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private static void rename(Map<String, Object> map, String from, String to) {
        if (map.containsKey(from)) {
            map.put(to, map.remove(from));
        }
    }
}
