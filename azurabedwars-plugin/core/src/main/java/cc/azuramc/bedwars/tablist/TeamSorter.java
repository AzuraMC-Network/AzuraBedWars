package cc.azuramc.bedwars.tablist;

import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.TeamColor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 队伍排序类
 * 负责处理队伍优先级排序逻辑
 *
 * @author an5w1r@163.com
 */
public class TeamSorter {

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
     * 存储每个玩家的固定随机数，用于TabList排序
     */
    private final Map<GamePlayer, Integer> playerRandomNumbers = new HashMap<>();

    /**
     * 生成有序的队伍名称，用于TabList排序
     * 格式: "sort#[玩家状态][队伍优先级][固定随机数]"
     *
     * @param gamePlayer 游戏玩家
     * @return 排序用的队伍名称
     */
    public String generateSortedTeamName(GamePlayer gamePlayer) {
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
    private int getTeamPriority(GameTeam gameTeam) {
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
    private int getPlayerPriority(GamePlayer gamePlayer) {
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
     * 清除玩家的随机数缓存
     */
    public void clearPlayerRandomNumber(GamePlayer gamePlayer) {
        playerRandomNumbers.remove(gamePlayer);
    }

    /**
     * 清除所有玩家的随机数缓存
     */
    public void clearAllPlayerRandomNumbers() {
        playerRandomNumbers.clear();
    }
}
