package cc.azuramc.bedwars.config.object;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class PlayerConfig {

    private GamePlayer gamePlayer = new GamePlayer();

    @Data
    public static class GamePlayer {
        private int maxHealth = 20;
    }
}
