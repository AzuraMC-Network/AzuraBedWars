package cc.azuramc.bedwars.api.extension;

import cc.azuramc.bedwars.api.game.IGamePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Runs instead of giving the item when a shop item with {@code action: <plugin>:<id>} is bought.
 * The {@code item} of the shop item is only its icon then
 *
 * @param <S> settings bound from the {@code settings} section of the shop item
 * @author an5w1r@163.com
 */
@FunctionalInterface
public interface PurchaseAction<S> {

    /**
     * Called after the price is paid
     */
    void purchase(@NotNull IGamePlayer player, @NotNull S settings);
}
