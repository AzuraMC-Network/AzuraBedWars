package cc.azuramc.bedwars.api.event.game;

import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameTeam;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author an5w1r@163.com
 */
@Getter
@Setter
public class BedwarsGameEndEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * 游戏管理器
     */
    private final GameManager gameManager;
    /**
     * 获胜队伍（可能为null，表示平局）
     */
    @Nullable
    private final GameTeam winner;
    /**
     * 存活的队伍列表
     */
    private final List<GameTeam> aliveTeams;
    /**
     * 聊天消息
     */
    private String message;
    /**
     * 标题消息
     */
    private String title;
    /**
     * 副标题消息
     */
    private String subTitle;

    /**
     * 构造函数
     *
     * @param gameManager 游戏管理器
     */
    public BedwarsGameEndEvent(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
        this.winner = gameManager.getWinner();
        this.aliveTeams = List.copyOf(gameManager.getAliveTeams());
        this.message = winner != null ? "§a游戏结束！获胜队伍：" + winner.getName() : "§e游戏结束！平局！";
        this.title = "§6游戏结束";
        this.subTitle = winner != null ? "§a获胜队伍：" + winner.getName() : "§e平局！";
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
