package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.api.config.Comment;
import cc.azuramc.bedwars.api.config.Range;
import lombok.Data;

/**
 * @author an5w1r@163.com
 */
@Data
@Comment("Game rules")
public class GameplayConfig {

    @Comment("Max health of players")
    @Range(min = 1, max = 1024)
    private int maxHealth = 20;

    @Comment("Seconds without moving before a player counts as AFK")
    @Range(min = 1)
    private int maxNoMovementTime = 45;

    @Comment("Spawn protection radius of a team")
    @Range(min = 0)
    private int teamSpawnProtectionRadius = 6;

    @Comment("Protection radius of a generator")
    @Range(min = 0)
    private int resourceSpawnProtectionRadius = 2;

    @Comment("Radius to search for team blocks")
    @Range(min = 1)
    private int teamBlockSearchRadius = 15;

    @Comment("Radius around the team spawn to search for its bed")
    @Range(min = 1)
    private int bedSearchRadius = 18;

    @Comment("Coins for breaking a bed")
    @Range(min = 0)
    private int bedDestroyReward = 10;

    @Comment("Fireball")
    private FireBall fireBall = new FireBall();

    @Comment("Chat")
    private Chat chat = new Chat();

    @Comment("Player death")
    private PlayerDeath playerDeath = new PlayerDeath();

    @Comment("Player respawn")
    private PlayerRespawn playerRespawn = new PlayerRespawn();

    @Comment("Spectators")
    private Spectator spectator = new Spectator();

    @Data
    public static class FireBall {
        @Comment("Explosion damage of a fireball")
        @Range(min = 0)
        private int fireballDamage = 3;
    }

    @Data
    public static class Chat {
        @Comment("Prefix that sends a message to everyone")
        private String globalChatPrefix = "!";

        @Comment("Spectator chat prefix")
        private String spectatorPrefix = "&7[SPECTATOR]";

        @Comment("Global chat tag")
        private String globalChatTag = "&6[SHOUT]";

        @Comment("Team chat tag")
        private String teamChatTag = "&9[TEAM]";

        @Comment("Chat separator")
        private String chatSeparator = "&7: ";

        @Comment("Global chat cooldown in seconds")
        @Range(min = 0)
        private int globalChatCooldown = 10;
    }

    @Data
    public static class PlayerDeath {
        @Comment("Times the coin reward shows in the action bar")
        @Range(min = 0)
        private int coinsActionBarTimes = 5;

        @Comment("Action bar interval in ticks")
        @Range(min = 1)
        private int actionBarPeriod = 10;

        @Comment("Coins for a kill")
        @Range(min = 0)
        private double coinsReward = 1.0;

        @Comment("Fall damage multiplier after a fireball jump")
        @Range(min = 0)
        private double fireballFallenDamageRate = 0.4;

        @Comment("Fall damage multiplier")
        @Range(min = 0)
        private double normalFallenDamageRate = 0.6;

        @Comment("Explosion damage multiplier")
        @Range(min = 0)
        private double explosionDamageRate = 0.4;
    }

    @Data
    public static class PlayerRespawn {
        @Comment("Respawn countdown in seconds")
        @Range(min = 0)
        private int respawnCountdownSeconds = 5;

        @Comment("Respawn protection in ticks")
        @Range(min = 0)
        private int respawnProtectionTicks = 60;

        @Comment("Title stay time in ticks")
        @Range(min = 0)
        private int titleStay = 20;
    }

    @Data
    public static class Spectator {
        @Comment("Spectator settings menu")
        private SettingGui settingGui = new SettingGui();

        @Comment("Target tracking")
        private Target target = new Target();

        @Data
        public static class SettingGui {
            @Comment("Menu size, a multiple of 9")
            @Range(min = 9, max = 54)
            private int inventorySize = 36;

            @Comment("Slot of the no speed button")
            private int speedNoneSlot = 11;

            @Comment("Slot of the speed I button")
            private int speedISlot = 12;

            @Comment("Slot of the speed II button")
            private int speedIISlot = 13;

            @Comment("Slot of the speed III button")
            private int speedIIISlot = 14;

            @Comment("Slot of the speed IV button")
            private int speedIVSlot = 15;

            @Comment("Slot of the auto teleport button")
            private int autoTpSlot = 20;

            @Comment("Slot of the night vision button")
            private int nightVisionSlot = 21;

            @Comment("Slot of the first person button")
            private int firstPersonSlot = 22;

            @Comment("Slot of the hide spectators button")
            private int hideOthersSlot = 23;

            @Comment("Slot of the always fly button")
            private int flySlot = 24;
        }

        @Data
        public static class Target {
            @Comment("Distance that triggers an auto teleport")
            @Range(min = 0)
            private double autoTpDistance = 20.0D;

            @Comment("Title duration in ticks")
            @Range(min = 0)
            private int titleDuration = 20;
        }
    }
}
