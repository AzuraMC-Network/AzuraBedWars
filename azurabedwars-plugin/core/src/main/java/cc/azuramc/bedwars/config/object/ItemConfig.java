package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.config.annotation.Comment;
import cc.azuramc.bedwars.util.MessageUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ant1aura@qq.com
 */
@Data
@EqualsAndHashCode
@Comment("物品配置文件")
public class ItemConfig {

    @Comment("救援平台配置")
    private RescuePlatform rescuePlatform = new RescuePlatform();

    @Comment("传送粉配置")
    private WarpPowder warpPowder = new WarpPowder();

    @Comment("速度羊毛配置")
    private SpeedWoolHandler speedWoolHandler = new SpeedWoolHandler();

    @Comment("搭桥蛋配置")
    private EggBridge eggBridge = new EggBridge();

    @Comment("游戏管理物品配置")
    private GameManager gameManager = new GameManager();

    @Data
    public static class SpeedWoolHandler {
        @Comment("速度羊毛最大延伸长度")
        private int maxSpeedWoolLength = 6;
    }

    @Data
    public static class RescuePlatform {
        @Comment("平台存在时间(秒)")
        private int defaultBreakTime = 12;

        @Comment("使用冷却时间(秒)")
        private int defaultWaitTime = 20;

        @Comment("跳跃提升力度")
        private double jumpBoost = 0.7;
    }

    @Data
    public static class WarpPowder {
        @Comment("传送所需时间(秒)")
        private int defaultTeleportTime = 6;

        @Comment("每个粒子环的粒子数量")
        private int circleElements = 20;

        @Comment("粒子环半径")
        private double particleRadius = 1.0;

        @Comment("粒子效果总高度")
        private double particleHeight = 2.0;

        @Comment("粒子环的数量")
        private double circleCount = 15.0;

        @Comment("取消传送物品名称")
        private String cancelItemName = MessageUtil.color("&4取消传送");
    }

    @Data
    public static class GameManager {
        @Comment("资源类型选择器物品名称")
        private String resourceSelectorName = MessageUtil.color("&a资源类型选择 &7(右键选择)");

        @Comment("队伍选择器物品名称")
        private String teamSelectorName = MessageUtil.color("&e队伍选择 &7(右键选择)");

        @Comment("离开游戏物品名称")
        private String leaveGameName = MessageUtil.color("&c离开游戏 &7(右键离开)");
    }

    @Data
    public static class EggBridge {
        @Comment("搭桥蛋冷却时间(秒)")
        private int eggCooldownSeconds = 3;

        @Comment("冷却中提示消息")
        private String eggCooldownMessage = MessageUtil.color("&c搭桥蛋冷却中！");
    }
}
