package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.util.MessageUtil;
import lombok.Data;

/**
 * @author An5w1r@163.com
 */
@Data
public class MessageConfig {
    private String normalKillRewardsMessage = MessageUtil.color("&6+1个金币");
    private String finalKillRewardsMessage = MessageUtil.color("&6+1个金币 (最终击杀)");

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

    private String playerReconnectMessage = MessageUtil.color("&7%s&a重连上线");
    private String playerLeaveMessage = MessageUtil.color("&7%s&e离开游戏");

    private String warpPowderStartMessage = MessageUtil.color("&a在 &c%d&a 秒后你将被传送，请不要移动!");
    private String warpPowderCancelMessage = MessageUtil.color("&c你的传送被取消!");

    private String ironGeneratorName = MessageUtil.color("铁刷新");
    private String goldGeneratorName = MessageUtil.color("金刷新");
    private String diamondGeneratorName = MessageUtil.color("钻石刷新");
    private String diamondTimeDisplay = MessageUtil.color("钻石时间显示");
    private String emeraldGeneratorName = MessageUtil.color("绿宝石刷新");
    private String emeraldTimeDisplay = MessageUtil.color("绿宝石时间显示");

    private Spectator spectator = new Spectator();

    @Data
    public static class Spectator {

        private String spectatorCompassGuiTitle = MessageUtil.color("&8选择一个玩家来传送");
        private String healthFormat = MessageUtil.color("&f血量: &8%d");
        private String foodFormat = MessageUtil.color("&f饥饿: &8%d");
        private String levelFormat = MessageUtil.color("&f等级: &8%d");
        private String distanceFormat = MessageUtil.color("&f距离: &8%d");


        private String spectatorSettingsGuiTitle = MessageUtil.color("&8旁观者设置");
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

        private String targetLostMessage = MessageUtil.color("&c&l目标已丢失或不在同一个世界");
        private String firstPersonTitle = MessageUtil.color("&a正在旁观&7%s");
        private String firstPersonSubTitle = MessageUtil.color("&a点击左键打开菜单  &c按Shift键退出");
        private String firstPersonActionBar = MessageUtil.color("&f目标: &a&l%s  &f生命值: &a&l%d &c&l❤");
        private String thirdPersonActionBar = MessageUtil.color("&f目标: &a&l%s  &f生命值: &a&l%d  &f距离: &a&l%s米");
        private String menuHint = MessageUtil.color("  &a点击左键打开菜单  &c按Shift退出");
    }
}
