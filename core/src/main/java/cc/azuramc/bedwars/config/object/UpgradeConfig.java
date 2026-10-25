package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.config.annotation.Comment;
import cc.azuramc.bedwars.config.annotation.Range;
import cc.azuramc.bedwars.config.annotation.Raw;
import cc.azuramc.bedwars.config.object.shop.ItemSpec;
import cc.azuramc.bedwars.config.object.shop.Price;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author an5w1r@163.com
 */
@Data
@Comment({"团队升级与陷阱", "开局时读取, 进行中的对局不受重载影响"})
public class UpgradeConfig {

    @Comment("界面标题")
    private String title = "&8团队升级";

    @Comment("base 目标的范围(方块)，从队伍出生点算起")
    @Range(min = 0)
    private double baseRange = 18.0;

    @Comment("敌人离出生点多近时触发陷阱(方块)")
    @Range(min = 0)
    private double trapTriggerRange = 20.0;

    @Comment({"6 行 9 列的字符画，字符对应 upgrades、traps.types、decor 的键", "Q 是陷阱队列"})
    private List<String> layout = new ArrayList<>();

    @Comment("装饰格子")
    private LinkedHashMap<String, ItemSpec> decor = new LinkedHashMap<>();

    @Comment("团队升级，键是 layout 里的字符")
    private LinkedHashMap<String, Upgrade> upgrades = new LinkedHashMap<>();

    private Traps traps = new Traps();

    @Comment("界面文字")
    private Text text = new Text();

    @Data
    public static class Upgrade {
        @Comment("id，不写时用键")
        @Nullable
        @Raw
        private String id;

        private String name = "";

        private ItemSpec icon = new ItemSpec("STONE");

        private List<String> description = new ArrayList<>();

        @Comment("各级，第一项是 1 级")
        private List<Level> levels = new ArrayList<>();
    }

    @Data
    public static class Level {
        private String name = "";

        private Price price = new Price(1, "diamond");

        @Comment("经验模式的价格(级)，不写时按资源的 xp-value 换算")
        @Nullable
        @Range(min = 0)
        private Integer xp;

        @Comment("买下这一级时生效的效果，同类效果高等级覆盖低等级")
        private List<Effect> effects = new ArrayList<>();
    }

    @Data
    public static class Traps {
        @Comment("队列最多几个陷阱")
        @Range(min = 1, max = 9)
        private int queueSize = 3;

        @Comment("队列里第 1、2、3 个陷阱的价格")
        private List<Price> prices = new ArrayList<>(Arrays.asList(
                new Price(1, "diamond"), new Price(2, "diamond"), new Price(3, "diamond")));

        @Comment("经验模式的价格(级)，按队列位置")
        private List<Integer> xp = new ArrayList<>(Arrays.asList(100, 200, 300));

        @Comment("任意陷阱触发时对己方队伍生效")
        private List<Effect> alert = new ArrayList<>();

        @Comment("陷阱种类，键是 layout 里的字符")
        private LinkedHashMap<String, Trap> types = new LinkedHashMap<>();
    }

    @Data
    public static class Trap {
        @Comment("id，不写时用键")
        @Nullable
        @Raw
        private String id;

        private String name = "";

        private ItemSpec icon = new ItemSpec("TRIPWIRE_HOOK");

        private List<String> description = new ArrayList<>();

        @Comment("入侵者有陷阱保护时不生效")
        private List<Effect> effects = new ArrayList<>();

        @Comment("触发时对己方队伍生效，不写时用 traps.alert")
        @Nullable
        private List<Effect> alert;
    }

    /**
     * One effect type with the fields its type reads
     */
    @Data
    @NoArgsConstructor
    public static class Effect {
        public static final String ENCHANT = "enchant";
        public static final String POTION = "potion";
        public static final String GENERATOR = "generator";
        public static final String REVEAL = "reveal";
        public static final String TITLE = "title";
        public static final String SOUND = "sound";
        public static final String MESSAGE = "message";
        public static final String COMMAND = "command";
        public static final List<String> TYPES = Collections.unmodifiableList(Arrays.asList(
                ENCHANT, POTION, GENERATOR, REVEAL, TITLE, SOUND, MESSAGE, COMMAND));

        public static final String SWORD = "sword";
        public static final String ARMOR = "armor";
        public static final String BOOTS = "boots";
        public static final List<String> ENCHANT_TARGETS = Collections.unmodifiableList(Arrays.asList(SWORD, ARMOR, BOOTS));

        public static final String TEAM = "team";
        public static final String BASE = "base";
        public static final String INTRUDER = "intruder";
        public static final List<String> PLAYER_TARGETS = Collections.unmodifiableList(Arrays.asList(TEAM, BASE, INTRUDER));

        @Comment("enchant, potion, generator, reveal, title, sound, message, command")
        @Raw
        private String type = MESSAGE;

        @Comment({"enchant: sword, armor, boots", "potion title sound message: team, base, intruder"})
        @Nullable
        @Raw
        private String target;

        @Comment("enchant 的附魔")
        @Nullable
        @Raw
        private String enchant;

        @Comment("potion 的药水效果")
        @Nullable
        @Raw
        private String effect;

        @Comment("附魔或药水等级，1 是 I 级")
        @Range(min = 1, max = 256)
        private int level = 1;

        @Comment({"potion 的持续秒数", "团队升级的药水每秒重新给一次"})
        @Range(min = 0)
        private double seconds = 3;

        @Comment("generator 的资源 id，队伍没有这个资源点时新建一个")
        @Nullable
        @Raw
        private String resource;

        @Comment("generator 的刷新间隔(秒)，不写时不变")
        @Nullable
        @Range(min = 0.05)
        private Double interval;

        @Comment("generator 的最大堆叠，不写时不变")
        @Nullable
        @Range(min = 1)
        private Integer maxStack;

        @Nullable
        private String title;

        @Nullable
        private String subtitle;

        @Comment("title 的淡入、停留、淡出(tick)")
        @Range(min = 0)
        private int fadeIn = 10;

        @Range(min = 0)
        private int stay = 40;

        @Range(min = 0)
        private int fadeOut = 10;

        @Comment("sound 的声音，使用 XSeries 名称")
        @Nullable
        @Raw
        private String sound;

        @Range(min = 0)
        private float volume = 1;

        @Range(min = 0, max = 2)
        private float pitch = 1;

        @Comment("message 的文字")
        private List<String> message = new ArrayList<>();

        @Comment({"以控制台执行的命令", "变量: {team} {intruder} {intruder-team}"})
        @Raw
        private List<String> commands = new ArrayList<>();

        public Effect(String type) {
            this.type = type;
        }

        public static Effect fromString(String type) {
            return new Effect(type.trim());
        }
    }

    @Data
    public static class Text {
        private String tierOwned = "&a等级 {level}：{name}，&b{price}";
        private String tierNext = "&e等级 {level}：{name}，&b{price}";
        private String tierLocked = "&7等级 {level}：{name}，&b{price}";

        @Comment("只有一级的升级")
        private String buy = "&e点击购买";
        private String bought = "&a已购买";

        @Comment("变量: {level} 罗马数字")
        private String upgrade = "&e点击升级到等级 {level}";
        private String maxed = "&a已达到最高等级";

        private String trapPrice = "&7价格: &b{price}";
        private String trapActive = "&a已激活";
        private String trapFull = "&c陷阱已满";

        @Comment("变量: {index} {name}")
        private String queueName = "&a陷阱#{index}: {name}";
        private String queueEmptyName = "&c陷阱#{index}: 没有陷阱！";
        private ItemSpec queueEmpty = new ItemSpec("GRAY_STAINED_GLASS");

        @Comment("变量: {ordinal} {price}")
        private List<String> queueLore = new ArrayList<>(Arrays.asList(
                "&7第{ordinal}个敌人进入己方基地后",
                "&7会触发该陷阱！",
                "",
                "&7购买的陷阱会再次排列，具体",
                "&7费用取决已排列的陷阱数量。",
                "",
                "&7下一个陷阱: &b{price}"));

        @Comment("队列位置的序数词")
        private List<String> ordinals = new ArrayList<>(Arrays.asList("一", "二", "三"));
    }

    /**
     * Version 1 held the effect tuning only, the upgrades themselves came from code
     *
     * @param defaults the bundled version 2 file
     */
    public static Map<String, Object> fromVersion1(Map<String, Object> old, Map<String, Object> defaults) {
        Map<String, Object> upgraded = new LinkedHashMap<>(defaults);
        upgraded.remove("config-version");
        copy(old, "healing-pool-range", upgraded, "base-range");
        copy(old, "trap-trigger-range", upgraded, "trap-trigger-range");
        for (Map<String, Object> effect : effects(upgraded)) {
            Object type = effect.get("effect");
            if ("HASTE".equals(type)) {
                ticks(old, "haste-effect-duration", effect);
            } else if ("REGENERATION".equals(type)) {
                ticks(old, "regeneration-effect-duration", effect);
                level(old, "regeneration-effect-amplifier", effect);
            } else if ("BLINDNESS".equals(type) || "SLOWNESS".equals(type)) {
                ticks(old, "trap-effect-duration", effect);
                level(old, "trap-effect-amplifier", effect);
            } else if ("MINING_FATIGUE".equals(type)) {
                ticks(old, "mining-fatigue-effect-duration", effect);
                level(old, "mining-fatigue-effect-amplifier", effect);
            }
        }
        return upgraded;
    }

    private static List<Map<String, Object>> effects(Map<String, Object> root) {
        List<Map<String, Object>> effects = new ArrayList<>();
        for (Object upgrade : section(root.get("upgrades")).values()) {
            for (Object level : list(section(upgrade).get("levels"))) {
                addMaps(effects, section(level).get("effects"));
            }
        }
        for (Object trap : section(section(root.get("traps")).get("types")).values()) {
            addMaps(effects, section(trap).get("effects"));
        }
        return effects;
    }

    @SuppressWarnings("unchecked")
    private static void addMaps(List<Map<String, Object>> out, Object list) {
        for (Object item : list(list)) {
            if (item instanceof Map) {
                out.add((Map<String, Object>) item);
            }
        }
    }

    private static void copy(Map<String, Object> from, String fromKey, Map<String, Object> to, String toKey) {
        if (from.get(fromKey) instanceof Number) {
            put(to, toKey, ((Number) from.get(fromKey)).doubleValue());
        }
    }

    private static void ticks(Map<String, Object> old, String key, Map<String, Object> effect) {
        if (old.get(key) instanceof Number) {
            put(effect, "seconds", ((Number) old.get(key)).doubleValue() / 20);
        }
    }

    private static void level(Map<String, Object> old, String key, Map<String, Object> effect) {
        if (old.get(key) instanceof Number) {
            put(effect, "level", ((Number) old.get(key)).intValue() + 1);
        }
    }

    /**
     * Leaves values equal to the bundled ones untouched, so an unchanged old file migrates to the commented template
     */
    private static void put(Map<String, Object> map, String key, Number value) {
        Object current = map.containsKey(key) ? map.get(key) : "level".equals(key) ? 1 : null;
        if (!(current instanceof Number) || ((Number) current).doubleValue() != value.doubleValue()) {
            map.put(key, value);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> section(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : new LinkedHashMap<String, Object>();
    }

    private static List<?> list(Object value) {
        return value instanceof List ? (List<?>) value : new ArrayList<>();
    }
}
