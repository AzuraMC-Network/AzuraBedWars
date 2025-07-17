package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.util.MessageUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ant1aura@qq.com
 */
@Data
@EqualsAndHashCode
public class ItemConfig {

    private RescuePlatform rescuePlatform = new RescuePlatform();
    private WarpPowder warpPowder = new WarpPowder();
    private SpeedWoolHandler speedWoolHandler = new SpeedWoolHandler();
    private EggBridge eggBridge = new EggBridge();
    private FireBall fireBall = new FireBall();

    private GameManager gameManager = new GameManager();

    @Data
    public static class SpeedWoolHandler {
        private int maxSpeedWoolLength = 6;
    }

    @Data
    public static class RescuePlatform {
        private int defaultBreakTime = 12;      // 默认平台存在时间（秒）
        private int defaultWaitTime = 20;       // 默认使用冷却时间（秒）
        private double jumpBoost = 0.7;         // 跳跃提升力度
    }

    @Data
    public static class WarpPowder {
        private int defaultTeleportTime = 6;                 // 默认传送时间（秒）
        private int circleElements = 20;                     // 每个粒子环的粒子数量
        private double particleRadius = 1.0;                 // 粒子环半径
        private double particleHeight = 2.0;                 // 粒子效果总高度
        private double circleCount = 15.0;                   // 粒子环的数量

        private String cancelItemName = MessageUtil.color("&4取消传送");          // 取消传送物品名称
    }

    @Data
    public static class GameManager {
        private String resourceSelectorName = MessageUtil.color("&a资源类型选择 &7(右键选择)");
        private String leaveGameName = MessageUtil.color("&c离开游戏 &7(右键离开)");
    }

    @Data
    public static class EggBridge {
        private int eggCooldownSeconds = 3;    // 搭桥蛋冷却时间
        private String eggCooldownMessage = "&c搭桥蛋冷却中！";
    }

    @Data
    public static class FireBall {
        private int fireballExplosionRadiusX = 4;
        private int fireballExplosionRadiusY = 3;
        private int fireballExplosionRadiusZ = 4;
        private int fireballDamage = 3;
        private double fireballKnockbackMultiplier = 0.5;
    }
}
