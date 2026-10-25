package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.api.config.Comment;
import cc.azuramc.bedwars.api.config.Range;
import cc.azuramc.bedwars.api.config.Raw;
import cc.azuramc.bedwars.game.map.MapData;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author an5w1r@163.com
 */
@Data
@Comment({"Generators", "Every resource is also a shop currency, prices are written as amount and resource id, such as 4 iron"})
public class GeneratorConfig {

    @Comment("Resources by id")
    private LinkedHashMap<String, Resource> resources = defaults();

    @Nullable
    public Resource resource(String id) {
        return resources.get(id);
    }

    @Data
    public static class Resource {
        @Comment("Item material")
        @Raw
        private String material = "IRON_INGOT";

        @Comment("Name shown in prices")
        private String name = "Iron";

        @Comment("BASE spawns at every team base, PUBLIC at the public generators of the map")
        private Spawn at = Spawn.BASE;

        @Comment("Map generator type used by a PUBLIC resource")
        @Nullable
        private MapData.DropType drop;

        @Comment("Block spinning above a PUBLIC generator")
        @Nullable
        @Raw
        private String block;

        @Comment({"Hologram lines above a PUBLIC generator, from top to bottom", "Placeholders: {level} roman level, {seconds} until the next spawn"})
        private List<String> hologram = new ArrayList<>();

        @Comment("Levels gained per item picked up in experience mode")
        @Range(min = 0)
        private int xpValue = 1;

        @Comment({"Spawn interval and max stack per level", "The first entry is the starting level, the timeline moves a generator to the next one"})
        private List<Level> levels = new ArrayList<>();

        public Level level(int level) {
            return levels.get(Math.max(0, Math.min(level, levels.size()) - 1));
        }
    }

    @Data
    public static class Level {
        @Comment("Spawn interval in seconds")
        @Range(min = 0.05)
        private double interval = 1;

        @Comment("Most items a generator holds")
        @Range(min = 1)
        private int maxStack = 48;

        public Level() {
        }

        public Level(double interval, int maxStack) {
            this.interval = interval;
            this.maxStack = maxStack;
        }
    }

    public enum Spawn {
        BASE,
        PUBLIC
    }

    private static LinkedHashMap<String, Resource> defaults() {
        LinkedHashMap<String, Resource> resources = new LinkedHashMap<>();
        resources.put("iron", resource("IRON_INGOT", "Iron", 1, new Level(1, 48)));
        resources.put("gold", resource("GOLD_INGOT", "Gold", 3, new Level(3, 8)));
        Resource diamond = resource("DIAMOND", "Diamond", 40, new Level(35, 4), new Level(23, 6), new Level(15, 8));
        publicResource(diamond, MapData.DropType.DIAMOND, "DIAMOND_BLOCK", "&bDiamond");
        resources.put("diamond", diamond);
        Resource emerald = resource("EMERALD", "Emerald", 80, new Level(60, 2), new Level(23, 4), new Level(15, 4));
        publicResource(emerald, MapData.DropType.EMERALD, "EMERALD_BLOCK", "&2Emerald");
        resources.put("emerald", emerald);
        return resources;
    }

    private static Resource resource(String material, String name, int xpValue, Level... levels) {
        Resource resource = new Resource();
        resource.setMaterial(material);
        resource.setName(name);
        resource.setXpValue(xpValue);
        resource.setLevels(new ArrayList<>(Arrays.asList(levels)));
        return resource;
    }

    private static void publicResource(Resource resource, MapData.DropType drop, String block, String title) {
        resource.setAt(Spawn.PUBLIC);
        resource.setDrop(drop);
        resource.setBlock(block);
        resource.setHologram(new ArrayList<>(Arrays.asList("&eTier &c{level}", title, "&eSpawns in &c{seconds} &eseconds")));
    }

    /**
     * Version 1 kept every value in its own flat key and the level 2 and 3 intervals in timeline.yml
     *
     * @param timeline content of a version 1 timeline.yml
     */
    public static Map<String, Object> fromVersion1(Map<String, Object> generators, Map<String, Object> timeline, GeneratorConfig defaults) {
        Map<String, Object> old = new LinkedHashMap<>(generators);
        for (String id : Arrays.asList("diamond", "emerald")) {
            Object event = timeline.get(id + "-update-event");
            for (int level = 2; level <= 3 && event instanceof Map; level++) {
                Object seconds = ((Map<?, ?>) event).get("level" + level + "-refresh-second");
                if (seconds != null) {
                    old.putIfAbsent(id + "-level" + level + "-interval", seconds);
                }
            }
        }
        Map<String, Object> resources = new LinkedHashMap<>();
        for (Map.Entry<String, Resource> entry : defaults.getResources().entrySet()) {
            String id = entry.getKey();
            Resource resource = entry.getValue();
            Map<String, Object> upgraded = new LinkedHashMap<>();
            upgraded.put("material", resource.getMaterial());
            upgraded.put("name", resource.getName());
            upgraded.put("at", resource.getAt().name());
            if (resource.getDrop() != null) {
                upgraded.put("drop", resource.getDrop().name());
                upgraded.put("block", resource.getBlock());
                upgraded.put("hologram", hologramFromVersion1(old, resource.getHologram(), id + "-name"));
            }
            upgraded.put("xp-value", resource.getXpValue());
            List<Object> levels = new ArrayList<>();
            for (int i = 0; i < resource.getLevels().size(); i++) {
                Level level = resource.getLevels().get(i);
                Map<String, Object> upgradedLevel = new LinkedHashMap<>();
                upgradedLevel.put("interval", firstPresent(old, level.getInterval(), i == 0 ? id + "-interval" : id + "-level" + (i + 1) + "-interval"));
                upgradedLevel.put("max-stack", firstPresent(old, level.getMaxStack(), id + "-max-stack", "max-" + id + "-stack-level" + (i + 1)));
                levels.add(upgradedLevel);
            }
            upgraded.put("levels", levels);
            resources.put(id, upgraded);
        }
        Map<String, Object> upgraded = new LinkedHashMap<>();
        upgraded.put("resources", resources);
        return upgraded;
    }

    private static List<Object> hologramFromVersion1(Map<String, Object> old, List<String> defaults, String nameKey) {
        List<Object> lines = new ArrayList<>(defaults);
        Object level = old.get("level-i");
        if (level instanceof String && ((String) level).endsWith("I")) {
            lines.set(0, ((String) level).substring(0, ((String) level).length() - 1) + "{level}");
        }
        if (old.get(nameKey) != null) {
            lines.set(1, old.get(nameKey));
        }
        Object time = old.get("time-remaining-format");
        if (time instanceof String) {
            lines.set(2, ((String) time).replace("%d", "{seconds}"));
        }
        return lines;
    }

    private static Object firstPresent(Map<String, Object> old, Object fallback, String... keys) {
        for (String key : keys) {
            if (old.get(key) != null) {
                return old.get(key);
            }
        }
        return fallback;
    }
}
