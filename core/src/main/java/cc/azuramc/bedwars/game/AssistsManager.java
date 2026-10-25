package cc.azuramc.bedwars.game;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Enemies who hurt a player recently, used to credit kills and assists
 *
 * @author an5w1r@163.com
 */
public class AssistsManager {

    private static final long ASSIST_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(10);

    private final Map<GamePlayer, Long> lastDamage = new HashMap<>();
    private final GamePlayer targetPlayer;

    public AssistsManager(GamePlayer gamePlayer) {
        this.targetPlayer = gamePlayer;
    }

    public void setLastDamage(GamePlayer attacker, long time) {
        if (attacker == null || attacker.equals(targetPlayer)) {
            return;
        }
        lastDamage.put(attacker, time);
    }

    @Nullable
    public GamePlayer getLastDamager(long now) {
        GamePlayer latest = null;
        long latestTime = Long.MIN_VALUE;
        for (Map.Entry<GamePlayer, Long> entry : lastDamage.entrySet()) {
            if (now - entry.getValue() <= ASSIST_TIMEOUT_MS && entry.getValue() > latestTime) {
                latest = entry.getKey();
                latestTime = entry.getValue();
            }
        }
        return latest;
    }

    public List<GamePlayer> getAssists(long now) {
        List<GamePlayer> players = new ArrayList<>();
        for (Map.Entry<GamePlayer, Long> entry : lastDamage.entrySet()) {
            if (now - entry.getValue() <= ASSIST_TIMEOUT_MS) {
                players.add(entry.getKey());
            }
        }
        return players;
    }

    public void clear() {
        lastDamage.clear();
    }
}
