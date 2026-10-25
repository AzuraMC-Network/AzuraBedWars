package cc.azuramc.bedwars.config;

import cc.azuramc.bedwars.config.migrate.LegacyConfigMigrator;
import cc.azuramc.bedwars.config.object.DatabaseConfig;
import cc.azuramc.bedwars.config.object.DisplayConfig;
import cc.azuramc.bedwars.config.object.GameplayConfig;
import cc.azuramc.bedwars.config.object.GeneratorConfig;
import cc.azuramc.bedwars.config.object.MessageConfig;
import cc.azuramc.bedwars.config.object.SettingsConfig;
import cc.azuramc.bedwars.database.storage.MapStorageType;
import cc.azuramc.bedwars.game.economy.EconomyType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyConfigMigratorTest {

    @TempDir
    File dataFolder;

    @Test
    void movesOldFilesIntoTheNewLayout() throws IOException {
        TestServer.install();
        File legacy = new File(dataFolder, "config");
        ConfigService.writeAtomically(new File(legacy, "settings.yml"), "# AzuraBedWars 主配置文件\n\n"
                + "editorMode: true\nmapStorage: \"MYSQL\"\neconomyMode: \"EXPERIENCE\"\nmaxHealth: 40\n"
                + "database:\n  databaseType: \"MySQL\"\n  password: \"s3&cret\"\n"
                + "chatConfig:\n  globalChatTag: \"§6[G]\"\n"
                + "lobbyScoreboard:\n  lines:\n    - \"&f你的模式: &a{mode}\"\n"
                + "waitingState:\n  header:\n    - \"§bHi\"\n");
        ConfigService.writeAtomically(new File(legacy, "player.yml"),
                "spectator:\n  settingGUI:\n    nightVersionSlot: 30\n");
        ConfigService.writeAtomically(new File(legacy, "tasks.yml"), "goldSpawnInterval: 4\nmaxDiamondStackLevel2: 7\n");
        ConfigService.writeAtomically(new File(legacy, "message.yml"), "spectator:\n  nightVersionEnabled: \"§aNV\"\n");

        ConfigService service = new ConfigService(dataFolder, () -> false);
        service.register("settings", "settings.yml", SettingsConfig.class, ApplyMode.LIVE, 1);
        service.register("database", "database.yml", DatabaseConfig.class, ApplyMode.RESTART, 1);
        service.register("gameplay", "gameplay.yml", GameplayConfig.class, ApplyMode.LIVE, 1);
        service.register("display", "display.yml", DisplayConfig.class, ApplyMode.LIVE, 1);
        service.register("generators", "generators.yml", GeneratorConfig.class, ApplyMode.NEXT_GAME, 1);
        service.register("messages", "messages.yml", MessageConfig.class, ApplyMode.LIVE, 1);

        LegacyConfigMigrator.migrateIfPresent(dataFolder, service);
        assertTrue(service.loadAll().isEmpty());

        assertFalse(legacy.exists());
        File[] backups = dataFolder.listFiles((dir, name) -> name.startsWith("config-backup-"));
        assertEquals(1, backups.length);

        SettingsConfig settings = service.get(SettingsConfig.class);
        assertTrue(settings.isEditorMode());
        assertEquals(MapStorageType.MYSQL, settings.getMapStorage());
        assertEquals(EconomyType.EXPERIENCE, settings.getEconomyMode());
        assertEquals("s3&cret", service.get(DatabaseConfig.class).getPassword());

        GameplayConfig gameplay = service.get(GameplayConfig.class);
        assertEquals(40, gameplay.getMaxHealth());
        assertEquals("§6[G]", gameplay.getChat().getGlobalChatTag());
        assertEquals(30, gameplay.getSpectator().getSettingGui().getNightVisionSlot());

        DisplayConfig display = service.get(DisplayConfig.class);
        assertEquals("§f模式: §a{mode}", display.getLobbyScoreboard().getLines().get(0));
        assertEquals("§bHi", display.getTabList().getWaiting().getHeader().get(0));

        GeneratorConfig generators = service.get(GeneratorConfig.class);
        assertEquals(3, generators.getGoldInterval());
        assertEquals(7, generators.getMaxDiamondStackLevel2());
        assertEquals("§aNV", service.get(MessageConfig.class).getSpectator().getNightVisionEnabled());

        String written = ConfigService.read(new File(dataFolder, "gameplay.yml"));
        assertTrue(written.contains("global-chat-tag: \"&6[G]\""), written);
    }
}
