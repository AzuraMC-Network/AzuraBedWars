package cc.azuramc.bedwars.config.migrate;

import cc.azuramc.bedwars.config.ApplyResult;
import cc.azuramc.bedwars.config.ConfigProblem;
import cc.azuramc.bedwars.config.ConfigService;
import cc.azuramc.bedwars.util.LoggerUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Moves the texts changed in messages.yml, which lang/ replaced, into lang/zh_CN.yml
 *
 * @author an5w1r@163.com
 */
public final class MessagesMigrator {

    public static final String FILE = "messages.yml";
    private static final String TARGET = "lang/zh_CN";
    /** Keys of messages.yml, their lang/ path, and the placeholders that replace its %s and %d in order */
    private static final String[][] KEYS = {
            {"normal-kill-rewards-message", "game.normal-kill-reward"},
            {"final-kill-rewards-message", "game.final-kill-reward"},
            {"respawn-countdown-title", "game.respawn-countdown-title", "{seconds}"},
            {"respawn-countdown-sub-title", "game.respawn-countdown-subtitle"},
            {"respawn-complete-title", "game.respawned-title"},
            {"respawn-complete-sub-title", "game.respawned-subtitle"},
            {"death-permanent-title", "game.eliminated-title"},
            {"death-permanent-sub-title", "game.eliminated-subtitle"},
            {"team-eliminated-format", "game.team-eliminated-bar"},
            {"team-eliminated-message", "game.team-eliminated", "{team}", "{breaker}"},
            {"play-again-message", "game.play-again"},
            {"play-again-button", "game.play-again-button"},
            {"player-reconnect-message", "game.reconnected", "{player}"},
            {"player-leave-message", "game.left", "{player}"},
            {"warp-powder-start-message", "items.warp-powder-start", "{seconds}"},
            {"warp-powder-cancel-message", "items.warp-powder-cancelled"},
            {"spectator.spectator-compass-gui-title", "spectator.compass-title"},
            {"spectator.health-format", "spectator.health", "{value}"},
            {"spectator.food-format", "spectator.food", "{value}"},
            {"spectator.level-format", "spectator.level", "{value}"},
            {"spectator.distance-format", "spectator.distance", "{value}"},
            {"spectator.spectator-settings-gui-title", "spectator.settings-title"},
            {"spectator.speed-removed", "spectator.speed-removed"},
            {"spectator.speed-added", "spectator.speed-added", "{level}"},
            {"spectator.auto-tp-enabled", "spectator.auto-tp-enabled"},
            {"spectator.auto-tp-disabled", "spectator.auto-tp-disabled"},
            {"spectator.night-vision-enabled", "spectator.night-vision-enabled"},
            {"spectator.night-vision-disabled", "spectator.night-vision-disabled"},
            {"spectator.first-person-enabled", "spectator.first-person-enabled"},
            {"spectator.first-person-disabled", "spectator.first-person-disabled"},
            {"spectator.hide-others-enabled", "spectator.hide-others-enabled"},
            {"spectator.hide-others-disabled", "spectator.hide-others-disabled"},
            {"spectator.fly-enabled", "spectator.fly-enabled"},
            {"spectator.fly-disabled", "spectator.fly-disabled"},
            {"spectator.target-lost-message", "spectator.target-lost"},
            {"spectator.first-person-title", "spectator.first-person-title", "{player}"},
            {"spectator.first-person-sub-title", "spectator.first-person-subtitle"},
            {"spectator.third-person-action-bar", "spectator.third-person-action-bar", "{player}", "{health}", "{distance}"},
    };

    private MessagesMigrator() {
    }

    /**
     * @return problems of the changed lang/zh_CN.yml, empty when nothing changed
     */
    public static List<ConfigProblem> migrateIfPresent(File dataFolder, ConfigService service) {
        File file = new File(dataFolder, FILE);
        if (!file.isFile() || service.document(TARGET) == null) {
            return Collections.emptyList();
        }
        try {
            Map<String, Object> old = map(new Yaml().load(ConfigService.read(file)));
            Map<String, Object> bundled = ConfigService.bundled(TARGET + ".yml");
            Map<String, Object> changes = new LinkedHashMap<>();
            for (String[] key : KEYS) {
                put(changes, bundled, key[1], convert(get(old, key[0]), key));
            }
            Object actionBar = get(old, "spectator.first-person-action-bar");
            Object hint = get(old, "spectator.menu-hint");
            if (actionBar instanceof String) {
                String joined = convert(actionBar, new String[]{"", "", "{player}", "{health}"}) + (hint instanceof String ? (String) hint : "");
                put(changes, bundled, "spectator.first-person-action-bar", joined);
            }

            List<ConfigProblem> problems = Collections.emptyList();
            if (!changes.isEmpty()) {
                ApplyResult result = service.setValues(TARGET, changes, null);
                if (result.getStatus() == ApplyResult.Status.REJECTED) {
                    LoggerUtil.warn("Could not move the texts of " + FILE + " into " + TARGET + ".yml, the file stays");
                    return result.getProblems();
                }
                problems = result.getProblems();
            }
            File history = new File(dataFolder, ConfigService.HISTORY_DIR + "/" + FILE + "."
                    + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()));
            history.getParentFile().mkdirs();
            if (!file.renameTo(history)) {
                LoggerUtil.warn("Could not move " + FILE + " into " + ConfigService.HISTORY_DIR + ", delete it to stop this message");
            }
            LoggerUtil.info(changes.isEmpty() ? FILE + " is replaced by lang/, the old file is kept in " + ConfigService.HISTORY_DIR
                    : "Moved " + changes.size() + " changed texts of " + FILE + " into " + TARGET + ".yml");
            return problems;
        } catch (IOException | RuntimeException e) {
            LoggerUtil.error("Cannot move " + FILE + " into " + TARGET + ".yml", e);
            return Collections.emptyList();
        }
    }

    private static void put(Map<String, Object> changes, Map<String, Object> bundled, String path, Object value) {
        if (value instanceof String && !value.equals(get(bundled, path))) {
            changes.put(path, value);
        }
    }

    private static Object convert(Object value, String[] key) {
        if (!(value instanceof String)) {
            return value;
        }
        String text = (String) value;
        for (int i = 2; i < key.length; i++) {
            int at = indexOfPlaceholder(text);
            if (at < 0) {
                break;
            }
            text = text.substring(0, at) + key[i] + text.substring(at + 2);
        }
        return text;
    }

    private static int indexOfPlaceholder(String text) {
        int s = text.indexOf("%s");
        int d = text.indexOf("%d");
        return s < 0 ? d : d < 0 ? s : Math.min(s, d);
    }

    private static Object get(Map<String, Object> root, String path) {
        Object value = root;
        for (String part : path.split("\\.")) {
            value = value instanceof Map ? ((Map<?, ?>) value).get(part) : null;
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : new LinkedHashMap<String, Object>();
    }
}
