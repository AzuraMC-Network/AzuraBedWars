package cc.azuramc.bedwars.event;

import cc.azuramc.bedwars.game.GameManager;
import lombok.Getter;

/**
 * @author an5w1r@163.com
 */
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
