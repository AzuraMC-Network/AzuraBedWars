package cc.azuramc.bedwars.api.extension;

import cc.azuramc.bedwars.api.game.IGamePlayer;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * @author an5w1r@163.com
 */
@ApiStatus.NonExtendable
public interface ItemUseContext {

    @NotNull
    IGamePlayer getPlayer();

    /**
     * @return the stack in hand
     */
    @NotNull
    ItemStack getItem();

    @NotNull
    PlayerInteractEvent getEvent();

    /**
     * Takes one item from the stack in hand
     */
    void consume();
}
