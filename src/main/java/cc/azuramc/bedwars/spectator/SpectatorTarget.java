package cc.azuramc.bedwars.spectator;

import cc.azuramc.bedwars.game.GamePlayer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class SpectatorTarget {
    private final DecimalFormat df = new DecimalFormat("0.0");
    private final GamePlayer gamePlayer;
    private GamePlayer gameTarget;
    private final Player player;
    private Player target;

    public SpectatorTarget(GamePlayer gamePlayer, GamePlayer gameTarget) {
        this.gamePlayer = gamePlayer;
        this.player = gamePlayer.getPlayer();
        this.gameTarget = gameTarget;
    }

    public GamePlayer getPlayer() {
        return gamePlayer;
    }

    public GamePlayer getTarget() {
        return gameTarget;
    }

    public void setTarget(GamePlayer gameTarget) {
        this.gameTarget = gameTarget;
        this.target = gameTarget.getPlayer();
    }

    public void sendTip() {
        if (check()) {
            return;
        }

        if ((player.getSpectatorTarget() != null) && (player.getSpectatorTarget().equals(target))) {
            gamePlayer.sendActionBar(formatSpectatorTip(player, target, true) + "  §a点击左键打开菜单  §c按Shift退出");
            return;
        }

        if (!player.getWorld().equals(target.getWorld())) {
            gamePlayer.sendActionBar("§c§l目标已丢失或不在同一个世界");
            return;
        }

        gamePlayer.sendActionBar(formatSpectatorTip(player, target, false));
    }

    public void autoTp() {
        if (check()) {
            return;
        }

        if (SpectatorSettings.get(gamePlayer).getOption(SpectatorSettings.Option.AUTOTP)) {
            if ((!player.getWorld().equals(target.getWorld())) || (player.getLocation().distance(target.getLocation()) >= 20D)) {
                player.teleport(target);

                if (SpectatorSettings.get(gamePlayer).getOption(SpectatorSettings.Option.FIRSTPERSON)) {
                    gamePlayer.sendTitle(0, 20, 0, "§a正在旁观§7" + target.getName(), "§a点击左键打开菜单  §c按Shift键退出");
                    player.setGameMode(GameMode.SPECTATOR);
                    player.setSpectatorTarget(target);
                }
            }
        }
    }

    public void tp() {
        if (check()) {
            return;
        }

        if (SpectatorSettings.get(gamePlayer).getOption(SpectatorSettings.Option.FIRSTPERSON)) {
            player.teleport(target);
            gamePlayer.sendTitle(0, 20, 0, "§a正在旁观§7" + target.getName(), "§a点击左键打开菜单  §c按Shift键退出");
            player.setGameMode(GameMode.SPECTATOR);
            player.setSpectatorTarget(target);
            return;
        }

        player.teleport(target);
    }

    public boolean check() {
        if (gameTarget == null || target == null) {
            return true;
        }

        if (gameTarget.isSpectator() || (!target.isOnline())) {
            gameTarget = null;
            target = null;
            return true;
        }

        return false;
    }

    private String formatSpectatorTip(Player player, Player target, boolean firstMode) {
        if (firstMode) {
            return "§f目标: §a§l" + target.getName() + "  §f生命值: §a§l" + ((int) target.getHealth()) + " §c§l❤";
        } else {
            String distance = df.format(player.getLocation().distance(target.getLocation()));
            return "§f目标: §a§l" + target.getName() + "  §f生命值: §a§l" + ((int) target.getHealth()) + "  §f距离: §a§l" + distance + "米";
        }
    }
}
