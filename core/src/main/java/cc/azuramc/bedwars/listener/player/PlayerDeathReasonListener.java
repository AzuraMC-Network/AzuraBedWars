package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Objects;

/**
 * @author an5w1r@163.com
 */
public class PlayerDeathReasonListener implements Listener {

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    /**
     * 处理玩家死亡消息
     * @param event PlayerDeathEvent
     */
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();

        GamePlayer gamePlayer = GamePlayer.get(player);
        GamePlayer gameKiller = GamePlayer.get(killer);

        if (gamePlayer == null || gameKiller == null) {
            return;
        }

        switch (Objects.requireNonNull(player.getLastDamageCause()).getCause()) {
            case ENTITY_ATTACK -> {
                gameKiller.sendMessage("&e你击杀了 " + gamePlayer.getGameTeam().getColor() + gamePlayer.getNickName());
                gamePlayer.sendMessage("&e你被 " + gameKiller.getGameTeam().getColor() + gameKiller.getNickName() + " &e击杀了");
            }

            case FALL -> gameManager.broadcastMessage(gamePlayer.getGameTeam().getColor() + gamePlayer.getNickName() + " &e落地过猛");

            case VOID -> gameManager.broadcastMessage(gamePlayer.getGameTeam().getColor() + gamePlayer.getNickName() + " &e跌入虚空");

            case FIRE -> gameManager.broadcastMessage(gamePlayer.getGameTeam().getColor() + gamePlayer.getNickName() + " &e欲火焚身");

            case ENTITY_EXPLOSION -> gameManager.broadcastMessage(gamePlayer.getGameTeam().getColor() + gamePlayer.getNickName() + " &e被炸死了");

            default -> gameManager.broadcastMessage(gamePlayer.getGameTeam().getColor() + gamePlayer.getNickName() + "死亡");
        }

    }
}
