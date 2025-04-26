package cc.azuramc.bedwars.event;

import cc.azuramc.bedwars.game.GameManager;
import lombok.Getter;

@Getter
public abstract class AbstractGameEvent {
    private final String name;
    private final int executeSeconds;
    private final int priority;

    public AbstractGameEvent(String name, int executeSeconds, int priority) {
        this.name = name;
        this.executeSeconds = executeSeconds;
        this.priority = priority;
    }

    public void execute(GameManager gameManager) {
    }

    public void executeRunnable(GameManager gameManager, int seconds) {
    }
}
