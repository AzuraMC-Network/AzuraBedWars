package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.util.ChatColorUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务配置类
 * 包含所有任务相关的配置项
 */
@Data
@EqualsAndHashCode
public class TaskConfig {

    // 资源生成器配置
    private final GeneratorConfig generator;

    // 游戏开始倒计时配置
    private final GameStartConfig gameStart;

    // 游戏结束倒计时配置
    private final GameOverConfig gameOver;

    /**
     * 资源生成器配置
     */
    @Data
    public static class GeneratorConfig {
        // 资源生成时间间隔(秒)
        private int ironSpawnInterval = 2;
        private int goldSpawnInterval = 6;
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

        // 资源名称
        private String ironGeneratorName = ChatColorUtil.color("铁刷新");
        private String goldGeneratorName = ChatColorUtil.color("金刷新");
        private String diamondGeneratorName = ChatColorUtil.color("钻石刷新");
        private String diamondTimeDisplay = ChatColorUtil.color("钻石时间显示");
        private String emeraldGeneratorName = ChatColorUtil.color("绿宝石刷新");
        private String emeraldTimeDisplay = ChatColorUtil.color("绿宝石时间显示");

        // 显示文本
        private String timeRemainingFormat = ChatColorUtil.color("§e将在§c%d§e秒后刷新");
        private String diamondName = ChatColorUtil.color("§b钻石");
        private String emeraldName = ChatColorUtil.color("§2绿宝石");
        private String levelI = ChatColorUtil.color("§e等级 §cI");
        private String levelII = ChatColorUtil.color("§e等级 §cII");
        private String levelIII = ChatColorUtil.color("§e等级 §cIII");

        // 物品属性
        private String itemDisplayName = ChatColorUtil.color("§a§a§a§a§a§a");
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
        private Title title;
        private int defaultCountdown = 120;
        private int quickStartCountdown = 10;

        // 公告时间点
        private int[] announcementTimes = {60, 30, 5, 4, 3, 2, 1};

        // 标题显示配置
        @Data
        public static class Title {
            private String titleCountdown = ChatColorUtil.color("§c§l%d");
            private String subtitleText = ChatColorUtil.color("§e§l准备战斗吧！");
            private int fadeIn = 1;
            private int titleStay = 20;
            private int fadeOut = 1;
        }

        // 消息模板
        private String msgCountdown = ChatColorUtil.color("§e游戏将在§c%d§e秒后开始！");
        private String msgNotEnoughPlayers = ChatColorUtil.color("§c人数不足，取消倒计时！");
        private String msgGameFull = ChatColorUtil.color("§e游戏人数已满,10秒后开始游戏！");
    }

    /**
     * 游戏结束倒计时配置
     */
    @Data
    public static class GameOverConfig {
        // 游戏结束倒计时时间(秒)
        private final int defaultCountdown = 15;

        // 标题显示配置
        private final int titleFadeIn = 0;
        private final int titleStay = 40;
        private final int titleFadeOut = 0;

        // 服务器关闭延迟(ticks)
        private final long shutdownDelay = 40L;

        // 烟花高度
        private final double fireworkHeight = 2.0D;

        // 胜利/失败消息
        private final String victoryTitle = ChatColorUtil.color("§6§l获胜！");
        private final String victorySubtitle = ChatColorUtil.color("§7你获得了最终的胜利");
        private final String defeatTitle = ChatColorUtil.color("§c§l失败！");
        private final String defeatSubtitle = ChatColorUtil.color("§7你输掉了这场游戏");

        // 排行榜标题及分隔线
        private final String[] lead = ChatColorUtil.color(new String[]{"§e§l击杀数第一名", "§6§l击杀数第二名", "§c§l击杀数第三名"});
        private final String separatorLine = ChatColorUtil.color("§a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        private final String gameTitle = ChatColorUtil.color("§f                                              §l起床战争");
        private final String winnersPrefix = ChatColorUtil.color("                                    §e胜利者 §7- ");
        private final String noWinner = ChatColorUtil.color("§7无");
        private final String rankPrefix = ChatColorUtil.color("                          ");
    }
} 