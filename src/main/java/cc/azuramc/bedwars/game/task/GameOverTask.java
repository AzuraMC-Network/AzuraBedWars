package cc.azuramc.bedwars.game.task;

import cc.azuramc.bedwars.config.object.TaskConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.team.GameTeam;
import cc.azuramc.bedwars.listener.player.PlayerAFKListener;
import cc.azuramc.bedwars.util.FireWorkUtil;
import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.listener.world.ChunkListener;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 游戏结束倒计时
 * <p>
 * 处理游戏结束后的逻辑，包括胜利消息显示、烟花效果、排行榜展示和服务器重启
 * </p>
 */
public class GameOverTask extends BukkitRunnable {

    private static final TaskConfig.GameOverConfig config = AzuraBedWars.getInstance().getTaskConfig().getGameOver();

    // 游戏结束倒计时时间(秒)
    private static final int DEFAULT_COUNTDOWN = config.getDefaultCountdown();
    
    // 标题显示配置
    private static final int TITLE_FADE_IN = config.getTitleFadeIn();
    private static final int TITLE_STAY = config.getTitleStay();
    private static final int TITLE_FADE_OUT = config.getTitleFadeOut();
    
    // 服务器关闭延迟(ticks)
    private static final long SHUTDOWN_DELAY = config.getShutdownDelay();
    
    // 烟花高度
    private static final double FIREWORK_HEIGHT = config.getFireworkHeight();
    
    // 胜利/失败消息
    private static final String VICTORY_TITLE = config.getVictoryTitle();
    private static final String VICTORY_SUBTITLE = config.getVictorySubtitle();
    private static final String DEFEAT_TITLE = config.getDefeatTitle();
    private static final String DEFEAT_SUBTITLE = config.getDefeatSubtitle();
    
    // 排行榜标题及分隔线
    private static final String[] lead = config.getLead();
    private static final String SEPARATOR_LINE = config.getSeparatorLine();
    private static final String GAME_TITLE = config.getGameTitle();
    private static final String WINNERS_PREFIX = config.getWinnersPrefix();
    private static final String NO_WINNER = config.getNoWinner();
    private static final String RANK_PREFIX = config.getRankPrefix();
    
    private final GameManager gameManager;
    private int countdown = DEFAULT_COUNTDOWN;
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
                gameManager.broadcastTeamTitle(team, TITLE_FADE_IN, TITLE_STAY, TITLE_FADE_OUT,
                                       VICTORY_TITLE, VICTORY_SUBTITLE);
            } else {
                gameManager.broadcastTeamTitle(team, TITLE_FADE_IN, TITLE_STAY, TITLE_FADE_OUT,
                                       DEFEAT_TITLE, DEFEAT_SUBTITLE);
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
            winnerText.append(NO_WINNER);
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
        messages.add(SEPARATOR_LINE);
        messages.add(GAME_TITLE);
        messages.add(" ");
        messages.add(WINNERS_PREFIX + winnerText);
        messages.add(" ");
        
        // 添加击杀排行
        int i = 0;
        for (GamePlayer gamePlayer : GamePlayer.sortFinalKills()) {
            if (i > 2) {
                continue;
            }
            messages.add(RANK_PREFIX + lead[i] + " §7- " + gamePlayer.getNickName() + " - " + gamePlayer.getFinalKills());
            i++;
        }
        
        // 添加底部分隔线
        messages.add(" ");
        messages.add(SEPARATOR_LINE);
        
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
                gamePlayer.getPlayerProfile().addWins();
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
            winner.getAlivePlayers().forEach(gamePlayer -> {
                FireWorkUtil.spawnFireWork(
                    gamePlayer.getPlayer().getLocation().add(0.0D, FIREWORK_HEIGHT, 0.0D),
                        Objects.requireNonNull(gamePlayer.getPlayer().getLocation().getWorld())
                );
            });
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
        }.runTaskLater(AzuraBedWars.getInstance(), SHUTDOWN_DELAY);
    }
}
