package cc.azuramc.bedwars.api.event.game;

import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.map.MapData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * @author an5w1r@163.com
 */
@Getter
@Setter
public class BedwarsGameLoadEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * 游戏管理器
     */
    private final GameManager gameManager;

    /**
     * 地图数据
     */
    private final MapData mapData;

    /**
     * 事件是否被取消
     */
    private boolean cancelled = false;

    /**
     * 构造函数
     *
     * @param gameManager 游戏管理器
     * @param mapData     地图数据
     */
    public BedwarsGameLoadEvent(@NotNull GameManager gameManager,
                                @NotNull MapData mapData) {
        this.gameManager = gameManager;
        this.mapData = mapData;
    }

    /**
     * 获取游戏管理器
     *
     * @return 游戏管理器
     */
    @NotNull
    public GameManager getGameManager() {
        return gameManager;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

}
