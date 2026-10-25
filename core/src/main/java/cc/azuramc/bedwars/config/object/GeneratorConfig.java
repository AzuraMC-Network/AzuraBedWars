package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.config.annotation.Comment;
import cc.azuramc.bedwars.config.annotation.Range;
import cc.azuramc.bedwars.config.annotation.Raw;
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
@Comment({"资源点配置", "每种资源同时就是商店的货币，价格写成 数量 资源id，例如 4 iron"})
public class GeneratorConfig {

    @Comment("资源种类，键是资源 id")
    private LinkedHashMap<String, Resource> resources = defaults();

    @Nullable
    public Resource resource(String id) {
        return resources.get(id);
    }

    @Data
    public static class Resource {
        @Comment("物品材质")
        @Raw
        private String material = "IRON_INGOT";

        @Comment("价格里显示的名字")
        private String name = "铁";

        @Comment("BASE 在每个队伍基地生成，PUBLIC 在地图的公共资源点生成")
        private Spawn at = Spawn.BASE;

        @Comment("PUBLIC 资源使用的地图资源点类型")
        @Nullable
        private MapData.DropType drop;

        @Comment("PUBLIC 资源点上方旋转展示的方块")
        @Nullable
        @Raw
        private String block;

        @Comment({"PUBLIC 资源点全息文字，从上到下", "变量: {level} 为罗马数字等级, {seconds} 为距下次生成的秒数"})
        private List<String> hologram = new ArrayList<>();

        @Comment("经验模式下拾取一个换算的等级")
        @Range(min = 0)
        private int xpValue = 1;

        @Comment({"各级的生成间隔和堆积上限", "第一项是开局等级，时间线升级资源点时进入下一项"})
        private List<Level> levels = new ArrayList<>();

        public Level level(int level) {
            return levels.get(Math.max(0, Math.min(level, levels.size()) - 1));
        }
    }

    @Data
    public static class Level {
        @Comment("生成间隔(秒)")
        @Range(min = 0.05)
        private double interval = 1;

        @Comment("资源点最多堆积数量")
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
        resources.put("iron", resource("IRON_INGOT", "铁", 1, new Level(1, 48)));
        resources.put("gold", resource("GOLD_INGOT", "金", 3, new Level(3, 8)));
        Resource diamond = resource("DIAMOND", "钻石", 40, new Level(35, 4), new Level(23, 6), new Level(15, 8));
        publicResource(diamond, MapData.DropType.DIAMOND, "DIAMOND_BLOCK", "&b钻石");
        resources.put("diamond", diamond);
        Resource emerald = resource("EMERALD", "绿宝石", 80, new Level(60, 2), new Level(23, 4), new Level(15, 4));
        publicResource(emerald, MapData.DropType.EMERALD, "EMERALD_BLOCK", "&2绿宝石");
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
        resource.setHologram(new ArrayList<>(Arrays.asList("&e等级 &c{level}", title, "&e将在&c{seconds}&e秒后刷新")));
    }

    /**
     * Version 1 kept every value in its own flat key and the level 2 and 3 intervals in timeline.yml
     *
     * @param timeline content of a version 1 timeline.yml
     */
    public static Map<String, Object> fromVersion1(Map<String, Object> generators, Map<String, Object> timeline) {
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
        GeneratorConfig defaults = new GeneratorConfig();
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
