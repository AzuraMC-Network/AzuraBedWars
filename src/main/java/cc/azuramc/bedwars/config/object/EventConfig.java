package cc.azuramc.bedwars.config.object;

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

    private DestroyBedEvent destroyBedEvent = new DestroyBedEvent();
    private DiamondUpdateEvent diamondUpdateEvent = new DiamondUpdateEvent();
    private EmeraldUpdateEvent emeraldUpdateEvent = new EmeraldUpdateEvent();
    private ShutdownEvent shutdownEvent = new ShutdownEvent();
    private OverEvent overEvent = new OverEvent();
    private StartEvent startEvent = new StartEvent();

    @Data
    public static class DestroyBedEvent {

        private Title title = new Title();
        private int executeSecond = 360;

        @Data
        public static class Title {
            private int fadeIn = 10;
            private int titleStay = 20;
            private int fadeOut = 10;
        }
    }

    @Data
    public static class DiamondUpdateEvent {
        private int level2RefreshSecond = 23;
        private int level3RefreshSecond = 15;
    }


    @Data
    public static class EmeraldUpdateEvent {
        private int level2RefreshSecond = 23;
        private int level3RefreshSecond = 15;
    }

    @Data
    public static class ShutdownEvent {
        private int shutdownDelaySecond = 30;
    }

    @Data
    public static class OverEvent {
        private int executeSecond = 600;
    }

    @Data
    public static class StartEvent {

        private Title title = new Title();
        private Upgrade upgrade = new Upgrade();
        private int countDown = 5;
        private int eventPriority = 0;

        @Data
        public static class Title {
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