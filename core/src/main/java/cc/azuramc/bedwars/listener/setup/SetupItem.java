package cc.azuramc.bedwars.listener.setup;

import cc.azuramc.bedwars.command.admin.MapCommand;
import cc.azuramc.bedwars.util.MessageUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

/**
 * Hotbar tools of /setup, each runs the matching /map subcommand
 *
 * @author an5w1r@163.com
 */
public enum SetupItem {
    WAITING(XMaterial.COMPASS, "&e设置等待大厅", "在脚下设置等待大厅", MapCommand::setWaiting),
    RESPAWN(XMaterial.BEACON, "&e设置观战重生点", "在脚下设置观战重生点", MapCommand::setRespawn),
    POS1(XMaterial.GOLDEN_AXE, "&c设置边界1", "在脚下设置地图边界的一角", MapCommand::setPos1),
    POS2(XMaterial.DIAMOND_AXE, "&c设置边界2", "在脚下设置地图边界的另一角", MapCommand::setPos2),
    BASE(XMaterial.RED_BED, "&d添加队伍基地", "站在出生点 附近的羊毛决定队伍颜色", MapCommand::addBase),
    BASE_DROP(XMaterial.IRON_INGOT, "&7添加基地资源点", "在脚下添加基地资源点", (command, player, map) -> command.addDrop(player, map, "BASE")),
    DIAMOND_DROP(XMaterial.DIAMOND, "&b添加钻石资源点", "在脚下添加钻石资源点", (command, player, map) -> command.addDrop(player, map, "DIAMOND")),
    EMERALD_DROP(XMaterial.EMERALD, "&a添加绿宝石资源点", "在脚下添加绿宝石资源点", (command, player, map) -> command.addDrop(player, map, "EMERALD")),
    ITEM_SHOP(XMaterial.CHEST, "&6添加物品商店", "在脚下添加物品商店", (command, player, map) -> command.addShop(player, map, "ITEM")),
    UPGRADE_SHOP(XMaterial.ANVIL, "&d添加团队升级商店", "在脚下添加团队升级商店", (command, player, map) -> command.addShop(player, map, "UPGRADE")),
    REMOVE(XMaterial.BARRIER, "&c删除最近的点", "删除附近最近的基地 资源点或商店", MapCommand::remove),
    CHECK(XMaterial.BOOK, "&a检查地图", "列出地图还缺什么", MapCommand::check);

    private final XMaterial material;
    private final String name;
    private final String lore;
    private final Action action;

    SetupItem(XMaterial material, String name, String lore, Action action) {
        this.material = material;
        this.name = MessageUtil.color(name);
        this.lore = MessageUtil.color("&7" + lore);
        this.action = action;
    }

    public static SetupItem of(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return null;
        }
        String displayName = item.getItemMeta().getDisplayName();
        for (SetupItem setupItem : values()) {
            if (setupItem.name.equals(displayName)) {
                return setupItem;
            }
        }
        return null;
    }

    public ItemStack createItem() {
        ItemStack item = material.parseItem();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Collections.singletonList(lore));
        item.setItemMeta(meta);
        return item;
    }

    public void run(MapCommand command, Player player, String mapName) {
        action.run(command, player, mapName);
    }

    private interface Action {
        void run(MapCommand command, Player player, String mapName);
    }
}
