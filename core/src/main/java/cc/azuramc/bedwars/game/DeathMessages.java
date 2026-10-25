package cc.azuramc.bedwars.game;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author ImCur_
 */
public class DeathMessages {

    private static final Random RANDOM = new Random();
    private static final String QUIT = "&b&l» %player %color断开了连接 %tag";
    private static final String QUIT_IN_COMBAT = "&b&l» %player %color在逃离 %killer %color时断开了连接 %tag";
    /** Every table must have a VOID entry as the fallback */
    private static final Map<DamageCause, List<String>> suicideBroadcastMap = new EnumMap<>(DamageCause.class);
    private static final Map<DamageCause, List<String>> killBroadcastMap = new EnumMap<>(DamageCause.class);

    static {
        suicideBroadcastMap.put(DamageCause.VOID, Arrays.asList(
                "&b&l» %player %color坠入深渊 %tag",
                "&b&l» %player %color掉出了这个世界 %tag",
                "&b&l» %player %color迷失了自我 %tag",
                "&b&l» %player %color陨落了 %tag"
        ));
        suicideBroadcastMap.put(DamageCause.FALL, Arrays.asList(
                "&b&l» %player %color感受到了动能 %tag",
                "&b&l» %player %color没能摆脱地心引力 %tag",
                "&b&l» %player %color一跃而下, 可惜落地过猛 %tag"
        ));
        suicideBroadcastMap.put(DamageCause.FIRE, Arrays.asList(
                "&b&l» %player %color欲火焚身 %tag",
                "&b&l» %player %color被烤的酥脆 %tag"
        ));
        suicideBroadcastMap.put(DamageCause.FIRE_TICK, Arrays.asList(
                "&b&l» %player %color欲火焚身 %tag",
                "&b&l» %player %color被烤的酥脆 %tag"
        ));
        suicideBroadcastMap.put(DamageCause.LAVA, Arrays.asList(
                "&b&l» %player %color试图在岩浆中游泳 %tag",
                "&b&l» %player %color试图达成目标'热腾腾的' %tag"
        ));
        suicideBroadcastMap.put(DamageCause.PROJECTILE, Arrays.asList(
                "&b&l» %player %color被一击必杀 %tag"
        ));
        suicideBroadcastMap.put(DamageCause.ENTITY_EXPLOSION, Arrays.asList(
                "&b&l» %player %color在一团火球中消散了 %tag"
        ));
        suicideBroadcastMap.put(DamageCause.BLOCK_EXPLOSION, Arrays.asList(
                "&b&l» %player %color在一团火球中消散了 %tag"
        ));

        killBroadcastMap.put(DamageCause.VOID, Arrays.asList(
                "&b&l» %player %color被 %killer %color驱逐出了这个世界 %tag",
                "&b&l» %player %color在逃离 %killer %color时不慎坠入深渊 %tag",
                "&b&l» %player %color被 %killer %color扔下了虚空 %tag"
        ));
        killBroadcastMap.put(DamageCause.ENTITY_ATTACK, Arrays.asList(
                "&b&l» %killer %color击杀了 %player %tag",
                "&b&l» %killer %color超度了 %player %color的灵魂 %tag",
                "&b&l» %player %color没能逃脱 %killer %color的利爪 %tag",
                "&b&l» %player %color被 %killer %color写入了死亡笔记 %tag"
        ));
        killBroadcastMap.put(DamageCause.FALL, Arrays.asList(
                "&b&l» %player %color在逃离 %killer %color时落地过猛 %tag"
        ));
        killBroadcastMap.put(DamageCause.FIRE, Arrays.asList(
                "&b&l» %player %color在逃离 %killer %color时欲火焚身 %tag",
                "&b&l» %player %color在与 %killer %color战斗中被烤的酥脆 %tag"
        ));
        killBroadcastMap.put(DamageCause.FIRE_TICK, Arrays.asList(
                "&b&l» %player %color在逃离 %killer %color时欲火焚身 %tag",
                "&b&l» %player %color在与 %killer %color战斗中被烤的酥脆 %tag"
        ));
        killBroadcastMap.put(DamageCause.LAVA, Arrays.asList(
                "&b&l» %player %color在逃离 %killer %color时试图潜入岩浆 %tag"
        ));
        killBroadcastMap.put(DamageCause.PROJECTILE, Arrays.asList(
                "&b&l» %player %color被 %killer %color一箭射杀 %tag"
        ));
        killBroadcastMap.put(DamageCause.ENTITY_EXPLOSION, Arrays.asList(
                "&b&l» %player %color在逃离 %killer %color时爆炸了 %tag"
        ));
        killBroadcastMap.put(DamageCause.BLOCK_EXPLOSION, Arrays.asList(
                "&b&l» %player %color在逃离 %killer %color时爆炸了 %tag"
        ));
    }

    /**
     * @param cause null when the player quit
     */
    public static void broadcast(GameManager gameManager, GamePlayer victim, @Nullable GamePlayer killer,
                                 boolean finalKill, @Nullable DamageCause cause) {
        String template;
        if (cause == null) {
            template = killer == null ? QUIT : QUIT_IN_COMBAT;
        } else {
            Map<DamageCause, List<String>> table = killer == null ? suicideBroadcastMap : killBroadcastMap;
            List<String> templates = table.containsKey(cause) ? table.get(cause) : table.get(DamageCause.VOID);
            template = templates.get(RANDOM.nextInt(templates.size()));
        }

        String message = template
                .replace("%player", displayName(victim))
                .replace("%color", finalKill ? "&f" : "&7")
                .replace("%tag", finalKill ? "&b&l[最终击杀]" : "");
        if (killer != null) {
            message = message.replace("%killer", displayName(killer) + "&c " + Math.round((float) killer.getPlayer().getHealth()) + "❤&r");
        }
        gameManager.broadcastMessage(message);
    }

    private static String displayName(GamePlayer gamePlayer) {
        GameTeam team = gamePlayer.getGameTeam();
        return team == null ? gamePlayer.getNickName() : team.getChatColor() + "[" + team.getName() + "] " + gamePlayer.getNickName();
    }
}
