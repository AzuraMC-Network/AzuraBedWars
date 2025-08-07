package cc.azuramc.bedwars.config.object;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ant1aura@qq.com
 */
@Data
@EqualsAndHashCode
public class PlayerConfig {
    private PlayerDeath playerDeath = new PlayerDeath();
    private PlayerRespawn playerRespawn = new PlayerRespawn();
    private Spectator spectator = new Spectator();


    @Data
    public static class PlayerDeath {
        private int coinsActionBarTimes = 5;
        private int actionBarPeriod = 10;
        private double coinsReward = 1.0;
        private double fireballFallenDamageRate = 0.4;
        private double normalFallenDamageRate = 0.6;
        private double explosionDamageRate = 0.4;
    }

    @Data
    public static class PlayerRespawn {
        private int respawnCountdownSeconds = 5;
        private int respawnProtectionTicks = 60;
        private int titleStay = 20;
    }

    @Data
    public static class Spectator {

        private SettingGUI settingGUI = new SettingGUI();
        private Target target = new Target();

        @Data
        public static class SettingGUI {
            private int inventorySize = 36;
            private int speedNoneSlot = 11;
            private int speedISlot = 12;
            private int speedIISlot = 13;
            private int speedIIISlot = 14;
            private int speedIVSlot = 15;
            private int autoTPSlot = 20;
            private int nightVersionSlot = 21;
            private int firstPersonSlot = 22;
            private int hideOthersSlot = 23;
            private int flySlot = 24;
        }

        @Data
        public static class Target {
            private double autoTPDistance = 20.0D;
            private int titleDuration = 20;
        }
    }
}
