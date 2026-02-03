package cc.azuramc.bedwars.scoreboard.provider;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.SettingsConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.scoreboard.util.ScoreboardFormatter;

import java.util.List;

/**
 * @author an5w1r@163.com
 */
public class GameEndBoardProvider extends AbstractBoardProvider {

    private final SettingsConfig.GameEndScoreboard config;

    public GameEndBoardProvider(GameManager gameManager) {
        super(gameManager);
        this.config = AzuraBedWars.getInstance().getSettingsConfig().getGameEndScoreboard();
    }

    @Override
    protected String getTitle() {
        return config.getTitle();
    }

    @Override
    protected List<String> buildLines(GamePlayer gamePlayer) {
        updatePlayerLevel(gamePlayer.getPlayer(), gamePlayer.getPlayerData());

        return ScoreboardFormatter.create()
                .set("date", getFormattedDateRaw())
                .set("winner", getWinnerName())
                .set("server", "")
                .formatLines(config.getLines());
    }

    private String getWinnerName() {
        GameTeam winner = gameManager.getWinner();
        return winner != null ? winner.getName() : "无";
    }
}
