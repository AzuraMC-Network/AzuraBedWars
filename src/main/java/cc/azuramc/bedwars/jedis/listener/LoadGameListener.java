package cc.azuramc.bedwars.jedis.listener;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.jedis.event.BukkitPubSubMessageEvent;
import cc.azuramc.bedwars.jedis.util.IPUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class LoadGameListener implements Listener {

    private boolean isLoad;

    @EventHandler
    public void onMessage(BukkitPubSubMessageEvent event) {
        if (event.getChannel().equals("AZURA.BW." + IPUtil.getLocalIp())) {
            if (isLoad) return;

            isLoad = true;
            AzuraBedWars.getInstance().mainThreadRunnable(() -> {
                MapData mapData = AzuraBedWars.getInstance().getMapManager().getAndLoadMapData(event.getMessage());
                mapData.setName(event.getMessage());
                AzuraBedWars.getInstance().getGameManager().loadGame(mapData);
            });
        }
    }
}
