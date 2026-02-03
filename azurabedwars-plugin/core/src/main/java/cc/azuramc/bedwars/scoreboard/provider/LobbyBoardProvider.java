package cc.azuramc.bedwars.scoreboard.provider;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.SettingsConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.task.GameStartTask;
import cc.azuramc.bedwars.scoreboard.util.ScoreboardFormatter;

import java.util.List;

/**
 * @author an5w1r@163.com
 */
public class LobbyBoardProvider extends AbstractBoardProvider {

    private final SettingsConfig.LobbyScoreboard config;

    public LobbyBoardProvider(GameManager gameManager) {
        super(gameManager);
        this.config = AzuraBedWars.getInstance().getSettingsConfig().getLobbyScoreboard();
    }

    @Override
    protected String getTitle() {
        return config.getTitle();
    }

    @Override
    protected List<String> buildLines(GamePlayer gamePlayer) {
        updatePlayerLevel(gamePlayer.getPlayer(), gamePlayer.getPlayerData());

        String modeText = gamePlayer.getPlayerData().getMode() == GameModeType.DEFAULT
                ? config.getDefaultMode()
                : config.getExpMode();

        return ScoreboardFormatter.create()
                .set("date", getFormattedDateRaw())
                .set("map_name", gameManager.getMapData().getName())
                .set("team_size", gameManager.getMapData().getPlayers().getTeam())
                .set("team_count", gameManager.getGameTeams().size())
                .set("author", gameManager.getMapData().getAuthor())
                .set("players", GamePlayer.getOnlinePlayers().size())
                .set("max_players", gameManager.getMaxPlayers())
                .set("countdown", getCountdown())
                .set("mode", modeText)
                .set("version", plugin.getDescription().getVersion())
                .set("server", "")
                .formatLines(config.getLines());
    }

    private String getCountdown() {
        GameStartTask gameStartTask = gameManager.getGameStartTask();
        if (gameStartTask != null) {
            return config.getCountdownFormat().replace("{seconds}", String.valueOf(gameStartTask.getCountdown()));
        } else if (gameManager.getGameState() == GameState.WAITING) {
            return config.getWaitingMessage();
        }
        return "";
    }
}
