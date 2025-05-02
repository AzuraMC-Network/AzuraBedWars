package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.util.ChatColorUtil;
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
        private String title = ChatColorUtil.color("&e&l超级起床战争");
        private String serverInfo = ChatColorUtil.color("&bas.azuramc.cc");
        private String myTeamMark = ChatColorUtil.color(" &7(我的队伍)");
        private String bedDestroyed = ChatColorUtil.color("&7❤");
        private String bedAlive = ChatColorUtil.color("&c❤");
        private String separator = ChatColorUtil.color("&f | ");
        private String emptyLine = ChatColorUtil.color("");
        private long updateInterval = 500; // 默认为0.5秒更新一次
    }
    
    @Data
    public static class LobbyScoreboard {
        private String title = ChatColorUtil.color("&e&l超级起床战争");
        private String serverInfo = ChatColorUtil.color("&bas.azuramc.cc");
        private String waitingMessage = ChatColorUtil.color("&f等待中...");
        private String emptyLine = ChatColorUtil.color("");
        private String defaultMode = ChatColorUtil.color("普通模式");
        private String expMode = ChatColorUtil.color("经验模式");
        private long updateInterval = 500; // 默认为0.5秒更新一次
    }
}
