package cc.azuramc.bedwars.example.grapple;

import cc.azuramc.bedwars.api.extension.CustomItem;
import cc.azuramc.bedwars.api.extension.ItemUseContext;
import cc.azuramc.bedwars.api.game.IGamePlayer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author an5w1r@163.com
 */
public final class GrappleHook implements CustomItem<GrappleSettings> {

    private final Map<UUID, Long> lastPull = new HashMap<>();

    @NotNull
    @Override
    public ItemStack create(@NotNull IGamePlayer player, @NotNull ItemStack item, @NotNull GrappleSettings settings) {
        ItemMeta meta = item.getItemMeta();
        meta.setLore(settings.lore);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void onUse(@NotNull ItemUseContext context, @NotNull GrappleSettings settings) {
        Action action = context.getEvent().getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        // keeps the fishing rod from casting
        context.getEvent().setCancelled(true);
        Player player = context.getEvent().getPlayer();
        long now = System.currentTimeMillis();
        Long last = lastPull.get(player.getUniqueId());
        long readyIn = last == null ? 0 : last + (long) (settings.cooldown * 1000) - now;
        if (readyIn > 0) {
            player.sendMessage(settings.cooling.replace("{seconds}", String.valueOf((readyIn + 999) / 1000)));
            return;
        }
        Block target = player.getTargetBlock((Set<Material>) null, settings.range);
        if (target == null || target.getType() == Material.AIR) {
            player.sendMessage(settings.miss);
            return;
        }
        Vector pull = target.getLocation().add(0.5, 0.5, 0.5).toVector().subtract(player.getLocation().toVector());
        pull.normalize().multiply(settings.strength);
        pull.setY(Math.max(pull.getY(), 0.4));
        player.setVelocity(pull);
        player.setFallDistance(0);
        lastPull.put(player.getUniqueId(), now);
    }
}
