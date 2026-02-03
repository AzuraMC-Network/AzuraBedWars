package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.config.annotation.Comment;
import cc.azuramc.bedwars.util.MessageUtil;
import lombok.Data;

/**
 * @author an5w1r@163.com
 */
@Data
@Comment("游戏消息配置文件")
public class MessageConfig {

    @Comment("普通击杀奖励消息")
    private String normalKillRewardsMessage = MessageUtil.color("&6+1个金币");

    @Comment("最终击杀奖励消息")
    private String finalKillRewardsMessage = MessageUtil.color("&6+1个金币 (最终击杀)");

    @Comment({"复活倒计时标题", "变量: %d 为倒计时秒数"})
    private String respawnCountdownTitle = MessageUtil.color("&e&l%d");

    @Comment("复活倒计时副标题")
    private String respawnCountdownSubTitle = MessageUtil.color("&7你死了 将在稍后重生");

    @Comment("复活完成标题")
    private String respawnCompleteTitle = MessageUtil.color("&a已复活！");

    @Comment("复活完成副标题")
    private String respawnCompleteSubTitle = MessageUtil.color("&7因为你的床还在 所以你复活了");

    @Comment("永久死亡标题(床被破坏后)")
    private String deathPermanentTitle = MessageUtil.color("&c你凉了！");

    @Comment("永久死亡副标题")
    private String deathPermanentSubTitle = MessageUtil.color("&7你没床了");

    @Comment("队伍淘汰分隔线")
    private String teamEliminatedFormat = MessageUtil.color("&7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");

    @Comment({"队伍淘汰消息", "变量: %s 为队伍名和破坏者名"})
    private String teamEliminatedMessage = MessageUtil.color("%s &c凉了! &e挖床者: %s");

    @Comment("再来一局提示消息")
    private String playAgainMessage = MessageUtil.color("&c你凉了!想再来一局吗? ");

    @Comment("再来一局按钮文本")
    private String playAgainButton = MessageUtil.color("&b&l点击这里!");

    @Comment({"玩家重连消息", "变量: %s 为玩家名"})
    private String playerReconnectMessage = MessageUtil.color("&7%s&a重连上线");

    @Comment({"玩家离开消息", "变量: %s 为玩家名"})
    private String playerLeaveMessage = MessageUtil.color("&7%s&e离开游戏");

    @Comment({"传送粉开始传送消息", "变量: %d 为剩余秒数"})
    private String warpPowderStartMessage = MessageUtil.color("&a在 &c%d&a 秒后你将被传送，请不要移动!");

    @Comment("传送被取消消息")
    private String warpPowderCancelMessage = MessageUtil.color("&c你的传送被取消!");

    @Comment("铁资源点名称")
    private String ironGeneratorName = MessageUtil.color("铁刷新");

    @Comment("金资源点名称")
    private String goldGeneratorName = MessageUtil.color("金刷新");

    @Comment("钻石资源点名称")
    private String diamondGeneratorName = MessageUtil.color("钻石刷新");

    @Comment("钻石时间显示文本")
    private String diamondTimeDisplay = MessageUtil.color("钻石时间显示");

    @Comment("绿宝石资源点名称")
    private String emeraldGeneratorName = MessageUtil.color("绿宝石刷新");

    @Comment("绿宝石时间显示文本")
    private String emeraldTimeDisplay = MessageUtil.color("绿宝石时间显示");

    @Comment("旁观者相关消息配置")
    private Spectator spectator = new Spectator();

    @Data
    public static class Spectator {

        @Comment("旁观者指南针 GUI 标题")
        private String spectatorCompassGuiTitle = MessageUtil.color("&8选择一个玩家来传送");

        @Comment({"生命值显示格式", "变量: %d 为生命值"})
        private String healthFormat = MessageUtil.color("&f血量: &8%d");

        @Comment({"饥饿值显示格式", "变量: %d 为饥饿值"})
        private String foodFormat = MessageUtil.color("&f饥饿: &8%d");

        @Comment({"等级显示格式", "变量: %d 为等级"})
        private String levelFormat = MessageUtil.color("&f等级: &8%d");

        @Comment({"距离显示格式", "变量: %d 为距离"})
        private String distanceFormat = MessageUtil.color("&f距离: &8%d");

        @Comment("旁观者设置 GUI 标题")
        private String spectatorSettingsGuiTitle = MessageUtil.color("&8旁观者设置");

        @Comment("速度效果移除消息")
        private String speedRemoved = MessageUtil.color("&c你不再有任何速度效果！");

        @Comment({"速度效果添加消息", "变量: %s 为速度等级"})
        private String speedAdded = MessageUtil.color("&a你获得了 速度 %s 效果！");

        @Comment("自动传送开启消息")
        private String autoTPEnabled = MessageUtil.color("&a你开启了自动传送功能！");

        @Comment("自动传送关闭消息")
        private String autoTPDisabled = MessageUtil.color("&c你不再被自动传送到目标位置！");

        @Comment("夜视开启消息")
        private String nightVersionEnabled = MessageUtil.color("&a你现在拥有了夜视！");

        @Comment("夜视关闭消息")
        private String nightVersionDisabled = MessageUtil.color("&c你不再有夜视效果了！");

        @Comment("第一人称模式开启消息")
        private String firstPersonEnabled = MessageUtil.color("&a当你用你的指南针现在一个玩家后，你会被自动传送到他那里！");

        @Comment("第一人称模式关闭消息")
        private String firstPersonDisabled = MessageUtil.color("&c你将默认使用第三人称模式！");

        @Comment("隐藏其他旁观者开启消息")
        private String hideOthersEnabled = MessageUtil.color("&c你不会再看到其他的旁观者！");

        @Comment("隐藏其他旁观者关闭消息")
        private String hideOthersDisabled = MessageUtil.color("&a你现在可以看见其他旁观者了！");

        @Comment("飞行锁定开启消息")
        private String flyEnabled = MessageUtil.color("&a你现在不能停止飞行！");

        @Comment("飞行锁定关闭消息")
        private String flyDisabled = MessageUtil.color("&a你现在能停止飞行！");

        @Comment("目标丢失消息")
        private String targetLostMessage = MessageUtil.color("&c&l目标已丢失或不在同一个世界");

        @Comment({"第一人称标题", "变量: %s 为目标玩家名"})
        private String firstPersonTitle = MessageUtil.color("&a正在旁观&7%s");

        @Comment("第一人称副标题")
        private String firstPersonSubTitle = MessageUtil.color("&a点击左键打开菜单  &c按Shift键退出");

        @Comment({"第一人称动作栏消息", "变量: %s 为玩家名, %d 为生命值"})
        private String firstPersonActionBar = MessageUtil.color("&f目标: &a&l%s  &f生命值: &a&l%d &c&l❤");

        @Comment({"第三人称动作栏消息", "变量: %s 为玩家名和距离, %d 为生命值"})
        private String thirdPersonActionBar = MessageUtil.color("&f目标: &a&l%s  &f生命值: &a&l%d  &f距离: &a&l%s米");

        @Comment("菜单提示")
        private String menuHint = MessageUtil.color("  &a点击左键打开菜单  &c按Shift退出");
    }
}
