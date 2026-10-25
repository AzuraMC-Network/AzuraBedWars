package cc.azuramc.bedwars.config;

import cc.azuramc.bedwars.config.object.GeneratorConfig;
import cc.azuramc.bedwars.config.object.UpgradeConfig;
import cc.azuramc.bedwars.upgrade.UpgradeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpgradeConfigTest {

    @TempDir
    File dataFolder;

    private ConfigService service;

    @BeforeEach
    void setUp() {
        service = new ConfigService(dataFolder, () -> false);
        service.register("generators", "generators.yml", GeneratorConfig.class, ApplyMode.NEXT_GAME, 2);
        service.registerTemplate("upgrades", "upgrades.yml", UpgradeConfig.class, ApplyMode.NEXT_GAME, 2, "upgrades");
        service.addMigration("upgrades", 1, old -> UpgradeConfig.fromVersion1(old, ConfigService.bundled("upgrades.yml")));
        service.addValidator(new UpgradeValidator(ShopConfigTest.ANY_NAME));
    }

    private static String messages(List<ConfigProblem> problems) {
        StringBuilder text = new StringBuilder();
        for (ConfigProblem problem : problems) {
            text.append(problem).append('\n');
        }
        return text.toString();
    }

    @Test
    void bundledUpgradesLoadWithoutProblems() {
        List<ConfigProblem> problems = service.loadAll();
        assertTrue(problems.isEmpty(), () -> messages(problems));
        UpgradeConfig config = service.get(UpgradeConfig.class);
        assertEquals(6, config.getUpgrades().size());
        assertEquals(4, config.getUpgrades().get("R").getLevels().size());
        assertEquals("emerald", config.getUpgrades().get("R").getLevels().get(2).getEffects().get(2).getResource());
        assertEquals("reveal", config.getTraps().getTypes().get("L").getEffects().get(0).getType());
        assertEquals(2, config.getTraps().getTypes().get("L").getAlert().size());
        assertEquals("§c§l陷阱触发！", config.getTraps().getAlert().get(0).getTitle());
    }

    @Test
    void unchangedVersion1KeepsTheCommentedTemplate() throws IOException {
        ConfigService.writeAtomically(new File(dataFolder, "upgrades.yml"), "config-version: 1\n"
                + "healing-pool-range: 18.0\nhaste-effect-duration: 40\ntrap-effect-amplifier: 0\n");
        List<ConfigProblem> problems = service.loadAll();
        assertTrue(problems.isEmpty(), () -> messages(problems));
        assertEquals(service.template(service.document("upgrades")), ConfigService.read(new File(dataFolder, "upgrades.yml")));
    }

    @Test
    void version1TuningMovesIntoTheEffects() throws IOException {
        ConfigService.writeAtomically(new File(dataFolder, "upgrades.yml"), "config-version: 1\n"
                + "healing-pool-range: 12.0\nhaste-effect-duration: 100\ntrap-effect-duration: 60\ntrap-effect-amplifier: 1\n");
        List<ConfigProblem> problems = service.loadAll();
        assertTrue(problems.isEmpty(), () -> messages(problems));

        UpgradeConfig config = service.get(UpgradeConfig.class);
        assertEquals(12, config.getBaseRange());
        assertEquals(5, config.getUpgrades().get("M").getLevels().get(1).getEffects().get(0).getSeconds());
        UpgradeConfig.Effect blindness = config.getTraps().getTypes().get("B").getEffects().get(0);
        assertEquals(3, blindness.getSeconds());
        assertEquals(2, blindness.getLevel());
        assertEquals(8, config.getTraps().getTypes().get("N").getEffects().get(0).getSeconds());
        assertTrue(ConfigService.read(new File(dataFolder, "upgrades.yml")).contains("config-version: 2"));
    }

    @Test
    void brokenEffectsAndLayoutAreReported() throws IOException {
        service.loadAll();
        File file = new File(dataFolder, "upgrades.yml");
        String text = ConfigService.read(file)
                .replace("resource: gold, interval: 3", "resource: gld, interval: 3")
                .replace("type: enchant, target: sword", "type: enchnt, target: sword")
                .replace("target: base, effect: REGENERATION", "target: bse, effect: REGENERATION")
                .replace("\" RHC N   \"", "\" RHCXN   \"");
        ConfigService.writeAtomically(file, text);

        String problems = messages(service.loadAll());
        assertTrue(problems.contains("Unknown resource gld, did you mean gold?"), problems);
        assertTrue(problems.contains("Unknown effect type enchnt, did you mean enchant?"), problems);
        assertTrue(problems.contains("Unknown target bse, did you mean base?"), problems);
        assertTrue(problems.contains("No upgrade, trap or decor has the key X"), problems);
    }
}
