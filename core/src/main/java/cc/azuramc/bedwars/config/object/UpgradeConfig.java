package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.api.config.Comment;
import cc.azuramc.bedwars.api.config.Range;
import cc.azuramc.bedwars.api.config.Raw;
import cc.azuramc.bedwars.config.ExtensionSettings;
import cc.azuramc.bedwars.config.object.shop.ItemSpec;
import cc.azuramc.bedwars.config.object.shop.Price;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.nodes.Node;

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
@Comment({"Team upgrades and traps", "Read when a game starts, a running game keeps its own"})
public class UpgradeConfig {

    @Comment("Menu title")
    private String title = "&8Upgrades & Traps";

    @Comment("Range of the base target in blocks, from the team spawn")
    @Range(min = 0)
    private double baseRange = 18.0;

    @Comment("How close to the spawn an enemy triggers a trap in blocks")
    @Range(min = 0)
    private double trapTriggerRange = 20.0;

    @Comment({"6 rows of 9 characters, each one a key of upgrades, traps.types or decor", "Q is the trap queue"})
    private List<String> layout = new ArrayList<>();

    @Comment("Decoration slots")
    private LinkedHashMap<String, ItemSpec> decor = new LinkedHashMap<>();

    @Comment("Team upgrades, keyed by their layout character")
    private LinkedHashMap<String, Upgrade> upgrades = new LinkedHashMap<>();

    private Traps traps = new Traps();

    @Comment("Menu text")
    private Text text = new Text();

    @Data
    public static class Upgrade {
        @Comment("Id, the key when left out")
        @Nullable
        @Raw
        private String id;

        private String name = "";

        private ItemSpec icon = new ItemSpec("STONE");

        private List<String> description = new ArrayList<>();

        @Comment("Levels, the first entry is level 1")
        private List<Level> levels = new ArrayList<>();
    }

    @Data
    public static class Level {
        private String name = "";

        private Price price = new Price(1, "diamond");

        @Comment("Price in levels for experience mode, converted from the xp-value of the resource when left out")
        @Nullable
        @Range(min = 0)
        private Integer xp;

        @Comment("Effects of this level, a higher level of the same effect replaces a lower one")
        private List<Effect> effects = new ArrayList<>();
    }

    @Data
    public static class Traps {
        @Comment("Most traps in the queue")
        @Range(min = 1, max = 9)
        private int queueSize = 3;

        @Comment("Price of the 1st, 2nd and 3rd trap in the queue")
        private List<Price> prices = new ArrayList<>(Arrays.asList(
                new Price(1, "diamond"), new Price(2, "diamond"), new Price(3, "diamond")));

        @Comment("Price in levels for experience mode, by queue position")
        private List<Integer> xp = new ArrayList<>(Arrays.asList(100, 200, 300));

        @Comment("Effects on your own team when any trap triggers")
        private List<Effect> alert = new ArrayList<>();

        @Comment("Trap types, keyed by their layout character")
        private LinkedHashMap<String, Trap> types = new LinkedHashMap<>();
    }

    @Data
    public static class Trap {
        @Comment("Id, the key when left out")
        @Nullable
        @Raw
        private String id;

        private String name = "";

        private ItemSpec icon = new ItemSpec("TRIPWIRE_HOOK");

        private List<String> description = new ArrayList<>();

        @Comment("Skipped when the intruder is protected from traps")
        private List<Effect> effects = new ArrayList<>();

        @Comment("Effects on your own team when triggered, traps.alert when left out")
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

        @Comment({"enchant, potion, generator, reveal, title, sound, message, command", "or an effect of another plugin as plugin:id"})
        @Raw
        private String type = MESSAGE;

        @Comment({"enchant: sword, armor, boots", "potion title sound message: team, base, intruder"})
        @Nullable
        @Raw
        private String target;

        @Comment("Enchantment of enchant")
        @Nullable
        @Raw
        private String enchant;

        @Comment("Effect of potion")
        @Nullable
        @Raw
        private String effect;

        @Comment("Enchantment or potion level, 1 is I")
        @Range(min = 1, max = 256)
        private int level = 1;

        @Comment({"Seconds of potion", "Potions of team upgrades are given again every second"})
        @Range(min = 0)
        private double seconds = 3;

        @Comment("Resource id of generator, a team without this generator gets a new one")
        @Nullable
        @Raw
        private String resource;

        @Comment("Spawn interval of generator in seconds, unchanged when left out")
        @Nullable
        @Range(min = 0.05)
        private Double interval;

        @Comment("Max stack of generator, unchanged when left out")
        @Nullable
        @Range(min = 1)
        private Integer maxStack;

        @Nullable
        private String title;

        @Nullable
        private String subtitle;

        @Comment("Fade in, stay and fade out of title in ticks")
        @Range(min = 0)
        private int fadeIn = 10;

        @Range(min = 0)
        private int stay = 40;

        @Range(min = 0)
        private int fadeOut = 10;

        @Comment("Sound of sound, an XSeries name")
        @Nullable
        @Raw
        private String sound;

        @Range(min = 0)
        private float volume = 1;

        @Range(min = 0, max = 2)
        private float pitch = 1;

        @Comment("Text of message")
        private List<String> message = new ArrayList<>();

        @Comment({"Command run by the console", "Placeholders: {team} {intruder} {intruder-team}"})
        @Raw
        private List<String> commands = new ArrayList<>();

        @Comment("Settings of an effect of another plugin")
        @Nullable
        @ExtensionSettings({"type=upgrade-effect", "type=trap-effect"})
        private Node settings;

        public Effect(String type) {
            this.type = type;
        }

        public static Effect fromString(String type) {
            return new Effect(type.trim());
        }
    }

    @Data
    public static class Text {
        private String tierOwned = "&aTier {level}: {name}, &b{price}";
        private String tierNext = "&eTier {level}: {name}, &b{price}";
        private String tierLocked = "&7Tier {level}: {name}, &b{price}";

        @Comment("Upgrades with one level")
        private String buy = "&eClick to purchase!";
        private String bought = "&aPurchased";

        @Comment("Placeholders: {level} roman level")
        private String upgrade = "&eClick to upgrade to tier {level}";
        private String maxed = "&aMaxed out";

        private String trapPrice = "&7Cost: &b{price}";
        private String trapActive = "&aActive";
        private String trapFull = "&cTrap queue full";

        @Comment("Placeholders: {index} {name}")
        private String queueName = "&aTrap #{index}: {name}";
        private String queueEmptyName = "&cTrap #{index}: No Trap!";
        private ItemSpec queueEmpty = new ItemSpec("GRAY_STAINED_GLASS");

        @Comment("Placeholders: {ordinal} {price}")
        private List<String> queueLore = new ArrayList<>(Arrays.asList(
                "&7The {ordinal} enemy to walk into",
                "&7your base will trigger this trap!",
                "",
                "&7Purchasing traps will queue them",
                "&7here, the cost scales with the queue.",
                "",
                "&7Next trap: &b{price}"));

        @Comment("Ordinals of the queue positions")
        private List<String> ordinals = new ArrayList<>(Arrays.asList("first", "second", "third"));
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
