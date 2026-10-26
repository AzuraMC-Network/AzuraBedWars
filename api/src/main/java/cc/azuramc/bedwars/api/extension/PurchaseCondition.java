package cc.azuramc.bedwars.api.extension;

import cc.azuramc.bedwars.api.game.IGamePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A purchase requirement, listed under {@code requires.conditions} of a shop item
 *
 * @param <S> settings bound from the {@code settings} section of the condition
 * @author an5w1r@163.com
 */
@FunctionalInterface
public interface PurchaseCondition<S> {

    /**
     * Called before the price is paid
     *
     * @return null to allow the purchase, otherwise the message sent to the player
     */
    @Nullable
    String check(@NotNull IGamePlayer player, @NotNull S settings);
}
