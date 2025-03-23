package cc.azuramc.bedwars.game;

import lombok.Getter;

import java.util.UUID;

@Getter
public class GamePlayer {

    private final UUID uuid;
    private final String name;

    public GamePlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }
}
