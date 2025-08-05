package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.util.MessageUtil;
import lombok.Data;

/**
 * 设置配置对象
 * 存储插件的基本设置信息
 * @author an5w1r@163.com
 */
@Data
public class SettingsConfig {
    private boolean editorMode = false;
    private boolean debugMode = false;
    private final int teamBlockSearchRadius = 15;
    private String mapStorage = "JSON";
    private boolean enabledJedisMapFeature = false;
    private String defaultMapName = "game";
    private int bedSearchRadius = 18;
    private int bedDestroyReward = 10;
    private String playAgainCommand = MessageUtil.color("/not impl now");
    private int maxHealth = 20;
    private int maxNoMovementTime = 45;

    private DatabaseConfig database = new DatabaseConfig();
    private ChatConfig chatConfig = new ChatConfig();
    private GameScoreboard gameScoreboard = new GameScoreboard();
    private LobbyScoreboard lobbyScoreboard = new LobbyScoreboard();
    private DisplayDamage displayDamage = new DisplayDamage();
    private FireBall fireBall = new FireBall();

    @Data
    public static class DatabaseConfig {
        private String host = "localhost";
        private int port = 3306;
        private String username = "root";
        private String password = "123456";
        private String database = "azurabw";
    }

    @Data
    public static class FireBall {
        private int fireballDamage = 3;
    }

    @Data
    public static class ChatConfig {
        private String globalChatPrefix = MessageUtil.color("!");
        private String spectatorPrefix = MessageUtil.color("&7[旁观者]");
        private String globalChatTag = MessageUtil.color("&6[全局]");
        private String teamChatTag = MessageUtil.color("&9[团队]");
        private String chatSeparator = MessageUtil.color("&7: ");

        private int globalChatCooldown = 10;
    }

    @Data
    public static class GameScoreboard {
        private String title = MessageUtil.color("&e&l起床战争");
        private String serverInfo = MessageUtil.color("&bas.azuramc.cc");
        private String myTeamMark = MessageUtil.color(" &7(我的队伍)");
        private String bedDestroyed = MessageUtil.color("&7❤");
        private String bedAlive = MessageUtil.color("&c❤");
        private String separator = MessageUtil.color("&f | ");
        private String emptyLine = MessageUtil.color("");
        // 默认为0.5秒更新一次 50ms = 1tick
        private long updateInterval = 500;
    }

    @Data
    public static class LobbyScoreboard {
        private String title = MessageUtil.color("&e&l起床战争");
        private String serverInfo = MessageUtil.color("&bas.azuramc.cc");
        private String waitingMessage = MessageUtil.color("&f等待中...");
        private String emptyLine = MessageUtil.color("");
        private String defaultMode = MessageUtil.color("普通模式");
        private String expMode = MessageUtil.color("经验模式");
        // 默认为0.5秒更新一次 50ms = 1tick
        private long updateInterval = 500;
    }

    @Data
    public static class DisplayDamage {
        private final boolean arrowDisplayEnabled = true;
        private final boolean attackDisplayEnabled = true;
    }

}
