package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.util.ChatColorUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 设置配置对象
 * <p>
 * 存储插件的消息等内容
 * </p>
 */
@Data
@EqualsAndHashCode
public class EventConfig {

    private DestroyBedEvent destroyBedEvent;
    private DiamondUpdateEvent diamondUpdateEvent;
    private EmeraldUpdateEvent emeraldUpdateEvent;
    private ShutdownEvent shutdownEvent;
    private OverEvent overEvent;
    private StartEvent startEvent;

    @Data
    public static class DestroyBedEvent {

        private Title title;
        private String eventName = ChatColorUtil.color("床自毁");
        private int executeSecond = 360;

        @Data
        public static class Title {
            private String titleString = ChatColorUtil.color("§c§l床自毁");
            private String subtitle = ChatColorUtil.color("§e所有队伍床消失");
            private int fadeIn = 10;
            private int titleStay = 20;
            private int fadeOut = 10;
        }
    }

    @Data
    public static class DiamondUpdateEvent {
        private String eventName = ChatColorUtil.color("钻石刷新");
        private int level2RefreshSecond = 23;
        private int level3RefreshSecond = 15;
    }


    @Data
    public static class EmeraldUpdateEvent {
        private String eventName = ChatColorUtil.color("绿宝石刷新");
        private int level2RefreshSecond = 23;
        private int level3RefreshSecond = 15;
    }

    @Data
    public static class ShutdownEvent {
        private String eventName = ChatColorUtil.color("游戏关闭");
        private int shutdownDelaySecond = 30;
    }

    @Data
    public static class OverEvent {
        private String eventName = ChatColorUtil.color("游戏结束");
        private int executeSecond = 600;
    }

    @Data
    public static class StartEvent {

        private Title title;
        private Upgrade upgrade;
        private String eventName = ChatColorUtil.color("开始游戏");
        private int countDown = 5;
        private int eventPriority = 0;
        private String teamUpgradeTaskName = "团队升级";

        @Data
        public static class Title {
            private String titleString = ChatColorUtil.color("§c§l游戏即将开始");
            private String subtitle = ChatColorUtil.color("§e§l");
            private int fadeIn = 1;
            private int titleStay = 20;
            private int fadeOut = 1;
        }

        @Data
        public static class Upgrade {
            private double healingPoolRange = 7.0;
            private double trapTriggerRange = 20.0;

            private int hasteEffectDuration = 40;
            private int regenerationEffectDuration = 60;
            private int regenerationEffectAmplifier = 1;
            private int trapEffectDuration = 200;
            private int trapEffectAmplifier = 1;
            private int miningFatigueEffectDuration = 200;
            private int miningFatigueEffectAmplifier = 0;
        }
    }

}