package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.util.MessageUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author ant1aura@qq.com
 */
@Data
@EqualsAndHashCode
public class MessageConfig {

    private DestroyBed destroyBed = new DestroyBed();
    private DiamondUpdate diamondUpdate = new DiamondUpdate();
    private EmeraldUpdate emeraldUpdate = new EmeraldUpdate();
    private Shutdown shutdown = new Shutdown();
    private Over over = new Over();
    private Start start = new Start();

    private Generator generator = new Generator();
    private GameStart gameStart = new GameStart();
    private GameOver gameOver = new GameOver();

    private WarpPowder warpPowder = new WarpPowder();

    private Game game = new Game();

    private PlayerDeath playerDeath = new PlayerDeath();
    private PlayerRespawn playerRespawn = new PlayerRespawn();
    private Spectator spectator = new Spectator();

    @Data
    public static class DestroyBed {

        private Title title = new Title();
        private String eventName = MessageUtil.color("床自毁");

        @Data
        public static class Title {
            private String titleString = MessageUtil.color("&c&l床自毁");
            private String subtitle = MessageUtil.color("&e所有队伍床消失");
        }
    }

    @Data
    public static class DiamondUpdate {
        private String eventName = MessageUtil.color("钻石刷新");
    }

    @Data
    public static class EmeraldUpdate {
        private String eventName = MessageUtil.color("绿宝石刷新");
    }

    @Data
    public static class Shutdown {
        private String eventName = MessageUtil.color("游戏关闭");
    }

    @Data
    public static class Over {
        private String eventName = MessageUtil.color("游戏结束");
    }

    @Data
    public static class Start {

        private Title title = new Title();
        private String eventName = MessageUtil.color("开始游戏");
        private String teamUpgradeTaskName = MessageUtil.color("团队升级");

        @Data
        public static class Title {
            private String titleString = MessageUtil.color("&c&l游戏即将开始");
            private String subtitle = MessageUtil.color("&e&l");
        }
    }

    @Data
    public static class Generator {
        // 资源名称
        private String ironGeneratorName = MessageUtil.color("铁刷新");
        private String goldGeneratorName = MessageUtil.color("金刷新");
        private String diamondGeneratorName = MessageUtil.color("钻石刷新");
        private String diamondTimeDisplay = MessageUtil.color("钻石时间显示");
        private String emeraldGeneratorName = MessageUtil.color("绿宝石刷新");
        private String emeraldTimeDisplay = MessageUtil.color("绿宝石时间显示");

        // 显示文本
        private String timeRemainingFormat = MessageUtil.color("&e将在&c%d&e秒后刷新");
        private String diamondName = MessageUtil.color("&b钻石");
        private String emeraldName = MessageUtil.color("&2绿宝石");
        private String levelI = MessageUtil.color("&e等级 &cI");
        private String levelII = MessageUtil.color("&e等级 &cII");
        private String levelIII = MessageUtil.color("&e等级 &cIII");
    }

    @Data
    public static class GameStart {
        // 倒计时常量
        private Title title = new Title();

        // 标题显示配置
        @Data
        public static class Title {
            private String titleCountdown = MessageUtil.color("&c&l%d");
            private String subtitleText = MessageUtil.color("&e&l准备战斗吧！");
        }

        // 消息模板
        private String msgCountdown = MessageUtil.color("&e游戏将在&c%d&e秒后开始！");
        private String msgNotEnoughPlayers = MessageUtil.color("&c人数不足，取消倒计时！");
        private String msgGameFull = MessageUtil.color("&e游戏人数已满,10秒后开始游戏！");
    }

    @Data
    public static class GameOver {
        // 胜利/失败消息
        private String victoryTitle = MessageUtil.color("&6&l获胜！");
        private String victorySubtitle = MessageUtil.color("&7你获得了最终的胜利");
        private String defeatTitle = MessageUtil.color("&c&l失败！");
        private String defeatSubtitle = MessageUtil.color("&7你输掉了这场游戏");

        // 排行榜标题及分隔线
        private String[] lead = MessageUtil.color(new String[]{"&e&l击杀数第一名", "&6&l击杀数第二名", "&c&l击杀数第三名"});
        private String separatorLine = MessageUtil.color("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        private String gameTitle = MessageUtil.color("&f                                              &l起床战争");
        private String winnersPrefix = MessageUtil.color("                                    &e胜利者 &7- ");
        private String noWinner = MessageUtil.color("&7无");
        private String rankPrefix = MessageUtil.color("                          ");
    }

    @Data
    public static class WarpPowder {
        private String teleportStartMessage = MessageUtil.color("&a在 &c%d&a 秒后你将被传送，请不要移动!");
        private String teleportCancelMessage = MessageUtil.color("&c你的传送被取消!");
    }

    @Data
    public static class Game {
        private String msgPlayerReconnect = MessageUtil.color("&7%s&a重连上线");
        private String msgPlayerLeave = MessageUtil.color("&7%s&e离开游戏");
    }

    @Data
    public static class PlayerDeath {
        private String coinsActionBar = MessageUtil.color("&6+1个金币");
        private String coinsMessage = MessageUtil.color("&6+1个金币 (最终击杀)");
    }

    @Data
    public static class PlayerRespawn {
        private String respawnCountdownTitle = MessageUtil.color("&e&l%d");
        private String respawnCountdownSubTitle = MessageUtil.color("&7你死了 将在稍后重生");
        private String respawnCompleteTitle = MessageUtil.color("&a已复活！");
        private String respawnCompleteSubTitle = MessageUtil.color("&7因为你的床还在 所以你复活了");
        private String deathPermanentTitle = MessageUtil.color("&c你凉了！");
        private String deathPermanentSubTitle = MessageUtil.color("&7你没床了");
        private String teamEliminatedFormat = MessageUtil.color("&7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
        private String teamEliminatedMessage = MessageUtil.color("%s &c凉了! &e挖床者: %s");
        private String playAgainMessage = MessageUtil.color("&c你凉了!想再来一局吗? ");
        private String playAgainButton = MessageUtil.color("&b&l点击这里!");
        private String playAgainCommand = MessageUtil.color("/not impl now");
    }

    @Data
    public static class Spectator {

        private CompassGUI compassGUI = new CompassGUI();
        private SettingGUI settingGUI = new SettingGUI();
        private Target target = new Target();

        @Data
        public static class CompassGUI {
            private String guiTitle = MessageUtil.color("&8选择一个玩家来传送");
            private String healthFormat = MessageUtil.color("&f血量: &8%d");
            private String foodFormat = MessageUtil.color("&f饥饿: &8%d");
            private String levelFormat = MessageUtil.color("&f等级: &8%d");
            private String distanceFormat = MessageUtil.color("&f距离: &8%d");
        }

        @Data
        public static class SettingGUI {
            private String guiTitle = MessageUtil.color("&8旁观者设置");
            private String speedRemoved = MessageUtil.color("&c你不再有任何速度效果！");
            private String speedAdded = MessageUtil.color("&a你获得了 速度 %s 效果！");
            private String autoTPEnabled = MessageUtil.color("&a你开启了自动传送功能！");
            private String autoTPDisabled = MessageUtil.color("&c你不再被自动传送到目标位置！");
            private String nightVersionEnabled = MessageUtil.color("&a你现在拥有了夜视！");
            private String nightVersionDisabled = MessageUtil.color("&c你不再有夜视效果了！");
            private String firstPersonEnabled = MessageUtil.color("&a当你用你的指南针现在一个玩家后，你会被自动传送到他那里！");
            private String firstPersonDisabled = MessageUtil.color("&c你将默认使用第三人称模式！");
            private String hideOthersEnabled = MessageUtil.color("&c你不会再看到其他的旁观者！");
            private String hideOthersDisabled = MessageUtil.color("&a你现在可以看见其他旁观者了！");
            private String flyEnabled = MessageUtil.color("&a你现在不能停止飞行！");
            private String flyDisabled = MessageUtil.color("&a你现在能停止飞行！");
        }

        @Data
        public static class Target {
            private String targetLostMessage = MessageUtil.color("&c&l目标已丢失或不在同一个世界");
            private String firstPersonTitle = MessageUtil.color("&a正在旁观&7%s");
            private String firstPersonSubTitle = MessageUtil.color("&a点击左键打开菜单  &c按Shift键退出");
            private String firstPersonActionBar = MessageUtil.color("&f目标: &a&l%s  &f生命值: &a&l%d &c&l❤");
            private String thirdPersonActionBar = MessageUtil.color("&f目标: &a&l%s  &f生命值: &a&l%d  &f距离: &a&l%s米");
            private String menuHint = MessageUtil.color("  &a点击左键打开菜单  &c按Shift退出");
        }
    }
}
