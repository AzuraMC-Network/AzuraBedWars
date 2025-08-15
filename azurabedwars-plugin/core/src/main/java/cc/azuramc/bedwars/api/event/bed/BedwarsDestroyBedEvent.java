package cc.azuramc.bedwars.api.event.bed;

import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
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
public class BedwarsDestroyBedEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * 破坏床的玩家
     */
    private final GamePlayer gamePlayer;

    /**
     * 游戏管理器/竞技场
     */
    private final GameManager arena;

    /**
     * 被破坏床所属的队伍
     */
    private final GameTeam gameTeam;

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
     * 事件是否被取消
     */
    private boolean cancelled = false;

    /**
     * 构造函数
     *
     * @param gamePlayer  破坏床的玩家
     * @param gameTeam    被破坏床所属队伍
     * @param gameManager 游戏管理器
     * @param message     聊天消息
     * @param title       标题消息
     * @param subTitle    副标题消息
     */
    public BedwarsDestroyBedEvent(GamePlayer gamePlayer, GameTeam gameTeam, GameManager gameManager,
                                  String message, String title, String subTitle) {
        this.gamePlayer = gamePlayer;
        this.gameTeam = gameTeam;
        this.arena = gameManager;
        this.message = message;
        this.title = title;
        this.subTitle = subTitle;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * 获取事件是否被取消
     *
     * @return 是否被取消
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * 设置事件是否被取消
     *
     * @param cancelled 是否取消
     */
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }


}
