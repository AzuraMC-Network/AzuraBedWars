package cc.azuramc.bedwars.game.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Runnable {
    private int seconds;
    private int nextSeconds;
    private Runnable.Event event;

    public interface Event {
        void run(int seconds, int currentEvent);
    }
}

