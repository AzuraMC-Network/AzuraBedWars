package cc.azuramc.bedwars.tablist;

import cc.azuramc.bedwars.game.*;
import cc.azuramc.bedwars.util.LuckPermsUtil;
import cc.azuramc.bedwars.util.MessageUtil;
import cc.azuramc.bedwars.util.VaultUtil;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
public class TabList {

    private static final HashMap<GamePlayer, Team> teamMap = new HashMap<>();
    /**
     * 存储每个玩家的固定随机数，用于TabList排序
     */
    private static final HashMap<GamePlayer, Integer> playerRandomNumbers = new HashMap<>();
    public static GameManager gameManager;
    /**
     * TabList自动更新任务
     */
    private static BukkitTask updateTask;

    @Getter
    private static String header = "";
    @Getter
    private static String footer = "";

    /**
     * 队伍颜色优先级排序 - 用于TabList显示顺序
     */
    private static final List<TeamColor> TEAM_PRIORITY_ORDER = Arrays.asList(
            TeamColor.RED,
            TeamColor.BLUE,
            TeamColor.GREEN,
            TeamColor.YELLOW,
            TeamColor.AQUA,
            TeamColor.WHITE,
            TeamColor.PINK,
            TeamColor.GRAY,
            TeamColor.ORANGE,
            TeamColor.MAGENTA,
            TeamColor.LIGHT_BLUE,
            TeamColor.LIME,
            TeamColor.PURPLE,
            TeamColor.CYAN,
            TeamColor.BLACK,
            TeamColor.BROWN
    );

    /**
     * 启动TabList自动更新
     *
     * @param plugin 插件实例
     */
    public static void startAutoUpdate(Plugin plugin, GameManager gameManager) {
        if (updateTask != null && !updateTask.isCancelled()) {
            return;
        }

        TabList.gameManager = gameManager;

        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    updateAllTabListNames();
                    updateHeaderFooter();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    public static void addToTab(GamePlayer gamePlayer) {
        Scoreboard scoreboard = gamePlayer.getPlayer().getScoreboard();

        String teamName = generateSortedTeamName(gamePlayer);
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }

        team.addEntry(gamePlayer.getName());

        teamMap.put(gamePlayer, team);

        updateTabList(gamePlayer);
        updateTag(gamePlayer, team);

        // 发送当前的Header和Footer给新加入的玩家
        sendCurrentHeaderFooter(gamePlayer.getPlayer());
    }

    /**
     * 为所有在线玩家更新TabList显示名称
     */
    public static void updateAllTabListNames() {
        for (GamePlayer gamePlayer : teamMap.keySet()) {
            Team team = teamMap.get(gamePlayer);
            String newTeamName = generateSortedTeamName(gamePlayer);

            // team名发生变化
            if (!team.getName().equals(newTeamName)) {

                // 注销之前的team
                team.removeEntry(gamePlayer.getName());
                team.unregister();

                // 注册新的team
                Team newTeam = gamePlayer.getPlayer().getScoreboard().registerNewTeam(newTeamName);
                teamMap.put(gamePlayer, newTeam);
                updateTabList(gamePlayer);
                updateTag(gamePlayer, newTeam);
            }
        }
        updateHeaderFooterByGameStateWithList();
    }

    /**
     * 使用ScoreboardAPI设置玩家tabList显示
     *
     * @param gamePlayer 游戏玩家
     */
    public static void updateTabList(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();
        GameTeam gameTeam = gamePlayer.getGameTeam();

        String displayName;

        if (gameManager.getGameState() == GameState.WAITING) {
            String prefix = "";
            if (LuckPermsUtil.isLoaded) {
                prefix = LuckPermsUtil.getPrefix(gamePlayer);
            } else if (!VaultUtil.chatIsNull) {
                prefix = VaultUtil.getPlayerPrefix(gamePlayer);
            }

            player.setPlayerListName(prefix + gamePlayer.getNickName());
            return;
        }

        if (gameManager.getGameState() == GameState.RUNNING || gameManager.getGameState() == GameState.ENDING) {
            if (gamePlayer.isRespawning()) {
                displayName = "&7" + gameTeam.getNameWithoutColor() + " | " + gamePlayer.getNickName();
            } else if (gamePlayer.isSpectator()) {
                displayName = "&7[旁观者] | " + gamePlayer.getNickName();
            } else {
                displayName = gameTeam.getName() + " | " + gamePlayer.getNickName();
            }
            player.setPlayerListName(MessageUtil.color(displayName));
        }
    }

    /**
     * 使用ScoreboardAPI设置玩家tag
     * @param gamePlayer 游戏玩家
     * @param team team
     */
    public static void updateTag(GamePlayer gamePlayer, Team team) {
        Player player = gamePlayer.getPlayer();
        GameTeam gameTeam = gamePlayer.getGameTeam();
        Scoreboard scoreboard = player.getScoreboard();

        String prefix = "";

        if (gameManager.getGameState() == GameState.WAITING) {
            if (LuckPermsUtil.isLoaded) {
                prefix = LuckPermsUtil.getPrefix(gamePlayer);
            } else if (!VaultUtil.chatIsNull) {
                prefix = VaultUtil.getPlayerPrefix(gamePlayer);
            }

            // 设置前缀和后缀
            team.setPrefix(MessageUtil.color(prefix));

            // 添加玩家到这个队伍
            team.addEntry(player.getName());

            player.setScoreboard(scoreboard);
            return;
        }

        if (gameManager.getGameState() == GameState.RUNNING || gameManager.getGameState() == GameState.ENDING) {
            if (gamePlayer.isInvisible()) {
                team.removeEntry(gamePlayer.getName());
                return;
            } else if (gamePlayer.isRespawning()) {
                prefix = "&7" + gameTeam.getNameWithoutColor() + " | ";
            } else if (gamePlayer.isSpectator()) {
                prefix = "&7[旁观者] | ";
            } else {
                prefix = gameTeam.getName() + " | ";
            }

            // 设置前缀和后缀
            team.setPrefix(MessageUtil.color(prefix));

            // 添加玩家到这个队伍
            team.addEntry(player.getName());

            player.setScoreboard(scoreboard);
        }
    }

    /**
     * 设置Header内容（支持列表）
     *
     * @param lines Header文本行列表，支持颜色代码和占位符
     */
    public static void setHeader(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            header = "";
        } else {
            header = String.join("\n", lines);
        }
    }

    /**
     * 设置Footer内容（支持列表）
     *
     * @param lines Footer文本行列表，支持颜色代码和占位符
     */
    public static void setFooter(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            footer = "";
        } else {
            footer = String.join("\n", lines);
        }
    }

    /**
     * 设置Header内容
     *
     * @param header Header文本，支持颜色代码和占位符
     */
    public static void setHeader(String header) {
        TabList.header = header != null ? header : "";
    }

    /**
     * 设置Footer内容
     *
     * @param footer Footer文本，支持颜色代码和占位符
     */
    public static void setFooter(String footer) {
        TabList.footer = footer != null ? footer : "";
    }

    /**
     * 更新所有在线玩家的Header和Footer
     */
    public static void updateHeaderFooter() {
        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            Player player = gamePlayer.getPlayer();
            if (player != null && player.isOnline()) {
                sendHeaderFooter(player, header, footer);
            }
        }
    }

    /**
     * 发送Header和Footer数据包给指定玩家
     *
     * @param player 目标玩家
     * @param header Header文本
     * @param footer Footer文本
     */
    public static void sendHeaderFooter(Player player, String header, String footer) {
        if (player == null || !player.isOnline()) {
            return;
        }

        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

        String processedHeader = MessageUtil.parse(player, header);
        String processedFooter = MessageUtil.parse(player, footer)
                .replace("<currentGameKill>", String.valueOf(gamePlayer.getCurrentGameKills()))
                .replace("<currentGameFinalKill>", String.valueOf(gamePlayer.getCurrentGameFinalKills()))
                .replace("<currentGameBedBreak>", String.valueOf(gamePlayer.getCurrentGameDestroyedBeds()));

        // 将String转换为Adventure Component
        Component headerComponent = Component.text(processedHeader);
        Component footerComponent = Component.text(processedFooter);

        // 创建数据包
        WrapperPlayServerPlayerListHeaderAndFooter packet = new WrapperPlayServerPlayerListHeaderAndFooter(headerComponent, footerComponent);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    /**
     * 发送当前设置的Header和Footer给指定玩家
     *
     * @param player 目标玩家
     */
    public static void sendCurrentHeaderFooter(Player player) {
        sendHeaderFooter(player, header, footer);
    }

    /**
     * 清除Header和Footer
     */
    public static void clearHeaderFooter() {
        header = "";
        footer = "";
        updateHeaderFooter();
    }

    /**
     * 生成有序的队伍名称，用于TabList排序
     * 格式: "sort#[队伍优先级][玩家状态][固定随机数]"
     *
     * @param gamePlayer 游戏玩家
     * @return 排序用的队伍名称
     */
    private static String generateSortedTeamName(GamePlayer gamePlayer) {
        StringBuilder teamName = new StringBuilder("sort#");

        // 添加玩家状态优先级 (0-9)
        int playerPriority = getPlayerPriority(gamePlayer);
        teamName.append(playerPriority);

        // 添加队伍优先级 (00-99)
        int teamPriority = getTeamPriority(gamePlayer.getGameTeam());
        teamName.append(String.format("%02d", teamPriority));

        // 添加固定随机数避免冲突 每个玩家只生成一次
        int randomNumber = playerRandomNumbers.computeIfAbsent(gamePlayer,
                k -> (int) (Math.random() * 1000));
        teamName.append(String.format("%03d", randomNumber));

        return teamName.toString();
    }

    /**
     * 获取队伍优先级
     *
     * @param gameTeam 游戏队伍
     * @return 优先级数字，数字越小优先级越高
     */
    private static int getTeamPriority(GameTeam gameTeam) {
        if (gameTeam == null) {
            return 99; // 无队伍玩家排在最后
        }

        TeamColor teamColor = gameTeam.getTeamColor();
        int index = TEAM_PRIORITY_ORDER.indexOf(teamColor);
        return index == -1 ? 50 : index; // 未知颜色排在中间
    }

    /**
     * 获取玩家优先级
     *
     * @param gamePlayer 游戏玩家
     * @return 优先级数字，数字越小优先级越高
     */
    private static int getPlayerPriority(GamePlayer gamePlayer) {
        if (gamePlayer == null) {
            return 9;
        }

        // 旁观者
        if (gamePlayer.isSpectator()) {
            return 8;
        }

        // 复活中
        if (gamePlayer.isRespawning()) {
            return 7;
        }

        // 隐身
        if (gamePlayer.isInvisible()) {
            return 6;
        }

        // 普通存活玩家
        return 0;
    }

    /**
     * 根据游戏阶段自动设置Header和Footer（使用列表方式）
     */
    public static void updateHeaderFooterByGameStateWithList() {
        if (gameManager == null) {
            return;
        }

        GameState gameState = gameManager.getGameState();

        switch (gameState) {
            case WAITING:
                // 使用列表方式设置多行Header
                List<String> waitingHeader = Arrays.asList(
                        "&b你正在 &eAzuraMC &b游玩起床战争",
                        ""
                );
                setHeader(waitingHeader);

                List<String> waitingFooter = Arrays.asList(
                        "",
                        "&bas.azuramc.cc"
                );
                setFooter(waitingFooter);
                break;

            case RUNNING:
                List<String> runningHeader = Arrays.asList(
                        "&b你正在 &eAzuraMC &b游玩起床战争",
                        ""
                );
                setHeader(runningHeader);

                List<String> runningFooter = Arrays.asList(
                        "",
                        "&b击杀数: &e<currentGameKill> &b最终击杀数: &e<currentGameFinalKill> &b破坏床数: &e<currentGameBedBreak>",
                        "&bas.azuramc.cc"
                );
                setFooter(runningFooter);
                break;

            case ENDING:
                GameTeam winner = gameManager.getWinner();
                String winnerName = winner != null ? winner.getName() : "无";
                List<String> endingHeader = Arrays.asList(
                        "&b你正在 &eAzuraMC &b游玩起床战争",
                        "&b游戏结束 &e" + winnerName + " &b获胜",
                        ""
                );
                setHeader(endingHeader);

                List<String> endingFooter = Arrays.asList(
                        "",
                        "&b击杀数: &e<currentGameKill> &b最终击杀数: &e<currentGameFinalKill> &b破坏床数: &e<currentGameBedBreak>",
                        "&bas.azuramc.cc"
                );
                setFooter(endingFooter);
                break;
        }
    }
}
