package cc.azuramc.bedwars.upgrade.gui;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.economy.PriceCost;
import cc.azuramc.bedwars.gui.base.GUIData;
import cc.azuramc.bedwars.gui.base.CustomGUI;
import cc.azuramc.bedwars.gui.base.action.GUIAction;
import cc.azuramc.bedwars.upgrade.TeamUpgrade;
import cc.azuramc.bedwars.upgrade.TeamUpgrades;
import cc.azuramc.bedwars.upgrade.TrapType;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author an5w1r@163.com
 */
public class TeamUpgradeGUI extends CustomGUI {

    private static final String[] ROMAN = {"I", "II", "III", "IV", "V"};
    private static final String[] CHINESE_NUMBERS = {"一", "二", "三"};
    private static final int TRAP_QUEUE_SLOT = 39;

    public TeamUpgradeGUI(GamePlayer gamePlayer, GameManager gameManager) {
        super(gamePlayer, "§8团队升级", 54);

        GameTeam gameTeam = Objects.requireNonNull(gamePlayer.getGameTeam());
        TeamUpgrades upgrades = gameTeam.getUpgrades();

        for (int i = 27; i < 36; i++) {
            setItem(i, XMaterial.GRAY_STAINED_GLASS_PANE.parseItem(), new GUIAction(0, () -> {
            }, false));
        }

        for (TeamUpgrade upgrade : TeamUpgrade.values()) {
            ItemBuilder icon = new ItemBuilder().setType(upgrade.getIcon().get())
                    .setDisplayName("§a" + upgrade.getDisplayName())
                    .setLores(upgradeLore(upgrades, upgrade, gameManager))
                    .addItemFlag(ItemFlag.HIDE_ATTRIBUTES);
            if (upgrades.isMaxed(upgrade)) {
                icon.addEnchant(XEnchantment.EFFICIENCY.get(), 1).addItemFlag(ItemFlag.HIDE_ENCHANTS);
            }
            setItem(upgrade.getSlot(), icon.getItem(), new GUIAction(0, () -> {
                if (upgrades.buy(gamePlayer, upgrade)) {
                    refresh(gameTeam);
                }
            }, false));
        }

        String nextTrapPrice = gameManager.getEconomy().formatPrice(PriceCost.teamUpgrade(upgrades.getNextTrapPrice()));
        for (TrapType trap : TrapType.values()) {
            List<String> lore = new ArrayList<>(trap.getDescription());
            lore.add("");
            lore.add("§7价格: §b" + nextTrapPrice);
            lore.add("");
            boolean active = upgrades.hasTrap(trap);
            if (active) {
                lore.add("§a已激活");
            } else if (upgrades.isTrapQueueFull()) {
                lore.add("§c陷阱已满");
            } else {
                lore.add("§e点击购买");
            }

            ItemBuilder icon = new ItemBuilder().setType(trap.getIcon().get())
                    .setDisplayName("§a" + trap.getDisplayName())
                    .setLores(lore)
                    .addItemFlag(ItemFlag.HIDE_ATTRIBUTES);
            if (active) {
                icon.addEnchant(XEnchantment.EFFICIENCY.get(), 1).addItemFlag(ItemFlag.HIDE_ENCHANTS);
            }
            setItem(trap.getSlot(), icon.getItem(), new GUIAction(0, () -> {
                if (upgrades.buyTrap(gamePlayer, trap)) {
                    refresh(gameTeam);
                }
            }, false));
        }

        List<TrapType> queue = upgrades.getTraps();
        for (int i = 0; i < TeamUpgrades.MAX_TRAPS; i++) {
            ItemBuilder icon;
            if (i < queue.size()) {
                TrapType trap = queue.get(i);
                icon = new ItemBuilder().setType(trap.getIcon().get())
                        .setDisplayName("§a陷阱#" + (i + 1) + ": " + trap.getDisplayName());
            } else {
                icon = new ItemBuilder().setType(XMaterial.GRAY_STAINED_GLASS.get())
                        .setDisplayName("§c陷阱#" + (i + 1) + ": 没有陷阱！");
            }
            icon.addItemFlag(ItemFlag.HIDE_ATTRIBUTES).setLores(
                    "§7第" + CHINESE_NUMBERS[i] + "个敌人进入己方基地后",
                    "§7会触发该陷阱！",
                    "",
                    "§7购买的陷阱会再次排列，具体",
                    "§7费用取决已排列的陷阱数量。",
                    "",
                    "§7下一个陷阱: §b" + nextTrapPrice);
            setItem(TRAP_QUEUE_SLOT + i, icon.getItem(), new GUIAction(0, () -> {
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

    private static List<String> upgradeLore(TeamUpgrades upgrades, TeamUpgrade upgrade, GameManager gameManager) {
        int level = upgrades.getLevel(upgrade);
        List<String> lore = new ArrayList<>();
        lore.add(upgrade.getDescription());
        lore.add("");
        for (int tier = 0; tier < upgrade.getMaxLevel(); tier++) {
            String color = tier < level ? "§a" : tier == level ? "§e" : "§7";
            String price = gameManager.getEconomy().formatPrice(PriceCost.teamUpgrade(upgrade.getPrices()[tier]));
            lore.add(color + "等级 " + (tier + 1) + "：" + upgrade.getTierNames()[tier] + "，§b" + price);
        }
        lore.add("");

        if (upgrade.getMaxLevel() == 1) {
            lore.add(level == 0 ? "§e点击购买" : "§a已购买");
        } else if (level < upgrade.getMaxLevel()) {
            lore.add("§e点击升级到等级 " + ROMAN[level]);
        } else {
            lore.add("§a已达到最高等级");
        }
        return lore;
    }
}
