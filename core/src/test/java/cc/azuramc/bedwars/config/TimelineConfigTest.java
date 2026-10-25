package cc.azuramc.bedwars.config;

import cc.azuramc.bedwars.config.object.GeneratorConfig;
import cc.azuramc.bedwars.config.object.TimelineConfig;
import cc.azuramc.bedwars.game.timeline.TimelineValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimelineConfigTest {

    @TempDir
    File dataFolder;

    private ConfigService service;

    @BeforeEach
    void setUp() {
        service = new ConfigService(dataFolder, () -> false);
        service.register("generators", "generators.yml", GeneratorConfig.class, ApplyMode.NEXT_GAME, 2);
        service.register("timeline", "timeline.yml", TimelineConfig.class, ApplyMode.NEXT_GAME, 3);
        service.addMigration("timeline", 1, TimelineConfig::fromVersion1);
        service.addMigration("timeline", 2, TimelineConfig::fromVersion2);
        service.addValidator(new TimelineValidator(ShopConfigTest.ANY_NAME));
    }

    private static String messages(List<ConfigProblem> problems) {
        StringBuilder text = new StringBuilder();
        for (ConfigProblem problem : problems) {
            text.append(problem).append('\n');
        }
        return text.toString();
    }

    @Test
    void defaultStagesLoadWithoutProblems() {
        List<ConfigProblem> problems = service.loadAll();
        assertTrue(problems.isEmpty(), () -> messages(problems));
        List<TimelineConfig.Stage> stages = service.get(TimelineConfig.class).getStages();
        assertEquals(6, stages.size());
        assertEquals("§c§l床自毁", stages.get(4).getActions().get(1).getTitle());
    }

    @Test
    void version2EventsBecomeStages() throws IOException {
        ConfigService.writeAtomically(new File(dataFolder, "timeline.yml"), "config-version: 2\n"
                + "bed-destroyed-event:\n  execute-seconds: 100\n  title: \"&cBeds\"\n  title-stay: 30\n"
                + "game-over-event:\n  execute-seconds: 50\n  default-countdown: 9\n");
        List<ConfigProblem> problems = service.loadAll();
        assertTrue(problems.isEmpty(), () -> messages(problems));

        TimelineConfig timeline = service.get(TimelineConfig.class);
        TimelineConfig.Stage beds = timeline.getStages().get(4);
        assertEquals(100, beds.getAfter());
        assertEquals(TimelineConfig.Action.DESTROY_BEDS, beds.getActions().get(0).getType());
        assertEquals("§cBeds", beds.getActions().get(1).getTitle());
        assertEquals(30, beds.getActions().get(1).getStay());
        assertEquals(50, timeline.getStages().get(5).getAfter());
        assertEquals(9, timeline.getGameOverEvent().getDefaultCountdown());
    }

    @Test
    void generatorLevelsAreChecked() throws IOException {
        service.loadAll();
        File file = new File(dataFolder, "timeline.yml");
        String text = ConfigService.read(file);
        String first = "resource: \"diamond\"\n";
        int at = text.indexOf(first);
        text = text.substring(0, at) + "resource: \"iron\"\n" + text.substring(at + first.length());
        ConfigService.writeAtomically(file, text.replace("level: 3", "level: 4").replace("type: \"end-game\"", "type: \"message\""));

        String problems = messages(service.loadAll());
        assertTrue(problems.contains("Only a PUBLIC resource has a shared level, iron is BASE"), problems);
        assertTrue(problems.contains("Write a level from 1 to 3"), problems);
        assertTrue(problems.contains("A message action needs message lines"), problems);
        assertTrue(problems.contains("No stage has an end-game action"), problems);
    }
}
