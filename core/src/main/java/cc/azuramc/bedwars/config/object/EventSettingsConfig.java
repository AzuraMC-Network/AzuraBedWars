package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.config.annotation.Comment;
import cc.azuramc.bedwars.util.MessageUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
@Data
@Comment("游戏事件配置文件")
public class EventSettingsConfig {

    @Comment("床自毁事件配置")
    private BedDestroyedEvent bedDestroyedEvent = new BedDestroyedEvent();

    @Comment("钻石升级事件配置")
    private DiamondUpdateEvent diamondUpdateEvent = new DiamondUpdateEvent();

    @Comment("绿宝石升级事件配置")
    private EmeraldUpdateEvent emeraldUpdateEvent = new EmeraldUpdateEvent();

    @Comment("游戏结束事件配置")
    private GameOverEvent gameOverEvent = new GameOverEvent();

    @Comment("游戏关闭事件配置")
    private GameShutdownEvent gameShutdownEvent = new GameShutdownEvent();

    @Comment("游戏开始事件配置")
    private GameStartEvent gameStartEvent = new GameStartEvent();

    @Data
    public static class BedDestroyedEvent {
        @Comment("床自毁触发时间(秒，从游戏开始计算)")
        private int executeSeconds = 360;

        @Comment("床自毁标题")
        private String title = MessageUtil.color("&c&l床自毁");

        @Comment("床自毁副标题")
        private String subtitle = MessageUtil.color("&e所有队伍床消失");

        @Comment("标题淡入时间(tick)")
        private int fadeIn = 10;

        @Comment("标题停留时间(tick)")
        private int titleStay = 20;

        @Comment("标题淡出时间(tick)")
        private int fadeOut = 10;

    }

    @Data
    public static class DiamondUpdateEvent {
        @Comment("一级钻石刷新间隔(秒)")
        private int level1RefreshSecond = 30;

        @Comment("二级钻石刷新间隔(秒)")
        private int level2RefreshSecond = 23;

        @Comment("三级钻石刷新间隔(秒)")
        private int level3RefreshSecond = 15;
    }

    @Data
    public static class EmeraldUpdateEvent {
        @Comment("一级绿宝石刷新间隔(秒)")
        private int level1RefreshSecond = 30;

        @Comment("二级绿宝石刷新间隔(秒)")
        private int level2RefreshSecond = 23;

        @Comment("三级绿宝石刷新间隔(秒)")
        private int level3RefreshSecond = 15;
    }

    @Data
    public static class GameOverEvent {
        @Comment("游戏强制结束时间(秒，从游戏开始计算)")
        private int executeSeconds = 360;

        @Comment("游戏结束倒计时时间(秒)")
        private int defaultCountdown = 15;

        @Comment("标题淡入时间(tick)")
        private int titleFadeIn = 0;

        @Comment("标题停留时间(tick)")
        private int titleStay = 40;

        @Comment("标题淡出时间(tick)")
        private int titleFadeOut = 0;

        @Comment("服务器关闭延迟(tick)")
        private long shutdownDelay = 40L;

        @Comment("烟花生成高度(方块)")
        private double fireworkHeight = 2.0D;

        @Comment("胜利标题")
        private String victoryTitle = MessageUtil.color("&6&l获胜！");

        @Comment("胜利副标题")
        private String victorySubtitle = MessageUtil.color("&7你获得了最终的胜利");

        @Comment("失败标题")
        private String defeatTitle = MessageUtil.color("&c&l失败！");

        @Comment("失败副标题")
        private String defeatSubtitle = MessageUtil.color("&7你输掉了这场游戏");

        @Comment("平局标题")
        private String tieTitle = MessageUtil.color("&e&lTies！");

        @Comment("平局副标题")
        private String tieSubtitle = MessageUtil.color("&7No team has won");

        @Comment("无胜者消息")
        private String noWinner = MessageUtil.color("&eTie! No team has won");

        @Comment("平局时是否发射烟花")
        private boolean tieFireworkEnabled = true;

        @Comment({"自定义排行榜消息", "可用变量: <winnerFormat>, <firstName>, <firstKills>, <secondName>, <secondKills>, <thirdName>, <thirdKills>"})
        private List<String> customLeaderboardMessages = new ArrayList<>(List.of(
                "§a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§f                                   §l起床战争",
                "",
                "§f          <winnerFormat>",
                "",
                "",
                "§e                          §l击杀数第一名 §7- <firstName> - <firstKills>",
                "§6                          §l击杀数第二名 §7- <secondName> - <secondKills>",
                "§c                          §l击杀数第三名 §7- <thirdName> - <thirdKills>",
                "",
                "§a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
        ));
    }

    @Data
    public static class GameShutdownEvent {
        @Comment("服务器关闭延迟时间(秒)")
        private int shutdownDelaySecond = 30;
    }

    @Data
    public static class GameStartEvent {
        @Comment("开始倒计时时间(秒)")
        private int countDown = 5;

        @Comment("倒计时标题")
        private String titleString = MessageUtil.color("&c&l游戏即将开始");

        @Comment("倒计时副标题")
        private String subtitle = MessageUtil.color("&e&l");

        @Comment("标题淡入时间(tick)")
        private int fadeIn = 1;

        @Comment("标题停留时间(tick)")
        private int titleStay = 20;

        @Comment("标题淡出时间(tick)")
        private int fadeOut = 1;

        @Comment({"倒计时数字标题", "变量: %d 为倒计时秒数"})
        private String titleCountdown = MessageUtil.color("&c&l%d");

        @Comment("倒计时副标题文本")
        private String subtitleText = MessageUtil.color("&e&l准备战斗吧！");

        @Comment("默认等待倒计时(秒)")
        private int defaultCountdown = 120;

        @Comment("快速开始倒计时(秒)")
        private int quickStartCountdown = 10;

        @Comment("公告时间点(秒)")
        private int[] announcementTimes = {60, 30, 5, 4, 3, 2, 1};

        @Comment({"倒计时消息", "变量: %d 为剩余秒数"})
        private String msgCountdown = MessageUtil.color("&e游戏将在&c%d&e秒后开始！");

        @Comment("人数不足消息")
        private String msgNotEnoughPlayers = MessageUtil.color("&c人数不足，取消倒计时！");

        @Comment("游戏人满消息")
        private String msgGameFull = MessageUtil.color("&e游戏人数已满,10秒后开始游戏！");
    }

}
