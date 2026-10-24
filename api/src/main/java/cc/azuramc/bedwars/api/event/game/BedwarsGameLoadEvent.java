package cc.azuramc.bedwars.api.event.game;

import cc.azuramc.bedwars.api.game.IGameManager;
import cc.azuramc.bedwars.api.game.map.IMapData;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * 游戏加载事件
 *
 * @author an5w1r@163.com
 */
public abstract class BedwarsGameLoadEvent extends Event {

    private final IGameManager gameManager;
    private final IMapData mapData;

    /**
     * @param gameManager 游戏管理器
     * @param mapData     地图数据
     */
    protected BedwarsGameLoadEvent(@NotNull IGameManager gameManager, @NotNull IMapData mapData) {
        this.gameManager = gameManager;
        this.mapData = mapData;
    }

    /**
     * 获取游戏管理器
     *
     * @return 游戏管理器（不为 null）
     */
    @NotNull
    public IGameManager getGameManager() {
        return gameManager;
    }

    /**
     * 获取地图数据
     *
     * @return 地图数据（不为 null）
     */
    @NotNull
    public IMapData getMapData() {
        return mapData;
    }

    /**
     * 地图加载「之前」触发，可取消以阻止加载。
     */
    public static final class Pre extends BedwarsGameLoadEvent implements Cancellable {

        private static final HandlerList HANDLERS = new HandlerList();

        private boolean cancelled = false;

        public Pre(@NotNull IGameManager gameManager, @NotNull IMapData mapData) {
            super(gameManager, mapData);
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }

        @NotNull
        public static HandlerList getHandlerList() {
            return HANDLERS;
        }

        @Override
        public @NotNull HandlerList getHandlers() {
            return HANDLERS;
        }
    }

    /**
     * 地图加载完成「之后」触发，用于通知。
     */
    public static final class Post extends BedwarsGameLoadEvent {

        private static final HandlerList HANDLERS = new HandlerList();

        public Post(@NotNull IGameManager gameManager, @NotNull IMapData mapData) {
            super(gameManager, mapData);
        }

        @NotNull
        public static HandlerList getHandlerList() {
            return HANDLERS;
        }

        @Override
        public @NotNull HandlerList getHandlers() {
            return HANDLERS;
        }
    }
}
