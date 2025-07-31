package cc.azuramc.bedwars.tablist;

import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.TeamColor;
import cc.azuramc.bedwars.util.LuckPermsUtil;
import cc.azuramc.bedwars.util.MessageUtil;
import cc.azuramc.bedwars.util.VaultUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TabList管理类
 * 负责管理游戏中的TabList显示，包括队伍排序、玩家排序等功能
 *
 * @author an5w1r@163.com
 */
public class TabList {

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
     * 清除所有注册的team
     */
    public static void cleanUpScoreBoard() {
        Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager(), "ScoreboardManager为空").getMainScoreboard();
        for (Team team : new ArrayList<>(scoreboard.getTeams())) {
            if (team.getName().startsWith("tag#") || team.getName().startsWith("sort#")) {
                team.unregister();
            }
        }
    }

    /**
     * 编辑玩家的TabList显示名称
     * 如果玩家有队伍，显示格式为 "队伍颜色队伍名 | 玩家名"
     * 如果玩家没有队伍，只显示玩家名
     *
     * @param gamePlayer 目标玩家
     */
    public static void changeTabListNameInGame(GamePlayer gamePlayer) {
        if (gamePlayer == null) {
            // 如果GamePlayer为空，使用默认显示名
            gamePlayer.getPlayer().setPlayerListName(gamePlayer.getName());
            return;
        }

        String displayName;

        // 检查玩家是否为旁观者
        if (gamePlayer.isSpectator()) {
            displayName = "§7[旁观者] " + gamePlayer.getNickName();
            setPlayerNameTag(gamePlayer, "§7[旁观者] ", "");
        } else {
            GameTeam gameTeam = gamePlayer.getGameTeam();

            if (gameTeam != null) {
                displayName = gameTeam.getChatColor() + gameTeam.getName() + " | " + gamePlayer.getNickName();
                setPlayerNameTag(gamePlayer, gameTeam.getChatColor().toString(), gameTeam.getChatColor().toString());
            } else {
                // 如果玩家没有队伍，只显示玩家名
                displayName = "§7" + gamePlayer.getNickName();
            }
        }

        gamePlayer.getPlayer().setPlayerListName(displayName);
    }

    public static void changeTabListNameWhenWaiting(GamePlayer gamePlayer) {
        if (gamePlayer == null) {
            // 如果GamePlayer为空，使用默认显示名
            gamePlayer.getPlayer().setPlayerListName(gamePlayer.getName());
            return;
        }

        String prefix = "";

        if (LuckPermsUtil.isLoaded) {
            prefix = LuckPermsUtil.getPrefix(gamePlayer);
        } else if (!VaultUtil.chatIsNull) {
            prefix = VaultUtil.getPlayerPrefix(gamePlayer);
        }

        gamePlayer.getPlayer().setPlayerListName(prefix + gamePlayer.getNickName());
    }

    /**
     * 为所有在线玩家更新TabList显示名称
     */
    public static void updateAllTabListNames() {
        updateTabListSorting();
    }

    /**
     * 为指定队伍的所有玩家更新TabList显示名称
     *
     * @param gameTeam 目标队伍
     */
    public static void updateTeamTabListNames(GameTeam gameTeam) {
        if (gameTeam == null) {
            return;
        }

        // 获取排序后的玩家列表并更新
        List<GamePlayer> sortedPlayers = getSortedPlayersInTeam(gameTeam);
        for (GamePlayer gamePlayer : sortedPlayers) {
            if (gamePlayer.getPlayer() != null) {
                changeTabListNameInGame(gamePlayer);
            }
        }
    }

    /**
     * 重置玩家的TabList显示名称为默认名称
     *
     * @param gamePlayer 目标玩家
     */
    public static void resetTabListName(GamePlayer gamePlayer) {
        if (gamePlayer != null) {
            gamePlayer.getPlayer().setPlayerListName(gamePlayer.getName());
        }
    }

    /**
     * 使用ScoreboardAPI设置玩家tag
     * @param gamePlayer 游戏玩家
     * @param prefix 前缀
     * @param suffix 后缀
     */
    public static void setPlayerNameTag(GamePlayer gamePlayer, String prefix, String suffix) {
        Player player = gamePlayer.getPlayer();
        Scoreboard scoreboard = player.getScoreboard();

        // 移除玩家之前的队伍
        removePlayerFromAllTeams(player, scoreboard);

        // 生成有序的队伍名称
        String teamName = generateSortedTeamName(gamePlayer);
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }

        // 设置前缀和后缀
        team.setPrefix(MessageUtil.color(prefix));
        team.setSuffix(MessageUtil.color(suffix));

        // 添加玩家到这个队伍
        team.addEntry(player.getName());

        player.setScoreboard(scoreboard);
    }

    /**
     * 生成有序的队伍名称，用于TabList排序
     * 格式: "sort#[队伍优先级][玩家状态][随机数]"
     *
     * @param gamePlayer 游戏玩家
     * @return 排序用的队伍名称
     */
    private static String generateSortedTeamName(GamePlayer gamePlayer) {
        StringBuilder teamName = new StringBuilder("sort#");

        // 添加队伍优先级 (00-99)
        int teamPriority;
        if (gamePlayer.isSpectator()) {
            // 旁观者使用特殊的优先级，排在最后
            teamPriority = 98;
        } else {
            teamPriority = getTeamPriority(gamePlayer.getGameTeam());
        }
        teamName.append(String.format("%02d", teamPriority));

        // 添加玩家状态优先级 (0-9)
        int playerPriority = getPlayerPriority(gamePlayer);
        teamName.append(playerPriority);

        // 添加随机数避免冲突
        teamName.append(String.format("%03d", (int) (Math.random() * 1000)));

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

        // 旁观者优先级最低，排在最后
        if (gamePlayer.isSpectator()) {
            return 8; // 旁观者
        }

        // 可以根据需要添加更多优先级规则
        // 例如：VIP玩家、队长等
        return 0; // 普通存活玩家
    }

    /**
     * 移除玩家从所有Scoreboard队伍中
     *
     * @param player     玩家
     * @param scoreboard 计分板
     */
    private static void removePlayerFromAllTeams(Player player, Scoreboard scoreboard) {
        for (Team team : scoreboard.getTeams()) {
            if (team.hasEntry(player.getName())) {
                team.removeEntry(player.getName());
                // 如果队伍为空且是我们创建的，则删除它
                if (team.getEntries().isEmpty() &&
                        (team.getName().startsWith("tag#") || team.getName().startsWith("sort#"))) {
                    team.unregister();
                }
            }
        }
    }

    /**
     * 按队伍分组获取所有在线玩家
     *
     * @return 按队伍分组的玩家列表
     */
    public static Map<GameTeam, List<GamePlayer>> getPlayersGroupedByTeam() {
        return GamePlayer.getOnlinePlayers().stream()
                .filter(gamePlayer -> !gamePlayer.isSpectator()) // 排除旁观者
                .collect(Collectors.groupingBy(
                        gamePlayer -> gamePlayer.getGameTeam() != null ? gamePlayer.getGameTeam() : null,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    /**
     * 获取排序后的队伍列表
     *
     * @param teams 队伍集合
     * @return 排序后的队伍列表
     */
    public static List<GameTeam> getSortedTeams(Collection<GameTeam> teams) {
        return teams.stream()
                .filter(Objects::nonNull)
                .sorted((team1, team2) -> {
                    int priority1 = getTeamPriority(team1);
                    int priority2 = getTeamPriority(team2);
                    return Integer.compare(priority1, priority2);
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取队伍内排序后的玩家列表
     *
     * @param gameTeam 游戏队伍
     * @return 排序后的玩家列表
     */
    public static List<GamePlayer> getSortedPlayersInTeam(GameTeam gameTeam) {
        if (gameTeam == null) {
            return new ArrayList<>();
        }

        return gameTeam.getGamePlayers().stream()
                .sorted((player1, player2) -> {
                    int priority1 = getPlayerPriority(player1);
                    int priority2 = getPlayerPriority(player2);

                    if (priority1 != priority2) {
                        return Integer.compare(priority1, priority2);
                    }

                    // 同优先级按名称排序
                    return player1.getName().compareToIgnoreCase(player2.getName());
                })
                .collect(Collectors.toList());
    }

    /**
     * 更新所有玩家的TabList排序
     */
    public static void updateTabListSorting() {
        // 清理旧的排序队伍
        cleanUpSortingTeams();

        // 重新设置所有玩家的排序
        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            if (gamePlayer.getPlayer() != null) {
                changeTabListNameInGame(gamePlayer);
            }
        }
    }

    /**
     * 清理排序用的队伍
     */
    private static void cleanUpSortingTeams() {
        Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager(), "ScoreboardManager为空").getMainScoreboard();
        for (Team team : new ArrayList<>(scoreboard.getTeams())) {
            if (team.getName().startsWith("sort#")) {
                team.unregister();
            }
        }
    }

    /**
     * 获取队伍统计信息用于TabList显示
     *
     * @param gameTeam 游戏队伍
     * @return 队伍统计信息字符串
     */
    public static String getTeamStatsForTabList(GameTeam gameTeam) {
        if (gameTeam == null) {
            return "";
        }

        List<GamePlayer> players = gameTeam.getGamePlayers();
        long aliveCount = players.stream().filter(p -> !p.isSpectator()).count();
        int totalCount = players.size();

        String bedStatus = gameTeam.isHasBed() ? "§a✓" : "§c✗";

        return String.format("§7[%s §7%d/%d]", bedStatus, aliveCount, totalCount);
    }



    private static String random6Digits() {
        int number = (int) (Math.random() * 1_000_000); // 0 ~ 999999
        return String.format("%06d", number); // 补足前导0
    }

}
