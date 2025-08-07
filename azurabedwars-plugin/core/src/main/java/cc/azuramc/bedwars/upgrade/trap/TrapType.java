package cc.azuramc.bedwars.upgrade.trap;

import lombok.Getter;

/**
 * @author An5w1r@163.com
 */
@Getter
public enum TrapType {

    BLINDNESS("致盲陷阱"),
    FIGHT_BACK("反击陷阱"),
    ALARM("警报陷阱"),
    MINER("挖掘疲劳陷阱");

    private final String displayName;

    TrapType(String displayName) {
        this.displayName = displayName;
    }

}
