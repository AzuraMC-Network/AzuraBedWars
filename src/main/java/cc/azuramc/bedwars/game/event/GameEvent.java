package cc.azuramc.bedwars.game.event;

import cc.azuramc.bedwars.game.Game;

public abstract class GameEvent {
    private final String name;
    private final int excuteSeconds;
    private final int priority;

    public GameEvent(String name, int excuteSeconds, int priority) {
        this.name = name;
        this.excuteSeconds = excuteSeconds;
        this.priority = priority;
    }

    public String getName() {
        return this.name;
    }

    public int getExcuteSeconds() {
        return this.excuteSeconds;
    }

    public int getPriority() {
        return this.priority;
    }

    public void excute(Game game) {
    }

    public void excuteRunnbale(Game game, int seconds) {
    }
}
