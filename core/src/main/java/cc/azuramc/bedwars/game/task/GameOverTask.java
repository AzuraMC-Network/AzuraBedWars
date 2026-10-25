package cc.azuramc.bedwars.game.task;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.EventSettingsConfig;
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

    private static final EventSettingsConfig.GameOverEvent gameOverConfig = AzuraBedWars.getInstance().getEventSettingsConfig().getGameOverEvent();

    private static final String[] RANKS = {"first", "second", "third"};

    private final GameManager gameManager;
    private final GameTeam winner;
    private final boolean tie;
    private int countdown = gameOverConfig.getDefaultCountdown();

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
        Bukkit.getScheduler().runTaskLater(AzuraBedWars.getInstance().getPlugin(), Bukkit::shutdown, gameOverConfig.getShutdownDelay());
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
                title = gameOverConfig.getTieTitle();
                subtitle = gameOverConfig.getTieSubtitle();
            } else if (team == winner) {
                title = gameOverConfig.getVictoryTitle();
                subtitle = gameOverConfig.getVictorySubtitle();
            } else {
                title = gameOverConfig.getDefeatTitle();
                subtitle = gameOverConfig.getDefeatSubtitle();
            }
            team.getGamePlayers().forEach(gamePlayer -> gamePlayer.sendTitle(title, subtitle,
                    gameOverConfig.getTitleFadeIn(), gameOverConfig.getTitleStay(), gameOverConfig.getTitleFadeOut()));
        }
    }

    private String buildWinnerText() {
        if (winner == null) {
            return gameOverConfig.getNoWinner();
        }
        return winner.getGamePlayers().stream().map(GamePlayer::getNickName).collect(Collectors.joining(", "));
    }

    private List<String> generateLeaderboard(String winnerText) {
        Map<String, List<RankingEntry>> rankings = new LinkedHashMap<>();
        rankings.put("Kills", buildRanking(stats -> stats.getKills() + stats.getFinalKills()));
        rankings.put("Assists", buildRanking(GameStats::getAssists));
        rankings.put("BedBreaks", buildRanking(GameStats::getBedBreaks));

        List<String> messages = new ArrayList<>();
        for (String line : gameOverConfig.getCustomLeaderboardMessages()) {
            line = line.replace("<winnerFormat>", winnerText);
            for (Map.Entry<String, List<RankingEntry>> ranking : rankings.entrySet()) {
                line = fillRanking(line, ranking.getKey(), ranking.getValue());
            }
            if (!line.trim().isEmpty()) {
                messages.add(line);
            }
        }
        return messages;
    }

    /**
     * Placeholders such as {@code <firstKills>} and {@code <firstKillsName>}, plus {@code <firstName>} for the kill ranking
     */
    private static String fillRanking(String line, String type, List<RankingEntry> ranking) {
        for (int i = 0; i < RANKS.length; i++) {
            String name = i < ranking.size() ? ranking.get(i).name() : "";
            String value = i < ranking.size() ? String.valueOf(ranking.get(i).value()) : "0";
            line = line.replace("<" + RANKS[i] + type + "Name>", name).replace("<" + RANKS[i] + type + ">", value);
            if (type.equals("Kills")) {
                line = line.replace("<" + RANKS[i] + "Name>", name);
            }
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
        if (tie && !gameOverConfig.isTieFireworkEnabled()) {
            return;
        }
        for (GamePlayer gamePlayer : celebratingPlayers()) {
            Location location = gamePlayer.getPlayer().getLocation();
            FireWorkUtil.spawnFireWork(location.clone().add(0.0D, gameOverConfig.getFireworkHeight(), 0.0D), location.getWorld());
        }
    }
}
