package cc.azuramc.bedwars.config;

import cc.azuramc.bedwars.config.object.DatabaseConfig;
import cc.azuramc.bedwars.config.object.GameplayConfig;
import cc.azuramc.bedwars.config.object.SettingsConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigServiceTest {

    @TempDir
    File dataFolder;

    private final AtomicBoolean gameRunning = new AtomicBoolean();
    private ConfigService service;

    @BeforeEach
    void setUp() {
        service = new ConfigService(dataFolder, gameRunning::get);
        service.register("settings", "settings.yml", SettingsConfig.class, ApplyMode.LIVE, 1);
        service.register("database", "database.yml", DatabaseConfig.class, ApplyMode.RESTART, 1);
        service.register("gameplay", "nested/gameplay.yml", GameplayConfig.class, ApplyMode.NEXT_GAME, 1);
        assertTrue(service.loadAll().isEmpty());
    }

    private File file(String path) {
        return new File(dataFolder, path);
    }

    private void edit(String path, String from, String to) throws IOException {
        String text = ConfigService.read(file(path));
        assertTrue(text.contains(from), from);
        ConfigService.writeAtomically(file(path), text.replace(from, to));
    }

    @Test
    void createsCommentedTemplatesAndSchemas() throws IOException {
        String settings = ConfigService.read(file("settings.yml"));
        assertTrue(settings.startsWith("# yaml-language-server: $schema=schema/settings.json\n"));
        assertTrue(settings.contains("config-version: 1\n"));
        assertTrue(settings.contains("# Debug logging\ndebug-mode: false\n"));
        assertTrue(ConfigService.read(file("nested/gameplay.yml")).startsWith("# yaml-language-server: $schema=../schema/gameplay.json"));
        assertTrue(file("schema/gameplay.json").isFile());
    }

    @Test
    void loadingNeverRewritesAnExistingFile() throws IOException {
        ConfigService.writeAtomically(file("settings.yml"), "# my notes\ndebug-mode: yes\n");
        service.loadAll();
        assertEquals("# my notes\ndebug-mode: yes\n", ConfigService.read(file("settings.yml")));
        assertTrue(service.get(SettingsConfig.class).isDebugMode());
    }

    @Test
    void setValueChangesOnlyThatValue() throws IOException {
        ConfigService.writeAtomically(file("settings.yml"), "# keep me\ndebug-mode: false # inline\nplay-again-command: \"/hub\"\n");
        service.loadAll();
        ApplyResult result = service.setValue("settings", "debug-mode", true);
        assertEquals(ApplyResult.Status.APPLIED, result.getStatus());
        assertEquals("# keep me\ndebug-mode: true # inline\nplay-again-command: \"/hub\"\n", ConfigService.read(file("settings.yml")));
        assertTrue(service.get(SettingsConfig.class).isDebugMode());

        service.setValue("settings", "play-again-command", "/lobby \"x\"");
        assertEquals("/lobby \"x\"", service.get(SettingsConfig.class).getPlayAgainCommand());
        assertTrue(file(ConfigService.HISTORY_DIR).list().length >= 2);
    }

    @Test
    void setValueAppendsMissingTopLevelKey() throws IOException {
        ConfigService.writeAtomically(file("settings.yml"), "debug-mode: false");
        service.loadAll();
        service.setValue("settings", "enable-team-selection", false);
        assertEquals("debug-mode: false\nenable-team-selection: false\n", ConfigService.read(file("settings.yml")));
    }

    @Test
    void setValuesAppliesAllChangesOrNone() throws IOException {
        ConfigService.writeAtomically(file("settings.yml"), "debug-mode: false\nplay-again-command: \"/hub\" # keep\n");
        service.loadAll();
        String hash = service.hash("settings");

        Map<String, Object> bad = new LinkedHashMap<>();
        bad.put("play-again-command", "/lobby");
        bad.put("debug-mode", "maybe");
        assertEquals(ApplyResult.Status.REJECTED, service.setValues("settings", bad, hash).getStatus());
        assertEquals(hash, service.hash("settings"));

        Map<String, Object> good = new LinkedHashMap<>();
        good.put("play-again-command", "/lobby");
        good.put("debug-mode", true);
        assertEquals(ApplyResult.Status.APPLIED, service.setValues("settings", good, hash).getStatus());
        assertEquals("debug-mode: true\nplay-again-command: \"/lobby\" # keep\n", ConfigService.read(file("settings.yml")));
        assertEquals("/lobby", service.get(SettingsConfig.class).getPlayAgainCommand());

        assertEquals(ApplyResult.Status.REJECTED, service.setValues("settings", good, hash).getStatus());
    }

    @Test
    void reloadWithErrorsKeepsCurrentValues() throws IOException {
        edit("settings.yml", "debug-mode: false", "debug-mode: maybe");
        ApplyResult result = service.reload("settings").get(0);
        assertEquals(ApplyResult.Status.REJECTED, result.getStatus());
        assertEquals(1, result.getProblems().size());
        assertFalse(service.get(SettingsConfig.class).isDebugMode());
    }

    @Test
    void restartRequiredFieldsKeepTheirRunningValue() throws IOException {
        edit("settings.yml", "editor-mode: false", "editor-mode: true");
        edit("settings.yml", "debug-mode: false", "debug-mode: true");
        ApplyResult result = service.reload("settings").get(0);
        assertEquals(ApplyResult.Status.RESTART_REQUIRED, result.getStatus());
        assertEquals(Collections.singletonList("editor-mode"), result.getRestartPaths());
        assertFalse(service.get(SettingsConfig.class).isEditorMode());
        assertTrue(service.get(SettingsConfig.class).isDebugMode());
    }

    @Test
    void restartDocumentsAreNotApplied() throws IOException {
        edit("database.yml", "port: 3306", "port: 3307");
        ApplyResult result = service.reload("database").get(0);
        assertEquals(ApplyResult.Status.RESTART_REQUIRED, result.getStatus());
        assertEquals(3306, service.get(DatabaseConfig.class).getPort());
    }

    @Test
    void nextGameDocumentsWaitForTheNextGame() throws IOException {
        gameRunning.set(true);
        edit("nested/gameplay.yml", "max-health: 20", "max-health: 40");
        assertEquals(ApplyResult.Status.NEXT_GAME, service.reload("gameplay").get(0).getStatus());
        assertEquals(20, service.get(GameplayConfig.class).getMaxHealth());

        service.applyPending();
        assertEquals(40, service.get(GameplayConfig.class).getMaxHealth());
    }

    @Test
    void writeChecksHashAndValidates() throws IOException {
        String hash = service.hash("settings");
        String text = ConfigService.read(file("settings.yml"));
        assertEquals(ApplyResult.Status.REJECTED, service.write("settings", "debug-mode: [", hash).getStatus());
        assertEquals(text, ConfigService.read(file("settings.yml")));

        assertEquals(ApplyResult.Status.APPLIED, service.write("settings", "debug-mode: true\n", hash).getStatus());
        assertNotEquals(hash, service.hash("settings"));
        assertEquals(ApplyResult.Status.REJECTED, service.write("settings", "debug-mode: false\n", hash).getStatus());
        assertTrue(service.get(SettingsConfig.class).isDebugMode());
    }

    @Test
    void validateDoesNotTouchDisk() throws IOException {
        String text = ConfigService.read(file("settings.yml"));
        List<ConfigProblem> problems = service.validate("settings", "economy-mode: XP\nunknown: 1\n");
        assertEquals(2, problems.size());
        assertEquals(text, ConfigService.read(file("settings.yml")));
    }
}
