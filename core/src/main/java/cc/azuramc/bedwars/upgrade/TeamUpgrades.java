package cc.azuramc.bedwars.upgrade;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.economy.EconomyMode;
import cc.azuramc.bedwars.game.economy.PriceCost;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author an5w1r@163.com
 */
public class TeamUpgrades {

    public static final int MAX_TRAPS = 3;

    private final GameTeam gameTeam;
    private final Map<TeamUpgrade, Integer> levels = new EnumMap<>(TeamUpgrade.class);
    /** Triggered in purchase order */
    private final Set<TrapType> traps = new LinkedHashSet<>();

    public TeamUpgrades(GameTeam gameTeam) {
        this.gameTeam = gameTeam;
    }

    public int getLevel(TeamUpgrade upgrade) {
        return levels.getOrDefault(upgrade, 0);
    }

    public boolean isMaxed(TeamUpgrade upgrade) {
        return getLevel(upgrade) >= upgrade.getMaxLevel();
    }

    public boolean buy(GamePlayer buyer, TeamUpgrade upgrade) {
        int level = getLevel(upgrade);
        if (isMaxed(upgrade) || !economy().charge(buyer, PriceCost.teamUpgrade(upgrade.getPrices()[level]))) {
            return false;
        }
        levels.put(upgrade, level + 1);
        UpgradeEffects.onUpgrade(gameTeam, upgrade, level + 1);
        return true;
    }

    public List<TrapType> getTraps() {
        return new ArrayList<>(traps);
    }

    public boolean hasTrap(TrapType trap) {
        return traps.contains(trap);
    }

    public boolean isTrapQueueFull() {
        return traps.size() >= MAX_TRAPS;
    }

    public int getNextTrapPrice() {
        return traps.size() + 1;
    }

    public boolean buyTrap(GamePlayer buyer, TrapType trap) {
        if (hasTrap(trap) || isTrapQueueFull() || !economy().charge(buyer, PriceCost.teamUpgrade(getNextTrapPrice()))) {
            return false;
        }
        traps.add(trap);
        return true;
    }

    @Nullable
    public TrapType pollTrap() {
        Iterator<TrapType> iterator = traps.iterator();
        if (!iterator.hasNext()) {
            return null;
        }
        TrapType trap = iterator.next();
        iterator.remove();
        return trap;
    }

    private static EconomyMode economy() {
        return AzuraBedWars.getInstance().getGameManager().getEconomy();
    }
}
