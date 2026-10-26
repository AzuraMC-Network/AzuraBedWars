package cc.azuramc.bedwars.config;

import cc.azuramc.bedwars.config.object.*;
import cc.azuramc.bedwars.config.object.shop.ShopCategoryConfig;
import cc.azuramc.bedwars.config.object.shop.ShopMenuConfig;
import cc.azuramc.bedwars.config.object.shop.TierChains;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalizedTemplatesTest {

    private static final String[] TEMPLATES = {"upgrades.yml", "shop/menu.yml", "shop/tiers.yml",
            "shop/categories/blocks.yml", "shop/categories/melee.yml", "shop/categories/armor.yml", "shop/categories/tools.yml",
            "shop/categories/ranged.yml", "shop/categories/potions.yml", "shop/categories/utility.yml", "shop/categories/convert.yml"};

    @TempDir
    File dataFolder;

    private static ConfigService service(File dataFolder, String language) {
        ConfigService service = new ConfigService(dataFolder, () -> false);
        service.setLanguage(() -> language);
        service.register("database", "database.yml", DatabaseConfig.class, ApplyMode.RESTART, 1);
        service.register("gameplay", "gameplay.yml", GameplayConfig.class, ApplyMode.LIVE, 1);
        service.register("display", "display.yml", DisplayConfig.class, ApplyMode.LIVE, 1);
        service.register("generators", "generators.yml", GeneratorConfig.class, ApplyMode.NEXT_GAME, 2);
        service.register("timeline", "timeline.yml", TimelineConfig.class, ApplyMode.NEXT_GAME, 3);
        service.register("items", "items.yml", ItemConfig.class, ApplyMode.LIVE, 1);
        service.register("devtools", "devtools.yml", DevtoolsConfig.class, ApplyMode.LIVE, 1);
        service.registerTemplate("upgrades", "upgrades.yml", UpgradeConfig.class, ApplyMode.NEXT_GAME, 2, "upgrades");
        service.registerTemplate("shop/menu", "shop/menu.yml", ShopMenuConfig.class, ApplyMode.LIVE, 1, "shop/menu");
        service.registerTemplate("shop/tiers", "shop/tiers.yml", TierChains.class, ApplyMode.NEXT_GAME, 1, "shop/tiers");
        service.registerDirectory("shop/categories", ShopCategoryConfig.class, ApplyMode.LIVE, 1, "shop/category",
                "blocks.yml", "melee.yml", "armor.yml", "tools.yml", "ranged.yml", "potions.yml", "utility.yml", "convert.yml");
        return service;
    }

    @Test
    void chineseTemplatesHaveTheKeysOfTheDefaults() throws IOException {
        ConfigService english = service(new File(dataFolder, "en"), null);
        ConfigService chinese = service(new File(dataFolder, "zh"), "zh_CN");
        for (String id : new String[]{"database", "gameplay", "display", "generators", "timeline", "items", "devtools"}) {
            Map<String, Object> rendered = new Yaml().load(english.template(english.document(id)));
            rendered.remove("config-version");
            assertEquals(LangConfigTest.paths("", rendered), LangConfigTest.paths("", chinese.bundled(id + ".yml")), id);
        }
        for (String path : TEMPLATES) {
            assertEquals(LangConfigTest.paths("", english.bundled(path)), LangConfigTest.paths("", chinese.bundled(path)), path);
        }
    }

    @Test
    void chineseInstallUsesChineseTemplates() throws IOException {
        ConfigService service = service(dataFolder, "zh_CN");
        List<ConfigProblem> problems = service.loadAll();
        assertTrue(problems.isEmpty(), problems::toString);
        assertEquals("铁", service.get(GeneratorConfig.class).resource("iron").getName());
        assertEquals("§a方块", service.get("shop/categories/blocks", ShopCategoryConfig.class).getName());
        assertTrue(ConfigService.read(new File(dataFolder, "gameplay.yml")).contains("# 玩家最大生命值"));
        assertTrue(ConfigService.read(new File(dataFolder, "devtools.yml")).contains("# 监听端口"));
    }

    @Test
    void englishInstallUsesEnglishTemplates() {
        ConfigService service = service(dataFolder, "en_US");
        List<ConfigProblem> problems = service.loadAll();
        assertTrue(problems.isEmpty(), problems::toString);
        assertEquals("Iron", service.get(GeneratorConfig.class).resource("iron").getName());
        assertEquals("§aBlocks", service.get("shop/categories/blocks", ShopCategoryConfig.class).getName());
    }
}
