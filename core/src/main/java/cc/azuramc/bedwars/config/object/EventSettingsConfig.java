package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.util.MessageUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author An5w1r@163.com
 */
@Data
public class EventSettingsConfig {

    private BedDestroyedEvent bedDestroyedEvent = new BedDestroyedEvent();
    private DiamondUpdateEvent diamondUpdateEvent = new DiamondUpdateEvent();
    private EmeraldUpdateEvent emeraldUpdateEvent = new EmeraldUpdateEvent();
    private GameOverEvent gameOverEvent = new GameOverEvent();
    private GameShutdownEvent gameShutdownEvent = new GameShutdownEvent();
    private GameStartEvent gameStartEvent = new GameStartEvent();

    @Data
    public static class BedDestroyedEvent {
        private int executeSeconds = 360;

        private String title = MessageUtil.color("&c&l床自毁");
        private String subtitle = MessageUtil.color("&e所有队伍床消失");
        private int fadeIn = 10;
        private int titleStay = 20;
        private int fadeOut = 10;

    }

    @Data
    public static class DiamondUpdateEvent {
        private int level1RefreshSecond = 30;
        private int level2RefreshSecond = 23;
        private int level3RefreshSecond = 15;
    }

    @Data
    public static class EmeraldUpdateEvent {
        private int level1RefreshSecond = 30;
        private int level2RefreshSecond = 23;
        private int level3RefreshSecond = 15;
    }

    @Data
    public static class GameOverEvent {
        private int executeSeconds = 360;

        // 游戏结束倒计时时间(秒)
        private int defaultCountdown = 15;

        // 标题显示配置
        private int titleFadeIn = 0;
        private int titleStay = 40;
        private int titleFadeOut = 0;

        // 服务器关闭延迟(ticks)
        private long shutdownDelay = 40L;

        // 烟花高度
        private double fireworkHeight = 2.0D;

        // 胜利/失败消息
        private String victoryTitle = MessageUtil.color("&6&l获胜！");
        private String victorySubtitle = MessageUtil.color("&7你获得了最终的胜利");
        private String defeatTitle = MessageUtil.color("&c&l失败！");
        private String defeatSubtitle = MessageUtil.color("&7你输掉了这场游戏");

        private String noWinner = MessageUtil.color("&e平局！没有队伍获胜");

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
        private int shutdownDelaySecond = 30;
    }

    @Data
    public static class GameStartEvent {
        private int countDown = 5;
        private String titleString = MessageUtil.color("&c&l游戏即将开始");
        private String subtitle = MessageUtil.color("&e&l");
        private int fadeIn = 1;
        private int titleStay = 20;
        private int fadeOut = 1;

        private String titleCountdown = MessageUtil.color("&c&l%d");
        private String subtitleText = MessageUtil.color("&e&l准备战斗吧！");

        private int defaultCountdown = 120;
        private int quickStartCountdown = 10;

        // 公告时间点
        private int[] announcementTimes = {60, 30, 5, 4, 3, 2, 1};

        // 消息模板
        private String msgCountdown = MessageUtil.color("&e游戏将在&c%d&e秒后开始！");
        private String msgNotEnoughPlayers = MessageUtil.color("&c人数不足，取消倒计时！");
        private String msgGameFull = MessageUtil.color("&e游戏人数已满,10秒后开始游戏！");
    }

}
