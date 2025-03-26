package cc.azuramc.bedwars.game.event;

import cc.azuramc.bedwars.game.Game;

public class EmeraldUpdateEvent extends GameEvent {
    private final int level;

    public EmeraldUpdateEvent(int level, int second, int priority) {
        super("绿宝石升级到" + level + "级", second, priority);

        this.level = level;
    }

    @Override
    public void excute(Game game) {
        Runnable runnable = game.getEventManager().getRunnables().get("绿宝石刷新");
        if (level == 2) {
            if (runnable.getNextSeconds() > 40) {
                runnable.setNextSeconds(40);
            }
            runnable.setSeconds(40);
        } else if (level == 3) {
            if (runnable.getNextSeconds() > 35) {
                runnable.setNextSeconds(35);
            }
            runnable.setSeconds(35);
        }
    }
}
