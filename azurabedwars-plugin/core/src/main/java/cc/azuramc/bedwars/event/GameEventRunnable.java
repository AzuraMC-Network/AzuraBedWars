package cc.azuramc.bedwars.event;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author an5w1r@163.com
 */
@Data
@AllArgsConstructor
public class GameEventRunnable {
    private int seconds;
    private int nextSeconds;
    private GameEventRunnable.Event event;

    public interface Event {
        /**
         * 游戏自定义用EventRunnable
         *
         * @param seconds           时间
         * @param currentEventLevel 当前事件等级
         */
        void run(int seconds, int currentEventLevel);
    }
}

