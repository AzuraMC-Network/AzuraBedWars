package cc.azuramc.bedwars.shop;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.GeneratorConfig;
import cc.azuramc.bedwars.config.object.shop.Price;
import cc.azuramc.bedwars.config.object.shop.ShopItemSpec;
import cc.azuramc.bedwars.config.object.shop.TierChains;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.economy.EconomyMode;
import cc.azuramc.bedwars.game.economy.PriceCost;
import cc.azuramc.bedwars.upgrade.UpgradeEffects;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author an5w1r@163.com
 */
public final class ShopPurchase {

    public enum State {
        CAN_BUY,
        TOO_POOR,
        OWNED,
        MAXED,
        LOCKED
    }

    private ShopPurchase() {
    }

    /**
     * @return true when the item was paid for and given
     */
    public static boolean buy(GamePlayer gamePlayer, ShopItem item) {
        ShopItemSpec spec = item.getSpec();
        State state = state(gamePlayer, item);
        if (state == State.OWNED || state == State.MAXED || state == State.LOCKED) {
            return false;
        }
        if (spec.getRequires() != null && spec.getRequires().getPermission() != null
                && !gamePlayer.getPlayer().hasPermission(spec.getRequires().getPermission())) {
            gamePlayer.sendMessage(AzuraBedWars.getInstance().getLang().getShop().getNoPermission());
            return false;
        }
        boolean armor = item.getChain() != null && item.getChain().getSlot() == TierChains.Slot.ARMOR;
        if (!armor && gamePlayer.getPlayer().getInventory().firstEmpty() == -1) {
            gamePlayer.sendMessage(AzuraBedWars.getInstance().getLang().getShop().getInventoryFull());
            return false;
        }
        if (!economy().charge(gamePlayer, priceFor(gamePlayer, item))) {
            return false;
        }

        if (item.getChain() != null) {
            gamePlayer.getTierLevels().put(spec.getTier(), targetLevel(gamePlayer, item));
            gamePlayer.giveTier(spec.getTier(), true);
        } else {
            give(gamePlayer, item);
        }
        if (spec.getLimit() != null) {
            gamePlayer.getPurchases().merge(item.getRef(), 1, Integer::sum);
        }
        if (spec.isKeepOnRespawn()) {
            gamePlayer.getKeptItems().add(item.getRef());
        }
        GameTeam gameTeam = gamePlayer.getGameTeam();
        for (String command : spec.getCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                    .replace("{player}", gamePlayer.getName())
                    .replace("{team}", gameTeam != null ? gameTeam.getName() : ""));
        }
        return true;
    }

    /**
     * Puts the item of a shop item without a tier chain into the inventory
     */
    public static void give(GamePlayer gamePlayer, ShopItem item) {
        if (item.getItem() == null) {
            return;
        }
        ItemStack stack = item.getItem().clone();
        GameTeam gameTeam = gamePlayer.getGameTeam();
        if (gameTeam != null && item.getSpec().getItem() != null && item.getSpec().getItem().isTeamColor()) {
            stack = ItemSpecs.teamColored(stack, gameTeam);
        }
        if (item.getSpec().isSword()) {
            gamePlayer.getPlayer().getInventory().remove(Objects.requireNonNull(XMaterial.WOODEN_SWORD.get()));
            if (gameTeam != null) {
                UpgradeEffects.enchantSword(gameTeam, stack);
            }
        }
        gamePlayer.getPlayer().getInventory().addItem(stack);
    }

    public static State state(GamePlayer gamePlayer, ShopItem item) {
        ShopItemSpec spec = item.getSpec();
        if (spec.getLimit() != null && gamePlayer.getPurchases().getOrDefault(item.getRef(), 0) >= spec.getLimit().getTimes()) {
            return State.OWNED;
        }
        TierChains.Chain chain = item.getChain();
        if (chain != null) {
            int current = gamePlayer.getTierLevel(spec.getTier());
            int target = targetLevel(gamePlayer, item);
            if (spec.getLevel() == 0 && current >= chain.getTiers().size()) {
                return State.MAXED;
            }
            if (target <= current) {
                return State.OWNED;
            }
            TierChains.Tier tier = chain.getTiers().get(target - 1);
            if (chain.getBuyOrder() == TierChains.BuyOrder.SEQUENTIAL && target != current + 1 || tier.getPrice() == null && tier.getXp() == null) {
                return State.LOCKED;
            }
        }
        return economy().canAfford(gamePlayer.getPlayer(), priceFor(gamePlayer, item)) ? State.CAN_BUY : State.TOO_POOR;
    }

    public static PriceCost priceFor(GamePlayer gamePlayer, ShopItem item) {
        TierChains.Chain chain = item.getChain();
        if (chain == null) {
            return cost(item.getSpec().getPrice(), item.getSpec().getXp());
        }
        TierChains.Tier tier = chain.getTiers().get(targetLevel(gamePlayer, item) - 1);
        return cost(tier.getPrice(), tier.getXp());
    }

    public static PriceCost cost(@Nullable Price price, @Nullable Integer xp) {
        if (price == null) {
            return new PriceCost("", 0, xp != null ? xp : 0);
        }
        GeneratorConfig.Resource resource = AzuraBedWars.getInstance().getGeneratorConfig().resource(price.getCurrency());
        int levels = xp != null ? xp : price.getAmount() * (resource != null ? resource.getXpValue() : 0);
        return new PriceCost(price.getCurrency(), price.getAmount(), levels);
    }

    public static ItemStack displayIcon(GamePlayer gamePlayer, ShopItem item) {
        TierChains.Chain chain = item.getChain();
        if (item.getItem() == null && chain != null) {
            ItemStack tierItem = ShopCatalog.tierItem(chain, targetLevel(gamePlayer, item));
            if (tierItem != null) {
                return tierItem;
            }
        }
        return item.getItem() != null ? item.getItem().clone() : new ItemStack(Objects.requireNonNull(XMaterial.BARRIER.get()));
    }

    /**
     * The level a tier item buys, or shows once the chain is maxed
     */
    public static int targetLevel(GamePlayer gamePlayer, ShopItem item) {
        ShopItemSpec spec = item.getSpec();
        if (spec.getLevel() > 0) {
            return spec.getLevel();
        }
        int size = Objects.requireNonNull(item.getChain()).getTiers().size();
        return Math.min(gamePlayer.getTierLevel(spec.getTier()) + 1, size);
    }

    private static EconomyMode economy() {
        return AzuraBedWars.getInstance().getGameManager().getEconomy();
    }
}
