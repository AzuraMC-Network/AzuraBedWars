package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.config.annotation.ConfigComment;
import cc.azuramc.bedwars.util.MessageUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资源生成配置类
 * 包含所有资源生成相关的配置项
 *
 * @author an5w1r@163.com
 */
@Data
@EqualsAndHashCode
public class ResourceSpawnConfig {

    @ConfigComment("铁资源生成器名称")
    private String ironGeneratorName = MessageUtil.color("铁刷新");

    @ConfigComment("金资源生成器名称")
    private String goldGeneratorName = MessageUtil.color("金刷新");

    @ConfigComment("钻石资源生成器名称")
    private String diamondGeneratorName = MessageUtil.color("钻石刷新");

    @ConfigComment("钻石时间显示文本")
    private String diamondTimeDisplay = MessageUtil.color("钻石时间显示");

    @ConfigComment("绿宝石资源生成器名称")
    private String emeraldGeneratorName = MessageUtil.color("绿宝石刷新");

    @ConfigComment("绿宝石时间显示文本")
    private String emeraldTimeDisplay = MessageUtil.color("绿宝石时间显示");

    @ConfigComment({
            "剩余时间显示格式",
            "使用 %d 作为秒数占位符"
    })
    private String timeRemainingFormat = MessageUtil.color("&e将在&c%d&e秒后刷新");

    @ConfigComment("钻石名称显示")
    private String diamondName = MessageUtil.color("&b钻石");

    @ConfigComment("绿宝石名称显示")
    private String emeraldName = MessageUtil.color("&2绿宝石");

    @ConfigComment("一级资源点等级显示")
    private String levelI = MessageUtil.color("&e等级 &cI");

    @ConfigComment("二级资源点等级显示")
    private String levelII = MessageUtil.color("&e等级 &cII");

    @ConfigComment("三级资源点等级显示")
    private String levelIII = MessageUtil.color("&e等级 &cIII");

    @ConfigComment("铁资源生成时间间隔（秒）")
    private int ironSpawnInterval = 1;

    @ConfigComment("金资源生成时间间隔（秒）")
    private int goldSpawnInterval = 4;

    @ConfigComment("钻石资源生成时间间隔（秒）")
    private int diamondSpawnInterval = 30;

    @ConfigComment("绿宝石资源生成时间间隔（秒）")
    private int emeraldSpawnInterval = 55;

    @ConfigComment("一级铁资源点最大堆叠数量")
    private int maxIronStackLevel1 = 48;

    @ConfigComment("一级金资源点最大堆叠数量")
    private int maxGoldStackLevel1 = 8;

    @ConfigComment("一级钻石资源点最大堆叠数量")
    private int maxDiamondStackLevel1 = 4;

    @ConfigComment("一级绿宝石资源点最大堆叠数量")
    private int maxEmeraldStackLevel1 = 2;

    @ConfigComment("二级铁资源点最大堆叠数量")
    private int maxIronStackLevel2 = 48;

    @ConfigComment("二级金资源点最大堆叠数量")
    private int maxGoldStackLevel2 = 8;

    @ConfigComment("二级钻石资源点最大堆叠数量")
    private int maxDiamondStackLevel2 = 6;

    @ConfigComment("二级绿宝石资源点最大堆叠数量")
    private int maxEmeraldStackLevel2 = 4;

    @ConfigComment("三级铁资源点最大堆叠数量")
    private int maxIronStackLevel3 = 64;

    @ConfigComment("三级金资源点最大堆叠数量")
    private int maxGoldStackLevel3 = 12;

    @ConfigComment("三级钻石资源点最大堆叠数量")
    private int maxDiamondStackLevel3 = 8;

    @ConfigComment("三级绿宝石资源点最大堆叠数量")
    private int maxEmeraldStackLevel3 = 4;

    @ConfigComment({
            "检测资源周围范围（方块）",
            "用于检测资源点附近是否有堆积的资源"
    })
    private double resourceCheckRadius = 3;

    @ConfigComment("资源生成器名称显示高度")
    private float nameDisplayHeight = 6.0F;

    @ConfigComment("资源类型显示高度")
    private float resourceTypeHeight = 5.0F;

    @ConfigComment("等级显示高度")
    private float levelDisplayHeight = 4.0F;

    @ConfigComment("物品生成X轴速度")
    private double itemVelocityX = 0.0D;

    @ConfigComment({
            "物品生成Y轴速度",
            "向上弹起的初速度"
    })
    private double itemVelocityY = 0.1D;

    @ConfigComment("物品生成Z轴速度")
    private double itemVelocityZ = 0.0D;

}
