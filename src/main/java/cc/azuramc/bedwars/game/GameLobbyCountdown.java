package cc.azuramc.bedwars.game;

import cc.azuramc.bedwars.scoreboards.LobbyBoard;
import cc.azuramc.bedwars.utils.SoundUtil;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class GameLobbyCountdown extends BukkitRunnable {
    private final Game game;
    @Getter
    private int countdown = 120;
    private boolean s = false;

    public GameLobbyCountdown(Game game) {
        this.game = game;
    }

    @Override
    public void run() {
        if (game.isForceStart()) {
            cancel();
            return;
        }

        if (!game.hasEnoughPlayers()) {
            s = false;
            game.broadcastMessage("§c人数不足，取消倒计时！");
            countdown = 120;
            game.setGameState(GameState.WAITING);
            game.setGameLobbyCountdown(null);
            LobbyBoard.updateBoard();
            cancel();
        }

        if (countdown == 60 || countdown == 30 || countdown <= 5 && countdown > 0) {
            GamePlayer.getOnlinePlayers().forEach(player -> {
                player.sendMessage("§e游戏将在§c" + countdown + "§e秒后开始！");
                player.sendTitle(1, 20, 1, "§c§l" + countdown, "§e§l准备战斗吧！");
                player.playSound(SoundUtil.get("LEVEL_UP", "ENTITY_PLAYER_LEVELUP"), 1F, 10F);
            });
        }

        if (!s && GamePlayer.getOnlinePlayers().size() >= game.getMaxPlayers() && countdown > 10) {
            s = true;
            countdown = 10;
            game.broadcastMessage("§e游戏人数已满,10秒后开始游戏！");
        }

        if (countdown <= 0) {
            cancel();
            game.start();
            return;
        }

        LobbyBoard.updateBoard();

        GamePlayer.getOnlinePlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();

            player.setLevel(countdown);
            if (countdown == 120) {
                player.setExp(1.0F);
            } else {
                player.setExp(1.0F - ((1.0F / 120) * (120 - countdown)));
            }
        });

        --countdown;
    }
}
