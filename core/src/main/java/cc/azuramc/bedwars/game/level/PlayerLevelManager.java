package cc.azuramc.bedwars.game.level;

import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.game.GamePlayer;
import lombok.Getter;

import java.util.HashMap;

/**
 * @author an5w1r@163.com
 */
public class PlayerLevelManager {

    /**
     * 玩家等级与升级所需经验映射表
     */
    @Getter
    private static final HashMap<Integer, Integer> LEVEL_TOTAL_EXP = new HashMap<>();

    /** 最大等级 */
    private static final int MAX_LEVEL = 50;

    /**
     * 获取指定等级所需的经验值
     *
     * @param level 等级
     * @return 所需经验值，如果等级不存在返回-1
     */
    public static int getRequiredExperience(int level) {
        return LEVEL_TOTAL_EXP.getOrDefault(level, -1);
    }

    /**
     * 获取下一级所需的经验值
     *
     * @param currentLevel 当前等级
     * @return 下一级所需经验值，如果已是最高等级返回-1
     */
    public static int getNextLevelRequiredExperience(int currentLevel) {
        return LEVEL_TOTAL_EXP.getOrDefault(currentLevel + 1, -1);
    }

    /**
     * 根据当前等级内的经验值获取升级进度
     *
     * @param currentLevel    当前等级
     * @param currentLevelExp 当前等级内的经验值
     * @return 升级进度百分比（0.0 - 1.0）
     */
    public static double getLevelProgressByLevelExp(int currentLevel, double currentLevelExp) {
        if (currentLevel >= MAX_LEVEL) {
            return 1.0;
        }

        int expNeededForNextLevel = getExpRequiredForLevelUp(currentLevel);
        if (expNeededForNextLevel <= 0) {
            return 1.0;
        }

        return Math.min(1.0, Math.max(0.0, currentLevelExp / expNeededForNextLevel));
    }

    /**
     * 获取从当前等级升级到下一级需要的经验值
     *
     * @param currentLevel 当前等级
     * @return 升级所需经验值，如果已是最高等级返回-1
     */
    public static int getExpRequiredForLevelUp(int currentLevel) {
        if (currentLevel >= MAX_LEVEL) {
            return -1;
        }

        int currentLevelTotalExp = LEVEL_TOTAL_EXP.getOrDefault(currentLevel, 0);
        int nextLevelTotalExp = LEVEL_TOTAL_EXP.getOrDefault(currentLevel + 1, -1);

        if (nextLevelTotalExp == -1) {
            return -1;
        }

        return nextLevelTotalExp - currentLevelTotalExp;
    }

    /**
     * 为玩家增加经验值并自动处理升级
     *
     * @param gamePlayer 玩家
     * @param expToAdd 要增加的经验值
     * @return 升级的等级数（0表示没有升级，1表示升了1级，以此类推）
     */
    public static int addExperience(GamePlayer gamePlayer, double expToAdd) {
        if (gamePlayer == null || expToAdd <= 0) {
            return 0;
        }

        PlayerData playerData = gamePlayer.getPlayerData();
        if (playerData == null) {
            return 0;
        }

        // 记录升级前的等级
        int oldLevel = playerData.getLevel();

        // 增加经验值
        playerData.addExperience(expToAdd);
        double currentExp = playerData.getExperience();

        // 当前等级
        int currentLevel = oldLevel;
        int levelsGained = 0;

        // 循环检查是否可以升级（支持连续升级）
        while (currentLevel < MAX_LEVEL && currentExp > 0) {
            // 获取当前等级升级所需经验
            int expNeededForNextLevel = getExpRequiredForLevelUp(currentLevel);

            // 如果无法获取升级经验要求，或者当前经验不足，则停止升级
            if (expNeededForNextLevel == -1 || currentExp < expNeededForNextLevel) {
                break;
            }

            // 扣除升级消耗的经验
            currentExp -= expNeededForNextLevel;

            // 升级
            currentLevel++;
            levelsGained++;
        }

        // 更新玩家等级和剩余经验
        if (levelsGained > 0) {
            playerData.setLevel(currentLevel);
            // 设置升级后剩余的经验
            playerData.setExperience(currentExp);

            // 处理总体升级事件（如果升了多级）
            if (levelsGained > 1) {
                handleMultipleLevelUp(gamePlayer, oldLevel, currentLevel, levelsGained);
            } else {
                handleSingleLevelUp(gamePlayer, oldLevel, currentLevel);
            }
        }

        return levelsGained;
    }

    /**
     * 处理单次升级事件
     *
     * @param gamePlayer 玩家
     * @param oldLevel 旧等级
     * @param newLevel 新等级
     */
    private static void handleSingleLevelUp(GamePlayer gamePlayer, int oldLevel, int newLevel) {
        gamePlayer.getPlayer().sendMessage("§6恭喜！你从等级 " + oldLevel + " 升级到了等级 " + newLevel + "！");
    }

    /**
     * 处理多级升级事件
     *
     * @param gamePlayer 玩家
     * @param oldLevel 起始等级
     * @param newLevel 最终等级
     * @param levelsGained 升级的等级数
     */
    private static void handleMultipleLevelUp(GamePlayer gamePlayer, int oldLevel, int newLevel, int levelsGained) {
        gamePlayer.getPlayer().sendMessage("§6恭喜！你连续升级了 " + levelsGained + " 级！从等级 " + oldLevel + " 升级到了等级 " + newLevel + "！");
    }

    /**
     * 获取当前等级的进度百分比
     * 注意：由于改为每级独立经验系统，playerData.getExperience() 应该存储的是当前等级内的经验
     *
     * @param playerData 玩家数据
     * @return 当前等级进度百分比（0.0 - 1.0）
     */
    public static double getLevelProgress(PlayerData playerData) {
        if (playerData == null) return 0.0;

        int currentLevel = playerData.getLevel();
        double currentLevelExp = playerData.getExperience();

        return getLevelProgressByLevelExp(currentLevel, currentLevelExp);
    }

    /**
     * 获取升级到下一级还需要的经验值
     *
     * @param playerData 玩家数据
     * @return 还需要的经验值，如果已是最高等级返回0
     */
    public static double getExperienceToNextLevel(PlayerData playerData) {
        if (playerData == null) return 0.0;

        int currentLevel = playerData.getLevel();
        double currentLevelExp = playerData.getExperience();

        if (currentLevel >= MAX_LEVEL) {
            return 0.0;
        }

        int expNeededForNextLevel = getExpRequiredForLevelUp(currentLevel);
        if (expNeededForNextLevel <= 0) {
            return 0.0;
        }

        return Math.max(0.0, expNeededForNextLevel - currentLevelExp);
    }

    /**
     * 加载玩家等级经验表
     */
    public static void loadLevelData() {
        // 清理先前数据
        LEVEL_TOTAL_EXP.clear();

        // 等级1-10
        LEVEL_TOTAL_EXP.put(1, 5);
        LEVEL_TOTAL_EXP.put(2, 10);
        LEVEL_TOTAL_EXP.put(3, 25);
        LEVEL_TOTAL_EXP.put(4, 45);
        LEVEL_TOTAL_EXP.put(5, 100);
        LEVEL_TOTAL_EXP.put(6, 220);
        LEVEL_TOTAL_EXP.put(7, 450);
        LEVEL_TOTAL_EXP.put(8, 800);
        LEVEL_TOTAL_EXP.put(9, 900);
        LEVEL_TOTAL_EXP.put(10, 1050);

        // 等级11-20
        LEVEL_TOTAL_EXP.put(11, 1800);
        LEVEL_TOTAL_EXP.put(12, 2600);
        LEVEL_TOTAL_EXP.put(13, 3450);
        LEVEL_TOTAL_EXP.put(14, 4200);
        LEVEL_TOTAL_EXP.put(15, 5450);
        LEVEL_TOTAL_EXP.put(16, 6150);
        LEVEL_TOTAL_EXP.put(17, 6850);
        LEVEL_TOTAL_EXP.put(18, 7550);
        LEVEL_TOTAL_EXP.put(19, 8250);
        LEVEL_TOTAL_EXP.put(20, 8900);

        // 等级21-30
        LEVEL_TOTAL_EXP.put(21, 10000);
        LEVEL_TOTAL_EXP.put(22, 11250);
        LEVEL_TOTAL_EXP.put(23, 12500);
        LEVEL_TOTAL_EXP.put(24, 13750);
        LEVEL_TOTAL_EXP.put(25, 15000);
        LEVEL_TOTAL_EXP.put(26, 16250);
        LEVEL_TOTAL_EXP.put(27, 17500);
        LEVEL_TOTAL_EXP.put(28, 18750);
        LEVEL_TOTAL_EXP.put(29, 20000);
        LEVEL_TOTAL_EXP.put(30, 22000);

        // 等级31-40
        LEVEL_TOTAL_EXP.put(31, 24000);
        LEVEL_TOTAL_EXP.put(32, 26000);
        LEVEL_TOTAL_EXP.put(33, 28000);
        LEVEL_TOTAL_EXP.put(34, 30000);
        LEVEL_TOTAL_EXP.put(35, 32000);
        LEVEL_TOTAL_EXP.put(36, 34000);
        LEVEL_TOTAL_EXP.put(37, 36000);
        LEVEL_TOTAL_EXP.put(38, 38000);
        LEVEL_TOTAL_EXP.put(39, 40000);
        LEVEL_TOTAL_EXP.put(40, 45000);

        // 等级41-50
        LEVEL_TOTAL_EXP.put(41, 50000);
        LEVEL_TOTAL_EXP.put(42, 55000);
        LEVEL_TOTAL_EXP.put(43, 60000);
        LEVEL_TOTAL_EXP.put(44, 65000);
        LEVEL_TOTAL_EXP.put(45, 70000);
        LEVEL_TOTAL_EXP.put(46, 75000);
        LEVEL_TOTAL_EXP.put(47, 80000);
        LEVEL_TOTAL_EXP.put(48, 85000);
        LEVEL_TOTAL_EXP.put(49, 90000);
        LEVEL_TOTAL_EXP.put(50, 100000);
    }
}
