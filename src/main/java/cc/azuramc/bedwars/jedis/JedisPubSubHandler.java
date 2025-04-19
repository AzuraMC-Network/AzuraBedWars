package cc.azuramc.bedwars.jedis;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.jedis.event.BukkitPubSubMessageEvent;
import redis.clients.jedis.JedisPubSub;

public class JedisPubSubHandler extends JedisPubSub {

    @Override
    public void onMessage(final String s, final String s2) {
        if (s2.trim().isEmpty()) {
            return;
        }

        AzuraBedWars.getInstance().callEvent(new BukkitPubSubMessageEvent(s, s2));
    }
}