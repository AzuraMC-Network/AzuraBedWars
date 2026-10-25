package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.config.annotation.Comment;
import cc.azuramc.bedwars.config.annotation.Range;
import cc.azuramc.bedwars.config.annotation.Raw;
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
@Comment({"对局阶段配置", "阶段时间在开局时读取, 进行中的对局不受重载影响"})
public class TimelineConfig {

    @Comment("游戏开始配置")
    private GameStartEvent gameStartEvent = new GameStartEvent();

    @Comment({"开始游戏之后的阶段，after 是距离上一个阶段的秒数", "计分板显示下一个阶段的 name"})
    private List<Stage> stages = new ArrayList<>(Arrays.asList(
            new Stage("钻石资源点升级到II级", 360, Action.generatorLevel("diamond", 2)),
            new Stage("绿宝石升级到II级", 360, Action.generatorLevel("emerald", 2)),
            new Stage("钻石资源点升级到III级", 360, Action.generatorLevel("diamond", 3)),
            new Stage("绿宝石升级到III级", 360, Action.generatorLevel("emerald", 3)),
            new Stage("床自毁", 360, new Action(Action.DESTROY_BEDS),
                    Action.title("&c&l床自毁", "&e所有队伍床消失", 10, 20, 10), Action.sound("ENTITY_ENDER_DRAGON_GROWL")),
            new Stage("游戏结束", 360, new Action(Action.END_GAME))));

    @Comment("游戏结束事件配置")
    private GameOverEvent gameOverEvent = new GameOverEvent();

    @Data
    public static class GameStartEvent {
        @Comment("开始倒计时时间(秒)")
        @Range(min = 0)
        private int countDown = 5;

        @Comment("倒计时标题")
        private String titleString = "&c&l游戏即将开始";

        @Comment("倒计时副标题")
        private String subtitle = "&e&l";

        @Comment("标题淡入时间(tick)")
        @Range(min = 0)
        private int fadeIn = 1;

        @Comment("标题停留时间(tick)")
        @Range(min = 0)
        private int titleStay = 20;

        @Comment("标题淡出时间(tick)")
        @Range(min = 0)
        private int fadeOut = 1;

        @Comment({"倒计时数字标题", "变量: %d 为倒计时秒数"})
        private String titleCountdown = "&c&l%d";

        @Comment("倒计时副标题文本")
        private String subtitleText = "&e&l准备战斗吧！";

        @Comment("默认等待倒计时(秒)")
        @Range(min = 1)
        private int defaultCountdown = 120;

        @Comment("快速开始倒计时(秒)")
        @Range(min = 1)
        private int quickStartCountdown = 10;

        @Comment("公告时间点(秒)")
        private int[] announcementTimes = {60, 30, 5, 4, 3, 2, 1};

        @Comment({"倒计时消息", "变量: %d 为剩余秒数"})
        private String msgCountdown = "&e游戏将在&c%d&e秒后开始！";

        @Comment("人数不足消息")
        private String msgNotEnoughPlayers = "&c人数不足，取消倒计时！";

        @Comment("游戏人满消息")
        private String msgGameFull = "&e游戏人数已满,10秒后开始游戏！";
    }

    @Data
    public static class GameOverEvent {
        @Comment("游戏结束倒计时时间(秒)")
        @Range(min = 0)
        private int defaultCountdown = 15;

        @Comment("标题淡入时间(tick)")
        @Range(min = 0)
        private int titleFadeIn = 0;

        @Comment("标题停留时间(tick)")
        @Range(min = 0)
        private int titleStay = 40;

        @Comment("标题淡出时间(tick)")
        @Range(min = 0)
        private int titleFadeOut = 0;

        @Comment("服务器关闭延迟(tick)")
        @Range(min = 0)
        private long shutdownDelay = 40L;

        @Comment("烟花生成高度(方块)")
        private double fireworkHeight = 2.0D;

        @Comment("胜利标题")
        private String victoryTitle = "&6&l获胜！";

        @Comment("胜利副标题")
        private String victorySubtitle = "&7你获得了最终的胜利";

        @Comment("失败标题")
        private String defeatTitle = "&c&l失败！";

        @Comment("失败副标题")
        private String defeatSubtitle = "&7你输掉了这场游戏";

        @Comment("平局标题")
        private String tieTitle = "&e&lTies！";

        @Comment("平局副标题")
        private String tieSubtitle = "&7No team has won";

        @Comment("无胜者消息")
        private String noWinner = "&eTie! No team has won";

        @Comment("平局时是否发射烟花")
        private boolean tieFireworkEnabled = true;

        @Comment({"自定义排行榜消息", "可用变量: <winnerFormat>, <firstName>, <firstKills>, <secondName>, <secondKills>, <thirdName>, <thirdKills>"})
        private List<String> customLeaderboardMessages = new ArrayList<>(Arrays.asList(
                "&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "&f                                   &l起床战争",
                "",
                "&f          <winnerFormat>",
                "",
                "",
                "&e                          &l击杀数第一名 &7- <firstName> - <firstKills>",
                "&6                          &l击杀数第二名 &7- <secondName> - <secondKills>",
                "&c                          &l击杀数第三名 &7- <thirdName> - <thirdKills>",
                "",
                "&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
        ));
    }

    @Data
    @NoArgsConstructor
    public static class Stage {
        private String name = "";

        @Comment("距离上一个阶段的秒数")
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

        @Comment("generator-level 的资源 id")
        @Nullable
        @Raw
        private String resource;

        @Comment("generator-level 的等级，1 是第一级")
        @Nullable
        @Range(min = 1)
        private Integer level;

        @Nullable
        private String title;

        @Nullable
        private String subtitle;

        @Comment("title 的淡入、停留、淡出(tick)，默认 10 40 10")
        @Nullable
        @Range(min = 0)
        private Integer fadeIn;

        @Nullable
        @Range(min = 0)
        private Integer stay;

        @Nullable
        @Range(min = 0)
        private Integer fadeOut;

        @Comment("sound 的声音，使用 XSeries 名称")
        @Nullable
        @Raw
        private String sound;

        @Nullable
        @Range(min = 0)
        private Float volume;

        @Nullable
        @Range(min = 0, max = 2)
        private Float pitch;

        @Comment("message 发给所有玩家的文字")
        @Nullable
        private List<String> message;

        @Comment("以控制台执行的命令")
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
        title.put("title", bed.getOrDefault("title", "&c&l床自毁"));
        title.put("subtitle", bed.getOrDefault("subtitle", "&e所有队伍床消失"));
        title.put("fade-in", bed.getOrDefault("fade-in", 10));
        title.put("stay", bed.getOrDefault("title-stay", 20));
        title.put("fade-out", bed.getOrDefault("fade-out", 10));
        Map<String, Object> sound = new LinkedHashMap<>();
        sound.put("type", Action.SOUND);
        sound.put("sound", "ENTITY_ENDER_DRAGON_GROWL");

        List<Object> stages = new ArrayList<>();
        stages.add(stage("钻石资源点升级到II级", 360, generatorLevel("diamond", 2)));
        stages.add(stage("绿宝石升级到II级", 360, generatorLevel("emerald", 2)));
        stages.add(stage("钻石资源点升级到III级", 360, generatorLevel("diamond", 3)));
        stages.add(stage("绿宝石升级到III级", 360, generatorLevel("emerald", 3)));
        stages.add(stage("床自毁", bed.getOrDefault("execute-seconds", 360), Action.DESTROY_BEDS, title, sound));
        stages.add(stage("游戏结束", endSeconds != null ? endSeconds : 360, Action.END_GAME));
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
