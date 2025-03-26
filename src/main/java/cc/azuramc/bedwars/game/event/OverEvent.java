package cc.azuramc.bedwars.game.event;

import cc.azuramc.bedwars.events.BedwarsGameOverEvent;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GameOverRunnable;
import org.bukkit.Bukkit;

public class OverEvent extends GameEvent {
    public OverEvent() {
        super("游戏结束", 600, 6);
    }

    public void excute(Game game) {
        game.getEventManager().setCurrentEvent(7);
        Bukkit.getPluginManager().callEvent(new BedwarsGameOverEvent(game.getWinner()));
        new GameOverRunnable(game);
    }
}
