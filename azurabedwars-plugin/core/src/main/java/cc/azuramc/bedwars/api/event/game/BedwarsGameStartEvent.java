package cc.azuramc.bedwars.api.event.game;

import cc.azuramc.bedwars.game.GameManager;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * @author an5w1r@163.com
 */
@Getter
public class BedwarsGameStartEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * 游戏管理器
     */
    private final GameManager gameManager;

    /**
     * 构造函数
     *
     * @param gameManager 游戏管理器
     */
    public BedwarsGameStartEvent(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

}
