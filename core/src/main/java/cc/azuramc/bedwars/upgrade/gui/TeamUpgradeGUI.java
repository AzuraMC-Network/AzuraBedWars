package cc.azuramc.bedwars.upgrade.gui;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.config.object.UpgradeConfig;
import cc.azuramc.bedwars.config.object.shop.ItemSpec;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.gui.base.CustomGUI;
import cc.azuramc.bedwars.gui.base.GUIData;
import cc.azuramc.bedwars.gui.base.action.GUIAction;
import cc.azuramc.bedwars.shop.ItemSpecs;
import cc.azuramc.bedwars.shop.ShopCatalog;
import cc.azuramc.bedwars.shop.ShopPurchase;
import cc.azuramc.bedwars.upgrade.TeamUpgrades;
import cc.azuramc.bedwars.upgrade.UpgradeEffects;
import cc.azuramc.bedwars.upgrade.UpgradeValidator;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author an5w1r@163.com
 */
public class TeamUpgradeGUI extends CustomGUI {

    private static final String[] ROMAN = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    public TeamUpgradeGUI(GamePlayer gamePlayer, GameManager gameManager) {
        this(gamePlayer, gameManager, AzuraBedWars.getInstance().getUpgradeConfig());
    }

    private TeamUpgradeGUI(GamePlayer gamePlayer, GameManager gameManager, UpgradeConfig config) {
        super(gamePlayer, config.getTitle(), 54);

        GameTeam gameTeam = Objects.requireNonNull(gamePlayer.getGameTeam());
        TeamUpgrades upgrades = gameTeam.getUpgrades();
        UpgradeConfig.Text text = config.getText();

        for (Map.Entry<String, ItemSpec> entry : config.getDecor().entrySet()) {
            ItemStack decor = ItemSpecs.build(entry.getValue(), null);
            if (decor != null && entry.getKey().length() == 1) {
                for (int slot : ShopCatalog.slots(config.getLayout(), entry.getKey().charAt(0))) {
                    setItem(slot, decor, new GUIAction(0, () -> {
                    }, false));
                }
            }
        }

        for (Map.Entry<String, UpgradeConfig.Upgrade> entry : config.getUpgrades().entrySet()) {
            String id = UpgradeValidator.upgradeId(entry.getKey(), entry.getValue());
            UpgradeConfig.Upgrade upgrade = entry.getValue();
            ItemStack base = ItemSpecs.build(upgrade.getIcon(), null);
            if (base == null || entry.getKey().length() != 1) {
                continue;
            }
            ItemBuilder icon = new ItemBuilder().setItemStack(base)
                    .setDisplayName(upgrade.getName())
                    .setLores(upgradeLore(upgrades, id, upgrade, text, gameManager))
                    .addItemFlag(ItemFlag.HIDE_ATTRIBUTES);
            if (upgrades.isMaxed(id, upgrade)) {
                icon.addEnchant(XEnchantment.EFFICIENCY.get(), 1).addItemFlag(ItemFlag.HIDE_ENCHANTS);
            }
            ItemStack item = icon.getItem();
            for (int slot : ShopCatalog.slots(config.getLayout(), entry.getKey().charAt(0))) {
                setItem(slot, item, new GUIAction(0, () -> {
                    if (upgrades.buy(gamePlayer, id, upgrade)) {
                        refresh(gameTeam);
                    }
                }, false));
            }
        }

        String nextTrapPrice = gameManager.getEconomy().formatPrice(upgrades.getNextTrapPrice());
        for (Map.Entry<String, UpgradeConfig.Trap> entry : config.getTraps().getTypes().entrySet()) {
            String id = UpgradeValidator.trapId(entry.getKey(), entry.getValue());
            UpgradeConfig.Trap trap = entry.getValue();
            ItemStack base = ItemSpecs.build(trap.getIcon(), null);
            if (base == null || entry.getKey().length() != 1) {
                continue;
            }
            List<String> lore = new ArrayList<>(trap.getDescription());
            lore.add("");
            lore.add(text.getTrapPrice().replace("{price}", nextTrapPrice));
            lore.add("");
            boolean active = upgrades.hasTrap(id);
            lore.add(active ? text.getTrapActive() : upgrades.isTrapQueueFull() ? text.getTrapFull() : text.getBuy());

            ItemBuilder icon = new ItemBuilder().setItemStack(base)
                    .setDisplayName(trap.getName())
                    .setLores(lore)
                    .addItemFlag(ItemFlag.HIDE_ATTRIBUTES);
            if (active) {
                icon.addEnchant(XEnchantment.EFFICIENCY.get(), 1).addItemFlag(ItemFlag.HIDE_ENCHANTS);
            }
            ItemStack item = icon.getItem();
            for (int slot : ShopCatalog.slots(config.getLayout(), entry.getKey().charAt(0))) {
                setItem(slot, item, new GUIAction(0, () -> {
                    if (upgrades.buyTrap(gamePlayer, id)) {
                        refresh(gameTeam);
                    }
                }, false));
            }
        }

        List<String> queue = upgrades.getTraps();
        List<Integer> queueSlots = ShopCatalog.slots(config.getLayout(), UpgradeValidator.QUEUE_SLOT);
        for (int i = 0; i < queueSlots.size() && i < config.getTraps().getQueueSize(); i++) {
            UpgradeConfig.Trap trap = i < queue.size() ? UpgradeEffects.trap(config, queue.get(i)) : null;
            ItemStack base = trap != null ? ItemSpecs.build(trap.getIcon(), null) : ItemSpecs.build(text.getQueueEmpty(), null);
            String index = String.valueOf(i + 1);
            String name = trap != null ? text.getQueueName().replace("{index}", index).replace("{name}", trap.getName())
                    : text.getQueueEmptyName().replace("{index}", index);
            String ordinal = i < text.getOrdinals().size() ? text.getOrdinals().get(i) : index;
            List<String> lore = new ArrayList<>();
            for (String line : text.getQueueLore()) {
                lore.add(line.replace("{ordinal}", ordinal).replace("{price}", nextTrapPrice));
            }
            ItemStack icon = new ItemBuilder().setItemStack(base != null ? base : XMaterial.GRAY_STAINED_GLASS.parseItem())
                    .setDisplayName(name)
                    .setLores(lore)
                    .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .getItem();
            setItem(queueSlots.get(i), icon, new GUIAction(0, () -> {
            }, false));
        }
    }

    public static void refresh(GameTeam gameTeam) {
        for (GamePlayer gamePlayer : gameTeam.getAlivePlayers()) {
            Player player = gamePlayer.getPlayer();
            if (player != null && GUIData.getCURRENT_GUI().get(player) instanceof TeamUpgradeGUI) {
                new TeamUpgradeGUI(gamePlayer, gameTeam.getGameManager()).open();
            }
        }
    }

    private static List<String> upgradeLore(TeamUpgrades upgrades, String id, UpgradeConfig.Upgrade upgrade,
                                            UpgradeConfig.Text text, GameManager gameManager) {
        int owned = upgrades.getLevel(id);
        int max = upgrade.getLevels().size();
        List<String> lore = new ArrayList<>(upgrade.getDescription());
        lore.add("");
        for (int level = 0; level < max; level++) {
            UpgradeConfig.Level tier = upgrade.getLevels().get(level);
            String template = level < owned ? text.getTierOwned() : level == owned ? text.getTierNext() : text.getTierLocked();
            String price = gameManager.getEconomy().formatPrice(ShopPurchase.cost(tier.getPrice(), tier.getXp()));
            lore.add(template.replace("{level}", String.valueOf(level + 1)).replace("{name}", tier.getName()).replace("{price}", price));
        }
        lore.add("");

        if (max == 1) {
            lore.add(owned == 0 ? text.getBuy() : text.getBought());
        } else if (owned < max) {
            lore.add(text.getUpgrade().replace("{level}", owned < ROMAN.length ? ROMAN[owned] : String.valueOf(owned + 1)));
        } else {
            lore.add(text.getMaxed());
        }
        return lore;
    }
}
