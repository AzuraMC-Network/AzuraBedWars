package cc.azuramc.bedwars.config;

import cc.azuramc.bedwars.config.object.GeneratorConfig;
import cc.azuramc.bedwars.config.object.shop.ShopCategoryConfig;
import cc.azuramc.bedwars.config.object.shop.ShopMenuConfig;
import cc.azuramc.bedwars.config.object.shop.TierChains;
import cc.azuramc.bedwars.shop.ShopValidator;
import cc.azuramc.bedwars.shop.SpecChecks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopConfigTest {

    static final SpecChecks.Names ANY_NAME = new SpecChecks.Names() {
        @Override
        public boolean material(String name) {
            return !name.startsWith("NOT_");
        }

        @Override
        public boolean enchantment(String name) {
            return true;
        }

        @Override
        public boolean effect(String name) {
            return true;
        }

        @Override
        public boolean flag(String name) {
            return true;
        }

        @Override
        public boolean entity(String name) {
            return true;
        }

        @Override
        public boolean sound(String name) {
            return true;
        }
    };

    @TempDir
    File dataFolder;

    private ConfigService service;

    @BeforeEach
    void setUp() {
        service = new ConfigService(dataFolder, () -> false);
        service.register("generators", "generators.yml", GeneratorConfig.class, ApplyMode.NEXT_GAME, 2);
        service.addMigration("generators", 1, old -> GeneratorConfig.fromVersion1(old, service.content("timeline.yml"),
                service.bundled("generators.yml", GeneratorConfig.class)));
        service.registerTemplate("shop/menu", "shop/menu.yml", ShopMenuConfig.class, ApplyMode.LIVE, 1, "shop/menu");
        service.registerTemplate("shop/tiers", "shop/tiers.yml", TierChains.class, ApplyMode.NEXT_GAME, 1, "shop/tiers");
        service.registerDirectory("shop/categories", ShopCategoryConfig.class, ApplyMode.LIVE, 1, "shop/category",
                "blocks.yml", "melee.yml", "armor.yml", "tools.yml", "ranged.yml", "potions.yml", "utility.yml", "convert.yml");
        service.addValidator(new ShopValidator(ANY_NAME));
    }

    private void edit(String path, String from, String to) throws IOException {
        File file = new File(dataFolder, path);
        String text = ConfigService.read(file);
        assertTrue(text.contains(from), from);
        ConfigService.writeAtomically(file, text.replace(from, to));
    }

    private static String messages(List<ConfigProblem> problems) {
        StringBuilder text = new StringBuilder();
        for (ConfigProblem problem : problems) {
            text.append(problem).append('\n');
        }
        return text.toString();
    }

    @Test
    void bundledShopLoadsWithoutProblems() {
        List<ConfigProblem> problems = service.loadAll();
        assertTrue(problems.isEmpty(), () -> messages(problems));
        assertEquals(8, service.all(ShopCategoryConfig.class).size());
        ShopCategoryConfig blocks = service.get("shop/categories/blocks", ShopCategoryConfig.class);
        assertEquals(4, blocks.getItems().get("wool").getPrice().getAmount());
        assertEquals("iron", blocks.getItems().get("wool").getPrice().getCurrency());
        assertEquals(TierChains.OnDeath.DOWNGRADE, service.get(TierChains.class).get("pickaxe").getOnDeath());
        assertEquals(14, service.get(ShopMenuConfig.class).getQuickBuy().getDefaults().size());
        assertTrue(new File(dataFolder, "schema/shop-category.json").isFile());
    }

    @Test
    void brokenReferencesAreReportedWithTheirLine() throws IOException {
        service.loadAll();
        edit("shop/categories/tools.yml", "price: 30 iron", "price: 30 irn");
        edit("shop/categories/tools.yml", "tier: axe", "tier: axes");
        edit("shop/menu.yml", "utility.water]", "utility.watr]");
        edit("shop/categories/utility.yml", "item: TNT", "item: NOT_TNT");

        String problems = messages(service.loadAll());
        assertTrue(problems.contains("shop/categories/tools.yml:17 items.shears.price"), problems);
        assertTrue(problems.contains("shop/categories/utility.yml:16 items.tnt.item.material"), problems);
        assertTrue(problems.contains("Unknown resource irn, did you mean iron?"), problems);
        assertTrue(problems.contains("Unknown tier chain axes, did you mean axe?"), problems);
        assertTrue(problems.contains("Unknown item utility.watr, did you mean utility.water?"), problems);
        assertTrue(problems.contains("No material NOT_TNT on this server"), problems);
    }

    @Test
    void reloadRejectsAChangeThatBreaksAReference() throws IOException {
        service.loadAll();
        edit("shop/categories/tools.yml", "price: 30 iron", "price: 31 copper");
        ApplyResult result = service.reload("shop/categories/tools").get(0);
        assertEquals(ApplyResult.Status.REJECTED, result.getStatus());
        assertEquals(30, service.get("shop/categories/tools", ShopCategoryConfig.class).getItems().get("shears").getPrice().getAmount());

        edit("generators.yml", "  gold:", "  copper:\n    material: COPPER_INGOT\n    levels:\n      - interval: 2\n  gold:");
        service.reload("generators");
        assertEquals(ApplyResult.Status.APPLIED, service.reload("shop/categories/tools").get(0).getStatus());
    }

    @Test
    void addedCategoryFileIsCheckedBeforeItIsUsed() throws IOException {
        service.loadAll();
        ConfigService.writeAtomically(new File(dataFolder, "shop/categories/extra.yml"), "config-version: 1\n"
                + "items:\n  pick:\n    tier: pickaxe\n    level: 9\n");
        ApplyResult result = service.reload("shop/categories").stream()
                .filter(applied -> applied.getDocumentId().equals("shop/categories/extra")).findFirst().get();
        assertEquals(ApplyResult.Status.REJECTED, result.getStatus());
        assertFalse(service.all(ShopCategoryConfig.class).containsKey("shop/categories/extra"));
    }

    @Test
    void generatorsOfVersion1AreUpgradedOnLoad() throws IOException {
        ConfigService.writeAtomically(new File(dataFolder, "generators.yml"), "config-version: 1\n"
                + "iron-interval: 2\nmax-emerald-stack-level3: 9\ndiamond-name: \"&3Dia\"\nlevel-i: \"&eTier &cI\"\n"
                + "time-remaining-format: \"&e%d s\"\n");
        ConfigService.writeAtomically(new File(dataFolder, "timeline.yml"), "config-version: 1\n"
                + "diamond-update-event:\n  level2-refresh-second: 20\n");
        List<ConfigProblem> problems = service.loadAll();
        assertTrue(problems.isEmpty(), () -> messages(problems));

        GeneratorConfig generators = service.get(GeneratorConfig.class);
        assertEquals(2, generators.resource("iron").level(1).getInterval());
        assertEquals(9, generators.resource("emerald").level(3).getMaxStack());
        assertEquals(20, generators.resource("diamond").level(2).getInterval());
        assertEquals("§eTier §c{level}", generators.resource("diamond").getHologram().get(0));
        assertEquals("§3Dia", generators.resource("diamond").getHologram().get(1));
        assertEquals("§e{seconds} s", generators.resource("diamond").getHologram().get(2));

        String written = ConfigService.read(new File(dataFolder, "generators.yml"));
        assertTrue(written.contains("config-version: 2"), written);
        assertTrue(written.contains("- \"&3Dia\""), written);
        assertTrue(new File(dataFolder, ConfigService.HISTORY_DIR).list().length > 0);
    }
}
