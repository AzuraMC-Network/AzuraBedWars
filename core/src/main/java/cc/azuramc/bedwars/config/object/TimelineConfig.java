package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.api.config.Comment;
import cc.azuramc.bedwars.api.config.Range;
import cc.azuramc.bedwars.api.config.Raw;
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
@Comment({"Game stages", "Stage times are read when a game starts, a running game keeps its own"})
public class TimelineConfig {

    @Comment("Game start")
    private GameStartEvent gameStartEvent = new GameStartEvent();

    @Comment({"Stages after the start, after is the seconds since the previous stage", "The scoreboard shows the name of the next stage"})
    private List<Stage> stages = new ArrayList<>(Arrays.asList(
            new Stage("Diamond II", 360, Action.generatorLevel("diamond", 2)),
            new Stage("Emerald II", 360, Action.generatorLevel("emerald", 2)),
            new Stage("Diamond III", 360, Action.generatorLevel("diamond", 3)),
            new Stage("Emerald III", 360, Action.generatorLevel("emerald", 3)),
            new Stage("Bed gone", 360, new Action(Action.DESTROY_BEDS),
                    Action.title("&c&lBED DESTRUCTION", "&eAll beds have been destroyed", 10, 20, 10), Action.sound("ENTITY_ENDER_DRAGON_GROWL")),
            new Stage("Game over", 360, new Action(Action.END_GAME))));

    @Comment("Game end")
    private GameOverEvent gameOverEvent = new GameOverEvent();

    @Data
    public static class GameStartEvent {
        @Comment("Start countdown in seconds")
        @Range(min = 0)
        private int countDown = 5;

        @Comment("Countdown title")
        private String titleString = "&c&lGame starting";

        @Comment("Countdown subtitle")
        private String subtitle = "&e&l";

        @Comment("Title fade in in ticks")
        @Range(min = 0)
        private int fadeIn = 1;

        @Comment("Title stay time in ticks")
        @Range(min = 0)
        private int titleStay = 20;

        @Comment("Title fade out in ticks")
        @Range(min = 0)
        private int fadeOut = 1;

        @Comment({"Countdown number title", "Placeholders: %d seconds left"})
        private String titleCountdown = "&c&l%d";

        @Comment("Countdown subtitle text")
        private String subtitleText = "&e&lGet ready to fight!";

        @Comment("Waiting countdown in seconds")
        @Range(min = 1)
        private int defaultCountdown = 120;

        @Comment("Countdown when the game is full in seconds")
        @Range(min = 1)
        private int quickStartCountdown = 10;

        @Comment("Seconds that are announced")
        private int[] announcementTimes = {60, 30, 5, 4, 3, 2, 1};

        @Comment({"Countdown message", "Placeholders: %d seconds left"})
        private String msgCountdown = "&eThe game starts in &c%d &eseconds!";

        @Comment("Not enough players message")
        private String msgNotEnoughPlayers = "&cNot enough players, countdown cancelled!";

        @Comment("Game full message")
        private String msgGameFull = "&eThe game is full, starting in 10 seconds!";
    }

    @Data
    public static class GameOverEvent {
        @Comment("Game end countdown in seconds")
        @Range(min = 0)
        private int defaultCountdown = 15;

        @Comment("Title fade in in ticks")
        @Range(min = 0)
        private int titleFadeIn = 0;

        @Comment("Title stay time in ticks")
        @Range(min = 0)
        private int titleStay = 40;

        @Comment("Title fade out in ticks")
        @Range(min = 0)
        private int titleFadeOut = 0;

        @Comment("Server shutdown delay in ticks")
        @Range(min = 0)
        private long shutdownDelay = 40L;

        @Comment("Firework height in blocks")
        private double fireworkHeight = 2.0D;

        @Comment("Victory title")
        private String victoryTitle = "&6&lVICTORY!";

        @Comment("Victory subtitle")
        private String victorySubtitle = "&7You won the game";

        @Comment("Defeat title")
        private String defeatTitle = "&c&lGAME OVER!";

        @Comment("Defeat subtitle")
        private String defeatSubtitle = "&7You lost this game";

        @Comment("Draw title")
        private String tieTitle = "&e&lDRAW!";

        @Comment("Draw subtitle")
        private String tieSubtitle = "&7No team has won";

        @Comment("No winner message")
        private String noWinner = "&eTie! No team has won";

        @Comment("Launch fireworks on a draw")
        private boolean tieFireworkEnabled = true;

        @Comment({"Leaderboard message", "Placeholders: <winnerFormat>, <firstName>, <firstKills>, <secondName>, <secondKills>, <thirdName>, <thirdKills>"})
        private List<String> customLeaderboardMessages = new ArrayList<>(Arrays.asList(
                "&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "&f                                   &lBed Wars",
                "",
                "&f          <winnerFormat>",
                "",
                "",
                "&e                          &l1st Killer &7- <firstName> - <firstKills>",
                "&6                          &l2nd Killer &7- <secondName> - <secondKills>",
                "&c                          &l3rd Killer &7- <thirdName> - <thirdKills>",
                "",
                "&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
        ));
    }

    @Data
    @NoArgsConstructor
    public static class Stage {
        private String name = "";

        @Comment("Seconds since the previous stage")
        @Range(min = 0)
        private int after = 360;

        @Comment("generator-level, destroy-beds, end-game, title, sound, message, command")
        private List<Action> actions = new ArrayList<>();

        public Stage(String name, int after, Action... actions) {
            this.name = name;
            this.after = after;
            this.actions = new ArrayList<>(Arrays.asList(actions));
        }
    }

    /**
     * One action type with the fields its type reads
     */
    @Data
    @NoArgsConstructor
    public static class Action {
        public static final String GENERATOR_LEVEL = "generator-level";
        public static final String DESTROY_BEDS = "destroy-beds";
        public static final String END_GAME = "end-game";
        public static final String TITLE = "title";
        public static final String SOUND = "sound";
        public static final String MESSAGE = "message";
        public static final String COMMAND = "command";
        public static final List<String> TYPES = Collections.unmodifiableList(Arrays.asList(
                GENERATOR_LEVEL, DESTROY_BEDS, END_GAME, TITLE, SOUND, MESSAGE, COMMAND));

        @Raw
        private String type = END_GAME;

        @Comment("Resource id of generator-level")
        @Nullable
        @Raw
        private String resource;

        @Comment("Level of generator-level, 1 is the first")
        @Nullable
        @Range(min = 1)
        private Integer level;

        @Nullable
        private String title;

        @Nullable
        private String subtitle;

        @Comment("Fade in, stay and fade out of title in ticks, 10 40 10 by default")
        @Nullable
        @Range(min = 0)
        private Integer fadeIn;

        @Nullable
        @Range(min = 0)
        private Integer stay;

        @Nullable
        @Range(min = 0)
        private Integer fadeOut;

        @Comment("Sound of sound, an XSeries name")
        @Nullable
        @Raw
        private String sound;

        @Nullable
        @Range(min = 0)
        private Float volume;

        @Nullable
        @Range(min = 0, max = 2)
        private Float pitch;

        @Comment("Text of message, sent to every player")
        @Nullable
        private List<String> message;

        @Comment("Command run by the console")
        @Nullable
        @Raw
        private List<String> commands;

        public Action(String type) {
            this.type = type;
        }

        public static Action fromString(String type) {
            return new Action(type.trim());
        }

        public static Action generatorLevel(String resource, int level) {
            Action action = new Action(GENERATOR_LEVEL);
            action.resource = resource;
            action.level = level;
            return action;
        }

        public static Action title(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
            Action action = new Action(TITLE);
            action.title = title;
            action.subtitle = subtitle;
            action.fadeIn = fadeIn;
            action.stay = stay;
            action.fadeOut = fadeOut;
            return action;
        }

        public static Action sound(String sound) {
            Action action = new Action(SOUND);
            action.sound = sound;
            return action;
        }
    }

    /**
     * Version 1 also held the diamond and emerald intervals, which moved to the levels of generators.yml
     */
    public static Map<String, Object> fromVersion1(Map<String, Object> old) {
        Map<String, Object> upgraded = new LinkedHashMap<>(old);
        upgraded.remove("diamond-update-event");
        upgraded.remove("emerald-update-event");
        return upgraded;
    }

    /**
     * Version 2 had the bed destruction and the forced end as fixed events
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> fromVersion2(Map<String, Object> old) {
        Map<String, Object> upgraded = new LinkedHashMap<>(old);
        Map<String, Object> bed = old.get("bed-destroyed-event") instanceof Map
                ? (Map<String, Object>) old.get("bed-destroyed-event") : new LinkedHashMap<String, Object>();
        Map<String, Object> end = old.get("game-over-event") instanceof Map
                ? new LinkedHashMap<>((Map<String, Object>) old.get("game-over-event")) : new LinkedHashMap<String, Object>();
        upgraded.remove("bed-destroyed-event");
        Object endSeconds = end.remove("execute-seconds");
        if (!end.isEmpty()) {
            upgraded.put("game-over-event", end);
        } else {
            upgraded.remove("game-over-event");
        }

        Map<String, Object> title = new LinkedHashMap<>();
        title.put("type", Action.TITLE);
        title.put("title", bed.getOrDefault("title", "&c&lBED DESTRUCTION"));
        title.put("subtitle", bed.getOrDefault("subtitle", "&eAll beds have been destroyed"));
        title.put("fade-in", bed.getOrDefault("fade-in", 10));
        title.put("stay", bed.getOrDefault("title-stay", 20));
        title.put("fade-out", bed.getOrDefault("fade-out", 10));
        Map<String, Object> sound = new LinkedHashMap<>();
        sound.put("type", Action.SOUND);
        sound.put("sound", "ENTITY_ENDER_DRAGON_GROWL");

        List<Object> stages = new ArrayList<>();
        stages.add(stage("Diamond II", 360, generatorLevel("diamond", 2)));
        stages.add(stage("Emerald II", 360, generatorLevel("emerald", 2)));
        stages.add(stage("Diamond III", 360, generatorLevel("diamond", 3)));
        stages.add(stage("Emerald III", 360, generatorLevel("emerald", 3)));
        stages.add(stage("Bed gone", bed.getOrDefault("execute-seconds", 360), Action.DESTROY_BEDS, title, sound));
        stages.add(stage("Game over", endSeconds != null ? endSeconds : 360, Action.END_GAME));
        upgraded.put("stages", stages);
        return upgraded;
    }

    private static Map<String, Object> stage(String name, Object after, Object... actions) {
        Map<String, Object> stage = new LinkedHashMap<>();
        stage.put("name", name);
        stage.put("after", after);
        stage.put("actions", new ArrayList<>(Arrays.asList(actions)));
        return stage;
    }

    private static Map<String, Object> generatorLevel(String resource, int level) {
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("type", Action.GENERATOR_LEVEL);
        action.put("resource", resource);
        action.put("level", level);
        return action;
    }
}
