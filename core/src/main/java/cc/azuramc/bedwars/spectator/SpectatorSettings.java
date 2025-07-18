package cc.azuramc.bedwars.spectator;

import cc.azuramc.bedwars.game.GamePlayer;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 旁观者设置管理类
 * 管理旁观者的各种设置，包括速度、自动传送、夜视等
 *
 * @author an5w1r@163.com
 */
public class SpectatorSettings {

    private static final Map<GamePlayer, SpectatorSettings> SPECTATOR_SETTINGS_HASH_MAP = new HashMap<>();

    private final GamePlayer gamePlayer;
    @Getter private int speed;
    private boolean autoTp;
    private boolean nightVision;
    private boolean firstPerson;
    private boolean hideOther;
    private boolean fly;

    /**
     * 构造函数
     * 
     * @param gamePlayer 游戏玩家
     */
    public SpectatorSettings(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
        this.speed = 0;
        this.autoTp = false;
        this.nightVision = false;
        this.firstPerson = true;
        this.hideOther = false;
        this.fly = false;
    }

    /**
     * 获取玩家的旁观者设置
     * 
     * @param player 游戏玩家
     * @return 旁观者设置
     */
    public static SpectatorSettings get(GamePlayer player) {
        return SPECTATOR_SETTINGS_HASH_MAP.computeIfAbsent(player, SpectatorSettings::new);
    }

    /**
     * 设置速度等级
     * 
     * @param level 速度等级
     */
    public void setSpeed(int level) {
        if (level < 0 || level > 4) {
            return;
        }
        speed = level;
    }

    /**
     * 获取选项状态
     * 
     * @param option 选项
     * @return 是否启用
     */
    public boolean getOption(Option option) {
        return switch (option) {
            case AUTO_TP -> autoTp;
            case NIGHT_VISION -> nightVision;
            case FIRST_PERSON -> firstPerson;
            case HIDE_OTHER -> hideOther;
            case FLY -> fly;
        };
    }

    /**
     * 设置选项状态
     *
     * @param option 选项
     * @param value 新值
     */
    public void setOption(Option option, boolean value) {
        if (getOption(option) == value) {
            return;
        }

        switch (option) {
            case AUTO_TP:
                autoTp = value;
                break;
            case NIGHT_VISION:
                nightVision = value;
                break;
            case FIRST_PERSON:
                firstPerson = value;
                break;
            case HIDE_OTHER:
                hideOther = value;
                break;
            case FLY:
                fly = value;
                break;
            default: break;
        }
    }


    /**
     * 获取游戏玩家
     * 
     * @return 游戏玩家
     */
    public GamePlayer getPlayer() {
        return gamePlayer;
    }

    /**
     * 旁观者选项枚举
     */
    public enum Option {
        /** 自动传送至跟随的玩家 */
        AUTO_TP,
        NIGHT_VISION,
        FIRST_PERSON,
        HIDE_OTHER,
        FLY
    }
}
