package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.util.MessageUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务配置类
 * 包含所有任务相关的配置项
 * @author an5w1r@163.com
 */
@Data
@EqualsAndHashCode
public class ResourceSpawnConfig {

    private String ironGeneratorName = MessageUtil.color("铁刷新");
    private String goldGeneratorName = MessageUtil.color("金刷新");
    private String diamondGeneratorName = MessageUtil.color("钻石刷新");
    private String diamondTimeDisplay = MessageUtil.color("钻石时间显示");
    private String emeraldGeneratorName = MessageUtil.color("绿宝石刷新");
    private String emeraldTimeDisplay = MessageUtil.color("绿宝石时间显示");

    private String timeRemainingFormat = MessageUtil.color("&e将在&c%d&e秒后刷新");
    private String diamondName = MessageUtil.color("&b钻石");
    private String emeraldName = MessageUtil.color("&2绿宝石");
    private String levelI = MessageUtil.color("&e等级 &cI");
    private String levelII = MessageUtil.color("&e等级 &cII");
    private String levelIII = MessageUtil.color("&e等级 &cIII");

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
