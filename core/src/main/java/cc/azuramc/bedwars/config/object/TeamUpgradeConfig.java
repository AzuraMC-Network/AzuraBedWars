package cc.azuramc.bedwars.config.object;

import lombok.Data;

/**
 * @author An5w1r@163.com
 */
@Data
public class TeamUpgradeConfig {

    private int countDown = 5;
    private int eventPriority = 0;

    private int fadeIn = 1;
    private int titleStay = 20;
    private int fadeOut = 1;

    private double healingPoolRange = 18.0;
    private double trapTriggerRange = 20.0;

    private int hasteEffectDuration = 40;
    private int regenerationEffectDuration = 60;
    private int regenerationEffectAmplifier = 0;
    private int trapEffectDuration = 160;
    private int trapEffectAmplifier = 0;
    private int miningFatigueEffectDuration = 160;
    private int miningFatigueEffectAmplifier = 0;
}
