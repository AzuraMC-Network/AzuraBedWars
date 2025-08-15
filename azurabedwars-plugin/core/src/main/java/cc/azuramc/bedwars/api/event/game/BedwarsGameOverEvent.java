package cc.azuramc.bedwars.api.event.game;

import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameTeam;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author an5w1r@163.com
 */
@Getter
@Setter
public class BedwarsGameOverEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * 游戏管理器
     */
    private final GameManager gameManager;
    /**
     * 获胜队伍（可能为null，表示平局或无获胜者）
     */
    @Nullable
    private final GameTeam winnerTeam;

    /**
     * 构造函数
     *
     * @param gameManager 游戏管理器
     */
    public BedwarsGameOverEvent(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
        this.winnerTeam = gameManager.getWinner();
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

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

}
