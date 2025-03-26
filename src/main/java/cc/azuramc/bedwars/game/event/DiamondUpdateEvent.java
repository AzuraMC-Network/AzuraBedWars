package cc.azuramc.bedwars.game.event;

import cc.azuramc.bedwars.game.Game;

public class DiamondUpdateEvent extends GameEvent {
    private final int level;

    public DiamondUpdateEvent(int level, int second, int priority) {
        super("钻石资源点升级到" + level + "级", second, priority);

        this.level = level;
    }

    @Override
    public void excute(Game game) {
        Runnable runnable = game.getEventManager().getRunnables().get("钻石刷新");
        if (level == 2) {
            if (runnable.getNextSeconds() > 23) {
                runnable.setNextSeconds(23);
            }
            runnable.setSeconds(23);
        } else if (level == 3) {
            if (runnable.getNextSeconds() > 15) {
                runnable.setNextSeconds(15);
            }
            runnable.setSeconds(15);
        }
    }
}
