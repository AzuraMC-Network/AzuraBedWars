package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.util.ChatColorUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode
public class MessageConfig {

    private DestroyBedEvent destroyBedEvent = new DestroyBedEvent();
    private DiamondUpdateEvent diamondUpdateEvent = new DiamondUpdateEvent();
    private EmeraldUpdateEvent emeraldUpdateEvent = new EmeraldUpdateEvent();
    private ShutdownEvent shutdownEvent = new ShutdownEvent();
    private OverEvent overEvent = new OverEvent();
    private StartEvent startEvent = new StartEvent();

    private GeneratorConfig generator = new GeneratorConfig();
    private GameStartConfig gameStart = new GameStartConfig();
    private GameOverConfig gameOver = new GameOverConfig();

    private WarpPowder warpPowder = new WarpPowder();

    private GameManager gameManager = new GameManager();

    @Data
    public static class DestroyBedEvent {

        private Title title = new Title();
        private String eventName = ChatColorUtil.color("床自毁");

        @Data
        public static class Title {
            private String titleString = ChatColorUtil.color("&c&l床自毁");
            private String subtitle = ChatColorUtil.color("&e所有队伍床消失");
        }
    }

    @Data
    public static class DiamondUpdateEvent {
        private String eventName = ChatColorUtil.color("钻石刷新");
    }

    @Data
    public static class EmeraldUpdateEvent {
        private String eventName = ChatColorUtil.color("绿宝石刷新");
    }

    @Data
    public static class ShutdownEvent {
        private String eventName = ChatColorUtil.color("游戏关闭");
    }

    @Data
    public static class OverEvent {
        private String eventName = ChatColorUtil.color("游戏结束");
    }

    @Data
    public static class StartEvent {

        private Title title = new Title();
        private String eventName = ChatColorUtil.color("开始游戏");
        private String teamUpgradeTaskName = ChatColorUtil.color("团队升级");

        @Data
        public static class Title {
            private String titleString = ChatColorUtil.color("&c&l游戏即将开始");
            private String subtitle = ChatColorUtil.color("&e&l");
        }
    }

    @Data
    public static class GeneratorConfig {
        // 资源名称
        private String ironGeneratorName = ChatColorUtil.color("铁刷新");
        private String goldGeneratorName = ChatColorUtil.color("金刷新");
        private String diamondGeneratorName = ChatColorUtil.color("钻石刷新");
        private String diamondTimeDisplay = ChatColorUtil.color("钻石时间显示");
        private String emeraldGeneratorName = ChatColorUtil.color("绿宝石刷新");
        private String emeraldTimeDisplay = ChatColorUtil.color("绿宝石时间显示");

        // 显示文本
        private String timeRemainingFormat = ChatColorUtil.color("&e将在&c%d&e秒后刷新");
        private String diamondName = ChatColorUtil.color("&b钻石");
        private String emeraldName = ChatColorUtil.color("&2绿宝石");
        private String levelI = ChatColorUtil.color("&e等级 &cI");
        private String levelII = ChatColorUtil.color("&e等级 &cII");
        private String levelIII = ChatColorUtil.color("&e等级 &cIII");
    }

    @Data
    public static class GameStartConfig {
        // 倒计时常量
        private Title title = new Title();

        // 标题显示配置
        @Data
        public static class Title {
            private String titleCountdown = ChatColorUtil.color("&c&l%d");
            private String subtitleText = ChatColorUtil.color("&e&l准备战斗吧！");
        }

        // 消息模板
        private String msgCountdown = ChatColorUtil.color("&e游戏将在&c%d&e秒后开始！");
        private String msgNotEnoughPlayers = ChatColorUtil.color("&c人数不足，取消倒计时！");
        private String msgGameFull = ChatColorUtil.color("&e游戏人数已满,10秒后开始游戏！");
    }

    @Data
    public static class GameOverConfig {
        // 胜利/失败消息
        private String victoryTitle = ChatColorUtil.color("&6&l获胜！");
        private String victorySubtitle = ChatColorUtil.color("&7你获得了最终的胜利");
        private String defeatTitle = ChatColorUtil.color("&c&l失败！");
        private String defeatSubtitle = ChatColorUtil.color("&7你输掉了这场游戏");

        // 排行榜标题及分隔线
        private String[] lead = ChatColorUtil.color(new String[]{"&e&l击杀数第一名", "&6&l击杀数第二名", "&c&l击杀数第三名"});
        private String separatorLine = ChatColorUtil.color("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        private String gameTitle = ChatColorUtil.color("&f                                              &l起床战争");
        private String winnersPrefix = ChatColorUtil.color("                                    &e胜利者 &7- ");
        private String noWinner = ChatColorUtil.color("&7无");
        private String rankPrefix = ChatColorUtil.color("                          ");
    }

    @Data
    public static class WarpPowder {
        private String teleportStartMessage = ChatColorUtil.color("&a在 &c%d&a 秒后你将被传送，请不要移动!");
        private String teleportCancelMessage = ChatColorUtil.color("&c你的传送被取消!");
    }

    @Data
    public static class GameManager {
        private String msgPlayerReconnect = ChatColorUtil.color("&7%s&a重连上线");
        private String msgPlayerLeave = ChatColorUtil.color("&7%s&e离开游戏");
    }
}
