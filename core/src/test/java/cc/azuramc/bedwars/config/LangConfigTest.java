package cc.azuramc.bedwars.config;

import cc.azuramc.bedwars.config.migrate.MessagesMigrator;
import cc.azuramc.bedwars.config.object.LangConfig;
import cc.azuramc.bedwars.config.yaml.YamlWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LangConfigTest {

    @TempDir
    File dataFolder;

    private ConfigService service;

    @BeforeEach
    void setUp() {
        TestServer.install();
        service = new ConfigService(dataFolder, () -> false);
        service.registerDirectory("lang", LangConfig.class, ApplyMode.LIVE, 1, "lang", "en_US.yml", "zh_CN.yml");
    }

    @Test
    void bundledEnglishMatchesTheDefaults() throws IOException {
        try (InputStream in = LangConfigTest.class.getResourceAsStream("/config-templates/lang/en_US.yml")) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            assertEquals(YamlWriter.write(new LangConfig(), 1, "../schema/lang.json"), new String(out.toByteArray(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void bothLanguagesLoadWithTheSameKeys() {
        List<ConfigProblem> problems = service.loadAll();
        assertTrue(problems.isEmpty(), problems::toString);
        assertEquals(paths("", service.bundled("lang/en_US.yml")), paths("", service.bundled("lang/zh_CN.yml")));
        LangConfig chinese = service.get("lang/zh_CN", LangConfig.class);
        assertEquals("§c红", chinese.getTeams().get("RED"));
        assertEquals(4, chinese.getDeath().getSelf().get("VOID").size());
    }

    @Test
    void changedMessagesMoveIntoChinese() throws IOException {
        assertTrue(service.loadAll().isEmpty());
        ConfigService.writeAtomically(new File(dataFolder, MessagesMigrator.FILE), "team-eliminated-message: \"%s &c没了 %s\"\n"
                + "player-leave-message: \"&7%s&e离开游戏\"\n"
                + "spectator:\n  first-person-action-bar: \"&f%s %d\"\n  menu-hint: \" hint\"\n");

        assertTrue(MessagesMigrator.migrateIfPresent(dataFolder, service).isEmpty());
        LangConfig chinese = service.get("lang/zh_CN", LangConfig.class);
        assertEquals("{team} §c没了 {breaker}", chinese.getGame().getTeamEliminated());
        assertEquals("§f{player} {health} hint", chinese.getSpectator().getFirstPersonActionBar());
        assertEquals("§7{player}§e离开游戏", chinese.getGame().getLeft());
        String written = ConfigService.read(new File(dataFolder, "lang/zh_CN.yml"));
        assertTrue(written.contains("  # {team} {breaker}\n  team-eliminated: \"{team} &c没了 {breaker}\""), written);
        assertFalse(new File(dataFolder, MessagesMigrator.FILE).exists());
    }

    @Test
    void unchangedMessagesKeepTheBundledFile() throws IOException {
        assertTrue(service.loadAll().isEmpty());
        ConfigService.writeAtomically(new File(dataFolder, MessagesMigrator.FILE), "player-leave-message: \"&7%s&e离开游戏\"\n"
                + "spectator:\n  first-person-action-bar: \"&f目标: &a&l%s  &f生命值: &a&l%d &c&l❤\"\n"
                + "  menu-hint: \"  &a点击左键打开菜单  &c按Shift退出\"\n");

        String bundled = ConfigService.read(new File(dataFolder, "lang/zh_CN.yml"));
        assertTrue(MessagesMigrator.migrateIfPresent(dataFolder, service).isEmpty());
        assertEquals(bundled, ConfigService.read(new File(dataFolder, "lang/zh_CN.yml")));
        assertFalse(new File(dataFolder, MessagesMigrator.FILE).exists());
    }

    @Test
    void keysMissingFromAFileKeepTheBundledLanguage() throws IOException {
        ConfigService.writeAtomically(new File(dataFolder, "lang/zh_CN.yml"), "config-version: 1\nadmin:\n  config-clean: \"&a没有问题\"\n");

        List<ConfigProblem> problems = service.loadAll();
        assertTrue(problems.isEmpty(), problems::toString);
        LangConfig chinese = service.get("lang/zh_CN", LangConfig.class);
        assertEquals("§a没有问题", chinese.getAdmin().getConfigClean());
        assertEquals("§eMCP 未开启 /bw devtools on 开启", chinese.getAdmin().getDevtoolsNotRunning());
    }

    static Set<String> paths(String prefix, Map<String, Object> section) {
        Set<String> paths = new TreeSet<>();
        for (Map.Entry<String, Object> entry : section.entrySet()) {
            String path = prefix + entry.getKey();
            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> child = (Map<String, Object>) entry.getValue();
                paths.addAll(paths(path + ".", child));
            } else {
                paths.add(path);
            }
        }
        return paths;
    }
}
