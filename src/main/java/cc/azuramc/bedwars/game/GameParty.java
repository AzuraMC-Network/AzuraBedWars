package cc.azuramc.bedwars.game;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 游戏队伍管理类
 * 负责管理玩家小队，包括队长、队员以及与游戏的关联关系
 *
 * @author an5w1r@163.com
 */
public class GameParty {

    private final GameManager gameManager;

    @Getter
    private final GamePlayer leader;

    @Getter
    private final List<GamePlayer> players;

    /**
     * 创建一个游戏队伍
     *
     * @param gameManager 关联的游戏实例
     * @param leader 队长
     * @param initialPlayers 初始队伍成员（不包含队长）
     */
    public GameParty(GameManager gameManager, GamePlayer leader, List<GamePlayer> initialPlayers) {
        this.gameManager = Objects.requireNonNull(gameManager, "游戏实例不能为空");
        this.leader = Objects.requireNonNull(leader, "队长不能为空");
        
        // 初始化成员列表
        this.players = initialPlayers != null ? initialPlayers : new ArrayList<>();
        
        // 确保队长在成员列表中
        if (!this.players.contains(leader)) {
            this.players.add(leader);
        }
        
        // 将队伍添加到游戏中
        gameManager.addParty(this);
    }

    /**
     * 判断指定玩家是否为队长
     *
     * @param player 需要判断的玩家
     * @return 如果是队长返回true，否则返回false
     */
    public boolean isLeader(GamePlayer player) {
        return Objects.equals(player, leader);
    }

    /**
     * 判断指定玩家是否在队伍中
     *
     * @param player 需要判断的玩家
     * @return 如果在队伍中返回true，否则返回false
     */
    public boolean isInTeam(GamePlayer player) {
        return players.contains(player);
    }

    /**
     * 从队伍中移除玩家
     * <p>
     * 如果队伍变为空，会自动从游戏中移除该队伍
     * </p>
     *
     * @param player 需要移除的玩家
     * @return 如果玩家成功被移除返回true，如果玩家不在队伍中返回false
     */
    public boolean removePlayer(GamePlayer player) {
        if (!isInTeam(player)) {
            return false;
        }
        
        boolean removed = players.remove(player);
        
        // 如果队伍为空，从游戏中移除
        if (removed && players.isEmpty()) {
            gameManager.removeParty(this);
        }
        
        return removed;
    }
    
    /**
     * 添加玩家到队伍
     *
     * @param player 需要添加的玩家
     * @return 如果添加成功返回true，如果玩家已在队伍中返回false
     */
    public boolean addPlayer(GamePlayer player) {
        if (player == null || isInTeam(player)) {
            return false;
        }
        
        return players.add(player);
    }
    
    /**
     * 获取队伍人数
     *
     * @return 队伍当前成员数量
     */
    public int getSize() {
        return players.size();
    }
    
    /**
     * 判断队伍是否为空
     *
     * @return 如果队伍没有成员返回true，否则返回false
     */
    public boolean isEmpty() {
        return players.isEmpty();
    }
}
