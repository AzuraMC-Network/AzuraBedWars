package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.*;

/**
 * @author ImCur_
 */
public class PlayerDeathReasonListener implements Listener {

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @Setter
    private static boolean isFinalKill = false;

    private static Map<EntityDamageEvent.DamageCause, List<String>> suicideBroadcastMap = new HashMap<>();
    private static Map<EntityDamageEvent.DamageCause, List<String>> killBroadcastMap = new HashMap<>();

    // 设置死亡播报表, 默认类型VOID必须填写
    static {
        // 自杀
        suicideBroadcastMap.put(EntityDamageEvent.DamageCause.VOID, Arrays.asList(
                "&b&l» %player %color坠入深渊 %tag",
                "&b&l» %player %color掉出了这个世界 %tag",
                "&b&l» %player %color迷失了自我 %tag",
                "&b&l» %player %color陨落了 %tag"
        ));
        suicideBroadcastMap.put(EntityDamageEvent.DamageCause.FALL, Arrays.asList(
                "&b&l» %player %color感受到了动能 %tag",
                "&b&l» %player %color没能摆脱地心引力 %tag",
                "&b&l» %player %color一跃而下, 可惜落地过猛 %tag"
        ));
        suicideBroadcastMap.put(EntityDamageEvent.DamageCause.FIRE, Arrays.asList(
                "&b&l» %player %color欲火焚身 %tag",
                "&b&l» %player %color被烤的酥脆 %tag"
        ));
        suicideBroadcastMap.put(EntityDamageEvent.DamageCause.FIRE_TICK, Arrays.asList(
                "&b&l» %player %color欲火焚身 %tag",
                "&b&l» %player %color被烤的酥脆 %tag"
        ));
        suicideBroadcastMap.put(EntityDamageEvent.DamageCause.LAVA, Arrays.asList(
                "&b&l» %player %color试图在岩浆中游泳 %tag",
                "&b&l» %player %color试图达成目标'热腾腾的' %tag"
        ));
        suicideBroadcastMap.put(EntityDamageEvent.DamageCause.PROJECTILE, Arrays.asList(
                "&b&l» %player %color被一击必杀 %tag"
        ));
        suicideBroadcastMap.put(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, Arrays.asList(
                "&b&l» %player %color在一团火球中消散了 %tag"
        ));
        suicideBroadcastMap.put(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION, Arrays.asList(
                "&b&l» %player %color在一团火球中消散了 %tag"
        ));

        // 击杀
        killBroadcastMap.put(EntityDamageEvent.DamageCause.VOID, Arrays.asList(
                "&b&l» %player %color被 %killer %color驱逐出了这个世界 %tag",
                "&b&l» %player %color在逃离 %killer %color时不慎坠入深渊 %tag",
                "&b&l» %player %color被 %killer %color扔下了虚空 %tag"
        ));
        killBroadcastMap.put(EntityDamageEvent.DamageCause.ENTITY_ATTACK, Arrays.asList(
                "&b&l» %killer %color击杀了 %player %tag",
                "&b&l» %killer %color超度了 %player %color的灵魂 %tag",
                "&b&l» %player %color没能逃脱 %killer %color的利爪 %tag",
                "&b&l» %player %color被 %killer %color写入了死亡笔记 %tag"
        ));
        killBroadcastMap.put(EntityDamageEvent.DamageCause.FALL, Arrays.asList(
                "&b&l» %player %color在逃离 %killer %color时落地过猛 %tag"
        ));
        killBroadcastMap.put(EntityDamageEvent.DamageCause.FIRE, Arrays.asList(
                "&b&l» %player %color在逃离 %killer %color时欲火焚身 %tag",
                "&b&l» %player %color在与 %killer %color战斗中被烤的酥脆 %tag"
        ));
        killBroadcastMap.put(EntityDamageEvent.DamageCause.FIRE_TICK, Arrays.asList(
                "&b&l» %player %color在逃离 %killer %color时欲火焚身 %tag",
                "&b&l» %player %color在与 %killer %color战斗中被烤的酥脆 %tag"
        ));
        killBroadcastMap.put(EntityDamageEvent.DamageCause.LAVA, Arrays.asList(
                "&b&l» %player %color在逃离 %killer %color时试图潜入岩浆 %tag"
        ));
        killBroadcastMap.put(EntityDamageEvent.DamageCause.PROJECTILE, Arrays.asList(
                "&b&l» %player %color被 %killer %color一箭射杀 %tag"
        ));
        killBroadcastMap.put(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, Arrays.asList(
                "&b&l» %player %color在逃离 %killer %color时爆炸了 %tag"
        ));
        killBroadcastMap.put(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION, Arrays.asList(
                "&b&l» %player %color在逃离 %killer %color时爆炸了 %tag"
        ));
    }

    /**
     * 处理玩家死亡消息
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        // 移除原有死亡消息, 统一规划至此处
        event.setDeathMessage(null);

        Player player = event.getEntity();
        Player killer = player.getKiller();

        GamePlayer gamePlayer = GamePlayer.get(player);
        GamePlayer gameKiller = GamePlayer.get(killer);

        if (gamePlayer == null) {
            throw new RuntimeException("gamePlayer be null");
        }

        String finalKillStr = isFinalKill ? "&b&l[最终击杀]" : "";
        String mainColor = isFinalKill ? "&f" : "&7";
        String deadDisplayName =
                gamePlayer.getGameTeam().getChatColor()
                + "["
                + gamePlayer.getGameTeam().getName()
                + "] "
                + gamePlayer.getNickName();

        EntityDamageEvent.DamageCause cause =
                player.getLastDamageCause() != null
                        ?
                        (player.getLastDamageCause().isCancelled() ? EntityDamageEvent.DamageCause.VOID : player.getLastDamageCause().getCause())
                        :
                        EntityDamageEvent.DamageCause.VOID;


        if (gameKiller == null) {
            gameManager.broadcastMessage(getRandomBroadcast(cause, null, deadDisplayName, mainColor, finalKillStr));
        }
        else {
            int killerHeart = (int) (killer.getHealth() + 0.5);
            String killerDisplayName =
                    gameKiller.getGameTeam().getChatColor()
                            + "[" + gameKiller.getGameTeam().getName() + "] "
                            + gameKiller.getNickName()
                            + "&c " + killerHeart + "❤&r";
            gameManager.broadcastMessage(getRandomBroadcast(cause, killerDisplayName, deadDisplayName, mainColor, finalKillStr));
        }

        // 使用后必须重置标签
        isFinalKill = false;
    }

    private String getRandomBroadcast(EntityDamageEvent.DamageCause cause, String killerDisplayName, String deadDisplayName, String color, String finalKillStr) {
        String ret;
        if (killerDisplayName == null) {
            List<String> list = suicideBroadcastMap.get(cause);
            if (list == null)
                list = suicideBroadcastMap.get(EntityDamageEvent.DamageCause.VOID);
            ret = list.get(new Random().nextInt(list.size()));
        }
        else {
            List<String> list = killBroadcastMap.get(cause);
            if (list == null)
                list = killBroadcastMap.get(EntityDamageEvent.DamageCause.VOID);
            ret = list.get(new Random().nextInt(list.size()));
        }
        return ret.replace("%player", deadDisplayName).replace("%killer", String.valueOf(killerDisplayName)).replace("%color", color).replace("%tag", finalKillStr);
    }

}
