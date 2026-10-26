package cc.azuramc.bedwars.game;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * What a player keeps when they rejoin the running game
 *
 * @author an5w1r@163.com
 */
@Getter
public class ReconnectState {

    private final UUID uuid;
    private final String name;

    @Nullable
    private final TeamColor teamColor;

    private final int currentGameKills;
    private final int currentGameFinalKills;
    private final int currentGameAssists;
    private int currentGameDeaths;
    private final int currentGameDestroyedBeds;

    private final Map<String, Integer> tierLevels;
    private final Map<String, Integer> purchases;
    private final Set<String> keptItems;

    public ReconnectState(@NotNull GamePlayer gamePlayer) {
        this.uuid = gamePlayer.getUuid();
        this.name = gamePlayer.getNickName();
        this.teamColor = gamePlayer.getGameTeam() != null ? gamePlayer.getGameTeam().getTeamColor() : null;

        this.currentGameKills = gamePlayer.getCurrentGameKills();
        this.currentGameFinalKills = gamePlayer.getCurrentGameFinalKills();
        this.currentGameAssists = gamePlayer.getCurrentGameAssists();
        this.currentGameDeaths = gamePlayer.getCurrentGameDeaths();
        this.currentGameDestroyedBeds = gamePlayer.getCurrentGameDestroyedBeds();

        this.tierLevels = new HashMap<>(gamePlayer.getTierLevels());
        this.purchases = new HashMap<>(gamePlayer.getPurchases());
        this.keptItems = new LinkedHashSet<>(gamePlayer.getKeptItems());
    }

    public void addDeath() {
        currentGameDeaths++;
    }

    public void restoreTo(@NotNull GamePlayer gamePlayer, @NotNull GameManager gameManager) {
        if (teamColor != null) {
            GameTeam team = gameManager.getTeam(teamColor);
            if (team != null) {
                gamePlayer.setGameTeam(team);
            }
        }

        gamePlayer.setCurrentGameKills(currentGameKills);
        gamePlayer.setCurrentGameFinalKills(currentGameFinalKills);
        gamePlayer.setCurrentGameAssists(currentGameAssists);
        gamePlayer.setCurrentGameDeaths(currentGameDeaths);
        gamePlayer.setCurrentGameDestroyedBeds(currentGameDestroyedBeds);

        gamePlayer.setTierLevels(new HashMap<>(tierLevels));
        gamePlayer.setPurchases(new HashMap<>(purchases));
        gamePlayer.setKeptItems(new LinkedHashSet<>(keptItems));
    }
}
