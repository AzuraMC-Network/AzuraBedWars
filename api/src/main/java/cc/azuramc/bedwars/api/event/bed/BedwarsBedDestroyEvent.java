package cc.azuramc.bedwars.api.event.bed;

import cc.azuramc.bedwars.api.game.IGameManager;
import cc.azuramc.bedwars.api.game.IGamePlayer;
import cc.azuramc.bedwars.api.game.IGameTeam;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A player breaks another team's bed. Listen to {@link Pre} or {@link Post}, both are called on the main thread.
 *
 * @author an5w1r@163.com
 */
@ApiStatus.NonExtendable
public abstract class BedwarsBedDestroyEvent extends Event {

    private final IGamePlayer gamePlayer;
    private final IGameTeam gameTeam;
    private final IGameManager gameManager;

    @ApiStatus.Internal
    protected BedwarsBedDestroyEvent(@NotNull IGamePlayer gamePlayer, @NotNull IGameTeam gameTeam, @NotNull IGameManager gameManager) {
        this.gamePlayer = gamePlayer;
        this.gameTeam = gameTeam;
        this.gameManager = gameManager;
    }

    /**
     * @return the player who broke the bed
     */
    @NotNull
    public IGamePlayer getGamePlayer() {
        return gamePlayer;
    }

    /**
     * @return the team that owned the bed
     */
    @NotNull
    public IGameTeam getGameTeam() {
        return gameTeam;
    }

    @NotNull
    public IGameManager getGameManager() {
        return gameManager;
    }

    /**
     * Called before the bed is removed. Cancelling keeps the bed in place.
     */
    public static final class Pre extends BedwarsBedDestroyEvent implements Cancellable {

        private static final HandlerList HANDLERS = new HandlerList();

        private boolean cancelled;

        @ApiStatus.Internal
        public Pre(@NotNull IGamePlayer gamePlayer, @NotNull IGameTeam gameTeam, @NotNull IGameManager gameManager) {
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

        @NotNull
        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }
    }

    /**
     * Called after the bed is removed and before the announcement is sent.
     * The texts can be changed here, null skips that part of the announcement.
     */
    public static final class Post extends BedwarsBedDestroyEvent {

        private static final HandlerList HANDLERS = new HandlerList();

        @Nullable
        private String message;
        @Nullable
        private String title;
        @Nullable
        private String subTitle;

        @ApiStatus.Internal
        public Post(@NotNull IGamePlayer gamePlayer, @NotNull IGameTeam gameTeam, @NotNull IGameManager gameManager,
                    @Nullable String message, @Nullable String title, @Nullable String subTitle) {
            super(gamePlayer, gameTeam, gameManager);
            this.message = message;
            this.title = title;
            this.subTitle = subTitle;
        }

        /**
         * @return the chat message broadcast to every player
         */
        @Nullable
        public String getMessage() {
            return message;
        }

        public void setMessage(@Nullable String message) {
            this.message = message;
        }

        /**
         * @return the title shown to the team that lost its bed
         */
        @Nullable
        public String getTitle() {
            return title;
        }

        public void setTitle(@Nullable String title) {
            this.title = title;
        }

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

        @NotNull
        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }
    }
}
