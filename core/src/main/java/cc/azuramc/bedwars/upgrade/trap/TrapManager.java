package cc.azuramc.bedwars.upgrade.trap;

import cc.azuramc.bedwars.upgrade.trap.impl.AlarmTrapStrategy;
import cc.azuramc.bedwars.upgrade.trap.impl.BlindnessTrapStrategy;
import cc.azuramc.bedwars.upgrade.trap.impl.FightBackTrapStrategy;
import cc.azuramc.bedwars.upgrade.trap.impl.MinerTrapStrategy;
import org.bukkit.Material;

import java.util.*;

/**
 * @author An5w1r@163.com
 */
public class TrapManager {

    private final Set<TrapType> activeTraps = new LinkedHashSet<>();

    /** 资源类型名称缓存 */
    private final Map<Material, String> resourceNames = new HashMap<>();

    private final int maxTrapCount = 3;

    /**
     * 获取激活的陷阱数量
     */
    public int getActiveTrapCount() {
        return activeTraps.size();
    }

    /**
     * 获取激活的陷阱类型列表
     */
    public List<TrapType> getActiveTraps() {
        return new ArrayList<>(activeTraps);
    }

    /**
     * 获取激活陷阱的显示名称列表
     */
    public List<String> getActiveTrapNames() {
        return activeTraps.stream()
                .map(TrapType::getDisplayName)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * 根据陷阱激活数量动态调整价格
     * @return 当前陷阱的价格
     */
    public int getCurrentTrapPrice() {
        if (activeTraps.isEmpty()) {
            return 1;
        }

        return activeTraps.size() + 1;
    }

    /**
     * 检查特定陷阱是否激活
     */
    public boolean isTrapActive(TrapType trapType) {
        return activeTraps.contains(trapType);
    }

    /**
     * 检查是否达到最大陷阱数量限制
     */
    public boolean isReachedActiveLimit() {
        return activeTraps.size() >= maxTrapCount;
    }


    /**
     * 激活指定陷阱
     */
    public void activateTrap(TrapType trapType) {
        activeTraps.add(trapType);
    }

    /**
     * 停用指定陷阱
     */
    public void deactivateTrap(TrapType trapType) {
        activeTraps.remove(trapType);
    }

    /**
     * 设置陷阱激活状态
     */
    public void setTrapActive(TrapType trapType, boolean active) {
        if (active) {
            activeTraps.add(trapType);
        } else {
            activeTraps.remove(trapType);
        }
    }

    /**
     * 检查是否有任何陷阱激活
     */
    public boolean hasAnyActiveTrap() {
        return !activeTraps.isEmpty();
    }

    /**
     * 停用所有陷阱
     */
    public void deactivateAllTraps() {
        activeTraps.clear();
    }

    /**
     * 获取所有陷阱的状态映射
     */
    public Map<TrapType, Boolean> getTrapStatusMap() {
        Map<TrapType, Boolean> statusMap = new HashMap<>();
        for (TrapType type : TrapType.values()) {
            statusMap.put(type, activeTraps.contains(type));
        }
        return statusMap;
    }

    /**
     * 获取陷阱策略实例
     *
     * @param trapType 陷阱类型
     * @return 陷阱策略实例
     */
    public TrapStrategy getTrapStrategy(TrapType trapType) {
        return switch (trapType) {
            case BLINDNESS -> new BlindnessTrapStrategy();
            case FIGHT_BACK -> new FightBackTrapStrategy();
            case ALARM -> new AlarmTrapStrategy();
            case MINER -> new MinerTrapStrategy();
            default -> throw new IllegalArgumentException("Unknown trap type: " + trapType);
        };
    }
}
