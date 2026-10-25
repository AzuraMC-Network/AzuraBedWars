package cc.azuramc.bedwars.game.economy;

import lombok.Getter;

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
}
