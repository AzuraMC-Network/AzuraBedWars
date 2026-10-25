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
        switch (num) {
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            case 5:
                return "V";
            case 6:
                return "VI";
            case 7:
                return "VII";
            case 8:
                return "VIII";
            case 9:
                return "IX";
            case 10:
                return "X";
            default:
                return String.valueOf(num);
        }
    }

}
