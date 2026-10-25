package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.config.annotation.Comment;
import lombok.Data;

/**
 * @author an5w1r@163.com
 */
@Data
@Comment("游戏消息")
public class MessageConfig {

    @Comment("普通击杀奖励消息")
    private String normalKillRewardsMessage = "&6+1个金币";

    @Comment("最终击杀奖励消息")
    private String finalKillRewardsMessage = "&6+1个金币 (最终击杀)";

    @Comment({"复活倒计时标题", "变量: %d 为倒计时秒数"})
    private String respawnCountdownTitle = "&e&l%d";

    @Comment("复活倒计时副标题")
    private String respawnCountdownSubTitle = "&7你死了 将在稍后重生";

    @Comment("复活完成标题")
    private String respawnCompleteTitle = "&a已复活！";

    @Comment("复活完成副标题")
    private String respawnCompleteSubTitle = "&7因为你的床还在 所以你复活了";

    @Comment("永久死亡标题(床被破坏后)")
    private String deathPermanentTitle = "&c你凉了！";

    @Comment("永久死亡副标题")
    private String deathPermanentSubTitle = "&7你没床了";

    @Comment("队伍淘汰分隔线")
    private String teamEliminatedFormat = "&7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃";

    @Comment({"队伍淘汰消息", "变量: %s 为队伍名和破坏者名"})
    private String teamEliminatedMessage = "%s &c凉了! &e挖床者: %s";

    @Comment("再来一局提示消息")
    private String playAgainMessage = "&c你凉了!想再来一局吗? ";

    @Comment("再来一局按钮文本")
    private String playAgainButton = "&b&l点击这里!";

    @Comment({"玩家重连消息", "变量: %s 为玩家名"})
    private String playerReconnectMessage = "&7%s&a重连上线";

    @Comment({"玩家离开消息", "变量: %s 为玩家名"})
    private String playerLeaveMessage = "&7%s&e离开游戏";

    @Comment({"传送粉开始传送消息", "变量: %d 为剩余秒数"})
    private String warpPowderStartMessage = "&a在 &c%d&a 秒后你将被传送，请不要移动!";

    @Comment("传送被取消消息")
    private String warpPowderCancelMessage = "&c你的传送被取消!";

    @Comment("旁观者相关消息配置")
    private Spectator spectator = new Spectator();

    @Data
    public static class Spectator {

        @Comment("旁观者指南针 GUI 标题")
        private String spectatorCompassGuiTitle = "&8选择一个玩家来传送";

        @Comment({"生命值显示格式", "变量: %d 为生命值"})
        private String healthFormat = "&f血量: &8%d";

        @Comment({"饥饿值显示格式", "变量: %d 为饥饿值"})
        private String foodFormat = "&f饥饿: &8%d";

        @Comment({"等级显示格式", "变量: %d 为等级"})
        private String levelFormat = "&f等级: &8%d";

        @Comment({"距离显示格式", "变量: %d 为距离"})
        private String distanceFormat = "&f距离: &8%d";

        @Comment("旁观者设置 GUI 标题")
        private String spectatorSettingsGuiTitle = "&8旁观者设置";

        @Comment("速度效果移除消息")
        private String speedRemoved = "&c你不再有任何速度效果！";

        @Comment({"速度效果添加消息", "变量: %s 为速度等级"})
        private String speedAdded = "&a你获得了 速度 %s 效果！";

        @Comment("自动传送开启消息")
        private String autoTPEnabled = "&a你开启了自动传送功能！";

        @Comment("自动传送关闭消息")
        private String autoTPDisabled = "&c你不再被自动传送到目标位置！";

        @Comment("夜视开启消息")
        private String nightVisionEnabled = "&a你现在拥有了夜视！";

        @Comment("夜视关闭消息")
        private String nightVisionDisabled = "&c你不再有夜视效果了！";

        @Comment("第一人称模式开启消息")
        private String firstPersonEnabled = "&a当你用你的指南针现在一个玩家后，你会被自动传送到他那里！";

        @Comment("第一人称模式关闭消息")
        private String firstPersonDisabled = "&c你将默认使用第三人称模式！";

        @Comment("隐藏其他旁观者开启消息")
        private String hideOthersEnabled = "&c你不会再看到其他的旁观者！";

        @Comment("隐藏其他旁观者关闭消息")
        private String hideOthersDisabled = "&a你现在可以看见其他旁观者了！";

        @Comment("飞行锁定开启消息")
        private String flyEnabled = "&a你现在不能停止飞行！";

        @Comment("飞行锁定关闭消息")
        private String flyDisabled = "&a你现在能停止飞行！";

        @Comment("目标丢失消息")
        private String targetLostMessage = "&c&l目标已丢失或不在同一个世界";

        @Comment({"第一人称标题", "变量: %s 为目标玩家名"})
        private String firstPersonTitle = "&a正在旁观&7%s";

        @Comment("第一人称副标题")
        private String firstPersonSubTitle = "&a点击左键打开菜单  &c按Shift键退出";

        @Comment({"第一人称动作栏消息", "变量: %s 为玩家名, %d 为生命值"})
        private String firstPersonActionBar = "&f目标: &a&l%s  &f生命值: &a&l%d &c&l❤";

        @Comment({"第三人称动作栏消息", "变量: %s 为玩家名和距离, %d 为生命值"})
        private String thirdPersonActionBar = "&f目标: &a&l%s  &f生命值: &a&l%d  &f距离: &a&l%s米";

        @Comment("菜单提示")
        private String menuHint = "  &a点击左键打开菜单  &c按Shift退出";
    }
}
