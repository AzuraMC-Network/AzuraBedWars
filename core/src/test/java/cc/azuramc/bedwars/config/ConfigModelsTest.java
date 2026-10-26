package cc.azuramc.bedwars.config;

import cc.azuramc.bedwars.config.object.DatabaseConfig;
import cc.azuramc.bedwars.config.object.DisplayConfig;
import cc.azuramc.bedwars.config.object.GameplayConfig;
import cc.azuramc.bedwars.config.object.GeneratorConfig;
import cc.azuramc.bedwars.config.object.ItemConfig;
import cc.azuramc.bedwars.config.object.LangConfig;
import cc.azuramc.bedwars.config.object.SettingsConfig;
import cc.azuramc.bedwars.config.object.TimelineConfig;
import cc.azuramc.bedwars.config.object.UpgradeConfig;
import cc.azuramc.bedwars.config.yaml.SchemaExporter;
import cc.azuramc.bedwars.config.yaml.YamlBinder;
import cc.azuramc.bedwars.config.yaml.YamlWriter;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigModelsTest {

    private static final List<Class<?>> MODELS = Arrays.asList(SettingsConfig.class, DatabaseConfig.class,
            GameplayConfig.class, DisplayConfig.class, GeneratorConfig.class, TimelineConfig.class,
            ItemConfig.class, UpgradeConfig.class, LangConfig.class);

    @Test
    void generatedTemplatesReadBackAsTheDefaults() throws Exception {
        for (Class<?> model : MODELS) {
            Object defaults = model.getDeclaredConstructor().newInstance();
            String text = YamlWriter.write(defaults, 1, "schema/x.json");
            YamlBinder binder = new YamlBinder(model.getSimpleName(), false);
            Object read = binder.bind(binder.parse(text), model);
            assertTrue(binder.getProblems().isEmpty(), () -> model.getSimpleName() + " " + binder.getProblems() + "\n" + text);
            assertEquals(defaults, read, model.getSimpleName());
        }
    }

    @Test
    void defaultsUseAmpersandColors() throws Exception {
        for (Class<?> model : MODELS) {
            String text = YamlWriter.write(model.getDeclaredConstructor().newInstance(), 1, null);
            assertTrue(text.indexOf('§') < 0, model.getSimpleName());
        }
    }

    @Test
    void schemasDescribeEveryKey() {
        JsonObject schema = SchemaExporter.export(GameplayConfig.class, "gameplay.yml", kind -> Collections.emptyMap());
        JsonObject properties = schema.getAsJsonObject("properties");
        assertTrue(properties.has("max-health"));
        assertEquals(1, properties.getAsJsonObject("max-health").get("minimum").getAsInt());
        assertTrue(properties.getAsJsonObject("chat").getAsJsonObject("properties").has("global-chat-cooldown"));
        JsonObject settings = SchemaExporter.export(SettingsConfig.class, "settings.yml", kind -> Collections.emptyMap()).getAsJsonObject("properties");
        assertEquals("ITEM", settings.getAsJsonObject("economy-mode").get("default").getAsString());
        assertTrue(settings.getAsJsonObject("editor-mode").get("x-restart-required").getAsBoolean());
    }
}
