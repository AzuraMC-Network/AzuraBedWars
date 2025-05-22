package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.util.MessageUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ant1aura@qq.com
 */
@Data
@EqualsAndHashCode
public class ScoreboardConfig {
    
    private GameScoreboard gameScoreboard = new GameScoreboard();
    private LobbyScoreboard lobbyScoreboard = new LobbyScoreboard();
    
    @Data
    public static class GameScoreboard {
        private String title = MessageUtil.color("&e&l超级起床战争");
        private String serverInfo = MessageUtil.color("&bas.azuramc.cc");
        private String myTeamMark = MessageUtil.color(" &7(我的队伍)");
        private String bedDestroyed = MessageUtil.color("&7❤");
        private String bedAlive = MessageUtil.color("&c❤");
        private String separator = MessageUtil.color("&f | ");
        private String emptyLine = MessageUtil.color("");
        private long updateInterval = 500; // 默认为0.5秒更新一次
    }
    
    @Data
    public static class LobbyScoreboard {
        private String title = MessageUtil.color("&e&l超级起床战争");
        private String serverInfo = MessageUtil.color("&bas.azuramc.cc");
        private String waitingMessage = MessageUtil.color("&f等待中...");
        private String emptyLine = MessageUtil.color("");
        private String defaultMode = MessageUtil.color("普通模式");
        private String expMode = MessageUtil.color("经验模式");
        private long updateInterval = 500; // 默认为0.5秒更新一次
    }
}
