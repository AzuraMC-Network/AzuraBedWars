package cc.azuramc.bedwars.extension;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.api.extension.ItemUseContext;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.shop.ShopCatalog;
import cc.azuramc.bedwars.shop.ShopItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Items of {@code custom} shop items, recognized by the {@code azurabedwars:item} tag holding their shop reference
 *
 * @author an5w1r@163.com
 */
public final class CustomItems implements Listener {

    private static final String TAG_KEY = "item";

    public static ItemStack tag(ItemStack item, String shopRef) {
        return AzuraBedWars.getInstance().getNmsAccess().setItemTag(item, TAG_KEY, shopRef);
    }

    @Nullable
    public static String of(@Nullable ItemStack item) {
        return item == null ? null : AzuraBedWars.getInstance().getNmsAccess().getItemTag(item, TAG_KEY);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        String ref = of(event.getItem());
        Player player = event.getPlayer();
        // isSimilar skips the off hand of 1.9 and later
        if (ref == null || event.getAction() == Action.PHYSICAL || !event.getItem().isSimilar(PlayerUtil.getItemInHand(player))) {
            return;
        }
        ShopItem item = ShopCatalog.get().find(ref);
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        if (item == null || item.getSpec().getCustom() == null || gamePlayer == null || gamePlayer.isSpectator()) {
            return;
        }
        AzuraBedWars.getInstance().getExtensions().useItem(item.getSpec().getCustom(), item.getSpec().getSettings(), new ItemUseContext() {
            @NotNull
            @Override
            public GamePlayer getPlayer() {
                return gamePlayer;
            }

            @NotNull
            @Override
            public ItemStack getItem() {
                return event.getItem();
            }

            @NotNull
            @Override
            public PlayerInteractEvent getEvent() {
                return event;
            }

            @Override
            public void consume() {
                ItemStack hand = PlayerUtil.getItemInHand(player);
                if (hand != null && hand.getAmount() > 1) {
                    hand.setAmount(hand.getAmount() - 1);
                    PlayerUtil.setItemInHand(player, hand);
                } else {
                    PlayerUtil.setItemInHand(player, null);
                }
            }
        });
    }
}
