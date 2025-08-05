package cc.azuramc.bedwars.game.task;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.EventSettingsConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.listener.player.PlayerAFKListener;
import cc.azuramc.bedwars.listener.world.ChunkListener;
import cc.azuramc.bedwars.util.FireWorkUtil;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 游戏结束倒计时
 * 处理游戏结束后的逻辑，包括胜利消息显示、烟花效果、排行榜展示和服务器重启
 *
 * @author an5w1r@163.com
 */
public class GameOverTask extends BukkitRunnable {

    private static final EventSettingsConfig.GameOverEvent gameOverConfig = AzuraBedWars.getInstance().getEventSettingsConfig().getGameOverEvent();

    private final GameManager gameManager;
    private int countdown = gameOverConfig.getDefaultCountdown();
    private boolean isFirstRun = true;

    /**
     * 创建游戏结束倒计时
     *
     * @param gameManager 游戏实例
     */
    public GameOverTask(GameManager gameManager) {
        this.gameManager = gameManager;
        this.runTaskTimer(AzuraBedWars.getInstance(), 0L, 20L);
    }

    @Override
    public void run() {
        GameTeam winner = gameManager.getWinner();

        if (countdown > 0) {
            // 首次运行时显示胜利消息和排行榜
            if (isFirstRun) {
                displayGameResults(winner);

                // 首次运行时取消挂机状态检测任务
                PlayerAFKListener.stop();
                isFirstRun = false;
            }

            // 为胜利队伍玩家播放烟花效果
            spawnVictoryFireworks(winner);
        }

        // 倒计时结束，执行清理和关闭服务器
        if (countdown == 0) {
            performShutdown();
        }

        --countdown;
    }

    /**
     * 显示游戏结果，包括队伍标题和排行榜
     *
     * @param winner 胜利队伍
     */
    private void displayGameResults(GameTeam winner) {
        // 为每个队伍显示胜利或失败标题
        displayTeamTitles(winner);

        // 构建胜利者文本
        String winnerText = buildWinnerText(winner);

        // 生成并显示排行榜
        List<String> messages = generateLeaderboard(winnerText);
        for (String line : messages) {
            Bukkit.broadcastMessage(line);
        }

        // 增加胜利队伍玩家的胜利次数
        updatePlayerStats(winner);
    }

    /**
     * 为每个队伍显示胜利或失败标题
     *
     * @param winner 胜利队伍
     */
    private void displayTeamTitles(GameTeam winner) {
        gameManager.getGameTeams().forEach(team -> {
            boolean isWinner = winner != null && winner.getName().equals(team.getName());
            if (isWinner) {
                gameManager.broadcastTeamTitle(team, gameOverConfig.getVictoryTitle(), gameOverConfig.getVictorySubtitle(),
                        gameOverConfig.getTitleFadeIn(), gameOverConfig.getTitleStay(), gameOverConfig.getTitleFadeOut()
                );
            } else {
                gameManager.broadcastTeamTitle(team, gameOverConfig.getDefeatTitle(), gameOverConfig.getDefeatSubtitle(),
                        gameOverConfig.getTitleFadeIn(), gameOverConfig.getTitleStay(), gameOverConfig.getTitleFadeOut()
                );
            }
        });
    }

    /**
     * 构建胜利者文本
     *
     * @param winner 胜利队伍
     * @return 胜利者文本
     */
    private String buildWinnerText(GameTeam winner) {
        StringBuilder winnerText = new StringBuilder();

        if (winner != null) {
            // 添加所有胜利队伍玩家名称
            boolean isFirst = true;
            for (GamePlayer gamePlayer : winner.getGamePlayers()) {
                if (!isFirst) {
                    winnerText.append(", ");
                }
                winnerText.append(gamePlayer.getNickName());
                isFirst = false;
            }
        } else {
            winnerText.append(gameOverConfig.getNoWinner());
        }

        return winnerText.toString();
    }

    /**
     * 生成排行榜消息
     *
     * @param winnerText 胜利者文本
     * @return 排行榜消息列表
     */
    private List<String> generateLeaderboard(String winnerText) {
        List<String> messages = new ArrayList<>();

        // 添加标题和分隔线
        messages.add(gameOverConfig.getSeparatorLine());
        messages.add(gameOverConfig.getGameTitle());
        messages.add(" ");
        messages.add(gameOverConfig.getWinnersPrefix() + winnerText);
        messages.add(" ");

        // 添加击杀排行
        int i = 0;
        for (GamePlayer gamePlayer : GamePlayer.sortCurrentGameFinalKills()) {
            if (i > 2) {
                continue;
            }
            messages.add(gameOverConfig.getRankPrefix() + gameOverConfig.getLead()[i] + " §7- " + gamePlayer.getNickName() + " - " + gamePlayer.getCurrentGameFinalKills());
            i++;
        }

        // 添加底部分隔线
        messages.add(" ");
        messages.add(gameOverConfig.getSeparatorLine());

        return messages;
    }

    /**
     * 更新玩家统计数据
     *
     * @param winner 胜利队伍
     */
    private void updatePlayerStats(GameTeam winner) {
        if (winner != null) {
            for (GamePlayer gamePlayer : winner.getAlivePlayers()) {
                gamePlayer.getPlayerData().addWins();
            }
        }
    }

    /**
     * 为胜利队伍玩家生成烟花
     *
     * @param winner 胜利队伍
     */
    private void spawnVictoryFireworks(GameTeam winner) {
        if (winner != null) {
            winner.getAlivePlayers().forEach(gamePlayer -> FireWorkUtil.spawnFireWork(
                    gamePlayer.getPlayer().getLocation().add(0.0D, gameOverConfig.getFireworkHeight(), 0.0D),
                    Objects.requireNonNull(gamePlayer.getPlayer().getLocation().getWorld())
            ));
        }
    }

    /**
     * 执行服务器关闭前的清理工作
     */
    private void performShutdown() {
        // 释放强制加载的区块
        ChunkListener.releaseForceLoadedChunks();

        // 取消当前任务
        cancel();

        // 延迟关闭服务器
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.shutdown();
            }
        }.runTaskLater(AzuraBedWars.getInstance(), gameOverConfig.getShutdownDelay());
    }
}
