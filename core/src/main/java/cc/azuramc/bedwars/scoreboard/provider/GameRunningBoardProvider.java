package cc.azuramc.bedwars.scoreboard.provider;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.SettingsConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.scoreboard.util.ScoreboardFormatter;
import cc.azuramc.bedwars.util.MessageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
public class GameRunningBoardProvider extends AbstractBoardProvider {

    private final SettingsConfig.GameScoreboard config;

    public GameRunningBoardProvider(GameManager gameManager) {
        super(gameManager);
        this.config = AzuraBedWars.getInstance().getSettingsConfig().getGameScoreboard();
    }

    @Override
    protected String getTitle() {
        return config.getTitle();
    }

    @Override
    protected List<String> buildLines(GamePlayer gamePlayer) {
        List<String> teamLines = buildTeamLines(gamePlayer);

        return ScoreboardFormatter.create()
                .set("date", getFormattedDateRaw())
                .set("next_event", gameManager.getGameEventManager().formattedNextEvent())
                .set("time_left", gameManager.getFormattedTime(gameManager.getGameEventManager().getLeftTime()))
                .set("server", "")
                .formatLinesWithTeams(config.getLines(), teamLines);
    }

    private List<String> buildTeamLines(GamePlayer gamePlayer) {
        List<String> teamLines = new ArrayList<>();

        for (GameTeam gameTeam : gameManager.getGameTeams()) {
            String bedStatus = gameTeam.isDestroyed() ? config.getBedDestroyed() : config.getBedAlive();
            String myTeamMark = gameTeam.isInTeam(gamePlayer) ? config.getMyTeamMark() : "";

            String line = config.getTeamLineFormat()
                    .replace("{team_name}", gameTeam.getName())
                    .replace("{bed_status}", bedStatus)
                    .replace("{alive_count}", String.valueOf(gameTeam.getAlivePlayers().size()))
                    .replace("{my_team_mark}", myTeamMark);

            teamLines.add(MessageUtil.color(line));
        }

        return teamLines;
    }
}
