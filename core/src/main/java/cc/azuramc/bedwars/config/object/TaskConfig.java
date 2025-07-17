package cc.azuramc.bedwars.config.object;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务配置类
 * 包含所有任务相关的配置项
 * @author an5w1r@163.com
 */
@Data
@EqualsAndHashCode
public class TaskConfig {

    private GeneratorConfig generator = new GeneratorConfig();
    private GameStartConfig gameStart = new GameStartConfig();
    private GameOverConfig gameOver = new GameOverConfig();

    /**
     * 资源生成器配置
     */
    @Data
    public static class GeneratorConfig {
        // 资源生成时间间隔(秒)
        private int ironSpawnInterval = 1;
        private int goldSpawnInterval = 4;
        private int diamondSpawnInterval = 30;
        private int emeraldSpawnInterval = 55;

        // 基础资源生成最大堆叠数量(一级)
        private int maxIronStackLevel1 = 48;
        private int maxGoldStackLevel1 = 8;
        private int maxDiamondStackLevel1 = 4;
        private int maxEmeraldStackLevel1 = 2;

        // 二级资源生成最大堆叠数量
        private int maxIronStackLevel2 = 48;
        private int maxGoldStackLevel2 = 8;
        private int maxDiamondStackLevel2 = 6;
        private int maxEmeraldStackLevel2 = 4;

        // 三级资源生成最大堆叠数量
        private int maxIronStackLevel3 = 64;
        private int maxGoldStackLevel3 = 12;
        private int maxDiamondStackLevel3 = 8;
        private int maxEmeraldStackLevel3 = 4;

        // 检测资源周围范围(方块)
        private double resourceCheckRadius = 3;

        // 盔甲架显示高度
        private float nameDisplayHeight = 6.0F;
        private float resourceTypeHeight = 5.0F;
        private float levelDisplayHeight = 4.0F;

        // 物品属性
        private double itemVelocityX = 0.0D;
        private double itemVelocityY = 0.1D;
        private double itemVelocityZ = 0.0D;
    }

    /**
     * 游戏开始倒计时配置
     */
    @Data
    public static class GameStartConfig {
        // 倒计时常量
        private Title title = new Title();
        private int defaultCountdown = 120;
        private int quickStartCountdown = 10;

        // 公告时间点
        private int[] announcementTimes = {60, 30, 5, 4, 3, 2, 1};

        // 标题显示配置
        @Data
        public static class Title {
            private int fadeIn = 1;
            private int titleStay = 20;
            private int fadeOut = 1;
        }
    }

    /**
     * 游戏结束倒计时配置
     */
    @Data
    public static class GameOverConfig {
        // 游戏结束倒计时时间(秒)
        private int defaultCountdown = 15;

        // 标题显示配置
        private int titleFadeIn = 0;
        private int titleStay = 40;
        private int titleFadeOut = 0;

        // 服务器关闭延迟(ticks)
        private long shutdownDelay = 40L;

        // 烟花高度
        private double fireworkHeight = 2.0D;
    }
} 