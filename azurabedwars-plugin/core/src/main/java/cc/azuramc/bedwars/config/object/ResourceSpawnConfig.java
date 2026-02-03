package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.config.annotation.Comment;
import cc.azuramc.bedwars.util.MessageUtil;
import lombok.Data;

/**
 * @author an5w1r@163.com
 */
@Data
@Comment("资源生成配置文件")
public class ResourceSpawnConfig {

    @Comment("铁资源点全息名称")
    private String ironGeneratorName = MessageUtil.color("铁刷新");

    @Comment("金资源点全息名称")
    private String goldGeneratorName = MessageUtil.color("金刷新");

    @Comment("钻石资源点全息名称")
    private String diamondGeneratorName = MessageUtil.color("钻石刷新");

    @Comment("钻石时间显示文本")
    private String diamondTimeDisplay = MessageUtil.color("钻石时间显示");

    @Comment("绿宝石资源点全息名称")
    private String emeraldGeneratorName = MessageUtil.color("绿宝石刷新");

    @Comment("绿宝石时间显示文本")
    private String emeraldTimeDisplay = MessageUtil.color("绿宝石时间显示");

    @Comment({"剩余时间显示格式", "变量: %d 为剩余秒数"})
    private String timeRemainingFormat = MessageUtil.color("&e将在&c%d&e秒后刷新");

    @Comment("钻石名称")
    private String diamondName = MessageUtil.color("&b钻石");

    @Comment("绿宝石名称")
    private String emeraldName = MessageUtil.color("&2绿宝石");

    @Comment("等级 I 显示文本")
    private String levelI = MessageUtil.color("&e等级 &cI");

    @Comment("等级 II 显示文本")
    private String levelII = MessageUtil.color("&e等级 &cII");

    @Comment("等级 III 显示文本")
    private String levelIII = MessageUtil.color("&e等级 &cIII");

    @Comment("铁资源生成间隔(秒)")
    private int ironSpawnInterval = 1;

    @Comment("金资源生成间隔(秒)")
    private int goldSpawnInterval = 4;

    @Comment("钻石资源生成间隔(秒)")
    private int diamondSpawnInterval = 30;

    @Comment("绿宝石资源生成间隔(秒)")
    private int emeraldSpawnInterval = 55;

    @Comment("一级铁资源最大堆叠数量")
    private int maxIronStackLevel1 = 48;

    @Comment("一级金资源最大堆叠数量")
    private int maxGoldStackLevel1 = 8;

    @Comment("一级钻石资源最大堆叠数量")
    private int maxDiamondStackLevel1 = 4;

    @Comment("一级绿宝石资源最大堆叠数量")
    private int maxEmeraldStackLevel1 = 2;

    @Comment("二级铁资源最大堆叠数量")
    private int maxIronStackLevel2 = 48;

    @Comment("二级金资源最大堆叠数量")
    private int maxGoldStackLevel2 = 8;

    @Comment("二级钻石资源最大堆叠数量")
    private int maxDiamondStackLevel2 = 6;

    @Comment("二级绿宝石资源最大堆叠数量")
    private int maxEmeraldStackLevel2 = 4;

    @Comment("三级铁资源最大堆叠数量")
    private int maxIronStackLevel3 = 64;

    @Comment("三级金资源最大堆叠数量")
    private int maxGoldStackLevel3 = 12;

    @Comment("三级钻石资源最大堆叠数量")
    private int maxDiamondStackLevel3 = 8;

    @Comment("三级绿宝石资源最大堆叠数量")
    private int maxEmeraldStackLevel3 = 4;

    @Comment("资源检测范围(方块)")
    private double resourceCheckRadius = 3;

    @Comment("资源点名称显示高度")
    private float nameDisplayHeight = 6.0F;

    @Comment("资源类型显示高度")
    private float resourceTypeHeight = 5.0F;

    @Comment("等级显示高度")
    private float levelDisplayHeight = 4.0F;

    @Comment("物品弹出 X 轴速度")
    private double itemVelocityX = 0.0D;

    @Comment("物品弹出 Y 轴速度")
    private double itemVelocityY = 0.1D;

    @Comment("物品弹出 Z 轴速度")
    private double itemVelocityZ = 0.0D;

}
