package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.config.annotation.Comment;
import lombok.Data;

/**
 * @author an5w1r@163.com
 */
@Data
@Comment("队伍升级配置文件")
public class TeamUpgradeConfig {

    @Comment("升级倒计时时间(秒)")
    private int countDown = 5;

    @Comment("事件优先级")
    private int eventPriority = 0;

    @Comment("标题淡入时间(tick)")
    private int fadeIn = 1;

    @Comment("标题停留时间(tick)")
    private int titleStay = 20;

    @Comment("标题淡出时间(tick)")
    private int fadeOut = 1;

    @Comment("治愈池效果范围(方块)")
    private double healingPoolRange = 18.0;

    @Comment("陷阱触发范围(方块)")
    private double trapTriggerRange = 20.0;

    @Comment("急迫效果持续时间(tick)")
    private int hasteEffectDuration = 40;

    @Comment("生命恢复效果持续时间(tick)")
    private int regenerationEffectDuration = 60;

    @Comment("生命恢复效果等级(0 = I级，1 = II级)")
    private int regenerationEffectAmplifier = 0;

    @Comment("陷阱效果持续时间(tick)")
    private int trapEffectDuration = 160;

    @Comment("陷阱效果等级")
    private int trapEffectAmplifier = 0;

    @Comment("挖掘疲劳效果持续时间(tick)")
    private int miningFatigueEffectDuration = 160;

    @Comment("挖掘疲劳效果等级")
    private int miningFatigueEffectAmplifier = 0;
}
