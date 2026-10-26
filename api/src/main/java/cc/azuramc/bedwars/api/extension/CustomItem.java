package cc.azuramc.bedwars.api.extension;

import cc.azuramc.bedwars.api.game.IGamePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * An item sold by the shop with {@code custom: <plugin>:<id>}
 *
 * @param <S> settings bound from the {@code settings} section of the shop item
 * @author an5w1r@163.com
 */
@FunctionalInterface
public interface CustomItem<S> {

    /**
     * Called each time the item is given, on purchase and again on respawn when the shop item is kept
     *
     * @param item the {@code item} of the shop item, already tagged as this custom item
     * @return the stack to give, change {@code item} rather than building a new stack so the tag stays
     */
    @NotNull
    default ItemStack create(@NotNull IGamePlayer player, @NotNull ItemStack item, @NotNull S settings) {
        return item;
    }

    /**
     * Called on every click with the item in hand. The event is left alone unless this cancels it
     */
    void onUse(@NotNull ItemUseContext context, @NotNull S settings);
}
