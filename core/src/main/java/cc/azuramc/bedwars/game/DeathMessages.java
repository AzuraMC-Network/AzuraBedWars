package cc.azuramc.bedwars.game;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.LangConfig;
import cc.azuramc.bedwars.util.MessageUtil;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author ImCur_
 */
public class DeathMessages {

    private static final Random RANDOM = new Random();

    /**
     * @param cause null when the player quit
     */
    public static void broadcast(GameManager gameManager, GamePlayer victim, @Nullable GamePlayer killer,
                                 boolean finalKill, @Nullable DamageCause cause) {
        LangConfig.Death lang = AzuraBedWars.getInstance().getLang().getDeath();
        String template;
        if (cause == null) {
            template = killer == null ? lang.getQuit() : lang.getQuitInCombat();
        } else {
            Map<String, List<String>> table = killer == null ? lang.getSelf() : lang.getKill();
            List<String> templates = table.containsKey(cause.name()) ? table.get(cause.name()) : table.get("VOID");
            template = templates == null || templates.isEmpty() ? lang.getQuit() : templates.get(RANDOM.nextInt(templates.size()));
        }

        String message = MessageUtil.fill(template, "player", displayName(victim),
                "color", finalKill ? "§f" : "§7", "final", finalKill ? lang.getFinalKill() : "");
        if (killer != null) {
            message = MessageUtil.fill(message, "killer", displayName(killer) + "§c " + Math.round((float) killer.getPlayer().getHealth()) + "❤§r");
        }
        gameManager.broadcastMessage(message);
    }

    private static String displayName(GamePlayer gamePlayer) {
        GameTeam team = gamePlayer.getGameTeam();
        return team == null ? gamePlayer.getNickName() : team.getChatColor() + "[" + team.getName() + "] " + gamePlayer.getNickName();
    }
}
