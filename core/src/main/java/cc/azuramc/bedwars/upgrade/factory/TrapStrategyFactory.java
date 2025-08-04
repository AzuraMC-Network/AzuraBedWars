package cc.azuramc.bedwars.upgrade.factory;

import cc.azuramc.bedwars.upgrade.trap.ITrapStrategy;
import cc.azuramc.bedwars.upgrade.trap.TrapType;
import cc.azuramc.bedwars.upgrade.trap.impl.AlarmITrapStrategy;
import cc.azuramc.bedwars.upgrade.trap.impl.BlindnessITrapStrategy;
import cc.azuramc.bedwars.upgrade.trap.impl.FightBackITrapStrategy;
import cc.azuramc.bedwars.upgrade.trap.impl.MinerITrapStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 * 陷阱策略工厂类
 * 负责创建各种陷阱策略实例
 *
 * @author an5w1r@163.com
 */
public class TrapStrategyFactory {

    private static final Map<TrapType, ITrapStrategy> STRATEGY_CACHE = new HashMap<>();

    static {
        // 初始化所有陷阱策略
        STRATEGY_CACHE.put(TrapType.BLINDNESS, new BlindnessITrapStrategy());
        STRATEGY_CACHE.put(TrapType.FIGHT_BACK, new FightBackITrapStrategy());
        STRATEGY_CACHE.put(TrapType.ALARM, new AlarmITrapStrategy());
        STRATEGY_CACHE.put(TrapType.MINER, new MinerITrapStrategy());
    }

    /**
     * 根据陷阱类型获取陷阱策略
     *
     * @param trapType 陷阱类型
     * @return 陷阱策略实例
     */
    public static ITrapStrategy getStrategy(TrapType trapType) {
        return STRATEGY_CACHE.get(trapType);
    }

    /**
     * 获取所有陷阱策略
     *
     * @return 所有陷阱策略的Map
     */
    public static Map<TrapType, ITrapStrategy> getAllStrategies() {
        return new HashMap<>(STRATEGY_CACHE);
    }
}
