package cc.azuramc.bedwars.tablist;

import cc.azuramc.bedwars.game.*;
import cc.azuramc.bedwars.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

/**
 * @author an5w1r@163.com
 */
public class TabList {

    private static final HashMap<GamePlayer, Team> teamMap = new HashMap<>();
    public static GameManager gameManager;
    /**
     * TabList自动更新任务
     */
    private static BukkitTask updateTask;

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
            team.addEntry(gamePlayer.getName());
        }

        teamMap.put(gamePlayer, team);

        updateTag(gamePlayer, team);
    }

    /**
     * 清除所有注册的team
     */
    public static void cleanUpScoreBoard() {
        Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager(), "ScoreboardManager为空").getMainScoreboard();
        for (Team team : new ArrayList<>(scoreboard.getTeams())) {
            if (team.getName().startsWith("sort#")) {
                team.unregister();
            }
        }
    }

    /**
     * 为所有在线玩家更新TabList显示名称
     */
    public static void updateAllTabListNames() {

        if (gameManager.getGameState() == GameState.WAITING) {
            handleWaitingTabList();
            return;
        }

        if (gameManager.getGameState() == GameState.RUNNING) {
            handleGamingTabList();
            return;
        }

        if (gameManager.getGameState() == GameState.ENDING) {
            handleEndingTabList();
            return;
        }
    }

    private static void handleWaitingTabList() {
    }

    private static void handleGamingTabList() {
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
    }

    private static void handleEndingTabList() {
        handleGamingTabList();
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

        if (gameTeam == null) {
            return;
        }
        if (gamePlayer.isRespawning()) {
            displayName = "&7" + gameTeam.getName() + " | " + gamePlayer.getNickName();
        } else if (gamePlayer.isSpectator()) {
            displayName = "&7[旁观者] | " + gamePlayer.getNickName();
        } else {
            displayName = gameTeam.getChatColor() + gameTeam.getName() + " | " + gamePlayer.getNickName();
        }

        player.setPlayerListName(MessageUtil.color(displayName));
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

        if (gameTeam == null) {
            return;
        }

        String prefix;

        if (gamePlayer.isRespawning()) {
            prefix = "&7" + gameTeam.getName() + " | ";
        } else if (gamePlayer.isSpectator()) {
            prefix = "&7" + gameTeam.getName() + " | ";
        } else {
            prefix = gameTeam.getChatColor() + gameTeam.getName() + " | ";
        }

        // 设置前缀和后缀
        team.setPrefix(MessageUtil.color(prefix));

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
        int teamPriority = getTeamPriority(gamePlayer.getGameTeam());
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

        // 旁观者
        if (gamePlayer.isSpectator()) {
            return 8;
        }

        // 复活中
        if (gamePlayer.isRespawning()) {
            return 7;
        }

        // 普通存活玩家
        return 0;
    }

}
