package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.api.config.Comment;
import lombok.Data;

/**
 * @author ant1aura@qq.com
 */
@Data
@Comment("Special items")
public class ItemConfig {

    @Comment("Rescue platform")
    private RescuePlatform rescuePlatform = new RescuePlatform();

    @Comment("Warp powder")
    private WarpPowder warpPowder = new WarpPowder();

    @Comment("Speed wool")
    private SpeedWoolHandler speedWoolHandler = new SpeedWoolHandler();

    @Comment("Bridge egg")
    private EggBridge eggBridge = new EggBridge();

    @Comment("Lobby items")
    private GameManager gameManager = new GameManager();

    @Data
    public static class SpeedWoolHandler {
        @Comment("Longest path of speed wool")
        private int maxSpeedWoolLength = 6;
    }

    @Data
    public static class RescuePlatform {
        @Comment("Seconds the platform stays")
        private int defaultBreakTime = 12;

        @Comment("Cooldown in seconds")
        private int defaultWaitTime = 20;

        @Comment("Jump boost strength")
        private double jumpBoost = 0.7;
    }

    @Data
    public static class WarpPowder {
        @Comment("Seconds before the teleport")
        private int defaultTeleportTime = 6;

        @Comment("Particles per ring")
        private int circleElements = 20;

        @Comment("Ring radius")
        private double particleRadius = 1.0;

        @Comment("Total height of the particles")
        private double particleHeight = 2.0;

        @Comment("Number of rings")
        private double circleCount = 15.0;

        @Comment("Name of the cancel item")
        private String cancelItemName = "&4Cancel Teleport";
    }

    @Data
    public static class GameManager {
        @Comment("Name of the team selector item")
        private String teamSelectorName = "&eTeam Selector &7(Right Click)";

        @Comment("Name of the leave item")
        private String leaveGameName = "&cReturn to Lobby &7(Right Click)";
    }

    @Data
    public static class EggBridge {
        @Comment("Bridge egg cooldown in seconds")
        private int eggCooldownSeconds = 3;

        @Comment("Cooldown message")
        private String eggCooldownMessage = "&cYour bridge egg is on cooldown!";
    }
}
