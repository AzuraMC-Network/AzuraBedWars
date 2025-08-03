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

    public abstract void execute(GameManager gameManager);

    public void executeRunnable(GameManager gameManager, int seconds) {
    }

    /**
     * @return 罗马数字表示形式
     */
    protected static String toRoman(int num) {
        return switch (num) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(num);
        };
    }

}
