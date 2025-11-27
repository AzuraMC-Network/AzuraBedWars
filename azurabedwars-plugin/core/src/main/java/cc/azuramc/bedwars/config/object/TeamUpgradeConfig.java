package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.config.annotation.ConfigComment;
import lombok.Data;

/**
 * 队伍升级配置类
 *
 * @author an5w1r@163.com
 */
@Data
public class TeamUpgradeConfig {

    @ConfigComment("购买升级后的倒计时时长（秒）")
    private int countDown = 5;

    @ConfigComment("事件优先级")
    private int eventPriority = 0;

    @ConfigComment("标题淡入时间（ticks）")
    private int fadeIn = 1;

    @ConfigComment("标题停留时间（ticks）")
    private int titleStay = 20;

    @ConfigComment("标题淡出时间（ticks）")
    private int fadeOut = 1;

    @ConfigComment({
            "治疗池有效范围（方块）",
            "玩家在此范围内会受到治疗效果"
    })
    private double healingPoolRange = 18.0;

    @ConfigComment({
            "陷阱触发范围（方块）",
            "敌人进入此范围会触发陷阱"
    })
    private double trapTriggerRange = 20.0;

    @ConfigComment({
            "急迫效果持续时间（ticks）",
            "1秒 = 20 ticks"
    })
    private int hasteEffectDuration = 40;

    @ConfigComment({
            "生命恢复效果持续时间（ticks）",
            "1秒 = 20 ticks"
    })
    private int regenerationEffectDuration = 60;

    @ConfigComment({
            "生命恢复效果等级",
            "0 = 生命恢复I, 1 = 生命恢复II"
    })
    private int regenerationEffectAmplifier = 0;

    @ConfigComment({
            "陷阱效果持续时间（ticks）",
            "1秒 = 20 ticks"
    })
    private int trapEffectDuration = 160;

    @ConfigComment({
            "陷阱效果等级",
            "0 = 失明I, 1 = 失明II"
    })
    private int trapEffectAmplifier = 0;

    @ConfigComment({
            "挖掘疲劳效果持续时间（ticks）",
            "1秒 = 20 ticks"
    })
    private int miningFatigueEffectDuration = 160;

    @ConfigComment({
            "挖掘疲劳效果等级",
            "0 = 挖掘疲劳I, 1 = 挖掘疲劳II"
    })
    private int miningFatigueEffectAmplifier = 0;
}
