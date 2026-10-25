package cc.azuramc.bedwars.game.economy;

import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.shop.PriceCost;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author an5w1r@163.com
 */
public interface EconomyMode {

    String getDisplayName();

    boolean canAfford(Player player, PriceCost price);

    void withdraw(Player player, PriceCost price);

    String formatPrice(PriceCost price);

    /**
     * @return false to leave the resource to vanilla pickup
     */
    boolean deposit(GamePlayer gamePlayer, ItemStack resource);

    void rewardKill(GamePlayer victim, GamePlayer killer);
}
