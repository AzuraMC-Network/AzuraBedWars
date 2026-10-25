package cc.azuramc.bedwars.config;

import cc.azuramc.bedwars.config.annotation.Range;
import cc.azuramc.bedwars.config.annotation.Raw;
import cc.azuramc.bedwars.config.yaml.YamlBinder;
import cc.azuramc.bedwars.game.economy.EconomyType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlBinderTest {

    public static class Sample {
        @Range(min = 1, max = 10)
        int count = 5;
        boolean enabled = true;
        EconomyType mode = EconomyType.ITEM;
        String title = "&adefault";
        @Raw
        String password = "a&b";
        List<String> lines = new ArrayList<>(Arrays.asList("one", "two"));
        int[] times = {1, 2};
        Nested nested = new Nested();
    }

    public static class Nested {
        double radius = 1.5;
        String name = "nested";
    }

    private static Bound bind(String text) {
        YamlBinder binder = new YamlBinder("sample.yml", true);
        Sample value = binder.bind(binder.parse(text), Sample.class);
        return new Bound(value, binder.getProblems());
    }

    @Test
    void bindsValuesAndKeepsDefaultsForMissingKeys() {
        Bound bound = bind("count: 7\nmode: experience\ntitle: \"&cRed\"\npassword: \"p&ss\"\nnested:\n  name: inner\n");
        assertTrue(bound.problems.isEmpty(), bound.problems::toString);
        assertEquals(7, bound.value.count);
        assertEquals(EconomyType.EXPERIENCE, bound.value.mode);
        assertEquals("§cRed", bound.value.title);
        assertEquals("p&ss", bound.value.password);
        assertEquals("inner", bound.value.nested.name);
        assertEquals(1.5, bound.value.nested.radius);
        assertEquals(Arrays.asList("one", "two"), bound.value.lines);
    }

    @Test
    void reportsWrongTypeWithLineAndKeepsDefault() {
        Bound bound = bind("enabled: true\ncount: lots\n");
        assertEquals(1, bound.problems.size());
        ConfigProblem problem = bound.problems.get(0);
        assertTrue(problem.isError());
        assertEquals(2, problem.getLine());
        assertEquals("count", problem.getPath());
        assertEquals(5, bound.value.count);
    }

    @Test
    void suggestsCloseKeysAndEnumValues() {
        Bound bound = bind("cuont: 3\nmode: ITEMS\n");
        assertEquals(2, bound.problems.size());
        assertFalse(bound.problems.get(0).isError());
        assertTrue(bound.problems.get(0).getMessage().contains("Did you mean count?"), bound.problems.get(0)::getMessage);
        assertTrue(bound.problems.get(1).getMessage().contains("Did you mean ITEM?"), bound.problems.get(1)::getMessage);
        assertEquals(EconomyType.ITEM, bound.value.mode);
    }

    @Test
    void rejectsValuesOutOfRange() {
        Bound bound = bind("count: 11\n");
        assertEquals(1, bound.problems.size());
        assertEquals(5, bound.value.count);
    }

    @Test
    void explainsTabIndentation() {
        Bound bound = bind("nested:\n\tname: x\n");
        assertEquals(1, bound.problems.size());
        assertEquals(2, bound.problems.get(0).getLine());
        assertTrue(bound.problems.get(0).getMessage().startsWith("Tabs"), bound.problems.get(0)::getMessage);
    }

    @Test
    void acceptsSingleStringForListAndReadsArrays() {
        Bound bound = bind("lines: only\ntimes: [3, 4, 5]\n");
        assertTrue(bound.problems.isEmpty(), bound.problems::toString);
        assertEquals(Arrays.asList("only"), bound.value.lines);
        assertEquals(3, bound.value.times.length);
    }

    @Test
    void ignoresVersionKeyAndEmptyDocument() {
        assertTrue(bind("config-version: 1\n").problems.isEmpty());
        assertEquals(5, bind("").value.count);
    }

    @Test
    void skipsBadListItemsOnly() {
        Bound bound = bind("times: [1, x, 3]\n");
        assertEquals(1, bound.problems.size());
        assertEquals("times[1]", bound.problems.get(0).getPath());
        assertEquals(2, bound.value.times.length);
    }

    private static final class Bound {
        final Sample value;
        final List<ConfigProblem> problems;

        Bound(Sample value, List<ConfigProblem> problems) {
            this.value = value;
            this.problems = problems;
        }
    }
}
