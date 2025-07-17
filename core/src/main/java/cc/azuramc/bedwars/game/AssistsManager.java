package cc.azuramc.bedwars.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 助攻跟踪系统
 * <p>
 * 负责记录和管理玩家的助攻信息，包括最后一次造成伤害的时间和获取助攻列表。
 * 可以自动清理过期的助攻记录，提高内存使用效率。
 * </p>
 * @author an5w1r@163.com
 */
public class AssistsManager {
    /**
     * 助攻有效时间（毫秒）
     * 玩家造成伤害后在此时间内仍然计为助攻
     */
    private static final long ASSIST_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(10);
    
    /**
     * 存储玩家最后伤害时间的映射
     * 键: 造成伤害的玩家
     * 值: 最后一次造成伤害的时间戳（毫秒）
     */
    private final Map<GamePlayer, Long> lastDamage = new HashMap<>();
    
    /**
     * 被跟踪助攻的玩家
     */
    private final GamePlayer targetPlayer;

    /**
     * 创建一个新的助攻跟踪器
     *
     * @param gamePlayer 被跟踪助攻的玩家
     */
    public AssistsManager(GamePlayer gamePlayer) {
        this.targetPlayer = gamePlayer;
    }

    /**
     * 设置玩家最后一次造成伤害的时间
     *
     * @param attacker 造成伤害的玩家
     * @param time 伤害发生的时间戳（毫秒）
     */
    public void setLastDamage(GamePlayer attacker, long time) {
        if (attacker == null) {
            return; // 忽略空玩家
        }
        
        // 不记录自己对自己的伤害
        if (attacker.equals(targetPlayer)) {
            return;
        }
        
        lastDamage.put(attacker, time);
    }

    /**
     * 获取当前有效的助攻玩家列表
     *
     * @param currentTime 当前时间戳（毫秒）
     * @return 有效的助攻玩家列表
     */
    public List<GamePlayer> getAssists(long currentTime) {
        List<GamePlayer> players = new ArrayList<>();
        for (Map.Entry<GamePlayer, Long> entry : lastDamage.entrySet()) {
            if (currentTime - entry.getValue() <= ASSIST_TIMEOUT_MS) {
                players.add(entry.getKey());
            }
        }
        return players;
    }
    
    /**
     * 获取当前有效的助攻玩家列表（使用系统当前时间）
     *
     * @return 有效的助攻玩家列表
     */
    public List<GamePlayer> getAssists() {
        return getAssists(System.currentTimeMillis());
    }
    
    /**
     * 使用Stream API获取助攻列表（替代实现）
     *
     * @param currentTime 当前时间戳（毫秒）
     * @return 有效的助攻玩家列表
     */
    public List<GamePlayer> getAssistsStream(long currentTime) {
        return lastDamage.entrySet().stream()
                .filter(entry -> currentTime - entry.getValue() <= ASSIST_TIMEOUT_MS)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    /**
     * 清理过期的助攻记录
     *
     * @param currentTime 当前时间戳（毫秒）
     * @return 清理的记录数量
     */
    public int cleanupExpiredAssists(long currentTime) {
        int initialSize = lastDamage.size();
        lastDamage.entrySet().removeIf(entry -> currentTime - entry.getValue() > ASSIST_TIMEOUT_MS);
        return initialSize - lastDamage.size();
    }
    
    /**
     * 清理过期的助攻记录（使用系统当前时间）
     *
     * @return 清理的记录数量
     */
    public int cleanupExpiredAssists() {
        return cleanupExpiredAssists(System.currentTimeMillis());
    }
    
    /**
     * 获取跟踪的助攻数量
     *
     * @return 当前记录的助攻数量（包括过期的）
     */
    public int size() {
        return lastDamage.size();
    }
    
    /**
     * 清空所有助攻记录
     */
    public void clear() {
        lastDamage.clear();
    }
}
