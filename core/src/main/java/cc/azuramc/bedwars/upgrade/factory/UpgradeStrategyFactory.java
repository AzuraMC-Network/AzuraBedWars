package cc.azuramc.bedwars.upgrade.factory;

import cc.azuramc.bedwars.upgrade.upgrade.UpgradeStrategy;
import cc.azuramc.bedwars.upgrade.upgrade.impl.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 升级策略工厂类
 * 负责创建各种升级策略实例
 *
 * @author an5w1r@163.com
 */
public class UpgradeStrategyFactory {

    private static final Map<String, UpgradeStrategy> STRATEGY_CACHE = new HashMap<>();

    static {
        // 初始化所有升级策略
        STRATEGY_CACHE.put("磨刀石", new SharpenedSwordsUpgradeStrategy());
        STRATEGY_CACHE.put("精制护甲", new ReinforcedArmorUpgradeStrategy());
        STRATEGY_CACHE.put("疯狂矿工", new ManicMinerUpgradeStrategy());
        STRATEGY_CACHE.put("铁锻炉", new ResourceFurnaceUpgradeStrategy());
        STRATEGY_CACHE.put("治愈池", new HealingPoolUpgradeStrategy());
        STRATEGY_CACHE.put("缓冲靴子", new FallingProtectionUpgradeStrategy());
    }

    /**
     * 根据升级名称获取升级策略
     *
     * @param upgradeName 升级名称
     * @return 升级策略实例
     */
    public static UpgradeStrategy getStrategy(String upgradeName) {
        return STRATEGY_CACHE.get(upgradeName);
    }

    /**
     * 获取所有升级策略
     *
     * @return 所有升级策略的Map
     */
    public static Map<String, UpgradeStrategy> getAllStrategies() {
        return new HashMap<>(STRATEGY_CACHE);
    }
}
