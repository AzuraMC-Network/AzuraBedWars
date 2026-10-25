package cc.azuramc.bedwars.shop.helper;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.economy.EconomyMode;
import cc.azuramc.bedwars.shop.PriceCost;
import com.cryptomorin.xseries.XSound;
import org.bukkit.entity.Player;

/**
 * @author an5w1r@163.com
 */
public final class ShopPaymentHandler {

    private ShopPaymentHandler() {
    }

    public static boolean processPayment(GamePlayer gamePlayer, PriceCost price) {
        EconomyMode economy = AzuraBedWars.getInstance().getGameManager().getEconomy();
        Player player = gamePlayer.getPlayer();

        if (!economy.canAfford(player, price)) {
            if (XSound.ENTITY_ENDERMAN_TELEPORT.get() != null) {
                gamePlayer.playSound(XSound.ENTITY_ENDERMAN_TELEPORT.get(), 30F, 1F);
            }
            gamePlayer.sendMessage("§c没有足够资源购买！");
            return false;
        }

        economy.withdraw(player, price);
        if (XSound.ENTITY_ITEM_PICKUP.get() != null) {
            gamePlayer.playSound(XSound.ENTITY_ITEM_PICKUP.get(), 1F, 1F);
        }
        return true;
    }
}
