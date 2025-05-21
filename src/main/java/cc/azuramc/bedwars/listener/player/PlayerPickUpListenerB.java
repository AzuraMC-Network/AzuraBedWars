package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.game.GamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * 处理新版本Minecraft的物品拾取事件
 *
 * @author an5w1r@163.com
 */
public class PlayerPickUpListenerB implements Listener {

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        // 检查拾取实体是否为玩家
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack itemStack = event.getItem().getItemStack();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        
        // 基本条件检查
        if (PickupItemHandler.isPickupDisabled(player, gamePlayer)) {
            event.setCancelled(true);
            return;
        }
        
        // 处理床的拾取
        if (PickupItemHandler.handleBedPickup(itemStack, event.getItem())) {
            event.setCancelled(true);
            return;
        }
        
        // 处理剑的拾取
        PickupItemHandler.handleSwordPickup(itemStack, player);
        
        // 检查AFK玩家拾取资源
        if (PickupItemHandler.checkAfkResourcePickup(itemStack, gamePlayer)) {
            event.setCancelled(true);
            return;
        }
        
        // 处理金铁锭拾取
        if (PickupItemHandler.handleIngotPickup(itemStack, player, gamePlayer, event.getItem())) {
            event.setCancelled(true);
            return;
        }
        
        // 处理钻石拾取
        if (PickupItemHandler.handleDiamondPickup(itemStack, player, gamePlayer, event.getItem())) {
            event.setCancelled(true);
            return;
        }
        
        // 处理绿宝石拾取
        if (PickupItemHandler.handleEmeraldPickup(itemStack, player, gamePlayer, event.getItem())) {
            event.setCancelled(true);
        }
    }
}
