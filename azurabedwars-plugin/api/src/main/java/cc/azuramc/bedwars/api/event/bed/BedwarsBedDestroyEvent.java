package cc.azuramc.bedwars.api.event.bed;

import cc.azuramc.bedwars.api.game.IGameManager;
import cc.azuramc.bedwars.api.game.IGamePlayer;
import cc.azuramc.bedwars.api.game.IGameTeam;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 床被破坏事件
 *
 * @author an5w1r@163.com
 */
public abstract class BedwarsBedDestroyEvent extends Event {

    private final IGamePlayer gamePlayer;
    private final IGameTeam gameTeam;
    private final IGameManager gameManager;

    /**
     * @param gamePlayer  破坏床的玩家
     * @param gameTeam    被破坏床所属的队伍
     * @param gameManager 游戏管理器
     */
    protected BedwarsBedDestroyEvent(
            @NotNull IGamePlayer gamePlayer,
            @NotNull IGameTeam gameTeam,
            @NotNull IGameManager gameManager
    ) {
        this.gamePlayer = gamePlayer;
        this.gameTeam = gameTeam;
        this.gameManager = gameManager;
    }

    /**
     * 获取破坏床的玩家
     *
     * @return 破坏者（不为 null）
     */
    @NotNull
    public IGamePlayer getGamePlayer() {
        return gamePlayer;
    }

    /**
     * 获取被破坏床所属的队伍
     *
     * @return 队伍（不为 null）
     */
    @NotNull
    public IGameTeam getGameTeam() {
        return gameTeam;
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
     * 床被破坏「之前」触发，可取消以阻止床被破坏。
     */
    public static final class Pre extends BedwarsBedDestroyEvent implements Cancellable {

        private static final HandlerList HANDLERS = new HandlerList();

        private boolean cancelled = false;

        public Pre(
                @NotNull IGamePlayer gamePlayer,
                @NotNull IGameTeam gameTeam,
                @NotNull IGameManager gameManager
        ) {
            super(gamePlayer, gameTeam, gameManager);
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
     * 床被破坏「之后」触发，用于通知。广播用的聊天/标题文本可在此修改。
     */
    public static final class Post extends BedwarsBedDestroyEvent {

        private static final HandlerList HANDLERS = new HandlerList();

        @Nullable
        private String message;
        @Nullable
        private String title;
        @Nullable
        private String subTitle;

        public Post(
                @NotNull IGamePlayer gamePlayer,
                @NotNull IGameTeam gameTeam,
                @NotNull IGameManager gameManager,
                @Nullable String message,
                @Nullable String title,
                @Nullable String subTitle
        ) {
            super(gamePlayer, gameTeam, gameManager);
            this.message = message;
            this.title = title;
            this.subTitle = subTitle;
        }

        /**
         * 获取广播聊天消息
         *
         * @return 聊天消息，可能为 null
         */
        @Nullable
        public String getMessage() {
            return message;
        }

        public void setMessage(@Nullable String message) {
            this.message = message;
        }

        /**
         * 获取标题消息
         *
         * @return 标题，可能为 null
         */
        @Nullable
        public String getTitle() {
            return title;
        }

        public void setTitle(@Nullable String title) {
            this.title = title;
        }

        /**
         * 获取副标题消息
         *
         * @return 副标题，可能为 null
         */
        @Nullable
        public String getSubTitle() {
            return subTitle;
        }

        public void setSubTitle(@Nullable String subTitle) {
            this.subTitle = subTitle;
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
