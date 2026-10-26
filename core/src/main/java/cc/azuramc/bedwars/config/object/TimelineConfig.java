package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.config.annotation.Comment;
import cc.azuramc.bedwars.config.annotation.Range;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
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

    @Comment("床自毁事件配置")
    private BedDestroyedEvent bedDestroyedEvent = new BedDestroyedEvent();

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
    public static class BedDestroyedEvent {
        @Comment("床自毁触发时间(秒，从上一阶段结束计算)")
        @Range(min = 1)
        private int executeSeconds = 360;

        @Comment("床自毁标题")
        private String title = "&c&l床自毁";

        @Comment("床自毁副标题")
        private String subtitle = "&e所有队伍床消失";

        @Comment("标题淡入时间(tick)")
        @Range(min = 0)
        private int fadeIn = 10;

        @Comment("标题停留时间(tick)")
        @Range(min = 0)
        private int titleStay = 20;

        @Comment("标题淡出时间(tick)")
        @Range(min = 0)
        private int fadeOut = 10;
    }

    @Data
    public static class GameOverEvent {
        @Comment("游戏强制结束时间(秒，从床自毁后计算)")
        @Range(min = 1)
        private int executeSeconds = 360;

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

    /**
     * Version 1 also held the diamond and emerald intervals, which moved to the levels of generators.yml
     */
    public static Map<String, Object> fromVersion1(Map<String, Object> old) {
        Map<String, Object> upgraded = new LinkedHashMap<>(old);
        upgraded.remove("diamond-update-event");
        upgraded.remove("emerald-update-event");
        return upgraded;
    }
}
