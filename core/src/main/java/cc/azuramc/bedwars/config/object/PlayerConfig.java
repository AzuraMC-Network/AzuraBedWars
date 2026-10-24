package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.config.annotation.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ant1aura@qq.com
 */
@Data
@EqualsAndHashCode
@Comment("玩家配置文件")
public class PlayerConfig {

    @Comment("玩家死亡相关配置")
    private PlayerDeath playerDeath = new PlayerDeath();

    @Comment("玩家复活相关配置")
    private PlayerRespawn playerRespawn = new PlayerRespawn();

    @Comment("旁观者相关配置")
    private Spectator spectator = new Spectator();


    @Data
    public static class PlayerDeath {
        @Comment("金币奖励动作栏显示次数")
        private int coinsActionBarTimes = 5;

        @Comment("动作栏显示间隔(tick)")
        private int actionBarPeriod = 10;

        @Comment("击杀金币奖励数量")
        private double coinsReward = 1.0;

        @Comment("火球造成的摔落伤害系数")
        private double fireballFallenDamageRate = 0.4;

        @Comment("普通摔落伤害系数")
        private double normalFallenDamageRate = 0.6;

        @Comment("爆炸伤害系数")
        private double explosionDamageRate = 0.4;
    }

    @Data
    public static class PlayerRespawn {
        @Comment("复活倒计时时间(秒)")
        private int respawnCountdownSeconds = 5;

        @Comment("复活保护时间(tick)")
        private int respawnProtectionTicks = 60;

        @Comment("标题停留时间(tick)")
        private int titleStay = 20;
    }

    @Data
    public static class Spectator {

        @Comment("旁观者设置 GUI 配置")
        private SettingGUI settingGUI = new SettingGUI();

        @Comment("目标追踪配置")
        private Target target = new Target();

        @Data
        public static class SettingGUI {
            @Comment("GUI 大小(必须是9的倍数)")
            private int inventorySize = 36;

            @Comment("无速度按钮槽位")
            private int speedNoneSlot = 11;

            @Comment("速度 I 按钮槽位")
            private int speedISlot = 12;

            @Comment("速度 II 按钮槽位")
            private int speedIISlot = 13;

            @Comment("速度 III 按钮槽位")
            private int speedIIISlot = 14;

            @Comment("速度 IV 按钮槽位")
            private int speedIVSlot = 15;

            @Comment("自动传送按钮槽位")
            private int autoTPSlot = 20;

            @Comment("夜视按钮槽位")
            private int nightVersionSlot = 21;

            @Comment("第一人称按钮槽位")
            private int firstPersonSlot = 22;

            @Comment("隐藏其他旁观者按钮槽位")
            private int hideOthersSlot = 23;

            @Comment("飞行锁定按钮槽位")
            private int flySlot = 24;
        }

        @Data
        public static class Target {
            @Comment("自动传送触发距离")
            private double autoTPDistance = 20.0D;

            @Comment("标题显示持续时间(tick)")
            private int titleDuration = 20;
        }
    }
}
