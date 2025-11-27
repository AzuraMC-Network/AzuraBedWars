package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.config.annotation.ConfigComment;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 玩家配置类
 *
 * @author ant1aura@qq.com
 */
@Data
@EqualsAndHashCode
public class PlayerConfig {
    @ConfigComment("玩家死亡配置")
    private PlayerDeath playerDeath = new PlayerDeath();

    @ConfigComment("玩家复活配置")
    private PlayerRespawn playerRespawn = new PlayerRespawn();

    @ConfigComment("旁观者配置")
    private Spectator spectator = new Spectator();


    @Data
    public static class PlayerDeath {
        @ConfigComment("金币奖励Action Bar显示次数")
        private int coinsActionBarTimes = 5;

        @ConfigComment("Action Bar显示间隔（ticks）")
        private int actionBarPeriod = 10;

        @ConfigComment("击杀玩家获得的金币奖励")
        private double coinsReward = 1.0;

        @ConfigComment({
                "火球造成的坠落伤害比率",
                "实际伤害 = 坠落高度 × 此比率"
        })
        private double fireballFallenDamageRate = 0.4;

        @ConfigComment({
                "正常坠落伤害比率",
                "实际伤害 = 坠落高度 × 此比率"
        })
        private double normalFallenDamageRate = 0.6;

        @ConfigComment({
                "爆炸造成的伤害比率",
                "实际伤害 = 原始爆炸伤害 × 此比率"
        })
        private double explosionDamageRate = 0.4;
    }

    @Data
    public static class PlayerRespawn {
        @ConfigComment("复活倒计时时长（秒）")
        private int respawnCountdownSeconds = 5;

        @ConfigComment({
                "复活后的保护时间（ticks）",
                "1秒 = 20 ticks"
        })
        private int respawnProtectionTicks = 60;

        @ConfigComment("复活标题显示时长（ticks）")
        private int titleStay = 20;
    }

    @Data
    public static class Spectator {

        @ConfigComment("旁观者设置GUI配置")
        private SettingGUI settingGUI = new SettingGUI();

        @ConfigComment("旁观者目标配置")
        private Target target = new Target();

        @Data
        public static class SettingGUI {
            @ConfigComment("GUI大小（格子数）")
            private int inventorySize = 36;

            @ConfigComment("无速度效果选项槽位")
            private int speedNoneSlot = 11;

            @ConfigComment("速度I选项槽位")
            private int speedISlot = 12;

            @ConfigComment("速度II选项槽位")
            private int speedIISlot = 13;

            @ConfigComment("速度III选项槽位")
            private int speedIIISlot = 14;

            @ConfigComment("速度IV选项槽位")
            private int speedIVSlot = 15;

            @ConfigComment("自动传送选项槽位")
            private int autoTPSlot = 20;

            @ConfigComment("夜视选项槽位")
            private int nightVersionSlot = 21;

            @ConfigComment("第一人称视角选项槽位")
            private int firstPersonSlot = 22;

            @ConfigComment("隐藏其他玩家选项槽位")
            private int hideOthersSlot = 23;

            @ConfigComment("飞行选项槽位")
            private int flySlot = 24;
        }

        @Data
        public static class Target {
            @ConfigComment({
                    "自动传送触发距离（方块）",
                    "当旁观的目标超过此距离时自动传送"
            })
            private double autoTPDistance = 20.0D;

            @ConfigComment("目标切换标题显示时长（ticks）")
            private int titleDuration = 20;
        }
    }
}
