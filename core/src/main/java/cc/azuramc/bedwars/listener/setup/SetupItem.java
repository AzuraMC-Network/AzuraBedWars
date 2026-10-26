package cc.azuramc.bedwars.listener.setup;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.command.admin.MapCommand;
import cc.azuramc.bedwars.config.object.LangConfig;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.function.Function;

/**
 * Hotbar tools of /setup, each runs the matching /map subcommand
 *
 * @author an5w1r@163.com
 */
public enum SetupItem {
    WAITING(XMaterial.COMPASS, LangConfig.Setup::getWaiting, MapCommand::setWaiting),
    RESPAWN(XMaterial.BEACON, LangConfig.Setup::getRespawn, MapCommand::setRespawn),
    POS1(XMaterial.GOLDEN_AXE, LangConfig.Setup::getPos1, MapCommand::setPos1),
    POS2(XMaterial.DIAMOND_AXE, LangConfig.Setup::getPos2, MapCommand::setPos2),
    BASE(XMaterial.RED_BED, LangConfig.Setup::getBase, MapCommand::addBase),
    BASE_DROP(XMaterial.IRON_INGOT, LangConfig.Setup::getBaseDrop, (command, player, map) -> command.addDrop(player, map, "BASE")),
    DIAMOND_DROP(XMaterial.DIAMOND, LangConfig.Setup::getDiamondDrop, (command, player, map) -> command.addDrop(player, map, "DIAMOND")),
    EMERALD_DROP(XMaterial.EMERALD, LangConfig.Setup::getEmeraldDrop, (command, player, map) -> command.addDrop(player, map, "EMERALD")),
    ITEM_SHOP(XMaterial.CHEST, LangConfig.Setup::getItemShop, (command, player, map) -> command.addShop(player, map, "ITEM")),
    UPGRADE_SHOP(XMaterial.ANVIL, LangConfig.Setup::getUpgradeShop, (command, player, map) -> command.addShop(player, map, "UPGRADE")),
    REMOVE(XMaterial.BARRIER, LangConfig.Setup::getRemove, MapCommand::remove),
    CHECK(XMaterial.BOOK, LangConfig.Setup::getCheck, MapCommand::check);

    private final XMaterial material;
    private final Function<LangConfig.Setup, LangConfig.Tool> text;
    private final Action action;

    SetupItem(XMaterial material, Function<LangConfig.Setup, LangConfig.Tool> text, Action action) {
        this.material = material;
        this.text = text;
        this.action = action;
    }

    public static SetupItem of(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return null;
        }
        String displayName = item.getItemMeta().getDisplayName();
        for (SetupItem setupItem : values()) {
            if (setupItem.tool().getName().equals(displayName)) {
                return setupItem;
            }
        }
        return null;
    }

    public ItemStack createItem() {
        ItemStack item = material.parseItem();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(tool().getName());
        meta.setLore(Collections.singletonList(ChatColor.GRAY + tool().getLore()));
        item.setItemMeta(meta);
        return item;
    }

    private LangConfig.Tool tool() {
        return text.apply(AzuraBedWars.getInstance().getLang().getSetup());
    }

    public void run(MapCommand command, Player player, String mapName) {
        action.run(command, player, mapName);
    }

    private interface Action {
        void run(MapCommand command, Player player, String mapName);
    }
}
