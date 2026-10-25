package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.config.annotation.Comment;
import cc.azuramc.bedwars.config.annotation.Range;
import lombok.Data;

/**
 * @author an5w1r@163.com
 */
@Data
@Comment("资源点配置")
public class GeneratorConfig {

    @Comment("铁锭生成间隔(秒)")
    @Range(min = 0.05)
    private double ironInterval = 1;

    @Comment("金锭生成间隔(秒)")
    @Range(min = 0.05)
    private double goldInterval = 3;

    @Comment({"钻石一级生成间隔(秒)", "二级和三级的间隔在 timeline.yml 里"})
    @Range(min = 0.05)
    private double diamondInterval = 35;

    @Comment({"绿宝石一级生成间隔(秒)", "二级和三级的间隔在 timeline.yml 里"})
    @Range(min = 0.05)
    private double emeraldInterval = 60;

    @Comment("铁锭在资源点最多堆积数量")
    @Range(min = 1)
    private int ironMaxStack = 48;

    @Comment("金锭在资源点最多堆积数量")
    @Range(min = 1)
    private int goldMaxStack = 8;

    @Comment("一级钻石资源最大堆叠数量")
    @Range(min = 1)
    private int maxDiamondStackLevel1 = 4;

    @Comment("二级钻石资源最大堆叠数量")
    @Range(min = 1)
    private int maxDiamondStackLevel2 = 6;

    @Comment("三级钻石资源最大堆叠数量")
    @Range(min = 1)
    private int maxDiamondStackLevel3 = 8;

    @Comment("一级绿宝石资源最大堆叠数量")
    @Range(min = 1)
    private int maxEmeraldStackLevel1 = 2;

    @Comment("二级绿宝石资源最大堆叠数量")
    @Range(min = 1)
    private int maxEmeraldStackLevel2 = 4;

    @Comment("三级绿宝石资源最大堆叠数量")
    @Range(min = 1)
    private int maxEmeraldStackLevel3 = 4;

    @Comment("钻石资源点全息名称")
    private String diamondName = "&b钻石";

    @Comment("绿宝石资源点全息名称")
    private String emeraldName = "&2绿宝石";

    @Comment({"剩余时间显示格式", "变量: %d 为剩余秒数"})
    private String timeRemainingFormat = "&e将在&c%d&e秒后刷新";

    @Comment("等级 I 显示文本")
    private String levelI = "&e等级 &cI";

    @Comment("等级 II 显示文本")
    private String levelII = "&e等级 &cII";

    @Comment("等级 III 显示文本")
    private String levelIII = "&e等级 &cIII";
}
