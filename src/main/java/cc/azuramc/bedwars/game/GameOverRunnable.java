package cc.azuramc.bedwars.game;

import cc.azuramc.bedwars.utils.FireWorkUtil;
import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.listeners.ChunkListener;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class GameOverRunnable extends BukkitRunnable {
    private final String[] lead = new String[]{"§e§l击杀数第一名", "§6§l击杀数第二名", "§c§l击杀数第三名"};
    private final Game game;
    private int counter = 15;
    private boolean b = true;

    public GameOverRunnable(Game game) {
        this.game = game;
        this.runTaskTimer(AzuraBedWars.getInstance(), 0L, 20L);
    }

    public void run() {
        GameTeam winner = this.game.getWinner();
        if (this.counter > 0) {
            if (this.b) {
                this.game.getGameTeams().forEach(team -> {
                    boolean isWinner = winner != null && winner.getName().equals(team.getName());
                    if (isWinner) {
                        this.game.broadcastTeamTitle(team, 0, 40, 0, "§6§l获胜！", "§7你获得了最终的胜利");
                    } else {
                        this.game.broadcastTeamTitle(team, 0, 40, 0, "§c§l失败！", "§7你输掉了这场游戏");
                    }
                });

                StringBuilder winnerText = new StringBuilder("§7");
                if (winner != null) {
                    for (GamePlayer gamePlayer : winner.getGamePlayers()) {
                        winnerText.append(gamePlayer.getDisplayname()).append(", ");
                    }

                    winnerText = new StringBuilder(winnerText.substring(0, winnerText.length() - 2));
                } else {
                    winnerText.append("无");
                }


                List<String> messages = new ArrayList<>();
                messages.add("§a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                messages.add("§f                                              §l起床战争");
                messages.add(" ");
                messages.add("                                    §e胜利者 §7- " + winnerText);
                messages.add(" ");
                int i = 0;
                for (GamePlayer gamePlayer : GamePlayer.sortFinalKills()) {
                    if (i > 2) {
                        continue;
                    }

                    messages.add("                          " + this.lead[i] + " §7- " + gamePlayer.getDisplayname() + " - " + gamePlayer.getKills());
                    i++;
                }
                messages.add(" ");
                messages.add("§a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

                for (String line : messages) {
                    Bukkit.broadcastMessage(line);
                }

                if (winner != null) {
                    for (GamePlayer gamePlayer : winner.getAlivePlayers()) {
                        gamePlayer.getPlayerData().addWins();
                    }
                }

                this.b = false;
            }

            if (winner != null) {
                winner.getAlivePlayers().forEach(gamePlayer -> FireWorkUtil.spawnFireWork(gamePlayer.getPlayer().getLocation().add(0.0D, 2.0D, 0.0D), gamePlayer.getPlayer().getLocation().getWorld()));
            }
        }

        if (this.counter == 0) {
            ChunkListener.releaseForceLoadedChunks();

            cancel();

            //            Bukkit.getOnlinePlayers().forEach(player -> PBukkitChannelTask.createTask().sender(player).channel(BukkitChannel.getInst().getBukkitChannel()).command("BungeeHub", "send", player.getName(), "BWLobby").run());

            (new BukkitRunnable() {
                public void run() {
                    Bukkit.shutdown();
                }
            }).runTaskLater(AzuraBedWars.getInstance(), 40L);
        }

        --this.counter;
    }
}
