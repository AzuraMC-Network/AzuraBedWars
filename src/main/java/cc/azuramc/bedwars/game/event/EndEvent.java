package cc.azuramc.bedwars.game.event;

import cc.azuramc.bedwars.events.BedwarsGameEndEvent;
import cc.azuramc.bedwars.game.Game;
import org.bukkit.Bukkit;

public class EndEvent extends GameEvent {
    public EndEvent() {
        super("游戏结束！", 30, 7);
    }

    public void excute(Game game) {
        Bukkit.getPluginManager().callEvent(new BedwarsGameEndEvent());
        Bukkit.shutdown();
    }
}
