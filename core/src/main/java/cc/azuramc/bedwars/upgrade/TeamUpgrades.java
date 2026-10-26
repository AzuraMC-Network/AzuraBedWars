package cc.azuramc.bedwars.upgrade;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.UpgradeConfig;
import cc.azuramc.bedwars.config.object.shop.Price;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.economy.EconomyMode;
import cc.azuramc.bedwars.game.economy.PriceCost;
import cc.azuramc.bedwars.shop.ShopPurchase;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Levels and trap queue of one team, keyed by the ids of upgrades.yml
 *
 * @author an5w1r@163.com
 */
public class TeamUpgrades {

    private final GameTeam gameTeam;
    private final Map<String, Integer> levels = new HashMap<>();
    /** Triggered in purchase order */
    private final Set<String> traps = new LinkedHashSet<>();

    public TeamUpgrades(GameTeam gameTeam) {
        this.gameTeam = gameTeam;
    }

    public int getLevel(String upgrade) {
        return levels.getOrDefault(upgrade, 0);
    }

    public boolean isMaxed(String id, UpgradeConfig.Upgrade upgrade) {
        return getLevel(id) >= upgrade.getLevels().size();
    }

    public boolean buy(GamePlayer buyer, String id, UpgradeConfig.Upgrade upgrade) {
        int level = getLevel(id);
        if (isMaxed(id, upgrade)) {
            return false;
        }
        UpgradeConfig.Level next = upgrade.getLevels().get(level);
        if (!economy().charge(buyer, ShopPurchase.cost(next.getPrice(), next.getXp()))) {
            return false;
        }
        levels.put(id, level + 1);
        UpgradeEffects.onUpgrade(gameTeam, next);
        return true;
    }

    public List<String> getTraps() {
        return new ArrayList<>(traps);
    }

    public boolean hasTrap(String trap) {
        return traps.contains(trap);
    }

    public boolean isTrapQueueFull() {
        return traps.size() >= config().getTraps().getQueueSize();
    }

    /**
     * The last price of traps.prices repeats for a queue longer than the list
     */
    public PriceCost getNextTrapPrice() {
        UpgradeConfig.Traps settings = config().getTraps();
        int index = traps.size();
        Integer xp = settings.getXp().isEmpty() ? null : settings.getXp().get(Math.min(index, settings.getXp().size() - 1));
        List<Price> prices = settings.getPrices().isEmpty() ? new UpgradeConfig.Traps().getPrices() : settings.getPrices();
        return ShopPurchase.cost(prices.get(Math.min(index, prices.size() - 1)), xp);
    }

    public boolean buyTrap(GamePlayer buyer, String trap) {
        if (hasTrap(trap) || isTrapQueueFull() || !economy().charge(buyer, getNextTrapPrice())) {
            return false;
        }
        traps.add(trap);
        return true;
    }

    @Nullable
    public String pollTrap() {
        Iterator<String> iterator = traps.iterator();
        if (!iterator.hasNext()) {
            return null;
        }
        String trap = iterator.next();
        iterator.remove();
        return trap;
    }

    private static UpgradeConfig config() {
        return AzuraBedWars.getInstance().getUpgradeConfig();
    }

    private static EconomyMode economy() {
        return AzuraBedWars.getInstance().getGameManager().getEconomy();
    }
}
