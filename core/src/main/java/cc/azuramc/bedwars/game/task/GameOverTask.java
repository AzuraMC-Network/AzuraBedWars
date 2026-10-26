package cc.azuramc.bedwars.game.task;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.TimelineConfig;
import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.database.service.PlayerDataService;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.ReconnectState;
import cc.azuramc.bedwars.listener.player.PlayerAFKListener;
import cc.azuramc.bedwars.util.BungeeUtil;
import cc.azuramc.bedwars.util.FireWorkUtil;
import lombok.Value;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * Shows the results, celebrates and shuts the server down
 *
 * @author an5w1r@163.com
 */
public class GameOverTask extends BukkitRunnable {


    private static final String[] RANKS = {"first", "second", "third"};

    private final GameManager gameManager;
    private final GameTeam winner;
    private final boolean tie;
    private int countdown = gameOver().getDefaultCountdown();

    public GameOverTask(GameManager gameManager) {
        this.gameManager = gameManager;
        this.winner = gameManager.getWinner();
        this.tie = gameManager.isTie();

        PlayerAFKListener.stop();
        displayTeamTitles();
        for (String line : generateLeaderboard(buildWinnerText())) {
            Bukkit.broadcastMessage(line);
        }
        updatePlayerStats();
        runTaskTimer(AzuraBedWars.getInstance().getPlugin(), 0L, 20L);
    }

    @Override
    public void run() {
        if (countdown-- > 0) {
            spawnVictoryFireworks();
            return;
        }
        cancel();
        GamePlayer.getOnlinePlayers().forEach(BungeeUtil::connectToLobby);
        Bukkit.getScheduler().runTaskLater(AzuraBedWars.getInstance().getPlugin(), Bukkit::shutdown, gameOver().getShutdownDelay());
    }

    private List<GamePlayer> celebratingPlayers() {
        if (tie) {
            return gameManager.getAliveTeams().stream()
                    .flatMap(team -> team.getAlivePlayers().stream())
                    .collect(Collectors.toList());
        }
        return winner != null ? winner.getAlivePlayers() : Collections.<GamePlayer>emptyList();
    }

    private void displayTeamTitles() {
        for (GameTeam team : gameManager.getGameTeams()) {
            String title;
            String subtitle;
            if (isTied(team)) {
                title = gameOver().getTieTitle();
                subtitle = gameOver().getTieSubtitle();
            } else if (team == winner) {
                title = gameOver().getVictoryTitle();
                subtitle = gameOver().getVictorySubtitle();
            } else {
                title = gameOver().getDefeatTitle();
                subtitle = gameOver().getDefeatSubtitle();
            }
            team.getGamePlayers().forEach(gamePlayer -> gamePlayer.sendTitle(title, subtitle,
                    gameOver().getTitleFadeIn(), gameOver().getTitleStay(), gameOver().getTitleFadeOut()));
        }
    }

    private String buildWinnerText() {
        if (winner == null) {
            return gameOver().getNoWinner();
        }
        return winner.getGamePlayers().stream().map(GamePlayer::getNickName).collect(Collectors.joining(", "));
    }

    private List<String> generateLeaderboard(String winnerText) {
        Map<String, List<RankingEntry>> rankings = new LinkedHashMap<>();
        rankings.put("Kills", buildRanking(stats -> stats.getKills() + stats.getFinalKills()));
        rankings.put("Assists", buildRanking(GameStats::getAssists));
        rankings.put("BedBreaks", buildRanking(GameStats::getBedBreaks));

        List<String> messages = new ArrayList<>();
        for (String line : gameOver().getCustomLeaderboardMessages()) {
            line = line.replace("<winnerFormat>", winnerText);
            for (Map.Entry<String, List<RankingEntry>> ranking : rankings.entrySet()) {
                if (line != null) {
                    line = fillRanking(line, ranking.getKey(), ranking.getValue());
                }
            }
            if (line != null && !line.trim().isEmpty()) {
                messages.add(line);
            }
        }
        return messages;
    }

    /**
     * Placeholders such as {@code <firstKills>} and {@code <firstKillsName>}, plus {@code <firstName>} for the kill ranking
     *
     * @return null when the line names a rank nobody holds
     */
    @Nullable
    private static String fillRanking(String line, String type, List<RankingEntry> ranking) {
        for (int i = 0; i < RANKS.length; i++) {
            String nameKey = "<" + RANKS[i] + type + "Name>";
            String valueKey = "<" + RANKS[i] + type + ">";
            String shortNameKey = type.equals("Kills") ? "<" + RANKS[i] + "Name>" : nameKey;
            if (i >= ranking.size()) {
                if (line.contains(nameKey) || line.contains(valueKey) || line.contains(shortNameKey)) {
                    return null;
                }
                continue;
            }
            String name = ranking.get(i).name();
            line = line.replace(nameKey, name).replace(valueKey, String.valueOf(ranking.get(i).value())).replace(shortNameKey, name);
        }
        return line;
    }

    @Value
    @Accessors(fluent = true)
    private static class RankingEntry {
        String name;
        int value;
    }

    @Value
    private static class GameStats {
        String name;
        int kills;
        int finalKills;
        int assists;
        int bedBreaks;
    }

    /**
     * Includes players who left and have not come back
     */
    private List<RankingEntry> buildRanking(ToIntFunction<GameStats> value) {
        List<GameStats> stats = new ArrayList<>();
        for (GamePlayer player : GamePlayer.getOnlinePlayers()) {
            stats.add(new GameStats(player.getNickName(), player.getCurrentGameKills(), player.getCurrentGameFinalKills(),
                    player.getCurrentGameAssists(), player.getCurrentGameDestroyedBeds()));
        }
        for (ReconnectState state : gameManager.getReconnectStates().values()) {
            stats.add(new GameStats(state.getName(), state.getCurrentGameKills(), state.getCurrentGameFinalKills(),
                    state.getCurrentGameAssists(), state.getCurrentGameDestroyedBeds()));
        }
        return stats.stream()
                .map(stat -> new RankingEntry(stat.getName(), value.applyAsInt(stat)))
                .filter(entry -> entry.value() > 0)
                .sorted((a, b) -> Integer.compare(b.value(), a.value()))
                .collect(Collectors.toList());
    }

    /**
     * Players who left and did not come back forfeit a win or tie but still take the loss
     */
    private void updatePlayerStats() {
        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            GameTeam team = gamePlayer.getGameTeam();
            if (team == null) {
                continue;
            }
            if (team == winner) {
                gamePlayer.recordWin();
            } else if (isTied(team)) {
                gamePlayer.getPlayerData().addTies();
            } else {
                gamePlayer.getPlayerData().addLosses();
            }
        }

        PlayerDataService playerDataService = AzuraBedWars.getInstance().getDatabaseProviderFactory().getPlayerDataService();
        for (ReconnectState state : gameManager.getReconnectStates().values()) {
            GameTeam team = state.getTeamColor() != null ? gameManager.getTeam(state.getTeamColor()) : null;
            PlayerData playerData = playerDataService.getCachedPlayerData(state.getUuid());
            if (team != null && team != winner && !isTied(team) && playerData != null) {
                playerData.addLosses();
            }
        }
    }

    private boolean isTied(GameTeam team) {
        return tie && gameManager.getAliveTeams().contains(team);
    }

    private void spawnVictoryFireworks() {
        if (tie && !gameOver().isTieFireworkEnabled()) {
            return;
        }
        for (GamePlayer gamePlayer : celebratingPlayers()) {
            Location location = gamePlayer.getPlayer().getLocation();
            FireWorkUtil.spawnFireWork(location.clone().add(0.0D, gameOver().getFireworkHeight(), 0.0D), location.getWorld());
        }
    }

    private static TimelineConfig.GameOverEvent gameOver() {
        return AzuraBedWars.getInstance().getTimelineConfig().getGameOverEvent();
    }
}
