package cc.azuramc.bedwars.game.level;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author an5w1r@163.com
 */
public class PlayerLevelMap {

    /** 玩家等级经验值映射表 */
    @Getter private static final HashMap<Integer, Integer> PLAYER_LEVEL = new HashMap<>();

    /**
     * 根据经验值获取玩家等级
     *
     * @param experience 玩家当前经验值
     * @return 玩家等级
     */
    public static int getLevel(int experience) {
        // 默认最低等级为1
        int level = 1;

        for (Map.Entry<Integer, Integer> entry : PLAYER_LEVEL.entrySet()) {
            if (experience >= entry.getValue()) {
                level = entry.getKey();
            } else {
                break;
            }
        }

        return level;
    }

    /**
     * 加载玩家等级经验表
     */
    public static void loadLevelData() {
        // 清理先前数据
        PLAYER_LEVEL.clear();

        // 等级1-10
        PLAYER_LEVEL.put(1, 0);
        PLAYER_LEVEL.put(2, 10);
        PLAYER_LEVEL.put(3, 25);
        PLAYER_LEVEL.put(4, 45);
        PLAYER_LEVEL.put(5, 100);
        PLAYER_LEVEL.put(6, 220);
        PLAYER_LEVEL.put(7, 450);
        PLAYER_LEVEL.put(8, 800);
        PLAYER_LEVEL.put(9, 900);
        PLAYER_LEVEL.put(10, 1050);

        // 等级11-20
        PLAYER_LEVEL.put(11, 1800);
        PLAYER_LEVEL.put(12, 2600);
        PLAYER_LEVEL.put(13, 3450);
        PLAYER_LEVEL.put(14, 4200);
        PLAYER_LEVEL.put(15, 5450);
        PLAYER_LEVEL.put(16, 6150);
        PLAYER_LEVEL.put(17, 6850);
        PLAYER_LEVEL.put(18, 7550);
        PLAYER_LEVEL.put(19, 8250);
        PLAYER_LEVEL.put(20, 8900);

        // 等级21-30
        PLAYER_LEVEL.put(21, 10000);
        PLAYER_LEVEL.put(22, 11250);
        PLAYER_LEVEL.put(23, 12500);
        PLAYER_LEVEL.put(24, 13750);
        PLAYER_LEVEL.put(25, 15000);
        PLAYER_LEVEL.put(26, 16250);
        PLAYER_LEVEL.put(27, 17500);
        PLAYER_LEVEL.put(28, 18750);
        PLAYER_LEVEL.put(29, 20000);
        PLAYER_LEVEL.put(30, 22000);

        // 等级31-40
        PLAYER_LEVEL.put(31, 24000);
        PLAYER_LEVEL.put(32, 26000);
        PLAYER_LEVEL.put(33, 28000);
        PLAYER_LEVEL.put(34, 30000);
        PLAYER_LEVEL.put(35, 32000);
        PLAYER_LEVEL.put(36, 34000);
        PLAYER_LEVEL.put(37, 36000);
        PLAYER_LEVEL.put(38, 38000);
        PLAYER_LEVEL.put(39, 40000);
        PLAYER_LEVEL.put(40, 45000);

        // 等级41-50
        PLAYER_LEVEL.put(41, 50000);
        PLAYER_LEVEL.put(42, 55000);
        PLAYER_LEVEL.put(43, 60000);
        PLAYER_LEVEL.put(44, 65000);
        PLAYER_LEVEL.put(45, 70000);
        PLAYER_LEVEL.put(46, 75000);
        PLAYER_LEVEL.put(47, 80000);
        PLAYER_LEVEL.put(48, 85000);
        PLAYER_LEVEL.put(49, 90000);
        PLAYER_LEVEL.put(50, 100000);
    }
}
