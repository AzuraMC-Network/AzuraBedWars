package cc.azuramc.bedwars.game.economy;

import cc.azuramc.bedwars.util.LoggerUtil;
import lombok.Getter;

import java.util.Locale;

/**
 * @author an5w1r@163.com
 */
@Getter
public enum EconomyType {
    ITEM(new ItemEconomy()),
    EXPERIENCE(new ExperienceEconomy());

    private final EconomyMode mode;

    EconomyType(EconomyMode mode) {
        this.mode = mode;
    }

    public static EconomyType fromConfig(String name) {
        try {
            return valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            LoggerUtil.warn("未知的资源模式 " + name + "，已改用 ITEM");
            return ITEM;
        }
    }
}
