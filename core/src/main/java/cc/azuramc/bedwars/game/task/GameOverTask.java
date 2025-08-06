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

    private boolean isTie(GameTeam winner) {
        if (winner == null) {
            long aliveTeams = gameManager.getGameTeams().stream()
                    .filter(team -> !team.isDead())
                    .count();
            return aliveTeams > 1;
        }
        return false;
    }

    /**
     * 为每个队伍显示胜利或失败标题
     *
     * @param winner 胜利队伍
     */
    private void displayTeamTitles(GameTeam winner) {
        boolean isTie = isTie(winner);

        gameManager.getGameTeams().forEach(team -> {
            if (isTie) {
                if (!team.isDead()) {
                    gameManager.broadcastTeamTitle(team, gameOverConfig.getTieTitle(), gameOverConfig.getTieSubtitle(),
                            gameOverConfig.getTitleFadeIn(), gameOverConfig.getTitleStay(), gameOverConfig.getTitleFadeOut()
                    );
                } else {
                    gameManager.broadcastTeamTitle(team, gameOverConfig.getDefeatTitle(), gameOverConfig.getDefeatSubtitle(),
                            gameOverConfig.getTitleFadeIn(), gameOverConfig.getTitleStay(), gameOverConfig.getTitleFadeOut()
                    );
                }
            } else {
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


        // 使用自定义排行榜配置
        List<String> customMessages = gameOverConfig.getCustomLeaderboardMessages();
        if (customMessages != null && !customMessages.isEmpty()) {
            for (String message : customMessages) {
                String processedMessage = processLeaderboardPlaceholders(message, winnerText);
                if (!processedMessage.trim().isEmpty()) {
                    messages.add(processedMessage);
                }
            }
        }
        return messages;
    }

    /**
     * 处理排行榜占位符
     *
     * @param message    原始消息
     * @param winnerText 胜利者文本
     * @return 处理后的消息
     */
    private String processLeaderboardPlaceholders(String message, String winnerText) {
        if (message == null) {
            return "";
        }

        String processedMessage = message;

        // 处理胜利者占位符
        processedMessage = processedMessage.replace("<winnerFormat>", winnerText);

        // 获取排行榜数据
        List<GamePlayer> killRanking = GamePlayer.sortCurrentGameFinalKills();
        List<GamePlayer> assistRanking = GamePlayer.sortCurrentGameAssists();
        List<GamePlayer> bedBreakRanking = GamePlayer.sortCurrentGameBedBreaks();

        // 处理击杀排行占位符
        processedMessage = processRankingPlaceholders(processedMessage, killRanking, "Kills");

        // 处理助攻排行占位符
        processedMessage = processRankingPlaceholders(processedMessage, assistRanking, "Assists");

        // 处理拆床排行占位符
        processedMessage = processRankingPlaceholders(processedMessage, bedBreakRanking, "BedBreaks");

        return processedMessage;
    }

    /**
     * 处理排行榜占位符的通用方法
     *
     * @param message 原始消息
     * @param ranking 排行榜数据
     * @param type    排行类型（Kills/Assists/BedBreaks）
     * @return 处理后的消息
     */
    private String processRankingPlaceholders(String message, List<GamePlayer> ranking, String type) {
        String processedMessage = message;

        // 处理前三名占位符
        for (int i = 0; i < Math.min(3, ranking.size()); i++) {
            GamePlayer player = ranking.get(i);
            String rank = getRankName(i);

            // 替换名称占位符
            processedMessage = processedMessage.replace("<" + rank + "Name>", player.getNickName());

            // 替换数值占位符
            int value = getPlayerValue(player, type);
            processedMessage = processedMessage.replace("<" + rank + type + ">", String.valueOf(value));
        }

        // 如果排行榜不足3人，将剩余占位符替换为空字符串
        for (int i = ranking.size(); i < 3; i++) {
            String rank = getRankName(i);
            processedMessage = processedMessage.replace("<" + rank + "Name>", "");
            processedMessage = processedMessage.replace("<" + rank + type + ">", "0");
        }

        return processedMessage;
    }

    /**
     * 获取排名名称
     *
     * @param index 排名索引（0-2）
     * @return 排名名称
     */
    private String getRankName(int index) {
        return switch (index) {
            case 0 -> "first";
            case 1 -> "second";
            case 2 -> "third";
            default -> "first";
        };
    }

    /**
     * 获取玩家指定类型的数值
     *
     * @param player 玩家
     * @param type   类型
     * @return 数值
     */
    private int getPlayerValue(GamePlayer player, String type) {
        return switch (type) {
            case "Kills" -> player.getCurrentGameFinalKills();
            case "Assists" -> player.getCurrentGameAssists();
            case "BedBreaks" -> player.getCurrentGameDestroyedBeds();
            default -> 0;
        };
    }

    /**
     * 更新玩家统计数据
     *
     * @param winner 胜利队伍
     */
    private void updatePlayerStats(GameTeam winner) {
        if (isTie(winner)) {
            gameManager.getGameTeams().stream()
                    .filter(team -> !team.isDead())
                    .flatMap(team -> team.getAlivePlayers().stream());
            // maybe we need to add a addTies method?
//                    .forEach(gamePlayer -> gamePlayer.getPlayerData().addTies());
        } else if (winner != null) {
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
        if (isTie(winner)) {
            if (gameOverConfig.isTieFireworkEnabled()) {
                gameManager.getGameTeams().stream()
                        .filter(team -> !team.isDead())
                        .flatMap(team -> team.getAlivePlayers().stream())
                        .forEach(gamePlayer -> FireWorkUtil.spawnFireWork(
                                gamePlayer.getPlayer().getLocation().add(0.0D, gameOverConfig.getFireworkHeight(), 0.0D),
                                Objects.requireNonNull(gamePlayer.getPlayer().getLocation().getWorld())
                        ));
            }
        } else if (winner != null) {
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
