package cc.azuramc.bedwars.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GameEventRunnable {
    private int seconds;
    private int nextSeconds;
    private GameEventRunnable.Event event;

    public interface Event {
        void run(int seconds, int currentEvent);
    }
}

