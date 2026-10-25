package cc.azuramc.bedwars.game.item.special;

import cc.azuramc.bedwars.AzuraBedWars;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Built-in items with behavior, recognized by the {@code azurabedwars:id} tag the shop writes
 *
 * @author an5w1r@163.com
 */
public final class SpecialItems implements Listener {

    public static final String FIREBALL = "fireball";
    public static final String TNT = "tnt";
    public static final String POPUP_TOWER = "popup_tower";
    public static final String RESCUE_PLATFORM = "rescue_platform";
    public static final String WARP_POWDER = "warp_powder";
    public static final String BRIDGE_EGG = "bridge_egg";
    public static final String SILVERFISH = "silverfish";
    public static final String DREAM_DEFENDER = "dream_defender";
    public static final String MAGIC_MILK = "magic_milk";
    public static final String INVISIBILITY = "invisibility";
    public static final String SPEED_WOOL = "speed_wool";

    public static final List<String> IDS = Collections.unmodifiableList(Arrays.asList(FIREBALL, TNT, POPUP_TOWER,
            RESCUE_PLATFORM, WARP_POWDER, BRIDGE_EGG, SILVERFISH, DREAM_DEFENDER, MAGIC_MILK, INVISIBILITY, SPEED_WOOL));

    private static final String TAG_KEY = "id";
    private static final String PROJECTILE_METADATA = "azurabedwars:special";

    /** Item the player last used, a throw spawns its projectile within that interaction */
    private final Map<UUID, ItemStack> thrown = new HashMap<>();

    public static ItemStack tag(ItemStack item, String special) {
        return AzuraBedWars.getInstance().getNmsAccess().setItemTag(item, TAG_KEY, special);
    }

    @Nullable
    public static String of(@Nullable ItemStack item) {
        return item == null ? null : AzuraBedWars.getInstance().getNmsAccess().getItemTag(item, TAG_KEY);
    }

    public static boolean is(@Nullable ItemStack item, String special) {
        return special.equals(of(item));
    }

    public static boolean isProjectile(Entity entity, String special) {
        return is(thrownItem(entity), special);
    }

    /**
     * @return a single item of the stack the projectile was thrown from, null when it was not special
     */
    @Nullable
    public static ItemStack thrownItem(Entity entity) {
        for (MetadataValue value : entity.getMetadata(PROJECTILE_METADATA)) {
            if (value.value() instanceof ItemStack) {
                return ((ItemStack) value.value()).clone();
            }
        }
        return null;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (of(event.getItem()) != null) {
            ItemStack item = event.getItem().clone();
            item.setAmount(1);
            thrown.put(event.getPlayer().getUniqueId(), item);
        } else {
            thrown.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        if (!(projectile.getShooter() instanceof Player)) {
            return;
        }
        ItemStack item = thrown.remove(((Player) projectile.getShooter()).getUniqueId());
        if (item != null) {
            projectile.setMetadata(PROJECTILE_METADATA, new FixedMetadataValue(AzuraBedWars.getInstance().getPlugin(), item));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        thrown.remove(event.getPlayer().getUniqueId());
    }
}
