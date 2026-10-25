package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.config.annotation.Comment;
import cc.azuramc.bedwars.config.annotation.Range;
import lombok.Data;

/**
 * @author an5w1r@163.com
 */
@Data
@Comment("对局规则")
public class GameplayConfig {

    @Comment("玩家最大生命值")
    @Range(min = 1, max = 1024)
    private int maxHealth = 20;

    @Comment("最大静止时间(秒)，超过将被判定为挂机")
    @Range(min = 1)
    private int maxNoMovementTime = 45;

    @Comment("队伍出生点保护半径")
    @Range(min = 0)
    private int teamSpawnProtectionRadius = 6;

    @Comment("资源点保护半径")
    @Range(min = 0)
    private int resourceSpawnProtectionRadius = 2;

    @Comment("队伍方块搜索半径")
    @Range(min = 1)
    private int teamBlockSearchRadius = 15;

    @Comment("床搜索半径(以队伍出生点为中心查找该队的床)")
    @Range(min = 1)
    private int bedSearchRadius = 18;

    @Comment("破坏床奖励金币数量")
    @Range(min = 0)
    private int bedDestroyReward = 10;

    @Comment("火球配置")
    private FireBall fireBall = new FireBall();

    @Comment("聊天配置")
    private Chat chat = new Chat();

    @Comment("玩家死亡相关配置")
    private PlayerDeath playerDeath = new PlayerDeath();

    @Comment("玩家复活相关配置")
    private PlayerRespawn playerRespawn = new PlayerRespawn();

    @Comment("旁观者相关配置")
    private Spectator spectator = new Spectator();

    @Data
    public static class FireBall {
        @Comment("火球爆炸伤害值")
        @Range(min = 0)
        private int fireballDamage = 3;
    }

    @Data
    public static class Chat {
        @Comment("全局聊天前缀(使用此前缀发送消息将被所有人看到)")
        private String globalChatPrefix = "!";

        @Comment("旁观者聊天前缀")
        private String spectatorPrefix = "&7[旁观者]";

        @Comment("全局聊天标签")
        private String globalChatTag = "&6[全局]";

        @Comment("队伍聊天标签")
        private String teamChatTag = "&9[团队]";

        @Comment("聊天分隔符")
        private String chatSeparator = "&7: ";

        @Comment("全局聊天冷却时间(秒)")
        @Range(min = 0)
        private int globalChatCooldown = 10;
    }

    @Data
    public static class PlayerDeath {
        @Comment("金币奖励动作栏显示次数")
        @Range(min = 0)
        private int coinsActionBarTimes = 5;

        @Comment("动作栏显示间隔(tick)")
        @Range(min = 1)
        private int actionBarPeriod = 10;

        @Comment("击杀金币奖励数量")
        @Range(min = 0)
        private double coinsReward = 1.0;

        @Comment("火球造成的摔落伤害系数")
        @Range(min = 0)
        private double fireballFallenDamageRate = 0.4;

        @Comment("普通摔落伤害系数")
        @Range(min = 0)
        private double normalFallenDamageRate = 0.6;

        @Comment("爆炸伤害系数")
        @Range(min = 0)
        private double explosionDamageRate = 0.4;
    }

    @Data
    public static class PlayerRespawn {
        @Comment("复活倒计时时间(秒)")
        @Range(min = 0)
        private int respawnCountdownSeconds = 5;

        @Comment("复活保护时间(tick)")
        @Range(min = 0)
        private int respawnProtectionTicks = 60;

        @Comment("标题停留时间(tick)")
        @Range(min = 0)
        private int titleStay = 20;
    }

    @Data
    public static class Spectator {
        @Comment("旁观者设置 GUI 配置")
        private SettingGui settingGui = new SettingGui();

        @Comment("目标追踪配置")
        private Target target = new Target();

        @Data
        public static class SettingGui {
            @Comment("GUI 大小(必须是9的倍数)")
            @Range(min = 9, max = 54)
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
            private int autoTpSlot = 20;

            @Comment("夜视按钮槽位")
            private int nightVisionSlot = 21;

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
            @Range(min = 0)
            private double autoTpDistance = 20.0D;

            @Comment("标题显示持续时间(tick)")
            @Range(min = 0)
            private int titleDuration = 20;
        }
    }
}
