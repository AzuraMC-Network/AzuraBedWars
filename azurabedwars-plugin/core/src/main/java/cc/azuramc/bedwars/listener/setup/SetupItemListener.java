package cc.azuramc.bedwars.listener.setup;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * 地图设置物品监听器
 *
 * @author an5w1r@163.com
 */
public class SetupItemListener implements Listener {

    private final AzuraBedWars plugin;

    public SetupItemListener(AzuraBedWars plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player);

        // 检查玩家是否有权限
        if (!player.hasPermission("azurabedwars.admin")) {
            return;
        }

        // 检查互动类型
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        // 获取物品
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        if (!item.hasItemMeta()) {
            return;
        }

        // 获取物品名称
        ItemMeta meta = item.getItemMeta();
        String itemName;
        if (meta != null) {
            itemName = meta.getDisplayName();
            itemName = ChatColor.stripColor(itemName);
        } else {
            return;
        }

        // 获取地图上下文
        String mapName = plugin.getSetupItemManager().getPlayerMapContext(player.getName());

        if (mapName == null || mapName.isEmpty()) {
            return;
        }

        if (plugin.getSetupItemManager().handleItemClick(player, itemName, mapName)) {
            event.setCancelled(true);
        }
    }
}
