package cc.azuramc.bedwars.extension;

import cc.azuramc.bedwars.api.config.Range;
import cc.azuramc.bedwars.api.extension.NoSettings;
import cc.azuramc.bedwars.config.ApplyMode;
import cc.azuramc.bedwars.config.ConfigProblem;
import cc.azuramc.bedwars.config.ConfigService;
import cc.azuramc.bedwars.config.object.GeneratorConfig;
import cc.azuramc.bedwars.config.object.TimelineConfig;
import cc.azuramc.bedwars.config.object.UpgradeConfig;
import cc.azuramc.bedwars.config.object.shop.ShopCategoryConfig;
import cc.azuramc.bedwars.config.object.shop.ShopMenuConfig;
import cc.azuramc.bedwars.config.object.shop.TierChains;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtensionsTest {

    public static class GrappleSettings {
        @Range(min = 5, max = 60)
        private double range = 30;
    }

    private static final String GRAPPLE = "items:\n"
            + "  grapple:\n"
            + "    name: \"Grapple\"\n"
            + "    item: FISHING_ROD\n"
            + "    price: 1 iron\n"
            + "    custom: test:grapple\n"
            + "    settings:\n"
            + "      range: 100\n";

    @TempDir
    File dataFolder;

    private ConfigService service;
    private Extensions extensions;
    private ExtensionValidator validator;
    private final Plugin plugin = (Plugin) Proxy.newProxyInstance(Plugin.class.getClassLoader(), new Class<?>[]{Plugin.class},
            (proxy, method, args) -> "getName".equals(method.getName()) ? "Test" : null);

    @BeforeEach
    void setUp() {
        service = new ConfigService(dataFolder, () -> false);
        service.register("generators", "generators.yml", GeneratorConfig.class, ApplyMode.NEXT_GAME, 2);
        service.register("timeline", "timeline.yml", TimelineConfig.class, ApplyMode.NEXT_GAME, 3);
        service.registerTemplate("upgrades", "upgrades.yml", UpgradeConfig.class, ApplyMode.NEXT_GAME, 2, "upgrades");
        service.registerTemplate("shop/menu", "shop/menu.yml", ShopMenuConfig.class, ApplyMode.LIVE, 1, "shop/menu");
        service.registerTemplate("shop/tiers", "shop/tiers.yml", TierChains.class, ApplyMode.NEXT_GAME, 1, "shop/tiers");
        service.registerDirectory("shop/categories", ShopCategoryConfig.class, ApplyMode.LIVE, 1, "shop/category", "utility.yml");
        extensions = new Extensions("test"::equals);
        validator = new ExtensionValidator(extensions);
        service.addValidator(validator);
        service.setExtensionSettings(kind -> extensions.settingsTypes(ExtensionKind.of(kind)));
    }

    private void edit(String path, String from, String to) throws IOException {
        File file = new File(dataFolder, path);
        ConfigService.writeAtomically(file, ConfigService.read(file).replace(from, to));
    }

    private static String messages(List<ConfigProblem> problems) {
        StringBuilder text = new StringBuilder();
        for (ConfigProblem problem : problems) {
            text.append(problem).append('\n');
        }
        return text.toString();
    }

    @Test
    void referencesAreCheckedOnceReady() throws IOException {
        service.loadAll();
        edit("shop/categories/utility.yml", "items:\n", GRAPPLE);
        edit("upgrades.yml", "        - reveal\n", "        - reveal\n        - type: other:siren\n");
        edit("timeline.yml", "      - type: \"end-game\"", "      - type: test:meteor\n      - type: \"end-game\"");
        assertTrue(service.loadAll().isEmpty());

        extensions.registerItem(plugin, "grapple", GrappleSettings.class, (context, settings) -> {
        });
        extensions.markReady();
        String problems = messages(service.validate(validator));
        assertTrue(problems.contains("shop/categories/utility.yml:14 items.grapple.settings.range: 100.0 is out of range 5 to 60"), problems);
        assertTrue(problems.contains("Plugin other is not installed or not enabled"), problems);
        assertTrue(problems.contains("Unknown timeline action test:meteor"), problems);
    }

    @Test
    void schemaTypesTheSettingsOfRegisteredExtensions() {
        extensions.registerItem(plugin, "grapple", GrappleSettings.class, (context, settings) -> {
        });
        service.loadAll();
        String schema = service.schema("shop/categories/utility").toString();
        assertTrue(schema.contains("{\"if\":{\"properties\":{\"custom\":{\"const\":\"test:grapple\"}},\"required\":[\"custom\"]},"
                + "\"then\":{\"properties\":{\"settings\":{\"type\":\"object\",\"properties\":{\"range\":{\"type\":\"number\",\"default\":30.0,"
                + "\"minimum\":5.0,\"maximum\":60.0}},\"additionalProperties\":false}}}}"), schema);
    }

    @Test
    void disabledPluginsLoseTheirExtensions() {
        extensions.registerTimelineAction(plugin, "meteor", NoSettings.class, (game, settings) -> {
        });
        extensions.markReady();
        assertEquals(Extensions.Status.AVAILABLE, extensions.status(ExtensionKind.TIMELINE_ACTION, "test:meteor"));
        assertThrows(IllegalArgumentException.class, () -> extensions.registerTimelineAction(plugin, "meteor", NoSettings.class, (game, settings) -> {
        }));
        assertThrows(IllegalArgumentException.class, () -> extensions.registerTimelineAction(plugin, "Meteor", NoSettings.class, (game, settings) -> {
        }));

        extensions.unregisterAll(plugin);
        assertEquals(Extensions.Status.UNKNOWN, extensions.status(ExtensionKind.TIMELINE_ACTION, "test:meteor"));
        assertEquals(Extensions.Status.NO_PLUGIN, extensions.status(ExtensionKind.TIMELINE_ACTION, "other:meteor"));
    }

    @Test
    void devToolsAreNamedByPluginAndRemovedWithIt() {
        JsonObject schema = new JsonObject();
        extensions.registerDevTool(plugin, "dump", "Dump", schema, true, arguments -> new JsonPrimitive("ok"));
        assertEquals("test_dump", extensions.getDevTools().get(0).getName());
        assertThrows(IllegalArgumentException.class,
                () -> extensions.registerDevTool(plugin, "dump", "Again", schema, true, arguments -> new JsonPrimitive("ok")));
        assertThrows(IllegalArgumentException.class,
                () -> extensions.registerDevTool(plugin, "Bad Id", "Bad", schema, true, arguments -> new JsonPrimitive("ok")));
        extensions.unregisterAll(plugin);
        assertTrue(extensions.getDevTools().isEmpty());
    }
}
