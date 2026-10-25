package cc.azuramc.bedwars.game.timeline;

import lombok.Value;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntConsumer;

/**
 * @author an5w1r@163.com
 */
@Value
public class TimelineEvent {
    String name;
    int seconds;
    /** Called every second with the seconds left */
    @Nullable
    IntConsumer countdown;
    Runnable action;

    public TimelineEvent(String name, int seconds, Runnable action) {
        this(name, seconds, null, action);
    }

    public TimelineEvent(String name, int seconds, @Nullable IntConsumer countdown, Runnable action) {
        this.name = name;
        this.seconds = seconds;
        this.countdown = countdown;
        this.action = action;
    }
}
