package cc.azuramc.bedwars.game.economy;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GamePlayer;
import com.cryptomorin.xseries.XSound;
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

    default boolean charge(GamePlayer gamePlayer, PriceCost price) {
        Player player = gamePlayer.getPlayer();
        if (!canAfford(player, price)) {
            if (XSound.ENTITY_ENDERMAN_TELEPORT.get() != null) {
                gamePlayer.playSound(XSound.ENTITY_ENDERMAN_TELEPORT.get(), 30F, 1F);
            }
            gamePlayer.sendMessage(AzuraBedWars.getInstance().getLang().getShop().getNotEnoughResources());
            return false;
        }

        withdraw(player, price);
        if (XSound.ENTITY_ITEM_PICKUP.get() != null) {
            gamePlayer.playSound(XSound.ENTITY_ITEM_PICKUP.get(), 1F, 1F);
        }
        return true;
    }

    /**
     * @return false to leave the resource to vanilla pickup
     */
    boolean deposit(GamePlayer gamePlayer, ItemStack resource);

    void rewardKill(GamePlayer victim, GamePlayer killer);
}
