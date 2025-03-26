package cc.azuramc.bedwars.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssistsMap {
    private final Map<GamePlayer, Long> lastDamage = new HashMap<>();

    public AssistsMap(GamePlayer gamePlayer) {
    }

    public void setLastDamage(GamePlayer damager, long time) {
        lastDamage.put(damager, time);
    }

    public List<GamePlayer> getAssists(long time) {
        List<GamePlayer> players = new ArrayList<>();
        for (Map.Entry<GamePlayer, Long> entry : lastDamage.entrySet()) {
            if (time - entry.getValue() <= 10000L) {
                players.add(entry.getKey());
            }
        }
        return players;
    }
}
